package com.kvs.erasmuslink.service.controller;

import com.kvs.erasmuslink.data.model.dto.OrganizationDTO;
import com.kvs.erasmuslink.data.model.entity.Organization;
import com.kvs.erasmuslink.service.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for organization endpoints
 *
 * @author Venislav Kirilov
 */
@RestController
@RequestMapping("api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public Organization createOrganization(@RequestBody @Valid OrganizationDTO requestDTO) {
        return organizationService.createOrganization(requestDTO);
    }
}

