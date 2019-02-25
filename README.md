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

##### example

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

