package ai.duclo.scimtest.dao.repository;

import ai.duclo.scimtest.common.model.DeviceDBConstants;
import ai.duclo.scimtest.entity.RevokedScimToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(transactionManager = DeviceDBConstants.DEVICE_TRANSACTION_MANAGER)
public interface RevokedScimTokenRepository extends JpaRepository<RevokedScimToken, String> {
}
