import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';

import CompanyLogo from '../../components/icons/CompanyLogo'


const PAGE_BODY_WRAPPER = "page-body-wrapper"
const BLUE_COLOR = "#3C5294"
const GREEN_COLOR = "#90B348"

class ManufacturerProtocolPDF extends React.Component {

    constructor(props) {
        super(props)
        this.state = {}
    }

    formatDateParameters(date) {
        return date < 10 ? "0" + date : date
    }

    getCurrentDate() {
        let day = this.formatDateParameters(new Date().getDate())
        let month = this.formatDateParameters(new Date().getMonth() + 1)
        let year = new Date().getFullYear()

        return day + "." + month + "." + year
    }

    getRangeDate() {
        let fromDay = this.formatDateParameters(new Date(this.props.fromDate).getDate())
        let toDay = this.formatDateParameters(new Date(this.props.toDate).getDate())
        let month = ""

        switch (new Date(this.props.fromDate).getMonth()) {
            case 0:
                month = "ЯНУАРИ"
                break;
            case 1:
                month = "ФЕВРУАРИ"
                break;
            case 2:
                month = "МАРТ"
                break;
            case 3:
                month = "АПРИЛ"
                break;
            case 4:
                month = "МАЙ"
                break;
            case 5:
                month = "ЮНИ"
                break;
            case 6:
                month = "ЮЛИ"
                break;
            case 7:
                month = "АВГУСТ"
                break;
            case 8:
                month = "СЕПТЕМВРИ"
                break;
            case 9:
                month = "ОКТОМВРИ"
                break;
            case 10:
                month = "НОЕМВРИ"
                break;
            case 11:
                month = "ДЕКЕМВРИ"
                break;
            default:
        }

        if (new Date(this.props.fromDate).getDate() == 1 && new Date(this.props.toDate).getDate() >= 30) {
            return month
        }

        return fromDay + "-" + toDay + ". " + month
    }

    getYear() {
        return new Date(this.props.fromDate).getFullYear()
    }

