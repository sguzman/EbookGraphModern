package com.github.sguzman.ebook.graph

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object IOUtil {
  object Async {
    val work: ListBuffer[(Future[Unit], Throwable => Unit)] =
      ListBuffer[(Future[Unit], Throwable => Unit)]()

    def ~ : Async.type = Async
    def sync(): Unit = {
      val errors =
        work.par.map(a => util.Try(Await.result(a._1, Duration.Inf)) match {
          case Success(_) => Right(())
          case Failure(e) => Left((a._2, e))
        })

      errors.filter({
        case Right(_) => false
        case Left(_) => true
      }).map(_.left.get).foreach(a => Future(a._1(a._2)))

      work.clear()
    }

    def onError(e: Throwable): Unit = {
      println(e.getMessage)
      e.printStackTrace()
    }

    def apply(a: => Unit, handle: Throwable => Unit = onError): Async.type = {
      work.append((Future(a), handle))
      Async
    }
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
