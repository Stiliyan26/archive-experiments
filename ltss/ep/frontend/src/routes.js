import React, { useState, Suspense, lazy } from 'react';
import ReactDOM from 'react-dom';
import { AppContainer } from 'react-hot-loader';
import { Provider } from 'react-redux';
import { Router, Route, Switch, Redirect } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useTranslation } from 'react-i18next';

import history from './scripts/history';

import NavigationContainer from './containers/NavigationContainer';
import LoginContainer from './containers/LoginContainer';
import InfoBarContainer from './containers/InfoBarContainer';
import PageViewSecUserContainer from './containers/PageViewSecUserContainer';
import { selfieEntities } from './containers/selfie/constants/selfieEntities';

const PageTableElectricityInvoiceContainer = lazy(() => import('./containers/selfie/electricityInvoiceRelated/PageTableElectricityInvoiceContainer'));
const PageTableElectricityInvoiceReferencesContainer = lazy(() => import('./containers/selfie/referencesRelated/PageTableElectricityInvoiceReferencesContainer'));
const PageTableAgreementTypeContainer = lazy(() => import('./containers/selfie/agreementTypeRelated/PageTableAgreementTypeContainer'));
const PageTableAgreementTypeMappingContainer = lazy(() => import('./containers/selfie/agreementTypeMappingRelated/PageTableAgreementTypeMappingContainer'));
const PageTableImportValueContainer = lazy(() => import('./containers/selfie/importValueRelated/PageTableImportValueContainer'));
const PageTableImportQuantityContainer = lazy(() => import('./containers/selfie/importQuantityRelated/PageTableImportQuantityContainer'));
const PageTableImportValueAndQuantityContainer = lazy(() => import('./containers/selfie/importValueAndQuantityRelated/PageTableImportValueAndQuantityContainer'));


const PageNomenclaturesContainer = lazy(() => import('./containers/PageNomenclaturesContainer'));
const PageNotificationsContainer = lazy(() => import('./containers/PageNotificationsContainer'));
const PageTransportsReportContainer = lazy(() => import('./containers/transport/PageTransportsReportContainer'));
const PageSalesOpportunitiesContainer = lazy(() => import('./containers/crm/PageSalesOpportunitiesContainer'));
const PageViewLegalPersonContainer = lazy(() => import('./containers/crm/PageViewLegalPersonContainer'));
const PageViewTaskContainer = lazy(() => import('./containers/tasks/PageViewTaskContainer'));
const PageKanbanTaskAssigned = lazy(() => import('./containers/tasks/PageKanbanTaskAssigned'));
const PageViewOfferToClientContainer = lazy(() => import('./containers/crm/PageViewOfferToClientContainer'));
const PageWasteContainer = lazy(() => import('./containers/waste/PageWasteContainer'));
const PageCalendarContainer = lazy(() => import('./containers/PageCalendarContainer'));
const PageTrainingsContainer = lazy(() => import('./containers/employee/PageTrainingsContainer'));
const PageKnowledgeContainer = lazy(() => import('./containers/employee/PageKnowledgeContainer'));
const PageViewEmployeeAttestationContainer = lazy(() => import('./containers/employee/PageViewEmployeeAttestationContainer'));
const PageViewVehicleContainer = lazy(() => import('./containers/transport/PageViewVehicleContainer'));
const PageViewExpenditureContainer = lazy(() => import('./containers/crm/PageViewExpenditureContainer'));
const PageViewIncomeContainer = lazy(() => import('./containers/crm/PageViewIncomeContainer'));
const PageViewVehicleFuelReportContainer = lazy(() => import('./containers/transport/PageViewVehicleFuelReportContainer'));
const PageViewEmployeeContainer = lazy(() => import('./containers/employee/PageViewEmployeeContainer'));
const PageViewTransportOrderContainer = lazy(() => import('./containers/transport/PageViewTransportOrderContainer'));
const PageViewTransportContainer = lazy(() => import('./containers/transport/PageViewTransportContainer'));
const PageViewInvoiceContainer = lazy(() => import('./containers/crm/PageViewInvoiceContainer'));
const PageAllocationContainer = lazy(() => import('./containers/PageAllocationContainer'));
const PageTableEntityContainer = lazy(() => import('./containers/PageTableEntityContainer'));
const PageTableInvoicesContainer = lazy(() => import('./containers/crm/PageTableInvoicesContainer'));
const PageViewSecRoleContainer = lazy(() => import('./containers/PageViewSecRoleContainer'));

