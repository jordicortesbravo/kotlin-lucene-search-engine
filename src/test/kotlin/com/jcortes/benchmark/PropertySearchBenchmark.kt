package com.jcortes.benchmark

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jcortes.property.infrastructure.PropertyIndex
import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.springframework.core.io.ClassPathResource
import java.util.concurrent.TimeUnit

/**
 * JMH Benchmarks for Lucene search performance
 * Run with: mvn test -Dtest=PropertySearchBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(1)
open class PropertySearchBenchmark {

    private lateinit var propertyIndex: PropertyIndex
    private lateinit var testProperties: List<Property>

    @Setup
    fun setUp() {
        propertyIndex = PropertyIndex()

        // Load performance test dataset (5k properties)
        val objectMapper = ObjectMapper().registerKotlinModule()
        val resource = ClassPathResource("data/properties-test-performance.json")
        testProperties = objectMapper.readValue(
            resource.inputStream,
            object : TypeReference<List<Property>>() {}
        )

        // Build index
        propertyIndex.buildIndex(testProperties)
    }

    @Benchmark
    fun benchmarkSimpleSearch(): Long {
        val params = PropertySearchParams(limit = 20)
        val result = propertyIndex.query(params)
        return result.totalHits
    }

    @Benchmark
    fun benchmarkPriceRangeSearch(): Long {
        val params = PropertySearchParams(
            minPrice = 100,
            maxPrice = 300,
            limit = 20
        )
        val result = propertyIndex.query(params)
        return result.totalHits
    }

    @Benchmark
    fun benchmarkCitySearch(): Long {
        val params = PropertySearchParams(
            cities = listOf("Madrid", "Barcelona"),
            limit = 20
        )
        val result = propertyIndex.query(params)
        return result.totalHits
    }

    @Benchmark
    fun benchmarkComplexSearch(): Long {
        val params = PropertySearchParams(
            minPrice = 80,
            maxPrice = 250,
            cities = listOf("Madrid", "Barcelona", "Valencia"),
            propertyTypes = listOf("HOTEL", "APARTMENT"),
            amenities = listOf("WiFi", "Pool"),
            minGuests = 2,
            minRating = 4.0,
            limit = 20
        )
        val result = propertyIndex.query(params)
        return result.totalHits
    }

    @Benchmark
    fun benchmarkGeoLocationSearch(): Long {
        val params = PropertySearchParams(
            latitude = 40.4168,
            longitude = -3.7038,
            radiusKm = 50.0,
            limit = 20
        )
        val result = propertyIndex.query(params)
        return result.totalHits
    }

    @Benchmark
    fun benchmarkFacetedSearch(): Long {
        val params = PropertySearchParams(
            cities = listOf("Madrid", "Barcelona"),
            facets = listOf("city", "type", "amenities", "priceRange"),
            limit = 20
        )
        val result = propertyIndex.query(params)
        return result.totalHits
    }

    @Benchmark
    fun benchmarkComplexFacetedSearch(): Long {
        val params = PropertySearchParams(
            minPrice = 100,
            maxPrice = 400,
            cities = listOf("Madrid", "Barcelona", "Valencia"),
            propertyTypes = listOf("HOTEL", "VILLA"),
            amenities = listOf("WiFi"),
            minGuests = 4,
            minRating = 4.2,
            facets = listOf("city", "type", "amenities", "priceRange"),
            limit = 50
        )
        val result = propertyIndex.query(params)
        return result.totalHits
    }

    @Benchmark
    fun benchmarkLargeResultSet(): Long {
        val params = PropertySearchParams(
            minPrice = 50,
            maxPrice = 500,
            limit = 100 // Larger result set
        )
        val result = propertyIndex.query(params)
        return result.totalHits
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val options = OptionsBuilder()
                .include(PropertySearchBenchmark::class.java.simpleName)
                .build()

            Runner(options).run()
        }
    }
}