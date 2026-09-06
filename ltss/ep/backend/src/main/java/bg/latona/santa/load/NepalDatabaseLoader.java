package bg.latona.santa.load;

import java.util.Calendar;
import java.util.Date;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.support.Repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.mail.MailAccount;
import bg.latona.santa.entities.mail.MailTemplate;
import bg.latona.santa.entities.nepal.LoiContractFee;
import bg.latona.santa.entities.nepal.LoiContractPrice;
import bg.latona.santa.entities.nepal.LoiContractQuantity;
import bg.latona.santa.entities.nepal.LoiContractStatus;
import bg.latona.santa.entities.nepal.LoiGrid;
import bg.latona.santa.entities.nepal.LoiMaxLoadMWSeason;
import bg.latona.santa.entities.nepal.LoiMaxLoadWeather;
import bg.latona.santa.entities.nepal.LoiNotificationType;
import bg.latona.santa.entities.nepal.LoiProtocolCountPerMonth;
import bg.latona.santa.entities.nepal.LoiProtocolLineCount;
import bg.latona.santa.entities.nepal.LoiProtocolStatus;
import bg.latona.santa.entities.nepal.LoiTypeOfPowerPlant;
import bg.latona.santa.entities.security.SecRole;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.security.SecUserRole;
import bg.latona.santa.integration.DynamicRouteStarter;
import bg.latona.santa.reports.ReportsRepository;
import bg.latona.santa.repositories.SecUserRepository;

public class NepalDatabaseLoader {
	
	private static Logger logger = LoggerFactory.getLogger(NepalDatabaseLoader.class);

