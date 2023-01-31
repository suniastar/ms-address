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

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SortOrder

inline fun String?.parseSortOrder(
    valueOf: (String) -> Expression<*>,
): Array<Pair<Expression<*>, SortOrder>> = this
    ?.split(';')
    ?.filterNot { s -> s.isBlank() }
    ?.map { s ->
        val split = s.split(',')
        val fieldString = split.first().trim()
        val orderString = split.getOrNull(1)
        val field = valueOf(fieldString)
        val order = orderString?.let { SortOrder.valueOf(orderString.trim().uppercase()) } ?: SortOrder.ASC
        field to order
    }
    ?.toTypedArray()
    ?: emptyArray()
