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

package de.fenste.ms.address.infrastructure.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column

object CountryTable : UUIDTable("countries") {

    private const val ALPHA2_MAX_LENGTH = 2
    private const val ALPHA3_MAX_LENGTH = 3
    private const val NAME_MAX_LENGTH = 255
    private const val LOCALIZED_NAME_MAX_LENGTH = 255

    val alpha2 = char("alpha2", ALPHA2_MAX_LENGTH).uniqueIndex()

    val alpha3 = char("alpha3", ALPHA3_MAX_LENGTH).uniqueIndex()

    val name = varchar("name", NAME_MAX_LENGTH)

    val localizedName = varchar("localized_name", LOCALIZED_NAME_MAX_LENGTH)

    fun valueOf(value: String): Column<*> = when (value.lowercase()) {
        "id" -> id
        "alpha2" -> alpha2
        "alpha3" -> alpha3
        "name" -> name
        "localized_name", "localizedname" -> localizedName
        else -> throw IllegalArgumentException("\"$value\" is not a valid column name.")
    }
}
