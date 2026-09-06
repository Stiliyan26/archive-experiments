import React from 'react';
import { withTranslation } from 'react-i18next';

class Header extends React.Component {

	render() {
		document.title = this.props.t("ModuleName") + " " + this.props.text;
		return (
			<h1 className={this.props.class}>
			 	{this.props.text}
			</h1>
		);
	}
}

Header.displayName = 'GenericHeader';
Header.propTypes = {};
Header.defaultProps = {};

export default withTranslation()(Header);