	public static void load(DatabaseLoader dbload, ManagedCompany managedCompany, Repositories repositories, CamelContext camelContext, ReportsRepository reportsRepository, Environment env) {
		logger.info("Starting NEPAL data loading to database");
		Calendar createTime = Calendar.getInstance();
		createTime.set(2020,10,01,12,00);
		SecUser admin = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin1");


		dbload.trySave(new LoiTypeOfPowerPlant(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Солар", LoiTypeOfPowerPlant.SOLAR_POWER_PLANT));
		dbload.trySave(new LoiTypeOfPowerPlant(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Вятърен парк", LoiTypeOfPowerPlant.WIND_FARM_POWER_PLANT));
		dbload.trySave(new LoiTypeOfPowerPlant(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Водна централа", LoiTypeOfPowerPlant.HYDROELECTRIC_POWER_PLANT));
		dbload.trySave(new LoiTypeOfPowerPlant(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Биомаса", LoiTypeOfPowerPlant.BIOMASS_POWER_PLANT));
		dbload.trySave(new LoiTypeOfPowerPlant(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Когенерация", LoiTypeOfPowerPlant.COGENERATION_POWER_PLANT));

		dbload.trySave(new LoiGrid(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Високо напрежение", LoiGrid.HIGH_VOLTAGE));
		dbload.trySave(new LoiGrid(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Средно напрежение", LoiGrid.MID_VOLTAGE));

		dbload.trySave(new LoiMaxLoadWeather(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Слънчево", LoiMaxLoadWeather.SUNNY));
		dbload.trySave(new LoiMaxLoadWeather(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Облачно", LoiMaxLoadWeather.CLOUDY));
		dbload.trySave(new LoiMaxLoadWeather(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Мъгливо", LoiMaxLoadWeather.FOG));
		dbload.trySave(new LoiMaxLoadWeather(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Дъждовно", LoiMaxLoadWeather.RAINING));
		dbload.trySave(new LoiMaxLoadWeather(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Снежно", LoiMaxLoadWeather.SNOWING));

		dbload.trySave(new LoiNotificationType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Импорт", LoiNotificationType.NOTIFICATION_TYPE_IMPORT));

		dbload.trySave(new LoiMaxLoadMWSeason(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Пролет", LoiMaxLoadMWSeason.SPRING));
		dbload.trySave(new LoiMaxLoadMWSeason(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Лято", LoiMaxLoadMWSeason.SUMMER));
		dbload.trySave(new LoiMaxLoadMWSeason(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Есен", LoiMaxLoadMWSeason.AUTUMN));
		dbload.trySave(new LoiMaxLoadMWSeason(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Зима", LoiMaxLoadMWSeason.WINTER));

		dbload.trySave(new LoiContractStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Не подписан", LoiContractStatus.NOT_SIGNED));
		dbload.trySave(new LoiContractStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Подписан", LoiContractStatus.SIGNED));
		dbload.trySave(new LoiContractStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Очаква се анекс", LoiContractStatus.AN_ANNEX_IS_EXPECTED));

		dbload.trySave(new LoiContractQuantity(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Количество по електромер", LoiContractQuantity.AMOUNT_PER_ELECTRICITY_METER));
		dbload.trySave(new LoiContractQuantity(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Количество по график", LoiContractQuantity.SCHEDULE_QUANTITY));

		dbload.trySave(new LoiContractPrice(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Фиксирана цена", LoiContractPrice.FIX_PRICE));
		dbload.trySave(new LoiContractPrice(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Цена IBEX", LoiContractPrice.IBEX_PRICE));

		dbload.trySave(new LoiContractFee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Такса/процент", LoiContractFee.FEE_PERCENTAGE));
		dbload.trySave(new LoiContractFee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Фиксирана такса", LoiContractFee.FIXED_FEE));
		dbload.trySave(new LoiContractFee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Няма такса", LoiContractFee.NO_FEE));

		dbload.trySave(new LoiProtocolCountPerMonth(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"За цял месец", LoiProtocolCountPerMonth.FOR_A_WHOLE_MONTH));
		dbload.trySave(new LoiProtocolCountPerMonth(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"За 15 дни", LoiProtocolCountPerMonth.FOR_FIFTEEN_DAYS));
		dbload.trySave(new LoiProtocolCountPerMonth(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"за 10 дни", LoiProtocolCountPerMonth.FOR_TEN_DAYS));

		dbload.trySave(new LoiProtocolLineCount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Протокол с един ред", LoiProtocolLineCount.ONE_LINE));
		dbload.trySave(new LoiProtocolLineCount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Протокол с три реда", LoiProtocolLineCount.THREE_LINES));

		dbload.trySave(new LoiProtocolStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Няма протокол", LoiProtocolStatus.THERE_IS_NO_PROTOCOL));
		dbload.trySave(new LoiProtocolStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Генериран протокол", LoiProtocolStatus.GENERATED_PROTOCOL));
		dbload.trySave(new LoiProtocolStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Изтеглен протокол", LoiProtocolStatus.DOWNLOADED_PROTOCOL));
		dbload.trySave(new LoiProtocolStatus(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Подписан протокол", LoiProtocolStatus.SIGNED_PROTOCOL));


		dbload.trySave(new MailTemplate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Email to ESO",null,"Mail subject","Mail Content", "redzhep.ali@latona.bg", MailTemplate.MAIL_TEMPLATE_FOR_ESO));
	//	dbload.trySave(new MailTemplate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Email to ESO",null,"Mail subject","Mail Content", "trade@finvest.bg", MailTemplate.MAIL_TEMPLATE_FOR_ESO));
		dbload.trySave(new MailTemplate(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Sent protocols to LegalPerson",null,"Protocols mail subject","Protocols mail content", "redzhep.ali@latona.bg", MailTemplate.MAIL_TEMPLATE_FOR_PROTOCOLS));

//		//trySave(new MailAccount(null,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),indParts,"Test Latona","latona.bg","mail.latona.bg","test@latona.bg","Lat0n@test",180000,"INBOX","INBOX.Sent",null,false,false,true,true));
//		trySave(new MailAccount(null,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),wato,"Test Nadia","imap.gmail.com","smtp.gmail.com","test.nadia@wato.bg","zapspzedep12",180000,"INBOX","[Gmail]/Sent Mail",null,false,false,true,true));
//		//trySave(new MailAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),"Test Svilen","imap.gmail.com","test.svilen@wato.bg","zedepzapsp12",180000));
	//	dbload.trySave(new MailAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Test Latona","latona.bg","mail.latona.bg","test@latona.bg","Lat0n@test",180000,"INBOX","INBOX.Sent",null,false,false,true,true));
		dbload.trySave(new MailAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Test Finvest","kmail.bg","kmail.bg","m.shiderska@finvest.bg","mshiderska@FI0899",180000,"INBOX","INBOX.Sent",null,false,false,true,true));
		//dbload.trySave(new MailAccount(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Test Send Latona","imap.gmail.com","smtp.gmail.com","latonasend@gmail.com","Latona123!",180000,"INBOX","[Gmail]/Sent Mail",null,false,false,true,true));

		if( camelContext.getRoute("authToken") == null ) {
			SecUser camelUser = (SecUser) dbload.trySave(new SecUser(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"camel1","Apache Camel","123",null,"AC"));
			SecRole roleAdmin = (SecRole) dbload.trySave(new SecRole(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Пълен достъп",1L));
			dbload.trySave(new SecUserRole(admin,Date.from(createTime.toInstant()),null,Date.from(createTime.toInstant()),false,managedCompany,camelUser,roleAdmin));
	
			DynamicRouteStarter routeStarter = new DynamicRouteStarter(repositories, camelContext, env);
			try {
				routeStarter.run(new String[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void loadTest(DatabaseLoader dbLoad, ManagedCompany managedCompany) {
		logger.info("Starting NEPAL test data loading to database");

	}
}
