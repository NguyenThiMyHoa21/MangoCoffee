package com.coffeemango.service;

import com.coffeemango.model.CoffeeTable;
import com.coffeemango.model.CoffeeTable.TableStatus;
import com.coffeemango.repository.CoffeeTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CoffeeTableService {
    
    @Autowired
    private CoffeeTableRepository tableRepository;
    
    public List<CoffeeTable> findAll() {
        return tableRepository.findAll();
    }
    
    public CoffeeTable findById(Long id) {
        return tableRepository.findById(id).orElse(null);
    }
    
    public List<CoffeeTable> findByLocation(String location) {
        return tableRepository.findByLocationOrderByTableNumberAsc(location);
    }
    
    public Map<String, List<CoffeeTable>> getTablesByLocation() {
        Map<String, List<CoffeeTable>> map = new LinkedHashMap<>();
        map.put("Trong nhà", findByLocation("Trong nhà"));
        map.put("Ngoài sân", findByLocation("Ngoài sân"));
        return map;
    }
    
    @Transactional
    public CoffeeTable save(CoffeeTable table) {
        return tableRepository.save(table);
    }
    
    @Transactional
    public void delete(Long id) {
        tableRepository.deleteById(id);
    }
    
    @Transactional
    public void updateStatus(Long id, TableStatus status) {
        CoffeeTable table = findById(id);
        if (table != null) {
            table.setStatus(status);
            tableRepository.save(table);
        }
    }

    @Transactional
    public void setStatus(Long id, TableStatus status) {
        updateStatus(id, status);
    }
    
    public Map<String, Long> getTableStatusCount() {
        Map<String, Long> statusCount = new HashMap<>();
        statusCount.put("available", tableRepository.countByStatus(TableStatus.AVAILABLE));
        statusCount.put("occupied", tableRepository.countByStatus(TableStatus.OCCUPIED));
        statusCount.put("reserved", tableRepository.countByStatus(TableStatus.RESERVED));
        return statusCount;
    }
    
    public List<CoffeeTable> getAvailableTables() {
        return tableRepository.findByStatus(TableStatus.AVAILABLE);
    }
    
    public long countByStatus(TableStatus status) {
        return tableRepository.countByStatus(status);
    }
}
