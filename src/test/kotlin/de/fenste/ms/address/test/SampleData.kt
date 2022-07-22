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

package de.fenste.ms.address.test

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
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

object SampleData {

    private var pCountries: List<Country>? = null
    private var pStates: List<State>? = null
    private var pCities: List<City>? = null
    private var pPostCodes: List<PostCode>? = null
    private var pStreets: List<Street>? = null
    private var pAddresses: List<Address>? = null

    val countries: List<Country>
        get() = pCountries!!
    val states: List<State>
        get() = pStates!!
    val cities: List<City>
        get() = pCities!!
    val postCodes: List<PostCode>
        get() = pPostCodes!!
    val streets: List<Street>
        get() = pStreets!!
    val addresses: List<Address>
        get() = pAddresses!!

    @Suppress("LongMethod")
    fun reset() {
        TransactionManager.defaultDatabase = Database.connect(
            driver = "org.h2.Driver",
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
            user = "",
            password = "",
        )

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                AddressTable,
                CityTable,
                CountryTable,
                PostCodeTable,
                StateTable,
                StreetTable,
            )

            AddressTable.deleteAll()
            StreetTable.deleteAll()
            PostCodeTable.deleteAll()
            CityTable.deleteAll()
            StateTable.deleteAll()
            CountryTable.deleteAll()

            val cGermany = Country.new {
                alpha2 = "DE"
                alpha3 = "DEU"
                name = "Germany"
                localizedName = "Deutschland"
            }
            val cFrance = Country.new {
                alpha2 = "FR"
                alpha3 = "FRA"
                name = "France"
                localizedName = "Frankreich"
            }
            val cGB = Country.new {
                alpha2 = "GB"
                alpha3 = "GBR"
                name = "United Kingdom of Great Britain and Northern Ireland"
                localizedName = "Großbritannien und Nordirland"
            }

            val sBerlin = State.new {
                name = "Berlin"
                country = cGermany
            }
            val sBadenWuerttemberg = State.new {
                name = "Baden-Württemberg"
                country = cGermany
            }
            val sIleDeFrance = State.new {
                name = "Ile-de-France"
                country = cFrance
            }

            val cBerlin = City.new {
                name = "Berlin"
                country = cGermany
                state = sBerlin
            }
            val cSpandau = City.new {
                name = "Berlin-Spandau"
                country = cGermany
                state = sBerlin
            }
            val cKarlsruhe = City.new {
                name = "Karlsruhe"
                country = cGermany
                state = sBadenWuerttemberg
            }
            val cParis = City.new {
                name = "Paris"
                country = cFrance
                state = sIleDeFrance
            }
            val cBirmingham = City.new {
                name = "Birmingham"
                country = cGB
                state = null
            }

            val p10557 = PostCode.new {
                code = "10557"
                city = cBerlin
            }
            val p10117 = PostCode.new {
                code = "10117"
                city = cBerlin
            }
            val p13597 = PostCode.new {
                code = "13597"
                city = cSpandau
            }
            val p76131 = PostCode.new {
                code = "76131"
                city = cKarlsruhe
            }
            val p75007 = PostCode.new {
                code = "75007"
                city = cParis
            }
            val pB100RJ = PostCode.new {
                code = "B10 0RJ"
                city = cBirmingham
            }

            val sPlatzDerRepublik = Street.new {
                name = "Platz der Republik"
                postCode = p10557
            }
            val sWillyBrandtStrasse = Street.new {
                name = "Willy-Brandt-Straße"
                postCode = p10557
            }
            val sFriedrichEbertPlatz = Street.new {
                name = "Friedrich-Ebert-Platz"
                postCode = p10117
            }
            val sBreiteStrasse = Street.new {
                name = "Breite Str."
                postCode = p13597
            }
            val sAmFasanengarten = Street.new {
                name = "Am Fasanengarten"
                postCode = p76131
            }
            val sAnatoleFrance = Street.new {
                name = "Anatole France"
                postCode = p75007
            }
            val sCoventryRoad = Street.new {
                name = "Coventry Rd"
                postCode = pB100RJ
            }

            val aPlatzDerRepublik1 = Address.new {
                houseNumber = "1"
                extra = null
                street = sPlatzDerRepublik
            }
            val aPlatzDerRepublik2a = Address.new {
                houseNumber = "2a"
                extra = null
                street = sPlatzDerRepublik
            }
            val aWillyBrandtStrasse1 = Address.new {
                houseNumber = "1"
                extra = null
                street = sWillyBrandtStrasse
            }
            val aFriedrichEbertPlatz2 = Address.new {
                houseNumber = "2"
                extra = null
                street = sFriedrichEbertPlatz
            }
            val aBreiteStrasse25 = Address.new {
                houseNumber = "25"
                extra = null
                street = sBreiteStrasse
            }
            val aAmFasanengarten5 = Address.new {
                houseNumber = "5"
                extra = null
                street = sAmFasanengarten
            }
            val aAnatoleFrance5 = Address.new {
                houseNumber = "5"
                extra = "Av."
                street = sAnatoleFrance
            }
            val aCoventryRoad109 = Address.new {
                houseNumber = "109"
                extra = null
                street = sCoventryRoad
            }

            pCountries = listOf(
                cGermany,
                cFrance,
                cGB,
            )
            pStates = listOf(
                sBerlin,
                sBadenWuerttemberg,
                sIleDeFrance,
            ).map { it.load(State::country) }
            pCities = listOf(
                cBerlin,
                cSpandau,
                cKarlsruhe,
                cParis,
                cBirmingham
            )
            pPostCodes = listOf(
                p10557,
                p10117,
                p13597,
                p76131,
                p75007,
                pB100RJ,
            )
            pStreets = listOf(
                sPlatzDerRepublik,
                sWillyBrandtStrasse,
                sFriedrichEbertPlatz,
                sBreiteStrasse,
                sAmFasanengarten,
                sAnatoleFrance,
                sCoventryRoad,
            )
            pAddresses = listOf(
                aPlatzDerRepublik1,
                aPlatzDerRepublik2a,
                aWillyBrandtStrasse1,
                aFriedrichEbertPlatz2,
                aBreiteStrasse25,
                aAmFasanengarten5,
                aAnatoleFrance5,
                aCoventryRoad109,
            )
        }
    }
}
