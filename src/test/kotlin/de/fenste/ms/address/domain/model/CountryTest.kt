package de.fenste.ms.address.domain.model

import de.fenste.ms.address.test.SampleData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@DataJpaTest
class CountryTest(
    @Autowired private val testEntityManager: TestEntityManager,
) {
    private lateinit var copy: Country
    private lateinit var notSaved: Country

    @BeforeEach
    fun `set up`() {
        SampleData.reset(testEntityManager)

        copy = testEntityManager.find(Country::class.java, SampleData.countries[0].id)

        notSaved = with(SampleData.countries[1]) {
            Country(
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test equals`() {
        assertEquals(SampleData.countries[0], SampleData.countries[0])
        assertEquals(copy, copy)
        assertEquals(SampleData.countries[0], copy)
        assertEquals(copy, SampleData.countries[0])

        assertNotEquals(SampleData.countries[0], SampleData.countries[1])
        assertNotEquals(copy, SampleData.countries[1])
        assertNotEquals(SampleData.countries[1], SampleData.countries[0])
        assertNotEquals(SampleData.countries[1], copy)

        assertNotEquals<Country?>(copy, null)
        assertNotEquals<Country?>(null, SampleData.countries[0])

        assertNotEquals(SampleData.countries[1], notSaved)
        assertNotEquals(notSaved, SampleData.countries[1])
    }

    @Test
    fun `test hashCode`() {
        assertEquals(SampleData.countries[0].hashCode(), SampleData.countries[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(SampleData.countries[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), SampleData.countries[0].hashCode())

        assertNotEquals(SampleData.countries[0].hashCode(), SampleData.countries[1].hashCode())
        assertNotEquals(copy.hashCode(), SampleData.countries[1].hashCode())
        assertNotEquals(SampleData.countries[1].hashCode(), SampleData.countries[0].hashCode())
        assertNotEquals(SampleData.countries[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), SampleData.countries[0].hashCode())

        assertNotEquals(SampleData.countries[1].hashCode(), notSaved.hashCode())
        assertNotEquals(notSaved.hashCode(), SampleData.countries[1].hashCode())
    }

    @Test
    fun `test toString`() {
        val cId = copy.id
        val cExpected =
            "Country(id='$cId', alpha2='DE', alpha3='DEU', name='Germany', localizedName='Deutschland')"
        val cActual = SampleData.countries[0].toString()
        assertEquals(cExpected, cActual)

        val nExpected =
            "Country(id='null', alpha2='FR', alpha3='FRA', name='France', localizedName='Frankreich')"
        val nActual = notSaved.toString()
        assertEquals(nExpected, nActual)
    }
}
