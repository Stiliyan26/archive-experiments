package bg.latona.santa.reports;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.transaction.Transactional;

import org.hibernate.MappingException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.query.criteria.internal.BasicPathUsageException;
import org.hibernate.query.criteria.internal.path.SingularAttributeJoin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.core.types.dsl.Coalesce;

import bg.latona.santa.DroolsRepositoryValidator;
import bg.latona.santa.RepositoryConfiguration;
import bg.latona.santa.entities.*;
import bg.latona.santa.repositories.*;
import bg.latona.santa.entities.QAttachable;
import bg.latona.santa.entities.QAttachableRevenuesAndExpenses;
import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.article.ArticlePriceRate;
import bg.latona.santa.entities.article.Currency;
import bg.latona.santa.entities.article.QArticle;
import bg.latona.santa.entities.employee.JobPosition;
import bg.latona.santa.entities.employee.JobRequirement;
import bg.latona.santa.entities.invoice.Invoice;
import bg.latona.santa.entities.invoice.LoiPaymentType;
import bg.latona.santa.entities.invoice.LoiVatExemptionReason;
import bg.latona.santa.entities.mail.MailAccount;
import bg.latona.santa.entities.mail.SendMailMessage;
import bg.latona.santa.entities.offer.OfferLine;
import bg.latona.santa.entities.person.*;
import bg.latona.santa.entities.santa.common.*;
import bg.latona.santa.entities.santa.finance.*;
import bg.latona.santa.entities.security.*;
import bg.latona.santa.entities.task.*;
import bg.latona.santa.entities.transport.LoiTransportType;
import bg.latona.santa.entities.transport.LoiVehicleType;
import bg.latona.santa.entities.transport.ShippingContainerType;
import bg.latona.santa.entities.wato.*;

@Repository
@Transactional
public class ReportsRepository {

	private static Logger logger = LoggerFactory.getLogger(ReportsRepository.class);
	// entity manager insert
	@PersistenceContext
	private EntityManager entityManager; //USING HQL
	@Autowired
	private WebApplicationContext appContext;
	@Autowired
	private RepositoryResourceMappings mappings;
	private Repositories repositories = null;
	
	private Repositories getRepositories() {
		if(repositories == null) {
			repositories = new Repositories(appContext);
		}
		return repositories;
	}
	
	public static Class<?> getClassFromRestUriOrClassName(RepositoryResourceMappings mappings, String searchName) throws ClassNotFoundException {
		Optional<Class<?>> domainType = mappings.stream()
			.filter(metadata -> metadata.getPath().matches(searchName))
			.findFirst()
			.map(ResourceMetadata::getDomainType);
		if(domainType.isPresent()) {
			logger.trace("Found class "+domainType.get().getName()+" for the searched name "+searchName);
			return domainType.get();
		} else {
			String rootEntitiesPackageName = CommonRecord.class.getPackage().getName();
			for(Package aPackage: Package.getPackages()) {
				try {
					if(aPackage.getName().startsWith(rootEntitiesPackageName)) {
						logger.trace("Search package "+aPackage.getName()+" for class for the searched name "+searchName);
						return Class.forName(aPackage.getName()+"."+searchName);
					}
				} catch(ClassNotFoundException e) {
					//ignore and try next package
				}
			}
			throw new ClassNotFoundException(searchName);
		}
	}
	
	public SecUser findSecUserByName(String name) { //this is unique in all companies
		SecUserRepository repo = ((SecUserRepository) getRepositories().getRepositoryFor(SecUser.class).get());
		return repo.findFirstByName(name);
	}

	public List<Tuple> getUsernameAuthorities(String username) {
		JPAQuery<?> query = new JPAQuery<Void>(entityManager);
		QSecUser user = QSecUser.secUser;
		QSecUserRole userRole = QSecUserRole.secUserRole;
		QSecRole secRole = QSecRole.secRole;
		QSecRolePermission rolePerm = QSecRolePermission.secRolePermission;
		//no company filter is needed, because username is unique across companies
		//no ACL check needed, this is internal
		List<Tuple> result = query.select(user.name,user.password,rolePerm.permission.code)
				.from(user)
				.innerJoin(user.roles,userRole).on(userRole.deleted.eq(false))
				.innerJoin(userRole.role,secRole).on(secRole.deleted.eq(false))
				.innerJoin(secRole.permissions,rolePerm).on(rolePerm.deleted.eq(false))
				.where(user.name.eq(username)
						.and(user.deleted.eq(false)))
				.fetch();
		return result;
	}
	
	private boolean hasGetPermissionForEntity(Class entityClass) {
		final RepositoryResourceMappings repositoryResourceMappings = appContext.getBean(RepositoryResourceMappings.class);
		if(repositoryResourceMappings == null) {
			throw new RuntimeException("Cannot get RepositoryResourceMappings bean");
		}
		ResourceMetadata resMetadata = repositoryResourceMappings.getMetadataFor(entityClass);
		if(resMetadata == null) {
			throw new RuntimeException("Cannot get ResourceMetadata for "+entityClass.getSimpleName());
		}
		String rel = resMetadata.getRel().value();
		String role = "ROLE_GET_"+rel;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		for(GrantedAuthority authority: authentication.getAuthorities()) {
			if(((SimpleGrantedAuthority)authority).getAuthority().equalsIgnoreCase(role)) {
				logger.debug("Granted GET permission for entity: "+role);
				return true;
			}
			if(((SimpleGrantedAuthority)authority).getAuthority().equalsIgnoreCase("ROLE_GET_ANY")) {
				logger.debug("Granted GET permission for entity: "+"ROLE_GET_ANY");
				return true;
			}
		}
		logger.debug("No GET permission for entity: "+role);
		new org.springframework.security.access.AccessDeniedException("User doesn't have authority: "+role);
		return false;
	}
	
	public BigInteger isRecognizedContact(String fromAddress, Long companyCode) {
		try {
			BigInteger queryResult = (BigInteger) entityManager.createNativeQuery("select min(contact.id) from contact \n" + 
					"inner join managed_company mc on mc.id = contact.company_id and mc.code = :companyCode\n" + 
					"where :fromAddress like '%'||contact.email||'%'\n" + 
					"and length(contact.email) > 0\n" + 
					"and contact.deleted = false")
					.setParameter("companyCode", companyCode)
					.setParameter("fromAddress", fromAddress)
					.getSingleResult();
			return queryResult;
		} catch (NoResultException e) {
			return null;
		}
	}

