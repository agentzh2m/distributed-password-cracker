import akka.actor.{ActorRef, ActorSystem, Props}
import com.rabbitmq.client.{Address, ConnectionFactory}
import com.redis.RedisClientPool
import com.spingo.op_rabbit.PlayJsonSupport._
import com.spingo.op_rabbit._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._


case class CrackerJob(start: Long, end: Long, hash: String)
case class CrackerResult(result: Option[String])
case class WorkerMsg(msg: String)

object Main extends App {
  val redisClient: RedisClientPool = new RedisClientPool(sys.env.get("REDIS_ENDPOINT").get, 6379) //in the future get the server address from env path
  val connectionParams = ConnectionParams(
    hosts = List(new Address(sys.env.get("RABBIT_ENDPOINT").get, ConnectionFactory.DEFAULT_AMQP_PORT) ),
    username = "guest",
    password = "guest"
  )
  implicit val actorSystem: ActorSystem = ActorSystem("rabbit-actor")
  val rabbitControl: ActorRef = actorSystem.actorOf(Props{new RabbitControl(connectionParams)})
  implicit val crackResultFormat: OFormat[CrackerResult] = Json.format[CrackerResult]
  implicit val crackJobResultFormat: OFormat[CrackerJob] = Json.format[CrackerJob]
  implicit val workerMsgResultFormat: OFormat[WorkerMsg] = Json.format[WorkerMsg]
  implicit val recoveryStrategy: RecoveryStrategy = RecoveryStrategy.none

  println("Initiating subscriber")

  val subscriptionRef: SubscriptionRef = Subscription.run(rabbitControl) {
    import Directives._
    val directive = body(as[CrackerJob])
    channel(qos = 4) {
      consume(queue("job-queue")) {
        directive(crackerJob => {
          println(s"recieving job $crackerJob")
          //verify that the job is already done or not
          val job = Future{redisClient.withClient {
            client => {
              if (client.bitcount(crackerJob.hash).get == 0) {
                println(s"Starting job $crackerJob")
                var start = crackerJob.start
                var end = crackerJob.end
                if (crackerJob.start % 63 == 0) start = start + 1
                if (crackerJob.end % 63 == 0) end = end + 1
                val res = Worker.crack(Permutator.convertDecToSt(start), Permutator.convertDecToSt(end), crackerJob.hash)
                rabbitControl ! Message.queue(CrackerResult(res), s"${crackerJob.hash}_result_queue")
              } else {
                println(s"skipping job $crackerJob because it is already done")
              }
            }
          }}
          ack(job)
        })
      }
    }
  }

  val subscriptionKill: SubscriptionRef = Subscription.run(rabbitControl) {
    import com.spingo.op_rabbit.Directives._
    val directive = body(as[WorkerMsg])
    channel() {
      consume(Binding.fanout(Queue("broadcaster", arguments = List(ModeledMessageHeaders.`x-message-ttl`(1.seconds))),
        Exchange.fanout("broadcaster"))) {
        directive(wmsg => {
          println(s"receiving message ${wmsg.msg}")
          if (wmsg.msg == "terminate"){
            actorSystem.stop(rabbitControl)
            System.exit(0)
          }
          ack
        })
      }
    }
  }

}
