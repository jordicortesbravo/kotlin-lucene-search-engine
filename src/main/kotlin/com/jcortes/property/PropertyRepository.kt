package com.jcortes.property

import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import com.jcortes.property.model.PropertySearchResponse

interface PropertyRepository {

    fun get(id: Long): Property?
    fun search(params: PropertySearchParams): PropertySearchResponse
}