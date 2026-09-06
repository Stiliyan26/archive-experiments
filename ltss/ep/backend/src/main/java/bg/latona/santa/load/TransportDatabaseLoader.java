package bg.latona.santa.load;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bg.latona.santa.entities.AttachableRevenuesAndExpenses;
import bg.latona.santa.entities.BankAccount;
import bg.latona.santa.entities.Expenditure;
import bg.latona.santa.entities.LoiExpenditureType;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationType;
import bg.latona.santa.entities.article.ArticleService;
import bg.latona.santa.entities.article.Currency;
import bg.latona.santa.entities.employee.CompanyDepartment;
import bg.latona.santa.entities.employee.Employee;
import bg.latona.santa.entities.employee.JobPosition;
import bg.latona.santa.entities.invoice.Invoice;
import bg.latona.santa.entities.invoice.InvoiceRow;
import bg.latona.santa.entities.invoice.LoiPaymentType;
import bg.latona.santa.entities.invoice.LoiVatExemptionReason;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.person.LoiLegalPersonType;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.task.TaskPriority;
import bg.latona.santa.entities.task.TaskRelation;
import bg.latona.santa.entities.task.TaskRelationType;
import bg.latona.santa.entities.task.TaskStatus;
import bg.latona.santa.entities.task.TaskType;
import bg.latona.santa.entities.transport.LoiTransportType;
import bg.latona.santa.entities.transport.LoiVehicleType;
import bg.latona.santa.entities.transport.ShippingContainerType;
import bg.latona.santa.entities.transport.Transport;
import bg.latona.santa.entities.transport.TransportOrder;
import bg.latona.santa.entities.transport.Vehicle;
import bg.latona.santa.repositories.*;

public class TransportDatabaseLoader {
	
	private static Logger logger = LoggerFactory.getLogger(TransportDatabaseLoader.class);

