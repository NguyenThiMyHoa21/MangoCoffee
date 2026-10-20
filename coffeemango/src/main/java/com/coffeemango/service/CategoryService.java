package com.coffeemango.service;

import com.coffeemango.model.Category;
import com.coffeemango.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findAllActive() {
        return categoryRepository.findByActive(true);
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category save(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Danh mục '" + category.getName() + "' đã tồn tại");
        }
        return categoryRepository.save(category);
    }

    public Category update(Long id, Category updatedCategory) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        existing.setName(updatedCategory.getName());
        existing.setDescription(updatedCategory.getDescription());
        existing.setActive(updatedCategory.isActive());
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        category.setActive(false);
        categoryRepository.save(category);
    }
}
