package com.jcortes.property.entrypoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.jcortes.property.model.PropertySearchParams
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PropertyControllerIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun createUrl(path: String) = "http://localhost:$port/api/properties$path"

    @Test
    fun `should get property by id`() {
        val response = restTemplate.getForEntity(createUrl("/1"), String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)

        val jsonResponse = objectMapper.readTree(response.body)
        assertEquals(1, jsonResponse.get("id").asInt())
        assertEquals("Hotel Madrid Centro", jsonResponse.get("name").asText())
        assertEquals("Madrid", jsonResponse.get("location").get("city").asText())
        assertEquals(150, jsonResponse.get("pricePerNight").asInt())
    }

    @Test
    fun `should return 404 for non-existent property`() {
        val response = restTemplate.getForEntity(createUrl("/999"), String::class.java)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should search all properties with empty parameters`() {
        val searchParams = PropertySearchParams()
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(objectMapper.writeValueAsString(searchParams), headers)

        val response = restTemplate.postForEntity(createUrl("/search"), entity, String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)

        val jsonResponse = objectMapper.readTree(response.body)
        assertEquals(10, jsonResponse.get("totalHits").asLong())
        assertTrue(jsonResponse.get("items").isArray)
        assertEquals(10, jsonResponse.get("items").size())
        assertTrue(jsonResponse.get("tookMs").isNumber)
        assertTrue(jsonResponse.get("facets").isArray)
    }

    @Test
    fun `should filter properties by city`() {
        val searchParams = PropertySearchParams(
            cities = listOf("Madrid")
        )
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(objectMapper.writeValueAsString(searchParams), headers)

        val response = restTemplate.postForEntity(createUrl("/search"), entity, String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)

        val jsonResponse = objectMapper.readTree(response.body)
        assertEquals(4, jsonResponse.get("totalHits").asLong()) // Madrid has 4 properties in test data

        val items = jsonResponse.get("items")
        assertTrue(items.isArray)
        for (item in items) {
            assertEquals("Madrid", item.get("location").get("city").asText())
        }
    }

    @Test
    fun `should filter properties by type`() {
        val searchParams = PropertySearchParams(
            propertyTypes = listOf("HOTEL")
        )
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(objectMapper.writeValueAsString(searchParams), headers)

        val response = restTemplate.postForEntity(createUrl("/search"), entity, String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)

        val jsonResponse = objectMapper.readTree(response.body)
        assertTrue(jsonResponse.get("totalHits").asLong() > 0)

        val items = jsonResponse.get("items")
        assertTrue(items.isArray)
        for (item in items) {
            assertEquals("HOTEL", item.get("type").asText())
        }
    }

    @Test
    fun `should return facets when requested`() {
        val searchParams = PropertySearchParams(
            facets = listOf("city", "type", "amenities", "priceRange")
        )
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity(objectMapper.writeValueAsString(searchParams), headers)

        val response = restTemplate.postForEntity(createUrl("/search"), entity, String::class.java)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)

        val jsonResponse = objectMapper.readTree(response.body)
        val facets = jsonResponse.get("facets")
        assertTrue(facets.isArray)
        assertEquals(4, facets.size())

        // Verify all requested facets are present
        val facetNames = facets.map { it.get("name").asText() }.toSet()
        assertTrue(facetNames.contains("city"))
        assertTrue(facetNames.contains("type"))
        assertTrue(facetNames.contains("amenities"))
        assertTrue(facetNames.contains("priceRange"))
    }

    @Test
    fun `should handle malformed JSON gracefully`() {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val entity = HttpEntity("{invalid json}", headers)

        val response = restTemplate.postForEntity(createUrl("/search"), entity, String::class.java)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}