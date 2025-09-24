package ai.duclo.scimtest.model.scim;

import lombok.Data;

@Data
public class Email {
    private boolean primary;
    private String value;
    private String type; // work, home
}
