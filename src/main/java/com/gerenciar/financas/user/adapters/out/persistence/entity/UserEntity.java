package com.gerenciar.financas.user.adapters.out.persistence.entity;

import com.gerenciar.financas.user.domain.model.User;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false)
    private String password;

    protected UserEntity() {
    }

    public UserEntity(
            UUID id,
            String name,
            String email,
            String password
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public User toDomain() {
        return new User(
                id,
                name,
                email,
                password
        );
    }

    public static UserEntity fromDomain(User user) {
        return new UserEntity(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword()
        );
    }
}
