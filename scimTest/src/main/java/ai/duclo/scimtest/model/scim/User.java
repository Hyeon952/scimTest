package ai.duclo.scimtest.model.scim;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class User extends Schemas{
    private String userName;
    private Name name;
    private List<Email> emails;
    private String displayName;
    private String locale;
    private String externalId;
    private List<Object> groups;
    private String password;
    private boolean active;
}
