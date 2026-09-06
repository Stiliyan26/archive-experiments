import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import history from './../scripts/history'
import { isAuthenticated } from './../components/pages/login/Login.js'

import Header from './../components/generic/Header'
import NewHeader from '../components/generic/NewHeader.js';
import Select from './OverflowSelect'

import { getCombinedEntityDefinitions, getEntityDefinition } from './nomenclatures/entityDefinitions.js'
import { resetRESTCallLimit } from './../actions/taskActions';
import EmbedRetrieveEntityListContainer from './nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//Container: redux container class
class PageNomenclaturesContainer extends React.Component {

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
		let definitions = getCombinedEntityDefinitions();
		for(let key in definitions) {
			let label = definitions[key].label;
			if(definitions[key].className != undefined) {
				label = this.props.t(definitions[key].className+"._className_plural");
			}
			if(label != undefined) {
				nomenclatures.push({label: label, value: key});
			}
		}
		const nomenclature = getEntityDefinition(this.props.match.params.selectedNomenclature);
		let selectedNomenclature = undefined;
		let body;
		if(nomenclature) {
			body = nomenclature.body;
			if(body === undefined) {
				body = <EmbedRetrieveEntityListContainer
						key={this.props.match.params.selectedNomenclature}
						title={nomenclature.label}
						icon={nomenclature.icon}
						columns={nomenclature.columns}
						componentPath={"nomenclatures."+this.props.match.params.selectedNomenclature}
						retrieveType={this.props.match.params.selectedNomenclature}
						expanded={true}
						asTable={true}
						creatable={nomenclature.creatable}
					/>;
			}
			//must be the same object from the array, because of === indexOf compare in Select
			selectedNomenclature = nomenclatures.find((elem) => (elem.value == this.props.match.params.selectedNomenclature));
		}
		return (
			<div className="nomenclatures-wrapper">
				{/* <Header text={this.props.t("Nomenclatures")} class='page-header text-align-center no-margin' /> */}
				<NewHeader text={this.props.t("Nomenclatures")} auth={this.props.auth.userAuthenticated}/>
				<div className='page-body'>
					<div className="col-sm-12 form-group">
						<Select
							className="col-sm-12 no-padding"
							name="form-field-name"
							options={nomenclatures}
							onChange={(e) => {resetRESTCallLimit(); history.push('/nomenclatures/'+e.value);}}
							value={selectedNomenclature}
							placeholder={this.props.t("ChooseNomenclature")}
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
	const data = state.rest.taskView ? state.rest.taskView.task : undefined;
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

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageNomenclaturesContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
