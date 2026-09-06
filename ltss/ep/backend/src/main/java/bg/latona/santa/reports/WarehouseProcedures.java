package bg.latona.santa.reports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.WebApplicationContext;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Coalesce;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;

import bg.latona.santa.RepositoryConfiguration;
import bg.latona.santa.entities.CommonRecord;
import bg.latona.santa.entities.QManagedCompany;
import bg.latona.santa.entities.allocation.QAllocationProxy;
import bg.latona.santa.entities.allocation.QAllocationRecord;
import bg.latona.santa.entities.allocation.QAllocationType;
import bg.latona.santa.entities.invoice.LoiPaymentType;
import bg.latona.santa.entities.santa.common.*;
import bg.latona.santa.entities.santa.finance.FBreTransition;
import bg.latona.santa.entities.santa.finance.FCtInvDealType;
import bg.latona.santa.entities.santa.finance.FCtTransitionType;
import bg.latona.santa.entities.santa.finance.LoiBreTransitionResult;
import bg.latona.santa.entities.santa.finance.QFBreTransition;
import bg.latona.santa.entities.santa.finance.QFCtTransitionType;
import bg.latona.santa.entities.security.QSecUser;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.repositories.*;
import bg.latona.santa.security.SantaUser;

@Repository
@Transactional
public class WarehouseProcedures {

	private static Logger logger = LoggerFactory.getLogger(WarehouseProcedures.class);
	// entity manager insert
	@PersistenceContext
	private EntityManager entityManager; //USING HQL
	@Autowired
	private WebApplicationContext appContext;
	private Repositories repositories = null;
	
	private Repositories getRepositories() {
		if(repositories == null) {
			repositories = new Repositories(appContext);
		}
		return repositories;
	}
	
