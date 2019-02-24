package com.carrefour.phenix.spark.utils

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

  "srbsupport" must "load avro objects into class instances with fields as SpecificRecordBase" in {
    store2.load[MyStoreW] shouldBe myStore3
  }

  it must "load avro objects into class instances" in {
    load[MyStore](Seq(store1, store2)) shouldBe List(myStore1, myStore2)
  }

  it must "load map from a collection of avro object" in {
    List(store1, store2)
      .loadMap[String, MyStore]((x: MyStore) ⇒ x.stoEan) shouldBe
      Map("abc" -> myStore1, "xyz" -> myStore2)

  }

  "srbsupport implicit" must "load avro objects into class instances" in {
    Seq(store1, store2).load[MyStore] shouldBe List(myStore1, myStore2)
  }

  it must "load avro object into case class instance" in {
    store1.load[MyStore] shouldBe myStore1
    store2.load[MyStore] shouldBe myStore2
  }

}

case class MyStore(stoEan: String, stoAnabelKey: String, weekPattern: MyWeekPattern)
case class MyStoreW(stoEan: String, stoAnabelKey: String, weekPattern: WeekPattern)
case class MyWeekPattern(patternId: Int, begDate: String)