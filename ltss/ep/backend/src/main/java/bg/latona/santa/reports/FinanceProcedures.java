package bg.latona.santa.reports;

import bg.latona.santa.DroolsRuleException;
import bg.latona.santa.RepositoryConfiguration;
import bg.latona.santa.entities.CommonRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.*;
import bg.latona.santa.entities.santa.finance.*;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.repositories.*;
import bg.latona.santa.security.SantaUser;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.hibernate.HibernateQueryFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.WebApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

@Repository
@Transactional
public class FinanceProcedures {

	private final String EBK = "EBK";
	private final String FUN = "FUN";
	private final String PRM = "PRM";
	private final String FIE = "FIE";
	private final String PAR = "PAR";
	private final String GTE = "GTE";
	private final String COT = "COT";
	private final String OUT = "OUT";
	private final String RE1 = "RE1";
	private final String RE2 = "RE2";
	private final String CREDIT = "CT";
	private final String DEBIT = "DT";
	private static Logger logger = LoggerFactory.getLogger(FinanceProcedures.class);
	// entity manager insert
	@PersistenceContext
	private EntityManager entityManager; //USING HQL
	@Autowired
	private WebApplicationContext appContext;
	private Repositories repositories = null;

	private Repositories getRepositories() {
		if (repositories == null) {
			repositories = new Repositories(appContext);
		}
		return repositories;
	}

	//ATTENTION! All calls to trySave should be checked for errors (call with second param = true and check if null is returned) and properly handled (throw exception if there is no other handling)
	CommonRecord trySave(CommonRecord entity, boolean doReturnNullAtErrors) {
		logger.trace("FinanceProcedures.trySave " + entity);
		try {
			CommonRecord result;
			if (entity.getId() == null) {
				RepositoryConfiguration.getBeforeCreateValidator().validate(entity, null);
				result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass()).get()).saveAndFlush(entity);
				RepositoryConfiguration.getAfterCreateValidator().validate(entity, null);
			} else {
				RepositoryConfiguration.getBeforeSaveValidator().validate(entity, null);
				result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass()).get()).saveAndFlush(entity);
				RepositoryConfiguration.getAfterSaveValidator().validate(entity, null);
			}
			return result;
		} catch (DroolsRuleException e) {
			if (!doReturnNullAtErrors && e.getResult().getFieldErrors().size() > 0 && e.getResult().getFieldErrors().get(0).getCodes()[0].equals("notUnique")) {
				return (CommonRecord) e.getResult().getFieldErrors().get(0).getRejectedValue();
			} else {
				logger.info(/*e.getStackTrace()[0].getFileName()+" "+e.getStackTrace()[0].getLineNumber()+" "+*/entity.getClass().getSimpleName() + ": " + e.getMessage());
				return null;
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			logger.error(e.getMessage());
			return null;
		}
	}
	
	//TODO All procedures to be checked if their queries properly check for "deleted" flag of the entities

	//bridge.bridge_data_process_ui
	public Map<String, Object> bridgeDataProcessUi(String pCode, Long pTrnId) {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		LoiBreTransitionResult vResult = null;
		CCcOrganizationUnit vMainOutCode = null;
		FCtTransitionType vTteId = null;
		Long vDependenceId = null;
		
		SecUser user = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			user = ((SantaUser) authentication.getPrincipal()).getSecUser();
		}
		CCcOrganizationUnitRepository cCcOrganizationUnitRepository = ((CCcOrganizationUnitRepository) getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get());
		vMainOutCode = cCcOrganizationUnitRepository.findFirstByCodeAndCompanyAndDeleted(pCode, user.getCompany(), false);
		LoiBreTransitionResultRepository loiBreTransitionResultRepository = ((LoiBreTransitionResultRepository) getRepositories().getRepositoryFor(LoiBreTransitionResult.class).get());

		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QFBreTransition qfBreTransition = QFBreTransition.fBreTransition;

		List<FBreTransition> vRecBre = queryFactory.select(qfBreTransition) //TODO must be made to work for hierarchical organizations
				.from(qfBreTransition)
				.where(qfBreTransition.result.listOptionItemCode.eq(LoiBreTransitionResult.BRE_TRANSITION_RESULT_WAITING)
						.and(qfBreTransition.outCode.code.eq(pCode))
						.and(qfBreTransition.deleted.eq(false))
						.and(qfBreTransition.id.eq(pTrnId == null || pTrnId == 0L ? qfBreTransition.id : Expressions.constant(pTrnId))))
				.orderBy(qfBreTransition.id.asc())
				.fetch();

		logger.trace("bridgeDataProcessUi: Processing " + vRecBre.size() + " FBreTransition objects");
		for (FBreTransition vRec : vRecBre) {
			vMainOutCode = vRec.getOutCode();
			vTteId = vRec.getTteCode();

			if(vRec.getDependenceType() != null) {
				vDependenceId = vwRuleDependenceId(vRec.getDependenceType().getListOptionItemCode(), vRec.getDependenceCode(), vRec.getOutCode());
			} else {
				logger.info("bridgeDataProcessUi: FBreTransition "+vRec.getId()+" has no dependenceType");
			}

			Integer checkBridgeCheckRules = bridgeCheckRules(vRec.getOutCode(), vTteId, vRec.getDependenceType(), vDependenceId, vRec.getPostDate());
			if (checkBridgeCheckRules != 0) {
				logger.error("bridgeDataProcessUi: 2 Failed on bridgeCheckRules for FBreTransition refNo "+vRec.getRefNo());
				vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_CHECK_RULES, user.getCompany(), false);
			} else {
				if (bridgeCheckRequiredCostCenters(vRec, null, 1, null) != 0) {
					logger.error("bridgeDataProcessUi: 100 Failed on bridgeCheckRequiredCostCenters for FBreTransition refNo "+vRec.getRefNo());
					vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_CHECK_COST_CENTERS, user.getCompany(), false);
				} else {
					if (bridgeCheckBteTteType(vRec.getOutCode(), vTteId) != 0) {
						logger.error("bridgeDataProcessUi: 102 Failed on bridgeCheckBteTteType for FBreTransition refNo "+vRec.getRefNo());
						vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_CHECK_BTE_TTE_TYPE, user.getCompany(), false);
					} else {
						vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_OK, user.getCompany(), false);
						Map<String, Long> bridgeFindOrCreateBatchVar =  bridgeFindOrCreateBatch(vRec, null, null, 1, null, null);
						Long vBatch = bridgeFindOrCreateBatchVar.get("poBahId");
						Long vResultB = bridgeFindOrCreateBatchVar.get("poResult");

						String vRuleJournal = "";
//                        						if v_batch > 0 then
						if(vBatch > 0) {
//                                     for v_expr in (select rue.expression, btr.rue_id, btr.jte_id
//                                                    from ct_rules rue, ct_batch_type_rules btr, register.ct_transition_types tte
//                                                   where tte.id=v_tte_id
//                                                     and btr.tte_id=tte.id
//                                                     and btr.out_code=v_rec.out_code
//                                                     and btr.rue_id=rue.id
//                                                     and coalesce(btr.dependence_type,'')=coalesce(v_rec.dependence_type,'')
//                                                     and coalesce(btr.dependence_id,0)=coalesce(v_dependence_id,0)
//                                                     and v_rec.post_date between btr.active_from_date and btr.active_to_date
//                                                   order by order_num) loop
							List<Tuple> vExprList = getBatchTypeRules(vTteId, vRec.getOutCode(), vRec.getDependenceType(), vDependenceId, vRec.getPostDate());
							logger.trace("bridgeDataProcessUi: Processing " + vExprList.size() + " rules for FBreTransition refNo "+vRec.getRefNo());
							for(Tuple vExpr: vExprList) {
								QFCtBatchTypeRule qFCtBatchTypeRule = QFCtBatchTypeRule.fCtBatchTypeRule;
								QFCtRule qFCtRule = QFCtRule.fCtRule;
//                                           if ( v_expr.rue_id::varchar||'-'||v_expr.jte_id::varchar != v_rule_journal ) then
								if( !vRuleJournal.equals(vExpr.get(qFCtBatchTypeRule.rueId).getId().toString()+"-"+vExpr.get(qFCtBatchTypeRule.jteId).getId()) ) {
//                                                v_journal := bridge.create_journal( v_batch, v_expr.jte_id, v_rec,null,null,'1');
									FPtJournal vJournal = bridgeCreateJournal(entityManager.getReference(FPtBatch.class, vBatch), vExpr.get(qFCtBatchTypeRule.jteId), vRec, null, null, 1, null);
//                                                v_rule_journal := v_expr.rue_id::varchar||'-'||v_expr.jte_id::varchar;
									vRuleJournal = vExpr.get(qFCtBatchTypeRule.rueId).getId().toString()+"-"+vExpr.get(qFCtBatchTypeRule.jteId).getId();
//
//                                               if v_journal > 0 then
									if(vJournal != null) {
//                                                    if v_expr.expression = 'new_posting' then
										if( vExpr.get(qFCtRule.expression).equals("new_posting") ) {
//                                       raise notice 'record %',v_rec;
//                                                       v_result := v_result + bridge.new_posting(v_batch, v_journal,v_expr.jte_id, v_expr.rue_id,v_rec,null,'1' );
											vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(
													bridgeNewPosting(entityManager.getReference(FPtBatch.class, vBatch), vJournal, vExpr.get(qFCtBatchTypeRule.jteId), vExpr.get(qFCtBatchTypeRule.rueId), vRec, null, 1, null), 
													user.getCompany(), false);
//                                                    elsif v_expr.expression = 'payment_posting' then
										} else if( vExpr.get(qFCtRule.expression).equals("payment_posting") ) {
//                                                       v_result := v_result + bridge.payment_posting(v_batch, v_journal,v_expr.jte_id, v_expr.rue_id,v_rec,null,'1');
											vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(
													bridgePaymentPosting(entityManager.getReference(FPtBatch.class, vBatch), vJournal, vExpr.get(qFCtBatchTypeRule.jteId), vExpr.get(qFCtBatchTypeRule.rueId), vRec, null, 1, null),
													user.getCompany(), false);
//                                                    else
										} else {
//                                                       --raise exception 'Unknown processing function: %1', v_expr.expression;
//                                                       v_result := 2;
											logger.error("bridgeDataProcessUi: Unknown processing function " + vExpr.get(qFCtRule.expression));
											vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_UNKNOWN_FUNCTION, user.getCompany(), false);
//                                                    end if;
										}
//                                                if v_result != 0 then
										if(vResult.getListOptionItemCode() != LoiBreTransitionResult.BRE_TRANSITION_RESULT_OK) {
//                                                   exit;
											break;
//                                                end if;
										}
//                                            else
									} else {
//                                                   v_result := 1;
										logger.error("bridgeDataProcessUi: Failed on bridgeCreateJournal for FJournalType ID "+vExpr.get(qFCtBatchTypeRule.jteId).getId());
										vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiBreTransitionResult.BRE_TRANSITION_RESULT_FAIL_CREATE_JOURNAL, user.getCompany(), false);
//                                          end if;
									}
//                                    end if;
								}
//                                 end loop; -- loop  through rules
							}
//                                 update register.bre_transitions
//                                 set bah_id = v_batch
//                                 where id= v_rec.id;
							vRec.setBahId(entityManager.getReference(FPtBatch.class, vBatch));
							if(trySave(vRec, true) == null) {
								logger.error("bridgeDataProcessUi: 1 Couldn't update the batch of FBreTransition");
								resultMap.put("result", 1);
								return resultMap;
							}
//                                 if v_result_b <0 then
							if(vResultB < 0) {
//                                     v_result := (-1)*v_result_b;
								vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(
										-vResultB,
										user.getCompany(), false);
//                                 end if;
							}
//                               else
						} else {
//                                    v_result := (-1)*v_result_b;
							logger.error("bridgeDataProcessUi failed on bridgeFindOrCreateBatch for FBreTransition refNo "+vRec.getRefNo());
							vResult = loiBreTransitionResultRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(
									-vResultB,
									user.getCompany(), false);
//                               end if;
						}
//                               end;
//                             end if;
					}
//                            end if;
				}
//                           end if;
			}
//                         exception
//                               when others then
//                           raise notice 'exception : %',SQLERRM;
//                               v_result := 1;
//                             end;
			//check exceptions
//                             update register.bre_transitions
//                               set result = v_result
//                               where id= v_rec.id;
			vRec.setResult(vResult);
			if(trySave(vRec, true) == null) {
				logger.error("bridgeDataProcessUi: 1 Failed updating FBreTransition refNo "+vRec.getRefNo());
				resultMap.put("result", 1);
				resultMap.put("error", "Failed updating FBreTransition refNo "+vRec.getRefNo());
				return resultMap;
			}
//                         end loop; -- loop throgh reccord
		}
//                        /*  exception
//                           when others then
//                              return 1;
//                           end; */
		FPtJournalRepository fPtJournalRepository = ((FPtJournalRepository) getRepositories().getRepositoryFor(FPtJournal.class).get());
		LoiPtJournalStatusRepository loiPtJournalStatusRepository = ((LoiPtJournalStatusRepository) getRepositories().getRepositoryFor(LoiPtJournalStatus.class).get());
		LoiPtJournalStatus loiPtJournalStatusC = loiPtJournalStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtJournalStatus.PT_JOURNAL_STATUS_C, vMainOutCode.getCompany(), false);
//                        						for v_rec_ag in (select jol.id from pt_journals jol where jol.out_code like (p_code||'%') and jol.status='C') loop
		List<FPtJournal> vRecAgList = fPtJournalRepository.findByOutCodeAndStatusAndCompanyAndDeleted(vMainOutCode, loiPtJournalStatusC, vMainOutCode.getCompany(), false);
		logger.trace("bridgeDataProcessUi: Processing " + vRecAgList.size() + " FPtJournal objects");
		for(FPtJournal vRecAg: vRecAgList) {
//                        						v_res_agg :=aggregate_journals_ui(v_rec_ag.id);
			accountingAggregateJournalsUi(vRecAg.getId());
//                        						end loop;
		}
//                        						return 0;
		resultMap.put("result", 0);
