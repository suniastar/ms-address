/*
 * Copyright (c) 2023 Frederik Enste <frederik@fenste.de>.
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.fenste.ms.address.application.util

import org.springframework.hateoas.Link

object PageHelper {

    @Suppress("LongParameterList")
    inline fun generatePageLinks(
        base: String,
        size: Int?,
        page: Int?,
        totalPages: Int?,
        sort: String?,
        affordanceBuilder: (Link) -> Link = { l -> l },
    ): Set<Link> {
        val p = page ?: 0
        val t = totalPages ?: 1

        val self = affordanceBuilder(Link.of("$base{?page,size,sort}").withSelfRel())
        val sortUri = sort?.let { "&sort=$sort" } ?: ""

        return when (size) {
            null -> setOf(self)
            else -> setOfNotNull(
                self,
                Link.of("$base?page=0&size=$size$sortUri").withRel("first"),
                if (p - 1 >= 0) {
                    Link.of("$base?page=${p - 1}&size=$size$sortUri").withRel("prev")
                } else {
                    null
                },
                if (p + 1 < t) {
                    Link.of("$base?page=${p + 1}&size=$size$sortUri").withRel("next")
                } else {
                    null
                },
                Link.of("$base?page=${t - 1}&size=$size$sortUri").withRel("last"),
            )
        }
    }
}
