package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.IOUtil.{Async, Sync, putLine}

object Main {
  def main(args: Array[String]): Unit = {
    val _ = Sync {
      Async {
        println("begin")
      } ~ {

      }
    } ~ {
      putLine("done").unsafeRunSync()
    }
  }
}
