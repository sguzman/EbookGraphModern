package com.github.sguzman.ebook.graph.io

import scala.util.{Failure, Success}

case class Sync[A](a: A) {
  def ~[B](f: A => B): Sync[B] = Sync(f(a))

  def apply[B](b: => B): Sync[B] = util.Try(a) match {
    case Success(_) => Sync(b)
    case Failure(e) => throw e
  }
}
