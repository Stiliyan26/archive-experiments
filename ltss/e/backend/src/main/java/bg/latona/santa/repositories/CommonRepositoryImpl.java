package bg.latona.santa.repositories;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QuerydslJpaPredicateExecutor;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;

import bg.latona.santa.entities.CommonRecord;
import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.security.AccessControl;
import bg.latona.santa.entities.security.QAccessControl;
import bg.latona.santa.entities.security.QSecPermission;
import bg.latona.santa.entities.security.QSecRole;
import bg.latona.santa.entities.security.QSecRolePermission;
import bg.latona.santa.entities.security.QSecUser;
import bg.latona.santa.entities.security.QSecUserRole;

@Transactional(readOnly=true)
public class CommonRepositoryImpl<Entity, QEntity extends EntityPath<Entity>, ID extends Serializable> 
		extends SimpleJpaRepository<Entity, ID> 
		implements CommonRepository<Entity, QEntity, ID> {
	
	private static Logger logger = LoggerFactory.getLogger(CommonRepositoryImpl.class);
	private QuerydslJpaPredicateExecutor<Entity> executor;

	EntityManager entityManager; //USING HQL
	JpaEntityInformation<Entity, ID> entityInformation;
	PathBuilder<Entity> entityPathBuilder;
	
	public CommonRepositoryImpl(JpaEntityInformation<Entity, ID> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
		this.entityInformation = entityInformation;
		this.entityManager = entityManager;
		EntityPath<Entity> entityPath = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
		this.entityPathBuilder = new PathBuilder<Entity>(entityInformation.getJavaType(), entityPath.getMetadata());
		executor = new QuerydslJpaPredicateExecutor<Entity>(entityInformation, entityManager, SimpleEntityPathResolver.INSTANCE, null);
		logger.trace("CommonRepositoryImpl public CommonRepositoryImpl(JpaEntityInformation<Entity, ID> entityInformation, EntityManager entityManager)");
	}
	
	private Predicate getSecuredPredicate(Predicate pred) {
		//TODO ACL logic should be triple: ALLOW, DENY, NONE, because now if you want to restrict write, but allow read to anyone, it is not possible (you should give read permission manually to everyone). Another solution would be to have a rule/functionality that gives permissions to everyone.
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		BooleanBuilder builder = new BooleanBuilder();
		if(pred != null) {
			builder.and(pred);
		}
		//hide deleted entities
		Predicate filterDeleted = entityPathBuilder.getBoolean("deleted").eq(Expressions.constant(false));
		builder.and(filterDeleted);

		if (authentication != null && authentication.isAuthenticated()) {
			String currentUser = authentication.getPrincipal().toString();
		
			logger.trace("Type: "+entityInformation.getJavaType()+"; name: "+entityInformation.getEntityName()); //name should be with small letter
			//hide entities not created by the current user, unless has special permission ROLE_GET_ALL_<entityName>
			QSecUser user = QSecUser.secUser;
			QSecUserRole user_role = QSecUserRole.secUserRole;
			QSecRole sec_role = QSecRole.secRole;
			QSecRolePermission role_perm = QSecRolePermission.secRolePermission;
			QSecPermission sec_perm = QSecPermission.secPermission;
			QAccessControl access_control = QAccessControl.accessControl;

			@SuppressWarnings("unchecked")
			Predicate filterUserGetAll = 
					JPAExpressions.select(user).from(user)
					.innerJoin(user.roles, user_role).on(user_role.deleted.eq(Expressions.constant(false)))
					.innerJoin(user_role.role,sec_role).on(sec_role.deleted.eq(Expressions.constant(false)))
					.innerJoin(sec_role.permissions, role_perm).on(role_perm.deleted.eq(Expressions.constant(false)))
					.innerJoin(role_perm.permission, sec_perm).on(sec_perm.deleted.eq(Expressions.constant(false)))
					.where(user.name.eq(Expressions.constant(currentUser))
						.and(user.deleted.eq(Expressions.constant(false)))
						.and(sec_perm.code.toUpperCase().in(Expressions.constant("ROLE_GET_ALL_"+entityInformation.getEntityName().toUpperCase()))))
					.exists();
			Predicate filterUserAllow = JPAExpressions.select(access_control.read).from(access_control)
					.innerJoin(access_control.secUser,user).on(user.name.eq(Expressions.constant(currentUser)).and(user.deleted.eq(Expressions.constant(false))))
					.where(access_control.deleted.eq(Expressions.constant(false))
						.and(access_control.read.eq(Expressions.constant(true)))
						.and(access_control.commonRecordId.eq(entityPathBuilder.get("id")))
					)
					.exists();
			Predicate filterSomeoneHasAllow = JPAExpressions.select(access_control.read).from(access_control)
					.where(access_control.deleted.eq(Expressions.constant(false))
						.and(access_control.read.eq(Expressions.constant(false)))
						.and(access_control.commonRecordId.eq(entityPathBuilder.get("id")))
					)
					.exists();
			Predicate filterUserDeny = JPAExpressions.select(access_control.read).from(access_control)
					.innerJoin(access_control.secUser,user).on(user.name.eq(Expressions.constant(currentUser)).and(user.deleted.eq(Expressions.constant(false))))
					.where(access_control.deleted.eq(Expressions.constant(false))
						.and(access_control.read.eq(Expressions.constant(false)))
						.and(access_control.commonRecordId.eq(entityPathBuilder.get("id")))
					)
					.exists();
			BooleanBuilder filterDeny = (new BooleanBuilder()).or(filterSomeoneHasAllow).or(filterUserDeny).not();
			BooleanBuilder filterACL = (new BooleanBuilder()).or(filterUserGetAll).or(filterUserAllow).or(filterDeny);
			builder.and(filterACL);
			if(CompanyRecord.class.isAssignableFrom(entityInformation.getJavaType())) {
				//filter by company of the current user
				//Predicate filterCompany = JPAExpressions.select(user.company.id).from(user).where(user.name.eq(Expressions.constant(currentUser)).and(user.company.eq(entityPathBuilder.get("company")))).exists();
				Predicate filterCompany = entityPathBuilder.get("company").in(JPAExpressions.select(user.company).from(user).where(user.name.eq(Expressions.constant(currentUser))));
				builder.and(filterCompany);
			}
		}
		return builder.getValue();
	}

	//override CrudRepository
	@Override
	@Transactional
	public <S extends Entity> S save(S entity) {
		logger.trace("CommonRepositoryImpl public Entity save(Entity entity)");
		/*
		 * Since the JPA wraps in transaction/entity manager the new entity
		 * we don't have the old entity values available.
		 * So when we try to change 'administer', we cannot have the old, actual value.
		 * Without the old value of 'administer' the check if the user is allowed to administer
		 * is not correct. Example: user doesn't have 'administer', but marks it and saves, then 
		 * we see it 'marked' and allow the save, which is wrong.
		 * Solution: get in another transaction/entity manager, where we can check the old values
		 * which are persisted - we create new entity manager for this
		 */
		//TODO check if this 'clean entity manager' can be static, created only once
		EntityManager cleanEM = entityManager.getEntityManagerFactory().createEntityManager();
		Long commonRecordId = null;
		if(entity instanceof CommonRecord) { //check ACL for CommonRecord entities
			commonRecordId = ((CommonRecord) entity).getId();
		}
		if(entity instanceof AccessControl) { //check ACL for AccessControl entities (administrate permission)
			commonRecordId = ((AccessControl) entity).getCommonRecordId();
		}
		if(commonRecordId != null) { //check ACL only for existing entities (or we should use entityInformation.isNew(entity)?)
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated()) {
				String currentUser = authentication.getPrincipal().toString();
				//get the ACL of the entity for the current user
				BooleanBuilder builder = new BooleanBuilder();
				QAccessControl accessControl = QAccessControl.accessControl;
				Predicate pred = builder.and(
						accessControl.commonRecordId.eq(commonRecordId)
					).and(
						accessControl.secUser.name.eq(Expressions.constant(currentUser))
					).and(
						accessControl.deleted.eq(Expressions.constant(false))
					);
				JPAQuery<AccessControl> query = new JPAQuery<AccessControl>(cleanEM);
				AccessControl acl = query.select(accessControl).from(accessControl).where(pred).fetchFirst();
				if(acl != null) {
					//check if the permission is ALLOW or DENY
					if(entity instanceof AccessControl) {
						if(!acl.getAdminister()) {
							//DENY administer
							cleanEM.close();
							throw new RuntimeException("ACL.DENY_administer");
						} //else it is ALLOW and save is OK
					} else if(!acl.getWrite()) {
						//DENY save
						cleanEM.close();
						throw new RuntimeException("ACL.DENY_write");
					} //else it is ALLOW and save is OK
				} else {
					//check if there is any ACL for this entity that is with ALLOW permission
					BooleanBuilder builder2 = new BooleanBuilder();
					builder2.and(
							accessControl.commonRecordId.eq(commonRecordId)
						).and(
							accessControl.deleted.eq(Expressions.constant(false))
						);
					if(entity instanceof AccessControl) {
						builder2.and(
							accessControl.administer.eq(Expressions.constant(true))
						);
					} else {
						builder2.and(
							accessControl.write.eq(Expressions.constant(true))
						);
					}
					JPAQuery<AccessControl> query2 = new JPAQuery<AccessControl>(cleanEM);
					AccessControl acl2 = query2.select(accessControl).from(accessControl).where(builder2).fetchFirst();
					if(acl2 != null) {
						//there is someone with ALLOW permission, but you don't have it
						if(entity instanceof AccessControl) {
							cleanEM.close();
							throw new RuntimeException("ACL.no_ALLOW_administer");
						} else {
							cleanEM.close();
							throw new RuntimeException("ACL.no_ALLOW_write");
						}
					}
				}
			}
		}
		cleanEM.close();
		return super.save(entity);
	}
	
	@Override
	public long count() {
		logger.trace("CommonRepositoryImpl public List<Entity> count()");
		Predicate securedPred = getSecuredPredicate(null);
		return securedPred == null ? super.count() : executor.count(securedPred);
	}

	@Override
	@Transactional
	public void delete(final Entity entity) {
		logger.trace("CommonRepositoryImpl public void delete(final Entity entity)");
		if(entity instanceof CommonRecord) {
			((CommonRecord)entity).setDeleted(true);
			this.save(entity);
		} else {
			super.delete(entity);
		}
	}
	
	@Transactional
	@Override
	public void deleteById(ID id) {
		//override this to avoid findById not finding the entity that should be deleted 
		//WARNING: this skips the permissions check, but deleteById is not actually used elsewhere at the moment
		logger.trace("CommonRepositoryImpl public void deleteById(ID id)");
		delete(super.findById(id).orElseThrow(() -> new EmptyResultDataAccessException(
				String.format("No %s entity with id %s exists", entityInformation.getJavaType(), id), 1)));
	}
	
	@Override
	public Optional<Entity> findById(ID id) {
		logger.trace("CommonRepositoryImpl public Optional<Entity> findById(ID id)");
		if(id == null) return Optional.empty();
		Predicate pred = entityPathBuilder.get("id").eq(Expressions.constant(id));
		Predicate securedPred = getSecuredPredicate(pred);
		return executor.findOne(securedPred);
	}
	
	//override ListCrudRepository
	@Override
	public List<Entity> findAll() {
		logger.trace("CommonRepositoryImpl public List<Entity> findAll()");
		Predicate securedPred = getSecuredPredicate(null);
		return securedPred == null ? super.findAll() : executor.findAll(securedPred);
	}
	
	@Override
	public List<Entity> findAllById(Iterable<ID> ids) {
		logger.trace("CommonRepositoryImpl public List<Entity> findAllById(Iterable<ID> ids)");
		throw new RuntimeException("findAllById not supported");
	}
	
	//override PagingAndSortingRepository
	@Override
	public Page<Entity> findAll(Pageable pageable) {
		logger.trace("CommonRepositoryImpl public Page<Entity> findAll(Pageable pageable)");
		Predicate securedPred = getSecuredPredicate(null);
		return securedPred == null ? super.findAll(pageable) : executor.findAll(securedPred, pageable);
	}

	@Override
	public List<Entity> findAll(Sort sort) {
		logger.trace("CommonRepositoryImpl public List<Entity> findAll(Sort sort)");
		Predicate securedPred = getSecuredPredicate(null);
		return securedPred == null ? super.findAll(sort) : executor.findAll(securedPred, sort);
	}
	
	//override JpaRepository
	@Override
	public <S extends Entity> List<S> findAll(Example<S> example) {
		logger.trace("CommonRepositoryImpl public <S extends Entity> List<S> findAll(Example<S> example)");
		throw new RuntimeException("findAll(Example) not supported");
		//return null;//super.findAllById(ids);
	}
	
	@Override
	public <S extends Entity> List<S> findAll(Example<S> example, Sort sort) {
		logger.trace("CommonRepositoryImpl public <S extends Entity> List<S> findAll(Example<S> example, Sort sort)");
		throw new RuntimeException("findAll(Example,Sort) not supported");
		//return null;//super.findAllById(ids);
	}

	@Override
	public Entity getById(ID id) {
		logger.trace("CommonRepositoryImpl public Entity getById(ID id)");
		Predicate pred = entityPathBuilder.get("id").eq(Expressions.constant(id));
		Predicate securedPred = getSecuredPredicate(pred);
		Optional<Entity> result = executor.findOne(securedPred);
		if(result.isPresent()) {
			return result.get();
		} else {
			return null;
		}
	}
	
	@Override
	public Entity getOne(ID id) {
		logger.trace("CommonRepositoryImpl public Entity getOne(ID id)");
		Predicate pred = entityPathBuilder.get("id").eq(Expressions.constant(id));
		Predicate securedPred = getSecuredPredicate(pred);
		Optional<Entity> result = executor.findOne(securedPred);
		if(result.isPresent()) {
			return result.get();
		} else {
			return null;
		}
	}

	@Override
	public Entity getReferenceById(ID id) {
		logger.trace("CommonRepositoryImpl public Entity getReferenceById(ID id)");
		Predicate pred = entityPathBuilder.get("id").eq(Expressions.constant(id));
		Predicate securedPred = getSecuredPredicate(pred);
		Optional<Entity> result = executor.findOne(securedPred);
		if(result.isPresent()) {
			return result.get();
		} else {
			return null;
		}
	}
	
	//QuerydslPredicateExecutor
	public long count(Predicate pred) {
		logger.trace("CommonRepositoryImpl public long count(Predicate pred)");
		Predicate securedPred = getSecuredPredicate(pred);
		return executor.count(securedPred);
	}

	public boolean exists(Predicate pred) {
		logger.trace("CommonRepositoryImpl public boolean exists(Predicate pred)");
		Predicate securedPred = getSecuredPredicate(pred);
		return executor.exists(securedPred);
	}

	public List<Entity> findAll(Predicate pred) {
		logger.trace("CommonRepositoryImpl public List<Entity> findAll(Predicate pred)");
		Predicate securedPred = getSecuredPredicate(pred);
		return executor.findAll(securedPred);
	}

	public List<Entity> findAll(OrderSpecifier<?>... orders) {
		logger.trace("CommonRepositoryImpl public List<Entity> findAll(OrderSpecifier<?>... orders)");
		Predicate pred = Expressions.TRUE.eq(true);
		Predicate securedPred = getSecuredPredicate(pred);
		return executor.findAll(securedPred, orders);
	}

	public List<Entity> findAll(Predicate pred, Sort sort) {
		logger.trace("CommonRepositoryImpl public List<Entity> findAll(Predicate pred, Sort sort)");
		Predicate securedPred = getSecuredPredicate(pred);
		return executor.findAll(securedPred, sort);
	}

	public List<Entity> findAll(Predicate pred, OrderSpecifier<?>... orders) {
		logger.trace("CommonRepositoryImpl public List<Entity> findAll(Predicate pred, OrderSpecifier<?>... orders)");
		Predicate securedPred = getSecuredPredicate(pred);
		return executor.findAll(securedPred, orders);
	}

	public Page<Entity> findAll(Predicate pred, Pageable pageable) {
		logger.trace("CommonRepositoryImpl public Page<Entity> findAll(Predicate pred, Pageable pageable)");
		Predicate securedPred = getSecuredPredicate(pred);
		return executor.findAll(securedPred, pageable);
	}

	public <S extends Entity, R> R findBy(Predicate predicate, Function<FetchableFluentQuery<S>, R> queryFunction) {
		logger.trace("CommonRepositoryImpl public <S extends Entity, R> R findBy(Predicate predicate, Function<FetchableFluentQuery<S>, R> queryFunction)");
		return executor.findBy(getSecuredPredicate(predicate), queryFunction);
	}

	public Optional<Entity> findOne(Predicate pred) {
		logger.trace("CommonRepositoryImpl public Entity findOne(Predicate pred)");
		Predicate securedPred = getSecuredPredicate(pred);
		return executor.findOne(securedPred);
	}
}
