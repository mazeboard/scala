package com.mazeboard.avro

import org.apache.avro.specific.SpecificRecordBase
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.catalyst.expressions.BoundReference
import org.apache.spark.sql.catalyst.expressions.objects.AssertNotNull

import scala.language.experimental.macros
import scala.reflect.{ ClassTag, api }
import scala.reflect.macros.blackbox.Context
import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

/**
 * load a Seq[T], or a Map[_, T] from a Avro objects
 */
object AvroSupport {

  private def getTypeTag(tpe: Type): TypeTag[_] = {
    val mirror = runtimeMirror(tpe.getClass.getClassLoader)
    TypeTag(mirror, new api.TypeCreator {
      def apply[U <: api.Universe with Singleton](m: api.Mirror[U]) =
        if (m eq mirror) tpe.asInstanceOf[U#Type]
        else throw new IllegalArgumentException(s"Type tag defined in $mirror cannot be migrated to other mirrors.")
    })
  }

  private def createInstance[T](elems: AnyRef*)(implicit ttag: TypeTag[T]): T = {
    val tpe = typeOf[T]
    val classMirror = currentMirror.reflectClass(tpe.typeSymbol.asClass)
    val constructor = tpe.decls.filter(m ⇒ m.isConstructor && m.asMethod.isPrimaryConstructor).head.asMethod
    val invoke = classMirror.reflectConstructor(constructor)
    invoke(elems: _*).asInstanceOf[T]
  }

  private def convert[T](x: T*)(implicit ttag: TypeTag[T]): Seq[T] = Seq[T](x: _*)

  private def _load[T](obj: SpecificRecordBase)(implicit ttag: TypeTag[T]): T = {
    val tpe = typeOf[T]
    val classMirror = currentMirror.reflectClass(tpe.typeSymbol.asClass)
    val constructor = tpe.decls.filter(m ⇒ m.isConstructor && m.asMethod.isPrimaryConstructor).head.asMethod

    val schema = obj.getSchema()
    val cols = constructor.typeSignature.paramLists.head
    val elems: Seq[AnyRef] = cols.map(c => {
      obj.get(c.name.toString) match {
        case v: SpecificRecordBase if !(c.typeSignature <:< typeOf[SpecificRecordBase]) =>
          implicit val ttag = getTypeTag(c.typeSignature)
          _load(v).asInstanceOf[AnyRef]
        case v => v
      }
    })
    createInstance[T](elems: _*)
  }

  implicit class SpecificRecordBaseSupportSeqRich(objs: Seq[SpecificRecordBase]) {
    def load[T: TypeTag]: Seq[T] = objs.map(_load[T])

    def loadMap[S, T: TypeTag](getKey: T ⇒ S): Map[S, T] =
      objs.map(_load[T]).map(x ⇒ (getKey(x), x)).toMap
  }

  implicit class SpecificRecordBaseSupportRich(obj: SpecificRecordBase) {
    def load[T: TypeTag]: T = _load[T](obj)
  }

  import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder

  def AvroExpressionEncoder[T <: SpecificRecordBase: TypeTag]() = {
    // We convert the not-serializable TypeTag into StructType and ClassTag.
    val mirror = ScalaReflection.mirror
    val tpe = typeTag[T].in(mirror).tpe
    val cls = mirror.runtimeClass(tpe)
    val inputObject = BoundReference(0, ScalaReflection.dataTypeFor[T], nullable = !cls.isPrimitive)
    val nullSafeInput = AssertNotNull(inputObject, Seq("top level Product input object"))
    val serializer = ScalaReflection.serializerFor[T](nullSafeInput)
    val deserializer = ScalaReflection.deserializerFor[T]

    val schema = serializer.dataType

    new ExpressionEncoder[T](
      schema,
      false,
      serializer.flatten,
      deserializer,
      ClassTag[T](cls))
  }

}
