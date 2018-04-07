package com.github.sguzman.ebook.graph

import java.io.{File, FileInputStream, FileOutputStream}

import com.github.sguzman.ebook.graph.protoc.items._
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.{element, elementList}
import org.apache.commons.lang3.StringUtils

object Main {
  var itemCache: ItemStore = identity {
    val file = new File("./items.msg")
    if (!file.exists) {
      scribe.info("Creating items.msg file")
      file.createNewFile()
      ItemStore(Seq())
    } else {
      scribe.info("Found items.msg file")
      val input = new FileInputStream(file)
      val out = ItemStore.parseFrom(input)
      input.close()
      out
    }
  }

  def writeItemCache(): Unit = {
    scribe.info("Writing items.msg...")
    val file = new File("./items.msg")
    val output = new FileOutputStream(file)
    itemCache.writeTo(output)
    output.close()

    scribe.info("Wrote items.msg")
  }

  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    writeItemCache()
  }))

  implicit final class DocWrap(doc: Browser#DocumentType) {
    def map(s: String, a: String = ""): Element = doc.>?>(element(s)) match {
      case Some(v) => v
      case None => throw new Exception(s"$s $a")
    }

    def maybe(s: String) = doc.>?>(element(s))

    def flatMap(s: String): List[Element] = doc.>?>(elementList(s)) match {
      case Some(v) => v
      case None => throw new Exception(s)
    }
  }

  implicit final class StrWrap(str: String) {
    def doc = JsoupBrowser().parseString(str)
    def after(sep: String) = StringUtils.substringAfter(str, sep)
    def afterLast(sep: String) = StringUtils.substringAfterLast(str, sep)
  }

  trait Cacheable[B] {
    def contains(s: String): Boolean
    def apply(s: String): B
  }

  def get[A <: Cacheable[B], B](url: String, cache: A) (f: Browser#DocumentType => B): B =
    if (cache.contains(url)) {
      val value = cache.apply(url)
      scribe.info(s"Hit cache for key $url -> $value")
      value
    }
    else if (HttpUtil.httpCache.contains(url)) {
      val html = HttpUtil.retryHttpGet(url)
      val result = f(html.doc)
      scribe.info(s"Got key $url -> $result")
      result
    } else {
      val html = HttpUtil.retryHttpGet(url)
      val result = f(html.doc)
      scribe.info(s"After HTTP request, got key $url -> $result")
      result
    }

  def extract[A](s: String)
            (cont: String => Boolean)
            (appl: String => A)
            (f: Browser#DocumentType => A): A =
    get[Cacheable[A], A](s, new Cacheable[A] {
      override def contains(s: String): Boolean = cont(s)
      override def apply(s: String): A = appl(s)
    }) (f)

  def main(args: Array[String]): Unit = {
    locally {
      val pages = 1 to 1248
      val links = pages.par.flatMap{a =>
        val url = s"https://it-eb.com/page/$a/"
        val doc = HttpUtil.retryHttpGet(url).doc

        val links = doc.flatMap("article.post > div.post-inner > div.post-content > div.post-header > h2.post-title > a[href]")
          .map(_.attr("href"))
          .map(b => Link(b))

        links.foreach(b => scribe.info(b.link))
        links
      }.toList

      itemCache = itemCache.addAllLinks(links)
    }

    locally {
      val cache = itemCache.books
      val books = itemCache.links.par.map{a =>
        val book = extract(a.link)(cache.contains)(cache.apply) {doc =>
          val title = doc.map("h1.post-title").text
          val date = doc.map("time.post-date").text
          val img = doc.map("div.book-cover > img[src]").attr("src")
          val id = doc.map("""article[id^="post"]""").attr("id").stripPrefix("post-")
          val desc = doc.map("div.entry-inner").text

          val details = doc.flatMap("div.book-details > ul > li > span").map(_.text)
          val detailVals = doc.flatMap("div.book-details > ul > li").map(_.text)
          val detailMap = details.zip(detailVals).map(b => (b._1.trim.stripSuffix(":").toLowerCase, b._2.stripPrefix(b._1))).toMap

          val publisher = detailMap("publishers")
          val author = detailMap("authors")
          val pubDate = detailMap("publication date")
          val isbn10 = detailMap("isbn-10")
          val isbn13 = detailMap("isbn-13")
          val pages = detailMap("pages").stripSuffix(" pages")
          val format = detailMap("format")
          val size = detailMap("size").init.init.toString
          val sizeType = detailMap("size").stripPrefix(size) match {
            case "K" | "K" => Size.Types.Kb
            case "M" | "m" => Size.Types.Mb
            case "G" | "g" => Size.Types.Gb
          }

          val relatedPosts = doc.flatMap("li.related-article > article.post > figure.post-thumbnail > a[href]").map(_.attr("href"))
          val categories = doc.flatMap("div.btm-post-meta > p.post-btm-cats > a[href]").map(_.text)
          val prev = doc.map("li.prev > a[href]").attr("href")
          val next = doc.map("li.next > a[href]").attr("href")

          Ebook(
            title,
            date,
            img,
            desc,
            id.toInt,
            Some(Details(
              author,
              pubDate,
              isbn10,
              isbn13,
              publisher,
              pages.toInt,
              format,
              Some(Size(size.toInt, sizeType))
            )),
            categories,
            relatedPosts.map(b => Link(b)),
            prev,
            next
          )
        }
        (a.link, book)
      }.toList

      itemCache.addAllBooks(books)
    }

    scribe.info("done")
  }
}
