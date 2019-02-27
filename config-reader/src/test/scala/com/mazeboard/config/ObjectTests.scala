package com.mazeboard.config

import com.mazeboard.config.ConfigReader._
import com.typesafe.config._
import org.apache.spark.SparkConf

import scala.reflect.runtime.universe._

object ObjectTests {
  class MyConfigReader(config: Config) extends ConfigReader(config) {
    override def reader[T: TypeTag](obj: Config): T = {
      (typeOf[T] match {
        case t if t =:= typeOf[SparkConf] => {
          val m = getMap[String](obj).mapValues(x => unwrap(x).toString)
          new SparkConf().setAll(m)
        }
        case _ => super.reader(obj)
      }).asInstanceOf[T]
    }
  }

  abstract class Animal(val name: String) {
    override def equals(obj: Any): Boolean = {
      obj match {
        case x: Animal => x.name == this.name
        case _ => false
      }
    }
  }

  object Animal {
    def apply(name: String): Animal = {
      name match {
        case _ if List("Hummingbird", "Parrot", "Heron").contains(name) => new Bird(name)
        case _ if List("Pygmy", "Vervet", "Squirrel").contains(name) => new Monkey(name)
        case _ => new UnknownAnimal(name)
      }
    }
  }

  class Bird(name: String) extends Animal(name)

  class Monkey(name: String) extends Animal(name)

  class UnknownAnimal(name: String) extends Animal(name)

  case class Food(food: String)

  case class Foo(a: Int)

  object Foo {
    def apply(x: String) = new Foo(x.length)
  }

  case class TestA(adtlist: List[MyTestA])

  case class MyTestA(bytes: Bytes, maps: Option[List[Map[String, List[Bytes]]]])

  case class TestB(a: Int, b: Option[Int])

  case class Test(
    port: Option[Port],
    map: Map[String, String],
    maplistbytes: Map[String, List[Bytes]],
    adtlist: List[MyTest],
    adtmap: Map[String, MyTest],
    sparkConf: Map[String, String])

  case class MyTest(
    `type`: String,
    b: Int,
    bytes: Bytes,
    durations: Option[List[DurationMilliSeconds]],
    doesnotexist: Int = 100,
    map: Map[String, String],
    maps: Option[List[Map[String, List[Bytes]]]],
    count: Long,
    counts: List[Long])

  class MySparkConf(sparkConf: Map[String, String])
    extends SparkConf(false) {
    assert(sparkConf.contains("spark.app.name"))
    this.setAll(sparkConf)

    override def toString(): String = {
      s"MySparkConf(${sparkConf})"
    }
  }

  case class MyTestDefault(bar: Double = 1.7, foo: Int = 10)

  case class MyTestDefaultFloat(bar: Float = 1.2F, foo: Int = 100)

  case class Point(x: Double, y: Double)

  case class Adt(b: Int)

  case class Port(value: Int) extends AnyVal

  case class MyPort(port: Port)

  case class MyClass(
    boolean: Boolean,
    port: Port,
    adt: Adt,
    list: List[Double],
    map: Map[String, List[Adt]],
    option: Option[String] = Some("foo"))

  class MyPoint(val x: Double, val y: Double) {
    def distance(p: MyPoint): Double = {
      math.sqrt(math.pow(x - p.x, 2) + math.pow(y - p.y, 2))
    }

    override def equals(obj: Any): Boolean = {
      obj match {
        case x: MyPoint => this.x == x.x && this.y == x.y
        case _ => false
      }
    }

    override def toString: String = s"MyPoint($x, $y)"
  }

  case class ConfMyPoint(a: MyPoint, b: MyPoint) {
    def distance(): Double = a.distance(b)
  }

  class MyInt(val value: Int) {
    override def equals(obj: Any): Boolean = {
      obj match {
        case x: MyInt => this.value == x.value
        case _ => false
      }
    }

    override def toString: String = s"MyInt($value)"
  }

  case class ConfInt(n: MyInt)

  case class YClass(val x: Long, val y: Long = 1L) {
    override def equals(obj: Any): Boolean = {
      obj match {
        case x: YClass => this.x == x.x && this.y == x.y
        case _ => false
      }
    }
  }

  object YClass {
    val foo = 5L

    def apply(x: Double, y: Double): YClass = {
      YClass(x.round, y.round)
    }

    def apply(x: String): YClass = {
      YClass(x.length.toLong, foo)
    }
  }

  class XClass(val x: Long, val y: Long) {
    def this(x: Long, y: String = "foo") {
      this(x, y.length.toLong)
    }

    override def equals(obj: Any): Boolean = {
      obj match {
        case x: XClass => this.x == x.x && this.y == x.y
        case _ => false
      }
    }

    override def toString() = s"XClass($x,$y)"
  }

  object XClass {
    def apply(x: String): XClass = {
      new XClass(x.length.toLong + 1L, 1L)
    }

    def apply(x: Double, y: Int): XClass = {
      new XClass(x.round, y.toLong)
    }

    def apply(x: Double, y: Double, z: Int = 10): XClass = {
      new XClass(x.round, y.round + z.toLong)
    }

    def bar(x: String = "bar"): Int = {
      x.length
    }

  }

  class ZClass(val port: Port) {
    override def equals(obj: Any): Boolean = {
      obj match {
        case x: ZClass => this.port == x.port
        case _ => false
      }
    }

    override def toString(): String = s"ZClass($port)"
  }

  class WClass(val ports: List[Port]) {
    override def equals(obj: Any): Boolean = {
      obj match {
        case x: WClass => this.ports == x.ports
        case _ => false
      }
    }

    override def toString(): String = s"WClass($ports)"
  }

  class UClass[T](val data: T) {
    override def toString(): String = s"UClass(${data.toString})"
    override def equals(obj: Any): Boolean = {
      obj match {
        case x: UClass[T] => this.data == x.data
        case _ => false
      }
    }
  }

  final class FloatValue(val value: Float) extends AnyVal

  final class PrivateFloatValue private (val value: Float) extends AnyVal

  final class DoubleValue(val value: Double) extends AnyVal

  final class PrivateDoubleValue private (val value: Double) extends AnyVal

  class DClass(val x: Map[String, Int] = Map()) {
    override def toString(): String = s"DClass($x)"
    override def equals(obj: Any): Boolean = {
      obj match {
        case x: DClass => this.x == x.x
        case _ => false
      }
    }

  }

}
