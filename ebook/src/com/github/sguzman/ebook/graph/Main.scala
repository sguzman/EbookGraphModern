package com.github.sguzman.ebook.graph

import java.io.{File, FileInputStream, FileOutputStream}

import com.github.sguzman.ebook.graph.protoc.items.PageDimension.Units
import com.github.sguzman.ebook.graph.protoc.items._
import com.google.protobuf.ByteString
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

  var storeCache: Store = Store(Seq())

  def writeStoreCache(): Unit = {
    scribe.info("Writing store.msg...")
    val file = new File("./store.msg")
    val output = new FileOutputStream(file)
    itemCache.writeTo(output)
    output.close()

    scribe.info("Wrote store.msg")
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
    def put(a: String, b: B): Unit
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
      cache.put(url, result)
      result
    } else {
      val html = HttpUtil.retryHttpGet(url)
      val result = f(html.doc)
      scribe.info(s"After HTTP request, got key $url -> $result")
      cache.put(url, result)
      result
    }

  def extract[A](s: String)
            (cont: String => Boolean)
            (appl: String => A)
            (putF: (String, A) => Unit)
            (f: Browser#DocumentType => A): A =
    get[Cacheable[A], A](s, new Cacheable[A] {
      override def contains(s: String): Boolean = cont(s)
      override def apply(s: String): A = appl(s)
      override def put(a: String, b: A): Unit = putF(a, b)
    }) (f)

  def main(args: Array[String]): Unit = {
    locally {
      val pages = 1 to 1248
      val links = pages.par.flatMap{a =>
        val url = s"https://it-eb.com/page/$a/"
        val doc = HttpUtil.retryHttpGet(url).doc

        doc.flatMap("article.post > div.post-inner > div.post-content > div.post-header > h2.post-title > a[href]")
          .map(_.attr("href"))
          .map(b => Link(b))
      }.toList

      itemCache = itemCache.addAllLinks(links)
    }

    locally {
      val cache = itemCache.books
      itemCache.links.par.foreach{a =>
        extract(a.link)(cache.contains)(cache.apply)((a, b) => itemCache = itemCache.addBooks((a, b))) {doc =>
          val title = doc.map("h1.post-title").text
          val date = (doc.maybe("time.post-date") match {
            case None => doc.map("span.post-date")
            case Some(v) => v
          }).text


          val img = doc.map("div.book-cover > img[src]").attr("src")
          val id = doc.map("""article[id^="post"]""").attr("id").stripPrefix("post-")
          val desc = doc.map("div.entry-inner").text

          val details = doc.flatMap("div.book-details > ul > li > span").map(_.text)
          val detailVals = doc.flatMap("div.book-details > ul > li").map(_.text)
          val detailMap = details.zip(detailVals).map(b => (b._1.trim.stripSuffix(":").toLowerCase, b._2.stripPrefix(b._1))).toMap

          val publisher = detailMap.getOrElse("publisher", "")
          val author = detailMap.getOrElse("authors", "")
          val pubDate = detailMap.getOrElse("publication date", "")
          val isbn10 = detailMap.getOrElse("isbn-10", "")
          val isbn13 = detailMap.getOrElse("isbn-13", "")
          val pages = detailMap.getOrElse("pages", "").stripSuffix(" pages")
          val format = detailMap("format")
          val size = detailMap.getOrElse("size", "-1__").init.init.toString
          val sizeType = detailMap.getOrElse("size", "kb").stripPrefix(size) match {
            case "Kb" | "kb" => Size.Types.Kb
            case "Mb" | "mb" => Size.Types.Mb
            case "Gb" | "gb" => Size.Types.Gb
          }

          val relatedPosts = doc.flatMap("li.related-article > article.post > figure.post-thumbnail > a[href]").map(_.attr("href"))
          val categories = doc.flatMap("div.btm-post-meta > p.post-btm-cats > a[href]").map(_.text)
          val prev = doc.maybe("li.prev > a[href]").map(_.attr("href")).getOrElse("")
          val next = doc.maybe("li.next > a[href]").map(_.attr("href")).getOrElse("")

          Ebook(
            title,
            date,
            img,
            ByteString.copyFrom(Brotli.compress(desc)),
            id.toInt,
            Some(Details(
              author,
              pubDate,
              isbn10,
              isbn13,
              publisher,
              if (pages.nonEmpty) pages.toInt else -1,
              format,
              Some(Size(size.toFloat, sizeType))
            )),
            categories,
            relatedPosts.map(b => Link(b)),
            Some(Link(prev)),
            Some(Link(next))
          )
        }
      }
    }

    locally {
      val cache = itemCache.host
      itemCache.books.par.map(_._2.id).foreach{a =>
        val url = s"https://it-eb.com/download.php?id=$a"
        extract(url)(cache.contains)(cache.apply)((a, b) => itemCache = itemCache.addHost((a, b))) {doc =>
          Link(doc.root.text)
        }
      }
    }

    locally {
      val cache = itemCache.rapidHost
      itemCache.host.par.map(_._2.link).foreach{a =>
        extract(a)(cache.contains)(cache.apply)((a, b) => itemCache = itemCache.addRapidHost((a, b))) {doc =>
          val maybeTitle = doc.maybe("h1")

          maybeTitle match {
            case None => Hosting()
            case Some(topTitle) => identity {
              val key = doc.flatMap("div.col-md-6.file-info > ul > li > span").map(_.text)
              val values = doc.flatMap("div.col-md-6.file-info > ul > li").map(_.text)
              val keyVals = key.zip(values).map(a => a._1.toLowerCase -> a._2.stripPrefix(a._1).stripPrefix(": ")).toMap

              val encryption = keyVals.getOrElse("encryption", "no").toLowerCase match {
                case "yes" => true
                case "no" => false
              }

              val pageSize = identity {
                val split = keyVals.getOrElse("page size", "-1 x -1 px")
                  .stripSuffix(" (letter)")
                  .stripSuffix(" (A4)")
                  .split(" ")

                val height = split.head.toFloat
                val width = split(2).toFloat
                val units = split.last.toLowerCase match {
                  case "pts" => Units.PTS
                  case "px" | "pixels" => Units.PX
                  case "inch" | "inches" => Units.INCH
                }

                PageDimension(height, width, units)
              }

              Hosting(
                topTitle.text,
                keyVals.getOrElse("file type", ""),
                keyVals.getOrElse("title", ""),
                keyVals.getOrElse("author", ""),
                keyVals.getOrElse("creator", ""),
                keyVals.getOrElse("producer", ""),
                keyVals.getOrElse("creation date", ""),
                keyVals.getOrElse("modification date", ""),
                keyVals.getOrElse("pages", "-1").toInt,
                encryption,
                Some(pageSize),
                keyVals.getOrElse("file size", "-1").stripSuffix(" bytes").toInt,
                keyVals.getOrElse("md5 checksum", "")
              )
            }
          }


        }
      }
    }

    locally {
      val store = itemCache.links.par.map{a =>
        val book = itemCache.books(a)
        val url = s"https://it-eb.com/download.php?id=${book.id}"
        val host = itemCache.host(url)
        val hosting = itemCache.rapidHost(host.link)
        StoreEbook(Some(book), Some(hosting))
      }.toList

      storeCache = storeCache.addAllStore(store)
      writeStoreCache()
    }

    scribe.info("done")
  }
}
