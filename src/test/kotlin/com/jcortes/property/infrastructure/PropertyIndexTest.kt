package com.jcortes.property.infrastructure

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.ClassPathResource
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PropertyIndexTest {

    private lateinit var propertyIndex: PropertyIndex
    private lateinit var testProperties: List<Property>

    @BeforeEach
    fun setUp() {
        propertyIndex = PropertyIndex()

        // Load small test dataset
        val objectMapper = ObjectMapper().registerKotlinModule()
        val resource = ClassPathResource("data/properties-test-small.json")
        testProperties = objectMapper.readValue(
            resource.inputStream,
            object : TypeReference<List<Property>>() {}
        )

        propertyIndex.buildIndex(testProperties)
    }

    @Test
    fun `should build index successfully`() {
        // Index should be built without errors
        assertTrue(testProperties.isNotEmpty())
        assertEquals(10, testProperties.size)
    }

    @Test
    fun `should get property by id`() {
        val property = propertyIndex.get(1L)
        assertNotNull(property)
        assertEquals("Hotel Madrid Centro", property.name)
        assertEquals("Madrid", property.location.city)
        assertEquals(150, property.pricePerNight)
    }

    @Test
    fun `should throw error for non-existent property`() {
        assertThrows<IllegalStateException> {
            propertyIndex.get(999L)
        }
    }

    @Test
    fun `should search all properties with empty params`() {
        val params = PropertySearchParams()
        val result = propertyIndex.query(params)

        assertEquals(10, result.totalHits)
        assertEquals(10, result.items.size)
        assertTrue(result.facets.isEmpty())
    }

    @Test
    fun `should filter by price range`() {
        val params = PropertySearchParams(
            minPrice = 100,
            maxPrice = 200
        )
        val result = propertyIndex.query(params)

        assertTrue(result.totalHits > 0)
        result.items.forEach { property ->
            assertTrue(property.pricePerNight >= 100)
            assertTrue(property.pricePerNight <= 200)
        }
    }

    @Test
    fun `should filter by city`() {
        val params = PropertySearchParams(
            cities = listOf("Madrid")
        )
        val result = propertyIndex.query(params)

        assertEquals(4, result.totalHits) // Madrid has 4 properties in test data
        result.items.forEach { property ->
            assertEquals("Madrid", property.location.city)
        }
    }

    @Test
    fun `should filter by property type`() {
        val params = PropertySearchParams(
            propertyTypes = listOf("HOTEL")
        )
        val result = propertyIndex.query(params)

        assertTrue(result.totalHits > 0)
        result.items.forEach { property ->
            assertEquals("HOTEL", property.type.name)
        }
    }

    @Test
    fun `should filter by multiple cities`() {
        val params = PropertySearchParams(
            cities = listOf("Madrid", "Barcelona")
        )
        val result = propertyIndex.query(params)

        assertEquals(7, result.totalHits) // Madrid(4) + Barcelona(3) = 7 properties
        result.items.forEach { property ->
            assertTrue(property.location.city in listOf("Madrid", "Barcelona"))
        }
    }

    @Test
    fun `should filter by amenities`() {
        val params = PropertySearchParams(
            amenities = listOf("Pool")
        )
        val result = propertyIndex.query(params)

        assertTrue(result.totalHits > 0)
        result.items.forEach { property ->
            assertTrue("Pool" in property.amenities)
        }
    }

    @Test
    fun `should filter by minimum guests`() {
        val params = PropertySearchParams(
            minGuests = 6
        )
        val result = propertyIndex.query(params)

        assertTrue(result.totalHits > 0)
        result.items.forEach { property ->
            assertTrue(property.maxGuests >= 6)
        }
    }

    @Test
    fun `should filter by minimum bedrooms`() {
        val params = PropertySearchParams(
            minBedrooms = 3
        )
        val result = propertyIndex.query(params)

        assertTrue(result.totalHits > 0)
        result.items.forEach { property ->
            assertTrue(property.bedrooms >= 3)
        }
    }

    @Test
    fun `should filter by minimum rating`() {
        val params = PropertySearchParams(
            minRating = 4.5
        )
        val result = propertyIndex.query(params)

        assertTrue(result.totalHits > 0)
        result.items.forEach { property ->
            assertTrue(property.rating >= 4.5)
        }
    }

    @Test
    fun `should combine multiple filters`() {
        val params = PropertySearchParams(
            cities = listOf("Madrid"),
            minPrice = 100,
            maxPrice = 200,
            propertyTypes = listOf("HOTEL")
        )
        val result = propertyIndex.query(params)

        result.items.forEach { property ->
            assertEquals("Madrid", property.location.city)
            assertTrue(property.pricePerNight >= 100)
            assertTrue(property.pricePerNight <= 200)
            assertEquals("HOTEL", property.type.name)
        }
    }

    @Test
    fun `should return empty results for impossible criteria`() {
        val params = PropertySearchParams(
            minPrice = 1000, // No property costs more than 1000 in test data
            maxPrice = 2000
        )
        val result = propertyIndex.query(params)

        assertEquals(0, result.totalHits)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `should limit results`() {
        val params = PropertySearchParams(limit = 5)
        val result = propertyIndex.query(params)

        assertTrue(result.items.size <= 5)
        assertEquals(10, result.totalHits) // Total hits should still show all matches
    }

    @Test
    fun `should perform geo-location search`() {
        // Search near Madrid center (40.4168, -3.7038) within 5km
        val params = PropertySearchParams(
            latitude = 40.4168,
            longitude = -3.7038,
            radiusKm = 5.0
        )
        val result = propertyIndex.query(params)

        assertTrue(result.totalHits > 0)
        // All results should be Madrid properties (within 5km radius)
        result.items.forEach { property ->
            assertEquals("Madrid", property.location.city)
        }
    }

    @Test
    fun `should perform geo-location search with no results`() {
        // Search in the middle of the ocean
        val params = PropertySearchParams(
            latitude = 0.0,
            longitude = 0.0,
            radiusKm = 1.0
        )
        val result = propertyIndex.query(params)

        assertEquals(0, result.totalHits)
        assertTrue(result.items.isEmpty())
    }
}