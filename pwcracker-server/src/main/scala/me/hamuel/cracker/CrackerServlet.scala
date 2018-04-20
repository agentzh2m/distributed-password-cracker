package me.hamuel.cracker


import com.redis.RedisClient
import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.slf4j.{Logger, LoggerFactory}


case class Job(hash: String)
case class Msg(status: String, message: String)

class CrackerServlet extends ScalatraServlet with JacksonJsonSupport  {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  val logger: Logger = LoggerFactory.getLogger(getClass)
  val redisClient: RedisClient = new RedisClient("redis", 6379)


  redisClient.smembers("hashes").get
    .map(s => new JobDispatcher(s.get, redisClient))
    .foreach(job => job.run())


  before() {
    contentType = formats("json")
  }

  get("/") {
    Msg("success", "this is to check whether the server is alive")
  }

  /**
    * receive and start the job will return ONLY when the password
    * is crack if cannot crack the password then it is bad
    */
  post("/send_job") {
    logger.info("receive post event processing the job")
    val job = parsedBody.extract[Job]
    if (redisClient.get(job).isDefined){
      Msg("fail", "job is already dispatch it is currently working")
    }else {
      val dispatcher = new JobDispatcher(job.hash, redisClient)
      dispatcher.run()
      Msg("success", "dispatch job to the queue follow the status by calling /status?hash=<hash>")
    }

  }

  /**
    * get status of a particular hash that is in the job queue
    */
  get("/status") {
    val hash: Option[String] = params.get("hash")
    if (hash.isDefined) Map(hash.get -> redisClient.get(hash.get))
    else {
      val hashes = redisClient.smembers("hashes")
      if(hashes.isDefined) hashes.get.map(k => (k.get, redisClient.get(k.get))).toMap else List()
    }
  }

  /**
    * kill all worker that receive the job, broadcast a "fanout" exchange type
    * which all subscriber will receive
    */
  get("/kill") {
    WorkerController.killAllWorker()
    Msg("success", "killing all worker")

  }


}


