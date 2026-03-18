package ca.koona.cron_job.dao.repositories;

import ca.koona.cron_job.dao.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByStatus(String active);
}