    render() {
        const { itn, energy_MHw, price_MHw, sum } = this.props;

        let year = this.getYear()
        let rangeDate = this.getRangeDate()
        let currentDate = this.getCurrentDate()

        let body = ""
        console.log("NumberOfRows: ", this.props.numberOfRows)

        if (this.props.numberOfRows == 1) {
            body = <div className={PAGE_BODY_WRAPPER} style={{ borderStyle: "solid", borderColor: `${BLUE_COLOR} ${GREEN_COLOR} ${GREEN_COLOR} ${BLUE_COLOR}` }}>
                <div style={{ display: "grid", gridTemplateColumns: "200px auto 200px", margin: "20px" }} >
                    <div><CompanyLogo /></div>
                    <div style={{ fontWeight: "bold", textAlign: "center", margin: "auto" }}> Ф Инвест ЕООД </div>
                </div>
                <div style={{ textAlign: "center", marginTop: "20px", fontWeight: "bold" }}> ПРОТОКОЛ </div>
                <div style={{ textAlign: "center", marginTop: "50px", marginBottom: "20px" }}>
                    за продадена електрическа енергия по Договор за покупко-продажба на електрическа енергия за енергиен обект ФтЕЦ Централа 6 на Дружество 6
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "auto auto auto", marginTop: "50px" }}>
                    <div style={{ textAlign: "center" }}>за месец</div>
                    <div style={{ textAlign: "center", fontWeight: "bold" }}> {rangeDate} </div>
                    <div style={{ textAlign: "center" }}>{year} г.</div>
                </div>
                <div style={{ marginTop: "50px", marginLeft: "40px" }}>Днес, {currentDate} г.,</div>
                <div style={{ marginLeft: "20px" }}>Се състави настоящият протокол за продадената електрическа енергия на Ф Инвест ЕООД за периода от</div>
                <div style={{ margin: "50px", display: "grid", gridTemplateColumns: "1fr 1fr 1fr" }}>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>ИТН</div>
                    <div style={{ textAlign: "center", borderLeftStyle: "solid", borderTopStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}`, gridColumn: "2 span" }}>{itn}</div>

                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Енергия за фактуриране, MWh</div>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Единична цена, лв./MWh</div>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Сума без ДДС, лв.</div>

                    <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{energy_MHw}</div>
                    <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{price_MHw} лв.</div>
                    <div style={{ textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{sum} лв.</div>
                </div>

                <div style={{ margin: "50px", display: "grid", gridTemplateColumns: "auto auto" }}>
                    <div style={{ marginTop: "50px", marginBottom: "100px", textAlign: "center", fontWeight: "bold" }}>Подпис за Производителя:</div>
                    <div style={{ marginTop: "50px", marginBottom: "100px", textAlign: "center", fontWeight: "bold" }}>Подпис за Ф Инвест ЕООД :</div>
                </div>
            </div>
        } else if (this.props.numberOfRows == 3) {
            body = <div className={PAGE_BODY_WRAPPER} style={{ borderStyle: "solid", borderColor: `${BLUE_COLOR} ${GREEN_COLOR} ${GREEN_COLOR} ${BLUE_COLOR}` }}>
                <div style={{ display: "grid", gridTemplateColumns: "200px auto 200px", margin: "20px" }} >
                    <div><CompanyLogo /></div>
                    <div style={{ fontWeight: "bold", textAlign: "center", margin: "auto" }}> Ф Инвест ЕООД </div>
                </div>
                <div style={{ textAlign: "center", marginTop: "20px", fontWeight: "bold" }}> ПРОТОКОЛ </div>
                <div style={{ textAlign: "center", marginTop: "50px", marginBottom: "20px" }}>
                    за продадена електрическа енергия по Договор за покупко-продажба на електрическа енергия за енергиен обект ФтЕЦ Централа 6 на Дружество 6
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "auto auto auto", marginTop: "50px" }}>
                    <div style={{ textAlign: "center" }}>за месец</div>
                    <div style={{ textAlign: "center", fontWeight: "bold" }}> {rangeDate} </div>
                    <div style={{ textAlign: "center" }}>{year} г.</div>
                </div>
                <div style={{ marginTop: "50px", marginLeft: "40px" }}>Днес, {currentDate} г.,</div>
                <div style={{ marginLeft: "20px" }}>Се състави настоящият протокол за продадената електрическа енергия на Ф Инвест ЕООД за периода от</div>
                <div style={{ margin: "50px", display: "grid", gridTemplateColumns: "1fr 1fr 1fr" }}>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>ИТН</div>
                    <div style={{ textAlign: "center", borderLeftStyle: "solid", borderTopStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}`, gridColumn: "2 span" }}>{itn}</div>

                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Енергия по график, MWh</div>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Единична цена, лв./MWh</div>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Сума без ДДС, лв.</div>

                    <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{energy_MHw}</div>
                    <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{price_MHw} лв.</div>
                    <div style={{ textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{sum} лв.</div>

                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Балансираща енергия, MWh излишък/недостиг</div>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Единична цена, лв./MWh</div>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Сума без ДДС, лв.</div>

                    <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{energy_MHw}</div>
                    <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{price_MHw} лв.</div>
                    <div style={{ textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{sum} лв.</div>

                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Обща енергия за фактуриране, MWh</div>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Единична цена, лв./MWh</div>
                    <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Обща сума без ДДС, лв.</div>

                    <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{energy_MHw}</div>
                    <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{price_MHw} лв.</div>
                    <div style={{ textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{sum} лв.</div>
                </div>

                <div style={{ margin: "50px", display: "grid", gridTemplateColumns: "auto auto" }}>
                    <div style={{ marginTop: "50px", marginBottom: "100px", textAlign: "center", fontWeight: "bold" }}>Подпис за Производителя:</div>
                    <div style={{ marginTop: "50px", marginBottom: "100px", textAlign: "center", fontWeight: "bold" }}>Подпис за Ф Инвест ЕООД :</div>
                </div>
            </div>
        }

        return (
            <div>
                {body}
            </div>
            // <div className={PAGE_BODY_WRAPPER} style={{ borderStyle: "solid", borderColor: `${BLUE_COLOR} ${GREEN_COLOR} ${GREEN_COLOR} ${BLUE_COLOR}` }}>
            //     <div style={{ display: "grid", gridTemplateColumns: "200px auto 200px", margin: "20px" }} >
            //         <div><CompanyLogo /></div>
            //         <div style={{ fontWeight: "bold", textAlign: "center", margin: "auto" }}> Ф Инвест ЕООД </div>
            //     </div>
            //     {/* <div><CompanyLogo /></div>

            //     <div style={{ textAlign: "center", marginTop: "50px", fontWeight: "bold" }}> Ф Инвест ЕООД </div> */}
            //     <div style={{ textAlign: "center", marginTop: "20px", fontWeight: "bold" }}> ПРОТОКОЛ </div>
            //     <div style={{ textAlign: "center", marginTop: "50px", marginBottom: "20px" }}>
            //         за продадена електрическа енергия по Договор за покупко-продажба на електрическа енергия за енергиен обект ФтЕЦ Централа 6 на Дружество 6
            //     </div>
            //     <div style={{ display: "grid", gridTemplateColumns: "auto auto auto", marginTop: "50px" }}>
            //         <div style={{ textAlign: "center" }}>за месец</div>
            //         <div style={{ textAlign: "center", fontWeight: "bold" }}> {rangeDate} </div>
            //         <div style={{ textAlign: "center" }}>{year} г.</div>
            //     </div>
            //     <div style={{ marginTop: "50px", marginLeft: "40px" }}>Днес, {currentDate} г.,</div>
            //     <div style={{ marginLeft: "20px" }}>Се състави настоящият протокол за продадената електрическа енергия на Ф Инвест ЕООД за периода от</div>

            //     {/* <table style={{ margin: "50px", width: "-webkit-fill-available" }}>
            //         <tbody>
            //             <tr>
            //                 <td style={{ textAlign: "center", fontWeight: "bold", borderStyle: "solid", borderWidth: "2px", borderColor: "#3C5294" }}>ИТН</td>
            //                 <td style={{ textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: "#3C5294" }} colSpan={2}>{itn}</td>
            //             </tr>
            //             <tr>
            //                 <td style={{ textAlign: "center", fontWeight: "bold", borderStyle: "solid", borderWidth: "2px", borderColor: "#3C5294" }}>Енергия за фактуриране, MWh</td>
            //                 <td style={{ textAlign: "center", fontWeight: "bold", borderStyle: "solid", borderWidth: "2px", borderColor: "#3C5294" }}>Единична цена, лв./MWh</td>
            //                 <td style={{ textAlign: "center", fontWeight: "bold", borderStyle: "solid", borderWidth: "2px", borderColor: "#3C5294" }}>Сума без ДДС, лв.</td>
            //             </tr>
            //             <tr>
            //                 <td style={{ textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: "#3C5294" }}>{energy_MHw}</td>
            //                 <td style={{ textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: "#3C5294" }}>{price_MHw} лв.</td>
            //                 <td style={{ textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: "#3C5294" }}>{sum} лв.</td>
            //             </tr>
            //         </tbody>
            //     </table> */}

            //     {/* <div style={{margin: "50px"}}>
            //         <div style={{display: "grid", gridTemplateColumns: "auto auto"}}>
            //             <div style={{textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: "blue"}}>ИТН</div>
            //             <div style={{textAlign: "center", borderLeftStyle: "solid", borderTopStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: "blue"}}>{itn}</div>
            //         </div>
            //         <div style={{display: "grid", gridTemplateColumns: "auto auto auto"}}>
            //             <div style={{textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: "blue"}}>Енергия за фактуриране, MWh</div>
            //             <div style={{textAlign: "center", fontWeight: "bold", borderTopStyle: "solid", borderWidth: "2px", borderColor: "blue"}}>Единична цена, лв./MWh</div>
            //             <div style={{textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: "blue"}}>Сума без ДДС, лв.</div>
            //             <div style={{textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: "blue"}}>{energy_MHw}</div>
            //             <div style={{textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: "blue"}}>{price_MHw} лв.</div>
            //             <div style={{textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: "blue"}}>{sum} лв.</div>
            //         </div>
            //     </div> */}
            //     <div style={{ margin: "50px", display: "grid", gridTemplateColumns: "1fr 1fr 1fr" }}>
            //         <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>ИТН</div>
            //         <div style={{ textAlign: "center", borderLeftStyle: "solid", borderTopStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}`, gridColumn: "2 span" }}>{itn}</div>

            //         <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Енергия за фактуриране, MWh</div>
            //         <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Единична цена, лв./MWh</div>
            //         <div style={{ textAlign: "center", fontWeight: "bold", borderLeftStyle: "solid", borderTopStyle: "solid", borderRightStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>Сума без ДДС, лв.</div>

            //         <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{energy_MHw}</div>
            //         <div style={{ textAlign: "center", borderTopStyle: "solid", borderLeftStyle: "solid", borderBottomStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{price_MHw} лв.</div>
            //         <div style={{ textAlign: "center", borderStyle: "solid", borderWidth: "2px", borderColor: `${BLUE_COLOR}` }}>{sum} лв.</div>
            //     </div>

            //     <div style={{ margin: "50px", display: "grid", gridTemplateColumns: "auto auto" }}>
            //         <div style={{ marginTop: "50px", marginBottom: "100px", textAlign: "center", fontWeight: "bold" }}>Подпис за Производителя:</div>
            //         <div style={{ marginTop: "50px", marginBottom: "100px", textAlign: "center", fontWeight: "bold" }}>Подпис за Ф Инвест ЕООД :</div>
            //     </div>
            // </div>

        )
    }
}

function mapStateToProps(state, ownProps) {
    return {};
}
function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Object.assign({}, {}), dispatch)
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(ManufacturerProtocolPDF);