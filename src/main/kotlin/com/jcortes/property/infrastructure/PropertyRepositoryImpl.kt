package com.jcortes.property.infrastructure

import com.jcortes.property.PropertyRepository
import org.springframework.stereotype.Repository

@Repository
class PropertyRepositoryImpl(
    private val propertyIndex: PropertyIndex
): PropertyRepository {

}