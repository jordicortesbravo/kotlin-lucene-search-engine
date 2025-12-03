package com.jcortes.config

import com.jcortes.property.model.Location
import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertyType

/**
 * Generates small, controlled datasets for testing
 */
object TestDataGenerator {

    fun generateTestProperties(): List<Property> = listOf(
        // Madrid properties
        Property(1L, "Hotel Madrid Centro", PropertyType.HOTEL, "Luxury hotel in city center",
            Location("Madrid", 40.4168, -3.7038), 150,
            listOf("WiFi", "Pool", "Gym"), 4, 2, 4.5),

        Property(2L, "Apartment Madrid", PropertyType.APARTMENT, "Modern apartment near Retiro",
            Location("Madrid", 40.4200, -3.6900), 80,
            listOf("WiFi", "Kitchen", "Balcony"), 2, 1, 4.2),

        Property(3L, "Villa Madrid Luxury", PropertyType.VILLA, "Exclusive villa with garden",
            Location("Madrid", 40.4300, -3.6800), 300,
            listOf("WiFi", "Pool", "Garden", "Parking"), 8, 4, 4.8),

        // Barcelona properties
        Property(4L, "Hotel Barcelona Beach", PropertyType.HOTEL, "Beachfront hotel with sea views",
            Location("Barcelona", 41.3851, 2.1734), 200,
            listOf("WiFi", "Pool", "Beach Access", "Spa"), 6, 3, 4.6),

        Property(5L, "Barcelona Apartment", PropertyType.APARTMENT, "Cozy apartment in Gothic Quarter",
            Location("Barcelona", 41.3800, 2.1700), 120,
            listOf("WiFi", "City Center", "Balcony"), 4, 2, 4.3),

        // Valencia properties
        Property(6L, "Valencia Modern Hotel", PropertyType.HOTEL, "Business hotel near City of Arts",
            Location("Valencia", 39.4699, -0.3763), 90,
            listOf("WiFi", "Gym", "Restaurant"), 4, 2, 4.1),

        Property(7L, "Valencia Beach Villa", PropertyType.VILLA, "Villa near Malvarosa Beach",
            Location("Valencia", 39.4800, -0.3200), 250,
            listOf("WiFi", "Beach Access", "Pool", "Terrace"), 10, 5, 4.7),

        // Sevilla properties
        Property(8L, "Sevilla Historic Hotel", PropertyType.HOTEL, "Traditional hotel in historic center",
            Location("Sevilla", 37.3886, -5.9823), 110,
            listOf("WiFi", "City Center", "Restaurant"), 4, 2, 4.4),

        // Budget properties for price range testing
        Property(9L, "Budget Madrid Hostel", PropertyType.HOTEL, "Affordable accommodation",
            Location("Madrid", 40.4100, -3.7100), 45,
            listOf("WiFi"), 2, 1, 3.8),

        Property(10L, "Ultra Luxury Barcelona", PropertyType.VILLA, "Premium beachfront villa",
            Location("Barcelona", 41.3900, 2.1800), 550,
            listOf("WiFi", "Pool", "Spa", "Beach Access", "Concierge", "Restaurant"), 12, 6, 4.9)
    )

    fun generateLargeTestDataset(size: Int = 500): List<Property> {
        return PropertyDataGenerator.generateProperties(size)
    }
}