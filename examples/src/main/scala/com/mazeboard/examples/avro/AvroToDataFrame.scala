package com.mazeboard.examples.avro

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.sql._
import com.mazeboard.config.reader.ConfigReader

object AvroToDataFrame extends App {

  val sparkConf = new ConfigReader(ConfigFactory.parseString(
    """{ spark.master: "local[2]", spark.app.name: "test"}""".stripMargin))

  val spark = SparkSession
    .builder()
    .config(new SparkConf().setAll(sparkConf[Map[String, String]]))
    .getOrCreate()
  val sqlContext = spark.sqlContext

  import scala.collection.JavaConverters._
  import sqlContext.implicits._

  val week = WeekPattern.newBuilder().setPatternId(1900).setBegDate("20190101").setEndDate("20190107").build()
  val obj1 = Store.newBuilder().setStoEan("1").setWeekPattern(week).build()
  val obj2 = Store.newBuilder().setStoEan("2").setWeekPattern(week).build()
  val obj3 = Store.newBuilder().setStoEan("3").setWeekPattern(week).build()

  implicit val x1 = Encoders.kryo(classOf[Store])
  implicit val x2 = Encoders.tuple(Encoders.kryo(classOf[Store]), Encoders.kryo(classOf[Store]))

  val df: DataFrame = Seq(obj1, obj2, obj3).toDF() // Dataset[Row]
  val ds: Dataset[Store] = df.as[Store] //OR sqlContext.createDataset(Seq(obj1, obj2, obj3))

  println(ds.map(_.getStoEan).collect().toList)

  println(ds.map(_.getStoEan).reduce((x, y) => (x.toInt + y.toInt).toString))

  val d3 = ds.alias("d1")
    .joinWith(
      ds.alias("d2"),
      $"d1.value" === $"d2.value") // how to join with fields in Store ?

  println(d3.collect().map(d ⇒ (d._1.getStoEan, d._2.getStoEan)).toList)

  val d4 = ds.crossJoin(ds).as[(Store, Store)]

  val d5 = d4.filter(d ⇒ d._1.getStoEan == d._2.getStoEan)

  println(d5.collect().map(d ⇒ (d._1.getStoEan, d._2.getStoEan)).toList)

}
