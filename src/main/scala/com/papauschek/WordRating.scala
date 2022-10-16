package com.papauschek

object WordRating {

  def getBest(words: Seq[String]): Seq[String] = {

    val allChars = words.flatten.toVector
    val allCharCount = allChars.length
    val frequency = allChars.groupBy(c => c).map { case (c, list) => (c, list.length / allCharCount.toDouble) }

    //println(frequency.toList.sortBy(- _._2).mkString("\r\n"))

    def rateWord(word: String): Double = {
      word.map(frequency).sum// / word.length
    }

    //println(words.map(w => (w, rateWord(w))).sortBy(-_._2).mkString("\r\n"))

    words.sortBy(rateWord).reverse
  }

}
