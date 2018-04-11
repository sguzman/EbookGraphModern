package com.github.sguzman.ebook.graph

import cats.effect.IO

import scala.util.{Failure, Success}

object IOUtil {
  def putLine(msg: => String): IO[Unit] = IO {
    println(msg)
  }

  object Pure {
    def apply[A](a: => A, handle: Throwable => Unit = e => throw e): Pure.type = util.Try(a) match {
      case Success(_) => Pure
      case Failure(e) =>
        handle(e)
        Pure
    }
  }
}
