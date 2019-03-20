package com.mazeboard.kafka

import org.apache.kafka.clients.consumer.{ ConsumerRecord, ConsumerRecords, KafkaConsumer }
import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }
import java.util
import java.time.Duration
import scala.reflect.runtime.universe._
import scala.reflect._
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream }

import com.typesafe.config.{ Config, ConfigFactory }
import com.mazeboard.config.ConfigReader
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.errors.AuthorizationException
import org.apache.kafka.common.errors.OutOfOrderSequenceException
import org.apache.kafka.common.errors.ProducerFencedException
import org.apache.kafka.common.KafkaException
import scala.util.Random
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.slf4j.LoggerFactory

// partitions migrate within the cluster
/*
To use the transactional producer and the attendant APIs, you must set the
transactional.id configuration property. If the transactional.id is set,
idempotence is automatically enabled along with the producer configs which
idempotence depends on. Further, topics which are included in transactions
should be configured for durability. In particular, the replication.factor
should be at least 3, and the min.insync.replicas for these topics should
be set to 2. Finally, in order for transactional guarantees to be realized
from end-to-end, the consumers must be configured to read only committed
messages as well.
 */
class DataStream(config: Config) {
  private val configReader = new ConfigReader(config)
  val logger = LoggerFactory.getLogger(this.getClass)
  val servers = configReader.`kafka.bootstrap.servers`[String]

  def apply[K: TypeTag, V: TypeTag](topic: String, keySerde: Serde[K], valueSerde: Serde[V]): () => DataStreamVariableKV[K, V] = {
    val producerProperties: util.Properties = new util.Properties
    val consumerProperties: util.Properties = new util.Properties
    consumerProperties.setProperty("bootstrap.servers", servers)
    producerProperties.setProperty("bootstrap.servers", servers)

    configReader.`kafka.producer`[Option[Map[String, String]]]
      .foreach(_.foreach {
        case (key, value) => producerProperties.setProperty(key, value)
      })

    configReader.`kafka.consumer`[Option[Map[String, String]]]
      .foreach(_.foreach {
        case (key, value) => consumerProperties.setProperty(key, value)
      })

    consumerProperties.put("enable.auto.commit", "false")
    consumerProperties.put("auto.offset.reset", "latest")
    //consumerProperties.put("partition.assignment.strategy", "RoundRobin") // Class not found
    // TODO the consumers must be configured to read only committed messages
    consumerProperties.put("key.deserializer", keySerde.deserializer().getClass.getName)
    consumerProperties.put("value.deserializer", valueSerde.deserializer().getClass.getName)
    producerProperties.put("key.serializer", keySerde.serializer().getClass.getName)
    producerProperties.put("value.serializer", valueSerde.serializer().getClass.getName)
    // TODO do not create DataStreamVariable - see withinTransaction
    () => new DataStreamVariableKV[K, V](topic, consumerProperties, producerProperties)
  }

  def apply[K: TypeTag, V: TypeTag](topic: String, valueSerde: Serde[V]): () => DataStreamVariableKV[K, V] = {
    apply[K, V](topic, getSerde[K], valueSerde)
  }

  def apply[K: TypeTag, V: TypeTag](topic: String): () => DataStreamVariableKV[K, V] = {
    apply[K, V](topic, getSerde[K], getSerde[V])
  }

  def apply[V: TypeTag](topic: String, valueSerde: Serde[V]): () => DataStreamVariableV[V] = {
    val producerProperties: util.Properties = new util.Properties
    val consumerProperties: util.Properties = new util.Properties
    consumerProperties.setProperty("bootstrap.servers", servers)
    producerProperties.setProperty("bootstrap.servers", servers)

    configReader.`kafka.producer`[Option[Map[String, String]]]
      .foreach(_.foreach {
        case (key, value) => producerProperties.setProperty(key, value)
      })

    configReader.`kafka.consumer`[Option[Map[String, String]]]
      .foreach(_.foreach {
        case (key, value) => consumerProperties.setProperty(key, value)
      })

    consumerProperties.put("enable.auto.commit", "false")
    consumerProperties.put("auto.offset.reset", "latest")
    //consumerProperties.put("partition.assignment.strategy", "RoundRobin") // Class not found
    consumerProperties.put("key.deserializer", Serdes.Long().deserializer().getClass.getName)
    consumerProperties.put("value.deserializer", valueSerde.deserializer().getClass.getName)
    producerProperties.put("key.serializer", Serdes.Long().serializer().getClass.getName)
    producerProperties.put("value.serializer", valueSerde.serializer().getClass.getName)
    // TODO do not create DataStreamVariable - see withinTransaction
    () => new DataStreamVariableV[V](topic, consumerProperties, producerProperties)
  }

  def apply[V: TypeTag](topic: String): () => DataStreamVariableV[V] = {
    apply[V](topic, getSerde[V])
  }

  private def getSerde[T: TypeTag]: Serde[T] = {
    (typeOf[T] match {
      case t if t =:= typeOf[String] => Serdes.String()
      case t if t =:= typeOf[Short] => Serdes.Short()
      case t if t =:= typeOf[Int] => Serdes.Integer()
      case t if t =:= typeOf[Long] => Serdes.Long()
      case t if t =:= typeOf[Float] => Serdes.Float()
      case t if t =:= typeOf[Double] => Serdes.Double()
      case t if t =:= typeOf[Array[Byte]] => Serdes.ByteArray()
      case t if t =:= typeOf[java.util.UUID] => Serdes.UUID()
      // TODO use kryo serde
      case _ => new IllegalArgumentException("Unknown class for built-in serializer. Supported types are: " + "String, Short, Integer, Long, Float, Double, ByteArray, UUID")
    })
      .asInstanceOf[Serde[T]]
  }

  /*
   at any point in time, all consumers in a group must read a distinct
   partition, otherwise committing offsets may lead to a loss of messages.
   Kafka Doc: """The way consumption is implemented in Kafka is by
   dividing up the partitions in the log over the consumer instances
   so that each instance is the exclusive consumer of a "fair share"
   of partitions at any point in time"""
  */

  //TODO create DataStreamVariable: set transaction.id for all DataStreamVariables
  def withinTransaction(window: Window, dss: (() => DataStreamVariable[_, _])*)(code: => Unit): Unit = {
    val dsss = dss.map(x => {
      x()
    })
    while (true) {
      try {
        import scala.collection.JavaConverters._

        dsss.foreach(_.begin())

        implicit val w: Window = window // if window == null, than possible issue with typeOf[T] => NullPointerException (ie. configReader)
        code

        dsss.foreach(ds => ds.consumer.commitSync(ds.offsets))
        dsss.foreach(_.producer.commitTransaction)
      } catch {
        case e @ (_: ProducerFencedException | _: OutOfOrderSequenceException | _: AuthorizationException) =>
          // We can't recover from these exceptions, close and set producer to null so that a new producer will be created for the next transaction.
          println(e)
          logger.error("transaction failed", e)
          dsss.foreach(ds => {
            ds.producer.close
            ds.createProducer()
          })
          dsss.foreach(ds => {
            ds.consumer.close
            ds.createConsumer()
          })
        case e: KafkaException =>
          // For all other exceptions, just abort the transaction and try again.
          println(e)
          logger.error("transaction failed", e)
          dsss.foreach(_.producer.abortTransaction)
          dsss.foreach(ds => {
            ds.consumer.close
            ds.createConsumer()
          })
        case e: Throwable =>
          println(e)
          throw e
      }
    }
  }
}
