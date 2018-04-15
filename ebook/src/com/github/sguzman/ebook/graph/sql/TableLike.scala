package com.github.sguzman.ebook.graph.sql

import com.github.sguzman.ebook.graph.sql.Links.{linkTable, name}
import slick.lifted.TableQuery

import scala.collection.parallel.ParSeq

trait TableLike[A] {

  def insert(col: Seq[String], table: TableQuery[A]): Unit

  def insert(col: ParSeq[String], table: TableQuery[A]): Unit

  def get(table: TableQuery[A]): Unit
}
