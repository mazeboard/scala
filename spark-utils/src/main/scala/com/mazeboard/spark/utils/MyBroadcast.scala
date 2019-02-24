package com.mazeboard.spark.utils

import java.util.{ Timer, TimerTask }
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.SparkContext
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

class MyBroadcast[T: ClassTag](@transient sc: SparkContext, delay: Long, name: String, computeValue: (String) ⇒ Option[(T, String)]) extends Serializable {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private var bc: Broadcast[T] = _

  var attribute: String = ""
  def value = bc.value

  private def update(data: T, attr: String) {
    if (data != null && attr != null) {
      if (bc != null) bc.unpersist()
      bc = sc.broadcast[T](data)
      logger.info(s"updated broadcast name: $name (attr:$attr attribute:$attribute)")
      attribute = attr
    } else {
      logger.info(s"broadcast not updated name: $name (attr:$attr attribute:$attribute)")
    }
  }

  computeValue(attribute)
    .foreach {
      case (data, attr) ⇒ update(data, attr)
    }

  new Timer(true).schedule(new TimerTask {
    def run(): Unit = {
      if (sc.isStopped) {
        logger.warn("SparContext is stopped. broadcast update is cancelled")
        attribute = ""
        bc = null // make sure that no one uses bc while the scheduler is stopped
        this.cancel()
      } else {
        try {
          computeValue(attribute)
            .foreach {
              case (data, attr) ⇒ update(data, attr)
            }
        } catch {
          case e: Throwable ⇒
            logger.error("failed to update broadcast", e)
        }
      }
    }
  }, delay, delay)
}