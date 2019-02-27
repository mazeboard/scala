package com.mazeboard.json

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._
import org.scalatest.{ FlatSpec, Matchers }

class JsonReaderSpec
  extends FlatSpec
  with Matchers {

  val jsonReader1 = new JsonReader("{a:1,b:[2,3],c:foo}")
  val jsonReader2 = new JsonReader("{a:1,b:[2,3],c:foo,d:3.0,e:{a:1,b:2,c:3},f:[{a:1,b:2},{a:3,b:4}]}")
  val jsonReader3 = new JsonReader("{a:[{a:1.0,b:2}]}")

  behavior of "JsonReader"

  it should "pass tests" in {

    assert(new JsonReader("[1,2,3]")[List[Int]] ==
      List(1, 2, 3))

    assert(new JsonReader("{a:1,b:2,c:3}")[Map[String, Int]] ==
      Map("a" -> 1, "b" -> 2, "c" -> 3))

    assert(new JsonReader("{a:1,b:2,c:3}")[Map[String, String]] ==
      Map("a" -> "1", "b" -> "2", "c" -> "3"))

    assert(jsonReader1[Foo] ==
      Foo(a = 1, b = List(2, 3), c = "foo"))

    assert(jsonReader2[Foo] ==
      Foo(a = 1, b = List(2, 3), c = "foo", d = 3.0))

    assert(jsonReader2[Bar] ==
      Bar(a = 1, b = List(2, 3), c = "foo", d = 3.0, e = Map("a" -> 1, "b" -> 2, "c" -> 3)))

    assert(jsonReader2[Dar] ==
      Dar(a = 1, b = List(2, 3), c = "foo", d = 3.0, e = Map("a" -> 1, "b" -> 2, "c" -> 3),
        f = List(Map("a" -> 1, "b" -> 2), Map("a" -> 3, "b" -> 4))))

    assert(jsonReader3[Goo] == Goo(a = List(Map("a" -> 1.0, "b" -> 2.0))))

  }

}

case class Foo(a: Int, b: List[Int], c: String, d: Double = 1.0)
case class Bar(a: Int, b: List[Int], c: String, d: Double = 1.0, e: Map[String, Int])
case class Dar(a: Int, b: List[Int], c: String, d: Double = 1.0, e: Map[String, Int], f: List[Map[String, Int]])
case class Goo(a: List[Map[String, Double]])