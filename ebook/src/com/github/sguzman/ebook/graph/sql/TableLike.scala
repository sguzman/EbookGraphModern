package com.github.sguzman.ebook.graph.sql

import scala.collection.parallel.immutable.ParSet

trait TableLike[A, B] {
  def insert(col: ParSet[B]): Unit
  def get: ParSet[B]
}
