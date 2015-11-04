﻿/*declare @owner_user;
SET @owner_user = 'exsipqluzgntfw';*/

CREATE SCHEMA IF NOT EXISTS SEGURIDAD /*AUTHORIZATION @owner_user*/;
set search_path to SEGURIDAD;

CREATE EXTENSION pgcrypto;

-- ACL Schema SQL for PostgreSQL
CREATE TYPE ESTATUS_USUARIO AS ENUM ('ACTIVO','ELIMINADO','BLOQUEADO','SIN PASSWORD');

CREATE TABLE USUARIO
(
	ID_USUARIO VARCHAR(254) NOT NULL,
	PASSWORD VARCHAR(60),
	ESTATUS ESTATUS_USUARIO NOT NULL DEFAULT 'SIN PASSWORD'
);

ALTER TABLE USUARIO ADD CONSTRAINT "USUARIO_PK" PRIMARY KEY (ID_USUARIO);

create table acl_sid(
    id bigserial not null primary key,
    principal boolean not null,
    sid VARCHAR(254) not null,
    constraint unique_uk_1 unique(sid)
);

ALTER TABLE acl_sid ADD CONSTRAINT "acl_sid_USUARIO_FK1" FOREIGN KEY (sid) REFERENCES USUARIO (ID_USUARIO)  ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX acl_sid_FK1IDX ON acl_sid USING BTREE (sid);

create table acl_class(
    id bigserial not null primary key,
    class varchar(100) not null,
    constraint unique_uk_2 unique(class)
);

