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
class PostCodeTest(
    @Autowired private val testEntityManager: TestEntityManager,
) {
    private lateinit var copy: PostCode
    private lateinit var notSaved: PostCode

    @BeforeEach
    fun `set up`() {
        SampleData.reset(testEntityManager)

        copy = testEntityManager.find(PostCode::class.java, SampleData.postCodes[0].id)

        notSaved = with(SampleData.postCodes[1]) {
            PostCode(
                city = city,
                code = code,
            )
        }
    }

    @Test
    fun `test equals`() {
        assertEquals(SampleData.postCodes[0], SampleData.postCodes[0])
        assertEquals(copy, copy)
        assertEquals(SampleData.postCodes[0], copy)
        assertEquals(copy, SampleData.postCodes[0])

        assertNotEquals(SampleData.postCodes[0], SampleData.postCodes[1])
        assertNotEquals(copy, SampleData.postCodes[1])
        assertNotEquals(SampleData.postCodes[1], SampleData.postCodes[0])
        assertNotEquals(SampleData.postCodes[1], copy)

        assertNotEquals<PostCode?>(copy, null)
        assertNotEquals<PostCode?>(null, SampleData.postCodes[0])

        assertNotEquals(SampleData.postCodes[1], notSaved)
        assertNotEquals(notSaved, SampleData.postCodes[1])
    }

    @Test
    fun `test hashCode`() {
        assertEquals(SampleData.postCodes[0].hashCode(), SampleData.postCodes[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(SampleData.postCodes[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), SampleData.postCodes[0].hashCode())

        assertNotEquals(SampleData.postCodes[0].hashCode(), SampleData.postCodes[1].hashCode())
        assertNotEquals(copy.hashCode(), SampleData.postCodes[1].hashCode())
        assertNotEquals(SampleData.postCodes[1].hashCode(), SampleData.postCodes[0].hashCode())
        assertNotEquals(SampleData.postCodes[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), SampleData.postCodes[0].hashCode())

        assertNotEquals(SampleData.postCodes[1].hashCode(), notSaved.hashCode())
        assertNotEquals(notSaved.hashCode(), SampleData.postCodes[1].hashCode())
    }

    @Test
    fun `test toString`() {
        val cId = copy.id
        val pId = copy.city.id
        val cExpected = "PostCode(id='$cId', city='$pId', code='10557')"
        val cActual = SampleData.postCodes[0].toString()
        assertEquals(cExpected, cActual)

        val nId = notSaved.city.id
        val nExpected = "PostCode(id='null', city='$nId', code='10117')"
        val nActual = notSaved.toString()
        assertEquals(nExpected, nActual)
    }
}
