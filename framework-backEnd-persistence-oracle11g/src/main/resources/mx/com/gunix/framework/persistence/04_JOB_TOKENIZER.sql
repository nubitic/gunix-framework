alter session set current_schema = SEGURIDAD;

CREATE TABLE JOB_TOKEN
(
	TOKEN VARCHAR2(254) NOT NULL,
	INICIO TIMESTAMP NOT NULL,
	FIN TIMESTAMP
);

ALTER TABLE JOB_TOKEN ADD CONSTRAINT "JOB_TOKEN_PK" PRIMARY KEY (TOKEN);
