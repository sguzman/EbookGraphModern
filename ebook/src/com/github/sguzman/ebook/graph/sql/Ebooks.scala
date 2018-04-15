package com.github.sguzman.ebook.graph.sql

import com.github.sguzman.ebook.graph.wrap.FutureWrap._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable
import slick.lifted.ProvenShape

import scala.collection.parallel.immutable.ParSet
import scala.concurrent.ExecutionContext.Implicits.global

object Ebooks {
  lazy val table = identity {
    val name = "ebookinfo"
    type Row = (Long, String, String, String, Int, String, String, String, String, String, String, Int, String, Double, String)

    final case class EbookInfo(tag: Tag) extends Table[Row](tag, name) {
      def id = column[Long]("id", O.Unique, O.PrimaryKey, O.AutoInc)
      def title = column[String]("title")
      def date = column[String]("date")
      def img = column[String]("img")
      def ebookId = column[Int]("ebook_id")
      def desc = column[String]("desc")
      def publisher = column[String]("publisher")
      def authors = column[String]("author")
      def pubDate = column[String]("pubDate")
      def isbn10 = column[String]("isbn10")
      def isbn13 = column[String]("isbn13")
      def pages = column[Int]("pages")
      def format = column[String]("format")
      def size = column[Double]("size")
      def sizeType = column[String]("sizeType")

      def * : ProvenShape[Row] = (id, title, date, img, ebookId, desc, publisher, authors, pubDate, isbn10, isbn13, pages, format, size, sizeType)
    }

    val table = TableQuery[EbookInfo]
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
    object Table extends TableLike[EbookInfo, Row] {
      override def insert(col: ParSet[Row]): Unit = {
        println(s"Inserting ${col.size} items into $name")
        val _ = Util.db.run(DBIO.sequence(col.toIterator.map(a => table.insertOrUpdate((0L, a._2, a._3 ,a._4, a._5, a._6, a._7, a._8, a._9, a._10, a._11, a._12, a._13, a._14, a._15))))).v
      }

      override def get: ParSet[Row] = Util.db.run(table.result.map(_.map(a => a))).v.toSet.par
    }

    Table
  }
}