package com.mazeboard.spark.utils

import org.apache.spark.sql._
import scala.reflect.api
import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror
import org.apache.avro.specific.SpecificRecordBase

/**
 * load a Seq[T], or a Map[_, T] from a Dataframe
 */
object AvroSupport {

  import scala.collection.JavaConverters._

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

  def load[T: TypeTag](objs: Seq[SpecificRecordBase]): Seq[T] = convert(objs.map(_load[T]): _*)

  implicit class SpecificRecordBaseSupportSeqRich(objs: Seq[SpecificRecordBase]) {
    def load[T: TypeTag]: Seq[T] = convert(objs.map(_load[T]): _*)

    def loadMap[S, T: TypeTag](getKey: T ⇒ S): Map[S, T] =
      objs.map(_load[T]).map(x ⇒ (getKey(x), x)).toMap
  }

  implicit class SpecificRecordBaseSupportRich(obj: SpecificRecordBase) {
    def load[T: TypeTag]: T = _load[T](obj)
  }
}
