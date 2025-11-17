package com.jcortes.property.model

enum class PropertyType { HOTEL, APARTMENT, VILLA }

data class Property(
    val id: Long,
    val name: String,
    val type: PropertyType,
    val description: String,
    val location: Location,
    val pricePerNight: Int,
    val amenities: List<String>,
    val maxGuests: Int,
    val bedrooms: Int,
    val rating: Double
)

data class Location(
    val city: String,
    val country: String,
    val latitude: Double?,
    val longitude: Double?
)
