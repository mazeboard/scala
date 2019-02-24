package com.mazeboard.reader

import java.io.{ ByteArrayInputStream, ObjectInputStream, ByteArrayOutputStream, ObjectOutputStream }
import com.typesafe.config.{ Config, ConfigFactory }
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import scala.reflect.api
import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror
import org.apache.spark.sql._

object AvroToDataFrame extends App {
  val config: Config = ConfigFactory.load()
  import scala.collection.JavaConversions._
  val sparkConf = config.getConfig("phenix.sparkConf").entrySet().map(e ⇒ (e.getKey, e.getValue.unwrapped().toString))
  val sc = new SparkContext(new SparkConf().setAll(sparkConf))
  val sqlContext = new SQLContext(sc)
  import sqlContext.implicits._
  implicit val x1 = Encoders.kryo(classOf[Store])

  val obj1 = Store.newBuilder().setStoreSalSize(1).build()
  val obj2 = Store.newBuilder().setStoreSalSize(2).build()
  val obj3 = Store.newBuilder().setStoreSalSize(3).build()

  val df: DataFrame = Seq(obj1, obj2, obj3).toDF() // Dataset[Row]

  val ds: Dataset[Store] = df.as[Store]
  //OR val ds = sqlContext.createDataset(Seq(obj1, obj2, obj3))

  println(ds.map(_.getStoreSalSize).collect().toList)

  println(ds.map(_.getStoreSalSize).reduce(_ + _))

  val d3 = ds.alias("d1")
    .joinWith(
      ds.alias("d2"),
      $"d1.value" === $"d2.value") // how to join with fields in Store ?

  println(d3.collect().map(d ⇒ (d._1.getStoreSalSize, d._2.getStoreSalSize)).toList)

  implicit val x2 =
    Encoders.tuple(Encoders.kryo(classOf[Store]), Encoders.kryo(classOf[Store]))
  val d4 = ds.crossJoin(ds).as[(Store, Store)]
  val d5 = d4.filter(d ⇒ d._1.getStoreSalSize == d._2.getStoreSalSize)
  println(d5.collect().map(d ⇒ (d._1.getStoreSalSize, d._2.getStoreSalSize)).toList)

  /* use javaassit to make the Avro instances serializable
      , libraryDependencies += "org.javassist" % "javassist" % "3.24.1-GA"

import javassist.ClassPool
  val pool = ClassPool.getDefault();
vall cc = pool.get("referential.store.v2.Store");
cc.addInterface(pool.get("java.io.Serializable"))

   */
  val df = toDF[XStore](List(
    XStore.newBuilder().setAclGesKey("HELLO").build(),
    XStore.newBuilder().setAclGesKey("WORLD").build()))

  import sqlContext.implicits._
  // TODO write as an Encoder for XStore
  // then we can do List(XStore.newBuilder().build()).toDF
  val r = mapDF[XStore, String](df.toDF(), s ⇒ s.getAclGesKey)

  println(r.collect().toList)

  def toDF[T](objs: Seq[T]): DataFrame = {
    import sqlContext.implicits._

    objs.map(obj ⇒ {
      val bos = new ByteArrayOutputStream()
      val oos = new ObjectOutputStream(bos)
      oos.writeObject(obj)
      oos.flush()
      bos.toByteArray()
    }).toDF()
  }

  def mapDF[T, S: Encoder: TypeTag](df: DataFrame, f: T ⇒ S): Dataset[S] = {
    import sqlContext.implicits._
    df.map(row ⇒ {
      val bis = new ByteArrayInputStream(row.toSeq.head.asInstanceOf[Array[Byte]])
      val ois = new ObjectInputStream(bis)
      val obj = ois.readObject().asInstanceOf[T]

      f(obj)
    })
  }
}
