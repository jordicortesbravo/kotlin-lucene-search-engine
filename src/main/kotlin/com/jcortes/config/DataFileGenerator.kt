package com.jcortes.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

/**
 * Utility to generate sample data files for development and testing
 * Run this main function to create the sample data files
 */
object DataFileGenerator {

    private val objectMapper = ObjectMapper().registerKotlinModule()
    private const val N_PROPERTIES = 5_000

    @JvmStatic
    fun main(args: Array<String>) {
        val resourcesPath = "src/main/resources/data"

        println("Generating 5,000 properties...")
        val properties5k = PropertyDataGenerator.generateProperties(N_PROPERTIES)
        writePropertiesToFile(properties5k, "$resourcesPath/properties-${N_PROPERTIES / 1_000}k.json")
        println("✓ Generated properties-5k.json (${properties5k.size} records)")
    }

    private fun writePropertiesToFile(properties: List<com.jcortes.property.model.Property>, filePath: String) {
        val file = File(filePath)
        file.parentFile.mkdirs()
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, properties)
    }
}