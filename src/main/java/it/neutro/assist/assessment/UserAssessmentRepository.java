package it.neutro.assist.assessment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAssessmentRepository extends JpaRepository<UserAssessment, Long> {

    Optional<UserAssessment> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
