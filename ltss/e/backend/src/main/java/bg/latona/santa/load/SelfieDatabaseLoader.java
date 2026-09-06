package bg.latona.santa.load;

import bg.latona.santa.entities.BankAccount;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.Contact;
import bg.latona.santa.entities.selfie.*;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecRole;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.security.SecUserRole;
import bg.latona.santa.repositories.SecUserRepository;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelfieDatabaseLoader {

	private static Logger logger = LoggerFactory.getLogger(SelfieDatabaseLoader.class);

	public static void load(DatabaseLoader dbload, ManagedCompany managedCompany) { // , Repositories repositories, CamelContext camelContext, ReportsRepository reportsRepository, Environment env
		logger.info("Starting selfie data loading to database");
		Calendar createTime = Calendar.getInstance();
		createTime.set(2020,10,01,12,00);

		SecUser admin = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin1");

		dbload.trySave(new LoiDocumentType(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Фактура", LoiDocumentType.INVOICE));
		dbload.trySave(new LoiDocumentType(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Дебитно известие", LoiDocumentType.DEBIT_NOTE));
		dbload.trySave(new LoiDocumentType(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Кредитно известие", LoiDocumentType.CREDIT_NOTE));

		dbload.trySave(new LoiMeasurementUnit(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"MWh", LoiMeasurementUnit.MWh));
		dbload.trySave(new LoiMeasurementUnit(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"kWh", LoiMeasurementUnit.kWh));

		dbload.trySave(new LoiTypeOFService(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Енергия по график", LoiTypeOFService.SCHEDULED_ENERGY));
		dbload.trySave(new LoiTypeOFService(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Измерена енергия", LoiTypeOFService.METERED_ENERGY));

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

//		SecUser camelUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"camel1","Apache Camel","123",null,"AC"));
		SecRole roleAdmin = (SecRole) dbload.trySave(new SecRole(admin,Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()),false, managedCompany,"Пълен достъп",1L));
		dbload.trySave(new SecUserRole(admin, Date.from(createTime.toInstant()),null, Date.from(createTime.toInstant()),false, managedCompany, null, roleAdmin));


		LegalPerson legalPerson = new LegalPerson(admin, Date.from(createTime.toInstant()), admin, Date.from(createTime.toInstant()), false, managedCompany, "ЕВН ТРЕЙДИНГ САУТ ИЙСТ ЮРЪП ЕАД", "175370769", "EGN", "BG175370769", "", "БУЛ. ЦАР ОСВОБОДИТЕЛ 14 ЕТ.3 1000 ГР. СОФИЯ", "1000", "", "България", "София", null, null);
		legalPerson.setEik("175370769");
		legalPerson.setStreet("бул. Цар Освободител 14");
		legalPerson.setIsSender(true);
		dbload.trySave(legalPerson);

//		DynamicRouteStarter routeStarter = new DynamicRouteStarter(repositories, camelContext, env);
//		try {
//			routeStarter.run(new String[0]);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void loadTest(DatabaseLoader dbLoad, ManagedCompany managedCompany) {
		logger.info("Starting selfie test data loading to database");
	}
}
