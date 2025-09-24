package ai.duclo.scimtest.model.scim;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UrnIetfParamsEnum {
    LIST_RESPONSE("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
    ERROR("urn:ietf:params:scim:api:messages:2.0:Error"),
    USER("urn:ietf:params:scim:schemas:core:2.0:User"),
    GROUP("urn:ietf:params:scim:schemas:core:2.0:Group"),
    PATCH_OP("urn:ietf:params:scim:api:messages:2.0:PatchOp")
    ;

    private final String value;
}
