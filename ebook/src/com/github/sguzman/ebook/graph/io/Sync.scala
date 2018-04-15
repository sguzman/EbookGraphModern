package com.github.sguzman.ebook.graph.io

import scala.util.{Failure, Success}

case class Sync[A](a: A) {
  def ~[B](f: A => B): Sync[B] = util.Try(f(a)) match {
    case Success(b) => Sync(b)
    case Failure(e) => throw e
  }
}
