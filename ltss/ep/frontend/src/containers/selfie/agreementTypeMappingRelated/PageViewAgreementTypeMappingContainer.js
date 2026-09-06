import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { withTranslation } from 'react-i18next';

import * as Constants from '../../../static/constants';
import history from '../../../scripts/history';
import { isAuthenticated } from '../../../components/pages/login/Login.js';

import NewHeader from '../../../components/generic/NewHeader';
import EmbedEntityContainer from '../../embeds/EmbedEntityContainer';
//import EmbedCustomerContainer from './EmbedCustomerContainer';
//import EmbedVendorContainer from './EmbedVendorContainer';
import EmbedChangeHistory from '../../embeds/EmbedChangeHistory';

import { getEntityDefinition } from '../../nomenclatures/entityDefinitions.js';
import { selfieEntities } from "../constants/selfieEntities.js";
import { LOGIN } from "../constants/selfiePaths.js";
import EmbedRetrieveEntityListContainer from '../../nomenclatures/EmbedRetrieveEntityListContainer';


// Page: can be used as a landing page
// View: presents details of object
// Container: redux container class
class PageViewAgreementTypeMappingContainer extends React.Component {
    componentDidMount() {
        if (!isAuthenticated(this.props.auth)) {
            history.push(LOGIN);
        }
    }

    render() {
        let body = '';

        if (this.props.data) {
            if (this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
                body = <FontAwesomeIcon icon="spinner" size="2x" spin />;

            } else if (this.props.id && this.props.id == parseInt(this.props.match.params.person_id, 10)) {
                // const powerPlantsDef = getEntityDefinition("powerPlants", { owner: { show: false } });

                body = <div className='page-body'>

                    {/* <EmbedRetrieveEntityListContainer
                        title={powerPlantsDef.label}
                        icon={powerPlantsDef.icon}
                        columns={powerPlantsDef.columns}
                        componentPath="legalPersonView.powerPlants"
                        retrieveType="powerPlants"
                        parentHref={this.props.href}
                        parentAttr="owner"
                        parentData={this.props.data}
                    /> */}

                    <EmbedChangeHistory
                        entity_id={this.props.id}
                        componentPath={`${selfieEntities.AgreementTypeMapping.view}.changelog`}
                        retrieveType={selfieEntities.AgreementTypeMapping.pluralCamelCase}
                    />
                </div>;
            }
        }

        return (
            <div className="page-body-wrapper">
                <NewHeader text={this.props.t(`${selfieEntities.AgreementTypeMapping.className}._className`)} auth={this.props.auth.userAuthenticated} />

                <EmbedEntityContainer
                    componentPath={`${selfieEntities.AgreementTypeMapping.view}.${selfieEntities.AgreementTypeMapping.singleCamelCase}`}
                    loading={this.props.loading}
                    onBeforeChange={(data) => {
                        if (data.id) {
                            history.push(`/${selfieEntities.AgreementTypeMapping.pluralCamelCase}/` + data.id);
                        } else {
                            history.push(`/${selfieEntities.AgreementTypeMapping.pluralCamelCase}/add`);
                        }
                    }}
                    retrieve_id={this.props.match.params.agreementTypeMapping_id}
                    entityName={selfieEntities.AgreementTypeMapping.pluralCamelCase}
                    headerText={this.props.t(`${selfieEntities.AgreementTypeMapping.className}._className`)}
                    expanded={true}
                    creatable={false}
                    columnOverride={
                        {
                            // hasDbFile: { show: "hidden" },
                        }
                    }
                />

                {body}
            </div>
        );
    }
}

// redux mapping
function mapStateToProps(state, ownProps) {
    const data = state.rest.agreementTypeMappingView && state.rest.agreementTypeMappingView.agreementTypeMapping && state.rest.agreementTypeMappingView.agreementTypeMapping._embedded ? state.rest.agreementTypeMappingView.agreementTypeMapping._embedded.agreementTypeMappings[0] : undefined;
    return {
        auth: state.auth,
        data: data,
        href: data && data._links && data._links.self ? data._links.self.href : undefined,
        id: data && data.id ? data.id : undefined,
        // isPhy: data && data.legalStatus ? data.legalStatus.code == 3 || data.legalStatus.code == 5 : false, //needed as prop to force the re-render when changed
    };
}

// redux mapping
function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Object.assign({}, {}), dispatch)
    };
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewAgreementTypeMappingContainer);
const WithRouterComponent = withRouter(ConnectComponent);
// export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
