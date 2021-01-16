package org.jorion.trainingtool.repositories;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.types.RegistrationStatus;

/**
 * Repository to retrieve the registrations from the database.
 */
@Transactional(readOnly = true)
public interface IRegistrationRepository extends JpaRepository<Registration, Long>
{
    // @formatter:off
    String SQL_REG_BY_STATUS = "SELECT r FROM Registration r "
            + "WHERE r.status IN :statuses "
            + "ORDER BY r.id DESC ";
    
    String SQL_REG_BY_STATUS_AND_MANAGER = "SELECT r FROM Registration r LEFT JOIN User u ON r.member = u.id "
            + "WHERE (r.status IN :statuses) "
            + "AND (:managername IS NULL OR u.managername = :managername) "
            + "ORDER BY r.id DESC ";
    
    String SQL_REG_BY_EXAMPLE_AND_MANAGER = "SELECT r FROM Registration r LEFT JOIN User u ON r.member = u.id "
            + "WHERE (:status IS null OR r.status = :status) "
            + "AND (lower(u.lastname) LIKE :lastname) "
            + "AND (:startDate IS NULL OR (r.startDate >= :startDate)) "
            + "AND (:managername IS NULL OR u.managername = :managername OR u.username = :username) "
            + "ORDER BY r.id DESC ";
    // @formatter:on

    @Query("SELECT r FROM Registration r LEFT JOIN User u ON r.member = u.id WHERE r.id = :id AND u.username = :username")
    Optional<Registration> findRegistrationByIdAndUsername(Long id, String username);

    @Query(SQL_REG_BY_STATUS)
    List<Registration> findRegistrationsByStatuses(EnumSet<RegistrationStatus> statuses);

    @Query(SQL_REG_BY_STATUS_AND_MANAGER)
    List<Registration> findRegistrationsByStatusesAndManager(EnumSet<RegistrationStatus> statuses, String managername);

    @Query(SQL_REG_BY_EXAMPLE_AND_MANAGER)
    List<Registration> findAllByExample(String lastname, LocalDate startDate, RegistrationStatus status, String username, String managername);
    
    @Query("SELECT r FROM Registration r LEFT JOIN User u ON r.member = u.id WHERE (:managername IS NULL OR u.managername = :managername OR u.username = :username)")
    Page<Registration> findByManager(String username, String managername, Pageable pageable);
}
