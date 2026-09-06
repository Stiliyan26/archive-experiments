package com.kvs.erasmuslink.data.model.repository;

import com.kvs.erasmuslink.data.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository class for CRUD operations for Project
 *
 * @author Venislav Kirilov
 */
public interface ProjectRepository extends JpaRepository<Project, Integer> {}