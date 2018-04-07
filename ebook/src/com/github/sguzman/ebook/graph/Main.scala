package com.github.sguzman.ebook.graph

import java.io.{File, FileInputStream, FileOutputStream}

import com.github.sguzman.ebook.graph.protoc.items.ItemStore
import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
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
    def put(s: String, b: B): Unit
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

  def get[A](s: String)
            (cont: String => Boolean)
            (appl: String => A)
            (pu: (String, A) => Unit)
            (f: Browser#DocumentType => A): A =
    get[Cacheable[A], A](s, new Cacheable[A] {
      override def contains(s: String): Boolean = cont(s)
      override def apply(s: String): A = appl(s)
      override def put(s: String, b: A): Unit = pu(s, b)
    }) (f)

  def main(args: Array[String]): Unit = {
    println("Hello")
  }
}
