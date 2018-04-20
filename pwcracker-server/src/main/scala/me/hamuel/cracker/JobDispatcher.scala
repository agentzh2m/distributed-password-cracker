package me.hamuel.cracker

import akka.actor.{ActorRef, ActorSystem, Props}
import com.newmotion.akka.rabbitmq.{Connection, ConnectionFactory, CreateChannel}
import com.redis.RedisClient
import com.spingo.op_rabbit.PlayJsonSupport._
import com.spingo.op_rabbit._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
case class CrackerJob(start: Long, end: Long, hash: String)
case class CrackerResult(result: Option[String])
class JobDispatcher(hash: String, redisClient: RedisClient) {
  implicit val actorSystem: ActorSystem = ActorSystem(s"rabbit-actor-$hash")
  val rabbitControl: ActorRef = actorSystem.actorOf(Props[RabbitControl])
  val delta: Long = 5000000

  implicit val crackResultFormat: OFormat[CrackerResult] = Json.format[CrackerResult]
  implicit val crackJobResultFormat: OFormat[CrackerJob] = Json.format[CrackerJob]
  implicit val recoveryStrategy: RecoveryStrategy = RecoveryStrategy.none
  var result: Option[String] = None
  val logger: Logger = LoggerFactory.getLogger(getClass)


  /**
    * when a client finishes a job add more job to the queue
    */
  val subscriptionRef: SubscriptionRef = Subscription.run(rabbitControl) {
    import com.spingo.op_rabbit.Directives._
    val directive = body(as[CrackerResult])
    channel() {
      consume(queue(s"${hash}_result_queue")) {
          directive(crackerResult => {
            logger.info(s"worker result $crackerResult")
            if (crackerResult.result.isDefined) {
              redisClient.set(hash, crackerResult.result.get)
            }
            else{
              addJob()
            }
            ack
          })
        }}
    }


  def addJob(): Unit ={
    val currentCounter: Long = redisClient.incrby(s"${hash}_count", delta).get - delta
    logger.info(s"sending job $currentCounter for $hash hash")
    rabbitControl ! Message.queue(
      CrackerJob(currentCounter, currentCounter + delta, hash),
      queue = "job-queue"
    )
  }

  /**
    * Run indefinitely and dispatches job to the workqueue
    * @return
    */
  def run(): Unit = {
    //seed the job queue with 10 jobs
    (0 to 10).foreach(_ => addJob())
    redisClient.set(hash, "")
    redisClient.sadd("hashes", hash)

  }

}
