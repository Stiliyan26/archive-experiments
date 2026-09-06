package com.kvs.erasmuslink.data.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO class for Project Entity
 *
 * @author Venislav Kirilov
 */
public class ProjectDTO {
    @NotNull(message = "organizationId cannot be null")
    private Integer organizationId;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 5, max = 20, message = "Title must be between 5 and 20 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[ ._-][a-zA-Z0-9]+)*$",
            message = "Title can only contain alphanumerics separated by single spaces or ._-")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 30, max = 500, message = "Description must be between 30 and 500 characters long")
    private String description;

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "OPEN|CLOSED",
            message = "Status must be OPEN or CLOSED")
    private String status;

    @NotBlank(message = "Start date cannot be blank")
    private String startDate; // Format: dd/MM/yyyy

    @NotBlank(message = "End date cannot be blank")
    private String endDate; // Format: dd/MM/yyyy

    public ProjectDTO() {
    }

    public ProjectDTO(Integer organizationId, String title, String description, String status, String startDate, String endDate) {
        this.organizationId = organizationId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}

