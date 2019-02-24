package com.mazeboard.spark.utils

import org.apache.spark.sql._
import scala.reflect.api
import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror
import org.apache.avro.specific.SpecificRecordBase

/**
 * load a Seq[T], or a Map[_, T] from a Dataframe
 */
object DataFrameSupport {

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

  private def _load[T](row: Row)(implicit ttag: TypeTag[T]): T = {
    val tpe = typeOf[T]
    val classMirror = currentMirror.reflectClass(tpe.typeSymbol.asClass)
    val constructor = tpe.decls.filter(m ⇒ m.isConstructor && m.asMethod.isPrimaryConstructor).head.asMethod

    val cols = constructor.typeSignature.paramLists.head
    val elems: Seq[AnyRef] = cols.map(c => {
      val index = row.fieldIndex(c.name.toString)
      c.typeSignature match {
        case t if t <:< typeOf[Seq[_]] =>
          implicit val ttag: TypeTag[_] = getTypeTag(t.typeArgs.head)
          convert(row.getAs[Seq[Row]](index).map(row => _load(row)): _*)
        case t if t <:< typeOf[Number]
          || t =:= typeOf[Int]
          || t =:= typeOf[Long]
          || t =:= typeOf[Double]
          || t =:= typeOf[Float]
          || t =:= typeOf[Boolean]
          || t =:= typeOf[String] => row.get(index)
        case t =>
          implicit val ttag: TypeTag[_] = getTypeTag(t)
          _load(row.getAs[Row](index))
      }
    }.asInstanceOf[AnyRef])
    createInstance[T](elems: _*)
  }

  implicit class DataFrameSupportRich(df: DataFrame) {
    def load[T: Encoder: TypeTag]: Seq[T] = {
      convert(df.map(_load[T]).collect(): _*)
    }

    def loadMap[S: Encoder: TypeTag, T: Encoder: TypeTag](getKey: T ⇒ S)(implicit sqlContext: SQLContext): Map[S, T] = {
      import sqlContext.implicits._
      df.map(_load[T])
        .map(x ⇒ (getKey(x), x))
        .collect()
        .toMap
    }

  }

}
