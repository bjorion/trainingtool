package org.jorion.trainingtool.registration;

import org.jorion.trainingtool.type.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Repository to retrieve the registrations from the database.
 */
public interface IRegistrationRepository extends JpaRepository<Registration, Long> {

    String SQL_REG_BY_ID_AND_USERNAME = """
            SELECT r FROM Registration r LEFT JOIN User u ON r.member.id = u.id
            WHERE (r.id = :id) AND (u.userName = :userName)
            """;

    String SQL_REG_BY_ID_AND_MANAGERNAME = """
            SELECT r FROM Registration r LEFT JOIN User u ON r.member.id = u.id
            WHERE (r.id = :id) AND (u.userName = :managerName OR u.managerName = :managerName)
            """;

    String SQL_REGS_BY_STATUSES = """
            SELECT r FROM Registration r
            WHERE r.status IN :statuses
            ORDER BY r.id DESC
            """;

    String SQL_REGS_BY_STATUSES_AND_MANAGERNAME = """
            SELECT r FROM Registration r LEFT JOIN User u ON r.member.id = u.id
            WHERE (r.status IN :statuses)
            AND (:managerName IS NULL OR u.managerName = :managerName)
            ORDER BY r.id DESC
            """;

    String SQL_REGS_BY_EXAMPLE_AND_MANAGERNAME = """
            SELECT r FROM Registration r LEFT JOIN User u ON r.member.id = u.id
            WHERE (:status IS null OR r.status = :status)
            AND (lower(u.lastName) LIKE :lastName)
            AND (:startDate IS NULL OR (r.startDate >= :startDate))
            AND (:managerName IS NULL OR u.managerName = :managerName OR u.userName = :userName)
            ORDER BY r.id DESC
            """;

    String SQL_REGS_BY_MANAGERNAME = """
            SELECT r FROM Registration r LEFT JOIN User u ON r.member.id = u.id
            WHERE (u.managerName = :managerName OR u.userName = :managerName)
            """;

    String SQL_REGS_BY_USERNAME = """
            SELECT r FROM Registration r LEFT JOIN User u ON r.member.id = u.id
            WHERE (u.userName = :userName)
            """;

    @Query(SQL_REG_BY_ID_AND_USERNAME)
    Optional<Registration> findRegistrationByIdAndUser(
            @Param("id") Long id, @Param("userName") String userName);

    @Query(SQL_REG_BY_ID_AND_MANAGERNAME)
    Optional<Registration> findRegistrationByIdAndManager(
            @Param("id") Long id, @Param("managerName") String managerName);

    @Query(SQL_REGS_BY_STATUSES)
    List<Registration> findRegistrationsByStatuses(
            @Param("statuses") EnumSet<RegistrationStatus> statuses);

    @Query(SQL_REGS_BY_STATUSES_AND_MANAGERNAME)
    List<Registration> findRegistrationsByStatusesAndManager(
            @Param("statuses") EnumSet<RegistrationStatus> statuses, @Param("managerName") String managerName);

    @Query(SQL_REGS_BY_EXAMPLE_AND_MANAGERNAME)
    List<Registration> findAllByExample(
            @Param("lastName") String lastName, @Param("startDate") LocalDate startDate, @Param("status") RegistrationStatus status,
            @Param("userName") String userName, @Param("managerName") String managerName);

    @Query(SQL_REGS_BY_MANAGERNAME)
    Page<Registration> findByManagerName(@Param("managerName") String managerName, Pageable pageable);

    @Query(SQL_REGS_BY_USERNAME)
    Page<Registration> findByUserName(@Param("userName") String userName, Pageable pageable);
}
