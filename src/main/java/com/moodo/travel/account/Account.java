package com.moodo.travel.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Setter
@Table(name = "account", 
    uniqueConstraints = @UniqueConstraint(columnNames = "email"),
    indexes = {
        @Index(name = "idx_account_email", columnList = "email"),
        @Index(name = "idx_account_uuid", columnList = "uuid"),
        @Index(name = "idx_account_created_at", columnList = "created_at")
    })
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Comment("UUID")
    private String uuid;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    @Comment("이메일")
    private String email;

    @NotBlank
    @Size(min = 6)
    @Column(nullable = false)
    @Comment("비밀번호")
    private String passwordHash;

    @NotBlank
    @Comment("이름")
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
