set search_path to SEGURIDAD;

CREATE TABLE JOB_TOKEN
(
	TOKEN VARCHAR(254) NOT NULL,
	INICIO TIMESTAMP NOT NULL,
	FIN TIMESTAMP
);

ALTER TABLE JOB_TOKEN ADD CONSTRAINT "JOB_TOKEN_PK" PRIMARY KEY (TOKEN);