const PageViewOrganizationUnitContainer = lazy(() => import('./containers/finance/PageViewOrganizationUnitContainer'));
const PageViewChartAccountContainer = lazy(() => import('./containers/finance/accounting/PageViewChartAccountContainer'));
const PageViewPartnerContainer = lazy(() => import('./containers/finance/PageViewPartnerContainer'));
const PageViewOfferContainer = lazy(() => import('./containers/finance/PageViewOfferContainer'));
const PageViewDeliveryGoodMapContainer = lazy(() => import('./containers/finance/PageViewDeliveryGoodMapContainer'));
const PageViewRequestContainer = lazy(() => import('./containers/finance/PageViewRequestContainer'));
const PageTableRequestEntityContainer = lazy(() => import('./containers/finance/PageTableRequestEntityContainer'));
const PageViewOrderContainer = lazy(() => import('./containers/finance/PageViewOrderContainer'));
const PageReportGoodsToOrderContainer = lazy(() => import('./containers/finance/PageReportGoodsToOrderContainer'));
const PageReportStocksContainer = lazy(() => import('./containers/finance/PageReportStocksContainer'));
const PageReportStocksDateContainer = lazy(() => import('./containers/finance/PageReportStocksDateContainer'));
const PageGenerateOrderContainer = lazy(() => import('./containers/finance/PageGenerateOrderContainer'));
const PageViewCDeliveryContainer = lazy(() => import('./containers/finance/PageViewCDeliveryContainer'));
const PageViewCSaleContainer = lazy(() => import('./containers/finance/PageViewCSaleContainer'));
const PageViewRevertSaleContainer = lazy(() => import('./containers/finance/PageViewRevertSaleContainer'));
const PageViewCGoodsContainer = lazy(() => import('./containers/finance/PageViewCGoodsContainer'));
const PageViewCPriceListContainer = lazy(() => import('./containers/finance/PageViewCPriceListContainer'));
const PageViewCStockContainer = lazy(() => import('./containers/finance/PageViewCStockContainer'));
const PageViewTransferBetweenWarehousesContainer = lazy(() => import('./containers/finance/PageViewTransferBetweenWarehousesContainer'));
const PageViewReturnToVendorContainer = lazy(() => import('./containers/finance/PageViewReturnToVendorContainer'));
const PageViewVendorInvoiceContainer = lazy(() => import('./containers/finance/PageViewVendorInvoiceContainer'));
const PageViewCtBatchTypeContainer = lazy(() => import('./containers/finance/accounting/PageViewCtBatchTypeContainer'));
const PageViewJournalTypeContainer = lazy(() => import('./containers/finance/accounting/PageViewJournalTypeContainer'));
const PageViewPtBatchContainer = lazy(() => import('./containers/finance/accounting/PageViewPtBatchContainer'));
const PageViewCCtBankContainer = lazy(() => import('./containers/finance/PageViewCCtBankContainer'));
const PageViewCCtBankAccountContainer = lazy(() => import('./containers/finance/PageViewCCtBankAccountContainer'));
const PageViewCCtCurrencyContainer = lazy(() => import('./containers/finance/PageViewCCtCurrencyContainer'));
const PageViewCtTransitionTypeContainer = lazy(() => import('./containers/finance/accounting/PageViewCtTransitionTypeContainer'));
const PageViewCtRepresentativeContainer = lazy(() => import('./containers/finance/accounting/PageViewCtRepresentativeContainer'));
const PageViewCtRuleContainer = lazy(() => import('./containers/finance/accounting/PageViewCtRuleContainer'));
const PageViewCtBatchTypeRuleContainer = lazy(() => import('./containers/finance/accounting/PageViewCtBatchTypeRuleContainer'));
const PageViewCtGoodContainer = lazy(() => import('./containers/finance/accounting/PageViewCtGoodContainer'));
const PageViewCPmtCurrencyRateContainer = lazy(() => import('./containers/finance/PageViewCPmtCurrencyRateContainer'));
const PageViewFBreTransitionContainer = lazy(() => import('./containers/finance/accounting/PageViewFBreTransitionContainer'));
const PageTableFBreTransitionContainer = lazy(() => import('./containers/finance/accounting/PageTableFBreTransitionContainer'));
const PageViewFPtJournalContainer = lazy(() => import('./containers/finance/accounting/PageViewFPtJournalContainer'));

const PageReportCoaBalanceAllByMonthContainer = lazy(() => import('./containers/finance/accounting/PageReportCoaBalanceAllByMonthContainer'));
const PageReportParBalanceByMonthsContainer = lazy(() => import('./containers/finance/accounting/PageReportParBalanceByMonthsContainer'));
const PageReportOutBalanceByMonthsContainer = lazy(() => import('./containers/finance/accounting/PageReportOutBalanceByMonthsContainer'));
const PageReportUnpaidDocsByParContainer = lazy(() => import('./containers/finance/accounting/PageReportUnpaidDocsByParContainer'));
const PageReportUnpaidDocsByParSumContainer = lazy(() => import('./containers/finance/accounting/PageReportUnpaidDocsByParSumContainer'));
const PageViewCInvoiceContainer = lazy(() => import('./containers/finance/PageViewCInvoiceContainer'));
const PageTableCInvoiceContainer = lazy(() => import('./containers/finance/PageTableCInvoiceContainer'));

const PageReportsContainer = lazy(() => import('./containers/PageReportsContainer'));
const PageImportContainer = lazy(() => import('./containers/importData/PageImportContainer'));

