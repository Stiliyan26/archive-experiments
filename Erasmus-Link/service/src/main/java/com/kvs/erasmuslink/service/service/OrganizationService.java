package com.kvs.erasmuslink.service.service;

import com.kvs.erasmuslink.data.model.dto.OrganizationDTO;
import com.kvs.erasmuslink.data.model.entity.Organization;
import com.kvs.erasmuslink.data.model.entity.User;
import com.kvs.erasmuslink.data.model.repository.OrganizationRepository;
import com.kvs.erasmuslink.data.model.repository.UserRepository;
import com.kvs.erasmuslink.service.exception.OrganizationException;
import org.springframework.stereotype.Service;

/**
 * Service class for organization endpoints
 *
 * @author Venislav Kirilov
 */
@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                               UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new organization entity
     *
     * @param organizationDTO  contains the data for the entity
     * @return the entity
     */
    public Organization createOrganization(OrganizationDTO organizationDTO) {
        User manager = userRepository.findById(organizationDTO.getManagerId())
                .orElseThrow(() -> new OrganizationException("Manager not found"));

        Organization organization = new Organization(
                manager,
                organizationDTO.getName().trim(),
                organizationDTO.getDescription() == null ? "" : organizationDTO.getDescription(),
                organizationDTO.getContactInfo()
        );

        return organizationRepository.save(organization);
    }
}
