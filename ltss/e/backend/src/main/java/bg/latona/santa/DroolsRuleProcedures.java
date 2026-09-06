package bg.latona.santa;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import bg.latona.santa.entities.selfie.*;
import bg.latona.santa.entities.nepal.*;
import bg.latona.santa.selfie.service.electricityInvoice.impls.PdfGenerationServiceImpl;
import bg.latona.santa.selfie.service.electricityInvoice.interfaces.PdfGenerationService;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.context.WebApplicationContext;

import bg.latona.santa.entities.*;
import bg.latona.santa.entities.allocation.*;
import bg.latona.santa.entities.article.*;
import bg.latona.santa.entities.employee.*;
import bg.latona.santa.entities.invoice.*;
import bg.latona.santa.entities.mail.*;
import bg.latona.santa.entities.offer.*;
import bg.latona.santa.entities.person.*;
import bg.latona.santa.entities.santa.common.*;
import bg.latona.santa.entities.santa.finance.*;
import bg.latona.santa.entities.security.*;
import bg.latona.santa.entities.task.*;
import bg.latona.santa.entities.transport.*;
import bg.latona.santa.entities.wato.*;
import bg.latona.santa.repositories.*;


@Component
public class DroolsRuleProcedures {

	private static Logger logger = LoggerFactory.getLogger(DroolsRuleProcedures.class);
	private static Map<String, Long> autoIncValuesMap = new HashMap<String, Long>();
	
	@PersistenceContext
	private EntityManager entityManager; //USING HQL
	@Autowired
	private WebApplicationContext appContext;
	@Autowired
	private RepositoryResourceMappings mappings;
	private Repositories repositories = null;
	private final PdfGenerationService pdfGenerationService;

	public DroolsRuleProcedures(PdfGenerationService pdfGenerationService) {
		this.pdfGenerationService = pdfGenerationService;
	}

	private Repositories getRepositories() {
		if(repositories == null) {
			repositories = new Repositories(appContext);
		}
		return repositories;
	}
	
	//must not be in a @Transactional class
	public class CleanEmWrapper {
		private EntityManager cleanEM = null;
		private EntityManager workEM = null;
		
		protected CleanEmWrapper(EntityManager entityManager) {
			this.workEM = entityManager;
		}
		
		public CommonRecord getOriginalEntity(CommonRecord commonRecord) {
			CommonRecord obj = commonRecord;
			if(commonRecord instanceof HibernateProxy) {
				obj = (CommonRecord) Hibernate.unproxy(commonRecord); //https://www.baeldung.com/hibernate-proxy-to-real-entity-object
			}
			logger.trace("getOriginalEntity for "+obj.getClass()+" "+obj);
			if(obj instanceof CommonRecord && ((CommonRecord) obj).getId() != null) {
				if(this.workEM == null) {
					logger.error("workEM is null!");
					return null;
				}
				if(cleanEM == null) {
					logger.trace("cleanEM is null, initializing");
					cleanEM = this.workEM.getEntityManagerFactory().createEntityManager();
					cleanEM.setFlushMode(FlushModeType.COMMIT); //optimize because this should be read-only EM (not tested if working)
				}
				return ((CommonRecord) cleanEM.find(obj.getClass(), ((CommonRecord) obj).getId()));
			} else {
				return null;
			}
		}
		
		public void release() {
			if(cleanEM != null) {
				cleanEM.close();
			}
		}
	}
	
	public CleanEmWrapper getCleanEmWrapper() {
		return new CleanEmWrapper(this.entityManager);
	}
	
	public void delete(CommonRecord entity) {
		((CommonRepository) getRepositories().getRepositoryFor(entity.getClass()).get()).delete(entity);
	}
	
