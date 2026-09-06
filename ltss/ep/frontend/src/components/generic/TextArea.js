import React from 'react';

export default class TextArea extends React.Component {

	constructor(props){
		super(props);
	}

	render() {
		return (
			<textarea 	className={this.props.class}
					cols={this.props.cols}
					rows={this.props.rows}
					placeholder={this.props.placeholder}>
			</textarea>
		);
	}
}

TextArea.displayName = 'GenericTextArea';
TextArea.propTypes = {};
TextArea.defaultProps = {};
