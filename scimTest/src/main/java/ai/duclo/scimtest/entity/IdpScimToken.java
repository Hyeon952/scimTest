package ai.duclo.scimtest.entity;

import java.io.Serializable;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "IDP_SCIM_TOKEN")
@NoArgsConstructor
@AllArgsConstructor
public class IdpScimToken implements Serializable {

    @Id
    @Column(name = "IDP_ID", length = 50, nullable = false, columnDefinition = "varchar(50) COMMENT 'Identifier for the ID Provider'")
    private String idpId;

    @Column(name = "JTI", length = 36, nullable = false, columnDefinition = "varchar(36) COMMENT 'Unique identifier for the JWT (JWT ID)'")
    private String jti;

    @Column(name = "CREATED_DATE", columnDefinition = "bigint COMMENT 'Token creation timestamp (Unix time)'")
    private Long createdDate;

    @Column(name = "UPDATED_DATE", columnDefinition = "bigint COMMENT 'Token update timestamp (Unix time)'")
    private Long updatedDate;

}
