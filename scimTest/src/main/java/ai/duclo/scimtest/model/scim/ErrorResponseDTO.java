package ai.duclo.scimtest.model.scim;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ErrorResponseDTO extends Schemas{
    private String detail;
    private int status;
}
