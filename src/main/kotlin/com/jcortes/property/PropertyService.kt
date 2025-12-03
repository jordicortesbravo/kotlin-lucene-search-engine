package com.jcortes.property

import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import com.jcortes.property.model.PropertySearchResponse
import org.springframework.stereotype.Service

@Service
class PropertyService(
    private val propertyRepository: PropertyRepository
) {

    fun get(id: Long): Property? {
        return propertyRepository.get(id)
    }

    fun search(params: PropertySearchParams): PropertySearchResponse {
        return propertyRepository.search(params)
    }

}