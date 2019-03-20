package com.mazeboard.tests

import scala.collection.immutable.Range

object KafkaSupport {
  import scala.collection.JavaConverters._
  import java.util
  import org.apache.kafka.clients.consumer.{ ConsumerRecord, KafkaConsumer }
  import org.apache.kafka.clients.producer.{ KafkaProducer, ProducerRecord }

  def startProducer(count: Int, topic: String) {
    Thread.sleep(1000)
    println(s"startProducer count:$count topic:$topic")
    val props = new util.Properties
    props.put("bootstrap.servers", "localhost:9092")
    props.put("acks", "all") //The "all" setting we have specified will result in blocking on the full commit of the record, the slowest but most durable setting
    props.put("delivery.timeout.ms", "30000")
    props.put("batch.size", "16384")
    props.put("linger.ms", "1")
    props.put("request.timeout.ms", "5000")
    props.put("buffer.memory", "33554432")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)
    Range(0, count).foreach(i => {
      Thread.sleep(100)
      println(s"send $i to $topic")
      producer.send(new ProducerRecord[String, String](topic, Integer.toString(i), Integer.toString(i)))
    })
    producer.close()
  }

  def startConsumer(id: Int, groupid: String, topics: List[String], minBatchSize: Int) {
    Thread.sleep(1000)
    println(s"startConsumer $id groupid:$groupid topics:$topics")
    val props = new util.Properties
    props.put("bootstrap.servers", "localhost:9092")
    props.put("group.id", groupid)
    props.put("enable.auto.commit", "false")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(topics.asJava)
    val buffer = new util.ArrayList[ConsumerRecord[String, String]]
    while (buffer.size < minBatchSize) {
      val records = consumer.poll(java.time.Duration.ofMillis(50))
      val iter = records.iterator()
      while (iter.hasNext) {
        val x = iter.next()
        println(s"$id. key:${x.key()} value:${x.value()}")
        buffer.add(x)
      }
    }
    consumer.commitSync()
    consumer.close()
  }

}
