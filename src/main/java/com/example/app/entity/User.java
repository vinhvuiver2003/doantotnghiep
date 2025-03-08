package com.example.app.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "User_ID")
    private Integer id;

    @Column(name = "Username", unique = true, nullable = false)
    private String username;

    @Column(name = "Password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "FirstName", nullable = false)
    private String firstName;

    @Column(name = "LastName", nullable = false)
    private String lastName;

    @Column(name = "Email", unique = true, nullable = false)
    private String email;

    @Column(name = "Phone", nullable = false)
    private String phone;

    @Column(name = "Address", nullable = false)
    private String address;

    @ManyToOne
    @JoinColumn(name = "Role_ID", nullable = false)
    private Role role;

    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Last_Login")
    private LocalDateTime lastLogin;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}