	public static void load(DatabaseLoader dbload, ManagedCompany managedCompany) {
		logger.info("Starting transport data loading to database");
		Calendar createTime = Calendar.getInstance();
		createTime.set(2020,10,01,12,00);
		SecUser admin = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin1");
		
		LoiVehicleType loiVehicleTypeTruck = (LoiVehicleType) dbload.trySave(new LoiVehicleType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Влекач",LoiVehicleType.VEHICLE_TYPE_TRACTOR_TRUCK));
		LoiVehicleType loiVehicleTypeTrailer = (LoiVehicleType) dbload.trySave(new LoiVehicleType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ремарке",LoiVehicleType.VEHICLE_TYPE_TRAILER));

		LoiTransportType loiTransportTypeImport = (LoiTransportType) dbload.trySave(new LoiTransportType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Внос",LoiTransportType.TRANSPORT_TYPE_IMPORT));
		dbload.trySave(new LoiTransportType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Износ",LoiTransportType.TRANSPORT_TYPE_EXPORT));
		dbload.trySave(new LoiTransportType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Вътрешен курс",LoiTransportType.TRANSPORT_TYPE_INTRA));
		dbload.trySave(new LoiTransportType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разтоварване",LoiTransportType.TRANSPORT_TYPE_UNLOAD));
		dbload.trySave(new LoiTransportType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Връща празни",LoiTransportType.TRANSPORT_TYPE_EMPTY));

		LoiExpenditureType loiExpenditureTypeMaintenance = (LoiExpenditureType) dbload.trySave(new LoiExpenditureType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Поддръжка",LoiExpenditureType.EXPENDITURE_TYPE_MAINTENANCE));
		dbload.trySave(new LoiExpenditureType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разходи по курс",LoiExpenditureType.EXPENDITURE_TYPE_TASK));
		dbload.trySave(new LoiExpenditureType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Административни",LoiExpenditureType.EXPENDITURE_TYPE_SHARED));

		LoiLegalPersonType loiLegalPersonTypeRequester = (LoiLegalPersonType) dbload.trySave(new LoiLegalPersonType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Поръчител",LoiLegalPersonType.LEGAL_PERSON_TYPE_REQUESTER));
		LoiLegalPersonType loiLegalPersonTypeSender = (LoiLegalPersonType) dbload.trySave(new LoiLegalPersonType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Изпращач",LoiLegalPersonType.LEGAL_PERSON_TYPE_SENDER));
		LoiLegalPersonType loiLegalPersonTypeTransporter = (LoiLegalPersonType) dbload.trySave(new LoiLegalPersonType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Превозвач",LoiLegalPersonType.LEGAL_PERSON_TYPE_TRANSPORTER));

		dbload.trySave(new AllocationType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разходи към курс","expenditures","transports"));
		dbload.trySave(new AllocationType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разходи към влекач","expenditures","vehicles"));
		dbload.trySave(new AllocationType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разходи към шофьор","expenditures","employees"));

		ArticleService transportService = (ArticleService) dbload.trySave(new ArticleService(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Транспорт",null,null,null,null));
		ArticleService standByService = (ArticleService) dbload.trySave(new ArticleService(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Престой",null,null,null,null));
		ArticleService unLoadAndLoadByService = (ArticleService) dbload.trySave(new ArticleService(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Претоварване",null,null,null,null));

		TaskType typeTransportOrder = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Заявка за транспорт",TaskType.TASK_TYPE_TRANSPORT_ORDER));
		TaskType typeTransport = (TaskType) dbload.trySave(new TaskType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Курс на транспорт",TaskType.TASK_TYPE_TRANSPORT));

		ShippingContainerType shippingContainerType40HC = (ShippingContainerType) dbload.trySave(new ShippingContainerType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"40HC","40HC"));
		dbload.trySave(new ShippingContainerType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"40DV","40DV"));
		dbload.trySave(new ShippingContainerType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"20HC","20HC"));
		dbload.trySave(new ShippingContainerType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"20DV","20DV"));
		dbload.trySave(new ShippingContainerType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"40OT","40OT"));
		dbload.trySave(new ShippingContainerType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"20OT","20OT"));
		dbload.trySave(new ShippingContainerType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"40RF","40RF"));
		dbload.trySave(new ShippingContainerType(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"20RF","20RF"));
	}
	
	public static void loadTest(DatabaseLoader dbload, ManagedCompany managedCompany) {
		logger.info("Starting transport test data loading to database");
		SecUser admin = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin1");
		SecUser otherUser = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("mmarkov1");

		Calendar createTime = Calendar.getInstance();
		createTime.set(2020,10,01,12,00);
		
		TaskStatus statusPlanned = ((TaskStatusRepository) dbload.getRepositories().getRepositoryFor(TaskStatus.class).get()).findFirstByCodeAndCompany(TaskStatus.TASK_STATUS_PLANNED, managedCompany);
		TaskType typeTransportOrder = ((TaskTypeRepository) dbload.getRepositories().getRepositoryFor(TaskType.class).get()).findFirstByCodeAndCompanyAndDeleted(TaskType.TASK_TYPE_TRANSPORT_ORDER, managedCompany, false);
		TaskPriority priorityMedium = ((TaskPriorityRepository) dbload.getRepositories().getRepositoryFor(TaskPriority.class).get()).findFirstByCodeAndCompanyAndDeleted(TaskPriority.TASK_PRIORITY_MEDIUM, managedCompany, false);
		LoiTransportType loiTransportTypeImport = ((LoiTransportTypeRepository) dbload.getRepositories().getRepositoryFor(LoiTransportType.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiTransportType.TRANSPORT_TYPE_IMPORT, managedCompany, false);
		LoiVehicleType loiVehicleTypeTruck = ((LoiVehicleTypeRepository) dbload.getRepositories().getRepositoryFor(LoiVehicleType.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiVehicleType.VEHICLE_TYPE_TRACTOR_TRUCK, managedCompany, false);
		LoiVehicleType loiVehicleTypeTrailer = ((LoiVehicleTypeRepository) dbload.getRepositories().getRepositoryFor(LoiVehicleType.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiVehicleType.VEHICLE_TYPE_TRAILER, managedCompany, false);
		ShippingContainerType shippingContainerType40HC = ((ShippingContainerTypeRepository) dbload.getRepositories().getRepositoryFor(ShippingContainerType.class).get()).findFirstByCodeAndCompanyAndDeleted("40HC", managedCompany, false);
		Currency currencyEUR = ((CurrencyRepository) dbload.getRepositories().getRepositoryFor(Currency.class).get()).findFirstByNameAndCompany("EUR", managedCompany);
		Currency currencyBGN = ((CurrencyRepository) dbload.getRepositories().getRepositoryFor(Currency.class).get()).findFirstByNameAndCompany("BGN", managedCompany);
		LoiExpenditureType loiExpenditureTypeMaintenance = ((LoiExpenditureTypeRepository) dbload.getRepositories().getRepositoryFor(LoiExpenditureType.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiExpenditureType.EXPENDITURE_TYPE_MAINTENANCE, managedCompany, false);
		LegalPerson legalPersonOwn = ((LegalPersonRepository) dbload.getRepositories().getRepositoryFor(LegalPerson.class).get()).findFirstByEikAndCompanyAndDeleted("204074306", managedCompany, false);
		LegalPerson person2 = ((LegalPersonRepository) dbload.getRepositories().getRepositoryFor(LegalPerson.class).get()).findFirstByEikAndCompanyAndDeleted("103612886", managedCompany, false);
		TaskRelationType typeChild = ((TaskRelationTypeRepository) dbload.getRepositories().getRepositoryFor(TaskRelationType.class).get()).findFirstByCodeAndCompanyAndDeleted(TaskRelationType.TASK_RELATION_TYPE_SUBTASK, managedCompany, false);
		LoiPaymentType loiPaymentTypeBank = ((LoiPaymentTypeRepository) dbload.getRepositories().getRepositoryFor(LoiPaymentType.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPaymentType.PAYMENT_TYPE_WIRE_TRANSFER, managedCompany, false);
		LoiVatExemptionReason loiVatExemptionReasonNone = ((LoiVatExemptionReasonRepository) dbload.getRepositories().getRepositoryFor(LoiVatExemptionReason.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiVatExemptionReason.VAT_EXEMPTION_REASON_NONE, managedCompany, false);
		ArticleService transportService = ((ArticleServiceRepository) dbload.getRepositories().getRepositoryFor(ArticleService.class).get()).findAll().iterator().next();
		BankAccount bankAccountDskBgn = ((BankAccountRepository) dbload.getRepositories().getRepositoryFor(BankAccount.class).get()).findFirstByIbanAndCompanyAndDeleted("BG16STSA93000024589399", managedCompany, false);
		JobPosition posTruckDriver = ((JobPositionRepository) dbload.getRepositories().getRepositoryFor(JobPosition.class).get()).findFirstByCodeAndCompanyAndDeleted("8", managedCompany, false);
		CompanyDepartment dep = ((CompanyDepartmentRepository) dbload.getRepositories().getRepositoryFor(CompanyDepartment.class).get()).findAll().iterator().next();
		
		Vehicle vehicle1 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Камион Е0283КР",loiVehicleTypeTruck, "Е0283КР", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		Vehicle vehicle2 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Камион СВ3759НК",loiVehicleTypeTruck, "СВ3759НК", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		Vehicle vehicle3 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Камион СВ6168НК",loiVehicleTypeTruck, "СВ6168НК", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));

		Vehicle trailer11 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ремарке Е5530ЕА",loiVehicleTypeTrailer, "Е5530ЕА", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		Vehicle trailer12 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ремарке Е9619ЕА",loiVehicleTypeTrailer, "Е9619ЕА", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		Vehicle trailer21 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ремарке CB1259EA",loiVehicleTypeTrailer, "CB1259EA", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		Vehicle trailer22 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ремарке E4936ЕА",loiVehicleTypeTrailer, "E4936ЕА", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		Vehicle trailer23 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ремарке E1112ЕА",loiVehicleTypeTrailer, "E1112ЕА", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		Vehicle trailer31 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ремарке Е0411ЕА",loiVehicleTypeTrailer, "Е0411ЕА", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		Vehicle trailer32 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ремарке Е0414ЕА",loiVehicleTypeTrailer, "Е0414ЕА", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		Vehicle trailer33 = (Vehicle) dbload.trySave(new Vehicle(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Ремарке Е1908ЕА",loiVehicleTypeTrailer, "Е1908ЕА", "modelName", "ownerName", "euroStandard", Date.from(createTime.toInstant()), Date.from(createTime.toInstant()), new BigDecimal(110653), "engineNo", "chassisNo", "enginePowerAndCc", new BigDecimal(4), "fuelType", "tankLiters", "oil", "antifreeze"));
		

		Employee driver1 = (Employee) dbload.trySave(new Employee(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Михаил Николов","1234567890",null,null,null,otherUser,posTruckDriver,dep));
		
		Transport transport1 = (Transport) dbload.trySave(new Transport(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,
				statusPlanned,typeTransportOrder, priorityMedium, "Поръчка за транспорт","Детайли за транспорта",null,null,null,
				loiTransportTypeImport,vehicle1,trailer11,"СОЛУН СОФИЯ",new BigDecimal(294),person2,"БАНАНИ","container number",shippingContainerType40HC,null,null,driver1,null,null,null,"confirmation",false,currencyEUR,new BigDecimal(2345.67)));
		Transport transport2 = (Transport) dbload.trySave(new Transport(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,
				statusPlanned,typeTransportOrder, priorityMedium, "Поръчка за транспорт","Детайли за транспорта",null,null,null,
				loiTransportTypeImport,vehicle1,trailer11,"СОФИЯ СОЛУН",new BigDecimal(294),person2,"празен","container number",shippingContainerType40HC,null,null,driver1,null,null,null,"confirmation",false,currencyEUR,new BigDecimal(200)));
		
		TransportOrder order1 = (TransportOrder) dbload.trySave(new TransportOrder(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,
				statusPlanned,typeTransportOrder, priorityMedium, "Поръчка за транспорт","Детайли за транспорта",null,null,null,
				Date.from(createTime.toInstant()),legalPersonOwn,person2,person2,"sender contact","route","loading point",Date.from(createTime.toInstant()),"export customs","export customs agent","cargo","contents", new BigDecimal(4345.34),"loading warehouse ref","receiver","receiver contact","unloading point",Date.from(createTime.toInstant()),"import customs","import customs agent","empties return point",currencyEUR,"x480 per container","payer","payment details","notes"));
		
		dbload.trySave(new TaskRelation(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,typeChild,order1,transport1));
		dbload.trySave(new TaskRelation(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,typeChild,order1,transport2));
		
		Invoice invoice1 = (Invoice) dbload.trySave(new Invoice(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,null,createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"invoice code1","invoice description1",false,false,person2,loiPaymentTypeBank,bankAccountDskBgn,loiVatExemptionReasonNone,currencyBGN,new BigDecimal(2000),new BigDecimal(200),new BigDecimal(2200),admin));
		dbload.trySave(new InvoiceRow(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,invoice1,transportService,"invoice row description1",BigDecimal.ONE,new BigDecimal(2000),transport1));
		Invoice invoice2 = (Invoice) dbload.trySave(new Invoice(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,null,createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"invoice code2","invoice description2",false,false,person2,loiPaymentTypeBank,bankAccountDskBgn,loiVatExemptionReasonNone,currencyBGN,new BigDecimal(200),new BigDecimal(20),new BigDecimal(220),admin));
		dbload.trySave(new InvoiceRow(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,invoice2,transportService,"invoice row description2",BigDecimal.ONE,new BigDecimal(200),transport2));

		Calendar expenditureTime = (Calendar)createTime.clone();
		
		expenditureTime.add(Calendar.DAY_OF_WEEK, 1);
		Expenditure expenditure1 = (Expenditure) dbload.trySave(new Expenditure(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Термометър",null,loiExpenditureTypeMaintenance,"Термометър",null,Date.from(expenditureTime.toInstant()),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),person2));
		dbload.trySave(new AttachableRevenuesAndExpenses(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(6),expenditure1));
//		dbload.trySave(new TaskAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",transport1,expenditure1));
//		dbload.trySave(new AssetAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",vehicle1,expenditure1));
		expenditureTime.add(Calendar.DAY_OF_WEEK, 1);
		Expenditure expenditure2 = (Expenditure) dbload.trySave(new Expenditure(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"ГТП",null,loiExpenditureTypeMaintenance,"ГТП",null,Date.from(expenditureTime.toInstant()),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),person2));
		dbload.trySave(new AttachableRevenuesAndExpenses(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(45.83),expenditure2));
//		dbload.trySave(new TaskAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",transport1,expenditure2));
//		dbload.trySave(new AssetAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",vehicle1,expenditure2));
		expenditureTime.add(Calendar.DAY_OF_WEEK, 1);
		Expenditure expenditure3 = (Expenditure) dbload.trySave(new Expenditure(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Стъкло огледало LH",null,loiExpenditureTypeMaintenance,"Стъкло огледало LH",null,Date.from(expenditureTime.toInstant()),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),person2));
		dbload.trySave(new AttachableRevenuesAndExpenses(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(25),expenditure3));
//		dbload.trySave(new TaskAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",transport1,expenditure3));
//		dbload.trySave(new AssetAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",vehicle1,expenditure3));
		expenditureTime.add(Calendar.DAY_OF_WEEK, 1);
		Expenditure expenditure4 = (Expenditure) dbload.trySave(new Expenditure(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Маркуч помпане",null,loiExpenditureTypeMaintenance,"Маркуч помпане",null,Date.from(expenditureTime.toInstant()),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),person2));
		dbload.trySave(new AttachableRevenuesAndExpenses(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(85.76),expenditure4));
//		dbload.trySave(new TaskAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",transport1,expenditure4));
//		dbload.trySave(new AssetAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",vehicle1,expenditure4));
		expenditureTime.add(Calendar.DAY_OF_WEEK, 1);
		Expenditure expenditure5 = (Expenditure) dbload.trySave(new Expenditure(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Лост мигачи",null,loiExpenditureTypeMaintenance,"Лост мигачи",null,Date.from(expenditureTime.toInstant()),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),person2));
		dbload.trySave(new AttachableRevenuesAndExpenses(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(44),expenditure5));
//		dbload.trySave(new TaskAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",transport1,expenditure5));
//		dbload.trySave(new AssetAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",vehicle1,expenditure5));
		expenditureTime.add(Calendar.DAY_OF_WEEK, 1);
		Expenditure expenditure6 = (Expenditure) dbload.trySave(new Expenditure(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Тапицер волан+дръжки",null,loiExpenditureTypeMaintenance,"Тапицер волан+дръжки",null,Date.from(expenditureTime.toInstant()),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),person2));
		dbload.trySave(new AttachableRevenuesAndExpenses(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(380),expenditure6));
//		dbload.trySave(new TaskAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",transport1,expenditure6));
//		dbload.trySave(new AssetAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",vehicle1,expenditure6));
		expenditureTime.add(Calendar.DAY_OF_WEEK, 1);
		Expenditure expenditure7 = (Expenditure) dbload.trySave(new Expenditure(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Чистачки 650мм",null,loiExpenditureTypeMaintenance,"Чистачки 650мм",null,Date.from(expenditureTime.toInstant()),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),person2));
		dbload.trySave(new AttachableRevenuesAndExpenses(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(21.75),expenditure7));
//		dbload.trySave(new TaskAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",transport1,expenditure7));
//		dbload.trySave(new AssetAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",vehicle1,expenditure7));
		expenditureTime.add(Calendar.DAY_OF_WEEK, 1);
		Expenditure expenditure8 = (Expenditure) dbload.trySave(new Expenditure(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"БОЯДИСВАНЕ",null,loiExpenditureTypeMaintenance,"БОЯДИСВАНЕ",null,Date.from(expenditureTime.toInstant()),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),expenditureTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),person2));
		dbload.trySave(new AttachableRevenuesAndExpenses(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,currencyBGN,new BigDecimal(700),expenditure8));
//		dbload.trySave(new TaskAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",transport1,expenditure8));
//		dbload.trySave(new AssetAttachment(admin,Date.from(createTime.toInstant()),admin,Date.from(createTime.toInstant()),false,managedCompany,"Разход",vehicle1,expenditure8));
	}

}
