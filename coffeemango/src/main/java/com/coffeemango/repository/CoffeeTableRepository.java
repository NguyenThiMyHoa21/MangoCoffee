package com.coffeemango.repository;

import com.coffeemango.model.CoffeeTable;
import com.coffeemango.model.CoffeeTable.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoffeeTableRepository extends JpaRepository<CoffeeTable, Long> {
    
    List<CoffeeTable> findByLocation(String location);
    
    List<CoffeeTable> findByStatus(TableStatus status);
    
    long countByStatus(TableStatus status);
    
    List<CoffeeTable> findByLocationOrderByTableNumberAsc(String location);
}
