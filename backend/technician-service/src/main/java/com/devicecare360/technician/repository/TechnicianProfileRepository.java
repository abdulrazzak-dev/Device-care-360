package com.devicecare360.technician.repository;

import com.devicecare360.technician.document.TechnicianProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TechnicianProfileRepository extends MongoRepository<TechnicianProfile, String> {
    List<TechnicianProfile> findBySpecializationsContaining(String specialization);
    List<TechnicianProfile> findByServiceAreasContaining(String serviceArea);
    List<TechnicianProfile> findByVerificationStatus(String verificationStatus);
    List<TechnicianProfile> findByAvailable(boolean available);
}
