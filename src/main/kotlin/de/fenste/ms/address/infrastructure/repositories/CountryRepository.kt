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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.infrastructure.tables.CountryTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CountryRepository {

    private companion object {

        private fun checkDuplicate(
            original: Country? = null,
            alpha2: String,
            alpha3: String,
            name: String,
        ) {
            val country = CountryTable
                .slice(CountryTable.columns)
                .select {
                    (CountryTable.alpha2 eq alpha2) or (CountryTable.alpha3 eq alpha3) or (CountryTable.name eq name)
                }
                .apply { original?.let { andWhere { CountryTable.id neq original.id } } }
                .limit(1)
                .notForUpdate()
                .firstOrNull()
                ?.let { r -> Country.wrapRow(r) }

            require(country == null) { "This country does already exist: $country" }
        }
    }

    fun count(): Long = Country
        .all()
        .count()

    fun list(
        page: Int? = null,
        size: Int? = null,
        vararg order: Pair<Expression<*>, SortOrder> = emptyArray(),
    ): SizedIterable<Country> = when (size) {
        null ->
            Country
                .all()
                .orderBy(*order, CountryTable.id to SortOrder.ASC)
                .notForUpdate()

        else ->
            Country
                .all()
                .orderBy(*order, CountryTable.id to SortOrder.ASC)
                .limit(size, (page ?: 0).toLong() * size)
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
        checkDuplicate(
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
        )

        return Country.new {
            this.alpha2 = alpha2
            this.alpha3 = alpha3
            this.name = name
            this.localizedName = localizedName
        }
    }

    fun update(
        id: UUID,
        alpha2: String,
        alpha3: String,
        name: String,
        localizedName: String,
    ): Country {
        val country = Country
            .find { CountryTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(country) { "The country ($id) does not exist." }

        checkDuplicate(
            original = country,
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
        )

        country.alpha2 = alpha2
        country.alpha3 = alpha3
        country.name = name
        country.localizedName = localizedName
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
