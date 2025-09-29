package ai.duclo.scimtest.entity;

import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "REVOKED_SCIM_TOKEN")
public class RevokedScimToken implements Serializable {

    @Id
    @Column(name = "JTI", length = 36, nullable = false, columnDefinition = "varchar(36) COMMENT 'JWT ID of the revoked token'")
    private String jti;

    @Column(name = "CREATED_DATE", columnDefinition = "bigint COMMENT 'Revocation registration timestamp (Unix time)'")
    private Long createdDate;

    @Column(name = "UPDATED_DATE", columnDefinition = "bigint COMMENT 'Revocation update timestamp (Unix time)'")
    private Long updatedDate;

}
