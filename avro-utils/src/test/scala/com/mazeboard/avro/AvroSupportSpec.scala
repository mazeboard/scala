package com.mazeboard.avro

import com.mazeboard.config.ConfigReader
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.sql.catalyst.{ DefinedByConstructorParams, ScalaReflection }
import org.apache.spark.sql.{ Encoder, SparkSession }
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.catalyst.expressions.{ BoundReference, CreateNamedStruct, Expression }
import org.apache.spark.sql.catalyst.expressions.objects.AssertNotNull
import org.scalatest.{ FlatSpec, Matchers }
import org.apache.avro.specific.SpecificRecordBase
import org.apache.spark.network.protocol.Encoders

import scala.language.dynamics
import scala.reflect.ClassTag
import scala.reflect.api
import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror
import org.apache.spark.sql.types._

class AvroSupportSpec extends FlatSpec with Matchers {

  import scala.collection.JavaConverters._
  import AvroSupport._

  ///import org.apache.spark.sql.SparkSession
  //val spark = SparkSession.builder().master("local[2]").getOrCreate()
  //import spark.implicits._

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

  "avro generic" must "pass tests" in {
    import org.apache.avro.Schema
    import org.apache.avro.generic.GenericData
    import org.apache.avro.generic.GenericRecord
    import org.apache.avro.generic.GenericDatumReader
    import org.apache.avro.generic.GenericDatumWriter
    import org.apache.avro.file.DataFileReader
    import org.apache.avro.file.DataFileWriter
    import org.apache.avro.io.DatumReader
    import org.apache.avro.io.DatumWriter
    import java.io._

    val s = """{"namespace": "example.avro",
               | "type": "record",
               | "name": "User",
               | "fields": [
               |     {"name": "name", "type": "string"},
               |     {"name": "favorite_number",  "type": ["int", "null"]},
               |     {"name": "favorite_color", "type": ["string", "null"]}
               | ]
               |}"""
    val schema: Schema = new Schema.Parser().parse(s)
    val user1: GenericData.Record = new GenericData.Record(schema)
    user1.put("name", "Alyssa")
    user1.put("favorite_number", 256)
    val user2 = new GenericData.Record(schema)
    user2.put("name", "Ben")
    user2.put("favorite_number", 7)
    user2.put("favorite_color", "red")

    // Serialize user1 and user2 to disk
    val file: File = new File("users.avro")
    val datumWriter: DatumWriter[GenericRecord] = new GenericDatumWriter[GenericRecord](schema)
    val dataFileWriter: DataFileWriter[GenericRecord] = new DataFileWriter[GenericRecord](datumWriter)
    dataFileWriter.create(schema, file)
    dataFileWriter.append(user1)
    dataFileWriter.append(user2)
    dataFileWriter.close()

    // Deserialize users from disk
    val datumReader: DatumReader[GenericRecord] = new GenericDatumReader[GenericRecord](schema)
    val dataFileReader: DataFileReader[GenericRecord] = new DataFileReader[GenericRecord](file, datumReader)
    var user: GenericRecord = null
    while (dataFileReader.hasNext()) {
      // Reuse user object by passing it to next(). This saves us from
      // allocating and garbage collecting many objects for files with
      // many items.
      user = dataFileReader.next(user)
      System.out.println(user)
    }
  }

  "avro compile user.avsc to scala" must "pass tests" in {
    import org.apache.avro.compiler.specific.SpecificCompiler
    import java.io.File

    val src = new File("user.avsc")
    val dest = new File("avro-utils/src/main/scala")

    System.setProperty(
      "org.apache.avro.specific.templates",
      "avro-utils/src/main/scala/com/mazeboard/avro/compiler/specific/templates/scala/spark/")

    SpecificCompiler.compileSchema(src, dest)
  }

  "avro XUser reader/writer" must "pass tests" in {
    import org.apache.avro.generic.GenericRecord
    import org.apache.avro.generic.GenericDatumReader
    import org.apache.avro.generic.GenericDatumWriter
    import org.apache.avro.file.DataFileReader
    import org.apache.avro.file.DataFileWriter
    import org.apache.avro.io.DatumReader
    import org.apache.avro.io.DatumWriter
    import java.io._
    import example.avro.XUser

    val schema = XUser.getClassSchema
    val user1 = new XUser("ben", 5, "red")
    val user2 = new XUser("zen", 3, "yellow")

    // Serialize user1 and user2 to disk
    val file: File = new File("users.avro")
    val datumWriter: DatumWriter[GenericRecord] = new GenericDatumWriter[GenericRecord](schema)
    val dataFileWriter: DataFileWriter[GenericRecord] = new DataFileWriter[GenericRecord](datumWriter)
    dataFileWriter.create(schema, file)
    dataFileWriter.append(user1)
    dataFileWriter.append(user2)
    dataFileWriter.close()

    // Deserialize users from disk
    val datumReader: DatumReader[GenericRecord] = new GenericDatumReader[GenericRecord](schema)
    val dataFileReader: DataFileReader[GenericRecord] = new DataFileReader[GenericRecord](file, datumReader)
    var user: GenericRecord = null
    while (dataFileReader.hasNext()) {
      // Reuse user object by passing it to next(). This saves us from
      // allocating and garbage collecting many objects for files with
      // many items.
      user = dataFileReader.next(user)
      System.out.println(user)
    }
  }