//                        						END;
		return resultMap;
	}

	private Long vwRuleDependenceId(Long dependenceTypeCode, String dependenceCode, CCcOrganizationUnit outCode) {
		Long vDependenceId = null;
		if(dependenceTypeCode == LoiBatchTypeRuleDependenceType.BATCH_TYPE_RULE_DEPENDENCE_TYPE_PRT) {
			CCtPartnerGroupRepository cCtPartnerGroupRepository = ((CCtPartnerGroupRepository) getRepositories().getRepositoryFor(CCtPartnerGroup.class).get());
			CCtPartnerGroup cCtPartnerGroup = cCtPartnerGroupRepository.findFirstByCodeAndOutCodeAndCompanyAndDeleted(dependenceCode, outCode, outCode.getCompany(), false);
			vDependenceId = cCtPartnerGroup.getId();
		} else if(dependenceTypeCode == LoiBatchTypeRuleDependenceType.BATCH_TYPE_RULE_DEPENDENCE_TYPE_BAK) {
			CCtBankAccountRepository cCtBankAccountRepository = ((CCtBankAccountRepository) getRepositories().getRepositoryFor(CCtBankAccount.class).get());
			CCtBankAccount cCtBankAccount = cCtBankAccountRepository.findFirstByIbanAndOutCodeAndCompanyAndDeleted(dependenceCode, outCode, outCode.getCompany(), false);
			vDependenceId = cCtBankAccount.getId();
		} else if(dependenceTypeCode == LoiBatchTypeRuleDependenceType.BATCH_TYPE_RULE_DEPENDENCE_TYPE_CAH) {
			//TODO cash
//					CCtPartnerGroupRepository cCtPartnerGroupRepository = ((CCtPartnerGroupRepository) getRepositories().getRepositoryFor(CCtPartnerGroup.class).get());
//					CCtPartnerGroup cCtPartnerGroup = cCtPartnerGroupRepository.findFirstByCodeAndOutCodeAndCompanyAndDeleted(dependenceCode, outCode, outCode.getCompany(), false);
//					vDependenceId = cCtPartnerGroup.getId();
		}
		return vDependenceId;
	}

	private Map<String, Long> bridgeFindOrCreateBatch(FBreTransition piBre, FInvInvoice piInv, Object piLon, int piFlag, Integer piTteId, Long piAmount) { // piInv is accounting.inv_invoices, piLon accounting.lon_postings
		FPtBatch vBahId = null;
		FCtBatchType vBteId = null;
		LoiCTransitionType vTteType = null;
		LoiCTransitionSide vTteSide = null;
		FCtTransitionType vTteId = null;
		CCcOrganizationUnit vMainOutCode = null;
		Date vCreatedDate = null;
		SecUser vCreatedBy = null;
		BigDecimal vAmountTotal = null;
		BigDecimal vAmountOutstanding = null;
		BigDecimal vAmountDo = null;
		BigDecimal vAmountVat = null;
		BigDecimal vAmountTotalCurrency = null;
		BigDecimal vAmountOutstandingCurrency = null;
		BigDecimal vAmountDoCurrency = null;
		BigDecimal vAmountVatCurrency = null;
		CCtCurrency vDefaultCuyCode = null;
		CCtCurrency vCuyCode = null;
		BigDecimal vCuyRate = null;
		Integer vCuyUnit = null;
		LocalDate vPostDate = null;
		String vModule = null;
		String vDependenceCode = null;
		String vRefNo = null;
		LocalDate vRefDate = null;
		FCtInvDealType vIdeId = null;
		String vReason = null;
		LocalDate vAddRefDate = null;
		String vAddRefNo = null;
		LocalDate vDueDate = null;
		CCcPartner vCcParId = null;

		Map<String, Long> result = new HashMap<String, Long>();

		//logger.trace("in find or create batch");
		if (piFlag == 1) {
			vCreatedDate = piBre.getCreatedDate();
			vCreatedBy = piBre.getCreatedBy();
			vPostDate = piBre.getPostDate();
			vAmountTotal = piBre.getAmountTotal();
			vAmountOutstanding = piBre.getAmountOutstanding();
			vAmountDo = piBre.getAmountDo();
			vAmountVat = piBre.getAmountVat();
			vModule = piBre.getModule();
			vDependenceCode = piBre.getDependenceCode();  /*(case when v_rec.dependence_type='BAK' then
                        (select bat.iban from register.ct_bank_accounts bat
                        where bat.id=v_rec.dependence_id)
                        when v_rec.dependence_type='CAH' then
                        (select cdk.code from cash.ct_cash_desks cdk
                         where cdk.id=v_rec.dependence_id)
                         else null end);*/
			vRefNo = piBre.getRefNo();
			vRefDate = piBre.getRefDate();
			//	vIdeId = vRecBre.getIdeId();
			vReason = piBre.getDescr();
			vAddRefDate = piBre.getAddRefDate();
			vAddRefNo = piBre.getAddRefNo();
			vDueDate = piBre.getDueDate();
			vCcParId = piBre.getCcParId();
			vCuyUnit = null;
			vTteId = piBre.getTteCode();
			vIdeId = piBre.getIdeCode();
			vMainOutCode = piBre.getOutCode();
			vCuyRate = piBre.getCuyRate();
			vCuyCode = piBre.getCuyCode();
		} else if (piFlag == 2) {
			/*
			v_rec:=pi_inv;*/
//			v_tte_id := v_rec.tte_id;
			vTteId = piInv.getTteId();
			vCreatedDate = piInv.getCreatedDate();
			vCreatedBy = piInv.getCreatedBy();
//			v_post_date:=pi_inv.date_account;
			vPostDate = piInv.getDateAccount();
//			v_amount_total:=v_rec.total_amount;
			vAmountTotal = piInv.getTotalAmount();
//			v_amount_outstanding:=null;
			vAmountOutstanding = null;
//			v_amount_vat:=v_rec.vat_amount;
			vAmountVat = piInv.getVatAmount();
//			v_amount_do:=v_rec.tax_base;
			vAmountDo = piInv.getTaxBase();
//			v_module:='accounting';
			vModule = "accounting";
//			v_dependence_code:=null;
			vDependenceCode = null;
//			v_ref_no:=v_rec.inv_no;
			vRefNo = String.valueOf(piInv.getInvNo());
//			v_ref_date:=v_rec.inv_date;
			vRefDate = piInv.getInvDate();
//			v_ide_id:=v_rec.ide_id;
			vIdeId = piInv.getIdeId();
//			v_reason:=v_rec.reason;
			vReason = piInv.getReason();
//			v_add_ref_date:=null;
			vAddRefDate = null;
//			v_add_ref_no:=null;
			vAddRefNo = null;
//			v_due_date:=v_rec.date_payment;
			vDueDate = piInv.getDatePayment();
//			v_cc_par_id:=v_rec.cc_par_id;
			vCcParId = piInv.getCcParId();
//			v_cuy_unit := v_rec.cuy_unit;
			vCuyUnit = piInv.getCuyUnit();

			vMainOutCode = piInv.getOutCode();
			vCuyRate = piInv.getCuyRate();
			vCuyCode = piInv.getCuyCode();

		} else if (piFlag == 3) { //TODO CREATE accounting.lon_postings
			/*
			v_rec:=pi_lon;
                if pi_tte_id is not null then
                   v_tte_id := pi_tte_id;
                else
                   v_tte_id := v_rec.tte_id;
                end if;
                v_post_date:=current_date;
                if pi_amount <> 0 then
                   v_amount_total:= pi_amount;
                else
                   v_amount_total:=v_rec.amount;
                end if;
                v_amount_outstanding:=v_rec.amount_outstanding;
                v_amount_do:=null;
                v_amount_vat:=null;
                v_module:='accounting';
                v_ref_no := v_rec.ref_no;
                v_ref_date := v_rec.ref_date;
                v_cuy_unit := v_rec.cuy_unit;
                select
                                /*(case when pat.bfe_id is null then trim(pat.ref_no)
                                      when pat.bfe_id is not null then trim(bfe.ref_no)
                                else trim(pat.ref_no)
                                end) ref_no,
                                (case when pat.bfe_id is null then pat.date_payment
                                when pat.bfe_id is not null then bfe.date_upload
                                else pat.date_payment
                                end) ref_date,*/
			/*pat.reason,
					(case when pat.type='BAK' and pat.bfe_id is not null then bfe.iban
				when pat.type='BAK' and pat.bfe_id is null then trim(pat.iban)
                                      else (select cdk.code from cash.cah_desks dek, cash.ct_cash_desks cdk
				where dek.id=pat.dek_id and dek.cdk_id=cdk.id) end) dependence_code
				into --v_ref_no,v_ref_date,
						v_reason,v_dependence_code
				from cash.cah_payments pat
				left join cash.bak_files bfe on (pat.bfe_id=bfe.id)
				where pat.id=v_rec.pat_id;
				v_ide_id:=null;
				v_add_ref_date:=null;
				v_add_ref_no:=null;
				v_due_date:=v_rec.due_date;
				v_cc_par_id:=null;
			 */
			/*
			vMainOutCode = vRec.getOutCode();
			vCuyRate = vRec.getCuyRate();
			vCuyCode = vRec.getCuyCode();
			*/
		}

		//raise notice 'in find or create batch 1';
		//logger.trace("in find or create batch 1");

//		select po_cuy_unit, po_cuy_rate
//      into v_cuy_unit,v_cuy_rate
//      from accounting.get_currency_rates(v_rec.out_code,v_rec.cuy_code,v_post_date);
		Map<String, Object> accountingGetCurrencyRatesVar = accountingGetCurrencyRates(vMainOutCode, vCuyCode, vPostDate);
		if (vCuyRate == null) {
			vCuyUnit = (Integer) accountingGetCurrencyRatesVar.get("poCuyUnit");
			vCuyRate = (BigDecimal) accountingGetCurrencyRatesVar.get("poCuyRate");
		} else {
			if( vCuyUnit == null ) {
				vCuyUnit = (Integer) accountingGetCurrencyRatesVar.get("poCuyUnit");
			}
		}

		if (vCuyRate.compareTo(BigDecimal.ZERO) == 0) {
			//raise notice 'in -8';
			logger.error("bridgeFindOrCreateBatch: -8 Missing exchange rate");
			result.put("poBahId", -8L); //-- Nqma waluten kurs
			result.put("poResult", -LoiBreTransitionResult.BRE_TRANSITION_RESULT_NO_EXCHANGE_RATE);
			return result;
		}
		if (vTteId == null) {
			logger.error("bridgeFindOrCreateBatch: -2 Missing FCtTransitionType");
			result.put("poBahId", -2L);
			result.put("poResult", -LoiBreTransitionResult.BRE_TRANSITION_RESULT_NO_TTE);
			return result;
		}

		FCtBatchTteTypeRepository fCtBatchTteTypeRepository = ((FCtBatchTteTypeRepository) getRepositories().getRepositoryFor(FCtBatchTteType.class).get());
		FCtBatchTteType fCtBatchTteType = fCtBatchTteTypeRepository.findFirstByOutCodeAndTteIdAndCompanyAndDeleted(vMainOutCode, vTteId, vMainOutCode.getCompany(), false);
		vBteId = fCtBatchTteType.getBteId();

		vTteType = vTteId.getTteType();
		if(vTteType == null) {
			LoiCTransitionTypeRepository loiCTransitionTypeRepository = ((LoiCTransitionTypeRepository) getRepositories().getRepositoryFor(LoiCTransitionType.class).get());
			vTteType = loiCTransitionTypeRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiCTransitionType.CT_TRANSITION_TYPE_N, vMainOutCode.getCompany(), false);
		}
		vTteSide = vTteId.getTteSide();

		vDefaultCuyCode = accountingMainCurrency(vMainOutCode);
		vAmountTotalCurrency = vAmountTotal;
		vAmountOutstandingCurrency = vAmountOutstanding;
		vAmountDoCurrency = vAmountDo;
		vAmountVatCurrency = vAmountVat;

		if (vCuyCode.getId() != vDefaultCuyCode.getId()) {
			vAmountTotal = ((vAmountTotal.multiply(vCuyRate)).divide(BigDecimal.valueOf(vCuyUnit)).setScale(2, BigDecimal.ROUND_HALF_EVEN));
			vAmountOutstanding = ((vAmountOutstanding.multiply(vCuyRate)).divide(BigDecimal.valueOf(vCuyUnit)).setScale(2, BigDecimal.ROUND_HALF_EVEN));
			vAmountDo = ((vAmountDo.multiply(vCuyRate)).divide(BigDecimal.valueOf(vCuyUnit)).setScale(2, BigDecimal.ROUND_HALF_EVEN));
			vAmountVat = ((vAmountVat.multiply(vCuyRate)).divide(BigDecimal.valueOf(vCuyUnit)).setScale(2, BigDecimal.ROUND_HALF_EVEN));
		}

		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		CaseBuilder caseBuilder = new CaseBuilder();
		QFPtBatch qfPtBatch = QFPtBatch.fPtBatch;

		String vRefNoAndVDependenceCode = (vDependenceCode != null ? vRefNo.trim() + " - " + vDependenceCode.trim() : vRefNo.trim());

		vBahId = queryFactory.select(qfPtBatch)
				.from(qfPtBatch)
				.where(qfPtBatch.outCode.eq(vMainOutCode)
						.and(qfPtBatch.module.upper().eq(vModule.toUpperCase()))
						.and(qfPtBatch.bteId.eq(vBteId))
						.and(qfPtBatch.deleted.eq(false))
						.and(qfPtBatch.refNo.eq(vRefNoAndVDependenceCode)) /*
						(case when  v_dependence_code is not null then coalesce(trim(v_ref_no))||' - '||coalesce(trim(v_dependence_code),'')
                           else trim(v_ref_no)
                           end)*/
						.and(qfPtBatch.refDate.eq(vRefDate))
						.and(qfPtBatch.ideId.id.coalesce(0L).eq(vIdeId == null || vIdeId.getId() == null ? 0L : vIdeId.getId()))
				).fetchFirst();

		//if NOT FOUND then
		if (vBahId == null) {
//			 INSERT INTO pt_batches(
//			            id, post_date, out_code, module,
//			            ref_no, ref_date, amount,
//			            amount_outstanding,
//			            amount2,
//			            amount3,
//			            due_date, date_created, user_created,
//			            descr, bte_id, par_id, ide_id, cuy_code, cuy_rate, cuy_unit,
//			            amount_currency,
//			            amount_outstanding_currency,
//			            amount2_currency,
//			            amount3_currency)
//			        VALUES (nextval('bah_seq'),v_post_date,v_rec.out_code,v_module,
//			               (case when  v_dependence_code is not null then coalesce(trim(v_ref_no))||' - '||coalesce(trim(v_dependence_code),'')
//			                     else trim(v_ref_no)
//			                  end),v_ref_date,v_amount_total,
//			                (case when v_amount_outstanding is not null then v_amount_outstanding
//			                        when v_amount_outstanding is null and v_tte_type ='L' then  v_amount_total
//			                        when v_amount_outstanding is null and v_tte_type = 'P'  /*and v_tte_side in ('CHI','BKI') and v_add_ref_no is null and v_add_ref_date is null */ then v_amount_total
//			                        --when v_amount_outstanding is null and v_tte_type = 'P' and v_tte_side in ('CHI','BKI') and p_rec.add_ref_no is not null and p_rec.add_ref_date is not null then 0
//			                        --when v_amount_outstanding is null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') /* and v_add_ref_no is null and v_add_ref_date is null */ then coalesce(v_amount_total,0)--*(-1)
//			                       -- when v_amount_outstanding is null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') and p_rec.add_ref_no is not null and p_rec.add_ref_date is not null then 0
//			                        else 0
//			                    end),
//			                    v_amount_do,
//			                    v_amount_vat,
//			                    v_due_date,v_rec.date_created,v_rec.user_created,
//			                    v_reason,v_bte_id,v_cc_par_id,v_ide_id,v_rec.cuy_code,v_cuy_rate,v_cuy_unit,
//			                    v_amount_total_currency,
//			                (case when v_amount_outstanding_currency is not null then v_amount_outstanding_currency
//			                        when v_amount_outstanding_currency is null and v_tte_type ='L' then  v_amount_total_currency
//			                        when v_amount_outstanding_currency is null and v_tte_type = 'P'/* and v_tte_side in ('CHI','BKI') and v_add_ref_no is null and v_add_ref_date is null */ then v_amount_total_currency
//			                        --when v_amount_outstanding_currency is null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') /*and v_add_ref_no is null and v_add_ref_date is null */ then coalesce(v_amount_total_currency,0)--*(-1)
//			                        else 0
//			                    end),
//			                    v_amount_do_currency,
//			                    v_amount_vat_currency)returning id into v_bah_id;
			vBahId = new FPtBatch();
			vBahId.setPostDate(vPostDate);
			vBahId.setOutCode(vMainOutCode);
			vBahId.setModule(vModule);
			vBahId.setRefNo(vRefNoAndVDependenceCode);
			vBahId.setRefDate(vRefDate);
			vBahId.setAmount(vAmountTotal);
			vBahId.setAmountOutstanding(vAmountOutstanding != null ? vAmountOutstanding
						: (vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountTotal
							: (vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountTotal : BigDecimal.ZERO))
				);
			vBahId.setAmount2(vAmountDo);
			vBahId.setAmount3(vAmountVat);
			vBahId.setDueDate(vDueDate);
			vBahId.setCreatedDate(vCreatedDate);
			vBahId.setCreatedBy(vCreatedBy);
			vBahId.setDescr(vReason);
			vBahId.setBteId(vBteId);
			vBahId.setParId(vCcParId);
			vBahId.setIdeId(vIdeId);
			vBahId.setCuyCode(vCuyCode);
			vBahId.setCuyRate(vCuyRate);
			vBahId.setCuyUnit(vCuyUnit);
			vBahId.setAmountCurrency(vAmountTotalCurrency);
			vBahId.setAmountOutstandingCurrency(vAmountOutstandingCurrency != null ? vAmountOutstandingCurrency
						: (vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountTotalCurrency
							: (vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountTotalCurrency : BigDecimal.ZERO)));
			vBahId.setAmount2Currency(vAmountDoCurrency);
			vBahId.setAmount3Currency(vAmountVatCurrency);
			vBahId = (FPtBatch) trySave(vBahId, true);

			/*
			if NOT FOUND then
 				raise notice 'in -1 f';
             		return query select -1,-1;
			 */
			if (vBahId == null) {
				logger.error("bridgeFindOrCreateBatch: -1 Couldn't create FPtBatch");
				result.put("poBahId", -1L);
				result.put("poResult", -LoiBreTransitionResult.BRE_TRANSITION_RESULT_FAIL_CREATE_BATCH);
				return result;
			}
			logger.trace("bridgeFindOrCreateBatch: Created new FPtBatch ID "+vBahId.getId());
		} else {
//	        update pt_batches
//	        set date_updated = v_rec.date_created,
			vBahId.setLastModifiedDate(vCreatedDate);
//	            user_updated = v_rec.user_created,
			vBahId.setLastModifiedBy(vCreatedBy);
//	            amount = (case when v_amount_total is not null and v_tte_type ='L' then coalesce(amount,0) + v_amount_total
//	                           when v_amount_total is not null and v_tte_type = 'P' /*and v_tte_side in ('CHI','BKI')*/ then coalesce(amount,0)+ v_amount_total
//	                         --  when v_amount_total is not null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') then coalesce(amount,0)- v_amount_total
//	                           else amount
//	                       end),
			vBahId.setAmount(vAmountTotal != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountTotal.add(vBahId.getAmount() != null ? vBahId.getAmount() : BigDecimal.ZERO)
						: (vAmountTotal != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountTotal.add(vBahId.getAmount() != null ? vBahId.getAmount() : BigDecimal.ZERO)
							: (vBahId.getAmount())));
//	            amount_outstanding = (case when v_amount_outstanding is not null then v_amount_outstanding
//	                                       when v_amount_outstanding is null and v_tte_type ='L' then coalesce(amount_outstanding,0) + v_amount_total
//	                                       when v_amount_outstanding is null and v_tte_type = 'P' /* and v_tte_side in ('CHI','BKI')  and v_add_ref_no is null and v_add_ref_date is null */ then coalesce(amount_outstanding,0)+ v_amount_total
//	                                       --when v_amount_outstanding is null and v_tte_type = 'P' and v_tte_side in ('CHI','BKI') and p_rec.add_ref_no is not null and p_rec.add_ref_date is not null then coalesce(amount_outstanding,0)
//	                                       --when v_amount_outstanding is null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') /* and v_add_ref_no is null and v_add_ref_date is null */ then coalesce(amount_outstanding,0)- v_amount_total
//	                                       --when p_rec.amount_outstanding is null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') and p_rec.add_ref_no is not null and p_rec.add_ref_date is not null then coalesce(amount_outstanding,0)
//	                                       else amount_outstanding
//	                                       end),
			vBahId.setAmountOutstanding(vAmountOutstanding != null ? vAmountOutstanding
					: (vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountTotal.add(vBahId.getAmountOutstanding() != null ? vBahId.getAmountOutstanding() : BigDecimal.ZERO)
						: (vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountTotal.add(vBahId.getAmountOutstanding() != null ? vBahId.getAmountOutstanding() : BigDecimal.ZERO)
							: vBahId.getAmountOutstanding()))
				);
//	            amount2 = (case when v_amount_do is not null and v_tte_type ='L' then coalesce(amount2,0) + v_amount_do
//	                            when v_amount_do is not null and v_tte_type = 'P' /* and v_tte_side in ('CHI','BKI')*/ then coalesce(amount2,0)+ v_amount_do
//	                            --when v_amount_do is not null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') then coalesce(amount2,0)- v_amount_do
//	                            else amount2
//	                       end) ,
			vBahId.setAmount2(vAmountDo != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountDo.add(vBahId.getAmount2() != null ? vBahId.getAmount2() : BigDecimal.ZERO)
					: (vAmountDo != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountDo.add(vBahId.getAmount2() != null ? vBahId.getAmount2() : BigDecimal.ZERO)
						: (vBahId.getAmount2())));
//	            amount3 = (case when v_amount_vat is not null and v_tte_type ='L' then coalesce(amount3,0) + v_amount_vat
//	                            when v_amount_vat is not null and v_tte_type = 'P' /* and v_tte_side in ('CHI','BKI')*/ then coalesce(amount3,0)+ v_amount_vat
//	                           -- when v_amount_vat is not null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') then coalesce(amount3,0)- v_amount_vat
//	                            else amount3
//	                       end) ,
			vBahId.setAmount3(vAmountVat != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountVat.add(vBahId.getAmount3() != null ? vBahId.getAmount3() : BigDecimal.ZERO)
					: (vAmountVat != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountVat.add(vBahId.getAmount3() != null ? vBahId.getAmount3() : BigDecimal.ZERO)
						: (vBahId.getAmount3())));
//	           amount_currency = (case when v_amount_total_currency is not null and v_tte_type ='L' then coalesce(amount_currency,0) + v_amount_total_currency
//	                           when v_amount_total_currency is not null and v_tte_type = 'P' /*and v_tte_side in ('CHI','BKI')*/ then coalesce(amount_currency,0)+ v_amount_total_currency
//	                          -- when v_amount_total_currency is not null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') then coalesce(amount_currency,0)- v_amount_total_currency
//	                           else amount_currency
//	                       end),
			vBahId.setAmountCurrency(vAmountTotalCurrency != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountTotalCurrency.add(vBahId.getAmountCurrency() != null ? vBahId.getAmountCurrency() : BigDecimal.ZERO)
					: (vAmountTotalCurrency != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountTotalCurrency.add(vBahId.getAmountCurrency() != null ? vBahId.getAmountCurrency() : BigDecimal.ZERO)
						: (vBahId.getAmountCurrency())));
//	            amount_outstanding_currency = (case when v_amount_outstanding_currency is not null then v_amount_outstanding_currency
//	                                       when v_amount_outstanding_currency is null and v_tte_type ='L' then coalesce(amount_outstanding_currency,0) + v_amount_total_currency
//	                                       when v_amount_outstanding_currency is null and v_tte_type = 'P' /* and v_tte_side in ('CHI','BKI')  and v_add_ref_no is null and v_add_ref_date is null */ then coalesce(amount_outstanding_currency,0)+ v_amount_total_currency
//	                                       --when v_amount_outstanding_currency is null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') /* and v_add_ref_no is null and v_add_ref_date is null */ then coalesce(amount_outstanding_currency,0)- v_amount_total_currency
//	                                       else amount_outstanding_currency
//	                                       end),
			vBahId.setAmountOutstandingCurrency(vAmountOutstandingCurrency != null ? vAmountOutstandingCurrency
					: (vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountTotalCurrency.add(vBahId.getAmountOutstandingCurrency() != null ? vBahId.getAmountOutstandingCurrency() : BigDecimal.ZERO)
						: (vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountTotalCurrency.add(vBahId.getAmountOutstandingCurrency() != null ? vBahId.getAmountOutstandingCurrency() : BigDecimal.ZERO)
							: vBahId.getAmountOutstandingCurrency()))
				);
//	            amount2_currency = (case when v_amount_do_currency is not null and v_tte_type ='L' then coalesce(amount2_currency,0) + v_amount_do_currency
//	                            when v_amount_do_currency is not null and v_tte_type = 'P' /*and v_tte_side in ('CHI','BKI') */then coalesce(amount2_currency,0)+ v_amount_do_currency
//	                            --when v_amount_do_currency is not null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') then coalesce(amount2_currency,0)- v_amount_do_currency
//	                            else amount2_currency
//	                       end) ,
			vBahId.setAmount2Currency(vAmountDoCurrency != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountDoCurrency.add(vBahId.getAmount2Currency() != null ? vBahId.getAmount2Currency() : BigDecimal.ZERO)
					: (vAmountDoCurrency != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountDoCurrency.add(vBahId.getAmount2Currency() != null ? vBahId.getAmount2Currency() : BigDecimal.ZERO)
						: (vBahId.getAmount2Currency())));
//	            amount3_currency = (case when v_amount_vat_currency is not null and v_tte_type ='L' then coalesce(amount3_currency,0) + v_amount_vat_currency
//	                            when v_amount_vat_currency is not null and v_tte_type = 'P' /* and v_tte_side in ('CHI','BKI')*/ then coalesce(amount3_currency,0)+ v_amount_vat_currency
//	                          --  when v_amount_vat_currency is not null and v_tte_type = 'P' and v_tte_side in ('CHE','BKE') then coalesce(amount3_currency,0)- v_amount_vat_currency
//	                            else amount3_currency
//	                       end)
			vBahId.setAmount3Currency(vAmountVatCurrency != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_L ? vAmountVatCurrency.add(vBahId.getAmount3Currency() != null ? vBahId.getAmount3Currency() : BigDecimal.ZERO)
					: (vAmountVatCurrency != null && vTteType.getListOptionItemCode() == LoiCTransitionType.CT_TRANSITION_TYPE_P ? vAmountVatCurrency.add(vBahId.getAmount3Currency() != null ? vBahId.getAmount3Currency() : BigDecimal.ZERO)
						: (vBahId.getAmount3Currency())));
//	        where id=v_bah_id;
			vBahId = (FPtBatch) trySave(vBahId, true);
			/*
			if NOT FOUND then
 				raise notice 'in -1 f1';
         			 return query select -1,-1;
			 */
			if (vBahId == null) { //check for errors
				logger.error("bridgeFindOrCreateBatch: -1 Couldn't update FPtBatch");
				result.put("poBahId", -1L);
				result.put("poResult", -LoiBreTransitionResult.BRE_TRANSITION_RESULT_FAIL_UPDATE_BATCH);
				return result;
			}
			
			logger.trace("bridgeFindOrCreateBatch: Updated existing FPtBatch ID "+vBahId.getId());
		}

		if (vAddRefNo != null && vAddRefDate != null) {
			QFCtBatchTteLink qfCtBatchTteLink = QFCtBatchTteLink.fCtBatchTteLink;

			FCtBatchType vLinkBteId = queryFactory.select(qfCtBatchTteLink.bteId)
					.from(qfCtBatchTteLink)
					.where(qfCtBatchTteLink.outCode.eq(vMainOutCode)
							.and(qfCtBatchTteLink.tteId.eq(vTteId))
							.and(qfCtBatchTteLink.deleted.eq(false))
							.and(qfCtBatchTteLink.status.listOptionItemCode.eq( LoiBatchTteLinkStatus.BATCH_TTE_RULE_LINK_STATUS_A ))
					).fetchFirst();

			/*
			if not found then
				 raise notice 'in -10';
             return query select v_bah_id,-10;
			 */
			if (vLinkBteId == null) {
				logger.error("bridgeFindOrCreateBatch: -10 Missing FCtBatchType");
				result.put("poBahId", vBahId.getId());
				result.put("poResult", -LoiBreTransitionResult.BRE_TRANSITION_RESULT_NO_BATCH_TYPE);
				return result;
			}

			FPtBatch vLinkBahId = queryFactory.select(qfPtBatch)
					.from(qfPtBatch)
					.where(qfPtBatch.outCode.eq(vMainOutCode)
							.and(qfPtBatch.bteId.eq(vLinkBteId))
							.and(qfPtBatch.refNo.trim().eq(vAddRefNo.trim()))
							.and(qfPtBatch.refDate.eq(vAddRefDate))
							.and(qfPtBatch.deleted.eq(false))
							.and(qfPtBatch.parId.id.coalesce(0L).eq(vCcParId != null ? vCcParId.getId() : 0L))
					).fetchFirst();

			if (vLinkBahId != null) {
				/*
				INSERT INTO pt_batch_links(
                        id, bah_id1, bah_id2, flag1, flag2, amount, amount_currency,
                        cuy_code, cuy_rate, cuy_unit, status, user_created)
            VALUES (nextval('blk_seq'),v_link_bah_id,v_bah_id,(select "type" from ct_batch_types where id=v_link_bte_id),v_tte_type,v_amount_total,v_amount_total_currency,
                        v_rec.cuy_code,v_cuy_rate,v_cuy_unit,'A',v_rec.user_created);
				 */
				FPtBatchLink newLink = new FPtBatchLink();
				newLink.setBahId1(vLinkBahId);
				newLink.setBahId2(vBahId);
				LoiLinkedFlagRepository loiLinkedFlagRepository = ((LoiLinkedFlagRepository) getRepositories().getRepositoryFor(LoiLinkedFlag.class).get());
				LoiLinkedFlag loiLinkedFlag1 = null;
				if(vLinkBteId.getType().getListOptionItemCode() == LoiBatchCalculationType.BATCH_CALCULATION_TYPE_L) {
					loiLinkedFlag1 = loiLinkedFlagRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiLinkedFlag.LINKED_SIDE_L,vMainOutCode.getCompany(), false);
				} else if(vLinkBteId.getType().getListOptionItemCode() == LoiBatchCalculationType.BATCH_CALCULATION_TYPE_P) {
					loiLinkedFlag1 = loiLinkedFlagRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiLinkedFlag.LINKED_SIDE_P,vMainOutCode.getCompany(), false);
				} else {
					logger.info("bridgeFindOrCreateBatch: Not a valid LoiLinkedFlag option from LoiBatchCalculationType " + vLinkBteId.getType().getListOptionItemCode());
				}
				newLink.setFlag1(loiLinkedFlag1);
				LoiLinkedFlag loiLinkedFlag2 = null;
				if(vTteType.getListOptionItemCode() == vTteType.CT_TRANSITION_TYPE_L) {
					loiLinkedFlag2 = loiLinkedFlagRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiLinkedFlag.LINKED_SIDE_L,vMainOutCode.getCompany(), false);
				} else if(vTteType.getListOptionItemCode() == vTteType.CT_TRANSITION_TYPE_P) {
					loiLinkedFlag2 = loiLinkedFlagRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiLinkedFlag.LINKED_SIDE_P,vMainOutCode.getCompany(), false);
				} else {
					logger.info("bridgeFindOrCreateBatch: Not a valid LoiLinkedFlag option from LoiCTransitionType " + vTteType.getListOptionItemCode());
				}
				newLink.setFlag2(loiLinkedFlag2);
				newLink.setAmount(vAmountTotal);
				newLink.setAmountCurrency(vAmountTotalCurrency);
				newLink.setCuyCode(vCuyCode);
				newLink.setCuyRate(vCuyRate);
				newLink.setCuyUnit(vCuyUnit);
				LoiPtBatchLinkStatusRepository loiPtBatchLinkStatusRepository = ((LoiPtBatchLinkStatusRepository) getRepositories().getRepositoryFor(LoiPtBatchLinkStatus.class).get());
				LoiPtBatchLinkStatus loiPtBatchLinkStatusA = loiPtBatchLinkStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtBatchLinkStatus.PT_BATCH_LINK_STATUS_A, vMainOutCode.getCompany(), false);
				newLink.setStatus(loiPtBatchLinkStatusA);
				newLink.setCreatedBy(vCreatedBy);
				newLink = (FPtBatchLink) trySave(newLink, true);
				if(newLink == null) { //check for errors
					logger.error("bridgeFindOrCreateBatch: -1 Couldn't create FPtBatchLink");
					result.put("poBahId", -1L);
					result.put("poResult", -LoiBreTransitionResult.BRE_TRANSITION_RESULT_FAIL_CREATE_BATCH_LINK);
					return result;
				}

				vBahId.setAmountOutstanding((vBahId.getAmountOutstanding() != null ? vBahId.getAmountOutstanding() : BigDecimal.ZERO).subtract(vAmountTotal));
				vBahId.setAmountOutstandingCurrency((vBahId.getAmountOutstandingCurrency() != null ? vBahId.getAmountOutstandingCurrency() : BigDecimal.ZERO).subtract(vAmountTotalCurrency));
				vBahId.setLastModifiedDate(vCreatedDate);
				vBahId.setLastModifiedBy(vCreatedBy);
				vBahId = (FPtBatch) trySave(vBahId, true);
				if (vBahId == null) { //check for errors
					logger.error("bridgeFindOrCreateBatch: -1 Couldn't update amounts of FPtBatch");
					result.put("poBahId", -1L);
					result.put("poResult", -LoiBreTransitionResult.BRE_TRANSITION_RESULT_FAIL_UPDATE_BATCH_AMOUNT);
					return result;
				}
				
				vLinkBahId.setAmountOutstanding((vLinkBahId.getAmountOutstanding() != null ? vLinkBahId.getAmountOutstanding() : BigDecimal.ZERO).subtract(vAmountTotal));
				vLinkBahId.setAmountOutstandingCurrency((vLinkBahId.getAmountOutstandingCurrency() != null ? vLinkBahId.getAmountOutstandingCurrency() : BigDecimal.ZERO).subtract(vAmountTotalCurrency));
				vLinkBahId.setLastModifiedDate(vCreatedDate);
				vLinkBahId.setLastModifiedBy(vCreatedBy);
				vLinkBahId = (FPtBatch) trySave(vLinkBahId, true);

				if (vLinkBahId == null) { //check for errors
					logger.error("bridgeFindOrCreateBatch: -1 Couldn't update amounts of link FPtBatch");
					result.put("poBahId", -1L);
					result.put("poResult", -LoiBreTransitionResult.BRE_TRANSITION_RESULT_FAIL_UPDATE_LINK_BATCH_AMOUNT);
					return result;
				}
			} else {
				logger.error("bridgeFindOrCreateBatch: -11 Missing link FPtBatch");
				result.put("poBahId", vBahId.getId());
				result.put("poResult", -LoiBreTransitionResult.BRE_TRANSITION_RESULT_NO_LINK_BATCH);
				return result;
			}
		}

		logger.trace("bridgeFindOrCreateBatch: Success! FPtBatch ID "+vBahId.getId());
		result.put("poBahId", vBahId.getId());
		result.put("poResult", LoiBreTransitionResult.BRE_TRANSITION_RESULT_OK);
		return result;
	}

	private CCcOrganizationUnit registerGetMainOutCode(CCcOrganizationUnit pOutCode) {

		CCcOrganizationUnit vMainOutCode = pOutCode;

		while(vMainOutCode.getOutId() != null) {
			vMainOutCode = vMainOutCode.getOutId();
		}

		return vMainOutCode;
	}

	private CCtCurrency accountingMainCurrency(CCcOrganizationUnit outCode) { //TODO CREATE register.cfg_parameters
		/*
		select sp_val
    into v_result
    from register.cfg_parameters
    where sp_name='DEFAULT_CURRENCY'
      and out_code=p_out_code;

    if NOT FOUND then

           select sp_val
           into v_result
           from register.cfg_parameters
           where sp_name='DEFAULT_CURRENCY'
             and out_code=register.get_main_outcode(p_out_code);

           if NOT FOUND then

              v_result := null;

           end if;

    end if;

    return v_result;
		 */
		CCtCurrencyRepository cCtCurrencyRepository = ((CCtCurrencyRepository) getRepositories().getRepositoryFor(CCtCurrency.class).get());
		return cCtCurrencyRepository.findFirstByCodeAndCompanyAndDeleted("BGN", outCode.getCompany(), false);
	}

	private Map<String, Object> accountingGetCurrencyRates(CCcOrganizationUnit piOutCode, CCtCurrency piCuyCode, LocalDate piPostDate) {
		Map<String,Object> result = new HashMap<String,Object>();
		Integer vCuyUnit = null;
		BigDecimal vCuyRate = null;
		
		/*                                  This comment is original comment which is in the source!!!
	       select cuy.is_default
	       into v_is_def
	       from register.ct_currencies cuy
	       where cuy.code=pi_cuy_code
	       and pi_post_date between cuy.active_from_date and cuy.active_to_date; */

	         /*                      This comment is created because the table register.cfg_parameters  not exist.
            	select par.sp_val
            	into v_def_cuy
            	from  register.cfg_parameters par
            	where par.sp_name ='DEFAULT_CURRENCY'
            	and par.out_code=register.get_main_outcode(pi_out_code);*/

            /*	IF v_def_cuy != pi_cuy_code then
	            	select cue.unit_of_cuy,cue.in_main_cuy
	            	into v_cuy_unit,v_cuy_rate
	            	from register.pmt_currency_rates cue
	            	where cue.cuy_code=pi_cuy_code
	            	and cue.out_code=pi_out_code
	            	and pi_post_date between cue.date_from and cue.date_to;*/
		CPmtCurrencyRateRepository cPmtCurrencyRateRepository = ((CPmtCurrencyRateRepository) getRepositories().getRepositoryFor(CPmtCurrencyRate.class).get());
		CPmtCurrencyRate rate = cPmtCurrencyRateRepository.findFirstByCuyCodeAndDateFromLessThanEqualAndDateToGreaterThanEqualAndCompanyAndDeleted(piCuyCode, piPostDate, piPostDate, piOutCode.getCompany(), false);

	            //	IF NOT FOUND THEN
		if(rate == null) {
				/*	select cue.unit_of_cuy,cue.in_main_cuy
		            	into v_cuy_unit,v_cuy_rate
		            	from register.pmt_currency_rates cue
		            	where cue.cuy_code=pi_cuy_code
		            	and cue.out_code=register.get_main_outcode(pi_out_code)
		            			--and out.code=pi_out_code
		            	and pi_post_date between cue.date_from and cue.date_to;*/
	
		            	/*IF NOT FOUND THEN
			            	v_cuy_unit := 0;
			            	v_cuy_rate := 0;
		            	end if;
	            	end if;
            	elsif v_def_cuy = pi_cuy_code then
	            	v_cuy_rate := 1;
	            	v_cuy_unit := 1;
	            else
	            	v_cuy_rate := 0;
	            	v_cuy_unit := 0;
            	end if;
            	return query select v_cuy_rate,v_cuy_unit;
	             */
			vCuyUnit = 0;
			vCuyRate = BigDecimal.ZERO;
		} else {
			vCuyUnit = rate.getUnitOfCuy();
			vCuyRate = rate.getInMainCuy();
		}
		result.put("poCuyUnit", vCuyUnit);
		result.put("poCuyRate", vCuyRate);
		return result;
	}

	private Integer bridgeCheckBteTteType(CCcOrganizationUnit cCcOrganizationUnit, FCtTransitionType tteCodeId) {
		FCtBatchTteTypeRepository fCtBatchTteTypeRepository = ((FCtBatchTteTypeRepository) getRepositories().getRepositoryFor(FCtBatchTteType.class).get());
		FCtBatchTteType fCtBatchTteType = fCtBatchTteTypeRepository.findFirstByOutCodeAndTteIdAndCompanyAndDeleted(cCcOrganizationUnit, tteCodeId, cCcOrganizationUnit.getCompany(), false);

		if (fCtBatchTteType != null) {
			return 0;
		}
		logger.error("bridgeCheckBteTteType: 1 Couldn't find FCtBatchTteType");
		return 1;
	}

	private Integer bridgeCheckRequiredCostCenters(FBreTransition piBre, FInvInvoice piInv, int piFlag, FPtBatchCcDetail piBcl) { //TODO CREATE accounting.inv_invoices TABLE
		CCcOrganizationUnit vRecOutCode = null;;
		Long vRecTteId = null;;
		LoiBatchTypeRuleDependenceType vRecDependenceType = null;;
		Long vRecDependenceId = null;;
		FCcEbk ccEbkId = null;;
		FCcFunction ccFunId = null;;
		FCcProgram ccPrmId = null;;
		FCcFinsource ccFieId = null;;
		CCcPartner ccParId = null;;
		CCcGoodsType ccGteId = null;;
		FCcContract ccCotId = null;;
		CCcOrganizationUnit ccOutId = null;;
		FCcReserve1 ccRe1Id = null;;
		FCcReserve2 ccRe2Id = null;;

		if (piFlag == 1) {
			ccEbkId = piBre.getCcEbkId();
			ccFunId = piBre.getCcFunId();
			ccPrmId = piBre.getCcPrmId();
			ccFieId = piBre.getCcFieId();
			ccParId = piBre.getCcParId();
			ccGteId = piBre.getCcGteId();
			ccCotId = piBre.getCcCotId();
			ccOutId = piBre.getCcOutId();
			ccRe1Id = piBre.getCcRe1Id();
			ccRe2Id = piBre.getCcRe2Id();
			vRecOutCode = piBre.getOutCode();
			if(piBre.getDependenceCode() != null) {
				vRecDependenceId = vwRuleDependenceId(piBre.getDependenceType().getListOptionItemCode(), piBre.getDependenceCode(), piBre.getOutCode());
			} else {
				vRecDependenceId = null;
			}
			vRecDependenceType = piBre.getDependenceType();
			vRecTteId = piBre.getTteCode().getId();
		} else if (piFlag == 2) {
//			vRec = piInv;
			ccEbkId = piInv.getCcEbkId();
			ccFunId = piInv.getCcFunId();
			ccPrmId = piInv.getCcPrmId();
			ccFieId = piInv.getCcFieId();
			ccParId = piInv.getCcParId();
			ccGteId = piInv.getCcGteId();
			ccCotId = piInv.getCcCotId();
			ccOutId = piInv.getCcOutId();
			ccRe1Id = piInv.getCcRe1Id();
			ccRe2Id = piInv.getCcRe2Id();
			vRecOutCode = piInv.getOutCode();
//			v_dependence_id:=null;
			vRecDependenceId = null;
//			v_dependence_type:=null;
			vRecDependenceType = null;
//			v_tte_id := v_rec.tte_id;
			vRecTteId = piInv.getTteId().getId();

		} else if (piFlag == 3) {
			ccEbkId = piBcl.getCcEbkId();
			ccFunId = piBcl.getCcFunId();
			ccPrmId = piBcl.getCcPrmId();
			ccFieId = piBcl.getCcFieId();
			ccParId = piBcl.getCcParId();
			ccGteId = piBcl.getCcGteId();
			ccCotId = piBcl.getCcCotId();
			ccOutId = piBcl.getCcOutId();
			ccRe1Id = piBcl.getCcRe1Id();
			ccRe2Id = piBcl.getCcRe2Id();
			vRecOutCode = piBcl.getOutCode();
			vRecDependenceId = piBcl.getDependenceId();
			vRecDependenceType = piBcl.getDependenceType();
			vRecTteId = piBcl.getTteId().getId();
		}

		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);

		QFCtBatchTypeRule qFCtBatchTypeRule = QFCtBatchTypeRule.fCtBatchTypeRule;
		List<FCtBatchTypeRule> vRecBTR = queryFactory.select(qFCtBatchTypeRule)
				.from(qFCtBatchTypeRule)
				.where(qFCtBatchTypeRule.outCode.id.eq(vRecOutCode.getId())
						.and(qFCtBatchTypeRule.tteId.id.eq(vRecTteId))
						.and(qFCtBatchTypeRule.deleted.eq(false))
						.and(vRecDependenceType == null ?
								qFCtBatchTypeRule.dependenceType.isNull()
								: qFCtBatchTypeRule.dependenceType.listOptionItemCode.eq(vRecDependenceType.getListOptionItemCode()))
						.and(qFCtBatchTypeRule.dependenceId.coalesce(-1L).eq(vRecDependenceId == null ? -1L : vRecDependenceId))
				)
				.fetch();

		logger.trace("bridgeCheckRequiredCostCenters: checking " + vRecBTR.size() + " FCtBatchTypeRules");
		for(FCtBatchTypeRule btr : vRecBTR) {
			if(btr.getCoaIdCt() != null && btr.getCoaIdDt() != null && (
					(btr.getCoaIdCt().getOutRequired() || btr.getCoaIdDt().getOutRequired()) && ccOutId == null
					|| (btr.getCoaIdCt().getEbkRequired() || btr.getCoaIdDt().getEbkRequired()) && ccEbkId == null
					|| (btr.getCoaIdCt().getFunRequired() || btr.getCoaIdDt().getFunRequired()) && ccFunId == null
					|| (btr.getCoaIdCt().getPrmRequired() || btr.getCoaIdDt().getPrmRequired()) && ccPrmId == null
					|| (btr.getCoaIdCt().getFieRequired() || btr.getCoaIdDt().getFieRequired()) && ccFieId == null
					|| (btr.getCoaIdCt().getParRequired() || btr.getCoaIdDt().getParRequired()) && ccParId == null
					|| (btr.getCoaIdCt().getGteRequired() || btr.getCoaIdDt().getGteRequired()) && ccGteId == null
					|| (btr.getCoaIdCt().getCotRequired() || btr.getCoaIdDt().getCotRequired()) && ccCotId == null
					|| (btr.getCoaIdCt().getRe1Required() || btr.getCoaIdDt().getRe1Required()) && ccRe1Id == null
					|| (btr.getCoaIdCt().getRe2Required() || btr.getCoaIdDt().getRe2Required()) && ccRe2Id == null
				)) {
				logger.error("bridgeCheckRequiredCostCenters: FCtBatchTypeRule " + btr.getId() + " doesn't have the required cost centers");
				return 100; //if we want to check that all are OK
			}
		}

		return 0; //if we want to check that all are OK
	}

	private String getAllChilledOut(String pCode) { //TODO implementation

		Integer[] parents = new Integer[4];
		Integer[] children = new Integer[4];

		/*
             		CREATE FUNCTION accounting.get_all_chiled_out(pi_id integer) RETURNS integer[]
                 LANGUAGE plpgsql
                 AS $$
             DECLARE
                 process_parents INT4[];
                 --v_out_id int4;
                 children INT4[] := '{}';
                 new_children INT4[];
             BEGIN

              process_parents:=ARRAY[pi_id];
              children:=children||process_parents;
                 WHILE ( array_upper( process_parents, 1 ) IS NOT NULL ) LOOP
                     new_children := ARRAY( SELECT id FROM register.cc_organization_units WHERE out_id = ANY( process_parents ) AND id <> ALL( children ) );
                     children := children || new_children;
                     process_parents := new_children;
                 END LOOP;
                 RETURN children;
             END;
             $$;
		 */
		return null;
	}

	private Integer bridgeCheckRules(CCcOrganizationUnit pOutCode, FCtTransitionType pTteId, LoiBatchTypeRuleDependenceType dependenceType, Long dependenceId, LocalDate postDate) {
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QFCtBatchTypeRule qfCtBatchTypeRule = QFCtBatchTypeRule.fCtBatchTypeRule;
		QLoiBatchTypeRuleDependenceType qLoiBatchTypeRuleDependenceType = QLoiBatchTypeRuleDependenceType.loiBatchTypeRuleDependenceType;

		List<Long> vBtrId = queryFactory.select(qfCtBatchTypeRule.id)
			.from(qfCtBatchTypeRule)
			.leftJoin(qLoiBatchTypeRuleDependenceType).on(qLoiBatchTypeRuleDependenceType.id.eq(qfCtBatchTypeRule.dependenceType.id)
					.and(qLoiBatchTypeRuleDependenceType.deleted.isFalse())
				)
			.where(qfCtBatchTypeRule.tteId.eq(pTteId)
				.and(qfCtBatchTypeRule.outCode.eq(pOutCode))
				.and(qfCtBatchTypeRule.deleted.eq(false))
				.and(dependenceType == null ?
						qfCtBatchTypeRule.dependenceType.isNull()
						: qfCtBatchTypeRule.dependenceType.listOptionItemCode.eq(dependenceType.getListOptionItemCode()))
				.and(qfCtBatchTypeRule.dependenceId.coalesce(-1L).eq(dependenceId == null ? -1L : dependenceId))
				.and(qfCtBatchTypeRule.activeFromDate.loe(postDate))
				.and(qfCtBatchTypeRule.activeToDate.goe(postDate)))
			.fetch();
		
		if (vBtrId.size() == 0) {
			logger.error("bridgeCheckRules: 2 No rules found");
			return 2;
		}
		logger.trace("bridgeCheckRules: Success");
		return 0;
	}

	//bridge.create_journal
	private FPtJournal bridgeCreateJournal(FPtBatch pBahId, FJournalType pJteId, FBreTransition piBre, FInvInvoice piInv, Object piLon, int piFlag, FPtBatchCcDetail piBcl) {

		SecUser vCreatedBy = null;
		Date vCreatedDate = null;
		FPtJournal vJolId = null;
		Integer vCuyUnit = null;
		BigDecimal vCuyRate = null;
		CCtCurrency vCuyCode = null;
		LocalDate vPostDate = null;
		String vReason = null;
		CCcOrganizationUnit vOutCode = null;

		if (piFlag == 1) {
			vCreatedBy = piBre.getCreatedBy();
			vCreatedDate = piBre.getCreatedDate();
			vPostDate = piBre.getPostDate();
			vReason = piBre.getDescr();
			vCuyRate = piBre.getCuyRate();
			vCuyUnit = null;
			vCuyCode = piBre.getCuyCode();
			vOutCode = piBre.getOutCode();
		} else if (piFlag == 2) {

//		    v_rec:=pi_inv;
			vCreatedBy = piInv.getCreatedBy();
			vCreatedDate = piInv.getCreatedDate();
//          v_post_date:=pi_inv.date_account;
        	vPostDate = piInv.getDateAccount();
//          v_reason:=v_rec.reason;
			vReason = piInv.getReason();
//          v_cuy_rate:= v_rec.cuy_rate;
			vCuyRate = piInv.getCuyRate();
//         v_cuy_unit := v_rec.cuy_unit;
			vCuyUnit = piInv.getCuyUnit();

			vCuyCode = piInv.getCuyCode();
			vOutCode = piInv.getOutCode();
		} else if (piFlag == 3) {
			/*
		    	v_rec:=pi_lon;
            v_post_date:=current_date;
            v_cuy_rate:= v_rec.cuy_rate;
            v_cuy_unit := v_rec.cuy_unit;
            select pat.reason
            into v_reason
            from cash.cah_payments pat
            where pat.id=v_rec.pat_id;
			 */
		} else if (piFlag == 4) {
			vCreatedBy = piBcl.getCreatedBy();
			vCreatedDate = piBcl.getCreatedDate();
			vPostDate = piBcl.getPostDate();
			vReason = pBahId.getDescr();
			vCuyRate = pBahId.getCuyRate();
			vCuyUnit = pBahId.getCuyUnit();
			vCuyCode = piBcl.getCuyCode();
			vOutCode = piBcl.getOutCode();
		}

		/*        select coalesce(main_out_code,v_rec.out_code)
        into v_out_code
        from register.cc_organization_units
        where code=v_rec.out_code; */

		vOutCode = registerGetMainOutCode(vOutCode);

		Map<String, Object> accountingGetCurrencyRatesVar = accountingGetCurrencyRates(vOutCode, vCuyCode, vPostDate);
		if(piFlag != 4) {
			if(vCuyUnit == null) {
				vCuyUnit = (Integer) accountingGetCurrencyRatesVar.get("poCuyUnit");
			}
		} else if(vCuyRate == null) {
			vCuyUnit = (Integer) accountingGetCurrencyRatesVar.get("poCuyUnit");
			vCuyRate = (BigDecimal) accountingGetCurrencyRatesVar.get("poCuyRate");
		}

		if (vCuyRate == null || vCuyRate.compareTo(BigDecimal.ZERO) == 0) {
			//result = -8;
			logger.error("bridgeCreateJournal: -8 Missing exchange rate");
			return null;
		}

		/*
	    	INSERT INTO pt_journals(
                id, bah_id, post_date, out_code, jte_id, status,
                date_created, user_created, descr, cuy_code, cuy_rate, cuy_unit)
         values
               (nextval('jol_seq'), p_bah_id, v_post_date, v_out_code, p_jte_id, 'C',
                v_rec.date_created, v_rec.user_created, coalesce(v_reason,'')/*||coalesce(' - '||p_rec.add_ref_no,'')*//*, v_rec.cuy_code, v_cuy_rate, v_cuy_unit)
	    	returning id into v_jol_id;
		 */

		LoiPtJournalStatusRepository loiPtJournalStatusRepository = ((LoiPtJournalStatusRepository) getRepositories().getRepositoryFor(LoiPtJournalStatus.class).get());
		LoiPtJournalStatus loiPtJournalStatusC = loiPtJournalStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtJournalStatus.PT_JOURNAL_STATUS_C, vOutCode.getCompany(), false);

		vJolId = new FPtJournal();
		vJolId.setBahId(pBahId);
		vJolId.setPostDate(vPostDate);
		vJolId.setOutCode(vOutCode);
		vJolId.setJteId(pJteId);
		vJolId.setStatus(loiPtJournalStatusC);
		vJolId.setCreatedDate(vCreatedDate);
		vJolId.setCreatedBy(vCreatedBy);
		vJolId.setDescr(vReason != null ? vReason : "");
		vJolId.setCuyCode(vCuyCode);
		vJolId.setCuyRate(vCuyRate);
		vJolId.setCuyUnit(vCuyUnit);
		if(trySave(vJolId, true) == null) {
			throw new ReportException("bridgeCreateJournal couldn't create FPtJournal");
		}

		logger.trace("bridgeCreateJournal: Created new FPtJournal ID "+vJolId.getId());
		return vJolId;
	}

	//	CREATE FUNCTION bridge.new_posting(p_bah_id integer, p_jol_id integer, p_jte_id integer, p_rue_id integer, pi_bre register.bre_transitions DEFAULT NULL::register.bre_transitions, pi_inv accounting.inv_invoices DEFAULT NULL::accounting.inv_invoices, pi_flag character DEFAULT '1'::bpchar, pi_bcl accounting.pt_batch_cc_details DEFAULT NULL::accounting.pt_batch_cc_details) RETURNS integer
	//	    LANGUAGE plpgsql
	//	    AS $$
	//	DECLARE
	private Long bridgeNewPosting(FPtBatch pBahId, FPtJournal pJolId, FJournalType pJteId, FCtRule pRueId, FBreTransition piBre, FInvInvoice piInv, int piFlag, FPtBatchCcDetail piBcl){
	//	    v_cuy_unit integer;
	//	    v_cuy_rate numeric(15,6);
	//	    v_rec record;
	//	    v_post_date date;
	//	    v_amount_total numeric(15,2);
	//	    v_amount_outstanding numeric(15,2);
	//	    v_amount_do numeric(15,2);
	//	    v_amount_vat numeric(15,2);
	//	    v_amount_advance numeric(15,2);
	//	    v_reason varchar(255);
	//	    v_add_ref_no varchar(15);
	//	    v_cost numeric(15,2);
	//	    v_tte_id integer;
	//	    v_out_code varchar(15);
		SecUser vCreatedBy = null;
		Date vCreatedDate = null;
		ManagedCompany vCompany = null; 
		LocalDate vPostDate = null;
		CCcOrganizationUnit vRecOutCode = null;
		BigDecimal vAmountTotal = null;
		BigDecimal vAmountOutstanding = null;
		BigDecimal vAmountDo = null;
		BigDecimal vAmountVat = null;
		BigDecimal vCost = null;
		BigDecimal vAmountAdvance = null;
		String vReason = null;
		String vAddRefNo = null;
		FCcEbk vCcEbkId = null;
		FCcFunction vCcFunId = null;
		FCcProgram vCcPrmId = null;
		FCcFinsource vCcFieId = null;
		CCcPartner vCcParId = null;
		CCcGoodsType vCcGteId = null;
		FCcContract vCcCotId = null;
		CCcOrganizationUnit vCcOutId = null;
		FCcReserve1 vCcRe1Id = null;
		FCcReserve2 vCcRe2Id = null;
		
		FCtTransitionType vTteId = null;
		LoiBatchTypeRuleDependenceType vDependenceType = null;
		Long vDependenceId = null;
		CCcOrganizationUnit vOutCode = null;
	//	BEGIN
	//	    if pi_flag='1' then
		if(piFlag == 1) {
	//		raise notice 'jol id %',p_jol_id;
	//			v_rec:=pi_bre;
			FBreTransition vRec = piBre;
	//	        raise notice 'v_rec %',v_rec;
			vCreatedBy = vRec.getCreatedBy();
			vCreatedDate = vRec.getCreatedDate();
			vCompany = vRec.getCompany();
			vRecOutCode = vRec.getOutCode();
			vCcEbkId = vRec.getCcEbkId();
			vCcFunId = vRec.getCcFunId();
			vCcPrmId = vRec.getCcPrmId();
			vCcFieId = vRec.getCcFieId();
			vCcParId = vRec.getCcParId();
			vCcGteId = vRec.getCcGteId();
			vCcCotId = vRec.getCcCotId();
			vCcOutId = vRec.getCcOutId();
			vCcRe1Id = vRec.getCcRe1Id();
			vCcRe2Id = vRec.getCcRe2Id();
			
			vOutCode = vRec.getOutCode();
	//			v_post_date:=pi_bre.post_date;
			vPostDate = piBre.getPostDate();
	//		raise notice 'v_post_date %',v_post_date;
	//			v_amount_total:=v_rec.amount_total;
			vAmountTotal = vRec.getAmountTotal();
	//	        raise notice 'amount total %',v_amount_total;
	//			v_amount_outstanding:=v_rec.amount_outstanding;
			vAmountOutstanding = vRec.getAmountOutstanding();
	//	        raise notice 'v_amount_outstanding %',v_amount_outstanding;
	//			v_amount_do:=v_rec.amount_do;
			vAmountDo = vRec.getAmountDo();
	//	        raise notice 'v_amount_do %',v_amount_do;
	//			v_amount_vat:=v_rec.amount_vat;
			vAmountVat = vRec.getAmountVat();
	//	        raise notice 'v_amount_vat %',v_amount_vat;
	//	        v_amount_advance :=v_rec.advance;
			vAmountAdvance = vRec.getAdvance();
	//	        raise notice 'v_amount_advance %',v_amount_advance;
	//			v_reason :=v_rec.descr;
			vReason = vRec.getDescr();
	//	        raise notice 'v_reason %',v_reason;
	//			v_add_ref_no:=v_rec.add_ref_no;
			vAddRefNo = vRec.getAddRefNo();
	//	        raise notice 'v_add_ref_no %',v_add_ref_no;
	//			v_cost:=v_rec.cost;
			vCost = vRec.getCost();
	//			select tte.id
	//			into v_tte_id
	//			from register.ct_transition_types tte
	//			where tte.code=v_rec.tte_code;
			vTteId = vRec.getTteCode();
	//		elsif pi_flag='2' then 
		} else if(piFlag == 2) {
	//	        v_rec:=pi_inv;
			vCreatedBy = piInv.getCreatedBy();
			vCreatedDate = piInv.getCreatedDate();
			vCompany = piInv.getCompany();
			vRecOutCode = piInv.getOutCode();
			vCcEbkId = piInv.getCcEbkId();
			vCcFunId = piInv.getCcFunId();
			vCcPrmId = piInv.getCcPrmId();
			vCcFieId = piInv.getCcFieId();
			vCcParId = piInv.getCcParId();
			vCcGteId = piInv.getCcGteId();
			vCcCotId = piInv.getCcCotId();
			vCcOutId = piInv.getCcOutId();
			vCcRe1Id = piInv.getCcRe1Id();
			vCcRe2Id = piInv.getCcRe2Id();
			
			vOutCode = piInv.getOutCode();
	//	        v_post_date:=pi_inv.date_account;
			vPostDate = piInv.getDateAccount();
	//	        v_amount_total:=v_rec.total_amount;
			vAmountTotal = piInv.getTotalAmount();
	//	        v_amount_outstanding:=null;
			vAmountOutstanding = null;
	//	        v_amount_vat:=v_rec.vat_amount;
			vAmountVat = piInv.getVatAmount();
	//	        v_amount_do:=v_rec.tax_base;
			vAmountDo = piInv.getTaxBase();
	//	        v_amount_advance :=0;
			vAmountAdvance = BigDecimal.ZERO;
	//	        v_reason:=v_rec.reason;
			vReason = piInv.getReason();
	//	        v_add_ref_no:=null;
			vAddRefNo = null;
	//	        v_cost:=null;
			vCost = null;
	//	        v_tte_id := v_rec.tte_id;
			vTteId = piInv.getTteId();
	//	    elsif pi_flag='4' then
		} else if(piFlag == 4) {
	//			v_rec:=pi_bcl;
			FPtBatchCcDetail vRec = piBcl;
			vCreatedBy = vRec.getCreatedBy();
			vCreatedDate = vRec.getCreatedDate();
			vCompany = vRec.getCompany();
			vRecOutCode = vRec.getOutCode();
			vCcEbkId = vRec.getCcEbkId();
			vCcFunId = vRec.getCcFunId();
			vCcPrmId = vRec.getCcPrmId();
			vCcFieId = vRec.getCcFieId();
			vCcParId = vRec.getCcParId();
			vCcGteId = vRec.getCcGteId();
			vCcCotId = vRec.getCcCotId();
			vCcOutId = vRec.getCcOutId();
			vCcRe1Id = vRec.getCcRe1Id();
			vCcRe2Id = vRec.getCcRe2Id();
			
			vOutCode = vRec.getOutCode();
	//			v_post_date:=v_rec.post_date;
			vPostDate = vRec.getPostDate();
	//			v_amount_total:=v_rec.amount_total;
			vAmountTotal = vRec.getAmountTotal();
	//			v_amount_outstanding:=null;
			vAmountOutstanding = null;
	//			v_amount_vat:=v_rec.amount_vat;
			vAmountVat = vRec.getAmountVat();
	//			v_amount_do:=v_rec.amount_do;
			vAmountDo = vRec.getAmountDo();
	//	        v_amount_advance :=0;
			vAmountAdvance = BigDecimal.ZERO;
	//	                select descr
	//			into v_reason
	//			from pt_journals 
	//			where id=p_jol_id;
			vReason = pJolId.getDescr();
	//			v_add_ref_no:=null;
			vAddRefNo = null;
	//			v_cost:=null;
			vCost = null;
	//			v_tte_id := v_rec.tte_id;	
			vTteId = vRec.getTteId();
	//		end if;
		}
	//	    select jol.cuy_unit,jol.cuy_rate
	//	    into v_cuy_unit,v_cuy_rate
	//	    from pt_journals jol
	//	    where jol.id=p_jol_id;
		Integer vCuyUnit = pJolId.getCuyUnit();
		BigDecimal vCuyRate = pJolId.getCuyRate();
	//	    
	//	/*        select coalesce(main_out_code,v_rec.out_code)
	//	        into v_out_code
	//	        from register.cc_organization_units
	//	        where code=v_rec.out_code; */
	//
	//	  v_out_code := register.get_main_outcode(v_rec.out_code);
		vOutCode = registerGetMainOutCode(vOutCode);
			//TODO hierarchy
	//		raise notice 'v_out_code %',v_out_code;
		logger.trace("bridgeNewPosting: vOutCode "+vOutCode.getCode());
	//	   INSERT INTO pt_postings(id,jol_id,post_date,out_code,coa_id_ct,coa_id_dt,amount,amount_currency,date_created,user_created,
	//			cc_ebk_id,cc_fun_id,cc_prm_id,cc_fie_id,cc_par_id,cc_gte_id,cc_cot_id,cc_out_id,cc_re1_id,cc_re2_id,descr)
	//	   select nextval('pog_seq'),p_jol_id,v_post_date,v_out_code,btr.coa_id_ct,btr.coa_id_dt,
	//	          (case when btr.amount_type = '0' then v_amount_total
	//	                when btr.amount_type = '1' then v_amount_outstanding
	//	                when btr.amount_type = '2' then v_amount_do
	//	                when btr.amount_type = '3' then v_amount_vat
	//	                when btr.amount_type = '4' then v_cost
	//	                when btr.amount_type = '5' then v_amount_advance
	//	                else v_amount_total
	//	                end) * v_cuy_rate / v_cuy_unit,
	//	          (case when btr.amount_type = '0' then v_amount_total
	//	                when btr.amount_type = '1' then v_amount_outstanding
	//	                when btr.amount_type = '2' then v_amount_do
	//	                when btr.amount_type = '3' then v_amount_vat
	//	                when btr.amount_type = '4' then v_cost
	//	                when btr.amount_type = '5' then v_amount_advance
	//	                else v_amount_total
	//	                end),
	//	           v_rec.date_created,v_rec.user_created,v_rec.cc_ebk_id,v_rec.cc_fun_id,v_rec.cc_prm_id,v_rec.cc_fie_id,
	//	           v_rec.cc_par_id,v_rec.cc_gte_id,v_rec.cc_cot_id,
	//	           (case when v_rec.cc_out_id is null and v_rec.out_code!=v_out_code then (select id from register.cc_organization_units where code=v_out_code)
	//	                else v_rec.cc_out_id end),
	//	           v_rec.cc_re1_id,v_rec.cc_re2_id,coalesce(v_reason,'')||coalesce(' - '||v_add_ref_no,'')
	//	   from ct_batch_type_rules btr
	//	   where btr.out_code=v_rec.out_code
	//	     and btr.tte_id=v_tte_id
	//	     and btr.jte_id=p_jte_id
	//	     and btr.rue_id=p_rue_id
	//	     and  abs(case when btr.amount_type = '0' then v_amount_total
	//	                when btr.amount_type = '1' then v_amount_outstanding
	//	                when btr.amount_type = '2' then v_amount_do
	//	                when btr.amount_type = '3' then v_amount_vat
	//	                when btr.amount_type = '4' then v_cost
	//	                else v_amount_total
	//	                end) >= 0.01
	//	     and v_post_date between btr.active_from_date and btr.active_to_date;
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QFCtBatchTypeRule qFCtBatchTypeRule = QFCtBatchTypeRule.fCtBatchTypeRule;
		List<FCtBatchTypeRule> rowList = queryFactory.select(qFCtBatchTypeRule)
				.from(qFCtBatchTypeRule)
				.where(qFCtBatchTypeRule.outCode.id.eq(vRecOutCode.getId())
						.and(qFCtBatchTypeRule.tteId.id.eq(vTteId.getId()))
						.and(qFCtBatchTypeRule.deleted.eq(false))
						.and(qFCtBatchTypeRule.jteId.id.eq(pJteId.getId()))
						.and(qFCtBatchTypeRule.rueId.id.eq(pRueId.getId()))
						.and(new CaseBuilder().when(qFCtBatchTypeRule.amountType.listOptionItemCode.eq(LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_0)).then(vAmountTotal != null ? vAmountTotal : BigDecimal.ZERO)
							.when(qFCtBatchTypeRule.amountType.listOptionItemCode.eq(LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_1)).then(vAmountOutstanding != null ? vAmountOutstanding : BigDecimal.ZERO)
							.when(qFCtBatchTypeRule.amountType.listOptionItemCode.eq(LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_2)).then(vAmountDo != null ? vAmountDo : BigDecimal.ZERO)
							.when(qFCtBatchTypeRule.amountType.listOptionItemCode.eq(LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_3)).then(vAmountVat != null ? vAmountVat : BigDecimal.ZERO)
							.when(qFCtBatchTypeRule.amountType.listOptionItemCode.eq(LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_4)).then(vCost != null ? vCost : BigDecimal.ZERO)
							.otherwise(vAmountTotal != null ? vAmountTotal : BigDecimal.ZERO).abs().goe(0.01)
						)
						.and(qFCtBatchTypeRule.activeFromDate.loe(vPostDate))
						.and(qFCtBatchTypeRule.activeToDate.goe(vPostDate))
					)
				.fetch();
		for(FCtBatchTypeRule row: rowList) {
			BigDecimal postingAmount = null;
			if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_0) {
				postingAmount = vAmountTotal;
			} else if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_1) {
				postingAmount = vAmountOutstanding;
			} else if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_2) {
				postingAmount = vAmountDo;
			} else if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_3) {
				postingAmount = vAmountVat;
			} else if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_4) {
				postingAmount = vCost;
			} else if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_5) {
				postingAmount = vAmountAdvance;
			} else {
				postingAmount = vAmountTotal;
			}
			FPtPosting newPosting = new FPtPosting(vCreatedBy, vCreatedDate, null, null, false, vCompany, 
					pJolId, vPostDate, vOutCode, postingAmount.multiply(vCuyRate).divide(new BigDecimal(vCuyUnit)), postingAmount, null, 
					null, (vReason != null ? vReason : "")+" - "+(vAddRefNo != null ? vAddRefNo : ""), 
					row.getCoaIdCt(), row.getCoaIdDt(), vCcEbkId, vCcFunId, vCcPrmId, vCcFieId, vCcParId, vCcGteId, 
					vCcCotId, (vCcOutId == null && vRecOutCode.getId() != vOutCode.getId() ? vOutCode : vCcOutId), vCcRe1Id, vCcRe2Id);
			newPosting = (FPtPosting) trySave(newPosting, true);
			if( newPosting == null ) {
				logger.error("bridgeNewPosting: Can't create FPtPosting for rule ID "+row.getId());
				return LoiBreTransitionResult.BRE_TRANSITION_RESULT_NO_BATCH_TYPE_RULE;
			}
		}
	//		raise notice 'after insert';
		//logger.trace("bridgeNewPosting: after insert");
	//	     if NOT FOUND then
		if( rowList.isEmpty() ) {
	//		raise notice 'in rturn 1';
			logger.error("bridgeNewPosting: 1 Missing FCtBatchTypeRules");
	//	        return 1;
			return LoiBreTransitionResult.BRE_TRANSITION_RESULT_NO_BATCH_TYPE_RULE;
	//	     else
		} else {
	//		raise notice 'in rturn 1';	
			logger.trace("bridgeNewPosting: Success");
	//	        return 0;
			return LoiBreTransitionResult.BRE_TRANSITION_RESULT_OK;
	//	     end if;
		}
	//	    -- return 0;
	//	END;
	}

	private Long bridgePaymentPosting(FPtBatch pBahId, FPtJournal pJolId, FJournalType pJteId, FCtRule pRueId, FBreTransition piBre, FInvInvoice piInv, int piFlag, FPtBatchCcDetail piBcl){
	//	CREATE FUNCTION bridge.payment_posting(p_bah_id integer, p_jol_id integer, p_jte_id integer, p_rue_id integer, pi_bre register.bre_transitions DEFAULT NULL::register.bre_transitions, pi_inv accounting.inv_invoices DEFAULT NULL::accounting.inv_invoices, pi_flag character DEFAULT '1'::bpchar, pi_bcl accounting.pt_batch_cc_details DEFAULT NULL::accounting.pt_batch_cc_details) RETURNS integer
	//	    LANGUAGE plpgsql
	//	    AS $$
	//	DECLARE
	//		v_cuy_unit integer;
	//		v_cuy_rate numeric(15,6);
	//		v_rec record;
	//		v_post_date date;
	//		v_amount_total numeric(15,2);
	//		v_amount_outstanding numeric(15,2);
	//		v_amount_do numeric(15,2);
	//		v_amount_vat numeric(15,2);
	//		v_reason varchar(255);
	//		v_add_ref_no varchar(15);
	//		v_cost numeric(15,2);
	//		v_dependence_type varchar(3);
	//		v_dependence_id integer;
	//		v_tte_id integer;
	//		v_out_code varchar(15);
	//	BEGIN
	//		if pi_flag='1' then 
		SecUser vCreatedBy = null;
		Date vCreatedDate = null;
		ManagedCompany vCompany = null; 
		LocalDate vPostDate = null;
		CCcOrganizationUnit vRecOutCode = null;
		BigDecimal vAmountTotal = null;
		BigDecimal vAmountOutstanding = null;
		BigDecimal vAmountDo = null;
		BigDecimal vAmountVat = null;
		BigDecimal vCost = null;
		String vReason = null;
		String vAddRefNo = null;
		FCcEbk vCcEbkId = null;
		FCcFunction vCcFunId = null;
		FCcProgram vCcPrmId = null;
		FCcFinsource vCcFieId = null;
		CCcPartner vCcParId = null;
		CCcGoodsType vCcGteId = null;
		FCcContract vCcCotId = null;
		CCcOrganizationUnit vCcOutId = null;
		FCcReserve1 vCcRe1Id = null;
		FCcReserve2 vCcRe2Id = null;
		
		FCtTransitionType vTteId = null;
		LoiBatchTypeRuleDependenceType vDependenceType = null;
		Long vDependenceId = null;
		CCcOrganizationUnit vOutCode = null;
		if(piFlag == 1) {
	//			v_rec:=pi_bre;
			FBreTransition vRec = piBre;
			vCreatedBy = vRec.getCreatedBy();
			vCreatedDate = vRec.getCreatedDate();
			vCompany = vRec.getCompany();
			vRecOutCode = vRec.getOutCode();
			vCcEbkId = vRec.getCcEbkId();
			vCcFunId = vRec.getCcFunId();
			vCcPrmId = vRec.getCcPrmId();
			vCcFieId = vRec.getCcFieId();
			vCcParId = vRec.getCcParId();
			vCcGteId = vRec.getCcGteId();
			vCcCotId = vRec.getCcCotId();
			vCcOutId = vRec.getCcOutId();
			vCcRe1Id = vRec.getCcRe1Id();
			vCcRe2Id = vRec.getCcRe2Id();
			
			vOutCode = vRec.getOutCode();
	//			v_post_date:=pi_bre.post_date;
			vPostDate = piBre.getPostDate();
	//			v_amount_total:=v_rec.amount_total;
			vAmountTotal = vRec.getAmountTotal();
	//			v_amount_outstanding:=v_rec.amount_outstanding;
			vAmountOutstanding = vRec.getAmountOutstanding();
	//			v_amount_do:=v_rec.amount_do;
			vAmountDo = vRec.getAmountDo();
	//			v_amount_vat:=v_rec.amount_vat;
			vAmountVat = vRec.getAmountVat();
	//			v_reason :=v_rec.descr;
			vReason = vRec.getDescr();
	//			v_add_ref_no:=v_rec.add_ref_no;
			vAddRefNo = vRec.getAddRefNo();
	//			v_cost:=v_rec.cost;
			vCost = vRec.getCost();
	//			select tte.id
	//			into v_tte_id
	//			from register.ct_transition_types tte
	//			where tte.code=v_rec.tte_code;
			vTteId = vRec.getTteCode();
	//			v_dependence_type:=v_rec.dependence_type;
			vDependenceType = vRec.getDependenceType();
	//	                if v_rec.dependence_code is not null then
			if(vRec.getDependenceCode() != null) {
	//	                   select dce.id
	//	                   into v_dependence_id
	//	                   from vw_rule_dependence_id dce
	//	                   where dce.code=v_rec.dependence_code
	//	                     and dce.dependence_type=v_rec.dependence_type
	//	                     and dce.out_code=v_rec.out_code;
				vDependenceId = vwRuleDependenceId(vDependenceType.getListOptionItemCode(), vRec.getDependenceCode(), vRec.getOutCode());
	//	                else     
			} else {
	//			    v_dependence_id:=null;
				vDependenceId = null;
	//			end if;    
			}
	//		elsif pi_flag='2' then 
		} else if(piFlag == 2) {
	//			v_rec:=pi_inv;
			vCreatedBy = piInv.getCreatedBy();
			vCreatedDate = piInv.getCreatedDate();
			vCompany = piInv.getCompany();
			vRecOutCode = piInv.getOutCode();
			vCcEbkId = piInv.getCcEbkId();
			vCcFunId = piInv.getCcFunId();
			vCcPrmId = piInv.getCcPrmId();
			vCcFieId = piInv.getCcFieId();
			vCcParId = piInv.getCcParId();
			vCcGteId = piInv.getCcGteId();
			vCcCotId = piInv.getCcCotId();
			vCcOutId = piInv.getCcOutId();
			vCcRe1Id = piInv.getCcRe1Id();
			vCcRe2Id = piInv.getCcRe2Id();
			
			vOutCode = piInv.getOutCode();
	//			v_tte_id := v_rec.tte_id;
			vTteId = piInv.getTteId();
	//			v_post_date:=pi_inv.date_account;
			vPostDate = piInv.getDateAccount();
	//			v_amount_total:=v_rec.total_amount;
			vAmountTotal = piInv.getTotalAmount();
	//			v_amount_outstanding:=null;
			vAmountOutstanding = null;
	//			v_amount_vat:=v_rec.vat_amount;
			vAmountVat = piInv.getVatAmount();
	//			v_amount_do:=v_rec.tax_base;
			vAmountDo = piInv.getTaxBase();
	//			v_reason:=v_rec.reason;
			vReason = piInv.getReason();
			//vReason = vRec.getReason();
	//			v_add_ref_no:=null;
			vAddRefNo = null;
	//			v_cost:=null;
			vCost = null;
	//			v_dependence_type:=null;
			vDependenceType = null;
	//			v_dependence_id:=null;
			vDependenceId = null;
	//		elsif pi_flag='4' then
		} else if(piFlag == 4) {
	//			v_rec:=pi_bcl;
			FPtBatchCcDetail vRec = piBcl;
			vCreatedBy = vRec.getCreatedBy();
			vCreatedDate = vRec.getCreatedDate();
			vCompany = vRec.getCompany();
			vRecOutCode = vRec.getOutCode();
			vCcEbkId = vRec.getCcEbkId();
			vCcFunId = vRec.getCcFunId();
			vCcPrmId = vRec.getCcPrmId();
			vCcFieId = vRec.getCcFieId();
			vCcParId = vRec.getCcParId();
			vCcGteId = vRec.getCcGteId();
			vCcCotId = vRec.getCcCotId();
			vCcOutId = vRec.getCcOutId();
			vCcRe1Id = vRec.getCcRe1Id();
			vCcRe2Id = vRec.getCcRe2Id();
			
			vOutCode = vRec.getOutCode();
	//			v_post_date:=v_rec.post_date;
			vPostDate = vRec.getPostDate();
	//			v_amount_total:=v_rec.amount_total;
			vAmountTotal = vRec.getAmountTotal();
	//			v_amount_outstanding:=null;
			vAmountOutstanding = null;
	//			v_amount_vat:=v_rec.amount_vat;
			vAmountVat = vRec.getAmountVat();
	//			v_amount_do:=v_rec.amount_do;
			vAmountDo = vRec.getAmountDo();
	//	                select descr
	//			into v_reason
	//			from pt_journals 
	//			where id=p_jol_id;
			vReason = pJolId.getDescr();
	//			v_add_ref_no:=null;
			vAddRefNo = null;
	//			v_cost:=null;
			vCost = null;
	//			v_tte_id := v_rec.tte_id;	
			vTteId = vRec.getTteId();
	//		end if;
		}
	//		select jol.cuy_unit,jol.cuy_rate
	//		into v_cuy_unit,v_cuy_rate
	//		from pt_journals jol
	//		where jol.id=p_jol_id;
		Integer vCuyUnit = pJolId.getCuyUnit();
		BigDecimal vCuyRate = pJolId.getCuyRate();
	//	/*        select coalesce(main_out_code,v_rec.out_code)
	//	        into v_out_code
	//	        from register.cc_organization_units
	//	        where code=v_rec.out_code; */
	//	   v_out_code := register.get_main_outcode(v_rec.out_code);     
		vOutCode = registerGetMainOutCode(vOutCode);
	//	   INSERT INTO pt_postings(id,jol_id,post_date,out_code,coa_id_ct,coa_id_dt,amount,amount_currency,date_created,user_created, 
	//	                 cc_ebk_id, cc_fun_id,cc_prm_id,cc_fie_id,cc_par_id,cc_gte_id,cc_cot_id,cc_out_id,cc_re1_id,cc_re2_id,descr)
	//	   select nextval('pog_seq'),p_jol_id,v_post_date,v_out_code,btr.coa_id_ct,btr.coa_id_dt,
	//	          (case when btr.amount_type = '0' then v_amount_total
	//	                when btr.amount_type = '1' then v_amount_outstanding
	//	                when btr.amount_type = '2' then v_amount_do
	//	                when btr.amount_type = '3' then v_amount_vat
	//	                when btr.amount_type = '4' then v_cost
	//	                else v_amount_total
	//	                end) * v_cuy_rate / v_cuy_unit,
	//	          (case when btr.amount_type = '0' then v_amount_total
	//	                when btr.amount_type = '1' then v_amount_outstanding
	//	                when btr.amount_type = '2' then v_amount_do
	//	                when btr.amount_type = '3' then v_amount_vat
	//	                when btr.amount_type = '4' then v_cost
	//	                else v_amount_total
	//	                end),
	//	           v_rec.date_created, v_rec.user_created,v_rec.cc_ebk_id,v_rec.cc_fun_id,v_rec.cc_prm_id,v_rec.cc_fie_id, 
	//	           v_rec.cc_par_id, v_rec.cc_gte_id,v_rec.cc_cot_id, 
	//	           (case when v_rec.cc_out_id is null and v_rec.out_code!=v_out_code then (select id from register.cc_organization_units where code=v_out_code)
	//	                else v_rec.cc_out_id end), 
	//	           v_rec.cc_re1_id, v_rec.cc_re2_id, 
	//	           coalesce(v_reason,'')||coalesce(' - '||v_add_ref_no,'')
	//	   from ct_batch_type_rules btr
	//	   where btr.out_code=v_rec.out_code
	//	     and btr.tte_id=v_tte_id
	//	     and btr.jte_id=p_jte_id
	//	     and btr.rue_id=p_rue_id
	//	     and coalesce(btr.dependence_type,'0')=coalesce(v_dependence_type,'0')
	//	     and coalesce(btr.dependence_id,0)=coalesce(v_dependence_id,0)
	//	     and v_post_date between btr.active_from_date and btr.active_to_date;
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QFCtBatchTypeRule qFCtBatchTypeRule = QFCtBatchTypeRule.fCtBatchTypeRule;
		List<FCtBatchTypeRule> rowList = queryFactory.select(qFCtBatchTypeRule)
				.from(qFCtBatchTypeRule)
				.where(qFCtBatchTypeRule.outCode.id.eq(vRecOutCode.getId())
						.and(qFCtBatchTypeRule.tteId.id.eq(vTteId.getId()))
						.and(qFCtBatchTypeRule.deleted.eq(false))
						.and(qFCtBatchTypeRule.jteId.id.eq(pJteId.getId()))
						.and(qFCtBatchTypeRule.rueId.id.eq(pRueId.getId()))
						.and(vDependenceType == null ? 
								qFCtBatchTypeRule.dependenceType.isNull()
								: qFCtBatchTypeRule.dependenceType.listOptionItemCode.eq(vDependenceType.getListOptionItemCode()))
						.and(qFCtBatchTypeRule.dependenceId.coalesce(-1L).eq(vDependenceId == null ? -1L : vDependenceId))
						.and(qFCtBatchTypeRule.activeFromDate.loe(vPostDate))
						.and(qFCtBatchTypeRule.activeToDate.goe(vPostDate))
					)
				.fetch();
		for(FCtBatchTypeRule row: rowList) {
			BigDecimal postingAmount = null;
			if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_0) {
				postingAmount = vAmountTotal;
			} else if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_1) {
				postingAmount = vAmountOutstanding;
			} else if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_2) {
				postingAmount = vAmountDo;
			} else if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_3) {
				postingAmount = vAmountVat;
			} else if(row.getAmountType().getListOptionItemCode() == LoiBatchTypeRuleAmountType.BATCH_TYPE_RULE_AMOUNT_TYPE_4) {
				postingAmount = vCost;
			} else {
				postingAmount = vAmountTotal;
			}
			FPtPosting newPosting = new FPtPosting(vCreatedBy, vCreatedDate, null, null, false, vCompany, 
					pJolId, vPostDate, vOutCode, postingAmount.multiply(vCuyRate).divide(new BigDecimal(vCuyUnit)), postingAmount, null, null, (vReason != null ? vReason : "")+" - "+(vAddRefNo != null ? vAddRefNo : ""), 
					row.getCoaIdCt(), row.getCoaIdDt(), vCcEbkId, vCcFunId, vCcPrmId, vCcFieId, vCcParId, vCcGteId, 
					vCcCotId, (vCcOutId == null && vRecOutCode.getId() != vOutCode.getId() ? vOutCode : vCcOutId), vCcRe1Id, vCcRe2Id);
			if(trySave(newPosting, true) == null) {
				throw new ReportException("bridgePaymentPosting couldn't create FPtPosting");
			}
		}
	//	     if NOT FOUND then
		if( rowList.isEmpty() ) {
	//	        return 1;
			logger.error("bridgePaymentPosting: 1 Missing FCtBatchTypeRules");
			return LoiBreTransitionResult.BRE_TRANSITION_RESULT_NO_BATCH_TYPE_RULE;
	//	     else 
		} else {
	//	        return 0;
			logger.trace("bridgePaymentPosting: Success");
			return LoiBreTransitionResult.BRE_TRANSITION_RESULT_OK;
	//	     end if; 
		}
	//	    -- return 0;
	//	END;
	}

	//accounting.aggregate_journals_ui
	//TODO This must be changed to run every time a FPtJournal is modified! So probably it must be transfered to Drools rules.
	public Integer accountingAggregateJournalsUi(Long pJolId){
		FPtJournal vJolId = entityManager.getReference(FPtJournal.class, pJolId);
		Integer vResult = 0;
		Integer vResult1 = 0;

		vResult = accountingAggregateJournals(vJolId);

		if (vResult == 0){
			vResult1 = populateJournals(vJolId);
		}

		if (vResult1 == 0 && vResult == 0){
			return 0;
		}

		logger.error("accountingAggregateJournalsUi: 1 Fail");
		return 1;
	}
	
