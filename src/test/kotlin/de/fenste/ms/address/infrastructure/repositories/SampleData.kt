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

import de.fenste.ms.address.domain.model.Address
import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.domain.model.PostCode
import de.fenste.ms.address.domain.model.State
import de.fenste.ms.address.domain.model.Street
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

object SampleData {

    private var pCountries: Collection<Country>? = null
    private var pStates: Collection<State>? = null
    private var pCities: Collection<City>? = null
    private var pPostCodes: Collection<PostCode>? = null
    private var pStreets: Collection<Street>? = null
    private var pAddresses: Collection<Address>? = null

    val countries: Collection<Country>
        get() = pCountries!!
    val states: Collection<State>
        get() = pStates!!
    val cities: Collection<City>
        get() = pCities!!
    val postCodes: Collection<PostCode>
        get() = pPostCodes!!
    val streets: Collection<Street>
        get() = pStreets!!
    val addresses: Collection<Address>
        get() = pAddresses!!

    @Suppress("LongMethod")
    fun reset(testEntityManager: TestEntityManager) {
        testEntityManager.clear()

        val cGermany = testEntityManager.persist(
            Country(
                alpha2 = "DE",
                alpha3 = "DEU",
                name = "Germany",
                localizedName = "Deutschland",
            )
        )
        val cFrance = testEntityManager.persist(
            Country(
                alpha2 = "FR",
                alpha3 = "FRA",
                name = "France",
                localizedName = "Frankreich",
            )
        )
        val cGB = testEntityManager.persist(
            Country(
                alpha2 = "GB",
                alpha3 = "GBR",
                name = "United Kingdom of Great Britain and Northern Ireland",
                localizedName = "Großbritannien und Nordirland",
            )
        )

        val sBerlin = testEntityManager.persist(
            State(
                name = "Berlin",
                country = cGermany,
            )
        )
        val sBadenWuerttemberg = testEntityManager.persist(
            State(
                name = "Baden-Württemberg",
                country = cGermany,
            )
        )
        val sIleDeFrance = testEntityManager.persist(
            State(
                name = "Ile-de-France",
                country = cFrance,
            )
        )

        val cBerlin = testEntityManager.persist(
            City(
                name = "Berlin",
                country = cGermany,
                state = sBerlin,
            )
        )
        val cSpandau = testEntityManager.persist(
            City(
                name = "Berlin-Spandau",
                country = cGermany,
                state = sBerlin,
            )
        )
        val cKarlsruhe = testEntityManager.persist(
            City(
                name = "Karlsruhe",
                country = cGermany,
                state = sBadenWuerttemberg,
            )
        )
        val cParis = testEntityManager.persist(
            City(
                name = "Paris",
                country = cFrance,
                state = sIleDeFrance,
            )
        )
        val cBirmingham = testEntityManager.persist(
            City(
                name = "Birmingham",
                country = cGB,
                state = null,
            )
        )

        val p10557 = testEntityManager.persist(
            PostCode(
                code = "10557",
                city = cBerlin,
            )
        )
        val p10117 = testEntityManager.persist(
            PostCode(
                code = "10117",
                city = cBerlin,
            )
        )
        val p13597 = testEntityManager.persist(
            PostCode(
                code = "13597",
                city = cSpandau,
            )
        )
        val p76131 = testEntityManager.persist(
            PostCode(
                code = "76131",
                city = cKarlsruhe,
            )
        )
        val p75007 = testEntityManager.persist(
            PostCode(
                code = "75007",
                city = cParis,
            )
        )
        val pB100RJ = testEntityManager.persist(
            PostCode(
                code = "B10 0RJ",
                city = cBirmingham,
            )
        )

        val sPlatzDerRepublik = testEntityManager.persist(
            Street(
                name = "Platz der Republik",
                postCode = p10557,
            )
        )
        val sWillyBrandtStrasse = testEntityManager.persist(
            Street(
                name = "Willy-Brandt-Straße",
                postCode = p10557,
            )
        )
        val sFriedrichEbertPlatz = testEntityManager.persist(
            Street(
                name = "Friedrich-Ebert-Platz",
                postCode = p10117,
            )
        )
        val sBreiteStrasse = testEntityManager.persist(
            Street(
                name = "Breite Str.",
                postCode = p13597,
            )
        )
        val sAmFasanengarten = testEntityManager.persist(
            Street(
                name = "Am Fasanengarten",
                postCode = p76131,
            )
        )
        val sAnatoleFrance = testEntityManager.persist(
            Street(
                name = "Anatole France",
                postCode = p75007,
            )
        )
        val sCoventryRoad = testEntityManager.persist(
            Street(
                name = "Coventry Rd",
                postCode = pB100RJ,
            )
        )

        val aPlatzDerRepublik1 = testEntityManager.persist(
            Address(
                houseNumber = "1",
                extra = null,
                street = sPlatzDerRepublik,
            )
        )
        val aPlatzDerRepublik2a = testEntityManager.persist(
            Address(
                houseNumber = "2a",
                extra = null,
                street = sPlatzDerRepublik,
            )
        )
        val aWillyBrandtStrasse1 = testEntityManager.persist(
            Address(
                houseNumber = "1",
                extra = null,
                street = sWillyBrandtStrasse,
            )
        )
        val aFriedrichEbertPlatz2 = testEntityManager.persist(
            Address(
                houseNumber = "2",
                extra = null,
                street = sFriedrichEbertPlatz,
            )
        )
        val aBreiteStrasse25 = testEntityManager.persist(
            Address(
                houseNumber = "25",
                extra = null,
                street = sBreiteStrasse,
            )
        )
        val aAmFasanengarten5 = testEntityManager.persist(
            Address(
                houseNumber = "5",
                extra = null,
                street = sAmFasanengarten,
            )
        )
        val aAnatoleFrance5 = testEntityManager.persist(
            Address(
                houseNumber = "5",
                extra = "Av.",
                street = sAnatoleFrance,
            )
        )
        val aCoventryRoad109 = testEntityManager.persist(
            Address(
                houseNumber = "109",
                extra = null,
                street = sCoventryRoad,
            )
        )

        pCountries = setOf(
            cGermany,
            cFrance,
            cGB,
        )
        pStates = setOf(
            sBerlin,
            sBadenWuerttemberg,
            sIleDeFrance,
        )
        pCities = setOf(
            cBerlin,
            cSpandau,
            cKarlsruhe,
            cParis,
            cBirmingham
        )
        pPostCodes = setOf(
            p10557,
            p10117,
            p13597,
            p76131,
            p75007,
            pB100RJ,
        )
        pStreets = setOf(
            sPlatzDerRepublik,
            sWillyBrandtStrasse,
            sFriedrichEbertPlatz,
            sBreiteStrasse,
            sAmFasanengarten,
            sAnatoleFrance,
            sCoventryRoad,
        )
        pAddresses = setOf(
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
