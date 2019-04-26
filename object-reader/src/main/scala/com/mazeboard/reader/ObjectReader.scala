package com.mazeboard.reader

import scala.language.dynamics
import scala.reflect.api
import scala.reflect.runtime.universe._
import scala.reflect.runtime.currentMirror

/**
 *
 * @param root
 * @tparam X
 */
abstract class ObjectReader[X](root: X) extends Dynamic {

  class Missing(e: Throwable) extends Throwable(e)

  class InvalidObject(e: Throwable) extends Throwable(e)

  class ReaderNotFound extends Throwable("reader not found")

  class NoMatch extends Throwable("no match")

  def newInstance[T](obj: X): T with ObjectReader[X] = {
    throw new UnsupportedOperationException("newInstance is not defined")
  }

  def getReaderTypeTag: TypeTag[_] = {
    throw new UnsupportedOperationException("getReaderTypeTag is not defined")
  }

  def reader[T](obj: X)(implicit ttag: TypeTag[T]): T = throw new ReaderNotFound

  def unwrap(obj: X): AnyRef = obj.asInstanceOf[AnyRef]

  def getList(obj: X): List[X] = throw new UnsupportedOperationException("getList is not defined")

  def getMap[T](obj: X)(implicit ttag: TypeTag[T]): Map[_, X] = throw new UnsupportedOperationException("getMap is not defined")

  def getValue(name: String, obj: X): X

  private def getObjectTypeTag[T: TypeTag](x: T): TypeTag[T] = typeTag[T]

  def apply[T: TypeTag](): T = {
    implicit val obj: X = root
    dispatch[T]()
  }

  def applyDynamic[T: TypeTag](name: String)(default: T): T = {
    try {
      implicit val obj: X = getValue(name, root)
      dispatch[T](default)
    } catch {
      case e: Missing => {
        if (default == null) {
          if (typeOf[T].typeSymbol == typeOf[Option[_]].typeSymbol)
            None.asInstanceOf[T]
          else throw e
        } else default
      }
    }
  }

  def selectDynamic[T: TypeTag](name: String): T = {
    applyDynamic[T](name)(null.asInstanceOf[T])
  }

  def dispatch[T](default: T = null.asInstanceOf[T])(implicit ttag: TypeTag[T], obj: X): T = {
    (try {
      reader[T](obj)
    } catch {
      case _: ReaderNotFound =>
        typeOf[T] match {
          case t if t =:= getReaderTypeTag.tpe => newInstance(obj)
          case t if t <:< typeOf[List[_]] => {
            val tpe = t.typeArgs.head
            getList(obj).map(xobj => {
              dispatch()(getTypeTag(tpe), xobj)
            })
          }
          case t if t <:< typeOf[Map[_, _]] => {
            val tpeKey = t.typeArgs.head
            implicit val ttag: TypeTag[_] = getTypeTag(t.typeArgs.drop(1).head)
            val m = getMap(obj)
            m.map {
              case (k, v) =>
                implicit val obj: X = v
                tpeKey match {
                  case t if t =:= typeOf[String] || t.erasure <:< typeOf[AnyVal] =>
                    (k, dispatch())
                  case _ =>
                    (
                      createInstance(tpeKey, (method: MethodSymbol) => k.asInstanceOf[AnyRef]),
                      dispatch())
                }
            }
          }
          case t if t.typeSymbol == typeOf[Option[_]].typeSymbol =>
            Some(dispatch()(getTypeTag(t.typeArgs.head), obj))
          case t if t =:= typeOf[Int] => unwrap(obj).toString.toInt
          case t if t =:= typeOf[Long] => unwrap(obj).toString.toLong
          case t if t =:= typeOf[Double] => unwrap(obj).toString.toDouble
          case t if t =:= typeOf[Float] => unwrap(obj).toString.toFloat
          case t if t =:= typeOf[Number] => unwrap(obj).toString.toInt // TODO what Number ?
          case t if t =:= typeOf[Boolean] => unwrap(obj).toString.toBoolean
          case t if t =:= typeOf[String] => unwrap(obj).toString
          case _ => load[T]
        }
      case e: Throwable =>
        throw e
    })
      .asInstanceOf[T]
  }

  // Note: a class has a companion when:
  // 1. there is an object with the same name in the same file,
  // 2. if a constructor has parameters with default values (the functions that return the default value is defined
  //  in the companion object)

