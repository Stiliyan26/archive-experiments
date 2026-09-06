import React from 'react';

const companyLogo = require('../../images/companyLogo-white.svg')

const CompanyLogo = () => (<img className="nav-icon" style={{ width: '137px', height: '53px', marginLeft: 'auto', marginRight: 'auto', display: 'block' }} src={companyLogo} alt="About us" />);
CompanyLogo.displayName = 'CompanyLogo';

export default CompanyLogo;
