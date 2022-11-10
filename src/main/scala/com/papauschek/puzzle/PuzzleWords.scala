package com.papauschek.puzzle

object PuzzleWords {

  /** @return sort the given words, such that the words with the most common characters are first.
   *          this is the most efficient way of incrementally adding words to a puzzle. */
  def sortByBest(words: Seq[String]): Seq[String] =

    val allChars = words.flatten.toVector
    val allCharCount = allChars.length
    val frequency = allChars.groupBy(c => c).map { case (c, list) => (c, list.length / allCharCount.toDouble) }

    def rateWord(word: String): Double = {
      word.map(frequency).sum
    }

    words.sortBy(rateWord).reverse

}
