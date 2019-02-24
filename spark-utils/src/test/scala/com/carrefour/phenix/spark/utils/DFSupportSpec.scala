package com.carrefour.phenix.spark.utils

import org.apache.spark.SparkConf
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import com.typesafe.config.ConfigFactory
import org.scalatest._

class DFSupportSpec extends FlatSpec with Matchers {

  import scala.collection.JavaConverters._

  System.setSecurityManager(null)

  val config = ConfigFactory.load()
  val conf = config.getConfig("phenix.sparkConf").entrySet().asScala
    .map(e ⇒ (e.getKey, e.getValue.unwrapped().toString)).toMap

  val spark = SparkSession.builder.config(new SparkConf().setAll(conf)).getOrCreate()

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
    df.load[MyDar] shouldBe List(
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
