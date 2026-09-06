package bg.latona.santa.repositories;

import bg.latona.santa.entities.security.QAccessControl;
import bg.latona.santa.entities.security.AccessControl;

public interface AccessControlRepository extends CommonRepository<AccessControl, QAccessControl, Long> {
	
}