import React, {
	Component,
	PropTypes
} from 'react';
import {
	ProSidebar,
	Menu,
	MenuItem,
	SubMenu,
	SidebarHeader,
	SidebarFooter,
	SidebarContent,
} from 'react-pro-sidebar';
import 'react-pro-sidebar/dist/css/styles.css';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import * as actions from '../actions/index';
import { withRouter, Link } from 'react-router-dom';
import { withTranslation } from 'react-i18next';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import packageLockJson from '../../package-lock.json';

import CompanyLogo from '../components/icons/CompanyLogo';
import { hasPermission } from './InfoBarContainer';
import { selfieEntities } from "./selfie/constants/selfieEntities";


class NavigationContainer extends Component {
	constructor(...args) {
		super(...args);
		this.state = {
			toggled: false,
		};
	}

	render() {
		let onToggle = this.props.handleToggleSidebar;
		let permSecUser = hasPermission(this.props.currentPermissions, "ROLE_GET_secUsers");
		let permSecRole = hasPermission(this.props.currentPermissions, "ROLE_GET_secRoles");
		return (
			<ProSidebar
				image={this.props.image ? false : false}
				rtl={this.props.rtl}
				collapsed={this.props.collapsed}
				toggled={this.props.toggled}
				breakPoint="lg"
				onToggle={onToggle}
			>
				<SidebarHeader>
					<div style={{ display: "flex", justifyContent: "center", alignItems: "center", margin: '42px 10px 10px 10px', border: '2px solid #ccc', backgroundColor: "#fff", boxShadow: "0px 4px 10px rgba(0, 0, 0, 0.1)" }}>
						<CompanyLogo />
					</div>

					<div className="row"
						style={{
							padding: '24px',
							textTransform: 'uppercase',
							fontWeight: 'bold',
							fontSize: 14,
							letterSpacing: '1px',
							overflow: 'hidden',
							textOverflow: 'ellipsis',
							whiteSpace: 'nowrap',
						}}
					>
						<span className="text-align-center sublogo col-sm-12"> {this.props.t("ModuleName")} </span>
						<span className="text-align-center sublogo col-sm-12"> {this.props.t("DeveloperName")} </span>
						<span className="text-align-center sublogo col-sm-12"> {packageLockJson.version} </span>
					</div>
				</SidebarHeader>

				<SidebarContent>
					<Menu>
						{/* <MenuItem icon={<FontAwesomeIcon size="lg" icon="tachometer-alt"/>}>
						{this.props.t("TransportsReport.title_short")} <Link to="/transportsReport"/>
						</MenuItem>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="handshake"/>} suffix={<Link to="/transportOrders/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
						{this.props.t("TransportOrder._className_plural")} <Link to="/transportOrders"/>
						</MenuItem>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="road"/>} suffix={<Link to="/transports/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
						{this.props.t("Transport._className_plural")} <Link to="/transports"/>
						</MenuItem>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/invoices/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
						{this.props.t("Invoice._className_plural")} <Link to="/invoices"/>
						</MenuItem>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>} suffix={<Link to="/expenditures/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
						{this.props.t("Expenditure._className_plural")} <Link to="/expenditures"/>
						</MenuItem>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="money-bill"/>} suffix={<Link to="/incomes/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
						{this.props.t("Income._className_plural")} <Link to="/incomes"/>
						</MenuItem>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="poll"/>}>
						{this.props.t("Reports.title")} <Link to="/reports"/>
						</MenuItem>
						*/}
						<MenuItem icon={<FontAwesomeIcon icon="tachometer-alt" />}>
							{this.props.t("DashboardScreen._className")} <Link to="/dashboardScreen" />
						</MenuItem>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="user-tie" />} suffix={<Link to="/legalPersons/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus" /></Link>}>
							{this.props.t("LegalPerson._className_plural")} <Link to="/legalPersons" />
						</MenuItem>
						{/* 
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="truck"/>} suffix={<Link to="/vehicles/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
						{this.props.t("Vehicle._className_plural")} <Link to="/vehicles"/>
						</MenuItem>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="user"/>} suffix={<Link to="/employees/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
						{this.props.t("Employee._className_plural")} <Link to="/employees"/>
						</MenuItem> */}

						{/* <SubMenu title="Справки" icon={<FontAwesomeIcon size="lg" icon="receipt"/>}>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>}>
							{this.props.t("CStock._className_plural")} <Link to="/cStocks"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>}>
							{this.props.t("CReserveQuantity._className_plural")} <Link to="/cReserveQuantities"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>}>
							Справка за наличности <Link to="/reportStocks"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>}>
							Справка за наличности към дата<Link to="/reportStocksDate"/>
							</MenuItem>
						</SubMenu> */}

						{/* <SubMenu title="Заявки" icon={<FontAwesomeIcon size="lg" icon="receipt"/>}>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/offers/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("COffer._className_plural")} <Link to="/offers"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/requests/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("COffer._className_warehouse_plural")} <Link to="/requests"/>
							</MenuItem>
						</SubMenu> */}

						{/* <SubMenu title="Доставки" icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>}>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>}>
							Стоки за поръчка <Link to="/generateOrder"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>} suffix={<Link to="/orders/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("COrder._className")} <Link to="/orders"/>
							</MenuItem>
						</SubMenu>

						<SubMenu title="Приход в склад" icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>}>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>} suffix={<Link to="/cDeliveries/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("CDelivery._className_plural")} <Link to="/cDeliveries"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>}>
							{this.props.t("CSale._className_KI_plural")} <Link to="/revertSales"/>
							</MenuItem>
						</SubMenu>


						<SubMenu title="Разход на склад" icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>}>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>} suffix={<Link to="/cSales/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("CSale._className_plural")} <Link to="/cSales"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/cInvoices/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
								{this.props.t("CInvoice._className_plural")} <Link to="/cInvoices"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>} suffix={<Link to="/vendorInvoices/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("VendorInvoice._className_plural")} <Link to="/vendorInvoices"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="code"/>}>
							{this.props.t("Allocation.Allocation")} <Link to="/allocation"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>} suffix={<Link to="/transferBetweenWarehouses/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("CSale._className_PP_plural")} <Link to="/transferBetweenWarehouses"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt"/>} suffix={<Link to="/returnToVendor/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("CSale._className_DI_plural")} <Link to="/returnToVendor"/>
							</MenuItem>
						</SubMenu>

						<SubMenu title="Настройки" icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>}>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="user"/>} suffix={<Link to="/partners/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("CCcPartner._className_plural")} <Link to="/partners"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="user"/>} suffix={<Link to="/organizationUnit/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("CCcOrganizationUnit._className_plural")} <Link to="/organizationUnit"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/chartAccounts/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("FChartAccount._className_plural")} <Link to="/chartAccounts"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/deliveryGoodMap/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("CDeliveryGoodMap._className_plural")} <Link to="/deliveryGoodMap"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/cGoods/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("CGoods._className_plural")} <Link to="/cGoods"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/cPriceList/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("CPriceList._className_plural")} <Link to="/cPriceList"/>
							</MenuItem>
						</SubMenu> */}
						{/* <SubMenu title="Финанси" icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>}>
							<SubMenu title="Основни данни">
								<MenuItem>
									Сметкоплан <Link to="/"/>
								</MenuItem>
								<SubMenu title="Консолидационни сметки" >
									<MenuItem>
										Консолидационни сметки <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Консолидационни сметки - Настройки <Link to="/"/>
									</MenuItem>
								</SubMenu>
								<MenuItem suffix={<Link to="/fCtBatchTypes/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
									{this.props.t("FCtBatchType._className_plural")} <Link to="/fCtBatchTypes"/>
								</MenuItem>
								<MenuItem suffix={<Link to="/fJournalTypes/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
									{this.props.t("FJournalType._className_plural")} <Link to="/fJournalTypes"/>
								</MenuItem>
								<MenuItem suffix={<Link to="/fCtTransitionTypes/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
									{this.props.t("FCtTransitionType._className_plural")} <Link to="/fCtTransitionTypes"/>
								</MenuItem>
								<SubMenu title="Правила за осчетоводяване" >
									<MenuItem suffix={<Link to="/fCtRules/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
										{this.props.t("FCtRule._className_plural")} <Link to="/fCtRules"/>
									</MenuItem>
									<MenuItem suffix={<Link to="/fCtBatchTypeRules/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
										{this.props.t("FCtBatchTypeRule._className_plural")} <Link to="/fCtBatchTypeRules"/>
									</MenuItem>
								</SubMenu>
							</SubMenu>
							<SubMenu title="Приходно-разходни центрове">
								<MenuItem>
									ЕБК <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Дейности <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Програми <Link to="/"/>
								</MenuItem>
								<SubMenu title="Договори" >
									<MenuItem>
										Типове договори <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Договори <Link to="/"/>
									</MenuItem>
								</SubMenu>
								<MenuItem>
									Източници на финансиране <Link to="/"/>
								 </MenuItem>
								<MenuItem>
									Резерв <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Транспорт <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Групи стоки/услуги <Link to="/"/>
								</MenuItem>
							</SubMenu>
							<SubMenu title="Номенклатури">
								<SubMenu title= "Контрагенти">
										<MenuItem>
											Групи контрагенти <Link to="/"/>
										</MenuItem>
										<MenuItem>
											Контрагенти <Link to="/"/>
										</MenuItem>
										<MenuItem>
											Лица за контакти <Link to="/"/>
										</MenuItem>
								</SubMenu>
								<MenuItem suffix={<Link to="/fCtRepresentatives/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
									{this.props.t("FCtRepresentative._className_plural")} <Link to="/fCtRepresentatives"/>
								</MenuItem>
								<SubMenu title="Банки">
									<MenuItem suffix={<Link to="/cCtBanks/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
										Банки <Link to="/cCtBanks"/>
									</MenuItem>
									<MenuItem suffix={<Link to="/cCtBankAccounts/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
										Банкови сметки <Link to="/cCtBankAccounts"/>
									</MenuItem>
								</SubMenu>
								<SubMenu title="Валути">
									<MenuItem suffix={<Link to="/cCtCurrencies/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
										Валути <Link to="/cCtCurrencies"/>
									</MenuItem>
									<MenuItem suffix={<Link to="/cPmtCurrencyRates/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
										Валутни курсове <Link to="/cPmtCurrencyRates"/>
									</MenuItem>
								</SubMenu>
								<MenuItem>
									Каси <Link to="/"/>
								</MenuItem>
							</SubMenu>
							<SubMenu title="Сч. Документи">
								<MenuItem>
									Нов сч. документ <Link to="/"/>
								</MenuItem>
								<MenuItem suffix={<Link to="/fPtBatches/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
									Редактиране сч. документи <Link to="/fPtBatches"/>
								</MenuItem>
								<MenuItem>
									Потвърждаване сч. документ <Link to="/fBreTransitions"/>
								</MenuItem>
								<MenuItem>
									Движение по сметка <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Движение по контрагент <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Движение по документи с ПРЦ <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Журнал <Link to="/"/>
								</MenuItem>
							</SubMenu>
							<SubMenu title="Фактуриране">
								<MenuItem suffix={<Link to="/fCtGoods/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
									{this.props.t("FCtGood._className_plural")} <Link to="/fCtGoods"/>
								</MenuItem>
								<SubMenu title="Продажба">
									<MenuItem>
										Нова фактура <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Търсене <Link to="/"/>
									</MenuItem>
								</SubMenu>
								<MenuItem>
									ДДС файлове <Link to="/"/>
								</MenuItem>
								<SubMenu title="Справки">
									<MenuItem>
										Продажби/Доставки за период <Link to="/"/>
									</MenuItem>
								</SubMenu>
							</SubMenu>
							<SubMenu title="Лизинги">
								<MenuItem>
									Въвеждане/Редакция <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Редакция на пог. план <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Преглед <Link to="/"/>
								</MenuItem>
								<SubMenu title="Цесии">
									<MenuItem>
										Цесии - договор <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Цесии - преглед <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Цесии - плащане <Link to="/"/>
									</MenuItem>
								</SubMenu>
								<SubMenu title="Уточняване плащания">
									<MenuItem>
										Уточняване на договор <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Уточняване на задължение <Link to="/"/>
									</MenuItem>
								</SubMenu>
								<SubMenu title="Справки">
									<MenuItem>
										Справки за договори <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Справка за провизии <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Справка за задължения по пог. план <Link to="/"/>
									</MenuItem>
								</SubMenu>
								<MenuItem>
									Автоматични обработки <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Групи провизиране <Link to="/"/>
								</MenuItem>
								<SubMenu title="Настройки">
									<MenuItem>
										Настройки на документи <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Типове приложения <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Лихвени проценти <Link to="/"/>
									</MenuItem>
								</SubMenu>
							</SubMenu>
							<SubMenu title="Плащания">
								<MenuItem>
									Касов салон <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Каса <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Нов касов ордер <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Нов банков документ <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Редактиране плащане <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Редактиране БД <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Банкова книга <Link to="/"/>
								</MenuItem>
							</SubMenu>
							<SubMenu title="Бюджет">
								<MenuItem>
									Нов бюджет <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Планиране <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Натурални показатели <Link to="/"/>
								</MenuItem>
							</SubMenu>
							<SubMenu title="Други">
								<MenuItem>
									Годишно приключване <Link to="/"/>
								</MenuItem>
							</SubMenu>
							<SubMenu title="Справки">
								<SubMenu title="Оборотни ведомости">
									<MenuItem>
										ОВ за период <Link to="/financeReportCoaBalanceAllByMonth"/>
									</MenuItem>
									<MenuItem>
										ОВ по дати <Link to="/"/>
									</MenuItem>
									<MenuItem>
										ОВ по сметка <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Обороти начално салдо <Link to="/"/>
									</MenuItem>
								</SubMenu>
								<MenuItem>
									Оборотна ведомост по контрагенти <Link to="/financeReportParBalanceByMonths"/>
								</MenuItem>
								<MenuItem>
									ОВ по контрагенти и орг.структура <Link to="/"/>
								</MenuItem>
								<SubMenu title="Оборотна ведомост по ПРЦ">
									<MenuItem>
										ОВ - договор <Link to="/"/>
									</MenuItem>
									<MenuItem>
										ОВ - транспорт <Link to="/"/>
									</MenuItem>
									<MenuItem>
										ОВ - източници на финансиране <Link to="/"/>
									</MenuItem>
									<MenuItem>
										ОВ - параграфи от ЕБК <Link to="/"/>
									</MenuItem>
									<MenuItem>
										ОВ - дейности <Link to="/"/>
									</MenuItem>
									<MenuItem>
										ОВ - типове стоки <Link to="/"/>
									</MenuItem>
									<MenuItem>
										ОВ - организационни единици <Link to="/financeReportOutBalanceByMonths"/>
									</MenuItem>
									<MenuItem>
										ОВ - програми/проекти <Link to="/"/>
									</MenuItem>
									<MenuItem>
										ОВ - управленски ПРЦ <Link to="/"/>
									</MenuItem>
								</SubMenu>
								<MenuItem>
									Конс. ОВ по орг. стр. <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Движение по сметка и ПРЦ <Link to="/"/>
								</MenuItem>
								<SubMenu title="Просрочени">
									<MenuItem>
										Предполагаеми просрочени вземания и задължения <Link to="/"/>
									</MenuItem>
									<MenuItem>
										Просрочени вземания и задължения <Link to="/financeReportUnpaidDocsByPar"/>
									</MenuItem>
									<MenuItem>
										Просрочени вземания	и задължения по контрагент <Link to="/financeReportUnpaidDocsByParSum"/>
									</MenuItem>
								</SubMenu>
								<MenuItem>
									Главна книга <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Счетоводен баланс, ОПР, ОПП <Link to="/"/>
								</MenuItem>
							</SubMenu>
							<SubMenu title="Модули">
								<MenuItem>
									Регистрация <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Склад <Link to="/"/>
								</MenuItem>
								<MenuItem>
									Д. Активи <Link to="/"/>
								</MenuItem>
							</SubMenu>
							<SubMenu title="Служебни">
								<MenuItem>
									Настройки <Link to="/"/>
									<MenuItem>
										Парола <Link to="/"/>
									</MenuItem>
								</MenuItem>
							</SubMenu>
						</SubMenu> */}

						{/* <SubMenu title="Графици" icon={<FontAwesomeIcon size="lg" icon="receipt"/>}>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/schedules/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("Schedule._className_plural")} <Link to="/schedules"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/scheduleTimeSeries/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("ScheduleTimeSeries._className_plural")} <Link to="/scheduleTimeSeries"/>
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice"/>} suffix={<Link to="/intervals/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus"/></Link>}>
							{this.props.t("Interval._className_plural")} <Link to="/intervals"/>
							</MenuItem>
						</SubMenu> */}
						{/* <MenuItem icon={<FontAwesomeIcon size="lg" icon="file-import" />}>
							{this.props.t("UploadSchedule._className")} <Link to="/uploadSchedules" />
						</MenuItem>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice" />}>
							{this.props.t("Schedule._className_plural")} <Link to="/schedules" />
						</MenuItem>
						<SubMenu title="Регистри" icon={<FontAwesomeIcon size="lg" icon="receipt" />}>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice" />} suffix={<Link to="/powerPlantProfiles/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus" /></Link>}>
								{this.props.t("PowerPlantProfile._className_plural")} <Link to="/powerPlantProfiles" />
							</MenuItem>
							
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice" />}>
								{this.props.t("IbexEnergyDeal._className_plural")} <Link to="/ibexEnergyDeals" />
							</MenuItem>
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice" />}>
								{this.props.t("IbexPrice._className_plural")} <Link to="/ibexPrices" />
							</MenuItem>
						</SubMenu>
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice" />}>
							{this.props.t("ManufacturerProtocol._className")} <Link to="/manufacturerProtocol" />
						</MenuItem> */}

						{/* <MenuItem icon={<FontAwesomeIcon size="lg" icon="receipt" />} suffix={<Link to={`/${selfieEntities.PowerPlant.pluralCamelCase}/add`} className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus" /></Link>}>
							{this.props.t(`${selfieEntities.PowerPlant.className}._className_plural`)} <Link to={`/${selfieEntities.PowerPlant.pluralCamelCase}`} />
						</MenuItem> */}

						{/* <SubMenu title="Качване на файл" icon={<FontAwesomeIcon size="lg" icon="upload" />} >

						</SubMenu> */}
						
						<SubMenu title={this.props.t(`Selfie.${selfieEntities.Titles.AgreementTypes}`)} icon={<FontAwesomeIcon size="lg" icon="handshake" />} >
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="handshake" />} suffix={<Link to={`/${selfieEntities.AgreementType.pluralCamelCase}/add`} className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus" /></Link>} >
								{this.props.t(`${selfieEntities.AgreementType.className}._className_plural`)} <Link to={`/${selfieEntities.AgreementType.pluralCamelCase}`} />
							</MenuItem>

							<MenuItem icon={<FontAwesomeIcon size="lg" icon="handshake" />} suffix={<Link to={`/${selfieEntities.AgreementTypeMapping.pluralCamelCase}/add`} className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus" /></Link>} >
								{this.props.t(`${selfieEntities.AgreementTypeMapping.className}._className_plural`)} <Link to={`/${selfieEntities.AgreementTypeMapping.pluralCamelCase}`} />
							</MenuItem>
						</SubMenu>

						<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-signature" />} >
							{this.props.t(`AccountingPeriod._className_plural`)} <Link to="/accountingPeriod" />
						</MenuItem>

						{/* suffix={<Link to={`/${selfieEntities.ElectricityInvoice.pluralCamelCase}/add`} className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus" /></Link>} */}
						<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice" />} >
							{this.props.t(`${selfieEntities.ElectricityInvoice.className}._className_plural`)} <Link to={`/${selfieEntities.ElectricityInvoice.pluralCamelCase}`} />
						</MenuItem>

						<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-invoice" />} >
							{this.props.t(`${selfieEntities.Reference.className}._className_plural`)} <Link to={`/${selfieEntities.Reference.pluralCamelCase}`} />
						</MenuItem>

						<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-import" />} >
						{this.props.t(`${selfieEntities.Titles.Import}.titlePlural`)} <Link to="/importSelfieFiles" />
						</MenuItem>

						{/* <MenuItem icon={<FontAwesomeIcon size="lg" icon="file-import" />} >
						{this.props.t(`ElectricityInvoice.invoiceCorrection`)} <Link to="/invoiceCorrection" />
						</MenuItem> */}

						{/* <SubMenu title={this.props.t(`${selfieEntities.Titles.Import}.titlePlural`)} icon={<FontAwesomeIcon size="lg" icon="file-import" />} >

							suffix={<Link to="/importValues/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus" /></Link>}
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-import" />} >
								{this.props.t(`${selfieEntities.ImportValue.className}._className_plural`)} <Link to={`/${selfieEntities.ImportValue.pluralCamelCase}`} />
							</MenuItem>

							suffix={<Link to="/importQuantities/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus" /></Link>}
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-import" />} >
								{this.props.t(`${selfieEntities.ImportQuantity.className}._className_plural`)} <Link to={`/${selfieEntities.ImportQuantity.pluralCamelCase}`} />
							</MenuItem>

							suffix={<Link to="/importValueAndQuantities/add" className="pro-sidebar-suffix-btn"><FontAwesomeIcon size="lg" icon="plus" /></Link>}
							<MenuItem icon={<FontAwesomeIcon size="lg" icon="file-import" />} >
								{this.props.t(`${selfieEntities.ImportValueAndQuantity.className}._className_plural`)} <Link to={`/${selfieEntities.ImportValueAndQuantity.pluralCamelCase}`} />
							</MenuItem>
						</SubMenu> */}

					</Menu>
				</SidebarContent>

				<SidebarFooter style={{ textAlign: 'center' }}>

					{permSecUser ?
						<Menu>
							<SubMenu title={this.props.t("Settings")} icon={<FontAwesomeIcon size="lg" icon="folder" />}>
								<MenuItem icon={<FontAwesomeIcon size="lg" icon="folder" />} >
									{this.props.t("Import.title")} <Link to="/import" />
								</MenuItem>
								<MenuItem icon={<FontAwesomeIcon size="lg" icon="folder" />} >
									{this.props.t("Nomenclatures")} <Link to="/nomenclatures" />
								</MenuItem>
								<MenuItem icon={<FontAwesomeIcon size="lg" icon="user" />} >
									{this.props.t("SecUser._className_plural")} <Link to="/secUsers" />
								</MenuItem>
								<MenuItem icon={<FontAwesomeIcon size="lg" icon="user" />} >
									{this.props.t("SecRole._className_plural")} <Link to="/secRoles" />
								</MenuItem>
							</SubMenu>
						</Menu>
						: ''
					}

					{/* {permSecUser ?
						<div className="sidebar-btn-wrapper" style={{ padding: '20px 24px', }}>
							<Link to="/import">
								<FontAwesomeIcon size="lg" icon="folder" />
								<span> {this.props.t("Import.title")}</span>
							</Link>
						</div>
						: ""
					} */}
					{/* {permSecUser ?
						<div className="sidebar-btn-wrapper" style={{ padding: '20px 24px', }}>
							<Link to="/nomenclatures">
								<FontAwesomeIcon size="lg" icon="folder" />
								<span> {this.props.t("Nomenclatures")}</span>
							</Link>
						</div>
						: ""
					} */}
					{/* {permSecUser ?
						<div className="sidebar-btn-wrapper" style={{ padding: '20px 24px', }}>
							<Link to="/secUsers">
								<FontAwesomeIcon size="lg" icon="user" />
								<span> {this.props.t("SecUser._className_plural")}</span>
							</Link>
						</div>
						: ""
					} */}
					{/* {permSecRole ?
						<div className="sidebar-btn-wrapper" style={{ padding: '20px 24px', }}>
							<Link to="/secRoles">
								<FontAwesomeIcon size="lg" icon="user" />
								<span> {this.props.t("SecRole._className_plural")}</span>
							</Link>
						</div>
						: ""
					} */}
				</SidebarFooter>
			</ProSidebar>
		);
	}
}

function mapStateToProps(state) {
	// eslint-disable-line no-unused-vars
	/* Populated by react-webpack-redux:reducer */
	const notificationCount = (state.rest.notifications instanceof Array ? state.rest.notifications : []).filter(elem => !elem.isChecked).length;
	return {
		home: state.home,
		auth: state.auth,
		currentPermissions: state.rest.currentPermissions,
		notificationCount: notificationCount,
	};
}
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(actions, dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(NavigationContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
