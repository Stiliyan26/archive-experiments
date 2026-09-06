package bg.latona.santa.selfie.dtos.sapXMLShipment;

import bg.latona.santa.selfie.constant.SapXmlConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlType(name = SapXmlConstants.INVOICE_HEADER)
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceHeaderDTO {

    @XmlElement(name = SapXmlConstants.CLIENT)
    private AttributeValueElement client;

    @XmlElement(name = SapXmlConstants.DOCUMENT_SCAN_DATE)
    private AttributeValueElement documentScanDate;

    @XmlElement(name = SapXmlConstants.DOCUMENT_SCAN_TIME)
    private AttributeValueElement documentScanTime;

    @XmlElement(name = SapXmlConstants.BARCODE)
    private AttributeValueElement barcode;

    @XmlElement(name = SapXmlConstants.MANDT)
    private AttributeValueElement mandt;

    @XmlElement(name = SapXmlConstants.COMPANY_CODE)
    private AttributeValueElement companyCode;

    @XmlElement(name = SapXmlConstants.PLC_EMPLOYM_CODE)
    private AttributeValueElement plcEmploymCode;

    @XmlElement(name = SapXmlConstants.SUPPLIER_DOC_REFERENCE)
    private AttributeValueElement supplierDocReference;

    @XmlElement(name = SapXmlConstants.DOCUMENT_DATE)
    private AttributeValueElement documentDate;

    @XmlElement(name = SapXmlConstants.PURCHASE_ORDER_ID)
    private AttributeValueElement purchaseOrderId;

    @XmlElement(name = SapXmlConstants.SUPPLIER_ID)
    private AttributeValueElement supplierId;

    @XmlElement(name = SapXmlConstants.DOCUMENT_CURRENCY)
    private AttributeValueElement documentCurrency;

    @XmlElement(name = SapXmlConstants.INVOICE_GROSS_AMOUNT_DC)
    private AttributeValueElement invoiceGrossAmountDc;

    @XmlElement(name = SapXmlConstants.INVOICE_NET_AMOUNT_DC)
    private AttributeValueElement invoiceNetAmountDc;

    @XmlElement(name = SapXmlConstants.TAX_CODE)
    private AttributeValueElement taxCode;

    @XmlElement(name = SapXmlConstants.INVOICE_TAX_AMOUNT_DC)
    private AttributeValueElement invoiceTaxAmountDc;

    @XmlElement(name = SapXmlConstants.CREATED_BY_USER)
    private AttributeValueElement createdByUser;

    @XmlElement(name = SapXmlConstants.ENERGY_QUANTITY)
    private AttributeValueElement energyQuantity;

    @XmlElement(name = SapXmlConstants.ESIGN_NAME)
    private AttributeValueElement esignName;
}
