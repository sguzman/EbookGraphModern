package com.github.sguzman.ebook.graph

import scala.collection.parallel.ParSeq
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object Async {
  def onError(e: Throwable): Unit = {
    println(e.getMessage)
    e.printStackTrace()
  }

  def apply[A](a: => A, handle: Throwable => Unit = onError): Async = new Async().apply(a, handle)
}

final class Async(work: ParSeq[(Future[Unit], Throwable => Unit)] = ParSeq.empty) {
  def ~[A](a: => A, handle: Throwable => Unit = onError): Async = new Async().apply(a, handle)
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
    new Async(work :+ ((Future(a), handle)))
}
