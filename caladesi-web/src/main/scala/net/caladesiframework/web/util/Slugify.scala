package net.caladesiframework.web.util

import java.util.regex.Pattern
import java.text.Normalizer
import java.text.Normalizer.Form
import java.util.Locale


object Slugify {
  
  private lazy val NonLatin = "[^\\w-]".r
  private lazy val Whitespace = "[\\s]".r

  def string(phrase : String, maxLength : Int = 50) : String = {
    def removeSpecialChars(phrase : String) =
      // Language DE
      phrase.replaceAll("Ä", "AE")
        .replaceAll("ß", "ss")
        .replaceAll("Ö", "OE")
        .replaceAll("Ü", "UE")
        .replaceAll("ä", "ae")
        .replaceAll("ö", "oe")
        .replaceAll("ü", "ue")

    def normalize(phrase: String) = Normalizer.normalize(phrase, Form.NFD)
    def removeNotLatin(phrase : String) = NonLatin.matcher(phrase).replaceAll("")
    def removeWhiteSpaces(phrase : String) = Whitespace.matcher(phrase).replaceAll("-")

    val slug = removeNotLatin (normalize(removeSpecialChars(removeWhiteSpaces(phrase))))
    
    // Restricted to maxLength
    slug.toLowerCase.take(maxLength)
  }
}
