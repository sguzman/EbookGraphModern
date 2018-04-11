package com.github.sguzman.ebook.graph

import com.github.sguzman.ebook.graph.IOUtil.{Async, Sync}

object Main {
  def main(args: Array[String]): Unit = {
    val _ = Sync {
      Async {
        println("begin")
      } ~ {
        println("next")
      }
    } ~ {
      println("done")
    }
  }
}
