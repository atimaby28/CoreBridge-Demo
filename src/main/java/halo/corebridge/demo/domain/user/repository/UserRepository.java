package halo.corebridge.demo.domain.user.repository;

import halo.corebridge.demo.domain.user.entity.User;
import halo.corebridge.demo.domain.user.enums.UserRole;
import halo.corebridge.demo.domain.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<User> findByRoleOrderByCreatedAtDesc(UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.email LIKE %:keyword% OR u.nickname LIKE %:keyword%")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Long countByStatus(UserStatus status);

    Long countByRole(UserRole role);
}
