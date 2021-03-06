/*
 * Copyright (c) 2022 Frederik Enste <frederik@fenste.de>.
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

object CountryTable : UUIDTable("countries") {

    const val ALPHA2_MAX_LENGTH = 2
    const val ALPHA3_MAX_LENGTH = 3
    const val NAME_MAX_LENGTH = 255
    const val LOCALIZED_NAME_MAX_LENGTH = 255

    val alpha2 = varchar("alpha2", ALPHA2_MAX_LENGTH).uniqueIndex()

    val alpha3 = varchar("alpha3", ALPHA3_MAX_LENGTH).uniqueIndex()

    val name = varchar("name", NAME_MAX_LENGTH)

    val localizedName = varchar("localized_name", LOCALIZED_NAME_MAX_LENGTH)
}
