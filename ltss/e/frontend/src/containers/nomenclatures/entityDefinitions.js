import React from 'react';
import { Link } from 'react-router-dom'
import { Card, Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import querystring from 'querystring';
import axios from 'axios';
import DatePicker from 'react-datepicker'
import 'react-datepicker/dist/react-datepicker.css';
import { Portal } from 'react-overlays'
import i18n from 'i18next';
import * as Constants from './../../static/constants';
import crmEntityDefinitions from './crmEntityDefinitions'
import employeeEntityDefinitions from './employeeEntityDefinitions'
import financeEntityDefinitions from './financeEntityDefinitions'
import taskEntityDefinitions from './taskEntityDefinitions'
import transportEntityDefinitions from './transportEntityDefinitions'
import warehouseEntityDefinitions from './warehouseEntityDefinitions'
import watoEntityDefinitions from './watoEntityDefinitions'
import nepalEntityDefinitions from './nepalEntityDefinitions';
import selfieEntityDefinitions from "./selfieEntityDefinitions";

import FieldAttachableContainer from '../fields/FieldAttachableContainer'
import FieldBooleanContainer from '../fields/FieldBooleanContainer'
import FieldDateContainer from '../fields/FieldDateContainer'
import FieldMonthContainer from '../fields/FieldMonthContainer'
import FieldMultiSelectOrEditContainer from '../fields/FieldMultiSelectOrEditContainer'
import FieldNomenclatureSelectContainer from '../fields/FieldNomenclatureSelectContainer'
import FieldSelectContainer from '../fields/FieldSelectContainer'
import FieldTextareaContainer from '../fields/FieldTextareaContainer'
import FieldTextContainer from '../fields/FieldTextContainer'
import FieldTimestampContainer from '../fields/FieldTimestampContainer'
import FieldUnitContainer from '../fields/FieldUnitContainer'


import { buildObjectPathAndAssign, resolveObjectPath, getMessageFromCode, extractColumnErrorMessage } from './../../scripts/dataUtils';
import moment from "moment/moment";


/**
 * entityDefinitions: map of entity metadata by their repository name
 *
 * className: the Java class name of the entity, needed when handling response from the builder endpoint
 * label: text put in the headers for this entity
 * icon: FontAwesome icon name for this entity
 * displayAttr: default text property of the entity to be used for displaying of the entity
 * displayFn: function to be used for displaying of the entity with entity data as parameter
 * pageURL: if this entity has a page, this should hold its URL for the purpose of creating links to the entity
 * columns: array with metadata for displaying of the entity properties in ReactTable or other components
 * aclRestrictable: shows additional button for view and editing of entity's ACL
 * creatable: if new entity can be created - should be false for abstract entities like Attachable and Article
 *
 * Header: text of the header of the column for the property
 * accessor: internal name of the property, should be the same as defined in Java entity
 * fluidSize: 1-12 relative size of the column for fluid displaying
 * show: if you want to hide the column
 * dataType: one of the types recognized in the functions below - brings many defaults
 * entityType: for dataType=='ENTITY' properties, holds the entity type (as defined in entityDefinitions) that is referenced by this property
 * optionFilter: for dataType=='ENTITY' properties, holds a function to filter the option items list of the entities
 * optionFilter: for dataType=='FILTERED_ENTITY' properties, holds an object with filters for the AXIOS call for the option items
 * expandColumns: for dataType=='FILTERED_ENTITY' properties, holds the string list of selected columns to be sent to the Builder
 * lookupColumns: for dataType=='FILTERED_ENTITY' properties, holds the array of strings for the columns to be used for filtering/search
 * pageURL: for isLink==true properties, holds the URL for the page of this entity (for the purpose of creating links to it)
 * mappedBy: for dataType=='ENTITY' properties, holds the name of the back-reference property (when there is such) for the purpose of removing its column when expanding entities
 * isLink: for dataType in 'TEXT' or 'UNIT' - show link when not editable //TODO add 'uniqueConstraints' that will be used to deduce this parameter (unique columns are links)
 * Cell: function for rendering of the cell
 * Filter: function for rendering of the filtering controls for the column
 * editableColumn: metadata specific for the functionality for editing this column for multiple selected rows in the same time
 * 	- post: settings if you want to create entities
 * 		- endpoint: endpoint for the post
 * 		- nodeField: back reference attribute of the posted entity to the current entity
 * 	- jsonPath: (if no post settings) path of the saved attribute of the entity for the PATCH
 * 	- editFieldPanel: function(currentPath) for generating of the element for editing
 * summary: define an aggregated expression that will be calculated with the report for use in a footer of the table, e.g. sum(ammount*price)
 * isAggregate: this column is a per-row aggregated expression
 * aggregation: define the aggregated (per row) expression that will be calculated for the column
 * isCalculated: not important, marks that column is a calculated column and so it doesn't have proper accessor
 * filterToParam: function(filtersObject,currentColumnFilter) that converts the filter to parameters for the endpoint call
 * isRequired: puts a red asterisk next to the label to mark it as a required field
 * isReadOnly: only display value, never editable field
 * width: absolute width for the column
 * doCalcOnChange: trigger a calculation when the user changes the value
 * onChange: function called by the inner onChange of the field
 */

const entityDefinitions = {
	commonRecords: {
		//className: "CommonRecord",
		columns: [{
			Header: 'Код',
			accessor: 'id',
			fluidSize: 1,
			dataType: "UNIT",
			isLink: true,
			width: 50,
		}, {
			Header: "Създал",
			accessor: 'createdBy',
			fluidSize: 2,
			dataType: "ENTITY",
			entityType: "secUsers",
			Cell: (props) => <div style={{display: 'inline-block'}}>{props.value ? props.value.fullName : "--няма--"}</div>,
		},{
			Header: "Дата на създаване",
			accessor: 'createdDate',
			fluidSize: 3,
			dataType: "TIMESTAMP",
		},{
			Header: "Променил",
			accessor: 'lastModifiedBy',
			fluidSize: 2,
			dataType: "ENTITY",
			entityType: "secUsers",
			Cell: (props) => <div style={{display: 'inline-block'}}>{props.value ? props.value.fullName : "--няма--"}</div>,
		},{
			Header: "Дата на промяна",
			accessor: 'lastModifiedDate',
			fluidSize: 3,
			dataType: "TIMESTAMP",
			Cell: (props) => <FieldTimestampContainer
				componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
			/>,
		}]
	},
	secUsers: {
		className: "SecUser",
		label: "Потребители",
		icon: "user",
		pageURL: "/secUsers",
		displayAttr: "fullName",
		columns: [
					{
						Header: "Потребителско име",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
						isLink: true,
						isRequired: true,
					}, {
						Header: "Име",
						accessor: 'fullName',
						fluidSize: 3,
						dataType: "TEXT",
					}, {
						Header: "Парола",
						accessor: 'password',
						fluidSize: 2,
						dataType: "TEXT",
					}, {
						Header: "Email",
						accessor: 'email',
						fluidSize: 2,
						dataType: "TEXT",
					}, {
						Header: "Код",
						accessor: 'code',
						fluidSize: 1,
						dataType: "TEXT",
					},{
						Header: "Роли",
						accessor: 'roles',
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "secUserRoles",
						mappedBy: "user",
					}
				]
	},
	secRoles: {
		className: "SecRole",
		label: "Роли",
		displayAttr: "name",
		pageURL: "/secRoles",
		columns: [
					{
						Header: "Код",
						accessor: 'code',
						fluidSize: 2,
						dataType: "UNIT",
						isLink: true,
					}, {
						Header: "Име",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Права",
						accessor: 'permissions',
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "secRolePermissions",
						mappedBy: "role",
					}
				]
	},
	secPermissions: {
		className: "SecPermission",
		label: "Права",
		displayAttr: "name",
		columns: [
					{
						Header: "Код",
						accessor: 'code',
						fluidSize: 2,
						dataType: "TEXT",
					}, {
						Header: "Име",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
	secUserRoles: {
		className: "SecUserRole",
		label: "Роли на потребител",
		columns: [
					{
						Header: "Потребител",
						accessor: 'user',
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "secUsers",
					}, {
						Header: "Роля",
						accessor: 'role',
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "secRoles",
					}
				]
	},
	secRolePermissions: {
		className: "SecRolePermission",
		label: "Права на роля",
		columns: [
					{
						Header: "Роля",
						accessor: 'role',
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "secRoles",
					}, {
						Header: "Право",
						accessor: 'permission',
						fluidSize: 4,
						dataType: "ENTITY",
						entityType: "secPermissions",
					}
				]
	},
	accessControls: {
		className: "AccessControl",
		label: "Поименен достъп",
		columns: [
					{
						Header: "Код на запис",
						accessor: 'commonRecordId',
						fluidSize: 2,
						dataType: "UNIT",
					},{
						Header: "Потребител",
						accessor: 'secUser',
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "secUsers",
					},{
						Header: "Преглед",
						accessor: 'read',
						fluidSize: 2,
						dataType: "BOOLEAN",
					},{
						Header: "Редакция",
						accessor: 'write',
						fluidSize: 2,
						dataType: "BOOLEAN",
					},{
						Header: "Администрация",
						accessor: 'administer',
						fluidSize: 2,
						dataType: "BOOLEAN",
					}
				]
	},
	// listOptionItems: {
	// 	className: "ListOptionItem",
	// 	label: "Опции",
	// 	displayAttr: "listOptionItemName",
	// 	columns: [
	// 				{
	// 					Header: "Код",
	// 					accessor: "listOptionItemCode",
	// 					fluidSize: 2,
	// 					dataType: "UNIT",
	// 				},{
	// 					Header: "Наименование",
	// 					accessor: "listOptionItemName",
	// 					fluidSize: 2,
	// 					dataType: "TEXT",
	// 				}
	// 			]
	// },
	loiPaymentTypes: {
		className: "LoiPaymentType",
		label: "Опции Тип плащане",
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
	hashTags: {
		className: "HashTag",
		label: "Хештаг етикети",
		displayAttr: "text",
		columns: [
					{
						Header: "Текст",
						accessor: 'text',
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
/*	articles: {
		className: "Article",
		label: "Артикули",
		displayAttr: "name",
		creatable: false,
		columns: [
					{
						Header: "Наименование",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Идентификатор",
						accessor: 'foreignId',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Идентификатор на мярката",
						accessor: 'measureForeignId',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Мярка",
						accessor: 'measure',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Мярка (съкр.)",
						accessor: 'measureShort',
						fluidSize: 2,
						dataType: "TEXT",
					},
					{
						Header: "Цена",
						accessor: 'articlePriceRates',
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "articlePriceRates",
						mappedBy: "article",
					},
				]
	},
*/
// 	articleServices: {
// 		className: "ArticleService",
// 		label: "Услуги",
// 		displayAttr: "name",
// 		columns: [
// 					{
// 						Header: "Наименование",
// 						accessor: 'name',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Идентификатор",
// 						accessor: 'foreignId',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Идентификатор на мярката",
// 						accessor: 'measureForeignId',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Мярка",
// 						accessor: 'measure',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Мярка (съкр.)",
// 						accessor: 'measureShort',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					}
// 				]
// 	},
// 	articleProducts: {
// 		className: "ArticleProduct",
// 		label: "Продукти",
// 		displayAttr: "name",
// 		columns: [
// 					{
// 						Header: "Наименование",
// 						accessor: 'name',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Идентификатор",
// 						accessor: 'foreignId',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "predNom",
// 						accessor: 'predNom',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "nomNom",
// 						accessor: 'nomNom',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Идентификатор на мярката",
// 						accessor: 'measureForeignId',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Мярка",
// 						accessor: 'measure',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Мярка (съкр.)",
// 						accessor: 'measureShort',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					}
// 				]
// 	},
	currencies: {
		className: "Currency",
		label: "Валута",
		displayAttr: "name",
		columns: [
					{
						Header: "Наименование",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Идентификатор",
						accessor: 'foreignId',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Обменен курс",
						accessor: 'articlePriceRates',
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "articlePriceRates",
						mappedBy: "article",
					}
				]
	},
// 	articlePriceRates: {
// 		className: "ArticlePriceRate",
// 		label: "Цени на артикули",
// 		columns: [
// 					{
// 						Header: "Доставчик",
// 						accessor: 'vendor',
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Артикул",
// 						accessor: 'article',
// 						fluidSize: 4,
// 						dataType: "ENTITY",
// 						entityType: "articles",
// 						isRequired: true,
// 					},{
// 						Header: "Цена",
// 						accessor: 'price',
// 						fluidSize: 2,
// 						dataType: "UNIT",
// 						isRequired: true,
// 					},{
// 						Header: "Валута",
// 						accessor: 'currency',
// 						fluidSize: 4,
// 						dataType: "ENTITY",
// 						entityType: "currencies",
// 						isRequired: true,
// 					},{
// 						Header: "Валидна от",
// 						accessor: 'validFromDate',
// 						fluidSize: 3,
// 						dataType: "DATE",
// 						isRequired: true,
// 					},{
// 						Header: "Валидна до",
// 						accessor: 'validToDate',
// 						fluidSize: 3,
// 						dataType: "DATE",
// 						isRequired: true,
// 					}
// 				]
// 	},
/*	attachables: {
		creatable: false,
		className: "Attachable",
		label: "Документи",
		displayAttr: "name",
		columns: [
					{
						Header: "Наименование",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					},{
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
							temp["Attachable."+curr.id + ".text"] = curr.value;
							return temp;
						}
//					},{
//						Header: "Документ",
//						accessor: 'taskAttachments',
//						isCalculated: true,
//						filterable: false,
//						sortable: false,
//						id: 'attachment',
//						fluidSize: 4,
//						dataType: "ENTITY",
//						entityType: "attachables",
//						Cell: (props) => <FieldAttachableContainer
//							href={props.original && props.original._links && props.original._links.self ? props.original._links.self.href : undefined}
//							componentPath={props.original._componentPath} //existing path in redux store where we put data
//							editable={false}
//							attachedToObjectData={undefined}
//							onChangeF={(attachable) => {}}
//						/>,
					},{
						Header: "Пр-ди и р-ди",
						accessor: 'attachableRevenuesAndExpenses',
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "attachableRevenuesAndExpenseses",
					},
				]
	},
*/
	dBFiles: {
		className: "DBFile",
		label: "Файлове",
		displayAttr: "name",
		aclRestrictable: true,
		columns: [
					{
						Header: "Наименование",
						accessor: 'name',
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Тип",
						accessor: 'contentType',
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
							temp["DBFile."+curr.id + ".text"] = curr.value;
							return temp;
						}
					}
				]
	},
/*
	attachableRevenuesAndExpenseses: {
		className: "AttachableRevenuesAndExpenses",
		label: "Пр-ди и р-ди по документ",
		displayAttr: "id",
		aclRestrictable: true,
		columns: [
					{
						Header: "Документ",
						accessor: 'attachable',
						fluidSize: 6,
						dataType: "ENTITY",
						entityType: "attachables",
						Cell: (props) => <FieldAttachableContainer
							href={props.original && props.original._links && props.original._links.attachable ? props.original._links.attachable.href : undefined}
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={props.original._editable}
							attachedToObjectData={undefined}
							onChangeF={(attachable) => {
								props.original._onChildUpdate(props.original._componentPath+'._links.attachable.href',attachable._links.self.href);
							}}
						/>,
					},
					{
						Header: "Артикул",
						accessor: 'article',
						fluidSize: 4,
						dataType: "ENTITY",
						entityType: "articles",
					},{
						Header: "Кол-во",
						accessor: 'ammount',
						fluidSize: 2,
						dataType: "UNIT",
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
*/
// 	timeChargeRates: {
// 		className: "TimeChargeRate",
// 		label: "Цени на час",
// 		aclRestrictable: true,
// 		columns: [
// 					{
// 						Header: "Изпълнител",
// 						accessor: 'provider',
// 						fluidSize: 2,
// 						dataType: "ENTITY",
// 						entityType: "secUsers",
// 					}, {
// 						Header: "Ресурс",
// 						accessor: 'resource',
// 						fluidSize: 2,
// 						dataType: "ENTITY",
// 						entityType: "secUsers",
// 					},{
// 						Header: "Цена",
// 						accessor: 'chargeRatePerHour',
// 						fluidSize: 2,
// 						dataType: "UNIT",
// 					}, {
// 						Header: "Валута",
// 						accessor: 'currency',
// 						fluidSize: 2,
// 						dataType: "ENTITY",
// 						entityType: "currencies",
// 					}, {
// 						Header: "Валидно от дата",
// 						accessor: 'validFromDate',
// 						filterable: false,
// 						fluidSize: 2,
// 						dataType: "DATE",
// 					}, {
// 						Header: "Валидно до дата",
// 						accessor: 'validToDate',
// 						filterable: false,
// 						fluidSize: 2,
// 						dataType: "DATE",
// 					}
// 				]
// 	},
	bankAccounts: {
		className: "BankAccount",
		label: "Банкови сметки",
		displayAttr: "description",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
				(item.description && item.iban ? item.description + " " + item.iban : "-"), 
				(item.description ? item.description : "-"), 
				(item.bankName && item.currency ? item.currency.name + " " + item.bankName : "-"), 
				(item.iban ? item.iban : "-"), 
			], constraints);}),
		columns: [
					{
						Header: "Титуляр",
						accessor: "bankAccountOwner",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "legalPersons",
					}, {
						Header: "IBAN",
						accessor: "iban",
						fluidSize: 4,
						dataType: "TEXT",
					}, {
						Header: "BIC",
						accessor: "bic",
						fluidSize: 4,
						dataType: "TEXT",
					}, {
						Header: "Банка",
						accessor: "bankName",
						fluidSize: 4,
						dataType: "TEXT",
					}, {
						Header: "Валута",
						accessor: "currency",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "currencies",
					}, {
						Header: "Описание",
						accessor: "description",
						fluidSize: 4,
						dataType: "TEXT",
					}
				]
	},
// 	assets: {
// 		className: "Asset",
// 		label: "Активи",
// 		displayAttr: "assetName",
// 		creatable: false,
// 		columns: [
// 					{
// 						Header: "Наименование",
// 						accessor: "assetName",
// 						fluidSize: 2,
// 						dataType: "TEXT",
// 					}, {
// 						Header: "Прикачени документи към актива",
// 						accessor: "assetAttachments",
// 						show: false,
// 						fluidSize: 2,
// 						dataType: "ENTITY",
// 						entityType: "assetAttachments",
// 						mappedBy: "asset",
// 					}
// 				]
// 	},
// 	assetComments: {
// 		className: "AssetComment",
// 		label: "Коментари за актива",
// 		icon: "comments",
// 		aclRestrictable: true,
// 		columns: [
// 				{
// 					Header: "Актив на коментара",
// 					accessor: 'asset',
// 					fluidSize: 3,
// 					dataType: "ENTITY",
// 					entityType: "assets",
// 				},{
// 					Header: "Променено на",
// 					accessor: 'lastModifiedDate',
// 					fluidSize: 3,
// 					dataType: "TIMESTAMP",
// 					Cell: (props) => <FieldTimestampContainer
// 						componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
// 					/>,
// 				},{
// 					Header: "Променил",
// 					accessor: 'lastModifiedBy',
// 					fluidSize: 3,
// 					dataType: "ENTITY",
// 					entityType: "secUsers",
// 					Cell: (props) => <FieldNomenclatureSelectContainer
// 						href={props.original && props.original._links && props.original._links.lastModifiedBy ? props.original._links.lastModifiedBy.href : undefined}
// 						componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
// 						nomenclatureKey={"secUsers"}
// 						editable={false}
// 						displayAttr={"fullName"}
// 						loading={props.original[Constants.PATH_FOR_LOADING]}
// 						onChange={(href) => {
// 							props.original._onChildUpdate(props.original._componentPath+'._links.lastModifiedBy.href',href);
// 						}}
// 					/>,
// 				},{
// 					Header: "Коментар",
// 					accessor: 'text',
// 					fluidSize: 12,
// 					dataType: "TEXTAREA",
// 				}
// 			]
// 	},
// 	assetAttachments: {
// 		className: "AssetAttachment",
// 		label: "Прилежащи документи на актива",
// 		icon: "folder",
// 		columns: [
// 					{
// 						Header: "Актив",
// 						accessor: 'asset',
// 						fluidSize: 3,
// 						dataType: "ENTITY",
// 						entityType: "assets",
// 					},{
// 						Header: "Описание",
// 						accessor: 'description',
// 						fluidSize: 3,
// 						dataType: "TEXT",
// 					},{
// 						Header: "Документ",
// 						accessor: 'attachmentToAsset',
// 						fluidSize: 4,
// 						dataType: "ENTITY",
// 						entityType: "attachables",
// 						Cell: (props) => <FieldAttachableContainer
// 							href={props.original && props.original._links && props.original._links.attachmentToAsset ? props.original._links.attachmentToAsset.href : undefined}
// 							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
// 							editable={props.original._editable}
// 							attachedToObjectData={props.original ? props.original.asset : undefined}
// 							onChangeF={(attachable) => {
// 								props.original._onChildUpdate(props.original._componentPath+'._links.attachmentToAsset.href',attachable._links.self.href);
// 							}}
// 							excludeAttachmentTypes={["mailTemplates"]}
// 						/>,
// 					}
// 				]
// 	},
	allocationTypes: {
		className: "AllocationType",
		label: "Вид Разпределяне",
		displayAttr: "name",
		columns: [
					{
						Header: "Име",
						accessor: "name",
						fluidSize: 2,
						dataType: "TEXT",
					}, {
						Header: "Източници за разпределяне",
						accessor: "producer",
						fluidSize: 5,
						dataType: "TEXT",
					}, {
						Header: "Разпределяне към (получатели)",
						accessor: "consumer",
						fluidSize: 5,
						dataType: "TEXT",
					}
				]
	},
	allocationOrigins: {
		className: "AllocationOrigin",
		displayAttr: "id",
		columns: [
					{
						accessor: "id",
						fluidSize: 1,
						dataType: "UNIT",
					},
				]
	},
	allocationProxies: {
		className: "AllocationProxy",
		displayAttr: "id",
		columns: [
					{
						accessor: "allocationOrigin",
						fluidSize: 3,
						dataType: "FILTERED_ENTITY",
						entityType: "allocationOrigins",
					},
					{
						accessor: "allocationType",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "allocationTypes",
					},
					{
						accessor: "quantityToAllocate",
						fluidSize: 1,
						dataType: "UNIT",
					},
					{
						accessor: "totalAllocatedQuantity",
						fluidSize: 1,
						dataType: "UNIT",
					},
					{
						accessor: "fullyAllocated",
						fluidSize: 1,
						dataType: "BOOLEAN",
					},
				]
	},
	allocationRecords: {
		className: "AllocationRecord",
		label: "Разпределени бройки",
		displayAttr: "name",
		columns: [
					{
						Header: "Кол-во",
						accessor: "allocatedQuantity",
						fluidSize: 1,
						dataType: "UNIT",
					}, {
						Header: "Разпределени към",
						accessor: "allocationConsumer",
						fluidSize: 4,
						dataType: "ENTITY",
						entityType: "allocationProxies",
					}, {
						Header: "Приход/Разход",
						accessor: "allocationProducer",
						fluidSize: 4,
						dataType: "ENTITY",
						entityType: "allocationProxies",
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
	cInvoices: {
		className: "CInvoice",
		label: "Фактури2",
		displayAttr: "invoiceNum",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
			(item.invoiceNum ? item.invoiceNum : "-"),
			(item.invoiceNum ? item.invoiceNum : "-") + "/" + moment(item.invoiceDate).format("YYYYMMDD"),
			(item.invoiceNum ? item.invoiceNum : "-") + "/" + moment(item.invoiceDate).format("YYYYMMDD") + (item.invoiceCounterParty ? ("/" + item.invoiceCounterParty.name) : "")], constraints);}),
			icon: "file-invoice",
		pageURL: "/cInvoices",
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
					return <Link to={"/cInvoices/"+parentId}>{props.value}</Link>;
				}
			},
			{
				Header: "Номер",
				accessor: "invoiceNum",
				fluidSize: 2,
				dataType: "UNIT",
				pageURL: "/cInvoices",
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
				entityType: "cCcPartners",
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
				entityType: "cCtBankAccounts",
			//	optionFilter: [{where: {
			//		op: "equal",
			//			operands: ["bankAccountOwner.eik", {op: "stringLiteral", operands: [{literal: MANAGED_COMPANY_EIK}]}],//TODO ASK FOR "bankAccountOwner.eik"
			//		},
			//	}],
				isRequired: true,
			}, {
				Header: "Валута",
				accessor: "invoiceCurrency",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "cCtCurrencies",
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
			},
			{
				Header: "Редове на фактурата",
				accessor: "invoiceRows",
				show: false,
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "cInvoiceRows",
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
	cInvoiceRows: {
		className: "CInvoiceRow",
		label: "Редове на фактури",
		displayAttr: "id",
		columns: [
			{
				Header: "Фактура",
				accessor: "invoice",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "cInvoices",
				isRequired: true,
			}, {
				Header: "Услуга",
				accessor: "article",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "cGoodses",
				isRequired: true,
			}, {
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

export function getUpdatedColumn(className, column, columnOverride, columnParentEntityType) {
	if(!column) {
		console.error("Column must be specified", column);
		return undefined;
	}
	if(columnOverride !== undefined && columnOverride[column.accessor] !== undefined) {
		column = Object.assign({},column,columnOverride[column.accessor]);
	}
	let filterMethod; //for client-side tables
	let sortMethod; //for client-side tables
	let displayFn;
	let cell;
	if(column.Cell != undefined) {
		cell = column.Cell;
	} else {
		let entityDefinition = undefined;
		switch(column.dataType) {
			case "UNIT":
				cell = (props) => <FieldUnitContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={column.isReadOnly ? false : (props.original._editable ? true : (props.original._asTable ? undefined : false))}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {
									props.original._onChildUpdate(undefined, undefined, column.doCalcOnChange);
									if( column.onChange instanceof Function) column.onChange(value);
								}}
						/>;
				filterMethod = (filter, row, column) => {
					const id = filter.pivotId || filter.id;
					if(filter.value.fromValue != undefined || filter.value.toValue != undefined ) {
						if(filter.value.fromValue && filter.value.fromValue != "" && filter.value.toValue && filter.value.toValue != "") {
							return row[id] >= filter.value.fromValue && row[id] <= filter.value.toValue;
						} else if(filter.value.toValue && filter.value.toValue != "") {
							return row[id] <= filter.value.toValue
						} else if(filter.value.fromValue && filter.value.fromValue != "") {
							return row[id] >= filter.value.fromValue;
						} else {
							return true;
						}
					} else {
						return row[id] == filter.value.fromValue;
					}
				};
				break;
			case "BOOLEAN":
				cell = (props) => <FieldBooleanContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={column.isReadOnly ? false : (props.original._editable ? true : (props.original._asTable ? undefined : false))}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {
									props.original._onChildUpdate(undefined, undefined, column.doCalcOnChange);
									if( column.onChange instanceof Function) column.onChange(value);
								}}
						/>;
				break;
			case "TEXTAREA":
				cell = (props) => <FieldTextareaContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={column.isReadOnly ? false : (props.original._editable ? true : (props.original._asTable ? undefined : false))}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {
									props.original._onChildUpdate(undefined, undefined, column.doCalcOnChange);
									if( column.onChange instanceof Function) column.onChange(value);
								}}
						/>;
				break;
			case "DATE":
				cell = (props) => <FieldDateContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={column.isReadOnly ? false : (props.original._editable ? true : (props.original._asTable ? undefined : false))}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {
									props.original._onChildUpdate(undefined, undefined, column.doCalcOnChange);
									if( column.onChange instanceof Function) column.onChange(value);
								}}
							//showTimeSelect
						/>;
				break;
			case "TIMESTAMP":
				cell = (props) => <FieldTimestampContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={column.isReadOnly ? false : (props.original._editable ? true : (props.original._asTable ? undefined : false))}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {
									props.original._onChildUpdate(undefined, undefined, column.doCalcOnChange);
									if( column.onChange instanceof Function) column.onChange(value);
								}}
						/>;
				break;
			case "MONTH":
				cell = (props) => <FieldMonthContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={column.isReadOnly ? false : (props.original._editable ? true : (props.original._asTable ? undefined : false))}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {
									props.original._onChildUpdate(undefined, undefined, column.doCalcOnChange);
									if( column.onChange instanceof Function) column.onChange(value);
								}}
						/>;
				break;
			case "ENTITY":
				entityDefinition =  getCombinedEntityDefinitions()[column.entityType];
				if(!entityDefinition) {
					console.error("Cannot find entityDefinition for: "+column.entityType);
					return undefined;
				}
				displayFn = entityDefinition.displayFn instanceof Function ? entityDefinition.displayFn : ((item) => item ? (entityDefinition.displayAttr ? item[entityDefinition.displayAttr] : "\u{1F194}"+item["id"]) : "--няма--");
				cell = (props) => <FieldNomenclatureSelectContainer
						href={props.original && props.original._links && props.original._links[column.accessor] ? props.original._links[column.accessor].href : undefined}
						componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
						nomenclatureKey={column.entityType}
						filter={column.optionFilter}
						editable={column.isReadOnly ? false : (props.original._editable)}
						displayAttr={entityDefinition.displayAttr ? entityDefinition.displayAttr : "name"}
						displayFn={displayFn}
						loading={props.original[Constants.PATH_FOR_LOADING]}
						onChange={(href,eventOrigin) => {
								props.original._onChildUpdate(
									props.original._componentPath+(props.column._parentPath.length > 0 ? '.'+props.column._parentPath : "")+'._links.'+props.column._fieldPath+'.href', 
									href, 
									(eventOrigin == "select" ? column.doCalcOnChange : false) //ignore changes not by user input
								);
								if( column.onChange instanceof Function) column.onChange(href);
							}}
						pageURL={entityDefinition ? entityDefinition.pageURL : undefined}
						isClearable={true}
					/>;
				filterMethod = (filter, row, column) => {
					const id = filter.pivotId || filter.id;
					return row[id] !== undefined ? displayFn(row[id]).toLowerCase().includes(filter.value.toLowerCase()) : true
				};
				sortMethod = (a, b, desc) => {
					return (displayFn(a).toLowerCase() < displayFn(b).toLowerCase() ? -1 : 1);
				};
				break;
			case "FILTERED_ENTITY":
				entityDefinition =  getCombinedEntityDefinitions()[column.entityType];
				if(!entityDefinition) {
					console.error("Cannot find entityDefinition for: "+column.entityType);
					return undefined;
				}
				displayFn = entityDefinition.displayFn instanceof Function ? entityDefinition.displayFn : ((item) => item ? (entityDefinition.displayAttr ? item[entityDefinition.displayAttr] : "\u{1F194}"+item["id"]) : "--няма--");
				cell = (props) => <FieldSelectContainer
						data={props.original}
						href={props.original && props.original._links && props.original._links[column.accessor] ? props.original._links[column.accessor].href : undefined}
						componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
						listType={column.entityType}
						listAttr={entityDefinition.displayAttr ? entityDefinition.displayAttr : "id"}
						listDisplayFn={displayFn}
						listExpandedColumns={column.expandColumns}
						listLookupColumns={column.lookupColumns ? column.lookupColumns : entityDefinition.lookupColumns}
						listFilter={column.optionFilter}
						editable={column.isReadOnly ? false : (props.original._editable)}
						loading={props.original[Constants.PATH_FOR_LOADING]}
						creatable={false}
						onChange={(object) => {
							if(props.original && props.original._onChildUpdate && props.original._componentPath && column.accessor) {
								if(object && object._links && object._links.self && object._links.self.href) {
									props.original._onChildUpdate(props.original._componentPath+'._links.'+column.accessor+'.href', object._links.self.href, column.doCalcOnChange);
								} else {
									props.original._onChildUpdate(props.original._componentPath+'._links.'+column.accessor+'.href', undefined, column.doCalcOnChange);
								}
							}
							if( column.onChange instanceof Function) column.onChange(object);
						}}
						pageURL={entityDefinition ? entityDefinition.pageURL : undefined}
						isClearable={true}
					/>;
				filterMethod = (filter, row, column) => {
					const id = filter.pivotId || filter.id;
					return row[id] !== undefined ? displayFn(row[id]).toLowerCase().includes(filter.value.toLowerCase()) : true
				};
				sortMethod = (a, b, desc) => {
					return (displayFn(a).toLowerCase() < displayFn(b).toLowerCase() ? -1 : 1);
				};
				break;
			default:
				cell = (props) => {
					//console.log('props',props);
					return <FieldTextContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={column.isReadOnly ? false : (props.original._editable ? true : (props.original._asTable ? undefined : false))}
							loading={props.original[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {
									props.original._onChildUpdate(undefined, undefined, column.doCalcOnChange);
									if( column.onChange instanceof Function) column.onChange(value);
								}}
						/>;
				}
		}
		if(column.isLink) {
			let origFunc = cell;
			cell = (props) => {
				if(props.original._editable == true) {
					return origFunc(props);
				} else {
					//because of the ID column, which is common
					let parentEntityDefinition =  getEntityDefinition(columnParentEntityType);
					const pageURL=(column.pageURL ? column.pageURL : (parentEntityDefinition && parentEntityDefinition.pageURL ? parentEntityDefinition.pageURL : undefined));
					const parentId = resolveObjectPath(props.column._parentPath+".id",props.original);
					return <Link to={pageURL+"/"+parentId}>{props.value}</Link>;
				}
			};
		}
	}
	let filter;
	if(column.Filter != undefined) {
		filter = column.Filter;
	} else {
		switch(column.dataType) {
			case "BOOLEAN":
				filter = ({ filter, onChange }) => {
						if(filter && filter.value == false) {
							return <Button variant="outline-dark" style={{cursor: "pointer"}} title={i18n.t("No")} onClick={(e) => {onChange(true)}}><FontAwesomeIcon size="lg" icon="times"/></Button>;
						}
						if(filter && filter.value == true) {
							return <Button variant="outline-dark" style={{cursor: "pointer"}} title={i18n.t("Yes")} onClick={(e) => {onChange(undefined)}}><FontAwesomeIcon size="lg" icon="check"/></Button>;
						}
						return <Button variant="outline-dark" style={{cursor: "pointer"}} title={i18n.t("All")} onClick={(e) => {onChange(false)}}><FontAwesomeIcon size="lg" icon="asterisk"/></Button>;
					};
				break;
			case "TIMESTAMP":
				filter = tableFilterDateFromTo;
				break;
			case "DATE":
				filter = tableFilterDateFromTo;
				break;
			case 'MONTH':
				filter = tableFilterDateFromTo;
				//filter = tableFilterMonthFromTo;
				break;
			case 'UNIT':
				filter = tableFilterValueFromTo;
				break;
			default:
		}
	}
	let width;
	if(column.width != undefined) {
		width = column.width;
	} else {
		switch(column.dataType) {
			case "TIMESTAMP":
				width = 170;
				break;
			default:
		}
	}
	let resultColumn = {
			...column,
			Cell: cell,
			Filter: filter,
			width: width,
		};
	if(filterMethod) {
		resultColumn.filterMethod = filterMethod;
	}
	if(sortMethod) {
		resultColumn.sortMethod = sortMethod;
	}
	//set defaults for path attributes
	if(resultColumn._fieldPath == undefined) {
		resultColumn._parentPath = "";
		resultColumn._fieldPath = column.accessor;
		resultColumn._fieldRelPath = column.accessor;
	}
	//translate Header
	resultColumn.Header = i18n.t(className+"."+resultColumn._fieldPath);
	//apply column override
	if(columnOverride !== undefined && columnOverride[column.accessor] !== undefined) {
		resultColumn = Object.assign({},resultColumn,columnOverride[column.accessor]);
	}
	if(resultColumn._id == undefined) {
		resultColumn._id = (resultColumn.id ? resultColumn.id : resultColumn.accessor);
	}
	return resultColumn;
}

export function getCombinedEntityDefinitions() {
	let definitions = {
		...entityDefinitions,
		...nepalEntityDefinitions,
		...selfieEntityDefinitions,
		//...financeEntityDefinitions,
		...warehouseEntityDefinitions,

		// ...taskEntityDefinitions,
		...crmEntityDefinitions,
		// ...employeeEntityDefinitions,
		// ...transportEntityDefinitions,
		// ...watoEntityDefinitions
	};
	return definitions;
}

export function getEntityDefinition(key, columnOverride, entityOverride={}) {
	//TODO check if key is in the API
	let definitions = getCombinedEntityDefinitions();
	if(definitions[key]) {
		let entityDef = Object.assign({},definitions[key],entityOverride);
		return {
			...entityDef,
			label: i18n.t(entityDef.className+"._className"),
			label_plural: i18n.t(entityDef.className+"._className_plural"),
			displayFn: entityDef.displayFn instanceof Function ? entityDef.displayFn : ((item) => entityDef.displayAttr ? item[entityDef.displayAttr] : item["name"]),
			pageURL: entityDef.pageURL ? entityDef.pageURL : "/"+key,
			columns:
				entityDef.columns.map((column) => getUpdatedColumn(entityDef.className, column, columnOverride, key))
		};
	} else {
		console.error("Cannot find such entity definition: ", key);
	}
}

export function getEntityDefinitionExpanded(key, expandPathsArray, columnOverride, entityOverride={}) {
	//TODO check if key is in the API
	let definitions = getCombinedEntityDefinitions();
	if(definitions[key]) {
		let entityDef = Object.assign({},definitions[key],entityOverride);
		return {
			...entityDef,
			label: i18n.t(entityDef.className+"._className"),
			label_plural: i18n.t(entityDef.className+"._className_plural"),
			displayFn: entityDef.displayFn instanceof Function ? entityDef.displayFn : ((item) => entityDef.displayAttr ? item[entityDef.displayAttr] : item["name"]),
			pageURL: entityDef.pageURL ? entityDef.pageURL : "/"+key,
			columns: getExpandedColumns(key, expandPathsArray, columnOverride)
		};
	}
}

export function getEntityField(key, curr, column_data, error) {
	return <div key={key} className={"col-sm-"+(curr.fluidSize == undefined ? 12 : curr.fluidSize)}>
				<label className="contracts-add-form-label">{curr.Header}{curr.isRequired ? <span className="text-red">*</span> : undefined}</label>
				{curr.Cell instanceof Function ?
						curr.Cell({
							original: column_data,
							value: resolveObjectPath(curr.accessor,column_data),
							column: {
								_parentPath: curr._parentPath,
								_fieldPath: curr._fieldPath,
								_fieldRelPath: curr._fieldRelPath,
							},
							_asTable: false,
						})
						: undefined
				}
				{error ? <small className="text-danger">{getMessageFromCode(error.error, error.value)}</small> : undefined}
			</div>;
}

export function getEntityForm(key, columnOverride, componentPath, editable, loading, data, onChildUpdate) {
	let errors = extractColumnErrorMessage(data[Constants.PATH_FOR_ERROR]);
	const columns = getEntityDefinition(key, columnOverride).columns;
	let formItems = [];
	for (var i = 0; i < columns.length; i++) {
		let curr = columns[i];
		//don't join for the hidden columns
		if(curr.show != false && curr.show != "hidden" && curr.isAggregate != true) {
			let column_data = {
				...data,
				_needUpdate: false,
				_asTable: false,
				_componentPath: componentPath,
				_onChildUpdate: onChildUpdate,
				[Constants.PATH_FOR_LOADING]: loading,
				_editable: editable,
			};
			let error;
			if(errors instanceof Array) {
				error = errors.find((error) => {
					return curr.accessor == error.field
				});
			}
			//console.warn("getEntityForm: "+JSON.stringify(column_data)+"; "+JSON.stringify(curr)+"; "+JSON.stringify(data));
			formItems.push(getEntityField("column_"+i, curr, column_data, error));
		}
	};
	let body = <div className="row" style={{width: "100%"}}>
					{formItems}
				</div>;
	return body;
}

export function builderDataToProjection(newData,retrieveType) {
	if(newData && !newData._embedded) {
		newData._embedded = {[retrieveType]: []};
	}
	if(newData && newData._embedded && newData._embedded.hashMaps instanceof Array) {
		const entityDef = getEntityDefinition(retrieveType);
		newData._embedded[retrieveType] = newData._embedded.hashMaps.map(
				(item) => {
					let temp = {};
					let keys = Object.keys(item);
					keys.sort();
					keys.forEach((key) => {
						buildObjectPathAndAssign(key,temp,item[key]);
					});
					let result = Object.assign(temp,temp[entityDef.className]);
					delete result[entityDef.className];
					return result;
				}
			);
	}
}

export function flattenReportColumns(columns) {
	if(columns instanceof Array) {
		return columns.reduce((acc,curr) => {
				if(!curr) {
					console.error("Skipping column that is undefined");
					return acc;
				}
				if(curr.columns instanceof Array) {
					return acc.concat(flattenReportColumns(curr.columns));
				} else {
					acc.push(curr);
					return acc;
				}
			},[]);
	} else {
		return undefined;
	}
}

export function mapReportColumns(columns, mapFunction) {
	if(columns instanceof Array) {
		let newColumns = columns.slice();
		return newColumns.map((column) => {
				if(!column) {
					console.error("Column must be specified");
					return {};
				}
				if(column.columns instanceof Array) {
					column.columns = mapReportColumns(column.columns, mapFunction);
				}
				return mapFunction(column);
			});
	} else {
		return undefined;
	}
}

export function retrieveData(retrieveType, columns, excludeRootEntity = false, pageIndex = 0, pageSize = 1000, sort = [{id: "id", desc: true}], filter = [{}]) {
	if(columns == undefined) {
		columns = getExpandedColumns(retrieveType, [], undefined);
	}
	let fetch_url = builderURLFromColumns(
			(retrieveType ? API_URL+"/"+retrieveType : undefined),
			retrieveType,
			columns,
			excludeRootEntity);
	let filter_param = tableFilterToParam(filter, columns, retrieveType);
	let restConfig = {
			url: fetch_url,
			params: {
				...filter_param,
				page: pageIndex,
				size: pageSize,
				sort: getEntityDefinition(retrieveType).className+"."+sort[0].id+','+(sort[0].desc?'desc':'asc') //TODO implement multi-sort
			},
			paramsSerializer: function(params) {
				//needed for the from-to dates
				return querystring.stringify(params)
			}
		};
	restConfig.headers = {...restConfig.headers, Authorization: sessionStorage["X-AUTH-TOKEN"]};
	const promise = axios(restConfig)
		.then((response) => {
			let newData = response.data;
			//transform data to "repository response"-like
			builderDataToProjection(newData,retrieveType);
			newData._retrieveHref = fetch_url;
			newData._sort = sort;
			newData._filter = filter;
			return newData;
		})
		.catch(error => {
			return error;
		});
	return promise;
}

function getOperandsAsParam(operand) {
	if(operand !== undefined) {
		if(operand.literal != undefined) {
			return operand.literal;
		} else if(operand.operands instanceof Array) {
			return operand.op+"("+operand.operands.map((operandElem) => getOperandsAsParam(operandElem)).join(";")+")";
		} else {
			return operand;
		}
	} else {
		return undefined;
	}
}

/**
 * Generate the URL that will should be called to get the columns. Columns may be generated by getExpandedColumns, but may be hand-written too.
 * @param {any} defaultURL
 * Returned if we cannot generate proper URL
 * @param {any} retrieveType
 * @param {any} columnsHierarchy
 */
export function builderURLFromColumns(defaultURL, retrieveType, columnsHierarchy, excludeRootEntity) {
	let fetch_url;
	//get the columns as an array, in case they are expanded and have a hierarchy (columns with subcolumns)
	let columns = flattenReportColumns(columnsHierarchy);
	if(retrieveType) {
		//get the definition for the entity type
		const entityDef = getEntityDefinition(retrieveType);
		if(entityDef) {
			//if there were no proper columns as parameter, use the ones from the entity definition
			if(columns === undefined || !(columns instanceof Array) ) {
				columns = entityDef.columns;
			}
			if(entityDef.className) {
				//use the report builder endpoint with the entity type as root
				fetch_url = API_URL+"/reports/builder/1?from="+entityDef.className;
				//get all the entities to be selected (joined to the root)
				const selectMap =
					columns.reduce((acc,curr) => {
						//don't join for the hidden columns
						if(curr.show != false) {
							if(curr.isAggregate) {
								acc[getOperandsAsParam(expandOperands(curr.aggregation, entityDef.className))+":"+entityDef.className+"."+curr.accessor] = curr;
								//acc[getOperandsAsParam(expandOperands(curr.aggregation, entityDef.className+(curr._parentPath ? "."+curr._parentPath : "")))+":"+entityDef.className+"."+curr.accessor] = curr;
							} else if(curr.accessor instanceof Function) {
								//TODO somehow get the accessed fields in the function
							} else if(curr.accessor && curr.accessor.length > 0) {
								//for entities we use the accessor as join path
								if(curr.dataType == "ENTITY" || curr.dataType == "FILTERED_ENTITY") {
									acc[entityDef.className+"."+curr.accessor] = curr;
								}
								//get the parent path of this column, if available
								const parent = curr.accessor.substring(0,curr.accessor.lastIndexOf("."));
								if(parent.length > 0) {
									//parent should be selected (at least for non-entity columns)
									acc[entityDef.className+"."+parent] = curr;
								}
							} else if(!curr.isCalculated) {
								console.warn("Column without accessor: ",curr);
							}
						}
						return acc;
					},{});
				if(!excludeRootEntity){
					selectMap[entityDef.className]= {}
				}
				//console.warn(selectMap,columns)
				//make a comma-separated list of the entities
				let select = Object.keys(selectMap).join();
				//if required, add the selected entities as parameter
				if(select.length > 0) {
					fetch_url += "&select="+select;
				}

				const summariesArray =
					columns.reduce((acc,curr) => {
						if(curr.summary !== undefined) {
							//add the parameter for the summaries
							let summaryParam = getOperandsAsParam(
									expandOperands(curr.summary, entityDef.className) //+(curr._parentPath ? "."+curr._parentPath : "")
								)+":"+curr.id;
							acc.push(summaryParam);
						}
						return acc;
					},[]);
				//make a comma-separated list of the summaries
				let summaries = summariesArray.join();
				//if required, add the summaries as parameter
				if(summaries.length > 0) {
					fetch_url += "&summary="+summaries;
				}
			} else {
				fetch_url = defaultURL;
			}
		} else {
			fetch_url = defaultURL;
		}
	} else {
		fetch_url = defaultURL;
	}
	return fetch_url;
}

function expandOperands(operand, expandPath) {
	if(operand !== undefined) {
		if(operand.literal != undefined) {
			return operand;
		} else if(operand.operands instanceof Array) {
			return {
				...operand,
				operands: operand.operands.map((operandElem) => expandOperands(operandElem, expandPath)),
			};
		} else {
			return expandPath + "." + operand;
		}
	} else {
		return undefined;
	}
}

/**
 * Generate an array of columns for reports by expanding a root entity with columns of entities that it references
 * @param {any} rootName
 * The key in entityDefinitions of the root entity that we start from
 * @param {any} expandPathsArray
 * An array of accessors (dot strings) for the referenced entities that we want to add to the report
 */
export function getExpandedColumns(rootName, expandPathsArray, columnOverride) {
	let expand = {};
	if(expandPathsArray instanceof Array) {
		expandPathsArray.forEach((expandPath) => {
			if(typeof expandPath === 'string' || expandPath instanceof String) {
				let splitPathArray = expandPath.split(".");
				let splitPath = "";
				splitPathArray.forEach((pathNode) => {
					splitPath = splitPath + pathNode;
					if(expand[splitPath] == undefined) {
						expand[splitPath] = true; //add all entities from the path, flagged to be hidden 
					}
					splitPath = splitPath + ".";
				});
				expand[expandPath] = false; //set the required entity to be not hidden
			}
		});
	}
	let allColumns = [];
	//always add the commonRecords columns first, then the root columns
	let rootDefinition = getEntityDefinition(rootName, columnOverride);
	allColumns = allColumns.concat(rootDefinition.columns);
	//update the columns to get the default values
	allColumns = allColumns.map((column) => getUpdatedColumn(rootDefinition.className, column, columnOverride, rootName));
	//filter out the entity columns that will be expanded
	let root = allColumns.reduce((acc,column) => {
		if(expand[column._id] == false && column.entityType) {
			return acc;
		}
		acc.push(column);
		return acc;
	},[]);
	if(expand instanceof Object) {
		let keys = Object.keys(expand);
		//keys.sort();
		//execute each expand
		keys.forEach(
			(expandPath) => {
				if(allColumns instanceof Array) {
					//find the column that will be expanded
					let expandColumn = allColumns.find((column) => column._id == expandPath);
					if(expandColumn && expandColumn.entityType) {
						//find the entity definition for expand
						let expandedEntity = getEntityDefinition(expandColumn.entityType);
						if(expandedEntity.columns instanceof Array) {
							//add the expanded entity as a column with subcolumns
							root.push(
								{
									Header: expandColumn.Header,
									_entityType: expandColumn.entityType,
									_mappedBy: expandColumn.mappedBy,
									columns: expandedEntity.columns.reduce((acc,column) => {
											//copy the definition of the column and add the path
											let newColumn = Object.assign({},
												column,
												{
													_id: expandPath + "." + (column.id ? column.id : column.accessor),
													accessor: expandPath + "." + column.accessor,
													_parentPath: expandPath,
													_fieldPath: column.accessor ? column.accessor : column.id,
													_fieldRelPath: (expandPath ? expandPath + "." : "") + (column.accessor ? column.accessor : column.id),
													summary: expandOperands(column.summary, expandPath), //get summaries with updated the accessors with full path
													aggregation: expandOperands(column.aggregation, expandPath), //get aggregation with updated the accessors with full path
												});
											//update the column with the defaults
											newColumn = getUpdatedColumn(expandedEntity.className, newColumn, columnOverride, expandColumn.entityType);
											//put the column in the list of all columns
											allColumns.push(newColumn);
											//skip columns that are expanded or are back references
											if(expand[newColumn._id] == false && newColumn.entityType != undefined //ignore the expanded entity columns
												|| expand[expandPath] == true && (expand[newColumn._id] == undefined || expand[newColumn._id] == true) //ignore the columns of the expanded entities that are hidden
												|| expandColumn.mappedBy == newColumn._fieldPath //ignore the back reference column - it is already expanded
													) {
												//console.warn('getExpandedColumns: skip column '+newColumn._id+'/'+newColumn.accessor+'; expand[newColumn._id]: '+expand[newColumn._id]+'; newColumn.entityType: '+newColumn.entityType+'; expand[expandPath]: '+expand[expandPath]+'; expandPath: '+expandPath);
												return acc;
											}
											//console.warn('getExpandedColumns: add column '+newColumn._id+'/'+newColumn.accessor+'; expand[newColumn._id]: '+expand[newColumn._id]+'; newColumn.entityType: '+newColumn.entityType+'; expand[expandPath]: '+expand[expandPath]+'; expandPath: '+expandPath);
											//add the column to the result
											acc.push(newColumn);
											return acc;
										},[]),
								}
							);
						} else {
							console.warn('getExpandedColumns: expandedEntity.columns is not Array!');
						}
					} else {
						//This is OK, only the column is requested
						//console.warn('getExpandedColumns: cannot find expandColumn.entityType!');
					}
				} else {
					console.warn('getExpandedColumns: allColumns is not Array!');
				}
			}
		);
	} else {
		console.warn('getExpandedColumns: Expand param is not Array!');
	}
	return root;
}

export function tableFilterToParam(filter, tableColumns, retrieveType) {
	let entityDefinition = getEntityDefinition(retrieveType);
	return filter.reduce((acc,curr) => {
		let temp = acc;
		if(curr && curr.id && curr.value != undefined) {
			const filterColumn = flattenReportColumns(tableColumns).find((column) => column.accessor == curr.id);
			//console.log("tableFilterToParam",filter, tableColumns, retrieveType, filterColumn);
			if(filterColumn && filterColumn.filterToParam instanceof Function) {
				filterColumn.filterToParam(temp,curr);
			} else if(curr.value.fromDate || curr.value.toDate) {
				let dateFormat = filterColumn.dataType == "DATE" ? "YYYY-MM-DD" : "YYYY/MM/DD HH:mm:ss ZZ"; //https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#ISO_LOCAL_DATE or ???-why do we use the other
				let dateFromDefault = filterColumn.dataType == "DATE" ? "1900-01-01" : "1900/01/01 00:00:00 +0000";
				let dateToDefault = filterColumn.dataType == "DATE" ? "3000-01-01" : "3000/01/01 00:00:00 +0000";
				if(curr.value.fromDate && curr.value.toDate) {
					//JAVA query param date format
					temp[entityDefinition.className+"."+curr.id] = [curr.value.fromDate.format(dateFormat),curr.value.toDate.clone().endOf("day").format(dateFormat)];
				} else {
					temp[entityDefinition.className+"."+curr.id] = [curr.value.fromDate ? curr.value.fromDate.format(dateFormat) : dateFromDefault
							,curr.value.toDate ? curr.value.toDate.clone().endOf("day").format(dateFormat) : dateToDefault];
				}
			} else if(curr.value.articleProduct) {
				temp[entityDefinition.className+"."+curr.id+".interested"] = curr.value.interested ? "true" : "false";
				temp[entityDefinition.className+"."+curr.id+".articleProduct.name"] = curr.value.articleProduct;
			} else {
				//for entities the filter should be for the displayed attribute
				let newClause = "like(upper("+entityDefinition.className+"."+curr.id+");upper(%"+curr.value+"%))";
				if(curr.id == "id" && curr.value && curr.value.fromValue == undefined && curr.value.toValue == undefined) {
					newClause = "equal("+entityDefinition.className+"."+curr.id+";"+curr.value+")";
				} else if(filterColumn && (filterColumn.dataType == "ENTITY" || filterColumn.dataType == "FILTERED_ENTITY")) {
					let newId = curr.id + "." + (getEntityDefinition(filterColumn.entityType).displayAttr !== undefined ? getEntityDefinition(filterColumn.entityType).displayAttr : "name");
					newClause = "like(upper("+entityDefinition.className+"."+newId+");upper(%"+curr.value+"%))";
					
					let listLookupColumns = filterColumn.lookupColumns ? filterColumn.lookupColumns : getEntityDefinition(filterColumn.entityType).lookupColumns;
					if(curr.value != "" && listLookupColumns instanceof Array) {
						listLookupColumns.forEach((lookupColumn, index) => {
							if(index == 0) {
								newClause = "like(upper("+entityDefinition.className+"."+curr.id+"."+lookupColumn+");upper(%"+curr.value+"%))";
							} else {
								newClause = "or("+newClause+";like(upper("+entityDefinition.className+"."+curr.id+"."+lookupColumn+");upper(%"+curr.value+"%)))";
							}
						});
					}
				} else if(filterColumn && filterColumn.dataType == "UNIT") {
					if(curr.value.fromValue != undefined || curr.value.toValue != undefined ) {
						if(curr.value.fromValue && curr.value.fromValue != "" && curr.value.toValue && curr.value.toValue != "") {
							newClause = "and(ge("+entityDefinition.className+"."+curr.id+";"+curr.value.fromValue+");le("+entityDefinition.className+"."+curr.id+";"+curr.value.toValue+"))";
						} else if(curr.value.toValue && curr.value.toValue != "") {
							newClause = "le("+entityDefinition.className+"."+curr.id+";"+curr.value.toValue+")";
						} else if(curr.value.fromValue && curr.value.fromValue != "") {
							newClause = "ge("+entityDefinition.className+"."+curr.id+";"+curr.value.fromValue+")";
						} else {
							newClause = "";
						}
					} else {
						newClause = "equal("+entityDefinition.className+"."+curr.id+";"+curr.value+")";
					}
				} else if(filterColumn && filterColumn.dataType == "BOOLEAN") {
					if(curr.value) {
						newClause = "isTrue("+entityDefinition.className+"."+curr.id+")";
					} else {
						newClause = "or(isNull("+entityDefinition.className+"."+curr.id+");isFalse("+entityDefinition.className+"."+curr.id+"))";
					}
				}
				if(newClause != "") {
					if(temp["having"] != undefined) {
						temp["having"] = "and("+temp["having"]+";"+newClause+")";
					} else {
						temp["having"] = newClause;
					}
				}
			}
		} else if(curr && curr.where != undefined) {
			let newClause = getOperandsAsParam(expandOperands(curr.where, entityDefinition.className));
			//console.log("where", newClause);
			if(temp["where"] != undefined) {
				temp["where"] = "and("+temp["where"]+";"+newClause+")";
			} else {
				temp["where"] = newClause;
			}
		} else if(curr && curr.having != undefined) {
			let newClause = getOperandsAsParam(expandOperands(curr.having, entityDefinition.className));
			//console.log("having", newClause);
			if(temp["having"] != undefined) {
				temp["having"] = "and("+temp["having"]+";"+newClause+")";
			} else {
				temp["having"] = newClause;
			}
		};
		return temp;
	},{});
}

/*
 * Uses canvas.measureText to compute and return the width of the given text of given font in pixels.
 * 
 * @param {String} text The text to be rendered.
 * @param {String} font The css font descriptor that text is to be rendered with (e.g. "bold 14px verdana").
 * 
 * @see https://stackoverflow.com/questions/118241/calculate-text-width-with-javascript/21015393#21015393
 */
function getTextWidth(text, font) {
	// re-use canvas object for better performance
	var canvas = getTextWidth.canvas || (getTextWidth.canvas = document.createElement("canvas"));
	var context = canvas.getContext("2d");
	context.font = font;
	var metrics = context.measureText(text);
	return metrics.width;
}

export function getLabelWithConstraints(labelArray, constraints = {}) {
	if(labelArray instanceof Array || labelArray.length == 0) {
		let filteredArray = labelArray;
		if(constraints.maxChars > 0) {
			filteredArray = labelArray.filter((label) => label && String(label).length <= constraints.maxChars);
			if(filteredArray.length == 0) {
				filteredArray = labelArray;
			}
		}
		filteredArray.sort((a, b) => String(b).length - String(a).length);
		return filteredArray[0];
	} else {
		return "Label error";
	}
}

function CalendarContainer (props) {
	const containerId = document.getElementById('container-outside-root')
	
	return (
		<Portal container={containerId}>
			{props.children}
		</Portal>
	)
}

function tableFilterValueFromTo({ filter, onChange }) {
	return <div><div>
		<input type={"number"} style={{width: "100%"}}
			onChange={(e) => {
				if(e || filter && filter.value.toValue) {
					onChange({
						fromValue: e.target.value,
						toValue: filter && filter.value ? filter.value.toValue : undefined
					});
				} else {
					onChange(undefined);
				}
			}}
			value={filter && filter.value ? filter.value.fromValue : ''}>
		</input>
		</div><div>
		<input type={"number"} style={{width: "100%"}}
			onChange={(e) => {
				if(e || filter && filter.value.fromValue) {
					onChange({
						fromValue: filter && filter.value ? filter.value.fromValue : undefined,
						toValue: e.target.value,
					});
				} else {
					onChange(undefined);
				}
			}}
			value={filter && filter.value ? filter.value.toValue : ''}>
		</input>
		</div>
	</div>;
}

function tableFilterDateFromTo({ filter, onChange }) {
	return <div>
		<DatePicker
			selected={filter && filter.value ? filter.value.fromDate : null}
			onChange={(e) => {
				if(e || filter && filter.value.toDate) {
					onChange({
						fromDate: e,
						toDate: filter && filter.value ? filter.value.toDate : e
					});
				} else {
					onChange(undefined);
				}
			}}
			selectsStart
			startDate={filter && filter.value ? filter.value.fromDate : undefined}
			endDate={filter && filter.value ? filter.value.toDate : undefined}
			placeholderText={i18n.t("ChooseFromDate")}
			locale="bg-bg"
			isClearable={true}
			popperContainer={CalendarContainer}
			popperPlacement="bottom-end"
		/>
		<DatePicker
			selected={filter && filter.value ? filter.value.toDate : null}
			onChange={(e) => {
				if(e || filter && filter.value.fromDate) {
					onChange({
						fromDate: filter && filter.value ? filter.value.fromDate : e,
						toDate: e
					});
				} else {
					onChange(undefined);
				}
			}}
			selectsEnd
			startDate={filter && filter.value ? filter.value.fromDate : undefined}
			endDate={filter && filter.value ? filter.value.toDate : undefined}
			placeholderText={i18n.t("ChooseToDate")}
			locale="bg-bg"
			isClearable={true}
			popperContainer={CalendarContainer}
			popperPlacement="bottom-end"
		/>
	</div>;
}

function tableFilterMonthFromTo({ filter, onChange }) {
	return <div>
		<DatePicker
			selected={filter && filter.value ? filter.value.fromDate : null}
			onChange={(e) => {
				if(e || filter && filter.value.toDate) {
					onChange({
						fromDate: e,
						toDate: filter && filter.value ? filter.value.toDate : e
					});
				} else {
					onChange(undefined);
				}
			}}
			selectsStart
			startDate={filter && filter.value ? filter.value.fromDate : undefined}
			endDate={filter && filter.value ? filter.value.toDate : undefined}
			placeholderText={i18n.t("ChooseFromDate")}
			locale="bg-bg"
			isClearable={true}
			popperContainer={CalendarContainer}
			popperPlacement="bottom-end"
			showMonthDropdown
		/>
		<DatePicker
			selected={filter && filter.value ? filter.value.toDate : null}
			onChange={(e) => {
				if(e || filter && filter.value.fromDate) {
					onChange({
						fromDate: filter && filter.value ? filter.value.fromDate : e,
						toDate: e
					});
				} else {
					onChange(undefined);
				}
			}}
			selectsEnd
			startDate={filter && filter.value ? filter.value.fromDate : undefined}
			endDate={filter && filter.value ? filter.value.toDate : undefined}
			placeholderText={i18n.t("ChooseToDate")}
			locale="bg-bg"
			isClearable={true}
			popperContainer={CalendarContainer}
			popperPlacement="bottom-end"
			showMonthDropdown
		/>
	</div>;
}
