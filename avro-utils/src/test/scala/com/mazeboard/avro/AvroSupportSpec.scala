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
                "type": "record",
                "name": "User",
                "fields": [
                    {"name": "name", "type": "string"},
                    {"name": "favorite_number",  "type": ["int", "null"]},
                    {"name": "favorite_color", "type": ["string", "null"]}
                ]
               }"""
    val schema: Schema = new Schema.Parser().parse(s)
    println("name schema", schema.getField("name").schema())
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

  /*"avro compile avro schema" must "pass tests" in {
    import org.apache.avro.compiler.specific.SpecificCompiler
    import java.io.File
    import org.apache.avro.Schema
    import org.apache.avro.generic.GenericData.StringType

    val src = new File("user.avsc")
    val dest = new File("avro-utils/src/main/scala")

    System.setProperty(
      "org.apache.avro.specific.templates",
      "avro-utils/src/main/templates/avro/java/")

    //SpecificCompiler.compileSchema(src, dest)

    val s = """{"namespace": "example.avro",
                "type": "record",
                "name": "YUser",
                "fields": [
                    {"name": "name", "type": "string"},
                    {"name": "favorite_number",  "type": ["int", "null"]},
                    {"name": "favorite_color", "type": ["string", "null"]}
                ]
               }"""
    val schema: Schema = new Schema.Parser().parse(s)
    val compiler = new SpecificCompiler(schema)
    compiler.setStringType(StringType.valueOf("String"))
    compiler.compileToDestination(null, dest)
  }*/

  "avro User reader/writer" must "pass tests" in {
    import org.apache.avro.generic.GenericRecord
    import org.apache.avro.generic.GenericDatumReader
    import org.apache.avro.generic.GenericDatumWriter
    import org.apache.avro.file.DataFileReader
    import org.apache.avro.file.DataFileWriter
    import org.apache.avro.io.DatumReader
    import org.apache.avro.io.DatumWriter
    import java.io._
    import example.avro.User

    val schema = User.getClassSchema
    val user1 = new User("ben", 5, "red")
    val user2 = new User("zen", 3, "yellow")

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

  "avro User encoder" must "pass tests" in {
    import example.avro.User
    import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
    import org.apache.spark.sql.catalyst.dsl.expressions._

    println("parameters", ScalaReflection.getConstructorParameters(typeOf[User]))
    val userEncoder = ExpressionEncoder[User]()
    println(userEncoder.schema)
    val userExprEncoder = userEncoder.asInstanceOf[ExpressionEncoder[User]]
    println(userExprEncoder.serializer)
    println(userExprEncoder.deserializer)
    val jacek = User.newBuilder()
      .setName("Ben")
      .setFavoriteNumber(7)
      .setFavoriteColor("red")
      .build()
    val row = userExprEncoder.toRow(jacek)
    val jacekReborn = userExprEncoder.resolveAndBind().fromRow(row)
    println("jacek", jacek)
    println("jacekReborn", jacekReborn)
    println(jacek == jacekReborn)

    val attrs = Seq(DslSymbol('name).string, DslSymbol('favorite_number).int, DslSymbol('favorite_color).string)
    println("using attrs", userExprEncoder.resolveAndBind(attrs).fromRow(row))
  }

  "avro User implicit encoder" must "pass tests" in {
    import example.avro.User
    import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
    import org.apache.spark.sql.Dataset

    val spark = SparkSession.builder.master("local[2]").getOrCreate()
    import spark.implicits._

    implicit val userEncoder = ExpressionEncoder[User]()

    val ds: Dataset[User] = 0.until(10000).map(i => User.newBuilder()
      .setName("Bar")
      .setFavoriteNumber(i)
      .setFavoriteColor("green")
      .build()).toDS()

    val x = ds.map(x => {
      (x.getName(), x.getFavoriteColor(), x.getFavoriteNumber())
    })

    val count = x.count()

    println(s"count: $count head: ${x.collect().toList.head}")

  }

  "avro Barcode implicit encoder" must "pass tests" in {

    import referential.product.v2.Barcode
    import common.lib.v1.Money
    import common.lib.v1.Currency
    import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
    import org.apache.spark.sql.Encoders
    import org.apache.spark.sql.Dataset
    import scala.reflect.ClassTag

    val spark = SparkSession.builder.master("local[2]").getOrCreate()
    import spark.implicits._

    // no implicit encoder for java.util.List[_], replace, in record.vm,  by scala.collection.Seq ?
    // no implicit encoder for java.util.Map[_,_], replace,  in record.vm, by scala.collection.Map ?

    // enum Currency, new common.lib.v1.Currency() ?

    println(ScalaReflection.getConstructorParameters(typeOf[Barcode]))
    implicit val barcodeEncoder = ExpressionEncoder[Barcode]()

    val ds: Dataset[Barcode] = 0.until(10000).map(i => {
      val barcode = Barcode.newBuilder()
        .setBarcode("Bar")
        .setPrdTaxVal(Money.newBuilder().setUnscaledAmount(1L).setCurrency(Currency.EUR).build)
        .build()
      barcode
    })
      .toDS()

    val x = ds.map(a => {
      (
        a.getBarcode(),
        a.getPrdTaxVal())
    })

    println(s"head: ${x.collect().toList.head}")

  }

  "case class XUser encoder" must "pass tests" in {
    import org.apache.spark.sql.Dataset

    val spark = SparkSession.builder.master("local[2]").getOrCreate()
    import spark.implicits._

    val ds: Dataset[XUser] = 0.until(10000).map(i => XUser("Foo", i, "green")).toDS()

    val x = ds.map(x => {
      (x.name, x.favorite_color, x.favorite_number)
    })

    val count = x.count()

    println(s"count: $count head: ${x.collect().toList.head}")

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

case class XUser(name: String, favorite_number: Int, favorite_color: String)