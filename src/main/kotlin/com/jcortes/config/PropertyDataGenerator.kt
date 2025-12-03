package com.jcortes.config

import com.jcortes.property.model.Location
import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertyType
import kotlin.random.Random

object PropertyDataGenerator {

    private val cities = listOf(
        "Madrid", "Barcelona", "Valencia", "Sevilla", "Bilbao", "Zaragoza",
        "Málaga", "Palma", "Tenerife", "San Sebastián"
    )

    private val amenities = listOf(
        "WiFi", "Pool", "Gym", "Parking", "Air Conditioning", "Kitchen",
        "Balcony", "Terrace", "Garden", "Sea View", "Mountain View",
        "Pet Friendly", "Breakfast", "Room Service", "Concierge", "Spa",
        "Restaurant", "Bar", "Beach Access", "Ski Access", "City Center"
    )

    private val propertyNames = listOf(
        "Luxury Villa", "Modern Apartment", "Historic Hotel", "Cozy Studio",
        "Family House", "Boutique Hotel", "Beach Resort", "Mountain Lodge",
        "City Loft", "Countryside Retreat", "Penthouse Suite", "Garden Villa",
        "Seaside Hotel", "Urban Apartment", "Traditional Casa", "Designer Loft"
    )

    private val descriptions = listOf(
        "Stunning property with breathtaking views and modern amenities",
        "Perfect for families looking for comfort and convenience",
        "Luxury accommodation in the heart of the city",
        "Peaceful retreat with all the comforts of home",
        "Elegant design meets functionality in this beautiful space",
        "Experience authentic local culture in this charming property",
        "Modern amenities combined with traditional architecture",
        "Ideal location for exploring the city and surrounding areas"
    )

    fun generateProperties(count: Int): List<Property> {
        return (1..count).map { generateProperty(it.toLong()) }
    }

    private fun generateProperty(id: Long): Property {
        val city = cities.random()
        val location = generateLocationForCity(city)
        val type = PropertyType.entries.toTypedArray().random()
        val maxGuests = Random.nextInt(1, 13) // 1-12 guests
        val bedrooms = when (maxGuests) {
            1, 2 -> Random.nextInt(1, 3)
            3, 4 -> Random.nextInt(1, 4)
            5, 6 -> Random.nextInt(2, 5)
            else -> Random.nextInt(3, 7)
        }

        val basePrice = when (type) {
            PropertyType.HOTEL -> Random.nextInt(50, 300)
            PropertyType.APARTMENT -> Random.nextInt(40, 250)
            PropertyType.VILLA -> Random.nextInt(100, 500)
        }

        // Adjust price based on city (Madrid, Barcelona more expensive)
        val priceMultiplier = when (city) {
            "Madrid", "Barcelona" -> 1.3
            "Valencia", "Sevilla", "Bilbao" -> 1.1
            else -> 1.0
        }

        val finalPrice = (basePrice * priceMultiplier).toInt()

        return Property(
            id = id,
            name = "${propertyNames.random()} $city",
            type = type,
            description = descriptions.random(),
            location = location,
            pricePerNight = finalPrice,
            amenities = generateAmenities(),
            maxGuests = maxGuests,
            bedrooms = bedrooms,
            rating = generateRating()
        )
    }

    private fun generateLocationForCity(city: String): Location {
        // Approximate coordinates for Spanish cities
        val coordinates = mapOf(
            "Madrid" to (40.4168 to -3.7038),
            "Barcelona" to (41.3851 to 2.1734),
            "Valencia" to (39.4699 to -0.3763),
            "Sevilla" to (37.3886 to -5.9823),
            "Bilbao" to (43.2627 to -2.9253),
            "Zaragoza" to (41.6488 to -0.8891),
            "Málaga" to (36.7213 to -4.4214),
            "Palma" to (39.5696 to 2.6502),
            "Las Palmas" to (28.1248 to -15.4300),
            "Córdoba" to (37.8882 to -4.7794)
        )

        val baseCoords = coordinates[city] ?: (40.0 to -3.0)

        // Add small random variation to coordinates
        val lat = baseCoords.first + Random.nextDouble(-0.1, 0.1)
        val lon = baseCoords.second + Random.nextDouble(-0.1, 0.1)

        return Location(city, lat, lon)
    }

    private fun generateAmenities(): List<String> {
        val count = Random.nextInt(3, 8) // 3-7 amenities
        return amenities.shuffled().take(count)
    }

    private fun generateRating(): Double {
        // Generate ratings with realistic distribution (more high ratings)
        return when (Random.nextInt(100)) {
            in 0..5 -> Random.nextDouble(1.0, 2.5)      // 5% poor
            in 6..15 -> Random.nextDouble(2.5, 3.5)     // 10% below average
            in 16..35 -> Random.nextDouble(3.5, 4.0)    // 20% average
            in 36..70 -> Random.nextDouble(4.0, 4.5)    // 35% good
            else -> Random.nextDouble(4.5, 5.0)         // 30% excellent
        }.let { Math.round(it * 10) / 10.0 }
    }
}