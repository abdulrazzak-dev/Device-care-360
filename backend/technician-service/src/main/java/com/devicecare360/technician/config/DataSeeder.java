package com.devicecare360.technician.config;

import com.devicecare360.shared.model.DeviceSpecialization;
import com.devicecare360.technician.document.TechnicianProfile;
import com.devicecare360.technician.repository.TechnicianProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Automatically seeds initial technician directory records on application startup
 * if the MongoDB technician_profiles collection is empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TechnicianProfileRepository technicianRepository;

    @Override
    public void run(String... args) throws Exception {
        long existingCount = technicianRepository.count();
        if (existingCount > 0) {
            log.info("Technician records already exist (count={}); skipping seeding to preserve existing data.", existingCount);
            return;
        }

        log.info("Technician collection is empty. Seeding initial verified technician profiles...");

        List<TechnicianProfile> sampleTechnicians = List.of(
                TechnicianProfile.builder()
                        .id("tech-001")
                        .fullName("Kasun Perera")
                        .email("kasun.perera.demo@devicecare360.com")
                        .phone("+94 77 123 4567")
                        .specializations(List.of(DeviceSpecialization.SMARTPHONE_MOBILE.name(), "Smartphone Repair", "Smartphone", "Screen Replacement"))
                        .serviceAreas(List.of("Colombo", "Western Province"))
                        .city("Colombo")
                        .serviceArea("Western Province")
                        .professionalBio("Certified mobile phone hardware specialist with 7 years of field service experience.")
                        .available(true)
                        .verificationStatus("VERIFIED")
                        .averageRating(4.9)
                        .totalReviews(128)
                        .hourlyRate(2500.0)
                        .yearsOfExperience(7)
                        .profileImageUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=400")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),

                TechnicianProfile.builder()
                        .id("tech-002")
                        .fullName("Mohamed Rizwan")
                        .email("mohamed.rizwan.demo@devicecare360.com")
                        .phone("+94 71 987 6543")
                        .specializations(List.of(DeviceSpecialization.LAPTOP_DESKTOP_PC.name(), "Laptop/PC Hardware", "Laptop", "Motherboard Repair"))
                        .serviceAreas(List.of("Negombo", "Western Province"))
                        .city("Negombo")
                        .serviceArea("Western Province")
                        .professionalBio("Expert computer engineer specializing in motherboard diagnostics and data recovery.")
                        .available(true)
                        .verificationStatus("VERIFIED")
                        .averageRating(4.8)
                        .totalReviews(94)
                        .hourlyRate(3000.0)
                        .yearsOfExperience(9)
                        .profileImageUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=400")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),

                TechnicianProfile.builder()
                        .id("tech-003")
                        .fullName("Nimal Fernando")
                        .email("nimal.fernando.demo@devicecare360.com")
                        .phone("+94 76 555 1234")
                        .specializations(List.of(DeviceSpecialization.SMART_TV_AUDIO.name(), "Audio/Video Gear", "Television", "Display Panels"))
                        .serviceAreas(List.of("Gampaha", "Western Province"))
                        .city("Gampaha")
                        .serviceArea("Western Province")
                        .professionalBio("Specialist in smart TV panel repairs and integrated sound systems.")
                        .available(true)
                        .verificationStatus("VERIFIED")
                        .averageRating(4.7)
                        .totalReviews(82)
                        .hourlyRate(2000.0)
                        .yearsOfExperience(6)
                        .profileImageUrl("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=400")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),

                TechnicianProfile.builder()
                        .id("tech-004")
                        .fullName("Sahan Jayawardena")
                        .email("sahan.jayawardena.demo@devicecare360.com")
                        .phone("+94 75 333 9876")
                        .specializations(List.of(DeviceSpecialization.REFRIGERATOR.name(), "Smart Home Appliances", "Refrigerator", "Power Systems"))
                        .serviceAreas(List.of("Kandy", "Central Province"))
                        .city("Kandy")
                        .serviceArea("Central Province")
                        .professionalBio("Master cooling technician handling inverter and compressor repairs.")
                        .available(true)
                        .verificationStatus("VERIFIED")
                        .averageRating(4.9)
                        .totalReviews(110)
                        .hourlyRate(2800.0)
                        .yearsOfExperience(8)
                        .profileImageUrl("https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=400")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );

        technicianRepository.saveAll(sampleTechnicians);
        log.info("Successfully seeded {} verified technician profiles into MongoDB.", sampleTechnicians.size());
    }
}