  "avro XUser encoder" must "pass tests" in {
    /*
    {"namespace": "example.avro",
 "type": "record",
 "name": "User",
 "fields": [
     {"name": "name", "type": "string"},
     {"name": "favorite_number",  "type": ["int", "null"]},
     {"name": "favorite_color", "type": ["string", "null"]}
 ]
}
     */
    // java -jar ~/.ivy2/cache/org.apache.avro/avro-tools/jars/avro-tools-1.8.2.jar compile schema user.avsc .
    import org.apache.avro.compiler.specific.SpecificCompiler.main

    import example.avro.XUser
    import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
    import org.apache.spark.sql.catalyst.dsl.expressions._

    println("parameters", ScalaReflection.getConstructorParameters(typeOf[XUser]))
    implicit val userEncoder = ExpressionEncoder[XUser]()
    println(userEncoder.schema)
    println(userEncoder.clsTag)
    val userExprEncoder = userEncoder.asInstanceOf[ExpressionEncoder[XUser]]
    println(userExprEncoder.flat)
    println(userExprEncoder.serializer)
    println(userExprEncoder.deserializer)
    println(userExprEncoder.namedExpressions)
    val jacek = XUser.newBuilder()
      .setName("Ben")
      .setFavoriteNumber(7)
      .setFavoriteColor("red")
      .build()
    val row = userExprEncoder.toRow(jacek)
    val attrs = Seq(DslSymbol('name).string, DslSymbol('favorite_number).int, DslSymbol('favorite_color).string)
    val jacekReborn = userExprEncoder.resolveAndBind(attrs).fromRow(row)
    println(jacek == jacekReborn)
  }

  "avro Store encoder" must "pass tests" in {
    // modify an Avro <avro> as follows:
    // 1. implement org.apache.spark.sql.catalyst.DefinedByConstructorParams
    // 2. implement Serializable
    // 3. for each field <fieldname> of type <type> add a function "public <type> <fieldname>() { return this.<fieldname>; }
    // 4. remove the constructor "public <avro>()"
    // 5. modify the function build to use the constructor with parameters

    import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
    import org.apache.spark.sql.catalyst.dsl.expressions._
    import org.apache.spark.sql.Encoders

    val avroEncoder = ExpressionEncoder[Store]()
    println(avroEncoder.schema)
    println(avroEncoder.clsTag)
    val storeExprEncoder = avroEncoder.asInstanceOf[ExpressionEncoder[Store]]
    println(storeExprEncoder.flat)
    println(storeExprEncoder.serializer)
    println(storeExprEncoder.deserializer)
    println(storeExprEncoder.namedExpressions)
    val jacek = Store.newBuilder()
      .setStoEan("sto123")
      .setStoAnabelKey("sto456")
      .setWeekPattern(WeekPattern.newBuilder()
        .setPatternId(0)
        .setEndDate("20190401")
        .setBegDate("20190301")
        .build())
      .build()
    val row = storeExprEncoder.toRow(jacek)
    val attrs = Seq(DslSymbol('stoEan).string, DslSymbol('stoAnabelKey).string, DslSymbol('weekPattern).struct(StructType(Seq(StructField("patternId", IntegerType, true), StructField("begDate", StringType, true), StructField("endDate", StringType, true)))))
    val jacekReborn = storeExprEncoder.resolveAndBind(attrs).fromRow(row)
    println(jacek == jacekReborn)

  }
}

//case class MyStore(stoEan: String, stoAnabelKey: String, weekPattern: MyWeekPattern)
case class MyStoreW(stoEan: String, stoAnabelKey: String, weekPattern: WeekPattern)
//case class MyWeekPattern(patternId: Int, begDate: String)
case class Person(id: Long, name: String)
//case class XStore(stoEan: String, stoAnabelKey: String, weekPattern: XWeekPattern) extends Store //with DefinedByConstructorParams
//case class XWeekPattern(patternId: Int, begDate: String, endDate: String) extends WeekPattern

case class MyStore(stoEan: String, stoAnabelKey: String, weekPattern: MyWeekPattern)
case class MyWeekPattern(patternId: Int, begDate: String)