const PageViewScheduleContainer = lazy(() => import('./containers/nepal/PageViewScheduleContainer'));
const PageViewScheduleTimeSeriesContainer = lazy(() => import('./containers/nepal/PageViewScheduleTimeSeriesContainer'));
const PageViewIntervalContainer = lazy(() => import('./containers/nepal/PageViewIntervalContainer'));
const PageViewPowerPlantProfileContainer = lazy(() => import('./containers/nepal/PageViewPowerPlantProfileContainer'));
const PageViewPowerPlantContainer = lazy(() => import('./containers/nepal/PageViewPowerPlantContainer'));
const PageViewQuarterOfHourContainer = lazy(() => import('./containers/nepal/PageViewQuarterOfHourContainer'));
const PageTableScheduleContainer = lazy(() => import('./containers/nepal/PageTableScheduleContainer'));
const PageViewUploadScheduleContainer = lazy(() => import('./containers/nepal/PageViewUploadScheduleContainer'));
const PageViewManufacturerProtocolContainer = lazy(() => import('./containers/nepal/PageViewManufacturerProtocolContainer'));
const PageViewIbexEnergyDealContainer = lazy(() => import('./containers/nepal/PageViewIbexEnergyDealContainer'));
const PageViewDashboardScreenContainer = lazy(() => import('./containers/nepal/dashboardScreen/PageViewDashboardScreenConrainer'));

const PageViewElectricityInvoiceContainer = lazy(() => import('./containers/selfie/electricityInvoiceRelated/PageViewElectricityInvoiceContainer'));
const PageViewAgreementTypeContainer = lazy(() => import('./containers/selfie/agreementTypeRelated/PageViewAgreementTypeContainer'));
const PageViewAgreementTypeMappingContainer = lazy(() => import('./containers/selfie/agreementTypeMappingRelated/PageViewAgreementTypeMappingContainer'));
const PageViewImportValueContainer = lazy(() => import('./containers/selfie/importValueRelated/PageViewImportValueContainer'));
const PageViewImportQuantityContainer = lazy(() => import('./containers/selfie/importQuantityRelated/PageViewImportQuantityContainer'));
const PageViewImportValueAndQuantityContainer = lazy(() => import('./containers/selfie/importValueAndQuantityRelated/PageViewImportValueAndQuantityContainer'));
const PageTableImportFilesContainer = lazy(() => import('./containers/selfie/PageTableImportFilesContainer'));
const PageTableAccountingPeriodContainer = lazy(() => import('./containers/selfie/PageTableAccountingPeriodContainer'));
const PageViewAgreementSelfInvoicingContainer = lazy(() => import('./containers/selfie/PageViewAgreementSelfInvoicingContainer'))
// const PageTableInvoiceCorrectionContainer = lazy(() => import('./containers/selfie/electricityInvoiceRelated/PageTableInvoiceCorrectionContainer'));


const redirectToHome = () => (<Redirect to="/home" />);
const redirect = (route) => {
	let routeMatch = route.match.path;
	return (<Redirect to={route.match.path} />)
}

