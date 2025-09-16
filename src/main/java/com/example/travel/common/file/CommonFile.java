package com.example.travel.common.file;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "common_file",
    uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        columnNames = {"table_name", "table_id"},
        name = "unique_table_name_table_id"
    )
)
public class CommonFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String uuid;

    @NotBlank
    @Column(name = "table_name", nullable = false)
    private String tableName;

    @NotBlank
    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @NotBlank
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @NotBlank
    @Column(name = "change_file_name", nullable = false)
    private String changeFileName;

    @NotBlank
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @NotBlank
    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @NotBlank
    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
