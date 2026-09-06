import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { Button, Card, Form } from 'react-bootstrap';
import axios from "axios";

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants';
import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import NewHeader from '../../components/generic/NewHeader'
import EmbedEntityContainer from '../embeds/EmbedEntityContainer'
//import EmbedCustomerContainer from './EmbedCustomerContainer'
//import EmbedVendorContainer from './EmbedVendorContainer'
import EmbedChangeHistory from '../embeds/EmbedChangeHistory'

import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

const X_AUTH_TOKEN = "X-AUTH-TOKEN"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewLegalPersonContainer extends React.Component {
	componentDidMount() {
		if (!isAuthenticated(this.props.auth)) {
			history.push('/login')
		}
	}

	updateLegalPerson() {
		console.log("PageViewPowerPlantContainer -> updatePowerPlant() -> this.props: ", this.props.data.eik)
		let eik = this.props.data.eik
		// let eik = "4632006.0"

		axios({
			method: "post",
			url: API_URL + `/reports/legal-person/synchronize`,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] },
			data: {
				"eik": eik
			}
		}).then(resLegalPerson => {
			console.log("updateLegalPerson() -> resLegalPerson: ", resLegalPerson)

		}).catch(error => {
			console.log("PageViewPowerPlantContainer.updateLegalPerson().axios.error" + error);
		});
	}

	render() {
		let body = '';
		if (this.props.data) {
			if (this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin />;
			} else if (this.props.id && this.props.id == parseInt(this.props.match.params.person_id, 10)) {
				// const energyBalancingContractsDef = getEntityDefinition("energyBalancingContracts",{owner: {show: false}});
				const powerPlantsDef = getEntityDefinition("powerPlants", {
					owner: { show: false }, traderEic: { show: "hidden" }, address: { show: "hidden" }, contactPerson: { show: "hidden" },
					contractId: { show: "hidden" }, contractDate: { show: "hidden" }, annex: { show: "hidden" }, term: { show: "hidden" },
					identificationNumber: { show: "hidden" }, distributionNetwork: { show: "hidden" }, value: { show: "hidden" }, valueSec: { show: "hidden" },
					minPriceMWh: { show: "hidden" }, grid: { show: "hidden" }, contractStatus: { show: "hidden" }, loiContractQuantity: { show: "hidden" },
					loiProtocolCountPerMonth: { show: "hidden" }, loiProtocolLineCount: { show: "hidden" }, loiContractPrice: { show: "hidden" }, loiContractFee: { show: "hidden" }
				});
				const documentRangesDef = getEntityDefinition("documentRanges", {});

				const contactsDef = getEntityDefinition("contacts", {});

				// const attachmentDef = getEntityDefinition("legalPersonAttachments",{person: {show: false}});
				// const contactsDef = getEntityDefinition("contacts",{person: {show: false}});
				const bankAccountsDef = getEntityDefinition("bankAccounts", { bankAccountOwner: { show: false } });
				// const commentsDef = getEntityDefinition("legalPersonComments",{person: {show: false}});
				body = <div className='page-body'>
					{/* <EmbedRetrieveEntityListContainer
							title={energyBalancingContractsDef.label}
							icon={energyBalancingContractsDef.icon}
							columns={energyBalancingContractsDef.columns}
							componentPath="legalPersonView.energyBalancingContracts"
							retrieveType="energyBalancingContracts"
							parentHref={this.props.href}
							parentAttr="owner"
							parentData={this.props.data}
						/> */}
					<EmbedRetrieveEntityListContainer
						title={contactsDef.label}
						icon={contactsDef.icon}
						columns={contactsDef.columns}
						componentPath="legalPersonView.contacts"
						retrieveType="contacts"
						parentHref={this.props.href}
						parentAttr="person"
						parentData={this.props.data}
						asTable={true}
					/>
					<EmbedRetrieveEntityListContainer
						title={powerPlantsDef.label}
						icon={powerPlantsDef.icon}
						columns={powerPlantsDef.columns}
						componentPath="legalPersonView.powerPlants"
						retrieveType="powerPlants"
						parentHref={this.props.href}
						parentAttr="owner"
						parentData={this.props.data}
						asTable={true}
					/>
					<EmbedRetrieveEntityListContainer
						title={documentRangesDef.label}
						icon={documentRangesDef.icon}
						columns={documentRangesDef.columns}
						componentPath="legalPersonView.documentRanges"
						retrieveType="documentRanges"
						parentHref={this.props.href}
						parentAttr="person"
						parentData={this.props.data}
						asTable={true}
					/>
					{/* <EmbedRetrieveEntityListContainer
							title={contactsDef.label}
							icon={contactsDef.icon}
							columns={contactsDef.columns}
							componentPath="legalPersonView.contacts"
							retrieveType="contacts"
							parentHref={this.props.href}
							parentAttr="person"
							parentData={this.props.data}
						/> */}
					<EmbedRetrieveEntityListContainer
						title={bankAccountsDef.label}
						icon={bankAccountsDef.icon}
						columns={bankAccountsDef.columns}
						componentPath="legalPersonView.bankAccounts"
						retrieveType="bankAccounts"
						parentHref={this.props.href}
						parentAttr="bankAccountOwner"
						parentData={this.props.data}
						asTable={true}
					/>
					{/* TODO make these to be enabled/disabled from some "module settings"
						<EmbedCustomerContainer href={this.props.href} partnerId={this.props.id} componentPath='legalPersonView.customers'/>
						<EmbedVendorContainer href={this.props.href} partnerId={this.props.id} componentPath='legalPersonView.vendors'/>
						*/}
					{/* <EmbedRetrieveEntityListContainer
							title={attachmentDef.label}
							icon={attachmentDef.icon}
							columns={attachmentDef.columns}
							componentPath="legalPersonView.attachments"
							retrieveType="legalPersonAttachments"
							parentHref={this.props.href}
							parentAttr="person"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={commentsDef.label}
							icon={commentsDef.icon}
							columns={commentsDef.columns}
							componentPath="legalPersonView.comments"
							retrieveType="legalPersonComments"
							parentHref={this.props.href}
							parentAttr="person"
							parentData={this.props.data}
						/> */}
					<EmbedChangeHistory entity_id={this.props.id} componentPath='legalPersonView.changelog' retrieveType="legalPersons" />
				</div>;
			}
		}
		return (
			<div className="page-body-wrapper">
				{/* <Header text={this.props.t("LegalPerson._className")} class='page-header text-align-center no-margin' /> */}
				<NewHeader text={this.props.t("LegalPerson._className")} auth={this.props.auth.userAuthenticated} />
				<EmbedEntityContainer
					componentPath='legalPersonView.legalPerson'
					loading={this.props.loading}
					onBeforeChange={(data) => {
						if (data.id) {
							history.push('/legalPersons/' + data.id);
						} else {
							history.push('/legalPersons/add');
						}
					}}
					retrieve_id={this.props.match.params.person_id}
					entityName="legalPersons"
					headerText={this.props.t("LegalPerson._className")}
					expanded={true}
					creatable={true}
					columnOverride={{
						legalStatus: { show: "hidden" },
						legalPersonType: { show: "hidden" },
					}}
				/>
				
					<div>
						<Card>
							<Card.Footer>
								<div style={{ display: "flex", flexDirection: "column", alignItems: "start", justifyContent: "center" }}>
									<button className="button" onClick={() => { this.updateLegalPerson() }}>
										{this.props.t("Update")}
									</button>
								</div>
							</Card.Footer>
						</Card>
					</div>
				
				{body}
			</div>
		);
	}
}

//redux mapping
function mapStateToProps(state, ownProps) {
	const data = state.rest.legalPersonView && state.rest.legalPersonView.legalPerson && state.rest.legalPersonView.legalPerson._embedded ? state.rest.legalPersonView.legalPerson._embedded.legalPersons[0] : undefined;
	return {
		auth: state.auth,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		isPhy: data && data.legalStatus ? data.legalStatus.code == 3 || data.legalStatus.code == 5 : false, //needed as prop to force the re-render when changed
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {}), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewLegalPersonContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
