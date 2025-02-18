package com.example.elasticsearchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.elasticsearchservice.client.CategoryClient;
import com.example.elasticsearchservice.dto.CategoryDTO;
import com.example.elasticsearchservice.entity.CategoryEntity;
import com.example.elasticsearchservice.repository.CategoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryClient categoryClient;
    private final CategoryRepo categoryRepo;
//    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearch;


    public List<CategoryEntity> searchCategory(String searchText) throws IOException {
        Query matchQuery = MatchQuery.of(m -> m
                .field("categoryName")
                .query(searchText)
        )._toQuery();

        SearchResponse<CategoryEntity> response = elasticsearch.search(s -> s
                        .index("categories")
                        .query(matchQuery),
                CategoryEntity.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public Iterable<CategoryEntity> findAll() {
        List<CategoryDTO> categoryDTOs = categoryClient.getAllCategories();

        List<CategoryEntity> categoryEntities = new ArrayList<>();
        for (CategoryDTO categoryDTO : categoryDTOs) {
            categoryEntities.add(convertToEntity(categoryDTO));
        }

        return categoryEntities;
    }


    public CategoryEntity insertCategory(CategoryEntity categoryEntity) {
        return categoryRepo.save(categoryEntity);
    }

    public CategoryEntity updateCategory(CategoryEntity categoryEntity, Long categoryId) {
        CategoryEntity category = categoryRepo.findById(categoryId).orElse(null);
        if (category != null) {
            category.setCategoryName(categoryEntity.getCategoryName());
            category.setCategoryDescription(categoryEntity.getCategoryDescription());
            return categoryRepo.save(category);
        }
        return null;
    }

    public void deleteCategory(Long categoryId) {
        categoryRepo.deleteById(categoryId);
    }

    public CategoryEntity convertToEntity(CategoryDTO categoryDTO) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setCategoryId(categoryDTO.getCategoryId());
        categoryEntity.setCategoryName(categoryDTO.getCategoryName());
        categoryEntity.setCategoryDescription(categoryDTO.getCategoryDescription());
        return categoryEntity;
    }


    public CategoryDTO fallbackMethod(Long categoryId, Throwable throwable) {
        return new CategoryDTO(categoryId, "Unknown", "Unknown");
    }

}

















//    private static final String CATEGORYENTITY = "categories";
//
//
//    public String createOrUpdateDocument(CategoryEntity categoryEntity) throws IOException {
//        IndexResponse response = elasticsearch.index(i -> i
//                .index(CATEGORYENTITY)
//                .id(String.valueOf(categoryEntity.getCategoryId()))
//                .document(categoryEntity));
//
//        Map<String, String> responseMessages = Map.of(
//                "Created", "Document has been created",
//                "Updated", "Document has been updated"
//        );
//
//        return responseMessages.getOrDefault(response.result().name(), "Error has occurred");
//
//    }
//
//    public CategoryEntity findDocById(String categoryId) throws IOException {
//        GetResponse<CategoryEntity> response = elasticsearch.get(g -> g.index(CATEGORYENTITY).id(categoryId), CategoryEntity.class);
//
//        if (response.found()) {
//            return response.source(); // Doküman bulunduysa döndür
//        } else {
//            throw new IOException("Document not found with id: " + categoryId); // Eğer bulamazsa hata fırlat
//        }
//    }
//
//    // ID'ye göre doküman sil
//    public String deleteDocById(String categoryId) throws IOException {
//        DeleteRequest deleteRequest = DeleteRequest.of(d -> d.index(CATEGORYENTITY).id(categoryId));
//        DeleteResponse response = elasticsearch.delete(deleteRequest);
//
//        // Silme sonucu mesajı
//        if (response.result().name().equalsIgnoreCase("NOT_FOUND")) {
//            return "Document not found with id: " + categoryId;
//        } else {
//            return "Document has been deleted";
//        }
//    }
//
//    // Tüm kategorileri listele
//    public List<CategoryEntity> findAll() throws IOException {
//        SearchRequest request = SearchRequest.of(s -> s.index(CATEGORYENTITY));
//        SearchResponse<CategoryEntity> response = elasticsearch.search(request, CategoryEntity.class);
//
//        List<CategoryEntity> categoryEntities = new ArrayList<>();
//        response.hits().hits().forEach(hit -> categoryEntities.add(hit.source()));
//        return categoryEntities;
//    }
//
//    // Birden fazla kategori kaydını topluca kaydet
//    public String bulkSave(List<CategoryEntity> categoryEntities) throws IOException {
//        BulkRequest.Builder br = new BulkRequest.Builder();
//        categoryEntities.forEach(product -> br.operations(operation ->
//                operation.index(i -> i
//                        .index(CATEGORYENTITY)
//                        .id(String.valueOf(product.getCategoryId()))
//                        .document(product))));
//
//        BulkResponse response = elasticsearch.bulk(br.build());
//        if (response.errors()) {
//            // Hata varsa detaylı mesaj döndür
//            return "Bulk operation has errors: " + response.items().toString();
//        } else {
//            return "Bulk save success";
//        }



