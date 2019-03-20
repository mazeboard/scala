package com.mazeboard.kafka

import org.scalatest.{ FlatSpec, Matchers }
import com.typesafe.config.{ Config, ConfigFactory }
import scala.concurrent.Future
import scala.concurrent.blocking
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/*
IMPLEMENTING CUSTOM SERDES
If you need to implement custom SerDes, your best starting point is to take a look
 at the source code references of existing SerDes (see previous section). Typically,
 your workflow will be similar to:

Write a serializer for your data type T by implementing org.apache.kafka.common.serialization.Serializer.
Write a deserializer for T by implementing org.apache.kafka.common.serialization.Deserializer.
Write a serde for T by implementing org.apache.kafka.common.serialization.Serde, which
you either do manually (see existing SerDes in the previous section) or by leveraging
helper functions in Serdes such as Serdes.serdeFrom(Serializer<T>, Deserializer<T>).
 */

class DataStreamSpec extends FlatSpec with Matchers {

  val name = "goo"

  "dataStream" must "pass tests" in {
    // read timeout? write timeout?
    // auto commit?
    val config = ConfigFactory.parseString("""{kafka.bootstrap.servers: "localhost:9092"}""")
    implicit val ec = ExecutionContext.global

    val ds = new DataStream(config)

    val x = ds[Long]("topicX")
    val y = ds[Long]("topicY")
    val z = ds[Long]("topicZ")

    class Win extends Window

    val future1 = Future({
      ds.withinTransaction(null, x, y, z) {
        val a = x().read.map(_.value).sum // sum all values in window
        val b = y().read.map(_.value).sum
        z().write(a + b)
      }
    })

    val future2 = Future({
      ds.withinTransaction(new Win, x, y, z) {
        0.until(10).foreach(i => x().write(i))
        5.until(15).foreach(i => y().write(i))
        println(s"z: ${z().read.map(_.value).sum}")
      }
    })

    println("wait future1 and future2")
    Await.ready(future1, Duration("5min"))
    Await.ready(future2, Duration("5min"))

  }

}

/*
    val fooJob = ds.newJob({
      z.write(value = x.read + y.read)
    }, group = true)

    trait OUT[T] extends Function1[T, Unit]
    trait IN[T] extends Function0[T]

    def Block1(A:IN[Int])(B:OUT[Int], C:OUT[Int]) {
      val a = A()
      B(2*a)
      C(a*a)
    }

    def Block2(A:IN[Int])(B:OUT[Int]) {
      B(A()+1)
    }

    Block1(() => 1)((x:Int) => Block2(() => x)((x:Int) => print(x)),
      (x:Int) => print(x))

    fooJob.onReadFail({...})

    fooJob.onWriteFail({...})

    ds.start(fooJob)
    run foo on all brokers
    all readers share the same groupId ?
    if group is false then all readers read all values (each reader is in a distinct group)
    otherwise values will be distributed evenly over all readers (all readers will be in the same group)

    ds.stop(fooJob):
      stop fooJob

        DataStream[V].read:
      read value from topic and convert to V
        callback readFail if cannot convert value to V
    read returns the value or the result of readFail

    DataStream[V].write:
      send value to topic
        callback writeFail if exception


 */ 