package bg.latona.santa.load;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.util.TypeInformation;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import bg.latona.santa.DroolsRuleException;
import bg.latona.santa.RepositoryConfiguration;
import bg.latona.santa.anvoice.AnvoiceDatabaseLoader;
import bg.latona.santa.entities.*;
import bg.latona.santa.entities.security.*;
import bg.latona.santa.reports.ReportsRepository;
import bg.latona.santa.repositories.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component //automatically picked up by @SpringBootApplication
public class DatabaseLoader implements CommandLineRunner { //gets run after all the beans are created and registered

	private static Logger logger = LoggerFactory.getLogger(DatabaseLoader.class);
	private static WebApplicationContext appContext;
	private Repositories repositories = null;
	final CamelContext camelContext;
	private final ReportsRepository reportsRepository;
	private final Environment env;

	@Autowired
	public DatabaseLoader(WebApplicationContext appContext,
			CamelContext camelContext, 
			ReportsRepository reportsRepository,
			Environment env,
			PersistentEntities persistentEntities) {
		DatabaseLoader.appContext = appContext;
		this.camelContext = camelContext;
		this.reportsRepository = reportsRepository;
		this.persistentEntities = persistentEntities;
		this.env = env;
	}

	final PersistentEntities persistentEntities;
	
	public static WebApplicationContext getAppContext() {
		return DatabaseLoader.appContext;
	}
	
	@Override
	public void run(String... strings) throws Exception {
		logger.info("Starting default data loading to database");
		Calendar createTime = Calendar.getInstance();
		createTime.set(2017,10,01,12,00);
		Date createDate = Date.from(createTime.toInstant());
		
		ManagedCompany managedCompany;
		List<ManagedCompany> existingCompanyList = (List<ManagedCompany>) ((ManagedCompanyRepository) getRepositories().getRepositoryFor(ManagedCompany.class).get()).findByCode(1L);
		if(existingCompanyList.isEmpty()) {
			managedCompany = ((ManagedCompanyRepository) getRepositories().getRepositoryFor(ManagedCompany.class).get()).save(new ManagedCompany(null,createDate,null,createDate,false,"BG Mashini",1L,"f950f2ba-d5c0-404a-b145-77e0fa027fd7"));
		} else {
			managedCompany = existingCompanyList.get(0);
		}
		
//		ManagedCompany indParts;
//		try {
//			indParts = ((ManagedCompanyRepository) getRepositories().getRepositoryFor(ManagedCompany.class).get()).save(new ManagedCompany(null,createDate,null,createDate,"Industrial Parts",2L,"7a48bce3-2164-4212-ac0b-90efca0b2555"));
//		} catch(org.springframework.dao.DataIntegrityViolationException e) {
//			indParts = ((List<ManagedCompany>) ((ManagedCompanyRepository) getRepositories().getRepositoryFor(ManagedCompany.class).get()).findByCode(2L)).get(0);
//		}
		
//		//trySave(new MailAccount(null,createDate,null,createDate,indParts,"Test Latona","latona.bg","mail.latona.bg","test@latona.bg","Lat0n@test",180000,"INBOX","INBOX.Sent",null,false,false,true,true));
//		trySave(new MailAccount(null,createDate,null,createDate,wato,"Test Nadia","imap.gmail.com","smtp.gmail.com","test.nadia@wato.bg","zapspzedep12",180000,"INBOX","[Gmail]/Sent Mail",null,false,false,true,true));
//		//trySave(new MailAccount(admin,createDate,admin,createDate,"Test Svilen","imap.gmail.com","test.svilen@wato.bg","zedepzapsp12",180000));
//		//logger.info("Mail accounts saved");
		
		dataLoad(managedCompany,false);
		//dataLoad(indParts,false);
		logger.info("Finished default data loading to database");
	}
	
	public Repositories getRepositories() {
		if (repositories == null) {
			repositories = new Repositories(appContext);
		}
		return repositories;
	}
	