//CREATE FUNCTION accounting.update_cc_balances(p_id integer, p_start_date date, p_period character varying, p_cc_id integer, p_cc_name character varying, p_side character varying, p_amount numeric, p_coa_id integer, p_journal_type character varying) RETURNS void
//    LANGUAGE plpgsql
//    AS $$
	private void accountingUpdateCcBalances(FPtCcBalance pId, LocalDate pStartDate, int pPeriodMonth, Long pCcId, String pCcName, 
			String pSide, BigDecimal pAmount, FChartAccount pCoaId, LoiJournalTypeCalculationType pJournalType) {
//BEGIN
//   				       update pt_cc_balances 
//			                  set date_updated = (case when substr(p_period,5,2) = '00' then date_trunc('month',p_start_date)::date
//			                                           when substr(p_period,5,2) = '13' then to_date(to_char(p_start_date,'yyyymm')||'31','yyyymmdd') 
//			                                           when date_trunc('month',date_updated)::date = date_trunc('month', p_start_date)::date 
//			                                                then greatest (date_updated,p_start_date)
//			                                           else date_updated end),
		pId.setLastModifiedDate( pPeriodMonth == 0 ? Date.from(pStartDate.withDayOfMonth(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) 
				: ( pPeriodMonth == 13 ? Date.from(pStartDate.withDayOfMonth(31).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
						: (pId.getLastModifiedDate().getYear() == pStartDate.getYear() && pId.getLastModifiedDate().getMonth() == pStartDate.getMonthValue() ?
								( pId.getLastModifiedDate().getDay() >= pStartDate.getDayOfMonth() ? pId.getLastModifiedDate() : Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
								: pId.getLastModifiedDate()
							)
					)
			);
//			                      cc_ebk_id = (case when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_ebk_id
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_ebk_id
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_ebk_id end),
		if( pCcName.equals(EBK) && (pId.getCcEbkId() == null || pId.getCcEbkId().getId() == pCcId) ) {
			FCcEbk newCostCenter = entityManager.getReference(FCcEbk.class, pCcId);
			pId.setCcEbkId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcEbkId()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcEbkId()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcEbkId()
												)
										)
								)
						)
				);
		}
//			                      cc_ebk_ct_amount = (case when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_ebk_ct_amount,0)+p_amount
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_ebk_ct_amount
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_ebk_ct_amount,0)+p_amount      
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_ebk_ct_amount
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_ebk_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_ebk_ct_amount end),
		if( pCcName.equals(EBK) && (pId.getCcEbkId() == null || pId.getCcEbkId().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcEbkCtAmount() != null ? pId.getCcEbkCtAmount() : BigDecimal.ZERO);
			pId.setCcEbkCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcEbkCtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcEbkCtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcEbkCtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_ebk_dt_amount = (case when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_ebk_dt_amount,0)+p_amount
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_ebk_dt_amount
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_ebk_dt_amount,0)+p_amount      
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_ebk_dt_amount
//			                                          when p_cc_name='EBK' and (cc_ebk_id=p_cc_id or cc_ebk_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_ebk_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_ebk_dt_amount end),
		if( pCcName.equals(EBK) && (pId.getCcEbkId() == null || pId.getCcEbkId().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcEbkDtAmount() != null ? pId.getCcEbkDtAmount() : BigDecimal.ZERO);
			pId.setCcEbkDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcEbkDtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcEbkDtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcEbkDtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_fun_id = (case when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_fun_id
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_fun_id
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_fun_id end),
		if( pCcName.equals(FUN) && (pId.getCcFunId() == null || pId.getCcFunId().getId() == pCcId) ) {
			FCcFunction newCostCenter = entityManager.getReference(FCcFunction.class, pCcId);
			pId.setCcFunId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcFunId()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcFunId()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcFunId()
												)
										)
								)
						)
				);
		}
//			                      cc_fun_ct_amount = (case when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_fun_ct_amount,0)+p_amount
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_fun_ct_amount
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_fun_ct_amount,0)+p_amount      
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_fun_ct_amount
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_fun_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_fun_ct_amount end),
		if( pCcName.equals(FUN) && (pId.getCcFunId() == null || pId.getCcFunId().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcFunCtAmount() != null ? pId.getCcFunCtAmount() : BigDecimal.ZERO);
			pId.setCcFunCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcFunCtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcFunCtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcFunCtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_fun_dt_amount = (case when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_fun_dt_amount,0)+p_amount
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_fun_dt_amount
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_fun_dt_amount,0)+p_amount      
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_fun_dt_amount
//			                                          when p_cc_name='FUN' and (cc_fun_id=p_cc_id or cc_fun_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_fun_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_fun_dt_amount end),
		if( pCcName.equals(FUN) && (pId.getCcFunId() == null || pId.getCcFunId().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcFunDtAmount() != null ? pId.getCcFunDtAmount() : BigDecimal.ZERO);
			pId.setCcFunDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcFunDtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcFunDtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcFunDtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_prm_id = (case when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_prm_id
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_prm_id
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_prm_id end),
		if( pCcName.equals(PRM) && (pId.getCcFunId() == null || pId.getCcFunId().getId() == pCcId) ) {
			FCcProgram newCostCenter = entityManager.getReference(FCcProgram.class, pCcId);
			pId.setCcPrmId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcPrmId()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcPrmId()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcPrmId()
												)
										)
								)
						)
				);
		}
//			                      cc_prm_ct_amount = (case when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_prm_ct_amount,0)+p_amount
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_prm_ct_amount
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_prm_ct_amount,0)+p_amount      
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_prm_ct_amount
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_prm_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_prm_ct_amount end),
		if( pCcName.equals(PRM) && (pId.getCcPrmId() == null || pId.getCcPrmId().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcPrmCtAmount() != null ? pId.getCcPrmCtAmount() : BigDecimal.ZERO);
			pId.setCcPrmCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcPrmCtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcPrmCtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcPrmCtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_prm_dt_amount = (case when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_prm_dt_amount,0)+p_amount
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_prm_dt_amount
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_prm_dt_amount,0)+p_amount      
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_prm_dt_amount
//			                                          when p_cc_name='PRM' and (cc_prm_id=p_cc_id or cc_prm_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_prm_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_prm_dt_amount end),
		if( pCcName.equals(PRM) && (pId.getCcPrmId() == null || pId.getCcPrmId().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcPrmDtAmount() != null ? pId.getCcPrmDtAmount() : BigDecimal.ZERO);
			pId.setCcPrmDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcPrmDtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcPrmDtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcPrmDtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_fie_id = (case when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_fie_id
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_fie_id
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_fie_id end),
		if( pCcName.equals(FIE) && (pId.getCcFieId() == null || pId.getCcFieId().getId() == pCcId) ) {
			FCcFinsource newCostCenter = entityManager.getReference(FCcFinsource.class, pCcId);
			pId.setCcFieId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcFieId()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcFieId()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcFieId()
												)
										)
								)
						)
				);
		}
//			                      cc_fie_ct_amount = (case when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_fie_ct_amount,0)+p_amount
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_fie_ct_amount
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_fie_ct_amount,0)+p_amount      
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_fie_ct_amount
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_fie_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_fie_ct_amount end),
		if( pCcName.equals(FIE) && (pId.getCcFieId() == null || pId.getCcFieId().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcFieCtAmount() != null ? pId.getCcFieCtAmount() : BigDecimal.ZERO);
			pId.setCcFieCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcFieCtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcFieCtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcFieCtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_fie_dt_amount = (case when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_fie_dt_amount,0)+p_amount
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_fie_dt_amount
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_fie_dt_amount,0)+p_amount      
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_fie_dt_amount
//			                                          when p_cc_name='FIE' and (cc_fie_id=p_cc_id or cc_fie_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_fie_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_fie_dt_amount end),
		if( pCcName.equals(FIE) && (pId.getCcFieId() == null || pId.getCcFieId().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcFieDtAmount() != null ? pId.getCcFieDtAmount() : BigDecimal.ZERO);
			pId.setCcFieDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcFieDtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcFieDtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcFieDtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_par_id = (case when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_par_id
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_par_id
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_par_id end),
		if( pCcName.equals(PAR) && (pId.getCcParId() == null || pId.getCcParId().getId() == pCcId) ) {
			CCcPartner newCostCenter = entityManager.getReference(CCcPartner.class, pCcId);
			pId.setCcParId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcParId()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcParId()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcParId()
												)
										)
								)
						)
				);
		}
//			                      cc_par_ct_amount = (case when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_par_ct_amount,0)+p_amount
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_par_ct_amount
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_par_ct_amount,0)+p_amount      
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_par_ct_amount
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_par_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_par_ct_amount end),
		if( pCcName.equals(PAR) && (pId.getCcParId() == null || pId.getCcParId().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcParCtAmount() != null ? pId.getCcParCtAmount() : BigDecimal.ZERO);
			pId.setCcParCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcParCtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcParCtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcParCtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_par_dt_amount = (case when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_par_dt_amount,0)+p_amount
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_par_dt_amount
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_par_dt_amount,0)+p_amount      
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_par_dt_amount
//			                                          when p_cc_name='PAR' and (cc_par_id=p_cc_id or cc_par_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_par_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_par_dt_amount end),
		if( pCcName.equals(PAR) && (pId.getCcParId() == null || pId.getCcParId().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcParDtAmount() != null ? pId.getCcParDtAmount() : BigDecimal.ZERO);
			pId.setCcParDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcParDtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcParDtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcParDtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_gte_id = (case when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_gte_id
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_gte_id
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_gte_id end),
		if( pCcName.equals(GTE) && (pId.getCcGteId() == null || pId.getCcGteId().getId() == pCcId) ) {
			CCcGoodsType newCostCenter = entityManager.getReference(CCcGoodsType.class, pCcId);
			pId.setCcGteId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcGteId()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcGteId()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcGteId()
												)
										)
								)
						)
				);
		}
//			                      cc_gte_ct_amount = (case when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_gte_ct_amount,0)+p_amount
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_gte_ct_amount
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_gte_ct_amount,0)+p_amount      
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_gte_ct_amount
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_gte_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_gte_ct_amount end),
		if( pCcName.equals(GTE) && (pId.getCcGteId() == null || pId.getCcGteId().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcGteCtAmount() != null ? pId.getCcGteCtAmount() : BigDecimal.ZERO);
			pId.setCcGteCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcGteCtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcGteCtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcGteCtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_gte_dt_amount = (case when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_gte_dt_amount,0)+p_amount
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_gte_dt_amount
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_gte_dt_amount,0)+p_amount      
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_gte_dt_amount
//			                                          when p_cc_name='GTE' and (cc_gte_id=p_cc_id or cc_gte_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_gte_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_gte_dt_amount end),
		if( pCcName.equals(GTE) && (pId.getCcGteId() == null || pId.getCcGteId().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcGteDtAmount() != null ? pId.getCcGteDtAmount() : BigDecimal.ZERO);
			pId.setCcGteDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcGteDtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcGteDtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcGteDtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_cot_id = (case when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_cot_id
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_cot_id
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_cot_id end),
		if( pCcName.equals(COT) && (pId.getCcCotId() == null || pId.getCcCotId().getId() == pCcId) ) {
			FCcContract newCostCenter = entityManager.getReference(FCcContract.class, pCcId);
			pId.setCcCotId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcCotId()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcCotId()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcCotId()
												)
										)
								)
						)
				);
		}
//			                      cc_cot_ct_amount = (case when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_cot_ct_amount,0)+p_amount
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_cot_ct_amount
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_cot_ct_amount,0)+p_amount      
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_cot_ct_amount
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_cot_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_cot_ct_amount end),
		if( pCcName.equals(COT) && (pId.getCcCotId() == null || pId.getCcCotId().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcCotCtAmount() != null ? pId.getCcCotCtAmount() : BigDecimal.ZERO);
			pId.setCcCotCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcCotCtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcCotCtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcCotCtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_cot_dt_amount = (case when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_cot_dt_amount,0)+p_amount
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_cot_dt_amount
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_cot_dt_amount,0)+p_amount      
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_cot_dt_amount
//			                                          when p_cc_name='COT' and (cc_cot_id=p_cc_id or cc_cot_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_cot_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_cot_dt_amount end),
		if( pCcName.equals(COT) && (pId.getCcCotId() == null || pId.getCcCotId().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcCotDtAmount() != null ? pId.getCcCotDtAmount() : BigDecimal.ZERO);
			pId.setCcCotDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcCotDtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcCotDtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcCotDtAmount()
												)
										)
								)
						)
				);
		}
//			                       cc_out_id = (case when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_out_id
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_out_id
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_out_id end),
		if( pCcName.equals(OUT) && (pId.getCcOutId() == null || pId.getCcOutId().getId() == pCcId) ) {
			CCcOrganizationUnit newCostCenter = entityManager.getReference(CCcOrganizationUnit.class, pCcId);
			pId.setCcOutId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcOutId()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcOutId()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcOutId()
												)
										)
								)
						)
				);
		}
//			                      cc_out_ct_amount = (case when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_out_ct_amount,0)+p_amount
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_out_ct_amount
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_out_ct_amount,0)+p_amount      
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_out_ct_amount
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_out_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_out_ct_amount end),
		if( pCcName.equals(OUT) && (pId.getCcOutId() == null || pId.getCcOutId().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcOutCtAmount() != null ? pId.getCcOutCtAmount() : BigDecimal.ZERO);
			pId.setCcOutCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcOutCtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcOutCtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcOutCtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_out_dt_amount = (case when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_out_dt_amount,0)+p_amount
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_out_dt_amount
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_out_dt_amount,0)+p_amount      
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_out_dt_amount
//			                                          when p_cc_name='OUT' and (cc_out_id=p_cc_id or cc_out_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_out_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_out_dt_amount end),                     
		if( pCcName.equals(OUT) && (pId.getCcOutId() == null || pId.getCcOutId().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcOutDtAmount() != null ? pId.getCcOutDtAmount() : BigDecimal.ZERO);
			pId.setCcOutDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcOutDtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcOutDtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcOutDtAmount()
												)
										)
								)
						)
				);
		}
//			                     cc_re1_id = (case when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_re1_id
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_re1_id
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_re1_id end),
		if( pCcName.equals(RE1) && (pId.getCcRe1Id() == null || pId.getCcRe1Id().getId() == pCcId) ) {
			FCcReserve1 newCostCenter = entityManager.getReference(FCcReserve1.class, pCcId);
			pId.setCcRe1Id( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcRe1Id()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcRe1Id()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcRe1Id()
												)
										)
								)
						)
				);
		}
//			                      cc_re1_ct_amount = (case when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_re1_ct_amount,0)+p_amount
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_re1_ct_amount
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_re1_ct_amount,0)+p_amount      
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_re1_ct_amount
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_re1_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_re1_ct_amount end),
		if( pCcName.equals(RE1) && (pId.getCcRe1Id() == null || pId.getCcRe1Id().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcRe1CtAmount() != null ? pId.getCcRe1CtAmount() : BigDecimal.ZERO);
			pId.setCcRe1CtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcRe1CtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcRe1CtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcRe1CtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_re1_dt_amount = (case when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_re1_dt_amount,0)+p_amount
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_re1_dt_amount
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_re1_dt_amount,0)+p_amount      
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_re1_dt_amount
//			                                          when p_cc_name='RE1' and (cc_re1_id=p_cc_id or cc_re1_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_re1_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_re1_dt_amount end),
		if( pCcName.equals(RE1) && (pId.getCcRe1Id() == null || pId.getCcRe1Id().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcRe1DtAmount() != null ? pId.getCcRe1DtAmount() : BigDecimal.ZERO);
			pId.setCcRe1DtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcRe1DtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcRe1DtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcRe1DtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_re2_id = (case when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type='S' and substr(p_period,5,2)='00'
//			                                               then  p_cc_id
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00'
//			                                               then  cc_re2_id
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type='E' and substr(p_period,5,2)='13'
//			                                               then  p_cc_id      
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13'
//			                                               then  cc_re2_id
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00'
//			                                               then  p_cc_id           			                                                   
//			                                          else cc_re2_id end),
		if( pCcName.equals(RE2) && (pId.getCcRe2Id() == null || pId.getCcRe2Id().getId() == pCcId) ) {
			FCcReserve2 newCostCenter = entityManager.getReference(FCcReserve2.class, pCcId);
			pId.setCcRe2Id( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcRe2Id()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcRe2Id()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: pId.getCcRe2Id()
												)
										)
								)
						)
				);
		}
