package ai.duclo.scimtest.controller;

import ai.duclo.scimtest.model.internal.InternalResponseDTO;
import ai.duclo.scimtest.model.internal.InternalScimTokenRequestDTO;
import ai.duclo.scimtest.service.ScimTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/scim/internal")
@RequiredArgsConstructor
public class ScimTokenController {

    private final ScimTokenService scimTokenService;

    @PostMapping("/v1/{appType}/token")
    public ResponseEntity<InternalResponseDTO> getScimToken(@PathVariable(name = "appType") String appType, @RequestBody InternalScimTokenRequestDTO internalScimTokenRequestDTO) {
        if(!StringUtils.hasText(internalScimTokenRequestDTO.getIdpId())){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(scimTokenService.createScimToken(appType, internalScimTokenRequestDTO.getIdpId()));
    }

}
