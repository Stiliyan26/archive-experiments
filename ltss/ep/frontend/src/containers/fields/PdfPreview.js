import React from 'react';
import classNames from 'classnames'
import { Modal } from 'react-bootstrap';
import { Document, Page } from 'react-pdf';

export default class PdfPreview extends React.Component {
	constructor(props){
		super(props);
		this.state= {
		    numPages: null,
    		pageNumber: 1,
    	};
	}
	onDocumentLoad({ numPages }){
		this.setState({ numPages });
  	}
	
	render() {
		const {imageSrc, imageName} = this.props;
	    const { pageNumber, numPages } = this.state;
		let preview
		let imgOctet
		if(imageSrc){
			if(imageSrc.split(';')[0] === 'data:application/pdf'){
				imgOctet = imageSrc.replace('pdf;base64', 'octet-stream;base64')
				// preview = ( <embed className="contracts-image-preview" src={imgOctet} height="99%" /> )
				preview = (
					<div className="contracts-image-preview">
						<Document file={imgOctet} onLoadSuccess={(x)=>this.onDocumentLoad(x)} className="contracts-image-preview" >
							{
								Array.from(
									new Array(numPages),
									(el, index) => (
										<Page
											className="contracts-image-preview"
											key={`page_${index + 1}`}
											pageNumber={index + 1}
											// width={1200}
											scale={2}
										/>
									),
								)
							}
						</Document>
					</div>
				)
			}
			else{
				preview = ( <img className="contracts-image-preview" src={imageSrc}/> )
			}
		}

		return (
			<div>
				{preview}
			</div>
		);
	}
}
