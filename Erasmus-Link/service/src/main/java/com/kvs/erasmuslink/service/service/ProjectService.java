package com.kvs.erasmuslink.service.service;

import com.kvs.erasmuslink.data.model.dto.ProjectDTO;
import com.kvs.erasmuslink.data.model.entity.Organization;
import com.kvs.erasmuslink.data.model.entity.Project;
import com.kvs.erasmuslink.data.model.enums.ProjectStatus;
import com.kvs.erasmuslink.data.model.repository.OrganizationRepository;
import com.kvs.erasmuslink.data.model.repository.ProjectRepository;
import com.kvs.erasmuslink.service.exception.ProjectException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Service class for project endpoints
 *
 * @author Venislav Kirilov
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;

    public ProjectService(ProjectRepository projectRepository,
                          OrganizationRepository organizationRepository) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
    }

    /**
     * Creates a new project entity
     *
     * @param projectDTO contains the data for the entity
     * @return the entity
     */
    public Project createProject(ProjectDTO projectDTO) {
        verifyStatus(projectDTO.getStatus());

        Organization organization = organizationRepository.findById(projectDTO.getOrganizationId())
                .orElseThrow(() -> new ProjectException("Organization with ID " + projectDTO.getOrganizationId() + " not found"));

        LocalDate startDate = parseDate(projectDTO.getStartDate());
        LocalDate endDate = parseDate(projectDTO.getEndDate());
        verifyDateLogic(startDate, endDate);

        Project project = new Project(
                organization,
                projectDTO.getTitle().trim(),
                projectDTO.getDescription().trim(),
                ProjectStatus.valueOf(projectDTO.getStatus().toUpperCase()),
                startDate,
                endDate
        );

        return projectRepository.save(project);
    }

    /**
     * Asserts that the given status is OPEN or CLOSED
     *
     * @param status status of the project
     */
    private void verifyStatus(String status) {
        if (!ProjectStatus.contains(status)) {
            throw new ProjectException("Invalid Project status!");
        }
    }

    /**
     * Verifies that the dates follow the right formatting
     * and simple logic as end date must be after the start date
     * and start date must be before the current date of the system
     *
     * @param startDate start date
     * @param endDate   end date
     */
    private void verifyDateLogic(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new ProjectException("End date cannot be before start date!");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new ProjectException("Start date cannot be before the current date!");
        }
    }

    /**
     * Formats the date accordingly to pattern dd/MM/yyyy
     *
     * @param dateStr the date string
     * @return the formatted date
     */
    private LocalDate parseDate(String dateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new ProjectException("Invalid date format! Use dd/MM/yyyy.");
        }
    }
}
