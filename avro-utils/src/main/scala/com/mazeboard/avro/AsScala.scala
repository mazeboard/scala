package com.mazeboard.avro

object AsScala {
  def toList[T](list: java.util.List[T]): scala.collection.immutable.List[T] = {
    if (list == null)
      null
    else {
      val buf = scala.collection.mutable.ListBuffer.empty[T]
      val iter = list.listIterator()
      while (iter.hasNext) {
        buf += iter.next()
      }
      buf.toList
    }
  }

  def toMap[K, V](m: java.util.Map[K, V]): scala.collection.immutable.Map[K, V] = {
    if (m == null)
      null
    else {
      val buf = new scala.collection.mutable.HashMap[K, V]()
      val iter = m.entrySet().iterator()
      while (iter.hasNext) {
        val entry = iter.next()
        buf.update(entry.getKey, entry.getValue)
      }
      buf.toMap
    }
  }

}