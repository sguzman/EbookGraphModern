package com.github.sguzman.ebook.graph.sql

import com.github.sguzman.ebook.graph.wrap.FutureWrap._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable
import scala.concurrent.ExecutionContext.Implicits.global

final case class Links(tag: Tag) extends Table[(Long, String)](tag, "links") {
  def id = column[Long]("id", O.Unique, O.PrimaryKey, O.AutoInc)
  def link = column[String]("link", O.Length(100), O.Unique)

  def * = (id, link)
}

object Links {
  lazy val linkTable = identity {
    val table = TableQuery[Links]
    val created = Util.db.run(MTable.getTables)
      .map(_.exists(_.name.name == "links"))
      .filter(a => a)
      .map({_ =>
        println("Creating SChema for links")
        Util.db.run(DBIO.seq(table.schema.create))
      })

    created.v

    table
  }
}