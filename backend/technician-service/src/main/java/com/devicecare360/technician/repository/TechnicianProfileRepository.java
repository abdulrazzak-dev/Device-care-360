package com.devicecare360.technician.repository;

import com.devicecare360.technician.document.TechnicianProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface TechnicianProfileRepository extends MongoRepository<TechnicianProfile, String> {
    List<TechnicianProfile> findBySpecializationsContaining(String specialization);
    List<TechnicianProfile> findBySpecializationsContainingIgnoreCase(String specialization);
    List<TechnicianProfile> findByServiceAreasContaining(String serviceArea);
    List<TechnicianProfile> findByServiceAreasContainingIgnoreCase(String serviceArea);
    List<TechnicianProfile> findByVerificationStatus(String verificationStatus);
    List<TechnicianProfile> findByVerificationStatusIn(Collection<String> statuses);
    List<TechnicianProfile> findByAvailable(boolean available);
    List<TechnicianProfile> findByVerificationStatusInAndAvailable(Collection<String> statuses, boolean available);
}
