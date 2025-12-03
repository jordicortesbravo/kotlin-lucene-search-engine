package com.jcortes.property.model

data class FacetResult(
    val name: String,
    val buckets: List<FacetBucket>
)

data class FacetBucket(
    val value: String,
    val count: Long
)