	public CommonRecord trySave(CommonRecord entity) {
		try {
			RepositoryConfiguration.getBeforeCreateValidator().validate(entity, null);
			CommonRecord result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass()).get()).save(entity);
			RepositoryConfiguration.getAfterCreateValidator().validate(entity, null);
			return result;
		} catch (DroolsRuleException e) {
			if (e.getResult().getFieldErrors().size() > 0 && e.getResult().getFieldErrors().get(0).getField().equals("uniqueness")) {
				logger.debug(/*e.getStackTrace()[0].getFileName()+" "+e.getStackTrace()[0].getLineNumber()+" "+*/entity.getClass().getSimpleName()+": "+e.getMessage());
				CommonRecord rejectedValue = (CommonRecord) e.getResult().getFieldErrors().get(0).getRejectedValue();
				if ( !entity.equals(rejectedValue) ) {
					logger.info("Data in DB:\n" + rejectedValue.toString()+ "\ndiffers from the default:\n" + entity.toString());
				}
				return rejectedValue;
			} else {
				logger.debug(/*e.getStackTrace()[0].getFileName()+" "+e.getStackTrace()[0].getLineNumber()+" "+*/entity.getClass().getSimpleName()+": "+e.getMessage());
				return null;
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			logger.debug(e.getMessage());
			return null;
		}
	}
	
	public void dataLoad(ManagedCompany managedCompany, boolean isTestData) throws Exception {
		Long managedCompanyCode = managedCompany.getCode();
		Calendar createTime = Calendar.getInstance();
		createTime.set(2017,10,01,12,00);
		Date createDate = Date.from(createTime.toInstant());
		
		//the admin should be created if not already present in the system and all permissions should be assigned to him
		SecUser admin = ((SecUserRepository) getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin"+managedCompanyCode);

		if(admin == null) {
			admin = ((SecUserRepository) getRepositories().getRepositoryFor(SecUser.class).get()).save(
				new SecUser(null,createDate,null,createDate,false,managedCompany,"admin"+managedCompanyCode,"Администратор","123",null,"AD")
			);
			admin.setCreatedBy(admin);
			trySave(admin);
		}
		
		SecPermission permDelete = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Триене","ROLE_DELETE_ANY"));
		SecPermission permGet = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Преглед","ROLE_GET_ANY"));
		SecPermission permHead = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Head","ROLE_HEAD_ANY"));
		SecPermission permOptions = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Options","ROLE_OPTIONS_ANY"));
		SecPermission permPatch = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Редакция","ROLE_PATCH_ANY"));
		SecPermission permPost = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Създаване","ROLE_POST_ANY"));
		SecPermission permPut = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Put","ROLE_PUT_ANY"));
		SecPermission permTrace = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Trace","ROLE_TRACE_ANY"));

		SecPermission permHistory = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Преглед на историята на промените","ROLE_GET_CHANGE_HISTORY"));
		SecPermission permGetReports = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Преглед на справки","ROLE_GET_reports"));
		SecPermission permPostReports = (SecPermission) trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Преглед на справки с параметри","ROLE_POST_reports"));

		final RepositoryResourceMappings repositoryResourceMappings = appContext.getBean(RepositoryResourceMappings.class);
		if(repositoryResourceMappings == null) {
			throw new RuntimeException("Cannot get RepositoryResourceMappings bean");
		}
		for(TypeInformation<?> typeInfo : persistentEntities.getManagedTypes())
		{
			String rel = repositoryResourceMappings.getMetadataFor(typeInfo.getType()).getRel().value();
			trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Преглед на "+rel,"ROLE_GET_"+rel));
			trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Създаване на "+rel,"ROLE_POST_"+rel));
			trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Редакция на "+rel,"ROLE_PATCH_"+rel));
			trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Изтриване на "+rel,"ROLE_DELETE_"+rel));
			trySave(new SecPermission(admin,createDate,null,createDate,false,managedCompany,"Преглед на ВСИЧКИ "+rel,"ROLE_GET_ALL_"+typeInfo.getType().getSimpleName()));
		}
		
		
		SecRole roleAdmin = (SecRole) trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Пълен достъп",1L));
		//give all permissions
		for(SecPermission perm : ((SecPermissionRepository) getRepositories().getRepositoryFor(SecPermission.class).get()).findByCompanyAndDeleted(managedCompany, false)) {
			trySave(new SecRolePermission(admin,createDate,null,createDate,false,managedCompany,roleAdmin,perm));
		}
		
		trySave(new SecUserRole(admin,createDate,null,createDate,false,managedCompany,admin,roleAdmin));
		createTime.add(Calendar.MINUTE, 1);
		
	//	trySave(new LoiPaymentType(admin,createDate,admin,createDate,false,managedCompany,"В брой",LoiPaymentType.PAYMENT_TYPE_CASH));
	//	LoiPaymentType loiPaymentTypeBank = (LoiPaymentType) trySave(new LoiPaymentType(admin,createDate,admin,createDate,false,managedCompany,"По банков път",LoiPaymentType.PAYMENT_TYPE_WIRE_TRANSFER));
	//	trySave(new LoiPaymentType(admin,createDate,admin,createDate,false,managedCompany,"дебитна/кредитна карта",LoiPaymentType.PAYMENT_TYPE_CDC));
	//	trySave(new LoiPaymentType(admin,createDate,admin,createDate,false,managedCompany,"наложен платеж",LoiPaymentType.PAYMENT_TYPE_CD));
	//	LoiVatExemptionReason loiVatExemptionReasonNone = (LoiVatExemptionReason) trySave(new LoiVatExemptionReason(admin,createDate,admin,createDate,false,managedCompany,"Няма",LoiVatExemptionReason.VAT_EXEMPTION_REASON_NONE));
	//	trySave(new LoiVatExemptionReason(admin,createDate,admin,createDate,false,managedCompany,"Износ (чл.28, т.2 от ЗДДС)",LoiVatExemptionReason.VAT_EXEMPTION_REASON_EXPORT));
	//	trySave(new LoiVatExemptionReason(admin,createDate,admin,createDate,false,managedCompany,"Обратно начисляване (чл.21, ал.2 от ЗДДС)",LoiVatExemptionReason.VAT_EXEMPTION_REASON_REVERSE));

	//	TaskStatus statusPlanned = (TaskStatus) trySave(new TaskStatus(admin,createDate,admin,createDate,false,managedCompany,"Планирана",TaskStatus.TASK_STATUS_PLANNED,false));
	//	TaskStatus statusAssigned = (TaskStatus) trySave(new TaskStatus(admin,createDate,admin,createDate,false,managedCompany,"Възложена",TaskStatus.TASK_STATUS_ASSIGNED,false));
	//	TaskStatus statusStarted = (TaskStatus) trySave(new TaskStatus(admin,createDate,admin,createDate,false,managedCompany,"Започната",TaskStatus.TASK_STATUS_STARTED,false));
	//	TaskStatus statusFinished = (TaskStatus) trySave(new TaskStatus(admin,createDate,admin,createDate,false,managedCompany,"Приключена",TaskStatus.TASK_STATUS_FINISHED,true));
	//	TaskStatus statusFailed = (TaskStatus) trySave(new TaskStatus(admin,createDate,admin,createDate,false,managedCompany,"Неуспешна",TaskStatus.TASK_STATUS_FAILED,true));
	//	TaskStatus statusRejected = (TaskStatus) trySave(new TaskStatus(admin,createDate,admin,createDate,false,managedCompany,"Отказана",TaskStatus.TASK_STATUS_REJECTED,true));
	//	TaskStatus statusAcceptedFinish = (TaskStatus) trySave(new TaskStatus(admin,createDate,admin,createDate,false,managedCompany,"Предадена и Приета",TaskStatus.TASK_STATUS_ACCEPTED_FINISH,true));
	//	TaskStatus statusCancelled = (TaskStatus) trySave(new TaskStatus(admin,createDate,admin,createDate,false,managedCompany,"Отменена",TaskStatus.TASK_STATUS_CANCELLED,true));
	//	TaskStatus statusError = (TaskStatus) trySave(new TaskStatus(admin,createDate,admin,createDate,false,managedCompany,"Грешна",TaskStatus.TASK_STATUS_ERROR,true));
	//
	//	TaskPriority priorityLow = (TaskPriority) trySave(new TaskPriority(admin,createDate,admin,createDate,false,managedCompany,"Нисък",TaskPriority.TASK_PRIORITY_LOW));
	//	TaskPriority priorityMedium = (TaskPriority) trySave(new TaskPriority(admin,createDate,admin,createDate,false,managedCompany,"Среден",TaskPriority.TASK_PRIORITY_MEDIUM));
	//	TaskPriority priorityHigh = (TaskPriority) trySave(new TaskPriority(admin,createDate,admin,createDate,false,managedCompany,"Висок",TaskPriority.TASK_PRIORITY_HIGH));
	//	TaskPriority priorityUrgent = (TaskPriority) trySave(new TaskPriority(admin,createDate,admin,createDate,false,managedCompany,"Спешно",TaskPriority.TASK_PRIORITY_URGENT));

	//	TaskType typeGeneral = (TaskType) trySave(new TaskType(admin,createDate,admin,createDate,false,managedCompany,"Работна задача",null));
		
	//	TaskRelationType typeRelated = (TaskRelationType) trySave(new TaskRelationType(admin,createDate,admin,createDate,false,managedCompany,"свързана","свързана",TaskRelationType.TASK_RELATION_TYPE_RELATED));
	//	TaskRelationType typeChild = (TaskRelationType) trySave(new TaskRelationType(admin,createDate,admin,createDate,false,managedCompany,"подзадача","надзадача",TaskRelationType.TASK_RELATION_TYPE_SUBTASK));
	//	TaskRelationType typePlan = (TaskRelationType) trySave(new TaskRelationType(admin,createDate,admin,createDate,false,managedCompany,"план","план за",TaskRelationType.TASK_RELATION_TYPE_PLAN));
	//	TaskRelationType typeBlock = (TaskRelationType) trySave(new TaskRelationType(admin,createDate,admin,createDate,false,managedCompany,"блокира","блокирана от",TaskRelationType.TASK_RELATION_TYPE_FINISH_TO_START));
	//	TaskRelationType typeCanStart = (TaskRelationType) trySave(new TaskRelationType(admin,createDate,admin,createDate,false,managedCompany,"позволява започване","не започва преди",TaskRelationType.TASK_RELATION_TYPE_START_TO_START));
	//	TaskRelationType typeCanFinish = (TaskRelationType) trySave(new TaskRelationType(admin,createDate,admin,createDate,false,managedCompany,"позволява приключване","не приключва преди",TaskRelationType.TASK_RELATION_TYPE_START_TO_FINISH));
	//	TaskRelationType typeFinish = (TaskRelationType) trySave(new TaskRelationType(admin,createDate,admin,createDate,false,managedCompany,"приключва","се приключва от",TaskRelationType.TASK_RELATION_TYPE_FINISH_TO_FINISH));
		
		//trySave(new AllocationType(admin,createDate,admin,createDate,false,managedCompany,"Приходи към фактури","incomes","invoices"));
		
		CrmDatabaseLoader.load(this, managedCompany);
		
		//TransportDatabaseLoader.load(this, managedCompany);

		CommonDatabaseLoader.load(this, managedCompany);
		
		FinanceDatabaseLoader.load(this, managedCompany);

		SelfieDatabaseLoader.load(this, managedCompany);

		AnvoiceDatabaseLoader.load(this, managedCompany, camelContext, getAppContext(), env);

		NepalDatabaseLoader.load(this, managedCompany, getRepositories(), camelContext, reportsRepository, env);
		
		//----------------from here on it is the test data------------------------------------------------------------------
		if (!isTestData) {
			return;
		}

		CommonDatabaseLoader.loadTest(this, managedCompany);

		FinanceDatabaseLoader.loadTest(this, managedCompany);

		NepalDatabaseLoader.loadTest(this, managedCompany);

		SelfieDatabaseLoader.loadTest(this, managedCompany);

		AnvoiceDatabaseLoader.loadTest(this, managedCompany);

		//CrmDatabaseLoader.loadTest(this, managedCompany);
		
		//TransportDatabaseLoader.loadTest(this, managedCompany);
	}
}