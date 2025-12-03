package com.jcortes.property.model

data class PropertySearchParams(
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val minGuests: Int? = null,
    val minBedrooms: Int? = null,
    val minRating: Double? = null,

    val amenities: List<String>? = null,
    val cities: List<String>? = null,
    val propertyTypes: List<String>? = null,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusKm: Double? = null,

    val limit: Int = 20,

    // Faceted search support
    val facets: List<String>? = null  // e.g., ["city", "type", "amenities", "priceRange"]
)