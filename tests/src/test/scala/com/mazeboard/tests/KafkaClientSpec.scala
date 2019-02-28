package com.mazeboard.tests

import org.scalatest.{ FlatSpec, Matchers }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class KafkaClientSpec extends FlatSpec with Matchers {
  import KafkaSupport._

  val name = "goo"

  "producers && consumers" must "pass tests" in {
    val futures = Range(0, 3).map(i => {
      (Future(startProducer(count = 20, s"${name}_$i"))(ExecutionContext.global), Future(startConsumer(id = i, groupid = name, List(s"${name}_0", s"${name}_1", s"${name}_2"), minBatchSize = 20))(ExecutionContext.global))
    })
    futures.foreach {
      case (producer, consumer) => {
        Await.ready(producer, Duration("1min"))
        Await.ready(consumer, Duration("1min"))
      }
    }
  }

}

