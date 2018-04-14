package com.github.sguzman.ebook.graph

import scala.util.{Failure, Success}

object Sync {
  def apply[A](a: => A, handle: Throwable => Unit = e => throw e): Sync = new Sync().apply(a, handle)
}

class Sync {
  def ~[A](a: => A, handle: Throwable => Unit = e => throw e): Sync = new Sync().apply(a, handle)

  def apply[A](a: => A, handle: Throwable => Unit = e => throw e): Sync = util.Try(a) match {
    case Success(_) => new Sync()
    case Failure(e) =>
      handle(e)
      new Sync()
  }
}
