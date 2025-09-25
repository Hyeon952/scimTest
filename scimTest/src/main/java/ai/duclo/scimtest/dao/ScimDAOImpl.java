package ai.duclo.scimtest.dao;

import ai.duclo.scimtest.common.helper.DateTimeUtil;
import ai.duclo.scimtest.dao.repository.IdpScimTokenRepository;
import ai.duclo.scimtest.dao.repository.RevokedScimTokenRepository;
import ai.duclo.scimtest.entity.IdpScimToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class ScimDAOImpl implements ScimDAO {

    private final IdpScimTokenRepository idpScimTokenRepository;
    private final RevokedScimTokenRepository revokedScimTokenRepository;
    private final DateTimeUtil dateTimeUtil;

    @Override
    public void saveScimToken(String idpId, String jti) {
        Long currentTimeInMillis = dateTimeUtil.getEpoch();

        idpScimTokenRepository.save(new IdpScimToken(idpId, jti, currentTimeInMillis, currentTimeInMillis));
    }
}
