package com.jcortes.property.entrypoint

import com.jcortes.property.PropertyService
import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import com.jcortes.property.model.PropertySearchResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/properties")
class PropertyController(
    private val propertyService: PropertyService
) {

    @GetMapping("/{id}")
    fun getProperty(@PathVariable id: Long): ResponseEntity<Property> {
        val property = propertyService.get(id)
        return if (property != null) {
            ResponseEntity.ok(property)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/search")
    fun searchProperties(@RequestBody params: PropertySearchParams): PropertySearchResponse {
        return propertyService.search(params)
    }

}