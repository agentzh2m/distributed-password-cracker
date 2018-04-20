
object Permutator {
  import scala.collection.mutable.ArrayBuffer

  val combine: Array[Byte] = (' ' +: (('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9'))).toArray.map(_.toByte)
  val ncombine: Int  = combine.length

  /**
    * find the next string in the sequence the non functional way
    * but this is Amotarized O(1) the list does not need to be reverse initially also
    * I am too lazy to proof this but once every 62 times the number of n will be n+1 will increase
    * however to fully run n times is (1/62) to the power of n
    * @param st
    * @return byte array
    */
  def increment(st: Array[Byte]): Array[Byte] = {
    val n = st.length - 1
    var i = n
    var isValid = true
    while (i >= 0 && (isValid || i == n)  ){
      if(st(i) == '9') isValid = true else isValid = false
      st(i) = if (nextChar(st(i)) == ' ') nextChar(nextChar(st(i))) else nextChar(st(i))
      i = i-1
    }
    if(isValid) Array('A'.toByte) ++ st else st
  }


  /**
    * Move to the next character
    * @param ch
    * @return the next character in our permute system
    */
  def nextChar(ch:Byte): Byte = {combine((combine.indexOf(ch) + 1) % combine.length)}

  /**
    * Create a sequence of range of permutated string within start to end
    * @param start
    * @param end
    * @return seq of permutated string
    */

  def permuteRange(start: String, end: String, incr: Array[Byte] => Array[Byte]): Seq[String] = {
    var ar: ArrayBuffer[String] = ArrayBuffer[String]()
    val endAr = end.toCharArray
    var current:Array[Byte] = start.getBytes()
    while(!(current sameElements endAr)){
      ar += current.mkString("")
      current = increment(current)
    }
    ar += current.mkString("")
    ar
  }

  /**
    * convert string to a decimal number
    * @param st
    * @return a decimal number that can be converted back to a string
    */
  def convertStToDec(st: String): Long = st.reverse.foldLeft((0, 0:Long)){
    case ((i, sum), elt) => (i+1, sum + (combine.indexOf(elt) * math.pow(ncombine, i).toLong))
  }._2

  /**
    * convert a decimal back to a string in our base 62 system
    * warning if n%63 == 0 a rubbish will be generated I suggest you skip
    * to the next n if that happens
    * @param n
    * @return a string within our base 62 system or a rubbish string if n%63 == 0
    */
  def convertDecToByte(n: Long): Vector[Byte] = {
    if (n == 0){
      Vector[Byte]()
    }else{
      convertDecToByte(n / ncombine) :+ combine((n % ncombine).toInt)
    }
  }

  def convertDecToSt(n: Long): String = {
    convertDecToByte(n).map(_.toChar).mkString("")
  }


}
