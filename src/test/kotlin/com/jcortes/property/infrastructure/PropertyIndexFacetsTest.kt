package com.jcortes.property.infrastructure

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyIndexFacetsTest {

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
    fun `should return city facets`() {
        val params = PropertySearchParams(
            facets = listOf("city")
        )
        val result = propertyIndex.query(params)

        assertEquals(1, result.facets.size)
        val cityFacet = result.facets.first()
        assertEquals("city", cityFacet.name)

        // Should have buckets for all cities in test data
        assertTrue(cityFacet.buckets.isNotEmpty())

        // Verify Madrid has 3 properties
        val madridBucket = cityFacet.buckets.find { it.value == "Madrid" }
        assertEquals(4L, madridBucket?.count)

        // Verify Barcelona has 2 properties
        val barcelonaBucket = cityFacet.buckets.find { it.value == "Barcelona" }
        assertEquals(3L, barcelonaBucket?.count)
    }

    @Test
    fun `should return type facets`() {
        val params = PropertySearchParams(
            facets = listOf("type")
        )
        val result = propertyIndex.query(params)

        assertEquals(1, result.facets.size)
        val typeFacet = result.facets.first()
        assertEquals("type", typeFacet.name)

        // Should have buckets for different property types
        assertTrue(typeFacet.buckets.isNotEmpty())

        // Verify we have HOTEL, APARTMENT, and VILLA buckets
        val hotelBucket = typeFacet.buckets.find { it.value == "HOTEL" }
        assertTrue(hotelBucket != null && hotelBucket.count > 0)

        val apartmentBucket = typeFacet.buckets.find { it.value == "APARTMENT" }
        assertTrue(apartmentBucket != null && apartmentBucket.count > 0)

        val villaBucket = typeFacet.buckets.find { it.value == "VILLA" }
        assertTrue(villaBucket != null && villaBucket.count > 0)
    }

    @Test
    fun `should return amenities facets`() {
        val params = PropertySearchParams(
            facets = listOf("amenities")
        )
        val result = propertyIndex.query(params)

        assertEquals(1, result.facets.size)
        val amenitiesFacet = result.facets.first()
        assertEquals("amenities", amenitiesFacet.name)

        // Should have buckets for different amenities
        assertTrue(amenitiesFacet.buckets.isNotEmpty())

        // WiFi should be in most properties
        val wifiBucket = amenitiesFacet.buckets.find { it.value == "WiFi" }
        assertTrue(wifiBucket != null && wifiBucket.count >= 9) // 9/10 properties have WiFi

        // Pool should be in some properties
        val poolBucket = amenitiesFacet.buckets.find { it.value == "Pool" }
        assertTrue(poolBucket != null && poolBucket.count > 0)
    }

    @Test
    fun `should return price range facets`() {
        val params = PropertySearchParams(
            facets = listOf("priceRange")
        )
        val result = propertyIndex.query(params)

        assertEquals(1, result.facets.size)
        val priceRangeFacet = result.facets.first()
        assertEquals("priceRange", priceRangeFacet.name)

        // Should have buckets for different price ranges
        assertTrue(priceRangeFacet.buckets.isNotEmpty())

        // Should have Budget (45€), Standard (80€, 90€, 110€, 120€, 150€), Premium (200€, 250€), Ultra-Luxury (300€, 550€)
        val budgetBucket = priceRangeFacet.buckets.find { it.value == "Budget" }
        assertEquals(3L, budgetBucket?.count) // Only the 45€ hostel

        val standardBucket = priceRangeFacet.buckets.find { it.value == "Standard" }
        assertTrue(standardBucket != null && standardBucket.count > 0)

        val premiumBucket = priceRangeFacet.buckets.find { it.value == "Premium" }
        assertTrue(premiumBucket != null && premiumBucket.count > 0)

        val ultraLuxuryBucket = priceRangeFacet.buckets.find { it.value == "Ultra-Luxury" }
        assertTrue(ultraLuxuryBucket != null && ultraLuxuryBucket.count > 0)
    }

    @Test
    fun `should return multiple facets`() {
        val params = PropertySearchParams(
            facets = listOf("city", "type", "amenities", "priceRange")
        )
        val result = propertyIndex.query(params)

        assertEquals(4, result.facets.size)

        val facetNames = result.facets.map { it.name }.toSet()
        assertEquals(setOf("city", "type", "amenities", "priceRange"), facetNames)

        // Each facet should have buckets
        result.facets.forEach { facet ->
            assertTrue(facet.buckets.isNotEmpty(), "Facet ${facet.name} should have buckets")
        }
    }

    @Test
    fun `should filter and facet simultaneously`() {
        // Filter by Madrid and get type facets
        val params = PropertySearchParams(
            cities = listOf("Madrid"),
            facets = listOf("type")
        )
        val result = propertyIndex.query(params)

        // Should only return Madrid properties
        assertEquals(4, result.totalHits)
        result.items.forEach { property ->
            assertEquals("Madrid", property.location.city)
        }

        // Facets should only count Madrid properties
        assertEquals(1, result.facets.size)
        val typeFacet = result.facets.first()

        // Sum of all type bucket counts should equal total Madrid properties
        val totalFacetCount = typeFacet.buckets.sumOf { it.count }
        assertEquals(result.totalHits, totalFacetCount)
    }

    @Test
    fun `should return empty facets for no results`() {
        val params = PropertySearchParams(
            minPrice = 2000, // No property costs this much
            facets = listOf("city", "type")
        )
        val result = propertyIndex.query(params)

        assertEquals(0, result.totalHits)
        assertEquals(0, result.facets.size)

        // All facet buckets should be empty
        result.facets.forEach { facet ->
            assertTrue(facet.buckets.isEmpty(), "Facet ${facet.name} should have no buckets for empty results")
        }
    }

    @Test
    fun `should ignore unknown facet names`() {
        val params = PropertySearchParams(
            facets = listOf("city", "unknown_facet", "type")
        )
        val result = propertyIndex.query(params)

        // Should only return facets for known fields (city and type)
        assertEquals(2, result.facets.size)
        val facetNames = result.facets.map { it.name }.toSet()
        assertEquals(setOf("city", "type"), facetNames)
    }

    @Test
    fun `facet counts should be accurate`() {
        val params = PropertySearchParams(
            facets = listOf("city")
        )
        val result = propertyIndex.query(params)

        val cityFacet = result.facets.first()
        val totalFacetCount = cityFacet.buckets.sumOf { it.count }

        // Total facet count should equal total properties
        assertEquals(result.totalHits, totalFacetCount)
        assertEquals(10L, totalFacetCount) // We have 10 test properties
    }

    @Test
    fun `amenities facet should handle multi-valued fields correctly`() {
        val params = PropertySearchParams(
            facets = listOf("amenities")
        )
        val result = propertyIndex.query(params)

        val amenitiesFacet = result.facets.first()

        // Total amenities count should be > total properties (since properties have multiple amenities)
        val totalAmenitiesCount = amenitiesFacet.buckets.sumOf { it.count }
        assertTrue(totalAmenitiesCount > result.totalHits,
                  "Total amenities count ($totalAmenitiesCount) should be greater than properties count (${result.totalHits})")

        // WiFi should appear in almost all properties
        val wifiBucket = amenitiesFacet.buckets.find { it.value == "WiFi" }
        assertTrue(wifiBucket != null && wifiBucket.count >= 9, "WiFi should be in at least 9/10 properties")
    }
}