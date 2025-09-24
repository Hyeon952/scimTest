

DROP TABLE IF EXISTS IDP_SCIM_TOKEN;
DROP TABLE IF EXISTS REVOKED_SCIM_TOKEN;

CREATE TABLE IDP_SCIM_TOKEN (
  IDP_ID varchar(50) NOT NULL COMMENT 'Identifier for the ID Provider',
  JTI varchar(36) NOT NULL COMMENT 'Unique identifier for the JWT (JWT ID)',
  CREATED_DATE bigint DEFAULT NULL COMMENT 'Token creation timestamp (Unix time)',
  UPDATED_DATE bigint DEFAULT NULL COMMENT 'Token update timestamp (Unix time)',
  PRIMARY KEY (IDP_ID)
)
COMMENT='Table for managing SCIM tokens per ID Provider';

CREATE TABLE REVOKED_SCIM_TOKEN (
  JTI varchar(36) NOT NULL COMMENT 'JWT ID of the revoked token',
  CREATED_DATE bigint DEFAULT NULL COMMENT 'Revocation registration timestamp (Unix time)',
  UPDATED_DATE bigint DEFAULT NULL COMMENT 'Revocation update timestamp (Unix time)',
  PRIMARY KEY (JTI)
)
COMMENT='Table for managing revoked SCIM token JTIs';


commit;