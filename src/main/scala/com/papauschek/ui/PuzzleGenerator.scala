package com.papauschek.ui

import org.scalajs.dom.{Element, Worker}

import scala.concurrent.{Future, Promise}

/** puzzle generator that is parallelized using Web Workers */
object PuzzleGenerator {

  private val WORKER_COUNT = 4

  private var results = Seq.empty[Any]
  private var promise: Promise[Seq[Any]] = Promise.successful(Nil)

  private val workers = (0 until WORKER_COUNT).map(_ => new Worker("worker.js"))

  workers.foreach {
    worker =>
      worker.onmessage = {
        msg =>
          println(s"Received: ${msg.data}")
          results :+= msg.data
          if (results.length == WORKER_COUNT) {
            println("promised")
            promise.success(results)
          }
      }
  }

  def send(): Future[Seq[Any]] = {
    results = Nil
    promise = Promise[Seq[Any]]
    workers.foreach {
      worker => worker.postMessage("Message")
    }
    promise.future
  }

}
