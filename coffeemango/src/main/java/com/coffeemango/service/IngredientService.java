package com.coffeemango.service;

import com.coffeemango.model.Ingredient;
import com.coffeemango.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public List<Ingredient> findAll() {
        return ingredientRepository.findAll();
    }

    public Optional<Ingredient> findById(Long id) {
        return ingredientRepository.findById(id);
    }

    public List<Ingredient> findLowStock() {
        return ingredientRepository.findLowStockIngredients();
    }

    public Ingredient save(Ingredient ingredient) {
        if (ingredientRepository.existsByName(ingredient.getName())) {
            throw new RuntimeException("Nguyên liệu '" + ingredient.getName() + "' đã tồn tại");
        }
        return ingredientRepository.save(ingredient);
    }

    public Ingredient update(Long id, Ingredient updatedIngredient) {
        Ingredient existing = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu với ID: " + id));
        existing.setName(updatedIngredient.getName());
        existing.setQuantity(updatedIngredient.getQuantity());
        existing.setUnit(updatedIngredient.getUnit());
        existing.setMinQuantity(updatedIngredient.getMinQuantity());
        existing.setCostPerUnit(updatedIngredient.getCostPerUnit());
        return ingredientRepository.save(existing);
    }

    public void delete(Long id) {
        ingredientRepository.deleteById(id);
    }
}
