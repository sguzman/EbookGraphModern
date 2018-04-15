package com.github.sguzman.ebook.graph

import cats.effect.IO
import com.github.sguzman.ebook.graph.io.Sync
import com.github.sguzman.ebook.graph.sql.Links
import com.github.sguzman.ebook.graph.wrap.DocWrap._
import com.github.sguzman.ebook.graph.wrap.StrWrap._

object Main {
  def main(args: Array[String]): Unit = {
    val _ = Sync {
      val pages = 1 to 623

      val links = Cache.flatMap(pages.par) {body =>
        val doc = body.doc
        val links = "div.thumbnail > a[href]"
        doc.flatMap(links).map(_.attr("href"))
      } (a => s"https://www.foxebook.net/page/$a/?sort=default")

      val insertIfAbsent = IO {
        val incumbent = Links.get
        val diff = links.toSet.diff(incumbent).toSeq
        Links.insert(diff)
      }

      insertIfAbsent.unsafeRunSync()
    }
  }
}
