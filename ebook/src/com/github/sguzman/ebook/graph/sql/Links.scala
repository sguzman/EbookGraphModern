package com.github.sguzman.ebook.graph.sql

import com.github.sguzman.ebook.graph.wrap.FutureWrap._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable

import scala.collection.parallel.ParSeq
import scala.concurrent.ExecutionContext.Implicits.global

object Links {
  private final case class Links(tag: Tag) extends Table[(Long, String)](tag, "links") {
    def id = column[Long]("id", O.Unique, O.PrimaryKey, O.AutoInc)
    def link = column[String]("link", O.Length(100), O.Unique)

    def * = (id, link)
  }

  private lazy val linkTable = identity {
    val table = TableQuery[Links]
    val created = Util.db.run(MTable.getTables)
      .map(_.exists(_.name.name == "links"))
      .map({cond =>
        if (!cond) {
            println("Creating SChema for links")
            Util.db.run(DBIO.seq(table.schema.create))
          }
        else ()
      })

    created.v
    table
  }

  def insert(col: Seq[String]): Unit = {
    val _: Unit = Util.db.run(DBIO.sequence(col.map(a => linkTable.insertOrUpdate(0L, a)))).v
  }

  def insert(col: ParSeq[String]): Unit = {
    val _: Unit = Util.db.run(DBIO.sequence(col.toIterator.map(a => linkTable.insertOrUpdate(0L, a)))).v
  }
}