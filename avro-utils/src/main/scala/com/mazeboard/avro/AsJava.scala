package com.mazeboard.avro

object AsJava {
  def toList[T](seq: Seq[T]): java.util.List[T] = {
    val jl = new java.util.ArrayList[T]()
    seq.foreach(jl.add(_))
    jl
  }

  def toMap[K, V](m: Map[K, V]): java.util.Map[K, V] = {
    val jm = new java.util.HashMap[K, V]()
    m.foreach {
      case (k, v) => jm.put(k, v)
    }
    jm
  }
}