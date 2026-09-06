package com.kvs.erasmuslink.service.controller;

import com.kvs.erasmuslink.data.model.dto.ProjectDTO;
import com.kvs.erasmuslink.data.model.entity.Project;
import com.kvs.erasmuslink.service.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for project endpoints
 *
 * @author Venislav Kirilov
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public Project createProject(@RequestBody @Valid ProjectDTO requestDTO) {
        return projectService.createProject(requestDTO);
    }
}

