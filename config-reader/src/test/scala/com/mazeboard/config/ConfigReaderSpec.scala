package com.mazeboard.config

import com.mazeboard.config.ConfigReader._
import com.mazeboard.config.ObjectTests._
import com.typesafe.config._
import org.apache.spark.SparkConf
import org.scalatest.{ FlatSpec, Matchers }

import scala.util.Try

class ConfigReaderSpec
  extends FlatSpec
  with Matchers {

  behavior of "ConfigReader"

  it should "pass tests" in {
    testConfigReader
  }

  def testConfigReader() = {

    // TODO add java.nio.file.Path
    // TODO add java.util.regex.Pattern
    // TODO add scala.util.matching.Regex
    // TODO scala.math.BigDecimal and java.math.BigDecimal

    //assert(configFromString("""{x: "234.123"}""").x[scala.math.BigDecimal].compareTo(scala.math.BigDecimal("234.123")) == 0)
    //assert(configFromString("""{x: "234.123"}""").x[java.math.BigDecimal].compareTo(new java.math.BigDecimal("234.123")) == 0)

    assert(configFromString("""{level: "HIGH"}""").level[Level] == Level.HIGH)

    assert(configFromString("""{uuid: "123e4567-e89b-12d3-a456-556642440000"}""").uuid[java.util.UUID] == java.util.UUID.fromString("123e4567-e89b-12d3-a456-556642440000"))

    assert(configFromString("""{file: "/tmp/foo.txt"}""").file[java.io.File] == new java.io.File("/tmp/foo.txt"))

    assert(configFromString("""{x: "123456789"}""").x[scala.math.BigInt] == scala.math.BigInt(123456789))

    assert(configFromString("""{x: "123456789"}""").x[java.math.BigInteger] == new java.math.BigInteger("123456789"))

    assert(configFromString("{x: foo}")[Foo] == Foo(3))

    assert(configFromString("{x: 1.0}").x[Option[Double]] == Some(1.0))

    assert(configFromString("{x: 12.5}").foo[Double](1.0) == 1.0)

    assert(configFromString("{x: 2.7, y:3.1}")[Point] == Point(2.7, 3.1))

    assert(configFromString("{x: 2.7, y:3.1}").x[Double] == 2.7)

    assert(configFromString("{bar: 3.4}")[MyTestDefault] == MyTestDefault(3.4, 10))

    assert(configFromString("{bar: 1.5F}")[MyTestDefaultFloat] == MyTestDefaultFloat(1.5F, 100))

    assert(configFromString("{x: 1k}").x[Bytes] == 1024L)

    assert(Try(configFromString("{x: 2.5}")[Point]).isFailure)

    assert(configFromString("{x: [1k, 2k]}").x[List[Bytes]] == List(1024L, 2048L))

    assert(configFromString("{foo: 1, bar: 2}")[Map[String, Int]] == Map("foo" -> 1, "bar" -> 2))

    assert(configFromString("{x: {a:1k, b:2k}}").x[Map[String, Bytes]] == Map("b" -> 2048L, "a" -> 1024L))

    assert(configFromString("{port: 8080}").port[Port] == Port(8080))

    assert(configFromString("""{adtlist:[{bytes = "3k", maps = [{u:[1k,2k], v:[3k,4k]}]}]}""")[TestA] ==
      TestA(List(MyTestA(
        3072L.asInstanceOf[Bytes],
        Some(List(Map(
          "u" -> List(1024L.asInstanceOf[Bytes], 2048L.asInstanceOf[Bytes]),
          "v" -> List(3072L.asInstanceOf[Bytes], 4096L.asInstanceOf[Bytes]))))))))

    assert(configFromString("a: 1")[TestB] == TestB(1, None))

    val myTest = MyTest("adta", 5, 3072L.asInstanceOf[Bytes], Some(List(1L.asInstanceOf[DurationMilliSeconds], 1000L.asInstanceOf[DurationMilliSeconds], 3600000L.asInstanceOf[DurationMilliSeconds])), 100, Map("u" -> "foo", "v" -> "bar"), None, 2, List(15, 23))

    assert(configFromString("""{
             port = 8080
             map {
                foo = "bar"
                zoo = "dar"}
             maplistbytes {
                 foo = ["1k", "2k"]
                 bar = ["3k", "4k"]}
             sparkConf {
                 spark.master = "loacl[*]"
                 spark.app.name = "mySpark"}
             mySparkConf = ${sparkConf}
             adtlist = [${adtmap.adt1}]
             adtmap {
                  adt1 {
                    type = "adta"
                    b = 5
                    bytes = "3k"
                    durations = [ "1ms", "1s", "1h" ]
                    map = {u:foo, v:bar}
                    count = 2
                    counts = [15, 23]}
                 adt2 = ${adtmap.adt1}}}
              """)[Test] == Test(
      Some(Port(8080)),
      Map("zoo" -> "dar", "foo" -> "bar"),
      Map(
        "foo" -> List(1024L.asInstanceOf[Bytes], 2048L.asInstanceOf[Bytes]),
        "bar" -> List(3072L.asInstanceOf[Bytes], 4096L.asInstanceOf[Bytes])),
      List(myTest),
      Map("adt2" -> myTest, "adt1" -> myTest),
      Map("spark.master" -> "loacl[*]", "spark.app.name" -> "mySpark")))

    assert(configFromString("{a = {x:12,y:14} , b = {x:3,y:5}}")[ConfMyPoint] ==
      ConfMyPoint(new MyPoint(12.0, 14.0), new MyPoint(3.0, 5.0)))

    assert(configFromString(
      """{boolean = true
         port = 8080
         adt { b = 1 }
         list = [1, 0.2]
         map {key = [${adt}]}}""")[MyClass] ==
      MyClass(true, Port(8080), Adt(1), List(1.0, 0.2), Map("key" -> List(Adt(1))), Some("foo")))

    assert(configFromString("{ value: 1 }")[MyInt] == new MyInt(1))

    assert(configFromString("{x: hello}")[YClass] == YClass(5, 5))

    assert(configFromString("xclass: {x: hello}").xclass[XClass] == XClass(6, 1))

    assert(configFromString("{x: hello}")[XClass] == XClass(6, 1))

    assert(configFromString("{port: { value: 8080}}")[ZClass] == new ZClass(Port(8080)))

    assert(configFromString("{value: 8080}")[ZClass] == new ZClass(Port(8080)))

    assert(configFromString("{ports: [8080, 5050]}")[WClass] == new WClass(List(Port(8080), Port(5050))))

    assert(configFromString("{ x: 1, y:2}")[MyPoint] == new MyPoint(1.0, 2.0))

    assert(configFromString("{port:134}")[MyPort] == MyPort(Port(134)))

    assert(configFromString("{foo: toto, hello: world}")[Map[XClass, YClass]] ==
      Map(XClass(6, 1) -> YClass(5, 5), XClass(4, 1) -> YClass(4, 5)))

    assert(configFromString(
      """{"Toto": "Anything",
        |"Hummingbird": {"food": "worms"},
        |"Parrot": "Broccoli",
        |"Squirrel": "banana"}""".stripMargin)[Map[Animal, Food]] ==
      Map(
        new UnknownAnimal("Toto") -> Food("Anything"),
        new Bird("Hummingbird") -> Food("worms"),
        new Bird("Parrot") -> Food("Broccoli"),
        new Monkey("Squirrel") -> Food("banana")))

    assert(configFromString(
      """{spark.master: "local[*]", spark.app.name: "test sparkconf"}""")[MySparkConf].getAll.toList ==
      List(("spark.master", "local[*]"), ("spark.app.name", "test sparkconf")))

    assert(configFromString(
      """sparkConf: {spark.master: "local[*]", spark.app.name: "test sparkconf"}""")[MySparkConf].getAll.toList ==
      List(("spark.master", "local[*]"), ("spark.app.name", "test sparkconf")))

    assert(new MyConfigReader(ConfigFactory.parseString("{spark.master: local, spark.app.name: test}"))[SparkConf].getAll.toList ==
      List(("spark.master", "local"), ("spark.app.name", "test")))

    assert(configFromString("{a:1,b:2}")[UClass[Map[String, Int]]] == new UClass(Map("a" -> 1, "b" -> 2)))

    assert(configFromString("""{"Parrot": "Broccoli","Squirrel": "banana"}""".stripMargin)[UClass[Map[Animal, Food]]] ==
      new UClass(Map(new Bird("Parrot") -> Food("Broccoli"), new Monkey("Squirrel") -> Food("banana"))))

    assert(configFromString("{x:20, y:30}")[DClass] == new DClass(Map("y" -> 30, "x" -> 20)))

    assert(configFromString("{z:20, y:30}")[DClass] == new DClass(Map("y" -> 30, "z" -> 20)))
  }

  def configFromString(s: String) = {
    new ConfigReader(ConfigFactory.parseString(s).resolve())
  }
}
