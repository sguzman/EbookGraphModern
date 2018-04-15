package com.github.sguzman.ebook.graph.sql

import com.github.sguzman.ebook.graph.wrap.FutureWrap._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable

import scala.collection.parallel.immutable.ParSet
import scala.concurrent.ExecutionContext.Implicits.global

object Links {
  lazy val table = identity {
    val name = "links"
    final case class Links(tag: Tag) extends Table[(Long, String)](tag, name) {
      def id = column[Long]("id", O.Unique, O.PrimaryKey, O.AutoInc)
      def link = column[String]("link", O.Length(200), O.Unique)

      def * = (id, link)
    }

    val table = TableQuery[Links]
    val created = Util.db.run(MTable.getTables)
      .map(_.exists(_.name.name == name))
      .map({cond =>
        if (!cond) {
            println("Creating SChema for links")
            Util.db.run(DBIO.seq(table.schema.create))
          }
        else ()
      })

    created.v
    object Table extends TableLike[Links, String] {
      override def insert(col: ParSet[String]): Unit = {
        println(s"Inserting ${col.size} items into $name")
        val _ = Util.db.run(DBIO.sequence(col.toIterator.map(a => table.insertOrUpdate((0L, a))))).v
      }

      override def get: ParSet[String] = Util.db.run(table.result.map(_.map(_._2))).v.toSet.par
    }

    Table
  }
}