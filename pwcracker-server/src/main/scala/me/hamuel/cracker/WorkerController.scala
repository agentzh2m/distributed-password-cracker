package me.hamuel.cracker

import akka.actor.{ActorRef, ActorSystem, Props}
import com.spingo.op_rabbit.{Message, Publisher, RabbitControl}
import play.api.libs.json.{Json, OFormat}
import com.spingo.op_rabbit.PlayJsonSupport._

case class WorkerMsg(msg: String)
object WorkerController {
  implicit val actorSystem: ActorSystem = ActorSystem("rabbit-actor")
  val rabbitControl: ActorRef = actorSystem.actorOf(Props[RabbitControl])
  implicit val workerMsgResultFormat: OFormat[WorkerMsg] = Json.format[WorkerMsg]
  def killAllWorker(): Unit = {
    rabbitControl ! Message(WorkerMsg("terminate"), Publisher.exchange("broadcaster"))
  }

}
