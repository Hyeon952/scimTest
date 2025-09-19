package ai.duclo.scimtest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserResponseDTO extends User{
    private Meta meta;
}