	//TODO add ACL filtering
	public List<PlannedTimeDTO> reportSubTasksPlannedTime(List<Long> subTaskIds) {
		//get the logged user name
		String currentUser = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}
		List<PlannedTimeDTO> result = new LinkedList<PlannedTimeDTO>();
		//TODO check for cycles?
		for(Long Id : subTaskIds) {
			JPAQuery<?> query = new JPAQuery<Void>(entityManager);
			QPlannedTime plannedTime = QPlannedTime.plannedTime;
			QSecUser secUser = QSecUser.secUser;
			QSecUser resource = new QSecUser("resource");
			result.addAll(query.select(Projections.constructor(PlannedTimeDTO.class, plannedTime.resource,plannedTime.minutes,plannedTime.task))
				.from(plannedTime)
				.join(plannedTime.resource,resource).on(resource.deleted.eq(false))
				.where(plannedTime.task.id.eq(Id)
						.and(plannedTime.company.users.contains(JPAExpressions.select(secUser).from(secUser).where(secUser.name.eq(Expressions.constant(currentUser)))))
						.and(plannedTime.deleted.eq(false)))
				.fetch());
			JPAQuery<?> querySubTasks = new JPAQuery<Void>(entityManager);
			QTaskRelation taskRelation = QTaskRelation.taskRelation;
			List<Long> subTasks = querySubTasks.select(taskRelation.toTask.id)
					.from(taskRelation)
					.where(taskRelation.fromTask.id.eq(Id)
							.and(taskRelation.toTask.id.isNotNull())
							.and(taskRelation.relation.code.eq(TaskRelationType.TASK_RELATION_TYPE_SUBTASK))
							.and(taskRelation.deleted.eq(false)))
					.fetch();
			result.addAll(reportSubTasksPlannedTime(subTasks));
		}
		return result;
	}

	//TODO add ACL filtering
	public List<PlannedIncomeOrExpenseDTO> reportSubTasksPlannedIncomeOrExpense(List<Long> subTaskIds) {
		//get the logged user name
		String currentUser = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}
		List<PlannedIncomeOrExpenseDTO> result = new LinkedList<PlannedIncomeOrExpenseDTO>();
		//TODO check for cycles?
		for(Long Id : subTaskIds) {
			JPAQuery<?> query = new JPAQuery<Void>(entityManager);
			QPlannedIncomeOrExpense plannedIncomeOrExpense = QPlannedIncomeOrExpense.plannedIncomeOrExpense;
			QArticle article = QArticle.article;
			QSecUser secUser = QSecUser.secUser;
			result.addAll(query.select(Projections.constructor(PlannedIncomeOrExpenseDTO.class, plannedIncomeOrExpense.article,plannedIncomeOrExpense.ammount,plannedIncomeOrExpense.task))
				.from(plannedIncomeOrExpense)
				.join(plannedIncomeOrExpense.article, article).on(article.deleted.eq(false))
				.where(plannedIncomeOrExpense.task.id.eq(Id)
						.and(plannedIncomeOrExpense.company.users.contains(JPAExpressions.select(secUser).from(secUser).where(secUser.name.eq(Expressions.constant(currentUser)))))
						.and(plannedIncomeOrExpense.deleted.eq(false)))
				.fetch());
			JPAQuery<?> querySubTasks = new JPAQuery<Void>(entityManager);
			QTaskRelation taskRelation = QTaskRelation.taskRelation;
			List<Long> subTasks = querySubTasks.select(taskRelation.toTask.id).from(taskRelation)
					.where(taskRelation.fromTask.id.eq(Id)
							.and(taskRelation.toTask.id.isNotNull())
							.and(taskRelation.relation.code.eq(TaskRelationType.TASK_RELATION_TYPE_SUBTASK))
							.and(taskRelation.deleted.eq(false)))
					.fetch();
			result.addAll(reportSubTasksPlannedIncomeOrExpense(subTasks));
		}
		return result;
	}


	public List<Tuple> getViewGoodMeasures(){

		JPAQuery<?> query = new JPAQuery<Void>(entityManager);
		QCGoods cGoods = QCGoods.cGoods;
		QCMeasure cMeasure = QCMeasure.cMeasure;

		// Out mask is not available for now
		List<Tuple> v = query.select(cGoods.id, cGoods.nameBg, cGoods.code, cGoods.barcode, cGoods.gteId, cGoods.goodType, cGoods.outCode.id /*, cGoods.outMask*/, cGoods.minQuantity, cGoods.maxQuantity,
		cGoods.meeId, cGoods.description, cMeasure.name, cMeasure.code, cMeasure.meeId, cMeasure.cofficient, cGoods.volume,cGoods.weight,cGoods.godId)
		.from(cGoods)
		.innerJoin(cMeasure).on(cMeasure.id.eq(cGoods.meeId.id))
		.fetch();

	  return v;
	}

	


	//TODO add ACL filtering
	public List<ActualTimeDTO> reportSubTasksActualTime(List<Long> subTaskIds) {
		//get the logged user name
		String currentUser = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}
		List<ActualTimeDTO> result = new LinkedList<ActualTimeDTO>();
		//TODO check for cycles?
		for(Long Id : subTaskIds) {
			JPAQuery<?> query = new JPAQuery<Void>(entityManager);
			QTimeSheetItem timeSheetItem = QTimeSheetItem.timeSheetItem;
			QSecUser secUser = QSecUser.secUser;
			QSecUser resource = new QSecUser("resource");
			result.addAll(query.select(Projections.constructor(ActualTimeDTO.class, timeSheetItem.resource,timeSheetItem.fromTime,timeSheetItem.toTime,timeSheetItem.task))
				.from(timeSheetItem)
				.join(timeSheetItem.resource,resource).on(resource.deleted.eq(false))
				.where(timeSheetItem.task.id.eq(Id)
						.and(timeSheetItem.company.users.contains(JPAExpressions.select(secUser).from(secUser).where(secUser.name.eq(Expressions.constant(currentUser)))))
						.and(timeSheetItem.deleted.eq(false)))
				.fetch());
			JPAQuery<?> querySubTasks = new JPAQuery<Void>(entityManager);
			QTaskRelation taskRelation = QTaskRelation.taskRelation;
			List<Long> subTasks = querySubTasks.select(taskRelation.toTask.id)
					.from(taskRelation)
					.where(taskRelation.fromTask.id.eq(Id)
							.and(taskRelation.toTask.id.isNotNull())
							.and(taskRelation.relation.code.eq(TaskRelationType.TASK_RELATION_TYPE_SUBTASK))
							.and(taskRelation.deleted.eq(false)))
					.fetch();
			result.addAll(reportSubTasksActualTime(subTasks));
		}
		return result;
	}

	//TODO add ACL filtering
	public List<ActualRevenuesAndExpensesDTO> reportSubTasksActualRevenuesAndExpenses(List<Long> subTaskIds) {
		//get the logged user name
		String currentUser = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}
		List<ActualRevenuesAndExpensesDTO> result = new LinkedList<ActualRevenuesAndExpensesDTO>();
		//TODO check for cycles?
		for(Long Id : subTaskIds) {
			logger.trace("reportSubTasksActualRevenuesAndExpenses fetch for id: "+Id);
			JPAQuery<?> query = new JPAQuery<Void>(entityManager);
			QTaskAttachment taskAttachment = QTaskAttachment.taskAttachment;
			QAttachable attachable = QAttachable.attachable;
			QAttachableRevenuesAndExpenses attachableRevenuesAndExpenses = QAttachableRevenuesAndExpenses.attachableRevenuesAndExpenses;
			QArticle article = QArticle.article;
			QSecUser secUser = QSecUser.secUser;
			result.addAll(query.select(Projections.constructor(ActualRevenuesAndExpensesDTO.class, attachableRevenuesAndExpenses.article,attachableRevenuesAndExpenses.ammount,taskAttachment.task,taskAttachment.description))
				.from(taskAttachment)
				.join(taskAttachment.attachment, attachable).on(attachable.deleted.eq(false))
				.join(attachable.attachableRevenuesAndExpenses, attachableRevenuesAndExpenses).on(attachableRevenuesAndExpenses.deleted.eq(false))
				.join(attachableRevenuesAndExpenses.article, article).on(article.deleted.eq(false))
				.where(taskAttachment.task.id.eq(Id)
						.and(taskAttachment.company.users.contains(JPAExpressions.select(secUser).from(secUser).where(secUser.name.eq(Expressions.constant(currentUser)))))
						.and(taskAttachment.deleted.eq(false)))
				.fetch());
			JPAQuery<?> querySubTasks = new JPAQuery<Void>(entityManager);
			QTaskRelation taskRelation = QTaskRelation.taskRelation;
			List<Long> subTasks = querySubTasks.select(taskRelation.toTask.id)
					.from(taskRelation)
					.where(taskRelation.fromTask.id.eq(Id)
							.and(taskRelation.toTask.id.isNotNull())
							.and(taskRelation.relation.code.eq(TaskRelationType.TASK_RELATION_TYPE_SUBTASK))
							.and(taskRelation.deleted.eq(false)))
					.fetch();
			result.addAll(reportSubTasksActualRevenuesAndExpenses(subTasks));
		}
		logger.trace("reportSubTasksActualRevenuesAndExpenses return list count: "+result.size());
		return result;
	}
	
	public void offerTrack(Long offrId, String pUser, LoiOfferStatus status, String remark/*,DroolsRepositoryValidator droolsRepositoryValidator*/){
		JPAQuery<?> query = new JPAQuery<Void>(entityManager);

		COfferRepository offerRepo = ((COfferRepository) getRepositories().getRepositoryFor(COffer.class).get());
		COffer toUpdate = entityManager.getReference(COffer.class, offrId);

		COfferStatusRepository offerStatRepo = ((COfferStatusRepository) getRepositories().getRepositoryFor(COfferStatus.class).get());
		// currentUser
		COfferStatus offerStat = new COfferStatus(toUpdate, toUpdate.getStatus(), status, remark);
		offerStatRepo.save(offerStat);

		toUpdate.setStatus(status);	
		//droolsRepositoryValidator.validate(toUpdate, null);
		offerRepo.save(toUpdate);
	}
	
	//TODO add ACL filtering
	public List<Object[]> getWasteQuantities(){
		//get the logged user name
		String currentUser = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}
		@SuppressWarnings("unchecked")
		List<Object[]> queryResult = entityManager.createNativeQuery("select \n" +
				"( select sum(generated_amount - processed_amount) from waste \n" + 
				"INNER JOIN sec_user AS sec_user_company ON sec_user_company.company_id = waste.company_id AND sec_user_company.name=:currentUser\n" + 
				"where waste.deleted = false AND date_part('year', generated_date) < date_part('year', now())) as prevPresent,\n" +
				"sum(generated_amount) as currGenerated, \n" +
				"sum(processed_amount) as currProcessed,\n" +
				"( select sum(generated_amount - processed_amount) from waste \n" + 
				"INNER JOIN sec_user AS sec_user_company ON sec_user_company.company_id = waste.company_id AND sec_user_company.name=:currentUser\n" + 
				"WHERE waste.deleted = false) as currPresent\n" +
				"from waste\n" +
				"INNER JOIN sec_user AS sec_user_company ON sec_user_company.company_id = waste.company_id AND sec_user_company.name=:currentUser\n" + 
				"where waste.deleted = false AND date_part('year', generated_date) = date_part('year', now()) \n" +
				"AND date_part('year', processed_date) = date_part('year', now())")
				.setParameter("currentUser", currentUser)
				.getResultList();

		return queryResult;
	}

	//TODO add ACL filtering
	public List<Object[]> timeSheetsPerDay(Date fromDate, Date toDate){
		//get the logged user name
		String currentUser = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}
		@SuppressWarnings("unchecked")
		List<Object[]> queryResult = entityManager.createNativeQuery("SELECT\n" + 
				"	employee.name AS employee_name,\n" + 
				"	job_position.name AS job_position_name,\n" + 
				"	company_department.name AS company_department_name,\n" + 
				"	time_sheet_item_type.code,\n" + 
				"	CAST(time_sheet_item.from_time AS DATE) AS from_time,\n" + 
				"	EXTRACT(EPOCH FROM SUM(time_sheet_item.to_time - time_sheet_item.from_time)) AS total_time\n" + 
				"FROM \n" + 
				"	time_sheet_item\n" + 
				"	LEFT JOIN sec_user ON sec_user.id = time_sheet_item.resource_id AND sec_user.deleted = false\n" + 
				"	LEFT JOIN time_sheet_item_type ON time_sheet_item_type.id = time_sheet_item.time_sheet_item_type_id AND time_sheet_item_type.deleted = false\n" + 
				"	LEFT JOIN employee ON employee.sec_user_id = sec_user.id AND employee.deleted = false\n" + 
				"	LEFT JOIN job_position ON job_position.id = employee.position_id AND job_position.deleted = false\n" + 
				"	LEFT JOIN company_department ON company_department.id = employee.department_id AND company_department.deleted = false\n" + 
				"	INNER JOIN sec_user AS sec_user_company ON sec_user_company.company_id = time_sheet_item.company_id AND sec_user_company.name=:currentUser\n" + 
				"WHERE time_sheet_item.deleted = false AND time_sheet_item.from_time <= :toDate AND time_sheet_item.to_time >= :fromDate\n" + 
				"GROUP BY employee.name,job_position.name,company_department.name,time_sheet_item_type.code,CAST(time_sheet_item.from_time AS DATE)")
				.setParameter("currentUser", currentUser)
				.setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate)
				.getResultList();

		return queryResult;
	}

	//TODO add ACL filtering
	public List<Object[]> reportRequestOfferContractOpportunityAnalysis(Date fromDate) {
		//get the logged user name
		String currentUser = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}
		//TODO change to named query
		@SuppressWarnings("unchecked")
		List<Object[]> queryResult = entityManager.createNativeQuery("SELECT request_task.id AS request_task_id,request_task.title AS request_task_title\n" + 
				",request_task.status_name AS request_task_status_name,request_task.status_terminal AS request_task_status_terminal\n" + 
				",request_task.plan_sum AS request_task_plan_sum,request_task.counter_party AS request_counter_party,request_task.assigned AS request_assigned\n" + 
				",offer_task.id AS offer_task_id,offer_task.title AS offer_task_title\n" + 
				",offer_task.status_name AS offer_task_status_name,offer_task.status_terminal AS offer_task_status_terminal\n" + 
				",offer_task.plan_sum AS offer_task_plan_sum,offer_task.counter_party AS offer_counter_party,offer_task.assigned AS offer_assigned\n" + 
				",contract_task.id AS contract_task_id,contract_task.title AS contract_task_title\n" + 
				",contract_task.status_name AS contract_task_status_name,contract_task.status_terminal AS contract_task_status_terminal\n" + 
				",contract_task.plan_sum AS contract_task_plan_sum,contract_task.counter_party AS contract_counter_party,contract_task.assigned AS contract_assigned\n" + 
				"FROM (SELECT task.id,task.title,task_status.name status_name,task_status.terminal status_terminal\n" + 
				",SUM(plan.ammount*price.price) AS plan_sum,legal_person.name AS counter_party,sec_user.name AS assigned\n" + 
				"FROM task\n" + 
				"INNER JOIN sec_user AS sec_user_company ON sec_user_company.company_id = task.company_id AND sec_user_company.name=:currentUser\n" + 
				"INNER JOIN task_type ON task_type.id = task.type_id AND task_type.code=:requestType\n" + 
				"LEFT JOIN task_status ON task_status.id = task.status_id\n" + 
				"LEFT JOIN planned_income_or_expense AS plan ON plan.task_id = task.id AND plan.deleted = false\n" + 
				"LEFT JOIN article_price_rate AS price ON price.article_id = plan.article_id AND price.deleted = false\n" + 
				"AND price.valid_from_date<=current_date AND price.valid_to_date>=current_date\n" + 
				"LEFT JOIN legal_person ON legal_person.id = task.counter_party_id AND legal_person.deleted = false\n" + 
				"LEFT JOIN sec_user ON sec_user.id = task.assigned_id AND sec_user.deleted = false\n" + 
				"WHERE task.last_modified_date>=:fromDate AND task.deleted = false\n" + 
				"GROUP BY task.id,task.title,task_status.name,task_status.terminal,legal_person.name,sec_user.name\n" + 
				") AS request_task\n" + 
				"LEFT JOIN (SELECT request_task_relation.*\n" + 
				"FROM task_relation AS request_task_relation \n" + 
				"INNER JOIN sec_user AS sec_user_company ON sec_user_company.company_id = request_task_relation.company_id AND sec_user_company.name=:currentUser\n" + 
				"INNER JOIN task AS offer_task ON offer_task.id = request_task_relation.to_task_id AND offer_task.deleted = false\n" + 
				"INNER JOIN task_type AS offer_task_type ON offer_task_type.id = offer_task.type_id AND offer_task_type.code=:offerType\n" + 
				"WHERE request_task_relation.deleted = false\n" + 
				") AS request_task_relation ON request_task_relation.from_task_id = request_task.id\n" + 
				"FULL JOIN (SELECT task.id,task.title,task_status.name status_name,task_status.terminal status_terminal\n" + 
				",SUM(plan.ammount*price.price) AS plan_sum,legal_person.name AS counter_party,sec_user.name AS assigned\n" + 
				"FROM task\n" + 
				"INNER JOIN sec_user AS sec_user_company ON sec_user_company.company_id = task.company_id AND sec_user_company.name=:currentUser\n" + 
				"INNER JOIN task_type ON task_type.id = task.type_id AND task_type.code=:offerType\n" + 
				"LEFT JOIN task_status ON task_status.id = task.status_id\n" + 
				"LEFT JOIN planned_income_or_expense AS plan ON plan.task_id = task.id AND plan.deleted = false\n" + 
				"LEFT JOIN article_price_rate AS price ON price.article_id = plan.article_id AND price.deleted = false\n" + 
				"AND price.valid_from_date<=current_date AND price.valid_to_date>=current_date\n" + 
				"LEFT JOIN legal_person ON legal_person.id = task.counter_party_id AND legal_person.deleted = false\n" + 
				"LEFT JOIN sec_user ON sec_user.id = task.assigned_id AND sec_user.deleted = false\n" + 
				"WHERE task.last_modified_date>=:fromDate AND task.deleted = false\n" + 
				"GROUP BY task.id,task.title,task_status.name,task_status.terminal,legal_person.name,sec_user.name\n" + 
				") AS offer_task ON offer_task.id = request_task_relation.to_task_id\n" + 
				"LEFT JOIN (SELECT offer_task_relation.*\n" + 
				"FROM task_relation AS offer_task_relation \n" + 
				"INNER JOIN sec_user AS sec_user_company ON sec_user_company.company_id = offer_task_relation.company_id AND sec_user_company.name=:currentUser\n" + 
				"INNER JOIN task AS contract_task ON contract_task.id = offer_task_relation.to_task_id AND contract_task.deleted = false\n" + 
				"INNER JOIN task_type AS contract_task_type ON contract_task_type.id = contract_task.type_id AND contract_task_type.code=:contractType\n" + 
				"WHERE offer_task_relation.deleted = false) AS offer_task_relation ON offer_task_relation.from_task_id = offer_task.id\n" + 
				"FULL JOIN (SELECT task.id,task.title,task_status.name status_name,task_status.terminal status_terminal\n" + 
				",SUM(plan.ammount*price.price) AS plan_sum,legal_person.name AS counter_party,sec_user.name AS assigned\n" + 
				"FROM task\n" + 
				"INNER JOIN sec_user AS sec_user_company ON sec_user_company.company_id = task.company_id AND sec_user_company.name=:currentUser\n" + 
				"INNER JOIN task_type ON task_type.id = task.type_id AND task_type.code=:contractType\n" + 
				"LEFT JOIN task_status ON task_status.id = task.status_id\n" + 
				"LEFT JOIN planned_income_or_expense AS plan ON plan.task_id = task.id AND plan.deleted = false\n" + 
				"LEFT JOIN article_price_rate AS price ON price.article_id = plan.article_id AND price.deleted = false\n" + 
				"AND price.valid_from_date<=current_date AND price.valid_to_date>=current_date\n" + 
				"LEFT JOIN legal_person ON legal_person.id = task.counter_party_id AND legal_person.deleted = false\n" + 
				"LEFT JOIN sec_user ON sec_user.id = task.assigned_id AND sec_user.deleted = false\n" + 
				"WHERE task.last_modified_date>=:fromDate AND task.deleted = false\n" + 
				"GROUP BY task.id,task.title,task_status.name,task_status.terminal,legal_person.name,sec_user.name\n" + 
				") AS contract_task ON contract_task.id = offer_task_relation.to_task_id")
				.setParameter("requestType", TaskType.TASK_TYPE_REQUEST_FOR_OFFER)
				.setParameter("offerType", TaskType.TASK_TYPE_PREPARE_OFFER)
				.setParameter("contractType", TaskType.TASK_TYPE_PREPARE_CONTRACT)
				.setParameter("fromDate", fromDate)
				.setParameter("currentUser", currentUser)
				.getResultList();
		return queryResult;
	}

	//TODO add ACL filtering
	@SuppressWarnings("unchecked")
	public List<Object> findRevisions(String entityName, Long id) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.isAuthenticated()) {
			boolean isAuth = false;
			for (GrantedAuthority auth : authentication.getAuthorities()) {
				if (("ROLE_GET_CHANGE_HISTORY").equalsIgnoreCase(auth.getAuthority()))
					isAuth = true;
			}
			if(!isAuth) {
				throw new org.springframework.security.access.AccessDeniedException("User doesn't have authority: "+"ROLE_GET_CHANGE_HISTORY");
			}
		}
		
		Class<?> type;
		try {
			type = getClassFromRestUriOrClassName(mappings, entityName);
			logger.trace("findRevisions for class: "+type.toString());
			AuditReader reader = AuditReaderFactory.get(entityManager);
			return reader.createQuery()
				.forRevisionsOfEntity( type, true, true )
				.add(AuditEntity.property( "id" ).eq( id ))
				.getResultList();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Object findLastRevision(String entityName, Long id) {
		Class<?> type;
		try {
			type = getClassFromRestUriOrClassName(mappings, entityName);
			logger.trace("findLastRevision for class: "+type.toString()+" with ID "+id);
			AuditReader reader = AuditReaderFactory.get(entityManager);
			return reader.createQuery()
				.forRevisionsOfEntity( type, true, true )
				.add(AuditEntity.property( "id" ).eq( id ))
				.addOrder(AuditEntity.revisionNumber().desc())
				.getResultList()
				.get(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Expression<Boolean> aclRestriction(CriteriaBuilder builder, CriteriaQuery query, From join) {
		//get the logged user name
		String currentUser = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}

		//if someone has explicit ALLOW read access, entity is assumed private
		Subquery<Boolean> subquery_somebody_has_true = query.subquery(Boolean.class);
		Root<AccessControl> root_somebody_has_true = subquery_somebody_has_true.from(AccessControl.class);
		subquery_somebody_has_true.where(builder.and(
				builder.equal(root_somebody_has_true.get("deleted"),false),
				builder.equal(root_somebody_has_true.get("commonRecordId"), join.get("id")), //for this entity
				builder.equal(root_somebody_has_true.get("read"), true) //ALLOW read permission
			)
		);
		subquery_somebody_has_true.select(root_somebody_has_true.get("read"));
		
		//the user has explicit ALLOW read access
		Subquery<Boolean> subquery_user_ALLOW_read_perm = query.subquery(Boolean.class);
		Root<AccessControl> root_user_ALLOW_read_perm = subquery_user_ALLOW_read_perm.from(AccessControl.class);
		subquery_user_ALLOW_read_perm.where(builder.and(
				builder.equal(root_user_ALLOW_read_perm.get("deleted"),false),
				builder.equal(root_user_ALLOW_read_perm.get("commonRecordId"), join.get("id")), //for this entity
				builder.equal(root_user_ALLOW_read_perm.get("read"), true) //ALLOW read permission
			)
		);
		//TODO Try to do this as a static check
		Join<AccessControl, SecUser> joinSecUserALLOW = root_user_ALLOW_read_perm.join("secUser",JoinType.INNER);
		joinSecUserALLOW.on(builder.and(
				builder.equal(joinSecUserALLOW.get("deleted"),false),
				builder.equal(joinSecUserALLOW.get("name"), currentUser) //for current user
			)
		);
		subquery_user_ALLOW_read_perm.select(root_user_ALLOW_read_perm.get("read"));
		

		//the user has explicit DENY read access
		Subquery<Boolean> subquery_user_DENY_read_perm = query.subquery(Boolean.class);
		Root<AccessControl> root_user_DENY_read_perm = subquery_user_DENY_read_perm.from(AccessControl.class);
		subquery_user_DENY_read_perm.where(builder.and(
				builder.equal(root_user_DENY_read_perm.get("deleted"),false),
				builder.equal(root_user_DENY_read_perm.get("commonRecordId"), join.get("id")), //for this entity
				builder.equal(root_user_DENY_read_perm.get("read"), false) //ALLOW read permission
			)
		);
		//TODO Try to do this as a static check
		Join<AccessControl, SecUser> joinSecUserDENY = root_user_DENY_read_perm.join("secUser",JoinType.INNER);
		joinSecUserDENY.on(builder.and(
				builder.equal(joinSecUserDENY.get("deleted"),false),
				builder.equal(joinSecUserDENY.get("name"), currentUser) //for current user
			)
		);
		subquery_user_DENY_read_perm.select(root_user_DENY_read_perm.get("read"));

		//the user has GET_ALL permission
		JPAQuery<String> jpaquery_user_get_all = new JPAQuery<String>(entityManager);
		QSecUser secUser = QSecUser.secUser;
		QSecUserRole secUserRole = QSecUserRole.secUserRole;
		QSecRole secRole = QSecRole.secRole;
		QSecRolePermission secRolePermission = QSecRolePermission.secRolePermission;
		QSecPermission secPermission = QSecPermission.secPermission;
		String permissionCode = jpaquery_user_get_all.select(secPermission.code)
			.from(secUser)
			.innerJoin(secUser.roles, secUserRole)
			.innerJoin(secUserRole.role, secRole)
			.innerJoin(secRole.permissions, secRolePermission)
			.innerJoin(secRolePermission.permission, secPermission)
			.where(secUser.deleted.isFalse()
					.and(secUserRole.deleted.isFalse())
					.and(secRole.deleted.isFalse())
					.and(secRolePermission.deleted.isFalse())
					.and(secPermission.deleted.isFalse())
					.and(secUser.name.eq(currentUser)) //for current user
					.and(secPermission.code.upper().contains(Expressions.constant("ROLE_GET_ALL_"+join.getJavaType().getSimpleName().toUpperCase()))))
			.fetchFirst();
		if(permissionCode != null) { //if user has GET_ALL permission, we don't need other checks
			return null;
		}
		
		return builder.or(
				builder.exists(subquery_user_ALLOW_read_perm), //has explicit ALLOW permission
				builder.not(builder.or(
					builder.exists(subquery_somebody_has_true), //entity is private
					builder.exists(subquery_user_DENY_read_perm) //has explicit DENY permission
				))
			);
	}
	
	private String getFromsForPath(From[] lastJoins /*out*/, String selectedEntity, Map<String,From[]> joinsCache, CriteriaBuilder builder, CriteriaQuery query0, CriteriaQuery query1) {
		String[] selectedEntityPath = selectedEntity.split("\\."); //dot-path
		String path = "";
		logger.trace("Full path has "+selectedEntityPath.length+" joins");
		for(int pathIndex = 0; pathIndex < selectedEntityPath.length; pathIndex++) {
			if(pathIndex == 0) {
				path = selectedEntityPath[pathIndex];
			} else {
				path = path + "." + selectedEntityPath[pathIndex];
			}
			logger.trace("path: "+path);
			From[] joins = joinsCache.get(path);
			if(joins == null) {
				try {
					logger.trace("Creating new joins: "+selectedEntityPath[pathIndex]);
					//TODO check if fetch will improve performance, skipping the additional loads for the entities
					Join leftJoin = lastJoins[0].join(selectedEntityPath[pathIndex],JoinType.LEFT);
					//check access permissions
					hasGetPermissionForEntity(leftJoin.getModel().getBindableJavaType());
					//filter deleted rows
					Predicate predDeleted0;
					Expression<Boolean> acl0 = aclRestriction(builder, query0, leftJoin);
					if(acl0 == null) {
						predDeleted0 = builder.isFalse(leftJoin.get("deleted"));
					} else {
						predDeleted0 = builder.and(acl0,builder.isFalse(leftJoin.get("deleted")));
					}
					leftJoin.on(predDeleted0);
	
					Join leftJoinCount = lastJoins[1].join(selectedEntityPath[pathIndex],JoinType.LEFT);
					//filter deleted rows
					Predicate predDeleted1;
					Expression<Boolean> acl1 = aclRestriction(builder, query1, leftJoinCount);
					if(acl1 == null) {
						predDeleted1 = builder.isFalse(leftJoinCount.get("deleted"));
					} else {
						predDeleted1 = builder.and(acl1,builder.isFalse(leftJoinCount.get("deleted")));
					}
					leftJoinCount.on(predDeleted1);
					
					joins = new Join[]{ leftJoin, leftJoinCount };
					joinsCache.put(path, joins);
				} catch(BasicPathUsageException e) {
					//not a table join, but field - return it
					return selectedEntityPath[pathIndex];
				}
			}
			//one by one because we want to send it as out param
			lastJoins[0] = joins[0];
			lastJoins[1] = joins[1];
		}
		return "";
	}
	
	//TESTS: NOT ANYMORE curl --url https://localhost:8443/api/reports/builder -k --header "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTUyOTY1NzIzN30.xXU4vnvLcVm4eg27cEjxsMOAP9TRPKWKxaOESyYFQTbwHwW39TiioO_Pvfd_TMfM_5Cvbp0HmlROCQmseVUHzw" -H 'Content-Type: application/json' -d "{\"page\": \"1\", \"size\": \"5\", \"sort\": [{\"direction\": \"ASC\", \"property\": \"id\"}], \"from\": \"TaskStatus\", \"leftJoin\": \"SecUser\", \"joinPath\": \"createdBy\"}"
	// curl --url "https://localhost:8443/api/reports/builder/1?page=0&size=2&from=Comment&select=createdBy,task,task.createdBy,task.counterParty,task.counterParty.createdBy&id=366" -k --header "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTUyOTY1NzIzN30.xXU4vnvLcVm4eg27cEjxsMOAP9TRPKWKxaOESyYFQTbwHwW39TiioO_Pvfd_TMfM_5Cvbp0HmlROCQmseVUHzw" -H 'Content-Type: application/json'
	// curl --url "https://localhost:8443/api/reports/builder/1?page=0&size=2&from=Task&select=createdBy,counterParty,counterParty.createdBy" -k --header "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTUyOTY1NzIzN30.xXU4vnvLcVm4eg27cEjxsMOAP9TRPKWKxaOESyYFQTbwHwW39TiioO_Pvfd_TMfM_5Cvbp0HmlROCQmseVUHzw" -H 'Content-Type: application/json'
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page<javax.persistence.Tuple> reportBuilder(Pageable pageable, MultiValueMap<String, String> params) {
		Class<?> entityClass;
		try {
			//build the query
			Metamodel metamodel = entityManager.getMetamodel();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<javax.persistence.Tuple> criteriaQuery = builder.createTupleQuery();
			CriteriaQuery<javax.persistence.Tuple> countQuery = builder.createTupleQuery();
			List<Selection<?>> selections = new LinkedList<Selection<?>>();
			List<Expression<?>> groupByExpressions = new LinkedList<Expression<?>>();
			List<Expression<?>> countGroupByExpressions = new LinkedList<Expression<?>>();
			
			//set from
			entityClass = getClassFromRestUriOrClassName(mappings, params.getFirst("from"));
			EntityType entityType = metamodel.entity(entityClass);
			
			//check access permissions
			hasGetPermissionForEntity(entityClass);
			
			Root root = criteriaQuery.from( entityType );
			Root rootCount = countQuery.from( entityType );
			
			//filter by company by inner-joining it
			if(CompanyRecord.class.isAssignableFrom(entityClass)) {
				//get the logged user name
				String currentUser = null;
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if (authentication != null && authentication.isAuthenticated()) {
					currentUser = authentication.getPrincipal().toString();
				}
				Join rootCompanyUser = root.join("company",JoinType.INNER).join("users",JoinType.INNER);
				rootCompanyUser.on(builder.equal(rootCompanyUser.get("name"), currentUser));
				Join rootCountCompanyUser = rootCount.join("company",JoinType.INNER).join("users",JoinType.INNER);
				rootCountCompanyUser.on(builder.equal(rootCountCompanyUser.get("name"), currentUser));
			}
			
			//set joins
			Map<String,From[]> joinsCache = new HashMap<String,From[]>();
			joinsCache.put(params.getFirst("from"), new From[]{ root, rootCount });
			
			//set selected expressions
			logger.trace("Start selecting expressions and setting joins");
			Map<String,Expression[]> aliasCache = new HashMap<String,Expression[]>();
			String selectParam = params.getFirst("select");
			if(selectParam != null) {
				for(String selectedEntity : selectParam.split(",")) { //comma-separated list of paths 
					logger.trace("selectedEntity: "+selectedEntity);
					if(selectedEntity.length() > 0) {
						if(selectedEntity.contains("(")) { //bracket -> aggregate
							int colonPos = selectedEntity.lastIndexOf(':');
							String expr;
							String alias;
							if(colonPos >= 0) {
								expr = selectedEntity.substring(0,colonPos);
								alias = selectedEntity.substring(colonPos+1);
							} else {
								expr = selectedEntity;
								alias = selectedEntity;
							}
							String[] param = {expr};
							Expression[] aggregateSelection = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, param, null);
							if(aggregateSelection != null) {
								aggregateSelection[0].alias(alias);
								selections.add(aggregateSelection[0]);
								logger.trace("Cache alias: "+alias+"; "+expr);
								aliasCache.put(alias,aggregateSelection);
							}
						} else {
							From[] lastJoins = new From[]{ root, rootCount };
							String field = getFromsForPath(lastJoins, selectedEntity, joinsCache, builder, criteriaQuery, countQuery);
							if("".equalsIgnoreCase(field)) {
								//it was a table join (entity select)
								String alias = selectedEntity.replaceAll("\\.", "_dot_");
								logger.trace("Add entity alias: "+alias+"; "+lastJoins[0].getClass());
								selections.add(lastJoins[0].alias(alias));
								//logger.trace("Cache alias: "+alias+"; "+lastJoins[0].getClass());
								//aliasCache.put(alias,lastJoins); //put only aggregates or else find how to mark which is aggregate (check the only aliasCache.get in this code!)
								
								//now set the group-by expressions for this join
								EntityType joinEntityType = metamodel.entity(lastJoins[0].getModel().getBindableJavaType());
								for(Object attr : joinEntityType.getAttributes()) {
									if(attr instanceof SingularAttribute) {
										logger.trace("SingularAttribute: "+((SingularAttribute) attr).getName());
										groupByExpressions.add(lastJoins[0].get((SingularAttribute) attr));
										countGroupByExpressions.add(lastJoins[1].get((SingularAttribute) attr));
									} else if(attr instanceof PluralAttribute) {
										logger.trace("Skipping PluralAttribute: "+((PluralAttribute) attr).getName());
									} else {
										logger.trace("Skipping attribute: "+attr);
									}
								}
								
								addJoinInheritanceTypesFieldsToGroupBy(joinEntityType, metamodel, groupByExpressions, countGroupByExpressions, lastJoins);
							} else {
								//field selected
								selections.add(lastJoins[0].get(field));
								//Expression[] fields = {lastJoins[0].get(field),lastJoins[1].get(field)};
								//logger.trace("Cache alias: "+selectedEntity+"; "+selectedEntity);
								//aliasCache.put(selectedEntity,fields); //put only aggregates or else find how to mark which is aggregate (check the only aliasCache.get in this code!)
								groupByExpressions.add(lastJoins[0].get(field));
								countGroupByExpressions.add(lastJoins[1].get(field));
							}
						}
					}
				}
			}
			
			//set where clause to be deleted check
			Expression<Boolean> criteriaWhere = builder.or(builder.isNull(root.get("id")),builder.equal(root.get("deleted"),false));
			Expression<Boolean> countWhere = builder.or(builder.isNull(rootCount.get("id")),builder.equal(rootCount.get("deleted"),false));
			//add ACL if needed
			Expression<Boolean> acl0 = aclRestriction(builder, criteriaQuery, root);
			if(acl0 != null) {
				criteriaWhere = builder.and(criteriaWhere, acl0);
			}
			Expression<Boolean> acl1 = aclRestriction(builder, countQuery, rootCount);
			if(acl1 != null) {
				countWhere = builder.and(countWhere, acl1);
			}
			
			//and having clause
			Expression<Boolean> having = null;
			Expression<Boolean> havingCount = null;
			

			//set where clause expression
			logger.trace("Start where clause");
			String whereParam = params.getFirst("where");
			if(whereParam != null) {
				logger.trace("whereParam: "+whereParam);

				String[] param = {whereParam};
				Expression[] whereExpression = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, param, Boolean.class);
				if(whereExpression != null) {
					if(whereExpression[0] != null) {
						criteriaWhere = builder.and(criteriaWhere, whereExpression[0]);
					} else {
						logger.error("Where expression is null!");
					}
					if(whereExpression[1] != null) {
						countWhere = builder.and(countWhere, whereExpression[1]);
					} else {
						logger.error("Where expression is null!");
					}
				}
			}
			//set having clause expression
			logger.trace("Start having clause");
			String havingParam = params.getFirst("having");
			if(havingParam != null) {
				logger.trace("havingParam: "+havingParam);

				String[] param = {havingParam};
				Expression[] havingExpression = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, param, Boolean.class);
				if(havingExpression != null) {
					if(havingExpression[0] != null) {
						having = having == null ? havingExpression[0] : builder.and(having, havingExpression[0]);
					} else {
						logger.error("Having expression is null!");
					}
					if(havingExpression[1] != null) {
						havingCount = havingCount == null ? havingExpression[1] : builder.and(havingCount, havingExpression[1]);
					} else {
						logger.error("Having expression is null!");
					}
				}
			}
			
			for(Entry<String, List<String>> param : params.entrySet()) {
				String key = param.getKey();
				logger.trace("Where clause?: "+key);
				String field = key;
				From[] lastJoins = new From[]{ root, rootCount };
				int dotPos = key.lastIndexOf(".");
				int bracketPos = key.lastIndexOf("(");
				//if this field is has a path that needs to be joined
				if(dotPos >= 0 && bracketPos == -1) { // dot -> it has path; bracket -> it is calculation/aggregation, don't touch it
					String fieldPath = key.substring(0,dotPos);
					if(fieldPath.length() > 0) {
						getFromsForPath(lastJoins, fieldPath, joinsCache, builder, criteriaQuery, countQuery);
					}
					field = key.substring(dotPos+1);
				}
				logger.trace("Where clause field?: "+field);
				if(lastJoins != null) {
					logger.trace("joins != null");
					try {
						Expression<Boolean> paramRestriction = null;
						Expression<Boolean> paramRestrictionCount = null;
						Expression[] cachedAlias = aliasCache.get(key);
						if(field.equalsIgnoreCase("dtype")) {
							logger.trace("Special filter: dtype");
							try {
								SessionFactoryImpl sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
								EntityPersister entityPersister = sessionFactory.getEntityPersister( Task.class.getPackage().getName()+"."+param.getValue().get(0) );
								int clazz_ = 0;
								if(UnionSubclassEntityPersister.class.isInstance(entityPersister)) {
									clazz_ = (Integer) ((UnionSubclassEntityPersister) entityPersister).getDiscriminatorValue();
									paramRestriction = builder.equal(lastJoins[0].type(), clazz_);
									paramRestrictionCount = builder.equal(lastJoins[1].type(), clazz_);	
								} else if(JoinedSubclassEntityPersister.class.isInstance(entityPersister)) {
									clazz_ = (Integer) ((JoinedSubclassEntityPersister) entityPersister).getDiscriminatorValue();
									paramRestriction = builder.equal(lastJoins[0].type(), clazz_);
									paramRestrictionCount = builder.equal(lastJoins[1].type(), clazz_);	
								} else {
									Class dtypeClass = getClassFromRestUriOrClassName(mappings, param.getValue().get(0));
									paramRestriction = builder.equal(lastJoins[0].type(), dtypeClass);
									paramRestrictionCount = builder.equal(lastJoins[1].type(), dtypeClass);
								}
							} catch(MappingException|ClassCastException|ClassNotFoundException e) {
								logger.error("Filter DTYPE ignored: "+key+" reason: "+e.getMessage());
							}
						} else {
							Expression fieldPath;
							Expression fieldCountPath;
							if(cachedAlias == null) {
								try {
									fieldPath = lastJoins[0].get(field);
									fieldCountPath = lastJoins[1].get(field);
								} catch(IllegalArgumentException e) {
									SingularAttribute attr = getInheritedSingularAttributeByName(field, lastJoins[0], metamodel);
									if(attr != null) {
										fieldPath = lastJoins[0].get(attr);
										fieldCountPath = lastJoins[1].get(attr);
									} else {
										throw e;
									}
								}
							} else {
								logger.trace("Using cached expression for alias: "+key);
								fieldPath = cachedAlias[0];
								fieldCountPath = cachedAlias[1];
							}
							Class fieldClass = fieldPath.getJavaType();
							logger.trace("Param type: "+fieldClass);
							if(Boolean.class == fieldClass || fieldClass == boolean.class) {
								paramRestriction = builder.equal(fieldPath, Boolean.parseBoolean(param.getValue().get(0)));
								paramRestrictionCount = builder.equal(fieldCountPath, Boolean.parseBoolean(param.getValue().get(0)));
							} else if(fieldClass == String.class) {
								paramRestriction = builder.like(builder.upper(fieldPath), builder.upper(builder.literal("%"+param.getValue().get(0)+"%")));
								paramRestrictionCount = builder.like(builder.upper(fieldCountPath), builder.upper(builder.literal("%"+param.getValue().get(0)+"%")));
							} else if(fieldClass == Long.class || fieldClass == Integer.class || fieldClass == int.class) {
								paramRestriction = builder.equal(fieldPath, param.getValue().get(0));
								paramRestrictionCount = builder.equal(fieldCountPath, param.getValue().get(0));
							} else if(fieldClass == Date.class || fieldClass == Calendar.class || fieldClass == LocalDateTime.class) {
								List<String> stringParams = param.getValue();
								SimpleDateFormat formatter=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z"); //2018/06/01 00:00:00 +0300
								Date date1 = formatter.parse(param.getValue().get(0));
								if(stringParams.size() > 1) {
									Date date2 = formatter.parse(param.getValue().get(1));
									if(date1.before(date2)) {
										paramRestriction = builder.between(fieldPath, date1, date2);
										paramRestrictionCount = builder.between(fieldCountPath, date1, date2);
									} else {
										paramRestriction = builder.between(fieldPath, date2, date1);
										paramRestrictionCount = builder.between(fieldCountPath, date2, date1);
									}
								} else {
									paramRestriction = builder.equal(fieldPath, date1);
									paramRestrictionCount = builder.equal(fieldCountPath, date1);
								}
							} else if(fieldClass == LocalDate.class) {
								List<String> stringParams = param.getValue();
								LocalDate date1 = LocalDate.parse(param.getValue().get(0)); // ISO_LOCAL_DATE '2011-12-03'
								if(stringParams.size() > 1) {
									LocalDate date2 = LocalDate.parse(param.getValue().get(1));
									if(date1.isBefore(date2)) {
										paramRestriction = builder.between(fieldPath, date1, date2);
										paramRestrictionCount = builder.between(fieldCountPath, date1, date2);
									} else {
										paramRestriction = builder.between(fieldPath, date2, date1);
										paramRestrictionCount = builder.between(fieldCountPath, date2, date1);
									}
								} else {
									paramRestriction = builder.equal(fieldPath, date1);
									paramRestrictionCount = builder.equal(fieldCountPath, date1);
								}
							} else {
								logger.error("Filter parameter type not supported for "+field);
							}
						}
						//only aggregates are with alias
						if(cachedAlias == null) {
							if(paramRestriction != null) {
								criteriaWhere = builder.and(criteriaWhere, paramRestriction);
							} else {
								logger.error("Filter is null!");
							}
							if(paramRestrictionCount != null) {
								countWhere = builder.and(countWhere, paramRestrictionCount);
							} else {
								logger.error("Filter is null!");
							}
						} else {
							if(paramRestriction != null) {
								having = having == null ? paramRestriction : builder.and(having, paramRestriction);
							} else {
								logger.error("Filter is null!");
							}
							if(paramRestrictionCount != null) {
								havingCount = havingCount == null ? paramRestrictionCount : builder.and(havingCount, paramRestrictionCount);
							} else {
								logger.error("Filter is null!");
							}
						}
						logger.trace("Where clause added!");
					} catch (IllegalArgumentException e) {
						logger.debug("Filter ignored (IllegalArgumentException): "+key);
					} catch (IllegalStateException e) {
						logger.debug("Filter ignored (IllegalStateException): "+key);
					} catch (ParseException e) {
						logger.error("Filter for date cannot be parsed from string: "+e.getMessage());
					}
				}
			}
			
			criteriaQuery.where(criteriaWhere);
			countQuery.where(countWhere);
			
			//set summary aggregates
			List<Selection<?>> aggregations = new LinkedList<Selection<?>>();
			aggregations.add(builder.literal("_summaryRow"));
			aggregations.add(builder.count(rootCount));
			String summaryParam = params.getFirst("summary");
			if(summaryParam != null) {
				for(String aggregation : summaryParam.split(",")) { //comma-separated list of paths
					int colonPos = aggregation.lastIndexOf(':');
					String expr;
					if(colonPos >= 0) {
						expr = aggregation.substring(0,colonPos);
					} else {
						expr = aggregation;
					}
					String[] param = {expr};
					Selection[] aggregateSelection = getExpressionFromParam(builder,root,rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, param, null);
					if(aggregateSelection != null) {
						if(colonPos >= 0) {
							aggregateSelection[1].alias(aggregation.substring(colonPos+1));
						}
						aggregations.add(aggregateSelection[1]);
					}
				}
			}
			
			criteriaQuery.multiselect(selections);
			countQuery.multiselect(aggregations);
			
			criteriaQuery.groupBy(groupByExpressions);
			//countQuery.groupBy(countGroupByExpressions); //why did I put this here??? summary should be 1 row only! but having filters are missing now!
			
			if(having != null) {
				criteriaQuery.having(having);
			}
			if(havingCount != null) {
				countQuery.having(havingCount);
			}
			
			if(pageable.getSort() != null) {
				List<Order> orderList = new LinkedList<Order>();
				for(org.springframework.data.domain.Sort.Order order : pageable.getSort()){
					String key = order.getProperty();
					if(key != null) {
						String field = key;
						From[] joins = new From[]{ root, rootCount };
						int dotPos = key.lastIndexOf(".");
						if(dotPos >= 0) {
							joins = joinsCache.get(key.substring(0,dotPos));
							field = key.substring(dotPos+1);
						}
						try {
							if(order.isAscending()) {
								orderList.add(builder.asc(joins[0].get(field)));
							} else {
								orderList.add(builder.desc(joins[0].get(field)));
							}
						} catch(java.lang.IllegalArgumentException e) {
							logger.debug("Sort order ignored: "+key);
						}
					}
				}
				criteriaQuery.orderBy(orderList);
			}
			
			Query count = entityManager.createQuery(countQuery);
			Query query = entityManager.createQuery(criteriaQuery);
			// pagination
			int resultCount = query.getResultList().size(); //TODO terrible solution, fix and use "native" solution
			//TODO a solution to get all rows in one page
			query.setFirstResult((int) pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());
			List<javax.persistence.Tuple> result = query.getResultList();
			List<javax.persistence.Tuple> summaryList = count.getResultList();
			if(logger.isTraceEnabled()) {
				logger.trace("Tuple count: "+result.size());
				for(javax.persistence.Tuple tuple: result) {
					for(TupleElement telem: tuple.getElements()) {
						logger.trace("TupleElement: "+telem.toString());
					}
					for(int i = 0; i<tuple.getElements().size(); i++) {
						logger.trace("Element value: "+tuple.get(i));
					}
				}
				for(TupleElement telem: summaryList.get(0).getElements()) {
					logger.trace("SummaryElement: "+telem.toString());
				}
				for(int i = 0; i<summaryList.get(0).getElements().size(); i++) {
					logger.trace("Element value: "+summaryList.get(0).get(i));
				}
			}
			//TODO solution to get summary when there is groupBy
			if(summaryList.size() > 0) {
				javax.persistence.Tuple summary = summaryList.get(0);
				//export as paging specific attributes
				if(summaryParam != null) {
					result.add(summary);
				}
			}
			return PageableExecutionUtils.getPage(result, pageable, () -> resultCount);
			//return result;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void addJoinInheritanceTypesFieldsToGroupBy(EntityType joinEntityType, Metamodel metamodel, List<Expression<?>> groupByExpressions, List<Expression<?>> countGroupByExpressions, From[] lastJoins) {
		//this should be done only if the entity is annotated as inherited with union strategy
		SessionFactoryImpl sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
		EntityPersister entityPersister = sessionFactory.getEntityPersister( joinEntityType.getBindableJavaType().getName() );
		if(JoinedSubclassEntityPersister.class.isInstance(entityPersister)) {
			for(EntityType et : metamodel.getEntities()) {
				if(joinEntityType.equals(et.getSupertype())) {
					logger.trace("Inherited type: "+joinEntityType.getName()+" -> "+et.getName());
					for(Object attr : et.getAttributes()) {
						if(attr instanceof SingularAttribute) {
							logger.trace("Inherited SingularAttribute: "+((SingularAttribute) attr).getName());
							groupByExpressions.add(lastJoins[0].get((SingularAttribute) attr));
							countGroupByExpressions.add(lastJoins[1].get((SingularAttribute) attr));
						}
					}
					groupByExpressions.add(lastJoins[0].type());
					countGroupByExpressions.add(lastJoins[1].type());
					//recursive call for this type
					addJoinInheritanceTypesFieldsToGroupBy(et, metamodel, groupByExpressions, countGroupByExpressions, lastJoins);
				}
			}
		}
	}

	private SingularAttribute getInheritedSingularAttributeByName(String name, From lastJoin, Metamodel metamodel) {
		//this should be done only if the entity is annotated as inherited with union strategy
		SessionFactoryImpl sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
		EntityType joinEntityType = metamodel.entity(lastJoin.getModel().getBindableJavaType());
		EntityPersister entityPersister = sessionFactory.getEntityPersister( joinEntityType.getBindableJavaType().getName() );
		if(JoinedSubclassEntityPersister.class.isInstance(entityPersister)) {
			for(EntityType et : metamodel.getEntities()) {
				if(joinEntityType.equals(et.getSupertype())) {
					for(Object attr : et.getAttributes()) {
						if(attr instanceof SingularAttribute && name.equalsIgnoreCase(((SingularAttribute) attr).getName())) {
							logger.trace("Inherited SingularAttribute: "+joinEntityType.getName()+" -> "+et.getName()+((SingularAttribute) attr).getName());
							return (SingularAttribute) attr;
						}
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Expression<?>[] getExpressionFromParam(CriteriaBuilder builder, Root root, Root rootCount, 
			CriteriaQuery criteriaQuery, CriteriaQuery countQuery, 
			Map<String, From[]> joinsCache, Map<String,Expression[]> aliasCache,
			String[] aggregation, Class exprClass) {
		if(aggregation[0].length() > 0) {
			String aggregationField = "";
			String operation = "";
			int openBracketPos = aggregation[0].indexOf("(");
			int commaPos = aggregation[0].indexOf(";");
			int closeBracketPos = aggregation[0].indexOf(")");
			if(openBracketPos >= 0 && (commaPos < 0 || openBracketPos < commaPos) ) {
				//comma is not first and not a field (we have bracket), it is openBracket or closeBracket
				if(closeBracketPos < 0 || openBracketPos < closeBracketPos) {
					//openBracketPos is first
					aggregationField = aggregation[0].substring(openBracketPos+1, aggregation[0].length());
					operation = aggregation[0].substring(0, openBracketPos);
					
					logger.trace("Expression with operation: "+operation+" and rest is: "+aggregationField);
					String[] rest = {aggregationField};
					//literals
					if(operation.equalsIgnoreCase("stringLiteral")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, String.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = field;
						return result;
					}
					if(operation.equalsIgnoreCase("localDateLiteral")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, LocalDate.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = field;
						return result;
					}
					if(operation.equalsIgnoreCase("localDateTimeLiteral")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, LocalDateTime.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = field;
						return result;
					}
					//aggregate
					if(operation.equalsIgnoreCase("sum")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.sum(field[0]),builder.sum(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("count")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.count(field[0]),builder.count(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("avg")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.avg(field[0]),builder.avg(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("min")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.min(field[0]),builder.min(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("max")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.max(field[0]),builder.max(field[1])};
						return result;
					}
					//with 1 param
					if(operation.equalsIgnoreCase("abs")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.abs(field[0]),builder.abs(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("length")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, String.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.length(field[0]),builder.length(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("lower")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, String.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.lower(field[0]),builder.lower(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("neg")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.neg(field[0]),builder.neg(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("sqrt")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.sqrt(field[0]),builder.sqrt(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("trim")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, String.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.trim(field[0]),builder.trim(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("upper")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, String.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.upper(field[0]),builder.upper(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("isFalse")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, Boolean.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.isFalse(field[0]),builder.isFalse(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("isNotNull")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.isNotNull(field[0]),builder.isNotNull(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("isNull")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.isNull(field[0]),builder.isNull(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("isTrue")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, Boolean.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.isTrue(field[0]),builder.isTrue(field[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("not")||operation.equalsIgnoreCase("!")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, Boolean.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.not(field[0]),builder.not(field[1])};
						return result;
					}
					//with 2 params
					if(operation.equalsIgnoreCase("quot")||operation.equalsIgnoreCase("/")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.quot(field[0],param2[0]),builder.quot(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("prod")||operation.equalsIgnoreCase("*")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.prod(field[0],param2[0]),builder.prod(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("+")||operation.equalsIgnoreCase("plus")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.sum(field[0],param2[0]),builder.sum(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("diff")||operation.equalsIgnoreCase("-")||operation.equalsIgnoreCase("minus")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.diff(field[0],param2[0]),builder.diff(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("lessThan")||operation.equalsIgnoreCase("lt")||operation.equalsIgnoreCase("<")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.lessThan(field[0],param2[0]),builder.lessThan(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("lessThanOrEqualTo")||operation.equalsIgnoreCase("le")||operation.equalsIgnoreCase("<=")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.lessThanOrEqualTo(field[0],param2[0]),builder.lessThanOrEqualTo(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("greaterThan")||operation.equalsIgnoreCase("gt")||operation.equalsIgnoreCase(">")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.greaterThan(field[0],param2[0]),builder.greaterThan(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("greaterThanOrEqualTo")||operation.equalsIgnoreCase("ge")||operation.equalsIgnoreCase(">=")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.greaterThanOrEqualTo(field[0],param2[0]),builder.greaterThanOrEqualTo(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("equal")||operation.equalsIgnoreCase("=")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.equal(field[0],param2[0]),builder.equal(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("notEqual")||operation.equalsIgnoreCase("<>")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.notEqual(field[0],param2[0]),builder.notEqual(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("coalesce")||operation.equalsIgnoreCase("nvl")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.coalesce(field[0],param2[0]),builder.coalesce(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("concat")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, String.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, String.class);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.concat(field[0],param2[0]),builder.concat(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("like")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, String.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, String.class);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.like(field[0],param2[0]),builder.like(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("and")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, Boolean.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, Boolean.class);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.and(field[0],param2[0]),builder.and(field[1],param2[1])};
						return result;
					}
					if(operation.equalsIgnoreCase("or")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, Boolean.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, Boolean.class);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.or(field[0],param2[0]),builder.or(field[1],param2[1])};
						return result;
					}
					//with 3 params
					if(operation.equalsIgnoreCase("case")) {
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, Boolean.class);
						logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param2 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						Expression[] param3 = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, null);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						Expression[] result = {builder.selectCase().when(field[0],param2[0]).otherwise(param3[0]),builder.selectCase().when(field[1],param2[1]).otherwise(param3[1])};
						return result;
					}
				} else {
					//closeBracket is first
					String[] rest = {aggregation[0].substring(0, closeBracketPos)};
					Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, exprClass);
					aggregation[0] = aggregation[0].substring(closeBracketPos, aggregation[0].length());
					return field;
				}
			} else {
				//openBracket is not first, it is comma, closeBracket or field
				if(closeBracketPos >= 0 && (commaPos < 0 || closeBracketPos < commaPos) ) {
					//closeBracket is first
					String[] rest = {aggregation[0].substring(0, closeBracketPos)};
					logger.trace("Calculating till closeBracket: "+rest[0]);
					Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, exprClass);
					aggregation[0] = aggregation[0].substring(closeBracketPos, aggregation[0].length());
					return field;
				} else {
					//comma is first or field
					if(commaPos >= 0) {
						//comma is first
						String[] rest = {aggregation[0].substring(0, commaPos)};
						logger.trace("Calculating till comma: "+rest[0]);
						Expression[] field = getExpressionFromParam(builder, root, rootCount, criteriaQuery, countQuery, joinsCache, aliasCache, rest, exprClass);
						aggregation[0] = aggregation[0].substring(commaPos, aggregation[0].length());
						return field;
					} else {
						//it is field or literal
						logger.trace("Expression field or literal: "+aggregation[0]);
						aggregationField = aggregation[0];
						Expression[] field = {null,null};
						try {
							Expression[] cachedAlias = aliasCache.get(aggregationField);
							if(cachedAlias == null) {
								int dotPos = aggregationField.lastIndexOf(".");
								if(dotPos >= 0) {
									String fieldPath = aggregationField.substring(0,dotPos);
									From[] lastJoins = new From[]{ root, rootCount };
									getFromsForPath(lastJoins, fieldPath, joinsCache, builder, criteriaQuery, countQuery);
									field[0] = lastJoins[0].get(aggregationField.substring(dotPos+1));
									field[1] = lastJoins[1].get(aggregationField.substring(dotPos+1));
								} else {
									if(aggregationField.equalsIgnoreCase("*")) {
										field[0] = root;
										field[1] = rootCount;
									} else {
										field[0] = root.get(aggregationField);
										field[1] = rootCount.get(aggregationField);
									}
								}
								logger.trace("Expression is found as field: "+aggregationField);
							} else {
								logger.trace("Using cached expression for alias: "+aggregationField);
								field[0] = cachedAlias[0];
								field[1] = cachedAlias[1];
							}
						} catch(java.lang.IllegalArgumentException e2) {
							logger.trace("Expression is not found as field: "+aggregationField);
							if(exprClass == String.class) {
								field[0] = builder.literal(aggregationField);
								field[1] = builder.literal(aggregationField);
								logger.trace("Expression is cast as string literal: "+aggregationField);
							} else if(exprClass == LocalDate.class) {
								field[0] = builder.literal(LocalDate.parse(aggregationField)); // ISO_LOCAL_DATE '2011-12-03'
								field[1] = builder.literal(LocalDate.parse(aggregationField));
								logger.trace("Expression is cast as LocalDate literal: "+aggregationField);
							} else if(exprClass == LocalDateTime.class) {
								field[0] = builder.literal(LocalDateTime.parse(aggregationField)); // ISO_LOCAL_DATE_TIME '2007-12-03T10:15:30'
								field[1] = builder.literal(LocalDateTime.parse(aggregationField));
								logger.trace("Expression is cast as LocalDateTime literal: "+aggregationField);
							} else {
								try {
									double literalValue = Double.parseDouble(aggregationField);
									field[0] = builder.literal(literalValue);
									field[1] = builder.literal(literalValue);
									logger.trace("Expression is cast as double literal: "+aggregationField);
								} catch(NumberFormatException e) {
									field[0] = builder.literal(aggregationField);
									field[1] = builder.literal(aggregationField);
									logger.trace("Expression is treated as string literal: "+aggregationField);
								}
							}
						}
						aggregation[0] = "";
						return field;
					}
				}
			}
		}
		return null;
	}
}
