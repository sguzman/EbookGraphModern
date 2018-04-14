package com.github.sguzman.ebook.graph

import slick.jdbc.PostgresProfile.api._
import com.github.sguzman.ebook.graph.DocWrap._
import com.github.sguzman.ebook.graph.StrWrap._

object Main {
  def main(args: Array[String]): Unit = {
    val ns = "ebooks"
    val cache = Redis.cache(ns, _: String)

    val linkTable = TableQuery[Links]
    println(linkTable)

    val pages = 1 until 1256
    val links = pages.par.flatMap{a =>
      val url = s"https://it-eb.com/page/$a/"
      val body = cache(url)
      val doc = body.doc

      doc.flatMap("article.post > div.post-inner > div.post-content > div.post-header > h2.post-title > a[href]")
        .map(_.attr("href"))
    }
  }
}
