package com.mazeboard.tests.avro

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.sql._
import com.mazeboard.config.ConfigReader
import com.mazeboard.spark.utils.AvroSupport
import org.scalatest.{ FlatSpec, Matchers }

class AvroDataFrameSpec extends FlatSpec with Matchers {

  import AvroSupport._

  val sparkConf = new ConfigReader(ConfigFactory.parseString(
    """{ spark.master: "local[2]", spark.app.name: "test"}""".stripMargin))

  val spark = SparkSession
    .builder()
    .config(new SparkConf().setAll(sparkConf[Map[String, String]]))
    .getOrCreate()

  import scala.collection.JavaConverters._
  import spark.sqlContext.implicits._

  val week = WeekPattern.newBuilder().setPatternId(1900).setBegDate("20190101").setEndDate("20190107").build()
  val store1 = Store.newBuilder().setStoEan("1").setWeekPattern(week).build()
  val store2 = Store.newBuilder().setStoEan("2").setWeekPattern(week).build()
  val store3 = Store.newBuilder().setStoEan("3").setWeekPattern(week).build()

  behavior of "AvroToDataFrameSpec"

  it should "pass tests" in {

    val ds: Dataset[MyStore] = Seq(store1, store2, store3)
      .load[MyStore] // use AvroSupport to load Avro objects into case classes
      .toDF()
      .as[MyStore]

    println(ds.map(_.stoEan).reduce((x, y) => (x.toInt + y.toInt).toString))

    println(ds.alias("d1")
      .joinWith(
        ds.alias("d2"),
        $"d1.value" === $"d2.value")
      .collect()
      .map(d ⇒ (d._1.stoEan, d._2.stoEan))
      .toList)

    println(ds.crossJoin(ds).as[(MyStore, MyStore)]
      .filter(d ⇒ d._1.stoEan == d._2.stoEan)
      .collect()
      .map(d ⇒ (d._1.stoEan, d._2.stoEan))
      .toList)
  }

}

case class MyStore(stoEan: String, stoAnabelKey: String)