create table acl_object_identity(
    id bigserial primary key,
    object_id_class bigint not null,
    object_id_identity BIGSERIAL NOT NULL,
    parent_object bigint,
    owner_sid bigint,
    entries_inheriting boolean not null,
    constraint unique_uk_3 unique(object_id_identity),
    constraint foreign_fk_1 foreign key(parent_object)references acl_object_identity(id)  ON UPDATE NO ACTION ON DELETE NO ACTION,
    constraint foreign_fk_2 foreign key(object_id_class)references acl_class(id)  ON UPDATE NO ACTION ON DELETE NO ACTION,
    constraint foreign_fk_3 foreign key(owner_sid)references acl_sid(id)  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE INDEX acl_object_identity_FK1IDX ON acl_object_identity USING BTREE (parent_object);
CREATE INDEX acl_object_identity_FK2IDX ON acl_object_identity USING BTREE (object_id_class);
CREATE INDEX acl_object_identity_FK3IDX ON acl_object_identity USING BTREE (owner_sid);


create table acl_entry(
    id bigserial primary key,
    acl_object_identity bigint not null,
    ace_order int not null,
    sid bigint not null,
    mask integer not null,
    granting boolean not null,
    audit_success boolean not null,
    audit_failure boolean not null,
    constraint unique_uk_4 unique(acl_object_identity,ace_order),
    constraint foreign_fk_4 foreign key(acl_object_identity) references acl_object_identity(id)  ON UPDATE NO ACTION ON DELETE NO ACTION,
    constraint foreign_fk_5 foreign key(sid) references acl_sid(id)  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE INDEX acl_entry_FK4IDX ON acl_entry USING BTREE (acl_object_identity);
CREATE INDEX acl_entry_FK5IDX ON acl_entry USING BTREE (sid);

INSERT INTO USUARIO(ID_USUARIO,PASSWORD,ESTATUS) VALUES('bjvences@gmail.com',crypt('loloq123', gen_salt('bf', 16)),'ACTIVO');
INSERT INTO USUARIO(ID_USUARIO,PASSWORD,ESTATUS) VALUES('anonymous',crypt('anonymous', gen_salt('bf', 16)),'ACTIVO');
INSERT INTO acl_sid(principal,sid) VALUES(true,'bjvences@gmail.com');

/****************************************************/

CREATE TABLE APLICACION
(
	ID_APLICACION VARCHAR(30) NOT NULL,
	ACL_ID BIGINT NOT NULL,
	DESCRIPCION VARCHAR(100) NOT NULL DEFAULT 'APLICACION SIN DESCRIPCION',
	ICONO VARCHAR(25) NOT NULL DEFAULT 'APLICACION_SIN_ICONO.png'
);
ALTER TABLE APLICACION ADD CONSTRAINT "APLICACION_PK" PRIMARY KEY (ID_APLICACION);
ALTER TABLE APLICACION ADD CONSTRAINT "APLICACION_ACL_UQ" UNIQUE (ACL_ID);
ALTER TABLE APLICACION ADD CONSTRAINT "APLICACION_ACL_FK1" FOREIGN KEY (ACL_ID) REFERENCES acl_object_identity (object_id_identity)  ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX APLICACION_ACL_FK1IDX ON APLICACION USING BTREE (ACL_ID);

CREATE TABLE USUARIO_APLICACION
(
	ID_USUARIO VARCHAR(254) NOT NULL,
	ID_APLICACION VARCHAR(30) NOT NULL
);
ALTER TABLE USUARIO_APLICACION ADD CONSTRAINT "USUARIO_APLICACION_PK" PRIMARY KEY (ID_USUARIO,ID_APLICACION);
ALTER TABLE USUARIO_APLICACION ADD CONSTRAINT "USUARIO_APLICACION_USUARIO_FK1" FOREIGN KEY (ID_USUARIO) REFERENCES USUARIO (ID_USUARIO)  ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE USUARIO_APLICACION ADD CONSTRAINT "USUARIO_APLICACION_APLICACION_FK2" FOREIGN KEY (ID_APLICACION) REFERENCES APLICACION (ID_APLICACION)  ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX USUARIO_APLICACION_FK1IDX ON USUARIO_APLICACION USING BTREE (ID_USUARIO);
CREATE INDEX USUARIO_APLICACION_FK2IDX ON USUARIO_APLICACION USING BTREE (ID_APLICACION);

CREATE TABLE ROL
(
    ID_APLICACION VARCHAR(30) NOT NULL,
	ID_ROL VARCHAR(30) NOT NULL,
	DESCRIPCION VARCHAR(100) NOT NULL DEFAULT 'ROL SIN DESCRIPCION',
	HABILITADO BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE ROL ADD CONSTRAINT "ROL_PK" PRIMARY KEY (ID_APLICACION,ID_ROL);
ALTER TABLE ROL ADD CONSTRAINT "ROL_APLICACION_FK1" FOREIGN KEY (ID_APLICACION) REFERENCES APLICACION (ID_APLICACION)  ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX ROL_APLICACION_FK1IDX ON ROL USING BTREE (ID_APLICACION);

CREATE TABLE USUARIO_ROL
(
	ID_USUARIO VARCHAR(254) NOT NULL,
	ID_APLICACION VARCHAR(30) NOT NULL,
	ID_ROL VARCHAR(30) NOT NULL
);
ALTER TABLE USUARIO_ROL ADD CONSTRAINT "USUARIO_ROL_PK" PRIMARY KEY (ID_USUARIO,ID_APLICACION,ID_ROL);
ALTER TABLE USUARIO_ROL ADD CONSTRAINT "USUARIO_ROL_USUARIO_FK1" FOREIGN KEY (ID_USUARIO) REFERENCES USUARIO (ID_USUARIO)  ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE USUARIO_ROL ADD CONSTRAINT "USUARIO_ROL_ROL_FK2" FOREIGN KEY (ID_APLICACION,ID_ROL) REFERENCES ROL (ID_APLICACION,ID_ROL)  ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX USUARIO_ROL_FK1IDX ON USUARIO_ROL USING BTREE (ID_USUARIO);
CREATE INDEX USUARIO_ROL_FK2IDX ON USUARIO_ROL USING BTREE (ID_APLICACION,ID_ROL);

CREATE TABLE MODULO
(
    ID_APLICACION VARCHAR(30) NOT NULL,
	ID_MODULO VARCHAR(30) NOT NULL,
	DESCRIPCION VARCHAR(200) NOT NULL DEFAULT 'MODULO SIN DESCRIPCION',
	ICONO VARCHAR(25) NOT NULL DEFAULT 'MODULO_SIN_ICONO.png'
);

ALTER TABLE MODULO ADD CONSTRAINT "MODULO_PK" PRIMARY KEY (ID_APLICACION,ID_MODULO);
ALTER TABLE MODULO ADD CONSTRAINT "MODULO_APLICACION_FK1" FOREIGN KEY (ID_APLICACION) REFERENCES APLICACION (ID_APLICACION)  ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX MODULO_APLICACION_FK1IDX ON MODULO USING BTREE (ID_APLICACION);

CREATE TYPE HORARIO_OPERACION AS ENUM ('LD24','LV24','LV9_18','PERSONALIZADO');

CREATE TABLE FUNCION
(
    ID_APLICACION VARCHAR(30) NOT NULL,
	ID_MODULO VARCHAR(30) NOT NULL,
	ID_FUNCION VARCHAR(30) NOT NULL,
	TITULO VARCHAR(20) NOT NULL,
	DESCRIPCION VARCHAR(100) NOT NULL,
	PROCESS_KEY VARCHAR(255) NOT NULL DEFAULT '',
	ORDEN NUMERIC(3,2) NOT NULL DEFAULT 0.0,
	HORARIO HORARIO_OPERACION NOT NULL DEFAULT 'LD24',
	ID_FUNCION_PADRE VARCHAR(30)
);

ALTER TABLE FUNCION ADD CONSTRAINT "FUNCION_PK" PRIMARY KEY (ID_APLICACION,ID_MODULO,ID_FUNCION);
ALTER TABLE FUNCION ADD CONSTRAINT "MODULO_FUNCION_FK" FOREIGN KEY (ID_APLICACION,ID_MODULO) REFERENCES MODULO (ID_APLICACION,ID_MODULO) ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE FUNCION ADD CONSTRAINT "FUNCION_FUNCION_FK" FOREIGN KEY (ID_APLICACION,ID_MODULO,ID_FUNCION_PADRE) REFERENCES FUNCION (ID_APLICACION,ID_MODULO,ID_FUNCION)  ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX MODULO_FUNCION_FKIDX ON FUNCION USING BTREE (ID_APLICACION,ID_MODULO);
CREATE INDEX FUNCION_FUNCION_FKIDX ON FUNCION USING BTREE (ID_APLICACION,ID_MODULO,ID_FUNCION_PADRE);

CREATE TABLE PARAM_FUNCION
(
    ID_APLICACION VARCHAR(30) NOT NULL,
	ID_MODULO VARCHAR(30) NOT NULL,
	ID_FUNCION VARCHAR(30) NOT NULL,
	ID_PARAM VARCHAR(15) NOT NULL,
	VALOR VARCHAR(500) NOT NULL
);

ALTER TABLE PARAM_FUNCION ADD CONSTRAINT "PARAM_FUNCION_PK" PRIMARY KEY (ID_APLICACION,ID_MODULO,ID_FUNCION,ID_PARAM);
ALTER TABLE PARAM_FUNCION ADD CONSTRAINT "PARAM_FUNCION_FK" FOREIGN KEY (ID_APLICACION,ID_MODULO,ID_FUNCION) REFERENCES FUNCION (ID_APLICACION,ID_MODULO,ID_FUNCION)  ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX PARAM_FUNCION_FKIDX ON PARAM_FUNCION USING BTREE (ID_APLICACION,ID_MODULO,ID_FUNCION);

CREATE TYPE NIVEL_ACCESO AS ENUM ('COMPLETO','PUNTUAL');

CREATE TABLE ROL_FUNCION
(
    ID_APLICACION VARCHAR(30) NOT NULL,
	ID_ROL VARCHAR(30) NOT NULL,
	ID_MODULO VARCHAR(30) NOT NULL,
	ID_FUNCION VARCHAR(30) NOT NULL,
	NIV_ACC NIVEL_ACCESO NOT NULL DEFAULT 'PUNTUAL'
);

ALTER TABLE ROL_FUNCION ADD CONSTRAINT "ROL_FUNCION_PK" PRIMARY KEY (ID_APLICACION,ID_ROL,ID_MODULO,ID_FUNCION);
ALTER TABLE ROL_FUNCION ADD CONSTRAINT "ROL_FUNCION_FUNCION_FK1" FOREIGN KEY (ID_APLICACION,ID_MODULO,ID_FUNCION) REFERENCES FUNCION (ID_APLICACION,ID_MODULO,ID_FUNCION)  ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE ROL_FUNCION ADD CONSTRAINT "ROL_FUNCION_ROL_FK2" FOREIGN KEY (ID_APLICACION,ID_ROL) REFERENCES ROL (ID_APLICACION,ID_ROL)  ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX ROL_FUNCION_FK1IDX ON ROL_FUNCION USING BTREE (ID_APLICACION,ID_MODULO,ID_FUNCION);
CREATE INDEX ROL_FUNCION_FK2IDX ON ROL_FUNCION USING BTREE (ID_APLICACION,ID_ROL);

create table PERSISTENT_LOGINS 
(
        USERNAME VARCHAR(254) not null,
        SERIES varchar(64) primary key,
        TOKEN varchar(64) not null,
        LAST_USED timestamp not null
);


INSERT INTO acl_class(class) values ('mx.com.gunix.framework.security.domain.Aplicacion');
INSERT INTO acl_object_identity(object_id_class,owner_sid,entries_inheriting) values( currval(pg_get_serial_sequence('acl_class', 'id')),(select id from acl_sid where sid = 'bjvences@gmail.com'),true);

INSERT INTO acl_entry(acl_object_identity, ace_order, sid, mask, granting, audit_success,audit_failure) VALUES (currval(pg_get_serial_sequence('acl_object_identity', 'id')), 0, (select id from acl_sid where sid = 'bjvences@gmail.com'), 16, TRUE, FALSE, FALSE);
INSERT INTO acl_entry(acl_object_identity, ace_order, sid, mask, granting, audit_success,audit_failure) VALUES (currval(pg_get_serial_sequence('acl_object_identity', 'id')), 1, (select id from acl_sid where sid = 'bjvences@gmail.com'), 1, TRUE, FALSE, FALSE);
INSERT INTO acl_entry(acl_object_identity, ace_order, sid, mask, granting, audit_success,audit_failure) VALUES (currval(pg_get_serial_sequence('acl_object_identity', 'id')), 2, (select id from acl_sid where sid = 'bjvences@gmail.com'), 2, TRUE, FALSE, FALSE);
INSERT INTO acl_entry(acl_object_identity, ace_order, sid, mask, granting, audit_success,audit_failure) VALUES (currval(pg_get_serial_sequence('acl_object_identity', 'id')), 3, (select id from acl_sid where sid = 'bjvences@gmail.com'), 8, TRUE, FALSE, FALSE);

INSERT INTO APLICACION VALUES('ADMIN_APP',currval(pg_get_serial_sequence('acl_object_identity', 'object_id_identity')),'Gunix Admin App','Gunix.png');

	INSERT INTO USUARIO_APLICACION VALUES('bjvences@gmail.com','ADMIN_APP');

        /* Funcionalidad Privada (Segura) */
        INSERT INTO MODULO VALUES('ADMIN_APP','APLICACIONES','Aplicaciones','window-128.png');
                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,ORDEN) VALUES('ADMIN_APP','APLICACIONES','ADMINISTRACION','Administración','Menú con opciones de Administración de aplicaciones',1);
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','APLICACIONES','ALTA','Alta','Alta de Aplicaciones','AdministraciónAplicaciones','ADMINISTRACION',1);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','APLICACIONES','ALTA','operación','Alta');
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','APLICACIONES','CONSULTA','Consulta','Consulta de Aplicaciones','AdministraciónAplicaciones','ADMINISTRACION',2);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','APLICACIONES','CONSULTA','operación','Consulta');
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','APLICACIONES','MODIFICACION','Modificación','Modificación de Aplicaciones','AdministraciónAplicaciones','ADMINISTRACION',3);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','APLICACIONES','MODIFICACION','operación','Modificación');

		INSERT INTO MODULO VALUES('ADMIN_APP','ACL','Lista de Control de Acceso','window-128.png');
                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,ORDEN) VALUES('ADMIN_APP','ACL','ADMINISTRACION','Administración','Menú con opciones de Administración de ACL',1);
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','ACL','ALTA','Alta','Alta de Control de Acceso','AdministraciónACL','ADMINISTRACION',1);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','ACL','ALTA','operación','Alta');

        INSERT INTO ROL VALUES('ADMIN_APP','ADMINISTRADOR','Administrador de Aplicaciones');
	     	 INSERT INTO ROL_FUNCION VALUES('ADMIN_APP','ADMINISTRADOR','APLICACIONES','ADMINISTRACION','COMPLETO');

        INSERT INTO ROL VALUES('ADMIN_APP','ACL_ADMIN','Administrador de la Lista de Control de Acceso');
                INSERT INTO ROL_FUNCION VALUES('ADMIN_APP','ACL_ADMIN','ACL','ADMINISTRACION','COMPLETO');

        INSERT INTO USUARIO_ROL VALUES('bjvences@gmail.com','ADMIN_APP','ADMINISTRADOR');
        INSERT INTO USUARIO_ROL VALUES('bjvences@gmail.com','ADMIN_APP','ACL_ADMIN');
        

        /* Funcionalidad Pública */
        INSERT INTO ROL VALUES('ADMIN_APP','PUBLIC','Público en General');
        INSERT INTO MODULO VALUES('ADMIN_APP','MODULO_PRUEBA','Demostraciones','1436568412_demo.png');
                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,ORDEN) VALUES('ADMIN_APP','MODULO_PRUEBA','DEMOS','Demos','Menú con funcionalidad de demostración',1);
                	INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','MODULO_PRUEBA','ALTA_FORM','Formulario Alta','Demostración de Formularios','DemoForm','DEMOS',1);
                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','MODULO_PRUEBA','ALTA_FORM','operación','Alta');

        INSERT INTO ROL_FUNCION VALUES('ADMIN_APP','PUBLIC','MODULO_PRUEBA','DEMOS','COMPLETO');

        INSERT INTO USUARIO_ROL VALUES('anonymous','ADMIN_APP','PUBLIC');

