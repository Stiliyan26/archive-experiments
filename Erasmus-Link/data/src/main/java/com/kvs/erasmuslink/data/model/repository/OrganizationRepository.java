package com.kvs.erasmuslink.data.model.repository;

import com.kvs.erasmuslink.data.model.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository class for CRUD operations for Organization
 *
 * @author Venislav Kirilov
 */
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    Organization findByName(String name);
    Organization findByManagerId(int managerId);
}