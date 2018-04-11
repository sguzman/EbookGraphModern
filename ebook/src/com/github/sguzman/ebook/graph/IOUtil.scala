package com.github.sguzman.ebook.graph

import cats.effect.IO

import scala.util.{Failure, Success}

object IOUtil {
  def putLine(msg: => String): IO[Unit] = IO {
    println(msg)
  }

  object Sync {
    def ~ : Sync.type = Sync

    def apply[A](a: => A, handle: Throwable => Unit = e => throw e): Sync.type = util.Try(a) match {
      case Success(_) => Sync
      case Failure(e) =>
        handle(e)
        Sync
    }
  }
}
