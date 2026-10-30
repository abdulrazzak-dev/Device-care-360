package com.devicecare360.repairguide.service;

import com.devicecare360.repairguide.document.RepairGuide;
import com.devicecare360.repairguide.repository.RepairGuideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepairGuideService {

    private final RepairGuideRepository repository;

    public List<RepairGuide> getAllGuides(String category, String brand) {
        List<RepairGuide> guides;
        boolean hasCategory = category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL");
        boolean hasBrand = brand != null && !brand.trim().isEmpty() && !brand.equalsIgnoreCase("ALL");

        if (hasCategory && hasBrand) {
            guides = repository.findByCategoryIgnoreCaseAndBrandIgnoreCase(category.trim(), brand.trim());
        } else if (hasCategory) {
            guides = repository.findByCategoryIgnoreCase(category.trim());
        } else {
            guides = repository.findAll();
        }

        // Return only approved guides (approved == true, or null for legacy records)
        return guides.stream()
                .filter(g -> g.getApproved() == null || Boolean.TRUE.equals(g.getApproved()))
                .toList();
    }

    public RepairGuide getGuideById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repair guide not found for ID: " + id));
    }

    public RepairGuide createGuide(RepairGuide guide) {
        if (guide.getCreatedAt() == null) guide.setCreatedAt(LocalDateTime.now());
        if (guide.getUpdatedAt() == null) guide.setUpdatedAt(LocalDateTime.now());
        if (guide.getApproved() == null) guide.setApproved(true);
        // Synchronize aliases
        if (guide.getSkillLevel() == null && guide.getDifficulty() != null) guide.setSkillLevel(guide.getDifficulty());
        if (guide.getDifficulty() == null && guide.getSkillLevel() != null) guide.setDifficulty(guide.getSkillLevel());
        if (guide.getIssue() == null && guide.getDescription() != null) guide.setIssue(guide.getDescription());
        if (guide.getDescription() == null && guide.getIssue() != null) guide.setDescription(guide.getIssue());
        if (guide.getSafeInstructions() == null && guide.getSteps() != null) guide.setSafeInstructions(guide.getSteps());
        if (guide.getSteps() == null && guide.getSafeInstructions() != null) guide.setSteps(guide.getSafeInstructions());
        return repository.save(guide);
    }

    public RepairGuide updateGuide(String id, RepairGuide guide) {
        RepairGuide existing = getGuideById(id);
        if (guide.getTitle() != null) existing.setTitle(guide.getTitle());
        if (guide.getCategory() != null) existing.setCategory(guide.getCategory());
        if (guide.getBrand() != null) existing.setBrand(guide.getBrand());
        if (guide.getIssue() != null) existing.setIssue(guide.getIssue());
        if (guide.getDescription() != null) existing.setDescription(guide.getDescription());
        if (guide.getRiskLevel() != null) existing.setRiskLevel(guide.getRiskLevel());
        if (guide.getSkillLevel() != null) existing.setSkillLevel(guide.getSkillLevel());
        if (guide.getDifficulty() != null) existing.setDifficulty(guide.getDifficulty());
        if (guide.getToolsRequired() != null) existing.setToolsRequired(guide.getToolsRequired());
        if (guide.getSafeInstructions() != null) existing.setSafeInstructions(guide.getSafeInstructions());
        if (guide.getSteps() != null) existing.setSteps(guide.getSteps());
        if (guide.getSafetyWarnings() != null) existing.setSafetyWarnings(guide.getSafetyWarnings());
        if (guide.getOfficialDocumentationUrl() != null) existing.setOfficialDocumentationUrl(guide.getOfficialDocumentationUrl());
        if (guide.getMaintenanceTips() != null) existing.setMaintenanceTips(guide.getMaintenanceTips());
        if (guide.getEstimatedTime() != null) existing.setEstimatedTime(guide.getEstimatedTime());
        if (guide.getApproved() != null) existing.setApproved(guide.getApproved());
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    public void deleteGuide(String id) {
        repository.deleteById(id);
    }
}
