package com.devicecare360.repairguide.repository;

import com.devicecare360.repairguide.document.RepairGuide;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RepairGuideRepository extends MongoRepository<RepairGuide, String> {
    List<RepairGuide> findByCategory(String category);
    List<RepairGuide> findByCategoryAndBrand(String category, String brand);
}
