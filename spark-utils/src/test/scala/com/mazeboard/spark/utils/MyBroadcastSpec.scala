package com.mazeboard.spark.utils

import org.apache.spark.SparkConf
import org.apache.spark.sql._
import com.typesafe.config.ConfigFactory
import com.mazeboard.config.ConfigReader
import org.scalatest._

class MyBroadcastSpec extends FlatSpec with Matchers {

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

  val sparkContext = spark.sparkContext

  "spark tasks" must "in one stage all tasks read the same broadcast value" in {
    import spark.sqlContext.implicits._

    var n = 0
    val count = new MyBroadcast[Int](sparkContext, 5000,
      name = "count",
      computeValue = (attr: String) ⇒ {
        n += 1
        Some((n, attr))
      })

    val ds: Dataset[Int] = (1 until 1000).toList.toDS()

    val m = ds.map(a ⇒ {
      count.value
    }).distinct().count()

    m shouldBe 1
  }

  "spark tasks" must "all tasks in a new stage read a new broadcast value" in {
    import spark.sqlContext.implicits._

    var n = 0
    val count = new MyBroadcast[Int](sparkContext, 5000,
      name = "count",
      computeValue = (attr: String) ⇒ {
        n += 1
        Some((n, attr))
      })

    val ds: Dataset[Int] = (1 until 1000).toList.toDS()

    val r1 = ds.map(x ⇒ count.value)
      .distinct().collect().toList
    Thread.sleep(5000) // make sure that the broadcast update fires
    val r2 = ds.map(x ⇒ count.value)
      .distinct().collect().toList

    r1.length shouldBe 1
    r2.length shouldBe 1
    r2.head should be > r1.head
  }
}