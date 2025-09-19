package ai.duclo.scimtest.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ListResponseDTO<T> extends Schemas{
    private int totalResults;
    private int startIndex;
    private int itemsPerPage;
    private List<T> Resources;
}
