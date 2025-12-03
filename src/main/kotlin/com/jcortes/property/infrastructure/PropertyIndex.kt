package com.jcortes.property.infrastructure

import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import com.jcortes.property.model.PropertySearchResponse
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.document.*
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
    private val cache = ConcurrentHashMap<Long, Property>()

    fun buildIndex(properties: List<Property>) {
        val directory = ByteBuffersDirectory()

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
            writer.addDocument(property.toDocument())
        }

        writer.commit()
        writer.close()

        // Open read-only searcher (for this showcase we keep it simple)
        searcher = IndexSearcher(DirectoryReader.open(directory))
    }

    fun get(id: Long): Property = cache[id] ?: error("Not found")

    fun query(params: PropertySearchParams): PropertySearchResponse {
        val query = params.toLuceneQuery()

        val results: List<Property>
        val totalHits: Long
        val elapsedTime = measureTimeMillis {
            val topDocs = searcher.search(query, params.limit)
            totalHits = topDocs.totalHits.value
            results = topDocs.scoreDocs.mapNotNull {
                val doc = searcher.storedFields().document(it.doc)
                cache[doc["id"].toLong()]
            }
        }

        return PropertySearchResponse(totalHits, elapsedTime, results)
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
    }
}