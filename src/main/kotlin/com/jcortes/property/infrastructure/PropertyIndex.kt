package com.jcortes.property.infrastructure

import com.jcortes.property.model.FacetBucket
import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import com.jcortes.property.model.PropertySearchResponse
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.document.*
import org.apache.lucene.facet.*
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts
import org.apache.lucene.facet.taxonomy.TaxonomyReader
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.search.*
import org.apache.lucene.search.BooleanClause.Occur.MUST
import org.apache.lucene.store.ByteBuffersDirectory
import org.apache.lucene.util.BytesRef
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

@Component
class PropertyIndex {

    private lateinit var searcher: IndexSearcher
    private lateinit var taxonomyReader: TaxonomyReader
    private lateinit var facetsConfig: FacetsConfig
    private val cache = ConcurrentHashMap<Long, Property>()

    fun buildIndex(properties: List<Property>) {
        val directory = ByteBuffersDirectory()
        val taxonomyDirectory = ByteBuffersDirectory()

        // Configure facets
        facetsConfig = FacetsConfig().apply {
            setMultiValued("amenities", true)
            setHierarchical("priceRange", true)
        }

        val taxonomyWriter = DirectoryTaxonomyWriter(taxonomyDirectory)
        val writer = IndexWriter(
            directory,
            IndexWriterConfig(SimpleAnalyzer()).apply {
                openMode = IndexWriterConfig.OpenMode.CREATE
                ramBufferSizeMB = 1024.0
                useCompoundFile = false
            }
        )

        properties.parallelStream().forEach { property ->
            cache[property.id] = property
            val doc = property.toDocument()
            writer.addDocument(facetsConfig.build(taxonomyWriter, doc))
        }

        writer.commit()
        writer.close()
        taxonomyWriter.commit()
        taxonomyWriter.close()

        // Open read-only searcher and taxonomy reader
        searcher = IndexSearcher(DirectoryReader.open(directory))
        taxonomyReader = DirectoryTaxonomyReader(taxonomyDirectory)
    }

    fun get(id: Long): Property = cache[id] ?: error("Not found")

    fun query(params: PropertySearchParams): PropertySearchResponse {
        val query = params.toLuceneQuery()

        val results: List<Property>
        val totalHits: Long
        val facetResults: List<com.jcortes.property.model.FacetResult>

        val elapsedTime = measureTimeMillis {
            val topDocs = searcher.search(query, params.limit)
            totalHits = topDocs.totalHits.value
            results = topDocs.scoreDocs.mapNotNull {
                val doc = searcher.storedFields().document(it.doc)
                cache[doc["id"].toLong()]
            }

            // Compute facets if requested
            facetResults = if (params.facets.isNullOrEmpty()) {
                emptyList()
            } else {
                computeFacets(query, params.facets)
            }
        }

        return PropertySearchResponse(totalHits, elapsedTime, results, facetResults)
    }

    private fun computeFacets(query: Query, requestedFacets: List<String>): List<com.jcortes.property.model.FacetResult> {
        val facetsCollector = FacetsCollector()
        searcher.search(query, facetsCollector)

        val facets = FastTaxonomyFacetCounts(taxonomyReader, facetsConfig, facetsCollector)

        return requestedFacets.mapNotNull { facetName ->
            when (facetName) {
                "city" -> facets.getTopChildren(20, "city")?.let { luceneFacetResult ->
                    com.jcortes.property.model.FacetResult("city", luceneFacetResult.labelValues.map { lv ->
                        FacetBucket(lv.label, lv.value.toLong())
                    })
                }
                "type" -> facets.getTopChildren(10, "type")?.let { luceneFacetResult ->
                    com.jcortes.property.model.FacetResult("type", luceneFacetResult.labelValues.map { lv ->
                        FacetBucket(lv.label, lv.value.toLong())
                    })
                }
                "amenities" -> facets.getTopChildren(50, "amenities")?.let { luceneFacetResult ->
                    com.jcortes.property.model.FacetResult("amenities", luceneFacetResult.labelValues.map { lv ->
                        FacetBucket(lv.label, lv.value.toLong())
                    })
                }
                "priceRange" -> facets.getTopChildren(10, "priceRange")?.let { luceneFacetResult ->
                    com.jcortes.property.model.FacetResult("priceRange", luceneFacetResult.labelValues.map { lv ->
                        FacetBucket(lv.label, lv.value.toLong())
                    })
                }
                else -> null
            }
        }
    }

