package com.github.sguzman.ebook.graph

import java.net.SocketTimeoutException

import cats.effect.IO
import com.github.sguzman.brotli.Brotli
import com.github.sguzman.ebook.graph.sql.Links
import com.redis._
import com.redis.serialization.Parse.Implicits._
import scalaj.http.Http
import slick.lifted.TableQuery

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.collection.parallel.immutable.ParSeq
import scala.util.{Failure, Success}

object Cache {
  lazy val get  = identity {
    val ns: String = "ebooks:http"
    val redis: RedisClient = new RedisClient(host = "localhost", port = 6379)
    println("Init caching client...")
    val cache: Map[String, Array[Byte]] = redis.hgetall1[String, Array[Byte]](ns) match {
      case None =>
        println("No Http cache found - starting from scratch")
        Map()
      case Some(v) =>
        println(s"Found Http cache with ${v.size} entries")
        v
    }

    val shells: mutable.ListBuffer[ParSeq[(String, Array[Byte])]] = mutable.ListBuffer()
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      println(s"Writing ${shells.length} sessions worth of cache entries...")
      val toWrite = shells.map(_.toIndexedSeq).flatMap(a => a.map(a => IO{
        val key = a._1
        val value = a._2

        println(s"Inserting key $key into hash $ns with value of length ${value.length}")
        redis.hset(ns, a._1, a._2)
      }))

      println(s"${toWrite.length} new entries in total")

      val record = toWrite.map(_.unsafeRunSync())
      if (record.forall(a => a))
        println("All records written successfully")
      else
        println("Some records did not write correctly")

      println("Closing caching client...")
      if (redis.disconnect)
        println("Disconnected successfully")
      else
        println("Could not disconnect")
    }))

    object CacheMeOutside {
      val incumbent: Map[String, Array[Byte]] = cache
      val furtherWrites: ListBuffer[ParSeq[(String, Array[Byte])]] = shells
      val client: RedisClient = redis

      private def http(url: String): String = util.Try(Http(url).asString) match {
        case Success(v) => v.body
        case Failure(e) => e match {
          case _: SocketTimeoutException => http(url)
          case _ => throw e
        }
      }

      private def _get(cache: Map[String, Array[Byte]], key: String): (Option[String], String) =
        cache.get(key) match {
          case None =>
            println(s"Miss Http cache for key $key")
            val body: String = http(key)
            (Some(key), body)
          case Some(v) =>
            println(s"Hit Http cache for key $key")
            (None, Brotli.decompress(v))
        }

      private def get(key: String): (Option[String], String) =
        _get(incumbent, key)

      def flatMap[A, B, C](col: ParSeq[A], table: TableQuery[C])(body: String => Seq[B])(toUrl: A => String): ParSeq[B]  = {
        val results: ParSeq[(Option[String], String)] = col.map(toUrl).map(get)
        val (missing, htmlBody): (ParSeq[Option[String]], ParSeq[String]) = results.unzip
        val newValues: ParSeq[(String, String)] = missing
          .zipWithIndex
          .filter(_._1.isDefined)
          .map(a => (a._1.get, a._2))
          .map(a => (a._1, htmlBody(a._2)))

        val brotli: ParSeq[(String, Array[Byte])] = newValues.map(a => {
          val key = a._1
          println(s"Compressing html body for key $key")
          val body = Brotli.compress(a._2)
          (key, body)
        })

        println(s"${brotli.length} new http cache entries in this session")
        furtherWrites.append(brotli)

        val retVals: ParSeq[B] = htmlBody.flatMap(body)

        val insertIfAbsent = IO {
          val incumbent = Links.table
          val diff = retVals.toSet.diff(incumbent).toSeq
          Links.insert(diff)
        }

        retVals
      }
    }

    CacheMeOutside
  }
}
