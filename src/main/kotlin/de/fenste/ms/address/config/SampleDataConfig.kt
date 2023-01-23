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

package de.fenste.ms.address.config

import de.fenste.ms.address.domain.model.Address
import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.domain.model.PostCode
import de.fenste.ms.address.domain.model.State
import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.infrastructure.tables.AddressTable
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.CountryTable
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
import de.fenste.ms.address.infrastructure.tables.StateTable
import de.fenste.ms.address.infrastructure.tables.StreetTable
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.UUID

@Configuration
@Profile("sample")
class SampleDataConfig : InitializingBean {

    @Suppress("LongMethod")
    override fun afterPropertiesSet(): Unit = transaction {
        AddressTable.deleteAll()
        StreetTable.deleteAll()
        PostCodeTable.deleteAll()
        CityTable.deleteAll()
        StateTable.deleteAll()
        CountryTable.deleteAll()

        val cGermany = Country.new(UUID.fromString("12345678-1234-1234-1234-000000000000")) {
            alpha2 = "DE"
            alpha3 = "DEU"
            name = "Germany"
            localizedName = "Deutschland"
        }
        val cFrance = Country.new(UUID.fromString("12345678-1234-1234-1234-000000000001")) {
            alpha2 = "FR"
            alpha3 = "FRA"
            name = "France"
            localizedName = "Frankreich"
        }
        val cGB = Country.new(UUID.fromString("12345678-1234-1234-1234-000000000002")) {
            alpha2 = "GB"
            alpha3 = "GBR"
            name = "United Kingdom of Great Britain and Northern Ireland"
            localizedName = "Großbritannien und Nordirland"
        }

        val sBerlin = State.new(UUID.fromString("12345678-1234-1234-1234-000000000000")) {
            name = "Berlin"
            country = cGermany
        }
        val sBadenWuerttemberg = State.new(UUID.fromString("12345678-1234-1234-1234-000000000001")) {
            name = "Baden-Württemberg"
            country = cGermany
        }
        val sIleDeFrance = State.new(UUID.fromString("12345678-1234-1234-1234-000000000002")) {
            name = "Ile-de-France"
            country = cFrance
        }

        val cBerlin = City.new(UUID.fromString("12345678-1234-1234-1234-000000000000")) {
            name = "Berlin"
            country = cGermany
            state = sBerlin
        }
        val cSpandau = City.new(UUID.fromString("12345678-1234-1234-1234-000000000001")) {
            name = "Berlin-Spandau"
            country = cGermany
            state = sBerlin
        }
        val cKarlsruhe = City.new(UUID.fromString("12345678-1234-1234-1234-000000000002")) {
            name = "Karlsruhe"
            country = cGermany
            state = sBadenWuerttemberg
        }
        val cParis = City.new(UUID.fromString("12345678-1234-1234-1234-000000000003")) {
            name = "Paris"
            country = cFrance
            state = sIleDeFrance
        }
        val cBirmingham = City.new(UUID.fromString("12345678-1234-1234-1234-000000000004")) {
            name = "Birmingham"
            country = cGB
            state = null
        }

        val p10557 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000000000")) {
            code = "10557"
            city = cBerlin
        }
        val p10117 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000000001")) {
            code = "10117"
            city = cBerlin
        }
        val p13597 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000000002")) {
            code = "13597"
            city = cSpandau
        }
        val p76131 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000000003")) {
            code = "76131"
            city = cKarlsruhe
        }
        val p75007 = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000000004")) {
            code = "75007"
            city = cParis
        }
        val pB100RJ = PostCode.new(UUID.fromString("12345678-1234-1234-1234-000000000005")) {
            code = "B10 0RJ"
            city = cBirmingham
        }

        val sPlatzDerRepublik = Street.new(UUID.fromString("12345678-1234-1234-1234-000000000000")) {
            name = "Platz der Republik"
            postCode = p10557
        }
        val sWillyBrandtStrasse = Street.new(UUID.fromString("12345678-1234-1234-1234-000000000001")) {
            name = "Willy-Brandt-Straße"
            postCode = p10557
        }
        val sFriedrichEbertPlatz = Street.new(UUID.fromString("12345678-1234-1234-1234-000000000002")) {
            name = "Friedrich-Ebert-Platz"
            postCode = p10117
        }
        val sBreiteStrasse = Street.new(UUID.fromString("12345678-1234-1234-1234-000000000003")) {
            name = "Breite Str."
            postCode = p13597
        }
        val sAmFasanengarten = Street.new(UUID.fromString("12345678-1234-1234-1234-000000000004")) {
            name = "Am Fasanengarten"
            postCode = p76131
        }
        val sAnatoleFrance = Street.new(UUID.fromString("12345678-1234-1234-1234-000000000005")) {
            name = "Anatole France"
            postCode = p75007
        }
        val sCoventryRoad = Street.new(UUID.fromString("12345678-1234-1234-1234-000000000006")) {
            name = "Coventry Rd"
            postCode = pB100RJ
        }

        Address.new(UUID.fromString("12345678-1234-1234-1234-000000000000")) {
            houseNumber = "1"
            extra = null
            street = sPlatzDerRepublik
        }
        Address.new(UUID.fromString("12345678-1234-1234-1234-000000000001")) {
            houseNumber = "2a"
            extra = null
            street = sPlatzDerRepublik
        }
        Address.new(UUID.fromString("12345678-1234-1234-1234-000000000002")) {
            houseNumber = "1"
            extra = null
            street = sWillyBrandtStrasse
        }
        Address.new(UUID.fromString("12345678-1234-1234-1234-000000000003")) {
            houseNumber = "2"
            extra = null
            street = sFriedrichEbertPlatz
        }
        Address.new(UUID.fromString("12345678-1234-1234-1234-000000000004")) {
            houseNumber = "25"
            extra = null
            street = sBreiteStrasse
        }
        Address.new(UUID.fromString("12345678-1234-1234-1234-000000000005")) {
            houseNumber = "5"
            extra = null
            street = sAmFasanengarten
        }
        Address.new(UUID.fromString("12345678-1234-1234-1234-000000000006")) {
            houseNumber = "5"
            extra = "Av."
            street = sAnatoleFrance
        }
        Address.new(UUID.fromString("12345678-1234-1234-1234-000000000007")) {
            houseNumber = "109"
            extra = null
            street = sCoventryRoad
        }
    }
}
