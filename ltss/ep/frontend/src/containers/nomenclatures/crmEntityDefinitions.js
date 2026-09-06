import React from 'react';
import { Link } from 'react-router-dom'
import { Card, Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import moment from 'moment';
moment.locale('bg')

import * as Constants from './../../static/constants';
import { resolveObjectPath, getMessageFromCode, extractColumnErrorMessage } from './../../scripts/dataUtils';
import { getLabelWithConstraints } from './entityDefinitions'

import FieldArticlePriceRateContainer from '../fields/FieldArticlePriceRateContainer'
import FieldArticleWarehouseQuantityContainer from '../fields/FieldArticleWarehouseQuantityContainer'
import FieldAttachableContainer from '../fields/FieldAttachableContainer'
import FieldMultiSelectOrEditContainer from '../fields/FieldMultiSelectOrEditContainer'
import FieldNomenclatureSelectContainer from '../fields/FieldNomenclatureSelectContainer'
import FieldSelectOrEditContainer from '../fields/FieldSelectOrEditContainer'
import FieldTextContainer from '../fields/FieldTextContainer'
import FieldTimestampContainer from '../fields/FieldTimestampContainer'

const crmEntityDefinitions = {
	loiExpenditureTypes: {
		className: "LoiExpenditureType",
		label: "Опции Тип разход",
		displayAttr: "listOptionItemName",
		columns: [
					{
						Header: "Код",
						accessor: "listOptionItemCode",
						fluidSize: 2,
						dataType: "UNIT",
					},{
						Header: "Наименование",
						accessor: "listOptionItemName",
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
	loiVatExemptionReasons: {
		className: "LoiVatExemptionReason",
		label: "Опции Причина за неначисляване на ДДС",
		displayAttr: "listOptionItemName",
		columns: [
					{
						Header: "Код",
						accessor: "listOptionItemCode",
						fluidSize: 2,
						dataType: "UNIT",
					},{
						Header: "Наименование",
						accessor: "listOptionItemName",
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
	loiLegalPersonTypes: {
		className: "LoiLegalPersonType",
		label: "Опции Тип контрагент",
		displayAttr: "listOptionItemName",
		columns: [
					{
						Header: "Код",
						accessor: "listOptionItemCode",
						fluidSize: 2,
						dataType: "UNIT",
					},{
						Header: "Наименование",
						accessor: "listOptionItemName",
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
	legalStatuses: {
		className: "LegalStatus",
		label: "Видове субекти",
		displayAttr: "name",
		columns: [
					{
						Header: "Код",
						accessor: 'code',
						fluidSize: 2,
						dataType: "UNIT",
					},{
						Header: "Наименование",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Идентификатор",
						accessor: 'foreignId',
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
	legalPersons: {
		className: "LegalPerson",
		label: "Контрагенти",
		displayAttr: "name",
		pageURL: "/legalPersons",
		icon: "user-tie",
		columns: [
					{
						Header: 'Име',
						accessor: 'name',
						fluidSize: 6,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/legalPersons",
						isRequired: true,
					}, {
						Header: 'МОЛ',
						accessor: 'mol',
						fluidSize: 6,
						dataType: "TEXT",
						isRequired: true,
					}, {
						Header: "Вид субект",
						accessor: 'legalStatus',
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "legalStatuses",
						editableColumn: true,
						isRequired: true,
					}, {
						Header: 'ЕГН',
						accessor: 'egn',
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/legalPersons",
						isRequired: true,
					}, {
						Header: 'ЕИК',
						accessor: 'eik',
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/legalPersons",
						// isRequired: true,
					}, {
						Header: 'ДДС номер',
						accessor: 'vatNumber',
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/legalPersons",
					}, {
						Header: 'САП номер',
						accessor: 'sapNumber',
						fluidSize: 3,
						dataType: "TEXT",
					}, {
						Header: "Тип",
						accessor: "legalPersonType",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "loiLegalPersonTypes",
					}, {
						Header: 'Държава',
						accessor: 'country',
						fluidSize: 6,
						dataType: "TEXT",
						isRequired: true,
					},{
						Header: 'Населено място',
						accessor: 'city',
						fluidSize: 6,
						dataType: "TEXT",
					},{
						Header: 'Седалище',
						accessor: 'address',
						fluidSize: 6,
						dataType: "TEXT",
						isRequired: true,
					},{
						Header: 'ПК',
						accessor: 'postCode',
						fluidSize: 2,
						dataType: "TEXT",
						show: true,
					},{
						Header: 'Адрес за кореспонденция',
						accessor: 'currentAddress',
						fluidSize: 6,
						dataType: "TEXT",
					},{
						Header: "Контакти на контрагента",
						accessor: "contacts",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "contacts",
						mappedBy: "person",
					},{
						accessor: "powerPlants",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "powerPlants",
						mappedBy: "owner",
//					},{
//						Header: "Клиент",
//						accessor: "customers",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "customers",
//						mappedBy: "person",
//					},{
//						Header: "Доставчик",
//						accessor: "vendors",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "vendors",
//						mappedBy: "person",
//					},{
//						Header: "Импортиран контрагент",
//						accessor: "importedLegalPerson",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedLegalPersons",
//						mappedBy: "legalPerson",
					},
					{
						accessor: "contacts",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "contacts",
						mappedBy: "person"
					}
				]
	},
	contacts: {
		className: "Contact",
		label: "Контакти",
		displayAttr: "name",
		icon: "address-card",
		aclRestrictable: true,
		columns: [
					{
						Header: "Контрагент",
						accessor: 'person',
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: 'legalPersons',
					},{
						Header: "Тип",
						accessor: 'type',
						fluidSize: 4,
						dataType: "ENTITY",
						entityType: "contactTypes",
					},{
						Header: "Име",
						accessor: 'name',
						fluidSize: 6,
						dataType: "TEXT",
					},{
						Header: "Описание",
						accessor: 'description',
						fluidSize: 12,
						dataType: "TEXT",
					},{
						Header: "Телефон",
						accessor: 'phone',
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Факс",
						accessor: 'fax',
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Email",
						accessor: 'email',
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Уеб страница",
						accessor: 'webSite',
						fluidSize: 3,
						dataType: "TEXT",
					}
				]
	},
	contactTypes: {
		className: "ContactType",
		label: "Типове контакти",
		displayAttr: "name",
		columns: [
					{
						Header: "Код",
						accessor: 'code',
						fluidSize: 2,
						dataType: "UNIT",
					},{
						Header: "Наименование",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
	legalPersonComments: {
		className: "LegalPersonComment",
		label: "Коментари за контрагента",
		icon: "comments",
		aclRestrictable: true,
		columns: [
				{
					Header: "Контрагент на коментара",
					accessor: 'person',
					fluidSize: 3,
					dataType: "ENTITY",
					entityType: "legalPersons",
				},{
					Header: "Променено на",
					accessor: 'lastModifiedDate',
					fluidSize: 3,
					dataType: "TIMESTAMP",
					Cell: (props) => <FieldTimestampContainer
						componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
					/>,
				},{
					Header: "Променил",
					accessor: 'lastModifiedBy',
					fluidSize: 3,
					dataType: "ENTITY",
					entityType: "secUsers",
					Cell: (props) => <FieldNomenclatureSelectContainer
						href={props.original && props.original._links && props.original._links.lastModifiedBy ? props.original._links.lastModifiedBy.href : undefined}
						componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
						nomenclatureKey={"secUsers"}
						editable={false}
						displayAttr={"fullName"}
						loading={props.original[Constants.PATH_FOR_LOADING]}
						onChange={(href) => {
							props.original._onChildUpdate(props.original._componentPath+'._links.lastModifiedBy.href',href);
						}}
					/>,
				},{
					Header: "Коментар",
					accessor: 'text',
					fluidSize: 12,
					dataType: "TEXTAREA",
				}
			]
	},
//	directionCategories: {
//		className: "DirectionCategory",
//		label: "Направление",
//		displayAttr: "name",
//		columns: [
//					{
//						Header: "Код",
//						accessor: 'code',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Наименование",
//						accessor: 'name',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}
//				]
//	},
//	areaCategories: {
//		className: "AreaCategory",
//		label: "Район",
//		displayAttr: "name",
//		columns: [
//					{
//						Header: "Код",
//						accessor: 'code',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Наименование",
//						accessor: 'name',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}
//				]
//	},
//	businessCategories: {
//		className: "BusinessCategory",
//		label: "Вид дейност",
//		displayAttr: "name",
//		columns: [
//					{
//						Header: "Код",
//						accessor: 'code',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Наименование",
//						accessor: 'name',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}
//				]
//	},
//	generalCategories: {
//		className: "GeneralCategory",
//		label: "Група 5",
//		displayAttr: "name",
//		columns: [
//					{
//						Header: "Код",
//						accessor: 'code',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Наименование",
//						accessor: 'name',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}
//				]
//	},
	customers: {
		className: "Customer",
		label: "Данни за клиент",
		displayAttr: "id",
		icon: "star",
		columns: [
				{
					accessor: 'person',
					fluidSize: 4,
					dataType: "ENTITY",
					entityType: "legalPersons",
				}, {
					accessor: 'client',
					fluidSize: 2,
					dataType: "BOOLEAN",
				},{
					accessor: 'stage',
					fluidSize: 4,
					dataType: "ENTITY",
					entityType: "salesStages",
					editableColumn: true,
				},{
					accessor: 'assignedSales',
					fluidSize: 6,
					dataType: "ENTITY",
					entityType: "secUsers",
					editableColumn: true,
				},{
					accessor: 'segment',
					fluidSize: 6,
					dataType: "TEXT",
					Cell: (props) => <FieldSelectOrEditContainer
						componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} existing path in redux store where we put data
						listReportEntity={"Customer"}
						listReportAttr={"segment"}
						editable={props.original._editable}
						onChange={(href) => {}}
					/>,
					editableColumn: {
						jsonPath: "/segment",
						editFieldPanel: (_componentPath) => <Card body>
								<label className="contracts-add-form-label">Сегмент</label>
								<FieldSelectOrEditContainer
									componentPath={_componentPath+'.edit.segment'} existing path in redux store where we put data
									listReportEntity={"Customer"}
									listReportAttr={"segment"}
									editable={true}
									onChange={(href) => {}}
								/>
							</Card>
					},
				},{
					accessor: 'direction',
					fluidSize: 6,
					dataType: "ENTITY",
					entityType: "directionCategories",
				},{
					accessor: 'area',
					fluidSize: 6,
					dataType: "ENTITY",
					entityType: "areaCategories",
				},{
					accessor: 'business',
					fluidSize: 6,
					dataType: "ENTITY",
					entityType: "businessCategories",
				},{
					accessor: 'generalCategory',
					fluidSize: 6,
					dataType: "ENTITY",
					entityType: "generalCategories",
					isRequired: true,
				},{
					accessor: 'unsubscribed',
					fluidSize: 2,
					dataType: "BOOLEAN",
				},{
					accessor: 'interests',
					show: false,
					fluidSize: 2,
					dataType: "ENTITY",
					entityType: "clientInterests",
					mappedBy: "customer",
				}
			]
	},
//	salesStages: {
//		className: "SalesStage",
//		label: "Етапи на клиент",
//		displayAttr: "name",
//		columns: [
//					{
//						Header: "Код",
//						accessor: 'code',
//						fluidSize: 2,
//						dataType: "UNIT",
//					},{
//						Header: "Наименование",
//						accessor: 'name',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}
//				]
//	},
	clientInterests: {
		className: "ClientInterest",
		label: "Интереси на клиента",
		icon: "thumbs-up",
		columns: [
					{
						Header: "Клиент",
						accessor: 'customer',
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: 'customers',
					},{
						Header: "Интерес към продукт",
						accessor: 'interested',
						fluidSize: 2,
						dataType: "BOOLEAN",
						Cell: (props) => {
							if(!props.original._editable && props.value == undefined) {
								return "--няма--";
							}
							return <Button
								variant={(props.value ? "success" : "danger")}
								onClick={(e) => {if(props.original._editable) {
									props.original._onChildUpdate(props.original._componentPath+'.'+props.column._fieldRelPath,!props.value);
								}}}
								disabled={!props.original._editable}>
							<FontAwesomeIcon icon={props.value ? "thumbs-up" : "thumbs-down"}/>&nbsp;
							{props.value ? "Интересува се от" : "Не се интересува от"}
							</Button>;
						},
					},{
						Header: "Артикул",
						accessor: 'articleProduct',
						fluidSize: 4,
						dataType: "ENTITY",
						entityType: "articleProducts",
					}
				]
	},
	vendors: {
		className: "Vendor",
		label: "Данни за доставчик",
		columns: [
				{
					accessor: 'person',
					fluidSize: 4,
					dataType: "ENTITY",
					entityType: "legalPersons",
				},{
					accessor: 'vendorCategory',
					fluidSize: 4,
					dataType: "TEXT",
				},{
					accessor: 'paymentMethod',
					fluidSize: 4,
					dataType: "TEXT",
				}
			]
	},
//	offerLines: {
//		className: "OfferLine",
//		label: "Редове на офертата",
//		displayAttr: "id",
//		columns: [
//					{
//						Header: "Оферта",
//						accessor: "offerToClient",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "offerToClients",
//					}, {
//						Header: "Описание",
//						accessor: "description",
//						fluidSize: 4,
//						dataType: "TEXTAREA",
//					}, {
//						Header: "Aртикул",
//						accessor: "article",
//						fluidSize: 4,
//						dataType: "ENTITY",
//						entityType: "articles",
//					}, {
//						Header: "Мярка (съкр.)",
//						accessor: 'article.measureShort',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Складова наличност",
//						id: 'importedWarehouseStocks',
//						isCalculated: true,
//						filterable: false,
//						sortable: false,
//						fluidSize: 2,
//						dataType: "UNIT",
//						Cell: (props) => <FieldArticleWarehouseQuantityContainer
//							articleId={props.original && props.original.article ? props.original.article.id : undefined}
//							componentPath={props.original._componentPath+'.article.importedWarehouseStocks'} //existing path in redux store where we put data
//							editable={props.original._asTable ? (props.original._editable ? false : undefined) : false}
//							loading={props.original[Constants.PATH_FOR_LOADING]}
//							onChange={(href) => {props.original._onChildUpdate();}}
//						/>
//					}, {
//						Header: "Количество",
//						accessor: 'ammount',
//						fluidSize: 2,
//						dataType: "UNIT",
//						isRequired: true,
//					}, {
//						Header: "Цена от ценова листа",
//						id: 'articlePriceRate',
//						isCalculated: true,
//						filterable: false,
//						sortable: false,
//						fluidSize: 2,
//						dataType: "UNIT",
//						Cell: (props) => <FieldArticlePriceRateContainer
//							articleId={props.original && props.original.article ? props.original.article.id : undefined}
//							componentPath={props.original._componentPath+'.article.articlePriceRates'} //existing path in redux store where we put data
//							editable={props.original._asTable ? (props.original._editable ? false : undefined) : false}
//							loading={props.original[Constants.PATH_FOR_LOADING]}
//							onChange={(href) => {props.original._onChildUpdate();}}
//						/>
//					}, {
//						id: 'price',
//						Header: "Ед. цена",
//						accessor: 'price',
//						fluidSize: 2,
//						dataType: "UNIT",
//						summary: {
//							op: "sum",
//							operands: [{
//								op: "/",
//								operands: [
//									{
//										op: "*",
//										operands: [
//											"ammount",
//											{
//												op: "*",
//												operands: [
//													{
//														op: "minus",
//														operands: [{literal: "100"},"discountPercent"],
//													},
//													"price"],
//											}],
//									},
//									{literal: "100"}],
//							}],
//						},
//						isRequired: true,
//					}, {
//						id: 'discountPercent',
//						Header: "Отстъпка %",
//						accessor: 'discountPercent',
//						fluidSize: 2,
//						dataType: "UNIT",
//						summary: {
//							op: "sum",
//							operands: [{
//								op: "/",
//								operands: [
//									{
//										op: "*",
//										operands: [
//											{
//												op: "*",
//												operands: [
//													"ammount",
//													{
//														op: "*",
//														operands: [
//															{
//																op: "minus",
//																operands: [{literal: "100"},"discountPercent"],
//															},
//															"price"],
//													}],
//											},
//											{
//												op: "/",
//												operands: [
//													"offerToClient.discountPercent",
//													{literal: "100"}],
//											}],
//									},
//									{literal: "100"}],
//							}],
//						},
//					}, {
//						id: 'discountedPrice',
//						Header: 'Ед. цена с отстъпка',
//						isCalculated: true,
//						filterable: false,
//						sortable: false,
//						fluidSize: 2,
//						dataType: "UNIT",
//						Cell: (props) => {
//							const thisEntityData = resolveObjectPath(props.column._parentPath,props.original);
//							return <input className="form-control" type="text"
//										value={thisEntityData ? ((100-thisEntityData.discountPercent)/100*thisEntityData.price).toFixed(2) : "N/A"}
//										disabled={true}
//									/>;
//						},
//						summary: {
//							op: "sum",
//							operands: [{
//								op: "/",
//								operands: [
//									{
//										op: "*",
//										operands: [
//											{
//												op: "*",
//												operands: [
//													"ammount",
//													{
//														op: "*",
//														operands: [
//															{
//																op: "minus",
//																operands: [{literal: "100"},"discountPercent"],
//															},
//															"price"],
//													}],
//											},
//											{
//												op: "/",
//												operands: [
//													{
//														op: "minus",
//														operands: [{literal: "100"},"offerToClient.discountPercent"],
//													},
//													{literal: "100"}],
//											}],
//									},
//									{literal: "100"}],
//							}],
//						},
//					}, {
//						id: 'deliveryDate',
//						Header: "Срок за доставка",
//						accessor: 'deliveryDate',
//						fluidSize: 2,
//						dataType: "TEXT",
//						summary: {
//							op: "sum",
//							operands: [{
//								op: "/",
//								operands: [
//									{
//										op: "*",
//										operands: [
//											{
//												op: "*",
//												operands: [
//													{
//														op: "*",
//														operands: [
//															"ammount",
//															{
//																op: "*",
//																operands: [
//																	{
//																		op: "minus",
//																		operands: [{literal: "100"},"discountPercent"],
//																	},
//																	"price"],
//															}],
//													},
//													{
//														op: "/",
//														operands: [
//															{
//																op: "minus",
//																operands: [{literal: "100"},"offerToClient.discountPercent"],
//															},
//															{literal: "100"}],
//													}],
//											},
//											{
//												op: "/",
//												operands: [
//													"offerToClient.vatPercent",
//													{literal: "100"}],
//											}],
//									},
//									{literal: "100"}],
//							}],
//						},
//					}, {
//						id: 'total',
//						Header: 'Общо',
//						isCalculated: true,
//						filterable: false,
//						sortable: false,
//						fluidSize: 2,
//						dataType: "UNIT",
//						Cell: (props) => {
//							const thisEntityData = resolveObjectPath(props.column._parentPath,props.original);
//							return <input className="form-control" type="text"
//										value={thisEntityData ? ((100-thisEntityData.discountPercent)/100*thisEntityData.ammount*thisEntityData.price).toFixed(2) : "N/A"}
//										disabled={true}
//									/>;
//						},
//						summary: {
//							op: "sum",
//							operands: [{
//								op: "/",
//								operands: [
//									{
//										op: "*",
//										operands: [
//											{
//												op: "*",
//												operands: [
//													{
//														op: "*",
//														operands: [
//															"ammount",
//															{
//																op: "*",
//																operands: [
//																	{
//																		op: "minus",
//																		operands: [{literal: "100"},"discountPercent"],
//																	},
//																	"price"],
//															}],
//													},
//													{
//														op: "/",
//														operands: [
//															{
//																op: "minus",
//																operands: [{literal: "100"},"offerToClient.discountPercent"],
//															},
//															{literal: "100"}],
//													}],
//											},
//											{
//												op: "/",
//												operands: [
//													{
//														op: "plus",
//														operands: [{literal: "100"},"offerToClient.vatPercent"],
//													},
//													{literal: "100"}],
//											}],
//									},
//									{literal: "100"}],
//							}],
//						},
//						Footer: (columnProps) => {
//							const price = columnProps.column._summaryData && columnProps.column._summaryData["price"] ? columnProps.column._summaryData["price"].toFixed(2) : undefined;
//							const discount = columnProps.column._summaryData && columnProps.column._summaryData["discountPercent"] ? columnProps.column._summaryData["discountPercent"].toFixed(2) : undefined;
//							const sum = columnProps.column._summaryData && columnProps.column._summaryData["discountedPrice"] ? columnProps.column._summaryData["discountedPrice"].toFixed(2) : undefined;
//							const vat = columnProps.column._summaryData && columnProps.column._summaryData["deliveryDate"] ? columnProps.column._summaryData["deliveryDate"].toFixed(2) : undefined;
//							const vatPrice = columnProps.column._summaryData && columnProps.column._summaryData["total"] ? columnProps.column._summaryData["total"].toFixed(2) : undefined;
//							return <div>
//									<label className="contracts-add-form-label">Цена без доп. отстъпка:</label>{price}
//									<label className="contracts-add-form-label">Стойност на доп. отстъпка:</label>{discount}
//									<label className="contracts-add-form-label">Крайна цена без ДДС:</label>{sum}
//									<label className="contracts-add-form-label">ДДС:</label>{vat}
//									<label className="contracts-add-form-label">Крайна цена с ДДС:</label>{vatPrice}
//								</div>;
//						}
//					}, {
//						Header: "Редове на поръчки",
//						accessor: "importedOrderRows",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedOrderRows",
//						mappedBy: "offerLine",
//					}
//				]
//	},
//	offerToClients: {
//		className: "OfferToClient",
//		label: "Оферти",
//		pageURL: "/offerToClients",
//		displayAttr: "code",
//		aclRestrictable: true,
//		columns: [
//					{
//						Header: "Номер",
//						accessor: 'offerCode',
//						fluidSize: 2,
//						dataType: "TEXT",
//						isLink: true,
//						pageURL: "/offerToClients"
//					}, {
//						Header: "Ревизия",
//						accessor: "revision",
//						fluidSize: 1,
//						dataType: "UNIT",
//					}, {
//						Header: "Оригинална оферта",
//						accessor: "originalOffer",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "offerToClients",
//					}, {
//						Header: "Контрагент",
//						accessor: "person",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "legalPersons",
//						isRequired: true,
//					}, {
//						Header: "Заглавие",
//						accessor: "name",
//						fluidSize: 4,
//						dataType: "TEXT",
//						isLink: true,
//						pageURL: "/offerToClients"
//					}, {
//						Header: "Валута",
//						accessor: "currency",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "currencies",
//						isRequired: true,
//					}, {
//						Header: "Банкова сметка",
//						accessor: "bankAccount",
//						fluidSize: 4,
//						dataType: "ENTITY",
//						entityType: "bankAccounts",
//					}, {
//						Header: "Краен срок на офертата",
//						accessor: 'validToDate',
//						fluidSize: 2,
//						dataType: "DATE",
//					}, {
//						Header: "ДДС %",
//						accessor: 'vatPercent',
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Търговска отстъпка %",
//						accessor: 'discountPercent',
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Условия за отстъпка",
//						accessor: 'discountCondition',
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					}, {
//						Header: "Условия на доставка",
//						accessor: 'deliveryTerms',
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					}, {
//						Header: "Гаранционен срок",
//						accessor: "guaranteeTerms",
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					}, {
//						Header: "Забележки",
//						accessor: "notes",
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					}, {
//						Header: "Идентификатор на поръчка",
//						accessor: "foreignId",
//						fluidSize: 12,
//						dataType: "TEXT",
//					}, {
//						Header: "Хештаг етикети",
//						accessor: 'hashTags',
//						sortable: false,
//						fluidSize: 2,
//						Cell: (props) => <FieldMultiSelectOrEditContainer
//							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
//							href={props.original && props.original._links && props.original._links.hashTags ? props.original._links.hashTags.href : undefined}
//							listType="hashTags"
//							listAttr="text"
//							editable={props.original._editable}
//						/>,
//						filterToParam: (temp,curr) => {
//							//TODO filter with select from list of hashtags
//							//TODO put proper filter column name (_fieldPath?)
//							temp["OfferToClient."+curr.id + ".text"] = curr.value;
//							return temp;
//						}
//					}, {
//						Header: "Редове на офертата",
//						accessor: 'offerLines',
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "offerLines",
//						mappedBy: "offerToClient",
//					}, {
//						Header: "Поръчки на офертата",
//						accessor: "importedOrders",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedOrders",
//						mappedBy: "offer",
//					}
//				]
//	},
	mailTemplates: {
		className: "MailTemplate",
		label: "Шаблони за писма",
		displayAttr: "name",
		columns: [
					{
						Header: "Наименование",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Тема",
						accessor: 'templateSubject',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Съдържание",
						accessor: 'templateContent',
						fluidSize: 12,
						dataType: "TEXTAREA",
					}, {
						Header: "Хештаг етикети",
						accessor: 'hashTags',
						sortable: false,
						fluidSize: 2,
						Cell: (props) => <FieldMultiSelectOrEditContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							href={props.original && props.original._links && props.original._links.hashTags ? props.original._links.hashTags.href : undefined}
							listType="hashTags"
							listAttr="text"
							editable={props.original._editable}
						/>,
						filterToParam: (temp,curr) => {
							//TODO filter with select from list of hashtags
							//TODO put proper filter column name (_fieldPath?)
							temp["MailTemplate."+curr.id + ".text"] = curr.value;
							return temp;
						}
					},{
						accessor: 'templateToRecipient',
						fluidSize: 2,
						dataType: "TEXT",
					}, {
						accessor: 'templateCode',
						fluidSize: 2,
						dataType: "UNIT",
					}
				]
	},
	mailAccounts: {
		className: "MailAccount",
		label: "Настройки на поща",
		displayAttr: "name",
		columns: [
					{
						Header: "Наименование",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Сървър IMAP",
						accessor: 'imapHost',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Сървър SMTP",
						accessor: 'smtpHost',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Потребител",
						accessor: 'username',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Парола",
						accessor: 'password',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Интервал на проверка",
						accessor: 'delay',
						fluidSize: 2,
						dataType: "UNIT",
						isRequired: true,
					},{
						Header: "Папка входящи",
						accessor: 'inboxFolder',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Папка изходящи",
						accessor: 'sentFolder',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Папка архив",
						accessor: 'archiveFolder',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Изтрива писмата?",
						accessor: 'deleteMail',
						fluidSize: 2,
						dataType: "BOOLEAN",
					},{
						Header: "Само непрочетените?",
						accessor: 'unseenMail',
						fluidSize: 2,
						dataType: "BOOLEAN",
					},{
						Header: "Само поща от контактите?",
						accessor: 'recognizedContact',
						fluidSize: 2,
						dataType: "BOOLEAN",
					},{
						Header: "Административна",
						accessor: 'defaultAccount',
						fluidSize: 2,
						dataType: "BOOLEAN",
					}
				]
	},
//	mailMessages: {
//		className: "MailMessage",
//		label: "Получени писма",
//		displayAttr: "mailSubject",
//		aclRestrictable: true,
//		columns: [
//					{
//						Header: "Подател",
//						accessor: 'fromAddress',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Получател",
//						accessor: 'mailToRecipient',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "replyTo",
//						accessor: 'replyTo',
//						show: false,
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Тема",
//						accessor: 'mailSubject',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Получено на",
//						accessor: 'receivedDate',
//						fluidSize: 3,
//						dataType: "TIMESTAMP",
//					},{
//						Header: "Изпратено на",
//						accessor: 'sentDate',
//						fluidSize: 3,
//						dataType: "TIMESTAMP",
//					},{
//						Header: "Headers",
//						accessor: 'headers',
//						show: false,
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					},{
//						Header: "Съдържание",
//						accessor: 'mailContent',
//						show: false, //TODO special type for such content that should be sanitized before preview
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					}, {
//						Header: "Хештаг етикети",
//						accessor: 'hashTags',
//						sortable: false,
//						fluidSize: 2,
//						Cell: (props) => <FieldMultiSelectOrEditContainer
//							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
//							href={props.original && props.original._links && props.original._links.hashTags ? props.original._links.hashTags.href : undefined}
//							listType="hashTags"
//							listAttr="text"
//							editable={props.original._editable}
//						/>,
//						filterToParam: (temp,curr) => {
//							//TODO filter with select from list of hashtags
//							//TODO put proper filter column name (_fieldPath?)
//							temp["MailMessage."+curr.id + ".text"] = curr.value;
//							return temp;
//						}
//					}
//				]
//	},
	sendMailMessages: {
		className: "SendMailMessage",
		label: "Изпратени писма",
		displayAttr: "name",
		aclRestrictable: true,
		columns: [
					{
						Header: "Наименование",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Подател",
						accessor: 'fromAccount',
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "mailAccounts",
					},{
						Header: "До адреси",
						accessor: 'sendMailToRecipient',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Получатели",
						accessor: "sendMailContacts",
						filterable: false,
						sortable: false,
						fluidSize: 4,
						Cell: (props) => <FieldMultiSelectOrEditContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							href={props.original && props.original._links && props.original._links.sendMailContacts ? props.original._links.sendMailContacts.href : undefined}
							listType="contacts"
							listAttr="email"
							editable={props.original._editable}
						/>,
					},{
						Header: "Заглавие",
						accessor: 'sendMailSubject',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Изпратено",
						accessor: 'sent',
						fluidSize: 2,
						dataType: "BOOLEAN",
					},{
						Header: "Съдържание",
						accessor: 'sendMailContent',
						fluidSize: 12,
						dataType: "TEXTAREA",
					},{
						Header: "Прикачени файлове",
						accessor: 'attachments',
						fluidSize: 2,
						filterable: false,
						sortable: false,
						Cell: (props) => <FieldMultiSelectOrEditContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							href={props.original && props.original._links && props.original._links.attachments ? props.original._links.attachments.href : undefined}
							listType="dBFiles"
							listAttr="name"
							editable={props.original._editable}
						/>,
					}, {
						Header: "Хештаг етикети",
						accessor: 'hashTags',
						sortable: false,
						fluidSize: 2,
						Cell: (props) => <FieldMultiSelectOrEditContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							href={props.original && props.original._links && props.original._links.hashTags ? props.original._links.hashTags.href : undefined}
							listType="hashTags"
							listAttr="text"
							editable={props.original._editable}
						/>,
						filterToParam: (temp,curr) => {
							//TODO filter with select from list of hashtags
							//TODO put proper filter column name (_fieldPath?)
							temp["SendMailMessage."+curr.id + ".text"] = curr.value;
							return temp;
						}
					}
				]
	},
	legalPersonAttachments: {
		className: "LegalPersonAttachment",
		label: "Прилежащи документи на контрагента",
		icon: "folder",
		columns: [
					{
						Header: "Контрагент",
						accessor: 'person',
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "legalPersons",
					},{
						Header: "Описание",
						accessor: 'description',
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Документ",
						accessor: 'attachmentToPerson',
						fluidSize: 4,
						dataType: "ENTITY",
						entityType: "attachables",
						Cell: (props) => <FieldAttachableContainer
							href={props.original && props.original._links && props.original._links.attachmentToPerson ? props.original._links.attachmentToPerson.href : undefined}
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={props.original._editable}
							attachedToObjectData={props.original ? props.original.person : undefined}
							onChangeF={(attachable) => {
								props.original._onChildUpdate(props.original._componentPath+'._links.attachmentToPerson.href',attachable._links.self.href);
							}}
							excludeAttachmentTypes={["mailTemplates","employeeAttestations","offerToClients"]}
						/>,
					}
				]
	},
//	mailAttachments: {
//		className: "MailAttachment",
//		label: "Прилежащи документи на писмото",
//		icon: "folder",
//		columns: [
//					{
//						Header: "Писмо",
//						accessor: 'mail',
//						fluidSize: 3,
//						dataType: "ENTITY", //TODO Field component for mail
//						entityType: "mailMessages",
//						Cell: (props) => <div>{props.original && props.original._links && props.original._links.mail ? props.original._links.mail.href : undefined}</div>,
//					},{
//						Header: "Документ",
//						accessor: 'attachment',
//						fluidSize: 6,
//						dataType: "ENTITY",
//						entityType: "attachables",
//						Cell: (props) => <FieldAttachableContainer
//							href={props.original && props.original._links && props.original._links.attachment ? props.original._links.attachment.href : undefined}
//							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
//							editable={props.original._editable}
//							attachedToObjectData={props.original ? props.original.mail : undefined}
//							onChangeF={(attachable) => {
//								props.original._onChildUpdate(props.original._componentPath+'._links.attachment.href',attachable._links.self.href);
//							}}
//						/>,
//					}
//				]
//	},
	expenditures: {
		className: "Expenditure",
		label: "Разходи",
		displayAttr: "expenseDesc",
		icon: "receipt",
		pageURL: "/expenditures",
		columns: [{
				Header: 'Код',
				accessor: 'id',
				fluidSize: 1,
				dataType: "UNIT",
				width: 50,
				Cell: (props) => {
					//because of the ID column, which is common
					const parentId = resolveObjectPath(props.column._parentPath+".id",props.original);
					return <Link to={"/expenditures/"+parentId}>{props.value}</Link>;
				}
			}, {
				Header: "Дата",
				accessor: 'expenseDate',
				fluidSize: 2,
				dataType: "DATE",
				isRequired: true,
			},{
				Header: "Вид р-д",
				accessor: "expenseType",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "loiExpenditureTypes",
				isRequired: true,
			},{
				Header: "Наименование",
				accessor: 'name',
				fluidSize: 3,
				dataType: "TEXT",
				Cell: (props) => <FieldSelectOrEditContainer
					componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
					listReportEntity={"Expenditure"}
					listReportAttr={"name"}
					editable={props.original._editable}
					onChange={(href) => {}}
				/>,
				isRequired: true,
			}, {
				Header: "Описание",
				accessor: "expenseDesc",
				fluidSize: 4,
				dataType: "TEXT",
				isRequired: true,
			}, {
				Header: "За период от дата",
				accessor: 'expenseFromDate',
				fluidSize: 2,
				dataType: "DATE",
			}, {
				Header: "За период до дата",
				accessor: 'expenseToDate',
				fluidSize: 2,
				dataType: "DATE",
			},{
				Header: "Доставчик",
				accessor: "expenseVendor",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "legalPersons",
			},{
				Header: "Хештаг етикети",
				accessor: 'hashTags',
				sortable: false,
				fluidSize: 4,
				Cell: (props) => <FieldMultiSelectOrEditContainer
					componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
					href={props.original && props.original._links && props.original._links.hashTags ? props.original._links.hashTags.href : undefined}
					listType="hashTags"
					listAttr="text"
					editable={props.original._editable}
				/>,
				filterToParam: (temp,curr) => {
					//TODO filter with select from list of hashtags
					//TODO put proper filter column name (_fieldPath?)
					temp["Expenditure."+curr.id + ".text"] = curr.value;
					return temp;
				}
			}, {
				Header: "Забележки",
				accessor: "expenseNotes",
				dataType: "TEXTAREA",
			},{
				Header: "Пр-ди и р-ди",
				accessor: 'attachableRevenuesAndExpenses',
				fluidSize: 3,
				show: false,
				dataType: "ENTITY",
				entityType: "attachableRevenuesAndExpenseses",
			},
		]
	},
	incomes: {
		className: "Income",
		label: "Плащания",
		displayAttr: "name",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
				"\u{1F194}"+item.id, 
				item.name,
				item.incomeDesc,
				item.name + " " + item.incomeDesc]
			,constraints);}),
		icon: "money-bill",
		pageURL: "/incomes",
		columns: [{
				Header: 'Код',
				accessor: 'id',
				fluidSize: 1,
				dataType: "UNIT",
				width: 50,
				Cell: (props) => {
					//because of the ID column, which is common
					const parentId = resolveObjectPath(props.column._parentPath+".id",props.original);
					return <Link to={"/incomes/"+parentId}>{props.value}</Link>;
				}
			}, {
				Header: "Наименование",
				accessor: 'name',
				fluidSize: 2,
				dataType: "TEXT",
			}, {
				Header: "Хештаг етикети",
				accessor: 'hashTags',
				sortable: false,
				fluidSize: 2,
				Cell: (props) => <FieldMultiSelectOrEditContainer
					componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
					href={props.original && props.original._links && props.original._links.hashTags ? props.original._links.hashTags.href : undefined}
					listType="hashTags"
					listAttr="text"
					editable={props.original._editable}
				/>,
				filterToParam: (temp,curr) => {
					//TODO filter with select from list of hashtags
					//TODO put proper filter column name (_fieldPath?)
					temp["Income."+curr.id + ".text"] = curr.value;
					return temp;
				}
			}, {
				Header: "Описание",
				accessor: "incomeDesc",
				fluidSize: 2,
				dataType: "TEXT",
			},{
				Header: "Дата",
				accessor: 'incomeDate',
				fluidSize: 2,
				dataType: "DATE",
			},{
				Header: "Платец",
				accessor: "incomePayer",
				fluidSize: 4,
				dataType: "ENTITY",
				entityType: "legalPersons",
			},{
				Header: "Пр-ди и р-ди",
				accessor: 'attachableRevenuesAndExpenses',
				fluidSize: 3,
				show: false,
				dataType: "ENTITY",
				entityType: "attachableRevenuesAndExpenseses",
				mappedBy: "attachable",
			},
		]
	},
	invoices: {
		className: "Invoice",
		label: "Фактури",
		displayAttr: "invoiceNum",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
			(item.invoiceNum ? item.invoiceNum : "-"), 
			(item.invoiceNum ? item.invoiceNum : "-") + "/" + moment(item.invoiceDate).format("YYYYMMDD"), 
			(item.invoiceNum ? item.invoiceNum : "-") + "/" + moment(item.invoiceDate).format("YYYYMMDD") + (item.invoiceCounterParty ? ("/" + item.invoiceCounterParty.name) : "")], constraints);}),
		icon: "file-invoice",
		pageURL: "/invoices",
		columns: [
					{
						Header: 'Код',
						accessor: 'id',
						fluidSize: 1,
						dataType: "UNIT",
						width: 50,
						Cell: (props) => {
							//because of the ID column, which is common
							const parentId = resolveObjectPath(props.column._parentPath+".id",props.original);
							return <Link to={"/invoices/"+parentId}>{props.value}</Link>;
						}
					}, {
						Header: "Номер",
						accessor: "invoiceNum",
						fluidSize: 2,
						dataType: "UNIT",
						pageURL: "/invoices",
						Cell: (props) => <FieldTextContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={false}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {props.original._onChildUpdate();}}
						/>,
						isRequired: true,
					},{
						Header: "Данъчна основа",
						accessor: "taxBaseAmount",
						fluidSize: 2,
						dataType: "UNIT",
						Cell: (props) => <FieldTextContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={false}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {props.original._onChildUpdate();}}
						/>,
					}, {
						Header: "Данък",
						accessor: "taxAmount",
						fluidSize: 2,
						dataType: "UNIT",
						Cell: (props) => <FieldTextContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={false}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {props.original._onChildUpdate();}}
						/>,
					}, {
						Header: "Обща сума",
						accessor: "totalAmount",
						fluidSize: 2,
						dataType: "UNIT",
						Cell: (props) => <FieldTextContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={false}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {props.original._onChildUpdate();}}
						/>,
					}, {
						Header: "Дата",
						accessor: "invoiceDate",
						fluidSize: 2,
						dataType: "DATE",
						isRequired: true,
					},{
						Header: "Съставил",
						accessor: "issuedBy",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "secUsers",
						isRequired: true,
					}, {
						Header: "Клиент",
						accessor: "invoiceCounterParty",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "legalPersons",
						isRequired: true,
					}, {
						Header: "Код документ",
						accessor: "invoiceCode",
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Описание",
						accessor: "invoiceDescription",
						fluidSize: 2,
						dataType: "TEXT",
						isRequired: true,
					}, {
						Header: "Тип плащане",
						accessor: "paymentType",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "loiPaymentTypes",
						isRequired: true,
					}, {
						Header: "Банкова сметка",
						accessor: "bankAccount",
						fluidSize: 2,
						dataType: "FILTERED_ENTITY",
						entityType: "bankAccounts",
						optionFilter: [{where: {
								op: "equal",
								operands: ["bankAccountOwner.eik", {op: "stringLiteral", operands: [{literal: MANAGED_COMPANY_EIK}]}],
							},
						}],
						isRequired: true,
					}, {
						Header: "Валута",
						accessor: "invoiceCurrency",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "currencies",
						isRequired: true,
					}, {
						Header: "Причина за неначисляване на ДДС",
						accessor: "vatExemptionReason",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "loiVatExemptionReasons",
						isRequired: true,
					}, {
						Header: "Издадена",
						accessor: "isIssued",
						fluidSize: 2,
						dataType: "BOOLEAN",
					},{
						Header: "Резултат авт. обработка",
						accessor: "isExported",
						fluidSize: 2,
						dataType: "BOOLEAN",
					}, {
						Header: "Редове на фактурата",
						accessor: "invoiceRows",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "invoiceRows",
						mappedBy: "invoice",
					}, 
					{
						Header: "Разпределения",
						accessor: "allocationProxies",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "allocationProxies",
						mappedBy: "allocationOrigin",
					},
				]
	},
	invoiceRows: {
		className: "InvoiceRow",
		label: "Редове на фактури",
		displayAttr: "id",
		columns: [
					{
						Header: "Фактура",
						accessor: "invoice",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "invoices",
						isRequired: true,
					}, {
						Header: "Услуга",
						accessor: "article",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "articleServices",
						isRequired: true,
					}, {
						Header: "Курс",
						accessor: "transport",
						fluidSize: 2,
						dataType: "FILTERED_ENTITY",
						entityType: "transports",
						optionFilter: [{where: {
									op: "isNull",
									operands: ["invoiceRows.id"],
								},
							}],
						expandColumns: ["invoiceRows"],
					},{
						Header: "Описание",
						accessor: "invoiceRowDescription",
						fluidSize: 3,
						dataType: "TEXT",
						isRequired: true,
					}, {
						Header: "Кол-во",
						accessor: "quantity",
						fluidSize: 1,
						dataType: "UNIT",
					}, {
						Header: "Ед. цена",
						accessor: "priceRate",
						fluidSize: 2,
						dataType: "UNIT",
						isRequired: true,
					}
				]
	},
}

export default crmEntityDefinitions
