package com.github.sguzman.ebook.graph

import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

object SQL {
  lazy val db: PostgresProfile.backend.DatabaseDef = identity {
    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      println("Closing pg")
      db.close()
    }))
    Database.forURL("jdbc:postgresql://localhost:5432/postgres", driver = "org.postgresql.Driver", user = "alice", password = "pass")
  }
}
