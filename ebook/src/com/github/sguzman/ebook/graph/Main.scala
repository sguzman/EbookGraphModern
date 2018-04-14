package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.io.Sync
import com.github.sguzman.ebook.graph.sql.Links
import com.github.sguzman.ebook.graph.wrap.StrWrap._
import com.github.sguzman.ebook.graph.wrap.DocWrap._

object Main {
  def main(args: Array[String]): Unit = {
    val _ = Sync {
      val cache = Redis.cache("ebooks", _: String)
      val pages = 1 to 1259

      val links = pages.par.flatMap{a =>
        val url = s"https://it-eb.com/page/$a/"
        val doc = cache(url).doc
        val links = "article.post > div.post-inner > div.post-content > div.post-header > h2.post-title > a[href]"

        doc.flatMap(links).map(_.attr("href"))
      }

      Links.insert(links)
    }
  }
}