	CommonRecord droolsSave(CommonRecord entity) {
		logger.trace("WarehouseProcedures.droolsSave "+entity);
		CommonRecord result;
		if(entity.getId() == null) {
			RepositoryConfiguration.getBeforeCreateValidator().validate(entity, null);
			result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass()).get()).saveAndFlush(entity);
			RepositoryConfiguration.getAfterCreateValidator().validate(entity, null);
		} else {
			RepositoryConfiguration.getBeforeSaveValidator().validate(entity, null);
			result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass()).get()).saveAndFlush(entity);
			RepositoryConfiguration.getAfterSaveValidator().validate(entity, null);
		}
		return result;
	}
	
	public List<Object> blockStocks(Long pdeyId, Long pOutCode){
		
		List<Object> result = new LinkedList<Object>();
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);

		QCDeliveryDetail cDeliveryDetail = QCDeliveryDetail.cDeliveryDetail;
		QCMeasure cMeasure = QCMeasure.cMeasure;
		QCGoods cGoods = QCGoods.cGoods;
		QCStock cStock = QCStock.cStock;
		CBlock vBlkId = null;

		//To do check how to for update of stock
		List<Tuple> pors = queryFactory.select(cStock.id, cDeliveryDetail.quantity, cDeliveryDetail.meeId.id, cStock.quantity, cMeasure.cofficient, cGoods.meeId.id,
				cDeliveryDetail.orlId.id, cStock.godId.id, cStock.meeId.id, cGoods.nameBg, cStock.blockedQuantity)
			.from(cDeliveryDetail)
				.innerJoin(cMeasure).on(cMeasure.id.eq(cDeliveryDetail.meeId.id).and(cMeasure.deleted.eq(false)))
				.innerJoin(cStock).on(cStock.ddlId.id.eq(cDeliveryDetail.id).and(cStock.deleted.eq(false)))
				.innerJoin(cGoods).on(cGoods.id.eq(cDeliveryDetail.godId.id).and(cGoods.deleted.eq(false)))
			.where(cDeliveryDetail.deyId.id.eq(pdeyId)
				.and(cDeliveryDetail.deleted.eq(false))
			)
			.fetch();

		for(Tuple por: pors) {
			BigDecimal nonBloQuan= por.get(cStock.quantity).subtract(por.get(cStock.blockedQuantity));
			BigDecimal coef = BigDecimal.ZERO;

			if (por.get(cDeliveryDetail.meeId.id) == por.get(cGoods.meeId.id)) {
				coef = BigDecimal.ONE;
			} else {
				coef = converMeasuers(por.get(cDeliveryDetail.meeId.id), por.get(cGoods.meeId.id));
			}

			BigDecimal block = (por.get(cStock.quantity).multiply(por.get(cMeasure.cofficient))).setScale(2, BigDecimal.ROUND_HALF_EVEN);
			BigDecimal nonblocked = (nonBloQuan.multiply(por.get(cMeasure.cofficient))).setScale(2, BigDecimal.ROUND_HALF_EVEN);

			if (block.compareTo(nonblocked) <= 0) {
				if(vBlkId == null) {
					CDelivery deliveryTo = entityManager.getReference(CDelivery.class, pdeyId);
					String remark = String.format("Скл.разп.%s", deliveryTo.getDeyNumber());
					CCcOrganizationUnit organUnitToIns = entityManager.getReference(CCcOrganizationUnit.class, pOutCode);

					vBlkId = new CBlock(null, null, null, null, false, organUnitToIns.getCompany(), "QB", null, remark, organUnitToIns);
					droolsSave(vBlkId);
					result.add(vBlkId);
				}
				CStock stockTo = entityManager.getReference(CStock.class, por.get(cStock.id));

				//TODO this should activate the drools rules
				CBlockedQuantity newBlockQuan = new CBlockedQuantity(null, null, null, null, false, stockTo.getCompany(), block, stockTo, vBlkId, null, null);
				droolsSave(newBlockQuan);
				result.add(newBlockQuan);
			} else {
				if (nonblocked.equals(BigDecimal.ZERO)) {
					throw new ReportException(String.format("Цялото количество за стока %s е вече блокирано", por.get(cGoods.nameBg)));
				} else {
					throw new ReportException(String.format("Блокиранe за стока %s на количество, по-голямо от оставащото неблокирано %s > %s", por.get(cGoods.nameBg), block, nonblocked));
				}
			}
		}
		return result;
	}

	public BigDecimal converMeasuers(Long mee1, Long mee2){
		if(mee1 == mee2){
			return BigDecimal.ONE;
		}

		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QCMeasure cMeasure = QCMeasure.cMeasure;
		QCMeasure measure2 = new QCMeasure("measure2");

		Coalesce<Long> coalesceMeeId = new Coalesce<>(Long.class).add(mee2).add(cMeasure.meeId.id);

		List<Tuple> vCoeff = queryFactory.select(cMeasure.cofficient, measure2.cofficient)
			.from(cMeasure)
				.innerJoin(measure2).on(cMeasure.id.eq(measure2.id).and(measure2.deleted.eq(false)))
			.where(cMeasure.id.eq(mee1).and(measure2.id.eq(coalesceMeeId)).and(cMeasure.deleted.eq(false)))
			.fetch();

		BigDecimal coeff = null;
		for (Tuple row : vCoeff) {
			BigDecimal m1Coeff = row.get(cMeasure.cofficient);
			BigDecimal m2Coeff = row.get(measure2.cofficient);
		
			coeff = m2Coeff.divide(m1Coeff);
		}
		if( coeff == null ) {
			throw new ReportException("Грешка при конвертиране на мерни единици!");
		}

		return coeff;
	}
	
	public BigDecimal exchangeRate(CCtCurrency p_from_cur, CCtCurrency p_to_cur, LocalDate p_date) {
		if( p_from_cur == null || p_to_cur == null ) return BigDecimal.ONE;
		if( p_from_cur.equals( p_to_cur ) ) return BigDecimal.ONE;
		CPmtCurrencyRateRepository rateRepo = ((CPmtCurrencyRateRepository) getRepositories().getRepositoryFor(CPmtCurrencyRate.class).get());
		CPmtCurrencyRate rate1 = rateRepo.findFirstByCuyCodeAndDateFromLessThanEqualAndDateToGreaterThanEqualAndCompanyAndDeleted(p_from_cur, p_date, p_date, p_from_cur.getCompany(), false);
		CPmtCurrencyRate rate2 = rateRepo.findFirstByCuyCodeAndDateFromLessThanEqualAndDateToGreaterThanEqualAndCompanyAndDeleted(p_to_cur, p_date, p_date, p_from_cur.getCompany(), false);
		if( rate1 == null ) {
			throw new ReportException("Не може да се определи актуален курс на "+p_from_cur.getName()+"/"+p_to_cur.getName()+" към "+p_date);
		}
		if( rate2 == null) {
			return rate1.getInMainCuy().divide(new BigDecimal(rate1.getUnitOfCuy()), 5, RoundingMode.HALF_UP);
		} else {
			return rate1.getInMainCuy().multiply(new BigDecimal(rate2.getUnitOfCuy())).divide( rate2.getInMainCuy().multiply(new BigDecimal(rate1.getUnitOfCuy())), 5, RoundingMode.HALF_UP);
		}
	}

	public BigDecimal calcExchangeRate(Long pFromCur, Long pToCur, LocalDate pDate, Long pOutCode){
		if (pFromCur == pToCur) {
			return BigDecimal.ONE;
		}	

		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QCPmtCurrencyRate cPmtCurrencyRate = QCPmtCurrencyRate.cPmtCurrencyRate;
		BigDecimal firstCur = queryFactory.select(cPmtCurrencyRate.inMainCuy.divide(cPmtCurrencyRate.unitOfCuy).as("firstCuy"))
			.from(cPmtCurrencyRate)
			.where(cPmtCurrencyRate.cuyCode.id.eq(pFromCur)
				.and(cPmtCurrencyRate.outCode.id.eq(pOutCode))
				.and(cPmtCurrencyRate.dateFrom.before(pDate)).and(cPmtCurrencyRate.dateTo.after(pDate))
				.and(cPmtCurrencyRate.deleted.eq(false))
			)
			.fetchFirst();

		BigDecimal secondCur = queryFactory.select(cPmtCurrencyRate.inMainCuy.divide(cPmtCurrencyRate.unitOfCuy).as("secondCuy"))
			.from(cPmtCurrencyRate)
			.where(cPmtCurrencyRate.cuyCode.id.eq(pToCur)
				.and(cPmtCurrencyRate.outCode.id.eq(pOutCode))
				.and(cPmtCurrencyRate.dateFrom.before(pDate)).and(cPmtCurrencyRate.dateTo.after(pDate))
				.and(cPmtCurrencyRate.deleted.eq(false))
			)
			.fetchFirst();

		if(firstCur == null) {
			return BigDecimal.ONE;
		}
		if(secondCur == null) {
			secondCur = BigDecimal.ONE;
		}

		BigDecimal vRate = firstCur.divide(secondCur, 5, RoundingMode.HALF_UP);

		if (vRate == null) {
			throw new ReportException(String.format("Не може да се определи актуален курс на %s/%s към %sг", pFromCur, pToCur, pDate));
			//throw new ReportException("Не може да се определи актуален курс на "+p_from_cur.getName()+"/"+p_to_cur.getName()+" към "+p_date);
		}

		return vRate;
	}
	
	public List<Map<String,Object>> getStocksDate(Long outCodeId, Long parId, LocalDate toDate) {
		LoiTypeDocRepository orderStatusRepo = ((LoiTypeDocRepository) getRepositories().getRepositoryFor(LoiTypeDoc.class).get());
		LoiTypeDoc typeDocKi = orderStatusRepo.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiTypeDoc.TYPE_DOC_KI, entityManager.getReference(CCcOrganizationUnit.class, outCodeId).getCompany(), false);
		List<Object[]> queryResult = entityManager.createNativeQuery("select b.*\n" + 
				"from\n" + 
				"(select\n" + 
				"    a.stk_id,\n" + 
				"    sum(round(a.presence,2)) as presence,\n" + 
				"    sum(round(a.delivered,2)) as delivered,\n" + 
				"    sum(round(a.saled,2)) as saled,\n" + 
				"    sum(round(a.cost*a.presence,2)) as presence_cost,\n" + 
				"    sum(round(a.cost*a.delivered,2)) as delivered_cost,\n" + 
				"    sum(round(a.cost*a.saled,2)) as saled_cost,\n" + 
				"    sum(round(a.delivered_b,2)) as delivered_b,			            -- закупени предх.година\n" + 
				"	sum(round(a.saled_b,2)) as saled_b,			  	                -- закупени предх.година\n" + 
				"	sum(round(a.reserve_quantity,2)) as reserve_quantity,	        -- резервирано количество\n" + 
				"	sum(round(a.blocked_quantity,2)) as blockedQuantity,	-- блокирано количество, чакащо някакъв вид качествен контрол\n" + 
				"\n" + 
				"sum(round(a.consignment_quantity,2)) as consignmentQuantity,	-- количество на консигнация\n" + 
				"	(round(sum(a.presence) - sum(a.blocked_quantity) - sum(a.consignment_quantity),2)) as free,	-- free to sell\n" + 
				"	(case when sum(a.presence) = 0 then 0 else round(sum(a.cost*a.presence)/sum(a.presence),5) end) as avgPrice	-- средна цена\n" + 
				"\n" + 
				"from (select god.id godId, stk.id as stk_id, m1.name as mee_name, stk.cost as cost,\n" + 
				"             (stk.initial_quantity*m1.cofficient/m2.cofficient\n" + 
				"                - coalesce((select sum( case when sae.type_doc_id=:typeDocKiId then - sdl.quantity/m3.cofficient else sdl.quantity/m3.cofficient end)\n" + 
				"                      from csale_detail sdl, csale sae, cmeasure m3\n" + 
				"                      where stk.id=sdl.stk_id_id and sdl.sae_id_id=sae.id and\n" + 
				"                            sdl.mee_id_id=m3.id and\n" + 
				"                            sae.sale_date <= :toDate\n" + 
				"                            and sdl.deleted=false and sae.deleted=false and m3.deleted=false\n" +
				"                      group by sdl.stk_id_id),0)* m1.cofficient)  as presence,\n" + 
				"            0 as delivered,\n" + 
				"            0 as saled,\n" + 
				"            0 as delivered_b,\n" + 
				"            0 as saled_b,\n" + 
				"            stk.reserve_quantity as reserve_quantity,\n" + 
				"            stk.blocked_quantity as blocked_quantity,\n" + 
				"	  stk.consignment_quantity as consignment_quantity\n" + 
				"\n" + 
				"      from cgoods god, cstock stk, cmeasure m1, cmeasure m2, ccc_organization_unit out, ccc_partner par,  cgood_mark godm, ccc_goods_type gte\n" + 
				"            where god.id = stk.god_id_id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and stk.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"            and stk.out_code_id = :outCodeId\n" + 
				"            and stk.stock_date <= :toDate\n" + 
				"            and stk.par_id_id = par.id\n" + 
				"            and god.good_mark_id = godm.id\n" + 
				"            and god.gte_id_id = gte.id\n" + 
				"            and stk.par_id_id = coalesce(:parId, stk.par_id_id)\n" + 
				"            and god.deleted = false and stk.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false and par.deleted = false and godm.deleted = false and gte.deleted = false\n" +
				"--$P!{WHERE}\n" + 
				"\n" + 
				"	  and stk.god_id_id = god.id\n" + 
				"\n" + 
				"\n" + 
				"      union all\n" + 
				"\n" + 
				"      select god.id, stk.id as stk_id, m1.name as mee_name, stk.cost as cost,\n" + 
				"             0 as presence,\n" + 
				"             stk.initial_quantity*m1.cofficient/m2.cofficient as delivered,\n" + 
				"             0 as saled,\n" + 
				"             0 as delivered_b,\n" + 
				"             0 as saled_b,\n" + 
				"             0 as reserve_quantity,\n" + 
				"             0 as blocked_quantity,\n" + 
				"	   0  as consignment_quantity\n" + 
				"      from cgoods god, cstock stk, cmeasure m1, cmeasure m2, ccc_organization_unit out,ccc_partner par, cgood_mark godm, ccc_goods_type gte\n" + 
				"      where god.id = stk.god_id_id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and stk.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"            and stk.out_code_id = :outCodeId\n" + 
				"            and stk.stock_date <= :toDate\n" + 
				"            and stk.par_id_id = par.id\n" + 
				"            and god.good_mark_id = godm.id\n" + 
				"            and god.gte_id_id = gte.id\n" + 
				"			and stk.par_id_id = coalesce(:parId, stk.par_id_id)\n" + 
				"            and god.deleted = false and stk.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false and par.deleted = false and godm.deleted = false and gte.deleted = false\n" +
				"--$P!{WHERE}\n" + 
				"\n" + 
				"	  and stk.god_id_id = god.id\n" + 
				"\n" + 
				"      union all\n" + 
				"\n" + 
				"      select god.id, stk.id as stk_id, m1.name as mee_name, sdl.price*sdl.rate_exchange as cost,\n" + 
				"             0 as presence,\n" + 
				"             0 as delivered,\n" + 
				"             (case when sae.type_doc_id=:typeDocKiId THEN (-sdl.quantity*m1.cofficient/m2.cofficient)\n" + 
				"                            else  sdl.quantity*m1.cofficient/m2.cofficient\n" + 
				"                        end) as saled,\n" + 
				"             0 as delivered_b,\n" + 
				"             0 as saled_b,\n" + 
				"             0 as reserve_quantity,\n" + 
				"             0 as blocked_quantity,\n" + 
				"	   0  as consignment_quantity\n" + 
				"      from cgoods god, cstock stk, csale_detail sdl, csale sae, cmeasure m1, cmeasure m2, ccc_organization_unit out,ccc_partner par, cgood_mark godm, ccc_goods_type gte\n" + 
				"      where god.id = stk.god_id_id\n" + 
				"            and stk.id=sdl.stk_id_id\n" + 
				"            and sdl.sae_id_id=sae.id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and sdl.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"           and stk.out_code_id = :outCodeId\n" + 
				"            and sae.sale_date <= :toDate\n" + 
				"            and stk.par_id_id = par.id\n" + 
				"            and god.good_mark_id = godm.id\n" + 
				"            and god.gte_id_id = gte.id\n" + 
				"			and stk.par_id_id = coalesce(:parId, stk.par_id_id)\n" + 
				"            and god.deleted = false and stk.deleted = false and sdl.deleted = false and sae.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false and par.deleted = false and godm.deleted = false and gte.deleted = false\n" +
				"		--$P!{WHERE}\n" + 
				"	  and stk.god_id_id = god.id\n" + 
				"\n" + 
				"      --group by stk.id;\n" + 
				"\n" + 
				"      union all\n" + 
				"\n" + 
				"      select god.id,\n" + 
				"            stk.id as stk_id,\n" + 
				"            m1.name as mee_name,\n" + 
				"            stk.cost as cost,\n" + 
				"            0 as presence,\n" + 
				"            0 as delivered,\n" + 
				"            0 as saled,\n" + 
				"            stk.initial_quantity*m1.cofficient/m2.cofficient as delivered_b,\n" + 
				"            0 as saled_b,\n" + 
				"            0 as reserve_quantity,\n" + 
				"            0 as blocked_quantity,\n" + 
				"	  0  as consignment_quantity\n" + 
				"      from cgoods god, cstock stk, cmeasure m1, cmeasure m2, ccc_organization_unit out,ccc_partner par, cgood_mark godm, ccc_goods_type gte\n" + 
				"      where god.id = stk.god_id_id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and stk.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"            and stk.out_code_id = :outCodeId\n" + 
				"            and stk.stock_date < (date_trunc('year', current_date))\n" + 
				"            and stk.par_id_id = par.id\n" + 
				"            and god.good_mark_id = godm.id\n" + 
				"            and god.gte_id_id = gte.id\n" + 
				"			and stk.par_id_id = coalesce(:parId, stk.par_id_id)\n" + 
				"           and god.deleted = false and stk.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false and par.deleted = false and godm.deleted = false and gte.deleted = false\n" +
				"		--$P!{WHERE}\n" + 
				"\n" + 
				"	  and stk.god_id_id = god.id\n" + 
				"\n" + 
				"	union all\n" + 
				"\n" + 
				"      select god.id,\n" + 
				"            stk.id as stk_id,\n" + 
				"            m1.name as mee_name,\n" + 
				"            stk.cost as cost,\n" + 
				"            0 as presence,\n" + 
				"            0 as delivered,\n" + 
				"            0 as saled,\n" + 
				"            0 as delivered_b,\n" + 
				"           (case when sae.type_doc_id=:typeDocKiId THEN (-sdl.quantity*m1.cofficient/m2.cofficient)\n" + 
				"                            else  sdl.quantity*m1.cofficient/m2.cofficient\n" + 
				"                        end) as saled_b,\n" + 
				"            0 as reserve_quantity,\n" + 
				"            0 as blocked_quantity,\n" + 
				"	  0  as consignment_quantity\n" + 
				"      from cgoods god, cstock stk, csale_detail sdl, csale sae, cmeasure m1, cmeasure m2, ccc_organization_unit out,ccc_partner par, cgood_mark godm, ccc_goods_type gte\n" + 
				"      where god.id = stk.god_id_id\n" + 
				"            and stk.id=sdl.stk_id_id\n" + 
				"            and sdl.sae_id_id=sae.id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and sdl.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"            and stk.out_code_id = :outCodeId\n" + 
				"            and sae.sale_date < (date_trunc('year', current_date))\n" + 
				"	    and stk.par_id_id = par.id\n" + 
				"	    and god.good_mark_id = godm.id\n" + 
				"	    and god.gte_id_id = gte.id\n" + 
				"		and stk.god_id_id = god.id\n" + 
				"		and stk.par_id_id = coalesce(:parId, stk.par_id_id)\n" + 
				"       and god.deleted = false and stk.deleted = false and sdl.deleted = false and sae.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false and par.deleted = false and godm.deleted = false and gte.deleted = false\n" +
				"		--$P!{WHERE}\n" + 
				"\n" + 
				"\n" + 
				"       ) a\n" + 
				"group by a.stk_id, a.godId, a.mee_name\n" + 
				") b\n" + 
				"where b.presence <> 0")
				.setParameter("outCodeId", outCodeId)
				.setParameter("typeDocKiId", typeDocKi.getId())
				.setParameter("parId", parId)
				.setParameter("toDate", toDate.atTime(LocalTime.MAX))
				.getResultList();

		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {
			// 0:god_id, 1:mee_id, 2:mee_coefficien, 3:rqy_quantity, 4:stk_quantity, 5:quantity_confirm
			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("stkId", entityManager.getReference(CStock.class, Long.parseLong(queryRow[0].toString())));
			resultRow.put("available", queryRow[1]);
			resultRow.put("delivered", queryRow[2]);
			resultRow.put("sold", queryRow[3]);
			resultRow.put("availableCost", queryRow[4]);
			resultRow.put("deliveredCost", queryRow[5]);
			resultRow.put("soldCost", queryRow[6]);
			resultRow.put("deliveredBefore", queryRow[7]);
			resultRow.put("soldBefore", queryRow[8]);
			resultRow.put("reserveQuantity", queryRow[9]);
			resultRow.put("blockedQuantity", queryRow[10]);
			resultRow.put("consignmentQuantity", queryRow[11]);
			resultRow.put("free", queryRow[12]);
			resultRow.put("avgPrice", queryRow[13]);
			result.add(resultRow);
		}
		return result;
	}
	
	public List<Map<String,Object>> getStocks(Long outCodeId, LocalDate fromDate, LocalDate toDate) {
		LoiTypeDocRepository orderStatusRepo = ((LoiTypeDocRepository) getRepositories().getRepositoryFor(LoiTypeDoc.class).get());
		LoiTypeDoc typeDocKi = orderStatusRepo.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiTypeDoc.TYPE_DOC_KI, entityManager.getReference(CCcOrganizationUnit.class, outCodeId).getCompany(), false);
		List<Object[]> queryResult = entityManager.createNativeQuery("select\n" + 
				"    a.godId,\n" + 
				"    a.out_code_id as out_code_id,\n" + 
				"    sum(round(a.presence,2)) as presence,\n" + 
				"    sum(round(a.delivered,2)) as delivered,\n" + 
				"    sum(round(a.saled,2)) as saled,\n" + 
				"    sum(round(a.cost*a.presence,2)) as presence_cost,\n" + 
				"    sum(round(a.cost*a.delivered,2)) as delivered_cost,\n" + 
				"    sum(round(a.cost*a.saled,2)) as saled_cost,\n" + 
				"    sum(round(a.delivered_b,2)) as delivered_b,			            -- закупени предх.година\n" + 
				"	sum(round(a.saled_b,2)) as saled_b,			  	                -- закупени предх.година\n" + 
				"	sum(round(a.reserve_quantity,2)) as reserve_quantity,	        -- резервирано количество\n" + 
				"	sum(round(a.blocked_quantity,2)) as blocked_quantity,	-- блокирано количество, чакащо някакъв вид качествен контрол\n" + 
				"	(round(sum(a.presence) - sum(a.blocked_quantity),2)) as free,	-- free to sell\n" + 
				"	(case when sum(a.presence) = 0 then 0 else round(sum(a.cost*a.presence)/sum(a.presence),5) end) as spc	-- средна цена\n" + 
				"\n" + 
				"from (select god.id as godId, stk.cost as cost,\n" + 
				"             (stk.initial_quantity*m1.cofficient/m2.cofficient\n" + 
				"                - coalesce((select sum( case when sae.type_doc_id=:typeDocKiId/*'KI'*/ then - sdl.quantity/m3.cofficient else sdl.quantity/m3.cofficient end)\n" + 
				"                      from csale_detail sdl, csale sae, cmeasure m3\n" + 
				"                      where stk.id=sdl.stk_id_id and sdl.sae_id_id=sae.id and\n" + 
				"                            sdl.mee_id_id=m3.id and\n" + 
				"                            sae.sale_date <= :toDate\n" + 
				"                            and sdl.deleted = false and sae.deleted = false and m3.deleted = false\n" +
				"                      group by sdl.stk_id_id),0)* m1.cofficient)  as presence,\n" + 
				"            0 as delivered,\n" + 
				"            0 as saled,\n" + 
				"            0 as delivered_b,\n" + 
				"            0 as saled_b,\n" + 
				"            stk.reserve_quantity as reserve_quantity,\n" + 
				"            stk.blocked_quantity as blocked_quantity,\n" + 
				"            out.id as out_code_id\n" + 
				"      from cgoods god, cstock stk, cmeasure m1, cmeasure m2, ccc_organization_unit out\n" + 
				"      where god.id = stk.god_id_id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and stk.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"            and stk.out_code_id = :outCodeId\n" + 
				"            and stk.stock_date <= :toDate\n" +  
				"            and god.deleted = false and stk.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false\n" +
				"            --$P!{WHERE_CLAUSE}\n" + 
				"\n" + 
				"      union all\n" + 
				"\n" + 
				"      select god.id as godId, stk.cost as cost,\n" + 
				"             0 as presence,\n" + 
				"             stk.initial_quantity*m1.cofficient/m2.cofficient as delivered,\n" + 
				"             0 as saled,\n" + 
				"             0 as delivered_b,\n" + 
				"             0 as saled_b,\n" + 
				"             0 as reserve_quantity,\n" + 
				"             0 as blocked_quantity,\n" + 
				"             out.id as out_code_id\n" + 
				"      from cgoods god, cstock stk, cmeasure m1, cmeasure m2, ccc_organization_unit out\n" + 
				"      where god.id = stk.god_id_id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and stk.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"            and stk.out_code_id = :outCodeId\n" + 
				"            and stk.stock_date between :fromDate and :toDate\n" +  
				"            and god.deleted = false and stk.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false\n" +
				"            --where clause\n" + 
				"      union all\n" + 
				"\n" + 
				"      select god.id as godId, sdl.price*sdl.rate_exchange as cost,\n" + 
				"             0 as presence,\n" + 
				"             0 as delivered,\n" + 
				"             (case when sae.type_doc_id=:typeDocKiId/*'KI'*/ THEN (-sdl.quantity*m1.cofficient/m2.cofficient)\n" + 
				"                            else  sdl.quantity*m1.cofficient/m2.cofficient\n" + 
				"                        end) as saled,\n" + 
				"             0 as delivered_b,\n" + 
				"             0 as saled_b,\n" + 
				"             0 as reserve_quantity,\n" + 
				"             0 as blocked_quantity,\n" + 
				"             out.id as out_code_id\n" + 
				"      from cgoods god, cstock stk, csale_detail sdl, csale sae, cmeasure m1, cmeasure m2, ccc_organization_unit out\n" + 
				"      where god.id = stk.god_id_id\n" + 
				"            and stk.id=sdl.stk_id_id\n" + 
				"            and sdl.sae_id_id=sae.id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and sdl.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"            and stk.out_code_id = :outCodeId\n" + 
				"            and sae.sale_date between :fromDate and :toDate\n" +  
				"            and god.deleted = false and stk.deleted = false and sdl.deleted = false and sae.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false\n" +
				"            --where clause\n" + 
				"      --group by stk.id;\n" + 
				"\n" + 
				"      union all\n" + 
				"\n" + 
				"      select god.id as godId,\n" + 
				"            stk.cost as cost,\n" + 
				"            0 as presence,\n" + 
				"            0 as delivered,\n" + 
				"            0 as saled,\n" + 
				"            stk.initial_quantity*m1.cofficient/m2.cofficient as delivered_b,\n" + 
				"            0 as saled_b,\n" + 
				"            0 as reserve_quantity,\n" + 
				"            0 as blocked_quantity,\n" + 
				"            out.id as out_code_id\n" + 
				"      from cgoods god, cstock stk, cmeasure m1, cmeasure m2, ccc_organization_unit out\n" + 
				"      where god.id = stk.god_id_id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and stk.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"            and stk.out_code_id = :outCodeId\n" + 
				"            and stk.stock_date < (date_trunc('year', current_date))\n" +  
				"            and god.deleted = false and stk.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false\n" +
				"            --where clause\n" + 
				"\n" + 
				"	union all\n" + 
				"\n" + 
				"      select god.id as godId,\n" + 
				"            stk.cost as cost,\n" + 
				"            0 as presence,\n" + 
				"            0 as delivered,\n" + 
				"            0 as saled,\n" + 
				"            0 as delivered_b,\n" + 
				"           (case when sae.type_doc_id=:typeDocKiId/*'KI'*/ THEN (-sdl.quantity*m1.cofficient/m2.cofficient)\n" + 
				"                            else  sdl.quantity*m1.cofficient/m2.cofficient\n" + 
				"                        end) as saled_b,\n" + 
				"            0 as reserve_quantity,\n" + 
				"            0 as blocked_quantity,\n" + 
				"            out.id as out_code_id\n" + 
				"      from cgoods god, cstock stk, csale_detail sdl, csale sae, cmeasure m1, cmeasure m2, ccc_organization_unit out\n" + 
				"      where god.id = stk.god_id_id\n" + 
				"            and stk.id=sdl.stk_id_id\n" + 
				"            and sdl.sae_id_id=sae.id\n" + 
				"            and god.mee_id_id = m1.id\n" + 
				"            and sdl.mee_id_id=m2.id\n" + 
				"            and out.id = stk.out_code_id\n" + 
				"            and stk.out_code_id = :outCodeId\n" + 
				"            and sae.sale_date < (date_trunc('year', current_date))\n" + 
				"            and god.deleted = false and stk.deleted = false and sdl.deleted = false and sae.deleted = false and m1.deleted = false and m2.deleted = false and out.deleted = false\n" + 
				"	    --where clause\n" + 
				"      ) a\n" + 
				"group by a.out_code_id, a.godId")
				.setParameter("outCodeId", outCodeId)
				.setParameter("typeDocKiId", typeDocKi.getId())
				.setParameter("fromDate", fromDate)
				.setParameter("toDate", toDate.atTime(LocalTime.MAX))
				.getResultList();

		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {
			// 0:god_id, 1:mee_id, 2:mee_coefficien, 3:rqy_quantity, 4:stk_quantity, 5:quantity_confirm
			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("godId", entityManager.getReference(CGoods.class, Long.parseLong(queryRow[0].toString())));
			resultRow.put("outCode", entityManager.getReference(CCcOrganizationUnit.class, Long.parseLong(queryRow[1].toString())));
			resultRow.put("available", queryRow[2]);
			resultRow.put("delivered", queryRow[3]);
			resultRow.put("sold", queryRow[4]);
			resultRow.put("availableCost", queryRow[5]);
			resultRow.put("deliveredCost", queryRow[6]);
			resultRow.put("soldCost", queryRow[7]);
			resultRow.put("deliveredBefore", queryRow[8]);
			resultRow.put("soldBefore", queryRow[9]);
			resultRow.put("reserveQuantity", queryRow[10]);
			resultRow.put("blockedQuantity", queryRow[11]);
			resultRow.put("free", queryRow[12]);
			resultRow.put("avgPrice", queryRow[13]);
			result.add(resultRow);
		}
		return result;
	}
	
	public List<Map<String,Object>> getCurrentStocks(String[] codes) {
		List<String> codesList = Arrays.asList(codes);
		List<Object[]> queryResult = entityManager.createNativeQuery("select b.*\n" + 
				"from\n" + 
				"(select\n" + 
				"    a.godId, \n"+
				"    a.out_code_id, \n"+
				"    sum(round(a.initial_quantity,2)) as initial_quantity,\n" + 
				"    sum(round(a.quantity,2)) as quantity,\n" + 
				"    sum(round(a.cost*a.initial_quantity,2)) as initial_quantity_cost,\n" + 
				"	sum(round(a.reserve_quantity,2)) as reserve_quantity,	        -- резервирано количество\n" + 
				"	sum(round(a.blocked_quantity,2)) as blockedQuantity,	-- блокирано количество, чакащо някакъв вид качествен контрол\n" +
				"	sum(round(a.consignment_quantity,2)) as consignmentQuantity	-- количество на консигнация\n" +  
				"\n" + 
				"from (select stk.out_code_id, god.code godId, stk.id as stk_id,"
				+ "			stk.cost as cost,\n" + 
				"            stk.initial_quantity as initial_quantity,\n" + 
				"            stk.quantity as quantity,\n" + 
				"            stk.reserve_quantity as reserve_quantity,\n" + 
				"            stk.blocked_quantity as blocked_quantity,\n" + 
				"	  stk.consignment_quantity as consignment_quantity\n" + 
				"\n" + 
				"      from cgoods god, cstock stk\n" + 
				"            where god.id = stk.god_id_id\n" + 
				"			and stk.quantity <> 0 \n" + 
				"            and god.deleted = false and stk.deleted = false \n" +
				"--$P!{WHERE}\n" + 
				"			and god.code in (:codes) \n" + 
				"       ) a\n" + 
				"group by a.out_code_id, a.godId\n" + 
				") b\n" + 
				"where b.quantity <> 0")
				.setParameter("codes", codesList)
				.getResultList();

		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {
			// 0:god_id, 1:mee_id, 2:mee_coefficien, 3:rqy_quantity, 4:stk_quantity, 5:quantity_confirm
			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("godCode", queryRow[0]);
			resultRow.put("out_code_id", queryRow[1]);
			resultRow.put("initialQuantity", queryRow[2]);
			resultRow.put("quantity", queryRow[3]);
			resultRow.put("initialQuantityCost", queryRow[4]);
			resultRow.put("reserveQuantity", queryRow[5]);
			resultRow.put("blockedQuantity", queryRow[6]);
			resultRow.put("consignmentQuantity", queryRow[7]);
			result.add(resultRow);
		}
		return result;
	}

	public List<Map<String,Object>> getOrderGoods(Long charVar){
		logger.trace("Start getOrderGoods");
		//TODO company
		Query query = entityManager.createNativeQuery("select "
				+ "cgoods.id cgoods_id, cgoods.created_date cgoods_created_date, cgoods.deleted cgoods_deleted, cgoods.last_modified_date cgoods_last_modified_date, cgoods.barcode cgoods_barcode, cgoods.bundle_quantity cgoods_bundle_quantity, cgoods.code cgoods_code, cgoods.description cgoods_description, cgoods.manufacturer_code cgoods_manufacturer_code, cgoods.max_quantity cgoods_max_quantity, cgoods.min_quantity cgoods_min_quantity, cgoods.name_bg cgoods_name_bg, cgoods.name_eng cgoods_name_eng, cgoods.volume cgoods_volume, cgoods.weight cgoods_weight, cgoods.created_by_id cgoods_created_by_id, cgoods.last_modified_by_id cgoods_last_modified_by_id, cgoods.company_id cgoods_company_id, cgoods.default_vendor_id cgoods_default_vendor_id, cgoods.god_id_id cgoods_god_id_id, cgoods.good_mark_id cgoods_good_mark_id, cgoods.good_type_id cgoods_good_type_id, cgoods.gte_id_id cgoods_gte_id_id, cgoods.mee_id_id cgoods_mee_id_id, cgoods.out_code_id cgoods_out_code_id, "
				+ "ccc_partner.id ccc_partner_id, ccc_partner.created_date ccc_partner_created_date, ccc_partner.deleted ccc_partner_deleted, ccc_partner.last_modified_date ccc_partner_last_modified_date, ccc_partner.active_from ccc_partner_active_from, ccc_partner.active_to ccc_partner_active_to, ccc_partner.address ccc_partner_address, ccc_partner.bulstat ccc_partner_bulstat, ccc_partner.code ccc_partner_code, ccc_partner.egn ccc_partner_egn, ccc_partner.email ccc_partner_email, ccc_partner.fax ccc_partner_fax, ccc_partner.foreign_no ccc_partner_foreign_no, ccc_partner.mol ccc_partner_mol, ccc_partner.name ccc_partner_name, ccc_partner.old_code ccc_partner_old_code, ccc_partner.tel ccc_partner_tel, ccc_partner.vat_no ccc_partner_vat_no, ccc_partner.created_by_id ccc_partner_created_by_id, ccc_partner.last_modified_by_id ccc_partner_last_modified_by_id, ccc_partner.company_id ccc_partner_company_id, ccc_partner.account_mgr_user_id ccc_partner_account_mgr_user_id, ccc_partner.legal_status_id ccc_partner_legal_status_id, ccc_partner.out_code_id ccc_partner_out_code_id, ccc_partner.partner_group_id ccc_partner_partner_group_id, ccc_partner.partner_type_id ccc_partner_partner_type_id, "
				+ "cmeasure.id cmeasure_id, cmeasure.created_date cmeasure_created_date, cmeasure.deleted cmeasure_deleted, cmeasure.last_modified_date cmeasure_last_modified_date, cmeasure.code cmeasure_code, cmeasure.cofficient cmeasure_cofficient, cmeasure.name cmeasure_name, cmeasure.created_by_id cmeasure_created_by_id, cmeasure.last_modified_by_id cmeasure_last_modified_by_id, cmeasure.company_id cmeasure_company_id, cmeasure.mee_id_id cmeasure_mee_id_id, "
				+ "corder.id corder_id, corder.created_date corder_created_date, corder.deleted corder_deleted, corder.last_modified_date corder_last_modified_date, corder.date_orr corder_date_orr, corder.finish_date corder_finish_date, corder.order_num corder_order_num, corder.time_limit corder_time_limit, corder.total corder_total, corder.created_by_id corder_created_by_id, corder.last_modified_by_id corder_last_modified_by_id, corder.company_id corder_company_id, corder.currency_id corder_currency_id, corder.out_code_id corder_out_code_id, corder.par_id_id corder_par_id_id, corder.status_id corder_status_id, "
				+ "corder_detail.id corder_detail_id, corder_detail.cancelled_quantity corder_detail_cancelled_quantity, corder_detail.ddl_quantity corder_detail_ddl_quantity, corder_detail.pln_quantity corder_detail_pln_quantity, corder_detail.price corder_detail_price, corder_detail.price_confirm corder_detail_price_confirm, corder_detail.quantity corder_detail_quantity, corder_detail.quantity_confirm corder_detail_quantity_confirm, corder_detail.rqy_quantity corder_detail_rqy_quantity, corder_detail.stk_quantity corder_detail_stk_quantity, corder_detail.doi_id_id corder_detail_doi_id_id, corder_detail.god_id_id corder_detail_god_id_id, corder_detail.mee_id_id corder_detail_mee_id_id, corder_detail.orr_id_id corder_detail_orr_id_id, "
				+ "corder_detail.created_date corder_detail_created_date, corder_detail.deleted corder_detail_deleted, corder_detail.last_modified_date corder_detail_last_modified_date, corder_detail.created_by_id corder_detail_created_by_id, corder_detail.last_modified_by_id corder_detail_last_modified_by_id, corder_detail.company_id corder_detail_company_id, "
				+ "cprice_list.id cprice_list_id, cprice_list.created_date cprice_list_created_date, cprice_list.deleted cprice_list_deleted, cprice_list.last_modified_date cprice_list_last_modified_date, cprice_list.base_price cprice_list_base_price, cprice_list.discount cprice_list_discount, cprice_list.end_date cprice_list_end_date, cprice_list.price1 cprice_list_price1, cprice_list.price2 cprice_list_price2, cprice_list.price3 cprice_list_price3, cprice_list.price4 cprice_list_price4, cprice_list.price5 cprice_list_price5, cprice_list.start_date cprice_list_start_date, cprice_list.status cprice_list_status, cprice_list.created_by_id cprice_list_created_by_id, cprice_list.last_modified_by_id cprice_list_last_modified_by_id, cprice_list.company_id cprice_list_company_id, cprice_list.currency_id cprice_list_currency_id, cprice_list.doi_id_id cprice_list_doi_id_id, cprice_list.god_id_id cprice_list_god_id_id, cprice_list.mee_id_id cprice_list_mee_id_id, cprice_list.out_code_id cprice_list_out_code_id, cprice_list.par_id_id cprice_list_par_id_id, cprice_list.stk_id_id cprice_list_stk_id_id, "
				+ "b.mee_cofficient, "
				+ "b.rqy_quantity as rqy_quantity, b.stk_quantity as stk_quantity, "
				+ "round(coalesce("
					+ "(select sum((od.quantity_confirm - od.ddl_quantity) / m1.cofficient) as qty \n" +
							" from corder_detail as od, corder as orr, cmeasure as m1, list_option_item as loi, allocation_origin cOrderDetailAO \n" +
							" where od.orr_id_id = orr.id \n" +
								" and m1.id = od.mee_id_id \n" +
								" and loi.id = orr.status_id \n" +
								" and loi.list_option_item_code = 2 \n" + //CF
								" and od.god_id_id = b.god_id_id \n" +
								" and od.id = cOrderDetailAO.id \n" +
								" and cOrderDetailAO.deleted = false and orr.deleted = false and m1.deleted = false and loi.deleted = false"
					+ ")"
				+ ",0),2) as quantity_confirm, \n"
				+ "round(coalesce("
					+ "(select sum((od.quantity_confirm - od.ddl_quantity) / m1.cofficient) as qty \n" +
							" from corder_detail as od, corder as orr, cmeasure as m1, list_option_item as loi, allocation_origin cOrderDetailAO \n" +
							" where od.orr_id_id = orr.id \n" +
								" and m1.id = od.mee_id_id \n" +
								" and loi.id = orr.status_id \n" +
								" and loi.list_option_item_code = 1 \n" + //CR
								" and od.god_id_id = b.god_id_id \n" +
								" and od.id = cOrderDetailAO.id \n" +
								" and cOrderDetailAO.deleted = false and orr.deleted = false and m1.deleted = false and loi.deleted = false"
					+ ")"
				+ ",0),2) as quantity_not_confirmed, \n"
				+ "min_price \n"
				+ "from (select god_id_id, mee_id_id, mee_cofficient, sum(rqy_quantity) rqy_quantity, sum(stk_quantity) stk_quantity, min(min_price) min_price \n"
			+ " from (select r.god_id_id, god.mee_id_id, m3.cofficient as mee_cofficient, "
						+ "round(sum(r.initial_quantity / m3.cofficient),2) as rqy_quantity, 0 as stk_quantity, min(odl.price1) min_price \n" +
					" from creserve_quantity as r, coffer as o, cgoods as god, cmeasure as m3, list_option_item as loi, coffer_detail odl, allocation_origin cOrderDetailAO \n" +
					" where o.id = r.ofr_id_id \n" +
						" and odl.id = r.odl_id_id \n" +
						" and god.id = r.god_id_id \n" +
						" and m3.id = r.mee_id_id \n" +
						" and odl.id = cOrderDetailAO.id \n" +
						" and r.stk_id_id is null \n" +
						" and loi.id = o.status_id \n" +
						" and loi.list_option_item_code in (1) \n" + //RA //status PI too?
						" and o.end_date >= current_date \n" +
						" and r.deleted = false and o.deleted = false and god.deleted = false and m3.deleted = false and loi.deleted = false and cOrderDetailAO.deleted = false\n" +
					" group by r.god_id_id, god.mee_id_id, m3.cofficient \n" +
					"union all \n" +
					" select god.id as god_id_id, god.mee_id_id, m2.cofficient as mee_cofficient, 0 as rqy_quantity, "
						+ "round(sum(god.max_quantity / m2.cofficient - coalesce(stk.quantity,0)),2) as stk_quantity, 0 as min_price \n" +
					" from cgoods god "
						+ "left join \n" +
							" (select sum((s1.quantity-s1.reserve_quantity-s1.blocked_quantity) / m1.cofficient) as quantity, s1.god_id_id, "
								+ "s1.deleted \n" +
							" from cstock s1, cmeasure m1 \n" +
							" where s1.mee_id_id = m1.id "+
							" and s1.deleted = false and m1.deleted = false\n" +
							" group by s1.god_id_id,  s1.deleted) stk "
						+ "on stk.god_id_id = god.id and stk.deleted = false and god.deleted = false\n" +
						" join cmeasure m2 on m2.id = god.mee_id_id and m2.deleted = false \n" +
					" where round(coalesce(stk.quantity,0),2) < god.min_quantity / m2.cofficient \n" +
					" group by god.id, god.mee_id_id, m2.cofficient \n"
				+ ") a group by god_id_id, mee_id_id, mee_cofficient \n"
				+ ") b \n"
				+ " left join cgoods on cgoods.id = b.god_id_id and cgoods.deleted = false\n"
				+ " left join ccc_partner on ccc_partner.id = cgoods.default_vendor_id and ccc_partner.deleted = false\n"
				+ " left join (select min(corder.id) id, par_id_id, corder.company_id \n" + 
				"	from corder \n" + 
				"	inner join list_option_item loi on loi.id = status_id and loi.deleted = false \n" + 
				"	where loi.list_option_item_code = 1 and corder.deleted = false and loi.dtype = 'LoiOrderStatus'\n" +
				(charVar != null ? " and corder.out_code_id = :charVar \n" : "") + 
				"	group by par_id_id, corder.company_id) as first_corder on first_corder.par_id_id = ccc_partner.id and first_corder.company_id = cgoods.company_id\n" + 
				"left join corder on corder.id = first_corder.id and corder.deleted = false\n" + 
				"left join (select min(corder_detail.id) id, orr_id_id, god_id_id, cOrderDetailAO.company_id \n" + 
				"	from corder_detail \n" + 
				"	inner join allocation_origin cOrderDetailAO on cOrderDetailAO.id = corder_detail.id \n" + 
				"	where cOrderDetailAO.deleted = false \n" + 
				"	group by orr_id_id, god_id_id, cOrderDetailAO.company_id) as first_corder_detail on first_corder_detail.orr_id_id = first_corder.id and first_corder_detail.god_id_id = cgoods.id and first_corder_detail.company_id = cgoods.company_id\n" + 
				"left join (select corder_detail.*, created_date, deleted, last_modified_date, created_by_id, last_modified_by_id, company_id from corder_detail inner join allocation_origin cOrderDetailAO on cOrderDetailAO.id = corder_detail.id and cOrderDetailAO.deleted = false\n" + 
				") as corder_detail on corder_detail.id = first_corder_detail.id\n" + 
				"left join (select cprice_list.id, par_id_id, god_id_id, cprice_list.company_id \n" + 
				"	from cprice_list \n" + 
				"	where cprice_list.deleted = false and start_date <= CURRENT_DATE and CURRENT_DATE <= end_date\n" + 
				"	limit 1) as first_cprice_list on first_cprice_list.par_id_id = ccc_partner.id and first_cprice_list.god_id_id = cgoods.id and first_cprice_list.company_id = cgoods.company_id\n" + 
				"left join cprice_list on cprice_list.id = first_cprice_list.id and cprice_list.deleted = false"
				+ " left join cmeasure on cmeasure.id = b.mee_id_id and cmeasure.deleted = false",
			"getOrderGoodsMapping");
		if(charVar != null) {
			query.setParameter("charVar", charVar);
		}
		List<Object[]> queryResult = query.getResultList();

		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {
			// 0:god_id, 1:mee_id, 2:mee_coefficien, 3:rqy_quantity, 4:stk_quantity, 5:quantity_confirm
			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("godId", queryRow[0]);
			resultRow.put("defaultVendor", queryRow[1]);
			resultRow.put("meeId", queryRow[2]);
			resultRow.put("order", queryRow[3]);
			resultRow.put("orderDetail", queryRow[4]);
			resultRow.put("priceList", queryRow[5]);
			resultRow.put("cofficient", queryRow[6]);
			resultRow.put("rqyQuantity", queryRow[7]);
			resultRow.put("stkQuantity", queryRow[8]);
			resultRow.put("quantityConfirm", queryRow[9]);
			resultRow.put("quantityNotConfirmed", queryRow[10]);
			resultRow.put("minPrice", queryRow[11]);
			result.add(resultRow);
		}
		logger.trace("Finish getOrderGoods");
		return result;
	}

	public Map<String,List<Object>> makeOrder(Long pParId, Long pOutCode){
		logger.trace("Start makeOrder");
		//а) стоки, които са резервирани но не са в наличност
		//б) стоки, които са по-малко от критичната бройка за склада
		//в) от горното се изважда стоката, която вече е поръчана и се очаква да бъде доставена
		//забележка: количествата се представят в базова мерна единица
		Map<String,List<Object>> result = new HashMap<String,List<Object>>();
		List<Object> resultCOrders = new LinkedList<Object>();
		result.put("COrder", resultCOrders);
		List<Object> resultCOrderDetails = new LinkedList<Object>();
		result.put("COrderDetail", resultCOrderDetails);
		List<Object> resultMessages = new LinkedList<Object>();
		result.put("message", resultMessages);
		
		//List<Map<String,Object>> orderGoods = getOrderGoods(pOutCode);
		List<Map<String,Object>> orderGoods = getOrderGoods(pOutCode);
		logger.trace("Loaded goods to be ordered: " + orderGoods.size());

		CCcPartner partnerToIns = entityManager.getReference(CCcPartner.class, pParId);
		CCcOrganizationUnit organUnitToIns = entityManager.getReference(CCcOrganizationUnit.class, pOutCode);
		LoiOrderStatusRepository orderStatusRepo = ((LoiOrderStatusRepository) getRepositories().getRepositoryFor(LoiOrderStatus.class).get());
		LoiOrderStatus orderStatusCR = orderStatusRepo.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiOrderStatus.ORDER_STATUS_CR, partnerToIns.getCompany(), false);
		COrder vOrrId = null;
		boolean doUpdateOrder = false;
		//COrderDetailRepository orderDetailRepo = ((COrderDetailRepository) getRepositories().getRepositoryFor(COrderDetail.class).get());
		//CMeasureRepository measureRepo = ((CMeasureRepository) getRepositories().getRepositoryFor(CMeasure.class).get());
		Calendar currentDate = Calendar.getInstance();
		Date currentD = Date.from(currentDate.toInstant()); 
		currentDate.add(Calendar.DAY_OF_WEEK, 7);
		Date dateTo = Date.from(currentDate.toInstant());

		for(Map<String,Object> rqy: orderGoods) {
			// 0:god_id, 1:mee_id, 2:mee_coefficien, 3:rqy_quantity, 4:stk_quantity, 5:quantity_confirm
			BigDecimal scaled = ((BigDecimal)rqy.get("rqyQuantity")).add((BigDecimal)rqy.get("stkQuantity")).subtract((BigDecimal)rqy.get("quantityConfirm"));
			boolean isGreater = scaled.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) > 0;

			if (isGreater) {
				CGoods goodTo = (CGoods) rqy.get("godId");
				//only the default vendors
				if( goodTo.getDefaultVendor() == null ) {
					logger.trace("Липсва основен доставчик за тази стока: " +goodTo.getId());
					Map<String,Object> resultMessage = new HashMap<String,Object>();
					resultMessage.put("message", "Липсва основен доставчик за тази стока");
					resultMessage.put("messageCode", "noDefaultVendor");
					resultMessage.put("goods", goodTo);
					resultMessages.add(resultMessage);
					//continue;
				} else if( partnerToIns.equals( goodTo.getDefaultVendor() ) ) {
					logger.trace("Found goods from this vendor");
					CPriceList par = (CPriceList) rqy.get("priceList");
					//CPriceListRepository priceListRepo = ((CPriceListRepository) getRepositories().getRepositoryFor(CPriceList.class).get());
					//CPriceList par = priceListRepo.findFirstByParIdAndGodIdAndCompanyAndDeletedOrderByStartDateDesc(partnerToIns, goodTo, goodTo.getCompany(), false); //TODO and current date between start date and end date
					if( par == null ) {
						//throw new ReportException("Липсва ценова листа за този доставчик и тази стока: " + partnerToIns.toString() + "; " + goodTo.toString());
						logger.trace("Липсва ценова листа за този доставчик и тази стока: " + partnerToIns.getId() + ", " + goodTo.getId());
						Map<String,Object> resultMessage = new HashMap<String,Object>();
						resultMessage.put("message", "Липсва ценова листа за този доставчик и тази стока");
						resultMessage.put("messageCode", "noPriceList");
						resultMessage.put("partner", partnerToIns);
						resultMessage.put("goods", goodTo);
						resultMessages.add(resultMessage);
						//continue;
					}
					
					//if (par.getParId() != null) {
						//COrderRepository orderRepo = ((COrderRepository) getRepositories().getRepositoryFor(COrder.class).get());
						//COrder vOrrId = orderRepo.findFirstByOutCodeAndParIdAndStatusAndCompanyAndDeleted(organUnitToIns, partnerToIns, orderStatusCR, partnerToIns.getCompany(), false);
						if(vOrrId == null) { //check for existing order
							vOrrId = (COrder) rqy.get("order");
						}
						if(vOrrId == null) {
							logger.trace("Create new order");
							CCtCurrency vCurrency = null;
							if( par != null ) {
								vCurrency = par.getCurrency(); 
								if( par.getDoiId() != null && par.getDoiId().getCurrency() != null ) {
									vCurrency = par.getDoiId().getCurrency();
								}
							}
		
							vOrrId = new COrder(null, currentD, null, null, false, organUnitToIns.getCompany(), currentD, dateTo, partnerToIns, organUnitToIns, orderStatusCR, BigDecimal.ZERO, vCurrency, currentD, null);
							droolsSave(vOrrId);
							resultCOrders.add(vOrrId);
							logger.trace("Created COrder "+vOrrId.getId());
						} else {
							doUpdateOrder = true;
						}
						// 0:god_id, 1:mee_id, 2:mee_coefficien, 3:rqy_quantity, 4:stk_quantity, 5:quantity_confirm
						BigDecimal dQty = ((BigDecimal)rqy.get("quantityConfirm")).setScale(2, BigDecimal.ROUND_HALF_EVEN);
						if( dQty.compareTo( BigDecimal.ZERO ) > 0 ) {
							BigDecimal dRqy = (((BigDecimal)rqy.get("rqyQuantity")).setScale(2, BigDecimal.ROUND_HALF_EVEN));
							if( dQty.compareTo( dRqy ) >= 0) {
								rqy.put("quantityConfirm", dQty.subtract(dRqy));
								rqy.put("rqyQuantity", BigDecimal.ZERO);
							} else {
								rqy.put("rqyQuantity", dRqy.subtract(dQty));
								rqy.put("quantityConfirm", BigDecimal.ZERO);
							}
							dQty = ((BigDecimal)rqy.get("quantityConfirm")).setScale(2, BigDecimal.ROUND_HALF_EVEN);
							if( dQty.compareTo( BigDecimal.ZERO ) > 0 ) {
								BigDecimal stkQ = (((BigDecimal)rqy.get("stkQuantity")).setScale(2, BigDecimal.ROUND_HALF_EVEN));
								rqy.put("stkQuantity", stkQ.subtract(dQty));
								rqy.put("quantityConfirm", BigDecimal.ZERO);
							}
						}

						COrderDetail orderDetailTo = (COrderDetail) rqy.get("orderDetail");
						//COrderDetail orderDetailTo = orderDetailRepo.findFirstByOrrIdAndGodIdAndCompanyAndDeleted(vOrrId, goodTo, goodTo.getCompany(), false);
						
						BigDecimal measureCoef = BigDecimal.ONE;
						CMeasure measureTo = goodTo.getMeeId();
						if(par != null) {
							measureTo = par.getMeeId();
							if( par.getDoiId() != null && par.getDoiId().getMeasure() != null ) {
								measureTo = par.getDoiId().getMeasure();
							}
							measureCoef = measureTo.getCofficient();
							if( measureCoef == null ) {
								measureCoef = par.getMeeId().getCofficient();
							}
						}
						if( orderDetailTo == null ) {
							logger.trace("Create order detail");
							// 0:god_id, 1:mee_id, 2:mee_coefficien, 3:rqy_quantity, 4:stk_quantity, 5:quantity_confirm
							BigDecimal quantityIns = measureCoef.multiply(((BigDecimal)rqy.get("rqyQuantity")).add((BigDecimal)rqy.get("stkQuantity")));
							BigDecimal price = (BigDecimal) rqy.get("minPrice"); // TODO currency exchange conversion and measure conversion //BigDecimal.ZERO;
							CDeliveryOfferItem doiId = null;
							//date check
							if(par != null) {
								doiId = par.getDoiId();
								price = par.getBasePrice().multiply(exchangeRate(par.getCurrency(), vOrrId.getCurrency(), currentD.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));
								if( par.getDoiId() != null && par.getDoiId().getPrice() != null ) {
									price = par.getDoiId().getPrice();
								}
							}
							BigDecimal rqyQuantityIns = measureCoef.multiply((BigDecimal)rqy.get("rqyQuantity"));
							BigDecimal stkQuantityIns = measureCoef.multiply((BigDecimal)rqy.get("stkQuantity"));
							
							COrderDetail newCOrderDetail = new COrderDetail(null, currentD, null, null, false, partnerToIns.getCompany(), goodTo, vOrrId, quantityIns, price, measureTo, price, quantityIns, doiId, rqyQuantityIns, stkQuantityIns, null, null, null);
							droolsSave(newCOrderDetail);
							resultCOrderDetails.add(newCOrderDetail);
							logger.trace("Created COrderDetail "+newCOrderDetail.getId());
						} else {
							// 0:god_id, 1:mee_id, 2:mee_coefficien, 3:rqy_quantity, 4:stk_quantity, 5:quantity_confirm
							BigDecimal quantityUpdate = measureCoef.multiply(((BigDecimal)rqy.get("rqyQuantity")).add((BigDecimal)rqy.get("stkQuantity"))); 
							BigDecimal rqyQuantityUpdate = measureCoef.multiply((BigDecimal)rqy.get("rqyQuantity"));
							BigDecimal stkQuantityUpdate = measureCoef.multiply((BigDecimal)rqy.get("stkQuantity"));
							if( quantityUpdate.compareTo(orderDetailTo.getQuantity()) != 0 
									|| rqyQuantityUpdate.compareTo(orderDetailTo.getRqyQuantity()) != 0 
									|| stkQuantityUpdate.compareTo(orderDetailTo.getStkQuantity()) != 0 ) {
								logger.trace("Update order detail");
								orderDetailTo.setQuantity(quantityUpdate);
								orderDetailTo.setRqyQuantity(rqyQuantityUpdate);
								orderDetailTo.setStkQuantity(stkQuantityUpdate);
								droolsSave(orderDetailTo);
								resultCOrderDetails.add(orderDetailTo);
								logger.trace("Updated COrderDetail "+orderDetailTo.getId());
							}
						}
					//}
				}
			}
		}
		if( doUpdateOrder && vOrrId != null) {
			logger.trace("Update order");
			Calendar newTimeLimit = Calendar.getInstance();
			Calendar dateOrder = Calendar.getInstance();
			dateOrder.setTime(vOrrId.getDateOrr());
			Calendar timeLimit = Calendar.getInstance();
			timeLimit.setTime(vOrrId.getTimeLimit());
			newTimeLimit.add(Calendar.MILLISECOND,timeLimit.compareTo(dateOrder));

			vOrrId.setTimeLimit(Date.from(newTimeLimit.toInstant()));
			vOrrId.setDateOrr(currentD);	
			droolsSave(vOrrId);
			//resultCOrders.add(vOrrId);
			logger.trace("Updated COrder "+vOrrId.getId());
		}
		logger.trace("Finish makeOrder");
		return result;
	}

	public List<Object> modifyStocks(Long pdeyId, Long pOutCode){
		List<Object> result = new LinkedList<Object>();
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);

		QCDeliveryDetail cDeliveryDetail = QCDeliveryDetail.cDeliveryDetail;
		QCDelivery cDelivery = QCDelivery.cDelivery;
		QCMeasure cMeasure = QCMeasure.cMeasure;
		QCGoods cGoods = QCGoods.cGoods;
		QCStock cStock = QCStock.cStock;
		
		
		List<Tuple> pors = queryFactory.select(cStock.id, cDeliveryDetail.id, cDeliveryDetail.godId.id, cDeliveryDetail.quantity, cDeliveryDetail.meeId.id, cDeliveryDetail.price, 
				cDeliveryDetail.deyId.id, cDelivery.outCode.id, cStock.initialQuantity, cStock.quantity, cMeasure.cofficient, cGoods.meeId.id, cDeliveryDetail.cost, 
				cDelivery.exchangeRate, cDelivery.parId.id, cDeliveryDetail.orlId.id, cStock.reserveQuantity, cStock.ddlCost, cStock.godId.id, cStock.meeId.id, cGoods.nameBg)
			.from(cDeliveryDetail)
				.innerJoin(cDelivery).on(cDelivery.id.eq(cDeliveryDetail.deyId.id).and(cDelivery.deleted.eq(false)))
				.innerJoin(cMeasure).on(cMeasure.id.eq(cDeliveryDetail.meeId.id).and(cMeasure.deleted.eq(false)))
				.innerJoin(cStock).on(cStock.ddlId.id.eq(cDeliveryDetail.id).and(cStock.deleted.eq(false)))
				.innerJoin(cGoods).on(cGoods.id.eq(cDeliveryDetail.godId.id).and(cGoods.deleted.eq(false)))
			.where(cDeliveryDetail.deyId.id.eq(pdeyId)
				.and(cDeliveryDetail.deleted.eq(false))
			)
			.fetch();

		for(Tuple por: pors) {
			BigDecimal delta = por.get(cStock.initialQuantity).subtract(por.get(cStock.quantity)).multiply(por.get(cMeasure.cofficient)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
			//if good is changed and sales is done - do not allow good change
			if ( por.get(cDeliveryDetail.godId.id) != por.get(cStock.godId.id) && (delta.compareTo(BigDecimal.ZERO) > 0 || por.get(cStock.reserveQuantity).compareTo(BigDecimal.ZERO) != 0) ) {
				throw new ReportException("Не можете да промените стоката в скл. разписка, ако има продажби или резервация по тази стока. (стока: "+por.get(cGoods.nameBg)+", разходвано: "+delta+", резервирано: "+por.get(cStock.reserveQuantity)+")");
			}
			BigDecimal coef = BigDecimal.ZERO;

			if (por.get(cDeliveryDetail.meeId.id) == por.get(cGoods.meeId.id)) {
				coef = BigDecimal.ONE;
			} else {
				coef = converMeasuers(por.get(cDeliveryDetail.meeId.id), por.get(cGoods.meeId.id));
			}

			BigDecimal calc = (por.get(cDeliveryDetail.quantity).multiply(coef)).setScale(2, BigDecimal.ROUND_HALF_EVEN);

			if(delta.compareTo(calc) <= 0) {
				CStock stockTo = entityManager.getReference(CStock.class, por.get(cStock.id));

				BigDecimal quantity = calc.subtract(delta);
				BigDecimal newPrice1 = stockTo.getPrice1() == null ? por.get(cDeliveryDetail.price).multiply(por.get(cDelivery.exchangeRate)).divide(coef, 2, RoundingMode.HALF_UP) : stockTo.getPrice1();
				BigDecimal newCost = por.get(cDeliveryDetail.cost).multiply(por.get(cDelivery.exchangeRate)).divide(coef, 5, RoundingMode.HALF_UP);
				BigDecimal newCostQuantity = (por.get(cDeliveryDetail.quantity).subtract(delta)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
				BigDecimal newDdlCost = por.get(cDeliveryDetail.cost).multiply(por.get(cDelivery.exchangeRate)).divide(coef, 5, RoundingMode.HALF_UP);

				CGoods goodTo = entityManager.getReference(CGoods.class, por.get(cDeliveryDetail.godId.id));
				CMeasure measureToIns = entityManager.getReference(CMeasure.class, por.get(cGoods.meeId.id));
				CCcPartner partnerToIns = null;
				if( por.get(cDelivery.parId.id) != null ) {
					partnerToIns = entityManager.getReference(CCcPartner.class, por.get(cDelivery.parId.id));
				}
				CCcOrganizationUnit organUnitToIns = entityManager.getReference(CCcOrganizationUnit.class, por.get(cDelivery.outCode.id));

				stockTo.setGodId(goodTo);
				stockTo.setInitialQuantity(calc);
				stockTo.setQuantity(quantity);
				stockTo.setMeeId(measureToIns);
				stockTo.setPrice1(newPrice1);
				stockTo.setOutCode(organUnitToIns);
				stockTo.setCost(newCost);
				stockTo.setCostQuantity(newCostQuantity);
				stockTo.setDdlCost(newDdlCost);
				stockTo.setParId(partnerToIns);
				droolsSave(stockTo);
				result.add(stockTo);
				logger.trace("modifyStocks updated CStock "+stockTo.getId());

				if(por.get(cDeliveryDetail.orlId.id) != null)
				{
					COrderDetail ordTo = entityManager.getReference(COrderDetail.class, por.get(cDeliveryDetail.orlId.id));

					BigDecimal newDdlQuantity = ordTo.getDdlQuantity()
													.subtract(por.get(cStock.initialQuantity)
															.multiply(converMeasuers(por.get(cGoods.meeId.id), ordTo.getMeeId().getId() ))
													)
													.add(por.get(cDeliveryDetail.quantity)
															.multiply(converMeasuers(por.get(cDeliveryDetail.meeId.id), ordTo.getMeeId().getId() ))
													);

					ordTo.setDdlQuantity(newDdlQuantity);
					droolsSave(ordTo);
					result.add(ordTo);
				}

				BigDecimal newDiff = ((por.get(cDeliveryDetail.quantity).multiply(coef)).setScale(2, BigDecimal.ROUND_HALF_EVEN))
										.subtract(por.get(cStock.reserveQuantity)); //round(por.quantity*coef,2) - por.reserve_quantity)
				
				reserveReallocate(por.get(cStock.id), newDiff);
			} else {
				throw new ReportException("Разходваното количество за стока "+por.get(cGoods.nameBg)+" е по-голямо от заприходеното "+por.get(cDeliveryDetail.quantity)+" > "+delta);
			}
		}
		
		List<Tuple> newPors = queryFactory.select(cDeliveryDetail.id, cDeliveryDetail.godId.id, cDeliveryDetail.quantity, cDeliveryDetail.meeId.id, cDeliveryDetail.price, 
				cDeliveryDetail.deyId.id, cDelivery.outCode.id, cMeasure.cofficient, cGoods.meeId.id, cDeliveryDetail.cost, cDelivery.exchangeRate, cDelivery.parId.id,
				cDeliveryDetail.orlId.id)
			.from(cDeliveryDetail)
				.innerJoin(cDelivery).on(cDelivery.id.eq(cDeliveryDetail.deyId.id).and(cDelivery.deleted.eq(false)))
				.innerJoin(cMeasure).on(cMeasure.id.eq(cDeliveryDetail.meeId.id).and(cMeasure.deleted.eq(false)))
				.innerJoin(cGoods).on(cGoods.id.eq(cDeliveryDetail.godId.id).and(cGoods.deleted.eq(false)))
				.leftJoin(cStock).on(cDeliveryDetail.id.eq(cStock.ddlId.id).and(cStock.deleted.eq(false)))
			.where(cDelivery.id.eq(pdeyId)
				.and(cStock.id.isNull()) //simulates this: "and not exists (select 1 from stocks s where s.ddl_id=ddl.id"
				.and(cDeliveryDetail.deleted.eq(false))
			)
			.fetch();

		for(Tuple por: newPors) {
			CMeasure measureToIns = entityManager.getReference(CMeasure.class, por.get(cGoods.meeId.id));

			BigDecimal newCoef = (measureToIns.getCofficient()).divide(por.get(cMeasure.cofficient));
			BigDecimal calc = por.get(cDeliveryDetail.quantity).multiply(newCoef);
			BigDecimal price1To = por.get(cDeliveryDetail.price).divide(newCoef).multiply(por.get(cDelivery.exchangeRate));
			BigDecimal costTo = por.get(cDeliveryDetail.cost).divide(newCoef).multiply(por.get(cDelivery.exchangeRate));
			Calendar currentDate = Calendar.getInstance();
			Date currentD = Date.from(currentDate.toInstant()); 
			BigDecimal costQuanTo = por.get(cDeliveryDetail.quantity).multiply(newCoef);
			BigDecimal ddlCostTo = por.get(cDeliveryDetail.cost).divide(newCoef).multiply(por.get(cDelivery.exchangeRate));

			CCcPartner partnerToIns = null;
			if( por.get(cDelivery.parId.id) != null) {
				partnerToIns = entityManager.getReference(CCcPartner.class, por.get(cDelivery.parId.id));
			}
			CCcOrganizationUnit organUnitToIns = entityManager.getReference(CCcOrganizationUnit.class, por.get(cDelivery.outCode.id));
			CGoods goodTo = entityManager.getReference(CGoods.class, por.get(cDeliveryDetail.godId.id));				
			CMeasure meeTo = entityManager.getReference(CMeasure.class, por.get(cGoods.meeId.id));
			CDeliveryDetail ddlTo = entityManager.getReference(CDeliveryDetail.class, por.get(cDeliveryDetail.id));

			CStock newStock = new CStock(null, currentD, null, currentD, false, ddlTo.getCompany(), goodTo, ddlTo, calc, calc, meeTo, price1To, null, null, null, null,
					organUnitToIns, BigDecimal.ZERO, costTo, currentD, ddlCostTo, costQuanTo, partnerToIns, BigDecimal.ZERO,BigDecimal.ZERO);
			droolsSave(newStock);
			result.add(newStock);
			logger.trace("modifyStocks created CStock "+newStock.getId()+" for CDeliveryDetail "+ddlTo.getId());

			if(por.get(cDeliveryDetail.orlId.id) != null)
			{
				COrderDetail ordTo = entityManager.getReference(COrderDetail.class, por.get(cDeliveryDetail.orlId.id));

				BigDecimal newDdlQuantity = ordTo.getDdlQuantity().add(por.get(cDeliveryDetail.quantity).multiply( converMeasuers( por.get(cDeliveryDetail.meeId.id), ordTo.getMeeId().getId() ) ));

				ordTo.setDdlQuantity(newDdlQuantity);
				droolsSave(ordTo);
				result.add(ordTo);
			}

			BigDecimal newDiff = ((por.get(cDeliveryDetail.quantity).multiply(newCoef)).setScale(2, BigDecimal.ROUND_HALF_EVEN)); 
			
			reserveReallocate(newStock.getId(), newDiff);
		}

		List<Tuple> partnersIds = queryFactory.select(cStock.parId.id, cDelivery.parId.id, cStock.id)
			.from(cDeliveryDetail)
				.innerJoin(cDelivery).on(cDelivery.id.eq(cDeliveryDetail.deyId.id).and(cDelivery.deleted.eq(false)))
				.leftJoin(cStock).on(cStock.id.eq(cDeliveryDetail.stkId.id).and(cStock.deleted.eq(false)))
			.where(cDeliveryDetail.deyId.id.eq(pdeyId)
				.and(cStock.ddlId.id.eq(cDeliveryDetail.id))
				.and(cDeliveryDetail.deleted.eq(false))
			)
			.fetch();

		for(Tuple par: partnersIds){
			CStock stockTo = entityManager.getReference(CStock.class, par.get(cStock.id));

			CCcPartner partnerToIns = null;
			if( par.get(cStock.parId.id) != null ) {
				partnerToIns = entityManager.getReference(CCcPartner.class, par.get(cStock.parId.id));
			} else if( par.get(cDelivery.parId.id) != null ) {
				partnerToIns = entityManager.getReference(CCcPartner.class, par.get(cDelivery.parId.id));
			}

			stockTo.setParId(partnerToIns);
			droolsSave(stockTo);
			result.add(stockTo);
			logger.trace("modifyStocks updated CStock "+stockTo.getId()+" with new partner "+partnerToIns.getId());
		}

		CDelivery deyTo = (CDelivery) Hibernate.unproxy(entityManager.getReference(CDelivery.class, pdeyId)); //https://www.baeldung.com/hibernate-proxy-to-real-entity-object

		List<BigDecimal> newCostList = queryFactory.select((cDeliveryDetail.cost.multiply(cDeliveryDetail.quantity)).sum())
			.from(cDeliveryDetail)
			.where(cDeliveryDetail.deyId.id.eq(pdeyId)
				.and(cDeliveryDetail.deleted.eq(false))
			)
			.fetch();

		BigDecimal newCost = newCostList.get(0).setScale(2, BigDecimal.ROUND_HALF_EVEN);
		
		deyTo.setCost(newCost);
		droolsSave(deyTo);
		result.add(deyTo);

		return result;
	}

	public List<CReserveQuantity> reserveReallocate(Long pStockId, BigDecimal pDiff) {
		List<CReserveQuantity> result = new LinkedList<CReserveQuantity>();

		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);

		QCDeliveryDetail cDeliveryDetail = QCDeliveryDetail.cDeliveryDetail;
		QCDelivery cDelivery = QCDelivery.cDelivery;
		QCMeasure cMeasure = QCMeasure.cMeasure;
		QCGoods cGoods = QCGoods.cGoods;
		QCStock cStock = QCStock.cStock;

		List<Tuple> stk = queryFactory.select(cStock.id, cDeliveryDetail.varId)
			.from(cStock)
				.innerJoin(cDeliveryDetail).on(cDeliveryDetail.id.eq(cStock.ddlId.id).and(cDeliveryDetail.deleted.eq(false)))
			.where(cStock.id.eq(pStockId)
				.and(cStock.deleted.eq(false))
			)
			.fetch();

		BigDecimal vDiff = pDiff;
		CStock stockTo = entityManager.getReference(CStock.class, stk.get(0).get(cStock.id));
		CMeasure measureTo = stockTo.getMeeId();
		
		//при налично количество по-голямо от резервираното досега
		if(vDiff.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) > 0) {
			
			QCOffer cOffer = QCOffer.cOffer;
			QCOfferDetail cOfferDetail = QCOfferDetail.cOfferDetail;
			QCReserveQuantity cReserveQuantity = QCReserveQuantity.cReserveQuantity;
			QLoiOfferStatus cOfferStatus = QLoiOfferStatus.loiOfferStatus;

			//проверка за резервация без асоц. наличности
			//Todo coalesce(odl.var_id,-1)=coalesce(stk.var_id,-1)
			//find reserve quantity without stock
			List<Tuple> rqys = queryFactory.select(cMeasure.cofficient, cReserveQuantity.id, cReserveQuantity.meeId.id, cReserveQuantity.ofrId.id, 
					cMeasure.id, cOffer.id, cOfferDetail.id, cReserveQuantity.odlId.id, cOffer.status, //cOffer.outCode.id, 
					cReserveQuantity.outId.id, cReserveQuantity.godId.id, cReserveQuantity.term, cReserveQuantity.stkId.id, 
					cReserveQuantity.initialQuantity, cReserveQuantity.partner.id)
				.from(cReserveQuantity)
					.innerJoin(cMeasure).on(cMeasure.id.eq(cReserveQuantity.meeId.id).and(cMeasure.deleted.eq(false)))
					.innerJoin(cOffer).on(cOffer.id.eq(cReserveQuantity.ofrId.id).and(cOffer.deleted.eq(false)))
					.innerJoin(cOfferDetail).on(cOfferDetail.id.eq(cReserveQuantity.odlId.id).and(cOfferDetail.deleted.eq(false)))
					.innerJoin(cOfferStatus).on(cOfferStatus.id.eq(cOffer.status.id).and(cOfferStatus.deleted.eq(false)))
				.where(cOfferStatus.listOptionItemCode.in(LoiOfferStatus.OFFER_STATUS_RA,LoiOfferStatus.OFFER_STATUS_PI)
					//.and(cOffer.outCode.id.eq(stockTo.getOutCode().getId()))
					.and(cReserveQuantity.godId.id.eq(stockTo.getGodId().getId()))
					.and(cReserveQuantity.stkId.id.isNull())
					//and coalesce(odl.var_id,-1)=coalesce(stk.var_id,-1)
					.and(cReserveQuantity.deleted.eq(false))
				)
				.orderBy( cReserveQuantity.term.asc(), cReserveQuantity.stkId.id.desc()) //, cReserveQuantity.term
				.fetch();

			//ако има - добави резервацията към тази стока
			for(Tuple rqy: rqys) {
				CReserveQuantityRepository cReserveQuantityRepo = ((CReserveQuantityRepository) getRepositories().getRepositoryFor(CReserveQuantity.class).get());
				CReserveQuantity resQuan = entityManager.getReference(CReserveQuantity.class, rqy.get(cReserveQuantity.id));

				BigDecimal vCalc = vDiff.multiply(rqy.get(cMeasure.cofficient).divide(measureTo.getCofficient()));

				//намаляване на неналични резервирани количества
				if(rqy.get(cReserveQuantity.initialQuantity).compareTo(vCalc.setScale(2, BigDecimal.ROUND_HALF_EVEN)) <= 0) {
					vCalc = rqy.get(cReserveQuantity.initialQuantity);
					logger.trace("reserveReallocate delete CReserveQuantity "+resQuan.getId());
					cReserveQuantityRepo.delete(resQuan);
				} else {
					BigDecimal initQuantity = resQuan.getInitialQuantity().subtract(vCalc);
					logger.trace("reserveReallocate update CReserveQuantity InitialQuantity from "+resQuan.getInitialQuantity()+" to "+initQuantity);
					resQuan.setInitialQuantity(initQuantity);
					droolsSave(resQuan);
					result.add(resQuan);
				}

				List<Long> rqyId = queryFactory.select(cReserveQuantity.id)
					.from(cReserveQuantity)
					.where(cReserveQuantity.odlId.id.eq(rqy.get(cReserveQuantity.odlId.id))
						.and(cReserveQuantity.stkId.id.eq(stockTo.getId()))
						.and(cReserveQuantity.deleted.eq(false))
					)
					.fetch();

				//увеличаване на налични резервирани количества
				if(rqyId.size() == 0) {
					CCcPartner partnerToIns = null;
					if( rqy.get(cReserveQuantity.partner.id) != null) {
						partnerToIns = entityManager.getReference(CCcPartner.class, rqy.get(cReserveQuantity.partner.id));
					}
					CGoods goodTo = entityManager.getReference(CGoods.class, rqy.get(cReserveQuantity.godId.id));				
					COfferDetail odlTo = entityManager.getReference(COfferDetail.class, rqy.get(cReserveQuantity.odlId.id));
					CMeasure meeTo = entityManager.getReference(CMeasure.class, rqy.get(cReserveQuantity.meeId.id));
					COffer offTo = entityManager.getReference(COffer.class, rqy.get(cReserveQuantity.ofrId.id));
					CCcOrganizationUnit organUnitToIns = null;
					if( rqy.get(cReserveQuantity.outId.id) != null) {
						organUnitToIns = entityManager.getReference(CCcOrganizationUnit.class, rqy.get(cReserveQuantity.outId.id));
					}

					CReserveQuantity resQuantity = new CReserveQuantity(null, null, null, null, false, offTo.getCompany(), vCalc, vCalc, stockTo, partnerToIns, resQuan.getRetId(), rqy.get(cReserveQuantity.term), offTo, goodTo, odlTo, meeTo, organUnitToIns);
					logger.trace("reserveReallocate create CReserveQuantity");
					droolsSave(resQuantity);
					entityManager.refresh(stockTo);
					logger.trace("CStock: "+stockTo.getId()+"reserveQuantity: "+stockTo.getReserveQuantity()+" reserves count: "+(stockTo.getReserveQuantities() != null ? stockTo.getReserveQuantities().size() : "null"));
				} else {
					BigDecimal initQuantity = resQuan.getInitialQuantity().add(vCalc);
					BigDecimal quantity = resQuan.getQuantity().add(vCalc);
					logger.trace("reserveReallocate update CReserveQuantity InitialQuantity,Quantity from "+resQuan.getInitialQuantity()+","+resQuan.getQuantity()+" to "+initQuantity+","+quantity);
					resQuan.setInitialQuantity(initQuantity);
					resQuan.setQuantity(quantity);
					droolsSave(resQuan);
					result.add(resQuan);
				}

				vDiff = vDiff.subtract(vCalc.divide(rqy.get(cMeasure.cofficient).divide(measureTo.getCofficient())));

				if(vDiff.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) == 0) {
					break;
				}
			}
		//при налично количество по-малко от резервираното досега
		} else if( vDiff.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) < 0) {

			QCOffer cOffer = QCOffer.cOffer;
			QCOfferDetail cOfferDetail = QCOfferDetail.cOfferDetail;
			QCReserveQuantity cReserveQuantity = QCReserveQuantity.cReserveQuantity;
			QLoiOfferStatus cOfferStatus = QLoiOfferStatus.loiOfferStatus;

			//провери сегашните резервации към тази наличност
			//r,* 							 and coalesce(odl.var_id,-1)=coalesce(stk.var_id,-1)
			List<Tuple> rqys = queryFactory.select(cMeasure.cofficient, cReserveQuantity.id)
				.from(cReserveQuantity)
					.innerJoin(cMeasure).on(cMeasure.id.eq(cReserveQuantity.meeId.id).and(cMeasure.deleted.eq(false)))
					.innerJoin(cOffer).on(cOffer.id.eq(cReserveQuantity.ofrId.id).and(cOffer.deleted.eq(false)))
					.innerJoin(cOfferDetail).on(cOfferDetail.id.eq(cReserveQuantity.odlId.id).and(cOfferDetail.deleted.eq(false)))
					.innerJoin(cOfferStatus).on(cOfferStatus.id.eq(cOffer.status.id).and(cOfferStatus.deleted.eq(false)))
				.where(cOfferStatus.listOptionItemCode.in(LoiOfferStatus.OFFER_STATUS_RA,LoiOfferStatus.OFFER_STATUS_PI)
					.and(cReserveQuantity.quantity.gt(0))
					.and(cReserveQuantity.stkId.id.eq(stockTo.getId()))
					//and coalesce(odl.var_id,-1)=coalesce(stk.var_id,-1)
					.and(cReserveQuantity.deleted.eq(false))
				)
				.orderBy(cReserveQuantity.term.desc(), cReserveQuantity.stkId.id.desc()) //cReserveQuantity.term.desc(),
				.fetch();

			for(Tuple rqy: rqys) {
				BigDecimal vCalc = vDiff.negate().multiply(rqy.get(cMeasure.cofficient).divide(measureTo.getCofficient()));

				CReserveQuantityRepository cReserveQuantityRepo = ((CReserveQuantityRepository) getRepositories().getRepositoryFor(CReserveQuantity.class).get());
				CReserveQuantity resQuan = entityManager.getReference(CReserveQuantity.class, rqy.get(cReserveQuantity.id));

				//намаляване на налични резервирани количества
				if(resQuan.getInitialQuantity().compareTo(vCalc.setScale(2, BigDecimal.ROUND_HALF_EVEN)) <= 0) {
					vCalc = resQuan.getInitialQuantity();
					logger.trace("reserveReallocate delete CReserveQuantity "+resQuan.getId());
					cReserveQuantityRepo.delete(resQuan);
				} else {
					BigDecimal initQuantity = resQuan.getInitialQuantity().subtract(vCalc);
					BigDecimal quantity = resQuan.getQuantity().subtract(vCalc);
					logger.trace("reserveReallocate update CReserveQuantity InitialQuantity,Quantity from "+resQuan.getInitialQuantity()+","+resQuan.getQuantity()+" to "+initQuantity+","+quantity);
					resQuan.setInitialQuantity(initQuantity);
					resQuan.setQuantity(initQuantity);
					droolsSave(resQuan);
					result.add(resQuan);
				}
				
				List<Long> rqyId = queryFactory.select(cReserveQuantity.id)
					.from(cReserveQuantity)
					.where(cReserveQuantity.odlId.id.eq(resQuan.getOdlId().getId())
						.and(cReserveQuantity.stkId.id.isNull())
						.and(cReserveQuantity.deleted.eq(false))
					)
					.fetch();

				//увеличаване на неналични резервирани количества
				if(rqyId.size() == 0) {
					CReserveQuantity resQuantity = new CReserveQuantity(null, null, null, null, false, resQuan.getCompany(), vCalc, BigDecimal.ZERO, null, 
							resQuan.getPartner(), resQuan.getRetId(), resQuan.getTerm(), resQuan.getOfrId(), resQuan.getGodId(), resQuan.getOdlId(), resQuan.getMeeId(), 
							resQuan.getOutId());
					logger.trace("reserveReallocate create CReserveQuantity");
					droolsSave(resQuantity);
					result.add(resQuantity);
				} else {
					CReserveQuantity rqyQuan = entityManager.getReference(CReserveQuantity.class, rqyId.get(0));
					BigDecimal initQuantity = rqyQuan.getInitialQuantity().add(vCalc);
					logger.trace("reserveReallocate update CReserveQuantity InitialQuantity from "+rqyQuan.getInitialQuantity()+" to "+initQuantity);
					rqyQuan.setInitialQuantity(initQuantity);
					droolsSave(rqyQuan);
					result.add(resQuan);
				}

				vDiff = vDiff.subtract(vCalc.negate().divide(rqy.get(cMeasure.cofficient).divide(measureTo.getCofficient())));
				if(vDiff.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) == 0 ) {
					break;
				}
			}
		}

		//this should be handled by the Drools rule