create or replace function MENU_USUARIO(id_usuario text)
  returns table (
	ID_USUARIO text,
	PASSWORD text,
	ELIMINADO text,
	BLOQUEADO text,
	ACTIVO text,
	ID_APLICACION text,
	DESCRIPCION_APLICACION text,
        ICONO_APLICACION text,
                ID_ROL text,
                DESCRIPCION_ROL TEXT,
                HABILITADO_ROL BOOLEAN,
                        ID_MODULO text,
                        DESCRIPCION_MODULO text,
                        ICONO_MODULO text,
                                ID_FUNCION text,
                                TITULO text,
                                DESCRIPCION_FUNCION text,
                                PROCESS_KEY text,
                                ORDEN NUMERIC,
                                HORARIO TEXT,
                                ID_PARAM text,
                                VALOR text,
                                ID_FUNCION_PADRE text,
                                        TITULO_PADRE text,
                                        DESCRIPCION_PADRE text,
                                        PROCESS_KEY_PADRE text,
                                        ORDEN_PADRE NUMERIC,
                                        ID_PARAM_PADRE text,
                                        VALOR_PADRE text)
as
$body$
WITH RECURSIVE
h as(
	select
		U.ID_USUARIO,
		U.PASSWORD,
		case
			when U.ESTATUS='ELIMINADO' then 'true'
			WHEN U.ID_USUARIO IS NULL THEN NULL
			else 'false' 
		end as ELIMINADO,
		case
			when U.ESTATUS='BLOQUEADO' then 'true'
			WHEN U.ID_USUARIO IS NULL THEN NULL
			else 'false' 
		end as BLOQUEADO,
		case
			when U.ESTATUS='ACTIVO' then 'true'
			WHEN U.ID_USUARIO IS NULL THEN NULL
			else 'false' 
		end as ACTIVO,
                APP.ID_APLICACION,
                APP.DESCRIPCION AS DESCRIPCION_APLICACION,
                APP.ICONO AS ICONO_APLICACION,
		UR.ID_ROL,
		RL.DESCRIPCION AS DESCRIPCION_ROL,
		RL.HABILITADO AS HABILITADO_ROL,
		M.ID_MODULO,
		M.DESCRIPCION AS DESCRIPCION_MODULO,
		M.ICONO AS ICONO_MODULO,
		F.ID_FUNCION,
		F.TITULO,
		F.DESCRIPCION AS DESCRIPCION_FUNCION,
		F.PROCESS_KEY,
		F.ORDEN,
		F.HORARIO::text AS HORARIO,
		case 
			when RF.NIV_ACC='COMPLETO' then 'true'
			when RF.NIV_ACC IS NULL then NULL
			else 'false' 
		end as ACCESO_COMPLETO,
		F.ID_FUNCION_PADRE,
		PF.ID_PARAM,
		PF.VALOR
	from
		SEGURIDAD.FUNCION F  LEFT JOIN
		SEGURIDAD.MODULO M ON (M.ID_APLICACION=F.ID_APLICACION AND M.ID_MODULO=F.ID_MODULO) LEFT JOIN 
		SEGURIDAD.PARAM_FUNCION PF ON (PF.ID_APLICACION=F.ID_APLICACION AND PF.ID_MODULO=F.ID_MODULO AND PF.ID_FUNCION=F.ID_FUNCION) LEFT JOIN
		SEGURIDAD.APLICACION APP ON (APP.ID_APLICACION=F.ID_APLICACION) LEFT JOIN
		SEGURIDAD.ROL_FUNCION RF ON (F.ID_APLICACION=RF.ID_APLICACION AND F.ID_MODULO = RF.ID_MODULO AND F.ID_FUNCION=RF.ID_FUNCION) LEFT JOIN
		SEGURIDAD.ROL RL ON (RL.ID_APLICACION = RF.ID_APLICACION AND RL.ID_ROL = RF.ID_ROL) LEFT JOIN
		SEGURIDAD.USUARIO_ROL UR ON (UR.ID_APLICACION=RF.ID_APLICACION AND UR.ID_ROL = RF.ID_ROL) LEFT JOIN
		SEGURIDAD.USUARIO U ON (U.ID_USUARIO=UR.ID_USUARIO and U.ID_USUARIO= $1)
), 
q AS(
	SELECT
		H.*,
		1 AS level, 
		ARRAY[ID_APLICACION||'-'||ID_MODULO||'-'||ID_FUNCION||'-'||orden] AS breadcrumb 
	FROM
		H
	where
		ID_FUNCION_PADRE is null        
	UNION ALL
	SELECT
		case
			WHEN hi.ID_USUARIO IS NULL THEN q.id_usuario
			else hi.ID_USUARIO 
		end as ID_USUARIO,
		case
			WHEN hi.PASSWORD IS NULL THEN q.PASSWORD
			else hi.PASSWORD 
		end as PASSWORD,
		case
			WHEN hi.ELIMINADO IS NULL THEN q.ELIMINADO
			else hi.ELIMINADO 
		end as ELIMINADO,
		case
			WHEN hi.BLOQUEADO IS NULL THEN q.BLOQUEADO
			else hi.BLOQUEADO 
		end as BLOQUEADO,
		case
			WHEN hi.ACTIVO IS NULL THEN q.ACTIVO
			else hi.ACTIVO 
		end as ACTIVO,
		case
			WHEN hi.ID_APLICACION IS NULL THEN q.ID_APLICACION
			else hi.ID_APLICACION 
		end as ID_APLICACION,
                hi.DESCRIPCION_APLICACION,
                hi.ICONO_APLICACION,
		case
			WHEN hi.ID_ROL IS NULL THEN q.ID_ROL
			else hi.ID_ROL 
		end as ID_ROL,
		case
			WHEN hi.DESCRIPCION_ROL IS NULL THEN q.DESCRIPCION_ROL
			else hi.DESCRIPCION_ROL 
		end as DESCRIPCION_ROL,
		case
			WHEN hi.HABILITADO_ROL IS NULL THEN q.HABILITADO_ROL
			else hi.HABILITADO_ROL 
		end as HABILITADO_ROL,
		hi.ID_MODULO,
		hi.DESCRIPCION_MODULO,
		hi.ICONO_MODULO,
		hi.ID_FUNCION,
		hi.TITULO,
		hi.DESCRIPCION_FUNCION,
		hi.PROCESS_KEY,
		hi.ORDEN,
		hi.HORARIO,
		case
			WHEN q.ACCESO_COMPLETO='true' THEN q.ACCESO_COMPLETO
			else hi.ACCESO_COMPLETO 
		end as ACCESO_COMPLETO,
		hi.ID_FUNCION_PADRE,
		hi.ID_PARAM,
		hi.VALOR,
		q.level + 1 AS level,
		Q.breadcrumb || (q.ID_APLICACION||'-'||q.ID_MODULO||'-'||q.ID_FUNCION||'-'||q.orden)
	FROM
		q
	JOIN
		h hi ON (hi.ID_APLICACION = q.ID_APLICACION AND hi.ID_MODULO = q.id_MODULO AND hi.ID_FUNCION_PADRE = q.id_FUNCION )
),
R AS (
SELECT
	q.ID_USUARIO,
	q.PASSWORD,
	q.ELIMINADO,
	q.BLOQUEADO,
	q.ACTIVO,
	q.ID_APLICACION,
        q.DESCRIPCION_APLICACION,
        q.ICONO_APLICACION,
	q.ID_ROL,
	q.DESCRIPCION_ROL,
	q.HABILITADO_ROL,
	q.ID_MODULO,
	q.DESCRIPCION_MODULO,
	q.ICONO_MODULO,
	q.ID_FUNCION,
	q.TITULO,
	q.DESCRIPCION_FUNCION,
	q.PROCESS_KEY,
	q.ORDEN,
	q.HORARIO,
	q.ACCESO_COMPLETO,
	q.ID_FUNCION_PADRE,
	q.ID_PARAM,
	q.VALOR
FROM
	q
),
Q2 AS (
SELECT
		R.*,
		1 AS level, 
		ARRAY[ID_APLICACION||'-'||ID_MODULO||'-'||ID_FUNCION||'-'||orden] AS breadcrumb
	FROM
		R
	where
		ACCESO_COMPLETO ='false'       
	UNION ALL
	SELECT
		case
			WHEN hi.ID_USUARIO IS NULL THEN q2.id_usuario
			else hi.ID_USUARIO 
		end as ID_USUARIO,
		case
			WHEN hi.PASSWORD IS NULL THEN q2.PASSWORD
			else hi.PASSWORD 
		end as PASSWORD,
		case
			WHEN hi.ELIMINADO IS NULL THEN q2.ELIMINADO
			else hi.ELIMINADO 
		end as ELIMINADO,
		case
			WHEN hi.BLOQUEADO IS NULL THEN q2.BLOQUEADO
			else hi.BLOQUEADO 
		end as BLOQUEADO,
		case
			WHEN hi.ACTIVO IS NULL THEN q2.ACTIVO
			else hi.ACTIVO 
		end as ACTIVO,
		case
			WHEN hi.ID_APLICACION IS NULL THEN q2.ID_APLICACION
			else hi.ID_APLICACION 
		end as ID_APLICACION,
                hi.DESCRIPCION_APLICACION,
                hi.ICONO_APLICACION,
		case
			WHEN hi.ID_ROL IS NULL THEN q2.ID_ROL
			else hi.ID_ROL 
		end as ID_ROL,
          case
			WHEN hi.DESCRIPCION_ROL IS NULL THEN q2.DESCRIPCION_ROL
			else hi.DESCRIPCION_ROL 
		end as DESCRIPCION_ROL,
          case
			WHEN hi.HABILITADO_ROL IS NULL THEN q2.HABILITADO_ROL
			else hi.HABILITADO_ROL 
		end as DESCRIPCION_ROL,
		hi.ID_MODULO,
		hi.DESCRIPCION_MODULO,
		hi.ICONO_MODULO,
		hi.ID_FUNCION,
		hi.TITULO,
		hi.DESCRIPCION_FUNCION,
		hi.PROCESS_KEY,
		hi.ORDEN,
		hi.HORARIO,
		case
			WHEN hi.ACCESO_COMPLETO IS NULL THEN q2.ACCESO_COMPLETO
			else hi.ACCESO_COMPLETO 
		end as ACCESO_COMPLETO,
		hi.ID_FUNCION_PADRE,
		hi.ID_PARAM,
		hi.VALOR,
		q2.level + 1 AS level,
		Q2.breadcrumb || (q2.ID_APLICACION||'-'||q2.ID_MODULO||'-'||q2.ID_FUNCION||'-'||q2.orden)
	FROM
		q2
	JOIN
		R hi ON (hi.ID_APLICACION = q2.ID_APLICACION AND hi.ID_MODULO = q2.id_MODULO AND hi.ID_FUNCION = q2.id_FUNCION_PADRE )
), MENU_USUARIO AS (
SELECT 
	q2.ID_USUARIO,
	q2.PASSWORD,
	q2.ELIMINADO,
	q2.BLOQUEADO,
	q2.ACTIVO,
	q2.ID_APLICACION,
        q2.DESCRIPCION_APLICACION,
        q2.ICONO_APLICACION,
	q2.ID_ROL,
	q2.DESCRIPCION_ROL,
	q2.HABILITADO_ROL,
	q2.ID_MODULO,
	q2.DESCRIPCION_MODULO,
	q2.ICONO_MODULO,
	q2.ID_FUNCION,
	q2.TITULO,
	q2.DESCRIPCION_FUNCION,
	q2.PROCESS_KEY,
	q2.ORDEN,
	q2.HORARIO,
	q2.ACCESO_COMPLETO,
	q2.ID_FUNCION_PADRE,
	q2.ID_PARAM,
	q2.VALOR,
	q.level,
	Q.breadcrumb::varchar
FROM 
	Q2 join Q ON(q2.ID_APLICACION=Q.ID_APLICACION AND q2.ID_MODULO=Q.ID_MODULO AND q2.ID_FUNCION=Q.ID_FUNCION)
UNION ALL
SELECT
	q.ID_USUARIO,
	q.PASSWORD,
	q.ELIMINADO,
	q.BLOQUEADO,
	q.ACTIVO,
	q.ID_APLICACION,
        q.DESCRIPCION_APLICACION,
        q.ICONO_APLICACION,
	q.ID_ROL,
	q.DESCRIPCION_ROL,
	q.HABILITADO_ROL,
	q.ID_MODULO,
	q.DESCRIPCION_MODULO,
	q.ICONO_MODULO,
	q.ID_FUNCION,
	q.TITULO,
	q.DESCRIPCION_FUNCION,
	q.PROCESS_KEY,
	q.ORDEN,
	q.HORARIO,
	q.ACCESO_COMPLETO,
	q.ID_FUNCION_PADRE,
	q.ID_PARAM,
	q.VALOR,
	q.level,
	Q.breadcrumb::varchar	
FROM
	Q
WHERE Q.ACCESO_COMPLETO='true'
)
SELECT
	MENU_USUARIO.ID_USUARIO,
	MENU_USUARIO.PASSWORD,
	MENU_USUARIO.ELIMINADO,
	MENU_USUARIO.BLOQUEADO,
	MENU_USUARIO.ACTIVO,
	MENU_USUARIO.ID_APLICACION,
        MENU_USUARIO.DESCRIPCION_APLICACION,
        MENU_USUARIO.ICONO_APLICACION,
                MENU_USUARIO.ID_ROL,
                MENU_USUARIO.DESCRIPCION_ROL,
                MENU_USUARIO.HABILITADO_ROL,
                        MENU_USUARIO.ID_MODULO,
                        MENU_USUARIO.DESCRIPCION_MODULO,
                        MENU_USUARIO.ICONO_MODULO,
                                MENU_USUARIO.ID_FUNCION,
                                MENU_USUARIO.TITULO,
                                MENU_USUARIO.DESCRIPCION_FUNCION,
                                MENU_USUARIO.PROCESS_KEY,
                                MENU_USUARIO.ORDEN,
                                MENU_USUARIO.HORARIO,
                                MENU_USUARIO.ID_PARAM,
                                MENU_USUARIO.VALOR,
                                MENU_USUARIO.ID_FUNCION_PADRE,
                                        F.TITULO AS TITULO_PADRE,
                                        F.DESCRIPCION AS DESCRIPCION_PADRE,
                                        F.PROCESS_KEY AS PROCESS_KEY_PADRE,
                                        F.ORDEN AS ORDEN_PADRE,
                                        PF.ID_PARAM AS ID_PARAM_PADRE,
                                        PF.VALOR AS VALOR_PADRE
