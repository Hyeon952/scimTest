package ai.duclo.scimtest.common.model;

import lombok.Getter;

@Getter
public class CustomPrincipal {
    private final String serviceName;
    private final String appId;

    public CustomPrincipal(String serviceName, String appId) {
        this.serviceName = serviceName;
        this.appId = appId;
    }
}
