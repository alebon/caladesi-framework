/*
 * Copyright 2012 Caladesi Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.caladesiframework.web.util

import java.util.regex.Pattern
import java.text.Normalizer
import java.text.Normalizer.Form


object Slugify {

  private lazy val NonLatin : Pattern = Pattern.compile("[^\\w-]")
  private lazy val Whitespace : Pattern = Pattern.compile("[\\s]")

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
    
    // Restricted to maxLength (in future versions)
    //slug.toLowerCase.take(maxLength)
    slug.toLowerCase
  }
}
