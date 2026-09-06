package bg.latona.santa.selfie.service.crm.interfaces;

import bg.latona.santa.selfie.dtos.Crm.CustomerDTO;
import bg.latona.santa.selfie.dtos.Crm.SelfInvoicingLineDTO;
import bg.latona.santa.selfie.dtos.Crm.VeiDTO;

import java.util.List;

public interface CrmMockEndpointService {

    CustomerDTO findCustomerByEik();


    VeiDTO findMeteringPointByNumber();


    List<SelfInvoicingLineDTO> getSelfInvoicingLine();
}
