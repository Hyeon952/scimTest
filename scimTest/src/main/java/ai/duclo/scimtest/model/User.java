package ai.duclo.scimtest.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class User {
    private String id;
    private String userName;
    @NotBlank
    private String externalId;
    private String name;
    private List<Email> emails;
}