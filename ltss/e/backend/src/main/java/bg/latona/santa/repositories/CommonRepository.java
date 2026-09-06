package bg.latona.santa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.querydsl.core.types.EntityPath;

import java.io.Serializable;

//TODO maybe something like: http://www.baeldung.com/rest-api-search-language-spring-data-querydsl
/*
 * Row filtering is required for security and for the 'deleted' rows.
 * Filtering solution:
 * - @PostFilter - doesn't work with Page
 * - @Query - not applicable for QueryDSL
 * - somehow inject additional URL parameters - not covering all access
 * - Hibernate entity-level filtering: https://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/filters.html - will have to be put on every separate entity and enabled in the session
 * - override GET method and implement our own methods - we lose QueryDSL and may have to totally change frontend - current
 * - implement an extended QueryDslPredicateExecutor - copy its source and modify it - too complicated?
 * - make custom controller - will have to parse all the filter and sort params
 * - make custom repository implementation over QueryDSL - extend the filter in the prepared Predicate! - can't have own implementation of QuerydslPredicateExecutor methods, it puts QuerydslJpaPredicateExecutor (https://github.com/spring-projects/spring-data-jpa/blob/main/spring-data-jpa/src/main/java/org/springframework/data/jpa/repository/support/JpaRepositoryFactory.java method getRepositoryFragments 
 */

@CrossOrigin //allows calls from other servers
@NoRepositoryBean //http://blog.netgloo.com/2014/12/18/handling-entities-inheritance-with-spring-data-jpa/
public interface CommonRepository<Entity, QEntity extends EntityPath<Entity>, ID extends Serializable> extends JpaRepository<Entity, ID> {
	
}