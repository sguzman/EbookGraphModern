package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.IO.{Async, Sync}
import slick.jdbc.PostgresProfile.api._

object Main {
  def main(args: Array[String]): Unit = {
    val _ = Sync {

      lazy val db = Database.forURL("jdbc:postgresql://localhost:5432/ebooks", driver = "org.postgresql.Driver", user = "alice", password = "pass")
      Async {
        println("begin")
      } ~ {

      }
    } ~ {
      println("done")
    }
  }
}
