package com.kvs.erasmuslink.data.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO class for Organization Entity
 *
 * @author Venislav Kirilov
 */
public class OrganizationDTO {
    @NotNull(message = "ManagerId cannot be null")
    private Integer managerId;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 5, max = 20, message = "Organization name must be between 5 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]+(?:[ ._-][a-zA-Z0-9]+)*$",
            message = "Name can only contain alphanumerics separated by single spaces or ._-")
    private String name;

    private String description;

    private double rating;

    @NotBlank(message = "ContactInfo cannot be blank")
    @Size(min = 5, max = 50, message = "ContactInfo must be between 5 and 50 characters")
    private String contactInfo;

    public OrganizationDTO() {}

    public OrganizationDTO(Integer managerId, String name, String description, String contactInfo) {
        this.managerId = managerId;
        this.name = name;
        this.description = description;
        this.contactInfo = contactInfo;
    }

    public Integer getManagerId() { return managerId; }
    public void setManagerId(Integer managerId) { this.managerId = managerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
}

