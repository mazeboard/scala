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
import referential.product.v2.{ CrpAddDesc, CrpAttributes }

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

    val s = """{"type":"record","name":"Barcode","namespace":"referential.product.v2","doc":"Barcode Pivot","fields":[{"name":"barcode","type":["null","string"],"doc":"Barcode of the product","default":null},{"name":"transBarcode","type":["null","string"],"doc":"Origin EAN13","default":null},{"name":"barcodeType","type":["null","string"],"doc":"Barcode type","default":null},{"name":"itemKey","type":["null","string"],"doc":"Internal Code of the Product","default":null},{"name":"itemDesc","type":["null","string"],"doc":"Description of the product","default":null},{"name":"itemDesc18","type":["null","string"],"doc":"Description of the product (18c) on the ticket","default":null},{"name":"mainItemKey","type":["null","string"],"doc":"Item code of the parent product (ifexists)","default":null},{"name":"mainBarcode","type":["null","string"],"doc":"Main Barcode of the product","default":null},{"name":"mainSupplierKey","type":["null","string"],"doc":"Identifier of the Main Supplier","default":null},{"name":"supplierKey","type":["null","string"],"doc":"Identifier of the supplier","default":null},{"name":"itemType","type":["null","string"],"doc":"Type of item : [D] Direct, [L] Logistique, [M] Mix , [XD] Cross-docking, [PCK] Picking, [FT] Flow Through, [VFT] Virtual Flow Through [HD] Home Delivery","default":null},{"name":"brandTypeKey","type":["null","string"],"doc":"ID for the BrandType (MD,MN,PP)(seevalues)","default":null},{"name":"brandTypeDesc","type":["null","string"],"doc":"Description for the Brand Type","default":null},{"name":"createDate","type":["null","string"],"doc":"Creation date","default":null},{"name":"updateDate","type":["null","string"],"doc":"Update date.(YYYYMMDD)","default":null},{"name":"deleteDate","type":["null","string"],"doc":"Delete Date","default":null},{"name":"nonActiveDate","type":["null","string"],"doc":"Non active date(YYYYMMDD)","default":null},{"name":"prdStopFlag","type":["null","string"],"doc":"Flag STOP PRODUC T(for the sales and the purchase)","default":null},{"name":"salStopFlag","type":["null","string"],"doc":"Flag STOP ITEM on sales","default":null},{"name":"purStopFlag","type":["null","string"],"doc":"Flag STOP ITEM on purchase","default":null},{"name":"prdVatPct","type":["null","double"],"doc":"VAT percentage","default":null},{"name":"prdTaxVal","type":["null",{"type":"record","name":"Money","namespace":"common.lib.v1","fields":[{"name":"unscaledAmount","type":"long","default":0},{"name":"scale","type":"int","default":0},{"name":"currency","type":{"type":"enum","name":"Currency","symbols":["EUR","USD","BRL"]},"default":"EUR"},{"name":"currencyAlphaCode","type":["null","string"],"default":null}]}],"doc":"Other taxes amount","default":null},{"name":"prdVigVal","type":["null","common.lib.v1.Money"],"doc":"Vignette amount","default":null},{"name":"prdDutyVal","type":["null","common.lib.v1.Money"],"doc":"Duty amount","default":null},{"name":"prdUnitType","type":["null","string"],"doc":"Reference nature code","default":null},{"name":"frRacCode","type":["null","string"],"doc":"RAC code of the product (ANABEL2)","default":null},{"name":"npceBaseRist","type":["null","common.lib.v1.Money"],"doc":"Net price without tax for margin computation","default":null},{"name":"frTheoSrpPceOrig","type":["null","common.lib.v1.Money"],"doc":"Theoretical Break Even Price (SRP) without tax","default":null},{"name":"frTheoSrpPceMod","type":["null","common.lib.v1.Money"],"doc":"Theoretical Break Even Price (SRP) updated by RSRP+RFA","default":null},{"name":"structHyp","type":["null",{"type":"record","name":"ProductStructHyp","doc":"Product HyperMarket Structure","fields":[{"name":"hypUbKey","type":["null","string"],"doc":"Identifier of the UB (HyperMarket)","default":null},{"name":"hypUbDesc","type":["null","string"],"doc":"Description of the UB (HyperMarket)","default":null},{"name":"hypSubClassKey","type":["null","string"],"doc":"Identifier of the Sub Class (HyperMarket)","default":null},{"name":"hypSubClassDesc","type":["null","string"],"doc":"Description of the Sub Class (HyperMarket)","default":null},{"name":"hypClassKey","type":["null","string"],"doc":"Identifier of the Class (HyperMarket)","default":null},{"name":"hypClassDesc","type":["null","string"],"doc":"Description of the Class (HyperMarket)","default":null},{"name":"hypGrpClassKey","type":["null","string"],"doc":"Identifier of the Group Class (HyperMarket)","default":null},{"name":"hypGrpClassDesc","type":["null","string"],"doc":"Description of the Group Class (HyperMarket)","default":null},{"name":"hypDepartmentKey","type":["null","string"],"doc":"Identifier of the Department (HyperMarket)","default":null},{"name":"hypDepartmentDesc","type":["null","string"],"doc":"Description of the Department (HyperMarket)","default":null},{"name":"hypSectorKey","type":["null","string"],"doc":"Identifier of the Sector (HyperMarket)","default":null},{"name":"hypSectorDesc","type":["null","string"],"doc":"Description of the Sector (HyperMarket)","default":null}]}],"doc":"Structure Hyper","default":null},{"name":"structSup","type":["null",{"type":"record","name":"ProductStructSup","doc":"Product SuperMarket Structure","fields":[{"name":"supUbKey","type":["null","string"],"doc":"Identifier of the UB (SuperMarket)","default":null},{"name":"supUbDesc","type":["null","string"],"doc":"Description of the UB (SuperMarket)","default":null},{"name":"supSubClassKey","type":["null","string"],"doc":"Identifier of the Sub Class (SuperMarket)","default":null},{"name":"supSubClassDesc","type":["null","string"],"doc":"Description of the Sub Class (SuperMarket)","default":null},{"name":"supClassKey","type":["null","string"],"doc":"Identifier of the Class (SuperMarket)","default":null},{"name":"supClassDesc","type":["null","string"],"doc":"Description of the Class (SuperMarket)","default":null},{"name":"supGrpClassKey","type":["null","string"],"doc":"Identifier of the Group Class (SuperMarket)","default":null},{"name":"supGrpClassDesc","type":["null","string"],"doc":"Description of the Group Class (SuperMarket)","default":null},{"name":"supDepartmentKey","type":["null","string"],"doc":"Identifier of the Department (SuperMarket)","default":null},{"name":"supDepartmentDesc","type":["null","string"],"doc":"Description of the Department (SuperMarket)","default":null},{"name":"supSectorKey","type":["null","string"],"doc":"Identifier of the Sector (SuperMarket)","default":null},{"name":"supSectorDesc","type":["null","string"],"doc":"Description of the Sector (SuperMarket)","default":null}]}],"doc":"Structure Super","default":null},{"name":"structPrx","type":["null",{"type":"record","name":"ProductStructPrx","doc":"Product Proxi Structure","fields":[{"name":"prxUbKey","type":["null","string"],"doc":"Identifier of the UB (Proxi)","default":null},{"name":"prxUbDesc","type":["null","string"],"doc":"Description of the UB (Proxi)","default":null},{"name":"prxSubClassKey","type":["null","string"],"doc":"Identifier of the Sub Class (Proxi)","default":null},{"name":"prxSubClassDesc","type":["null","string"],"doc":"Description of the Sub Class (Proxi)","default":null},{"name":"prxClassKey","type":["null","string"],"doc":"Identifier of the Class (Proxi)","default":null},{"name":"prxClassDesc","type":["null","string"],"doc":"Description of the Class (Proxi)","default":null},{"name":"prxGrpClassKey","type":["null","string"],"doc":"Identifier of the Group Class (Proxi)","default":null},{"name":"prxGrpClassDesc","type":["null","string"],"doc":"Description of the Group Class (Proxi)","default":null},{"name":"prxHahDepartmentKey","type":["null","string"],"doc":"Identifier of the Department (Huit a Huit)","default":null},{"name":"prxHahDepartmentDesc","type":["null","string"],"doc":"Description of the Department (Huit a Huit)","default":null},{"name":"prxHahSectorKey","type":["null","string"],"doc":"Identifier of the Sector (Huit a Huit)","default":null},{"name":"prxHahSectorDesc","type":["null","string"],"doc":"Description of the Sector (Huit a Huit)","default":null},{"name":"prxShoDepartmentKey","type":["null","string"],"doc":"Identifier of the Department (Shopi)","default":null},{"name":"prxShoDepartmentDesc","type":["null","string"],"doc":"Description of the Department (Shopi)","default":null},{"name":"prxShoSectorKey","type":["null","string"],"doc":"Identifier of the Sector (Shopi)","default":null},{"name":"prxShoSectorDesc","type":["null","string"],"doc":"Description of the Sector (Shopi)","default":null}]}],"doc":"Structure Proxi","default":null},{"name":"structPrs","type":["null",{"type":"record","name":"ProductStructPrs","doc":"Product Proxi Structure (Shopi)","fields":[{"name":"prsUbKey","type":["null","string"],"doc":"Identifier of the UB (Shopi)","default":null},{"name":"prsUbDesc","type":["null","string"],"doc":"Description of the UB (Shopi)","default":null},{"name":"prsSubClassKey","type":["null","string"],"doc":"Identifier of the Sub Class (Shopi)","default":null},{"name":"prsSubClassDesc","type":["null","string"],"doc":"Description of the Sub Class (Shopi)","default":null},{"name":"prsClassKey","type":["null","string"],"doc":"Identifier of the Class (Shopi)","default":null},{"name":"prsClassDesc","type":["null","string"],"doc":"Description of the Class (Shopi)","default":null},{"name":"prsGrpClassKey","type":["null","string"],"doc":"Identifier of the Group Class (Shopi)","default":null},{"name":"prsGrpClassDesc","type":["null","string"],"doc":"Description of the Group Class (Shopi)","default":null},{"name":"prsDepartmentKey","type":["null","string"],"doc":"Identifier of the Department (Shopi)","default":null},{"name":"prsDepartmentDesc","type":["null","string"],"doc":"Description of the Department (Shopi)","default":null},{"name":"prsSectorKey","type":["null","string"],"doc":"Identifier of the Sector (Shopi)","default":null},{"name":"prsSectorDesc","type":["null","string"],"doc":"Description of the Sector (Shopi)","default":null}]}],"doc":"Product Proxi Structure (Shopi)","default":null},{"name":"structPrh","type":["null",{"type":"record","name":"ProductStructPrh","doc":"Product Proxi Structure (8a8)","fields":[{"name":"prhUbKey","type":["null","string"],"doc":"Identifier of the UB (8a8)","default":null},{"name":"prhUbDesc","type":["null","string"],"doc":"Description of the UB (8a8)","default":null},{"name":"prhSubClassKey","type":["null","string"],"doc":"Identifier of the Sub Class (8a8)","default":null},{"name":"prhSubClassDesc","type":["null","string"],"doc":"Description of the Sub Class (8a8)","default":null},{"name":"prhClassKey","type":["null","string"],"doc":"Identifier of the Class (8a8)","default":null},{"name":"prhClassDesc","type":["null","string"],"doc":"Description of the Class (8a8)","default":null},{"name":"prhGrpClassKey","type":["null","string"],"doc":"Identifier of the Group Class (8a8)","default":null},{"name":"prhGrpClassDesc","type":["null","string"],"doc":"Description of the Group Class (8a8)","default":null},{"name":"prhDepartmentKey","type":["null","string"],"doc":"Identifier of the Department (8a8)","default":null},{"name":"prhDepartmentDesc","type":["null","string"],"doc":"Description of the Department (8a8)","default":null},{"name":"prhSectorKey","type":["null","string"],"doc":"Identifier of the Sector (8a8)","default":null},{"name":"prhSectorDesc","type":["null","string"],"doc":"Description of the Sector (8a8)","default":null}]}],"doc":"Product Proxi Structure (8a8)","default":null},{"name":"structPmc","type":["null",{"type":"record","name":"ProductStructPmc","doc":"Product PromoCash Structure","fields":[{"name":"pmcUbKey","type":["null","string"],"doc":"Identifier of the UB (PromoCash)","default":null},{"name":"pmcUbDesc","type":["null","string"],"doc":"Description of the UB (PromoCash)","default":null},{"name":"pmcSubClassKey","type":["null","string"],"doc":"Identifier of the Sub Class (PromoCash)","default":null},{"name":"pmcSubClassDesc","type":["null","string"],"doc":"Description of the Sub Class (PromoCash)","default":null},{"name":"pmcClassKey","type":["null","string"],"doc":"Identifier of the Class (PromoCash)","default":null},{"name":"pmcClassDesc","type":["null","string"],"doc":"Description of the Class (PromoCash)","default":null},{"name":"pmcDepartmentKey","type":["null","string"],"doc":"Identifier of the Department (PromoCash)","default":null},{"name":"pmcDepartmentDesc","type":["null","string"],"doc":"Description of the Department (PromoCash)","default":null},{"name":"pmcSectorKey","type":["null","string"],"doc":"Identifier of the Sector (PromoCash)","default":null},{"name":"pmcSectorDesc","type":["null","string"],"doc":"Description of the Sector (PromoCash)","default":null}]}],"doc":"Structure PromoCash","default":null},{"name":"sectorKey","type":["null","string"],"doc":"Unique ID for Sector (Product Sector)","default":null},{"name":"sectorDesc","type":["null","string"],"doc":"Sector description","default":null},{"name":"departmentKey","type":["null","string"],"doc":"Unique ID of the department (Product Department)","default":null},{"name":"departmentDesc","type":["null","string"],"doc":"Department description","default":null},{"name":"grpClassKey","type":["null","string"],"doc":"Unique ID for Group Class (Product Group Family)","default":null},{"name":"grpClassDesc","type":["null","string"],"doc":"Group Class (Product Group Family) description","default":null},{"name":"classKey","type":["null","string"],"doc":"Unique ID for Class (Product Family)","default":null},{"name":"classDesc","type":["null","string"],"doc":"Class (Product Family) description","default":null},{"name":"subClassKey","type":["null","string"],"doc":"Unique ID of for product sub-class (Sub-Family)","default":null},{"name":"subClassDesc","type":["null","string"],"doc":"product sub-class (Sub-Family) description","default":null},{"name":"ubKey","type":["null","string"],"doc":"Unique ID for Unite Besoin (Product UB Family)","default":null},{"name":"ubDesc","type":["null","string"],"doc":"Description for level6 in French Unité de Besoin","default":null},{"name":"ubDescShort","type":["null","string"],"doc":"Short Description of the UB","default":null},{"name":"sizeKey","type":["null","string"],"doc":"Size code","default":null},{"name":"sizeDesc","type":["null","string"],"doc":"size description","default":null},{"name":"colorKey","type":["null","string"],"doc":"Color code","default":null},{"name":"colorDesc","type":["null","string"],"doc":"Color description","default":null},{"name":"prdCapaType","type":["null","string"],"doc":"Capacity unit code","default":null},{"name":"prdCapaVolume","type":["null","double"],"doc":"Capacity","default":null},{"name":"prdCapaFactor","type":["null","double"],"doc":"Number of lot components","default":null},{"name":"prdUnitDesc","type":["null","string"],"doc":"Reference nature description","default":null},{"name":"prdCapaDesc","type":["null","string"],"doc":"Capacity unit description","default":null},{"name":"prdTechNature","type":["null","string"],"doc":"Technical nature of the product","default":null},{"name":"prdLotHomoFlag","type":["null","string"],"doc":"Homogeneous lot indicator","default":null},{"name":"prdLotQtyFree","type":["null","double"],"doc":"Free Quantity in the lot","default":null},{"name":"prdLotAmtFree","type":["null","common.lib.v1.Money"],"doc":"Free Amount in the lot","default":null},{"name":"prdLotQtyTotal","type":["null","double"],"doc":"Total Quantity in the lot","default":null},{"name":"prdQtyFree","type":["null","double"],"doc":"Free Quantity in the product","default":null},{"name":"prdQtyTotal","type":["null","double"],"doc":"Total Quantity in the product","default":null},{"name":"prdEan7Prix","type":["null","string"],"doc":"EAN7 prix","default":null},{"name":"prdEan7Poids","type":["null","string"],"doc":"EAN7 poids","default":null},{"name":"prdVarWeightFlag","type":["null","string"],"doc":"Variable measure indicator","default":null},{"name":"catSubManagerKey","type":["null","string"],"doc":"First level of Category Management Hierarchy","default":null},{"name":"bemSubClassKey","type":["null","string"],"doc":"A unique identifier of a product BEM sub-class","default":null},{"name":"prxUbKey","type":["null","string"],"doc":"Identifier of the UB (Proxi)","default":null},{"name":"pdrUbKey","type":["null","string"],"doc":"Code UB Prodirest","default":null},{"name":"pmcUbKey","type":["null","string"],"doc":"Identifier of the UB (PromoCash)","default":null},{"name":"brandOwnerCode","type":["null","string"],"doc":"External Code of the brand owner","default":null},{"name":"brandOwnerKey","type":["null","string"],"doc":"Identifier of the brand owner","default":null},{"name":"brandOwnerDesc","type":["null","string"],"doc":"Description of the brand owner","default":null},{"name":"brandKey","type":["null","string"],"doc":"Brand code","default":null},{"name":"brandDesc","type":["null","string"],"doc":"Brand description","default":null},{"name":"brandSubTypeKey","type":["null","string"],"doc":"Sub-qualification key","default":null},{"name":"brandSubTypeDesc","type":["null","string"],"doc":"Sub-qualification Description","default":null},{"name":"dispoKey","type":["null","string"],"doc":"Disponibility code","default":null},{"name":"dispoDesc","type":["null","string"],"doc":"Disponibility description","default":null},{"name":"negoOrigKey","type":["null","string"],"doc":"Identifier of the origin of the négociation","default":null},{"name":"negoOrigDesc","type":["null","string"],"doc":"Description of the origin of the négociation","default":null},{"name":"pceLevelType","type":["null","string"],"doc":"Price Level type","default":null},{"name":"prdCompoDesc","type":["null","string"],"doc":"Description ot the product composition","default":null},{"name":"modelBarcode","type":["null","string"],"doc":"Barcode of the model (textile)","default":null},{"name":"modelDesc","type":["null","string"],"doc":"Description of the model (textile)","default":null},{"name":"classDescShort","type":["null","string"],"doc":"Class (Product Family) short description","default":null},{"name":"mainSupplierPrcKey","type":["null","string"],"doc":"Identifier of the Main Supplier for the previous exercise","default":null},{"name":"purStopDate","type":["null","string"],"doc":"Date of STOP ITEM on purchase","default":null},{"name":"bcpBrdTypeKey","type":["null","string"],"doc":"identifier of the qualification in BCP","default":null},{"name":"bcpBrdTypeDesc","type":["null","string"],"doc":"Description of the qualification in BCP","default":null},{"name":"bcpBrdQualifKey","type":["null","string"],"doc":"identifier of the brand qualification in BCP","default":null},{"name":"bcpBrdQualifDesc","type":["null","string"],"doc":"Description of the brand qualification in BCP","default":null},{"name":"bcpBrdSubQualifKey","type":["null","string"],"doc":"Identifier of the brand sub-qualification in BCP","default":null},{"name":"bcpBrdSubQualifDesc","type":["null","string"],"doc":"Description of the brand sub-qualification in BCP","default":null},{"name":"bcpHypAppelPrixCode","type":["null","string"],"doc":"National POS Price short code for Hypermarket","default":null},{"name":"bcpSupAppelPrixCode","type":["null","string"],"doc":"National POS Price short code for Supermarket","default":null},{"name":"supUgKey","type":["null","string"],"doc":"Caroline Super Stock Keeping Unit (UG) Key","default":null},{"name":"supUgDesc","type":["null","string"],"doc":"Caroline Super Stock Keeping Unit (UG) Description","default":null},{"name":"prdRefNature","type":["null","string"],"doc":"Product reference nature","default":null},{"name":"prdModKey","type":["null","string"],"doc":"Code of manufacturing mode","default":null},{"name":"prdModDesc","type":["null","string"],"doc":"Description of manufacturing mode","default":null},{"name":"productLineDesc","type":["null","string"],"doc":"Product Line Description","default":null},{"name":"brandMasterKey","type":["null","string"],"doc":"Identifier of the Master Brand","default":null},{"name":"brandMasterDesc","type":["null","string"],"doc":"Description of the Master Brand","default":null},{"name":"prdOrigOpKey","type":["null","string"],"doc":"Operational Identifier of the origin of the product","default":null},{"name":"prdOrigOpDesc","type":["null","string"],"doc":"Operational Description of the origin of the product","default":null},{"name":"mbcItemKey","type":["null","string"],"doc":"MBC Atica Hyper Stock Keeping Unit (AG) Key","default":null},{"name":"hypTypoKey","type":["null","string"],"doc":"Product Typology for Hypermarkets","default":null},{"name":"supTypoKey","type":["null","string"],"doc":"Product Typology for Supermarkets","default":null},{"name":"prxTypoKey","type":["null","string"],"doc":"Product Typology for Proximity Stores","default":null},{"name":"cacTypoKey","type":["null","string"],"doc":"Product Typology for Cash & Carry Stores","default":null},{"name":"prdPackFlag","type":["null","string"],"doc":"Divisible product pack flag","default":null},{"name":"trendCode","type":["null","string"],"doc":"Code of textile classification","default":null},{"name":"trendDesc","type":["null","string"],"doc":"Description of textile classification","default":null},{"name":"prdSeasonCode","type":["null","string"],"doc":"Code of the Season","default":null},{"name":"mbcSubClassKey","type":["null","string"],"doc":"MBC sub family","default":null},{"name":"mbcSizeCode","type":["null","string"],"doc":"Size of the article","default":null},{"name":"mbcColorCode","type":["null","string"],"doc":"Color of the article","default":null},{"name":"ubcKey","type":["null","string"],"doc":"Identifier of the Consumer Unit of Need (UBC OptiCAT)","default":null},{"name":"ubcDesc","type":["null","string"],"doc":"Description of the Consumer Unit of Need (UBC OptiCAT)","default":null},{"name":"hypUgKey","type":["null","string"],"doc":"Caroline Hyper Stock Keeping Unit (UG) Key","default":null},{"name":"hypUgDesc","type":["null","string"],"doc":"Caroline Hyper Stock Keeping Unit (UG) Description","default":null},{"name":"prxUgKey","type":["null","string"],"doc":"Proxy Stock Keeping Unit (UG) Key","default":null},{"name":"prxUgDesc","type":["null","string"],"doc":"Proxy Stock Keeping Unit (UG) Description","default":null},{"name":"pmcUgKey","type":["null","string"],"doc":"PromoCash Stock Keeping Unit (UG) Key","default":null},{"name":"pmcUgDesc","type":["null","string"],"doc":"PromoCash Stock Keeping Unit (UG) Description","default":null},{"name":"dctHypDirectFlag","type":["null","string"],"doc":"Indicates that permanent product should use Direct Flow in HyperMarket","default":null},{"name":"dctSupDirectFlag","type":["null","string"],"doc":"Indicates that permanent product should use Direct Flow in SuperMarket","default":null},{"name":"crpAttributes","type":["null",{"type":"array","items":{"type":"record","name":"CrpAttributes","doc":"CRP Product characteristics Attributes","fields":[{"name":"crpKey","type":["null","string"],"doc":"CRP Key","default":null},{"name":"crpCode","type":["null","string"],"doc":"CRP external code","default":null},{"name":"crpValueBegDate","type":["null","string"],"doc":"CRP value begin date YYYYMMDD","default":null},{"name":"crpValueEndDate","type":["null","string"],"doc":"CRP value finish date YYYYMMDD","default":null},{"name":"catCrpKey","type":["null","string"],"doc":"CRP category key","default":null},{"name":"catCrpDesc","type":["null","string"],"doc":"CRP category descrpition","default":null},{"name":"subCatCrpKey","type":["null","string"],"doc":"CRP subcategory key","default":null},{"name":"subCatCrpDesc","type":["null","string"],"doc":"CRP subcategory descrpition","default":null},{"name":"crpNatDesc","type":["null","string"],"doc":"CRP national description","default":null},{"name":"crpEngDesc","type":["null","string"],"doc":"CRP english description","default":null},{"name":"crpAddDesc","type":["null",{"type":"array","items":{"type":"record","name":"CrpAddDesc","doc":"CRP Product characteristics additionnal description","fields":[{"name":"crpAddNatDesc","type":["null","string"],"doc":"CRP national additional description","default":null},{"name":"crpAddEngDesc","type":["null","string"],"doc":"CRP english additional description","default":null}]}}],"doc":"Additional descriptions","default":null},{"name":"crpDesc","type":["null","string"],"doc":"CRP description","default":null},{"name":"crpValueFormat","type":["null","string"],"doc":"CRP Value format","default":null},{"name":"crpValueLength","type":["null","int"],"doc":"CRP Value length","default":null},{"name":"crpUnitNatSymbol","type":["null","string"],"doc":"Mesurement unit key national symbol","default":null},{"name":"crpUnitEngSymbol","type":["null","string"],"doc":"Mesurement unit key english symbol","default":null},{"name":"crpUnitNatDesc","type":["null","string"],"doc":"Mesurement unit key national description","default":null},{"name":"crpUnitEngDesc","type":["null","string"],"doc":"Mesurement unit key english description","default":null},{"name":"crpUnitNatValue","type":["null","string"],"doc":"CRP national value","default":null},{"name":"crpUnitEngValue","type":["null","string"],"doc":"CRP english value","default":null},{"name":"crpLink","type":["null","string"],"doc":"CRP link between values","default":null},{"name":"crpSourceKey","type":["null","string"],"doc":"CRP code from source","default":null},{"name":"crpSourceDesc","type":["null","string"],"doc":"CRP source description","default":null},{"name":"extDateTime","type":["null","string"],"doc":"extraction timestamp ISO8601 deprecated yyyy-MM-dd''T''HH:mm:ss.SSSZZ use yyyyMMdd''T''HHmmss.SSSZ","default":null}]}}],"doc":"CRP Product characteristics Attributes","default":null},{"name":"hypSalPceUnitType","type":["null","string"],"doc":"Hyper Sales Price level type (UVC, UCT)","default":null},{"name":"supSalPceUnitType","type":["null","string"],"doc":"Super Sales Price level type (UVC, UCT)","default":null},{"name":"prxSalPceUnitType","type":["null","string"],"doc":"Proxi Sales Price level type (UVC, UCT)","default":null},{"name":"pmcSalPceUnitType","type":["null","string"],"doc":"Cash and Carry Sales Price level type (UVC, UCT)","default":null},{"name":"hypPluCode","type":["null","string"],"doc":"Hyper PLU Code","default":null},{"name":"supPluCode","type":["null","string"],"doc":"Super PLU Code","default":null},{"name":"prxPluCode","type":["null","string"],"doc":"Proxi PLU Code","default":null},{"name":"pmcPluCode","type":["null","string"],"doc":"Cash and Carry PLU Code","default":null},{"name":"prdSupplierCode","type":["null","string"],"doc":"Reference of the product by the supplier","default":null},{"name":"prdMarketingDesc","type":["null","string"],"doc":"Marketing description of the product","default":null},{"name":"prdPackaging","type":["null","string"],"doc":"Packaging of the products","default":null},{"name":"prdMarketingSaleUnit","type":["null","string"],"doc":"Type of sale Unit","default":null},{"name":"prdVariableTypePrep","type":["null","string"],"doc":"Type of preparation variable","default":null},{"name":"prdPublishableFlag","type":["null","string"],"doc":"Publishable flag","default":null},{"name":"specialTaxType","type":["null","string"],"doc":"Product Special tax Type","default":null},{"name":"prdDutyType","type":["null","string"],"doc":"Product Duty tax Type","default":null},{"name":"vatCode","type":["null","string"],"doc":"Identier of VAT","default":null},{"name":"prdAlcoholProof","type":["null","string"],"doc":"Product Alcohol Proof","default":null},{"name":"prdShortDesc","type":["null","string"],"doc":"Product short description","default":null},{"name":"productServiceType","type":["null","string"],"doc":"Type of the Service","default":null},{"name":"productServiceDesc","type":["null","string"],"doc":"Description of the service","default":null},{"name":"productPurchaseSaleType","type":["null","string"],"doc":"Type of the product (Purchase/Sale)","default":null},{"name":"prdDryNetWeight","type":["null","double"],"doc":"Dry net weight of the sales unit","default":null},{"name":"prdPackWeight","type":["null","double"],"doc":"Weight of the packaged saled product","default":null},{"name":"prdPackHeight","type":["null","double"],"doc":"Height of the packaged saled product","default":null},{"name":"prdPackWidth","type":["null","double"],"doc":"Width of the packaged saled product","default":null},{"name":"prdPackLength","type":["null","double"],"doc":"Length of the packaged saled product","default":null},{"name":"srcTimestamp","type":["null","string"],"doc":"Source Timestamp","default":null},{"name":"trtTimestamp","type":["null","string"],"doc":"Processing Timestamp","default":null}]}"""
    val schema: Schema = new Schema.Parser().parse(s)
    val compiler = new SpecificCompiler(schema)
    compiler.setStringType(StringType.valueOf("String"))
    //compiler.compileToDestination(null, dest)
    val tt = compiler.javaUnbox(schema.getField("crpAttributes").schema())
    println(tt)
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
    import org.apache.spark.sql.Dataset

    val spark = SparkSession.builder.master("local[2]").getOrCreate()
    import spark.implicits._

    // enum Currency, new common.lib.v1.Currency() ?

    println("getConstructorParameters", ScalaReflection.getConstructorParameters(typeOf[Barcode]))
    implicit val barcodeEncoder = ExpressionEncoder[Barcode]()

    val ds: Dataset[Barcode] = 0.until(10000).map(i => {
      val crpAddDesc = new java.util.ArrayList[CrpAddDesc]()
      crpAddDesc.add(CrpAddDesc.newBuilder()
        .setCrpAddEngDesc(s"Crp description1 $i")
        .build())
      crpAddDesc.add(CrpAddDesc.newBuilder()
        .setCrpAddEngDesc(s"Crp description2 $i")
        .build())
      val crpAttrs = new java.util.ArrayList[CrpAttributes]()
      crpAttrs.add(CrpAttributes.newBuilder()
        .setCrpCode(s"crp attr1 $i")
        .setCrpAddDesc(crpAddDesc)
        .build())
      crpAttrs.add(CrpAttributes.newBuilder()
        .setCrpCode(s"crp attr2 $i")
        .build())
      val barcode = Barcode.newBuilder()
        .setBarcode(s"Bar$i")
        /*.setPrdTaxVal(Money.newBuilder()
          .setUnscaledAmount(i.toLong)
          .setScale(0)
          .setCurrency(Currency.EUR)
          .setCurrencyAlphaCode("EUR")
          .build())*/
        .setCrpAttributes(crpAttrs)
        .build()
      barcode
    })
      .toDS()

    val x = ds.map(a => {
      (
        a.barcode(),
        a.prdTaxVal(),
        a.crpAttributes())
    })

    println(x.collect().toList.drop(100).head)

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