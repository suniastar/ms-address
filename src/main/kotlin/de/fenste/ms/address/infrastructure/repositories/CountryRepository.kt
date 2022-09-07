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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.infrastructure.tables.CountryTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CountryRepository {

    private companion object {

        private fun idOfAlpha2(
            alpha2: String,
        ): EntityID<UUID>? = CountryTable
            .slice(CountryTable.id)
            .select { CountryTable.alpha2 eq alpha2 }
            .limit(1)
            .notForUpdate()
            .firstOrNull()
            ?.let { r -> r[CountryTable.id] }

        private fun idOfAlpha3(
            alpha3: String,
        ): EntityID<UUID>? = CountryTable
            .slice(CountryTable.id)
            .select { CountryTable.alpha3 eq alpha3 }
            .limit(1)
            .notForUpdate()
            .firstOrNull()
            ?.let { r -> r[CountryTable.id] }

        private fun idOfName(
            name: String,
        ): EntityID<UUID>? = CountryTable
            .slice(CountryTable.id)
            .select { CountryTable.name eq name }
            .limit(1)
            .notForUpdate()
            .firstOrNull()
            ?.let { r -> r[CountryTable.id] }
    }

    fun list(
        limit: Int? = null,
        offset: Long = 0L,
        vararg order: Pair<Expression<*>, SortOrder> = arrayOf(CountryTable.id to SortOrder.ASC),
    ): SizedIterable<Country> = when (limit) {
        null ->
            Country
                .all()
                .orderBy(*order)
                .notForUpdate()

        else ->
            Country
                .all()
                .orderBy(*order)
                .limit(limit, offset)
                .notForUpdate()
    }

    fun find(
        id: UUID? = null,
        alpha2: String? = null,
        alpha3: String? = null,
    ): Country? = when {
        id != null ->
            Country
                .find { CountryTable.id eq id }
                .limit(1)
                .notForUpdate()
                .firstOrNull()

        alpha2 != null ->
            Country
                .find { CountryTable.alpha2 eq alpha2 }
                .limit(1)
                .notForUpdate()
                .firstOrNull()

        alpha3 != null ->
            Country
                .find { CountryTable.alpha3 eq alpha3 }
                .limit(1)
                .notForUpdate()
                .firstOrNull()

        else -> throw IllegalArgumentException("Either 'uuid', 'alpha2' or 'alpha3' must be specified.")
    }

    fun create(
        alpha2: String,
        alpha3: String,
        name: String,
        localizedName: String,
    ): Country {
        require(idOfAlpha2(alpha2) == null) { "A country with the country code ($alpha2) does already exist." }
        require(idOfAlpha3(alpha3) == null) { "A country with the country code ($alpha3) does already exist." }
        require(idOfName(name) == null) { "A country with the country code ($name) does already exist." }

        return Country.new {
            this.alpha2 = alpha2
            this.alpha3 = alpha3
            this.name = name
            this.localizedName = localizedName
        }
    }

    fun update(
        id: UUID,
        alpha2: String? = null,
        alpha3: String? = null,
        name: String? = null,
        localizedName: String? = null,
    ): Country {
        val country = Country
            .find { CountryTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(country) { "The country ($id) does not exist." }

        alpha2?.let {
            val uId = idOfAlpha2(alpha2)
            require(uId == null || uId == country.id) {
                "A country with the country code ($alpha2) does already exist."
            }
        }
        alpha3?.let {
            val uId = idOfAlpha3(alpha3)
            require(uId == null || uId == country.id) {
                "A country with the country code ($alpha3) does already exist."
            }
        }
        name?.let {
            val uId = idOfName(name)
            require(uId == null || uId == country.id) {
                "A country with the country code ($name) does already exist."
            }
        }

        alpha2?.let { country.alpha2 = alpha2 }
        alpha3?.let { country.alpha3 = alpha3 }
        name?.let { country.name = name }
        localizedName?.let { country.localizedName = localizedName }

        return country
    }

    fun delete(
        id: UUID,
    ) {
        val country = Country
            .find { CountryTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(country) { "The country ($id) does not exist." }

        country.delete()
    }
}
