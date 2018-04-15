package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.io.Sync
import com.github.sguzman.ebook.graph.sql.{Ebooks, Links}
import com.github.sguzman.ebook.graph.wrap.DocWrap._
import com.github.sguzman.ebook.graph.wrap.StrWrap._

import scala.language.reflectiveCalls

object Main {
  def main(args: Array[String]): Unit = {
    Sync {
      val pages = 1 to 1260

      Cache.get.flatMap(pages.par, Links.table) { body =>
        val doc = body.doc
        val links = "article.post > div.post-inner > div.post-content > div.post-header > h2.post-title > a[href]"
        doc.flatMap(links).map(_.attr("href"))
      } (a => s"https://it-eb.com/page/$a/")
    } ~ {links =>
      Cache.get.map(links, Ebooks.table) {body =>
        val doc = body.doc

        val title = doc.map("h1.post-title").text
        val date = (doc.maybe("time.post-date") match {
          case None => doc.map("span.post-date")
          case Some(v) => v
        }).text


        val img = doc.map("div.book-cover > img[src]").attr("src")
        val id = doc.map("""article[id^="post"]""").attr("id").stripPrefix("post-").toInt
        val desc = doc.map("div.entry-inner").text

        val details = doc.flatMap("div.book-details > ul > li > span").map(_.text)
        val detailVals = doc.flatMap("div.book-details > ul > li").map(_.text)
        val detailMap = details.zip(detailVals).map(b => (b._1.trim.stripSuffix(":").toLowerCase, b._2.stripPrefix(b._1))).toMap

        val publisher = detailMap.getOrElse("publisher", "")
        val author = detailMap.getOrElse("authors", "")
        val pubDate = detailMap.getOrElse("publication date", "")
        val isbn10 = detailMap.getOrElse("isbn-10", "")
        val isbn13 = detailMap.getOrElse("isbn-13", "")
        val pages = detailMap.getOrElse("pages", "").stripSuffix(" pages").toInt
        val format = detailMap("format")
        val size = detailMap.getOrElse("size", "-1__").init.init.toDouble
        val sizeType = detailMap.getOrElse("size", "kb").stripPrefix(size.toString)

        val relatedPosts = doc.flatMap("li.related-article > article.post > figure.post-thumbnail > a[href]").map(_.attr("href"))
        val categories = doc.flatMap("div.btm-post-meta > p.post-btm-cats > a[href]").map(_.text)
        val prev = doc.maybe("li.prev > a[href]").map(_.attr("href")).getOrElse("")
        val next = doc.maybe("li.next > a[href]").map(_.attr("href")).getOrElse("")
        (0L, title, date, img, id, desc, publisher, author, pubDate, isbn10, isbn13, pages, format, size, sizeType)
      } (identity)
    }
  }
}
