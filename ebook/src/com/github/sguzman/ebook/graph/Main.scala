package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.io.Sync
import com.github.sguzman.ebook.graph.sql.Links
import com.github.sguzman.ebook.graph.wrap.DocWrap._
import com.github.sguzman.ebook.graph.wrap.StrWrap._

import scala.language.reflectiveCalls

object Main {
  def main(args: Array[String]): Unit = {
    Sync {
      val pages = 1 to 623

      Cache.get.flatMap(pages.par, Links.table) { body =>
        val doc = body.doc
        val links = "div.thumbnail > a[href]"
        doc.flatMap(links).map(_.attr("href"))
      } (a => s"https://www.foxebook.net/page/$a/?sort=default")
    } ~ {links =>

    }
  }
}
