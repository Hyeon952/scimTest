package ai.duclo.scimtest.model.internal;

import ai.duclo.scimtest.common.helper.DateTimeUtil;
import ai.duclo.scimtest.model.scim.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Locale;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountRequestV2 {

    private String name;

    @NotNull
    @NotEmpty
    private String email;

    @NotNull
    private long activeDate;

    @NotNull
    @NotEmpty
    private String role;

    private String accountLanguage = Locale.US.toString();

    private String userName;

    private String password;

    private String temporaryPassword;

    private String accountId;

    private String paymentId;

    private String phoneNumber;

    private Boolean canShareAndDownload;

    private String phoenixRole;

    public static AccountRequestV2 of(User user) {
        DateTimeUtil dateTimeUtil = new DateTimeUtil();
        AccountRequestV2 accountRequest = new AccountRequestV2();
        if (user.getName() != null) {
            accountRequest.setName(user.getName().getGivenName() + " " + user.getName().getFamilyName());
        }
        if (user.getEmails() != null && !user.getEmails().isEmpty()) {
            accountRequest.setEmail(user.getEmails().get(0).getValue());
        }
        accountRequest.setActiveDate(dateTimeUtil.getEpoch());
        if (user.getUserName() != null) {
            accountRequest.setUserName(user.getUserName());
        }
        if (user.getPassword() != null) {
            accountRequest.setPassword(user.getPassword());
        }
        return accountRequest;
    }
}
