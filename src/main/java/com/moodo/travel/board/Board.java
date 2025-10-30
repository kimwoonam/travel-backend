package com.moodo.travel.board;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Setter
@Table(name = "board")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Comment("SELECT, DELETE, UPDATE에서 사용될 UUID")
    private String uuid;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    @Comment("제목")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 1, max = 255, message = "제목은 1자 이상 255자 이하여야 합니다.")
    private String title;

    @Column(columnDefinition = "TEXT")
    @Comment("본문내용")
    @Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다.")
    private String content;

    @Column(name = "nick_name", nullable = false)
    @Comment("등록자")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하여야 합니다.")
    private String nickName;

    @Column(name = "account_uuid", nullable = false)
    @Comment("등록자의 UUID")
    private String accountUuid;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
