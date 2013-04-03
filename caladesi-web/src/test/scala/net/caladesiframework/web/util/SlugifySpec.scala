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

import org.specs2.mutable._

class SlugifySpec extends SpecificationWithJUnit {

  "Caladesi Slugify Helper" should {
    "return a lower cased string slug" in {
      val slug = Slugify.string("A small rabbit")

      slug must_==("a-small-rabbit")
    }

    "remove special charactes not wanted in url" in {
      val slug = Slugify.string("A small rabbit @+`oäêìóblah my home")

      slug must_==("a-small-rabbit-oaeeioblah-my-home")
    }

    "replace non latin (russian) strings" in {
      val slug = Slugify.string("Что мы делаем")

      slug must_==("-")
    }

    "take only the specified size of the string into slug" in {
      val slug = Slugify.string("A Small rabbit", 5)

      slug must_==("a-sma")
    }
  }
}