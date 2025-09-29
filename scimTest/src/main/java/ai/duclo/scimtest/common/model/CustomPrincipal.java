package ai.duclo.scimtest.common.model;

import lombok.Getter;

@Getter
public class CustomPrincipal {
    private String subject;
    private String jti;
    private String idpId;

    public CustomPrincipal(String subject, String jti, String idpId) {
        this.subject = subject;
        this.jti = jti;
        this.idpId = idpId;
    }
}
