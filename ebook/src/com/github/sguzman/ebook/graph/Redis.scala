package com.github.sguzman.ebook.graph

import java.net.SocketTimeoutException

import com.github.sguzman.brotli.Brotli
import com.redis.RedisClient
import scalaj.http.Http

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Redis {
  lazy val redis = new RedisClient("localhost", 6379)

  def http(url: String): String = util.Try(Http(url).asString) match {
    case Success(v) => v.body
    case Failure(e) => e match {
      case _: SocketTimeoutException => http(url)
      case _ => throw e
    }
  }

  def cache(ns: String, url: String, redis: RedisClient = redis) =
    redis.get[Array[Byte]](s"$ns:$url") match {
      case None =>
        val body = http(url)
        Future(redis.set(s"$ns:$url", Brotli.compress(body)))
        body
      case Some(v) => Brotli.decompress(v)
}
