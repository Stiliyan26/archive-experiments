package bg.latona.santa.load;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationType;
import bg.latona.santa.entities.security.SecPermission;
import bg.latona.santa.entities.security.SecRole;
import bg.latona.santa.entities.security.SecRolePermission;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.santa.common.*;
import bg.latona.santa.repositories.*;


public class CommonDatabaseLoader {
	
	private static Logger logger = LoggerFactory.getLogger(CommonDatabaseLoader.class);
	
	public static void load(DatabaseLoader dbload, ManagedCompany managedCompany) {
		logger.info("Starting common data loading to database");
		Calendar createTime = Calendar.getInstance();
		createTime.set(2020,10,01,12,00);
		Date createDate = Date.from(createTime.toInstant());
		Calendar minTime = Calendar.getInstance();
		minTime.set(2000,1,1,0,0);
		Calendar maxTime = Calendar.getInstance();
		maxTime.set(3000,1,1,0,0);
		
		SecUser admin = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin1");
		
		LoiPartnerType cPartnerTypeClient = (LoiPartnerType) dbload.trySave(new LoiPartnerType(admin,createDate,admin,createDate,false,managedCompany,"Клиент",LoiPartnerType.PARTNER_TYPE_CL));
		dbload.trySave(new LoiPartnerType(admin,createDate,admin,createDate,false,managedCompany,"Доставчик",LoiPartnerType.PARTNER_TYPE_DV));
		dbload.trySave(new LoiPartnerType(admin,createDate,admin,createDate,false,managedCompany,"Клиент и доставчик",LoiPartnerType.PARTNER_TYPE_CD));

		dbload.trySave(new LoiOfferStatus(admin,createDate,admin,createDate,false,managedCompany,"Оферта",LoiOfferStatus.OFFER_STATUS_A));
		dbload.trySave(new LoiOfferStatus(admin,createDate,admin,createDate,false,managedCompany,"Заявка",LoiOfferStatus.OFFER_STATUS_RA));
		dbload.trySave(new LoiOfferStatus(admin,createDate,admin,createDate,false,managedCompany,"Проформа",LoiOfferStatus.OFFER_STATUS_PI));
		dbload.trySave(new LoiOfferStatus(admin,createDate,admin,createDate,false,managedCompany,"Стокова разписка",LoiOfferStatus.OFFER_STATUS_SA));
		dbload.trySave(new LoiOfferStatus(admin,createDate,admin,createDate,false,managedCompany,"Отказана",LoiOfferStatus.OFFER_STATUS_CA));

		LoiGoodsType cGoodsType = (LoiGoodsType) dbload.trySave(new LoiGoodsType(admin,createDate,admin,createDate,false,managedCompany,"Стока",LoiGoodsType.GOODS_TYPE_GD));
		dbload.trySave(new LoiGoodsType(admin,createDate,admin,createDate,false,managedCompany,"пакетна стока - съставена от няколко стоки",LoiGoodsType.GOODS_TYPE_BD));
		dbload.trySave(new LoiGoodsType(admin,createDate,admin,createDate,false,managedCompany,"полуфабрикат",LoiGoodsType.GOODS_TYPE_SM));
		dbload.trySave(new LoiGoodsType(admin,createDate,admin,createDate,false,managedCompany,"материал/суровина",LoiGoodsType.GOODS_TYPE_RM));
		dbload.trySave(new LoiGoodsType(admin,createDate,admin,createDate,false,managedCompany,"вариантна стока (псевдостока)",LoiGoodsType.GOODS_TYPE_VR));

		dbload.trySave(new LoiLegalStatus(admin,createDate,admin,createDate,false,managedCompany,"Бълг. юр. лице/ рег.",LoiLegalStatus.LEGAL_STATUS_BJT));
		dbload.trySave(new LoiLegalStatus(admin,createDate,admin,createDate,false,managedCompany,"Бълг. юр. лице/ не рег.",LoiLegalStatus.LEGAL_STATUS_BJN));
		LoiLegalStatus loiLegalStatusBN = (LoiLegalStatus) dbload.trySave(new LoiLegalStatus(admin,createDate,admin,createDate,false,managedCompany,"Бълг. физ. лице",LoiLegalStatus.LEGAL_STATUS_BN));
		dbload.trySave(new LoiLegalStatus(admin,createDate,admin,createDate,false,managedCompany,"Чужд. юр. лице",LoiLegalStatus.LEGAL_STATUS_ORJ));
		dbload.trySave(new LoiLegalStatus(admin,createDate,admin,createDate,false,managedCompany,"Чужд. физ. лице",LoiLegalStatus.LEGAL_STATUS_ORN));
		dbload.trySave(new LoiLegalStatus(admin,createDate,admin,createDate,false,managedCompany,"Други",LoiLegalStatus.LEGAL_STATUS_ORU));

		dbload.trySave(new LoiServiceType(admin,createDate,admin,createDate,false,managedCompany,"Услуга",LoiServiceType.SERVICE_TYPE_SV));
		dbload.trySave(new LoiServiceType(admin,createDate,admin,createDate,false,managedCompany,"Плащане",LoiServiceType.SERVICE_TYPE_PT));

		dbload.trySave(new LoiOrderStatus(admin,createDate,admin,createDate,false,managedCompany,"Създадена",LoiOrderStatus.ORDER_STATUS_CR));
		dbload.trySave(new LoiOrderStatus(admin,createDate,admin,createDate,false,managedCompany,"Потвърдена",LoiOrderStatus.ORDER_STATUS_CF));
		dbload.trySave(new LoiOrderStatus(admin,createDate,admin,createDate,false,managedCompany,"Приключена",LoiOrderStatus.ORDER_STATUS_FN));

		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Фактура продажби",LoiTypeDoc.TYPE_DOC_F));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Протокол за бракуване на стока",LoiTypeDoc.TYPE_DOC_PB));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Протокол за преместване в друг склад",LoiTypeDoc.TYPE_DOC_PP));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Авансово плащане",LoiTypeDoc.TYPE_DOC_AP));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Обратна стокова разписка",LoiTypeDoc.TYPE_DOC_KI));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Протокол за преместване от друг склад",LoiTypeDoc.TYPE_DOC_PZ));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Начални салда",LoiTypeDoc.TYPE_DOC_S));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Оферта",LoiTypeDoc.TYPE_DOC_OFR));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Поръчка",LoiTypeDoc.TYPE_DOC_ORR));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Запитване за оферта",LoiTypeDoc.TYPE_DOC_ODY));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Складова разписка",LoiTypeDoc.TYPE_DOC_ISR));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Инвентаризация",LoiTypeDoc.TYPE_DOC_INV));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Стокова разписка",LoiTypeDoc.TYPE_DOC_SR));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Отчет за производство",LoiTypeDoc.TYPE_DOC_MRT));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Връщане на доставчик",LoiTypeDoc.TYPE_DOC_DI));
		dbload.trySave(new LoiTypeDoc(admin,createDate,admin,createDate,false,managedCompany,"Продажби на дребно",LoiTypeDoc.TYPE_DOC_RL));



		//register
		CTypeDocument cTypeDocument = (CTypeDocument) dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Стокова разписка","СР"));
		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Фактура доставки","ФП"));
		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Експ.бележка","ЕБ"));
		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Приемо-пред.прот.","ПП"));
		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Нареждане","Н"));
		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Без документ","-"));
		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Вътрешен документ","ВД"));
