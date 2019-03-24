package com.mazeboard.avro

import org.apache.avro.specific.SpecificRecordBase

import scala.language.experimental.macros
import scala.reflect.api
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

  // TODO

  def declare[T <: SpecificRecordBase]: Int = macro declare_Impl[T]

  def declare_Impl[T: c.WeakTypeTag](c: Context) = {
    import c.universe._
    def termName(x: c.Expr[String]) = x match { case Expr(Literal(Constant(x: String))) => TermName(x) }
    def typeName(x: c.Expr[String]) = x match { case Expr(Literal(Constant(x: String))) => TypeName(x) }

    /*val name = tag match { case Expr(Literal(Constant(xval: String))) => xval }
      val sym = TypeName(c.freshName(s"_Tag_${name}_"))
      val typetag = TypeName(name)
      val termtag = TermName(name)*/

    // if args is empty then declare a case class with all fields in avro type
    // otherwise declare case class with fields in args (use defaults if provided)

    /*val params = fields.fold("")((a, b) => { // TODO create a list of params (Expr)
      if (a == "") {
        s"$b: Int"
      } else {
        s"$a, $b: Int"
      }
    })*/

    //val q"..$stats" = q"""case class $caseClassName $params"""
    //q"$stats"
    //q"""case class ${typeName(caseClassName)} (..$params)"""

    val x = q"""{
      import org.apache.avro.specific.SpecificRecordBase
      case class Foo() extends Product {
        var stoEan: String = _
        var stoAnabelKey: String = _

        def productArity = 2

        @throws(classOf[IndexOutOfBoundsException])
        def productElement(n: Int) = n match {
          case 0 => stoEan
          case 1 => stoAnabelKey
          case _ => throw new IndexOutOfBoundsException(n.toString())
        }

        def canEqual(that: Any): Boolean = {
          that match {
            case x: Foo => x.stoEan == this.stoEan && x.stoAnabelKey == this.stoAnabelKey
            case _ => false
          }
        }


        def load(obj: SpecificRecordBase): Foo = {
          val o = new Foo
          o.stoEan = obj.get("stoEan").asInstanceOf[String]
          o.stoAnabelKey = obj.get("stoAnabelKey").asInstanceOf[String]
          o
        }
      }
      val abc:Int=123
      abc
    }"""
    println(x)
    x
  }

}