//			                      cc_re2_ct_amount = (case when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  coalesce(cc_re2_ct_amount,0)+p_amount
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='CT'
//			                                               then  cc_re2_ct_amount
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='CT'
//			                                               then  coalesce(cc_re2_ct_amount,0)+p_amount      
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='CT'
//			                                               then  cc_re2_ct_amount
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='CT'
//			                                               then coalesce(cc_re2_ct_amount,0)+p_amount           			                                                   
//			                                          else cc_re2_ct_amount end),
		if( pCcName.equals(RE2) && (pId.getCcRe2Id() == null || pId.getCcRe2Id().getId() == pCcId) && pSide.equals(CREDIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcRe2CtAmount() != null ? pId.getCcRe2CtAmount() : BigDecimal.ZERO);
			pId.setCcRe2CtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcRe2CtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcRe2CtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcRe2CtAmount()
												)
										)
								)
						)
				);
		}
//			                      cc_re2_dt_amount = (case when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type='S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  coalesce(cc_re2_dt_amount,0)+p_amount
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type<>'S' and substr(p_period,5,2)='00' and p_side='DT'
//			                                               then  cc_re2_dt_amount
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type='E' and substr(p_period,5,2)='13' and p_side='DT'
//			                                               then  coalesce(cc_re2_dt_amount,0)+p_amount      
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type='E' and substr(p_period,5,2)<>'13' and p_side='DT'
//			                                               then  cc_re2_dt_amount
//			                                          when p_cc_name='RE2' and (cc_re2_id=p_cc_id or cc_re2_id is null) and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' and p_side='DT'
//			                                               then coalesce(cc_re2_dt_amount,0)+p_amount           			                                                   
//			                                          else cc_re2_dt_amount end)
		if( pCcName.equals(RE2) && (pId.getCcRe2Id() == null || pId.getCcRe2Id().getId() == pCcId) && pSide.equals(DEBIT) ) {
			BigDecimal newAmount = pAmount.add(pId.getCcRe2DtAmount() != null ? pId.getCcRe2DtAmount() : BigDecimal.ZERO);
			pId.setCcRe2DtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newAmount
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pId.getCcRe2DtAmount()
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newAmount
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? pId.getCcRe2DtAmount()
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newAmount
													: pId.getCcRe2DtAmount()
												)
										)
								)
						)
				);
		}
//			               where id = p_id;
		if(trySave(pId, true) == null) {
			throw new ReportException("accountingUpdateCcBalances couldn't update FPtCcBalance ID "+pId.getId());
		}
//END;
	}

//	CREATE FUNCTION accounting.populate_journals(p_jol_id integer) RETURNS integer
//    LANGUAGE plpgsql
//    AS $$
	private Integer populateJournals(FPtJournal pJolId) {
//		DECLARE 
//		   v_post_record record;
//		   v_status varchar;
//		   v_cc_status varchar;
//		   v_ccb_id  integer;
//		   v_ccb_id_not_null integer;
//		   v_id_null integer;
//		   v_previous_date date;
//		   v_start_date date;
//		   v_end_date date;
//		   v_flag integer;
//		   v_current_amount numeric;
//		   v_running_total numeric;
//		   v_journal_type char(1);
//		   v_start_period varchar(6);
//		   v_previous_period varchar(6);
//		   v_result varchar;
//		   v_flag_period varchar(6);
//		BEGIN
//		  v_flag:=0;
//		  v_current_amount:=0;
//		  v_running_total:=0;
//		  v_flag_period:='0';
		Integer vFlag = 0;
		BigDecimal vCurrentAmount = BigDecimal.ZERO;
		BigDecimal vRunningTotal = BigDecimal.ZERO;
		Integer vFlagPeriod = 0;

//		  select jol.status,jol.cc_status,coalesce(jte.type,'0')
//		  into v_status,v_cc_status,v_journal_type
//		  from pt_journals jol,ct_journal_types jte
//		  where jol.id=p_jol_id
//		  and jol.jte_id=jte.id;

//		  if NOT FOUND then
//		     return 1;
//		  end if;   
		if(pJolId == null || pJolId.getJteId() == null){
			logger.error("populateJournals: 1 The parameter FPtJournal or its FJournalType jteId is missing");
			return 1;
		}
		LoiPtJournalStatus vStatus = pJolId.getStatus();
		LoiPtJournalCcStatus vCcStatus = pJolId.getCcStatus();
		LoiJournalTypeCalculationType vJournalType = pJolId.getJteId().getType() != null ? pJolId.getJteId().getType() : null;

//	  if v_status in ('P','V') and v_cc_status ='U' then
		if( (vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_P || vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_V) && vCcStatus.getListOptionItemCode() == LoiPtJournalCcStatus.PT_JOURNAL_CC_STATUS_U) {

			Session session = entityManager.unwrap(Session.class);
			HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
			QFPtPosting qFPtPosting = QFPtPosting.fPtPosting;
//	     for v_post_record in (select * from 
//					(SELECT jol_id, 
//						post_date, 
//						out_code, 
//						coa_id_ct as coa_id,
//						'CT' as side_flag, 
//						sum(amount) as amount,
//						coalesce(cc_ebk_id,0) as cc_id,
//						'EBK'	as cc_name 
//					  FROM pt_postings 
//					  where jol_id=p_jol_id
//					  group by  jol_id, post_date, out_code, coa_id_ct,coalesce(cc_ebk_id,0)
			List<Tuple> postList = queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccEbkId.id,Expressions.asString(EBK).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccEbkId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccEbkId.id)
				.fetch();
//					UNION ALL
//					SELECT jol_id, 
//						post_date, 
//						out_code, 
//						coa_id_dt, 
//						'DT', 
//						sum(amount) as amount,
//						coalesce(cc_ebk_id,0) as cc_id,
//						'EBK' as cc_name
//						FROM pt_postings  
//						where jol_id=p_jol_id
//						group by  jol_id, post_date, out_code, coa_id_dt, coalesce (cc_ebk_id,0)
//					UNION ALL
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccEbkId.id,Expressions.asString(EBK).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccEbkId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccEbkId.id)
				.fetch());
//					SELECT jol_id, 
//						post_date, 
//						out_code, 
//						coa_id_ct,
//						'CT' as side_flag, 
//						sum(amount) as amount,
//						coalesce(cc_fun_id,0) as cc_id,
//						'FUN'	as cc_name 
//						FROM pt_postings
//						where jol_id=p_jol_id
//						group by  jol_id, post_date, out_code, coa_id_ct,coalesce(cc_fun_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccFunId.id,Expressions.asString(FUN).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccFunId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccFunId.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//						post_date, 
//						out_code, 
//						coa_id_dt, 
//						'DT', 
//						sum(amount) as amount,
//						coalesce(cc_fun_id,0) as cc_id,
//						'FUN' as cc_name
//						FROM pt_postings  
//						where jol_id=p_jol_id
//						group by  jol_id, post_date, out_code, coa_id_dt, coalesce (cc_fun_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccFunId.id,Expressions.asString(FUN).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccFunId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccFunId.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//						post_date, 
//						out_code, 
//						coa_id_ct,
//						'CT' as side_flag, 
//						sum(amount) as amount,
//						coalesce(cc_prm_id,0) as cc_id,
//						'PRM'	as cc_name 
//						FROM pt_postings
//						where jol_id=p_jol_id
//						group by  jol_id, post_date, out_code, coa_id_ct,coalesce(cc_prm_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccPrmId.id,Expressions.asString(PRM).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccPrmId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccPrmId.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//						post_date, 
//						out_code, 
//						coa_id_dt, 
//						'DT', 
//						sum(amount) as amount,
//						coalesce(cc_prm_id,0) as cc_id,
//						'PRM' as cc_name
//						FROM pt_postings  
//						where jol_id=p_jol_id
//						group by  jol_id, post_date, out_code, coa_id_dt, coalesce (cc_prm_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccPrmId.id,Expressions.asString(PRM).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccPrmId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccPrmId.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//						       post_date, 
//						       out_code, 
//                               coa_id_ct,
//                               'CT' as side_flag, 
//                               sum(amount) as amount,
//                               coalesce(cc_fie_id,0) as cc_id,
//                               'FIE'	as cc_name 
//                               FROM pt_postings
//                               where jol_id=p_jol_id
//                               group by  jol_id, post_date, out_code, coa_id_ct,coalesce(cc_fie_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccFieId.id,Expressions.asString(FIE).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccFieId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccFieId.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//							      post_date, 
//							      out_code,
//                                  coa_id_dt, 
//                                  'DT', 
//                                  sum(amount) as amount,
//                                  coalesce(cc_fie_id,0) as cc_id,
//                                  'FIE' as cc_name
//                            FROM pt_postings  
//                            where jol_id=p_jol_id
//                            group by  jol_id, post_date, out_code, coa_id_dt, coalesce (cc_fie_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccFieId.id,Expressions.asString(FIE).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccFieId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccFieId.id)
				.fetch());
//	                UNION ALL
//					SELECT jol_id, 
//							       post_date, 
//                                   out_code, 
//                                   coa_id_ct,
//                                   'CT' as side_flag, 
//                                   sum(amount) as amount,
//                                   coalesce(cc_par_id,0) as cc_id,
//                                   'PAR'	as cc_name 
//                            FROM pt_postings
//                            where jol_id=p_jol_id
//                            group by  jol_id, post_date, out_code, coa_id_ct,coalesce(cc_par_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccParId.id,Expressions.asString(PAR).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccParId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccParId.id)
				.fetch());
//					UNION ALL
//					SELECT  jol_id, 
//								post_date, 
//								out_code, 
//								coa_id_dt, 
//								'DT', 
//								sum(amount) as amount,
//								coalesce(cc_par_id,0) as cc_id,
//								'PAR' as cc_name
//							FROM pt_postings  
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_dt, coalesce (cc_par_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccParId.id,Expressions.asString(PAR).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccParId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccParId.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//								post_date, 
//								out_code, 
//								coa_id_ct,
//								'CT' as side_flag, 
//								sum(amount) as amount,
//								coalesce(cc_gte_id,0) as cc_id,
//								'GTE'	as cc_name 
//							FROM pt_postings
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_ct, coalesce(cc_gte_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccGteId.id,Expressions.asString(GTE).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccGteId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccGteId.id)
				.fetch());
//					UNION ALL
//					SELECT  jol_id, 
//								post_date, 
//								out_code, 
//								coa_id_dt,
//								'DT' as side_flag, 
//								sum(amount) as amount,
//								coalesce(cc_gte_id,0) as cc_id,
//								'GTE'	as cc_name 
//							FROM pt_postings
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_dt,coalesce(cc_gte_id,0)  
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccGteId.id,Expressions.asString(GTE).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccGteId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccGteId.id)
				.fetch());
//					UNION ALL
//					SELECT  jol_id, 
//								post_date, 
//								out_code, 
//								coa_id_ct,
//								'CT' as side_flag, 
//								sum(amount) as amount,
//								coalesce(cc_cot_id,0) as cc_id,
//								'COT'	as cc_name 
//							FROM pt_postings
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_ct,coalesce(cc_cot_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccCotId.id,Expressions.asString(COT).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccCotId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccCotId.id)
				.fetch());
//					UNION ALL
//					SELECT  jol_id,
//								post_date, 
//								out_code, 
//								coa_id_dt, 
//								'DT',	
//								sum(amount) as amount,
//								coalesce(cc_cot_id,0) as cc_id,
//								'COT' as cc_name
//							FROM pt_postings  
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_dt, coalesce (cc_cot_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccCotId.id,Expressions.asString(COT).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccCotId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccCotId.id)
				.fetch());
//					UNION ALL
//					SELECT  jol_id, 
//								post_date, 
//								out_code, 
//								coa_id_ct,
//								'CT' as side_flag, 
//								sum(amount) as amount,
//								coalesce(cc_out_id,0) as cc_id,
//								'OUT'	as cc_name 
//							FROM pt_postings
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_ct,coalesce(cc_out_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccOutId.id,Expressions.asString(OUT).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccOutId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccOutId.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//								post_date, 
//								out_code, 
//								coa_id_dt, 
//								'DT', 
//								sum(amount) as amount,
//								coalesce(cc_out_id,0) as cc_id,
//								'OUT' as cc_name
//							FROM pt_postings  
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_dt, coalesce (cc_out_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccOutId.id,Expressions.asString(OUT).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccOutId.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccOutId.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//								post_date, 
//								out_code, 
//								coa_id_ct,
//								'CT' as side_flag, 
//								sum(amount) as amount,		
//								coalesce(cc_re1_id,0) as cc_id,
//								'RE1'	as cc_name 
//							FROM pt_postings
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_ct,coalesce(cc_re1_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccRe1Id.id,Expressions.asString(RE1).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccRe1Id.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccRe1Id.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//								post_date, 
//								out_code, 
//								coa_id_dt, 
//								'DT', 
//								sum(amount) as amount,
//								coalesce(cc_re1_id,0) as cc_id,
//								'RE1' as cc_name
//							FROM pt_postings  
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_dt, coalesce (cc_re1_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccRe1Id.id,Expressions.asString(RE1).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccRe1Id.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccRe1Id.id)
				.fetch());
//					UNION ALL
//					SELECT jol_id, 
//								post_date, 
//								out_code, 
//								coa_id_ct,
//								'CT' as side_flag, 
//								sum(amount) as amount,
//								coalesce(cc_re2_id,0) as cc_id,
//								'RE2'	as cc_name 
//							FROM pt_postings
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_ct,coalesce(cc_re2_id,0)
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdCt.id,Expressions.asString(CREDIT).as("sideFlag"),qFPtPosting.ccRe2Id.id,Expressions.asString(RE2).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccRe2Id.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdCt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccRe2Id.id)
				.fetch());
//					UNION ALL
//					SELECT  jol_id, 
//							        post_date,
//								out_code, 
//								coa_id_dt, 
//								'DT', 
//								sum(amount) as amount,
//								coalesce(cc_re2_id,0) as cc_id,
//								'RE2' as cc_name
//							FROM pt_postings  
//							where jol_id=p_jol_id
//							group by  jol_id, post_date, out_code, coa_id_dt, coalesce (cc_re2_id,0) ) cc_all
			postList.addAll(queryFactory.select(qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.amount.sum().as("amount"),
					qFPtPosting.coaIdDt.id,Expressions.asString(DEBIT).as("sideFlag"),qFPtPosting.ccRe2Id.id,Expressions.asString(RE2).as("ccName"))
				.from(qFPtPosting)
				.where(qFPtPosting.jolId.eq(pJolId).and(qFPtPosting.ccRe2Id.isNotNull()).and(qFPtPosting.deleted.eq(false)))
				.groupBy(qFPtPosting.coaIdDt.id,qFPtPosting.postDate,qFPtPosting.outCode.id,qFPtPosting.ccRe2Id.id)
				.fetch());
//				where cc_all.cc_id !=0 ) loop
			for(Tuple vPostRecord: postList) {
				CCcOrganizationUnit vPostRecordOutCode = entityManager.getReference(CCcOrganizationUnit.class, vPostRecord.get(qFPtPosting.outCode.id));
				FChartAccount vPostRecordCoa = entityManager.getReference(FChartAccount.class, vPostRecord.get(3, Long.class));
//				       v_current_amount := v_post_record.amount;
				vCurrentAmount = vPostRecord.get(2,BigDecimal.class);
//				       --v_flag := 0;
//				       v_start_date := date_trunc('month',v_post_record.post_date)::date;
				LocalDate vStartDate = vPostRecord.get(qFPtPosting.postDate);
//				       v_start_period := to_char(v_start_date,'yyyymm');
				int vStartPeriodYear = vStartDate.getYear();
				int vStartPeriodMonth = vStartDate.getMonthValue();
//				       v_end_date := to_date(to_char(current_date,'yyyy')||'1231','yyyymmdd');
				LocalDate vEndDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);
//				       if extract (year from v_end_date) > extract(year from v_start_date) then
				if( vEndDate.getYear() > vStartDate.getYear() ) {
//	                                  v_end_date := to_date(to_char(v_start_date,'yyyy')||'1231','yyyymmdd');
					vEndDate = LocalDate.of(vStartDate.getYear(), 12, 31);
//				       end if;
				}
//				       if  v_journal_type='S' then 
				if( vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S ) {
//	                                   v_start_date:=date_trunc('year',v_post_record.post_date)::date;			
					vStartDate = LocalDate.of(vPostRecord.get(qFPtPosting.postDate).getYear(), 1, 1);
//	                                   v_start_period := to_char(v_start_date,'yyyy')||'00';
					vStartPeriodYear = vStartDate.getYear();
					vStartPeriodMonth = 0;
//				       end if;     
				}
				int vFlagPeriodYear = 0;
				int vFlagPeriodMonth = 0;
//				  while date_trunc('month',v_start_date)::date <= v_end_date loop
				while( !vStartDate.withDayOfMonth(1).isAfter( vEndDate ) ) {
//				       if (extract(month from v_start_date) = 1 and v_flag_period != v_start_period)  then
					if( vStartDate.getMonth() == Month.JANUARY && vFlagPeriodYear != vStartPeriodYear && vFlagPeriodMonth != vStartPeriodMonth ) {
//				          v_start_period := to_char(v_start_date,'yyyy')||'00';
						vStartPeriodYear = vStartDate.getYear();
						vStartPeriodMonth = 0;
//				       end if; 
					}
					//Prowerka, dali ima zapis za tozi mesec/godina
//				       if date_trunc('month',v_start_date)::date = date_trunc('month',v_post_record.post_date)::date then
					if( vStartDate.withDayOfMonth(1).isEqual(vPostRecord.get(qFPtPosting.postDate).withDayOfMonth(1))) {
//				          v_start_date := v_post_record.post_date;
						vStartDate = vPostRecord.get(qFPtPosting.postDate);
//				       else
					} else {
//	                                  v_start_date := date_trunc('month',v_start_date)::date;
						vStartDate = vStartDate.withDayOfMonth(1);
//	                   end if;   
					}
//	                --raise notice 'v_start_date: %1,%2',v_start_period,v_start_date;
//				       select ccb.id
//				       into v_ccb_id
//				       from pt_cc_balances as ccb
//				       where ccb.coa_id=v_post_record.coa_id
//				         and ccb.out_code=v_post_record.out_code
//				         and ccb.period::integer=v_start_period::integer
//				         and (case when v_post_record.cc_name='EBK' then (ccb.cc_ebk_id=v_post_record.cc_id or ccb.cc_ebk_id is null)
//				                   when v_post_record.cc_name='FUN' then (ccb.cc_fun_id=v_post_record.cc_id or ccb.cc_fun_id is null)
//				                   when v_post_record.cc_name='PRM' then (ccb.cc_prm_id=v_post_record.cc_id or ccb.cc_prm_id is null)
//				                   when v_post_record.cc_name='FIE' then (ccb.cc_fie_id=v_post_record.cc_id or ccb.cc_fie_id is null)
//				                   when v_post_record.cc_name='PAR' then (ccb.cc_par_id=v_post_record.cc_id or ccb.cc_par_id is null)
//				                   when v_post_record.cc_name='GTE' then (ccb.cc_gte_id=v_post_record.cc_id or ccb.cc_gte_id is null)
//				                   when v_post_record.cc_name='COT' then (ccb.cc_cot_id=v_post_record.cc_id or ccb.cc_cot_id is null)
//				                   when v_post_record.cc_name='OUT' then (ccb.cc_out_id=v_post_record.cc_id or ccb.cc_out_id is null)
//				                   when v_post_record.cc_name='RE1' then (ccb.cc_re1_id=v_post_record.cc_id or ccb.cc_re1_id is null)
//				                   when v_post_record.cc_name='RE2' then (ccb.cc_re2_id=v_post_record.cc_id or ccb.cc_re2_id is null)
//				                   end);
					QFPtCcBalance qFPtCcBalance = QFPtCcBalance.fPtCcBalance;
					List<FPtCcBalance> balList = queryFactory.select(qFPtCcBalance).from(qFPtCcBalance)
						.where(qFPtCcBalance.coaId.id.eq(vPostRecord.get(3,Long.class))
							.and(qFPtCcBalance.outCode.id.eq(vPostRecord.get(qFPtPosting.outCode.id)))
							.and(qFPtCcBalance.deleted.eq(false))
							.and(qFPtCcBalance.period.castToNum(Integer.class).eq( vStartPeriodYear*100+vStartPeriodMonth ))
							.and( (vPostRecord.get(6,String.class).equals(EBK) ? qFPtCcBalance.ccEbkId.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccEbkId.id.isNull())
									: (vPostRecord.get(6,String.class).equals(FUN) ? qFPtCcBalance.ccFunId.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccFunId.id.isNull())
										: (vPostRecord.get(6,String.class).equals(PRM) ? qFPtCcBalance.ccPrmId.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccPrmId.id.isNull())
											: (vPostRecord.get(6,String.class).equals(FIE) ? qFPtCcBalance.ccFieId.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccFieId.id.isNull())
												: (vPostRecord.get(6,String.class).equals(PAR) ? qFPtCcBalance.ccParId.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccParId.id.isNull())
													: (vPostRecord.get(6,String.class).equals(GTE) ? qFPtCcBalance.ccGteId.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccGteId.id.isNull())
														: (vPostRecord.get(6,String.class).equals(COT) ? qFPtCcBalance.ccCotId.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccCotId.id.isNull())
															: (vPostRecord.get(6,String.class).equals(OUT) ? qFPtCcBalance.ccOutId.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccOutId.id.isNull())
																: (vPostRecord.get(6,String.class).equals(RE1) ? qFPtCcBalance.ccRe1Id.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccRe1Id.id.isNull())
																	: qFPtCcBalance.ccRe2Id.id.eq( vPostRecord.get(5, Long.class) ).or(qFPtCcBalance.ccRe2Id.id.isNull())
																)
															)
														)
													)
												)
											)
										)
									)
								)
							)
						)
						.fetch();
//				        -- Nameren e zapis za syotvetnia mesec 
//				        if FOUND then
					if( balList.size() > 0 ) {
//				        -- Tyrsi se zapis za syotvetnia cost center
//				           select ccb.id
//				           into v_ccb_id_not_null
//				           from pt_cc_balances as ccb
//				           where ccb.coa_id=v_post_record.coa_id
//				             and ccb.out_code=v_post_record.out_code
//				             and ccb.period::integer=v_start_period::integer
//				             and (case when v_post_record.cc_name='EBK' then ccb.cc_ebk_id=v_post_record.cc_id
//				                       when v_post_record.cc_name='FUN' then ccb.cc_fun_id=v_post_record.cc_id
//				                       when v_post_record.cc_name='PRM' then ccb.cc_prm_id=v_post_record.cc_id
//				                       when v_post_record.cc_name='FIE' then ccb.cc_fie_id=v_post_record.cc_id
//				                       when v_post_record.cc_name='PAR' then ccb.cc_par_id=v_post_record.cc_id
//				                       when v_post_record.cc_name='GTE' then ccb.cc_gte_id=v_post_record.cc_id
//				                       when v_post_record.cc_name='COT' then ccb.cc_cot_id=v_post_record.cc_id
//				                       when v_post_record.cc_name='OUT' then ccb.cc_out_id=v_post_record.cc_id
//				                       when v_post_record.cc_name='RE1' then ccb.cc_re1_id=v_post_record.cc_id
//				                       when v_post_record.cc_name='RE2' then ccb.cc_re2_id=v_post_record.cc_id
//				                  end);
						FPtCcBalance vCcbIdNotNull = queryFactory.select(qFPtCcBalance).from(qFPtCcBalance)
								.where(qFPtCcBalance.coaId.id.eq(vPostRecord.get(3,Long.class))
									.and(qFPtCcBalance.outCode.id.eq(vPostRecord.get(qFPtPosting.outCode.id)))
									.and(qFPtCcBalance.deleted.eq(false))
									.and(qFPtCcBalance.period.castToNum(Integer.class).eq( vStartPeriodYear*100+vStartPeriodMonth ))
									.and( (vPostRecord.get(6,String.class).equals(EBK) ? qFPtCcBalance.ccEbkId.id.eq( vPostRecord.get(5, Long.class) )
											: (vPostRecord.get(6,String.class).equals(FUN) ? qFPtCcBalance.ccFunId.id.eq( vPostRecord.get(5, Long.class) )
												: (vPostRecord.get(6,String.class).equals(PRM) ? qFPtCcBalance.ccPrmId.id.eq( vPostRecord.get(5, Long.class) )
													: (vPostRecord.get(6,String.class).equals(FIE) ? qFPtCcBalance.ccFieId.id.eq( vPostRecord.get(5, Long.class) )
														: (vPostRecord.get(6,String.class).equals(PAR) ? qFPtCcBalance.ccParId.id.eq( vPostRecord.get(5, Long.class) )
															: (vPostRecord.get(6,String.class).equals(GTE) ? qFPtCcBalance.ccGteId.id.eq( vPostRecord.get(5, Long.class) )
																: (vPostRecord.get(6,String.class).equals(COT) ? qFPtCcBalance.ccCotId.id.eq( vPostRecord.get(5, Long.class) )
																	: (vPostRecord.get(6,String.class).equals(OUT) ? qFPtCcBalance.ccOutId.id.eq( vPostRecord.get(5, Long.class) )
																		: (vPostRecord.get(6,String.class).equals(RE1) ? qFPtCcBalance.ccRe1Id.id.eq( vPostRecord.get(5, Long.class) )
																			: qFPtCcBalance.ccRe2Id.id.eq( vPostRecord.get(5, Long.class) )
																		)
																	)
																)
															)
														)
													)
												)
											)
										)
									)
								)
								.fetchFirst();
//				            if FOUND then
						if( vCcbIdNotNull != null ) {
//				            ---Updatva namerenia zapis
//				            v_result:=update_cc_balances(v_ccb_id_not_null, v_start_date , v_start_period ,
//									v_post_record.cc_id , v_post_record.cc_name , v_post_record.side_flag,
//									v_post_record.amount, v_post_record.coa_id,
//									v_journal_type);
							accountingUpdateCcBalances(vCcbIdNotNull, vStartDate, vStartPeriodMonth, 
									vPostRecord.get(5, Long.class), vPostRecord.get(6,String.class), vPostRecord.get(4,String.class),
									vPostRecord.get(2, BigDecimal.class), vPostRecordCoa, 
									vJournalType);
//	                        else
						} else {
							//ima zaspis s null v syotvetnia cost center vzimase se tozi min id
//	                                       select min(ccb.id)
//	                                       into v_id_null
//	                                       from pt_cc_balances ccb
//	                                       where ccb.coa_id=v_post_record.coa_id
//				                 and ccb.out_code=v_post_record.out_code
//				                 and ccb.period::integer=v_start_period::integer
//				                 and (case when v_post_record.cc_name='EBK' then ccb.cc_ebk_id is null
//				                           when v_post_record.cc_name='FUN' then ccb.cc_fun_id is null
//										   when v_post_record.cc_name='PRM' then ccb.cc_prm_id is null
//				                           when v_post_record.cc_name='FIE' then ccb.cc_fie_id is null
//				                           when v_post_record.cc_name='PAR' then ccb.cc_par_id is null
//				                           when v_post_record.cc_name='GTE' then ccb.cc_gte_id is null
//				                           when v_post_record.cc_name='COT' then ccb.cc_cot_id is null
//				                           when v_post_record.cc_name='OUT' then ccb.cc_out_id is null
//				                           when v_post_record.cc_name='RE1' then ccb.cc_re1_id is null
//				                           when v_post_record.cc_name='RE2' then ccb.cc_re2_id is null
//				                       end);
							Long vIdNull = queryFactory.select(qFPtCcBalance.id.min()).from(qFPtCcBalance)
									.where(qFPtCcBalance.coaId.id.eq(vPostRecord.get(3,Long.class))
										.and(qFPtCcBalance.outCode.id.eq(vPostRecord.get(qFPtPosting.outCode.id)))
										.and(qFPtCcBalance.deleted.eq(false))
										.and(qFPtCcBalance.period.castToNum(Integer.class).eq( vStartPeriodYear*100+vStartPeriodMonth ))
										.and( (vPostRecord.get(6,String.class).equals(EBK) ? (qFPtCcBalance.ccEbkId.id.isNull())
												: (vPostRecord.get(6,String.class).equals(FUN) ? (qFPtCcBalance.ccFunId.id.isNull())
													: (vPostRecord.get(6,String.class).equals(PRM) ? (qFPtCcBalance.ccPrmId.id.isNull())
														: (vPostRecord.get(6,String.class).equals(FIE) ? (qFPtCcBalance.ccFieId.id.isNull())
															: (vPostRecord.get(6,String.class).equals(PAR) ? (qFPtCcBalance.ccParId.id.isNull())
																: (vPostRecord.get(6,String.class).equals(GTE) ? (qFPtCcBalance.ccGteId.id.isNull())
																	: (vPostRecord.get(6,String.class).equals(COT) ? (qFPtCcBalance.ccCotId.id.isNull())
																		: (vPostRecord.get(6,String.class).equals(OUT) ? (qFPtCcBalance.ccOutId.id.isNull())
																			: (vPostRecord.get(6,String.class).equals(RE1) ? (qFPtCcBalance.ccRe1Id.id.isNull())
																				: (qFPtCcBalance.ccRe2Id.id.isNull())
																			)
																		)
																	)
																)
															)
														)
													)
												)
											)
										)
									)
									.fetchFirst();
//				                --updatva namerenite zapisi s null
//				                v_result:=update_cc_balances(v_id_null, v_start_date , v_start_period ,
//									     v_post_record.cc_id , v_post_record.cc_name , v_post_record.side_flag,
//									     v_current_amount, v_post_record.coa_id,
//									     v_journal_type);
							accountingUpdateCcBalances(entityManager.getReference(FPtCcBalance.class, vIdNull), vStartDate, vStartPeriodMonth, 
									vPostRecord.get(5, Long.class), vPostRecord.get(6,String.class), vPostRecord.get(4,String.class),
									vCurrentAmount, vPostRecordCoa, 
									vJournalType);
//	                        end if;
						}
						//Wzemame nowata suma na CT/DT amount i s neq promenqme v_curent_amount
//				         select (case when v_post_record.cc_name='EBK' and (cc_ebk_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//											                    coalesce(cc_ebk_ct_amount,0)
//											                    else 0 end) +
//					                   (case when v_post_record.cc_name = 'EBK' and (cc_ebk_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//											                    coalesce(cc_ebk_dt_amount,0)
//											                    else 0 end) +
//									    (case when v_post_record.cc_name='FUN' and (cc_fun_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//											                    coalesce(cc_fun_ct_amount,0) 
//											                    else 0 end)+
//					                    (case when v_post_record.cc_name = 'FUN' and (cc_fun_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//											                    coalesce(cc_fun_dt_amount,0)
//											                    else 0 end)+
//				                         (case when v_post_record.cc_name='PRM' and (cc_prm_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_prm_ct_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name = 'PRM' and (cc_prm_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_prm_dt_amount,0)
//				                                               else 0  end)+
//	                                     (case when v_post_record.cc_name='FIE' and (cc_fie_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_fie_ct_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name = 'FIE' and (cc_fie_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_fie_dt_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name='PAR' and (cc_par_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_par_ct_amount,0)
//				                                               else 0 end)+
//				                          (case when v_post_record.cc_name = 'PAR' and (cc_par_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_par_dt_amount,0)
//				                                               else 0 end)+
//				                          (case when v_post_record.cc_name='GTE' and (cc_gte_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_gte_ct_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name = 'GTE' and (cc_gte_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_gte_dt_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name='COT' and (cc_cot_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_cot_ct_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name = 'COT' and (cc_cot_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_cot_dt_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name='OUT' and (cc_out_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_out_ct_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name = 'OUT' and (cc_out_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_out_dt_amount,0)
//				                                               else 0 end)+   			    
//				                         (case when v_post_record.cc_name='RE1' and (cc_re1_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_re1_ct_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name = 'RE1' and (cc_re1_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_re1_dt_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name = 'RE2' and (cc_re2_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_re2_ct_amount,0)
//				                                               else 0 end)+
//				                         (case when v_post_record.cc_name = 'RE2' and (cc_re2_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_re2_dt_amount,0)
//				                                               else 0 end)
//				            into v_running_total                                   
//				            from pt_cc_balances                                    
//				            where out_code=v_post_record.out_code
//				              and coa_id=v_post_record.coa_id
//				              and period::integer = v_start_period::integer
//				              and (case when v_post_record.cc_name='EBK' then (cc_ebk_id=v_post_record.cc_id)
//				                   when v_post_record.cc_name='FUN' then (cc_fun_id=v_post_record.cc_id)
//				                   when v_post_record.cc_name='PRM' then (cc_prm_id=v_post_record.cc_id)
//				                   when v_post_record.cc_name='FIE' then (cc_fie_id=v_post_record.cc_id)
//				                   when v_post_record.cc_name='PAR' then (cc_par_id=v_post_record.cc_id)
//				                   when v_post_record.cc_name='GTE' then (cc_gte_id=v_post_record.cc_id)
//				                   when v_post_record.cc_name='COT' then (cc_cot_id=v_post_record.cc_id)
//				                   when v_post_record.cc_name='OUT' then (cc_out_id=v_post_record.cc_id)
//				                   when v_post_record.cc_name='RE1' then (cc_re1_id=v_post_record.cc_id)
//				                   when v_post_record.cc_name='RE2' then (cc_re2_id=v_post_record.cc_id)
//				                   end);
						if( vCcbIdNotNull != null) {
							vRunningTotal = BigDecimal.ZERO;
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(EBK) && vCcbIdNotNull.getCcEbkId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcEbkCtAmount() != null ? vCcbIdNotNull.getCcEbkCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(EBK) && vCcbIdNotNull.getCcEbkId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcEbkDtAmount() != null ? vCcbIdNotNull.getCcEbkDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(FUN) && vCcbIdNotNull.getCcFunId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcFunCtAmount() != null ? vCcbIdNotNull.getCcFunCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(FUN) && vCcbIdNotNull.getCcFunId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcFunDtAmount() != null ? vCcbIdNotNull.getCcFunDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(PRM) && vCcbIdNotNull.getCcPrmId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcPrmCtAmount() != null ? vCcbIdNotNull.getCcPrmCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(PRM) && vCcbIdNotNull.getCcPrmId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcPrmDtAmount() != null ? vCcbIdNotNull.getCcPrmDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(FIE) && vCcbIdNotNull.getCcFieId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcFieCtAmount() != null ? vCcbIdNotNull.getCcFieCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(FIE) && vCcbIdNotNull.getCcFieId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcFieDtAmount() != null ? vCcbIdNotNull.getCcFieDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(PAR) && vCcbIdNotNull.getCcParId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcParCtAmount() != null ? vCcbIdNotNull.getCcParCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(PAR) && vCcbIdNotNull.getCcParId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcParDtAmount() != null ? vCcbIdNotNull.getCcParDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(GTE) && vCcbIdNotNull.getCcGteId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcGteCtAmount() != null ? vCcbIdNotNull.getCcGteCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(GTE) && vCcbIdNotNull.getCcGteId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcGteDtAmount() != null ? vCcbIdNotNull.getCcGteDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(COT) && vCcbIdNotNull.getCcCotId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcCotCtAmount() != null ? vCcbIdNotNull.getCcCotCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(COT) && vCcbIdNotNull.getCcCotId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcCotDtAmount() != null ? vCcbIdNotNull.getCcCotDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(OUT) && vCcbIdNotNull.getCcOutId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcOutCtAmount() != null ? vCcbIdNotNull.getCcOutCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(OUT) && vCcbIdNotNull.getCcOutId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcOutDtAmount() != null ? vCcbIdNotNull.getCcOutDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(RE1) && vCcbIdNotNull.getCcRe1Id().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcRe1CtAmount() != null ? vCcbIdNotNull.getCcRe1CtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(RE1) && vCcbIdNotNull.getCcRe1Id().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcRe1DtAmount() != null ? vCcbIdNotNull.getCcRe1DtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(RE2) && vCcbIdNotNull.getCcRe2Id().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (vCcbIdNotNull.getCcRe2CtAmount() != null ? vCcbIdNotNull.getCcRe2CtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
							vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(RE2) && vCcbIdNotNull.getCcRe2Id().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (vCcbIdNotNull.getCcRe2DtAmount() != null ? vCcbIdNotNull.getCcRe2DtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
//				        --pravi se zaradi nulevia mesec           
//				        IF NOT FOUND then
						} else {
//				          v_running_total := v_current_amount;
							vRunningTotal = vCurrentAmount;
//				        end if;              
						}
//					v_current_amount:=coalesce(v_running_total,0);
						vCurrentAmount = vRunningTotal;
//					v_flag:=1;
						vFlag = 1;
//					--raise notice 'v_running_total: %1,%2,%3',v_running_total,v_flag,v_current_amount;
						//Ne e nameren zapis za syotwetniqt mesec/godina            
//				        else
					} else {
						//Prowerka za predhodni danni
//				               select max(date_updated),max(period)
//				               into v_previous_date,v_previous_period  
//				               from pt_cc_balances as ccb
//				               where ccb.out_code=v_post_record.out_code
//				                 and ccb.coa_id=v_post_record.coa_id
//				                 and substr(period,1,4)=substr(v_start_period,1,4)
//				                 and period::integer<v_start_period::integer
//				                 and (case when v_post_record.cc_name='EBK' then ccb.cc_ebk_id=v_post_record.cc_id
//				                           when v_post_record.cc_name='FUN' then ccb.cc_fun_id=v_post_record.cc_id
//				                           when v_post_record.cc_name='PRM' then ccb.cc_prm_id=v_post_record.cc_id
//				                           when v_post_record.cc_name='FIE' then ccb.cc_fie_id=v_post_record.cc_id
//				                           when v_post_record.cc_name='PAR' then ccb.cc_par_id=v_post_record.cc_id
//				                           when v_post_record.cc_name='GTE' then ccb.cc_gte_id=v_post_record.cc_id
//				                           when v_post_record.cc_name='COT' then ccb.cc_cot_id=v_post_record.cc_id
//				                           when v_post_record.cc_name='OUT' then ccb.cc_out_id=v_post_record.cc_id
//				                           when v_post_record.cc_name='RE1' then ccb.cc_re1_id=v_post_record.cc_id
//				                           when v_post_record.cc_name='RE2' then ccb.cc_re2_id=v_post_record.cc_id
//				                      end); 
						Tuple prevData = queryFactory.select(qFPtCcBalance.lastModifiedDate.max(),qFPtCcBalance.period.max()).from(qFPtCcBalance)
							.where(qFPtCcBalance.coaId.id.eq(vPostRecord.get(3,Long.class))
								.and(qFPtCcBalance.outCode.id.eq(vPostRecord.get(qFPtPosting.outCode.id)))
								.and(qFPtCcBalance.deleted.eq(false))
								.and(qFPtCcBalance.period.substring(0, 4).castToNum(Integer.class).eq(vStartPeriodYear))
								.and(qFPtCcBalance.period.castToNum(Integer.class).lt( vStartPeriodYear*100+vStartPeriodMonth ))
								.and( (vPostRecord.get(6,String.class).equals(EBK) ? qFPtCcBalance.ccEbkId.id.eq( vPostRecord.get(5, Long.class) )
										: (vPostRecord.get(6,String.class).equals(FUN) ? qFPtCcBalance.ccFunId.id.eq( vPostRecord.get(5, Long.class) )
											: (vPostRecord.get(6,String.class).equals(PRM) ? qFPtCcBalance.ccPrmId.id.eq( vPostRecord.get(5, Long.class) )
												: (vPostRecord.get(6,String.class).equals(FIE) ? qFPtCcBalance.ccFieId.id.eq( vPostRecord.get(5, Long.class) )
													: (vPostRecord.get(6,String.class).equals(PAR) ? qFPtCcBalance.ccParId.id.eq( vPostRecord.get(5, Long.class) )
														: (vPostRecord.get(6,String.class).equals(GTE) ? qFPtCcBalance.ccGteId.id.eq( vPostRecord.get(5, Long.class) )
															: (vPostRecord.get(6,String.class).equals(COT) ? qFPtCcBalance.ccCotId.id.eq( vPostRecord.get(5, Long.class) )
																: (vPostRecord.get(6,String.class).equals(OUT) ? qFPtCcBalance.ccOutId.id.eq( vPostRecord.get(5, Long.class) )
																	: (vPostRecord.get(6,String.class).equals(RE1) ? qFPtCcBalance.ccRe1Id.id.eq( vPostRecord.get(5, Long.class) )
																		: qFPtCcBalance.ccRe2Id.id.eq( vPostRecord.get(5, Long.class) )
																	)
																)
															)
														)
													)
												)
											)
										)
									)
								)
							)
							.fetchFirst();
						Date vPreviousDate = prevData.get(0, Date.class);
						String vPreviousPeriod = prevData.get(1, String.class);
						//Nqma namereni predhodni danni
//				                 if v_previous_date is null  then
						if( vPreviousDate == null ) {
//				                    v_result := ins_pt_cc_balances (v_start_date, v_start_period, 
//				                                                    v_post_record.cc_id, 
//				                                                    v_post_record.cc_name, 
//				                                                    v_post_record.side_flag,
//				                                                    v_current_amount, 
//				                                                    v_post_record.coa_id, 
//				                                                    v_post_record.out_code, 
//				                                                    v_journal_type);
							accountingInsPtCcBalances(vStartDate, vStartPeriodYear, vStartPeriodMonth, vPostRecord.get(5, Long.class), 
									vPostRecord.get(6,String.class), vPostRecord.get(4,String.class), vCurrentAmount, 
									vPostRecordCoa, vPostRecordOutCode, vJournalType);
//	                                             v_flag := 1;
							vFlag = 1;
						//Nameren e predhoden zapis
//				                 else
						} else {
//				                      if v_flag = 0 then
							if( vFlag == 0 ) {
//				                         select (case when v_post_record.cc_name='EBK' and (cc_ebk_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_ebk_ct_amount,0)
//				                                               else 0 end) +
//								(case when v_post_record.cc_name = 'EBK' and (cc_ebk_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_ebk_dt_amount,0)
//				                                               else 0 end) +
//								(case when v_post_record.cc_name='FUN' and (cc_fun_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_fun_ct_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name = 'FUN' and (cc_fun_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_fun_dt_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name='PRM' and (cc_prm_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_prm_ct_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name = 'PRM' and (cc_prm_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_prm_dt_amount,0)
//				                                               else 0  end)+
//								(case when v_post_record.cc_name='FIE' and (cc_fie_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_fie_ct_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name = 'FIE' and (cc_fie_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_fie_dt_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name='PAR' and (cc_par_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_par_ct_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name = 'PAR' and (cc_par_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_par_dt_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name='GTE' and (cc_gte_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_gte_ct_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name = 'GTE' and (cc_gte_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_gte_dt_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name='COT' and (cc_cot_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_cot_ct_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name = 'COT' and (cc_cot_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_cot_dt_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name='OUT' and (cc_out_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_out_ct_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name = 'OUT' and (cc_out_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_out_dt_amount,0)
//				                                               else 0 end)+   			    
//								(case when v_post_record.cc_name='RE1' and (cc_re1_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_re1_ct_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name = 'RE1' and (cc_re1_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_re1_dt_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name='RE2' and (cc_re2_id=v_post_record.cc_id) and v_post_record.side_flag='CT' then
//				                                               coalesce(cc_re2_ct_amount,0)
//				                                               else 0 end)+
//								(case when v_post_record.cc_name = 'RE2' and (cc_re2_id=v_post_record.cc_id) and v_post_record.side_flag='DT' then 
//				                                               coalesce(cc_re2_dt_amount,0)
//				                                               else 0 end)
//							into v_running_total                                   
//							from pt_cc_balances                                    
//							where out_code=v_post_record.out_code
//							and coa_id=v_post_record.coa_id
//							and period::integer=v_previous_period::integer
//							and (case when v_post_record.cc_name='EBK' then (cc_ebk_id=v_post_record.cc_id)
//								when v_post_record.cc_name='FUN' then (cc_fun_id=v_post_record.cc_id)
//								when v_post_record.cc_name='PRM' then (cc_prm_id=v_post_record.cc_id)
//								when v_post_record.cc_name='FIE' then (cc_fie_id=v_post_record.cc_id)
//								when v_post_record.cc_name='PAR' then (cc_par_id=v_post_record.cc_id)
//								when v_post_record.cc_name='GTE' then (cc_gte_id=v_post_record.cc_id)
//								when v_post_record.cc_name='COT' then (cc_cot_id=v_post_record.cc_id)
//								when v_post_record.cc_name='OUT' then (cc_out_id=v_post_record.cc_id)
//								when v_post_record.cc_name='RE1' then (cc_re1_id=v_post_record.cc_id)
//								when v_post_record.cc_name='RE2' then (cc_re2_id=v_post_record.cc_id)
//								end);
								FPtCcBalance prevRec = queryFactory.select(qFPtCcBalance).from(qFPtCcBalance)
										.where(qFPtCcBalance.coaId.id.eq(vPostRecord.get(3,Long.class))
											.and(qFPtCcBalance.outCode.id.eq(vPostRecord.get(qFPtPosting.outCode.id)))
											.and(qFPtCcBalance.deleted.eq(false))
											.and(qFPtCcBalance.period.castToNum(Integer.class).eq( Integer.valueOf(vPreviousPeriod) ))
											.and( (vPostRecord.get(6,String.class).equals(EBK) ? qFPtCcBalance.ccEbkId.id.eq( vPostRecord.get(5, Long.class) )
													: (vPostRecord.get(6,String.class).equals(FUN) ? qFPtCcBalance.ccFunId.id.eq( vPostRecord.get(5, Long.class) )
														: (vPostRecord.get(6,String.class).equals(PRM) ? qFPtCcBalance.ccPrmId.id.eq( vPostRecord.get(5, Long.class) )
															: (vPostRecord.get(6,String.class).equals(FIE) ? qFPtCcBalance.ccFieId.id.eq( vPostRecord.get(5, Long.class) )
																: (vPostRecord.get(6,String.class).equals(PAR) ? qFPtCcBalance.ccParId.id.eq( vPostRecord.get(5, Long.class) )
																	: (vPostRecord.get(6,String.class).equals(GTE) ? qFPtCcBalance.ccGteId.id.eq( vPostRecord.get(5, Long.class) )
																		: (vPostRecord.get(6,String.class).equals(COT) ? qFPtCcBalance.ccCotId.id.eq( vPostRecord.get(5, Long.class) )
																			: (vPostRecord.get(6,String.class).equals(OUT) ? qFPtCcBalance.ccOutId.id.eq( vPostRecord.get(5, Long.class) )
																				: (vPostRecord.get(6,String.class).equals(RE1) ? qFPtCcBalance.ccRe1Id.id.eq( vPostRecord.get(5, Long.class) )
																					: qFPtCcBalance.ccRe2Id.id.eq( vPostRecord.get(5, Long.class) )
																				)
																			)
																		)
																	)
																)
															)
														)
													)
												)
											)
										)
										.fetchFirst();
//				                      if NOT FOUND then
								if( vPostRecord != null) {
									vRunningTotal = BigDecimal.ZERO;
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(EBK) && prevRec.getCcEbkId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcEbkCtAmount() != null ? prevRec.getCcEbkCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(EBK) && prevRec.getCcEbkId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcEbkDtAmount() != null ? prevRec.getCcEbkDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(FUN) && prevRec.getCcFunId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcFunCtAmount() != null ? prevRec.getCcFunCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(FUN) && prevRec.getCcFunId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcFunDtAmount() != null ? prevRec.getCcFunDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(PRM) && prevRec.getCcPrmId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcPrmCtAmount() != null ? prevRec.getCcPrmCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(PRM) && prevRec.getCcPrmId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcPrmDtAmount() != null ? prevRec.getCcPrmDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(FIE) && prevRec.getCcFieId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcFieCtAmount() != null ? prevRec.getCcFieCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(FIE) && prevRec.getCcFieId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcFieDtAmount() != null ? prevRec.getCcFieDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(PAR) && prevRec.getCcParId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcParCtAmount() != null ? prevRec.getCcParCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(PAR) && prevRec.getCcParId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcParDtAmount() != null ? prevRec.getCcParDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(GTE) && prevRec.getCcGteId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcGteCtAmount() != null ? prevRec.getCcGteCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(GTE) && prevRec.getCcGteId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcGteDtAmount() != null ? prevRec.getCcGteDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(COT) && prevRec.getCcCotId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcCotCtAmount() != null ? prevRec.getCcCotCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(COT) && prevRec.getCcCotId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcCotDtAmount() != null ? prevRec.getCcCotDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(OUT) && prevRec.getCcOutId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcOutCtAmount() != null ? prevRec.getCcOutCtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(OUT) && prevRec.getCcOutId().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcOutDtAmount() != null ? prevRec.getCcOutDtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(RE1) && prevRec.getCcRe1Id().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcRe1CtAmount() != null ? prevRec.getCcRe1CtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(RE1) && prevRec.getCcRe1Id().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcRe1DtAmount() != null ? prevRec.getCcRe1DtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(RE2) && prevRec.getCcRe2Id().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(CREDIT) ? (prevRec.getCcRe2CtAmount() != null ? prevRec.getCcRe2CtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
									vRunningTotal = vRunningTotal.add(vPostRecord.get(6,String.class).equals(RE2) && prevRec.getCcRe2Id().getId() == vPostRecord.get(5, Long.class) && vPostRecord.get(4,String.class).equals(DEBIT) ? (prevRec.getCcRe2DtAmount() != null ? prevRec.getCcRe2DtAmount() : BigDecimal.ZERO) : BigDecimal.ZERO);
								} else {
//				                          v_running_total := 0;
									vRunningTotal = BigDecimal.ZERO;
//				                      end if; 
								}
//				                      v_current_amount := v_current_amount + coalesce(v_running_total,0);
								vCurrentAmount = vCurrentAmount.add(vRunningTotal);
//				                   end if;
							}
//				                       v_result := ins_pt_cc_balances (v_start_date, v_start_period, 
//				                                                    v_post_record.cc_id, 
//				                                                    v_post_record.cc_name, 
//				                                                    v_post_record.side_flag,
//				                                                    v_current_amount, 
//				                                                    v_post_record.coa_id, 
//				                                                    v_post_record.out_code, 
//				                                                    v_journal_type);
							accountingInsPtCcBalances(vStartDate, vStartPeriodYear, vStartPeriodMonth, vPostRecord.get(5, Long.class), 
									vPostRecord.get(6,String.class), vPostRecord.get(4,String.class), vCurrentAmount, 
									vPostRecordCoa, vPostRecordOutCode, vJournalType);
//			                                v_flag := 1;
							vFlag = 1;
//	 			                 end if;
						}
//				        end if;
					}
					//exit when v_start_date >= v_end_date;
//				    -- Added 19.01.2010
//				    if substr(v_start_period,5,2)='00' then
					if( vStartPeriodMonth == 0 ) {
//					v_start_period:=to_char(v_start_date,'yyyymm');
						vStartPeriodYear = vStartDate.getYear();
						vStartPeriodMonth = vStartDate.getMonthValue();
//					v_start_date:=date_trunc('month',v_start_date)::date;
						vStartDate = vStartDate.withDayOfMonth(1);
//					v_flag_period:=v_start_period;
						vFlagPeriodYear = vStartPeriodYear;
						vFlagPeriodMonth = vStartPeriodMonth;
						vFlagPeriod = vFlagPeriodYear*100 + vFlagPeriodMonth;
//				    elsif substr(v_start_period,5,2)='12' then
					} else if( vStartPeriodMonth == 12 ) {
//					v_start_period:=to_char(v_start_date,'yyyy')||'13';
						vStartPeriodYear = vStartDate.getYear();
						vStartPeriodMonth = 13;
//					v_start_date:=date_trunc('month',v_start_date)::date;
						vStartDate = vStartDate.withDayOfMonth(1);
//				    else
					} else {
//				     v_start_date := v_start_date + interval '1 mons';
						vStartDate = vStartDate.plusMonths(1);
//				     v_start_period:=to_char(v_start_date,'yyyymm');
						vStartPeriodYear = vStartDate.getYear();
						vStartPeriodMonth = vStartDate.getMonthValue();
//				   end if;
					}
//				   end loop; 
				}
//				   v_flag_period:='0';
				vFlagPeriodYear = 0;
				vFlagPeriodMonth = 0;
				vFlagPeriod = 0;
//	     end loop;
			}
			LoiPtJournalCcStatusRepository loiPtJournalCcStatusRepository = ((LoiPtJournalCcStatusRepository) getRepositories().getRepositoryFor(LoiPtJournalCcStatus.class).get());
			LoiPtJournalCcStatus loiPtJournalCcStatusP = loiPtJournalCcStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtJournalCcStatus.PT_JOURNAL_CC_STATUS_P, pJolId.getCompany(), false);
			LoiPtPostingCcStatusRepository loiPtPostingCcStatusRepository = ((LoiPtPostingCcStatusRepository) getRepositories().getRepositoryFor(LoiPtPostingCcStatus.class).get());
			LoiPtPostingCcStatus loiPtPostingCcStatusP = loiPtPostingCcStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtPostingCcStatus.PT_POSTING_CC_STATUS_P, pJolId.getCompany(), false);
