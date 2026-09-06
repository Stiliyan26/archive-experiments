package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CBlockedQuantity;
import bg.latona.santa.entities.santa.common.QCBlockedQuantity;

public interface CBlockedQuantityRepository extends CommonRepository<CBlockedQuantity, QCBlockedQuantity, Long> {
    
}
