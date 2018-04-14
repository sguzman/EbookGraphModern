package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.io.Sync
import com.github.sguzman.ebook.graph.wrap.DocWrap._
import com.github.sguzman.ebook.graph.wrap.StrWrap._

object Main {
  def main(args: Array[String]): Unit = {
    val _ = Sync {
      val pages = 1 to 1259

      val links = pages.par.map{a =>
        val url = s"https://it-eb.com/page/$a/"
        val body = Cache.get(url)
        val doc = body.doc
        val links = "article.post > div.post-inner > div.post-content > div.post-header > h2.post-title > a[href]"
        doc.flatMap(links).map(_.attr("href"))
      }

      links foreach println
    }
  }
}
