package com.github.sguzman.ebook.graph

import java.net.SocketTimeoutException

import com.github.sguzman.brotli.Brotli
import scalaj.http.Http
import shade.memcached.{Configuration, Memcached}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object Mem {
  private val ns = "ebooks"

  private lazy val memcached: Memcached = identity {
    println("Init memcached client...")
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      println("Closing memcached client...")
      memcached.close()
    }))

    Memcached(Configuration("127.0.0.1:11211"))
  }

  private def http(url: String): String = util.Try(Http(url).asString) match {
    case Success(v) => v.body
    case Failure(e) => e match {
      case _: SocketTimeoutException => http(url)
      case _ => throw e
    }
  }

  def cache(url: String, ns: String = ns, mem: Memcached = memcached): String =
    memcached.awaitGet[Array[Byte]](s"$ns:$url") match {
      case None =>
        println(s"Miss Http cache for key $url")
        val body = http(url)
        memcached.awaitSet(s"$ns:$url", Brotli.compress(body), Duration.Inf)
        body
      case Some(v) =>
        println(s"Hit Http cache for key $url")
        Brotli.decompress(v)
    }
}
