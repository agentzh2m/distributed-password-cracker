import org.apache.commons.codec.digest.Crypt

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
object Worker {
  val salt = "ic"

  /**
    * Attempt to match the hash with the correct password by brute forcing it against
    * the range of start to end string
    * @param start (start string to search for)
    * @param end (end string to search for)
    * @param hash (the hash to solve)
    * @return None if cannot find and a String if the correct string exist
    */
  def crack(start: String, end: String, hash: String): Option[String] = {
    var current = start.getBytes()
    var currentHash = Crypt.crypt(current, salt)
    var res: Option[String] = None
    while (!(current sameElements end) && hash != currentHash ){
      current = Permutator.increment(current)
      currentHash = Crypt.crypt(current, salt)
      if (currentHash == hash){
        res = Some(current.map(_.toChar).mkString(""))
      }
    }
    res
  }

  /**
    * Attempt to find the hash in parallel using n cores
    * @param start (start string to search for)
    * @param end (last string to search for)
    * @param hash (the hash to solve)
    * @param n (number of chunks)
    * @return None if cannot find and a String if the correct string exist
    */
  def parCrack(start: String, end: String, hash: String, n: Int): Option[String] = {
    //split the job into n chunks
    val startn = Permutator.convertStToDec(start)
    val endn = Permutator.convertStToDec(end)
    val delta = endn - startn

    val intToSt = Permutator.convertDecToSt _

    val res = (0 until n).map(i => {
      val start = startn + i*(delta/n)
      val end = startn + (i+1)*(delta/n)
      val realStart = if (start % 63 !=0) start + 1 else start
      val realEnd = if (start % 63 != 0) end + 1 else end
      println(s"start $realStart, end: $realEnd")
      Future{crack(intToSt(realStart), intToSt(realEnd), hash)}
    } )

    Await.result(Future.sequence(res), Duration.Inf).find(_.isDefined) match {
      case Some(Some(st)) => Some(st)
      case _ => None
    }
  }



}
