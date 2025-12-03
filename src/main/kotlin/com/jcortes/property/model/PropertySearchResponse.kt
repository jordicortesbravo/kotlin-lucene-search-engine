package com.jcortes.property.model

data class PropertySearchResponse(
    val totalHits: Long,
    val tookMs: Long,
    val items: List<Property>,
    val facets: List<FacetResult>
)
