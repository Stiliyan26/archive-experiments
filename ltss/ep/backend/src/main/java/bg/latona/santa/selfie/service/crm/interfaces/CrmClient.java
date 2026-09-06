package bg.latona.santa.selfie.service.crm.interfaces;

import bg.latona.santa.selfie.dtos.Crm.CustomerDTO;
import bg.latona.santa.selfie.dtos.Crm.SelfInvoicingLineDTO;
import bg.latona.santa.selfie.dtos.Crm.VeiDTO;
import bg.latona.santa.reports.ReportException;

import java.util.List;

public interface CrmClient {

    /**
     * Fetches a customer by EIK from the external CRM.
     */
    CustomerDTO fetchCustomerByEik(String eik) throws ReportException;

    /**
     * Fetches a vei by MPID (access point) from the external CRM.
     */
    VeiDTO fetchVeiDtoByMpid(String mpid) throws ReportException;

    /**
     * Fetches all self-invoicing lines and agreements self invoicing related to an MPID (access point)
     */
    List<SelfInvoicingLineDTO> fetchSelfInvoicingLineByMpid(String mpid) throws  ReportException;
}