    private fun PropertySearchParams.toLuceneQuery(): Query {
        val booleanQuery = BooleanQuery.Builder()

        if (minPrice != null || maxPrice != null) {
            val min = minPrice ?: 0
            val max = maxPrice ?: Integer.MAX_VALUE
            booleanQuery.add(IntPoint.newRangeQuery("pricePerNight", min, max), MUST)
        }

        minGuests?.let { guests -> booleanQuery.add(IntPoint.newRangeQuery("maxGuests", guests, Integer.MAX_VALUE), MUST) }
        minBedrooms?.let { beds -> booleanQuery.add(IntPoint.newRangeQuery("bedrooms", beds, Integer.MAX_VALUE), MUST) }
        minRating?.let { rating -> booleanQuery.add(DoublePoint.newRangeQuery("rating", rating, Double.MAX_VALUE), MUST) }
        amenities?.forEach { amenity -> booleanQuery.add(TermQuery(Term("amenities", amenity)), MUST) }

        cities?.takeIf { it.isNotEmpty() }?.let { cityList ->
            val cityQuery = BooleanQuery.Builder()
            cityList.forEach { city ->
                cityQuery.add(TermQuery(Term("city", city)), BooleanClause.Occur.SHOULD)
            }
            booleanQuery.add(cityQuery.build(), MUST)
        }

        propertyTypes?.takeIf { it.isNotEmpty() }?.let { types ->
            val typeQuery = BooleanQuery.Builder()
            types.forEach { type ->
                typeQuery.add(TermQuery(Term("type", type)), BooleanClause.Occur.SHOULD)
            }
            booleanQuery.add(typeQuery.build(), MUST)
        }

        if (latitude != null && longitude != null && radiusKm != null) {
            val radiusMeters = radiusKm * 1000
            booleanQuery.add(LatLonPoint.newDistanceQuery("coordinates", latitude, longitude, radiusMeters), MUST)
        }

        return booleanQuery.build().takeIf { it.clauses().isNotEmpty() } ?: MatchAllDocsQuery()
    }

    private fun Property.toDocument() = Document().apply {
        add(StringField("id", id.toString(), Field.Store.YES)) // Stored to retrieve later
        add(TextField("name", name, Field.Store.NO))
        add(TextField("type", type.name, Field.Store.NO))
        add(IntPoint("pricePerNight", pricePerNight))
        add(IntPoint("maxGuests", maxGuests))
        add(IntPoint("bedrooms", bedrooms))
        amenities.forEach { amenity ->
            add(StringField("amenities", amenity, Field.Store.NO))
            add(SortedSetDocValuesField("amenities", BytesRef(amenity)))
        }
        add(DoublePoint("rating", rating))
        add(StringField("city", location.city, Field.Store.NO))
        add(SortedSetDocValuesField("city", BytesRef(location.city)))
        if (location.latitude != null && location.longitude != null) {
            add(LatLonPoint("coordinates", location.latitude, location.longitude))
        }

        // Add facet fields
        add(FacetField("city", location.city))
        add(FacetField("type", type.name))

        // Multi-valued amenities facet
        amenities.forEach { amenity ->
            add(FacetField("amenities", amenity))
        }

        // Price range facet (hierarchical)
        val priceRange = when (pricePerNight) {
            in 0..99 -> "Budget"
            in 100..199 -> "Standard"
            in 200..299 -> "Premium"
            in 300..499 -> "Luxury"
            else -> "Ultra-Luxury"
        }
        add(FacetField("priceRange", priceRange, pricePerNight.toString()))
    }
}