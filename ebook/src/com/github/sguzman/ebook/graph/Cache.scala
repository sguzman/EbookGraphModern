package com.github.sguzman.ebook.graph

import java.net.SocketTimeoutException

import com.github.sguzman.brotli.Brotli
import com.redis._
import com.redis.serialization.Parse.Implicits._
import scalaj.http.Http

import scala.collection.concurrent.TrieMap
import scala.util.{Failure, Success}

object Cache {
  private val ns = "ebooks"

  private lazy val cachingService: (TrieMap[String, Array[Byte]], RedisClient) = identity {
    val redis = cachingService._2
    println("Init caching client...")
    val cache: TrieMap[String, Array[Byte]] = redis.hgetall1[String, Array[Byte]](ns) match {
      case None =>
        println("No Http cache found - starting from scratch")
        TrieMap()
      case Some(v) =>
        println(s"Found Http cache with ${v.size} entries")
        TrieMap[String, Array[Byte]](v.toStream: _*)
    }

    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      println("Writing cache...")
      redis.hmset(ns, cache)

      println("Closing caching client...")
      if (redis.disconnect)
        println("Disconnected successfully")
      else
        println("Could not disconnect")
    }))

    (cache, new RedisClient("localhost", 6379))
  }

  private def http(url: String): String = util.Try(Http(url).asString) match {
    case Success(v) => v.body
    case Failure(e) => e match {
      case _: SocketTimeoutException => http(url)
      case _ => throw e
    }
  }

  private def _get(cache: TrieMap[String, Array[Byte]], key: String, client: RedisClient): String =
    cache.get(key) match {
      case None =>
        println(s"Miss Http cache for key $key")
        val body = http(key)
        cache.put(key, Brotli.compress(body))
        body
      case Some(v) =>
        println(s"Hit Http cache for key $key")
        Brotli.decompress(v)
    }

  def get(key: String): String =
    _get(cachingService._1, key, cachingService._2)
}
