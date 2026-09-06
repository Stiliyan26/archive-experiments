import React from 'react';

import FieldAttachableContainer from '../fields/FieldAttachableContainer'
import FieldMultiSelectOrEditContainer from '../fields/FieldMultiSelectOrEditContainer'

const employeeEntityDefinitions = {
	jobPositions: {
		className: 'JobPosition',
		label: 'Длъжности',
		icon: "user",
		displayAttr: 'name',
		columns: [{
				Header: 'Код',
				accessor: 'code',
				dataType: 'TEXT'
			},{
				Header: 'Наименование',
				accessor: 'name',
				dataType: 'TEXT'
			},
		]
	},
	companyDepartments: {
		className: 'CompanyDepartment',
		label: 'Отдели',
		icon: "user",
		displayAttr: 'name',
		columns: [{
				Header: 'Наименование',
				accessor: 'name',
				dataType: 'TEXT'
			},{
				Header: 'Подотдел на',
				accessor: 'parentDepartment',
				dataType: 'ENTITY',
				entityType: 'companyDepartments'
			},
		]
	},
	employees: {
		className: 'Employee',
		label: 'Служители',
		displayAttr: "employeeEgn",
		displayFn: ((item) => {return item.employeeName + " (" + item.employeeEgn + ")";}),
		icon: "user",
		pageURL: "/employees",
		columns: [{
				Header: 'Име',
				accessor: 'employeeName',
				dataType: "TEXT",
				isLink: true,
				pageURL: "/employees",
				isRequired: true,
			},{
				Header: 'ЕГН',
				accessor: 'employeeEgn',
				dataType: "TEXT",
				isLink: true,
				pageURL: "/employees",
				isRequired: true,
			},{
				Header: 'Адрес',
				accessor: 'employeeAddress',
				dataType: 'TEXT'
			},{
				Header: 'Личен телефон',
				accessor: 'businessPhone',
				dataType: 'TEXT'
			},{
				Header: 'Служебен телефон',
				accessor: 'personalPhone',
				dataType: 'TEXT'
			},{
				Header: 'Потребител',
				accessor: 'secUser',
				dataType: 'ENTITY',
				entityType: 'secUsers'
			},{
				Header: 'Длъжност',
				accessor: 'position',
				dataType: 'ENTITY',
				entityType: 'jobPositions'
			},{
				Header: 'Отдел',
				accessor: 'department',
				show: false,
				dataType: 'ENTITY',
				entityType: 'companyDepartments'
//			},{
//				Header: "Оценка на служителя",
//				accessor: 'employeeAttestations',
//				show: false,
//				fluidSize: 2,
//				dataType: "ENTITY",
//				entityType: "employeeAttestations",
//				mappedBy: "employee",
			}, {
				Header: "Прикачени документи към служителя",
				accessor: "employeeAttachments",
				show: false,
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "employeeAttachments",
				mappedBy: "employee",
			}, {
				Header: "Курсове на служителя",
				accessor: "transportsForDrivers",
				show: false,
				fluidSize: 2,
				dataType: "FILTERED_ENTITY",
				entityType: "transports",
				mappedBy: "driver",
			}, 
			{
				Header: "Разпределения",
				accessor: "allocationProxies",
				show: false,
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "allocationProxies",
				mappedBy: "allocationOrigin",
//			}, {
//				Header: "Дата на последна оценка",
//				accessor: "lastAttestationDate",
//				isAggregate: true,
//				filterable: true,
//				sortable: false,
//				aggregation: {
//					op: "max",
//					operands: ["employeeAttestations.attestationToDate"]
//				},
//				fluidSize: 2,
//				dataType: "DATE",
			},
		]
	},
	employeeAttachments: {
		className: "EmployeeAttachment",
		label: "Прилежащи документи на служителя",
		icon: "folder",
		displayAttr: "description",
		columns: [
					{
						Header: "Служител",
						accessor: 'employee',
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "employees",
					},{
						Header: "Описание",
						accessor: 'description',
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Документ",
						accessor: 'attachmentToEmployee',
						fluidSize: 4,
						dataType: "ENTITY",
						entityType: "attachables",
						Cell: (props) => <FieldAttachableContainer
							href={props.original && props.original._links && props.original._links.attachmentToEmployee ? props.original._links.attachmentToEmployee.href : undefined}
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							editable={props.original._editable}
							attachedToObjectData={props.original ? props.original.employee : undefined}
							onChangeF={(attachable) => {
								props.original._onChildUpdate(props.original._componentPath+'._links.attachmentToEmployee.href',attachable._links.self.href);
							}}
							excludeAttachmentTypes={["mailTemplates"]}
						/>,
					}
				]
	},
//	employeeAttestations: {
//		className: "EmployeeAttestation",
//		pageURL: "/employeeAttestations",
//		label: "Оценки на служителите",
//		displayAttr: "name",
//		columns: [
//					{
//						Header: "Наименование",
//						accessor: 'name',
//						fluidSize: 3,
//						dataType: "TEXT",
//					},{
//						Header: "Служител",
//						accessor: 'employee',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "employees",
//					},{
//						Header: "Оценяващ",
//						accessor: 'certifier',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "employees",
//					},{
//						Header: "Наблюдаващ",
//						accessor: 'watcher',
//						fluidSize: 3,
//						dataType: "ENTITY",
//						entityType: "employees",
//					},{
//						Header: "Оценка за периода от",
//						accessor: 'attestationFromDate',
//						fluidSize: 2,
//						dataType: "DATE",
//					},{
//						Header: "Оценка за периода до",
//						accessor: 'attestationToDate',
//						fluidSize: 2,
//						dataType: "DATE",
//					},{
//						Header: "Цели и задачи за служителя",
//						accessor: 'employeeGoals',
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					},{
//						Header: "Оценка",
//						accessor: 'attestationText',
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					},{
//						Header: "Мотиви",
//						accessor: 'motivesText',
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					},{
//						Header: "Потенциал за развитие",
//						accessor: 'potentialText',
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					},{
//						Header: "Коментар на служителя",
//						accessor: 'employeeCommentText',
//						fluidSize: 12,
//						dataType: "TEXTAREA",
//					},{
//						Header: "Коментар на контролиращия ръководител",
//						accessor: 'controllingOfficerCommentText',
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
//							temp["EmployeeAttestation."+curr.id + ".text"] = curr.value;
//							return temp;
//						}
//					}
//				]
//	},
	jobRequirements: {
		className: 'JobRequirement',
		label: 'Компетенции',
		icon: "graduation-cap",
		displayAttr: 'name',
		columns: [{
				Header: 'Код',
				accessor: 'code',
				dataType: 'TEXT'
			},{
				Header: 'Наименование',
				accessor: 'name',
				dataType: 'TEXT'
			},
		]
	},
//	jobPositionRequirements: {
//		className: 'JobPositionRequirement',
//		label: 'Длъжностни изисквания',
//		icon: "graduation-cap",
//		displayAttr: 'comment',
//		columns: [{
//				Header: 'Коментар',
//				accessor: 'comment',
//				dataType: 'TEXT'
//			},{
//				Header: 'Длъжност',
//				accessor: 'position',
//				dataType: 'ENTITY',
//				entityType: 'jobPositions'
//			},{
//				Header: 'Изискване',
//				accessor: 'requirement',
//				dataType: 'ENTITY',
//				entityType: 'jobRequirements'
//			},
//		]
//	},
	employeeCompetences: {
		className: 'EmployeeCompetence',
		label: 'Компетенции на служителите',
		icon: "graduation-cap",
		displayAttr: 'comment',
		columns: [{
				Header: 'Коментар',
				accessor: 'comment',
				fluidSize: 3,
				dataType: 'TEXT'
			},{
				Header: 'Служител',
				accessor: 'employee',
				fluidSize: 3,
				dataType: 'ENTITY',
				entityType: 'employees'
			},{
				Header: 'Компетенция',
				accessor: 'competence',
				fluidSize: 3,
				dataType: 'ENTITY',
				entityType: 'jobRequirements'
			},{
				Header: "Документ",
				accessor: "document",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "attachables",
				Cell: (props) => <FieldAttachableContainer
					href={props.original && props.original._links && props.original._links.document ? props.original._links.document.href : undefined}
					componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
					editable={props.original._editable}
					attachedToObjectData={props.original ? props.original : undefined}
					onChangeF={(attachable) => {
						props.original._onChildUpdate(props.original._componentPath+'._links.document.href',attachable._links.self.href);
					}}
					excludeAttachmentTypes={["mailTemplates","mailMessages","sendMailMessages","/offerToClients","employeeAttestations","expenditures","incomes"]}
				/>,
			},
		]
	},
}

export default employeeEntityDefinitions