function Layout({ setLocale }) {
	const { t, i18n } = useTranslation();
	const [rtl, setRtl] = useState(false);
	const [collapsed, setCollapsed] = useState(false);
	const [image, setImage] = useState(true);
	const [toggled, setToggled] = useState(false);

	const handleCollapsedChange = (checked) => {
		setCollapsed(checked);
	};

	const handleRtlChange = (checked) => {
		setRtl(checked);
		setLocale(checked ? 'ar' : 'en');
	};

	const handleImageChange = (checked) => {
		setImage(checked);
	};

	const handleToggleSidebar = (value) => {
		setToggled(value);
	};


	return (
		<Router history={history}>
			<div className={`app ${rtl ? 'rtl' : ''} ${toggled ? 'toggled' : ''}`}>
				{/* <Route component={InfoBarContainer} /> */}
				<NavigationContainer
					image={image}
					collapsed={collapsed}
					rtl={rtl}
					toggled={toggled}
					handleToggleSidebar={handleToggleSidebar}
				/>
				<div className="sidebarmain">
					<div className="btn-toggle" onClick={() => handleToggleSidebar(true)}
						style={{
							position: "fixed",
							left: 0,
							top: 0,
							zIndex: 1001,
							width: "70px",
							height: "70px",
							margin: "24px",
						}}
					>
						<FontAwesomeIcon size="2x" icon="bars" />
					</div>
					<Suspense fallback={<FontAwesomeIcon size="2x" icon="spinner" spin />}>
						<Switch>
							{ /* <Route exact path="/" render={redirect}/> */}
							<Route exact path="/" render={LoginContainer} />
							{ /* <Route path='/home' component={ Home } />

							 */ }
							{ /* <Route path='/settings' component={ Home } /> */}
							<Route path='/notifications' component={PageNotificationsContainer} />
							<Route path='/nomenclatures/:selectedNomenclature' component={PageNomenclaturesContainer} />
							<Route path='/nomenclatures' component={PageNomenclaturesContainer} />
							<Route path='/import/:selectedImportType' component={PageImportContainer} />
							<Route path='/import' component={PageImportContainer} />
							<Route path='/login' component={LoginContainer} />
							<Route path='/secUsers/:entity_id' component={PageViewSecUserContainer} />
							<Route path='/secUsers'><PageTableEntityContainer entityName={"secUsers"} title={t("SecUser._className_plural")} /></Route>
							<Route path='/secRoles/:entity_id' component={PageViewSecRoleContainer} />
							<Route path='/secRoles'><PageTableEntityContainer entityName={"secRoles"} title={t("SecRole._className_plural")} /></Route>

							{ /*<Route path='/home' component={ PageNotificationsContainer } /> */}
							{ /*
							<Route path='/home' component={ PageTransportsReportContainer } />
							<Route path='/kanbanTaskAssigned' component={ PageKanbanTaskAssigned } />
							*/ }
							<Route path='/legalPersons/:person_id' component={PageViewLegalPersonContainer} />
							<Route path='/legalPersons'>
								<PageTableEntityContainer
									entityName={"legalPersons"}
									expand={["interests"]}
									title={t("LegalPerson._className_plural")}
									hasRowSelecting={true}
									selectedRowsColumns={[{
										Header: t("CommonRecord.id"),
										accessor: 'id',
										width: 50
									}, {
										Header: t("LegalPerson.name"),
										accessor: 'name'
									}]}
									columnOverride={{
										legalStatus: { show: "hidden" },
										legalPersonType: { show: "hidden" },
									}}
								/>
							</Route>

							<Route path={`/${selfieEntities.ElectricityInvoice.pluralCamelCase}/:${selfieEntities.ElectricityInvoice.singleCamelCase}_id`} component={PageViewElectricityInvoiceContainer} />
							{/* Import PageViewElectricityInvoiceContainer after it's finished */}
							<Route path={`/${selfieEntities.ElectricityInvoice.pluralCamelCase}`} component={PageTableElectricityInvoiceContainer} />
						

							<Route path={`/${selfieEntities.Reference.pluralCamelCase}`} component={PageTableElectricityInvoiceReferencesContainer} />


							<Route path={`/${selfieEntities.AgreementType.pluralCamelCase}/:${selfieEntities.AgreementType.singleCamelCase}_id`} component={PageViewAgreementTypeContainer} />

							<Route path={`/${selfieEntities.AgreementType.pluralCamelCase}`} component={PageTableAgreementTypeContainer} />


							<Route path={`/${selfieEntities.AgreementTypeMapping.pluralCamelCase}/:${selfieEntities.AgreementTypeMapping.singleCamelCase}_id`} component={PageViewAgreementTypeMappingContainer} />

							<Route path={`/${selfieEntities.AgreementTypeMapping.pluralCamelCase}`} component={PageTableAgreementTypeMappingContainer} />


							<Route path={`/${selfieEntities.ImportValue.pluralCamelCase}/:${selfieEntities.ImportValue.singleCamelCase}_id`} component={PageViewImportValueContainer} />

							<Route path={`/${selfieEntities.ImportValue.pluralCamelCase}`} component={PageTableImportValueContainer} />


							<Route path={`/${selfieEntities.ImportQuantity.pluralCamelCase}/:${selfieEntities.ImportQuantity.singleCamelCase}_id`} component={PageViewImportQuantityContainer} />

							<Route path={`/${selfieEntities.ImportQuantity.pluralCamelCase}`} component={PageTableImportQuantityContainer} />


							<Route path={`/${selfieEntities.ImportValueAndQuantity.pluralCamelCase}/:${selfieEntities.ImportValueAndQuantity.singleCamelCase}_id`} component={PageViewImportValueAndQuantityContainer} />

							<Route path={`/${selfieEntities.ImportValueAndQuantity.pluralCamelCase}`} component={PageTableImportValueAndQuantityContainer} />

							<Route path="/importSelfieFiles" component={PageTableImportFilesContainer} />

							<Route path='/accountingPeriod' component={PageTableAccountingPeriodContainer} />

							<Route path='/agreementSelfInvoicings/:entity_id' component={PageViewAgreementSelfInvoicingContainer} />

							{/* <Route path='/invoiceCorrection' component={PageTableInvoiceCorrectionContainer} /> */}
								
							

							{ /*
							<Route path='/offerToClients/:offerToClients_id' component={ PageViewOfferToClientContainer } />
							<Route path='/offerToClients'><PageTableEntityContainer entityName={"offerToClients"} title={t("OfferToClient._className_plural")} expand={["person"]}/></Route>
							<Route path='/reports/:selectedReport' component={ PageReportsContainer } />
							<Route path='/reports' component={ PageReportsContainer } />
							<Route path='/salesOpportunities' component={ PageSalesOpportunitiesContainer } />
							<Route path='/tasks/:task_id' component={ PageViewTaskContainer } />
							<Route path='/tasks'>
								<PageTableEntityContainer
									entityName={"tasks"}
									expand={["interests","contacts"]}
									title={t("Task._className_plural")}
									hasRowSelecting={true}
									selectedRowsColumns={[{
											Header: t("CommonRecord.id"),
											accessor: 'id',
											width: 50
										}, {
											Header: t("Task.title"),
											accessor: 'title'
										}]}
								/>
							</Route>
							*/ }
							{ /*
							<Route path='/waste/:selectedWaste' component={ PageWasteContainer } />
							<Route path='/waste' component={ PageWasteContainer } />
							<Route path='/calendar' component={ PageCalendarContainer } />
							<Route path='/trainings/:selectedTraining' component={ PageTrainingsContainer } />
							<Route path='/trainings' component={ PageTrainingsContainer } />
							<Route path='/knowledge/:selectedDictionaryId' component={ PageKnowledgeContainer } />
							<Route path='/knowledge' component={ PageKnowledgeContainer } />
							<Route path='/employeeAttestations/:employeeAttestations_id' component={ PageViewEmployeeAttestationContainer } />
							<Route path='/employeeAttestations'><PageTableEntityContainer entityName={"employeeAttestations"} title={t("EmployeeAttestation._className_plural")} expand={["employee"]}/></Route>
							*/ }
							{/* <Route path='/vehicles/:entity_id' component={ PageViewVehicleContainer } />
							<Route path='/vehicles'><PageTableEntityContainer entityName={"vehicles"} title={t("Vehicle._className_plural")} /></Route>
							<Route path='/expenditures/:entity_id' component={ PageViewExpenditureContainer } />
							<Route path='/expenditures'><PageTableEntityContainer entityName={"expenditures"} title={t("Expenditure._className_plural")} /></Route>
							<Route path='/incomes/:entity_id' component={ PageViewIncomeContainer } />
							<Route path='/incomes'><PageTableEntityContainer entityName={"incomes"} title={t("Income._className_plural")} /></Route>
							<Route path='/vehicleFuelReports/:entity_id' component={ PageViewVehicleFuelReportContainer } />
							<Route path='/vehicleFuelReports'><PageTableEntityContainer entityName={"vehicleFuelReports"} title={t("VehicleFuelReport._className_plural")} /></Route>
							<Route path='/employees/:entity_id' component={ PageViewEmployeeContainer } />
							<Route path='/employees'><PageTableEntityContainer entityName={"employees"} title={t("Employee._className_plural")} /></Route>
							<Route path='/transportsReport' component={ PageTransportsReportContainer } />
							<Route path='/transportOrders/:entity_id' component={ PageViewTransportOrderContainer } />
							<Route path='/transportOrders'><PageTableEntityContainer entityName={"transportOrders"} title={t("TransportOrder._className_plural")} /></Route>
							<Route path='/transports/:entity_id' component={ PageViewTransportContainer } />
							<Route path='/transports'><PageTableEntityContainer entityName={"transports"} title={t("Transport._className_plural")} /></Route>
							<Route path='/invoices/:entity_id' component={ PageViewInvoiceContainer } />
							<Route path='/invoices'><PageTableInvoicesContainer entityName={"invoices"} title={t("Invoice._className_plural")} /></Route>
							<Route path='/cInvoices/:entity_id' component={ PageViewCInvoiceContainer } />
							<Route path='/cInvoices'><PageTableCInvoiceContainer entityName={"cInvoices"} title={t("CInvoice._className_plural")} /></Route>
							<Route path='/allocation/:selected' component={ PageAllocationContainer } />
							<Route path='/allocation' component={ PageAllocationContainer } />

							<Route path='/deliveryGoodMap/:entity_id' component={ PageViewDeliveryGoodMapContainer } />
							<Route path='/deliveryGoodMap'>
								<PageTableEntityContainer
									entityName={"cDeliveryGoodMaps"}
									title={t("CDeliveryGoodMap._className_plural")}
								/>
							</Route>
							<Route path='/organizationUnit/:entity_id' component={ PageViewOrganizationUnitContainer } />
							<Route path='/organizationUnit'>
								<PageTableEntityContainer
									entityName={"cCcOrganizationUnits"}
									title={t("CCcOrganizationUnit._className_plural")}
								/>
							</Route>
							<Route path='/chartAccounts/:entity_id' component={ PageViewChartAccountContainer } />
							<Route path='/chartAccounts'>
								<PageTableEntityContainer
									entityName={"fChartAccounts"}
									title={t("FChartAccount._className_plural")}
								/>
							</Route>
							<Route path='/partners/:entity_id' component={ PageViewPartnerContainer } />
							<Route path='/partners'>
								<PageTableEntityContainer
									entityName={"cCcPartners"}
									title={t("CCcPartner._className_plural")}
								/>
							</Route>
							<Route path='/offers/:entity_id' component={ PageViewOfferContainer } />
							<Route path='/offers'>
								<PageTableEntityContainer
									entityName={"cOffers"}
									componentPath = "offersTable"
									title={t("COffer._className_plural")}
									defaultFilter={[{where: {
										op: "isNotNull",
										operands: ["partner"],
									},}]}
								/>
							</Route>
							<Route path='/requests/:entity_id' component={ PageViewRequestContainer } />
							<Route path='/requests'>
								<PageTableRequestEntityContainer
									entityName={"cOffers"}
									title={t("CRequest._className_plural")}
									componentPath={"PageTableRequests"}
								/>
							</Route>
							<Route path='/orders/:entity_id' component={ PageViewOrderContainer } />
							<Route path='/orders'>
								<PageTableEntityContainer
									entityName={"cOrders"}
									title={t("COrder._className_plural")}
									columnOverride={{finishDate:{show:false}}}
								/>
							</Route>
							<Route path='/cDeliveries/:entity_id' component={ PageViewCDeliveryContainer } />
							<Route path='/cDeliveries'>
								<PageTableEntityContainer
									entityName={"cDeliveries"}
									title={t("CDelivery._className_plural")}
								/>
							</Route>
							<Route path='/reportGoodsToOrder' component={ PageReportGoodsToOrderContainer } />
							<Route path='/reportStocks' component={ PageReportStocksContainer } />
							<Route path='/reportStocksDate' component={ PageReportStocksDateContainer } />
							<Route path='/generateOrder' component={ PageGenerateOrderContainer } />
							<Route path='/revertSales/:entity_id' component={ PageViewRevertSaleContainer } />
							<Route path='/revertSales'>
								<PageTableEntityContainer
									entityName={"cSales"}
									title={t("CSale._className_KI_plural")}
									componentPath={"PageTableRevertSales"}
									defaultFilter={[{where: {
										op: "and",
										operands: [{
											op: "isNotNull",
											operands: ["saeId"],
										},{
											op: "equal",
											operands: ["typeDoc.listOptionItemCode",{literal: 5}],
										}],
									},}]}
									columnOverride={{documentNumber: {pageURL: "/revertSales"}, placeDeals: {show: false}, saeId: {show: false}, status: {show: false}, tdtId: {show: false}, vatto: {show: false}, outId: {show: false}, oldTypeDoc: {show: false}, ofrId: {show: false}, posted: {show: false}, advanceUsed: {show: false}, cost: {show: false}, totalPayed: {show: false}, payed: {show: false}}}
								/>
							</Route>
							<Route path='/transferBetweenWarehouses/:entity_id' component={ PageViewTransferBetweenWarehousesContainer } />
							<Route path='/transferBetweenWarehouses'>
								<PageTableEntityContainer
									entityName={"cSales"}
									title={t("CSale._className_PP_plural")}
									componentPath={"PageTableTransferBetweenWarehouses"}
									defaultFilter={[{where: {
											op: "equal",
											operands: ["typeDoc.listOptionItemCode",{literal: 3}],
									},}]}
									columnOverride={{documentNumber: {pageURL: "/transferBetweenWarehouses"}, placeDeals: {show: false}, saeId: {show: false}, status: {show: false}, tdtId: {show: false}, vatto: {show: false}, outId: {show: false}, oldTypeDoc: {show: false}, ofrId: {show: false}, posted: {show: false}, advanceUsed: {show: false}, cost: {show: false}, totalPayed: {show: false}, payed: {show: false}}}
								/>
							</Route>
							<Route path='/returnToVendor/:entity_id' component={ PageViewReturnToVendorContainer } />
							<Route path='/returnToVendor'>
								<PageTableEntityContainer
									entityName={"cSales"}
									title={t("CSale._className_DI_plural")}
									componentPath={"PageTableReturnToVendorWarehouses"}
									defaultFilter={[{where: {
											op: "equal",
											operands: ["typeDoc.listOptionItemCode",{literal: 15}],
									},}]}
									columnOverride={{documentNumber: {pageURL: "/returnToVendor"},
											parId: {Header: "Доставчик"}, 
											typeDoc: {show: false}, 
											outId: {show: false}, 
											discount: {show: false}, 
											paymentType: {show: false}, 
											datePayment: {show: false}, 
											idtCode: {show: false}, 
											vat: {show: false}, 
											sum: {show: false}, 
											oblSum: {show: false}, 
											danOsnova: {show: false}, 
											endSum: {show: false}, 
											vatSum: {show: false}, 
											total: {show: false}, 
											currency: {show: false}, 
											exchangeRate: {show: false}, 
											placeDeals: {show: false}, 
											saeId: {show: false}, 
											status: {show: false}, 
											tdtId: {show: false}, 
											vatto: {show: false}, 
											oldTypeDoc: {show: false}, 
											ofrId: {show: false}, 
											posted: {show: false}, 
											advanceUsed: {show: false}, 
											cost: {show: false}, 
											totalPayed: {show: false}, 
											payed: {show: false}
									}}
								/>
							</Route>
							<Route path='/cSales/:entity_id' component={ PageViewCSaleContainer } />
							<Route path='/cSales'>
								<PageTableEntityContainer
									entityName={"cSales"}
									title={t("CSale._className_plural")}
									defaultFilter={[{where: {
											op: "equal",
											operands: ["typeDoc.listOptionItemCode",{literal: 13}],
									},}]}
									columnOverride={{placeDeals: {show: false}, saeId: {show: false}, status: {show: false}, tdtId: {show: false}, vatto: {show: false}, outId: {show: false}, oldTypeDoc: {show: false}, ofrId: {show: false}, posted: {show: false}, advanceUsed: {show: false}, cost: {show: false}, totalPayed: {show: false}, payed: {show: false}}}
								/>
							</Route>
							<Route path='/cGoods/:entity_id' component={ PageViewCGoodsContainer } />
							<Route path='/cGoods'>
								<PageTableEntityContainer
									entityName={"cGoodses"}
									title={t("CGoods._className_plural")}
								/>
							</Route>
							<Route path='/cPriceList/:entity_id' component={ PageViewCPriceListContainer } />
							<Route path='/cPriceList'>
								<PageTableEntityContainer
									entityName={"cPriceLists"}
									title={t("CPriceList._className_plural")}
								/>
							</Route>
							<Route path='/cStocks/:entity_id' component={ PageViewCStockContainer } />
							<Route path='/cStocks'>
								<PageTableEntityContainer
									entityName={"cStocks"}
									title={t("CStock._className_plural")}
								/>
							</Route>
							<Route path='/cReserveQuantities'>
								<PageTableEntityContainer
									entityName={"cReserveQuantities"}
									title={t("CReserveQuantity._className_plural")}
								/>
							</Route>
							<Route path='/vendorInvoices/:entity_id' component={ PageViewVendorInvoiceContainer } />
							<Route path='/vendorInvoices'>
								<PageTableEntityContainer
									entityName={"vendorInvoices"}
									title={t("VendorInvoice._className_plural")}
								/>
							</Route>

							<Route path='/fCtBatchTypes/:entity_id' component={ PageViewCtBatchTypeContainer } />
							<Route path='/fCtBatchTypes'>
								<PageTableEntityContainer
									entityName={"fCtBatchTypes"}
									title={t("FCtBatchType._className_plural")}
									columnOverride={{side: {show:false}}}
								/>
							</Route>
							<Route path='/fJournalTypes/:entity_id' component={ PageViewJournalTypeContainer } />
							<Route path='/fJournalTypes'>
								<PageTableEntityContainer
									entityName={"fJournalTypes"}
									title={t("FJournalType._className_plural")}
									columnOverride={{outCode: {show: false},}}
								/>
							</Route>
							<Route path='/cCtBankAccounts/:entity_id' component={ PageViewCCtBankAccountContainer } />
							<Route path='/cCtBankAccounts'>
								<PageTableEntityContainer
									entityName={"cCtBankAccounts"}
									title={t("CCtBankAccount._className_plural")}
									columnOverride={{code: {show: false},outCode: {show: false}}}
								/>
							</Route>
							<Route path='/cCtBanks/:entity_id' component={ PageViewCCtBankContainer } />
							<Route path='/cCtBanks'>
								<PageTableEntityContainer
									entityName={"cCtBanks"}
									title={t("CCtBank._className_plural")}
									columnOverride={{format: {show: false},}}
								/>
							</Route>
							<Route path='/cCtCurrencies/:entity_id' component={ PageViewCCtCurrencyContainer } />
							<Route path='/cCtCurrencies'>
								<PageTableEntityContainer
									entityName={"cCtCurrencies"}
									title={t("CCtCurrency._className_plural")}
									columnOverride={{isDefault: {show: false}}}
								/>
							</Route>
							<Route path='/fCtTransitionTypes/:entity_id' component={ PageViewCtTransitionTypeContainer } />
							<Route path='/fCtTransitionTypes'>
								<PageTableEntityContainer
									entityName={"fCtTransitionTypes"}
									title={t("FCtTransitionType._className_plural")}
								/>
							</Route>
							<Route path='/fCtRepresentatives/:entity_id' component={ PageViewCtRepresentativeContainer } />
							<Route path='/fCtRepresentatives'>
								<PageTableEntityContainer
									entityName={"fCtRepresentatives"}
									title={t("FCtRepresentative._className_plural")}
									columnOverride={{parId: {show: false}}}
								/>
							</Route>
							<Route path='/fCtRules/:entity_id' component={ PageViewCtRuleContainer } />
							<Route path='/fCtRules'>
								<PageTableEntityContainer
									entityName={"fCtRules"}
									title={t("FCtRule._className_plural")}
									columnOverride={{outCode: {show: false},}}
								/>
							</Route>
							<Route path='/fCtBatchTypeRules/:entity_id' component={ PageViewCtBatchTypeRuleContainer } />
							<Route path='/fCtBatchTypeRules'>
								<PageTableEntityContainer
									entityName={"fCtBatchTypeRules"}
									title={t("FCtBatchTypeRule._className_plural")}
									columnOverride={{dependenceId: {show: false}}}
								/>
							</Route>
							<Route path='/fCtGoods/:entity_id' component={ PageViewCtGoodContainer } />
							<Route path='/fCtGoods'>
								<PageTableEntityContainer
									entityName={"fCtGoods"}
									title={t("FCtGood._className_plural")}
									columnOverride ={{outCode: {show: false},priceAdditional: {show: false},oldId: {show: false},outMask: {show: false}}}
								/>
							</Route>
							<Route path='/fPtJournals/:entity_id' component={ PageViewFPtJournalContainer } />
							<Route path='/fPtJournals'>
								<PageTableEntityContainer
									entityName={"fPtJournals"}
									title={t("FPtJournal._className_plural")}
								/>
							</Route>
							<Route path='/fPtBatches/:entity_id' component={ PageViewPtBatchContainer } />
							<Route path='/fPtBatches'>
								<PageTableEntityContainer
									entityName={"fPtBatches"}
									title={t("FPtBatch._className_plural")}
								/>
							</Route>
							<Route path='/fBreTransitions/:entity_id' component={ PageViewFBreTransitionContainer } />
							<Route path='/fBreTransitions'>
								<PageTableFBreTransitionContainer
									entityName={"fBreTransitions"}
									title={t("FBreTransition._className_plural")}
									expand={["tteCode.fCtBatchTteTypes.bteId.code"]}
									columnOverride ={{postDate: {show: false},module: {show: false},amountOutstanding: {show: false},
										amountDo: {show: false},amountVat: {show: false},cuyCode: {show: false},cuyRate: {show: false},
										dueDate: {show: false},cost: {show: false},ideCode: {show: false},
										addRefNo: {show: false},addRefDate: {show: false},advance: {show: false}}}
								/>
							</Route>
							<Route path='/cPmtCurrencyRates/:entity_id' component={ PageViewCPmtCurrencyRateContainer } />
							<Route path='/cPmtCurrencyRates'>
								<PageTableEntityContainer
									entityName={"cPmtCurrencyRates"}
									title={t("CPmtCurrencyRate._className_plural")}
									columnOverride={{outCode: {show: false},}}
								/>
							</Route>
							
							<Route path='/financeReportCoaBalanceAllByMonth' component={ PageReportCoaBalanceAllByMonthContainer } />
							<Route path='/financeReportParBalanceByMonths' component={ PageReportParBalanceByMonthsContainer } />
							<Route path='/financeReportOutBalanceByMonths' component={ PageReportOutBalanceByMonthsContainer } />
							<Route path='/financeReportUnpaidDocsByPar' component={ PageReportUnpaidDocsByParContainer } />
							<Route path='/financeReportUnpaidDocsByParSum' component={ PageReportUnpaidDocsByParSumContainer } /> */}

							{/* NEPAL */}

							{/* <Route path='/uploadSchedules/' component={ PageViewUploadScheduleContainer } /> */}
							{/* <Route path='/schedules/:entity_id' component={PageViewScheduleContainer} /> */}
							{/* <Route path='/schedules' component={PageTableScheduleContainer}/> */}
							{/* <Route path='/scheduleTimeSeries/:entity_id' component={PageViewScheduleTimeSeriesContainer} />
							<Route path='/scheduleTimeSeries'>
								<PageTableEntityContainer
									entityName={"scheduleTimeSeries"}
									title={t("ScheduleTimeSeries._className_plural")}
								/>
							</Route> */}
							{/* <Route path='/intervals/:entity_id' component={PageViewIntervalContainer} />
							<Route path='/intervals'>
								<PageTableEntityContainer
									entityName={"intervals"}
									title={t("Interval._className_plural")}
								/>
							</Route> */}
							{/* <Route path='/powerPlantProfiles/:entity_id' component={PageViewPowerPlantProfileContainer} /> */}
							{/* <Route path='/powerPlantProfiles'>
								<PageTableEntityContainer
									entityName={"powerPlantProfiles"}
									title={t("PowerPlantProfile._className_plural")}
								/>
							</Route> */}
							<Route path='/powerPlants/:entity_id' component={PageViewPowerPlantContainer} />
							<Route path='/powerPlants'>
								<PageTableEntityContainer
									entityName={"powerPlants"}
									title={t("PowerPlant._className_plural")}
									columnOverride={{
										traderEic: { show: "hidden" },
										address: { show: "hidden" },
										contactPerson: { show: "hidden" },
										contractId: { show: "hidden" },
										contractDate: { show: "hidden" },
										annex: { show: "hidden" },
										term: { show: "hidden" },
										identificationNumber: { show: "hidden" },
										distributionNetwork: { show: "hidden" },
										value: { show: "hidden" },
										valueSec: { show: "hidden" },
										minPriceMWh: { show: "hidden" },
										grid: { show: "hidden" },
										powerPlantProfile: { show: "hidden" },
										contractStatus: { show: "hidden" },
										loiContractQuantity: { show: "hidden" },
										loiProtocolCountPerMonth: { show: "hidden" },
										loiProtocolLineCount: { show: "hidden" },
										loiContractPrice: { show: "hidden" },
										loiContractFee: { show: "hidden" }
									}}
								/>
							</Route>
							{/* <Route path='/quarterOfHours/:entity_id' component={PageViewQuarterOfHourContainer} />
							<Route path='/quarterOfHours'>
								<PageTableEntityContainer
									entityName={"quarterOfHours"}
									title={t("QuarterOfHour._className_plural")}
								/>
							</Route> */}
							{/* <Route path='/manufacturerProtocol' component={ PageViewManufacturerProtocolContainer } /> */}
							{/* <Route path='/ibexPrices'>
								<PageTableEntityContainer
									entityName={"ibexPrices"}
									title={t("IbexPrice._className_plural")}
								/>
							</Route> */}
							{/* <Route path='/ibexEnergyDeals/:entity_id' component={PageViewIbexEnergyDealContainer} /> */}
							{/* <Route path='/ibexEnergyDeals'>
								<PageTableEntityContainer
									entityName={"ibexEnergyDeals"}
									title={t("IbexEnergyDeal._className_plural")}
								/>
							</Route> */}
							<Route path='/dashboardScreen' component={PageViewDashboardScreenContainer} />

							<Route path="*" render={redirect} />
						</Switch>
					</Suspense>
				</div>
			</div>
		</Router>
	);
}

export default Layout;