//		vDiff = pDiff.subtract(vDiff);
//		if(!(vDiff.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) == 0)) {
//			BigDecimal newResQuantity = stockTo.getReserveQuantity().add(vDiff);
//			logger.trace("reserveReallocate update CStock ReserveQuantity from "+stockTo.getReserveQuantity()+" to "+newResQuantity);
//			stockTo.setReserveQuantity(newResQuantity);
//			trySave(stockTo);
//		}
		return result;
	}

	public List<CReserveQuantity> makeRequest(Long pOfrId, String pUser, String remark){
		List<CReserveQuantity> result = new LinkedList<CReserveQuantity>();
		SecUser user = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			user = ((SantaUser) authentication.getPrincipal()).getSecUser();
		}

		COffer pOffer = entityManager.getReference(COffer.class, pOfrId);
		if( pOffer.getStatus().getListOptionItemCode() == LoiOfferStatus.OFFER_STATUS_RA || pOffer.getStatus().getListOptionItemCode() == LoiOfferStatus.OFFER_STATUS_PI ) {
			throw new ReportException("Вече има създадена заявка за тази поръчка.");
		}
		if( pOffer.getStatus().getListOptionItemCode() == LoiOfferStatus.OFFER_STATUS_CA ) {
			throw new ReportException("Тази поръчка е отказана.");
		}
		//for each row of the offer allocate a reserve quantity
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QCOfferDetail cOfferDetail = QCOfferDetail.cOfferDetail;
		List<Tuple> pors = queryFactory.select(cOfferDetail.id, cOfferDetail.godId.id, cOfferDetail.quantity, cOfferDetail.meeId.id)
			.from(cOfferDetail)
			.where(cOfferDetail.ofrId.id.eq(pOfrId)
				.and(cOfferDetail.quantity.gt(BigDecimal.ZERO))
				.and(cOfferDetail.deleted.eq(false))
			)
			.fetch();

		for(Tuple por: pors) {
			result.addAll( reserveAllocate(pOfrId, por.get(cOfferDetail.godId.id), por.get(cOfferDetail.meeId.id), por.get(cOfferDetail.id)) );
		}
		//change offer status to RA, add remark for the change
		LoiOfferStatusRepository repo = ((LoiOfferStatusRepository) getRepositories().getRepositoryFor(LoiOfferStatus.class).get());
		offerTrack(pOfrId, pUser, repo.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiOfferStatus.OFFER_STATUS_RA, user.getCompany(), false), remark);
		return result;
	}
	
	public List<CReserveQuantity> reserveAllocate(Long pOfrId, Long pGodId, Long pMeeId, Long pOdlId){
		List<CReserveQuantity> result = new LinkedList<CReserveQuantity>();
		String currentUser = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}

		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		//offer_details odl, offers ofr, measures m, goods god
		QCOfferDetail cOfferDetail = QCOfferDetail.cOfferDetail;
		QCOffer cOffer = QCOffer.cOffer;
		QCMeasure cMeasure = QCMeasure.cMeasure;
		QCGoods cGoods = QCGoods.cGoods;
		QCStock cStock = QCStock.cStock;
		
		
		if (pGodId == null){
			return result;
		}

		BigDecimal vReserved;

		//get the offer detail row data
		List<Tuple> pors = queryFactory.select(cOfferDetail.id, cOfferDetail.godId.id, cOfferDetail.quantity, cOfferDetail.meeId.id, //cOffer.outCode.id, 
					cMeasure.cofficient, cOffer.partner.id, cOffer.endDate, cOffer.id, cGoods.nameBg, cOffer.outId.id)
			.from(cOfferDetail)
				.innerJoin(cOffer).on(cOffer.id.eq(cOfferDetail.ofrId.id).and(cOffer.deleted.eq(false)))
				.innerJoin(cMeasure).on(cMeasure.id.eq(cOfferDetail.meeId.id).and(cMeasure.deleted.eq(false)))
				.innerJoin(cGoods).on(cGoods.id.eq(cOfferDetail.godId.id).and(cGoods.deleted.eq(false)))
			.where(cOffer.id.eq(pOfrId).and(cOfferDetail.quantity.gt(BigDecimal.ZERO))
				.and(cOfferDetail.id.eq(pOdlId)).and(cGoods.id.eq(pGodId))
				.and(cOfferDetail.deleted.eq(false))
			)
			.fetch();

		for(Tuple por: pors) {
			vReserved = por.get(cOfferDetail.quantity); 
			//find stocks for the same goods where quantity is more than the reserved quantity
			List<Tuple> c2s = queryFactory.select(cStock.id, cStock.quantity, cStock.reserveQuantity, cStock.blockedQuantity, cMeasure.cofficient)
				.from(cStock)
					.innerJoin(cMeasure).on(cMeasure.id.eq(cStock.meeId.id).and(cMeasure.deleted.eq(false)))
				.where(cStock.godId.id.eq(por.get(cOfferDetail.godId.id))
					.and(cStock.quantity.subtract(cStock.reserveQuantity).gt(BigDecimal.ZERO))
					//.and(cStock.outCode.id.eq(por.get(cOffer.outCode.id)))
					.and(cStock.deleted.eq(false))
				)
				.orderBy(cStock.stockDate.asc())
				.fetch();
			

			for(Tuple c2: c2s) {
				BigDecimal coeff = c2.get(cMeasure.cofficient).divide(por.get(cMeasure.cofficient));
				BigDecimal calc = vReserved.multiply(coeff);
				BigDecimal quantity = c2.get(cStock.quantity).subtract( c2.get(cStock.reserveQuantity) ).subtract( c2.get(cStock.blockedQuantity) );

				//TODO null checks
				//BigDecimal quantity = c2.get(cStock.quantity.subtract(Expressions.cases().when(cStock.reserveQuantity.isNull()).then(BigDecimal.ZERO).otherwise(cStock.reserveQuantity)).subtract(cStock.blockedQuantity.coalesce(BigDecimal.ZERO)));

				if (quantity.compareTo(calc.setScale(2, BigDecimal.ROUND_HALF_EVEN)) < 0){
					 calc = quantity;
				}
				
				COffer offerToIns = entityManager.getReference(COffer.class, pOfrId);
				CStock stockTo = entityManager.getReference(CStock.class, c2.get(cStock.id));
				CGoods goodToIns = entityManager.getReference(CGoods.class, por.get(cOfferDetail.godId.id));
				COfferDetail offerDetailToIns = entityManager.getReference(COfferDetail.class, por.get(cOfferDetail.id));
				CMeasure measureToIns = entityManager.getReference(CMeasure.class, por.get(cOfferDetail.meeId.id));
				CCcPartner partnerToIns = null;
				if( por.get(cOffer.partner.id) != null ) {
					partnerToIns = entityManager.getReference(CCcPartner.class, por.get(cOffer.partner.id));
				}
				CCcOrganizationUnit organUnitToIns = null;
				if( por.get(cOffer.outId.id) != null) {
					organUnitToIns = entityManager.getReference(CCcOrganizationUnit.class, por.get(cOffer.outId.id));
				}
				
				CReserveQuantity resQuantity = new CReserveQuantity(null, null, null, null, false, offerToIns.getCompany(), 
					calc.divide(coeff), calc.divide(coeff), stockTo, partnerToIns, null, offerToIns.getEndDate(), offerToIns, goodToIns, offerDetailToIns, measureToIns, organUnitToIns);
				droolsSave(resQuantity);
				result.add(resQuantity);
				entityManager.refresh(stockTo);
				logger.trace("CStock: "+stockTo.getId()+"reserveQuantity: "+stockTo.getReserveQuantity()+" reserves count: "+(stockTo.getReserveQuantities() != null ? stockTo.getReserveQuantities().size() : "null"));

				//this should be handled by the Drools rule
//				// TODO coalesce(reserve_quantity,0)
//				stockTo.setReserveQuantity(calc.add(stockTo.getReserveQuantity()));	
//				trySave(stockTo);

				vReserved = vReserved.subtract(calc.divide(coeff));
				if (vReserved.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) == 0){
					break;
				}
			}

			if(vReserved.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) > 0) {
				//second loop: use blocked quantities if necessary:
				List<Tuple> c2q = queryFactory.select(cStock.id, cStock.quantity, cStock.reserveQuantity, cStock.blockedQuantity, cMeasure.cofficient)
					.from(cStock)
						.innerJoin(cMeasure).on(cMeasure.id.eq(cStock.meeId.id).and(cMeasure.deleted.eq(false)))
					.where(cStock.godId.id.eq(por.get(cOfferDetail.godId.id))
						.and(cStock.quantity.subtract(cStock.reserveQuantity).gt(BigDecimal.ZERO))
						//.and(cStock.outCode.id.eq(por.get(cOffer.outCode.id)))
						.and(cStock.deleted.eq(false))
					)
					.orderBy(cStock.stockDate.asc())
					.fetch();

				for(Tuple c: c2q) {
					BigDecimal coeff = c.get(cMeasure.cofficient.divide(por.get(cMeasure.cofficient)));
					BigDecimal calc = vReserved.multiply(coeff);
					BigDecimal quantity = c.get(cStock.quantity).subtract( c.get(cStock.reserveQuantity) );
					if (quantity.compareTo(calc.setScale(2, BigDecimal.ROUND_HALF_EVEN)) < 0){
						calc = quantity;
					}

					COffer offerToIns = entityManager.getReference(COffer.class, pOfrId);
					CStock stockTo = entityManager.getReference(CStock.class, c.get(cStock.id));
					CGoods goodToIns = entityManager.getReference(CGoods.class, por.get(cOfferDetail.godId.id));
					COfferDetail offerDetailToIns = entityManager.getReference(COfferDetail.class, por.get(cOfferDetail.id));
					CMeasure measureToIns = entityManager.getReference(CMeasure.class, por.get(cOfferDetail.meeId.id));
					CCcPartner partnerToIns = null;
					if( por.get(cOffer.partner.id) != null ) {
						partnerToIns = entityManager.getReference(CCcPartner.class, por.get(cOffer.partner.id));
					}
					CCcOrganizationUnit organUnitToIns = null;
					if( por.get(cOffer.outId.id) != null) {
						organUnitToIns = entityManager.getReference(CCcOrganizationUnit.class, por.get(cOffer.outId.id));
					}

					CReserveQuantity resQuantity = new CReserveQuantity(null, null, null, null, false, offerToIns.getCompany(), 
						calc.divide(coeff), calc.divide(coeff), stockTo, partnerToIns, null, offerToIns.getEndDate(), offerToIns, goodToIns, offerDetailToIns, measureToIns, organUnitToIns);
					droolsSave(resQuantity);
					result.add(resQuantity);
					entityManager.refresh(stockTo);
					logger.trace("CStock: "+stockTo.getId()+"reserveQuantity: "+stockTo.getReserveQuantity()+" reserves count: "+(stockTo.getReserveQuantities() != null ? stockTo.getReserveQuantities().size() : "null"));

					//this should be handled by the Drools rule
//					// TODO coalesce(reserve_quantity,0)
//					stockTo.setReserveQuantity(calc.add(stockTo.getReserveQuantity()));	
//					trySave(stockTo);

					vReserved = vReserved.subtract(calc.divide(coeff));
					if (vReserved.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) == 0) {
						break;
					}
				}
			}
			//if not satisfied or not fully satisfied:
			if ( vReserved.setScale(2, BigDecimal.ROUND_HALF_EVEN).compareTo(BigDecimal.ZERO) > 0){
				COffer offerToIns = entityManager.getReference(COffer.class, pOfrId);
				CGoods goodToIns = entityManager.getReference(CGoods.class, por.get(cOfferDetail.godId.id));
				COfferDetail offerDetailToIns = entityManager.getReference(COfferDetail.class, por.get(cOfferDetail.id));
				CMeasure measureToIns = entityManager.getReference(CMeasure.class, por.get(cOfferDetail.meeId.id));
				CCcPartner partnerToIns = null;
				if( por.get(cOffer.partner.id) != null) {
					partnerToIns = entityManager.getReference(CCcPartner.class, por.get(cOffer.partner.id));
				}
				CCcOrganizationUnit organUnitToIns = null;
				if( por.get(cOffer.outId.id) != null) {
					organUnitToIns = entityManager.getReference(CCcOrganizationUnit.class, por.get(cOffer.outId.id));
				}

				CReserveQuantity resQuantity = new CReserveQuantity(null, null, null, null, false, offerToIns.getCompany(), 
					vReserved, BigDecimal.ZERO, null, partnerToIns, null, offerToIns.getEndDate(), offerToIns, goodToIns, offerDetailToIns, measureToIns, organUnitToIns);
				droolsSave(resQuantity);
				result.add(resQuantity);
				logger.info("Недостатъчно количество за резервиране за "+por.get(cGoods.nameBg)+": недостиг "+vReserved);
			}
		}
		
		return result;
	}
	
	public COffer offerTrack(Long offrId, String pUser, LoiOfferStatus status, String remark) {
		COffer toUpdate = entityManager.find(COffer.class, offrId); // https://www.baeldung.com/jpa-entity-manager-get-reference

		// currentUser
		COfferStatus offerStat = new COfferStatus(toUpdate, toUpdate.getStatus(), status, remark);
		droolsSave(offerStat);

		toUpdate.setStatus(status);
		droolsSave(toUpdate);
		
		return toUpdate;
	}
	
	public List<Map<String,Object>> getOfferState(Long pCOfferId) {
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QCOfferDetail cOfferDetail = QCOfferDetail.cOfferDetail;
		QCGoods cGoods = QCGoods.cGoods;
		QCReserveQuantity cReserveQuantity = QCReserveQuantity.cReserveQuantity;
		QCOrderDetail cOrderDetail = QCOrderDetail.cOrderDetail;
		QAllocationProxy offerAllocationProxy = QAllocationProxy.allocationProxy;
		QAllocationType offerToOrderAllocationType = QAllocationType.allocationType;
		List<Tuple> detailStates = queryFactory.select(cOfferDetail.godId.id, cGoods.code, cOfferDetail.quantity, cReserveQuantity.quantity, 
						cOfferDetail.sdlQuantity, offerAllocationProxy.totalAllocatedQuantity)
				.from(cOfferDetail)
					.innerJoin(cGoods).on(cGoods.id.eq(cOfferDetail.godId.id).and(cGoods.deleted.eq(false)))
					.leftJoin(cReserveQuantity).on(
						cReserveQuantity.odlId.id.eq(cOfferDetail.id)
						.and(cReserveQuantity.quantity.gt(0))
						.and(cReserveQuantity.deleted.eq(false))
					)
					.innerJoin(offerAllocationProxy).on(
							offerAllocationProxy.allocationOrigin.id.eq(cOfferDetail.id)
							.and(offerAllocationProxy.deleted.eq(false))
					)
					.innerJoin(offerToOrderAllocationType).on(
						offerToOrderAllocationType.id.eq(offerAllocationProxy.allocationType.id)
						.and(offerToOrderAllocationType.producer.eq("cOfferDetails"))
						.and(offerToOrderAllocationType.consumer.eq("cOrderDetails"))
						.and(offerToOrderAllocationType.deleted.eq(false))
					)
					//.leftJoin(cOrderDetail).on(
						
					//)
				.where(cOfferDetail.ofrId.id.eq(pCOfferId)
					.and(cOfferDetail.deleted.eq(false))
				)
				.fetch();
		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Tuple detailState : detailStates) {
			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("goodsId", detailState.get(cOfferDetail.godId.id));
			resultRow.put("goodsCode", detailState.get(cGoods.code));
			resultRow.put("quantity", detailState.get(cOfferDetail.quantity));
			resultRow.put("reservedQuantity", detailState.get(cReserveQuantity.quantity));
			resultRow.put("orderedQuantity", detailState.get(offerAllocationProxy.totalAllocatedQuantity));
			resultRow.put("soldQuantity", detailState.get(cOfferDetail.sdlQuantity));
			result.add(resultRow);
		}
		return result;
	}

	public Map<String,Object> makeSale(Long offerId, String pUser, String pRemark){
		Map<String,Object> result = new HashMap<String,Object>();
		List<Object> resultCSaleDetails = new LinkedList<Object>();

		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);

		COffer offer = entityManager.getReference(COffer.class, offerId);
		LoiOfferStatus offerStatus = offer.getStatus();
		BigDecimal vVat = offer.getVat();

		if(offerStatus.getListOptionItemCode() != LoiOfferStatus.OFFER_STATUS_RA && offerStatus.getListOptionItemCode() != LoiOfferStatus.OFFER_STATUS_PI) {
			throw new ReportException("Първо се създава заявка, след това Стокова разписка.");
		}
		if(offerStatus.getListOptionItemCode() == LoiOfferStatus.OFFER_STATUS_SA) {
			throw new ReportException("Вече има създадена Стокова разписка по тази заявка.");
		}
			
		CCcOrganizationUnit vOutCode = offer.getOutCode();
		CCcOrganizationUnit vOutId = offer.getOutId();
		CCcPartner vPar = offer.getPartner();
		Calendar currentD = Calendar.getInstance();
		Date currentDate = Date.from(currentD.toInstant()); 
		String status = null;
		LoiTypeDoc typeDoc = null;

		if (vOutId == null) {
			status = "1";
		} else {
			status = "2";
		}

		CCtCurrencyRepository repo = ((CCtCurrencyRepository) getRepositories().getRepositoryFor(CCtCurrency.class).get());
		CCtCurrency saleCurrency = repo.findFirstByCodeAndCompanyAndDeleted("BGN", offer.getCompany(), false);

		LoiTypeDocRepository tdRepo = ((LoiTypeDocRepository) getRepositories().getRepositoryFor(LoiTypeDoc.class).get());
		if (vOutId == null) {
			typeDoc = tdRepo.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiTypeDoc.TYPE_DOC_SR, offer.getCompany(), false);
		} else {
			typeDoc = tdRepo.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiTypeDoc.TYPE_DOC_PP, offer.getCompany(), false);
		}

		//TODO there is a trigger when new sale is INSERT. Need information about acc_postings
		/*if v_date is not null then
		if exists (select 1 from acc_postings where v_date between date_from and date_to and out_code=v_outcode) then
			raise exception 'Не може да се създаде документ за тази дата, защото данните за периода вече са осчетоводени.';
		end if;*/
		LoiPaymentTypeRepository ptRepo = ((LoiPaymentTypeRepository) getRepositories().getRepositoryFor(LoiPaymentType.class).get());
		LoiPaymentType pt = ptRepo.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPaymentType.PAYMENT_TYPE_CASH, offer.getCompany(), false);
		
		CSale newSale = new CSale(null, currentDate, null, null, false, null, currentDate, pt, currentDate, offer.getDiscount(), 
				vOutCode, pUser,vPar,null,status, BigDecimal.ZERO, offer.getVat(),saleCurrency, BigDecimal.ONE,null, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, 0, typeDoc, null, BigDecimal.ZERO,vOutId, null, offer, null, null, null, null, 
				null, null, null);
		droolsSave(newSale);
		result.put("CSale", newSale);

		//add goods
		QCOfferDetail cOfferDetail = QCOfferDetail.cOfferDetail;
		QCOffer cOffer = QCOffer.cOffer;
		QCReserveQuantity cReserveQuantity = QCReserveQuantity.cReserveQuantity;
		QCMeasure cMeasure = QCMeasure.cMeasure;
		QCMeasure measure2 = new QCMeasure("measure2");
		QCGoods cGoods = QCGoods.cGoods;
		QCStock cStock = QCStock.cStock;
		QCDeliveryDetail cDeliveryDetail = QCDeliveryDetail.cDeliveryDetail;

		List<Tuple> pors = queryFactory.select(cOfferDetail.id, cOfferDetail.godId.id, cOfferDetail.quantity, cOfferDetail.meeId.id, 
				cOfferDetail.price1, cMeasure.cofficient, cReserveQuantity.id, cReserveQuantity.quantity, cReserveQuantity.meeId.id, 
				cReserveQuantity.stkId.id, measure2.cofficient, cOffer.currency.id, cOffer.discount, cGoods.nameBg)
		.from(cOfferDetail)
			.innerJoin(cMeasure).on(cMeasure.id.eq(cOfferDetail.meeId.id).and(cMeasure.deleted.eq(false)))
			.innerJoin(cOffer).on(
				cOffer.id.eq(cOfferDetail.ofrId.id)
				.and(cOffer.deleted.eq(false))
			)
			.innerJoin(cReserveQuantity).on(
				cOfferDetail.id.eq(cReserveQuantity.odlId.id)
				.and(cReserveQuantity.deleted.eq(false))
			)
			.innerJoin(measure2).on(measure2.id.eq(cReserveQuantity.meeId.id).and(measure2.deleted.eq(false)))
			.innerJoin(cGoods).on(cGoods.id.eq(cOfferDetail.godId.id).and(cGoods.deleted.eq(false)))
		.where(cOffer.id.eq(offerId)
			.and(cOfferDetail.godId.id.isNotNull())
			.and(cOfferDetail.deleted.eq(false))
		)
		.fetch();
		
		BigDecimal vSum = BigDecimal.ZERO;
		BigDecimal vRate = exchangeRate(offer.getCurrency(), null, currentD.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

		for(Tuple por: pors) {
			BigDecimal calc = BigDecimal.ZERO;
			if (por.get(cOfferDetail.meeId.id) == por.get(cReserveQuantity.meeId.id)) {
				calc = por.get(cOfferDetail.quantity);
			} else {
				calc = por.get(cOfferDetail.quantity).multiply(por.get(measure2.cofficient)).divide(por.get(cMeasure.cofficient));
			}

			//Todo 
			if(por.get(cReserveQuantity.stkId.id) == null) {
				throw new ReportException(String.format("Не достатъчна наличност на стока. Моля, заприходете и след това генерирайте протокол стока %s, кол. в оферта %s,  calc %s", por.get(cGoods.nameBg), por.get(cOfferDetail.quantity),  calc));
			}

			//TODO vStkQuant is never used in make_sale. Chek if needed
			/*BigDecimal stocks = query.select(cMeasure.cofficient)
			.from(cStock)
			.innerJoin(cMeasure).on(cMeasure.id.eq(cStock.meeId.id))
			.where(cStock.id.eq(por.get(cReserveQuantity.stkId.id)))
			.fetchFirst();
			
			BigDecimal vStkQuant = stocks.divide(por.get(measure2.cofficient)).multiply(por.get(cReserveQuantity.quantity));*/
			
			List<Tuple> stos = queryFactory.select(cStock.id, cDeliveryDetail.serialNumber, cDeliveryDetail.batch )
				.from(cStock)
					.innerJoin(cDeliveryDetail).on(cDeliveryDetail.id.eq(cStock.ddlId.id)
						.and(cDeliveryDetail.deleted.eq(false))
					)
				.where(cStock.id.eq(por.get(cReserveQuantity.stkId.id))
					.and(cStock.deleted.eq(false))
				)
				.fetch();

			for (Tuple sto: stos) {
				CStock vCStock = entityManager.getReference(CStock.class, por.get(cReserveQuantity.stkId.id));
				CMeasure vCMeasure = entityManager.getReference(CMeasure.class, por.get(cReserveQuantity.meeId.id));
				COfferDetail vCOfferDetail = entityManager.getReference(COfferDetail.class, por.get(cOfferDetail.id));
	
				BigDecimal vat = por.get(cOffer.vat);
				if(vat == null) {
					vat = BigDecimal.ZERO;
				}
				BigDecimal price = por.get(cOfferDetail.price1).multiply(vRate);
				BigDecimal priceVat = por.get(cOfferDetail.price1).multiply((new BigDecimal(100).add(vat).divide(new BigDecimal(100))).multiply(vRate));
				BigDecimal totalWhtVat = por.get(cOfferDetail.price1).multiply(por.get(cOfferDetail.quantity)).multiply(vRate);
				BigDecimal totalVat = por.get(cOfferDetail.price1).multiply(por.get(cOfferDetail.quantity)).multiply(vRate).multiply(vat).divide(new BigDecimal(100));
				BigDecimal total = por.get(cOfferDetail.price1).multiply(por.get(cOfferDetail.quantity)).multiply(vRate).multiply(new BigDecimal(100).add(vat)).divide(new BigDecimal(100));
	
				CSaleDetail newSaleDetail = new CSaleDetail(null, currentDate, null, null, false, null, vCStock, 
						por.get(cReserveQuantity.quantity), sto.get(cDeliveryDetail.serialNumber), sto.get(cDeliveryDetail.batch), 
						vCMeasure, price, BigDecimal.ZERO,vat,priceVat,totalWhtVat,totalVat,  total, saleCurrency,
						vRate, null, newSale, null, vCOfferDetail, null);
				droolsSave(newSaleDetail);
				resultCSaleDetails.add(newSaleDetail);
			}

			vSum = vSum.add(por.get(cOfferDetail.price1).multiply(por.get(cReserveQuantity.quantity)).multiply(vRate));
		} 

		//add services
		List<Tuple> sees = queryFactory.select(cOfferDetail.id, cOfferDetail.seeId.id, cOfferDetail.quantity, 
					cOfferDetail.meeId.id, cOfferDetail.price1, cOffer.currency.id, cOffer.discount, cOffer.vat)
			.from(cOfferDetail)
				.innerJoin(cOffer).on(cOffer.id.eq(cOfferDetail.ofrId.id)
					.and(cOffer.deleted.eq(false))
				)
			.where(cOffer.id.eq(offerId)
				.and(cOfferDetail.deleted.eq(false))
				.and(cOfferDetail.seeId.id.isNotNull())
			)
			.fetch();

		for(Tuple see: sees){
			CService vCService = entityManager.getReference(CService.class, see.get(cOfferDetail.seeId.id));
			CMeasure vCMeasure = entityManager.getReference(CMeasure.class, see.get(cOfferDetail.meeId.id));
			COfferDetail vCOfferDetail = entityManager.getReference(COfferDetail.class, see.get(cOfferDetail.id));

			BigDecimal price = see.get(cOfferDetail.price1).multiply(vRate);
			BigDecimal priceVat = see.get(cOfferDetail.price1).multiply((new BigDecimal(100).add(see.get(cOffer.vat)).divide(new BigDecimal(100))).multiply(vRate));
			BigDecimal totalWhtVat = see.get(cOfferDetail.price1).multiply(see.get(cOfferDetail.quantity)).multiply(vRate);
			BigDecimal totalVat = see.get(cOfferDetail.price1).multiply(see.get(cOfferDetail.quantity)).multiply(vRate).multiply(see.get(cOffer.vat)).divide(new BigDecimal(100));
			BigDecimal total = see.get(cOfferDetail.price1).multiply(see.get(cOfferDetail.quantity)).multiply(vRate).multiply(new BigDecimal(100).add(see.get(cOffer.vat))).divide(new BigDecimal(100));

			CSaleDetail newSaleDetail = new CSaleDetail(null, currentDate, null, null, false, null, null, see.get(cOfferDetail.quantity), 
					null, null, vCMeasure, price, BigDecimal.ZERO,see.get(cOffer.vat),priceVat,totalWhtVat,totalVat, total, saleCurrency,
					vRate,null, newSale, vCService, vCOfferDetail, null);
			droolsSave(newSaleDetail);
			resultCSaleDetails.add(newSaleDetail);

			vSum = vSum.add(see.get(cOfferDetail.price1).multiply(see.get(cOfferDetail.quantity)).multiply(vRate));
		}

		//TODO do we need this update or the rules handle it?
		BigDecimal vDan = BigDecimal.ZERO;
		if (vVat.compareTo(BigDecimal.ZERO) != 0 ) {
			vDan = vSum;
		}

		BigDecimal updatedDanOs = vDan.multiply(new BigDecimal(100).subtract(newSale.getDiscount())).divide(new BigDecimal(100));
		BigDecimal updatedEndSum = vSum.multiply(new BigDecimal(100).subtract(newSale.getDiscount())).divide(new BigDecimal(100));
		BigDecimal updatedVatto = vDan.multiply(newSale.getVat()).divide(new BigDecimal(100));
		BigDecimal updatedTotal = vSum.multiply(new BigDecimal(100).subtract(newSale.getDiscount()))
				.add(vDan.multiply(newSale.getVat().divide(new BigDecimal(100)).multiply(new BigDecimal(100).subtract(newSale.getDiscount())).divide(new BigDecimal(100))));

		newSale.setSum(vSum);
		newSale.setOblSum(vDan);
		newSale.setDanOsnova(updatedDanOs);
		newSale.setEndSum(updatedEndSum);
		newSale.setVatto(updatedVatto);
		newSale.setTotal(updatedTotal);
		droolsSave(newSale);

		SecUser user = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			user = ((SantaUser) authentication.getPrincipal()).getSecUser();
		}
		LoiOfferStatusRepository repoOS = ((LoiOfferStatusRepository) getRepositories().getRepositoryFor(LoiOfferStatus.class).get());
		offerTrack(offerId, pUser, repoOS.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiOfferStatus.OFFER_STATUS_SA, user.getCompany(), false), pRemark);

		result.put("CSaleDetail", resultCSaleDetails);
		return result;
	}
	
	//TODO change this to be done by the rules when the "posted" is set to "P" and the front to save the "P" in posted instead of calling this endpoint
