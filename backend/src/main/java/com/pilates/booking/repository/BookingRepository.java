package com.pilates.booking.repository;

import com.pilates.booking.domain.models.Booking;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByPilatesClassId(Long classId);

    boolean existsByPilatesClassIdAndEmailIgnoreCase(Long classId, String email);

    List<Booking> findByPilatesClassIdOrderByCreatedAtAsc(Long classId);

    void deleteByPilatesClassId(Long classId);

    @Query("select b from Booking b join fetch b.pilatesClass where b.id = :id and b.pilatesClass.id = :classId")
    Optional<Booking> findWithClass(Long id, Long classId);

    interface ClassCount { Long getClassId(); long getCnt(); }

    @Query("select b.pilatesClass.id as classId, count(b) as cnt from Booking b where b.pilatesClass.id in :ids group by b.pilatesClass.id")
    List<ClassCount> countForClasses(Collection<Long> ids);
}
