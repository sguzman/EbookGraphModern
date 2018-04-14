package com.github.sguzman.ebook.graph

import slick.jdbc.PostgresProfile.api._

final case class Links(tag: Tag) extends Table[(Long, String)](tag, "links") {
  def id = column[Long]("id", O.Unique, O.PrimaryKey, O.AutoInc)
  def link = column[String]("link", O.Length(100), O.Unique)

  def * = (id, link)
}
