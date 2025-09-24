package ai.duclo.scimtest.service;

import ai.duclo.scimtest.common.helper.JwtTokenGenerator;
import ai.duclo.scimtest.model.internal.InternalResponseDTO;
import ai.duclo.scimtest.model.internal.InternalResponseMeta;
import ai.duclo.scimtest.model.internal.InternalScimToken;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ScimTokenService {

    public InternalResponseDTO createScimToken(String appType, String idpId) {

        JwtTokenGenerator tokenGenerator = new JwtTokenGenerator();

        String jti = java.util.UUID.randomUUID().toString();
        // 여러 클레임 예시
        Map<String, Object> claims = new HashMap<>();
        claims.put("idpId", idpId);
        claims.put("JTI", jti);

        String token = tokenGenerator.generateToken(appType, 31536000000L, claims); // 1 hour expiration
        InternalResponseDTO internalResponseDTO = new InternalResponseDTO();
        InternalResponseMeta responseMeta = new InternalResponseMeta();
        responseMeta.setCode(200);
        InternalScimToken internalScimToken = new InternalScimToken();
        internalScimToken.setScimToken(token);
        internalResponseDTO.setMeta(responseMeta);
        internalResponseDTO.setData(internalScimToken);

        return internalResponseDTO;
    }
}
