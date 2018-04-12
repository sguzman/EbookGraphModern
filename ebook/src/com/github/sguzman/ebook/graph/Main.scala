package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.IO.{Async, Sync}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {
  def main(args: Array[String]): Unit = {
    lazy val db: PostgresProfile.backend.DatabaseDef = identity {
      Runtime.getRuntime.addShutdownHook(new Thread(() => {
        println("Closing pg")
        db.close()
      }))
      Database.forURL("jdbc:postgresql://localhost:5432/postgres", driver = "org.postgresql.Driver", user = "alice", password = "pass")
    }
    val _ = Sync {

      Async {
        println("begin")
      } ~ {

      }
    } ~ {
      println("done")
    }
  }
}
