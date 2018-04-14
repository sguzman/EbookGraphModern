package com.github.sguzman.ebook.graph

import java.net.SocketTimeoutException

import com.github.sguzman.brotli.Brotli
import com.redis.RedisClient
import scalaj.http.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object Cache {
  private val ns = "ebooks"

  private lazy val redis: RedisClient = identity {
    println("Init caching client...")
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      println("Closing caching client...")
      if (redis.disconnect)
        println("Disconnected succesfully")
      else
        println("Could not disconnect")
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

  private def get(url: String, ns: String = ns, client: RedisClient = redis): String =
    client.get[Array[Byte]](s"$ns:$url") match {
      case None =>
        println(s"Miss Http cache for key $url")
        val body = http(url)
        client.set(s"$ns:$url", Brotli.compress(body))
        body
      case Some(v) =>
        println(s"Hit Http cache for key $url")
        Brotli.decompress(v)
    }
}
