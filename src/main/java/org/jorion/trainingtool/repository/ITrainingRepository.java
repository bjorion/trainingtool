package org.jorion.trainingtool.repository;

import org.jorion.trainingtool.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository to retrieve the registration templates from the database.
 */
public interface ITrainingRepository extends JpaRepository<Training, Long> {

    String SQL_AVAILABLE_TRAININGS = "SELECT t FROM Training t "
            + " WHERE t.enabled = true "
            + " AND (t.enabledFrom  IS NULL OR :currentDate >= t.enabledFrom)  "
            + " AND (t.enabledUntil IS NULL OR :currentDate <= t.enabledUntil) "
            + " ORDER BY t.startDate ";


    @Query(SQL_AVAILABLE_TRAININGS)
    List<Training> findAvailableTrainings(@Param("currentDate") LocalDate currentDate);
}