	public CommonRecord save(CommonRecord entity) {
		return (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass()).get()).save(entity);
	}
	
	public synchronized Long getMaxAutoIncNum(CommonRecord entity) {
		Long newNum = autoIncValuesMap.get(entity.getClass().getName());
		if(newNum == null) {
			if(entity instanceof CDelivery) {
				CDeliveryRepository repo = ((CDeliveryRepository) getRepositories().getRepositoryFor(CDelivery.class).get());
				newNum = repo.getMaxAutoIncNum();
			} else if(entity instanceof COffer) {
				COfferRepository repo = ((COfferRepository) getRepositories().getRepositoryFor(COffer.class).get());
				newNum = repo.getMaxAutoIncNum();
			} else if(entity instanceof COrder) {
				COrderRepository repo = ((COrderRepository) getRepositories().getRepositoryFor(COrder.class).get());
				newNum = repo.getMaxAutoIncNum();
			} else if(entity instanceof CSale) {
				CSaleRepository repo = ((CSaleRepository) getRepositories().getRepositoryFor(CSale.class).get());
				newNum = repo.getMaxAutoIncNum();
			} else if(entity instanceof FPtJournal) {
				FPtJournalRepository repo = ((FPtJournalRepository) getRepositories().getRepositoryFor(FPtJournal.class).get());
				newNum = repo.getMaxAutoIncNum();
			} else if(entity instanceof FInvInvoice) {
				FInvInvoiceRepository repo = ((FInvInvoiceRepository) getRepositories().getRepositoryFor(FInvInvoice.class).get());
				newNum = repo.getMaxAutoIncNum();
			}
			
			if(newNum == null) {
				newNum = 1L;
			}
		}
		newNum++;
		autoIncValuesMap.put(entity.getClass().getName(), newNum);
		return newNum;
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

	public LocalDate getInvoiceMaxIssuedInvoiceDate() {
		InvoiceRepository repo = ((InvoiceRepository) getRepositories().getRepositoryFor(Invoice.class).get());
		return repo.getMaxIssuedInvoiceDate();
	}

	public LocalDate getInvoiceMaxIssuedCInvoiceInvoiceDate() {
		CInvoiceRepository repo = ((CInvoiceRepository) getRepositories().getRepositoryFor(CInvoice.class).get());
		return repo.getMaxIssuedInvoiceDate();
	}
	
	public BigDecimal getInvoiceMaxIssuedInvoiceNum() {
		InvoiceRepository repo = ((InvoiceRepository) getRepositories().getRepositoryFor(Invoice.class).get());
		return repo.getMaxIssuedInvoiceNum();
	}

	public BigDecimal getInvoiceMaxIssuedCInvoiceInvoiceNum() {
		CInvoiceRepository repo = ((CInvoiceRepository) getRepositories().getRepositoryFor(CInvoice.class).get());
		return repo.getMaxIssuedInvoiceNum();
	}
	
	public Contact getRecognizedContact(String fromAddress, Long companyCode) {
		return (Contact) ((ContactRepository) getRepositories().getRepositoryFor(Contact.class).get())
			.findById(this.isRecognizedContact(fromAddress, companyCode).longValue()).get();
	}
	
	public BigDecimal convertMeasures(CMeasure from, CMeasure to) {
		if( from == to ) {
			return BigDecimal.ONE;
		}
		CMeasure m1 = from;
		CMeasure m2 = to;
		if( to == null ) {
			m2 = m1.getMeeId();
		}
		if( m1.getMeeId() == m2.getMeeId() ) {
			return m2.getCofficient().divide( m1.getCofficient() );
		}
		return null;
	}
	
	public void calculateDeliveryCost(BigDecimal[] costData /*costBase out, newCost out*/, CDeliveryDetail goodsDeliveryDetail, BigDecimal serviceSum, BigDecimal totalValue, BigDecimal totalQuantity, BigDecimal totalWeight, BigDecimal totalVolume) {
		costData[0] = BigDecimal.ZERO;
		if( goodsDeliveryDetail.getDeyId().getCostMethod().getListOptionItemCode() == LoiCostMethod.COST_METHOD_UI ) {
			costData[0] = goodsDeliveryDetail.getCostBase();
		} else if( goodsDeliveryDetail.getDeyId().getCostMethod().getListOptionItemCode() == LoiCostMethod.COST_METHOD_VL && goodsDeliveryDetail.getValueAll() != null ) {
			BigDecimal valDisc = goodsDeliveryDetail.getValueAll().multiply( (new BigDecimal( 100 )).subtract( goodsDeliveryDetail.getDiscount() ).divide(new BigDecimal( 100 ), 2, RoundingMode.HALF_UP) );
			costData[0] = ( BigDecimal.ZERO.compareTo( totalValue ) == 0 ) ? BigDecimal.ZERO 
				: valDisc.divide( totalValue, 5, RoundingMode.HALF_UP );
		} else if( goodsDeliveryDetail.getDeyId().getCostMethod().getListOptionItemCode() == LoiCostMethod.COST_METHOD_QT ) {
			costData[0] = ( BigDecimal.ZERO.compareTo( totalQuantity ) == 0 ) ? BigDecimal.ZERO
				: goodsDeliveryDetail.getQuantity().divide( totalQuantity, 5, RoundingMode.HALF_UP );
		} else if( goodsDeliveryDetail.getDeyId().getCostMethod().getListOptionItemCode() == LoiCostMethod.COST_METHOD_WT ) {
			costData[0] = ( BigDecimal.ZERO.compareTo( totalWeight ) == 0 ) ? BigDecimal.ZERO
				: goodsDeliveryDetail.getQuantity().multiply( goodsDeliveryDetail.getGodId().getWeight() ).divide( totalWeight, 5, RoundingMode.HALF_UP );
		} else if( goodsDeliveryDetail.getDeyId().getCostMethod().getListOptionItemCode() == LoiCostMethod.COST_METHOD_VM ) {
			costData[0] = ( BigDecimal.ZERO.compareTo( totalVolume ) == 0 ) ? BigDecimal.ZERO
				: goodsDeliveryDetail.getQuantity().multiply( goodsDeliveryDetail.getGodId().getVolume() ).divide( totalVolume, 5, RoundingMode.HALF_UP );
		}
		
		BigDecimal price_disc = goodsDeliveryDetail.getPrice().multiply( (new BigDecimal(100)).subtract( goodsDeliveryDetail.getDiscount() ).divide( (new BigDecimal(100)) ));
		costData[1] = BigDecimal.ZERO.compareTo( goodsDeliveryDetail.getQuantity() ) == 0 ? price_disc 
			: price_disc.add( 
					serviceSum.multiply( (new BigDecimal(100)).subtract( goodsDeliveryDetail.getDeyId().getDiscount() )).divide( (new BigDecimal(100)) )
					.multiply( costData[0] )
					.divide( goodsDeliveryDetail.getQuantity(), 5, RoundingMode.HALF_UP )
				);
	}

	//all searches should either search by some entity, either include company, either by by foreign ID (which is tied to company)
	public AccountingPeriod findAccountingPeriodByMonthAndCompanyAndDeleted(String month, ManagedCompany company, boolean deleted) {
		AccountingPeriodRepository repo = ((AccountingPeriodRepository) getRepositories().getRepositoryFor(AccountingPeriod.class).get());
		return repo.findFirstByMonthAndCompanyAndDeleted(month, company,  deleted);
	}

	public List<AccountingPeriod> findAccountingPeriodByIsActiveAndDeleted(boolean isActive, boolean deleted) {
		AccountingPeriodRepository repo = ((AccountingPeriodRepository) getRepositories().getRepositoryFor(AccountingPeriod.class).get());
		return repo.findByIsActiveAndDeleted(isActive,  deleted);
	}

	public AgreementSelfInvoicing findAgreementSelfInvoicingByAgreementSelfInvoicingIdCodeAndCompanyAndDeleted(String agreementSelfInvoicingIdCode, ManagedCompany company, boolean deleted) {
		AgreementSelfInvoicingRepository repo = ((AgreementSelfInvoicingRepository) getRepositories().getRepositoryFor(AgreementSelfInvoicing.class).get());
		return repo.findFirstByAgreementSelfInvoicingIdCodeAndCompanyAndDeleted(agreementSelfInvoicingIdCode, company,  deleted);
	}

	public AgreementTypeMapping findAgreementTypeMappingByCrmCodeAndCompanyAndDeleted(BigDecimal crmCode, ManagedCompany company, boolean deleted) {
		AgreementTypeMappingRepository repo = ((AgreementTypeMappingRepository) getRepositories().getRepositoryFor(AgreementTypeMapping.class).get());
		return repo.findFirstByCrmCodeAndCompanyAndDeleted(crmCode, company,  deleted);
	}

	public AllocationProxy findAllocationProxyByAllocationOriginAndAllocationTypeAndCompanyAndDeleted(AllocationOrigin allocationOrigin, AllocationType allocationType, ManagedCompany company, boolean deleted) {
		AllocationProxyRepository repo = ((AllocationProxyRepository) getRepositories().getRepositoryFor(AllocationProxy.class).get());
		return repo.findFirstByAllocationOriginAndAllocationTypeAndCompanyAndDeleted(allocationOrigin, allocationType, company, deleted);
	}
	
	public List<AllocationRecord> findAllocationRecordByAllocationConsumerAndCompanyAndDeleted(AllocationProxy allocationConsumer, ManagedCompany company, boolean deleted) {
		AllocationRecordRepository repo = ((AllocationRecordRepository) getRepositories().getRepositoryFor(AllocationRecord.class).get());
		return repo.findByAllocationConsumerAndCompanyAndDeleted(allocationConsumer, company, deleted);
	}
	
	public AllocationRecord findAllocationRecordByAllocationProducerAndAllocationConsumerAndCompanyAndDeleted(AllocationProxy allocationProducer, AllocationProxy allocationConsumer, ManagedCompany company, boolean deleted) {
		AllocationRecordRepository repo = ((AllocationRecordRepository) getRepositories().getRepositoryFor(AllocationRecord.class).get());
		return repo.findFirstByAllocationProducerAndAllocationConsumerAndCompanyAndDeleted(allocationProducer, allocationConsumer, company, deleted);
	}
	
	public List<AllocationRecord> findAllocationRecordByAllocationProducerAndCompanyAndDeleted(AllocationProxy allocationProducer, ManagedCompany company, boolean deleted) {
		AllocationRecordRepository repo = ((AllocationRecordRepository) getRepositories().getRepositoryFor(AllocationRecord.class).get());
		return repo.findByAllocationProducerAndCompanyAndDeleted(allocationProducer, company, deleted);
	}
	
	public AllocationType findAllocationTypeByNameAndCompanyAndDeleted(String name, ManagedCompany company, boolean deleted) {
		AllocationTypeRepository repo = ((AllocationTypeRepository) getRepositories().getRepositoryFor(AllocationType.class).get());
		return repo.findFirstByNameAndCompanyAndDeleted(name, company, deleted);
	}

	public AllocationType findAllocationTypeByProducerAndConsumerAndCompanyAndDeleted(String producer, String consumer, ManagedCompany company, boolean deleted) {
		AllocationTypeRepository repo = ((AllocationTypeRepository) getRepositories().getRepositoryFor(AllocationType.class).get());
		return repo.findFirstByProducerAndConsumerAndCompanyAndDeleted(producer, consumer, company, deleted);
	}
	
	public List<AllocationType> findAllocationTypeByProducerClassOrConsumerClassAndCompanyAndDeleted(Class claz, ManagedCompany company, boolean deleted) {
		String path = mappings.getMetadataFor( claz ).getPath().toString().substring(1);
		AllocationTypeRepository repo = ((AllocationTypeRepository) getRepositories().getRepositoryFor(AllocationType.class).get());
		List<AllocationType> result = repo.findByProducerAndCompanyAndDeleted(path, company, deleted);
		result.addAll( repo.findByConsumerAndCompanyAndDeleted(path, company, deleted) );
		//System.out.println("findAllocationTypeByProducerClassOrConsumerClassAndCompanyAndDeleted for "+claz+" path "+path+" result size "+result.size());
		return result;
	}
	
	public AreaCategory findAreaCategoryByCodeAndCompany(String code, ManagedCompany company) {
		AreaCategoryRepository repo = ((AreaCategoryRepository) getRepositories().getRepositoryFor(AreaCategory.class).get());
		return repo.findFirstByCodeAndCompany(code, company);
	}
	
	public AreaCategory findAreaCategoryByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		AreaCategoryRepository repo = ((AreaCategoryRepository) getRepositories().getRepositoryFor(AreaCategory.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public Article findArticleByNameAndCompanyAndDeleted(String name, ManagedCompany company, boolean deleted) {
		ArticleRepository repo = ((ArticleRepository) getRepositories().getRepositoryFor(Article.class).get());
		return repo.findFirstByNameAndCompanyAndDeleted(name, company, deleted);
	}
	
	public Article findArticleByForeignId(String foreignId) {
		ArticleRepository repo = ((ArticleRepository) getRepositories().getRepositoryFor(Article.class).get());
		return repo.findFirstByForeignId(foreignId);
	}
	
	public List<ArticlePriceRate> findArticlePriceRateByVendorAndArticle(String vendor, Article article) {
		if(article != null && article.getId() == null) { //can't find by transient
			return new LinkedList<ArticlePriceRate>();
		}
		ArticlePriceRateRepository repo = ((ArticlePriceRateRepository) getRepositories().getRepositoryFor(ArticlePriceRate.class).get());
		return repo.findByVendorAndArticle(vendor, article);
	}
	
	public List<AttachableRevenuesAndExpenses> findAttachableRevenuesAndExpensesByArticleAndAttachableAndCompanyAndDeleted(Article article, Attachable attachable, ManagedCompany company, boolean deleted) {
		if(article != null && article.getId() == null
			|| attachable != null && attachable.getId() == null) { //can't find by transient
			return new LinkedList<AttachableRevenuesAndExpenses>();
		}
		AttachableRevenuesAndExpensesRepository repo = ((AttachableRevenuesAndExpensesRepository) getRepositories().getRepositoryFor(AttachableRevenuesAndExpenses.class).get());
		return repo.findByArticleAndAttachableAndCompanyAndDeleted(article, attachable, company, deleted);
	}

	public BankAccount findBankAccountByIbanAndCompanyAndDeleted(String iban, ManagedCompany company, boolean deleted) {
		BankAccountRepository repo = ((BankAccountRepository) getRepositories().getRepositoryFor(BankAccount.class).get());
		return repo.findFirstByIbanAndCompanyAndDeleted(iban, company, deleted);
	}

	public BusinessCategory findBusinessCategoryByCodeAndCompany(String code, ManagedCompany company) {
		BusinessCategoryRepository repo = ((BusinessCategoryRepository) getRepositories().getRepositoryFor(BusinessCategory.class).get());
		return repo.findFirstByCodeAndCompany(code, company);
	}
	
	public BusinessCategory findBusinessCategoryByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		BusinessCategoryRepository repo = ((BusinessCategoryRepository) getRepositories().getRepositoryFor(BusinessCategory.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public CCcGoodsType findCCcGoodsTypeByCodeAndOutCodeAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted) {
		CCcGoodsTypeRepository repo = ((CCcGoodsTypeRepository) getRepositories().getRepositoryFor(CCcGoodsType.class).get());
		return repo.findFirstByCodeAndOutCodeAndCompanyAndDeleted(code, outCode, company, deleted);
	}
	
	public CCcOrganizationUnit findCCcOrganizationUnitByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		CCcOrganizationUnitRepository repo = ((CCcOrganizationUnitRepository) getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public List<CCcOrganizationUnit> findCCcOrganizationUnitByOutIdAndCompanyAndDeleted(CCcOrganizationUnit outId, ManagedCompany company, boolean deleted) {
		CCcOrganizationUnitRepository repo = ((CCcOrganizationUnitRepository) getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get());
		return repo.findByOutIdAndCompanyAndDeleted(outId, company, deleted);
	}
	
	public CCcPartner findCCcPartnerByCodeAndOutCodeAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted) {
		CCcPartnerRepository repo = ((CCcPartnerRepository) getRepositories().getRepositoryFor(CCcPartner.class).get());
		return repo.findFirstByCodeAndOutCodeAndCompanyAndDeleted(code, outCode, company, deleted);
	}
	
	public CCfgDocumentPattern findCCfgDocumentPatternByDocumentCodeAndOutCodeAndCompanyAndDeleted(String documentCode, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted) {
		CCfgDocumentPatternRepository repo = ((CCfgDocumentPatternRepository) getRepositories().getRepositoryFor(CCfgDocumentPattern.class).get());
		return repo.findFirstByDocumentCodeAndOutCodeAndCompanyAndDeleted(documentCode, outCode, company, deleted);
	}

	public CCtBankAccount findCCtBankAccountByIdAndCompanyAndDeleted(Long id, ManagedCompany company, boolean deleted) {
		CCtBankAccountRepository repo = ((CCtBankAccountRepository) getRepositories().getRepositoryFor(CCtBankAccount.class).get());
		return repo.findFirstByIdAndCompanyAndDeleted(id, company, deleted);
	}

	public CCtCurrency findCCtCurrencyByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		CCtCurrencyRepository repo = ((CCtCurrencyRepository) getRepositories().getRepositoryFor(CCtCurrency.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public FCtInvDealType findFCtInvDealTypeByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		FCtInvDealTypeRepository repo = ((FCtInvDealTypeRepository) getRepositories().getRepositoryFor(FCtInvDealType.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public CCtPartnerGroup findCCtPartnerGroupByCodeAndOutCodeAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted) {
		CCtPartnerGroupRepository repo = ((CCtPartnerGroupRepository) getRepositories().getRepositoryFor(CCtPartnerGroup.class).get());
		return repo.findFirstByCodeAndOutCodeAndCompanyAndDeleted(code, outCode, company, deleted);
	}

	public CCtPartnerGroup findCCtPartnerGroupByIdAndCompanyAndDeleted(Long id, ManagedCompany company, boolean deleted) {
		CCtPartnerGroupRepository repo = ((CCtPartnerGroupRepository) getRepositories().getRepositoryFor(CCtPartnerGroup.class).get());
		return repo.findFirstByIdAndCompanyAndDeleted(id, company, deleted);
	}
	
	public FCtTransitionType findFCtTransitionTypeByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		FCtTransitionTypeRepository repo = ((FCtTransitionTypeRepository) getRepositories().getRepositoryFor(FCtTransitionType.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public List<CDelivery> findCDeliveryByGodIdAndCompanyAndDeleted(ManagedCompany company, boolean deleted) {
		CDeliveryRepository repo = ((CDeliveryRepository) getRepositories().getRepositoryFor(CDelivery.class).get());
		return repo.findFirstByCompanyAndDeleted(company, deleted);
	}
	
	public List<CDeliveryDetail> findCDeliveryDetailByGodIdAndCompanyAndDeleted(CGoods godId, ManagedCompany company, boolean deleted) {
		CDeliveryDetailRepository repo = ((CDeliveryDetailRepository) getRepositories().getRepositoryFor(CDeliveryDetail.class).get());
		return repo.findByGodIdAndCompanyAndDeleted(godId, company, deleted);
	}
	
	public CDeliveryGoodMap findCDeliveryGoodMapByDeyCodeAndParIdAndCompanyAndDeleted(String deyCode, CCcPartner parId, ManagedCompany company, boolean deleted) {
		CDeliveryGoodMapRepository repo = ((CDeliveryGoodMapRepository) getRepositories().getRepositoryFor(CDeliveryGoodMap.class).get());
		return repo.findFirstByDeyCodeAndParIdAndCompanyAndDeleted(deyCode, parId, company, deleted);
	}
	
	public CDeliveryGoodMap findCDeliveryGoodMapByParIdAndGodIdAndCompanyAndDeleted(CCcPartner parId, CGoods godId, ManagedCompany company, boolean deleted) {
		CDeliveryGoodMapRepository repo = ((CDeliveryGoodMapRepository) getRepositories().getRepositoryFor(CDeliveryGoodMap.class).get());
		return repo.findFirstByParIdAndGodIdAndCompanyAndDeleted(parId, godId, company, deleted);
	}

	public CGoodMark findCGoodMarkByMarkCodeAndCompany(String markCode, ManagedCompany company) {
		CGoodMarkRepository repo = ((CGoodMarkRepository) getRepositories().getRepositoryFor(CGoodMark.class).get());
		return repo.findFirstByMarkCodeAndCompany(markCode, company);
	}

	public CGoodMark findCGoodMarkByMarkCodeAndOutCodeAndCompanyAndDeleted(String markCode, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted) {
		CGoodMarkRepository repo = ((CGoodMarkRepository) getRepositories().getRepositoryFor(CGoodMark.class).get());
		return repo.findFirstByMarkCodeAndOutCodeAndCompanyAndDeleted(markCode, outCode, company, deleted);
	}
	
	public CMeasure findCMeasureByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		CMeasureRepository repo = ((CMeasureRepository) getRepositories().getRepositoryFor(CMeasure.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public COffer findCOfferByNumberOfrAndCompanyAndDeleted(Integer numberOfr, ManagedCompany company, boolean deleted) {
		COfferRepository repo = ((COfferRepository) getRepositories().getRepositoryFor(COffer.class).get());
		return repo.findFirstByNumberOfrAndCompanyAndDeleted(numberOfr, company, deleted);
	}
	
	public List<CMeasure> findCMeasureByMeeIdAndCompanyAndDeleted(CMeasure meeId, ManagedCompany company, boolean deleted) {
		CMeasureRepository repo = ((CMeasureRepository) getRepositories().getRepositoryFor(CMeasure.class).get());
		return repo.findByMeeIdAndCompanyAndDeleted(meeId, company, deleted);
	}
	
	public List<COffer> findCOfferByCompanyAndDeleted(ManagedCompany company, boolean deleted) {
		COfferRepository repo = ((COfferRepository) getRepositories().getRepositoryFor(COffer.class).get());
		return repo.findByCompanyAndDeleted(company, deleted);
	}
	
	public List<COfferDetail> findCOfferDetailByGodIdAndCompanyAndDeleted(CGoods godId, ManagedCompany company, boolean deleted) {
		COfferDetailRepository repo = ((COfferDetailRepository) getRepositories().getRepositoryFor(COfferDetail.class).get());
		return repo.findByGodIdAndCompanyAndDeleted(godId, company, deleted);
	}
	
	public CommentTemplate findCommentTemplateByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		CommentTemplateRepository repo = ((CommentTemplateRepository) getRepositories().getRepositoryFor(CommentTemplate.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public List<Contact> findContactByPerson(LegalPerson person) {
		if(person != null && person.getId() == null) { //can't find by transient
			return new LinkedList<Contact>();
		}
		ContactRepository repo = ((ContactRepository) getRepositories().getRepositoryFor(Contact.class).get());
		return repo.findByPerson(person);
	}
	
	public List<ContactType> findContactTypeByCodeAndCompany(Long code, ManagedCompany company) {
		ContactTypeRepository repo = ((ContactTypeRepository) getRepositories().getRepositoryFor(ContactType.class).get());
		return repo.findByCodeAndCompany(code, company);
	}
	
	public ContactType findContactTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		ContactTypeRepository repo = ((ContactTypeRepository) getRepositories().getRepositoryFor(ContactType.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public List<COrder> findCOrderByCompanyAndDeleted(ManagedCompany company, boolean deleted) {
		COrderRepository repo = ((COrderRepository) getRepositories().getRepositoryFor(COrder.class).get());
		return repo.findCOrderByCompanyAndDeleted(company, deleted);
	}
	
	public List<COrderDetail> findCOrderDetailByGodIdAndCompanyAndDeleted(CGoods godId, ManagedCompany company, boolean deleted) {
		COrderDetailRepository repo = ((COrderDetailRepository) getRepositories().getRepositoryFor(COrderDetail.class).get());
		return repo.findByGodIdAndCompanyAndDeleted(godId, company, deleted);
	}
	
	public COrderDetail findCOrderDetailByOrrIdAndGodIdAndCompanyAndDeleted(COrder orrId, CGoods godId, ManagedCompany company, boolean deleted) {
		COrderDetailRepository repo = ((COrderDetailRepository) getRepositories().getRepositoryFor(COrderDetail.class).get());
		return repo.findFirstByOrrIdAndGodIdAndCompanyAndDeleted(orrId, godId, company, deleted);
	}
	
	public CPmtCurrencyRate findCPmtCurrencyRateByCuyCodeAndDateFromLessThanEqualAndDateToGreaterThanEqualAndCompanyAndDeleted(CCtCurrency cuyCode, LocalDate date, ManagedCompany company, boolean deleted) {
		CPmtCurrencyRateRepository repo = ((CPmtCurrencyRateRepository) getRepositories().getRepositoryFor(CPmtCurrencyRate.class).get());
		return repo.findFirstByCuyCodeAndDateFromLessThanEqualAndDateToGreaterThanEqualAndCompanyAndDeleted(cuyCode, date, date, company, deleted);
	}
	
	public List<CPmtCurrencyRate> findCPmtCurrencyRateByCuyCodeAndOutCodeAndCompanyAndDeleted(CCtCurrency cuyCode, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted) {
		CPmtCurrencyRateRepository repo = ((CPmtCurrencyRateRepository) getRepositories().getRepositoryFor(CPmtCurrencyRate.class).get());
		return repo.findByCuyCodeAndOutCodeAndCompanyAndDeleted(cuyCode, outCode, company, deleted);
	}
	
	public List<CPriceList> findCPriceListByParIdAndGodIdAndCompanyAndDeleted(CCcPartner parId, CGoods godId, ManagedCompany company, boolean deleted) {
		CPriceListRepository repo = ((CPriceListRepository) getRepositories().getRepositoryFor(CPriceList.class).get());
		return repo.findByParIdAndGodIdAndCompanyAndDeleted(parId, godId, company, deleted);
	}

	public List<CReserveQuantity> findCReserveQuantityByOdlIdAndCompanyAndDeleted(COfferDetail odlId, ManagedCompany company, boolean deleted) {
		CReserveQuantityRepository repo = ((CReserveQuantityRepository) getRepositories().getRepositoryFor(CReserveQuantity.class).get());
		return repo.findByOdlIdAndCompanyAndDeleted(odlId, company, deleted);
	}

	public List<CReserveQuantity> findCReserveQuantityByOdlIdAndStkIdAndCompanyAndDeleted(COfferDetail odlId, CStock stkId, ManagedCompany company, boolean deleted) {
		CReserveQuantityRepository repo = ((CReserveQuantityRepository) getRepositories().getRepositoryFor(CReserveQuantity.class).get());
		return repo.findByOdlIdAndStkIdAndCompanyAndDeleted(odlId, stkId, company, deleted);
	}

	public List<CReserveQuantity> findCReserveQuantityByOfrIdAndCompanyAndDeleted(COffer ofrId, ManagedCompany company, boolean deleted) {
		CReserveQuantityRepository repo = ((CReserveQuantityRepository) getRepositories().getRepositoryFor(CReserveQuantity.class).get());
		return repo.findByOfrIdAndCompanyAndDeleted(ofrId, company, deleted);
	}

	public List<CReserveQuantity> findCReserveQuantityByStkIdAndCompanyAndDeleted(CStock stkId, ManagedCompany company, boolean deleted) {
		CReserveQuantityRepository repo = ((CReserveQuantityRepository) getRepositories().getRepositoryFor(CReserveQuantity.class).get());
		return repo.findByStkIdAndCompanyAndDeleted(stkId, company, deleted);
	}
	
	public List<CSale> findCSaleByCompanyAndDeleted(ManagedCompany company, boolean deleted) {
		CSaleRepository repo = ((CSaleRepository) getRepositories().getRepositoryFor(CSale.class).get());
		return repo.findCSaleByCompanyAndDeleted(company, deleted);
	}

	public List<CSaleDetail> findCSaleDetailByOdlIdAndStkIdAndCompanyAndDeleted(COfferDetail odlId, CStock stkId, ManagedCompany company, boolean deleted) {
		CSaleDetailRepository repo = ((CSaleDetailRepository) getRepositories().getRepositoryFor(CSaleDetail.class).get());
		return repo.findByOdlIdAndStkIdAndCompanyAndDeleted(odlId, stkId, company, deleted);
	}
	
	public CService findCServiceByCodeAndOutCodeAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted) {
		CServiceRepository repo = ((CServiceRepository) getRepositories().getRepositoryFor(CService.class).get());
		return repo.findFirstByCodeAndOutCodeAndCompanyAndDeleted(code, outCode, company, deleted);
	}
	
	public List<CStock> findCStockByGodIdAndOutCodeAndCompanyAndDeleted(CGoods godId, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted) {
		CStockRepository repo = ((CStockRepository) getRepositories().getRepositoryFor(CStock.class).get());
		return repo.findByGodIdAndOutCodeAndCompanyAndDeleted(godId, outCode, company, deleted);
	}

	public CTypeDocument findCTypeDocumentByShortNameAndCompanyAndDeleted(String shortName, ManagedCompany company, boolean deleted) {
		CTypeDocumentRepository repo = ((CTypeDocumentRepository) getRepositories().getRepositoryFor(CTypeDocument.class).get());
		return repo.findFirstByShortNameAndCompanyAndDeleted(shortName, company, deleted);
	}

	public Currency findCurrencyByNameAndCompany(String name, ManagedCompany company) {
		CurrencyRepository repo = ((CurrencyRepository) getRepositories().getRepositoryFor(Currency.class).get());
		return repo.findFirstByNameAndCompany(name, company);
	}

	public DirectionCategory findDirectionCategoryByCodeAndCompany(String code, ManagedCompany company) {
		DirectionCategoryRepository repo = ((DirectionCategoryRepository) getRepositories().getRepositoryFor(DirectionCategory.class).get());
		return repo.findFirstByCodeAndCompany(code, company);
	}
	
	public DirectionCategory findDirectionCategoryByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		DirectionCategoryRepository repo = ((DirectionCategoryRepository) getRepositories().getRepositoryFor(DirectionCategory.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public ElectricityInvoice findElectricityInvoiceByReportingPointOwnAndPeriodFromAndPeriodToAndCompanyAndDeleted(String reportingPointOwn, LocalDate periodFrom, LocalDate periodTo, ManagedCompany company, boolean deleted) {
		ElectricityInvoiceRepository repo = ((ElectricityInvoiceRepository) getRepositories().getRepositoryFor(ElectricityInvoice.class).get());
		return repo.findFirstByReportingPointOwnAndPeriodFromAndPeriodToAndCompanyAndDeleted(reportingPointOwn, periodFrom, periodTo, company, deleted);
	}

	public FCcContract findFCcContractByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted) {
		FCcContractRepository repo = ((FCcContractRepository) getRepositories().getRepositoryFor(FCcContract.class).get());
		return repo.findFirstByOutCodeAndCodeAndCompanyAndDeleted(outCode, code, company, deleted);
	}

	public FCcEbk findFCcEbkByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted) {
		FCcEbkRepository repo = ((FCcEbkRepository) getRepositories().getRepositoryFor(FCcEbk.class).get());
		return repo.findFirstByOutCodeAndCodeAndCompanyAndDeleted(outCode, code, company, deleted);
	}

	public FCcFinsource findFCcFinsourceByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted) {
		FCcFinsourceRepository repo = ((FCcFinsourceRepository) getRepositories().getRepositoryFor(FCcFinsource.class).get());
		return repo.findFirstByOutCodeAndCodeAndCompanyAndDeleted(outCode, code, company, deleted);
	}

	public FCcFunction findFCcFunctionByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted) {
		FCcFunctionRepository repo = ((FCcFunctionRepository) getRepositories().getRepositoryFor(FCcFunction.class).get());
		return repo.findFirstByOutCodeAndCodeAndCompanyAndDeleted(outCode, code, company, deleted);
	}

	public FCcProgram findFCcProgramByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted) {
		FCcProgramRepository repo = ((FCcProgramRepository) getRepositories().getRepositoryFor(FCcProgram.class).get());
		return repo.findFirstByOutCodeAndCodeAndCompanyAndDeleted(outCode, code, company, deleted);
	}

	public FCcReserve1 findFCcReserve1ByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted) {
		FCcReserve1Repository repo = ((FCcReserve1Repository) getRepositories().getRepositoryFor(FCcReserve1.class).get());
		return repo.findFirstByOutCodeAndCodeAndCompanyAndDeleted(outCode, code, company, deleted);
	}

	public FCcReserve2 findFCcReserve2ByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted) {
		FCcReserve2Repository repo = ((FCcReserve2Repository) getRepositories().getRepositoryFor(FCcReserve2.class).get());
		return repo.findFirstByOutCodeAndCodeAndCompanyAndDeleted(outCode, code, company, deleted);
	}

	public FChartAccount findFChartAccountByOutCodeAndCodeAndCompanyAndDeleted(CCcOrganizationUnit outCode, String code, ManagedCompany company, boolean deleted) {
		FChartAccountRepository repo = ((FChartAccountRepository) getRepositories().getRepositoryFor(FChartAccount.class).get());
		return repo.findFirstByOutCodeAndCodeAndCompanyAndDeleted(outCode, code, company, deleted);
	}

	public FCtBatchJteDefault findFCtBatchJteDefaultByOutCodeAndBteIdAndJteIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtBatchType bteId, FJournalType jteId, ManagedCompany company, boolean deleted) {
		FCtBatchJteDefaultRepository repo = ((FCtBatchJteDefaultRepository) getRepositories().getRepositoryFor(FCtBatchJteDefault.class).get());
		return repo.findFirstByOutCodeAndBteIdAndJteIdAndCompanyAndDeleted(outCode, bteId, jteId, company, deleted);
	}

	public FCtBatchTteLink findFCtBatchTteLinkByOutCodeAndTteIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtBatchType bteId, FCtTransitionType tteId, FCtInvDealType ideId, ManagedCompany company, boolean deleted) {
		FCtBatchTteLinkRepository repo = ((FCtBatchTteLinkRepository) getRepositories().getRepositoryFor(FCtBatchTteLink.class).get());
		return repo.findFirstByOutCodeAndBteIdAndTteIdAndIdeIdAndCompanyAndDeleted(outCode, bteId, tteId, ideId, company, deleted);
	}

	public FCtBatchTteType findFCtBatchTteTypeByOutCodeAndTteIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtTransitionType tteId, ManagedCompany company, boolean deleted) {
		FCtBatchTteTypeRepository repo = ((FCtBatchTteTypeRepository) getRepositories().getRepositoryFor(FCtBatchTteType.class).get());
		return repo.findFirstByOutCodeAndTteIdAndCompanyAndDeleted(outCode, tteId, company, deleted);
	}

	public FCtBatchType findFCtBatchTypeByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		FCtBatchTypeRepository repo = ((FCtBatchTypeRepository) getRepositories().getRepositoryFor(FCtBatchType.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public FCtBatchTypeRule findFCtBatchTypeRuleByOutCodeAndTteIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtTransitionType tteId, ManagedCompany company, boolean deleted) {
		FCtBatchTypeRuleRepository repo = ((FCtBatchTypeRuleRepository) getRepositories().getRepositoryFor(FCtBatchTypeRule.class).get());
		return repo.findFirstByOutCodeAndTteIdAndCompanyAndDeleted(outCode, tteId, company, deleted);
	}

	public FCtBatchTypeRule findFCtBatchTypeRuleByOutCodeAndTteIdAndRueIdAndDependenceTypeAndDependenceIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtTransitionType tteId, FCtRule rueId, LoiBatchTypeRuleDependenceType dependenceType, Integer dependenceId, ManagedCompany company, boolean deleted) {
		FCtBatchTypeRuleRepository repo = ((FCtBatchTypeRuleRepository) getRepositories().getRepositoryFor(FCtBatchTypeRule.class).get());
		return repo.findFirstByOutCodeAndTteIdAndRueIdAndDependenceTypeAndDependenceIdAndCompanyAndDeleted(outCode, tteId, rueId, dependenceType, dependenceId, company, deleted);
	}

	public FCtBatchTypeRule findFCtBatchTypeRuleByOutCodeAndTteIdAndRueIdAndOrderNumAndDependenceTypeAndDependenceIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtTransitionType tteId, FCtRule rueId, Integer orderNum, LoiBatchTypeRuleDependenceType dependenceType, Integer dependenceId, ManagedCompany company, boolean deleted) {
		FCtBatchTypeRuleRepository repo = ((FCtBatchTypeRuleRepository) getRepositories().getRepositoryFor(FCtBatchTypeRule.class).get());
		return repo.findFirstByOutCodeAndTteIdAndRueIdAndOrderNumAndDependenceTypeAndDependenceIdAndCompanyAndDeleted(outCode, tteId, rueId, orderNum, dependenceType, dependenceId, company, deleted);
	}
	
	public FCtBatchTypeRule findFCtBatchTypeRuleByOutCodeAndAmountTypeAndTteIdAndRueIdAndJteIdAndCoaIdCtAndCoaIdDtAndActiveFromDateLessThanEqualAndActiveToDateGreaterThanEqualAndCompanyAndDeleted(CCcOrganizationUnit outCode, LoiBatchTypeRuleAmountType amountType, FCtTransitionType tteId, FCtRule rueId, FJournalType jteId, FChartAccount coaIdCt, FChartAccount coaIdDt, LocalDate activeFromDate, LocalDate activeToDate, ManagedCompany company, boolean deleted) {
		FCtBatchTypeRuleRepository repo = ((FCtBatchTypeRuleRepository) getRepositories().getRepositoryFor(FCtBatchTypeRule.class).get());
		return repo.findFirstByOutCodeAndAmountTypeAndTteIdAndRueIdAndJteIdAndCoaIdCtAndCoaIdDtAndActiveFromDateLessThanEqualAndActiveToDateGreaterThanEqualAndCompanyAndDeleted(outCode, amountType, tteId, rueId, jteId, coaIdCt, coaIdDt, activeFromDate, activeToDate, company, deleted);
	}

	public FCtGood findFCtGoodByCodeAndOutCodeAndGteIdAndCompanyAndDeleted(String code, CCcOrganizationUnit outCode, CCcGoodsType gteId, ManagedCompany company, boolean deleted) {
		FCtGoodRepository repo = ((FCtGoodRepository) getRepositories().getRepositoryFor(FCtGood.class).get());
		return repo.findFirstByCodeAndOutCodeAndGteIdAndCompanyAndDeleted(code, outCode, gteId, company, deleted);
	}
	
	public FCtJteDefault findFCtJteDefaultByJteAndOutCodeAndCompanyAndDeleted(FJournalType jte, CCcOrganizationUnit outCode, ManagedCompany company, boolean deleted) {
		FCtJteDefaultRepository repo = ((FCtJteDefaultRepository) getRepositories().getRepositoryFor(FCtJteDefault.class).get());
		return repo.findFirstByJteAndOutCodeAndCompanyAndDeleted(jte, outCode, company, deleted);
	}

	public FCtRepresentative findFCtRepresentativeByOutCodeAndIdeNoAndCompanyAndDeleted(CCcOrganizationUnit outCode,String ideNo, ManagedCompany company, boolean deleted) {
		FCtRepresentativeRepository repo = ((FCtRepresentativeRepository) getRepositories().getRepositoryFor(FCtRepresentative.class).get());
		return repo.findFirstByOutCodeAndIdeNoAndCompanyAndDeleted(outCode, ideNo, company, deleted);
	}
	
	public FCtRule findFCtRuleByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		FCtRuleRepository repo = ((FCtRuleRepository) getRepositories().getRepositoryFor(FCtRule.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public FCtStornoType findFCtStornoTypeByOutCodeAndTypeAndCompanyAndDeleted(CCcOrganizationUnit outCode, LoiCtStornoTypeType type, ManagedCompany company, boolean deleted) {
		FCtStornoTypeRepository repo = ((FCtStornoTypeRepository) getRepositories().getRepositoryFor(FCtStornoType.class).get());
		return repo.findFirstByOutCodeAndTypeAndCompanyAndDeleted(outCode, type, company, deleted);
	}
	
	public FDiscount findFDiscountByGodAndCompanyAndDeleted(CGoods god, ManagedCompany company, boolean deleted) {
		FDiscountRepository repo = ((FDiscountRepository) getRepositories().getRepositoryFor(FDiscount.class).get());
		return repo.findFirstByGodAndCompanyAndDeleted(god, company, deleted);
	}
	
	public FDiscount findFDiscountByGteIdAndCompanyAndDeleted(CCcGoodsType gteId, ManagedCompany company, boolean deleted) {
		FDiscountRepository repo = ((FDiscountRepository) getRepositories().getRepositoryFor(FDiscount.class).get());
		return repo.findFirstByGteIdAndCompanyAndDeleted(gteId, company, deleted);
	}
	
	public FDiscount findFDiscountByParIdAndCompanyAndDeleted(CCcPartner parId, ManagedCompany company, boolean deleted) {
		FDiscountRepository repo = ((FDiscountRepository) getRepositories().getRepositoryFor(FDiscount.class).get());
		return repo.findFirstByParIdAndCompanyAndDeleted(parId, company, deleted);
	}
	
	public FDiscount findFDiscountByPgpIdAndCompanyAndDeleted(CCtPartnerGroup pgpId, ManagedCompany company, boolean deleted) {
		FDiscountRepository repo = ((FDiscountRepository) getRepositories().getRepositoryFor(FDiscount.class).get());
		return repo.findFirstByPgpIdAndCompanyAndDeleted(pgpId, company, deleted);
	}

	public FJournalType findFJournalTypeByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		FJournalTypeRepository repo = ((FJournalTypeRepository) getRepositories().getRepositoryFor(FJournalType.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public FInvDdsFile findFInvDdsFileByOutCodeAndPeriodAndCompanyAndDeleted(CCcOrganizationUnit outCode,String period, ManagedCompany company, boolean deleted) {
		FInvDdsFileRepository repo = ((FInvDdsFileRepository) getRepositories().getRepositoryFor(FInvDdsFile.class).get());
		return repo.findFirstByOutCodeAndPeriodAndCompanyAndDeleted(outCode, period, company, deleted);
	}

	public FPtBatch findFPtBatchByPostDateAndOutCodeAndRefNoAndBteIdAndParIdAndCompanyAndDeleted(LocalDate postDate, CCcOrganizationUnit outCode, String refNo, FCtBatchType bteId, CCcPartner parId, ManagedCompany company, boolean deleted) {
		FPtBatchRepository repo = ((FPtBatchRepository) getRepositories().getRepositoryFor(FPtBatch.class).get());
		return repo.findFirstByPostDateAndOutCodeAndRefNoAndBteIdAndParIdAndCompanyAndDeleted(postDate, outCode, refNo, bteId, parId, company, deleted);
	}

	public FPtClosingAccount findFPtClosingAccountByOutCodeAndPeriodAndCompanyAndDeleted(CCcOrganizationUnit outCode, LoiPtClosingAccountPeriodMonth periodMonth, String periodYear, ManagedCompany company, boolean deleted) {
		FPtClosingAccountRepository repo = ((FPtClosingAccountRepository) getRepositories().getRepositoryFor(FPtClosingAccount.class).get());
		return repo.findFirstByOutCodeAndPeriodMonthAndPeriodYearAndCompanyAndDeleted(outCode, periodMonth, periodYear, company, deleted);
	}

	public FPtCoaBalance findFPtCoaBalanceByCoaIdAndOutCodeAndPeriodAndCompanyAndDeleted(FChartAccount coaId, CCcOrganizationUnit outCode, String period, ManagedCompany company, boolean deleted) {
		FPtCoaBalanceRepository repo = ((FPtCoaBalanceRepository) getRepositories().getRepositoryFor(FPtCoaBalance.class).get());
		return repo.findFirstByCoaIdAndOutCodeAndPeriodAndCompanyAndDeleted(coaId, outCode, period, company, deleted);
	}

	public GeneralCategory findGeneralCategoryByCodeAndCompany(String code, ManagedCompany company) {
		GeneralCategoryRepository repo = ((GeneralCategoryRepository) getRepositories().getRepositoryFor(GeneralCategory.class).get());
		return repo.findFirstByCodeAndCompany(code, company);
	}
	
	public GeneralCategory findGeneralCategoryByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		GeneralCategoryRepository repo = ((GeneralCategoryRepository) getRepositories().getRepositoryFor(GeneralCategory.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public IbexEnergyDeal findIbexEnergyDealByTradeIdAndCompanyAndDeleted(String tradeId, ManagedCompany company, boolean deleted) {
		IbexEnergyDealRepository repo = ((IbexEnergyDealRepository) getRepositories().getRepositoryFor(IbexEnergyDeal.class).get());
		return repo.findFirstByTradeIdAndCompanyAndDeleted(tradeId, company, deleted);
	}
	
	public List<ImportedArticle> findImportedArticleByForeignId(String foreignId) {
		ImportedArticleRepository repo = ((ImportedArticleRepository) getRepositories().getRepositoryFor(ImportedArticle.class).get());
		return repo.findByForeignId(foreignId);
	}
	
	public List<ImportedExpeditionList> findImportedExpeditionListByForeignId(String foreignId) {
		ImportedExpeditionListRepository repo = ((ImportedExpeditionListRepository) getRepositories().getRepositoryFor(ImportedExpeditionList.class).get());
		return repo.findByForeignId(foreignId);
	}
	
	public List<ImportedExpeditionList> findImportedExpeditionListByForeignOrderIdAndOrder(String foreignId, ImportedOrder order) {
		ImportedExpeditionListRepository repo = ((ImportedExpeditionListRepository) getRepositories().getRepositoryFor(ImportedExpeditionList.class).get());
		return repo.findByForeignOrderIdAndOrder(foreignId, order);
	}
	
	public List<ImportedExpeditionListRow> findImportedExpeditionListRowByForeignExpeditionListIdAndExpeditionList(String foreignId, ImportedExpeditionList expeditionList) {
		ImportedExpeditionListRowRepository repo = ((ImportedExpeditionListRowRepository) getRepositories().getRepositoryFor(ImportedExpeditionListRow.class).get());
		return repo.findByForeignExpeditionListIdAndExpeditionList(foreignId, expeditionList);
	}
	
	public List<ImportedExpeditionListRow> findImportedExpeditionListRowByForeignOrderRowIdAndOrderRow(String foreignId, ImportedOrderRow orderRow) {
		ImportedExpeditionListRowRepository repo = ((ImportedExpeditionListRowRepository) getRepositories().getRepositoryFor(ImportedExpeditionListRow.class).get());
		return repo.findByForeignOrderRowIdAndOrderRow(foreignId, orderRow);
	}
	
	public List<ImportedExpeditionListRow> findImportedExpeditionListRowByOrderRow(ImportedOrderRow orderRow) {
		ImportedExpeditionListRowRepository repo = ((ImportedExpeditionListRowRepository) getRepositories().getRepositoryFor(ImportedExpeditionListRow.class).get());
		return repo.findByOrderRow(orderRow);
	}
	
	public List<ImportedInvoice> findImportedInvoiceByForeignId(String foreignId) {
		ImportedInvoiceRepository repo = ((ImportedInvoiceRepository) getRepositories().getRepositoryFor(ImportedInvoice.class).get());
		return repo.findByForeignId(foreignId);
	}
	
	public List<ImportedInvoice> findImportedInvoiceByForeignOrderIdAndOrder(String foreignId, ImportedOrder order) {
		ImportedInvoiceRepository repo = ((ImportedInvoiceRepository) getRepositories().getRepositoryFor(ImportedInvoice.class).get());
		return repo.findByForeignOrderIdAndOrder(foreignId, order);
	}
	
	public List<ImportedInvoicePayment> findImportedInvoicePaymentByForeignContragentIdAndContragent(String foreignId, ImportedLegalPerson contragent) {
		ImportedInvoicePaymentRepository repo = ((ImportedInvoicePaymentRepository) getRepositories().getRepositoryFor(ImportedInvoicePayment.class).get());
		return repo.findByForeignContragentIdAndContragent(foreignId, contragent);
	}
	
	public List<ImportedInvoicePayment> findImportedInvoicePaymentByForeignIdAndInvoice(String foreignId, ImportedInvoice invoice) {
		ImportedInvoicePaymentRepository repo = ((ImportedInvoicePaymentRepository) getRepositories().getRepositoryFor(ImportedInvoicePayment.class).get());
		return repo.findByForeignIdAndInvoice(foreignId, invoice);
	}
	
	public List<ImportedInvoiceRow> findImportedInvoiceRowByForeignInvoiceIdAndInvoice(String foreignInvoiceId, ImportedInvoice invoice) {
		ImportedInvoiceRowRepository repo = ((ImportedInvoiceRowRepository) getRepositories().getRepositoryFor(ImportedInvoiceRow.class).get());
		return repo.findByForeignInvoiceIdAndInvoice(foreignInvoiceId, invoice);
	}
	
	public List<ImportedInvoiceRow> findImportedInvoiceRowByForeignOrderRowIdAndOrderRow(String foreignOrderRowId, ImportedOrderRow orderRow) {
		ImportedInvoiceRowRepository repo = ((ImportedInvoiceRowRepository) getRepositories().getRepositoryFor(ImportedInvoiceRow.class).get());
		return repo.findByForeignOrderRowIdAndOrderRow(foreignOrderRowId, orderRow);
	}
	
	public List<ImportedLegalPerson> findImportedLegalPersonByForeignId(String foreignId) {
		ImportedLegalPersonRepository repo = ((ImportedLegalPersonRepository) getRepositories().getRepositoryFor(ImportedLegalPerson.class).get());
		return repo.findByForeignId(foreignId);
	}
	
	public List<ImportedOrder> findImportedOrderByForeignId(String foreignId) {
		ImportedOrderRepository repo = ((ImportedOrderRepository) getRepositories().getRepositoryFor(ImportedOrder.class).get());
		return repo.findByForeignId(foreignId);
	}
	
	public List<ImportedOrder> findImportedOrderByForeignIdAndOffer(String foreignId, OfferToClient offer) {
		ImportedOrderRepository repo = ((ImportedOrderRepository) getRepositories().getRepositoryFor(ImportedOrder.class).get());
		return repo.findByForeignIdAndOffer(foreignId, offer);
	}
	
	public List<ImportedOrder> findImportedOrderByOffer(OfferToClient offer) {
		ImportedOrderRepository repo = ((ImportedOrderRepository) getRepositories().getRepositoryFor(ImportedOrder.class).get());
		return repo.findByOffer(offer);
	}
	
	public List<ImportedOrderRow> findImportedOrderRowByArticle(ImportedArticle article) {
		ImportedOrderRowRepository repo = ((ImportedOrderRowRepository) getRepositories().getRepositoryFor(ImportedOrderRow.class).get());
		return repo.findByArticle(article);
	}
	
	public List<ImportedOrderRow> findImportedOrderRowByForeignArticleIdAndArticle(String foreignArticleId, ImportedArticle article) {
		ImportedOrderRowRepository repo = ((ImportedOrderRowRepository) getRepositories().getRepositoryFor(ImportedOrderRow.class).get());
		return repo.findByForeignArticleIdAndArticle(foreignArticleId, article);
	}
	
	public List<ImportedOrderRow> findImportedOrderRowByForeignId(String foreignId) {
		ImportedOrderRowRepository repo = ((ImportedOrderRowRepository) getRepositories().getRepositoryFor(ImportedOrderRow.class).get());
		return repo.findByForeignId(foreignId);
	}
	
	public List<ImportedOrderRow> findImportedOrderRowByForeignOrderIdAndOrder(String foreignOrderId, ImportedOrder order) {
		ImportedOrderRowRepository repo = ((ImportedOrderRowRepository) getRepositories().getRepositoryFor(ImportedOrderRow.class).get());
		return repo.findByForeignOrderIdAndOrder(foreignOrderId, order);
	}
	
	public List<ImportedOrderRow> findImportedOrderRowByOrderAndArticleAndOfferLine(ImportedOrder order, ImportedArticle article, OfferLine offerLine) {
		ImportedOrderRowRepository repo = ((ImportedOrderRowRepository) getRepositories().getRepositoryFor(ImportedOrderRow.class).get());
		return repo.findByOrderAndArticleAndOfferLine(order, article, offerLine);
	}
	
	public List<ImportedWarehouseStock> findImportedWarehouseStockByForeignArticleId(String foreignId) {
		ImportedWarehouseStockRepository repo = ((ImportedWarehouseStockRepository) getRepositories().getRepositoryFor(ImportedWarehouseStock.class).get());
		return repo.findByForeignArticleId(foreignId);
	}

	public ImportQuantity findImportQuantityByReportingPointOwnAndCompanyAndDeleted(String reportingPointOwn, ManagedCompany company, boolean deleted) {
		ImportQuantityRepository repo = ((ImportQuantityRepository) getRepositories().getRepositoryFor(ImportQuantity.class).get());
		return repo.findFirstByReportingPointOwnAndCompanyAndDeleted(reportingPointOwn, company, deleted);
	}

	public ImportValueAndQuantity findImportValueAndQuantityByReportingPointOwnAndCompanyAndDeleted(String reportingPointOwn, ManagedCompany company, boolean deleted) {
		ImportValueAndQuantityRepository repo = ((ImportValueAndQuantityRepository) getRepositories().getRepositoryFor(ImportValueAndQuantity.class).get());
		return repo.findFirstByReportingPointOwnAndCompanyAndDeleted(reportingPointOwn, company, deleted);
	}

	public ImportValue findImportValueByReportingPointOwnAndCompanyAndDeleted(String reportingPointOwn, ManagedCompany company, boolean deleted) {
		ImportValueRepository repo = ((ImportValueRepository) getRepositories().getRepositoryFor(ImportValue.class).get());
		return repo.findFirstByReportingPointOwnAndCompanyAndDeleted(reportingPointOwn, company, deleted);
	}

	public Income findIncomeByNameAndCompanyAndDeleted(String name, ManagedCompany company, boolean deleted) {
		IncomeRepository repo = ((IncomeRepository) getRepositories().getRepositoryFor(Income.class).get());
		return repo.findFirstByNameAndCompanyAndDeleted(name, company, deleted);
	}
	
	public JobPosition findJobPositionByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		JobPositionRepository repo = ((JobPositionRepository) getRepositories().getRepositoryFor(JobPosition.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public JobRequirement findJobRequirementByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		JobRequirementRepository repo = ((JobRequirementRepository) getRepositories().getRepositoryFor(JobRequirement.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public List<LegalPerson> findLegalPersonByVatNumberAndCompany(String vatNumber, ManagedCompany company) {
		LegalPersonRepository repo = ((LegalPersonRepository) getRepositories().getRepositoryFor(LegalPerson.class).get());
		return repo.findByVatNumberAndCompany(vatNumber, company);
	}
	
	public LegalStatus findLegalStatusByCodeAndCompany(Long code, ManagedCompany company) {
		LegalStatusRepository repo = ((LegalStatusRepository) getRepositories().getRepositoryFor(LegalStatus.class).get());
		return repo.findFirstByCodeAndCompany(code, company);
	}
	
	public LegalStatus findLegalStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LegalStatusRepository repo = ((LegalStatusRepository) getRepositories().getRepositoryFor(LegalStatus.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LegalPerson findLegalPersonByNameAndCompanyAndDeleted(String name, ManagedCompany company, boolean deleted) {
		LegalPersonRepository repo = ((LegalPersonRepository) getRepositories().getRepositoryFor(LegalPerson.class).get());
		return repo.findFirstByNameAndCompanyAndDeleted(name, company, deleted);
	}
	
	public List<LegalPerson> findLegalPersonByEik(String eik) {
		LegalPersonRepository repo = ((LegalPersonRepository) getRepositories().getRepositoryFor(LegalPerson.class).get());
		return repo.findByEik(eik);
	}
	
	public LegalPerson findLegalPersonByEikAndCompanyAndDeleted(String eik, ManagedCompany company, boolean deleted) {
		LegalPersonRepository repo = ((LegalPersonRepository) getRepositories().getRepositoryFor(LegalPerson.class).get());
		return repo.findFirstByEikAndCompanyAndDeleted(eik, company, deleted);
	}
	
	public ListOptionItem findListOptionItemByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		ListOptionItemRepository repo = ((ListOptionItemRepository) getRepositories().getRepositoryFor(ListOptionItem.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiAccountActivityType findLoiAccountActivityTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiAccountActivityTypeRepository repo = ((LoiAccountActivityTypeRepository) getRepositories().getRepositoryFor(LoiAccountActivityType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiAgreementStatus findLoiAgreementStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiAgreementStatusRepository repo = ((LoiAgreementStatusRepository) getRepositories().getRepositoryFor(LoiAgreementStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiDocumentType findLoiDocumentTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiDocumentTypeRepository repo = ((LoiDocumentTypeRepository) getRepositories().getRepositoryFor(LoiDocumentType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiMeasurementUnit findLoiMeasurementUnitByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiMeasurementUnitRepository repo = ((LoiMeasurementUnitRepository) getRepositories().getRepositoryFor(LoiMeasurementUnit.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiBatchCalculationType findLoiBatchCalculationTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiBatchCalculationTypeRepository repo = ((LoiBatchCalculationTypeRepository) getRepositories().getRepositoryFor(LoiBatchCalculationType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiBatchJteDefaultSide findLoiBatchJteDefaultSideByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiBatchJteDefaultSideRepository repo = ((LoiBatchJteDefaultSideRepository) getRepositories().getRepositoryFor(LoiBatchJteDefaultSide.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiBatchTteLinkStatus findLoiBatchTteLinkStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiBatchTteLinkStatusRepository repo = ((LoiBatchTteLinkStatusRepository) getRepositories().getRepositoryFor(LoiBatchTteLinkStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiBatchTypeRuleAmountType findLoiBatchTypeRuleAmountTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiBatchTypeRuleAmountTypeRepository repo = ((LoiBatchTypeRuleAmountTypeRepository) getRepositories().getRepositoryFor(LoiBatchTypeRuleAmountType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiBatchTypeRuleDependenceType findLoiBatchTypeRuleDependenceTypeByListOptionItemCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiBatchTypeRuleDependenceTypeRepository repo = ((LoiBatchTypeRuleDependenceTypeRepository) getRepositories().getRepositoryFor(LoiBatchTypeRuleDependenceType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiBatchTypeRuleDependenceType findLoiBatchTypeRuleDependenceTypeByListOptionItemNameAndCompanyAndDeleted(String listOptionItemName, ManagedCompany company, boolean deleted) {
		LoiBatchTypeRuleDependenceTypeRepository repo = ((LoiBatchTypeRuleDependenceTypeRepository) getRepositories().getRepositoryFor(LoiBatchTypeRuleDependenceType.class).get());
		return repo.findFirstByListOptionItemNameAndCompanyAndDeleted(listOptionItemName, company, deleted);
	}

	public LoiBreTransitionResult findLoiBreTransitionResultByListOptionItemCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiBreTransitionResultRepository repo = ((LoiBreTransitionResultRepository) getRepositories().getRepositoryFor(LoiBreTransitionResult.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiContractFee findLoiContractFeeByListOptionItemCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiContractFeeRepository repo = ((LoiContractFeeRepository) getRepositories().getRepositoryFor(LoiContractFee.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiContractPrice findLoiContractPriceByListOptionItemCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiContractPriceRepository repo = ((LoiContractPriceRepository) getRepositories().getRepositoryFor(LoiContractPrice.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiContractQuantity findLoiContractQuantityByListOptionItemCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiContractQuantityRepository repo = ((LoiContractQuantityRepository) getRepositories().getRepositoryFor(LoiContractQuantity.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiContractStatus findLoiContractStatusByListOptionItemCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiContractStatusRepository repo = ((LoiContractStatusRepository) getRepositories().getRepositoryFor(LoiContractStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiCtBankAccountBatType findLoiCtBankAccountBatTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiCtBankAccountBatTypeRepository repo = ((LoiCtBankAccountBatTypeRepository) getRepositories().getRepositoryFor(LoiCtBankAccountBatType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiCostMethod findLoiCostMethodByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiCostMethodRepository repo = ((LoiCostMethodRepository) getRepositories().getRepositoryFor(LoiCostMethod.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiCTransitionSide findLoiCTransitionSideByTransitionSideAndCompany(Long code, ManagedCompany company, boolean deleted) {
		LoiCTransitionSideRepository repo = ((LoiCTransitionSideRepository) getRepositories().getRepositoryFor(LoiCTransitionSide.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiCTransitionType findLoiCTransitionTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiCTransitionTypeRepository repo = ((LoiCTransitionTypeRepository) getRepositories().getRepositoryFor(LoiCTransitionType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiCtStornoTypeType findLoiCtStornoTypeTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiCtStornoTypeTypeRepository repo = ((LoiCtStornoTypeTypeRepository) getRepositories().getRepositoryFor(LoiCtStornoTypeType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiLinkedFlag findLoiLinkedFlagByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiLinkedFlagRepository repo = ((LoiLinkedFlagRepository) getRepositories().getRepositoryFor(LoiLinkedFlag.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiMaxLoadMWSeason findLoiMaxLoadMWSeasonByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiMaxLoadMWSeasonRepository repo = ((LoiMaxLoadMWSeasonRepository) getRepositories().getRepositoryFor(LoiMaxLoadMWSeason.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiMaxLoadWeather findLoiMaxLoadWeatherByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiMaxLoadWeatherRepository repo = ((LoiMaxLoadWeatherRepository) getRepositories().getRepositoryFor(LoiMaxLoadWeather.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiProtocolLineCount findLoiProtocolLineCountByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiProtocolLineCountRepository repo = ((LoiProtocolLineCountRepository) getRepositories().getRepositoryFor(LoiProtocolLineCount.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiProtocolStatus findLoiProtocolStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiProtocolStatusRepository repo = ((LoiProtocolStatusRepository) getRepositories().getRepositoryFor(LoiProtocolStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiProtocolCountPerMonth findLoiProtocolCountPerMonthByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiProtocolCountPerMonthRepository repo = ((LoiProtocolCountPerMonthRepository) getRepositories().getRepositoryFor(LoiProtocolCountPerMonth.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiPtBatchLinkStatus findLoiFPtBatchLinkStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPtBatchLinkStatusRepository repo = ((LoiPtBatchLinkStatusRepository) getRepositories().getRepositoryFor(LoiPtBatchLinkStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiExpenditureType findLoiExpenditureTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiExpenditureTypeRepository repo = ((LoiExpenditureTypeRepository) getRepositories().getRepositoryFor(LoiExpenditureType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiGoodsType findLoiGoodsTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiGoodsTypeRepository repo = ((LoiGoodsTypeRepository) getRepositories().getRepositoryFor(LoiGoodsType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiGrid findLoiGridByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiGridRepository repo = ((LoiGridRepository) getRepositories().getRepositoryFor(LoiGrid.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiInvDdsFileStatus findLoiInvDdsFileStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiInvDdsFileStatusRepository repo = ((LoiInvDdsFileStatusRepository) getRepositories().getRepositoryFor(LoiInvDdsFileStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiInvInvoicePaymentType findLoiInvInvoicePaymentTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiInvInvoicePaymentTypeRepository repo = ((LoiInvInvoicePaymentTypeRepository) getRepositories().getRepositoryFor(LoiInvInvoicePaymentType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiInvInvoiceStatus findLoiInvInvoiceStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiInvInvoiceStatusRepository repo = ((LoiInvInvoiceStatusRepository) getRepositories().getRepositoryFor(LoiInvInvoiceStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiJournalTypeCalculationType findLoiJournalTypeCalculationTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiJournalTypeCalculationTypeRepository repo = ((LoiJournalTypeCalculationTypeRepository) getRepositories().getRepositoryFor(LoiJournalTypeCalculationType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiLegalPersonType findLoiLegalPersonTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiLegalPersonTypeRepository repo = ((LoiLegalPersonTypeRepository) getRepositories().getRepositoryFor(LoiLegalPersonType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiLegalStatus findLoiLegalStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiLegalStatusRepository repo = ((LoiLegalStatusRepository) getRepositories().getRepositoryFor(LoiLegalStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiNotificationType findLoiNotificationTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiNotificationTypeRepository repo = ((LoiNotificationTypeRepository) getRepositories().getRepositoryFor(LoiNotificationType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiOfferStatus findLoiOfferStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiOfferStatusRepository repo = ((LoiOfferStatusRepository) getRepositories().getRepositoryFor(LoiOfferStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiOrderStatus findLoiOrderStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiOrderStatusRepository repo = ((LoiOrderStatusRepository) getRepositories().getRepositoryFor(LoiOrderStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiPartnerType findLoiPartnerTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPartnerTypeRepository repo = ((LoiPartnerTypeRepository) getRepositories().getRepositoryFor(LoiPartnerType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiPaymentType findLoiPaymentTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPaymentTypeRepository repo = ((LoiPaymentTypeRepository) getRepositories().getRepositoryFor(LoiPaymentType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiPtBatchCcDetailStatus findLoiPtBatchCcDetailStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPtBatchCcDetailStatusRepository repo = ((LoiPtBatchCcDetailStatusRepository) getRepositories().getRepositoryFor(LoiPtBatchCcDetailStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiPtClosingAccountPeriodMonth findLoiPtClosingAccountPeriodMonthByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPtClosingAccountPeriodMonthRepository repo = ((LoiPtClosingAccountPeriodMonthRepository) getRepositories().getRepositoryFor(LoiPtClosingAccountPeriodMonth.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiPtClosingAccountStatus findLoiPtClosingAccountStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPtClosingAccountStatusRepository repo = ((LoiPtClosingAccountStatusRepository) getRepositories().getRepositoryFor(LoiPtClosingAccountStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiPtJournalCcStatus findLoiPtJournalCcStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPtJournalCcStatusRepository repo = ((LoiPtJournalCcStatusRepository) getRepositories().getRepositoryFor(LoiPtJournalCcStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiPtJournalStatus findLoiPtJournalStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPtJournalStatusRepository repo = ((LoiPtJournalStatusRepository) getRepositories().getRepositoryFor(LoiPtJournalStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiPtPostingCcStatus findLoiPtPostingCcStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPtPostingCcStatusRepository repo = ((LoiPtPostingCcStatusRepository) getRepositories().getRepositoryFor(LoiPtPostingCcStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiPtPostingCoaStatus findLoiPtPostingCoaStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiPtPostingCoaStatusRepository repo = ((LoiPtPostingCoaStatusRepository) getRepositories().getRepositoryFor(LoiPtPostingCoaStatus.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiReasonForTermination findLoiReasonForTerminationByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiReasonForTerminationRepository repo = ((LoiReasonForTerminationRepository) getRepositories().getRepositoryFor(LoiReasonForTermination.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiRepresentativeType findLoiRepresentativeTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiRepresentativeTypeRepository repo = ((LoiRepresentativeTypeRepository) getRepositories().getRepositoryFor(LoiRepresentativeType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiServiceType findLoiServiceTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiServiceTypeRepository repo = ((LoiServiceTypeRepository) getRepositories().getRepositoryFor(LoiServiceType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiStatusCode findLoiStatusCodeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiStatusCodeRepository repo = ((LoiStatusCodeRepository) getRepositories().getRepositoryFor(LoiStatusCode.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiTransportType findLoiTransportTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiTransportTypeRepository repo = ((LoiTransportTypeRepository) getRepositories().getRepositoryFor(LoiTransportType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiTypeDoc findLoiTypeDocByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiTypeDocRepository repo = ((LoiTypeDocRepository) getRepositories().getRepositoryFor(LoiTypeDoc.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiTypeOfFinancialAccount findLoiTypeOfFinancialAccountByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiTypeOfFinancialAccountRepository repo = ((LoiTypeOfFinancialAccountRepository) getRepositories().getRepositoryFor(LoiTypeOfFinancialAccount.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiTypeOfPowerPlant findLoiTypeOfPowerPlantByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiTypeOfPowerPlantRepository repo = ((LoiTypeOfPowerPlantRepository) getRepositories().getRepositoryFor(LoiTypeOfPowerPlant.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public LoiTypeOFService findLoiTypeOFServiceByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiTypeOFServiceRepository repo = ((LoiTypeOFServiceRepository) getRepositories().getRepositoryFor(LoiTypeOFService.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiVatExemptionReason findLoiVatExemptionReasonByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiVatExemptionReasonRepository repo = ((LoiVatExemptionReasonRepository) getRepositories().getRepositoryFor(LoiVatExemptionReason.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public LoiVehicleType findLoiVehicleTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		LoiVehicleTypeRepository repo = ((LoiVehicleTypeRepository) getRepositories().getRepositoryFor(LoiVehicleType.class).get());
		return repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public MailAccount findMailAccountByDefaultAccountAndCompanyAndDeleted(Boolean defaultAccount, ManagedCompany company, boolean deleted) {
		MailAccountRepository repo = ((MailAccountRepository) getRepositories().getRepositoryFor(MailAccount.class).get());
		return repo.findFirstByDefaultAccountAndCompanyAndDeleted(defaultAccount, company, deleted);
	}

	public MailAccount findMailAccountByImapHostAndUsernameAndCompanyAndDeleted(String imapHost, String username, ManagedCompany company, boolean deleted) {
		MailAccountRepository repo = ((MailAccountRepository) getRepositories().getRepositoryFor(MailAccount.class).get());
		return repo.findFirstByImapHostAndUsernameAndCompanyAndDeleted(imapHost, username, company, deleted);
	}

	public MailTemplate findMailTemplateByTemplateCodeAndCompanyAndDeleted(Long templateCode, ManagedCompany company, boolean deleted) {
		MailTemplateRepository repo = ((MailTemplateRepository) getRepositories().getRepositoryFor(MailTemplate.class).get());
		return repo.findFirstByTemplateCodeAndCompanyAndDeleted(templateCode, company, deleted);
	}

	public ManagedCompany findManagedCompanyByCodeAndDeleted(Long code, boolean deleted) {
		ManagedCompanyRepository repo = ((ManagedCompanyRepository) getRepositories().getRepositoryFor(ManagedCompany.class).get());
		return repo.findFirstByCodeAndDeleted(code, deleted);
	}

	public Notification findNotificationByIsActiveAndIdentificationFirstAndIdentificationSecond(boolean isActive, String identificationFirst, String identificationSecond) {
		NotificationRepository repo = ((NotificationRepository) getRepositories().getRepositoryFor(Notification.class).get());
		return repo.findFirstByIsActiveAndIdentificationFirstAndIdentificationSecond(isActive, identificationFirst, identificationSecond);
	}
	
	public OfferLine findOfferLineByArticleAndOfferToClient(Article article, OfferToClient offerToClient) {
		OfferLineRepository repo = ((OfferLineRepository) getRepositories().getRepositoryFor(OfferLine.class).get());
		return repo.findFirstByArticleAndOfferToClient(article, offerToClient);
	}
	
	public List<OfferToClient> findOfferToClientByOfferCode(String offerCode) {
		OfferToClientRepository repo = ((OfferToClientRepository) getRepositories().getRepositoryFor(OfferToClient.class).get());
		return repo.findByOfferCode(offerCode);
	}
	
	public OfferToClient findOfferToClientByOfferCodeAndCompanyAndDeleted(String offerCode, ManagedCompany company, boolean deleted) {
		OfferToClientRepository repo = ((OfferToClientRepository) getRepositories().getRepositoryFor(OfferToClient.class).get());
		return repo.findFirstByOfferCodeAndCompanyAndDeleted(offerCode, company, deleted);
	}

	public Map<String, List<String>> generatePdfDocuments() {
		return pdfGenerationService.generatePdfDocuments();
	}
	
	public List<OfferToClient> findOfferToClientByForeignId(String foreignId) {
		OfferToClientRepository repo = ((OfferToClientRepository) getRepositories().getRepositoryFor(OfferToClient.class).get());
		return repo.findByForeignId(foreignId);
	}
	
	public List<OfferToClient> findOfferToClientByOriginalOffer(OfferToClient originalOffer) {
		OfferToClientRepository repo = ((OfferToClientRepository) getRepositories().getRepositoryFor(OfferToClient.class).get());
		return repo.findByOriginalOffer(originalOffer);
	}
	
	public IbexPrice findIbexPriceByLocalDateAndHourAndCompanyAndDeleted(LocalDate localDate, BigDecimal hour, ManagedCompany company, boolean deleted) {
		IbexPriceRepository repo = ((IbexPriceRepository) getRepositories().getRepositoryFor(IbexPrice.class).get());
		return repo.findFirstByLocalDateAndHourAndCompanyAndDeleted(localDate, hour, company, deleted);
	}

	public PowerPlant findPowerPlantByAccessPointAndCompanyAndDeleted(String accessPoint, ManagedCompany company, boolean deleted) {
		PowerPlantRepository repo = ((PowerPlantRepository) getRepositories().getRepositoryFor(PowerPlant.class).get());
		return repo.findFirstByAccessPointAndCompanyAndDeleted(accessPoint, company, deleted);
	}
	
	public SalesStage findSalesStageByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		SalesStageRepository repo = ((SalesStageRepository) getRepositories().getRepositoryFor(SalesStage.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}

	public PowerPlantMeterReading findPowerPlantMeterReadingByIdentificationAndStartTSAndCompanyAndDeleted(String identification, LocalDateTime startTS,  ManagedCompany company, boolean deleted) {
		PowerPlantMeterReadingRepository repo = ((PowerPlantMeterReadingRepository) getRepositories().getRepositoryFor(PowerPlantMeterReading.class).get());
		return repo.findFirstByIdentificationAndStartTSAndCompanyAndDeleted(identification, startTS, company, deleted);
	}

	public PowerPlantProducedSchedule findPowerPlantProducedScheduleByIdentificationAndStartTSAndCompanyAndDeleted(String identification, LocalDateTime startTS,  ManagedCompany company, boolean deleted) {
		PowerPlantProducedScheduleRepository repo = ((PowerPlantProducedScheduleRepository) getRepositories().getRepositoryFor(PowerPlantProducedSchedule.class).get());
		return repo.findFirstByIdentificationAndStartTSAndCompanyAndDeleted(identification, startTS, company, deleted);
	}

	public Schedule findScheduleByMessageIdentificationAndMessageVersionAndCompanyAndDeleted(String messageIdentification, BigDecimal messageVersion, ManagedCompany company, boolean deleted) {
		ScheduleRepository repo = ((ScheduleRepository) getRepositories().getRepositoryFor(Schedule.class).get());
		return repo.findFirstByMessageIdentificationAndMessageVersionAndCompanyAndDeleted(messageIdentification, messageVersion, company, deleted);
	}
	
	public SecPermission findSecPermissionByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		SecPermissionRepository repo = ((SecPermissionRepository) getRepositories().getRepositoryFor(SecPermission.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public SecRole findSecRoleByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		SecRoleRepository repo = ((SecRoleRepository) getRepositories().getRepositoryFor(SecRole.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public SecRolePermission findSecRolePermissionByRoleAndPermissionAndDeleted(SecRole role, SecPermission permission, boolean deleted) {
		SecRolePermissionRepository repo = ((SecRolePermissionRepository) getRepositories().getRepositoryFor(SecRolePermission.class).get());
		return repo.findFirstByRoleAndPermissionAndDeleted(role, permission, deleted);
	}
	
	public SecUser findSecUserByFullNameAndCompany(String fullName, ManagedCompany company) {
		SecUserRepository repo = ((SecUserRepository) getRepositories().getRepositoryFor(SecUser.class).get());
		return repo.findFirstByFullNameAndCompany(fullName, company);
	}
	
	public SecUser findSecUserByName(String name) { //this is unique in all companies
		SecUserRepository repo = ((SecUserRepository) getRepositories().getRepositoryFor(SecUser.class).get());
		return repo.findFirstByName(name);
	}
	
	public List<SendMailMessage> findSendMailMessageByFromAccount(MailAccount fromAccount) {
		SendMailMessageRepository repo = ((SendMailMessageRepository) getRepositories().getRepositoryFor(SendMailMessage.class).get());
		return repo.findByFromAccount(fromAccount);
	}

	public SelfInvoicingLine findSelfInvoicingLineBySelfInvoicingLineIdCodeAndCompanyAndDeleted(String selfInvoicingLineIdCode, ManagedCompany company, boolean deleted) {
		SelfInvoicingLineRepository repo = ((SelfInvoicingLineRepository) getRepositories().getRepositoryFor(SelfInvoicingLine.class).get());
		return repo.findFirstBySelfInvoicingLineIdCodeAndCompanyAndDeleted(selfInvoicingLineIdCode, company, deleted);
	}
	
	public List<SendMailMessage> findSendMailMessageByNameAndCompany(String name, ManagedCompany company) {
		SendMailMessageRepository repo = ((SendMailMessageRepository) getRepositories().getRepositoryFor(SendMailMessage.class).get());
		return repo.findByNameAndCompany(name, company);
	}
	
	public TaskStatus findTaskStatusByCodeAndCompany(Long code, ManagedCompany company) {
		TaskStatusRepository repo = ((TaskStatusRepository) getRepositories().getRepositoryFor(TaskStatus.class).get());
		return repo.findFirstByCodeAndCompany(code, company);
	}
	
	public List<TaskWatcher> findTaskWatcherByTask(Task task) {
		TaskWatcherRepository repo = ((TaskWatcherRepository) getRepositories().getRepositoryFor(TaskWatcher.class).get());
		return repo.findByTask(task);
	}
	
	public List<TaskWatcher> findTaskWatcherByTaskAndWatcher(Task task, SecUser watcher) {
		TaskWatcherRepository repo = ((TaskWatcherRepository) getRepositories().getRepositoryFor(TaskWatcher.class).get());
		return repo.findByTaskAndWatcher(task, watcher);
	}
	
	public SecUser findSecUserByNameAndDeleted(String name, boolean deleted) {
		SecUserRepository repo = ((SecUserRepository) getRepositories().getRepositoryFor(SecUser.class).get());
		return repo.findFirstByNameAndDeleted(name, deleted);
	}
	
	public SecUserRole findSecUserRoleByUserAndRoleAndDeleted(SecUser user, SecRole role, boolean deleted) {
		SecUserRoleRepository repo = ((SecUserRoleRepository) getRepositories().getRepositoryFor(SecUserRole.class).get());
		return repo.findFirstByUserAndRoleAndDeleted(user, role, deleted);
	}
	
	public ShippingContainerType findShippingContainerTypeByCodeAndCompanyAndDeleted(String code, ManagedCompany company, boolean deleted) {
		ShippingContainerTypeRepository repo = ((ShippingContainerTypeRepository) getRepositories().getRepositoryFor(ShippingContainerType.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public TaskPriority findTaskPriorityByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		TaskPriorityRepository repo = ((TaskPriorityRepository) getRepositories().getRepositoryFor(TaskPriority.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public TaskRelationType findTaskRelationTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		TaskRelationTypeRepository repo = ((TaskRelationTypeRepository) getRepositories().getRepositoryFor(TaskRelationType.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public TaskStatus findTaskStatusByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		TaskStatusRepository repo = ((TaskStatusRepository) getRepositories().getRepositoryFor(TaskStatus.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
	public TaskType findTaskTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
		TaskTypeRepository repo = ((TaskTypeRepository) getRepositories().getRepositoryFor(TaskType.class).get());
		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
	}
	
//	public TimeSheetItemType findTimeSheetItemTypeByCodeAndCompanyAndDeleted(Long code, ManagedCompany company, boolean deleted) {
//		TimeSheetItemTypeRepository repo = ((TimeSheetItemTypeRepository) getRepositories().getRepositoryFor(TimeSheetItemType.class).get());
//		return repo.findFirstByCodeAndCompanyAndDeleted(code, company, deleted);
//	}
	
	public List<VendorInvoiceRow> findVendorInvoiceRowByGoodsAndCompanyAndDeleted(CGoods goods, ManagedCompany company, boolean deleted) {
		VendorInvoiceRowRepository repo = ((VendorInvoiceRowRepository) getRepositories().getRepositoryFor(VendorInvoiceRow.class).get());
		return repo.findByGoodsAndCompanyAndDeleted(goods, company, deleted);
	}
	
	//TODO Reimplement this as parameterized query building function
	@org.springframework.transaction.annotation.Transactional(readOnly = true,propagation = Propagation.REQUIRES_NEW) //optimize to be read-only and avoid flushing
	public CommonRecord checkForUniqueCommonRecord(CommonRecord entity, boolean deleted) {
		logger.trace("checkForUniqueCommonRecord for : " + entity + ", deleted: " + deleted);

		if(entity instanceof AllocationType) return findAllocationTypeByNameAndCompanyAndDeleted(((AllocationType) entity).getName(), ((AllocationType) entity).getCompany(), deleted);
		if(entity instanceof AreaCategory) return findAreaCategoryByCodeAndCompanyAndDeleted(((AreaCategory) entity).getCode(), ((AreaCategory) entity).getCompany(), deleted);
		if(entity instanceof Article) return findArticleByNameAndCompanyAndDeleted(((Article) entity).getName(), ((Article) entity).getCompany(), deleted);
		if(entity instanceof BankAccount) return findBankAccountByIbanAndCompanyAndDeleted(((BankAccount) entity).getIban(), ((BankAccount) entity).getCompany(), deleted);
		if(entity instanceof BusinessCategory) return findBusinessCategoryByCodeAndCompanyAndDeleted(((BusinessCategory) entity).getCode(), ((BusinessCategory) entity).getCompany(), deleted);
		if(entity instanceof CommentTemplate) return findCommentTemplateByCodeAndCompanyAndDeleted(((CommentTemplate) entity).getCode(), ((CommentTemplate) entity).getCompany(), deleted);
		if(entity instanceof ContactType) return findContactTypeByCodeAndCompanyAndDeleted(((ContactType) entity).getCode(), ((ContactType) entity).getCompany(), deleted);
		if(entity instanceof DirectionCategory) return findDirectionCategoryByCodeAndCompanyAndDeleted(((DirectionCategory) entity).getCode(), ((DirectionCategory) entity).getCompany(), deleted);
		if(entity instanceof GeneralCategory) return findGeneralCategoryByCodeAndCompanyAndDeleted(((GeneralCategory) entity).getCode(), ((GeneralCategory) entity).getCompany(), deleted);
		if(entity instanceof Income) return findIncomeByNameAndCompanyAndDeleted(((Income) entity).getName(), ((Income) entity).getCompany(), deleted);
		if(entity instanceof JobPosition) return findJobPositionByCodeAndCompanyAndDeleted(((JobPosition) entity).getCode(), ((JobPosition) entity).getCompany(), deleted);
		if(entity instanceof JobRequirement) return findJobRequirementByCodeAndCompanyAndDeleted(((JobRequirement) entity).getCode(), ((JobRequirement) entity).getCompany(), deleted);
		if(entity instanceof LegalStatus) return findLegalStatusByCodeAndCompanyAndDeleted(((LegalStatus) entity).getCode(), ((LegalStatus) entity).getCompany(), deleted);
		if(entity instanceof LegalPerson) return findLegalPersonByNameAndCompanyAndDeleted(((LegalPerson) entity).getName(), ((LegalPerson) entity).getCompany(), deleted);
		if(entity instanceof LegalPerson) return findLegalPersonByEikAndCompanyAndDeleted(((LegalPerson) entity).getEik(), ((LegalPerson) entity).getCompany(), deleted);
		if(entity instanceof LoiAccountActivityType) return findLoiAccountActivityTypeByCodeAndCompanyAndDeleted(((LoiAccountActivityType) entity).getListOptionItemCode(), ((LoiAccountActivityType) entity).getCompany(), deleted);
		if(entity instanceof LoiAgreementStatus) return findLoiAgreementStatusByCodeAndCompanyAndDeleted(((LoiAgreementStatus) entity).getListOptionItemCode(), ((LoiAgreementStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiDocumentType) return findLoiDocumentTypeByCodeAndCompanyAndDeleted(((LoiDocumentType) entity).getListOptionItemCode(), ((LoiDocumentType) entity).getCompany(), deleted);
		if(entity instanceof LoiMeasurementUnit) return findLoiMeasurementUnitByCodeAndCompanyAndDeleted(((LoiMeasurementUnit) entity).getListOptionItemCode(), ((LoiMeasurementUnit) entity).getCompany(), deleted);
		if(entity instanceof LoiBatchCalculationType) return findLoiBatchCalculationTypeByCodeAndCompanyAndDeleted(((LoiBatchCalculationType) entity).getListOptionItemCode(), ((LoiBatchCalculationType) entity).getCompany(), deleted);
		if(entity instanceof LoiBatchJteDefaultSide) return findLoiBatchJteDefaultSideByCodeAndCompanyAndDeleted(((LoiBatchJteDefaultSide) entity).getListOptionItemCode(), ((LoiBatchJteDefaultSide) entity).getCompany(), deleted);
		if(entity instanceof LoiBatchTteLinkStatus) return findLoiBatchTteLinkStatusByCodeAndCompanyAndDeleted(((LoiBatchTteLinkStatus) entity).getListOptionItemCode(), ((LoiBatchTteLinkStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiBatchTypeRuleAmountType) return findLoiBatchTypeRuleAmountTypeByCodeAndCompanyAndDeleted(((LoiBatchTypeRuleAmountType) entity).getListOptionItemCode(), ((LoiBatchTypeRuleAmountType) entity).getCompany(), deleted);
		if(entity instanceof LoiBatchTypeRuleDependenceType) return findLoiBatchTypeRuleDependenceTypeByListOptionItemCodeAndCompanyAndDeleted(((LoiBatchTypeRuleDependenceType) entity).getListOptionItemCode(), ((LoiBatchTypeRuleDependenceType) entity).getCompany(), deleted);
		if(entity instanceof LoiBreTransitionResult) return findLoiBreTransitionResultByListOptionItemCodeAndCompanyAndDeleted(((LoiBreTransitionResult) entity).getListOptionItemCode(), ((LoiBreTransitionResult) entity).getCompany(), deleted);
		if(entity instanceof LoiContractFee) return findLoiContractFeeByListOptionItemCodeAndCompanyAndDeleted(((LoiContractFee) entity).getListOptionItemCode(), ((LoiContractFee) entity).getCompany(), deleted);
		if(entity instanceof LoiContractPrice) return findLoiContractPriceByListOptionItemCodeAndCompanyAndDeleted(((LoiContractPrice) entity).getListOptionItemCode(), ((LoiContractPrice) entity).getCompany(), deleted);
		if(entity instanceof LoiContractQuantity) return findLoiContractQuantityByListOptionItemCodeAndCompanyAndDeleted(((LoiContractQuantity) entity).getListOptionItemCode(), ((LoiContractQuantity) entity).getCompany(), deleted);
		if(entity instanceof LoiContractStatus) return findLoiContractStatusByListOptionItemCodeAndCompanyAndDeleted(((LoiContractStatus) entity).getListOptionItemCode(), ((LoiContractStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiCtBankAccountBatType) return findLoiCtBankAccountBatTypeByCodeAndCompanyAndDeleted(((LoiCtBankAccountBatType) entity).getListOptionItemCode(), ((LoiCtBankAccountBatType) entity).getCompany(), deleted);
		if(entity instanceof LoiCostMethod) return findLoiCostMethodByCodeAndCompanyAndDeleted(((LoiCostMethod) entity).getListOptionItemCode(), ((LoiCostMethod) entity).getCompany(), deleted);
		if(entity instanceof LoiCTransitionSide) return findLoiCTransitionSideByTransitionSideAndCompany(((LoiCTransitionSide) entity).getListOptionItemCode(), ((LoiCTransitionSide) entity).getCompany(), deleted);
		if(entity instanceof LoiCTransitionType) return findLoiCTransitionTypeByCodeAndCompanyAndDeleted(((LoiCTransitionType) entity).getListOptionItemCode(), ((LoiCTransitionType) entity).getCompany(), deleted);
		if(entity instanceof LoiCtStornoTypeType) return findLoiCtStornoTypeTypeByCodeAndCompanyAndDeleted(((LoiCtStornoTypeType) entity).getListOptionItemCode(), ((LoiCtStornoTypeType) entity).getCompany(), deleted);
		if(entity instanceof LoiLinkedFlag) return findLoiLinkedFlagByCodeAndCompanyAndDeleted(((LoiLinkedFlag) entity).getListOptionItemCode(), ((LoiLinkedFlag) entity).getCompany(), deleted);
		if(entity instanceof LoiMaxLoadMWSeason) return findLoiMaxLoadMWSeasonByCodeAndCompanyAndDeleted(((LoiMaxLoadMWSeason) entity).getListOptionItemCode(), ((LoiMaxLoadMWSeason) entity).getCompany(), deleted);
		if(entity instanceof LoiMaxLoadWeather) return findLoiMaxLoadWeatherByCodeAndCompanyAndDeleted(((LoiMaxLoadWeather) entity).getListOptionItemCode(), ((LoiMaxLoadWeather) entity).getCompany(), deleted);
		if(entity instanceof LoiProtocolLineCount) return findLoiProtocolLineCountByCodeAndCompanyAndDeleted(((LoiProtocolLineCount) entity).getListOptionItemCode(), ((LoiProtocolLineCount) entity).getCompany(), deleted);
		if(entity instanceof LoiProtocolStatus) return findLoiProtocolStatusByCodeAndCompanyAndDeleted(((LoiProtocolStatus) entity).getListOptionItemCode(), ((LoiProtocolStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiProtocolCountPerMonth) return findLoiProtocolCountPerMonthByCodeAndCompanyAndDeleted(((LoiProtocolCountPerMonth) entity).getListOptionItemCode(), ((LoiProtocolCountPerMonth) entity).getCompany(), deleted);
		if(entity instanceof LoiPtBatchLinkStatus) return findLoiFPtBatchLinkStatusByCodeAndCompanyAndDeleted(((LoiPtBatchLinkStatus) entity).getListOptionItemCode(), ((LoiPtBatchLinkStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiExpenditureType) return findLoiExpenditureTypeByCodeAndCompanyAndDeleted(((LoiExpenditureType) entity).getListOptionItemCode(), ((LoiExpenditureType) entity).getCompany(), deleted);
		if(entity instanceof LoiGoodsType) return findLoiGoodsTypeByCodeAndCompanyAndDeleted(((LoiGoodsType) entity).getListOptionItemCode(), ((LoiGoodsType) entity).getCompany(), deleted);
		if(entity instanceof LoiGrid) return findLoiGridByCodeAndCompanyAndDeleted(((LoiGrid) entity).getListOptionItemCode(), ((LoiGrid) entity).getCompany(), deleted);
		if(entity instanceof LoiInvDdsFileStatus) return findLoiInvDdsFileStatusByCodeAndCompanyAndDeleted(((LoiInvDdsFileStatus) entity).getListOptionItemCode(), ((LoiInvDdsFileStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiInvInvoicePaymentType) return findLoiInvInvoicePaymentTypeByCodeAndCompanyAndDeleted(((LoiInvInvoicePaymentType) entity).getListOptionItemCode(), ((LoiInvInvoicePaymentType) entity).getCompany(), deleted);
		if(entity instanceof LoiInvInvoiceStatus) return findLoiInvInvoiceStatusByCodeAndCompanyAndDeleted(((LoiInvInvoiceStatus) entity).getListOptionItemCode(), ((LoiInvInvoiceStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiJournalTypeCalculationType) return findLoiJournalTypeCalculationTypeByCodeAndCompanyAndDeleted(((LoiJournalTypeCalculationType) entity).getListOptionItemCode(), ((LoiJournalTypeCalculationType) entity).getCompany(), deleted);
		if(entity instanceof LoiLegalPersonType) return findLoiLegalPersonTypeByCodeAndCompanyAndDeleted(((LoiLegalPersonType) entity).getListOptionItemCode(), ((LoiLegalPersonType) entity).getCompany(), deleted);
		if(entity instanceof LoiLegalStatus) return findLoiLegalStatusByCodeAndCompanyAndDeleted(((LoiLegalStatus) entity).getListOptionItemCode(), ((LoiLegalStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiNotificationType) return findLoiNotificationTypeByCodeAndCompanyAndDeleted(((LoiNotificationType) entity).getListOptionItemCode(), ((LoiNotificationType) entity).getCompany(), deleted);
		if(entity instanceof LoiOfferStatus) return findLoiOfferStatusByCodeAndCompanyAndDeleted(((LoiOfferStatus) entity).getListOptionItemCode(), ((LoiOfferStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiOrderStatus) return findLoiOrderStatusByCodeAndCompanyAndDeleted(((LoiOrderStatus) entity).getListOptionItemCode(), ((LoiOrderStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiPartnerType) return findLoiPartnerTypeByCodeAndCompanyAndDeleted(((LoiPartnerType) entity).getListOptionItemCode(), ((LoiPartnerType) entity).getCompany(), deleted);
		if(entity instanceof LoiPaymentType) return findLoiPaymentTypeByCodeAndCompanyAndDeleted(((LoiPaymentType) entity).getListOptionItemCode(), ((LoiPaymentType) entity).getCompany(), deleted);
		if(entity instanceof LoiPtBatchCcDetailStatus) return findLoiPtBatchCcDetailStatusByCodeAndCompanyAndDeleted(((LoiPtBatchCcDetailStatus) entity).getListOptionItemCode(), ((LoiPtBatchCcDetailStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiPtClosingAccountPeriodMonth) return findLoiPtClosingAccountPeriodMonthByCodeAndCompanyAndDeleted(((LoiPtClosingAccountPeriodMonth) entity).getListOptionItemCode(), ((LoiPtClosingAccountPeriodMonth) entity).getCompany(), deleted);
		if(entity instanceof LoiPtClosingAccountStatus) return findLoiPtClosingAccountStatusByCodeAndCompanyAndDeleted(((LoiPtClosingAccountStatus) entity).getListOptionItemCode(), ((LoiPtClosingAccountStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiPtJournalCcStatus) return findLoiPtJournalCcStatusByCodeAndCompanyAndDeleted(((LoiPtJournalCcStatus) entity).getListOptionItemCode(), ((LoiPtJournalCcStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiPtJournalStatus) return findLoiPtJournalStatusByCodeAndCompanyAndDeleted(((LoiPtJournalStatus) entity).getListOptionItemCode(), ((LoiPtJournalStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiPtPostingCcStatus) return findLoiPtPostingCcStatusByCodeAndCompanyAndDeleted(((LoiPtPostingCcStatus) entity).getListOptionItemCode(), ((LoiPtPostingCcStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiPtPostingCoaStatus) return findLoiPtPostingCoaStatusByCodeAndCompanyAndDeleted(((LoiPtPostingCoaStatus) entity).getListOptionItemCode(), ((LoiPtPostingCoaStatus) entity).getCompany(), deleted);
		if(entity instanceof LoiReasonForTermination) return findLoiReasonForTerminationByCodeAndCompanyAndDeleted(((LoiReasonForTermination) entity).getListOptionItemCode(), ((LoiReasonForTermination) entity).getCompany(), deleted);
		if(entity instanceof LoiRepresentativeType) return findLoiRepresentativeTypeByCodeAndCompanyAndDeleted(((LoiRepresentativeType) entity).getListOptionItemCode(), ((LoiRepresentativeType) entity).getCompany(), deleted);
		if(entity instanceof LoiServiceType) return findLoiServiceTypeByCodeAndCompanyAndDeleted(((LoiServiceType) entity).getListOptionItemCode(), ((LoiServiceType) entity).getCompany(), deleted);
		if(entity instanceof LoiStatusCode) return findLoiStatusCodeByCodeAndCompanyAndDeleted(((LoiStatusCode) entity).getListOptionItemCode(), ((LoiStatusCode) entity).getCompany(), deleted);
		if(entity instanceof LoiTransportType) return findLoiTransportTypeByCodeAndCompanyAndDeleted(((LoiTransportType) entity).getListOptionItemCode(), ((LoiTransportType) entity).getCompany(), deleted);
		if(entity instanceof LoiTypeDoc) return findLoiTypeDocByCodeAndCompanyAndDeleted(((LoiTypeDoc) entity).getListOptionItemCode(), ((LoiTypeDoc) entity).getCompany(), deleted);
		if(entity instanceof LoiTypeOfFinancialAccount) return findLoiTypeOfFinancialAccountByCodeAndCompanyAndDeleted(((LoiTypeOfFinancialAccount) entity).getListOptionItemCode(), ((LoiTypeOfFinancialAccount) entity).getCompany(), deleted);
		if(entity instanceof LoiTypeOfPowerPlant) return findLoiTypeOfPowerPlantByCodeAndCompanyAndDeleted(((LoiTypeOfPowerPlant) entity).getListOptionItemCode(), ((LoiTypeOfPowerPlant) entity).getCompany(), deleted);
		if(entity instanceof LoiTypeOFService) return findLoiTypeOFServiceByCodeAndCompanyAndDeleted(((LoiTypeOFService) entity).getListOptionItemCode(), ((LoiTypeOFService) entity).getCompany(), deleted);
		if(entity instanceof LoiVatExemptionReason) return findLoiVatExemptionReasonByCodeAndCompanyAndDeleted(((LoiVatExemptionReason) entity).getListOptionItemCode(), ((LoiVatExemptionReason) entity).getCompany(), deleted);
		if(entity instanceof LoiVehicleType) return findLoiVehicleTypeByCodeAndCompanyAndDeleted(((LoiVehicleType) entity).getListOptionItemCode(), ((LoiVehicleType) entity).getCompany(), deleted);
		//ancestors must be after descendants
		if(entity instanceof ListOptionItem) return findListOptionItemByCodeAndCompanyAndDeleted(((ListOptionItem) entity).getListOptionItemCode(), ((ListOptionItem) entity).getCompany(), deleted);
		if(entity instanceof MailAccount) return findMailAccountByImapHostAndUsernameAndCompanyAndDeleted(((MailAccount) entity).getImapHost(), ((MailAccount) entity).getUsername(), ((MailAccount) entity).getCompany(), false);
		if(entity instanceof MailTemplate) return findMailTemplateByTemplateCodeAndCompanyAndDeleted(((MailTemplate) entity).getTemplateCode(), ((MailTemplate) entity).getCompany(), false);
		if(entity instanceof ManagedCompany) return findManagedCompanyByCodeAndDeleted(((ManagedCompany) entity).getCode(), deleted);
		if(entity instanceof OfferToClient) return findOfferToClientByOfferCodeAndCompanyAndDeleted(((OfferToClient) entity).getOfferCode(), ((OfferToClient) entity).getCompany(), deleted);
		if(entity instanceof SalesStage) return findSalesStageByCodeAndCompanyAndDeleted(((SalesStage) entity).getCode(), ((SalesStage) entity).getCompany(), deleted);
		if(entity instanceof SecPermission) return findSecPermissionByCodeAndCompanyAndDeleted(((SecPermission) entity).getCode(), ((SecPermission) entity).getCompany(), deleted);
		if(entity instanceof SecRole) return findSecRoleByCodeAndCompanyAndDeleted(((SecRole) entity).getCode(), ((SecRole) entity).getCompany(), deleted);
		if(entity instanceof SecRolePermission) return findSecRolePermissionByRoleAndPermissionAndDeleted(((SecRolePermission) entity).getRole(), ((SecRolePermission) entity).getPermission(), deleted);
		if(entity instanceof SecUser) return findSecUserByNameAndDeleted(((SecUser) entity).getName(), deleted);
		if(entity instanceof SecUserRole) return findSecUserRoleByUserAndRoleAndDeleted(((SecUserRole) entity).getUser(), ((SecUserRole) entity).getRole(), deleted);
		if(entity instanceof ShippingContainerType) return findShippingContainerTypeByCodeAndCompanyAndDeleted(((ShippingContainerType) entity).getCode(), ((ShippingContainerType) entity).getCompany(), deleted);
		if(entity instanceof TaskPriority) return findTaskPriorityByCodeAndCompanyAndDeleted(((TaskPriority) entity).getCode(), ((TaskPriority) entity).getCompany(), deleted);
		if(entity instanceof TaskRelationType) return findTaskRelationTypeByCodeAndCompanyAndDeleted(((TaskRelationType) entity).getCode(), ((TaskRelationType) entity).getCompany(), deleted);
		if(entity instanceof TaskStatus) return findTaskStatusByCodeAndCompanyAndDeleted(((TaskStatus) entity).getCode(), ((TaskStatus) entity).getCompany(), deleted);
		if(entity instanceof TaskType) return findTaskTypeByCodeAndCompanyAndDeleted(((TaskType) entity).getCode(), ((TaskType) entity).getCompany(), deleted);
		//if(entity instanceof LoiCTransitionType) return findLoiCTransitionTypeByTransitionTypeAndCompany(((LoiCTransitionType) entity).getCode(), ((LoiCTransitionType) entity).getCompany());
		//if(entity instanceof TimeSheetItemType) return findTimeSheetItemTypeByCodeAndCompanyAndDeleted(((TimeSheetItemType) entity).getCode(), ((TimeSheetItemType) entity).getCompany(), deleted);
		
		//santa.common unique checks
		if(entity instanceof CCcGoodsType) return findCCcGoodsTypeByCodeAndOutCodeAndCompanyAndDeleted(((CCcGoodsType) entity).getCode(), ((CCcGoodsType) entity).getOutCode(), ((CCcGoodsType) entity).getCompany(), deleted);
		if(entity instanceof CCcOrganizationUnit) return findCCcOrganizationUnitByCodeAndCompanyAndDeleted(((CCcOrganizationUnit) entity).getCode(), ((CCcOrganizationUnit) entity).getCompany(), deleted);
		if(entity instanceof CCcPartner) return findCCcPartnerByCodeAndOutCodeAndCompanyAndDeleted(((CCcPartner) entity).getCode(), ((CCcPartner) entity).getOutCode(), ((CCcPartner) entity).getCompany(), deleted);
		if(entity instanceof CCfgDocumentPattern) return findCCfgDocumentPatternByDocumentCodeAndOutCodeAndCompanyAndDeleted(((CCfgDocumentPattern) entity).getDocumentCode(), ((CCfgDocumentPattern) entity).getOutCode(), ((CCfgDocumentPattern) entity).getCompany(), deleted);
		if(entity instanceof CCtCurrency) return findCCtCurrencyByCodeAndCompanyAndDeleted(((CCtCurrency) entity).getCode(), ((CCtCurrency) entity).getCompany(), deleted);
		if(entity instanceof FCtInvDealType) return findFCtInvDealTypeByCodeAndCompanyAndDeleted(((FCtInvDealType) entity).getCode(), ((FCtInvDealType) entity).getCompany(), deleted);
		if(entity instanceof CCtPartnerGroup) return findCCtPartnerGroupByCodeAndOutCodeAndCompanyAndDeleted(((CCtPartnerGroup) entity).getCode(), ((CCtPartnerGroup) entity).getOutCode(), ((CCtPartnerGroup) entity).getCompany(), deleted);
		if(entity instanceof FCtTransitionType) return findFCtTransitionTypeByCodeAndCompanyAndDeleted(((FCtTransitionType) entity).getCode(), ((FCtTransitionType) entity).getCompany(), deleted);
		if(entity instanceof CDeliveryGoodMap) {
			CDeliveryGoodMap existingEntity = findCDeliveryGoodMapByDeyCodeAndParIdAndCompanyAndDeleted(((CDeliveryGoodMap) entity).getDeyCode(), ((CDeliveryGoodMap) entity).getParId(), ((CDeliveryGoodMap) entity).getCompany(), deleted);
			if(existingEntity == null) {
				existingEntity = findCDeliveryGoodMapByParIdAndGodIdAndCompanyAndDeleted(((CDeliveryGoodMap) entity).getParId(), ((CDeliveryGoodMap) entity).getGodId(), ((CDeliveryGoodMap) entity).getCompany(), deleted);
			}
			return existingEntity;
		}
		if(entity instanceof CGoodMark) return findCGoodMarkByMarkCodeAndOutCodeAndCompanyAndDeleted(((CGoodMark) entity).getMarkCode(), ((CGoodMark) entity).getOutCode(), ((CGoodMark) entity).getCompany(), deleted);
		if(entity instanceof CMeasure) return findCMeasureByCodeAndCompanyAndDeleted(((CMeasure) entity).getCode(), ((CMeasure) entity).getCompany(), deleted);
		if(entity instanceof COffer) return findCOfferByNumberOfrAndCompanyAndDeleted(((COffer) entity).getNumberOfr(), ((COffer) entity).getCompany(), deleted);
		if(entity instanceof COrderDetail) return findCOrderDetailByOrrIdAndGodIdAndCompanyAndDeleted(((COrderDetail) entity).getOrrId(), ((COrderDetail) entity).getGodId(), ((COrderDetail) entity).getCompany(), deleted);
		if(entity instanceof CService) return findCServiceByCodeAndOutCodeAndCompanyAndDeleted(((CService) entity).getCode(), ((CService) entity).getOutCode(), ((CService) entity).getCompany(), deleted);

		// Accounting entities
		if(entity instanceof FCcContract) return findFCcContractByOutCodeAndCodeAndCompanyAndDeleted(((FCcContract) entity).getOutCode(), ((FCcContract) entity).getCode(), ((FCcContract) entity).getCompany(), deleted);
		if(entity instanceof FCcEbk) return findFCcEbkByOutCodeAndCodeAndCompanyAndDeleted(((FCcEbk) entity).getOutCode(), ((FCcEbk) entity).getCode(), ((FCcEbk) entity).getCompany(), deleted);
		if(entity instanceof FCcFinsource) return findFCcFinsourceByOutCodeAndCodeAndCompanyAndDeleted(((FCcFinsource) entity).getOutCode(), ((FCcFinsource) entity).getCode(), ((FCcFinsource) entity).getCompany(), deleted);
		if(entity instanceof FCcFunction) return findFCcFunctionByOutCodeAndCodeAndCompanyAndDeleted(((FCcFunction) entity).getOutCode(), ((FCcFunction) entity).getCode(), ((FCcFunction) entity).getCompany(), deleted);
		if(entity instanceof FCcProgram) return findFCcProgramByOutCodeAndCodeAndCompanyAndDeleted(((FCcProgram) entity).getOutCode(), ((FCcProgram) entity).getCode(), ((FCcProgram) entity).getCompany(), deleted);
		if(entity instanceof FCcReserve1) return findFCcReserve1ByOutCodeAndCodeAndCompanyAndDeleted(((FCcReserve1) entity).getOutCode(), ((FCcReserve1) entity).getCode(), ((FCcReserve1) entity).getCompany(), deleted);
		if(entity instanceof FCcReserve2) return findFCcReserve2ByOutCodeAndCodeAndCompanyAndDeleted(((FCcReserve2) entity).getOutCode(), ((FCcReserve2) entity).getCode(), ((FCcReserve2) entity).getCompany(), deleted);
		if(entity instanceof FChartAccount) return findFChartAccountByOutCodeAndCodeAndCompanyAndDeleted(((FChartAccount) entity).getOutCode(), ((FChartAccount) entity).getCode(), ((FChartAccount) entity).getCompany(), deleted);
		if(entity instanceof FCtBatchJteDefault) return findFCtBatchJteDefaultByOutCodeAndBteIdAndJteIdAndCompanyAndDeleted(((FCtBatchJteDefault) entity).getOutCode(),((FCtBatchJteDefault) entity).getBteId(), ((FCtBatchJteDefault) entity).getJteId(), ((FCtBatchJteDefault) entity).getCompany(), deleted);
		if(entity instanceof FCtBatchTteLink) return findFCtBatchTteLinkByOutCodeAndTteIdAndCompanyAndDeleted(((FCtBatchTteLink) entity).getOutCode(),((FCtBatchTteLink) entity).getBteId(), ((FCtBatchTteLink) entity).getTteId(), ((FCtBatchTteLink) entity).getIdeId(), ((FCtBatchTteLink) entity).getCompany(), deleted);
		if(entity instanceof FCtBatchTteType) return findFCtBatchTteTypeByOutCodeAndTteIdAndCompanyAndDeleted(((FCtBatchTteType) entity).getOutCode(), ((FCtBatchTteType) entity).getTteId(), ((FCtBatchTteType) entity).getCompany(), deleted);
		if(entity instanceof FCtBatchType) return findFCtBatchTypeByCodeAndCompanyAndDeleted(((FCtBatchType) entity).getCode(), ((FCtBatchType) entity).getCompany(), deleted);
		if(entity instanceof FCtBatchTypeRule) return findFCtBatchTypeRuleByOutCodeAndAmountTypeAndTteIdAndRueIdAndJteIdAndCoaIdCtAndCoaIdDtAndActiveFromDateLessThanEqualAndActiveToDateGreaterThanEqualAndCompanyAndDeleted(
				((FCtBatchTypeRule) entity).getOutCode(), ((FCtBatchTypeRule) entity).getAmountType(), ((FCtBatchTypeRule) entity).getTteId(), 
				((FCtBatchTypeRule) entity).getRueId(), ((FCtBatchTypeRule) entity).getJteId(), ((FCtBatchTypeRule) entity).getCoaIdCt(), 
				((FCtBatchTypeRule) entity).getCoaIdDt(), ((FCtBatchTypeRule) entity).getActiveToDate(), ((FCtBatchTypeRule) entity).getActiveFromDate(), 
				((FCtBatchTypeRule) entity).getCompany(), deleted);
		if(entity instanceof FCtGood) return findFCtGoodByCodeAndOutCodeAndGteIdAndCompanyAndDeleted(((FCtGood) entity).getCode(),((FCtGood) entity).getOutCode(), ((FCtGood) entity).getGteId(), ((FCtGood) entity).getCompany(), deleted);
		if(entity instanceof FCtRepresentative) return findFCtRepresentativeByOutCodeAndIdeNoAndCompanyAndDeleted(((FCtRepresentative) entity).getOutCode(), ((FCtRepresentative) entity).getIdeNo(), ((FCtRepresentative) entity).getCompany(), deleted);
		if(entity instanceof FCtRule) return findFCtRuleByCodeAndCompanyAndDeleted(((FCtRule) entity).getCode(), ((FCtRule) entity).getCompany(), deleted);
		if(entity instanceof FCtStornoType) return findFCtStornoTypeByOutCodeAndTypeAndCompanyAndDeleted(((FCtStornoType) entity).getOutCode(), ((FCtStornoType) entity).getType(), ((FCtStornoType) entity).getCompany(), deleted);
		if(entity instanceof FCtJteDefault) return findFCtJteDefaultByJteAndOutCodeAndCompanyAndDeleted(((FCtJteDefault) entity).getJte(), ((FCtJteDefault) entity).getOutCode(), ((FCtJteDefault) entity).getCompany(), deleted);
		if(entity instanceof FDiscount) {
			FDiscount existingEntity = findFDiscountByGodAndCompanyAndDeleted(((FDiscount) entity).getGod(), ((FDiscount) entity).getCompany(), deleted);
			if(existingEntity == null) {
				existingEntity = findFDiscountByGteIdAndCompanyAndDeleted(((FDiscount) entity).getGteId(), ((FDiscount) entity).getCompany(), deleted);
			}
			if(existingEntity == null) {
				existingEntity = findFDiscountByParIdAndCompanyAndDeleted(((FDiscount) entity).getParId(), ((FDiscount) entity).getCompany(), deleted);
			}
			if(existingEntity == null) {
				existingEntity = findFDiscountByPgpIdAndCompanyAndDeleted(((FDiscount) entity).getPgpId(), ((FDiscount) entity).getCompany(), deleted);
			}
			return existingEntity;
		}
		if(entity instanceof FJournalType) return findFJournalTypeByCodeAndCompanyAndDeleted(((FJournalType) entity).getCode(), ((FJournalType) entity).getCompany(), deleted);
		if(entity instanceof FInvDdsFile) return findFInvDdsFileByOutCodeAndPeriodAndCompanyAndDeleted(((FInvDdsFile) entity).getOutCode(), ((FInvDdsFile) entity).getPeriod(), ((FInvDdsFile) entity).getCompany(), deleted);
		if(entity instanceof FPtClosingAccount) return findFPtClosingAccountByOutCodeAndPeriodAndCompanyAndDeleted(((FPtClosingAccount) entity).getOutCode(), ((FPtClosingAccount) entity).getPeriodMonth(), ((FPtClosingAccount) entity).getPeriodYear(), ((FPtClosingAccount) entity).getCompany(), deleted);
		if(entity instanceof FPtCoaBalance) return findFPtCoaBalanceByCoaIdAndOutCodeAndPeriodAndCompanyAndDeleted(((FPtCoaBalance) entity).getCoaId(), ((FPtCoaBalance) entity).getOutCode(), ((FPtCoaBalance) entity).getPeriod(),((FPtCoaBalance) entity).getCompany(), deleted);

		// Nepal entities
		if(entity instanceof Schedule) return findScheduleByMessageIdentificationAndMessageVersionAndCompanyAndDeleted(((Schedule) entity).getMessageIdentification(), ((Schedule) entity).getMessageVersion(), ((Schedule) entity).getCompany(), false);
	//	if(entity instanceof PowerPlant) return findPowerPlantByIdentificationNumberAndCompanyAndDeleted(((PowerPlant) entity).getIdentificationNumber(), ((PowerPlant) entity).getCompany(),false);
		if(entity instanceof IbexPrice) return findIbexPriceByLocalDateAndHourAndCompanyAndDeleted(((IbexPrice) entity).getLocalDate(), ((IbexPrice) entity).getHour(), ((IbexPrice) entity).getCompany(), false);
		if(entity instanceof PowerPlantMeterReading) return findPowerPlantMeterReadingByIdentificationAndStartTSAndCompanyAndDeleted(((PowerPlantMeterReading) entity).getIdentification(), ((PowerPlantMeterReading) entity).getStartTS(), ((PowerPlantMeterReading) entity).getCompany(), false);
		if(entity instanceof PowerPlantProducedSchedule) return findPowerPlantProducedScheduleByIdentificationAndStartTSAndCompanyAndDeleted(((PowerPlantProducedSchedule) entity).getIdentification(), ((PowerPlantProducedSchedule) entity).getStartTS(), ((PowerPlantProducedSchedule) entity).getCompany(), false);
		if(entity instanceof IbexEnergyDeal) return findIbexEnergyDealByTradeIdAndCompanyAndDeleted(((IbexEnergyDeal) entity).getTradeId(), ((IbexEnergyDeal) entity).getCompany(), false);
		if(entity instanceof MailTemplate) return findMailTemplateByTemplateCodeAndCompanyAndDeleted(((MailTemplate) entity).getTemplateCode(), ((MailTemplate) entity).getCompany(), false);
		if(entity instanceof Notification) return findNotificationByIsActiveAndIdentificationFirstAndIdentificationSecond(((Notification) entity).getIsActive(), ((Notification) entity).getIdentificationFirst(), ((Notification) entity).getIdentificationSecond());

		// Selfie Entities
		// TODO: This is the new rule, has to be uncommented
//		if (entity instanceof ElectricityInvoice) return findElectricityInvoiceByReportingPointOwnAndPeriodFromAndPeriodToAndCompanyAndDeleted(((ElectricityInvoice) entity).getReportingPointOwn(), ((ElectricityInvoice) entity).getPeriodFrom(), ((ElectricityInvoice) entity).getPeriodTo(), ((ElectricityInvoice) entity).getCompany(), false);
		if (entity instanceof AccountingPeriod) return findAccountingPeriodByMonthAndCompanyAndDeleted(((AccountingPeriod) entity).getMonth(),  ((AccountingPeriod) entity).getCompany(), false);
		if (entity instanceof AgreementTypeMapping) return findAgreementTypeMappingByCrmCodeAndCompanyAndDeleted(((AgreementTypeMapping) entity).getCrmCode(),  ((AgreementTypeMapping) entity).getCompany(), false);
		if (entity instanceof AgreementSelfInvoicing) return findAgreementSelfInvoicingByAgreementSelfInvoicingIdCodeAndCompanyAndDeleted(((AgreementSelfInvoicing) entity).getAgreementSelfInvoicingIdCode(),  ((AgreementSelfInvoicing) entity).getCompany(), false);
		if (entity instanceof SelfInvoicingLine) return findSelfInvoicingLineBySelfInvoicingLineIdCodeAndCompanyAndDeleted(((SelfInvoicingLine) entity).getSelfInvoicingLineIdCode(),  ((SelfInvoicingLine) entity).getCompany(), false);
		logger.trace("checkForUniqueCommonRecord doesn't handle such entity class: " + entity.getClass().getSimpleName());

		return null;
	}

}
