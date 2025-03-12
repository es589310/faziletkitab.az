package com.example.elasticsearchservice.service;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.example.elasticsearchservice.document.AuthorDocument;
import com.example.elasticsearchservice.document.CatalogDocument;
import com.example.elasticsearchservice.document.CategoryDocument;
import com.example.elasticsearchservice.dto.SearchResponse;
import com.example.elasticsearchservice.dto.SimpleSearchResult;
import com.example.elasticsearchservice.dto.response.CategoryResponseDto;
import com.example.elasticsearchservice.dto.request.AuthorRequestDto;
import com.example.elasticsearchservice.dto.request.CatalogRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private final ElasticsearchOperations elasticsearchOperations;

    public SearchResponse search(String query, int page, int size) {
        Query boolQuery = QueryBuilders.bool(b -> b
            .should(QueryBuilders.match(m -> m.field("title").query(query)))
            .should(QueryBuilders.match(m -> m.field("name").query(query)))
            .should(QueryBuilders.match(m -> m.field("categoryName").query(query)))
        );

        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(boolQuery)
            .withPageable(PageRequest.of(page, size))
            .build();

        SearchHits<?> searchHits = elasticsearchOperations.search(searchQuery, Object.class);
        
        List<SimpleSearchResult> results = searchHits.stream()
            .map(hit -> mapToSimpleSearchResult(hit.getContent()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return SearchResponse.builder()
            .results(results)
            .totalHits(searchHits.getTotalHits())
            .page(page)
            .size(size)
            .build();
    }

    private SimpleSearchResult mapToSimpleSearchResult(Object content) {
        if (content instanceof CatalogDocument doc) {
            return new SimpleSearchResult("CATALOG", doc.getCatalogId(), doc.getTitle());
        } else if (content instanceof AuthorDocument doc) {
            return new SimpleSearchResult("AUTHOR", doc.getAuthorId(), doc.getName());
        } else if (content instanceof CategoryDocument doc) {
            return new SimpleSearchResult("CATEGORY", doc.getCategoryId(), doc.getCategoryName());
        }
        return null;
    }

    public List<CategoryResponseDto> searchCategories(String categoryName) {
        Query matchQuery = QueryBuilders.match(m -> m
            .field("categoryName")
            .query(categoryName)
        );

        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(matchQuery)
            .build();
        
        SearchHits<CategoryDocument> searchHits = elasticsearchOperations.search(
            searchQuery, 
            CategoryDocument.class
        );
        
        return searchHits.stream()
            .map(hit -> new CategoryResponseDto(
                hit.getContent().getCategoryId(),
                hit.getContent().getCategoryName()
            ))
            .collect(Collectors.toList());
    }

    public List<CatalogDocument> searchWithCatalogRequest(CatalogRequestDto request) {
        return searchByField("title", request.getTitle(), CatalogDocument.class);
    }

    public List<AuthorDocument> searchWithAuthorRequest(AuthorRequestDto request) {
        return searchByField("name", request.getName(), AuthorDocument.class);
    }

    private <T> List<T> searchByField(String field, String value, Class<T> entityClass) {
        Query matchQuery = QueryBuilders.match(m -> m
            .field(field)
            .query(value)
        );

        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(matchQuery)
            .build();
        
        SearchHits<T> searchHits = elasticsearchOperations.search(searchQuery, entityClass);
        
        return searchHits.stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }

    public SearchResult searchAll(String query) {
        Query boolQuery = QueryBuilders.bool(b -> b
            .should(QueryBuilders.match(m -> m.field("title").query(query)))
            .should(QueryBuilders.match(m -> m.field("name").query(query)))
            .should(QueryBuilders.match(m -> m.field("categoryName").query(query)))
        );

        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(boolQuery)
            .build();

        SearchHits<Object> searchHits = elasticsearchOperations.search(searchQuery, Object.class);
        
        return new SearchResult(
                extractDocuments(searchHits, CatalogDocument.class).toString(),
            extractDocuments(searchHits, AuthorDocument.class),
                (Attributes) extractDocuments(searchHits, CategoryDocument.class)
        );
    }

    public List<CategoryDocument> searchCategoriesFuzzy(String query) {
        Query fuzzyQuery = QueryBuilders.fuzzy(f -> f
            .field("categoryName")
            .value(query)
            .fuzziness("AUTO")
        );

        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(fuzzyQuery)
            .build();

        return elasticsearchOperations.search(searchQuery, CategoryDocument.class)
            .stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }

    public List<CatalogDocument> searchCatalogsByCategory(String categoryId, String query) {
        Query boolQuery = QueryBuilders.bool(b -> b
            .must(QueryBuilders.match(m -> m.field("categoryId").query(categoryId)))
            .must(QueryBuilders.match(m -> m.field("title").query(query)))
        );

        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(boolQuery)
            .build();

        return elasticsearchOperations.search(searchQuery, CatalogDocument.class)
            .stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }

    public List<AuthorDocument> searchAuthorsFuzzy(String query) {
        Query fuzzyQuery = QueryBuilders.fuzzy(f -> f
            .field("name")
            .value(query)
            .fuzziness("AUTO")
        );

        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(fuzzyQuery)
            .build();

        return elasticsearchOperations.search(searchQuery, AuthorDocument.class)
            .stream()
            .map(SearchHit::getContent)
            .collect(Collectors.toList());
    }

    private <T> List<T> extractDocuments(SearchHits<?> searchHits, Class<T> type) {
        return searchHits.stream()
            .map(SearchHit::getContent)
            .filter(type::isInstance)
            .map(type::cast)
            .collect(Collectors.toList());
    }
} 