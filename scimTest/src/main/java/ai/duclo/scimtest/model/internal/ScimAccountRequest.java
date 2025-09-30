package ai.duclo.scimtest.model.internal;

import ai.duclo.scimtest.model.scim.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScimAccountRequest {

    @NotNull
    @NotEmpty
    private String email;

    private String firstName;
    private String lastName;

    public static ScimAccountRequest of(User user){
        ScimAccountRequest accountRequest = new ScimAccountRequest();
        if (user.getName() != null) {
            accountRequest.setFirstName(user.getName().getGivenName());
            accountRequest.setLastName(user.getName().getFamilyName());
        }
        if (user.getEmails() != null && !user.getEmails().isEmpty()) {
            accountRequest.setEmail(user.getEmails().get(0).getValue());
        }
        return accountRequest;
    }
}