//	CREATE FUNCTION post_to_bridge(p_outcode character varying, p_user character varying, p_start_date date, p_end_date date, p_flags character varying) RETURNS integer
	public Map<String,Object> postToBridge(String pOutCode, String pUser, LocalDate pStartDate, LocalDate pEndDate, String pFlags){
//DECLARE
//    v_rec       record;
//    v_cnt       integer := 0;
//    v_rows      integer;
//    v_apg_id    integer;
//    v_outcode   varchar(20);
//    v_by_good_types varchar(1);
//BEGIN
		Map<String,Object> result = new HashMap<String,Object>();
		List<Object> errors = new LinkedList<Object>();
		long vCnt = 0;
		List<Tuple> vRows;
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QCCcOrganizationUnit qCCcOrganizationUnit = QCCcOrganizationUnit.cCcOrganizationUnit;
		QCDelivery qCDelivery = QCDelivery.cDelivery;
		QFBreTransition qFBreTransition = QFBreTransition.fBreTransition;
		QCDeliveryDetail qCDeliveryDetail = QCDeliveryDetail.cDeliveryDetail;
		QCGoods qCGoods = QCGoods.cGoods;
		QLoiGoodsType qLoiGoodsType = QLoiGoodsType.loiGoodsType;
		QSecUser qSecUser = QSecUser.secUser;
		QManagedCompany qManagedCompany = QManagedCompany.managedCompany;
		QFCtTransitionType qFCtTransitionType = QFCtTransitionType.fCtTransitionType;
		QCSale qCSale = QCSale.cSale;
		QCSaleDetail qCSaleDetail = QCSaleDetail.cSaleDetail;
		QCStock qCStock = QCStock.cStock;
		QLoiTypeDoc qLoiTypeDoc = QLoiTypeDoc.loiTypeDoc;
		QVendorInvoiceRow qVendorInvoiceRow = QVendorInvoiceRow.vendorInvoiceRow;
		QAllocationRecord qAllocationRecord = QAllocationRecord.allocationRecord;
		QAllocationProxy qAllocationProxy = QAllocationProxy.allocationProxy;
		QAllocationProxy qAllocationProducer = new QAllocationProxy("allocationProducer");
		LoiBreTransitionResultRepository loiBreTransitionResultRepository = ((LoiBreTransitionResultRepository) getRepositories().getRepositoryFor(LoiBreTransitionResult.class).get());
//    v_apg_id := costs.calculate_all(p_start_date, p_end_date, p_outcode, p_user);
//    v_outcode := p_outcode||'%';
		String vOutCode = pOutCode.concat("%");
//
//    select sp_val into v_by_good_types from register.cfg_parameters where sp_name='ACC_BY_GOOD_TYPES' and out_code=p_outcode;
//    if v_by_good_types is null then
//        v_by_good_types := 'N';
//    end if;
		String vByGoodTypes = "Y";
		
//    -- Deliveries with invoice
//    if position('D' in p_flags ) != 0 then -- post deliveries
		if(pFlags.contains("D")) {
//        -- set status to P - processing. This will lock rows and prevent others to update them in concurrency.
//        -- After this posted will be set to Y - the final status
//	        update deliveries set posted='P'
//          where posted='N' and out_code like v_outcode
//               and dey_date between p_start_date and p_end_date ;
			queryFactory.update(qCDelivery).set(qCDelivery.posted, "P")
				.where(
					qCDelivery.posted.eq("N")
					.and(qCDelivery.deleted.eq(false))
					.and(JPAExpressions.select(qCCcOrganizationUnit.code)
						.from(qCCcOrganizationUnit)
						.where(qCCcOrganizationUnit.id.eq(qCDelivery.outCode.id).and(qCCcOrganizationUnit.deleted.eq(false)))
						.eq(pOutCode) //TODO hierarchy is not implemented
					)
					.and(qCDelivery.deyDate.between(
							Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), 
							Date.from(pEndDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) 
						)
					)
				)
				.execute();

//        -- Cost of deliveries by good_type
//        if v_by_good_types = 'N' then
			if(vByGoodTypes.equals("N")) {
//	            insert into register.bre_transitions( id, out_code, post_date, module, tte_code,
//              ref_no, ref_date, amount_total, amount_do, amount_vat,
//              cuy_code, cuy_rate, date_created, user_created, descr,
//              cc_out_id, cc_par_id, result, cost)
//	            select nextval('register.trn_seq'), dey.out_code, dey_date, 'grasp', ddl.tte_code,
//              dey_number, dey_date, 0, 0, 0,
//              currency, exchange_rate, localtimestamp, p_user, 'Заприхождаване на стоки/материали/полуфабрикати',
//              out.id, dey.par_id, -1, ddl.cost
				vRows = queryFactory
					.select(qCDeliveryDetail.cost.multiply(qCDeliveryDetail.quantity).sum().as("cost"), qCDelivery.id,
							qCDelivery.outCode.id, qCDelivery.deyDate, qFCtTransitionType.id, qCDelivery.deyNumber, qCDelivery.deyDate, 
							qCDelivery.currency.id, qCDelivery.exchangeRate, qCCcOrganizationUnit, qCDelivery.parId.id
						)
//		                    from deliveries dey JOIN register.cc_organization_units out ON dey.out_code=out.code JOIN
//		                    (select dey_id, case god.good_type  when 'GD' then '50-12' when 'BD' then '50-12' when 'SM' then '50-13' when 'RM' then '50-11' end as tte_code, sum(ddl.cost*quantity) as cost
//		            from deliveries d0, delivery_details ddl, goods god where d0.posted='P' and d0.out_code like v_outcode
//		                            and d0.dey_date between p_start_date and p_end_date and ddl.god_id=god.id group by 1,2) ddl ON dey.id=ddl.dey_id
//		                    where dey.posted='P' and dey.out_code like v_outcode
//		                            and dey.dey_date between p_start_date and p_end_date
//		                            and ddl.dey_id=dey.id;
					.from(qCDelivery)
					.innerJoin(qCCcOrganizationUnit).on(qCCcOrganizationUnit.id.eq(qCDelivery.outCode.id).and(qCCcOrganizationUnit.deleted.eq(false)))
					.innerJoin(qCDeliveryDetail).on(qCDeliveryDetail.deyId.id.eq(qCDelivery.id).and(qCDeliveryDetail.deleted.eq(false)))
					.innerJoin(qCGoods).on(qCGoods.id.eq(qCDeliveryDetail.godId.id).and(qCGoods.deleted.eq(false)))
					.innerJoin(qLoiGoodsType).on(qLoiGoodsType.id.eq(qCGoods.goodType.id).and(qLoiGoodsType.deleted.eq(false)))
					.innerJoin(qFCtTransitionType).on(qFCtTransitionType.code.eq(new CaseBuilder()
								.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_GD)).then("50-12") 
								.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_BD)).then("50-12")
								.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_SM)).then("50-13")
								.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_RM)).then("50-11")
								.otherwise("50-00") //TODO what about VR?
							)
							.and(qFCtTransitionType.deleted.eq(false))
						)
					.where(qCDelivery.posted.eq("P")
						.and(qCDelivery.deleted.eq(false))
						.and(qCDelivery.outCode.code.like(vOutCode))
						.and(qCDelivery.deyDate.between(
								Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), 
								Date.from(pEndDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) 
							)
						)
					)
					.groupBy(qCDelivery.id, qCDelivery.outCode.id, qCDelivery.deyDate, qFCtTransitionType.id, qCDelivery.deyNumber, qCDelivery.deyDate, 
						qCDelivery.currency.id, qCDelivery.exchangeRate,  qCCcOrganizationUnit, qCDelivery.parId.id)
					.fetch();
				for(Tuple row: vRows) { 
//					BigDecimal invoiceCost = queryFactory.select(qVendorInvoiceRow.priceRate.sum())
//							.from(qCDelivery)
//							.where(qCDelivery.id.eq(row.get(qCDelivery.id)))
//							.fetchFirst();
					if(row.get(0,BigDecimal.class).compareTo(BigDecimal.ZERO) <= 0) {
						errors.add("Складова разписка "+row.get(qCDelivery.deyNumber).toString()+": Не може да се прехвърлят документи с нулева себестойност");
					} else {
						CCcOrganizationUnit cCcOrganizationUnit = entityManager.getReference(CCcOrganizationUnit.class, row.get(qCDelivery.outCode.id));
						CCtCurrency cCtCurrency = entityManager.getReference(CCtCurrency.class, row.get(qCDelivery.currency.id));
						FCtTransitionType fCtTransitionType = entityManager.getReference(FCtTransitionType.class, row.get(qFCtTransitionType.id));
						CCcPartner cCcPartner = entityManager.getReference(CCcPartner.class, row.get(qCDelivery.parId.id));
						LoiBreTransitionResult status = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_WAITING, cCcOrganizationUnit.getCompany(), false);
						FBreTransition breTrans = new FBreTransition(null,null,null,null,false,null,cCcOrganizationUnit,
								row.get(qCDelivery.deyDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"grasp",
								fCtTransitionType,row.get(qCDelivery.deyNumber).toString(),row.get(qCDelivery.deyDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
								null,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,cCtCurrency,row.get(qCDelivery.exchangeRate),
								null,"Заприхождаване на стоки/материали/полуфабрикати",status,row.get(0,BigDecimal.class),
								null,null,null,null,null,null,null,null,null,null,cCcPartner,null,null,
								cCcOrganizationUnit,null,null,null);
						
						droolsSave(breTrans);

						CDelivery delivery = entityManager.getReference(CDelivery.class, row.get(qCDelivery.id));
						delivery.setPosted("Y");
						droolsSave(delivery);
						
						vCnt++;
					}
				}
			} else {
//        else -- by good_type group
//            insert into register.bre_transitions( id, out_code, post_date, module, tte_code,
//                                                ref_no, ref_date, amount_total, amount_do, amount_vat,
//                                                cuy_code, cuy_rate, date_created, user_created, descr,
//                                                cc_out_id, cc_par_id, cc_gte_id, result, cost)
//            select nextval('register.trn_seq'), dey.out_code, dey_date, 'grasp', ddl.tte_code,
//                        dey_number, dey_date, 0, 0, 0,
//                        currency, exchange_rate, localtimestamp, p_user, 'Заприхождаване на стоки/материали/полуфабрикати',
//                        out.id, dey.par_id, ddl.gte_id, -1, ddl.cost
				vRows = queryFactory
						.select(qCDeliveryDetail.cost.multiply(qCDeliveryDetail.quantity).sum(),
								qCDelivery.id, qCDelivery.outCode.id, qCDelivery.deyDate,qFCtTransitionType.id,
								qCDelivery.deyNumber, qCDelivery.deyDate, 
								qCDelivery.currency.id, qCDelivery.exchangeRate, 
								qCDelivery.parId.id, qCGoods.gteId.id
							)
//                    from deliveries dey JOIN register.cc_organization_units out ON dey.out_code=out.code JOIN
//                    (select dey_id, case god.good_type  when 'GD' then '50-12' when 'BD' then '50-12' when 'SM' then '50-13' when 'RM' then '50-11' end as tte_code, god.gte_id as gte_id, sum(ddl.cost*quantity) as cost
//            from deliveries d0, delivery_details ddl, goods god where d0.posted='P' and d0.out_code like v_outcode
//                            and d0.dey_date between p_start_date and p_end_date and ddl.god_id=god.id group by 1,2,3) ddl ON dey.id=ddl.dey_id
//                    where dey.posted='P' and dey.out_code like v_outcode
//                            and dey.dey_date between p_start_date and p_end_date
//                            and ddl.dey_id=dey.id;
						.from(qCDelivery)
						.innerJoin(qCDeliveryDetail).on(qCDeliveryDetail.deyId.id.eq(qCDelivery.id))
						.innerJoin(qCGoods).on(qCGoods.id.eq(qCDeliveryDetail.godId.id).and(qCGoods.deleted.isFalse()))
						.innerJoin(qLoiGoodsType).on(qLoiGoodsType.id.eq(qCGoods.goodType.id).and(qLoiGoodsType.deleted.isFalse()))
						.innerJoin(qFCtTransitionType).on(qFCtTransitionType.code.eq(new CaseBuilder()
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_GD)).then("50-12") 
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_BD)).then("50-12")
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_SM)).then("50-13")
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_RM)).then("50-11")
									.otherwise("50-00") //TODO what about VR?
								)
								.and(qFCtTransitionType.deleted.isFalse())
							)
						.where(qCDeliveryDetail.deleted.isFalse()
							.and(qCDelivery.posted.eq("P"))
							.and(qCDelivery.deleted.isFalse())
							.and(qCDelivery.outCode.code.like(vOutCode))
							.and(qCDelivery.deyDate.between(
									Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), 
									Date.from(pEndDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) 
								)
							)
						)
						.groupBy(qCDelivery.id, qCDelivery.outCode.id, qCDelivery.deyDate, qFCtTransitionType.id,
							qCDelivery.deyNumber, qCDelivery.deyDate, 
							qCDelivery.currency.id, qCDelivery.exchangeRate, qCDelivery.parId.id, qCGoods.gteId.id)
						.fetch();
				for(Tuple row: vRows) { 
					if(row.get(0,BigDecimal.class).compareTo(BigDecimal.ZERO) <= 0) {
						errors.add("Складова разписка "+row.get(qCDelivery.deyNumber).toString()+": Не може да се прехвърлят документи с нулева себестойност");
					} else {
						CCcOrganizationUnit cCcOrganizationUnit = entityManager.getReference(CCcOrganizationUnit.class, row.get(qCDelivery.outCode.id));
						CCtCurrency cCtCurrency = entityManager.getReference(CCtCurrency.class, row.get(qCDelivery.currency.id));
						FCtTransitionType fCtTransitionType = entityManager.getReference(FCtTransitionType.class, row.get(qFCtTransitionType.id));
						CCcPartner cCcPartner = entityManager.getReference(CCcPartner.class, row.get(qCDelivery.parId.id));
						CCcGoodsType cCcGoodsType = entityManager.getReference(CCcGoodsType.class, row.get(qCGoods.gteId.id));
						LoiBreTransitionResult status = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_WAITING, cCcOrganizationUnit.getCompany(), false);
						FBreTransition breTrans = new FBreTransition(null,null,null,null,false,null,cCcOrganizationUnit,
								row.get(qCDelivery.deyDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"grasp",
								fCtTransitionType,row.get(qCDelivery.deyNumber).toString(),row.get(qCDelivery.deyDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
								null,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,cCtCurrency,row.get(qCDelivery.exchangeRate),
								null,"Заприхождаване на стоки/материали/полуфабрикати",status,row.get(0,BigDecimal.class),
								null,null,null,null,null,null,null,null,null,null,cCcPartner,cCcGoodsType,null,
								cCcOrganizationUnit,null,null,null);
						
						droolsSave(breTrans);

						CDelivery delivery = entityManager.getReference(CDelivery.class, row.get(qCDelivery.id));
						delivery.setPosted("Y");
						droolsSave(delivery);
						
						vCnt++;
					}
				}
//        end if;
			}
