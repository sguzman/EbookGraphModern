package com.github.sguzman.ebook.graph

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object FutureWrap {
  implicit final class FutureWrap[A](future: Future[A]) {
    def v: A = Await.result(future, Duration.Inf)
  }
}
