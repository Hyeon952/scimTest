package ai.duclo.scimtest.common.model;

import lombok.Getter;

@Getter
public class CustomPrincipal {
    private final String serviceName;
    private final String jti;
    private final String appId;

    public CustomPrincipal(String serviceName, String jti, String appId) {
        this.serviceName = serviceName;
        this.jti = jti;
        this.appId = appId;
    }
}
