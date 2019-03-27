package com.mazeboard.avro

object AsJava {
  def toList[T](list: scala.collection.immutable.List[T]): java.util.List[T] = {
    val jl = new java.util.ArrayList[T]()
    list.foreach(jl.add(_))
    jl
  }

  def toMap[K, V](m: scala.collection.immutable.Map[K, V]): java.util.Map[K, V] = {
    val jm = new java.util.HashMap[K, V]()
    m.foreach {
      case (k, v) => jm.put(k, v)
    }
    jm
  }
}