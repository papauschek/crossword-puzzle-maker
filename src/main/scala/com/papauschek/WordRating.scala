package com.papauschek

object WordRating {

  def getBest(words: Seq[String]): Seq[String] = {

    val allChars = words.flatten.toVector
    val allCharCount = allChars.length
    val frequency = allChars.groupBy(c => c).map { case (c, list) => (c, list.length / allCharCount.toDouble) }

    def rateWord(word: String): Double = {
      word.map(frequency).sum// / word.length
    }

    words.sortBy(rateWord).reverse
  }

}
