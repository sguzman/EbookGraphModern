package com.github.sguzman.ebook.graph

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.collection.parallel.ParSeq
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object IOUtil {
  object Async {
    def apply: Async = new Async
  }
  class Async(work: ParSeq[(Future[Unit], Throwable => Unit)] = ParSeq.empty) {
    def ~ : Async = new Async
    def sync(): Unit = {
      val errors =
        work.map(a => util.Try(Await.result(a._1, Duration.Inf)) match {
          case Success(_) => Right(())
          case Failure(e) => Left((a._2, e))
        })

      errors.filter({
        case Right(_) => false
        case Left(_) => true
      }).map(_.left.get).foreach(a => Future(a._1(a._2)))
    }

    def onError(e: Throwable): Unit = {
      println(e.getMessage)
      e.printStackTrace()
    }

    def apply(a: => Unit, handle: Throwable => Unit = onError): Async =
      new Async(work :+ (Future(a), handle))
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
