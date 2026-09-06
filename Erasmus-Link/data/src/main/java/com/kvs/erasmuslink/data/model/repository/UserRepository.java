package com.kvs.erasmuslink.data.model.repository;

import com.kvs.erasmuslink.data.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository class for CRUD operations for User
 *
 * @author Venislav Kirilov
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
}
