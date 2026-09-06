import React from 'react';

const companyLogo = require('../../images/companyLogo.png')

const CompanyLogo = () => (<img className="nav-icon" style={{ display: "flex", justifyContent: "center", alignItems: "center", width: '200px', height: '120px' }} src={companyLogo} alt="EVN" />);
CompanyLogo.displayName = 'CompanyLogo';

export default CompanyLogo;