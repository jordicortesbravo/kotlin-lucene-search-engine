package com.jcortes.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jcortes.property.infrastructure.PropertyIndex
import com.jcortes.property.model.Property
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Component
class DataLoader(
    private val propertyIndex: PropertyIndex,
    private val objectMapper: ObjectMapper,
    @Value("\${search-engine.data.source:file://5k}") private val dataSource: String
): ApplicationListener<ContextRefreshedEvent> {

    private val logger = LoggerFactory.getLogger(DataLoader::class.java)

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        logger.info("🚀 Starting Lucene index initialization...")
        logger.info("Loading properties from source: {}", dataSource)

        try {
            // Load properties based on configuration
            val properties = loadProperties()

            // Build Lucene index
            logger.info("Building Lucene index with {} properties...", properties.size)

            val indexingTime = measureTimeMillis {
                propertyIndex.buildIndex(properties)
            }

            logger.info("✅ Lucene index built successfully!")
            logger.info("📊 Index Statistics:")
            logger.info("   • Properties indexed: {}", properties.size)
            logger.info("   • Indexing time: {} ms", indexingTime)
            logger.info("   • Average time per property: %.2f ms".format(indexingTime.toDouble() / properties.size))
            logger.info("   • Data source: {}", dataSource)

        } catch (e: Exception) {
            logger.error("❌ Failed to initialize Lucene index: {}", e.message, e)
            throw e
        }
    }

    private fun loadProperties(): List<Property> {
        return when {
            dataSource.startsWith("file://") -> loadFromFile(dataSource)
            dataSource.startsWith("generate://") -> generateProperties(dataSource)
            else -> throw IllegalArgumentException("Invalid data source: $dataSource. Use file://[size] or generate://[count]")
        }
    }

    private fun loadFromFile(source: String): List<Property> {
        val fileKey = source.removePrefix("file://")
        val fileName = "data/properties-$fileKey.json"

        logger.info("Loading properties from file: {}", fileName)

        return try {
            val resource = ClassPathResource(fileName)
            val loadedProperties: List<Property>
            val elapsedTime = measureTimeMillis {
                loadedProperties = objectMapper.readValue(
                    resource.inputStream,
                    object : TypeReference<List<Property>>() {}
                )
            }

            logger.info("✓ Loaded {} properties from {} in {} ms", loadedProperties.size, fileName, elapsedTime)
            loadedProperties

        } catch (e: Exception) {
            logger.error("Failed to load properties from {}: {}", fileName, e.message)
            logger.info("Available files: properties-5k.json, properties-50k.json")
            logger.info("To generate files, run: mvn exec:java -Dexec.mainClass=com.jcortes.config.DataFileGenerator")
            throw IllegalStateException("Could not load data file: $fileName", e)
        }
    }

    private fun generateProperties(source: String): List<Property> {
        val countStr = source.removePrefix("generate://")
        val count = countStr.toIntOrNull() ?: throw IllegalArgumentException("Invalid count: $countStr")

        logger.info("Generating {} properties in memory", count)

        val properties: List<Property>
        val elapsedTime = measureTimeMillis {
            properties = PropertyDataGenerator.generateProperties(count)
        }

        logger.info("✓ Generated {} properties in {} ms", properties.size, elapsedTime)
        return properties
    }
}