package com.mazeboard.spark.utils

import org.apache.avro.specific.SpecificRecordBase
import org.scalatest.{ FlatSpec, Matchers }

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

  "avro macro" must "pass tests" in {
    // TODO use macro to declare case class from Schema
    // if fields is empty then the case class has all fields in the schema
    import scala.reflect.runtime.universe._
    declare(Store.getClassSchema, "Azerty", "stoEan", "stoAnabelKey")
    //println(Azerty())
  }

}

//case class MyStore(stoEan: String, stoAnabelKey: String, weekPattern: MyWeekPattern)
case class MyStoreW(stoEan: String, stoAnabelKey: String, weekPattern: WeekPattern)
//case class MyWeekPattern(patternId: Int, begDate: String)