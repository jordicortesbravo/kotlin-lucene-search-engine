package com.jcortes.property

import com.jcortes.property.model.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PropertyServiceTest {

    private lateinit var propertyRepository: PropertyRepository
    private lateinit var propertyService: PropertyService

    private val testProperty = Property(
        id = 1L,
        name = "Test Hotel",
        type = PropertyType.HOTEL,
        description = "A test hotel",
        location = Location("Madrid", 40.4168, -3.7038),
        pricePerNight = 100,
        amenities = listOf("WiFi", "Pool"),
        maxGuests = 4,
        bedrooms = 2,
        rating = 4.5
    )

    @BeforeEach
    fun setUp() {
        propertyRepository = mock()
        propertyService = PropertyService(propertyRepository)
    }

    @Test
    fun `should get property by id when exists`() {
        // Given
        whenever(propertyRepository.get(1L)).thenReturn(testProperty)

        // When
        val result = propertyService.get(1L)

        // Then
        assertEquals(testProperty, result)
        verify(propertyRepository).get(1L)
    }

    @Test
    fun `should return null when property does not exist`() {
        // Given
        whenever(propertyRepository.get(999L)).thenReturn(null)

        // When
        val result = propertyService.get(999L)

        // Then
        assertNull(result)
        verify(propertyRepository).get(999L)
    }

    @Test
    fun `should search properties with parameters`() {
        // Given
        val searchParams = PropertySearchParams(
            minPrice = 50,
            maxPrice = 200,
            cities = listOf("Madrid")
        )

        val expectedResponse = PropertySearchResponse(
            totalHits = 1L,
            tookMs = 10L,
            items = listOf(testProperty),
            facets = emptyList()
        )

        whenever(propertyRepository.search(searchParams)).thenReturn(expectedResponse)

        // When
        val result = propertyService.search(searchParams)

        // Then
        assertEquals(expectedResponse, result)
        verify(propertyRepository).search(searchParams)
    }

    @Test
    fun `should search with empty parameters`() {
        // Given
        val searchParams = PropertySearchParams()
        val expectedResponse = PropertySearchResponse(
            totalHits = 10L,
            tookMs = 5L,
            items = listOf(testProperty),
            facets = emptyList()
        )

        whenever(propertyRepository.search(searchParams)).thenReturn(expectedResponse)

        // When
        val result = propertyService.search(searchParams)

        // Then
        assertEquals(expectedResponse, result)
        verify(propertyRepository).search(searchParams)
    }

    @Test
    fun `should search with facets`() {
        // Given
        val searchParams = PropertySearchParams(
            cities = listOf("Madrid"),
            facets = listOf("city", "type")
        )

        val expectedResponse = PropertySearchResponse(
            totalHits = 1L,
            tookMs = 15L,
            items = listOf(testProperty),
            facets = emptyList() // Facets would be populated by repository
        )

        whenever(propertyRepository.search(searchParams)).thenReturn(expectedResponse)

        // When
        val result = propertyService.search(searchParams)

        // Then
        assertEquals(expectedResponse, result)
        verify(propertyRepository).search(searchParams)
    }

    @Test
    fun `should handle complex search parameters`() {
        // Given
        val searchParams = PropertySearchParams(
            minPrice = 100,
            maxPrice = 300,
            minGuests = 2,
            minBedrooms = 1,
            minRating = 4.0,
            amenities = listOf("WiFi", "Pool"),
            cities = listOf("Madrid", "Barcelona"),
            propertyTypes = listOf("HOTEL", "VILLA"),
            latitude = 40.4168,
            longitude = -3.7038,
            radiusKm = 10.0,
            limit = 20,
            facets = listOf("city", "type", "amenities")
        )

        val expectedResponse = PropertySearchResponse(
            totalHits = 5L,
            tookMs = 25L,
            items = listOf(testProperty),
            facets = emptyList()
        )

        whenever(propertyRepository.search(searchParams)).thenReturn(expectedResponse)

        // When
        val result = propertyService.search(searchParams)

        // Then
        assertEquals(expectedResponse, result)
        verify(propertyRepository).search(searchParams)
    }
}