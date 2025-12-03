package com.jcortes.property.infrastructure

import com.jcortes.property.PropertyRepository
import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import com.jcortes.property.model.PropertySearchResponse
import org.springframework.stereotype.Repository

@Repository
class PropertyRepositoryImpl(
    private val propertyIndex: PropertyIndex
): PropertyRepository {


    override fun get(id: Long): Property? {
        return try {
            propertyIndex.get(id)
        } catch (_: IllegalStateException) {
            // PropertyIndex.get() throws an error when not found
            null
        }
    }

    override fun search(params: PropertySearchParams): PropertySearchResponse {
        return propertyIndex.query(params)
    }


}