//		CTypeDocument cTypeDocument = (CTypeDocument) dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"AP","AP"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"CHE","CHE"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"CHI","CHI"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"INV","INV"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"ISR","ISR"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"IV","IV"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"KI","KI"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"LEN","LEN"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"MRT","MRT"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"ODY","ODY"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"OFR","OFR"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"ORR","ORR"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"PB","PB"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"PI","PI"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"PLN","PLN"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"PP","PP"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"PRN","PRN"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"PZ","PZ"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Стокова разписка","SR"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Фактура","IN"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Митническа декл-я","CD"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Експед. бележка","SH"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"ППП","PR"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Без документ","-"));
//		dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,"Вътрешен документ","ID"));

		dbload.trySave(new LoiCtBankAccountBatType(admin,createDate,admin,createDate,false,managedCompany,"Разплащателна", LoiCtBankAccountBatType.BAT_TYPE_CUT));
		dbload.trySave(new LoiCtBankAccountBatType(admin,createDate,admin,createDate,false,managedCompany,"Набирателна", LoiCtBankAccountBatType.BAT_TYPE_ESW));

		dbload.trySave(new LoiCostMethod(admin,createDate,admin,createDate,false,managedCompany,"Ръчен",LoiCostMethod.COST_METHOD_UI));
		dbload.trySave(new LoiCostMethod(admin,createDate,admin,createDate,false,managedCompany,"Стойностен",LoiCostMethod.COST_METHOD_VL));
		dbload.trySave(new LoiCostMethod(admin,createDate,admin,createDate,false,managedCompany,"Количествен",LoiCostMethod.COST_METHOD_QT));
		dbload.trySave(new LoiCostMethod(admin,createDate,admin,createDate,false,managedCompany,"Тегловен",LoiCostMethod.COST_METHOD_WT));
		dbload.trySave(new LoiCostMethod(admin,createDate,admin,createDate,false,managedCompany,"Обемен",LoiCostMethod.COST_METHOD_VM));
		
		//dbload.trySave(new CTypeDocument(admin,createDate,admin,createDate,false,managedCompany,));

		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"CEO,General manager",5L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Оперативен мениджър",6L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Мениджър маркетинг и продажби",7L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Мениджър ИТ и техническо развитие",8L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Бранд мениджър",9L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Отговорник СОК",10L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Специалист покупки",11L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Специалист обслужване клиенти",12L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Отговорник склад",13L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Специалист склад",14L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Сервизен инженер",15L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Отговорник Администрация и счетоводство",16L));
		dbload.trySave(new SecRole(admin,createDate,null,createDate,false,managedCompany,"Специалист Администрация и счетоводство",17L));
		//give all permissions to roles that have none
		SecRolePermissionRepository secRolePermissionRepository = ((SecRolePermissionRepository) dbload.getRepositories().getRepositoryFor(SecRolePermission.class).get());
		SecPermissionRepository secPermissionRepository = ((SecPermissionRepository) dbload.getRepositories().getRepositoryFor(SecPermission.class).get());
		for(SecRole role : ((SecRoleRepository) dbload.getRepositories().getRepositoryFor(SecRole.class).get()).findByCompanyAndDeleted(managedCompany, false)) {
			//if this role doesn't have any permissions
			if(secRolePermissionRepository.findFirstByRoleAndCompanyAndDeleted(role, managedCompany, false) == null) {
				for(SecPermission perm : secPermissionRepository.findByCompanyAndDeleted(managedCompany, false)) {
					dbload.trySave(new SecRolePermission(admin,createDate,null,createDate,false,managedCompany,role,perm));
				}
			}
		}

		CCcOrganizationUnit orgUnitBgMashini = (CCcOrganizationUnit) dbload.trySave(new CCcOrganizationUnit(admin,createDate,admin,createDate,false,managedCompany,"01","София","BGM","bulstat","гр.София","FM",null,"sebra",LocalDate.of(2000,1,1),LocalDate.of(2100,1,1),"vatNumber",createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),null,"01","Sofia","Sofia"));

		dbload.trySave(new CCcOrganizationUnit(admin,createDate,admin,createDate,false,managedCompany,"02","Враца","VR","bulstat2","гр.Враца","FM",null,"sebra",LocalDate.of(2000,1,1),LocalDate.of(2100,1,1),"vatNumber",createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),null,"02","Vratsa","Vratsa"));
		dbload.trySave(new CCcOrganizationUnit(admin,createDate,admin,createDate,false,managedCompany,"03","Тройна логистика","DD","bulstat3","гр.Враца","FM",null,"sebra",LocalDate.of(2000,1,1),LocalDate.of(2100,1,1),"vatNumber",createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),null,"03","Direct delivery","-"));
		dbload.trySave(new CCcOrganizationUnit(admin,createDate,admin,createDate,false,managedCompany,"04","Сервиз","SV","bulstat4","гр.София","FM",null,"sebra",LocalDate.of(2000,1,1),LocalDate.of(2100,1,1),"vatNumber",createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),null,"04","Repairs","Sofia"));

		//BG Mashini defaults
		CCtCurrency currBGN = (CCtCurrency) dbload.trySave(new CCtCurrency(admin,createDate,admin,createDate,false,managedCompany,"BGN","лева",true,"leva","st",LocalDate.of(2000,1,1),LocalDate.of(2100,1,1)));
		CCtCurrency currEUR = (CCtCurrency) dbload.trySave(new CCtCurrency(admin,createDate,admin,createDate,false,managedCompany,"EUR","евро",false,"euro","cents",LocalDate.of(2000,1,1),LocalDate.of(2100,1,1)));
		dbload.trySave(new CCtCurrency(admin,createDate,admin,createDate,false,managedCompany,"USD","долара",false,"dollars","cents",LocalDate.of(2000,1,1),LocalDate.of(2100,1,1)));

		dbload.trySave(new CPmtCurrencyRate(admin,createDate,admin,createDate,false,managedCompany,orgUnitBgMashini,currBGN,minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),1,BigDecimal.ONE,BigDecimal.ONE,null));
		dbload.trySave(new CPmtCurrencyRate(admin,createDate,admin,createDate,false,managedCompany,orgUnitBgMashini,currEUR,minTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),maxTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),1,new BigDecimal("1.95583"),BigDecimal.ONE,null));
		
		CMeasure countMeasure = (CMeasure) dbload.trySave(new CMeasure(admin,createDate,admin,createDate,false,managedCompany,"Брой","БР",BigDecimal.ONE,null));
		dbload.trySave(new CCcGoodsType(admin,createDate,admin,createDate,false,managedCompany,orgUnitBgMashini,"Други","Други",cGoodsType,LocalDate.of(2000,1,1),LocalDate.of(2100,1,1)));
		dbload.trySave(new CService(admin,createDate,admin,createDate,false,managedCompany,"ДОСТ","Доставка",countMeasure,"Доставка",orgUnitBgMashini,null));
		CCtPartnerGroup partnerGroup = (CCtPartnerGroup) dbload.trySave(new CCtPartnerGroup(admin,createDate,admin,createDate,false,managedCompany,orgUnitBgMashini,"Кл. на дребно","ДР",LocalDate.of(2000,1,1),LocalDate.of(2100,1,1),orgUnitBgMashini));
		dbload.trySave(new CCcPartner(admin,createDate,admin,createDate,false,managedCompany,orgUnitBgMashini,"ДР","Клиент на дребно","bulstat","egn","address",cPartnerTypeClient,partnerGroup,"tel","fax","mol",LocalDate.of(2000,1,1),LocalDate.of(2100,1,1),"vatNo",1,"foreignNo",loiLegalStatusBN,"email",admin));
		dbload.trySave(new CGoodMark(admin,createDate,admin,createDate,false,managedCompany,"N/A","Без марка","Без марка",orgUnitBgMashini,1,null));

		dbload.trySave(new AllocationType(admin,createDate,admin,createDate,false,managedCompany,"Фактури на доставчици","vendorInvoiceRows","cDeliveryDetails"));
		dbload.trySave(new AllocationType(admin,createDate,admin,createDate,false,managedCompany,"Поръчки на клиенти","cOfferDetails","cOrderDetails"));
}

	public static void loadTest(DatabaseLoader dbload, ManagedCompany managedCompany) {
		logger.info("Starting common test data loading to database");
		Calendar createTime = Calendar.getInstance();
		createTime.set(2020,10,01,12,00);
		Date createDate = Date.from(createTime.toInstant());
		
		SecUser admin = ((SecUserRepository) dbload.getRepositories().getRepositoryFor(SecUser.class).get()).findFirstByName("admin1");
		LoiPartnerType cPartnerTypeClient = ((LoiPartnerTypeRepository) dbload.getRepositories().getRepositoryFor(LoiPartnerType.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPartnerType.PARTNER_TYPE_CL,admin.getCompany(),false);
		LoiPartnerType cPartnerTypeVendor = ((LoiPartnerTypeRepository) dbload.getRepositories().getRepositoryFor(LoiPartnerType.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPartnerType.PARTNER_TYPE_DV,admin.getCompany(),false);
		LoiGoodsType cGoodsType = ((LoiGoodsTypeRepository) dbload.getRepositories().getRepositoryFor(LoiGoodsType.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiGoodsType.GOODS_TYPE_GD,admin.getCompany(),false);
		LoiOrderStatus loiOrderStatusCF = ((LoiOrderStatusRepository) dbload.getRepositories().getRepositoryFor(LoiOrderStatus.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiOrderStatus.ORDER_STATUS_CF,admin.getCompany(),false);

		//register
		LoiLegalStatus loiLegalStatusBJN = ((LoiLegalStatusRepository) dbload.getRepositories().getRepositoryFor(LoiLegalStatus.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiLegalStatus.LEGAL_STATUS_BJN,admin.getCompany(),false);
		LoiLegalStatus loiLegalStatusBN = ((LoiLegalStatusRepository) dbload.getRepositories().getRepositoryFor(LoiLegalStatus.class).get()).findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiLegalStatus.LEGAL_STATUS_BN,admin.getCompany(),false);

		CCcOrganizationUnit orgUnitBgMashini = ((CCcOrganizationUnitRepository) dbload.getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get()).findFirstByCodeAndCompanyAndDeleted("01",admin.getCompany(),false);
		CCtCurrency currency = ((CCtCurrencyRepository) dbload.getRepositories().getRepositoryFor(CCtCurrency.class).get()).findFirstByCodeAndCompanyAndDeleted("BGN",admin.getCompany(),false);
		CMeasure countMeasure = ((CMeasureRepository) dbload.getRepositories().getRepositoryFor(CMeasure.class).get()).findFirstByCodeAndCompanyAndDeleted("БР",admin.getCompany(),false);
		CCcGoodsType ccGoodsType = ((CCcGoodsTypeRepository) dbload.getRepositories().getRepositoryFor(CCcGoodsType.class).get()).findFirstByCodeAndOutCodeAndCompanyAndDeleted("Други",orgUnitBgMashini,admin.getCompany(),false);
		CGoodMark goodMark = ((CGoodMarkRepository) dbload.getRepositories().getRepositoryFor(CGoodMark.class).get()).findFirstByMarkCodeAndOutCodeAndCompanyAndDeleted("НЯМА",orgUnitBgMashini,admin.getCompany(),false);
		
		CCtPartnerGroup partnerGroup = ((CCtPartnerGroupRepository) dbload.getRepositories().getRepositoryFor(CCcGoodsType.class).get()).findFirstByCodeAndOutCodeAndCompanyAndDeleted("ДР",orgUnitBgMashini,admin.getCompany(),false);
		CCcPartner partner = ((CCcPartnerRepository) dbload.getRepositories().getRepositoryFor(CCcGoodsType.class).get()).findFirstByCodeAndOutCodeAndCompanyAndDeleted("ДР",orgUnitBgMashini,admin.getCompany(),false);
		
		
		//CCcOrganizationUnit orgUnit = (CCcOrganizationUnit) dbload.trySave(new CCcOrganizationUnit(admin,createDate,admin,createDate,false,managedCompany,"code","name","abbreviation","bulstat","address","orgUnitType",null,"sebra",null,null,"vatNumber",createDate,null,"mainOutCode","nameEn","addressEn"));
		//CCcOrganizationUnit orgUnitThree = (CCcOrganizationUnit) dbload.trySave(new CCcOrganizationUnit(admin,createDate,admin,createDate,false,managedCompany,"05","склад Тестова фирма","TST2","232323457","гр.София,ул.Пирински проход 47Г","OF",null,"sebra",null,null,"vatNumber",createDate,null,"05","nameEn","addressEn"));
		//CCcOrganizationUnit warehouse1 = (CCcOrganizationUnit) dbload.trySave(new CCcOrganizationUnit(admin,createDate,admin,createDate,false,managedCompany,"Склад 1","Склад 1","Склад 1","bulstat1","address","orgUnitType",null,"sebra",null,null,"vatNumber",createDate,null,"mainOutCode","nameEn","addressEn"));
		//CCcOrganizationUnit firma1	 = (CCcOrganizationUnit) dbload.trySave(new CCcOrganizationUnit(admin,createDate,admin,createDate,false,managedCompany,"Фирма 1","Фирма 1","Фирма 1","bulstat2","address","orgUnitType",null,"sebra",null,null,"vatNumber",createDate,null,"mainOutCode","nameEn","addressEn"));
		//COrder order = (COrder) dbload.trySave(new COrder(admin,createDate,admin,createDate,false,managedCompany,createDate,createDate,partner,orgUnitTwo,loiOrderStatusCF,BigDecimal.ZERO,currency,createDate,1));
		//COrder orderTwo = (COrder) dbload.trySave(new COrder(admin,createDate,admin,createDate,false,managedCompany,createDate,createDate,partner,orgUnitTwo,loiOrderStatusCF,BigDecimal.ZERO,currency,createDate,2));
		//CMeasure measure = (CMeasure) dbload.trySave(new CMeasure(admin,createDate,admin,createDate,false,managedCompany,"PKG","PKG",BigDecimal.ONE,null));
		CGoods goods = (CGoods) dbload.trySave(new CGoods(admin,createDate,admin,createDate,false,managedCompany,"nameBg","nameEng","code","barcode","manufacturerCode",ccGoodsType,cGoodsType,orgUnitBgMashini,1,1000,countMeasure,"description",new BigDecimal("1.5"),new BigDecimal("0.5"),BigDecimal.ONE,goodMark,null,null));
		CDeliveryOffer deliveryOffer = (CDeliveryOffer) dbload.trySave(new CDeliveryOffer(admin,createDate,admin,createDate,false,managedCompany,partner,"offerNo",createDate,null,null,BigDecimal.ZERO,"status","remarks",orgUnitBgMashini));
		CDeliveryOfferItem deliveryOfferItem = (CDeliveryOfferItem) dbload.trySave(new CDeliveryOfferItem(admin,createDate,admin,createDate,false,managedCompany,deliveryOffer,"code","name",new BigDecimal("2.49"),currency,new BigDecimal("2"),goods,"status",countMeasure));
		//dbload.trySave(new COrderDetail(admin,createDate,admin,createDate,false,managedCompany,goods,order,BigDecimal.ONE,new BigDecimal(2.49),measure,new BigDecimal(2.49),new BigDecimal(123),deliveryOfferItem,BigDecimal.ONE,new BigDecimal(2),new BigDecimal(3),new BigDecimal(4)));

		Calendar endDate = Calendar.getInstance();
		endDate.set(2011,02,10,00,00);
		CCcPartner vendor2 = (CCcPartner) dbload.trySave(new CCcPartner(admin,createDate,admin,createDate,false,managedCompany,orgUnitBgMashini,"04","MegaSun","040530711",null,"Тутракан",cPartnerTypeVendor,partnerGroup,"tel","fax","mol",null,null,"vatNo",1,"foreignNo",loiLegalStatusBJN,"email",admin));
		CCcPartner vendor3 = (CCcPartner) dbload.trySave(new CCcPartner(admin,createDate,admin,createDate,false,managedCompany,orgUnitBgMashini,"05","Гора 2000","117101656",null,"Кърджали",cPartnerTypeVendor, partnerGroup,"tel","fax","mol",null,null,"vatNo",1,"foreignNo",loiLegalStatusBJN,"email",admin));
		//COffer offer = (COffer) dbload.trySave(new COffer(admin,createDate,admin,createDate,false,managedCompany,Date.from(endDate.toInstant()),Date.from(endDate.toInstant()),partner,orgUnitTwo,"SA",1,null,BigDecimal.ZERO,BigDecimal.ZERO,null,new BigDecimal(250),new BigDecimal(250),currency,null,null,BigDecimal.ZERO,null));
		//CMeasure measureTwo = (CMeasure) dbload.trySave(new CMeasure(admin,createDate,admin,createDate,false,managedCompany,"Ролка10","РОЛ10",new BigDecimal(0.1),countMeasure));
		CGoods goodsLining = (CGoods) dbload.trySave(new CGoods(admin,createDate,admin,createDate,false,managedCompany,"Тапицерия","Тапицерия","1003-0005","barcode","manufacturerCode",ccGoodsType,cGoodsType,orgUnitBgMashini,5,10,countMeasure,"description",BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ONE,goodMark,null,vendor2));
		//dbload.trySave(new COfferDetail(admin,createDate,admin,createDate,false,managedCompany,goodsLining,offer,new BigDecimal(5),null,null,null,null,BigDecimal.ZERO,new BigDecimal(50),countMeasure,null,new BigDecimal(50)));
//		CDelivery delivery = (CDelivery) dbload.trySave(new CDelivery(admin,createDate,admin,createDate,false,managedCompany,
//				Date.from(endDate.toInstant()),cTypeDocument,"paymentType",Date.from(endDate.toInstant()),partner,orgUnitTwo,BigDecimal.ZERO,new BigDecimal(100),
//				new BigDecimal(20),"status",1,"groundsNumber",Date.from(endDate.toInstant()),currency,BigDecimal.ONE,"typeDoc","deyNumber",new BigDecimal(20),
//				new BigDecimal(100),new BigDecimal(100),new BigDecimal(120),new BigDecimal(100),null,"posted",new BigDecimal(120),"costMethod","remarks"));
//		CDeliveryDetail deliveryDetail = (CDeliveryDetail) dbload.trySave(new CDeliveryDetail(admin,createDate,admin,createDate,false,managedCompany,delivery,goods,"batch","serialNumber",Date.from(endDate.toInstant()),BigDecimal.ONE,countMeasure,new BigDecimal(1.5),new BigDecimal(0.3),new BigDecimal(1.5),BigDecimal.ZERO,new BigDecimal(1.5),new BigDecimal(0.3),new BigDecimal(1.8),null,new BigDecimal(1.8),null,new BigDecimal(1.8),null,null,0));
//		dbload.trySave(new CStock(admin,createDate,admin,createDate,false,managedCompany,goodsLining,deliveryDetail,BigDecimal.TEN,BigDecimal.TEN,countMeasure,new BigDecimal(50),null,null,null,null,orgUnitTwo,BigDecimal.ZERO,new BigDecimal(50),Date.from(endDate.toInstant()),new BigDecimal(50),new BigDecimal(100),null,BigDecimal.ZERO,BigDecimal.ZERO));
		
		CGoods goodsTwo = (CGoods) dbload.trySave(new CGoods(admin,createDate,admin,createDate,false,managedCompany,"ХХХ Диван Мария","nameEng","1004","barcode","manufacturerCode",ccGoodsType,cGoodsType,orgUnitBgMashini,5,10,countMeasure,"description",new BigDecimal("1.5"),new BigDecimal("0.5"),BigDecimal.ONE,goodMark,null,vendor3));
		CGoods goodsThree = (CGoods) dbload.trySave(new CGoods(admin,createDate,admin,createDate,false,managedCompany,"ХХХ Диван Милена","nameEng","1003","barcode","manufacturerCode",ccGoodsType,cGoodsType,orgUnitBgMashini,5,10,countMeasure,"description",new BigDecimal("1.5"),new BigDecimal("0.5"),BigDecimal.ONE,goodMark,null,vendor3));
		CGoods goodsFour = (CGoods) dbload.trySave(new CGoods(admin,createDate,admin,createDate,false,managedCompany,"ХХХ Диван Моника","nameEng","1005","barcode","manufacturerCode",ccGoodsType,cGoodsType,orgUnitBgMashini,10,15,countMeasure,"description",new BigDecimal("1.5"),new BigDecimal("0.5"),BigDecimal.ONE,goodMark,null,vendor3));
		//COrder exampleOrder = (COrder) dbload.trySave(new COrder(admin,createDate,admin,createDate,false,managedCompany,createDate,createDate,partner,orgUnitTwo,loiOrderStatusCF,BigDecimal.ZERO,null,createDate,3));
		
		//Stela's data for test
		Calendar activeFrom = Calendar.getInstance();
		activeFrom.set(2022,03,10,12,00);
		CCcPartner vendor1 = (CCcPartner) dbload.trySave(new CCcPartner(admin,createDate,admin,createDate,false,managedCompany,orgUnitBgMashini,"00002","Доставчик 1","9534523","8312046365","гр. София ул Иван Асен 68",cPartnerTypeVendor,partnerGroup,"885632458","fax","mol",activeFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),null,"BG9534523",1,"foreignNo",loiLegalStatusBJN,"email",admin));
		CGoods goods1 = (CGoods) dbload.trySave(new CGoods(admin,createDate,admin,createDate,false,managedCompany,"mc940zm/a ipad dock","mc940zm/a ipad dock","000023","barcode","manufacturerCode",ccGoodsType,cGoodsType,orgUnitBgMashini,1,10,countMeasure,"description",new BigDecimal("1.5"),new BigDecimal("0.5"),BigDecimal.ONE,goodMark,null,vendor1));
		dbload.trySave(new CDeliveryGoodMap(admin,createDate,admin,createDate,false,managedCompany,vendor1,"1",goods1));
		dbload.trySave(new CDeliveryGoodMap(admin,createDate,admin,createDate,false,managedCompany,vendor3,"2",goodsTwo));
		dbload.trySave(new CDeliveryGoodMap(admin,createDate,admin,createDate,false,managedCompany,vendor3,"3",goodsThree));
		dbload.trySave(new CDeliveryGoodMap(admin,createDate,admin,createDate,false,managedCompany,vendor3,"4",goodsFour));
		dbload.trySave(new CDeliveryGoodMap(admin,createDate,admin,createDate,false,managedCompany,vendor2,"5",goodsLining));

		dbload.trySave(new CPriceList(admin,createDate,admin,createDate,false,managedCompany,vendor1,goods1,new BigDecimal("20.00"),new BigDecimal("0.00"),orgUnitBgMashini,currency,createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"PN",new BigDecimal("20.00"),null,null,countMeasure,null,null,null,null));
		dbload.trySave(new CPriceList(admin,createDate,admin,createDate,false,managedCompany,vendor3,goodsTwo,new BigDecimal("20.00"),new BigDecimal("0.00"),orgUnitBgMashini,currency,createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"PN",new BigDecimal("40.00"),null,null,countMeasure,null,null,null,null));
		dbload.trySave(new CPriceList(admin,createDate,admin,createDate,false,managedCompany,vendor3,goodsThree,new BigDecimal("20.00"),new BigDecimal("0.00"),orgUnitBgMashini,currency,createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"PN",new BigDecimal("50.00"),null,null,countMeasure,null,null,null,null));
		//dbload.trySave(new CPriceList(admin,createDate,admin,createDate,false,managedCompany,vendor3,goodsFour,new BigDecimal("20.00"),new BigDecimal("0.00"),orgUnitBgMashini,currency,createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"PN",new BigDecimal("60.00"),null,null,countMeasure,null,null,null,null));
		dbload.trySave(new CPriceList(admin,createDate,admin,createDate,false,managedCompany,vendor2,goodsLining,new BigDecimal("20.00"),new BigDecimal("0.00"),orgUnitBgMashini,currency,createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), createTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"PN",new BigDecimal("60.00"),null,null,countMeasure,null,null,null,null));
	}
}