//        GET DIAGNOSTICS v_rows = ROW_COUNT;
//        raise notice 'delivery rows: %', v_rows;
			logger.info("Delivery rows: "+vRows);
//        v_cnt := v_cnt + v_rows;
			//count only the successful, not all of them here
//			vCnt = vCnt + vRows.size();
//
//        update deliveries set posted='Y'
//            where posted='P' and out_code like v_outcode
//                 and dey_date between p_start_date and p_end_date;
			//instead of here, the update will be done sale by sale after the document is saved
//			queryFactory.update(qCDelivery).set(qCDelivery.posted, "Y")
//				.where(
//					qCDelivery.posted.eq("P")
//					.and(qCDelivery.deleted.eq(false))
//					.and(JPAExpressions.select(qCCcOrganizationUnit.code)
//						.from(qCCcOrganizationUnit)
//						.where(qCCcOrganizationUnit.id.eq(qCDelivery.outCode.id).and(qCCcOrganizationUnit.deleted.eq(false)))
//						.eq(pOutCode) //TODO hierarchy is not implemented
//					)
//					.and(qCDelivery.deyDate.between(
//							Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), 
//							Date.from(pEndDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) 
//						)
//					)
//				)
//				.execute();
//    end if;
		}
//
//    -- Sales - with invoice and wihtout invoice
//    if position('S' in p_flags ) != 0 then -- post deliveries
		if(pFlags.contains("S")) {
//        -- set status to P - processing. This will lock rows and prevent others to update them in concurrency.
//        -- After this posted will be set to Y - the final status
//        update sales set posted='P'
//            where posted='N' and out_code like v_outcode
//                 and sale_date between p_start_date and p_end_date;
			queryFactory.update(qCSale).set(qCSale.posted, "P")
			.where(
				qCSale.posted.eq("N")
				.and(qCSale.deleted.eq(false))
				.and(JPAExpressions.select(qCCcOrganizationUnit.code)
					.from(qCCcOrganizationUnit)
					.where(qCCcOrganizationUnit.id.eq(qCSale.outCode.id).and(qCCcOrganizationUnit.deleted.eq(false)))
					.eq(pOutCode) //TODO hierarchy is not implemented
				)
				.and(qCSale.saleDate.between(
						Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), 
						Date.from(pEndDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) 
					)
				)
			)
			.execute();
