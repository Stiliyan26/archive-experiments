package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.finance.*;

import java.time.LocalDate;

import org.springframework.data.rest.core.annotation.RestResource;

public interface FCtBatchTypeRuleRepository extends CommonRepository<FCtBatchTypeRule, QFCtBatchTypeRule, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtBatchTypeRule findFirstByOutCodeAndTteIdAndRueIdAndOrderNumAndDependenceTypeAndDependenceIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtTransitionType tteId, FCtRule rueId, Integer orderNum, LoiBatchTypeRuleDependenceType dependenceType, Integer dependenceId, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtBatchTypeRule findFirstByOutCodeAndTteIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtTransitionType tteId, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtBatchTypeRule findFirstByOutCodeAndTteIdAndRueIdAndDependenceTypeAndDependenceIdAndCompanyAndDeleted(CCcOrganizationUnit outCode, FCtTransitionType tteId, FCtRule rueId, LoiBatchTypeRuleDependenceType dependenceType, Integer dependenceId, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	FCtBatchTypeRule findFirstByOutCodeAndAmountTypeAndTteIdAndRueIdAndJteIdAndCoaIdCtAndCoaIdDtAndActiveFromDateLessThanEqualAndActiveToDateGreaterThanEqualAndCompanyAndDeleted(
			CCcOrganizationUnit outCode, LoiBatchTypeRuleAmountType amountType, FCtTransitionType tteId, FCtRule rueId,
			FJournalType jteId, FChartAccount coaIdCt, FChartAccount coaIdDt, LocalDate activeFromDate, LocalDate activeToDate,
			ManagedCompany company, boolean deleted);

}
