package ai.duclo.scimtest.model.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ListResponseDTO<T> extends Schemas{
    private int totalResults;
    private int startIndex;
    private int itemsPerPage;
    @JsonProperty(value = "Resources")
    private List<T> Resources;
}
