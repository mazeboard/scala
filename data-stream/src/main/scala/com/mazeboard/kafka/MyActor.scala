package com.mazeboard.kafka

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object MyActor {
  val customConf = ConfigFactory.parseString(
    """
  akka.actor.deployment {
    /my-service {
      router = round-robin-pool
      nr-of-instances = 3
    }
  }
  """)
  // ConfigFactory.load sandwiches customConfig between default reference
  // config and default overrides, and then resolves it.
  val system = ActorSystem("MySystem", ConfigFactory.load(customConf))
}

class MyActor extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case "test" ⇒ log.info("received test")
    case _      ⇒ log.info("received unknown message")
  }
}