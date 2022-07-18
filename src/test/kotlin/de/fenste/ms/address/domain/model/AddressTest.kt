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
class AddressTest(
    @Autowired private val testEntityManager: TestEntityManager,
) {
    private lateinit var copy: Address
    private lateinit var notSaved: Address

    @BeforeEach
    fun `set up`() {
        SampleData.reset(testEntityManager)

        copy = testEntityManager.find(Address::class.java, SampleData.addresses[0].id)

        notSaved = with(SampleData.addresses[1]) {
            Address(
                street = street,
                houseNumber = houseNumber,
                extra = extra,
            )
        }
    }

    @Test
    fun `test equals`() {
        assertEquals(SampleData.addresses[0], SampleData.addresses[0])
        assertEquals(copy, copy)
        assertEquals(SampleData.addresses[0], copy)
        assertEquals(copy, SampleData.addresses[0])

        assertNotEquals(SampleData.addresses[0], SampleData.addresses[1])
        assertNotEquals(copy, SampleData.addresses[1])
        assertNotEquals(SampleData.addresses[1], SampleData.addresses[0])
        assertNotEquals(SampleData.addresses[1], copy)

        assertNotEquals<Address?>(copy, null)
        assertNotEquals<Address?>(null, SampleData.addresses[0])

        assertNotEquals(SampleData.addresses[1], notSaved)
        assertNotEquals(notSaved, SampleData.addresses[1])
    }

    @Test
    fun `test hashCode`() {
        assertEquals(SampleData.addresses[0].hashCode(), SampleData.addresses[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(SampleData.addresses[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), SampleData.addresses[0].hashCode())

        assertNotEquals(SampleData.addresses[0].hashCode(), SampleData.addresses[1].hashCode())
        assertNotEquals(copy.hashCode(), SampleData.addresses[1].hashCode())
        assertNotEquals(SampleData.addresses[1].hashCode(), SampleData.addresses[0].hashCode())
        assertNotEquals(SampleData.addresses[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), SampleData.addresses[0].hashCode())

        assertNotEquals(SampleData.addresses[1].hashCode(), notSaved.hashCode())
        assertNotEquals(notSaved.hashCode(), SampleData.addresses[1].hashCode())
    }

    @Test
    fun `test toString`() {
        val cId = copy.id
        val pId = copy.street.id
        val cExpected = "Address(id='$cId', street='$pId', houseNumber='1', extra='null')"
        val cActual = SampleData.addresses[0].toString()
        assertEquals(cExpected, cActual)

        val nId = notSaved.street.id
        val nExpected = "Address(id='null', street='$nId', houseNumber='2a', extra='null')"
        val nActual = notSaved.toString()
        assertEquals(nExpected, nActual)
    }
}