//	     update pt_postings
//	     set cc_status = 'P'
//	     where jol_id=p_jol_id;
			for(FPtPosting posting: pJolId.getFPtPostings()) {
				posting.setCcStatus(loiPtPostingCcStatusP);
				if(trySave(posting, true) == null) {
					throw new ReportException("populateJournals couldn't update FPtPosting ID "+posting.getId());
				}
			}
//	     update pt_journals
//	     set cc_status = 'P'
//	     where id=p_jol_id;	       
			pJolId.setCcStatus(loiPtJournalCcStatusP);
//	    return 0;
			return 0;
//	   end if;
		}
//	   return 1; 
		logger.error("populateJournals: 1 The status must be P or V and the ccStatus must be U");
		return 1;
//	END;
	}
	
	//CREATE FUNCTION accounting.get_parent_account(integer) RETURNS character varying
	private FChartAccount accountingGetParentAccount(FChartAccount pAccount) {
	//    LANGUAGE plpgsql
	//    AS $_$
	//declare 
	//v_parent integer;
	//begin 
	//select coalesce(coa_id_up,'0')
	//into v_parent
	//from ct_chart_of_accounts 
	//where id=$1;
	//
	//return v_parent;
		return pAccount.getCoaIdUp();
	//end;
	}
	
	//CREATE FUNCTION accounting.ins_pt_cc_balances(p_start_date date, p_period character varying, p_cc_id integer, p_cc_name character varying, p_side character varying, p_amount numeric, p_coa_id integer, p_out_code character varying, p_journal_type character varying) RETURNS void
	//    LANGUAGE plpgsql
	//    AS $$
	//DECLARE
	private void accountingInsPtCcBalances(LocalDate pStartDate, int pPeriodYear, int pPeriodMonth, Long pCcId, String pCcName, String pSide, BigDecimal pAmount, FChartAccount pCoaId, CCcOrganizationUnit pOutCode, LoiJournalTypeCalculationType pJournalType) {
	//BEGIN
	//			       /* INSERT INTO pt_cc_balances(id, out_code, coa_code, date_updated, cc_ebk_code, cc_ebk_ct_amount, 
	//                                                             cc_ebk_dt_amount, cc_fun_code, cc_fun_ct_amount, cc_fun_dt_amount, 
	//							     cc_prm_code, cc_prm_ct_amount, cc_prm_dt_amount, cc_fie_code, 
	//							     cc_fie_ct_amount, cc_fie_dt_amount, cc_par_code, cc_par_ct_amount, 
	//							     cc_par_dt_amount, cc_gte_code, cc_gte_ct_amount, cc_gte_dt_amount, 
	//							     cc_cot_code, cc_cot_ct_amount, cc_cot_dt_amount, cc_out_code, 
	//							     cc_out_ct_amount, cc_out_dt_amount, cc_re1_code, cc_re1_ct_amount, 
	//							     cc_re1_dt_amount, cc_re2_code, cc_re2_ct_amount, cc_re2_dt_amount,period) */
	//				INSERT INTO pt_cc_balances( id, out_code, coa_id, date_updated, 
	//				                            cc_ebk_id, cc_ebk_ct_amount, cc_ebk_dt_amount, 
	//				                            cc_fun_id, cc_fun_ct_amount, cc_fun_dt_amount, 
	//                                                            cc_prm_id, cc_prm_ct_amount, cc_prm_dt_amount, 
	//                                                            cc_fie_id, cc_fie_ct_amount, cc_fie_dt_amount, 
	//                                                            cc_par_id, cc_par_ct_amount, cc_par_dt_amount, 
	//                                                            cc_gte_id, cc_gte_ct_amount, cc_gte_dt_amount, 
	//                                                            cc_cot_id, cc_cot_ct_amount, cc_cot_dt_amount, 
	//                                                            cc_out_id, cc_out_ct_amount, cc_out_dt_amount, 
	//                                                            cc_re1_id, cc_re1_ct_amount, cc_re1_dt_amount, 
	//                                                            cc_re2_id, cc_re2_ct_amount, cc_re2_dt_amount, 
	//                                                          period)			     
		FPtCcBalance newBalance = new FPtCcBalance();
	//		                VALUES (nextval('ccb_seq'), 
	//		                        p_out_code, 
		newBalance.setOutCode(pOutCode);
	//		                        p_coa_id, 
		newBalance.setCoaId(pCoaId);
	//		                        (case when substr(p_period,5,2)='00' then date_trunc('year',p_start_date)::date
	//		                              when substr(p_period,5,2)='13' then to_date(to_char(p_start_date,'yyyymm')||'31','yyyymmdd')
	//		                              else p_start_date end),
		newBalance.setLastModifiedDate( pPeriodMonth == 0 ? Date.from(pStartDate.withDayOfYear(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
				: ( pPeriodMonth == 13 ? Date.from(pStartDate.withDayOfMonth(31).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
						: Date.from(pStartDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
					)
			);
	//					(case when p_cc_name='EBK' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='EBK' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='EBK' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='EBK' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='EBK' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(EBK)) {
			FCcEbk newCostCenter = entityManager.getReference(FCcEbk.class, pCcId);
			newBalance.setCcEbkId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='EBK' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='EBK' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='EBK' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='EBK' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='EBK' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end), 
			if( pSide.equals(CREDIT)) {
				newBalance.setCcEbkCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='EBK' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='EBK' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='EBK' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='EBK' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='EBK' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),
			if( pSide.equals(DEBIT)) {
				newBalance.setCcEbkDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					(case when p_cc_name='FUN' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='FUN' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='FUN' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='FUN' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='FUN' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(FUN)) {
			FCcFunction newCostCenter = entityManager.getReference(FCcFunction.class, pCcId);
			newBalance.setCcFunId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='FUN' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='FUN' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='FUN' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='FUN' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='FUN' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end), 
			if( pSide.equals(CREDIT)) {
				newBalance.setCcFunCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='FUN' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='FUN' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='FUN' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='FUN' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='FUN' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),
			if( pSide.equals(DEBIT)) {
				newBalance.setCcFunDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					(case when p_cc_name='PRM' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='PRM' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='PRM' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='PRM' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='PRM' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(PRM)) {
			FCcProgram newCostCenter = entityManager.getReference(FCcProgram.class, pCcId);
			newBalance.setCcPrmId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='PRM' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='PRM' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='PRM' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='PRM' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='PRM' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end), 
			if( pSide.equals(CREDIT)) {
				newBalance.setCcPrmCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='PRM' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='PRM' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='PRM' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='PRM' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='PRM' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),
			if( pSide.equals(DEBIT)) {
				newBalance.setCcPrmDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					(case when p_cc_name='FIE' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='FIE' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='FIE' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='FIE' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='FIE' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(FIE)) {
			FCcFinsource newCostCenter = entityManager.getReference(FCcFinsource.class, pCcId);
			newBalance.setCcFieId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='FIE' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='FIE' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='FIE' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='FIE' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='FIE' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end), 
			if( pSide.equals(CREDIT)) {
				newBalance.setCcFieCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='FIE' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='FIE' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='FIE' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='FIE' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='FIE' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),			
			if( pSide.equals(DEBIT)) {
				newBalance.setCcFieDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					(case when p_cc_name='PAR' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='PAR' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='PAR' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='PAR' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='PAR' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(PAR)) {
			CCcPartner newCostCenter = entityManager.getReference(CCcPartner.class, pCcId);
			newBalance.setCcParId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='PAR' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='PAR' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='PAR' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='PAR' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='PAR' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),	
			if( pSide.equals(CREDIT)) {
				newBalance.setCcParCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='PAR' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='PAR' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='PAR' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='PAR' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='PAR' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),					
			if( pSide.equals(DEBIT)) {
				newBalance.setCcParDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					(case when p_cc_name='GTE' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='GTE' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='GTE' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='GTE' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='GTE' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(GTE)) {
			CCcGoodsType newCostCenter = entityManager.getReference(CCcGoodsType.class, pCcId);
			newBalance.setCcGteId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='GTE' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='GTE' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='GTE' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='GTE' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='GTE' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end), 
			if( pSide.equals(CREDIT)) {
				newBalance.setCcGteCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='GTE' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='GTE' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='GTE' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='GTE' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='GTE' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),
			if( pSide.equals(DEBIT)) {
				newBalance.setCcGteDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					(case when p_cc_name='COT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='COT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='COT' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='COT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='COT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(COT)) {
			FCcContract newCostCenter = entityManager.getReference(FCcContract.class, pCcId);
			newBalance.setCcCotId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='COT' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='COT' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='COT' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='COT' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='COT' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end), 
			if( pSide.equals(CREDIT)) {
				newBalance.setCcCotCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='COT' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='COT' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='COT' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='COT' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='COT' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),		
			if( pSide.equals(DEBIT)) {
				newBalance.setCcCotDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					(case when p_cc_name='OUT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='OUT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='OUT' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='OUT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='OUT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(OUT)) {
			CCcOrganizationUnit newCostCenter = entityManager.getReference(CCcOrganizationUnit.class, pCcId);
			newBalance.setCcOutId( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='OUT' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='OUT' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='OUT' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='OUT' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='OUT' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end), 
			if( pSide.equals(CREDIT)) {
				newBalance.setCcOutCtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='OUT' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='OUT' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='OUT' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='OUT' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='OUT' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),
			if( pSide.equals(DEBIT)) {
				newBalance.setCcOutDtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					(case when p_cc_name='RE1' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='RE1' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='RE1' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='RE1' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='RE1' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(RE1)) {
			FCcReserve1 newCostCenter = entityManager.getReference(FCcReserve1.class, pCcId);
			newBalance.setCcRe1Id( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='RE1' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='RE1' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='RE1' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='RE1' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='RE1' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end), 
			if( pSide.equals(CREDIT)) {
				newBalance.setCcRe1CtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='RE1' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='RE1' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='RE1' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='RE1' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='RE1' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),
			if( pSide.equals(DEBIT)) {
				newBalance.setCcRe1DtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					(case when p_cc_name='RE2' and p_journal_type='S' and substr(p_period,5,2)='00' then p_cc_id 
	//					      when p_cc_name='RE2' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//					      when p_cc_name='RE2' and p_journal_type='E' and substr(p_period,5,2) = '13' then p_cc_id
	//					      when p_cc_name='RE2' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//					      when p_cc_name='RE2' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_cc_id
	//					      else null end), 
		if( pCcName.equals(RE2)) {
			FCcReserve2 newCostCenter = entityManager.getReference(FCcReserve2.class, pCcId);
			newBalance.setCcRe2Id( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? newCostCenter
					: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
							: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? newCostCenter
									: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
											: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? newCostCenter
													: null
												)
										)
								)
						)
				);
	//					(case when p_cc_name='RE2' and p_side='CT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='RE2' and p_side='CT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='RE2' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='RE2' and p_side='CT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='RE2' and p_side='CT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end), 
			if( pSide.equals(CREDIT)) {
				newBalance.setCcRe2CtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
	//					(case when p_cc_name='RE2' and p_side='DT' and p_journal_type='S' and substr(p_period,5,2)='00' then p_amount 
	//                                              when p_cc_name='RE2' and p_side='DT' and p_journal_type<>'S' and substr(p_period,5,2)='00' then null
	//                                              when p_cc_name='RE2' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)='13' then p_amount
	//                                              when p_cc_name='RE2' and p_side='DT' and p_journal_type='E' and substr(p_period,5,2)<>'13' then null
	//                                              when p_cc_name='RE2' and p_side='DT' and p_journal_type not in ('E') and substr(p_period,5,2)<>'00' then p_amount
	//					else null end),
			if( pSide.equals(DEBIT)) {
				newBalance.setCcRe2DtAmount( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? pAmount
						: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && pPeriodMonth == 0 ? null
								: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth == 13 ? pAmount
										: ( pJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 13 ? null
												: ( pJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && pPeriodMonth != 0 ? pAmount
														: null
													)
											)
									)
							)
					);
			}
		}
	//					p_period);
		newBalance.setPeriod(String.valueOf(pPeriodYear*100+pPeriodMonth));
		if(trySave(newBalance, true) == null) {
			throw new ReportException("accountingInsPtCcBalances couldn't update FPtCcBalance ID "+newBalance.getId());
		}
	//END;
	}
	
	//CREATE FUNCTION accounting.ins_pt_coa_balances(character varying, integer, character varying, date, date, character varying, numeric, character varying) RETURNS void
	private void accountingInsPtCoaBalances(CCcOrganizationUnit vCode, FChartAccount vAccount, int vStartPeriodYear, int vStartPeriodMonth, 
			LocalDate vStartDate, LocalDate vPostDate, LoiJournalTypeCalculationType vJournalType, BigDecimal vAmount, String vPost) {
	//    LANGUAGE plpgsql
	//    AS $_$
	//declare 
	//v_code alias for $1;
	//v_account alias for $2;
	//v_start_period alias for $3;
	//v_start_date alias for $4;
	//v_post_date alias for $5;
	//v_journal_type alias for $6;
	//v_amount alias for $7;
	//v_post alias for $8;
	//--v_main_out_code alias for $9;
	//begin
	//INSERT INTO pt_coa_balances(
	//				id, out_code, coa_id, date_updated, ct_amount, dt_amount,period)	
	//			VALUES
	//				(nextval('cab_seq'),v_code,v_account,
	//				 (CASE WHEN substr(v_start_period,5,2) = '00' then to_date(to_char(v_start_date,'yyyymm')||'01','yyyymmdd')
	//				       WHEN substr(v_start_period,5,2) = '13' then to_date(to_char(v_start_date,'yyyymm')||'31','yyyymmdd')
	//				       ELSE v_start_date END),
	//				 (CASE WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and v_journal_type='S' and substr(v_start_period,5,2)='00' then v_amount 
	//				       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and v_journal_type<>'S'and substr(v_start_period,5,2)='00' then 0
	//				       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and v_journal_type='E' and substr(v_start_period,5,2)='13' then v_amount
	//				       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and v_journal_type='E' and substr(v_start_period,5,2)<>'13' then 0
	//				       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and substr(v_start_period,5,2)<>'00' then v_amount 
	//				       ELSE 0 END),
	//    			 (CASE WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and v_journal_type='S' and substr(v_start_period,5,2)='00' then v_amount  
	//				       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and v_journal_type<>'S'and substr(v_start_period,5,2)='00' then 0
	//				       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and v_journal_type='E' and substr(v_start_period,5,2)='13' then v_amount
	//				       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and v_journal_type='E' and substr(v_start_period,5,2)<>'13' then 0
	//				       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and substr(v_start_period,5,2)<>'00' then v_amount 
	//    			       ELSE 0 END),
	//    				 v_start_period
	//				);
	//
	//end;
		FPtCoaBalance newBal = new FPtCoaBalance();
		newBal.setOutCode(vCode);
		newBal.setCoaId(vAccount);
		Calendar postDate = Calendar.getInstance();
		postDate.set(vPostDate.getYear(),vPostDate.getMonthValue(),vPostDate.getDayOfMonth());
		newBal.setDateUpdated(vStartPeriodMonth == 13 ? vStartDate.withDayOfMonth(31)
				: (vStartPeriodMonth == 0 ? vStartDate.withDayOfMonth(1)
						: vStartDate
					)
			);
		newBal.setCtAmount( !( !vStartDate.isBefore(vPostDate) && vStartDate.getYear() == vPostDate.getYear() && vPost.equals(CREDIT)) ?
				BigDecimal.ZERO
				: (vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && vStartPeriodMonth == 0 ?
					vAmount
					: (vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && vStartPeriodMonth == 0 ?
							BigDecimal.ZERO
							:(vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && vStartPeriodMonth == 13 ?
									vAmount
									:(vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && vStartPeriodMonth != 13 ?
											BigDecimal.ZERO
											:(vStartPeriodMonth != 0 ? vAmount : BigDecimal.ZERO)
										)
								)
						)
				)
			);
		newBal.setDtAmount( !( !vStartDate.isBefore(vPostDate) && vStartDate.getYear() == vPostDate.getYear() && vPost.equals(DEBIT)) ?
				BigDecimal.ZERO
				: (vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && vStartPeriodMonth == 0 ?
					vAmount
					: (vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && vStartPeriodMonth == 0 ?
							BigDecimal.ZERO
							:(vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && vStartPeriodMonth == 13 ?
									vAmount
									:(vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && vStartPeriodMonth != 13 ?
											BigDecimal.ZERO
											:(vStartPeriodMonth != 0 ? vAmount : BigDecimal.ZERO)
										)
								)
						)
				)
			);
		newBal.setPeriod( String.valueOf(vStartPeriodYear*100+vStartPeriodMonth) );
		if(trySave(newBal, true) == null) {
			throw new ReportException("accountingInsPtCoaBalances couldn't create new FPtCoaBalance");
		}
	}
	
	private int accountingCalcBalance(FChartAccount vAccount, LocalDate pPostDate, String vPost, BigDecimal vAmount, 
			CCcOrganizationUnit vCode, LoiJournalTypeCalculationType vJournalType) {
		LocalDate vPostDate = null;
		int vPostPeriodYear,vPostPeriodMonth;
		LocalDate vStartDate = null;
		int vStartPeriodYear,vStartPeriodMonth;
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);

		logger.trace("accountingCalcBalance: outCode "+vCode.getCode()+"; acc "+vAccount.getCode()+"; date "+pPostDate);
		//Proverka za nalichie na zapisi za dadenata smetka
//        if v_journal_type = 'S' then
		if(vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S) {
//           v_post_date := date_trunc('year',v_post_date_1)::date;
			vPostDate = LocalDate.ofYearDay(pPostDate.getYear(),1);
			logger.trace("accountingCalcBalance: Set start of year for calc of initial balance");
//        else    
		} else {
//           v_post_date := v_post_date_1;
			vPostDate = pPostDate;
//        end if;
		}
		
//	    select into v_coa_id coa_id
//	    from pt_coa_balances 
//	    where coa_id=v_account
//	      and extract(year from date_updated)=extract(year from v_post_date)
//	      and out_code=v_code;
		QFPtCoaBalance qFPtCoaBalance = QFPtCoaBalance.fPtCoaBalance;
		FChartAccount vCoaId = queryFactory.select(qFPtCoaBalance.coaId)
			.from(qFPtCoaBalance)
			.where(qFPtCoaBalance.coaId.eq(vAccount)
					.and(qFPtCoaBalance.outCode.eq(vCode))
					.and(qFPtCoaBalance.deleted.eq(false))
					.and(qFPtCoaBalance.dateUpdated.year().eq(vPostDate.getYear()))
				)
			.fetchFirst();
		
		//Nqma zapisi w PT_COA_BLANCES;
		//Triabwa da se syzdadat zapisi ot nachaloto na godinata do momenta na postinga/dneshna data
//	    v_current_period :=extract(year from current_date)::varchar||'12';
		int vCurrentPeriodYear = LocalDate.now().getYear();
		int vCurrentPeriodMonth = 12;
//	    v_current_date := extract(year from current_date)::varchar||'1231';
		LocalDate vCurrentDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);
//	/*    if current_date < v_post_date then
//	       return 1;
//	    end if;
//	*/    
//	    if substr(v_current_period,1,4) > to_char(v_post_date,'yyyy') then
		if(vCurrentPeriodYear > vPostDate.getYear()) {
//	       v_current_period := to_char(v_post_date,'yyyy')||'12';
			vCurrentPeriodYear = vPostDate.getYear();
			vCurrentPeriodMonth = 12;
//	       v_current_date := to_date(to_char(v_post_date,'yyyy')||'1231','yyyymmdd');
			vCurrentDate = LocalDate.of(vPostDate.getYear(), 12, 31);
//	    end if;
		}
//
//	    if extract(month from v_current_date) = 12 then
		if(vCurrentDate.getMonth() == Month.DECEMBER) {
//	       v_current_period := to_char(v_current_date,'yyyy')||'13';
			vCurrentPeriodYear = vCurrentDate.getYear();
			vCurrentPeriodMonth = 13;
//	    end if;
		}
//
//	    if NOT FOUND then
		if(vCoaId == null) {
			logger.trace("accountingCalcBalance: Account not found, creating new");
//	        v_start_date:=date_trunc('year',v_post_date)::date;
			vStartDate = LocalDate.ofYearDay(vPostDate.getYear(), 1);
//	        v_start_period := to_char(v_start_date,'yyyymm');
			vStartPeriodYear = vStartDate.getYear();
			vStartPeriodMonth = vStartDate.getMonthValue();
//	        loop
			do {
//	            if date_trunc('month',v_start_date)::date=date_trunc('month',v_post_date)::date then
//	                v_start_date:=v_post_date;
//	            end if;
				if(vStartDate.getYear() == vPostDate.getYear() && vStartDate.getMonth() == vPostDate.getMonth()) {
					vStartDate = vPostDate;
				}
//	    /*
//	            INSERT INTO pt_coa_balances(
//	                id, out_code, coa_code, date_updated, ct_amount, dt_amount,period)    
//	            VALUES
//	                (nextval('cab_seq'),v_code,v_account,
//	                 (CASE WHEN substr(v_start_period,5,2) = '00' then to_date(v_start_period||'01','yyyymmdd')
//	                       WHEN substr(v_start_period,5,2) = '13' then to_date(v_start_period||'31','yyyymmdd')
//	                       ELSE v_start_date END),
//	                 (CASE WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and v_journal_type='S' and substr(v_start_period,5,2)='00' then v_amount 
//	                       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and v_journal_type<>'S'and substr(v_start_period,5,2)='00' then 0
//	                       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and v_journal_type='E' and substr(v_start_period,5,2)='13' then v_amount
//	                       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and v_journal_type='E' and substr(v_start_period,5,2)<>'13' then 0
//	                       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='CT' and substr(v_start_period,5,2)<>'00' then v_amount 
//	                       ELSE 0 END),
//	                     (CASE WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and v_journal_type='S' and substr(v_start_period,5,2)='00' then v_amount  
//	                       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and v_journal_type<>'S'and substr(v_start_period,5,2)='00' then 0
//	                       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and v_journal_type='E' and substr(v_start_period,5,2)='13' then v_amount
//	                       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and v_journal_type='E' and substr(v_start_period,5,2)<>'13' then 0
//	                       WHEN v_start_date>=v_post_date and extract(year from v_start_date)=extract(year from v_post_date) and v_post='DT' and substr(v_start_period,5,2)<>'00' then v_amount 
//	                           ELSE 0 END),
//	                     v_start_period
//	                );
//	            */
				//Ako meseca 1 pyrvi se syzdava i nulevia period, ako e 12 se syzdava i 13
//	        if extract (month from v_start_date)=1 then 
				if(vStartDate.getMonth() == Month.JANUARY) {
//	            v_start_period := to_char(v_start_date,'yyyy')||'00';
					vStartPeriodYear = vStartDate.getYear();
					vStartPeriodMonth = 0;
//	            v_result:= ins_pt_coa_balances(v_code,v_account,v_start_period,v_start_date,v_post_date,v_journal_type,v_amount,v_post);
					accountingInsPtCoaBalances(vCode, vAccount, vStartPeriodYear, vStartPeriodMonth, vStartDate, vPostDate, vJournalType, vAmount, vPost);
//	            v_start_period:=to_char(v_start_date,'yyyymm');
					vStartPeriodYear = vStartDate.getYear();
					vStartPeriodMonth = vStartDate.getMonthValue();
//	            v_result:= ins_pt_coa_balances(v_code,v_account,v_start_period,v_start_date,v_post_date,v_journal_type,v_amount,v_post);
					accountingInsPtCoaBalances(vCode, vAccount, vStartPeriodYear, vStartPeriodMonth, vStartDate, vPostDate, vJournalType, vAmount, vPost);
//	        elsif extract(month from v_start_date) =12 then
				} else if(vStartDate.getMonth() == Month.DECEMBER) {
//	            v_result:= ins_pt_coa_balances(v_code,v_account,v_start_period,v_start_date,v_post_date,v_journal_type,v_amount,v_post);
					accountingInsPtCoaBalances(vCode, vAccount, vStartPeriodYear, vStartPeriodMonth, vStartDate, vPostDate, vJournalType, vAmount, vPost);
//	            v_start_period := to_char(v_start_date,'yyyy')||'13';
					vStartPeriodYear = vStartDate.getYear();
					vStartPeriodMonth = 13;
//	            v_result:= ins_pt_coa_balances(v_code,v_account,v_start_period,v_start_date,v_post_date,v_journal_type,v_amount,v_post);
					accountingInsPtCoaBalances(vCode, vAccount, vStartPeriodYear, vStartPeriodMonth, vStartDate, vPostDate, vJournalType, vAmount, vPost);
//	        else 
				} else {
//	             v_result:= ins_pt_coa_balances(v_code,v_account,v_start_period,v_start_date,v_post_date,v_journal_type,v_amount,v_post);
					accountingInsPtCoaBalances(vCode, vAccount, vStartPeriodYear, vStartPeriodMonth, vStartDate, vPostDate, vJournalType, vAmount, vPost);
//	        end if;   
				}
//
//	        exit when v_start_period::integer >= v_current_period::integer ;
				if( (vStartPeriodYear*100+vStartPeriodMonth) >= (vCurrentPeriodYear*100+vCurrentPeriodMonth) ) break;
//	                   v_start_date:=(date_trunc('month',v_start_date) + interval '1 months')::date;
				vStartDate = vStartDate.plusMonths(1);
//	               v_start_period := to_char(v_start_date,'yyyymm');    
				vStartPeriodYear = vStartDate.getYear();
				vStartPeriodMonth = vStartDate.getMonthValue();
//	        end loop;
			} while(true);

			//ako ima syzdadeni periodi za smetkata v syotvetnata godina    
//	        else
		} else {
			logger.trace("accountingCalcBalance: Account found, ID "+vCoaId.getId());
//	        select max(date_updated),max(period)
//	        into v_max_date,v_max_period 
//	        from pt_coa_balances
//	        where coa_id=v_account
//	          and extract(year from date_updated)=extract(year from v_post_date)
//	          and out_code=v_code;
			Tuple queryResult = queryFactory.select(qFPtCoaBalance.dateUpdated.max(),qFPtCoaBalance.period.max())
					.from(qFPtCoaBalance)
					.where(qFPtCoaBalance.coaId.eq(vAccount)
							.and(qFPtCoaBalance.outCode.eq(vCode))
							.and(qFPtCoaBalance.deleted.eq(false))
							.and(qFPtCoaBalance.dateUpdated.year().eq(vPostDate.getYear()))
						)
					.fetchFirst();

			String maxPeriodString = queryResult.get(1, String.class);
			int vMaxPeriodYear = Integer.valueOf(maxPeriodString.substring(0, 4));
			int vMaxPeriodMonth = Integer.valueOf(maxPeriodString.substring(4, 6));
			LocalDate vMaxDate = queryResult.get(0, LocalDate.class);

//	        if to_char(v_post_date,'mm')='01' then
			if(vPostDate.getMonth() == Month.JANUARY) {
//	           v_post_period := to_char(v_post_date,'yyyy')||'00';
				vPostPeriodYear = vPostDate.getYear();
				vPostPeriodMonth = 0;
//	        else
			} else {
//	                   v_post_period := to_char(v_post_date,'yyyymm');
				vPostPeriodYear = vPostDate.getYear();
				vPostPeriodMonth = vPostDate.getMonthValue();
//	        end if;
			}

//	        if v_max_period::integer>=v_post_period::integer then --date_trunc('month',v_max_date)::date>=date_trunc('month',v_post_date)::date
			if( (vMaxPeriodYear*100+vMaxPeriodMonth) >= (vPostPeriodYear*100+vPostPeriodMonth) ) {
//	            update pt_coa_balances 
//	                set ct_amount=(case when v_post='CT' and v_journal_type = 'S' and substr(period,5,2)='00' then ct_amount+v_amount
//	                                when v_post='CT' and v_journal_type <> 'S' and substr(period,5,2)='00' then ct_amount
//	                                    when v_post='CT' and v_journal_type='E' and substr(period,5,2)='13' then ct_amount+v_amount  
//	                                    when v_post='CT' and v_journal_type <> 'E' and substr(period,5,2)<>'00' then ct_amount+v_amount
//	                                    else ct_amount end),
//	                dt_amount=(case when v_post='DT' and v_journal_type = 'S' and substr(period,5,2)='00' then dt_amount+v_amount
//	                                when v_post='DT' and v_journal_type <> 'S' and substr(period,5,2)='00' then dt_amount 
//	                                    when v_post='DT' and v_journal_type='E' and substr(period,5,2)='13' then dt_amount+v_amount  
//	                                    when v_post='DT' and v_journal_type <> 'E' and substr(period,5,2)<>'00' then dt_amount+v_amount
//	                                    else dt_amount end),
//	                date_updated = (case when substr(period,5,2)='13' then to_date(substr(period,1,4)||'1231','yyyymmdd')
//	                                     when substr(period,5,2)='00' then to_date(substr(period,1,4)||'0101','yyyymmdd')
//	                                     else greatest (date_updated,v_post_date) end)
//	            where coa_id=v_account
//	            and out_code=v_code
//	            and period::integer>=v_post_period::integer
//	            and substr(period,1,4)=substring(v_post_period,1,4);
				List<FPtCoaBalance> balList = queryFactory.select(qFPtCoaBalance).from(qFPtCoaBalance)
						.where(qFPtCoaBalance.coaId.eq(vAccount)
								.and(qFPtCoaBalance.outCode.eq(vCode))
								.and(qFPtCoaBalance.deleted.eq(false))
								.and(qFPtCoaBalance.period.castToNum(Integer.class).goe(vPostPeriodYear*100+vPostPeriodMonth))
								.and(qFPtCoaBalance.period.substring(0, 4).castToNum(Integer.class).eq(vPostPeriodYear))
							)
						.fetch();
				for(FPtCoaBalance bal: balList) {
					bal.setCtAmount((vPost.equals(CREDIT) && vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && bal.getPeriod().substring(4, 6).equals("00")) ?
							bal.getCtAmount().add(vAmount)
							: (vPost.equals(CREDIT) && vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && bal.getPeriod().substring(4, 6).equals("00") ?
									bal.getCtAmount()
									:(vPost.equals(CREDIT) && vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && bal.getPeriod().substring(4, 6).equals("13") ?
											bal.getCtAmount().add(vAmount)
											:(vPost.equals(CREDIT) && vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && !bal.getPeriod().substring(4, 6).equals("00") ?
													bal.getCtAmount().add(vAmount)
													:bal.getCtAmount()
												)
										)
								)
						);
					bal.setDtAmount((vPost.equals(DEBIT) && vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && bal.getPeriod().substring(4, 6).equals("00")) ?
							bal.getDtAmount().add(vAmount)
							: (vPost.equals(DEBIT) && vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && bal.getPeriod().substring(4, 6).equals("00") ?
									bal.getDtAmount()
									:(vPost.equals(DEBIT) && vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && bal.getPeriod().substring(4, 6).equals("13") ?
											bal.getDtAmount().add(vAmount)
											:(vPost.equals(DEBIT) && vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && !bal.getPeriod().substring(4, 6).equals("00") ?
													bal.getDtAmount().add(vAmount)
													:bal.getDtAmount()
												)
										)
								)
						);
					bal.setDateUpdated(bal.getPeriod().substring(4, 6).equals("13") ?
							LocalDate.of(Integer.valueOf(bal.getPeriod().substring(0,4)),12,31)
							: (bal.getPeriod().substring(4, 6).equals("00") ?
									LocalDate.of(Integer.valueOf(bal.getPeriod().substring(0,4)),1,1)
									: (bal.getDateUpdated().isAfter(vPostDate) ?
											bal.getDateUpdated()
											: vPostDate
										)
								)
						);
					if(trySave(bal, true) == null) {
						throw new ReportException("accountingCalcBalance couldn't update FPtCoaBalance ID "+bal.getId());
					}
				}
//	        elsif v_max_period::integer < v_post_period::integer then --date_trunc('month',v_max_date)::date<date_trunc('month',v_post_date)::date then
			} else if( (vMaxPeriodYear*100+vMaxPeriodMonth) < (vPostPeriodYear*100+vPostPeriodMonth) ) {
//	            v_start_date:=v_max_date;
				vStartDate = vMaxDate;
//	            v_start_period := v_max_period;
				vStartPeriodYear = vMaxPeriodYear;
				vStartPeriodMonth = vMaxPeriodMonth;
//
//	            if substr(v_post_period,5,2)='12' then
				if(vPostPeriodMonth == 12) {
//	               v_post_period := to_char(v_post_date,'yyyy')||'13';
					vPostPeriodYear = vPostDate.getYear();
					vPostPeriodMonth = 13;
//	            end if;
				}
//
//	            loop
				do {
//	                v_start_date:=date_trunc('month',v_start_date + interval '1 months')::date;
					vStartDate = vStartDate.plusMonths(1);
//	                v_start_period := (v_max_period::integer + 1)::varchar;
					vStartPeriodYear = vMaxPeriodYear;
					vStartPeriodMonth = vMaxPeriodMonth+1;
//
//	                INSERT INTO pt_coa_balances(
//	                    id, out_code, coa_id, date_updated, ct_amount, dt_amount,period)
//	                    SELECT nextval('cab_seq'),out_code,coa_id,v_start_date,ct_amount,dt_amount,v_start_period
//	                    from pt_coa_balances
//	                    where out_code=v_code
//	                    and coa_id=v_account
//	                    and period::integer=v_max_period::integer;
					FPtCoaBalanceRepository fPtCoaBalanceRepository = ((FPtCoaBalanceRepository) getRepositories().getRepositoryFor(FPtCoaBalance.class).get());
					FPtCoaBalance prevFPtCoaBalance = fPtCoaBalanceRepository.findFirstByCoaIdAndOutCodeAndPeriodAndCompanyAndDeleted(
							vAccount, vCode, String.valueOf(vMaxPeriodYear*100+vMaxPeriodMonth), vCode.getCompany(), false);
					FPtCoaBalance newBal = new FPtCoaBalance();
					newBal.setOutCode(vCode);
					newBal.setCoaId(vAccount);
					newBal.setDateUpdated(vStartDate);
					newBal.setCtAmount(prevFPtCoaBalance.getCtAmount());
					newBal.setDtAmount(prevFPtCoaBalance.getDtAmount());
					newBal.setPeriod( String.valueOf(vStartPeriodYear*100+vStartPeriodMonth) );
					if(trySave(newBal, true) == null) {
						throw new ReportException("accountingCalcBalance couldn't create new FPtCoaBalance");
					}
//	            exit when v_start_period::integer>=v_post_period::integer;
//	            end loop;
				} while( (vStartPeriodYear*100+vStartPeriodMonth) >= (vPostPeriodYear*100+vPostPeriodMonth) );

//	            update pt_coa_balances 
//	            set    ct_amount=(case when v_post='CT' and v_journal_type = 'S' and substr(period,5,2)='00' then ct_amount+v_amount
//	                                when v_post='CT' and v_journal_type <> 'S' and substr(period,5,2)='00' then ct_amount
//	                                    when v_post='CT' and v_journal_type='E' and substr(period,5,2)='13' then ct_amount+v_amount  
//	                                    when v_post='CT' and v_journal_type <> 'E' and substr(period,5,2)<>'00' then ct_amount+v_amount
//	                                    else ct_amount end),
//	                dt_amount=(case when v_post='DT' and v_journal_type = 'S' and substr(period,5,2)='00' then dt_amount+v_amount
//	                                when v_post='DT' and v_journal_type <> 'S' and substr(period,5,2)='00' then dt_amount
//	                                    when v_post='DT' and v_journal_type='E' and substr(period,5,2)='13' then dt_amount+v_amount  
//	                                    when v_post='DT' and v_journal_type <> 'E' and substr(period,5,2)<>'00' then dt_amount+v_amount
//	                                    else dt_amount end),
//	                date_updated = (case when substr(period,5,2)='13' then to_date(substr(period,1,4)||'1231','yyyymmdd')
//	                                     when substr(period,5,2)='00' then to_date(substr(period,1,4)||'0101','yyyymmdd')
//	                                     else greatest (date_updated,v_post_date) end)
//	            where coa_id=v_account
//	            and out_code=v_code
//	            and period::integer>=v_post_period::integer
//	            and substr(period,1,4)=substring(v_post_period,1,4);
				List<FPtCoaBalance> balList = queryFactory.select(qFPtCoaBalance).from(qFPtCoaBalance)
						.where(qFPtCoaBalance.coaId.eq(vAccount)
								.and(qFPtCoaBalance.outCode.eq(vCode))
								.and(qFPtCoaBalance.deleted.eq(false))
								.and(qFPtCoaBalance.period.castToNum(Integer.class).goe(vPostPeriodYear*100+vPostPeriodMonth))
								.and(qFPtCoaBalance.period.substring(0, 4).castToNum(Integer.class).eq(vPostPeriodYear))
							)
						.fetch();
				for(FPtCoaBalance bal: balList) {
					bal.setCtAmount((vPost.equals(CREDIT) && vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && bal.getPeriod().substring(4, 6).equals("00")) ?
							bal.getCtAmount().add(vAmount)
							: (vPost.equals(CREDIT) && vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && bal.getPeriod().substring(4, 6).equals("00") ?
									bal.getCtAmount()
									:(vPost.equals(CREDIT) && vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && bal.getPeriod().substring(4, 6).equals("13") ?
											bal.getCtAmount().add(vAmount)
											:(vPost.equals(CREDIT) && vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && !bal.getPeriod().substring(4, 6).equals("00") ?
													bal.getCtAmount().add(vAmount)
													:bal.getCtAmount()
												)
										)
								)
						);
					bal.setDtAmount((vPost.equals(DEBIT) && vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && bal.getPeriod().substring(4, 6).equals("00")) ?
							bal.getDtAmount().add(vAmount)
							: (vPost.equals(DEBIT) && vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_S && bal.getPeriod().substring(4, 6).equals("00") ?
									bal.getDtAmount()
									:(vPost.equals(DEBIT) && vJournalType.getListOptionItemCode() == LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && bal.getPeriod().substring(4, 6).equals("13") ?
											bal.getDtAmount().add(vAmount)
											:(vPost.equals(DEBIT) && vJournalType.getListOptionItemCode() != LoiJournalTypeCalculationType.JOURNAL_TYPE_CALCULATION_TYPE_E && !bal.getPeriod().substring(4, 6).equals("00") ?
													bal.getDtAmount().add(vAmount)
													:bal.getDtAmount()
												)
										)
								)
						);
					bal.setDateUpdated(bal.getPeriod().substring(4, 6).equals("13") ?
							LocalDate.of(Integer.valueOf(bal.getPeriod().substring(0,4)),12,31)
							: (bal.getPeriod().substring(4, 6).equals("00") ?
									LocalDate.of(Integer.valueOf(bal.getPeriod().substring(0,4)),1,1)
									: (bal.getDateUpdated().isAfter(vPostDate) ?
											bal.getDateUpdated()
											: vPostDate
										)
								)
						);
					if(trySave(bal, true) == null) {
						throw new ReportException("accountingCalcBalance couldn't update FPtCoaBalance ID "+bal.getId());
					}
				}
//	        end if;
			}
//	    end if;
		}
//
//	    v_parent:=get_parent_account(v_account);
		FChartAccount vParent = accountingGetParentAccount(vAccount);
//
//	    if v_parent!='0' then 
		if(vParent != null) {
//	        v_flag:=calc_balance (v_parent,v_post_date,v_post,v_amount,v_code,v_journal_type);
			int vFlag = accountingCalcBalance(vParent, vPostDate, vPost, vAmount, vCode, vJournalType);
//	        if v_flag = 1 then
			if(vFlag == 1) {
//	           return 1;
				logger.trace("accountingCalcBalance: 1 Parent calc error");
				return 1;
//	        end if;
			}
//	    else 
		} else {
//	        return 0;
			logger.trace("accountingCalcBalance: Finish (no parent)");
			return 0;
//	    end if;
		}
//
//	    return 0;
		logger.trace("accountingCalcBalance: Finish");
		return 0;
	}

	//accounting.aggregate_journals
	private Integer accountingAggregateJournals(FPtJournal pJol){
		if(pJol == null) {
			logger.error("accountingAggregateJournals: 1 Parameter for FPtJournal is null");
			return 1;
		}
		LoiPtJournalStatus vStatus = pJol.getStatus();
		LoiJournalTypeCalculationType vJournalType = pJol.getJteId().getType();
		if(vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_V) {
			List<Object[]> queryResult = entityManager.createNativeQuery("select  post_date,account,flag,out_code_id,sum(amount) as amount\n" + 
					"from (\n" + 
					"    select post_date,coa_id_ct_id as account,amount,'CT' as flag,out_code_id\n" + 
					"    from fpt_posting\n" +
					"    where jol_id_id=:jolId and deleted = false\n" +
					"    union all\n" +
					"    select post_date,coa_id_dt_id,amount,'DT',out_code_id\n" + 
					"    from fpt_posting\n" +
					"    where jol_id_id=:jolId and deleted = false) as acc\n" +
					"group by post_date,account,flag,out_code_id\n" + 
					"order by account,post_date,flag,out_code_id")
					.setParameter("jolId", pJol.getId())
					.getResultList();
			for(Object[] row: queryResult) {
				int vFlag = accountingCalcBalance(
						entityManager.getReference(FChartAccount.class, ((BigInteger) row[1]).longValue() ), //rec.account,
						((java.sql.Date) row[0]).toLocalDate(), //rec.post_date,
						(String) row[2], //rec.flag,
						(BigDecimal) row[4], //rec.amount,
						entityManager.getReference(CCcOrganizationUnit.class, ((BigInteger) row[3]).longValue() ), //rec.out_code,
						vJournalType);
				if(vFlag == 1) {
					logger.error("accountingAggregateJournals: 1 Bad balance");
					return 1;
				}
			}
			LoiPtJournalStatusRepository loiPtJournalStatusRepository = ((LoiPtJournalStatusRepository) getRepositories().getRepositoryFor(LoiPtJournalStatus.class).get());
			LoiPtJournalStatus loiPtJournalStatusP = loiPtJournalStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtJournalStatus.PT_JOURNAL_STATUS_P, pJol.getCompany(), false);
			LoiPtPostingCoaStatusRepository loiPtPostingCoaStatusRepository = ((LoiPtPostingCoaStatusRepository) getRepositories().getRepositoryFor(LoiPtPostingCoaStatus.class).get());
			LoiPtPostingCoaStatus loiPtPostingCoaStatusP = loiPtPostingCoaStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtPostingCoaStatus.PT_POSTING_COA_STATUS_P, pJol.getCompany(), false);

			pJol.setStatus(loiPtJournalStatusP);
			if(trySave(pJol, true) == null) {
				throw new ReportException("accountingAggregateJournals couldn't update FPtJournal ID "+pJol.getId());
			}
			for(FPtPosting posting: pJol.getFPtPostings()) {
				posting.setCoaStatus(loiPtPostingCoaStatusP);
				if(trySave(posting, true) == null) {
					throw new ReportException("accountingAggregateJournals couldn't update FPtPosting ID "+posting.getId());
				}
			}

			logger.trace("accountingAggregateJournals: 0 Success FPtJournal ID "+pJol.getId());
			return 0;
		}
		logger.error("accountingAggregateJournals: 1 FPtJournal status must be 'V'");
		return 1;
	}

	public Map<String, Object> createDefaultJournals(Long pBahId, String pIsCenerate){
		Map<String,Object> result = new HashMap<String,Object>();
		FPtBatch fPtBatch = entityManager.find(FPtBatch.class, pBahId);

		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QFPtBatch qfPtBatch = QFPtBatch.fPtBatch;
		QFCtInvDealType qfCtInvDealType = QFCtInvDealType.fCtInvDealType;
		QFCtBatchJteDefault qfCtBatchJteDefault = QFCtBatchJteDefault.fCtBatchJteDefault;

		if (pIsCenerate.equals("N")){
			logger.error("createDefaultJournals: -1 pIsCenerate = N");
			result.put("resultCode", "-1");
			return result;
		}

		List<FJournalType> resultList = queryFactory.select(qfCtBatchJteDefault.jteId)
				.from(qfPtBatch)
				.leftJoin(qfCtInvDealType).on(qfPtBatch.ideId.eq(qfCtInvDealType).and(qfCtInvDealType.deleted.eq(false)))
				.leftJoin(qfCtBatchJteDefault).on(qfPtBatch.outCode.eq(qfCtBatchJteDefault.outCode)
						.and(qfPtBatch.bteId.eq(qfCtBatchJteDefault.bteId))
						.and(qfCtBatchJteDefault.deleted.eq(false))
					)
				.where(qfCtBatchJteDefault.side.listOptionItemCode.eq(qfCtInvDealType.side.listOptionItemCode.coalesce(LoiBatchJteDefaultSide.BATCH_JTE_DEFAULT_SIDE_N))
						.and(qfPtBatch.id.eq(fPtBatch.getId()))
						.and(qfPtBatch.deleted.eq(false))
				)
				.fetch();

		if (resultList.isEmpty()){
			logger.error("createDefaultJournals: -2 Missing FJournalTypes");
			result.put("resultCode", "-2");
			return result;
		}

		FJournalType vJteId = resultList.get(0);

		if (vJteId == null){
			logger.error("createDefaultJournals: -3 Missing FJournalType");
			result.put("resultCode", "-3");
			return result;
		}

		LoiPtJournalStatusRepository loiPtJournalStatusRepository = ((LoiPtJournalStatusRepository) getRepositories().getRepositoryFor(LoiPtJournalStatus.class).get());
		LoiPtJournalStatus status = loiPtJournalStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtJournalStatus.PT_JOURNAL_STATUS_C, fPtBatch.getCompany(), false);

		FPtJournal fPtJournal = new FPtJournal(fPtBatch.getCreatedBy(), null, null, null, false, fPtBatch.getCompany(), fPtBatch, 
				fPtBatch.getPostDate(), fPtBatch.getOutCode(), status, null, null, null, fPtBatch.getDescr(), fPtBatch.getCuyCode(), 
				null, fPtBatch.getCuyUnit(), vJteId);

		result.put("FPtJournal", trySave(fPtJournal, true));
		result.put("resultCode", "0");
		return result;
	}
	
//CREATE FUNCTION accounting.bah_enforce_rules_ui(p_bah_id integer, p_id integer, p_flag character) RETURNS character varying
//    LANGUAGE plpgsql
//    AS $$
//DECLARE
	public Map<String, Object> bahEnforceRulesUi(Long pBahId, Long pId, int pFlag) {
// v_result integer;
		Map<String,Object> resultMap = new HashMap<String,Object>();
//BEGIN
//    v_result := bah_enforce_rules (p_bah_id, p_id , p_flag);
		Long result = bahEnforceRules(pBahId, pId, pFlag);
//    if v_result = 0 then
		if(result == 0) {
//       return null;
			resultMap.put("result", "null");
		} else if(result == -300) {
//    elsif v_result = -300 then
//       return 'Не може да се обработи непотвърден запис!';
			resultMap.put("result", "Не може да се обработи непотвърден запис!");
//   else
		} else {
//       return 'Грешка !';
			resultMap.put("result", "Грешка! "+result);
//   end if;
		}
//END;
		return resultMap;
	}
	
	private Long bahEnforceRules(Long pBahId, Long pId, int pFlag){
//CREATE FUNCTION accounting.bah_enforce_rules(p_bah_id integer, p_id integer, p_flag character) RETURNS integer
//    LANGUAGE plpgsql
//    AS $$
//DECLARE
// v_result integer;
// v_result1 integer;
// v_rec record;
// v_status varchar;
//BEGIN
//  v_result := 0;
		Long vResult = 0L;
//  if p_flag=1 then
		if(pFlag == 1) {
//     for v_rec in (select id from pt_batch_cc_details where bah_id=p_bah_id and status='A') loop
			FPtBatch fPtBatch = entityManager.getReference(FPtBatch.class, pBahId);
			LoiPtBatchCcDetailStatusRepository loiPtBatchCcDetailStatusRepository = ((LoiPtBatchCcDetailStatusRepository) getRepositories().getRepositoryFor(LoiPtBatchCcDetailStatus.class).get());
			LoiPtBatchCcDetailStatus loiPtBatchCcDetailStatusA = loiPtBatchCcDetailStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtBatchCcDetailStatus.PT_BATCH_CC_DETAIL_STATUS_A , fPtBatch.getCompany(), false);
			
			FPtBatchCcDetailRepository fPtBatchCcDetailRepository = ((FPtBatchCcDetailRepository) getRepositories().getRepositoryFor(FPtBatchCcDetail.class).get());
			List<FPtBatchCcDetail> vRecList = fPtBatchCcDetailRepository.findByBahIdAndStatusAndCompanyAndDeleted(fPtBatch, loiPtBatchCcDetailStatusA, fPtBatch.getCompany(), false);
			for(FPtBatchCcDetail vRec: vRecList) {
//         v_result1 := bah_enforce_rules_for_one (v_rec.id);
				Long vResult1 = bahEnforceRulesForOne(vRec.getId());
//         update  pt_batch_cc_details
//         set result = v_result1
//         where id=v_rec.id;
				vRec.setResult(vResult1);
//         if v_result1 = 0 then
//         update  pt_batch_cc_details
//         set status = 'F'
//         where id=v_rec.id;
//         end if;
				if(vResult1 == 0) {
					LoiPtBatchCcDetailStatus loiPtBatchCcDetailStatusF = loiPtBatchCcDetailStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtBatchCcDetailStatus.PT_BATCH_CC_DETAIL_STATUS_F , fPtBatch.getCompany(), false);
					vRec.setStatus(loiPtBatchCcDetailStatusF);
				}
				if(trySave(vRec, true) == null) {
					throw new ReportException("bahEnforceRules couldn't update FPtBatchCcDetail ID "+vRec.getId());
				}
//         v_result := v_result1;
				vResult = vResult1;
//     end loop;
			}
//  elsif p_flag=2 then
		} else if(pFlag == 2) {
//     select status
//     into v_status
//     from pt_batch_cc_details
//     where id=p_id;
			FPtBatchCcDetail fPtBatchCcDetail = entityManager.getReference(FPtBatchCcDetail.class, pId);
//     if v_status != 'A' then
			if(fPtBatchCcDetail.getStatus() == null || fPtBatchCcDetail.getStatus().getListOptionItemCode() != LoiPtBatchCcDetailStatus.PT_BATCH_CC_DETAIL_STATUS_A) {
//         v_result := -300;
				logger.error("bahEnforceRules: -300 FPtBatchCcDetail status must be A");
				vResult = -300L;
//     else
			} else {
//         v_result := bah_enforce_rules_for_one (p_id);
				vResult = bahEnforceRulesForOne(pId);
//         update  pt_batch_cc_details
//         set result = v_result
//         where id=p_id;
				fPtBatchCcDetail.setResult(vResult);
				if(trySave(fPtBatchCcDetail, true) == null) {
					throw new ReportException("bahEnforceRules couldn't update FPtBatchCcDetail ID "+fPtBatchCcDetail.getId());
				}
//     end if;
			}
//  end if;
		}
//  return v_result;
		return vResult;
//END;
	}

	private Long bahEnforceRulesForOne(Long pId){
	//	CREATE FUNCTION accounting.bah_enforce_rules_for_one(p_id integer) RETURNS integer
	//	    LANGUAGE plpgsql
	//	    AS $$
	//	DECLARE
	//	 v_result integer;
	//	 v_rec pt_batch_cc_details%rowtype;
	//	 v_rule_journal varchar;
	//	 v_expr record;
	//	 v_journal integer;
	//	BEGIN
		Long vResult = null;
	//	    select *
	//	    into v_rec
	//	    from pt_batch_cc_details bcl
	//	    where bcl.id=p_id;
		FPtBatchCcDetail vRec = entityManager.getReference(FPtBatchCcDetail.class, pId);
	//	    if bridge.check_rules(v_rec.out_code, v_rec.tte_id, v_rec.dependence_type,v_rec.dependence_id,v_rec.post_date) <> 0 then
		if( bridgeCheckRules(vRec.getOutCode(), vRec.getTteId(), vRec.getDependenceType(), vRec.getDependenceId(), vRec.getPostDate()) != 0 ) {
	//	       v_result := 2;
			logger.error("bahEnforceRulesForOne: 2 bridgeCheckRules fail");
			vResult = 2L;
	//	    else
		} else {
	//	        if bridge.check_required_cost_centers (null,null,'3',v_rec)<>0 then
			if( bridgeCheckRequiredCostCenters(null, null, 3, vRec) != 0 ) {
	//	             v_result := 100;
				logger.error("bahEnforceRulesForOne: 100 bridgeCheckRequiredCostCenters fail");
				vResult = 100L;
	//	        else
			} else {
	//	            begin
	//	            v_rule_journal := '';
				String vRuleJournal = "";
	//	            for v_expr in (select rue.expression, btr.rue_id, btr.jte_id
	//	                             from ct_rules rue, ct_batch_type_rules btr, register.ct_transition_types tte
	//	                            where tte.id=v_rec.tte_id
	//	                              and btr.tte_id=tte.id
	//	                              and btr.out_code=v_rec.out_code
	//	                              and btr.rue_id=rue.id
	//	                              and coalesce(btr.dependence_type,'')=coalesce(v_rec.dependence_type,'')
	//	                              and coalesce(btr.dependence_id,-1)=coalesce(v_rec.dependence_id,-1)
	//	                              and v_rec.post_date between btr.active_from_date and btr.active_to_date
	//	                            order by order_num) loop
				QFCtBatchTypeRule qFCtBatchTypeRule = QFCtBatchTypeRule.fCtBatchTypeRule;
				QFCtRule qFCtRule = QFCtRule.fCtRule;
				List<Tuple> vExprList = getBatchTypeRules(vRec.getTteId(), vRec.getOutCode(), vRec.getDependenceType(), vRec.getDependenceId(), vRec.getPostDate());
				for(Tuple vExpr: vExprList) {
	//	                      if ( v_expr.rue_id::varchar||'-'||v_expr.jte_id::varchar != v_rule_journal ) then
					if( !vRuleJournal.equals(vExpr.get(qFCtBatchTypeRule.rueId).getId().toString()+"-"+vExpr.get(qFCtBatchTypeRule.jteId).getId()) ) {
	//	                         v_journal := bridge.create_journal( v_rec.bah_id, v_expr.jte_id, null,null,null,'4',v_rec);
						FPtJournal vJournal = bridgeCreateJournal(vRec.getBahId(), vExpr.get(qFCtBatchTypeRule.jteId), null, null, null, 4, vRec);
	//	                         v_rule_journal := v_expr.rue_id::varchar||'-'||v_expr.jte_id::varchar;
						vRuleJournal = vExpr.get(qFCtBatchTypeRule.rueId).getId().toString()+"-"+vExpr.get(qFCtBatchTypeRule.jteId).getId();
	//	                         if v_journal > 0 then
						if(vJournal != null) {
	//	                             if v_expr.expression = 'new_posting' then
							if( vExpr.get(qFCtRule.expression).equals("new_posting") ) {
	//	                                v_result := bridge.new_posting(v_rec.bah_id, v_journal,v_expr.jte_id, v_expr.rue_id,null,null,'4',v_rec);
								vResult = bridgeNewPosting(vRec.getBahId(), vJournal, vExpr.get(qFCtBatchTypeRule.jteId), vExpr.get(qFCtBatchTypeRule.rueId), null, null, 4, vRec);
	//	                             elsif v_expr.expression = 'payment_posting' then
							} else if( vExpr.get(qFCtRule.expression).equals("payment_posting") ) {
	//	                                v_result := bridge.payment_posting(v_rec.bah_id, v_journal,v_expr.jte_id, v_expr.rue_id,null,null,'4',v_rec);
								vResult = bridgePaymentPosting(vRec.getBahId(), vJournal, vExpr.get(qFCtBatchTypeRule.jteId), vExpr.get(qFCtBatchTypeRule.rueId), null, null, 4, vRec);
	//	                             else
							} else {
	//	                                --raise exception 'Unknown processing function: %1', v_expr.expression;
	//	                                v_result := 2;
								logger.error("bahEnforceRulesForOne: 2 Unknown processing function"+vExpr.get(qFCtRule.expression));
								vResult = 2L;
	//	                             end if;
							}
	//	/*
	//	                             if v_result != 0 then
	//	                                exit;
	//	                             end if; */
	//	                        else
						} else {
	//	                              v_result := 1;
							logger.error("bahEnforceRulesForOne: 1 bridgeCreateJournal fail");
							vResult = 1L;
	//	                        end if;
						}
	//	                   end if;
					}
	//	            end loop;
				}
	//	/*
	//	        exception
	//	            when others then
	//	               v_result := 1;*/
	//	         end;
	//	        end if;
			}
	//	    end if;
		}
	//	    return v_result;
		return vResult;
	//	END;
	}
	
	private List<Tuple> getBatchTypeRules(FCtTransitionType tteId, CCcOrganizationUnit outCode, LoiBatchTypeRuleDependenceType dependenceType, Long dependenceId, LocalDate postDate) {
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QFCtRule qFCtRule = QFCtRule.fCtRule;
		QFCtBatchTypeRule qFCtBatchTypeRule = QFCtBatchTypeRule.fCtBatchTypeRule;
		QFCtTransitionType qFCtTransitionType = QFCtTransitionType.fCtTransitionType;
		return queryFactory.select(qFCtRule.expression, qFCtBatchTypeRule.rueId, qFCtBatchTypeRule.jteId)
				.from(qFCtRule)
				.innerJoin(qFCtBatchTypeRule).on(qFCtBatchTypeRule.rueId.eq(qFCtRule))
				.innerJoin(qFCtTransitionType).on(qFCtTransitionType.eq(qFCtBatchTypeRule.tteId))
				.where(qFCtTransitionType.id.eq(tteId.getId())
						.and(qFCtBatchTypeRule.outCode.id.eq(outCode.getId()))
						.and(qFCtRule.deleted.eq(false))
						.and(qFCtBatchTypeRule.deleted.eq(false))
						.and(qFCtTransitionType.deleted.eq(false))
						.and(dependenceType == null ?
								qFCtBatchTypeRule.dependenceType.isNull()
								: qFCtBatchTypeRule.dependenceType.listOptionItemCode.eq(dependenceType.getListOptionItemCode()))
						.and(qFCtBatchTypeRule.dependenceId.coalesce(-1L).eq(dependenceId == null ? -1L : dependenceId))
						.and(qFCtBatchTypeRule.activeFromDate.loe(postDate))
						.and(qFCtBatchTypeRule.activeToDate.goe(postDate))
					)
				.orderBy(qFCtBatchTypeRule.orderNum.asc())
				.fetch();
	}

	//TODO I think we don't need this and it is not checked/tested
	private void hpInsHpCcBalances(Date pStartDate, String pPeriod, Long pCcId, String pCcName, String pSide, BigDecimal pAmount, FChartAccount pCoaId, CCcOrganizationUnit pOutCode,String pJournalType){

		BigDecimal ccEbkCtAmount = null;
		BigDecimal ccEbkDtAmount = null;
		BigDecimal ccFunCtAmount = null;
		BigDecimal ccFunDtAmount = null;
		BigDecimal ccPrmCtAmount = null;
		BigDecimal ccPrmDtAmount = null;
		BigDecimal ccFieCtAmount = null;
		BigDecimal ccFieDtAmount = null;
		BigDecimal ccParCtAmount = null;
		BigDecimal ccParDtAmount = null;
		BigDecimal ccGteCtAmount = null;
		BigDecimal ccGteDtAmount = null;
		BigDecimal ccCotCtAmount = null;
		BigDecimal ccCotDtAmount = null;
		BigDecimal ccOutCtAmount = null;
		BigDecimal ccOutDtAmount = null;
		BigDecimal ccRe1CtAmount = null;
		BigDecimal ccRe1DtAmount = null;
		BigDecimal ccRe2CtAmount = null;
		BigDecimal ccRe2DtAmount = null;
		FCcEbk ccEbkId = null;
		FCcFunction ccFunId = null;
		FCcFinsource ccFieId = null;
		FCcProgram ccPrmId = null;
		CCcPartner ccParId = null;
		CCcGoodsType ccGteId = null;
		FCcContract ccCotId = null;
		CCcOrganizationUnit ccOutId = null;
		FCcReserve1 ccRe1Id = null;
		FCcReserve2 ccRe2Id = null;

		FHpCcBalance fHpCcBalanceSec = null;

		Date dateTruncPStartDateMonth = DateUtils.truncate(pStartDate, Calendar.YEAR);
		Calendar calForPStartDate = Calendar.getInstance();
		calForPStartDate.setTime(pStartDate);

		Calendar cal = Calendar.getInstance();
		cal.set(calForPStartDate.get(Calendar.YEAR), calForPStartDate.get(Calendar.MONTH), 31); //TODO check if it is made for the months that are up to 31 or it is required to be the last day of the month

		String subStringPPeriod = pPeriod.substring(4, 6); //substr(p_period,5,2)

		Date newLastModifiedDate = subStringPPeriod.equals("00") ? dateTruncPStartDateMonth : (subStringPPeriod.equals("13") ? cal.getTime() : pStartDate);


		if (pJournalType.equals("S") && subStringPPeriod.equals("00") || pJournalType.equals("E") && subStringPPeriod.equals("13") || !pJournalType.equals("E") && !subStringPPeriod.equals("00") || !pJournalType.contains("E") && !subStringPPeriod.equals("00")){

			ccEbkCtAmount = pCcName.equals(EBK) && pSide.equals(CREDIT) ? pAmount : null;
			ccEbkDtAmount = pCcName.equals(EBK) && pSide.equals(DEBIT) ? pAmount : null;
			ccFunCtAmount = pCcName.equals(FUN) && pSide.equals(CREDIT) ? pAmount : null;
			ccFunDtAmount = pCcName.equals(FUN) && pSide.equals(DEBIT) ? pAmount : null;
			ccPrmCtAmount = pCcName.equals(PRM) && pSide.equals(CREDIT) ? pAmount : null;
			ccPrmDtAmount = pCcName.equals(PRM) && pSide.equals(DEBIT) ? pAmount : null;
			ccFieCtAmount = pCcName.equals(FIE) && pSide.equals(CREDIT) ? pAmount : null;
			ccFieDtAmount = pCcName.equals(FIE) && pSide.equals(DEBIT) ? pAmount : null;
			ccParCtAmount = pCcName.equals(PAR) && pSide.equals(CREDIT) ? pAmount : null;
			ccParDtAmount = pCcName.equals(PAR) && pSide.equals(DEBIT) ? pAmount : null;
			ccGteCtAmount = pCcName.equals(GTE) && pSide.equals(CREDIT) ? pAmount : null;
			ccGteDtAmount = pCcName.equals(GTE) && pSide.equals(DEBIT) ? pAmount : null;
			ccCotCtAmount = pCcName.equals(COT) && pSide.equals(CREDIT) ? pAmount : null;
			ccCotDtAmount = pCcName.equals(COT) && pSide.equals(DEBIT) ? pAmount : null;
			ccOutCtAmount = pCcName.equals(OUT) && pSide.equals(CREDIT) ? pAmount : null;
			ccOutDtAmount = pCcName.equals(OUT) && pSide.equals(DEBIT) ? pAmount : null;
			ccRe1CtAmount = pCcName.equals(RE1) && pSide.equals(CREDIT) ? pAmount : null;
			ccRe1DtAmount = pCcName.equals(RE1) && pSide.equals(DEBIT) ? pAmount : null;
			ccRe2CtAmount = pCcName.equals(RE2) && pSide.equals(CREDIT) ? pAmount : null;
			ccRe2DtAmount = pCcName.equals(RE2) && pSide.equals(DEBIT) ? pAmount : null;

			Long ebk = pCcName.equals(EBK) ? pCcId : null;
			Long fun = pCcName.equals(FUN) ? pCcId : null;
			Long prm = pCcName.equals(PRM) ? pCcId : null;
			Long fie = pCcName.equals(FIE) ? pCcId : null;
			Long par = pCcName.equals(PAR) ? pCcId : null;
			Long gte = pCcName.equals(GTE) ? pCcId : null;
			Long cot = pCcName.equals(COT) ? pCcId : null;
			Long out = pCcName.equals(OUT) ? pCcId : null;
			Long re1 = pCcName.equals(RE1) ? pCcId : null;
			Long re2 = pCcName.equals(RE2) ? pCcId : null;

			ccEbkId = entityManager.getReference(FCcEbk.class, ebk);
			ccFunId = entityManager.getReference(FCcFunction.class, fun);
			ccPrmId = entityManager.getReference(FCcProgram.class, prm);
			ccFieId = entityManager.getReference(FCcFinsource.class, fie);
			ccParId = entityManager.getReference(CCcPartner.class, par);
			ccGteId = entityManager.getReference(CCcGoodsType.class, gte);
			ccCotId = entityManager.getReference(FCcContract.class, cot);
			ccOutId = entityManager.getReference(CCcOrganizationUnit.class, out);
			ccRe1Id = entityManager.getReference(FCcReserve1.class, re1);
			ccRe2Id = entityManager.getReference(FCcReserve2.class, re2);


		}

		fHpCcBalanceSec = new FHpCcBalance(null, null,null,newLastModifiedDate, false,null, pOutCode,ccEbkCtAmount,ccEbkDtAmount,ccFunCtAmount,ccFunDtAmount,ccPrmCtAmount,ccPrmDtAmount,ccFieCtAmount,ccFieDtAmount,ccParCtAmount,ccParDtAmount,ccGteCtAmount,ccGteDtAmount,ccCotCtAmount,ccCotDtAmount,ccOutCtAmount,ccOutDtAmount,ccRe1CtAmount,ccRe1DtAmount,ccRe2CtAmount,ccRe2DtAmount,pPeriod,pCoaId,ccEbkId,ccFunId,ccFieId,ccPrmId,ccParId,ccGteId,ccCotId,ccOutId,ccRe1Id,ccRe2Id);
		if(trySave(fHpCcBalanceSec, true) == null) {
			throw new ReportException("hpInsHpCcBalances couldn't create FHpCcBalance");
		}

	}

	private Integer stornoJournal(FPtJournal pJolId, Long pStornoType) {
		/*
        		v_id integer;
         v_status varchar(20);
         v_default char(1);
         v_out_code varchar(15);
         v_storno_jol_id integer;
         v_res integer;
         v_storno_type char(1);
         v_journal_no integer;
         --v_main_out_code varchar(15);
		 */
		Session session = entityManager.unwrap(Session.class);
		HibernateQueryFactory queryFactory = new HibernateQueryFactory(session);
		QFCtStornoType qfCtStornoType = QFCtStornoType.fCtStornoType;
		QCCcOrganizationUnit qcCcOrganizationUnit = QCCcOrganizationUnit.cCcOrganizationUnit;
		LoiPtJournalStatus vStatus = pJolId.getStatus();
		CCcOrganizationUnit vOutCode = pJolId.getOutCode();
		FPtJournal vStornoJolId = pJolId.getStornoJolId();
		Integer vJournalNo = pJolId.getJournalNo();
		LoiCtStornoTypeType vStornoType;
		
		if(vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_P && vStornoJolId == null) {
			if(pStornoType == null) {
				LoiCtStornoTypeType vDefault = queryFactory.select(qfCtStornoType.type)
						.from(qfCtStornoType)
						.where(qfCtStornoType.outCode.id.eq(vOutCode.getId())
								.and(qfCtStornoType.isDefault.isTrue())
								.and(qfCtStornoType.deleted.eq(false)))
						.fetchFirst();
				vStornoType = vDefault;

				if(vStornoType == null) {
					/*
			    		(select ste.type
                              from ct_storno_types ste,register.cc_organization_units out1
			      where out1.code=v_out_code
			      and ste.out_code=out1.main_out_code
			      and ste.is_default = 'Y')
					 */
					LoiCtStornoTypeType vDefaultSec = queryFactory.select(qfCtStornoType.type)
							.from(qfCtStornoType)
							.innerJoin(qcCcOrganizationUnit).on(qfCtStornoType.outCode.code.eq(qcCcOrganizationUnit.mainOutCode))
							.where(qcCcOrganizationUnit.eq(vOutCode)
									.and(qcCcOrganizationUnit.deleted.eq(false))
									.and(qfCtStornoType.isDefault.isTrue())
									.and(qfCtStornoType.deleted.eq(false)))
							.fetchFirst();
					vStornoType = vDefaultSec;

					if(vStornoType == null) {
					/*
			            		(select min(ste.type)
                                   from ct_storno_types ste
			           where ste.out_code=v_out_code
			           and ste.is_default = 'N'),
					 */
						LoiCtStornoTypeType vDefaultThird = queryFactory.select(qfCtStornoType.type)
								.from(qfCtStornoType)
								.where(qfCtStornoType.outCode.id.eq(vOutCode.getId())
										.and(qfCtStornoType.deleted.eq(false))
										.and(qfCtStornoType.isDefault.isFalse()))
								.orderBy(qfCtStornoType.type.listOptionItemName.asc())
								.fetchFirst();
						vStornoType = vDefaultThird;
					} else {
						LoiCtStornoTypeType vDefaultFourth = queryFactory.select(qfCtStornoType.type)
								.from(qfCtStornoType)
								.innerJoin(qcCcOrganizationUnit).on(qfCtStornoType.outCode.code.eq(qcCcOrganizationUnit.mainOutCode))
								.where(qcCcOrganizationUnit.code.eq(vOutCode.getCode())
										.and(qcCcOrganizationUnit.deleted.isFalse())
										.and(qfCtStornoType.deleted.eq(false))
										.and(qfCtStornoType.isDefault.isFalse()))
								.orderBy(qfCtStornoType.type.listOptionItemName.asc())
								.fetchFirst();
						vStornoType = vDefaultFourth;
					}
				}
			} else {
				vStornoType = queryFactory.select(qfCtStornoType.type)
						.from(qfCtStornoType)
						.where(qfCtStornoType.type.listOptionItemCode.eq(pStornoType)
								.and(qfCtStornoType.deleted.eq(false))
								.and(qfCtStornoType.outCode.id.eq(vOutCode.getId())))
						.fetchFirst();
			}

			if (vStornoType == null){
				logger.error("stornoJournal: 1 Coudn't find LoiCtStornoTypeType");
				return 1;
			}

			LoiPtJournalStatusRepository loiPtJournalStatusRepository = ((LoiPtJournalStatusRepository) getRepositories().getRepositoryFor(LoiPtJournalStatus.class).get());
			LoiPtJournalStatus loiPtJournalStatusC = loiPtJournalStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtJournalStatus.PT_JOURNAL_STATUS_C, pJolId.getCompany(), false);

			vStornoJolId = new FPtJournal();
			vStornoJolId.setBahId(pJolId.getBahId());
			vStornoJolId.setPostDate(pJolId.getPostDate());
			vStornoJolId.setOutCode(pJolId.getOutCode());
			vStornoJolId.setJteId(pJolId.getJteId());
			vStornoJolId.setStatus(loiPtJournalStatusC);
			vStornoJolId.setStornoJolId(pJolId);
			vStornoJolId.setDescr("Сторнираща на счетоводна статия номер " + vJournalNo.toString());
			vStornoJolId.setCuyCode(pJolId.getCuyCode());
			vStornoJolId.setCuyRate(pJolId.getCuyRate());
			vStornoJolId.setCuyUnit(pJolId.getCuyUnit());
			if(trySave(vStornoJolId, true) == null) {
				throw new ReportException("stornoJournal couldn't create FPtJournal");
			}

			if(vStornoType.getListOptionItemCode() == LoiCtStornoTypeType.CT_STORNO_TYPE_TYPE_N) {
				for(FPtPosting pogSeq : pJolId.getFPtPostings()) {
					FPtPosting newPogSeq = new FPtPosting();
					newPogSeq.setJolId(vStornoJolId);
					newPogSeq.setPostDate(pogSeq.getPostDate());
					newPogSeq.setOutCode(pogSeq.getOutCode());
					newPogSeq.setCoaIdCt(pogSeq.getCoaIdDt()); //Ct to Dt because we make storno reversal!
					newPogSeq.setCoaIdDt(pogSeq.getCoaIdCt());
					newPogSeq.setAmount(pogSeq.getAmount());
					newPogSeq.setAmountCurrency(pogSeq.getAmountCurrency());
					newPogSeq.setCcEbkId(pogSeq.getCcEbkId());
					newPogSeq.setCcFunId(pogSeq.getCcFunId());
					newPogSeq.setCcPrmId(pogSeq.getCcPrmId());
					newPogSeq.setCcFieId(pogSeq.getCcFieId());
					newPogSeq.setCcParId(pogSeq.getCcParId());
					newPogSeq.setCcGteId(pogSeq.getCcGteId());
					newPogSeq.setCcCotId(pogSeq.getCcCotId());
					newPogSeq.setCcOutId(pogSeq.getCcOutId());
					newPogSeq.setCcRe1Id(pogSeq.getCcRe1Id());
					newPogSeq.setCcRe2Id(pogSeq.getCcRe2Id());
					newPogSeq.setDescr("Сторнираща на счетоводна статия номер " + vJournalNo.toString());
					if(trySave(newPogSeq, true) == null) {
						throw new ReportException("stornoJournal couldn't create FPtPosting");
					}
				}
			} else if(vStornoType.getListOptionItemCode() == LoiCtStornoTypeType.CT_STORNO_TYPE_TYPE_R) {
				for(FPtPosting pogSeq : pJolId.getFPtPostings()) {
					FPtPosting newPogSeq = new FPtPosting();
					newPogSeq.setJolId(vStornoJolId);
					newPogSeq.setPostDate(pogSeq.getPostDate());
					newPogSeq.setOutCode(pogSeq.getOutCode());
					newPogSeq.setCoaIdCt(pogSeq.getCoaIdCt());
					newPogSeq.setCoaIdDt(pogSeq.getCoaIdDt());
					newPogSeq.setAmount(pogSeq.getAmount().negate());
					newPogSeq.setAmountCurrency(pogSeq.getAmountCurrency().negate());
					newPogSeq.setCcEbkId(pogSeq.getCcEbkId());
					newPogSeq.setCcFunId(pogSeq.getCcFunId());
					newPogSeq.setCcPrmId(pogSeq.getCcPrmId());
					newPogSeq.setCcFieId(pogSeq.getCcFieId());
					newPogSeq.setCcParId(pogSeq.getCcParId());
					newPogSeq.setCcGteId(pogSeq.getCcGteId());
					newPogSeq.setCcCotId(pogSeq.getCcCotId());
					newPogSeq.setCcOutId(pogSeq.getCcOutId());
					newPogSeq.setCcRe1Id(pogSeq.getCcRe1Id());
					newPogSeq.setCcRe2Id(pogSeq.getCcRe2Id());
					newPogSeq.setDescr("Сторнираща на счетоводна статия номер " + vJournalNo.toString());
					if(trySave(newPogSeq, true) == null) {
						throw new ReportException("stornoJournal couldn't create FPtPosting");
					}
				}
			}

			LoiPtJournalStatus loiPtJournalStatusS = loiPtJournalStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtJournalStatus.PT_JOURNAL_STATUS_S, pJolId.getCompany(), false);

			pJolId.setStatus(loiPtJournalStatusS);
			pJolId.setStornoJolId(vStornoJolId);
			if(trySave(pJolId, true) == null) {
				throw new ReportException("stornoJournal couldn't update FPtJournal ID "+pJolId.getId());
			}

			LoiPtJournalStatus loiPtJournalStatusV = loiPtJournalStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiPtJournalStatus.PT_JOURNAL_STATUS_V, pJolId.getCompany(), false);

			vStornoJolId.setStatus(loiPtJournalStatusV);
			if(trySave(vStornoJolId, true) == null) {
				throw new ReportException("stornoJournal couldn't update FPtJournal ID "+vStornoJolId.getId());
			}

			Integer vRes = accountingAggregateJournalsUi(vStornoJolId.getId());

			if (vRes == 1){
				logger.error("stornoJournal: 1 accountingAggregateJournalsUi fail");
				return 1;
			}
			return 0;
		}
		logger.error("stornoJournal: 1 Status must be P and StornoJolId must be null");
		return 1;
	}

	public Map<String, Object> stornoJournalUi(Long pJolId, Long pStornoType){
		Map<String,Object> resultMap = new HashMap<String,Object>();

		FPtJournal fPtJournal = entityManager.getReference(FPtJournal.class, pJolId);

		LoiPtJournalStatus vStatus = fPtJournal.getStatus();
		FPtJournal vStornoJolId = fPtJournal.getStornoJolId();
		Integer vJournalNo = fPtJournal.getJournalNo();
		String vDescr = fPtJournal.getStatus().getListOptionItemName();

		if(vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_P && vStornoJolId == null) {
			Integer vRes = stornoJournal(fPtJournal, pStornoType);
			if(vRes == 0) {
				resultMap.put("result", "Счетоводна статия номер "+ vJournalNo.toString() + " беше сторнирана успешно!");
				return resultMap;
			} else {
				resultMap.put("result", "Грешка при осчетоводяване!");
				return resultMap;
			}
		} else if(vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_C 
				|| vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_V 
				|| vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_R) {
			resultMap.put("result", "Нямате право да сторнирате счетоводна статия, която е "+ vDescr + "!");
			return resultMap;
		} else if (vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_S) {
			resultMap.put("result", "Не може да сторнирате счетоводна статия " + vJournalNo.toString() +" , тя вече е била сторнирана!");
			return resultMap;
		} else if (vStatus.getListOptionItemCode() == LoiPtJournalStatus.PT_JOURNAL_STATUS_P && vStornoJolId != null) {
			resultMap.put("result", "Не може да сторнирате счетоводна статия " + vJournalNo.toString() +" , тя е сторнираща!");
			return resultMap;
		} else {
			resultMap.put("result", "Грешка в статуса на счетоводната статия!");
			return resultMap;
		}
	}


	public List<Map<String, Object>> coaBalanceAllByMonth(String pSOutCode, String pSDateFrom, String pSDateTo, String pSConsolidated, Long pSCoaType){

		SecUser user = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			user = ((SantaUser) authentication.getPrincipal()).getSecUser();
		}

		CCcOrganizationUnitRepository cCcOrganizationUnitRepository = ((CCcOrganizationUnitRepository) getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get());
		CCcOrganizationUnit pSOutCodeId = cCcOrganizationUnitRepository.findFirstByCodeAndCompanyAndDeleted(pSOutCode, user.getCompany(), false);

		List<Object[]> queryResult = entityManager.createNativeQuery("select coa.code coa_code, coa.name as coa_name, \n" +
						"\t(select coa1.code from fchart_account coa1 where coa1.id=coa.coa_id_up_id and coa1.deleted = false) as coa_parent_code,\n" +
						"       (case when (f.ct_amount - f.dt_amount) >= 0 then f.ct_amount - f.dt_amount \n" +
						"             else 0.0 \n" +
						"             end) as f_ct_amount,\n" +
						"       (case when (f.ct_amount - f.dt_amount) < 0 then f.dt_amount - f.ct_amount \n" +
						"             else 0.0 \n" +
						"             end) as f_dt_amount,\n" +
						"       s.ct_amount - f.ct_amount as o_ct_amount, s.dt_amount - f.dt_amount as o_dt_amount,\n" +
						"       case when (s.ct_amount - s.dt_amount) > 0 then s.ct_amount - s.dt_amount else 0.0 end as s_ct_amount,\n" +
						"       case when (s.ct_amount - s.dt_amount) < 0 then s.dt_amount - s.ct_amount else 0.0 end as s_dt_amount,\n" +
						"       out.name as out_name,\n" +
						"       coa.is_synthetic_id,\n" +
						"       'Период от дата: '||to_char(to_date('01'||:pSDateFrom,'ddyyyymm'),'dd.mm.yyyy')||\n" +
						"       (case when substr(:pSDateTo,5,2)='13' then ' до Годишно приключване'\n" +
						"             else ' до дата: '||to_char((to_date('01'||:pSDateTo,'ddyyyymm')+ interval '1 month' - interval '1 day')::::date ,'dd.mm.yyyy')\n" +
						"             end) as period,\n" +
						"        (case when (:pSCoaType is null or :pSCoaType =0) then null\n" +
						"              else 'За '||(select list_option_item_name from list_option_item where list_option_item.id= coa.is_synthetic_id and deleted = false) end) as par_sin    \n" +
						"from (select cab.coa_id_id, sum(cab.ct_amount) as ct_amount, sum(cab.dt_amount) as dt_amount\n" +
						"       from fpt_coa_balance cab\n" +
						"       where cab.period::::integer+1 = (:pSDateFrom)::::integer\n" +
						"       and cab.deleted = false\n" +
						"\t     and (case when :pSConsolidated = 'N' then out_code_id = :pSOutCodeId else out_code_id in (/*select out1.code from register.cc_organization_units out1\n" +
						"\t                                                                                                         where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE*/:pSOutCodeId/*}))*/) end)\n" +   //TODO hierarchically
						"       group by cab.coa_id_id) f,\n" +
						"\t     (select cab.coa_id_id, sum(cab.ct_amount) as ct_amount, sum(cab.dt_amount) as dt_amount\n" +
						"       from fpt_coa_balance cab\n" +
						"       where cab.period = :pSDateTo\n" +
						"       and cab.deleted = false\n" +
						"\t     and (case when :pSConsolidated = 'N' then out_code_id = :pSOutCodeId else out_code_id in (/*select out1.code from register.cc_organization_units out1\n" +
						"\t                                                                                                         where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE*/:pSOutCodeId/*}))*/) end)\n" +   //TODO hierarchically
						"       group by cab.coa_id_id) s,\n" +
						"        fchart_account coa,\n" +
						"        ccc_organization_unit out,\n" +
						"        list_option_item \n" +
						"       where f.coa_id_id = s.coa_id_id\n" +
						"		and coa.deleted = false" +
						"		and out.deleted = false" +
						"		and list_option_item.deleted = false" +
						"		and list_option_item.id= coa.is_synthetic_id" +
						"       and f.coa_id_id = coa.id\n" +
						"       and out.code = :pSOutCode\n" +
						"       and (case when (:pSCoaType is null or :pSCoaType =0) then list_option_item.list_option_item_code = "+LoiTypeOfFinancialAccount.COA_TYPE_SYNTHETIC_N+"\n" +
						"                 else list_option_item.list_option_item_code = :pSCoaType end)\n" +
						"       and not (f.ct_amount=0 and f.dt_amount=0 and s.ct_amount=0 and s.dt_amount=0 )\n" +
						"       order by coa.code")
				.setParameter("pSDateTo", pSDateTo)
				.setParameter("pSDateFrom", pSDateFrom)
				.setParameter("pSCoaType", pSCoaType)
				.setParameter("pSConsolidated", pSConsolidated)
				.setParameter("pSOutCode", pSOutCode)
				.setParameter("pSOutCodeId", pSOutCodeId)
				.getResultList();

		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {

			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("coa_code", queryRow[0]);
			resultRow.put("coa_name", queryRow[1]);
			resultRow.put("coa_parent_code", queryRow[2]);
			resultRow.put("f_ct_amount", queryRow[3]);
			resultRow.put("f_dt_amount", queryRow[4]);
			resultRow.put("o_ct_amount", queryRow[5]);
			resultRow.put("o_dt_amount", queryRow[6]);
			resultRow.put("s_ct_amount", queryRow[7]);
			resultRow.put("s_dt_amount", queryRow[8]);
			resultRow.put("out_name", queryRow[9]);
			resultRow.put("is_synthetic_id", queryRow[10]);
			resultRow.put("period", queryRow[11]);
			resultRow.put("par_sin", queryRow[12]);
			result.add(resultRow);
		}
		return result;
	}


	public List<Map<String, Object>> parBalanceByMonths(String pSOutCode, String pSDateFrom, String pSDateTo, Integer pICoaId, String pSConsolidated, Integer pIParId){

		SecUser user = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			user = ((SantaUser) authentication.getPrincipal()).getSecUser();
		}

		CCcOrganizationUnitRepository cCcOrganizationUnitRepository = ((CCcOrganizationUnitRepository) getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get());
		CCcOrganizationUnit pSOutCodeId = cCcOrganizationUnitRepository.findFirstByCodeAndCompanyAndDeleted(pSOutCode, user.getCompany(), false);

		List<Object[]> queryResult = entityManager.createNativeQuery("select par.code as par_code, par.name as par_name, coalesce (par.vat_no,coalesce(par.bulstat,coalesce(par.egn,par.code))) as par_vat_no,\n" +
						"       coa.code as coa_code, coa.name as coa_name,\n" +
						"       (case when :pICoaId is null then null else 'За сметки: '||(select code||' - '||name from fchart_account where id = :pICoaId and deleted = false) end) as param_coa,\n" +
						"       (case when :pIParId is null then null else 'За контрагент : '||(select name from ccc_partner where id = :pIParId and deleted = false) end) as param_par,\n" +
						"       (select name from ccc_organization_unit where code = :pSOutCode and deleted = false) as out_name,\n" +
						"       case when (coalesce(f.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0)) > 0\n" +
						"            then coalesce(f.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0) else 0.0 end as f_par_ct_amount,\n" +
						"       case when (coalesce(f.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0)) < 0\n" +
						"            then coalesce(f.cc_par_dt_amount, 0.0) - coalesce(f.cc_par_ct_amount, 0.0) else 0.0 end as f_par_dt_amount,\n" +
						"       coalesce(s.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_ct_amount, 0.0) as o_par_ct_amount,\n" +
						"       coalesce(s.cc_par_dt_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0) as o_par_dt_amount,\n" +
						"       case when (s.cc_par_ct_amount - s.cc_par_dt_amount ) > 0\n" +
						"            then s.cc_par_ct_amount - s.cc_par_dt_amount\n" +
						"            else 0.0 end as s_par_ct_amount,\n" +
						"       case when (s.cc_par_ct_amount - s.cc_par_dt_amount) < 0\n" +
						"            then s.cc_par_dt_amount - s.cc_par_ct_amount\n" +
						"            else 0.0 end as s_par_dt_amount,\n" +
						"       'Период от дата: '||to_char(to_date('01'||:pSDateFrom,'ddyyyymm'),'dd.mm.yyyy')||\n" +
						"       (case when substr(:pSDateTo,5,2)='13' then ' до Годишно приключване'\n" +
						"             else ' до дата: '||to_char((to_date('01'||:pSDateTo,'ddyyyymm')+ interval '1 month' - interval '1 day')::::date ,'dd.mm.yyyy')\n" +
						"             end) as period    \n" +
						"from (select ccb.cc_par_id_id as cc_par_id_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_par_ct_amount, 0.0)) as cc_par_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_par_dt_amount, 0.0)) as cc_par_dt_amount\n" +
						"        from fpt_cc_balance ccb\n" +
						"       where ccb.period::::integer+1 = (:pSDateFrom)::::integer\n" +
						"       and ccb.deleted = false\n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +    //TODO hierarchically
						"                                                  end)\n" +
						"         and coa_id_id = (case when :pICoaId is null then coa_id_id else (:pICoaId)::::integer end)\n" +
						"         and cc_par_id_id is not null\n" +
						"         and cc_par_id_id =(case when :pIParId is null then cc_par_id_id else (:pIParId)::::integer end) \n" +
						"       group by ccb.coa_id_id, ccb.cc_par_id_id) as f\n" +
						"     RIGHT OUTER JOIN\n" +
						"     (select ccb.cc_par_id_id as cc_par_id_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_par_ct_amount, 0.0)) as cc_par_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_par_dt_amount, 0.0)) as cc_par_dt_amount\n" +
						"        from fpt_cc_balance ccb\n" +
						"       where ccb.period =  :pSDateTo\n" +
						"       and ccb.deleted =  false\n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +   //TODO hierarchically
						"                                                  end)\n" +
						"         and coa_id_id = (case when :pICoaId is null then coa_id_id else (:pICoaId)::::integer end)\n" +
						"         and cc_par_id_id = (case when :pIParId is null then cc_par_id_id else (:pIParId)::::integer end)\n" +
						"         and cc_par_id_id is not null\n" +
						"       group by ccb.coa_id_id, ccb.cc_par_id_id ) as s on (f.cc_par_id_id = s.cc_par_id_id and f.coa_id_id = s.coa_id_id )\n" +
						"     JOIN  ccc_partner par on (s.cc_par_id_id = par.id)\n" +
						"     JOIN  fchart_account coa on s.coa_id_id = coa.id WHERE coa.deleted = false and par.deleted = false\n" +
						"order by coa.code,par.code,par.vat_no")
				.setParameter("pICoaId", pICoaId)
				.setParameter("pIParId", pIParId)
				.setParameter("pSOutCode", pSOutCode)
				.setParameter("pSOutCodeId", pSOutCodeId)
				.setParameter("pSDateFrom", pSDateFrom)
				.setParameter("pSDateTo", pSDateTo)
				.setParameter("pSConsolidated", pSConsolidated)
				.getResultList();


		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {

			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("par_code", queryRow[0]);
			resultRow.put("par_name", queryRow[1]);
			resultRow.put("par_vat_no", queryRow[2]);
			resultRow.put("coa_code", queryRow[3]);
			resultRow.put("coa_name", queryRow[4]);
			resultRow.put("param_coa", queryRow[5]);
			resultRow.put("param_par", queryRow[6]);
			resultRow.put("out_name", queryRow[7]);
			resultRow.put("f_par_ct_amount", queryRow[8]);
			resultRow.put("f_par_dt_amount", queryRow[9]);
			resultRow.put("o_par_ct_amount", queryRow[10]);
			resultRow.put("o_par_dt_amount", queryRow[11]);
			resultRow.put("s_par_ct_amount", queryRow[12]);
			resultRow.put("s_par_dt_amount", queryRow[13]);
			resultRow.put("period", queryRow[14]);
			result.add(resultRow);
		}
		return result;
	}

	public List<Map<String, Object>> parBalanceByMonths2(String pSOutCode, String pSDateFrom, String pSDateTo, Integer pICoaId, String pSConsolidated, Integer pIParId){

		SecUser user = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			user = ((SantaUser) authentication.getPrincipal()).getSecUser();
		}

		CCcOrganizationUnitRepository cCcOrganizationUnitRepository = ((CCcOrganizationUnitRepository) getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get());
		CCcOrganizationUnit pSOutCodeId = cCcOrganizationUnitRepository.findFirstByCodeAndCompanyAndDeleted(pSOutCode, user.getCompany(), false);

		List<Object[]> queryResult = entityManager.createNativeQuery("select par.code as par_code, par.name as par_name, coalesce(par.vat_no,coalesce(par.bulstat,coalesce(par.egn,par.code))) as par_vat_no,\n" +
						"       coa.code as coa_code, coa.name as coa_name,\n" +
						"       (case when :pICoaId is null then null else 'За сметки: '||(select code||' - '||name from fchart_account where id = :pICoaId and deleted = false) end) as param_coa,\n" +
						"       (case when :pIParId is null then null else 'За контрагент : '||(select name from ccc_partner where id = :pIParId and deleted = false) end) as param_par,\n" +
						"       (select name from ccc_organization_unit where code = :pSOutCode and deleted = false) as out_name,\n" +
						"       case when (coalesce(f.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0)) > 0\n" +
						"            then coalesce(f.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0) else 0.0 end as f_par_ct_amount,\n" +
						"       case when (coalesce(f.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0)) < 0\n" +
						"            then coalesce(f.cc_par_dt_amount, 0.0) - coalesce(f.cc_par_ct_amount, 0.0) else 0.0 end as f_par_dt_amount,\n" +
						"       coalesce(s.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_ct_amount, 0.0) as o_par_ct_amount,\n" +
						"       coalesce(s.cc_par_dt_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0) as o_par_dt_amount,\n" +
						"       case when (s.cc_par_ct_amount - s.cc_par_dt_amount ) > 0\n" +
						"            then s.cc_par_ct_amount - s.cc_par_dt_amount\n" +
						"            else 0.0 end as s_par_ct_amount,\n" +
						"       case when (s.cc_par_ct_amount - s.cc_par_dt_amount) < 0\n" +
						"            then s.cc_par_dt_amount - s.cc_par_ct_amount\n" +
						"            else 0.0 end as s_par_dt_amount,\n" +
						"             'Период от дата: '||to_char(to_date('01'||:pSDateFrom,'ddyyyymm'),'dd.mm.yyyy')||\n" +
						"       (case when substr(:pSDateTo,5,2)='13' then ' до Годишно приключване'\n" +
						"             else ' до дата: '||to_char((to_date('01'||:pSDateTo,'ddyyyymm')+ interval '1 month' - interval '1 day')::::date ,'dd.mm.yyyy')\n" +
						"             end) as period   \n" +
						"from (select ccb.cc_par_id_id as par_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_par_ct_amount, 0.0)) as cc_par_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_par_dt_amount, 0.0)) as cc_par_dt_amount\n" +
						"        from fpt_cc_balance ccb\n" +
						"       where ccb.period::::integer+1 = (:pSDateFrom)::::integer\n" +
						"       and ccb.deleted = false\n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +   //TODO hierarchically
						"                                                  end)\n" +
						"         and coa_id_id = (case when :pICoaId is null then coa_id_id else (:pICoaId)::::integer end)\n" +
						"         and cc_par_id_id is not null\n" +
						"         and cc_par_id_id =(case when :pIParId is null then cc_par_id_id else (:pIParId)::::integer end) \n" +
						"       group by  ccb.cc_par_id_id,ccb.coa_id_id) as f\n" +
						"     RIGHT OUTER JOIN\n" +
						"     (select ccb.cc_par_id_id as par_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_par_ct_amount, 0.0)) as cc_par_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_par_dt_amount, 0.0)) as cc_par_dt_amount\n" +
						"        from fpt_cc_balance ccb\n" +
						"       where ccb.period =  :pSDateTo\n" +
						"       and ccb.deleted =  false\n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +   //TODO hierarchically
						"                                                  end)\n" +
						"         and coa_id_id = (case when :pICoaId is null then coa_id_id else (:pICoaId)::::integer end)\n" +
						"         and cc_par_id_id = (case when :pIParId is null then cc_par_id_id else (:pIParId)::::integer end)\n" +
						"         and cc_par_id_id is not null\n" +
						"       group by ccb.cc_par_id_id, ccb.coa_id_id ) as s on (f.par_id = s.par_id and f.coa_id_id = s.coa_id_id )\n" +
						"     JOIN  ccc_partner par on (s.par_id = par.id)\n" +
						"     JOIN  fchart_account coa on s.coa_id_id = coa.id WHERE coa.deleted = false and par.deleted = false\n" +
						"order by par.code,par.vat_no,coa.code")
				.setParameter("pICoaId", pICoaId)
				.setParameter("pIParId", pIParId)
				.setParameter("pSOutCode", pSOutCode)
				.setParameter("pSOutCodeId", pSOutCodeId)
				.setParameter("pSDateFrom", pSDateFrom)
				.setParameter("pSDateTo", pSDateTo)
				.setParameter("pSConsolidated", pSConsolidated)
				.getResultList();


		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {

			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("par_code", queryRow[0]);
			resultRow.put("par_name", queryRow[1]);
			resultRow.put("par_vat_no", queryRow[2]);
			resultRow.put("coa_code", queryRow[3]);
			resultRow.put("coa_name", queryRow[4]);
			resultRow.put("param_coa", queryRow[5]);
			resultRow.put("param_par", queryRow[6]);
			resultRow.put("out_name", queryRow[7]);
			resultRow.put("f_par_ct_amount", queryRow[8]);
			resultRow.put("f_par_dt_amount", queryRow[9]);
			resultRow.put("o_par_ct_amount", queryRow[10]);
			resultRow.put("o_par_dt_amount", queryRow[11]);
			resultRow.put("s_par_ct_amount", queryRow[12]);
			resultRow.put("s_par_dt_amount", queryRow[13]);
			resultRow.put("period", queryRow[14]);
			result.add(resultRow);
		}
		return result;
	}

	public List<Map<String, Object>> parBalanceByMonthsNull(String pSOutCode, String pSDateFrom, String pSDateTo, Integer pICoaId, String pSConsolidated, Integer pIParId){

		SecUser user = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			user = ((SantaUser) authentication.getPrincipal()).getSecUser();
		}

		CCcOrganizationUnitRepository cCcOrganizationUnitRepository = ((CCcOrganizationUnitRepository) getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get());
		CCcOrganizationUnit pSOutCodeId = cCcOrganizationUnitRepository.findFirstByCodeAndCompanyAndDeleted(pSOutCode, user.getCompany(), false);

		List<Object[]> queryResult = entityManager.createNativeQuery("select par.code as par_code, par.name as par_name, coalesce (par.vat_no,coalesce(par.bulstat,coalesce(par.egn,par.code))) as par_vat_no,\n" +
						"       coa.code as coa_code, coa.name as coa_name,\n" +
						"       (case when :pICoaId is null then null else 'За сметки: '||(select code||' - '||name from fchart_account where id = :pICoaId and deleted = false) end) as param_coa,\n" +
						"       (case when :pIParId is null then null else 'За контрагент : '||(select name from ccc_partner where id = :pIParId and deleted = false) end) as param_par,\n" +
						"       (select name from ccc_organization_unit where code = :pSOutCode and deleted = false) as out_name,\n" +
						"       case when (coalesce(f.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0)) > 0\n" +
						"            then coalesce(f.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0) else 0.0 end as f_par_ct_amount,\n" +
						"       case when (coalesce(f.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0)) < 0\n" +
						"            then coalesce(f.cc_par_dt_amount, 0.0) - coalesce(f.cc_par_ct_amount, 0.0) else 0.0 end as f_par_dt_amount,\n" +
						"       coalesce(s.cc_par_ct_amount, 0.0) - coalesce(f.cc_par_ct_amount, 0.0) as o_par_ct_amount,\n" +
						"       coalesce(s.cc_par_dt_amount, 0.0) - coalesce(f.cc_par_dt_amount, 0.0) as o_par_dt_amount,\n" +
						"       case when (s.cc_par_ct_amount - s.cc_par_dt_amount ) > 0\n" +
						"            then s.cc_par_ct_amount - s.cc_par_dt_amount\n" +
						"            else 0.0 end as s_par_ct_amount,\n" +
						"       case when (s.cc_par_ct_amount - s.cc_par_dt_amount) < 0\n" +
						"            then s.cc_par_dt_amount - s.cc_par_ct_amount\n" +
						"            else 0.0 end as s_par_dt_amount,\n" +
						"       'Период от дата: '||to_char(to_date('01'||:pSDateFrom,'ddyyyymm'),'dd.mm.yyyy')||\n" +
						"       (case when substr(:pSDateTo,5,2)='13' then ' до Годишно приключване'\n" +
						"             else ' до дата: '||to_char((to_date('01'||:pSDateTo,'ddyyyymm')+ interval '1 month' - interval '1 day')::::date ,'dd.mm.yyyy')\n" +
						"             end) as period    \n" +
						"from (select ccb.cc_par_id_id as par_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_par_ct_amount, 0.0)) as cc_par_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_par_dt_amount, 0.0)) as cc_par_dt_amount\n" +
						"        from fpt_cc_balance ccb\n" +
						"       where ccb.period::::integer+1 = (:pSDateFrom)::::integer\n" +
						"       and ccb.deleted = false\n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +   //TODO hierarchically
						"                                                  end)\n" +
						"         and coa_id_id = (case when :pICoaId is null then coa_id_id else (:pICoaId)::::integer end)\n" +
						"         and cc_par_id_id is not null\n" +
						"         and cc_par_id_id =(case when :pIParId is null then cc_par_id_id else (:pIParId)::::integer end) \n" +
						"       group by ccb.coa_id_id, ccb.cc_par_id_id) as f\n" +
						"     RIGHT OUTER JOIN\n" +
						"     (select ccb.cc_par_id_id as par_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_par_ct_amount, 0.0)) as cc_par_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_par_dt_amount, 0.0)) as cc_par_dt_amount\n" +
						"        from fpt_cc_balance ccb\n" +
						"       where ccb.period =  :pSDateTo\n" +
						"       and ccb.deleted =  false\n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +   //TODO hierarchically
						"                                                  end)\n" +
						"         and coa_id_id = (case when :pICoaId is null then coa_id_id else (:pICoaId)::::integer end)\n" +
						"         and cc_par_id_id = (case when :pIParId is null then cc_par_id_id else (:pIParId)::::integer end)\n" +
						"         and cc_par_id_id is not null\n" +
						"       group by ccb.coa_id_id, ccb.cc_par_id_id ) as s on (f.par_id = s.par_id and f.coa_id_id = s.coa_id_id )\n" +
						"     JOIN  ccc_partner par on (s.par_id = par.id)\n" +
						"     JOIN  fchart_account coa on s.coa_id_id = coa.id\n" +
						"where s.cc_par_ct_amount <> s.cc_par_dt_amount and coa.deleted = false and par.deleted = false\n" +
						"order by coa.code,par.code,par.vat_no")
				.setParameter("pICoaId", pICoaId)
				.setParameter("pIParId", pIParId)
				.setParameter("pSOutCode", pSOutCode)
				.setParameter("pSOutCodeId", pSOutCodeId)
				.setParameter("pSDateFrom", pSDateFrom)
				.setParameter("pSDateTo", pSDateTo)
				.setParameter("pSConsolidated", pSConsolidated)
				.getResultList();


		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {

			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("par_code", queryRow[0]);
			resultRow.put("par_name", queryRow[1]);
			resultRow.put("par_vat_no", queryRow[2]);
			resultRow.put("coa_code", queryRow[3]);
			resultRow.put("coa_name", queryRow[4]);
			resultRow.put("param_coa", queryRow[5]);
			resultRow.put("param_par", queryRow[6]);
			resultRow.put("out_name", queryRow[7]);
			resultRow.put("f_par_ct_amount", queryRow[8]);
			resultRow.put("f_par_dt_amount", queryRow[9]);
			resultRow.put("o_par_ct_amount", queryRow[10]);
			resultRow.put("o_par_dt_amount", queryRow[11]);
			resultRow.put("s_par_ct_amount", queryRow[12]);
			resultRow.put("s_par_dt_amount", queryRow[13]);
			resultRow.put("period", queryRow[14]);
			result.add(resultRow);
		}
		return result;
	}

	public List<Map<String, Object>> outBalanceByMonths(String pSOutCode, String pSDateFrom, String pSDateTo, Integer pICoaId, String pSConsolidated, Integer pICcoutId){

		SecUser user = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			user = ((SantaUser) authentication.getPrincipal()).getSecUser();
		}

		CCcOrganizationUnitRepository cCcOrganizationUnitRepository = ((CCcOrganizationUnitRepository) getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get());
		CCcOrganizationUnit pSOutCodeId = cCcOrganizationUnitRepository.findFirstByCodeAndCompanyAndDeleted(pSOutCode, user.getCompany(), false);

		List<Object[]> queryResult = entityManager.createNativeQuery("select ccout1.code as cc_out_code, ccout1.name as cc_out_name,\n" +
						"       coa.code as coa_code, coa.name as coa_name,\n" +
						"       (case when :pICoaId is null then null else 'За сметки: '||(select code||' - '||name from fchart_account where id = :pICoaId and deleted = false) end) as param_coa,\n" +
						"       (case when :pICcoutId is null then null else 'За орг. единица : '||(select code||' - '||name from ccc_organization_unit where id = :pICcoutId and deleted = false) end) as param_ccout,\n" +
						"       (case when ccout1.out_id_id is null then '0' else '1' end) as out_up,\n" +
						"       (select name from ccc_organization_unit where code = :pSOutCode and deleted = false) as out_name,\n" +
						"       case when (coalesce(f.cc_out_ct_amount, 0.0) - coalesce(f.cc_out_dt_amount, 0.0)) > 0\n" +
						"            then coalesce(f.cc_out_ct_amount, 0.0) - coalesce(f.cc_out_dt_amount, 0.0) else 0.0 end as f_out_ct_amount,\n" +
						"       case when (coalesce(f.cc_out_ct_amount, 0.0) - coalesce(f.cc_out_dt_amount, 0.0)) < 0\n" +
						"            then coalesce(f.cc_out_dt_amount, 0.0) - coalesce(f.cc_out_ct_amount, 0.0) else 0.0 end as f_out_dt_amount,\n" +
						"       coalesce(s.cc_out_ct_amount, 0.0) - coalesce(f.cc_out_ct_amount, 0.0) as o_out_ct_amount,\n" +
						"       coalesce(s.cc_out_dt_amount, 0.0) - coalesce(f.cc_out_dt_amount, 0.0) as o_out_dt_amount,\n" +
						"       case when (s.cc_out_ct_amount - s.cc_out_dt_amount ) > 0\n" +
						"            then s.cc_out_ct_amount - s.cc_out_dt_amount\n" +
						"            else 0.0 end as s_out_ct_amount,\n" +
						"       case when (s.cc_out_ct_amount - s.cc_out_dt_amount) < 0\n" +
						"            then s.cc_out_dt_amount - s.cc_out_ct_amount\n" +
						"            else 0.0 end as s_out_dt_amount,\n" +
						"       'Период от дата: '||to_char(to_date('01'||:pSDateFrom,'ddyyyymm'),'dd.mm.yyyy')||\n" +
						"       (case when substr(:pSDateTo,5,2)='13' then ' до Годишно приключване'\n" +
						"             else ' до дата: '||to_char((to_date('01'||:pSDateTo,'ddyyyymm')+ interval '1 month' - interval '1 day')::::date ,'dd.mm.yyyy')\n" +
						"             end) as period\n" +
						"from (select ccout.id as cc_out_id_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_out_ct_amount, 0.0)) as cc_out_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_out_dt_amount, 0.0)) as cc_out_dt_amount\n" +
						"        from fpt_cc_balance ccb,\n" +
						"             ccc_organization_unit ccout\n" +
						"       where ccb.period::::integer+1 = (:pSDateFrom)::::integer\n" +
						"       and ccb.deleted = false and ccout.deleted = false \n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +  //TODO hierarchically
						"                                                  end)\n" +
						"         and (case when :pICoaId is null then ccb.coa_id_id=ccb.coa_id_id\n" +
						"              else ccb.coa_id_id = /*ANY (get_all_chiled_coa($P{P_I_COA_ID}*/:pICoaId/*::integer))*/\n" +  //TODO hierarchically
						"               end)\n" +
						"         and cc_out_id_id is not null\n" +
						"         and (case when :pICcoutId is null then cc_out_id_id = cc_out_id_id else (cc_out_id_id  = /*ANY (get_all_chiled_out($P{P_I_CCOUT_ID}*/:pICcoutId/*::integer))*/) end)\n" +  //TODO hierarchically
						"         and ccout.id = /*ANY (get_all_parent_out(ccb.cc_out_id))*/ccb.cc_out_id_id\n" +   //TODO hierarchically
						"       group by ccb.coa_id_id,ccout.id) as f\n" +
						"     RIGHT OUTER JOIN\n" +
						"     (select ccout.id as cc_out_id_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_out_ct_amount, 0.0)) as cc_out_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_out_dt_amount, 0.0)) as cc_out_dt_amount\n" +
						"        from fpt_cc_balance ccb,\n" +
						"             ccc_organization_unit ccout\n" +
						"       where ccb.period =  :pSDateTo\n" +
						"       and ccb.deleted = false and ccout.deleted = false \n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +  //TODO hierarchically
						"                                                  end)\n" +
						"         and (case when :pICoaId is null then ccb.coa_id_id=ccb.coa_id_id \n" +
						"              else ccb.coa_id_id = /*ANY (get_all_chiled_coa($P{P_I_COA_ID}*/:pICoaId/*::integer))*/\n" +  //TODO hierarchically
						"               end)\n" +
						"         and (case when :pICcoutId is null then cc_out_id_id = cc_out_id_id else (cc_out_id_id = /*ANY (get_all_chiled_out($P{P_I_CCOUT_ID}*/:pICcoutId/*::integer))*/) end)\n" +
						"         and ccout.id = /*ANY (get_all_parent_out(ccb.cc_out_id))*/ccb.cc_out_id_id\n" +   //TODO hierarchically
						"         and cc_out_id_id is not null\n" +
						"       group by ccb.coa_id_id,ccout.id ) as s on (f.cc_out_id_id = s.cc_out_id_id and f.coa_id_id = s.coa_id_id )\n" +
						"     JOIN  ccc_organization_unit ccout1 on (s.cc_out_id_id = ccout1.id)\n" +
						"     JOIN  fchart_account coa on s.coa_id_id = coa.id WHERE ccout1.deleted = false and coa.deleted = false\n" +
						"order by coa_code,ccout1.code")
				.setParameter("pICoaId", pICoaId)
				.setParameter("pICcoutId", pICcoutId)
				.setParameter("pSOutCode", pSOutCode)
				.setParameter("pSOutCodeId", pSOutCodeId)
				.setParameter("pSDateFrom", pSDateFrom)
				.setParameter("pSDateTo", pSDateTo)
				.setParameter("pSConsolidated", pSConsolidated)
				.getResultList();


		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {

			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("cc_out_code", queryRow[0]);
			resultRow.put("cc_out_name", queryRow[1]);
			resultRow.put("coa_code", queryRow[2]);
			resultRow.put("coa_name", queryRow[3]);
			resultRow.put("param_coa", queryRow[4]);
			resultRow.put("param_ccout", queryRow[5]);
			resultRow.put("out_up", queryRow[6]);
			resultRow.put("out_name", queryRow[7]);
			resultRow.put("f_out_ct_amount", queryRow[8]);
			resultRow.put("f_out_dt_amount", queryRow[9]);
			resultRow.put("o_out_ct_amount", queryRow[10]);
			resultRow.put("o_out_dt_amount", queryRow[11]);
			resultRow.put("s_out_ct_amount", queryRow[12]);
			resultRow.put("s_out_dt_amount", queryRow[13]);
			resultRow.put("period", queryRow[14]);
			result.add(resultRow);
		}
		return result;
	}

	public List<Map<String, Object>> outBalanceByMonths2(String pSOutCode, String pSDateFrom, String pSDateTo, Integer pICoaId, String pSConsolidated, Integer pICcoutId){

		SecUser user = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			user = ((SantaUser) authentication.getPrincipal()).getSecUser();
		}

		CCcOrganizationUnitRepository cCcOrganizationUnitRepository = ((CCcOrganizationUnitRepository) getRepositories().getRepositoryFor(CCcOrganizationUnit.class).get());
		CCcOrganizationUnit pSOutCodeId = cCcOrganizationUnitRepository.findFirstByCodeAndCompanyAndDeleted(pSOutCode, user.getCompany(), false);

		List<Object[]> queryResult = entityManager.createNativeQuery("select ccout1.code as cc_out_code, ccout1.name as cc_out_name,\n" +
						"       coa.code as coa_code, coa.name as coa_name,\n" +
						"       (case when :pICoaId is null then null else 'За сметки: '||(select code||' - '||name from fchart_account where id = :pICoaId and deleted = false) end) as param_coa,\n" +
						"       (case when :pICcoutId is null then null else 'За орг. единица : '||(select name from ccc_organization_unit where id = :pICcoutId and deleted = false) end) as param_ccout,\n" +
						"       (case when ccout1.out_id_id is null then '0' else '1' end) as out_up,\n" +
						"       (select name from ccc_organization_unit where code = :pSOutCode and deleted = false) as out_name,\n" +
						"       case when (coalesce(f.cc_out_ct_amount, 0.0) - coalesce(f.cc_out_dt_amount, 0.0)) > 0\n" +
						"            then coalesce(f.cc_out_ct_amount, 0.0) - coalesce(f.cc_out_dt_amount, 0.0) else 0.0 end as f_out_ct_amount,\n" +
						"       case when (coalesce(f.cc_out_ct_amount, 0.0) - coalesce(f.cc_out_dt_amount, 0.0)) < 0\n" +
						"            then coalesce(f.cc_out_dt_amount, 0.0) - coalesce(f.cc_out_ct_amount, 0.0) else 0.0 end as f_out_dt_amount,\n" +
						"       coalesce(s.cc_out_ct_amount, 0.0) - coalesce(f.cc_out_ct_amount, 0.0) as o_out_ct_amount,\n" +
						"       coalesce(s.cc_out_dt_amount, 0.0) - coalesce(f.cc_out_dt_amount, 0.0) as o_out_dt_amount,\n" +
						"       case when (s.cc_out_ct_amount - s.cc_out_dt_amount ) > 0\n" +
						"            then s.cc_out_ct_amount - s.cc_out_dt_amount\n" +
						"            else 0.0 end as s_out_ct_amount,\n" +
						"       case when (s.cc_out_ct_amount - s.cc_out_dt_amount) < 0\n" +
						"            then s.cc_out_dt_amount - s.cc_out_ct_amount\n" +
						"            else 0.0 end as s_out_dt_amount,\n" +
						"       'Период от дата: '||to_char(to_date('01'||:pSDateFrom,'ddyyyymm'),'dd.mm.yyyy')||\n" +
						"       (case when substr(:pSDateTo,5,2)='13' then ' до Годишно приключване'\n" +
						"             else ' до дата: '||to_char((to_date('01'||:pSDateTo,'ddyyyymm')+ interval '1 month' - interval '1 day')::::date ,'dd.mm.yyyy')\n" +
						"             end) as period            \n" +
						"from (select ccout.id as cc_out_id_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_out_ct_amount, 0.0)) as cc_out_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_out_dt_amount, 0.0)) as cc_out_dt_amount\n" +
						"        from fpt_cc_balance ccb,\n" +
						"             ccc_organization_unit ccout\n" +
						"       where ccb.period::::integer+1 = (:pSDateFrom)::::integer\n" +
						"       and ccb.deleted = false\n" +
						"       and ccout.deleted = false\n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +   //TODO hierarchically
						"                                                  end)\n" +
						"         and (case when :pICoaId is null then ccb.coa_id_id=ccb.coa_id_id \n" +
						"              else ccb.coa_id_id  = /*ANY (get_all_chiled_coa($P{P_I_COA_ID}::integer))*/:pICoaId \n" +   //TODO hierarchically
						"               end)\n" +
						"         and cc_out_id_id is not null\n" +
						"         and (case when :pICcoutId is null then cc_out_id_id = cc_out_id_id else (cc_out_id_id = /*ANY (get_all_chiled_out($P{P_I_CCOUT_ID}*/:pICcoutId/*::integer))*/) end)\n" +   //TODO hierarchically
						"         and ccout.id = /*ANY (get_all_parent_out(ccb.cc_out_id))*/ccb.cc_out_id_id \n" +   //TODO hierarchically
						"       group by ccout.id,ccb.coa_id_id) as f\n" +
						"     RIGHT OUTER JOIN\n" +
						"     (select ccout.id as cc_out_id_id,\n" +
						"             ccb.coa_id_id as coa_id_id,\n" +
						"             sum(coalesce(ccb.cc_out_ct_amount, 0.0)) as cc_out_ct_amount,\n" +
						"             sum(coalesce(ccb.cc_out_dt_amount, 0.0)) as cc_out_dt_amount\n" +
						"        from fpt_cc_balance ccb,\n" +
						"             ccc_organization_unit ccout\n" +
						"       where ccb.period =  :pSDateTo\n" +
						"       and ccb.deleted =  false\n" +
						"       and ccout.deleted =  false\n" +
						"         and (case when :pSConsolidated ='N' then ccb.out_code_id = :pSOutCodeId\n" +
						"                                                  else ccb.out_code_id in (/*select out1.code from register.cc_organization_units out1 \n" +
						"                                                                        where out1.id = ANY (get_all_chiled_out($P{P_S_OUT_CODE}*/:pSOutCodeId/*))*/)\n" +   //TODO hierarchically
						"                                                  end)\n" +
						"         and (case when :pICoaId is null then ccb.coa_id_id=ccb.coa_id_id \n" +
						"              else ccb.coa_id_id = /*ANY (get_all_chiled_coa($P{P_I_COA_ID}::integer))*/:pICoaId \n" +   //TODO hierarchically
						"               end)\n" +
						"         and (case when :pICcoutId is null then cc_out_id_id = cc_out_id_id else (cc_out_id_id = /*ANY (get_all_chiled_out($P{P_I_CCOUT_ID}*/:pICcoutId/*::integer))*/) end)\n" +   //TODO hierarchically
						"         and ccout.id = /*ANY (get_all_parent_out(ccb.cc_out_id))*/ccb.cc_out_id_id \n" +    //TODO hierarchically
						"         and cc_out_id_id is not null\n" +
						"       group by ccout.id,ccb.coa_id_id) as s on (f.cc_out_id_id = s.cc_out_id_id and f.coa_id_id = s.coa_id_id )\n" +
						"     JOIN  ccc_organization_unit ccout1 on (s.cc_out_id_id = ccout1.id)\n" +
						"     JOIN  fchart_account coa on s.coa_id_id = coa.id WHERE ccout1.deleted = false and coa.deleted = false\n" +
						"order by ccout1.code,coa_code")
				.setParameter("pICoaId", pICoaId)
				.setParameter("pICcoutId", pICcoutId)
				.setParameter("pSOutCode", pSOutCode)
				.setParameter("pSOutCodeId", pSOutCodeId)
				.setParameter("pSDateFrom", pSDateFrom)
				.setParameter("pSDateTo", pSDateTo)
				.setParameter("pSConsolidated", pSConsolidated)
				.getResultList();


		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {

			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("cc_out_code", queryRow[0]);
			resultRow.put("cc_out_name", queryRow[1]);
			resultRow.put("coa_code", queryRow[2]);
			resultRow.put("coa_name", queryRow[3]);
			resultRow.put("param_coa", queryRow[4]);
			resultRow.put("param_ccout", queryRow[5]);
			resultRow.put("out_up", queryRow[6]);
			resultRow.put("out_name", queryRow[7]);
			resultRow.put("f_out_ct_amount", queryRow[8]);
			resultRow.put("f_out_dt_amount", queryRow[9]);
			resultRow.put("o_out_ct_amount", queryRow[10]);
			resultRow.put("o_out_dt_amount", queryRow[11]);
			resultRow.put("s_out_ct_amount", queryRow[12]);
			resultRow.put("s_out_dt_amount", queryRow[13]);
			resultRow.put("period", queryRow[14]);
			result.add(resultRow);
		}
		return result;
	}

	public List<Map<String, Object>> unpaidDocsByPar(String pSOutCode, Integer pSBteSide, String pSCons){

		List<Object[]> queryResult = entityManager.createNativeQuery("WITH batches AS (SELECT bah.id AS bah_id, bah.xmin, bah.post_date, bah.out_code_id, " +
								"bah.module, bah.ref_no, bah.ref_date, bah.amount, bah.amount_outstanding, bah.amount2, bah.amount3, bah.due_date," +
								" (((((((((bah.ref_no)::::text || '/'::::text) || bah.ref_date) || ' тип: '::::text) || (bte.code)::::text) || '-'::::text)" +
								" || (bte.name)::::text) || ' '::::text) || (COALESCE(par.name, ''::::character varying))::::text) AS descr, bah.corr_ref_no," +
								" bah.bte_id_id, bte.name AS bte_name, bte.code AS bte_code, bte.type_id AS bte_type," +
								" CASE WHEN bte.is_dds = true" + /*'Y'::bpchar*/
								" THEN ide.side_id ELSE bte.side_id END AS bte_side, bah.par_id_id, par.code AS par_code, par.name AS par_name, bah.ide_id_id," +
								" ide.code AS ide_code, ide.name AS ide_name, bah.cuy_code_id, bah.cuy_rate, bah.cuy_unit, bah.amount_currency," +
								" bah.amount_outstanding_currency, bah.amount2currency, bah.amount3currency " + 
								/*pon.id AS pon_id,CASE WHEN pon.id IS NULL THEN (0)::numeric ELSE accounting.lon_int_liability(pon.id) END AS interest_in_now*/
								" FROM fpt_batch bah JOIN fct_batch_type bte ON bah.bte_id_id = bte.id AND bte.deleted = false" +
								" LEFT JOIN ccc_partner par ON bah.par_id_id = par.id AND par.deleted = false" +
								" LEFT JOIN list_option_item loi ON loi.dtype = 'LoiBatchCalculationType' AND loi.list_option_item_code = 2 AND loi.deleted = false" +
								" LEFT JOIN fct_inv_deal_type ide ON bah.ide_id_id = ide.id AND ide.deleted = false " +
//								"/*LEFT JOIN accounting.lon_postings pon ON ((bah.id = pon.bah_id))*/" +
								" WHERE " +
								" bah.deleted = false AND" +
								" (COALESCE(bte.type_id, -1/*'N'::bpchar*/) = /*'L'::bpchar*/loi.id) AND ((bah.amount_outstanding <> (0)::::numeric)" +
								" OR (bah.amount_outstanding_currency <> (0)::::numeric)) )" +
						"select a.*,\n" +
						"case when /*a.days <30 and*/ a.days<=30 then a.total else null end as less30,\n" +
						"case when a.days >30 and a.days<=60 then a.total else null end as less60,\n" +
						"case when a.days >60 and a.days<=120 then a.total else null end as less120,\n" +
						"case when a.days >120 then a.total else null end as more120\n" +
						"from \n" +
						"(SELECT\n" +
							"out_code_id,\n" +
							"out.name, \n" +
							"par_code as par_code, \n" +
							"par_name as par_name, \n" +
							"to_char(current_date, 'dd.mm.yyyy г.') as cur_date, \n" +
							"to_char(due_date,'dd.mm.yyyy') as due_date, \n" +
							"to_char(ref_date,'dd.mm.yyyy') as ref_date ,\n" +
							"ref_no||'/'||to_char(ref_date,'dd.mm.yyyy') as descr, \n" +
							"amount,\n" +
							"amount_outstanding-amount_in_main_cuy_no_pst AS total, \n" +
							"extract('days' from (now() - (coalesce(due_date,ref_date)))) as days\n" +
						"FROM (" +
							" SELECT b.bah_id, b.xmin, b.post_date, b.out_code_id, b.module," +
							" b.ref_no, b.ref_date, b.amount, b.amount_outstanding, b.amount2, b.amount3, b.due_date, b.descr, b.corr_ref_no," +
							" b.bte_id_id, b.bte_name, b.bte_code, b.bte_type, b.bte_side, b.par_id_id, b.par_code, b.par_name, b.ide_id_id, b.ide_code," +
							" b.ide_name, b.cuy_code_id, b.cuy_rate, b.cuy_unit, b.amount_currency, b.amount_outstanding_currency, b.amount2currency," +
							" b.amount3currency,/* b.pon_id, b.interest_in_now,*/ COALESCE(c.amount_in_main_cuy, (0)::::numeric)" +
							" AS amount_in_main_cuy_no_pst " +
	//						"/*COALESCE(c.in_liab_amount, (0)::numeric) AS amount_in_liab_cuy_no_pst*/" +
							" FROM (batches b "+
								" LEFT JOIN (SELECT a.bah_id, sum(a.amount_in_main_cuy) AS amount_in_main_cuy," +
									" sum(a.in_liab_amount) AS in_liab_amount " +
									"FROM ("+
				//						"/*SELECT ba.bah_id */" +
						//				"/*CASE WHEN ((pat.cuy_code)::text = (accounting.main_currency(pat.out_code))::text)" +
						//				" THEN plk.amount ELSE accounting.in_main_cuy(pat.cuy_code, pat.out_code, pat.date_payment, plk.amount)" +
						//				" END AS amount_in_main_cuy," +
						//				" CASE WHEN ((pat.cuy_code)::text = (ba.cuy_code)::text)" +
						//				" THEN plk.amount ELSE accounting.from_main_cuy(ba.cuy_code, pat.out_code, pat.date_payment," +
						//				" CASE WHEN ((pat.cuy_code)::text = (accounting.main_currency(pat.out_code))::text)" +
						//				" THEN plk.amount ELSE accounting.in_main_cuy(pat.cuy_code, pat.out_code, pat.date_payment, plk.amount) END)" +
						//				" END AS in_liab_amount*/ /*FROM*/ /*cash.cah_payments pat, cash.cah_payment_links plk, fct_batch_tte_link btk," +
						//				" fct_transition_type tte,*/ /*batches ba" +
						//				" WHERE ba.out_code_id != null*/ /*((((((((((((pat.out_code)::text = (btk.out_code)::text)" +
						//				" AND (pat.id = plk.pat_id)) AND ((plk.tte_code)::text = (tte.code)::text)) AND (tte.id = btk.tte_id))" +
						//				" AND (plk.pst_status = 'N'::bpchar)) AND (plk.ape_status = ANY (ARRAY['S'::bpchar, 'A'::bpchar])))" +
						//				" AND ((COALESCE(ba.ref_no, ''::character varying))::text = (COALESCE(plk.ref_no, ''::character varying))::text))" +
						//				" AND (COALESCE(ba.ref_date, ('now'::text)::date) = COALESCE(plk.ref_date, ('now'::text)::date)))" +
						//				" AND (COALESCE(ba.par_id_id, 0) = COALESCE(pat.par_id_id, 0))) AND (ba.bte_id_id = btk.bte_id_id))" +
						//				" AND ((ba.out_code)::text = (plk.out_code)::text))*/" +
						//				" /*UNION ALL*/ " +
										"SELECT ba.bah_id," +
				//						"/* CASE WHEN ((trn.cuy_code_id)::::text = (accounting.main_currency(trn.out_code_id))::::text) THEN */"+
										"trn.amount_total" +
				//						" /*ELSE accounting.in_main_cuy(trn.cuy_code, trn.out_code, trn.ref_date, trn.amount_total) END*/"+
										" AS amount_in_main_cuy," +
//										" CASE WHEN ((trn.cuy_code_id)::::text = (ba.cuy_code_id)::::text) THEN "+
										" trn.amount_total "+
				//						" /*ELSE accounting.from_main_cuy(ba.cuy_code," +
				//						" trn.out_code, trn.ref_date, CASE WHEN ((trn.cuy_code)::text = (accounting.main_currency(trn.out_code))::text)" +
				//						" THEN trn.amount_total ELSE accounting.in_main_cuy(trn.cuy_code, trn.out_code, trn.ref_date, trn.amount_total) END) */ END" +
										" AS in_liab_amount FROM fbre_transition trn, fct_batch_tte_link btk," +
										" fct_transition_type tte, batches ba "+
										" WHERE (((((((((trn.out_code_id)::::text = (btk.out_code_id)::::text)" +
										" AND (btk.tte_id_id = tte.id)) AND (btk.bte_id_id = ba.bte_id_id))" +
										" AND trn.deleted = false AND btk.deleted = false AND tte.deleted = false" +
										" AND ((COALESCE(ba.ref_no, ''::::character varying))::::text = (COALESCE(trn.add_ref_no, ''::::character varying))::::text))" +
										" AND (COALESCE(ba.ref_date, ('now'::::text)::::date) = COALESCE(trn.add_ref_date, ('now'::::text)::::date)))" +
										" AND (COALESCE(ba.par_id_id, 0) = COALESCE(trn.cc_par_id_id, 0))) AND ((ba.out_code_id)::::text = (trn.out_code_id)::::text))" +
										" AND ((SELECT loi.list_option_item_code FROM list_option_item loi WHERE loi.dtype = 'LoiBreTransitionResult' AND loi.id = trn.result_id AND loi.deleted = false) <> ALL (ARRAY[:constOK, :constManual])))"+
									") a GROUP BY a.bah_id "+
								") c ON ((b.bah_id = c.bah_id))"+
							") "+
						") as vw_liability_batches " +
						"join ccc_organization_unit out on out.id = out_code_id\n" +
						"WHERE bte_side = :pSBteSide and out.deleted = false and out.code like :pSOutCode || case when :pSCons='N' then '' else '%' end) a \n" +
						"ORDER BY :pSOutCode,par_code")
				.setParameter("pSOutCode", pSOutCode)
				.setParameter("pSBteSide", pSBteSide)
				.setParameter("pSCons", pSCons)
				.setParameter("constOK", LoiBreTransitionResult.BRE_TRANSITION_RESULT_OK)
				.setParameter("constManual", LoiBreTransitionResult.BRE_TRANSITION_RESULT_MANUAL_PROCESS)
				.getResultList();


		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {

			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("bah_id", queryRow[0]);
			resultRow.put("bte_name", queryRow[1]);
			resultRow.put("bte_code", queryRow[2]);
			resultRow.put("bte_type", queryRow[3]);
			resultRow.put("bte_side", queryRow[4]);
			resultRow.put("ide_code", queryRow[7]);
			resultRow.put("ide_name", queryRow[8]);
			resultRow.put("less30", queryRow[9]);
			resultRow.put("less60", queryRow[10]);
			resultRow.put("less120", queryRow[11]);
			resultRow.put("more120", queryRow[12]);
			resultRow.put("par_code", queryRow[14]);
			resultRow.put("par_name", queryRow[15]);
			resultRow.put("cur_date", queryRow[16]);
			resultRow.put("due_date", queryRow[17]);
			resultRow.put("ref_date", queryRow[18]);
			resultRow.put("descr", queryRow[19]);
			resultRow.put("total", queryRow[20]);
			resultRow.put("days", queryRow[21]);
			resultRow.put("amount_in_main_cuy_no_pst", queryRow[21]);
			resultRow.put("amount_in_main_cuy", queryRow[22]);
			resultRow.put("in_liab_amount", queryRow[23]);
			result.add(resultRow);

		}

		return result;
	}

	public List<Map<String, Object>> unpaidDocsByParSum(String pSOutCode, Integer pSBteSide, String pSCons){

		List<Object[]> queryResult = entityManager.createNativeQuery("WITH batches AS (SELECT bah.id AS bah_id, bah.xmin, bah.post_date, bah.out_code_id, " +
						"bah.module, bah.ref_no, bah.ref_date, bah.amount, bah.amount_outstanding, bah.amount2, bah.amount3, bah.due_date," +
						" (((((((((bah.ref_no)::::text || '/'::::text) || bah.ref_date) || ' тип: '::::text) || (bte.code)::::text) || '-'::::text)" +
						" || (bte.name)::::text) || ' '::::text) || (COALESCE(par.name, ''::::character varying))::::text) AS descr, bah.corr_ref_no," +
						" bah.bte_id_id, bte.name AS bte_name, bte.code AS bte_code, bte.type_id AS bte_type," +
						" CASE WHEN (bte.is_dds = true/*'Y'::bpchar*/)" +
						" THEN ide.side_id ELSE bte.side_id END AS bte_side, bah.par_id_id, par.code AS par_code, par.name AS par_name, bah.ide_id_id," +
						" ide.code AS ide_code, ide.name AS ide_name, bah.cuy_code_id, bah.cuy_rate, bah.cuy_unit, bah.amount_currency," +
						" bah.amount_outstanding_currency, bah.amount2currency, bah.amount3currency "+
//						"/*pon.id AS pon_id," +
//						" CASE WHEN (pon.id IS NULL) THEN (0)::numeric ELSE accounting.lon_int_liability(pon.id) END AS interest_in_now*/" +
						" FROM (((((fpt_batch bah JOIN fct_batch_type bte ON ((bah.bte_id_id = bte.id) AND bte.deleted = false))" +
						" LEFT JOIN ccc_partner par ON ((bah.par_id_id = par.id) AND par.deleted = false))" +
						" LEFT JOIN list_option_item loi ON ((loi.dtype = 'LoiBatchCalculationType' AND loi.list_option_item_code = 2) AND (bte.type_id = loi.id) AND loi.deleted = false ))" +
						" LEFT JOIN fct_inv_deal_type ide ON ((bah.ide_id_id = ide.id) AND ide.deleted = false))" +
						" /*LEFT JOIN accounting.lon_postings pon ON ((bah.id = pon.bah_id))*/)" +
						" WHERE (" +
						" bah.deleted = false AND" +
						" (COALESCE(bte.type_id, -1/*'N'::bpchar*/) = /*'L'::bpchar*/loi.id) AND ((bah.amount_outstanding <> (0)::::numeric)" +
						" OR (bah.amount_outstanding_currency <> (0)::::numeric))))" +
						"select a.*,par.code as par_code,\n" +
						"par.name as par_name,\n" +
						"COALESCE(bulstat, egn, vat_no, foreign_no,'') AS id_no,\n" +
						"to_char(current_date, 'dd.mm.yyyy г.') as cur_date\n" +
						"from \n" +
						"(SELECT " +
						"par_id_id, \n" +
						"out_code_id, \n" +
						"out.name, \n" +
		//				"--sum(amount)as amount, \n" +
						"sum(amount_outstanding-amount_in_main_cuy_no_pst) AS total \n" +
						"FROM (" +
						" SELECT b.bah_id, b.xmin, b.post_date, b.out_code_id, b.module," +
						" b.ref_no, b.ref_date, b.amount, b.amount_outstanding, b.amount2, b.amount3, b.due_date, b.descr, b.corr_ref_no," +
						" b.bte_id_id, b.bte_name, b.bte_code, b.bte_type, b.bte_side, b.par_id_id, b.par_code, b.par_name, b.ide_id_id, b.ide_code," +
						" b.ide_name, b.cuy_code_id, b.cuy_rate, b.cuy_unit, b.amount_currency, b.amount_outstanding_currency, b.amount2currency," +
						" b.amount3currency,/* b.pon_id, b.interest_in_now,*/ COALESCE(c.amount_in_main_cuy, (0)::::numeric)" +
						" AS amount_in_main_cuy_no_pst " +
//						"/*COALESCE(c.in_liab_amount, (0)::numeric) AS amount_in_liab_cuy_no_pst*/" +
						" FROM (batches b LEFT JOIN (SELECT a.bah_id, sum(a.amount_in_main_cuy) AS amount_in_main_cuy," +
						" sum(a.in_liab_amount) AS in_liab_amount " +
						"FROM ( "+
//						" /*SELECT ba.bah_id */" +
		//				"/*CASE WHEN ((pat.cuy_code)::text = (accounting.main_currency(pat.out_code))::text)" +
		//				" THEN plk.amount ELSE accounting.in_main_cuy(pat.cuy_code, pat.out_code, pat.date_payment, plk.amount)" +
		//				" END AS amount_in_main_cuy," +
		//				" CASE WHEN ((pat.cuy_code)::text = (ba.cuy_code)::text)" +
		//				" THEN plk.amount ELSE accounting.from_main_cuy(ba.cuy_code, pat.out_code, pat.date_payment," +
		//				" CASE WHEN ((pat.cuy_code)::text = (accounting.main_currency(pat.out_code))::text)" +
		//				" THEN plk.amount ELSE accounting.in_main_cuy(pat.cuy_code, pat.out_code, pat.date_payment, plk.amount) END)" +
		//				" END AS in_liab_amount*/ /*FROM*/ /*cash.cah_payments pat, cash.cah_payment_links plk, fct_batch_tte_link btk," +
		//				" fct_transition_type tte,*/ /*batches ba" +
		//				" WHERE ba.out_code_id != null*/ /*((((((((((((pat.out_code)::text = (btk.out_code)::text)" +
		//				" AND (pat.id = plk.pat_id)) AND ((plk.tte_code)::text = (tte.code)::text)) AND (tte.id = btk.tte_id))" +
		//				" AND (plk.pst_status = 'N'::bpchar)) AND (plk.ape_status = ANY (ARRAY['S'::bpchar, 'A'::bpchar])))" +
		//				" AND ((COALESCE(ba.ref_no, ''::character varying))::text = (COALESCE(plk.ref_no, ''::character varying))::text))" +
		//				" AND (COALESCE(ba.ref_date, ('now'::text)::date) = COALESCE(plk.ref_date, ('now'::text)::date)))" +
		//				" AND (COALESCE(ba.par_id_id, 0) = COALESCE(pat.par_id_id, 0))) AND (ba.bte_id_id = btk.bte_id_id))" +
		//				" AND ((ba.out_code)::text = (plk.out_code)::text))*/" +
		//				" /*UNION ALL*/ " +
						"SELECT ba.bah_id," +
//						"/* CASE WHEN ((trn.cuy_code_id)::::text = (accounting.main_currency(trn.out_code_id))::::text) THEN */"+
						"trn.amount_total" +
//						" /*ELSE accounting.in_main_cuy(trn.cuy_code, trn.out_code, trn.ref_date, trn.amount_total) END*/"+
						" AS amount_in_main_cuy," +
						" CASE WHEN ((trn.cuy_code_id)::::text = (ba.cuy_code_id)::::text) THEN trn.amount_total "+
//						"/*ELSE accounting.from_main_cuy(ba.cuy_code," +
//						" trn.out_code, trn.ref_date, CASE WHEN ((trn.cuy_code)::text = (accounting.main_currency(trn.out_code))::text)" +
//						" THEN trn.amount_total ELSE accounting.in_main_cuy(trn.cuy_code, trn.out_code, trn.ref_date, trn.amount_total) END)*/" +
						" END AS in_liab_amount FROM fbre_transition trn, fct_batch_tte_link btk," +
						" fct_transition_type tte, batches ba WHERE (((((((((trn.out_code_id)::::text = (btk.out_code_id)::::text)" +
						" AND (btk.tte_id_id = tte.id)) AND (btk.bte_id_id = ba.bte_id_id))" +
						" AND trn.deleted = false AND btk.deleted = false AND tte.deleted = false" +
						" AND ((COALESCE(ba.ref_no, ''::::character varying))::::text = (COALESCE(trn.add_ref_no, ''::::character varying))::::text))" +
						" AND (COALESCE(ba.ref_date, ('now'::::text)::::date) = COALESCE(trn.add_ref_date, ('now'::::text)::::date)))" +
						" AND (COALESCE(ba.par_id_id, 0) = COALESCE(trn.cc_par_id_id, 0))) AND ((ba.out_code_id)::::text = (trn.out_code_id)::::text))" +
						" AND ((SELECT loi.list_option_item_code FROM list_option_item loi WHERE loi.dtype = 'LoiBreTransitionResult' AND loi.id = trn.result_id AND loi.deleted = false) <> ALL (ARRAY[:constOK, :constManual])))"+
						") a GROUP BY a.bah_id) c ON ((b.bah_id = c.bah_id)))) as vw_liability_batches " +
						"join ccc_organization_unit out on out.id = out_code_id\n" +
						"WHERE out.code like :pSOutCode || case when :pSCons='N' then '' else '%' end\n" +
						"and bte_side=:pSBteSide\n" +
						"and out.deleted = false\n" +
						"group by par_id_id,out_code_id,out.name) a \n" +
						"join ccc_partner as par on par.id = a.par_id_id where par.deleted = false \n" +
						"ORDER BY total desc")
				.setParameter("pSOutCode", pSOutCode)
				.setParameter("pSBteSide", pSBteSide)
				.setParameter("pSCons", pSCons)
				.setParameter("constOK", LoiBreTransitionResult.BRE_TRANSITION_RESULT_OK)
				.setParameter("constManual", LoiBreTransitionResult.BRE_TRANSITION_RESULT_MANUAL_PROCESS)
				.getResultList();


		List<Map<String,Object>> result = new LinkedList<Map<String,Object>>();
		for(Object[] queryRow : queryResult) {

			Map<String,Object> resultRow = new HashMap<String,Object>();
			resultRow.put("bah_id", queryRow[0]);
			resultRow.put("bte_name", queryRow[1]);
			resultRow.put("bte_code", queryRow[2]);
			resultRow.put("bte_type", queryRow[3]);
			resultRow.put("bte_side", queryRow[4]);
			resultRow.put("ide_code", queryRow[7]);
			resultRow.put("ide_name", queryRow[8]);
			resultRow.put("par_code", queryRow[9]);
			resultRow.put("par_name", queryRow[10]);
			resultRow.put("id_no", queryRow[11]);
			resultRow.put("cur_date", queryRow[12]);
			resultRow.put("total", queryRow[14]);
			resultRow.put("amount_in_main_cuy_no_pst", queryRow[15]);
			resultRow.put("amount_in_main_cuy", queryRow[16]);
			resultRow.put("in_liab_amount", queryRow[17]);
			result.add(resultRow);

		}

		return result;
	}

}