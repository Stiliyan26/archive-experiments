package bg.latona.santa.anvoice;

import java.util.Calendar;
import java.util.Date;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecRole;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.security.SecUserRole;
import bg.latona.santa.entities.selfie.AccountingPeriod;
import bg.latona.santa.entities.selfie.LoiAgreementStatus;
import bg.latona.santa.entities.selfie.LoiDocumentType;
import bg.latona.santa.entities.selfie.LoiMeasurementUnit;
import bg.latona.santa.entities.selfie.LoiReasonForTermination;
import bg.latona.santa.entities.selfie.LoiStatusCode;
import bg.latona.santa.entities.selfie.LoiTypeOFService;
import bg.latona.santa.integration.AuthorizationRouteBuilder;
import bg.latona.santa.load.DatabaseLoader;
import bg.latona.santa.repositories.SecUserRepository;

public class AnvoiceDatabaseLoader {

	private static Logger logger = LoggerFactory.getLogger(AnvoiceDatabaseLoader.class);

	public static void load(DatabaseLoader dbload, ManagedCompany managedCompany, CamelContext camelContext, WebApplicationContext appContext, Environment env) throws Exception { // , Repositories repositories, CamelContext camelContext, ReportsRepository reportsRepository, Environment env
		logger.info("Starting Anvoice data loading to database");
		Calendar createTime = Calendar.getInstance();
		createTime.set(2020,10,01,12,00);

		SecUser admin = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin1");

		dbload.trySave(new LoiDocumentType(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Фактура", LoiDocumentType.INVOICE));
		dbload.trySave(new LoiDocumentType(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Дебитно известие", LoiDocumentType.DEBIT_NOTE));
		dbload.trySave(new LoiDocumentType(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Кредитно известие", LoiDocumentType.CREDIT_NOTE));

		//dbload.trySave(new LoiMeasurementUnit(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"MWh", LoiMeasurementUnit.MWh));
		dbload.trySave(new LoiMeasurementUnit(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"kWh", LoiMeasurementUnit.kWh));

		dbload.trySave(new LoiTypeOFService(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Произведено количество", LoiTypeOFService.PRODUCED));
		dbload.trySave(new LoiTypeOFService(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Прогнозно количество", LoiTypeOFService.PLANNED));
		dbload.trySave(new LoiTypeOFService(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Енергиен излишък", LoiTypeOFService.EXCESS));
		dbload.trySave(new LoiTypeOFService(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Произведено количество - включен разход за небаланс", LoiTypeOFService.BALANCE));
		dbload.trySave(new LoiTypeOFService(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Произведено количество - фактура 90", LoiTypeOFService.BALANCE90));
		dbload.trySave(new LoiTypeOFService(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Прогнозно количество – чл.100, ал.6 от ЗЕ", LoiTypeOFService.PLANNED_100_6));
		dbload.trySave(new LoiTypeOFService(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Енергиен излишък – чл.100, ал.6 от ЗЕ", LoiTypeOFService.EXCESS_100_6));

		dbload.trySave(new LoiReasonForTermination(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Отказ на клиента от услугата", LoiReasonForTermination.CUSTOMER_REFUSAL_OF_THE_SERVICE));
		dbload.trySave(new LoiReasonForTermination(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"По наша инициатива", LoiReasonForTermination.OWN_INITIATIVE));
		dbload.trySave(new LoiReasonForTermination(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Промяна на регистрацията по ДДС", LoiReasonForTermination.VAT_NUMBER_REGISTRATION_CHANGE));
		dbload.trySave(new LoiReasonForTermination(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Прекратяване на основния договор", LoiReasonForTermination.TERMINATION_OF_THE_MAIN_CONTRACT));

		dbload.trySave(new LoiAgreementStatus(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Подписан", LoiAgreementStatus.SIGNED));
		dbload.trySave(new LoiAgreementStatus(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Бъдещ", LoiAgreementStatus.FUTURE));
		dbload.trySave(new LoiAgreementStatus(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Активен", LoiAgreementStatus.ACTIVE));
		dbload.trySave(new LoiAgreementStatus(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Изчакване на разрешение за използване", LoiAgreementStatus.WAITING_FOR_PERMISSION_TO_USE));
		dbload.trySave(new LoiAgreementStatus(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Отказан от клиента", LoiAgreementStatus.DECLINED_FROM_CUSTOMER));
		dbload.trySave(new LoiAgreementStatus(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Изтекъл", LoiAgreementStatus.EXPIRED));
		dbload.trySave(new LoiAgreementStatus(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Прекратен", LoiAgreementStatus.TERMINATED));

		dbload.trySave(new LoiStatusCode(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Създаден", LoiStatusCode.CREATED));
		dbload.trySave(new LoiStatusCode(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Активен", LoiStatusCode.ACTIVE));
		dbload.trySave(new LoiStatusCode(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Деактивиран", LoiStatusCode.DEACTIVATED));

		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Януари", "1"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Февруари", "2"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Март", "3"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Април", "4"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Май", "5"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Юни", "6"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Юли", "7"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Август", "8"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Септември", "9"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Октомври", "10"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Ноември", "11"));
		dbload.trySave(new AccountingPeriod(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Декември", "12"));

		String crmHost = env.getProperty("santa.integration.CRM_HOST");

		if( camelContext.getRoute("authToken") == null ) {
			SecUser camelUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"camel1","Apache Camel","123",null,"AC"));
			SecRole roleAdmin = (SecRole) dbload.trySave(new SecRole(admin,Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Пълен достъп",1L));
			dbload.trySave(new SecUserRole(admin, Date.from(createTime.toInstant()),null, Date.from(createTime.toInstant()),false, managedCompany, camelUser, roleAdmin));
	
			camelContext.addRoutes(new AuthorizationRouteBuilder(1L, crmHost));
		}

		AnvoiceProcedures anvoiceProcedures = appContext.getBean(AnvoiceProcedures.class);

		camelContext.addRoutes(new AnvoiceRouteBuilder(anvoiceProcedures, crmHost));
	}

	public static void loadTest(DatabaseLoader dbLoad, ManagedCompany managedCompany) {
		logger.info("Starting Anvoice test data loading to database");
	}
}
