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

package de.fenste.ms.address.application.services

import de.fenste.ms.address.application.dtos.requests.CreateCountryDto
import de.fenste.ms.address.application.dtos.requests.UpdateCountryDto
import de.fenste.ms.address.application.dtos.responses.CountryDto
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.infrastructure.tables.CountryTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CountryService {

    private companion object {

        private fun idOf(
            alpha2: String,
            alpha3: String,
        ): EntityID<UUID>? = Country
            .find { (CountryTable.alpha2 eq alpha2) or (CountryTable.alpha3 eq alpha3) }
            .limit(1)
            .notForUpdate()
            .firstOrNull()
            ?.id
    }

    fun list(
        limit: Int? = null,
        offset: Long? = null,
    ): List<CountryDto>? = transaction {
        when (limit) {
            null ->
                Country
                    .all()
                    .orderBy(CountryTable.id to SortOrder.ASC)
                    .notForUpdate()
                    .map { c -> CountryDto(c) }
                    .ifEmpty { null }
            else ->
                Country
                    .all()
                    .orderBy(CountryTable.id to SortOrder.ASC)
                    .limit(limit, offset ?: 0L)
                    .notForUpdate()
                    .map { c -> CountryDto(c) }
                    .ifEmpty { null }
        }
    }

    fun find(
        id: UUID? = null,
        alpha2: String? = null,
        alpha3: String? = null,
    ): CountryDto? = transaction {
        when {
            id != null ->
                Country
                    .find { CountryTable.id eq id }
                    .limit(1)
                    .notForUpdate()
                    .firstOrNull()
                    ?.let { c -> CountryDto(c) }
            alpha2 != null ->
                Country
                    .find { CountryTable.alpha2 eq alpha2 }
                    .limit(1)
                    .notForUpdate()
                    .firstOrNull()
                    ?.let { c -> CountryDto(c) }
            alpha3 != null ->
                Country
                    .find { CountryTable.alpha3 eq alpha3 }
                    .limit(1)
                    .notForUpdate()
                    .firstOrNull()
                    ?.let { c -> CountryDto(c) }
            else -> throw IllegalArgumentException("Either 'uuid', 'alpha2' or 'alpha3' must be specified.")
        }
    }

    fun create(
        create: CreateCountryDto,
    ): CountryDto = transaction {
        require(
            idOf(
                alpha2 = create.alpha2,
                alpha3 = create.alpha3,
            ) == null,
        ) { "A country with these country codes does already exist." }

        val country = Country.new {
            alpha2 = create.alpha2
            alpha3 = create.alpha3
            name = create.name
            localizedName = create.localizedName
        }

        CountryDto(country)
    }

    fun update(
        update: UpdateCountryDto,
    ): CountryDto = transaction {
        val country = Country
            .find { CountryTable.id eq UUID.fromString(update.id) }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(country) { "The country (${update.id}) does not exist." }

        val uId = idOf(
            alpha2 = update.alpha2 ?: country.alpha2,
            alpha3 = update.alpha3 ?: country.alpha3,
        )
        require(uId == null || uId == country.id) { "A country with these country codes does already exist." }

        with(update) {
            alpha2?.let { country.alpha2 = alpha2 }
            alpha3?.let { country.alpha3 = alpha3 }
            name?.let { country.name = name }
            localizedName?.let { country.localizedName = localizedName }
        }

        commit()
        CountryDto(country)
    }

    fun delete(
        id: UUID,
    ): Boolean = transaction {
        val country = Country
            .find { CountryTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()
        requireNotNull(country) { "The country ($id) does not exist." }

        country.delete()
        true
    }
}
