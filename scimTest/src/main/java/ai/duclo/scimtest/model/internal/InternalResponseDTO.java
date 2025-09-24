package ai.duclo.scimtest.model.internal;

import lombok.Data;

@Data
public class InternalResponseDTO {
    private InternalResponseMeta meta;
    private Object data;
}