  private def load[A](implicit ttag: TypeTag[A], obj: X): A = {
    val tpe = ttag.tpe
    (getConstructors(tpe)
      .sortWith((m1, m2) =>
        m1.isPrimaryConstructor
          || (!m2.isPrimaryConstructor &&
            (m1.isConstructor
              || (m1.paramLists.head.length > m2.paramLists.head.length))))
      .map(m => (m, null.asInstanceOf[List[(AnyRef, Boolean)]]))
      .fold(null)((a, b) => {
        if (a != null)
          a
        else {
          (b match {
            case (m, _) =>
              (m, {
                m.paramLists.head.zipWithIndex.map {
                  case (param, index) =>
                    try {
                      val xobj = getValue(param.name.toString, obj)
                      (dispatch()(getTypeTag(param.typeSignature), xobj).asInstanceOf[AnyRef], false)
                    } catch {
                      case e: Missing =>
                        if (param.asTerm.isParamWithDefault) {
                          val d = tpe.companion.decl(TermName(s"${m.name.encodedName}$$default$$${index + 1}")).asMethod
                          (instanceMirror(tpe).reflectMethod(d)().asInstanceOf[AnyRef], true)
                        } else if (param.typeSignature <:< typeOf[Option[_]]) {
                          (None, false)
                        } else (null, false)
                      case e: InvalidObject =>
                        (null, false)
                      case e: Throwable =>
                        (null, false)
                    }
                }
              })
          }) match {
            case (m, list) if list.forall(_._1 != null) && !list.forall(_._2) => (m, list)
            case _ => a
          }
        }
      }) match {
        case e if e == null =>
          // create an instance with obj as parameter (if tpe has a constructor or an apply method that accepts one parameter)
          createInstance(tpe, (method: MethodSymbol) => {
            implicit val ttag: TypeTag[_] = getTypeTag(method.paramLists.head.head.typeSignature.asSeenFrom(tpe, tpe.typeSymbol))
            dispatch().asInstanceOf[AnyRef]
          })
        case (method, args) =>
          invoke(tpe, method, args.map(_._1): _*)
      })
      .asInstanceOf[A]
  }

  private def invoke(tpe: Type, method: MethodSymbol, args: AnyRef*) = {
    method match {
      case constructor if constructor.isConstructor =>
        classMirror(tpe).reflectConstructor(constructor)(args: _*)
      case method =>
        try {
          instanceMirror(tpe).reflectMethod(method)(args: _*)
        } catch { // Scala reflection can't invoke Java static methods
          case _: java.lang.ClassNotFoundException =>
            val classz: Class[_] = currentMirror.runtimeClass(tpe.typeSymbol.asClass)
            classz.getMethod(
              method.name.decodedName.toString,
              method.paramLists.flatMap(x => x)
                .map(x => currentMirror.runtimeClass(x.typeSignature.typeSymbol.asClass)): _*)
              .invoke(null, args: _*)
        }
    }
  }

  private def classMirror(tpe: Type): ClassMirror = {
    currentMirror.reflectClass(tpe.typeSymbol.asClass)
  }

  /*
  Scala reflection can't invoke Java static methods
  m.instance fails with java.lang.ClassNotFoundException: <class>$
  the issue is that in scala the static module is suffixed by $
   */
  private def instanceMirror(tpe: Type): InstanceMirror = {
    val sm = currentMirror.staticModule(tpe.typeSymbol.fullName)
    val m = currentMirror.reflectModule(sm)
    currentMirror.reflect(m.instance)

  }

  private def createInstance(tpe: Type, arg: (MethodSymbol) => AnyRef) = {
    val method = getConstructors(tpe)
      .filter(_.paramLists.head.length == 1)
      .head
    invoke(tpe, method, arg(method))
  }

  private def getConstructors(tpe: Type): List[MethodSymbol] = {
    val methods = if (tpe.typeSymbol.isClass && !tpe.typeSymbol.isAbstract)
      tpe.decls.filter(_.isConstructor).map(_.asMethod).toList
    else
      List()
    val companionMethods = tpe.companion.decls.filter(m => m.isMethod && m.isStatic && m.typeSignature.resultType =:= tpe)
      .map(_.asMethod)
      .toList
    methods ++ companionMethods
  }

  private def getTypeTag(tpe: Type): TypeTag[_] = {
    val mirror = runtimeMirror(tpe.getClass.getClassLoader)
    TypeTag(mirror, new api.TypeCreator {
      def apply[U <: api.Universe with Singleton](m: api.Mirror[U]) =
        if (m eq mirror) tpe.asInstanceOf[U#Type]
        else throw new IllegalArgumentException(s"Type tag defined in $mirror cannot be migrated to other mirrors.")
    })
  }
}