FROM
	MENU_USUARIO LEFT JOIN
	SEGURIDAD.FUNCION F ON(F.ID_APLICACION = MENU_USUARIO.ID_APLICACION AND F.ID_MODULO = MENU_USUARIO.ID_MODULO AND F.ID_FUNCION = MENU_USUARIO.ID_FUNCION_PADRE)LEFT JOIN 
	SEGURIDAD.PARAM_FUNCION PF ON (PF.ID_APLICACION = F.ID_APLICACION AND PF.ID_MODULO=F.ID_MODULO AND PF.ID_FUNCION=F.ID_FUNCION)
where 
	MENU_USUARIO.ID_USUARIO is not null
	AND 	MENU_USUARIO.HABILITADO_ROL=TRUE
ORDER BY
        MENU_USUARIO.breadcrumb,
        MENU_USUARIO.orden,
        MENU_USUARIO.TITULO
$body$
language sql;

CREATE TABLE JOSSO_SESSION
(
    SESSION_ID                       VARCHAR (64)                   NOT NULL
  , USERNAME                         VARCHAR (128)                  NOT NULL
  , CREATION_TIME                    INT8                         NOT NULL
  , LAST_ACCESS_TIME                 INT8                         NOT NULL
  , ACCESS_COUNT                     INTEGER                         NOT NULL
  , MAX_INACTIVE_INTERVAL            INTEGER                         NOT NULL
  , VALID                            BOOLEAN                         NOT NULL
);

ALTER TABLE JOSSO_SESSION
       ADD  PRIMARY KEY (SESSION_ID);

CREATE TABLE JOSSO_ASSERTION
(
    ASSERTION_ID                     VARCHAR (64)                   NOT NULL
  , SECURITY_DOMAIN_NAME             VARCHAR (64)                   NOT NULL
  , SSO_SESSION_ID                   VARCHAR (64)                   NOT NULL
  , CREATION_TIME                    INT8                           NOT NULL
  , VALID                            BOOLEAN                        NOT NULL
);

ALTER TABLE JOSSO_ASSERTION
       ADD  PRIMARY KEY (ASSERTION_ID);