//
//        -- set temp status of invoices
//        update invoices set posted='P'
//            where posted='N' and out_code like v_outcode
//                 and inv_date between p_start_date and p_end_date;
//
//        -- insert as sale as cost - just cost values by good_type
//        if v_by_good_types='N' then
			if(vByGoodTypes.equals("N")) {
//            insert into register.bre_transitions( id, out_code, post_date, module, tte_code,
//                                                ref_no, ref_date, amount_total, amount_do, amount_vat,
//                                                cuy_code, cuy_rate, date_created, user_created, descr,
//                                                cc_out_id, cc_par_id, result, cost, ide_code)
//            select nextval('register.trn_seq'), sae.out_code, sale_date, 'grasp', sdl.tte_code,
//                document_number, sale_date, 0, 0, 0,
//                currency, exchange_rate, localtimestamp, p_user, rde.rv_long,
//                out.id, sae.par_id, -1, sdl.cost, sae.idt_code
				vRows = queryFactory
						.select(qCSaleDetail.cost.multiply(qCSaleDetail.quantity).sum(), qCSale.id,
								qCSale.outCode.id, qCSale.saleDate, qFCtTransitionType.id, qCSale.documentNumber, qCSale.saleDate, 
								qCSale.currency.id, qCSale.exchangeRate, qLoiTypeDoc.listOptionItemName,
								qCSale.parId.id, qCSale.idtCode.id
							)
//            from sales sae JOIN register.cc_organization_units out on sae.out_code=out.code JOIN
//                (select sdl.sae_id, case god.good_type  when 'GD' then '50-52' when 'BD' then '50-52' when 'SM' then '50-53' when 'RM' then '50-51' end as tte_code, sum(sdl.cost*sdl.quantity) as cost
//            from sales sae, sale_details sdl, stocks stk, goods god
//            where sdl.sae_id=sae.id and sdl.stk_id=stk.id and stk.god_id=god.id and
//                    sae.posted='P' and sae.out_code like v_outcode
//                    and sae.sale_date between p_start_date and p_end_date
//                    group by 1,2) as sdl ON sae.id=sdl.sae_id JOIN ref_data rde ON rde.rv_domain='TYPE_DOC' and sae.type_doc=rde.rv_value
//            where sae.posted='P' and sae.out_code like v_outcode
//                    and sae.sale_date between p_start_date and p_end_date;
						.from(qCSale)
						.innerJoin(qCCcOrganizationUnit).on(qCCcOrganizationUnit.id.eq(qCSale.outCode.id).and(qCCcOrganizationUnit.deleted.eq(false)))
						.innerJoin(qCSaleDetail).on(qCSaleDetail.saeId.id.eq(qCSale.id).and(qCSaleDetail.deleted.eq(false)))
						.innerJoin(qCStock).on(qCStock.id.eq(qCSaleDetail.stkId.id).and(qCStock.deleted.eq(false)))
						.innerJoin(qCGoods).on(qCGoods.id.eq(qCStock.godId.id).and(qCGoods.deleted.eq(false)))
						.innerJoin(qLoiGoodsType).on(qLoiGoodsType.id.eq(qCGoods.goodType.id).and(qLoiGoodsType.deleted.eq(false)))
						.innerJoin(qFCtTransitionType).on(qFCtTransitionType.code.eq(new CaseBuilder()
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_GD)).then("50-52") 
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_BD)).then("50-52")
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_SM)).then("50-53")
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_RM)).then("50-51")
									.otherwise("50-00") //TODO what about VR?
								)
								.and(qFCtTransitionType.deleted.eq(false))
							)
						.innerJoin(qLoiTypeDoc).on(qLoiTypeDoc.id.eq(qCSale.typeDoc.id).and(qLoiTypeDoc.deleted.eq(false)))
						.where(qCSale.posted.eq("P")
							.and(qCSale.deleted.eq(false))
							.and(qCSale.outCode.code.like(vOutCode))
							.and(qCSale.saleDate.between(
									Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), 
									Date.from(pEndDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) 
								)
							)
						)
						.groupBy(qCSale.id, qCSale.outCode.id, qCSale.saleDate, qFCtTransitionType.id, qCSale.documentNumber, qCSale.saleDate,
								qCSale.currency.id, qCSale.exchangeRate, qLoiTypeDoc.listOptionItemName, 
								qCSale.parId.id, qCSale.idtCode.id)
					.fetch();
				for(Tuple row: vRows) { 
					if(row.get(0,BigDecimal.class).compareTo(BigDecimal.ZERO) <= 0) {
						errors.add("Продажба "+row.get(qCSale.documentNumber).toString()+": Не може да се прехвърлят документи с нулева себестойност");
					} else {
						CCcOrganizationUnit cCcOrganizationUnit = entityManager.getReference(CCcOrganizationUnit.class, row.get(qCSale.outCode.id));
						CCtCurrency cCtCurrency = entityManager.getReference(CCtCurrency.class, row.get(qCSale.currency.id));
						FCtTransitionType fCtTransitionType = entityManager.getReference(FCtTransitionType.class, row.get(qFCtTransitionType.id));
						CCcPartner cCcPartner = entityManager.getReference(CCcPartner.class, row.get(qCSale.parId.id));
						FCtInvDealType fCtInvDealType = entityManager.getReference(FCtInvDealType.class, row.get(qCSale.idtCode.id));
						LoiBreTransitionResult status = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_WAITING, cCcOrganizationUnit.getCompany(), false);
						FBreTransition breTrans = new FBreTransition(null,null,null,null,false,null,cCcOrganizationUnit,
								row.get(qCSale.saleDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"grasp",
								fCtTransitionType,row.get(qCSale.documentNumber).toString(),row.get(qCSale.saleDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
								null,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,cCtCurrency,row.get(qCSale.exchangeRate),
								null,row.get(qLoiTypeDoc.listOptionItemName),status,row.get(0,BigDecimal.class),
								null,fCtInvDealType,null,null,null,null,null,null,null,null,cCcPartner,
								null,null,cCcOrganizationUnit,null,null,null);
						
						droolsSave(breTrans);

						CSale sale = entityManager.getReference(CSale.class, row.get(qCSale.id));
						sale.setPosted("Y");
						droolsSave(sale);
						
						vCnt++;
					}
				}
//        else -- group by good_type
			} else {
//            insert into register.bre_transitions( id, out_code, post_date, module, tte_code,
//                                                ref_no, ref_date, amount_total, amount_do, amount_vat,
//                                                cuy_code, cuy_rate, date_created, user_created, descr,
//                                                cc_out_id, cc_par_id, cc_gte_id, result, cost, ide_code)
//
//            select nextval('register.trn_seq'), sae.out_code, sale_date, 'grasp', sdl.tte_code,
//                document_number, sale_date, 0, 0, 0,
//                currency, exchange_rate, localtimestamp, p_user, rde.rv_long,
//                out.id, sae.par_id, sdl.gte_id, -1, sdl.cost, sae.idt_code
				vRows = queryFactory
						.select(qCSaleDetail.cost.multiply(qCSaleDetail.quantity).sum(), qCSale.id,
								qCSale.outCode.id, qCSale.saleDate, qFCtTransitionType.id, qCSale.documentNumber, qCSale.saleDate, 
								qCSale.currency.id, qCSale.exchangeRate, qLoiTypeDoc.listOptionItemName,
								qCSale.parId.id, qCGoods.gteId.id, qCSale.idtCode.id
							)
//            from sales sae JOIN register.cc_organization_units out on sae.out_code=out.code JOIN
//                (select sdl.sae_id, case god.good_type  when 'GD' then '50-52' when 'BD' then '50-52' when 'SM' then '50-53' when 'RM' then '50-51' end as tte_code, god.gte_id, sum(sdl.cost*sdl.quantity) as cost
//            from sales sae, sale_details sdl, stocks stk, goods god
//            where sdl.sae_id=sae.id and sdl.stk_id=stk.id and stk.god_id=god.id and
//                    sae.posted='P' and sae.out_code like v_outcode
//                    and sae.sale_date between p_start_date and p_end_date
//                    group by 1,2,3) as sdl ON sae.id=sdl.sae_id JOIN ref_data rde ON rde.rv_domain='TYPE_DOC' and sae.type_doc=rde.rv_value
//            where sae.posted='P' and sae.out_code like v_outcode
//                    and sae.sale_date between p_start_date and p_end_date;
						.from(qCSale)
						.innerJoin(qCSaleDetail).on(qCSaleDetail.saeId.id.eq(qCSale.id).and(qCSaleDetail.deleted.eq(false)))
						.innerJoin(qCStock).on(qCStock.id.eq(qCSaleDetail.stkId.id).and(qCStock.deleted.eq(false)))
						.innerJoin(qCGoods).on(qCGoods.id.eq(qCStock.godId.id).and(qCGoods.deleted.eq(false)))
						.innerJoin(qLoiGoodsType).on(qLoiGoodsType.id.eq(qCGoods.goodType.id).and(qLoiGoodsType.deleted.eq(false)))
						.innerJoin(qFCtTransitionType).on(qFCtTransitionType.code.eq(new CaseBuilder()
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_GD)).then("50-52") 
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_BD)).then("50-52")
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_SM)).then("50-53")
									.when(qLoiGoodsType.listOptionItemCode.eq(LoiGoodsType.GOODS_TYPE_RM)).then("50-51")
									.otherwise("50-00") //TODO what about VR?
								)
								.and(qFCtTransitionType.deleted.eq(false))
							)
						.innerJoin(qLoiTypeDoc).on(qLoiTypeDoc.id.eq(qCSale.typeDoc.id))
						.where(qCSale.posted.eq("P")
							.and(qCSale.deleted.eq(false))
							.and(qCSale.outCode.code.like(vOutCode))
							.and(qCSale.saleDate.between(
									Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), 
									Date.from(pEndDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) 
								)
							)
						)
						.groupBy(qCSale.id, qCSale.outCode.id, qCSale.saleDate, qFCtTransitionType.id, qCSale.documentNumber, qCSale.saleDate,
								qCSale.currency.id, qCSale.exchangeRate, qLoiTypeDoc.listOptionItemName, 
								qCSale.parId.id, qCGoods.gteId.id, qCSale.idtCode.id)
					.fetch();
				for(Tuple row: vRows) { 
					if(row.get(0,BigDecimal.class).compareTo(BigDecimal.ZERO) <= 0) {
						errors.add("Продажба "+row.get(qCSale.documentNumber).toString()+": Не може да се прехвърлят документи с нулева себестойност");
					} else {
						CCcOrganizationUnit cCcOrganizationUnit = entityManager.getReference(CCcOrganizationUnit.class, row.get(qCSale.outCode.id));
						CCtCurrency cCtCurrency = entityManager.getReference(CCtCurrency.class, row.get(qCSale.currency.id));
						FCtTransitionType fCtTransitionType = entityManager.getReference(FCtTransitionType.class, row.get(qFCtTransitionType.id));
						CCcPartner cCcPartner = entityManager.getReference(CCcPartner.class, row.get(qCSale.parId.id));
						CCcGoodsType cCcGoodsType = entityManager.getReference(CCcGoodsType.class, row.get(qCGoods.gteId.id));
						FCtInvDealType fCtInvDealType = entityManager.getReference(FCtInvDealType.class, row.get(qCSale.idtCode.id));
						LoiBreTransitionResult status = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_WAITING, cCcOrganizationUnit.getCompany(), false);
						FBreTransition breTrans = new FBreTransition(null,null,null,null,false,null,cCcOrganizationUnit,
								row.get(qCSale.saleDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),"grasp",
								fCtTransitionType,row.get(qCSale.documentNumber).toString(),row.get(qCSale.saleDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
								null,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,cCtCurrency,row.get(qCSale.exchangeRate),
								null,row.get(qLoiTypeDoc.listOptionItemName),status,row.get(0,BigDecimal.class),
								null,fCtInvDealType,null,null,null,null,null,null,null,null,cCcPartner,
								cCcGoodsType,null,cCcOrganizationUnit,null,null,null);
						
						droolsSave(breTrans);

						CSale sale = entityManager.getReference(CSale.class, row.get(qCSale.id));
						sale.setPosted("Y");
						droolsSave(sale);
						
						vCnt++;
					}
				}
