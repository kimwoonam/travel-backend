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

@Entity
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getChangeFileName() {
        return changeFileName;
    }

    public void setChangeFileName(String changeFileName) {
        this.changeFileName = changeFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
