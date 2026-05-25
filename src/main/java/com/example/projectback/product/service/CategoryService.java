package com.example.projectback.product.service;

import com.example.projectback.product.dto.CategoryResponse;
import com.example.projectback.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(){
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(
                        c.getId(),
                        c.getName(),
                        c.getParent() != null ? c.getParent().getId() : null
                )).collect(Collectors.toList());
    }
}
