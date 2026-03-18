package ca.koona.cron_job.dao.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    private String status;

    public User() {
    }

    public User(String username, LocalDateTime lastLoginDate, String status) {
        this.username = username;
        this.lastLoginDate = lastLoginDate;
        this.status = status;
    }
}
