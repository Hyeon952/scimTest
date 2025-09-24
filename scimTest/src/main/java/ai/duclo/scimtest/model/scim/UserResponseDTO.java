package ai.duclo.scimtest.model.scim;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserResponseDTO extends User{
    private Meta meta;

    public static UserResponseDTO create(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUserName(user.getUserName());
        userResponseDTO.setName(user.getName());
        userResponseDTO.setDisplayName(user.getDisplayName());
        userResponseDTO.setEmails(user.getEmails());
        userResponseDTO.setLocale(user.getLocale());
        userResponseDTO.setExternalId(user.getExternalId());
        userResponseDTO.setGroups(user.getGroups());
        userResponseDTO.setSchemas(user.getSchemas());
        userResponseDTO.setPassword(null); // Password should not be exposed
        Meta meta = new Meta();
        meta.setResourceType("User");
        userResponseDTO.setMeta(meta);
        userResponseDTO.setActive(user.isActive());
        return userResponseDTO;
    }
}