//        end if;
			}
//
//        GET DIAGNOSTICS v_rows = ROW_COUNT;
//        raise notice 'sale cost rows: %', v_rows;
			logger.info("sale cost rows: "+vRows);
//        v_cnt := v_cnt + v_rows;
			//count only the successful, not all of them here
//			vCnt = vCnt + vRows.size();
//
//        -- insert as sale with invoices --
//        insert into register.bre_transitions( id, out_code, post_date, module, tte_code,
//                                              ref_no, ref_date, amount_total, amount_do, amount_vat, amount_outstanding,
//                                              cuy_code, cuy_rate, date_created, user_created, descr,
//                                              cc_out_id, cc_par_id, result, cost, ide_code)
//        select nextval('register.trn_seq'), ine.out_code, ine.inv_date, 'grasp',
//              case ine.type_doc when 'SR' then '01-31' when 'AP' then '01-51' when 'KI' then '03-31' else '01-31' end as tte_code,
//               inv_number, inv_date, total, dan_osnova, vat_sum, total-advanced,
//               currency, exchange_rate, localtimestamp, p_user,
//               case ine.type_doc when 'SR' then 'Фактура продажби' when 'AP' then 'Авансово плащане' when 'KI' then 'Кред. известие' else 'Фактура продажби' end,
//               out.id, ine.par_id, -1, ins.cost, ine.idt_code
//        from invoices ine JOIN register.cc_organization_units out ON ine.out_code=out.code JOIN
//            (select ins.ine_id, sum(sae.cost) as cost from inv_sales ins, sales sae
//                where ins.sae_id=sae.id  and sae.out_code like v_outcode
//                and sae.sale_date between p_start_date and p_end_date group by ins.ine_id) ins ON ins.ine_id=ine.id
//        where ine.posted='P' and ine.out_code like v_outcode
//                and ine.inv_date between p_start_date and p_end_date;
//        GET DIAGNOSTICS v_rows = ROW_COUNT;
//        raise notice 'invoice rows: %', v_rows;
//        v_cnt := v_cnt + v_rows;
//
//        update sales set posted='Y'
//            where posted='P' and out_code like v_outcode
//                 and sale_date between p_start_date and p_end_date;
			//instead of here, the update will be done sale by sale after the document is saved
