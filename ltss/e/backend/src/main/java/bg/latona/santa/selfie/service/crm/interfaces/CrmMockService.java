package bg.latona.santa.selfie.service.crm.interfaces;

import bg.latona.santa.selfie.domain.crm.CrmRequestMock;

import java.util.List;
import java.util.Map;

public interface CrmMockService {

	List<Map<String, Object>> createOrUpdateCRMData(CrmRequestMock crmRequestMock);
}