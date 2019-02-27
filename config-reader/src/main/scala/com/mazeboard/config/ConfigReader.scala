package com.mazeboard.config

import java.util.concurrent.TimeUnit._

import com.mazeboard.config.ConfigReader._
import com.mazeboard.reader.ObjectReader
import com.typesafe.config.{ ConfigException, _ }

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._

/**
 *
 * @param config
 */
class ConfigReader(config: Config) extends ObjectReader[Config](config.atKey(ID)) {

  override def newInstance[T](obj: Config): T with ObjectReader[Config] = {
    new ConfigReader(obj.getConfig(ID)).asInstanceOf[T with ObjectReader[Config]]
  }

  override def getReaderTypeTag(): TypeTag[ConfigReader] = typeTag[ConfigReader]

  override def unwrap(obj: Config): AnyRef = obj.getAnyRef(ID)

  override def getValue(name: String, obj: Config): Config = {
    try {
      obj.getConfig(ID).getValue(name).atKey(ID)
    } catch {
      case e: ConfigException.Missing => throw new Missing(e)
      case e: ConfigException.WrongType => throw new InvalidObject(e)
      case e: Throwable => throw e
    }
  }

  override def getList(obj: Config): List[Config] = {
    try {
      obj.getAnyRefList(ID).toArray().toList.map(x => ConfigFactory.parseString(s"{$ID: $x}"))
    } catch {
      case e: ConfigException.WrongType => throw new InvalidObject(e)
    }
  }

  override def getMap[T](obj: Config)(implicit ttag: TypeTag[T]): Map[String, Config] = {
    val entries = typeOf[T] match {
      case t if t =:= typeOf[String] || t.erasure <:< typeOf[AnyVal] =>
        obj.getConfig(ID).entrySet()
      case _ =>
        obj.getConfig(ID).root.entrySet()
    }
    entries.asScala.map(entry => entry.getKey -> entry.getValue.atKey(ID)).toMap
  }

  override def reader[T](obj: Config)(implicit ttag: TypeTag[T]): T = {
    (typeOf[T] match {
      case t if t =:= typeOf[Boolean] =>
        obj.getBoolean(ID)
      case t if t =:= typeOf[Bytes] =>
        obj.getBytes(ID)
      case t if t =:= typeOf[DurationNanoSeconds] =>
        obj.getDuration(ID, NANOSECONDS)
      case t if t =:= typeOf[DurationMicroSeconds] =>
        obj.getDuration(ID, MICROSECONDS)
      case t if t =:= typeOf[DurationMilliSeconds] =>
        obj.getDuration(ID, MILLISECONDS)
      case t if t =:= typeOf[DurationSeconds] =>
        obj.getDuration(ID, SECONDS)
      case t if t =:= typeOf[DurationMinutes] =>
        obj.getDuration(ID, MINUTES)
      case t if t =:= typeOf[DurationHours] =>
        obj.getDuration(ID, HOURS)
      case t if t =:= typeOf[DurationDays] =>
        obj.getDuration(ID, DAYS)
      case _ => throw new ReaderNotFound
    }).asInstanceOf[T]
  }
}

object ConfigReader {
  val ID = "id"

  sealed class _Bytes

  type Bytes = Long with _Bytes

  sealed class _DurationNanoSeconds

  type DurationNanoSeconds = Long with _DurationNanoSeconds

  sealed class _DurationMilliSeconds

  type DurationMilliSeconds = Long with _DurationMilliSeconds

  sealed class _DurationMicroSeconds

  type DurationMicroSeconds = Long with _DurationMicroSeconds

  sealed class _DurationSeconds

  type DurationSeconds = Long with _DurationSeconds

  sealed class _DurationMinutes

  type DurationMinutes = Long with _DurationMinutes

  sealed class _DurationHours

  type DurationHours = Long with _DurationHours

  sealed class _DurationDays

  type DurationDays = _DurationDays
}
