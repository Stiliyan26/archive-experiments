import React from "react";
import axios from "axios";

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"
import { Button, Card, Form } from 'react-bootstrap';

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { withTranslation } from "react-i18next";

import * as Constants from "../../static/constants";
import history from "../../scripts/history";
import { isAuthenticated } from "../../components/pages/login/Login.js";

import NewHeader from "../../components/generic/NewHeader";
import EmbedEntityContainer from "../embeds/EmbedEntityContainer";
import EmbedChangeHistory from "../embeds/EmbedChangeHistory";

import { getEntityDefinition } from "../nomenclatures/entityDefinitions.js";
import { resolveObjectPath } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer";
import { selfieEntities } from "../selfie/constants/selfieEntities";
import { LOGIN } from "../selfie/constants/selfiePaths.js";

const X_AUTH_TOKEN = "X-AUTH-TOKEN"
//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewPowerPlantContainer extends React.Component {
	componentDidMount() {
		if (!isAuthenticated(this.props.auth)) {
			history.push(LOGIN);
		}
		// this.getLastUpdatedPowerPlant()
	}

	updatePowerPlant() {
		console.log("PageViewPowerPlantContainer -> updatePowerPlant() -> this.props: ", this.props.data.accessPoint)
		let accessPoint = this.props.data.accessPoint
		// let accessPoint = "4632006.0"

		axios({
			method: "post",
			url: API_URL + `/reports/power-plant/synchronize`,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] },
			data: {
				"mpid": accessPoint
			}
		}).then(resPowerPlant => {
			console.log("updatePowerPlant() -> resPowerPlant: ", resPowerPlant)

			axios({
				method: "post",
				url: API_URL + `/reports/agreement-self-invoicing/synchronize`,
				headers: { Authorization: sessionStorage[X_AUTH_TOKEN] },
				data: {
					"mpid": accessPoint
				}
			}).then(resAgreementSelfInvoicing => {
				console.log("updatePowerPlant() -> resAgreementSelfInvoicing: ", resAgreementSelfInvoicing)

			}).catch(error => {
				console.log("PageViewPowerPlantContainer.updatePowerPlant().agreement-self-invoicing.axios.error" + error);
			});

		}).catch(error => {
			console.log("PageViewPowerPlantContainer.updatePowerPlant().axios.error" + error);
		});
	}


	render() {

		let body = "";

		if (this.props.data) {
			// console.log("PageViewPowerPlantContainer -> this.props: ", this.props.data.id)
			if (this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin />;

			} else if (this.props.id && this.props.id == parseInt(this.props.match.params.entity_id, 10)) {
				const agreementSelfInvoicingsDef = getEntityDefinition("agreementSelfInvoicings", {});

				body = <div className="page-body">
					<EmbedRetrieveEntityListContainer
						title={agreementSelfInvoicingsDef.label}
						icon={agreementSelfInvoicingsDef.icon}
						columns={agreementSelfInvoicingsDef.columns}
						componentPath="powerPlantView.agreementSelfInvoicings"
						retrieveType="agreementSelfInvoicings"
						parentHref={this.props.href}
						parentAttr="powerPlant"
						parentData={this.props.data}
						asTable={true}
					/>

					<EmbedChangeHistory
						entity_id={this.props.id}
						componentPath={`${selfieEntities.PowerPlant.view}.changelog`}
						retrieveType={this.props.entityName}
					/>
				</div>;
			}
		}

		return (
			<div className="page-body-wrapper">
				<NewHeader text={this.props.headerText} auth={this.props.auth.userAuthenticated} />

				<EmbedEntityContainer
					componentPath={`${selfieEntities.PowerPlant.view}.${selfieEntities.PowerPlant.singleCamelCase}`}
					loading={this.props.loading}
					onBeforeChange={(data) => {
						if (!this.props.entityDef.pageURL) {
							console.error("pageURL is missing for entity " + this.props.entityDef.className);
						}

						if (data.id) {
							history.push(`/${selfieEntities.PowerPlant.pluralCamelCase}/` + data.id);
						} else {
							history.push(`/${selfieEntities.PowerPlant.pluralCamelCase}/add`);
						}
					}}
					retrieve_id={this.props.match.params.entity_id}
					entityName={selfieEntities.PowerPlant.pluralCamelCase}
					headerText={this.props.t(`${selfieEntities.PowerPlant.className}._className`)}
					expanded={true}
					creatable={false}
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
				
					<div>
						<Card>
							<Card.Footer>
								<div style={{ display: "flex", flexDirection: "column", alignItems: "start", justifyContent: "center" }}>
									<button className="button" onClick={() => { this.updatePowerPlant() }}>
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
	const entityName = "powerPlants"
	const entityDef = getEntityDefinition(entityName, undefined);
	const componentPath = selfieEntities.PowerPlant.view;

	let data = resolveObjectPath(componentPath + "." + selfieEntities.PowerPlant.singleCamelCase + "._embedded." + entityName + ".0", state.rest);

	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t(`${selfieEntities.PowerPlant.className}._className`),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {}), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewPowerPlantContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);