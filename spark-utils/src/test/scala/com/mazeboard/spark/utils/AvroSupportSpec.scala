package com.mazeboard.spark.utils

import com.mazeboard.config.ConfigReader
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.scalatest.{FlatSpec, Matchers}

import scala.language.dynamics
import scala.reflect.runtime.universe

class AvroSupportSpec extends FlatSpec with Matchers {

  import scala.collection.JavaConverters._
  import AvroSupport._

  val week = WeekPattern.newBuilder().setPatternId(1900).setBegDate("20190101").setEndDate("20190107").build()
  val store1 = Store.newBuilder().setStoEan("abc").setStoAnabelKey("foo").setWeekPattern(week).build()
  val store2 = Store.newBuilder().setStoEan("xyz").setStoAnabelKey("bar").setWeekPattern(week).build()
  val myStore1 = MyStore(stoEan = "abc", stoAnabelKey = "foo", weekPattern = MyWeekPattern(patternId = 1900, begDate = "20190101"))
  val myStore2 = MyStore(stoEan = "xyz", stoAnabelKey = "bar", weekPattern = MyWeekPattern(patternId = 1900, begDate = "20190101"))
  val myStore3 = MyStoreW(stoEan = "xyz", stoAnabelKey = "bar", week)

  "avrosupport" must "pass tests" in {
    store2.load[MyStoreW] shouldBe myStore3

    List(store1, store2)
      .loadMap[String, MyStore]((x: MyStore) ⇒ x.stoEan) shouldBe
      Map("abc" -> myStore1, "xyz" -> myStore2)

    Seq(store1, store2).load[MyStore] shouldBe List(myStore1, myStore2)

    store1.load[MyStore] shouldBe myStore1
    store2.load[MyStore] shouldBe myStore2
  }

  "avro encoder" must "pass tests" in {
    val sparkConf = new ConfigReader(ConfigFactory.parseString(
      """{
                                               spark.master: "local[*]"
                                               spark.app.name: "spark-utils"
                                               spark.yarn.submit.file.replication: "0"
                                               spark.hadoop.hadoop.security.authentication: "simple"
                                               spark.sql.warehouse.dir: "/tmp"
                                               spark.driver.allowMultipleContexts: true
                                             }"""))[Map[String, String]]
    val spark = SparkSession.builder.config(new SparkConf().setAll(sparkConf)).getOrCreate()
    import spark.implicits._

    class MyObj(val i: Int)
    // Unable to find encoder for type MyObj. An implicit Encoder[MyObj] is needed to store MyObj instances in a Dataset
    val d = spark.createDataset(Seq(new MyObj(1), new MyObj(2), new MyObj(3)))

    import AvroSupport._
    val Foo = declare[Store]("Azerty")
    val foo = Foo.load(store1)
    println(foo.stoEan)

    var xx: String = _
    xx = "abc"

    class Xxx {
      var foo: String = _
    }
    object Xxx {
      def load(f:String): Xxx = {
        val o = new Xxx()
        o.foo = f
        o
      }
    }

      val x = Xxx.load("abc")
      x.foo
  }
}

//case class MyStore(stoEan: String, stoAnabelKey: String, weekPattern: MyWeekPattern)
case class MyStoreW(stoEan: String, stoAnabelKey: String, weekPattern: WeekPattern)
//case class MyWeekPattern(patternId: Int, begDate: String)