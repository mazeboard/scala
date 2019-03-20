package com.mazeboard.kafka

import java.time.Duration
import java.util

import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.header.Header

import scala.util.Random

class DataStreamVariableV[V](topic: String, consumerProperties: util.Properties, producerProperties: util.Properties)
  extends DataStreamVariable[Long, V](topic, consumerProperties, producerProperties) {

  def write(value: V): Unit = {
    write(value, List())
  }

  def write(value: V, headers: List[Header]): Unit = {
    import scala.collection.JavaConverters._
    if (producer == null) createProducer()
    producer.send(new ProducerRecord[Long, V](topic, null, null, Random.nextLong(), value, headers.asJava))
  }

}

class DataStreamVariableKV[K, V](topic: String, consumerProperties: util.Properties, producerProperties: util.Properties)
  extends DataStreamVariable[K, V](topic, consumerProperties, producerProperties) {

  def write(key: K, value: V): Unit = {
    write(key, value, List())
  }

  def write(key: K, value: V, headers: List[Header]): Unit = {
    import scala.collection.JavaConverters._
    if (producer == null) createProducer()
    producer.send(new ProducerRecord[K, V](topic, null, null, key, value, headers.asJava))
  }
}

class DataStreamVariable[K, V](topic: String, consumerProperties: util.Properties, producerProperties: util.Properties) {
  val offsets: java.util.HashMap[TopicPartition, OffsetAndMetadata] = new java.util.HashMap()

  var consumer: KafkaConsumer[K, V] = _
  var producer: KafkaProducer[K, V] = _
  var records: util.Iterator[ConsumerRecord[K, V]] = _

  def read[W <: Window]: Seq[ConsumerRecord[K, V]] = {
    Seq()
  }

  def read(): ConsumerRecord[K, V] = {
    if (records != null && records.hasNext) {
      val record: ConsumerRecord[K, V] = records.next
      offsets.put(new TopicPartition(topic, record.partition), new OffsetAndMetadata(record.offset))
      record
    } else {
      if (consumer == null) createConsumer()
      var done = false
      while (!done) {
        records = consumer
          .poll(Duration.ofMillis(1000))
          .iterator()
        if (records.hasNext) done = true
      }
      val record: ConsumerRecord[K, V] = records.next
      offsets.put(new TopicPartition(topic, record.partition), new OffsetAndMetadata(record.offset))
      record
    }
  }

  def begin(): Unit = {
    if (consumer == null) {
      consumerProperties.put("group.id", s"group_${Math.abs(Random.nextLong())}")
      createConsumer()
    }
    if (producer == null) {
      producerProperties.put("transactional.id", s"trx_${Math.abs(Random.nextLong())}")
      createProducer()
    } else producer.beginTransaction
  }

  def createProducer(): Unit = {
    producer = new KafkaProducer[K, V](producerProperties)
    producer.initTransactions()
  }

  def createConsumer(): Unit = {
    import scala.collection.JavaConverters._
    consumer = new KafkaConsumer[K, V](consumerProperties)
    consumer.subscribe(List(topic).asJava)
  }

  def close(): Unit = {
    if (consumer != null) {
      consumer.close()
      consumer = null
    }
    if (producer != null) {
      producer.close()
      producer = null
    }
  }
}