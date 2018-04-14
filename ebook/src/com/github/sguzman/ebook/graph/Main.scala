package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.io.Sync
import com.github.sguzman.ebook.graph.wrap.DocWrap._
import com.github.sguzman.ebook.graph.wrap.StrWrap._

object Main {
  def main(args: Array[String]): Unit = {
    val _ = Sync {
      val pages = 1 to 623

      val links = pages.par.map{a =>
        val url = s"https://www.foxebook.net/page/$a/?sort=default"
        val body = Cache.get(url)
        val doc = body.doc
        val links = "div.thumbnail > a[href]"
        doc.flatMap(links).map(_.attr("href"))
      }

      links foreach println
    }
  }
}
