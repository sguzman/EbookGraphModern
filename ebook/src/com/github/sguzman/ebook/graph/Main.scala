package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.IOUtil.Pure
import com.github.sguzman.ebook.graph.IOUtil.putLine

object Main {
  def main(args: Array[String]): Unit = {
    Pure {
      putLine("done").unsafeRunSync()
    }
  }
}
