## spark-utils

##### AvroSupport

create class instances from avro objects

##### DataFrameSupport

create class instances from a dataframe

##### MyBroadcast

concurrent broadcast update

## object-reader

create class instance from object

## config-reader

Like PureConfig, config-reader loads Typesafe Config configurations.

Compared to PureConfig, config-reader has less boilerplate with a simple implementation based on object-reader.

##### examples

```
val config = new ConfigReader(ConfigFactory.parseString("{foo:{a:1}}"))
val myObj = config.foo[MyObj]
assert(myObj == MyObj(a = 1, b = 0))

val myFoo = config[Foo]
assert(myFoo == Foo(foo = MyObj(a = 1, b = 0)))

val myBar = config[Map[Bar, MyObj]]
assert(myBar == Map(Bar("foo") -> MyObj(a = 1, b = 0)))

case class MyObj(a:Int, b:Int = 0)
case class Foo(foo:MyObj)
case class Bar(value: String)
```

To create a SparkConf there is two alternatives:

```
  val sparkConf: MySparkConf = new ConfigReader(ConfigFactory.parseString("{spark.master: local[2], spark.app.name: test}"))[MySparkConf]

  class MySparkConf(sparkConf: Map[String, String]) extends SparkConf(false) {
    assert(sparkConf.contains("spark.app.name"))
    this.setAll(sparkConf)

    override def toString(): String = {
      s"MySparkConf(${sparkConf})"
    }
  }
```

Or by extending ConfigReader and adding a new SprkConf reader

```
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
  
  val sparkConf: SparkConf = new MyConfigReader(ConfigFactory.parseString("{spark.master: local[2], spark.app.name: test}"))[SparkConf]
```

## json-reader

load class instance from a json string

##### example

```
val config = new JsonReader("{a:1, b:[1,2,3]}")
assert(config[MyObj] == MyObj(a = 1, b = List(1, 2, 3)))

case class MyObj(a: Int, b: List[Int])
```

## tests

##### AvroDataFrameSpec

##### WordCountScalaExample (Kafka steams)

