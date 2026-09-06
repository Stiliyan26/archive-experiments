import React from 'react';

import * as Constants from './../../static/constants';

import FieldArticlePriceRateContainer from '../fields/FieldArticlePriceRateContainer'
import FieldAttachableContainer from '../fields/FieldAttachableContainer'
import FieldNomenclatureSelectContainer from '../fields/FieldNomenclatureSelectContainer'
import FieldTimeChargeRateContainer from '../fields/FieldTimeChargeRateContainer'
import FieldTimestampContainer from '../fields/FieldTimestampContainer'
import FieldUnitContainer from '../fields/FieldUnitContainer'

const taskEntityDefinitions = {
	taskStatuses: {
		className: "TaskStatus",
		label: "Статуси на задача",
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
					},{ //TODO put proper filtering for boolean
						Header: "Краен статус",
						accessor: 'terminal',
						fluidSize: 2,
						dataType: "BOOLEAN",
					}
				]
	},
//	taskPriorities: {
//		className: "TaskPriority",
//		label: "Приоритети на задачи",
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
//	taskTypes: {
//		className: "TaskType",
//		label: "Типове задачи",
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
	taskRelationTypes: {
		className: "TaskRelationType",
		label: "Типове връзки между задачи",
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
						Header: "Наименование (свързана задача)",
						accessor: 'inverseName',
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
//	taskRelations: {
//		className: "TaskRelation",
//		label: "Свързани задачи",
//		icon: "tasks",
//		columns: [
//					{
//						Header: "Тип връзка",
//						accessor: 'relation',
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskRelationTypes",
//					},{
//						Header: "От задача",
//						accessor: 'fromTask',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "tasks",
//					},{
//						Header: "Към задача",
//						accessor: 'toTask',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "tasks",
//					}
//				]
//	},
	taskRelations: {
		className: "TaskRelation",
		label: "Връзки поръчка-курс",
		icon: "tasks",
		columns: [
					{
						Header: "Тип връзка",
						accessor: 'relation',
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "taskRelationTypes",
					},{
						Header: "Поръчка",
						accessor: "transportOrder",
						//show: false,
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "transportOrders",
					},{
						Header: "Курс",
						accessor: "transport",
						//show: false,
						fluidSize: 3,
						dataType: "FILTERED_ENTITY",
						entityType: "transports",
					}
				]
	},
//	taskWatchers: {
//		className: "TaskWatcher",
//		label: "Наблюдаващи задачата",
//		icon: "eye",
//		columns: [
//					{
//						Header: "Задача",
//						accessor: 'task',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "tasks",
//					},{
//						Header: "Потребител",
//						accessor: 'watcher',
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "secUsers",
//					}
//				]
//	},
	taskAttachments: {
		className: "TaskAttachment",
		label: "Прилежащи документи на задачата",
		icon: "folder",
		columns: [
					{
						Header: "Задача",
						accessor: 'task',
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "tasks",
					},{
						Header: "Описание",
						accessor: 'description',
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Документ",
						accessor: 'attachment',
						fluidSize: 4,
						dataType: "ENTITY",
						entityType: "attachables",
						Cell: (props) => <FieldAttachableContainer
							href={props.original && props.original._links && props.original._links.attachment ? props.original._links.attachment.href : undefined}
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={props.original._editable}
							attachedToObjectData={props.original ? props.original.task : undefined}
							onChangeF={(attachable) => {
								props.original._onChildUpdate(props.original._componentPath+'._links.attachment.href',attachable._links.self.href);
							}}
						/>,
					}
				]
	},
	comments: {
		className: "Comment",
		label: "Коментари",
		icon: "comments",
		aclRestrictable: true,
		columns: [
					{
//						Header: "Задача на коментара",
//						accessor: 'task',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "tasks",
//					},{
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
	commentTemplates: {
		className: "CommentTemplate",
		label: "Шаблонни коментари",
		displayAttr: "code",
		columns: [
					{
						Header: "Код",
						accessor: 'code',
						fluidSize: 2,
						dataType: "UNIT",
					},{
						Header: "Текст",
						accessor: 'text',
						fluidSize: 8,
						dataType: "TEXTAREA",
					}
				]
	},
//	taskRequiredAttachments: {
//		className: "TaskRequiredAttachment",
//		label: "Изходящи документи",
//		displayAttr: "description",
//		icon: "check-square",
//		aclRestrictable: true,
//		columns: [
//					{
//						Header: "Задача",
//						accessor: 'task',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "tasks",
//					},{
//						Header: "Тип",
//						accessor: 'type',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Описание",
//						accessor: 'description',
//						fluidSize: 4,
//						dataType: "TEXT",
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
//							attachedToObjectData={props.original ? props.original.task : undefined}
//							onChangeF={(attachable) => {
//								props.original._onChildUpdate(props.original._componentPath+'._links.attachment.href',attachable._links.self.href);
//							}}
//						/>,
//					}
//				]
//	},
//	plannedIncomeOrExpenses: {
//		className: "PlannedIncomeOrExpense",
//		label: "Планирани приходи и разходи за задачата",
//		icon: "exchange-alt",
//		aclRestrictable: true,
//		columns: [
//					{
//						Header: "Задача",
//						accessor: 'task',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "tasks",
//					},{
//						Header: "Артикул",
//						accessor: 'article',
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "articles",
//					}, {
//						Header: "Цена",
//						accessor: 'article.articlePriceRates.currency',
//						filterable: false,
//						sortable: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						Cell: (props) => <FieldArticlePriceRateContainer
//							articleId={props.original && props.original.article ? props.original.article.id : undefined}
//							componentPath={props.original._componentPath+'.article.articlePriceRates'} //existing path in redux store where we put data
//							editable={props.original._asTable ? (props.original._editable ? false : undefined) : false}
//							loading={props.original[Constants.PATH_FOR_LOADING]}
//							onChange={(href) => {props.original._onChildUpdate();}}
//						/>
//					}, {
//						Header: 'Кол-во',
//						accessor: 'ammount',
//						filterable: false,
//						fluidSize: 2,
//						dataType: "UNIT",
//						Cell: (props) => <FieldUnitContainer
//							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
//							editable={props.original._editable}
//							loading={props.original[Constants.PATH_FOR_LOADING]}
//							convertToDataF={(ammount) => (ammount)}
//							convertFromDataF={(ammount) => (ammount)}
//							onChange={(href) => {props.original._onChildUpdate();}}
//						/>
//					}, {
//						Header: 'Сума',
//						columns: [{
//							id: 'revenue_sum',
//							Header: 'Приход',
//							isCalculated: true,
//							filterable: false,
//							sortable: false,
//							fluidSize: 2,
//							dataType: "UNIT",
//							Cell: (props) => {
//								//console.log('props.original',props.original);
//								let children_articlePriceRate = props.original && props.original.article && props.original.article.articlePriceRates
//									? props.original.article.articlePriceRates : undefined;
//								let value = props.original && children_articlePriceRate ? props.original.ammount*children_articlePriceRate.price : "N/A";
//								return <input className="form-control" type="text" value={value>0 ? value : 0} disabled={true}/>;
//							}
//						}, {
//							id: 'expense_sum',
//							Header: 'Разход',
//							isCalculated: true,
//							filterable: false,
//							sortable: false,
//							fluidSize: 2,
//							dataType: "UNIT",
//							Cell: (props) => {
//								let children_articlePriceRate = props.original && props.original.article && props.original.article.articlePriceRates
//									? props.original.article.articlePriceRates : undefined;
//								let value = props.original && children_articlePriceRate ? props.original.ammount*children_articlePriceRate.price : "N/A";
//								return <input className="form-control" type="text" value={value < 0 ? value : 0} disabled={true}/>;
//							}
//						}]
//					}
//				]
//	},
//	plannedTimes: {
//		className: "PlannedTime",
//		label: "Планирани ангажирани за задачата",
//		icon: "clock",
//		columns: [
//					{
//						Header: "Задача",
//						accessor: 'task',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "tasks",
//					},{
//						Header: "Име",
//						accessor: 'resource',
//						fluidSize: 4,
//						dataType: "ENTITY",
//						entityType: "secUsers",
//					}, {
//						Header: "Цена на час",
//						accessor: 'resource.timeChargeRates.chargeRatePerHour',
//						filterable: false,
//						fluidSize: 2,
//						dataType: "UNIT",
//						Cell: (props) => <FieldTimeChargeRateContainer
//							resourceId={props.original && props.original.resource ? props.original.resource.id : undefined}
//							componentPath={props.original._componentPath+'.resource.timeChargeRates'} //existing path in redux store where we put data
//							nomenclatureKey={'timeChargeRates'}
//							editable={props.original._asTable ? (props.original._editable ? false : undefined) : false}
//							loading={props.original[Constants.PATH_FOR_LOADING] || !props.original.resource || props.original.resource[Constants.PATH_FOR_LOADING]}
//							onChange={(href) => {props.original._onChildUpdate();}}
//						/>
//					}, {
//						Header: "Часове",
//						accessor: 'minutes',
//						filterable: false,
//						fluidSize: 2,
//						dataType: "UNIT",
//						Cell: (props) => <FieldUnitContainer
//							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
//							editable={props.original._editable}
//							loading={props.original[Constants.PATH_FOR_LOADING]}
//							convertToDataF={(hours) => (hours*60)}
//							convertFromDataF={(minutes) => (minutes/60)}
//							onChange={(minutes) => {props.original._onChildUpdate();}}
//						/>
//					}, {
//						id: "sum",
//						Header: "Сума",
//						isCalculated: true,
//						filterable: false,
//						sortable: false,
//						fluidSize: 2,
//						dataType: "UNIT",
//						Cell: (props) => {
//							let children_timeChargeRate = props.original && props.original.resource && props.original.resource.timeChargeRates && props.original.resource.timeChargeRates._embedded && props.original.resource.timeChargeRates._embedded.timeChargeRates
//								? props.original.resource.timeChargeRates._embedded.timeChargeRates[0] : undefined;
//							let value = props.original && children_timeChargeRate ? props.original.minutes/60*children_timeChargeRate.chargeRatePerHour : "N/A";
//							return <input className="form-control" type="text" value={value} disabled={true}/>;
//						}
//					}
//				]
//	},
//	timeSheetItems: {
//		className: "TimeSheetItem",
//		label: "Положен труд",
//		icon: "clock",
//		columns: [
//					{
//						Header: "Задача",
//						accessor: 'task',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "tasks",
//					},{
//						Header: "Име",
//						accessor: 'resource',
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "secUsers",
//					}, {
//						Header: "Цена на час",
//						accessor: 'resource.timeChargeRates.chargeRatePerHour',  //TODO check if rate should be selected by some criteria
//						filterable: false,
//						fluidSize: 1,
//						dataType: "UNIT",
//						Cell: (props) => <FieldTimeChargeRateContainer
//							resourceId={props.original && props.original.resource ? props.original.resource.id : undefined}
//							componentPath={props.original._componentPath+'.resource.timeChargeRates'} //existing path in redux store where we put data
//							nomenclatureKey={'timeChargeRates'}
//							editable={props.original._asTable ? (props.original._editable ? false : undefined) : false}
//							loading={props.original[Constants.PATH_FOR_LOADING]}
//							onChange={(href) => {props.original._onChildUpdate();}}
//						/>
//					}, {
//						Header: "Начален час",
//						accessor: 'fromTime',
//						filterable: false,
//						fluidSize: 2,
//						dataType: "TIMESTAMP",
//						Cell: (props) => <FieldTimestampContainer
//							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath}
//							editable={props.original._editable}
//							loading={props.original[Constants.PATH_FOR_LOADING]}
//							onChange={(e) => {props.original._onChildUpdate();}}
//							showTimeSelect
//						/>
//					}, {
//						Header: "Краен час",
//						accessor: 'toTime',
//						filterable: false,
//						fluidSize: 2,
//						dataType: "TIMESTAMP",
//						Cell: (props) => <FieldTimestampContainer
//							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath}
//							editable={props.original._editable}
//							loading={props.original[Constants.PATH_FOR_LOADING]}
//							onChange={(e) => {props.original._onChildUpdate();}}
//							showTimeSelect
//						/>
//					}, {
//						Header: "Часове",
//						accessor: 'hours',
//						filterable: false,
//						fluidSize: 1,
//						dataType: "UNIT",
//						Cell: (props) => <input className="form-control" type="text" value={(new Date(props.original.toTime) - new Date(props.original.fromTime))/1000/60/60} disabled={true}/>
//					}, {
//						id: "sum",
//						Header: "Сума",
//						isCalculated: true,
//						filterable: false,
//						sortable: false,
//						fluidSize: 1,
//						dataType: "UNIT",
//						Cell: (props) => {
//							let children_timeChargeRate = props.original && props.original.resource && props.original.resource.timeChargeRates && props.original.resource.timeChargeRates._embedded && props.original.resource.timeChargeRates._embedded.timeChargeRates
//								? props.original.resource.timeChargeRates._embedded.timeChargeRates[0] : undefined;
//							let hours = (new Date(props.original.toTime) - new Date(props.original.fromTime))/1000/60/60;
//							let value = props.original && children_timeChargeRate ? hours*children_timeChargeRate.chargeRatePerHour : "N/A";
//							return <input className="form-control" type="text" value={value} disabled={true}/>;
//						}
//					},{
//						Header: "Коментар",
//						accessor: 'description',
//						fluidSize: 10,
//						dataType: "TEXTAREA",
//					}
//				]
//	},
//	tasks: {
//		className: "Task",
//		label: "Задачи",
//		pageURL: "/tasks",
//		displayAttr: "title",
//		aclRestrictable: true,
//		columns: [
//					{
//						Header: "Статус на задачата",
//						accessor: "status",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskStatuses",
//						editableColumn: true,
//					}, {
//						Header: "Тип на задачата",
//						accessor: "type",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskTypes",
//						editableColumn: true,
//					}, {
//						Header: "Приоритет на задачата",
//						accessor: "priority",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskPriorities",
//						editableColumn: true,
//					}, {
//						Header: "Заглавие на задачата",
//						accessor: 'title',
//						fluidSize: 4,
//						dataType: "TEXT",
//						isLink: true,
//						pageURL: "/tasks",
//					}, {
//						Header: "Описание на задачата",
//						accessor: 'description',
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					}, {
//						Header: "Контрагент на задачата",
//						accessor: "counterParty",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "legalPersons",
//						editableColumn: true,
//					}, {
//						Header: "Изпълнител на задачата",
//						accessor: "assigned",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "secUsers",
//						editableColumn: true,
//					}, {
//						Header: "Краен срок на задачата",
//						accessor: 'deadline',
//						fluidSize: 2,
//						dataType: "DATE",
//						editableColumn: true,
//					}, {
//						Header: "Коментари на задачата",
//						accessor: 'comments',
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "comments",
//						mappedBy: "task",
//					}, {
//						Header: "Прикачени документи към задачата",
//						accessor: 'taskAttachments',
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskAttachments",
//						mappedBy: "task",
//					}, {
//						Header: "Надзадачи",
//						accessor: "relationsToTask",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskRelations",
//						mappedBy: "fromTask",
//					}, {
//						Header: "Подзадачи",
//						accessor: "relationsFromTask",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskRelations",
//						mappedBy: "toTask",
//					}
//				]
//	},
}

export default taskEntityDefinitions