//			queryFactory.update(qCSale).set(qCSale.posted, "Y")
//			.where(
//				qCSale.posted.eq("P")
//				.and(qCSale.deleted.eq(false))
//				.and(JPAExpressions.select(qCCcOrganizationUnit.code)
//					.from(qCCcOrganizationUnit)
//					.where(qCCcOrganizationUnit.id.eq(qCSale.outCode.id).and(qCCcOrganizationUnit.deleted.eq(false)))
//					.eq(pOutCode) //TODO hierarchy is not implemented
//				)
//				.and(qCSale.saleDate.between(
//						Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), 
//						Date.from(pEndDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) 
//					)
//				)
//			)
//			.execute();
//        update invoices set posted='Y'
//            where posted='P' and out_code like v_outcode
//                 and inv_date between p_start_date and p_end_date;
//    end if;
		}
//
//     if position('P' in p_flags ) != 0 then -- post payments
		if(pFlags.contains("P")) {
//        update invoice_payments set posted='P'
//            where posted='N' and date_payment between p_start_date and p_end_date
//                  and exists (select 1 from invoices ine where ine.id=invoice_payments.ine_id and ine.out_code like v_outcode);
//
//        insert into register.bre_transitions( id, out_code, post_date, module, tte_code,
//                                              ref_no, ref_date, amount_total, amount_do, amount_vat, amount_outstanding,
//                                              cuy_code, cuy_rate, date_created, user_created, descr,
//                                              cc_out_id, cc_par_id, result, cost, ide_code,
//                                              dependence_type, dependence_code, add_ref_no, add_ref_date)
//        select nextval('register.trn_seq'), ine.out_code, ine.inv_date, 'grasp',
//              case ine.type_doc when 'KI' then
//                          case ipt.payment_type when 'CAH' then '33-01' else '31-01' end
//                     else case ipt.payment_type when 'CAH' then '32-01' else '30-01' end end as tte_code,
//               case ipt.payment_type when 'CAH' then pat.ref_no else bfe.ref_no end,
//               case ipt.payment_type when 'CAH' then pat.date_payment else bfe.date_upload end,
//               ipt.amount, 0.0, 0.0, 0.0,
//               ipt.currency, ine.exchange_rate, localtimestamp, p_user, 'Плащане по фактура '||ine.inv_number||'/'||ine.inv_date,
//               out.id, ine.par_id, -1, 0.0, null,
//               ipt.payment_type, case ipt.payment_type when 'CAH' then cdk.code else bfe.iban end,  inv_number, inv_date
//        from invoice_payments ipt
//            JOIN invoices ine on ipt.ine_id=ine.id
//            JOIN register.cc_organization_units out on ine.out_code=out.code
//            JOIN cash.cah_payments pat on ipt.pat_id=pat.id
//            LEFT OUTER JOIN cash.cah_desks dek on pat.dek_id=dek.id
//            LEFT OUTER JOIN cash.ct_cash_desks cdk on dek.cdk_id=cdk.id
//            LEFT OUTER JOIN cash.bak_files bfe on pat.bfe_id=bfe.id
//        where  ipt.posted='P' and ipt.date_payment between p_start_date and p_end_date and ine.out_code like v_outcode;
//
//        GET DIAGNOSTICS v_rows = ROW_COUNT;
//        raise notice 'payment rows: %', v_rows;
//        v_cnt := v_cnt + v_rows;
//
//        update invoice_payments set posted='Y'
//            where posted='P' and date_payment between p_start_date and p_end_date
//                  and exists (select 1 from invoices ine where ine.id=invoice_payments.ine_id and ine.out_code like v_outcode);
//     end if;
		}
//
//    return v_cnt;
		result.put("cnt", vCnt);
		result.put("errors", errors);
		return result;
	}
}
