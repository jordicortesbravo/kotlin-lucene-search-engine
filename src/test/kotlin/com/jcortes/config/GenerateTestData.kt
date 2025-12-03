package com.jcortes.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

/**
 * Generate test data files
 */
fun main() {
    val objectMapper = ObjectMapper().registerKotlinModule()
    val testResourcesPath = "src/test/resources/data"

    // Small dataset for unit tests (10 properties)
    println("Generating small test dataset (10 properties)...")
    val smallDataset = TestDataGenerator.generateTestProperties()
    writeToFile(objectMapper, smallDataset, "$testResourcesPath/properties-test-small.json")
    println("✓ Generated properties-test-small.json (${smallDataset.size} records)")

    // Medium dataset for integration tests (500 properties)
    println("Generating medium test dataset (500 properties)...")
    val mediumDataset = TestDataGenerator.generateLargeTestDataset(500)
    writeToFile(objectMapper, mediumDataset, "$testResourcesPath/properties-test-medium.json")
    println("✓ Generated properties-test-medium.json (${mediumDataset.size} records)")

    // Performance test dataset (2000 properties)
    println("Generating performance test dataset (2000 properties)...")
    val performanceDataset = TestDataGenerator.generateLargeTestDataset(2000)
    writeToFile(objectMapper, performanceDataset, "$testResourcesPath/properties-test-performance.json")
    println("✓ Generated properties-test-performance.json (${performanceDataset.size} records)")

    println("Test data generation completed!")
}

private fun writeToFile(objectMapper: ObjectMapper, properties: List<com.jcortes.property.model.Property>, filePath: String) {
    val file = File(filePath)
    file.parentFile.mkdirs()
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, properties)
}