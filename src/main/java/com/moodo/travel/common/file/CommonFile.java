package com.moodo.travel.common.file;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Setter
@Table(
    name = "common_file",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"uuid", "table_name", "table_id"},
        name = "unique_uuid_table_name_table_id"
    )
)
public class CommonFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    @Comment("SELECT, DELETE, UPDATE에서 사용될 UUID")
    private String uuid;

    @NotBlank
    @Column(name = "table_name", nullable = false)
    @Comment("등록한 테이블 이름")
    private String tableName;

    @NotNull
    @Column(name = "table_id", nullable = false)
    @Comment("등록한 테이블의 ID")
    private Long tableId;

    @NotBlank
    @Column(name = "original_file_name", nullable = false)
    @Comment("원본 파일 이름")
    private String originalFileName;

    @NotBlank
    @Column(name = "change_file_name", nullable = false)
    @Comment("변경된 파일 이름")
    private String changeFileName;

    @NotBlank
    @Column(name = "file_path", nullable = false)
    @Comment("파일경로")
    private String filePath;

    @NotNull
    @Column(name = "file_size", nullable = false)
    @Comment("파일크기")
    private long fileSize;

    @NotBlank
    @Column(name = "file_extension", nullable = false)
    @Comment("파일확장자")
    private String fileExtension;

    @Column(name = "delete_yn", nullable = false)
    @Comment("삭제여부 (Y: 삭제, N: 미삭제)")
    private String deleteYn;

    @Column(name = "deleted_at")
    @Comment("삭제일시")
    private LocalDateTime deletedAt;

    @Column(name = "created_at")
    @Comment("등록일시")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        deleteYn = "N";
    }
}
