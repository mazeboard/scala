package com.mazeboard.avro

import scala.reflect.ClassTag
import scala.reflect.runtime.universe.{ TypeTag, typeTag }
import scala.reflect.api._
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.avro.specific.SpecificRecordBase
import org.apache.spark.sql.catalyst.{ ScalaReflection, expressions }
import org.apache.spark.sql.catalyst.analysis.{ GetColumnByOrdinal, UnresolvedAttribute, UnresolvedExtractValue }
import org.apache.spark.sql.catalyst.expressions.{ BoundReference, CreateNamedStruct, Expression, GetStructField, IsNull, UnsafeArrayData, UpCast }
import org.apache.spark.sql.catalyst.expressions.objects._
import org.apache.spark.sql.catalyst.util.{ DateTimeUtils, GenericArrayData }
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String
import scala.collection.Map

object AvroExpressionEncoder {
  /*  def apply[T <: SpecificRecordBase: TypeTag](): ExpressionEncoder[T] = {
    // We convert the not-serializable TypeTag into StructType and ClassTag.
    val mirror = ScalaReflection.mirror
    val tpe = typeTag[T].in(mirror).tpe
    val cls = mirror.runtimeClass(tpe)

    val inputObject = BoundReference(0, ScalaReflection.dataTypeFor[T], nullable = !cls.isPrimitive)
    val nullSafeInput = AssertNotNull(inputObject, Seq("top level Product input object"))

    val serializer = serializerFor[T](nullSafeInput)
    val deserializer = deserializerFor[T]

    val schema = serializer.dataType

    new ExpressionEncoder[T](
      schema,
      false,
      serializer.flatten,
      deserializer,
      ClassTag[T](cls))
  }

  def serializerFor[T <: SpecificRecordBase: TypeTag](inputObject: Expression): CreateNamedStruct = {
    val tpe = localTypeOf[T]
    val clsName = getClassNameFromType(tpe)
    val walkedTypePath = s"""- root class: "$clsName"""" :: Nil
    serializerFor(inputObject, tpe, walkedTypePath) match {
      case expressions.If(_, _, s: CreateNamedStruct) => s
    }
  }

  private def serializerFor(
    inputObject: Expression,
    tpe: `Type`,
    walkedTypePath: Seq[String],
    seenTypeSet: Set[`Type`] = Set.empty): Expression = cleanUpReflectionObjects {
    tpe.dealias match {
      case t if t <:< SpecificRecordBase => {

      }
      case _ =>
        try {
          ScalaReflection.serializerFor(inputObject, tpe, walkedTypePath)
        } catch {
          case _: UnsupportedOperationException =>
        }
    }
  }

  def deserializerFor[T: TypeTag]: Expression = {
    val tpe = localTypeOf[T]
    val clsName = getClassNameFromType(tpe)
    val walkedTypePath = s"""- root class: "$clsName"""" :: Nil
    val expr = ScalaReflection.deserializerFor(tpe, None, walkedTypePath)
    val Schema(_, nullable) = schemaFor(tpe)
    if (nullable) {
      expr
    } else {
      AssertNotNull(expr, walkedTypePath)
    }
  }
*/
}
