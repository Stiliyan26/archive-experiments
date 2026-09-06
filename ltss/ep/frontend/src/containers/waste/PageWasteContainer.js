import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import Select from '../OverflowSelect'

import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'
import { resetRESTCallLimit } from '../../actions/taskActions';
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//Container: redux container class
class PageWasteContainer extends React.Component {

	constructor(props){
		super(props);
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}

	render() {
		const nomenclatures = [];
		const wasteEntityDefinitions = {
				wastes: {
					label: this.props.t("Waste.Waste"),
				},
				wasteCostCenters: {
					label: this.props.t("Waste.CostCenters"),
				},
				wasteTypes: {
					label: this.props.t("Waste.Types"),
				},
				wasteKinds: {
					label: this.props.t("Waste.Kinds"),
				},
				wasteAreas: {
					label: this.props.t("Waste.Areas"),
				}
			}
		for(let key in wasteEntityDefinitions) {
			if(wasteEntityDefinitions[key].label){
				nomenclatures.push({label: wasteEntityDefinitions[key].label, value: key});
			}
		}
		const nomenclature = getEntityDefinition(this.props.match.params.selectedWaste);
		let selectedNomenclature = undefined;
		let body;
		if(nomenclature) {
			body = nomenclature.body;
			if(body === undefined) {
				body = <EmbedRetrieveEntityListContainer
						key={this.props.match.params.selectedWaste}
						title={nomenclature.label}
						icon={nomenclature.icon}
						columns={nomenclature.columns}
						componentPath={"waste."+this.props.match.params.selectedWaste}
						retrieveType={this.props.match.params.selectedWaste}
						expanded={true}
						asTable={true}
					/>;
			}
			selectedNomenclature = {value: this.props.match.params.selectedWaste, label: nomenclature.label};
		}
		return (
			<div className="nomenclatures-wrapper">
				<Header text={this.props.t("Waste.Waste")} class='page-header text-align-center no-margin' />
				<div className='page-body'>
					<div className="col-sm-12 form-group">
						<Select
							className="col-sm-12 no-padding"
							name="form-field-name"
							options={nomenclatures}
							onChange={(e) => {resetRESTCallLimit(); history.push('/waste/'+e.value);}}
							value={selectedNomenclature}
							placeholder={this.props.t("Waste.ChooseWaste")}
						/>
					</div>
					<div className='col-sm-12'>
						{body}
					</div>
				</div>
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	return {
		auth: state.auth,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageWasteContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
