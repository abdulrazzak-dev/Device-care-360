package com.devicecare360.repairguide.service;

import com.devicecare360.repairguide.document.RepairGuide;
import com.devicecare360.repairguide.repository.RepairGuideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepairGuideService {

    private final RepairGuideRepository repository;

    public List<RepairGuide> getAllGuides(String category, String brand) {
        if (category != null && brand != null) {
            return repository.findByCategoryAndBrand(category, brand);
        } else if (category != null) {
            return repository.findByCategory(category);
        }
        return repository.findAll();
    }

    public RepairGuide getGuideById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repair guide not found for ID: " + id));
    }

    public RepairGuide createGuide(RepairGuide guide) {
        return repository.save(guide);
    }

    public RepairGuide updateGuide(String id, RepairGuide guide) {
        RepairGuide existing = getGuideById(id);
        if (guide.getTitle() != null) existing.setTitle(guide.getTitle());
        if (guide.getCategory() != null) existing.setCategory(guide.getCategory());
        if (guide.getBrand() != null) existing.setBrand(guide.getBrand());
        if (guide.getIssue() != null) existing.setIssue(guide.getIssue());
        if (guide.getRiskLevel() != null) existing.setRiskLevel(guide.getRiskLevel());
        if (guide.getSkillLevel() != null) existing.setSkillLevel(guide.getSkillLevel());
        if (guide.getToolsRequired() != null) existing.setToolsRequired(guide.getToolsRequired());
        if (guide.getSafeInstructions() != null) existing.setSafeInstructions(guide.getSafeInstructions());
        if (guide.getSafetyWarnings() != null) existing.setSafetyWarnings(guide.getSafetyWarnings());
        if (guide.getOfficialDocumentationUrl() != null) existing.setOfficialDocumentationUrl(guide.getOfficialDocumentationUrl());
        if (guide.getMaintenanceTips() != null) existing.setMaintenanceTips(guide.getMaintenanceTips());
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    public void deleteGuide(String id) {
        repository.deleteById(id);
    }
}
