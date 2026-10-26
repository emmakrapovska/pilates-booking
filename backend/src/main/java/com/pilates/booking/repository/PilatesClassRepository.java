package com.pilates.booking.repository;

import com.pilates.booking.domain.models.PilatesClass;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PilatesClassRepository extends JpaRepository<PilatesClass, Long> {

    List<PilatesClass> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime after);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from PilatesClass c where c.id = :id")
    Optional<PilatesClass> findByIdForUpdate(Long id);
}
