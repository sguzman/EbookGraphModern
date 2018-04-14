package com.github.sguzman.ebook.graph

import java.net.SocketTimeoutException

import cats.effect.IO
import com.github.sguzman.brotli.Brotli
import com.redis.RedisClient
import com.redis.serialization.Parse.Implicits._
import scalaj.http.Http

import scala.util.{Failure, Success}

object Redis {
  lazy val redis: RedisClient = identity {
    println("Init Redis...")

    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      println("Disconnecting Redis...")
      if (redis.disconnect)
        println("Disconnect successful")
      else
        println("Bad disconnect")
    }))
    new RedisClient("localhost", 6379)
  }

  private def http(url: String): String = util.Try(Http(url).asString) match {
    case Success(v) => v.body
    case Failure(e) => e match {
      case _: SocketTimeoutException => http(url)
      case _ => throw e
    }
  }

  private def _setIfNew(key: String, body: Array[Byte], redis: RedisClient) =
    if (redis.exists(key))
      println(s"Redis key $key exists already")
    else {
      println(s"Setting Redis key $key")
      redis.set(key, body)
    }

  private def setIfNew(ns: String, url: String, body: String, redis: RedisClient = redis): Unit =
    _setIfNew(s"$ns:$url", Brotli.compress(body), redis)

  def cache(ns: String, url: String, redis: RedisClient = redis) =
    IO {
      redis.get[Array[Byte]](s"$ns:$url") match {
        case None =>
          println(s"Missed Http cache for $url")
          val body = http(url)
          (body, IO(setIfNew(ns, url, body)))
        case Some(v) =>
          println(s"Hit Http cache for $url")
          (Brotli.decompress(v), IO.unit)
      }
    }
}
