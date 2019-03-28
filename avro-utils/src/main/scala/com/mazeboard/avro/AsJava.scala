package com.mazeboard.avro

object AsJava {
  def toList[T](list: scala.collection.immutable.List[T]): java.util.List[T] = {
    if (list == null)
      null
    else {
      val jl = new java.util.ArrayList[T]()
      list.foreach(jl.add(_))
      jl
    }
  }

  def toMap[K, V](m: scala.collection.immutable.Map[K, V]): java.util.Map[K, V] = {
    if (m == null)
      null
    else {
      val jm = new java.util.HashMap[K, V]()
      m.foreach {
        case (k, v) => jm.put(k, v)
      }
      jm
    }
  }
}