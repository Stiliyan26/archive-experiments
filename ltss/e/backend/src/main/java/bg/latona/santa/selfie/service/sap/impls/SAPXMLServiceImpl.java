package bg.latona.santa.selfie.service.sap.impls;

import bg.latona.santa.selfie.dtos.sapXMLShipment.AttributeValueElement;
import bg.latona.santa.selfie.dtos.sapXMLShipment.DocumentDTO;
import bg.latona.santa.selfie.dtos.sapXMLShipment.IndexDataDTO;
import bg.latona.santa.selfie.dtos.sapXMLShipment.InvoiceHeaderDTO;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.selfie.service.sap.interfaces.SAPXMLService;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class SAPXMLServiceImpl implements SAPXMLService {

    private final DbSecUserService dbSecUserService;

    private final String TCEE_CODE = "0306";
    private final String EC_CODE = "0302";

    @Override
    public DocumentDTO createDocumentDTO(ElectricityInvoice electricityInvoice) {

        InvoiceHeaderDTO invoiceHeaderDTO = createInvoiceHeader(electricityInvoice);
        IndexDataDTO indexDataDTO = new IndexDataDTO(invoiceHeaderDTO);

        return new DocumentDTO(indexDataDTO);
    }

    private InvoiceHeaderDTO createInvoiceHeader(ElectricityInvoice electricityInvoice) {
        return InvoiceHeaderDTO.builder()
                .client(attr("BG"))
                .documentScanDate(attr(formatDateToString(
                        electricityInvoice.getCreatedDate(), "yyyyMMdd")
                ))
                .documentScanTime(attr(formatDateToString(
                        electricityInvoice.getCreatedDate(), "HHmmss")
                ))
                .barcode(attr("BG_" + electricityInvoice.getInvoiceNumber()))
                .mandt(attr("300"))
                .companyCode(attr(getCompanyCode()))
                .plcEmploymCode(attr(""))
                .supplierDocReference(attr(electricityInvoice.getInvoiceNumber()))
                .documentDate(attr(formatLocalDateToString(
                        electricityInvoice.getTaxEventDate(), "yyyyMMdd")
                ))
                .purchaseOrderId(attr(""))
                .supplierId(attr("0000010192"))
                .documentCurrency(attr("BGN"))
                .invoiceGrossAmountDc(attr(calculateGrossAmount(
                        electricityInvoice.getTotalSumInLevs(),
                        electricityInvoice.getVatInLevs()
                )))
                .invoiceNetAmountDc(attr(formatAmountToSecondFloatingPoint(
                        electricityInvoice.getTotalSumInLevs()
                )))
                .taxCode(attr(""))
                .invoiceTaxAmountDc(attr(formatAmountToSecondFloatingPoint(
                        electricityInvoice.getVatInLevs()
                )))
                .createdByUser(attr(""))
                .energyQuantity(attr(getEnergyQuantity(
                        electricityInvoice.getTotalQuantity(),
                        electricityInvoice.getReportingPointOwn()
                )))
                .esignName(attr(""))
                .build();
    }

    private AttributeValueElement attr(String value) {
        return new AttributeValueElement(value);
    }

    private String formatDateToString(Date date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        return dateFormat.format(date);
    }

    private String formatLocalDateToString(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        return date.format(formatter);
    }

    private String getCompanyCode() {
        return dbSecUserService.getCompanyCode().toString();
    }

    private String getEnergyQuantity(BigDecimal installedPowerMw, String accessPoint) {
        String companyCode = getCompanyCode();

        String energyQuantity = "";

        if (companyCode.equals(TCEE_CODE)) {
            if (installedPowerMw != null) {
                energyQuantity = String.format("%.3f", installedPowerMw);
            }
        } else if (companyCode.equals(EC_CODE)) {
            energyQuantity = accessPoint.substring(accessPoint.length() - 7) + ".000";
        }

        return energyQuantity;
    }

    private String formatAmountToSecondFloatingPoint(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    private String calculateGrossAmount(BigDecimal netAmount, BigDecimal vatAmount) {
        BigDecimal grossAmount = netAmount.add(vatAmount);

        return formatAmountToSecondFloatingPoint(grossAmount);
    }
}
