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
class PageTrainingsContainer extends React.Component {

	constructor(props){
		super(props);
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}

	render() {
		const trainingEntityDefinitions = {
				employees: {
					label: this.props.t("Employee._className_plural"),
				},
				jobPositions: {
					label: this.props.t("JobPosition._className_plural"),
				},
				jobRequirements: {
					label: this.props.t("JobRequirement._className_plural"),
				},
				jobPositionRequirements: {
					label: this.props.t("JobPositionRequirement._className_plural"),
				},
				employeeCompetences: {
					label: this.props.t("EmployeeCompetence._className_plural"),
				}
			}
		
		const nomenclatures = [];
		for(let key in trainingEntityDefinitions) {
			if(trainingEntityDefinitions[key].label){
				nomenclatures.push({label: trainingEntityDefinitions[key].label, value: key});
			}
		}
		const nomenclature = getEntityDefinition(this.props.match.params.selectedTraining);
		let selectedNomenclature = undefined;
		let body;
		if(nomenclature) {
			body = nomenclature.body;
			if(body === undefined) {
				body = <EmbedRetrieveEntityListContainer
						key={this.props.match.params.selectedTraining}
						title={nomenclature.label}
						icon={nomenclature.icon}
						columns={nomenclature.columns}
						componentPath={"training."+this.props.match.params.selectedTraining}
						retrieveType={this.props.match.params.selectedTraining}
						expanded={true}
						asTable={true}
					/>;
			}
			selectedNomenclature = {value: this.props.match.params.selectedTraining, label: nomenclature.label};
		}
		return (
			<div className="nomenclatures-wrapper">
				<Header text={this.props.t("Trainings")} class='page-header text-align-center no-margin' />
				<div className='page-body'>
					<div className="col-sm-12 form-group">
						<Select
							className="col-sm-12 no-padding"
							name="form-field-name"
							options={nomenclatures}
							onChange={(e) => {resetRESTCallLimit(); history.push('/trainings/'+e.value);}}
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTrainingsContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
