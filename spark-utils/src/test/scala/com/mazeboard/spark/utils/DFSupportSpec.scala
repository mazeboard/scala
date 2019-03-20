package com.mazeboard.spark.utils

import org.apache.spark.SparkConf
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import com.typesafe.config.ConfigFactory
import com.mazeboard.config.ConfigReader
import org.apache.spark.rdd.RDD
import org.scalatest._
import com.databricks.spark.avro.SchemaConverters

class DFSupportSpec extends FlatSpec with Matchers {

  import scala.collection.JavaConverters._

  System.setSecurityManager(null)

  val sparkConf = new ConfigReader(ConfigFactory.parseString("""{
                                               spark.master: "local[*]"
                                               spark.app.name: "spark-utils"
                                               spark.yarn.submit.file.replication: "0"
                                               spark.hadoop.hadoop.security.authentication: "simple"
                                               spark.sql.warehouse.dir: "/tmp"
                                               spark.driver.allowMultipleContexts: true
                                             }"""))[Map[String, String]]

  val spark = SparkSession.builder.config(new SparkConf().setAll(sparkConf)).getOrCreate()

  implicit val sqlContext = spark.sqlContext
  import DataFrameSupport._

  val schema = StructType(
    List(
      StructField("dar", StringType, true),
      StructField(
        "foo",
        StructType(List(
          StructField("zoo", IntegerType, false),
          StructField("noo", IntegerType, false),
          StructField("hoo", IntegerType, false))), true),
      StructField(
        "doo",
        ArrayType(StructType(List(
          StructField("boo", IntegerType, false),
          StructField("joo", IntegerType, false))), true), true)))

  val df = spark.createDataFrame(
    spark.sparkContext.parallelize(List(
      Row("a", Row(1, 2, 3), List(Row(7, 9), Row(4, 8))),
      Row("b", Row(4, 5, 7), List(Row(8, 11))))),
    schema)

  import spark.sqlContext.implicits._

  val sqlSchema: StructType = SchemaConverters
    .toSqlType(Store.getClassSchema)
    .dataType
    .asInstanceOf[StructType]

  val week = WeekPattern.newBuilder().setPatternId(1900).setBegDate("20190101").setEndDate("20190107").build()
  val store1 = Store.newBuilder().setStoEan("abc").setStoAnabelKey("foo").setWeekPattern(week).build()
  val store2 = Store.newBuilder().setStoEan("xyz").setStoAnabelKey("bar").setWeekPattern(week).build()
  val myStore1 = MyStore(stoEan = "abc", stoAnabelKey = "foo", weekPattern = MyWeekPattern(patternId = 1900, begDate = "20190101"))
  val myStore2 = MyStore(stoEan = "xyz", stoAnabelKey = "bar", weekPattern = MyWeekPattern(patternId = 1900, begDate = "20190101"))

  val rdd: RDD[Store] = spark.sparkContext.parallelize(List(store1, store2))
  rdd.toDF()

  "dfsupport" must "convert dataframe to a Map[String, MyDar]" in {
    df.loadMap[String, MyDar]((x: MyDar) ⇒ x.dar) shouldBe Map(
      "a" -> MyDar("a", MyFoo(1, 2, 3), List(MyZoo(7, 9), MyZoo(4, 8))),
      "b" -> MyDar("b", MyFoo(4, 5, 7), List(MyZoo(8, 11))))
  }

  "dfsupport" must "convert dataframe to a Map[MyFoo, MyDar]" in {
    df.loadMap[MyFoo, MyDar]((x: MyDar) ⇒ x.foo) shouldBe Map(
      MyFoo(1, 2, 3) -> MyDar("a", MyFoo(1, 2, 3), List(MyZoo(7, 9), MyZoo(4, 8))),
      MyFoo(4, 5, 7) -> MyDar("b", MyFoo(4, 5, 7), List(MyZoo(8, 11))))
  }

  "dfsupport" must "convert dataframe to Seq[MyDar]" in {
    df.load[MyDar].collect.toList shouldBe List(
      MyDar("a", MyFoo(1, 2, 3), List(MyZoo(7, 9), MyZoo(4, 8))),
      MyDar("b", MyFoo(4, 5, 7), List(MyZoo(8, 11))))
  }

  "dfsupport" must "convert dataframe using as" in {
    val x: Dataset[MyDar] = df.as[MyDar]
    x.collect.toList shouldBe List(
      MyDar("a", MyFoo(1, 2, 3), List(MyZoo(7, 9), MyZoo(4, 8))),
      MyDar("b", MyFoo(4, 5, 7), List(MyZoo(8, 11))))
  }
}

case class MyDar(
  dar: String,
  foo: MyFoo,
  doo: List[MyZoo])

case class MyFoo(zoo: Int, noo: Int, hoo: Int)

case class MyZoo(boo: Int, joo: Int)

case class MyStore(stoEan: String, stoAnabelKey: String, weekPattern: MyWeekPattern)
case class MyWeekPattern(patternId: Int, begDate: String)
