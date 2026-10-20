package com.coffeemango.repository;

import com.coffeemango.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    boolean existsByName(String name);

    // Lấy danh sách nguyên liệu sắp hết (quantity <= minQuantity)
    @Query("SELECT i FROM Ingredient i WHERE i.quantity <= i.minQuantity")
    List<Ingredient> findLowStockIngredients();
}
