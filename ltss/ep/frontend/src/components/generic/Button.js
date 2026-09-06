import React from 'react';
import classNames from 'classnames'
class Button extends React.Component {

	constructor(props){
		super(props);
		this.state = {};
	}

	render() {
		const { disabled } = this.props;
		return (
			<div className={this.props.class} style={this.props.style} onClick={disabled ? null : this.props.onClick } disabled={disabled}>
				{this.props.text}
			</div>
		)
	}
}

Button.displayName = 'GenericButton';
Button.propTypes = {};
Button.defaultProps = {};

export default Button;
