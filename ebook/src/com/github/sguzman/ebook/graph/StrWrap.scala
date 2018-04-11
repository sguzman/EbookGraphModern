package com.github.sguzman.ebook.graph

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import org.apache.commons.lang3.StringUtils

implicit final class StrWrap(str: String) {
  def doc: Browser#DocumentType = JsoupBrowser().parseString(str)
  def after(sep: String): String = StringUtils.substringAfter(str, sep)
  def afterLast(sep: String): String = StringUtils.substringAfterLast(str, sep)
}