package com.jcortes.property.model

data class PropertySearchResponse(
    val total: Int,
    val tookMs: Long,
    val items: List<Property>
)
