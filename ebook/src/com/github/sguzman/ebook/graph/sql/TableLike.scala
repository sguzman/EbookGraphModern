package com.github.sguzman.ebook.graph.sql

import scala.collection.parallel.ParSeq
import scala.collection.parallel.immutable.ParSet

trait TableLike[A] {
  def insert(col: ParSet[String]): Unit
  def get: ParSet[String]
}
