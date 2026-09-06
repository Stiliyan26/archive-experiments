package bg.latona.santa.load;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bg.latona.santa.entities.AttachableRevenuesAndExpenses;
import bg.latona.santa.entities.BankAccount;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.*;
import bg.latona.santa.entities.dictionary.*;
import bg.latona.santa.entities.employee.*;
import bg.latona.santa.entities.mail.MailTemplate;
import bg.latona.santa.entities.offer.*;
import bg.latona.santa.entities.person.*;
import bg.latona.santa.entities.security.*;
import bg.latona.santa.entities.task.*;
import bg.latona.santa.entities.waste.*;
import bg.latona.santa.repositories.SecUserRepository;

public class CrmDatabaseLoader {
	
	private static Logger logger = LoggerFactory.getLogger(CrmDatabaseLoader.class);

	public static void load(DatabaseLoader dbload, ManagedCompany managedCompany) {
		logger.info("Starting CRM data loading to database");
		Long managedCompanyCode = managedCompany.getCode();
		Calendar createTime = Calendar.getInstance();
		createTime.set(2020,10,01,12,00);
		SecUser admin = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin1");
		
		
		LegalStatus localJuridicalPersonVAT = (LegalStatus) dbload.trySave(new LegalStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Бълг. юрид. лице (рег.по ЗДДС)",LegalStatus.LEGAL_STATUS_BG_COMPANY_VAT,"0D4C42C7-FF4C-4FA2-AD95-4A5FAC51E6E1"));
		LegalStatus localJuridicalPerson = (LegalStatus) dbload.trySave(new LegalStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Бълг. юрид. лице (не рег.по ЗДДС)",LegalStatus.LEGAL_STATUS_BG_COMPANY_NO_VAT,"0D4C42C7-FF4C-4FA2-AD95-4A5FAC51E6E1"));
		LegalStatus localNaturalPerson = (LegalStatus) dbload.trySave(new LegalStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Бълг. физ. лице",LegalStatus.LEGAL_STATUS_BG_PHYS,"0D66C290-32AE-48F1-A7D8-DCCB1B7758A9"));
		LegalStatus foreignJuridicalPerson = (LegalStatus) dbload.trySave(new LegalStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Чужд. юрид. лице",LegalStatus.LEGAL_STATUS_NON_BG_COMPANY,"0D4C42C7-FF4C-4FA2-AD95-4A5FAC51E6E1"));
		LegalStatus foreignNaturalPerson = (LegalStatus) dbload.trySave(new LegalStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Чужд. физ. лице",LegalStatus.LEGAL_STATUS_NON_BG_PHYS,"0D66C290-32AE-48F1-A7D8-DCCB1B7758A9"));
		LegalStatus otherLegalStatus = (LegalStatus) dbload.trySave(new LegalStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Други",LegalStatus.LEGAL_STATUS_OTHER,"0D66C290-32AE-48F1-A7D8-DCCB1B7758A9"));
			
		ContactType primaryContact = (ContactType) dbload.trySave(new ContactType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Основен",ContactType.CONTACT_TYPE_GENERAL));
		ContactType managerContact = (ContactType) dbload.trySave(new ContactType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Управител",2L));
		ContactType salesContact = (ContactType) dbload.trySave(new ContactType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Търговски",3L));
		ContactType technicalContact = (ContactType) dbload.trySave(new ContactType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Технически",4L));
		ContactType accountancyContact = (ContactType) dbload.trySave(new ContactType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Счетоводство",5L));

		SalesStage stageUnknown = (SalesStage) dbload.trySave(new SalesStage(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Непознат",1L));
		SalesStage stageTarget = (SalesStage) dbload.trySave(new SalesStage(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Целеви",2L));
		SalesStage stageContacted = (SalesStage) dbload.trySave(new SalesStage(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Потърсен",3L));
		SalesStage stageProspect = (SalesStage) dbload.trySave(new SalesStage(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Заинтересуван",4L));
		SalesStage stageActive = (SalesStage) dbload.trySave(new SalesStage(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Потенциален",5L));
		SalesStage stageClient = (SalesStage) dbload.trySave(new SalesStage(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Спечелен",6L));

		Calendar minTime = Calendar.getInstance();
		minTime.set(2000,1,1,0,0);
		Calendar maxTime = Calendar.getInstance();
		maxTime.set(3000,1,1,0,0);
		Currency currencyBGN = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"BGN", "8199F0BC-8E19-493A-BFB9-DE6ADB429026", null, null, null));
		dbload.trySave(new ArticlePriceRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,null,currencyBGN,BigDecimal.ONE,currencyBGN,minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		Currency currencyEUR = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"EUR", "F9614E4D-0976-45C5-9E23-8DEA3D5C505B", null, null, null));
		Currency currencyUSD = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"USD", "2B693AD5-99C0-4A4B-8D98-451EF0E883B6", null, null, null));
		Currency currencyGBP = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"GBP", "C6FAF1D7-F181-4C63-A699-734522091A39", null, null, null));
		Currency currencyCHF = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"CHF", "26412A02-70F6-4AEA-AE81-A236903982E7", null, null, null));
		Currency currencyTRL = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"TRL", "7AF22C1E-E3ED-45E9-AF6B-A5A83CC2DC52", null, null, null));
		Currency currencySEK = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"SEK", "10ED3E57-8B86-4200-9EDD-EB36FFCB1DD4", null, null, null));
			
	}
	
	public static void loadTest(DatabaseLoader dbload, ManagedCompany managedCompany) {
		logger.info("Starting CRM test data loading to database");
		Long managedCompanyCode = managedCompany.getCode();
		Calendar createTime = Calendar.getInstance();
		createTime.set(2020,10,01,12,00);
		SecUser admin = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin1");
		Calendar minTime = Calendar.getInstance();
		minTime.set(2000,1,1,0,0);
		Calendar maxTime = Calendar.getInstance();
		maxTime.set(3000,1,1,0,0);

		SecRole roleAdmin = (SecRole) dbload.trySave(new SecRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Пълен достъп",1L));
		
		SecPermission permGetAllArticles = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ артикули","ROLE_GET_ALL_Article"));
		SecPermission permGetAllArticlePriceRate = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ цени на артикули","ROLE_GET_ALL_ArticlePriceRate"));
		SecPermission permGetAllArticleProduct = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ продукти","ROLE_GET_ALL_ArticleProduct"));
		SecPermission permGetAllArticleService = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ ArticleService","ROLE_GET_ALL_ArticleService"));
		SecPermission permGetAllAsset = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ Asset","ROLE_GET_ALL_Asset"));
		SecPermission permGetAllAssetAttachment = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ AssetAttachment","ROLE_GET_ALL_AssetAttachment"));
		SecPermission permGetAllAssetComment = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ AssetComment","ROLE_GET_ALL_AssetComment"));
		SecPermission permGetAllAttachable = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ документи за прикачване","ROLE_GET_ALL_Attachable"));
		SecPermission permGetAllAttachableRevenuesAndExpenses = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ приходи и разходи по документи","ROLE_GET_ALL_AttachableRevenuesAndExpenses"));
		SecPermission permGetAllBankAccount = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ BankAccount","ROLE_GET_ALL_BankAccount"));
		SecPermission permGetAllBusinessCategory = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ BusinessCategory","ROLE_GET_ALL_BusinessCategory"));
		SecPermission permGetAllClientInterests = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ интереси на клиента","ROLE_GET_ALL_ClientInterest"));
		SecPermission permGetAllComments = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ коментари на задачите","ROLE_GET_ALL_Comment"));
		SecPermission permGetAllCompanyDepartment = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ CompanyDepartment","ROLE_GET_ALL_CompanyDepartment"));
		SecPermission permGetAllContacts = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ контакти","ROLE_GET_ALL_Contact"));
		SecPermission permGetAllContactType = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ типове контакти","ROLE_GET_ALL_ContactType"));
		SecPermission permGetAllCurrency = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ валути","ROLE_GET_ALL_Currency"));
		SecPermission permGetAllCustomer = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ Customer","ROLE_GET_ALL_Customer"));
		SecPermission permGetAllDBFiles = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ файлове","ROLE_GET_ALL_DBFile"));
		SecPermission permGetAllImportedArticle = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ ImportedArticle","ROLE_GET_ALL_ImportedArticle"));
		SecPermission permGetAllImportedLegalPerson = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ ImportedLegalPerson","ROLE_GET_ALL_ImportedLegalPerson"));
		SecPermission permGetAllImportedLegalPersonGroup = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ ImportedLegalPersonGroup","ROLE_GET_ALL_ImportedLegalPersonGroup"));
		SecPermission permGetAllImportedSantaPartner = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ ImportedSantaPartner","ROLE_GET_ALL_ImportedSantaPartner"));
		SecPermission permGetAllLegalPersons = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ контрагенти","ROLE_GET_ALL_LegalPerson"));
		SecPermission permGetAllLegalPersonAttachments = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ прилежащи документи на контрагентите","ROLE_GET_ALL_LegalPersonAttachment"));
		SecPermission permGetAllLegalPersonComments = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ коментари на контрагента","ROLE_GET_ALL_LegalPersonComment"));
		SecPermission permGetAllLegalStatus = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ видове на субекти","ROLE_GET_ALL_LegalStatus"));
		SecPermission permGetAllMailAccount = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ настройки за пощи","ROLE_GET_ALL_MailAccount"));
		SecPermission permGetAllMailAttachments = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ прилежащи документи на писмата","ROLE_GET_ALL_MailAttachment"));
		SecPermission permGetAllMailMessages = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ получени писма","ROLE_GET_ALL_MailMessage"));
		SecPermission permGetAllMailTemplate = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ шаблони за писма","ROLE_GET_ALL_MailTemplate"));
		SecPermission permGetAllOfferLines = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ редове на офертите","ROLE_GET_ALL_OfferLine"));
		SecPermission permGetAllOffersToClient = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ оферти","ROLE_GET_ALL_OfferToClient"));
		SecPermission permGetAllPlannedIncomeOrExpense = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ планирани приходи или разходи на задачите","ROLE_GET_ALL_PlannedIncomeOrExpense"));
		SecPermission permGetAllPlannedTime = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ планирани времена","ROLE_GET_ALL_PlannedTime"));
		SecPermission permGetAllSalesStage = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ етапи на клиенти","ROLE_GET_ALL_SalesStage"));
		SecPermission permGetAllSecPermission = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ права на потребители","ROLE_GET_ALL_SecPermission"));
		SecPermission permGetAllSecRole = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ роли на потребители","ROLE_GET_ALL_SecRole"));
		SecPermission permGetAllSecRolePermission = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ права на ролите","ROLE_GET_ALL_SecRolePermission"));
		SecPermission permGetAllSecUser = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ потребители","ROLE_GET_ALL_SecUser"));
		SecPermission permGetAllSecUserRole = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ роли на потребителите","ROLE_GET_ALL_SecUserRole"));
		SecPermission permGetAllSendMailMessages = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ изпратени писма","ROLE_GET_ALL_SendMailMessage"));
		SecPermission permGetAllTasks = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ задачи","ROLE_GET_ALL_Task"));
		SecPermission permGetAllTaskAttachments = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ прилежащи документи на задачите","ROLE_GET_ALL_TaskAttachment"));
		SecPermission permGetAllTaskPriority = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ приоритети на задачи","ROLE_GET_ALL_TaskPriority"));
		SecPermission permGetAllTaskRelation = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ връзки между задачите","ROLE_GET_ALL_TaskRelation"));
		SecPermission permGetAllTaskRelationType = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ типове връзки между задачите","ROLE_GET_ALL_TaskRelationType"));
		SecPermission permGetAllTaskRequiredAttachments = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ изходящи документи на задачите","ROLE_GET_ALL_TaskRequiredAttachment"));
		SecPermission permGetAllTaskStatus = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ статуси на задачи","ROLE_GET_ALL_TaskStatus"));
		SecPermission permGetAllTaskType = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ типове на задачи","ROLE_GET_ALL_TaskType"));
		SecPermission permGetAllTaskWatchers = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ наблюдаващи задачите","ROLE_GET_ALL_TaskWatcher"));
		SecPermission permGetAllTimeChargeRate = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ цени на час","ROLE_GET_ALL_TimeChargeRate"));
		SecPermission permGetAllTimeSheetItem = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ работни карти","ROLE_GET_ALL_TimeSheetItem"));
		SecPermission permGetAllVendor = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на ВСИЧКИ доставчици","ROLE_GET_ALL_Vendor"));
		
		SecPermission permGet = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед","ROLE_GET_ANY"));
		SecPermission permHead = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Head","ROLE_HEAD_ANY"));
		SecPermission permOptions = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Options","ROLE_OPTIONS_ANY"));
		SecPermission permTrace = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Trace","ROLE_TRACE_ANY"));

		SecPermission permGetReports = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на справки","ROLE_GET_reports"));
		SecPermission permPostReports = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на справки с параметри","ROLE_POST_reports"));

		SecPermission permPostImportedArticles = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани артикули","ROLE_POST_importedArticles"));
		SecPermission permPatchImportedArticles = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани артикули","ROLE_PATCH_importedArticles"));
		SecPermission permGetImportedArticles = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани артикули","ROLE_GET_importedArticles"));
		SecPermission permPostImportedSantaPartners = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на САНТА контрагенти","ROLE_POST_importedSantaPartners"));
		SecPermission permPatchImportedSantaPartners = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на САНТА контрагенти","ROLE_PATCH_importedSantaPartners"));
		SecPermission permGetImportedSantaPartners = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на САНТА контрагенти","ROLE_GET_importedSantaPartners"));
		SecPermission permPostImportedLegalPersons = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани контрагенти","ROLE_POST_importedLegalPersons"));
		SecPermission permPatchImportedLegalPersons = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани контрагенти","ROLE_PATCH_importedLegalPersons"));
		SecPermission permGetImportedLegalPersons = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани контрагенти","ROLE_GET_importedLegalPersons"));
		SecPermission permPostImportedLegalPersonGroups = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани групи на контрагенти","ROLE_POST_importedLegalPersonGroups"));
		SecPermission permPatchImportedLegalPersonGroups = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани групи на контрагенти","ROLE_PATCH_importedLegalPersonGroups"));
		SecPermission permGetImportedLegalPersonGroups = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани контрагенти","ROLE_GET_importedLegalPersonGroups"));
		SecPermission permPostMailMessages = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на получени писма","ROLE_POST_mailMessages"));
		SecPermission permPostDBFiles = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на документ: файл","ROLE_POST_dBFiles"));
		SecPermission permPostMailAttachments = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на прикачени към получено писмо документи","ROLE_POST_mailAttachments"));
		SecPermission permPatchSendMailMessages = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на изпратени писма","ROLE_PATCH_sendMailMessages"));
		SecPermission permGetSendMailMessages = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на изпратени писма","ROLE_GET_sendMailMessages"));
		SecPermission permPostImportedExpeditionList = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани експедиционни листи","ROLE_POST_importedExpeditionLists"));
		SecPermission permPatchImportedExpeditionList = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани експедиционни листи","ROLE_PATCH_importedExpeditionLists"));
		SecPermission permGetImportedExpeditionList = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани експедиционни листи","ROLE_GET_importedExpeditionLists"));
		SecPermission permPostImportedExpeditionListRow = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани редове на експедиционни листи","ROLE_POST_importedExpeditionListRows"));
		SecPermission permPatchImportedExpeditionListRow = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани редове на експедиционни листи","ROLE_PATCH_importedExpeditionListRows"));
		SecPermission permGetImportedExpeditionListRow = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани редове на експедиционни листи","ROLE_GET_importedExpeditionListRows"));
		SecPermission permPostImportedInvoice = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани фактури","ROLE_POST_importedInvoices"));
		SecPermission permPatchImportedInvoice = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани фактури","ROLE_PATCH_importedInvoices"));
		SecPermission permGetImportedInvoice = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани фактури","ROLE_GET_importedInvoices"));
		SecPermission permPostImportedInvoiceRow = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани редове на фактури","ROLE_POST_importedInvoiceRows"));
		SecPermission permPatchImportedInvoiceRow = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани редове на фактури","ROLE_PATCH_importedInvoiceRows"));
		SecPermission permGetImportedInvoiceRow = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани редове на фактури","ROLE_GET_importedInvoiceRows"));
		SecPermission permPostImportedInvoicePayment = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани плащания на фактури","ROLE_POST_importedInvoicePayments"));
		SecPermission permPatchImportedInvoicePayment = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани плащания на фактури","ROLE_PATCH_importedInvoicePayments"));
		SecPermission permGetImportedInvoicePayment = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани плащания на фактури","ROLE_GET_importedInvoicePayments"));
		SecPermission permPostImportedOrder = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани заявки","ROLE_POST_importedOrders"));
		SecPermission permPatchImportedOrder = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани заявки","ROLE_PATCH_importedOrders"));
		SecPermission permGetImportedOrder = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани заявки","ROLE_GET_importedOrders"));
		SecPermission permPostImportedOrderRow = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани редове на заявки","ROLE_POST_importedOrderRows"));
		SecPermission permPatchImportedOrderRow = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани редове на заявки","ROLE_PATCH_importedOrderRows"));
		SecPermission permGetImportedOrderRow = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани редове на заявки","ROLE_GET_importedOrderRows"));
		SecPermission permPostImportedWarehouseStock = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Създаване на импортирани складови наличности","ROLE_POST_importedWarehouseStocks"));
		SecPermission permPatchImportedWarehouseStock = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Редакция на импортирани складови наличности","ROLE_PATCH_importedWarehouseStocks"));
		SecPermission permGetImportedWarehouseStock = (SecPermission) dbload.trySave(new SecPermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Преглед на импортирани складови наличности","ROLE_GET_importedWarehouseStocks"));
		
		SecRole roleReadOnly = (SecRole) dbload.trySave(new SecRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Минимален достъп",2L));
		//TODO give all permissions that are not in other roles
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleReadOnly,permGet));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleReadOnly,permHead));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleReadOnly,permOptions));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleReadOnly,permTrace));

		SecRole roleSales = (SecRole) dbload.trySave(new SecRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Търговец",4L));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetReports));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permPostReports));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllArticles));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllArticleProduct));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllAttachable));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllAttachableRevenuesAndExpenses));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllClientInterests));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllComments));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllContacts));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllContactType));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllCurrency));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllDBFiles));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllImportedArticle));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllImportedSantaPartner));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllImportedLegalPerson));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllImportedLegalPersonGroup));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllLegalPersons));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllLegalPersonAttachments));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllLegalPersonComments));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllLegalStatus));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllMailAttachments));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllMailMessages));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllMailTemplate));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllOfferLines));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllOffersToClient));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllPlannedIncomeOrExpense));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllPlannedTime));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllSalesStage));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllSecUser));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllSendMailMessages));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTaskAttachments));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTaskPriority));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTaskRelation));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTaskRelationType));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTaskRequiredAttachments));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTasks));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTaskStatus));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTaskType));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTaskWatchers));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllTimeSheetItem));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllVendor));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleSales,permGetAllCustomer));

		SecRole roleImport = (SecRole) dbload.trySave(new SecRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Импорт",3L));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetAllSendMailMessages));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetReports));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostReports));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedArticles));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedArticles));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedArticles));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedSantaPartners));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedSantaPartners));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedSantaPartners));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedLegalPersons));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedLegalPersons));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedLegalPersons));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedLegalPersonGroups));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedLegalPersonGroups));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedLegalPersonGroups));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostMailMessages));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostDBFiles));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostMailAttachments));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchSendMailMessages));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetSendMailMessages));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedExpeditionList));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedExpeditionList));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedExpeditionList));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedExpeditionListRow));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedExpeditionListRow));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedExpeditionListRow));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedInvoice));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedInvoice));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedInvoice));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedInvoiceRow));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedInvoiceRow));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedInvoiceRow));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedInvoicePayment));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedInvoicePayment));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedInvoicePayment));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedOrder));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedOrder));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedOrder));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedOrderRow));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedOrderRow));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedOrderRow));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPostImportedWarehouseStock));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permPatchImportedWarehouseStock));
		dbload.trySave(new SecRolePermission(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,roleImport,permGetImportedWarehouseStock));
		
		
		TaskType typeCRMCampaign = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Маркетингова кампания",TaskType.TASK_TYPE_MARKETING_CAMPAIGN));
		TaskType typeCRMCommunication = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Комуникация с клиент",TaskType.TASK_TYPE_CLIENT_COMMUNICATION));
		TaskType typeCRMRfO = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Запитване от клиент",TaskType.TASK_TYPE_REQUEST_FOR_OFFER));
		TaskType typeCRMOffer = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Оферта",TaskType.TASK_TYPE_PREPARE_OFFER));
		TaskType typeCRMContract = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Договор",TaskType.TASK_TYPE_PREPARE_CONTRACT));
		TaskType typeCRMProject = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Проект",TaskType.TASK_TYPE_PROJECT));
		TaskType typeTrainingPlan = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"План на обученията",TaskType.TASK_TYPE_TRAINING_PLAN));
		TaskType typeTraining = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Обучение",TaskType.TASK_TYPE_TRAINING));
		TaskType typeEmployeeTraining = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Обучение на служител",TaskType.TASK_TYPE_EMPLOYEE_TRAINING));
		TaskType typeAttestCampaign = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Атестационна кампания",TaskType.TASK_TYPE_ATTESTATION_CAMPAIGN));
		TaskType typeCareerPlan = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"План за развитие на служителя",TaskType.TASK_TYPE_EMPLOYEE_CAREER_PLAN));
		TaskType typeEmployeeAttestation = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Оценка на служителя",TaskType.TASK_TYPE_EMPLOYEE_ATTESTATION));

		JobPosition posManager = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Управител","1"));
		JobPosition posLatheOperator = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"фрезист","2"));
		JobPosition posProductionOfficer = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Организатор производство","3"));
		JobPosition posMechanic = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"шлосер-монтьор","4"));
		JobPosition posWarehouseKeeper = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"склададжия","5"));
		JobPosition posLogisticsSpecialist = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"специалист доставки, логистика","6"));
		JobPosition posSalesAssistant = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"търговски сътрудник, логистика, Европейски програми и ISO","7"));
		JobPosition posTruckDriver = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"шофьор лекотоварен автомобил","8"));
		JobPosition posWarehouseManager = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"началник склад","9"));
		JobPosition posEngineer = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"инженер-конструктор","10"));
		JobPosition posProductionManager = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ръководител производство","11"));
		JobPosition posQA = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ОТК","12"));
		JobPosition posSales = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"специалист продажби","13"));
		JobPosition posSalesManager = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ръководител отдел продажби","14"));
		JobPosition posAttorney = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"адвокат на граждански договор","15"));
		JobPosition posMachineOperator = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"машинен оператор","16"));
		JobPosition posSecretary = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"секретарка","17"));
		JobPosition posCFO = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"финансов директор","18"));
		JobPosition posAccountant = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"счетоводител","19"));
		JobPosition posERPCRM = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ERP & CRM","20"));
		JobPosition posSalesOfficer = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"търговски отдел","21"));
		JobPosition posSalesME = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"търговски директор Близък изток","22"));
		JobPosition posIT = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"системен администратор","23"));
		JobPosition posCleaner = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"хигиенист","24"));
		JobPosition posProductManager = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"продуктов мениджър","25"));
		JobPosition posImportsOfficer = (JobPosition) dbload.trySave(new JobPosition(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Отдел Внос","26"));

		CompanyDepartment depSales = (CompanyDepartment) dbload.trySave(new CompanyDepartment(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Търговски Отдел",null));
		CompanyDepartment depImport = (CompanyDepartment) dbload.trySave(new CompanyDepartment(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Отдел Внос",depSales));
		CompanyDepartment depWarehouse = (CompanyDepartment) dbload.trySave(new CompanyDepartment(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"СКЛАД",null));
		CompanyDepartment depAdmin = (CompanyDepartment) dbload.trySave(new CompanyDepartment(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Администрация",null));
		CompanyDepartment depFinance = (CompanyDepartment) dbload.trySave(new CompanyDepartment(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Финансов Отдел",null));
		CompanyDepartment depProduction = (CompanyDepartment) dbload.trySave(new CompanyDepartment(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,"Производствен Отдел",null));

		//users for both companies
		SecUser otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"mmarkov"+managedCompanyCode,"Мирослав Марков","123","mm@industrial-parts.com","MM"));
		dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleAdmin));
		dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Мирослав Марков","2345678901",null,null,null,otherUser,posManager,depAdmin));
		otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"mstoyanova"+managedCompanyCode,"Мария Стоянова","123","m.stoyanova@industrial-parts.com","MS"));
		dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleAdmin));
		dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Мария Стоянова","3456789012",null,null,null,otherUser,posCFO,depFinance));
		otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"mnikolov"+managedCompanyCode,"Михаил Николов","123","support@industrial-parts.com","MN"));
		dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleAdmin));

		//only for WATO
		if(managedCompanyCode == 1L) {
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"vgeshkova","Венета Гешкова","123","veneta.geshkova@wato.bg","VG"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Венета Гешкова","4567890123",null,null,null,otherUser,posAccountant,depFinance));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"kgardeva","Калина Гърдева","123","kalina.gardeva@wato.bg","KG"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Калина Гърдева","5678901234",null,null,null,otherUser,posSales,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"hstefanova","Христина Стефанова","123","hristina.stefanova@wato.bg","HS"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleAdmin));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Христина Стефанова","6789012345",null,null,null,otherUser,posSalesManager,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"kdragnev","Константин Драгнев","123","konstantin.dragnev@wato.bg","KD"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Константин Драгнев","7890123456",null,null,null,otherUser,posProductionOfficer,depProduction));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"nbakalov","Николай Бакалов","123","nikolai.bakalov@wato.bg","NB"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Николай Бакалов","890123456",null,null,null,otherUser,posLogisticsSpecialist,depWarehouse));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ngeorgieva","Надя Георгиева","123","nadia.georgieva@wato.bg","NG"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleAdmin));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Надя Георгиева","9012345678",null,null,null,otherUser,posSalesAssistant,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"hduhtev","Христо Духтев","123","hristo.duhtev@wato.bg","HD"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Христо Духтев","0123456789",null,null,null,otherUser,posWarehouseManager,depWarehouse));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"kpenchev","Красимир Пенчев","123","krasimir.penchev@wato.bg","KP"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Красимир Пенчев","1234567899",null,null,null,otherUser,posEngineer,depProduction));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"kdyakov","Красимир Дяков","123","krasimir.dyakov@wato.bg","KD"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleAdmin));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Красимир Дяков","1234567898",null,null,null,otherUser,posProductionManager,depProduction));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"zhmarinov","Жюлиан Маринов","123","julian.marinov@wato.bg","JM"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleAdmin));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Жюлиан Маринов","1234567897",null,null,null,otherUser,posProductManager,depProduction));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ianev","Ивайло Анев","123","ivaylo.anev@wato.bg","IA"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ивайло Анев","1234567896",null,null,null,otherUser,posSalesOfficer,depSales));
		}
		//only for Industrial Parts
		if(managedCompanyCode == 2L) {
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"zkurumileva","Зорница Курумилева","123","zornica.kurumileva@industrial-parts.com","ZK"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Зорница Курумилева",null,null,null,null,otherUser,posAccountant,depFinance));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"mmihailova","Милена Михайлова","123","milena.mihaylova@industrial-parts.com","MM"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Милена Михайлова",null,null,null,null,otherUser,posAccountant,depFinance));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"skaraulanov","Свилен Карауланов","123","svilen.karaulanov@industrial-parts.com","SK"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleAdmin));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Свилен Карауланов",null,null,null,null,otherUser,posERPCRM,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"spetkov","Станислав Петков","123","stanislav.petkov@industrial-parts.com","SP"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Станислав Петков",null,null,null,null,otherUser,posWarehouseKeeper,depWarehouse));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"kpetrov","Кристиян Петров","123","sklad.carigradsko@gmail.com","KP"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Кристиян Петров",null,null,null,null,otherUser,posWarehouseKeeper,depWarehouse));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"igencheva","Ива Генчева","123","iva.gencheva@industrial-parts.com","IG"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ива Генчева",null,null,null,null,otherUser,posImportsOfficer,depImport));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"mgencheva","Марина Генчева","123","marina.gencheva@industrial-parts.com","MG"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Марина Генчева",null,null,null,null,otherUser,posImportsOfficer,depImport));

			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ddochev","Даниел Дочев","123","d.dochev@industrial-parts.com","DD"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Даниел Дочев",null,null,null,null,otherUser,posSalesOfficer,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"kkarpuzov","Кубрат Карпузов","123","kubrat.karpuzov@industrial-parts.com","KK"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Кубрат Карпузов",null,null,null,null,otherUser,posSalesOfficer,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"vsveshtarova","Виктория Свещарова","123","v.svestarova@industrial-parts.com","VS"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Виктория Свещарова",null,null,null,null,otherUser,posSalesOfficer,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"gdonev","Георги Донев","123","georgi.donev@industrial-parts.com","GD"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Георги Донев",null,null,null,null,otherUser,posSalesOfficer,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"tkaterov","Тодор Катеров","123","todor.katerov@industrial-parts.com","TK"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Тодор Катеров",null,null,null,null,otherUser,posSalesOfficer,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"nmatev","Николай Матев","123","nikolay.matev@industrial-parts.com","NM"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Николай Матев",null,null,null,null,otherUser,posSalesOfficer,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"pandreev","Пеньо Андреев","123","andreev@industrial-parts.com","PA"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Пеньо Андреев",null,null,null,null,otherUser,posSalesAssistant,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ggeorgiev","Георги Георгиев","123","georgi.georgiev@industrial-parts.com","GG"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Георги Георгиев",null,null,null,null,otherUser,posSalesOfficer,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"hiliev","Христо Илиев","123","hristo.iliev@industrial-parts.com","HI"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Христо Илиев",null,null,null,null,otherUser,posSalesOfficer,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"mtodorova","Мариела Тодорова","123","mariela.todorova@industrial-parts.com","MT"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Мариела Тодорова",null,null,null,null,otherUser,posSalesAssistant,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ggeorgieva","Галина Георгиева","123","galina.georgieva@industrial-parts.com","GG"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Галина Георгиева",null,null,null,null,otherUser,posSalesAssistant,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"rgeorgieva","Роза Георгиева","123","roza.georgieva@industrial-parts.com","RG"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Роза Георгиева",null,null,null,null,otherUser,posSalesAssistant,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"sslivkova","Стоянка Сливкова","123","s.slivkova@industrial-parts.com","SS"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleSales));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Стоянка Сливкова",null,null,null,null,otherUser,posSalesAssistant,depSales));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"pilieva","Павлина Илиева","123","pavlina.ilieva@industrial-parts.com","PI"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Павлина Илиева",null,null,null,null,otherUser,posAccountant,depAdmin));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"rdeshkov","Румен Дешков","123","rumen.deshkov@industrial-parts.com","RD"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Румен Дешков",null,null,null,null,otherUser,posAccountant,depAdmin));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ypetkova","Йонка Петкова","123","y.petkova@industrial-parts.com","YP"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Йонка Петкова",null,null,null,null,otherUser,posAccountant,depAdmin));
			otherUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"kgolev","Калин Голев","123","kalin.golev@industrial-parts.com","KG"));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,otherUser,roleReadOnly));
			dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Калин Голев",null,null,null,null,otherUser,posAccountant,depAdmin));
		}
		
		dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ОП Вода", "2.2"));
		dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Партньори", "2.1"));
		DirectionCategory defaultDirectionCategory = (DirectionCategory) dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Общи", "2.5"));
		dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ОП Индустрия", "2.3"));
		dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ОП Пречистване", "2.4"));
		dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Свързани фирми", "2.6"));
		dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Противопожарни", "2.7"));
		dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ТА Индустрия-Изток", "2.8"));
		dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Услуги", "2.9"));
		dbload.trySave(new DirectionCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Доставчик", "2.10"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Благоевград", "3.1"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Бургас", "3.4"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Варна", "3.3"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Велико Търново", "3.2"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Плевен", "3.5"));
		AreaCategory defaultAreaCategory = (AreaCategory) dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Пловдив", "3.6"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Русе", "3.7"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Силистра", "3.8"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Сливен", "3.9"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Шумен", "3.10"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"София", "3.11"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Стара Загора", "3.12"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Хасково", "3.13"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Кърджали", "3.14"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ловеч", "3.15"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Враца", "3.16"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Видин", "3.17"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Смолян", "3.18"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Монтана", "3.19"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разград", "3.20"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Добрич", "3.21"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Габрово", "3.22"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Търговище", "3.23"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ямбол", "3.24"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Кюстендил", "3.25"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Пазарджик", "3.26"));
		dbload.trySave(new AreaCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Троян", "3.27"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Монтажна фирма", "4.1"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Строителство", "4.2"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Търговска фирма", "4.3"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Завод/ Производство", "4.4"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Конкуренция", "4.5"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Газификация", "4.6"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Добив руди и суровини", "4.7"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Друга търговия на едро", "4.8"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Други производства", "4.9"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ЕЦ, ЕРП,ТЕЦ, ВЕЦ", "4.10"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Железария", "4.11"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Капково напояване,напояване", "4.12"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Корабостроит. и ремонти", "4.13"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Помпи и помпени съоръжени", "4.14"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Проектантска фирма", "4.15"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Произв нефтопродукти", "4.16"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Произв текстил и облекло", "4.17"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Произв ХВП и тютюн. изд", "4.18"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Производство на биодизел", "4.19"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Птицевъдство", "4.20"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Пътно строителство", "4.21"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Търговия на дребно", "4.22"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Дърводобив и свързани с тях услуги", "4.23"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Търг. едро строителни материали", "4.24"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Търг. едро вода и топло", "4.25"));
		BusinessCategory defaultBusinessCategory = (BusinessCategory) dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Конкурент/ Доставчик", "4.26"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Монтажна фирма/ Климатизация", "4.27"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Строителство на пътища", "4.28"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Нестандартно оборудване", "4.29"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Мандра","4.30"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Извършване на строително монтажни работи, инженерингова и изпълнителска дейност", "4.31"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Производство", "4.32"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Производство ХВП", "4.33"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Печатница", "4.34"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Хотел", "4.35"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Обработка на изделия", "4.36"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Търговска фирма /електроника/", "4.37"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Адвокат", "4.38"));
		dbload.trySave(new BusinessCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Транспортна фирма", "4.39"));
		dbload.trySave(new GeneralCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"за изтриване", "5.1"));
		dbload.trySave(new GeneralCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"факторинг", "5.2"));
		dbload.trySave(new GeneralCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"конкурент", "5.3"));
		GeneralCategory defaultGeneralCategory = (GeneralCategory) dbload.trySave(new GeneralCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Доставчик", "5.4"));
		dbload.trySave(new GeneralCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Услуги", "5.5"));
		dbload.trySave(new GeneralCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Клиент", "5.6"));
		dbload.trySave(new GeneralCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Клиент/ Доставчик", "5.7"));
		dbload.trySave(new GeneralCategory(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"МХГ", "5.8"));

		MailTemplate mailTemplate = (MailTemplate) dbload.trySave(new MailTemplate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Писмо до нов клиент",null,"Introducing Wato Bulgaria","Dear {counterParty.name},\n" + 
				"I would like to introduce our company Wato Bulgaria.\n" + 
				"We are manufacturer of valves and fittings aimed towards the water and waste water industries, which operates on the global market mainly in Europe, Middle East and Russia.\n" + 
				"At www.wato.bg you can find all our products with technical specifications.\n" + 
				"I have been researching {counterParty.name} and think that there is a good opportunity for future partnership.\n" + 
				"Please don’t hesitate to contact us for inquiry and questions.\n" + 
				"\n" + 
				"Best Regards,\n" + 
				"{assigned.fullName}", null, null));
		dbload.trySave(new MailTemplate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Напомнящо писмо до клиент",null,"Изпратена оферта от нашата компания","Уважаеми {counterParty.name},\nНапомняме Ви, че очакваме Вашият отговор на изпратена оферта.", null, null));
		dbload.trySave(new MailTemplate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Новини за абонирани клиенти",null,"Wato Bulgaria newsletter","Dear Clients and Partners,\n" + 
				"\n" + 
				"We would like to inform you, that Wato BG will change the price list from 01.March.2018. In the attached file you can find the Wato BG Basic Price List 2018.\n" + 
				"We’ve optimized the prices for some of our products, in order to give you the best competitive solutions. We are also glad to inform you, that we’ve released new products and wrote them in the new price list. \n" + 
				"For more information, achievements and future releasing of new products and news about Wato BG, you can visit our new website: www.wato.bg , make a free registration, check out our new catalogue and follow our E-Bulletin.\n" +
				"\n" + 
				"With Very Best Regards,\n" + 
				"Wato BG Team", null, null));
		
		Dictionary corporateDict = (Dictionary) dbload.trySave(new Dictionary(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Корпоративен (бизнес)"));
		Dictionary industrialDict = (Dictionary) dbload.trySave(new Dictionary(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Индустриален (браншови)"));
		Dictionary ordinaryDict = (Dictionary) dbload.trySave(new Dictionary(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Общ"));

		dbload.trySave(new DictionaryClassificationPolicy(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Правила за класификация на корпоративни термини", "Под корпоративни термини трябва да бъдат отнесени всички термини от бизнес сферата, включително, но не само, финансови, счетоводни, маркетингови, организационни и управленски.", corporateDict));
		dbload.trySave(new DictionaryClassificationPolicy(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Правила за класификация на индустриални термини", "Под индустриални термини трябва да бъдат отнесени всички термини, свързани с технически методологии, явления, оръдия на труда и процеси.", industrialDict));
		dbload.trySave(new DictionaryClassificationPolicy(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Правила за класификация на корпоративни термини", "Под общи термини трябва да бъдат отнесени всички термини, които не се отнасят към точно специфична област.", ordinaryDict));

		TaskStatus statusFinished = (TaskStatus) dbload.trySave(new TaskStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Приключена",TaskStatus.TASK_STATUS_FINISHED,true));
		TaskPriority priorityMedium = (TaskPriority) dbload.trySave(new TaskPriority(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Среден",TaskPriority.TASK_PRIORITY_MEDIUM));

		JobRequirement jobReqEnglish = (JobRequirement) dbload.trySave(new JobRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Английски език ниво 3", "1"));
		JobRequirement jobReqGerman = (JobRequirement) dbload.trySave(new JobRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Немски език ниво 3", "2"));
		JobRequirement jobReqArabic = (JobRequirement) dbload.trySave(new JobRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Арабски език ниво 3", "3"));
		JobRequirement jobReqEngineer = (JobRequirement) dbload.trySave(new JobRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Висше инженерно образование", "4"));
		JobRequirement jobReqEconomist = (JobRequirement) dbload.trySave(new JobRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Висше икономическо образование", "5"));
		JobRequirement jobReqTruckDriver = (JobRequirement) dbload.trySave(new JobRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Шофьорска книжка категория C", "6"));

		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posManager,jobReqEnglish));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posLogisticsSpecialist,jobReqEnglish));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSalesAssistant,jobReqEnglish));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSalesAssistant,jobReqEconomist));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posTruckDriver,jobReqTruckDriver));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posEngineer,jobReqEngineer));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posProductionManager,jobReqEngineer));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSales,jobReqEnglish));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSalesManager,jobReqEconomist));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSecretary,jobReqEnglish));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSecretary,jobReqGerman));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSecretary,jobReqArabic));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posCFO,jobReqEconomist));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posAccountant,jobReqEconomist));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posERPCRM,jobReqEnglish));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSalesOfficer,jobReqEnglish));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSalesME,jobReqEconomist));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posSalesME,jobReqArabic));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posIT,jobReqEngineer));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posProductManager,jobReqEnglish));
		dbload.trySave(new JobPositionRequirement(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"",posImportsOfficer,jobReqEnglish));

		createTime.add(Calendar.MINUTE, 1);
		SecUser readOnlyUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"readOnlyUser"+managedCompanyCode,"Наблюдател","123",null,"RO"));
		dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,readOnlyUser,roleReadOnly));

		SecUser userCTO = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"avelikov"+managedCompanyCode,"Ангел Великов","123","angel.velikov@latona.bg","AV"));
		dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,userCTO,roleAdmin));
		SecUser salesManager = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"odenkov"+managedCompanyCode,"Орлин Денков","123","orlin.denkov@latona.bg","OD"));
		dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,salesManager,roleAdmin));
		SecUser sales = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"pangelova"+managedCompanyCode,"Петя Ангелова","123","petya.angelova@latona.bg","PA"));
		dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,sales,roleAdmin));
		SecUser secretary = sales;
		SecUser worker1 = userCTO;
		SecUser worker2 = userCTO;
		SecUser expert1 = userCTO;

		Currency currencyBGN = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"BGN", "8199F0BC-8E19-493A-BFB9-DE6ADB429026", null, null, null));
		Currency currencyEUR = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"EUR", "F9614E4D-0976-45C5-9E23-8DEA3D5C505B", null, null, null));
		Currency currencyUSD = (Currency) dbload.trySave(new Currency(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"USD", "2B693AD5-99C0-4A4B-8D98-451EF0E883B6", null, null, null));

		SalesStage stageUnknown = (SalesStage) dbload.trySave(new SalesStage(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Непознат",1L));
		SalesStage stageActive = (SalesStage) dbload.trySave(new SalesStage(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Потенциален",5L));
		SalesStage stageClient = (SalesStage) dbload.trySave(new SalesStage(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Спечелен",6L));
		ContactType primaryContact = (ContactType) dbload.trySave(new ContactType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Основен",ContactType.CONTACT_TYPE_GENERAL));
		LegalStatus localJuridicalPersonVAT = (LegalStatus) dbload.trySave(new LegalStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Бълг. юрид. лице (рег.по ЗДДС)",LegalStatus.LEGAL_STATUS_BG_COMPANY_VAT,"0D4C42C7-FF4C-4FA2-AD95-4A5FAC51E6E1"));

		LegalPerson legalPersonOwn = (LegalPerson) dbload.trySave(new LegalPerson(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ТРАНС ЛОДЖИК ГРУП ЕООД", "204074306",null, "BG204074306", "АПОСТОЛ СТОИМЕНОВ", "обл. Благоевград, с. Ново Делчево, ул. Славянска 19", null, null, "България", null, localJuridicalPersonVAT, null));
		Customer customer1 = (Customer) dbload.trySave(new Customer(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, legalPersonOwn, true, stageClient, sales, false, null, defaultDirectionCategory, defaultAreaCategory, defaultBusinessCategory, defaultGeneralCategory));
		dbload.trySave(new Contact(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "Представител на Клиент 1", null, null, null, "angel.velikov@latona.bg", null, legalPersonOwn, primaryContact));
		LegalPerson person2 = (LegalPerson) dbload.trySave(new LegalPerson(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "БОН МАРИН ООД", "103612886",null, "BG103612886", "Емил Кайкамджозов", "9000, ул. Вардар 3", null, null, "България", "обл.ВАРНА, гр.ВАРНА", localJuridicalPersonVAT, null));
		dbload.trySave(new Customer(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, person2, true, stageUnknown, sales, false, null, defaultDirectionCategory, defaultAreaCategory, defaultBusinessCategory, defaultGeneralCategory));
		dbload.trySave(new Contact(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "Представител на Клиент 2", null, null, null, "velikan@vivansa.com", null, person2, primaryContact));
		LegalPerson person3 = (LegalPerson) dbload.trySave(new LegalPerson(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "ОРБИТ ЕООД", "121184049", null, "BG121184049", "КОЦИАС ЗИСИС СОТИРИОС", "гр. СОФИЯ, ул. Продан Таракчиев № 16", null, null, "България", null, localJuridicalPersonVAT, null));
		dbload.trySave(new Customer(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, person3, true, stageUnknown, sales, false, null, defaultDirectionCategory, defaultAreaCategory, defaultBusinessCategory, defaultGeneralCategory));
		dbload.trySave(new Contact(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "Представител на Клиент 3", null,	null, null, "orlin.denkov@latona.bg", null, person3, primaryContact));
		LegalPerson person4 = (LegalPerson) dbload.trySave(new LegalPerson(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "Клиент 4", "201849923",	null, "BG201849923", "МОЛ 4", "Адрес 4", "ПК 4", "Адрес 4", "Държава 4", "Град 4", localJuridicalPersonVAT, null));
		Customer customer4 = (Customer) dbload.trySave(new Customer(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, person4, true, stageActive, sales, false, null, defaultDirectionCategory, defaultAreaCategory, defaultBusinessCategory, defaultGeneralCategory));
		dbload.trySave(new Contact(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "Представител на Клиент 4", null,	null, null, "petya.angelova@latona.bg", null, person4, primaryContact));
		ArticleProduct material1 = (ArticleProduct) dbload.trySave(new ArticleProduct(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Материал 1", "6F5231B2-D661-47B3-9147-006D78F2806C", null, null, "CA484438-6FCB-4B3C-A101-7A4D79366C52", null, null));
		ArticleProduct material2 = (ArticleProduct) dbload.trySave(new ArticleProduct(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Материал 2", "AF60D9BB-DECB-4BDE-81AF-001B2617BD27", null, null, "CA484438-6FCB-4B3C-A101-7A4D79366C52", null, null));
		ArticleProduct productA = (ArticleProduct) dbload.trySave(new ArticleProduct(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Продукт А", "23C0B953-777B-4C81-8133-004D11FA3D1B", null, null, "CA484438-6FCB-4B3C-A101-7A4D79366C52", null, null));
		ArticleProduct productB = (ArticleProduct) dbload.trySave(new ArticleProduct(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Продукт В", "C216E286-B8E6-4008-BAF2-0063F3ED359A", null, null, "CA484438-6FCB-4B3C-A101-7A4D79366C52", null, null));
		
		dbload.trySave(new ClientInterest(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, customer1, productA, true));
		dbload.trySave(new ClientInterest(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, customer4, productB, true));
		
		//only for WATO
		if(managedCompanyCode == 1L) {
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ДСК BGN", "BG16STSA93000024589399", "БАНКА ДСК", "STSABGSF", currencyBGN));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ДСК EUR", "BG41STSA93000025217509", "БАНКА ДСК", "STSABGSF", currencyEUR));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ДСК USD", "BG74STSA93000025573681", "БАНКА ДСК", "STSABGSF", currencyUSD));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ДСК EUR factoring", "BG82STSA93000025406794", "БАНКА ДСК", "STSABGSF", currencyEUR));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"УНИКРЕДИТ BGN", "BG06UNCR70001521474450", "УНИКРЕДИТ БУЛБАНК АД", "UNCRBGSF", currencyBGN));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"УНИКРЕДИТ USD", "BG71UNCR70001523277965", "УНИКРЕДИТ БУЛБАНК АД", "UNCRBGSF", currencyUSD));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"РАЙФАЙЗЕН BGN", "BG15RZBB91551001077729", "РАЙФАЙЗЕНБАНК", "RZBBBGSF", currencyBGN));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"РАЙФАЙЗЕН EUR", "BG26RZBB91551002750393", "РАЙФАЙЗЕНБАНК", "RZBBBGSF", currencyEUR));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"РАЙФАЙЗЕН EUR 2", "BG97RZBB91551004445742", "РАЙФАЙЗЕНБАНК", "RZBBBGSF", currencyEUR));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"РАЙФАЙЗЕН USD", "BG02RZBB91551009131873", "РАЙФАЙЗЕНБАНК", "RZBBBGSF", currencyUSD));
		}
		//only for Industrial Parts
		if(managedCompanyCode == 2L) {
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"РАЙФАЙЗЕН BGN", "BG56RZBB91551089511212", "РАЙФАЙЗЕНБАНК", "RZBBBGSF", currencyBGN));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"РАЙФАЙЗЕН EUR", "BG44RZBB91551489511201", "РАЙФАЙЗЕНБАНК", "RZBBBGSF", currencyEUR));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"РАЙФАЙЗЕН USD", "BG89RZBB91551189511216", "РАЙФАЙЗЕНБАНК", "RZBBBGSF", currencyUSD));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ПРОКРЕДИТ BGN", "BG20PRCB92301049647801", "ПРОКРЕДИТ БАНК", "PRCBBGSF", currencyBGN));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ПРОКРЕДИТ EUR", "BG02PRCB92301449647801", "ПРОКРЕДИТ БАНК", "PRCBBGSF", currencyEUR));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ДСК BGN", "BG78STSA93000025499016", "БАНКА ДСК", "STSABGSF", currencyBGN));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ДСК BGN факторинг", "BG90STSA93000025550113", "БАНКА ДСК", "STSABGSF", currencyBGN));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ДСК EUR", "BG96STSA93000025565040", "БАНКА ДСК", "STSABGSF", currencyEUR));
			dbload.trySave(new BankAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,legalPersonOwn,"ДСК USD", "BG31STSA93000025565046", "БАНКА ДСК", "STSABGSF", currencyUSD));
		}

		dbload.trySave(new TimeChargeRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, admin, admin, BigDecimal.TEN, currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new TimeChargeRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, salesManager, salesManager, new BigDecimal(12), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new TimeChargeRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, sales, sales, new BigDecimal(8), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new TimeChargeRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, userCTO, userCTO, new BigDecimal(12), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new TimeChargeRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, expert1, expert1, BigDecimal.TEN, currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new TimeChargeRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, worker1, worker1, new BigDecimal(5), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new TimeChargeRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, worker2, worker2, new BigDecimal(8), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new ArticlePriceRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "Доставчик 1", material1, new BigDecimal(800), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new ArticlePriceRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "Доставчик 1", material2, new BigDecimal(500), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new ArticlePriceRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "Доставчик 1", productA, new BigDecimal(40000), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new ArticlePriceRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "Доставчик 1", productB, new BigDecimal(30000), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new ArticlePriceRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "БНБ", currencyBGN, BigDecimal.ONE, currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		dbload.trySave(new ArticlePriceRate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany, "БНБ", currencyEUR, new BigDecimal(1.95583), currencyBGN, minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
		
		//идентифициран и спечелен нов клиент
		createTime.add(Calendar.MINUTE, 1);
		Calendar deadlineTime = (Calendar)createTime.clone();
		deadlineTime.add(Calendar.MONTH, 1);
		
		Task commTask = (Task) dbload.trySave(new Task(salesManager,Date.from(createTime.toInstant()),salesManager,Date.from(createTime.toInstant()),false,managedCompany,statusFinished,typeCRMCommunication, priorityMedium,
				"Прозвъняване на Клиент 1","Да се направи първоначален контакт с клиента",sales,person2,Date.from(deadlineTime.toInstant())));
		createTime.add(Calendar.MINUTE, 1);
		dbload.trySave(new Comment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Контактът не отговаря на телефона",commTask));
		createTime.add(Calendar.MINUTE, 1);
		dbload.trySave(new Comment(salesManager,Date.from(createTime.toInstant()),salesManager,Date.from(createTime.toInstant()),false,managedCompany,"Пиши им на електронната поща",commTask));
		createTime.add(Calendar.MINUTE, 1);
		dbload.trySave(new Comment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Изпратих стандартната брошура",commTask));
		DBFile brochure = (DBFile) dbload.trySave(new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Стандартна_брошура.txt",null,"content".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Изпратена брошура",commTask,brochure));
		dbload.trySave(new LegalPersonAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Изпратена брошура",person2,brochure));
		Calendar startTime = Calendar.getInstance();
		startTime.set(2017,10,01,12,33);
		Calendar endTime = Calendar.getInstance();
		endTime.set(2017,10,01,12,34);
		dbload.trySave(new TimeSheetItem(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,sales,Date.from(startTime.toInstant()),Date.from(endTime.toInstant()),"Прозвъняване",commTask));
		startTime.set(2017,10,01,12,35);
		endTime.set(2017,10,01,12,36);
		dbload.trySave(new TimeSheetItem(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,sales,Date.from(startTime.toInstant()),Date.from(endTime.toInstant()),"Изпращане брошура",commTask));

		createTime.add(Calendar.DAY_OF_MONTH, 1);
		deadlineTime = (Calendar)createTime.clone();
		deadlineTime.add(Calendar.MONTH, 1);
		
		TaskRelationType typeRelated = (TaskRelationType) dbload.trySave(new TaskRelationType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"свързана","свързана",TaskRelationType.TASK_RELATION_TYPE_RELATED));

		Task offerTask = (Task) dbload.trySave(new Task(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,statusFinished,typeCRMOffer, priorityMedium,
				"Оферта до Клиент 1","Да се изпрати оферта за последния модел",salesManager,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeRelated,commTask,offerTask));

		DBFile emailRequestFromClient1 = (DBFile) dbload.trySave(new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Писмо 455 до sales",null,"content".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Изисквания на клиента",offerTask,emailRequestFromClient1));
		DBFile offerClient1 = (DBFile) dbload.trySave(new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Оферта 112",null,"content".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Оферта в счетоводството",offerTask,offerClient1));

		OfferToClient offer1 = (OfferToClient) dbload.trySave(new OfferToClient(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Оферта 110217PA0000"+managedCompanyCode+"/01 към Клиент 1",null,"110217PA0000"+managedCompanyCode+"/01",1,null,deadlineTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),BigDecimal.ONE,new BigDecimal(20),currencyBGN,null,person2,"Франко склада","",null,null,null));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Оферта в CRM",offerTask,offer1));
		
		dbload.trySave(new OfferLine(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,null,material2,BigDecimal.ONE,new BigDecimal(1000),BigDecimal.ZERO,"2 седмици",offer1));
		dbload.trySave(new OfferLine(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"",material1,new BigDecimal(2),new BigDecimal(2700),BigDecimal.ONE,"2 седмици",offer1));
		dbload.trySave(new OfferLine(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Продукт А с допълнителна обработка",productA,new BigDecimal(2),new BigDecimal(49000),BigDecimal.ONE,"3 седмици",offer1));
		
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"SANTA_OFFER","Оферта в счетоводството",offerTask,offerClient1));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker1,new BigDecimal(40*60),offerTask));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,material2,new BigDecimal(-1),offerTask));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(1000),offerTask));
		
		createTime.add(Calendar.WEEK_OF_MONTH, 2);
		deadlineTime = (Calendar)createTime.clone();
		deadlineTime.add(Calendar.MONTH, 1);
		
		TaskStatus statusAssigned = (TaskStatus) dbload.trySave(new TaskStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Възложена",TaskStatus.TASK_STATUS_ASSIGNED,false));
		TaskRelationType typeChild = (TaskRelationType) dbload.trySave(new TaskRelationType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"подзадача","надзадача",TaskRelationType.TASK_RELATION_TYPE_SUBTASK));

		Task contractTask = (Task) dbload.trySave(new Task(salesManager,Date.from(createTime.toInstant()),salesManager,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeCRMContract, priorityMedium,
				"Договор с Клиент 1","Да се подпише договор съгласно офертата",salesManager,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,offerTask,contractTask));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Изисквания на клиента",contractTask,emailRequestFromClient1));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Оферта в счетоводството",contractTask,offerClient1));
		DBFile standardContract = (DBFile) dbload.trySave(new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Стандартен договор",null,"content".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Изпратен стандартен договор",contractTask,standardContract));
		DBFile contractClient1 = (DBFile) dbload.trySave(new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Договор с корекции",null,"content".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Договор с корекции от клиента",contractTask,contractClient1));
		DBFile contractClient1_final = (DBFile) dbload.trySave(new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Договор 112",null,"content".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Договор в счетоводството",contractTask,contractClient1_final));
		dbload.trySave(new LegalPersonAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Договор в счетоводството",person2,contractClient1_final));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"SANTA_CONTRACT","Договор в счетоводството",contractTask,contractClient1_final));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"SANTA_CONTRACT","Сканиран подписан договор",contractTask,null));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker1,new BigDecimal(40*60),contractTask));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,material2,new BigDecimal(-1),contractTask));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(1000),contractTask));
		
		//план за изпълнение на договор
		deadlineTime.add(Calendar.MONTH, 1);
		TaskStatus statusPlanned = (TaskStatus) dbload.trySave(new TaskStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Планирана",TaskStatus.TASK_STATUS_PLANNED,false));
		TaskRelationType typePlan = (TaskRelationType) dbload.trySave(new TaskRelationType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"план","план за",TaskRelationType.TASK_RELATION_TYPE_PLAN));
		TaskType typeGeneral = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Работна задача",null));
		
		Task projectTask = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeCRMProject, priorityMedium,
				"План-проект по Договор с Клиент 1","Да се въведе в експлоатация продукт В при Клиент 1",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typePlan,contractTask,projectTask));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"DB_DOC","Приемо-предавателен протокол",projectTask,null));
		
		Task phase1Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Подготовка на площадката","Подготовка на площадката",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,projectTask,phase1Task));
		dbload.trySave(new PlannedIncomeOrExpense(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(1100),phase1Task));
		Task phase2Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Монтаж","Монтаж",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,projectTask,phase2Task));
		dbload.trySave(new PlannedIncomeOrExpense(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(51100),phase2Task));
		Task phase3Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Въвеждане в експлоатация","Въвеждане в експлоатация",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,projectTask,phase3Task));
		dbload.trySave(new PlannedIncomeOrExpense(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(2000),phase3Task));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"DB_DOC","Приемо-предавателен протокол",phase3Task,null));
		
		Task phase11Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Транспорт на материали","Транспорт на материали",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase1Task,phase11Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker1,new BigDecimal(8*60),phase11Task));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,material1,new BigDecimal(-1),phase11Task));
		Task phase12Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Изграждане на площадка","Изграждане на площадка",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase1Task,phase12Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker2,new BigDecimal(8*60),phase12Task));
		Task phase13Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Качествен контрол на площадката","Качествен контрол на площадката",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase1Task,phase13Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,expert1,new BigDecimal(8*60),phase13Task));

		Task phase21Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Транспорт на продукта","Транспорт на продукта",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase2Task,phase21Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker1,new BigDecimal(8*60),phase21Task));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,productA,new BigDecimal(-1),phase21Task));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"DB_DOC","Приемо-предавателен протокол",phase21Task,null));
		Task phase22Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Монтаж на продукта","Монтаж на продукта",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase2Task,phase22Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker2,new BigDecimal(8*60),phase22Task));
		Task phase23Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Качествен контрол на продукта","Качествен контрол на продукта",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase2Task,phase23Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,expert1,new BigDecimal(8*60),phase23Task));

		Task phase31Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Пробен пуск","Пробен пуск",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase3Task,phase31Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,expert1,new BigDecimal(8*60),phase31Task));
		Task phase32Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Експлоатационен пуск","Експлоатационен пуск",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase3Task,phase32Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,expert1,new BigDecimal(8*60),phase32Task));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"DB_DOC","Приемо-предавателен протокол",phase32Task,null));
		
		//изпълнение на договор
		createTime.add(Calendar.WEEK_OF_MONTH, 3);
		deadlineTime = (Calendar)createTime.clone();
		deadlineTime.add(Calendar.MONTH, 1);
		
		projectTask = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeCRMProject, priorityMedium,
				"Проект по Договор с Клиент 1","Да се въведе в експлоатация продукт В при Клиент 1",userCTO,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,contractTask,projectTask));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"DB_DOC","Приемо-предавателен протокол",projectTask,null));
		
		phase1Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusFinished,typeGeneral, priorityMedium,
				"Подготовка на площадката","Подготовка на площадката",userCTO,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,projectTask,phase1Task));
		dbload.trySave(new PlannedIncomeOrExpense(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(1100),phase1Task));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"SANTA_INVOICE","Фактура",phase1Task,null));
		DBFile outWHMaterial1 = (DBFile) dbload.trySave(new DBFile(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Изписване от склад на Материал 1",null,"content".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Изписан материал",phase1Task,outWHMaterial1));
		dbload.trySave(new AttachableRevenuesAndExpenses(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,material1,new BigDecimal(-1),outWHMaterial1));
		DBFile invoicePhase1 = (DBFile) dbload.trySave(new DBFile(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Фактура 1",null,"content".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Фактура",phase1Task,invoicePhase1));
		dbload.trySave(new AttachableRevenuesAndExpenses(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(1100),invoicePhase1));
		phase2Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeGeneral, priorityMedium,
				"Монтаж","Монтаж",userCTO,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,projectTask,phase2Task));
		dbload.trySave(new PlannedIncomeOrExpense(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(51100),phase2Task));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"SANTA_INVOICE","Фактура",phase2Task,null));
		phase3Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Въвеждане в експлоатация","Въвеждане в експлоатация",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,projectTask,phase3Task));
		dbload.trySave(new PlannedIncomeOrExpense(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(2000),phase3Task));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"SANTA_INVOICE","Фактура",phase3Task,null));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"DB_DOC","Приемо-предавателен протокол",phase3Task,null));
		
		phase11Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusFinished,typeGeneral, priorityMedium,
				"Транспорт на материали","Транспорт на материали",worker1,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase1Task,phase11Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker1,new BigDecimal(8*60),phase11Task));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,material1,new BigDecimal(-1),phase11Task));
		startTime = (Calendar)createTime.clone();
		startTime.add(Calendar.DAY_OF_MONTH, 1);
		endTime = (Calendar)startTime.clone();
		endTime.add(Calendar.HOUR_OF_DAY, 2);
		dbload.trySave(new TimeSheetItem(worker1,Date.from(createTime.toInstant()),worker1,Date.from(createTime.toInstant()),false,managedCompany,worker1,Date.from(startTime.toInstant()),Date.from(endTime.toInstant()),"Транспорт",phase11Task));
		
		phase12Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusFinished,typeGeneral, priorityMedium,
				"Изграждане на площадка","Изграждане на площадка",worker2,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase1Task,phase12Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker2,new BigDecimal(8*60),phase12Task));
		startTime = (Calendar)endTime.clone();
		startTime.add(Calendar.HOUR_OF_DAY, 1);
		endTime = (Calendar)startTime.clone();
		endTime.add(Calendar.HOUR_OF_DAY, 4);
		dbload.trySave(new TimeSheetItem(worker2,Date.from(createTime.toInstant()),worker2,Date.from(createTime.toInstant()),false,managedCompany,worker2,Date.from(startTime.toInstant()),Date.from(endTime.toInstant()),"Положен труд",phase12Task));
		
		phase13Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusFinished,typeGeneral, priorityMedium,
				"Качествен контрол на площадката","Качествен контрол на площадката",expert1,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase1Task,phase13Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,expert1,new BigDecimal(8*60),phase13Task));
		startTime = (Calendar)endTime.clone();
		startTime.add(Calendar.HOUR_OF_DAY, 12);
		endTime = (Calendar)startTime.clone();
		endTime.add(Calendar.HOUR_OF_DAY, 1);
		dbload.trySave(new TimeSheetItem(expert1,Date.from(createTime.toInstant()),expert1,Date.from(createTime.toInstant()),false,managedCompany,expert1,Date.from(startTime.toInstant()),Date.from(endTime.toInstant()),"Инспекция",phase13Task));

		phase21Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeGeneral, priorityMedium,
				"Транспорт на продукта","Транспорт на продукта",worker1,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase2Task,phase21Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker1,new BigDecimal(8*60),phase21Task));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,productA,new BigDecimal(-1),phase21Task));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"DB_DOC","Приемо-предавателен протокол",phase21Task,null));
		phase22Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Монтаж на продукта","Монтаж на продукта",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase2Task,phase22Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker2,new BigDecimal(8*60),phase22Task));
		phase23Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Качествен контрол на продукта","Качествен контрол на продукта",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase2Task,phase23Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,expert1,new BigDecimal(8*60),phase23Task));

		phase31Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Пробен пуск","Пробен пуск",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase3Task,phase31Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,expert1,new BigDecimal(8*60),phase31Task));
		phase32Task = (Task) dbload.trySave(new Task(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeGeneral, priorityMedium,
				"Експлоатационен пуск","Експлоатационен пуск",null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(userCTO,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,phase3Task,phase32Task));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,expert1,new BigDecimal(8*60),phase32Task));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"DB_DOC","Приемо-предавателен протокол",phase32Task,null));
		
		//маркетингова кампания
		createTime.add(Calendar.DAY_OF_MONTH, 1);
		TaskStatus statusStarted = (TaskStatus) dbload.trySave(new TaskStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Започната",TaskStatus.TASK_STATUS_STARTED,false));

		Task campaignTask = (Task) dbload.trySave(new Task(salesManager,Date.from(createTime.toInstant()),salesManager,Date.from(createTime.toInstant()),false,managedCompany,statusStarted,typeCRMCampaign, priorityMedium,
				"Кампания за нов продукт В","На всички клиенти, които се интересуват от продукт А и Б да се изпрати брошура за продукт В",salesManager,null,Date.from(deadlineTime.toInstant())));
		DBFile brochureV = new DBFile(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Брошура за продукт В.txt",null,"content".getBytes(),"text/plain");
		dbload.trySave(brochureV);
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Брошура за изпращане",campaignTask,brochureV));
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Шаблон за писмо",campaignTask,mailTemplate));

		createTime.add(Calendar.MINUTE, 1);
		Task subTask = (Task) dbload.trySave(new Task(salesManager,Date.from(createTime.toInstant()),salesManager,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeCRMCommunication, priorityMedium,
				"Кампания за нов продукт В - Клиент 1","На всички клиенти, които се интересуват от продукт А и Б да се изпрати брошура за продукт В",
				null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Брошура за изпращане",subTask,brochureV));
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Шаблон за писмо",subTask,mailTemplate));
		dbload.trySave(new TaskRelation(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,campaignTask,subTask));
		subTask = (Task) dbload.trySave(new Task(salesManager,Date.from(createTime.toInstant()),salesManager,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeCRMCommunication, priorityMedium,
				"Кампания за нов продукт В - Клиент 2","На всички клиенти, които се интересуват от продукт А и Б да се изпрати брошура за продукт В",
				null,person2,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Брошура за изпращане",subTask,brochureV));
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Шаблон за писмо",subTask,mailTemplate));
		dbload.trySave(new TaskRelation(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,campaignTask,subTask));
		subTask = (Task) dbload.trySave(new Task(salesManager,Date.from(createTime.toInstant()),salesManager,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeCRMCommunication, priorityMedium,
				"Кампания за нов продукт В - Клиент 3","На всички клиенти, които се интересуват от продукт А и Б да се изпрати брошура за продукт В",
				null,person3,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Брошура за изпращане",subTask,brochureV));
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Шаблон за писмо",subTask,mailTemplate));
		dbload.trySave(new TaskRelation(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,campaignTask,subTask));
		subTask = (Task) dbload.trySave(new Task(salesManager,Date.from(createTime.toInstant()),salesManager,Date.from(createTime.toInstant()),false,managedCompany,statusPlanned,typeCRMCommunication, priorityMedium,
				"Кампания за нов продукт В - Клиент 4","На всички клиенти, които се интересуват от продукт А и Б да се изпрати брошура за продукт В",
				null,person4,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Брошура за изпращане",subTask,brochureV));
		dbload.trySave(new TaskAttachment(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Шаблон за писмо",subTask,mailTemplate));
		dbload.trySave(new TaskRelation(salesManager,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeChild,campaignTask,subTask));
		
		//успех от кампанията
		createTime.add(Calendar.DAY_OF_MONTH, 1);
		Calendar modifyTime = (Calendar)createTime.clone();
		modifyTime.add(Calendar.DAY_OF_MONTH, 10);

		Task requestTask = (Task) dbload.trySave(new Task(sales,Date.from(createTime.toInstant()),sales,Date.from(modifyTime.toInstant()),false,managedCompany,statusStarted,typeCRMRfO, priorityMedium,
				"Клиент 4 пита за продукт В","Клиент 4 ни е писал, че иска повече информация и оферта за продукт В",sales,person4,Date.from(deadlineTime.toInstant())));
		DBFile emailRequestFromClient4 = new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Писмо 543 до sales",null,"content".getBytes(),"text/plain");
		dbload.trySave(emailRequestFromClient4);
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Писмо - запитване",requestTask,emailRequestFromClient4));
		dbload.trySave(new LegalPersonAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Писмо - запитване",person4,emailRequestFromClient4));
		dbload.trySave(new TaskRelation(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeRelated,requestTask,subTask));

		createTime.add(Calendar.DAY_OF_MONTH, 10);
		DBFile requestFromClientDoc = new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Запитване за оферта 123",null,"content".getBytes(),"text/plain");
		dbload.trySave(requestFromClientDoc);
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Запитване за оферта в счетоводството",requestTask,requestFromClientDoc));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"SANTA_REQ_FOR_OFFER","Запитване за оферта в счетоводството",requestTask,requestFromClientDoc));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,productB,new BigDecimal(-1),requestTask));

		createTime.add(Calendar.DAY_OF_MONTH, 1);
		deadlineTime = (Calendar)createTime.clone();
		deadlineTime.add(Calendar.MONTH, 1);
		
		offerTask = (Task) dbload.trySave(new Task(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeCRMOffer, priorityMedium,
				"Оферта до Клиент4","Да се изпрати оферта за продукт В съгласно изискванията на клиента",sales,person4,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new TaskRelation(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,typeRelated,requestTask,offerTask));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Изисквания на клиента",offerTask,emailRequestFromClient4));
		DBFile offerClient4 = (DBFile) dbload.trySave(new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Оферта 123",null,"content".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Оферта в счетоводството",offerTask,offerClient4));
		dbload.trySave(new LegalPersonAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Оферта в счетоводството",person4,offerClient4));
		dbload.trySave(new TaskRequiredAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"SANTA_OFFER","Оферта в счетоводството",offerTask,offerClient4));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker1,new BigDecimal(8*60),offerTask));
		dbload.trySave(new PlannedTime(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,worker2,new BigDecimal(8*60),offerTask));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,material1,new BigDecimal(-2),offerTask));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,material2,new BigDecimal(-6),offerTask));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,productB,new BigDecimal(-1),offerTask));
		dbload.trySave(new PlannedIncomeOrExpense(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(36000),offerTask));

		WasteArea wasteArea1 = (WasteArea) dbload.trySave(new WasteArea(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Площадка №1", "Казанлък, Южна индустриална зона, сграда ВАТО", "043199059", "Иван Калчев", "info@wato.bg"));
		WasteArea wasteArea2 = (WasteArea) dbload.trySave(new WasteArea(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Площадка №2", "Казанлък, Южна индустриална зона, сграда ВАТО", "043199059", "Иван Калчев", "info@wato.bg"));
		WasteKind wasteKindProduction = (WasteKind) dbload.trySave(new WasteKind(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Производствен"));
		WasteKind wasteKindDanger = (WasteKind) dbload.trySave(new WasteKind(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "Опасен"));
		WasteType wasteType = (WasteType) dbload.trySave(new WasteType(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "120101", "Стърготини, стружки и изрезки от черни метали", wasteKindProduction, "Струговане, разпробиване и др."));
		WasteCostCenter wasteCostCenterAlpha = (WasteCostCenter) dbload.trySave(new WasteCostCenter(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "АЛФА МЕТАЛС ООД", "123736375", "№13-ДО-444-01"));
		WasteCostCenter wasteCostCenterBeta = (WasteCostCenter) dbload.trySave(new WasteCostCenter(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "БЕТА МЕТАЛС ООД", "573637321", "№14-ДО-445-02"));


		Calendar decDate = Calendar.getInstance(); decDate.set(2017,11,01);
		Calendar janDate = Calendar.getInstance(); janDate.set(2017,12,01);
		Calendar febDate = Calendar.getInstance(); febDate.set(2018,01,01);
		Calendar marDate = Calendar.getInstance(); marDate.set(2018,02,01);
		Calendar aprDate = Calendar.getInstance(); aprDate.set(2018,03,01);
		Calendar mayDate = Calendar.getInstance(); mayDate.set(2018,04,01);
		Calendar junDate = Calendar.getInstance(); junDate.set(2018,05,01);

		dbload.trySave(new Waste(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, Date.from(decDate.toInstant()), new BigDecimal(100),new BigDecimal(20), Date.from(decDate.toInstant()), wasteCostCenterAlpha, wasteType));
		dbload.trySave(new Waste(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, Date.from(janDate.toInstant()), new BigDecimal(100),new BigDecimal(20), Date.from(janDate.toInstant()), wasteCostCenterAlpha, wasteType));
		dbload.trySave(new Waste(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, Date.from(febDate.toInstant()), new BigDecimal(100),new BigDecimal(20), Date.from(febDate.toInstant()), wasteCostCenterAlpha, wasteType));
		dbload.trySave(new Waste(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, Date.from(marDate.toInstant()), new BigDecimal(100),new BigDecimal(20), Date.from(marDate.toInstant()), wasteCostCenterAlpha, wasteType));
		dbload.trySave(new Waste(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, Date.from(aprDate.toInstant()), new BigDecimal(100),new BigDecimal(20), Date.from(aprDate.toInstant()), wasteCostCenterAlpha, wasteType));
		dbload.trySave(new Waste(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, Date.from(mayDate.toInstant()), new BigDecimal(100),new BigDecimal(20), Date.from(mayDate.toInstant()), wasteCostCenterBeta, wasteType));
		dbload.trySave(new Waste(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, Date.from(junDate.toInstant()), new BigDecimal(100),new BigDecimal(20), Date.from(junDate.toInstant()), wasteCostCenterBeta, wasteType));

		dbload.trySave(new DictionaryTerm(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "term 1", "Just a long test meaning, which describes the term 1",corporateDict));
		dbload.trySave(new DictionaryTerm(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "term 2", "Just a long test meaning, which describes the term 1",corporateDict));
		dbload.trySave(new DictionaryTerm(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "term 3", "Just a long test meaning, which describes the term 1",corporateDict));
		dbload.trySave(new DictionaryTerm(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false,managedCompany, "term 4", "Just a long test meaning, which describes the term 1",corporateDict));

		createTime = Calendar.getInstance();
		createTime.set(2017,12,15,12,00);
		deadlineTime.set(2018,12,15,12,00);

		Task trainingPlanTask = (Task) dbload.trySave(new Task(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeTrainingPlan, priorityMedium,
				"План за обученията 2018","Да се изготвят и одобрят план и бюджет за обученията през 2018 година.",secretary,null,Date.from(deadlineTime.toInstant())));
		dbload.trySave(new PlannedIncomeOrExpense(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(-10000),trainingPlanTask));

		createTime.add(Calendar.DAY_OF_WEEK, 1);
		dbload.trySave(new Comment(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,"Нужно е да подобрим знанията по немски език във връзка с новите възможности в Германия",trainingPlanTask));
		createTime.add(Calendar.DAY_OF_WEEK, 1);
		dbload.trySave(new Comment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Нямаме опит със социалните мрежи - добре ще е да имаме и такова обучение",trainingPlanTask));
		createTime.add(Calendar.DAY_OF_WEEK, 1);
		dbload.trySave(new Comment(worker1,Date.from(createTime.toInstant()),worker1,Date.from(createTime.toInstant()),false,managedCompany,"На новата машина ESRG не умеем да ползваме по-сложните функции и ако може да извикаме експерти да ни покажат",trainingPlanTask));
		
		startTime = (Calendar)createTime.clone();
		startTime.set(2018,07,15,9,0);
		endTime = (Calendar)startTime.clone();
		endTime.add(Calendar.HOUR_OF_DAY, 8);

		Task trainingTask = (Task) dbload.trySave(new Task(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeTraining, priorityMedium,
				"Обучение маркетинг в социални мрежи","Организиране и провеждане на обучение по маркетинг в социалните мрежи.",secretary,null,null));
		dbload.trySave(new PlannedIncomeOrExpense(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(-3000),trainingTask));
		dbload.trySave(new TaskRelation(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,typeChild,trainingPlanTask,trainingTask));

		Task employeeTrainingTask = (Task) dbload.trySave(new Task(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeEmployeeTraining, priorityMedium,
				"Обучение маркетинг в социални мрежи - Ангел Великов","",userCTO,null,null));
		dbload.trySave(new TaskRelation(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,typeChild,trainingTask,employeeTrainingTask));
		dbload.trySave(new TimeSheetItem(userCTO,Date.from(endTime.toInstant()),userCTO,Date.from(endTime.toInstant()),false,managedCompany,userCTO,Date.from(startTime.toInstant()),Date.from(endTime.toInstant()),"Обучение",employeeTrainingTask));
		DBFile trainingCertificate = (DBFile) dbload.trySave(new DBFile(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,"Сертификат за преминато обучение 1",null,"Certificate 1".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(userCTO,Date.from(createTime.toInstant()),userCTO,Date.from(createTime.toInstant()),false,managedCompany,"Сертификат",employeeTrainingTask,trainingCertificate));

		employeeTrainingTask = (Task) dbload.trySave(new Task(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeEmployeeTraining, priorityMedium,
				"Обучение маркетинг в социални мрежи - Калина Гърдева","",sales,null,null));
		dbload.trySave(new TaskRelation(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,typeChild,trainingTask,employeeTrainingTask));
		dbload.trySave(new TimeSheetItem(sales,Date.from(endTime.toInstant()),sales,Date.from(endTime.toInstant()),false,managedCompany,sales,Date.from(startTime.toInstant()),Date.from(endTime.toInstant()),"Обучение",employeeTrainingTask));
		trainingCertificate = (DBFile) dbload.trySave(new DBFile(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Сертификат за преминато обучение 2",null,"Certificate 2".getBytes(),"text/plain"));
		dbload.trySave(new TaskAttachment(sales,Date.from(createTime.toInstant()),sales,Date.from(createTime.toInstant()),false,managedCompany,"Сертификат",employeeTrainingTask,trainingCertificate));

		trainingTask = (Task) dbload.trySave(new Task(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeTraining, priorityMedium,
				"Обучение по немски език","Организиране и провеждане на обучение по немски език.",secretary,null,null));
		dbload.trySave(new PlannedIncomeOrExpense(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(-1000),trainingTask));
		dbload.trySave(new TaskRelation(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,typeChild,trainingPlanTask,trainingTask));

		trainingTask = (Task) dbload.trySave(new Task(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,statusAssigned,typeTraining, priorityMedium,
				"Обучение за работа с новата машина ESRG500","Организиране и провеждане на обучение за работа с новата машина ESRG500.",secretary,null,null));
		dbload.trySave(new PlannedIncomeOrExpense(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(-5000),trainingTask));
		dbload.trySave(new TaskRelation(secretary,Date.from(createTime.toInstant()),secretary,Date.from(createTime.toInstant()),false,managedCompany,typeChild,trainingPlanTask,trainingTask));
		
	}

}
