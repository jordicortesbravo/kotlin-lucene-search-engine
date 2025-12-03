package com.jcortes.benchmark

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class PropertySearchBenchmarkTest {

    @Test
    fun `benchmark class should instantiate and run setup successfully`() {
        val benchmark = PropertySearchBenchmark()

        // Test that setup runs without errors
        benchmark.setUp()

        // Test that benchmark methods can be called
        val simpleSearchResult = benchmark.benchmarkSimpleSearch()
        assertTrue(simpleSearchResult >= 0, "Simple search should return non-negative hit count")

        val priceRangeResult = benchmark.benchmarkPriceRangeSearch()
        assertTrue(priceRangeResult >= 0, "Price range search should return non-negative hit count")

        val citySearchResult = benchmark.benchmarkCitySearch()
        assertTrue(citySearchResult >= 0, "City search should return non-negative hit count")

        val complexSearchResult = benchmark.benchmarkComplexSearch()
        assertTrue(complexSearchResult >= 0, "Complex search should return non-negative hit count")

        val geoSearchResult = benchmark.benchmarkGeoLocationSearch()
        assertTrue(geoSearchResult >= 0, "Geo search should return non-negative hit count")

        val facetedSearchResult = benchmark.benchmarkFacetedSearch()
        assertTrue(facetedSearchResult >= 0, "Faceted search should return non-negative hit count")

        val complexFacetedResult = benchmark.benchmarkComplexFacetedSearch()
        assertTrue(complexFacetedResult >= 0, "Complex faceted search should return non-negative hit count")

        val largeResultSetResult = benchmark.benchmarkLargeResultSet()
        assertTrue(largeResultSetResult >= 0, "Large result set search should return non-negative hit count")

        println("✅ All benchmark methods executed successfully!")
        println("📊 Sample results:")
        println("   Simple search: $simpleSearchResult hits")
        println("   Price range: $priceRangeResult hits")
        println("   City search: $citySearchResult hits")
        println("   Complex search: $complexSearchResult hits")
        println("   Geo search: $geoSearchResult hits")
        println("   Faceted search: $facetedSearchResult hits")
    }
}