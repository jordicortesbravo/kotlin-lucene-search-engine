package com.jcortes.property.infrastructure

import com.jcortes.property.model.Property
import com.jcortes.property.model.PropertySearchParams
import com.jcortes.property.model.PropertySearchResponse
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.ByteBuffersDirectory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class PropertyIndex {

    private val directory = ByteBuffersDirectory()
    private lateinit var searcher: IndexSearcher
    private lateinit var reader: DirectoryReader

    private val cache = ConcurrentHashMap<Long, Property>()

    fun buildIndex(properties: List<Property>) {

        val writer = IndexWriter(
            directory,
            IndexWriterConfig(SimpleAnalyzer()).apply {
                openMode = IndexWriterConfig.OpenMode.CREATE
                ramBufferSizeMB = 512.0
                useCompoundFile = false
            }
        )

        // Bulk ingest
        properties.parallelStream().forEach { property ->
            cache[property.id] = property
            writer.addDocument(property.toDocument())
        }

        writer.commit()
        writer.close()

        // Open read-only searcher
        reader = DirectoryReader.open(directory)
        searcher = IndexSearcher(reader)
    }

    fun get(id: Long): Property =
        cache[id] ?: error("Not found")

    fun query(params: PropertySearchParams): PropertySearchResponse {
        val query = params.toLuceneQuery()
        val topDocs = searcher.search(query, params.limit)

        val results = topDocs.scoreDocs.mapNotNull {
            val doc = searcher.doc(it.doc)
            cache[doc.get("id").toLong()]
        }

        return PropertySearchResponse(results)
    }
}



