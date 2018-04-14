package com.github.sguzman.ebook.graph.sql

import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

object Util {
  lazy val db: PostgresProfile.backend.DatabaseDef = identity {
    println("Init sql connection...")

    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      println("Closing pg")
      db.close()
    }))
    Database.forURL("jdbc:postgresql://localhost:5432/ebooks", driver = "org.postgresql.Driver", user = "alice", password = "pass")
  }
}
