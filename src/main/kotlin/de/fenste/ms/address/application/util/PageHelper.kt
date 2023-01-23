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
import kotlin.math.max
import kotlin.math.min

object PageHelper {

    fun generatePageLinks(
        base: String,
        size: Long,
        page: Long,
        total: Long,
        sort: String?,
    ): Set<Link> {
        val sortUri = sort?.let { "&sort=$sort" } ?: ""
        return setOf(
            Link.of("$base?page=0&size=$size$sortUri").withRel("first"),
            Link.of("$base?page=${max(0, page - 1)}&size=$size$sortUri").withRel("prev"),
            Link.of("$base?page=$page&size=$size$sortUri").withSelfRel(),
            Link.of("$base?page=${min(total - 1, page + 1)}&size=$size$sortUri").withRel("next"),
            Link.of("$base?page=${total - 1}&size=$size$sortUri").withRel("last"),
        )
    }
}
