/*declare @owner_user;
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
    id_aplicacion varchar(30) not null,
    descripcion varchar(100) not null,
    get_all_uri varchar(100) not null,
    constraint unique_uk_2 unique(class)
);

create index acl_class_aplicacion_fk_idx on ACL_CLASS(id_aplicacion);

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

create table acl_fullreadaccess_sid(
	sid bigint not null,
	object_id_class bigint not null,
	constraint unique_uk_5 unique(sid,object_id_class),
	constraint foreign_fk_6 foreign key(sid) references acl_sid(id)  ON UPDATE NO ACTION ON DELETE NO ACTION,
	constraint foreign_fk_7 foreign key(object_id_class)references acl_class(id)  ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE INDEX acl_fullreadaccess_sid_FK6IDX ON acl_fullreadaccess_sid USING BTREE (sid);
CREATE INDEX acl_fullreadaccess_sid_FK7IDX ON acl_fullreadaccess_sid USING BTREE (object_id_class);

CREATE OR REPLACE FUNCTION MANAGE_FULLACCESSREAD_SIDS_ACES() RETURNS TRIGGER AS $body$
DECLARE
 currSid bigint;
 BEGIN
	 FOR currSid IN select sid from seguridad.acl_fullreadaccess_sid where object_id_class = NEW.object_id_class LOOP
		 insert into seguridad.acl_entry(ace_order,acl_object_identity,audit_failure,audit_success,granting,mask,sid)
		 select
		  coalesce(max(ace.ace_order)+1,0) as ace_order,
		  oi.id as acl_object_identity,
		  false audit_failure,
		  false audit_success,
		  true granting,
		  1 as mask,
		  afras.sid
		 from
		 seguridad.acl_object_identity oi inner join
		 seguridad.acl_fullreadaccess_sid afras on(oi.object_id_class = afras.object_id_class) left join
		 seguridad.acl_entry ace on(oi.id=ace.acl_object_identity)
		 where
		 not exists (select 1 as existe from seguridad.acl_entry iace where iace.acl_object_identity = ace.acl_object_identity and iace.sid = afras.sid )
		 and afras.object_id_class = NEW.object_id_class
		 and afras.sid=currSid
		 group by
		 afras.sid,
		 oi.id;
	end loop;
    RETURN NEW;
 END;
$body$ LANGUAGE plpgsql;


CREATE TRIGGER ACL_UPDATE_FULLACCESREAD_SIDS_ENTRIES_TRGR AFTER INSERT ON acl_object_identity FOR EACH ROW EXECUTE PROCEDURE MANAGE_FULLACCESSREAD_SIDS_ACES();

INSERT INTO USUARIO(ID_USUARIO,PASSWORD,ESTATUS) VALUES('admin@gunix.mx',crypt('loloq123', gen_salt('bf', 16)),'ACTIVO');
INSERT INTO USUARIO(ID_USUARIO,PASSWORD,ESTATUS) VALUES('anonymous',crypt('anonymous', gen_salt('bf', 16)),'ACTIVO');
INSERT INTO acl_sid(principal,sid) VALUES(true,'admin@gunix.mx');

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

create table DATOS_USUARIO (
ID_USUARIO varchar(254) not null,
CURP varchar(18) not null,
RFC  varchar(13) not null,
AP_PATERNO varchar(50) not null,
AP_MATERNO varchar(50),
NOMBRE varchar(100) not null,
CORREO_ELECTRONICO varchar(50) not null,
TELEFONO varchar(30),
CONSTRAINT curp UNIQUE(CURP),
CONSTRAINT rfc UNIQUE(RFC));

alter table DATOS_USUARIO     
add constraint DAT_FK_USUARIO foreign key (ID_USUARIO)
references USUARIO (ID_USUARIO); 

alter table DATOS_USUARIO     
add primary key(ID_USUARIO); 

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
CREATE TYPE VIEW_ENGINE AS ENUM ('VAADIN','SPRINGMVC');

CREATE TABLE FUNCION
(
    ID_APLICACION VARCHAR(30) NOT NULL,
	ID_MODULO VARCHAR(30) NOT NULL,
	ID_FUNCION VARCHAR(30) NOT NULL,
	TITULO VARCHAR(50) NOT NULL,
	DESCRIPCION VARCHAR(100) NOT NULL,
	PROCESS_KEY VARCHAR(255) NOT NULL DEFAULT '',
	ORDEN NUMERIC(4,2) NOT NULL DEFAULT 0.0,
	HORARIO HORARIO_OPERACION NOT NULL DEFAULT 'LD24',
	VENGINE VIEW_ENGINE,
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

create table SAML_AUTHS
(
	ID_APLICACION VARCHAR(30) NOT NULL,
	ID_USUARIO VARCHAR(254) NOT NULL,
	ID_SESION_LOCAL VARCHAR(32) NOT NULL,
	ID_SESION_SSO VARCHAR(36) NOT NULL,
	CREATE_TIME timestamp NOT NULL default now(),
	PRIMARY KEY(ID_APLICACION, ID_USUARIO, ID_SESION_LOCAL, ID_SESION_SSO)
);

INSERT INTO acl_class(class,descripcion,get_all_uri,id_aplicacion) values ('mx.com.gunix.framework.security.domain.Aplicacion','Aplicaciones alojadas en Gunix','http://localhost:8081/map-backEnd?servicio=aplicacionService','ADMIN_APP');
INSERT INTO acl_object_identity(object_id_class,owner_sid,entries_inheriting) values( currval(pg_get_serial_sequence('acl_class', 'id')),(select id from acl_sid where sid = 'admin@gunix.mx'),true);

/*INSERT INTO acl_entry(acl_object_identity, ace_order, sid, mask, granting, audit_success,audit_failure) VALUES (currval(pg_get_serial_sequence('acl_object_identity', 'id')), 0, (select id from acl_sid where sid = 'admin@gunix.mx'), 27, TRUE, FALSE, FALSE);*/

INSERT INTO APLICACION VALUES('ADMIN_APP',currval(pg_get_serial_sequence('acl_object_identity', 'object_id_identity')),'Gunix Admin App','Gunix.png');
alter table ACL_CLASS add CONSTRAINT "acl_class_aplicacion_fk" FOREIGN KEY (id_aplicacion) REFERENCES aplicacion (id_aplicacion)  ON UPDATE NO ACTION ON DELETE NO ACTION;

	INSERT INTO USUARIO_APLICACION VALUES('admin@gunix.mx','ADMIN_APP');

        /* Funcionalidad Privada (Segura) */
        INSERT INTO MODULO VALUES('ADMIN_APP','ADMINISTRACION','Administración','window-128.png');
                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,ORDEN) VALUES('ADMIN_APP','ADMINISTRACION','APLICACIONES','Aplicaciones','Menú con opciones de Administración de aplicaciones',1);
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','ADMINISTRACION','ALTA_APP','Alta','Alta de Aplicaciones','AdministraciónAplicaciones','APLICACIONES',1);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','ADMINISTRACION','ALTA_APP','operación','Alta');
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','ADMINISTRACION','CONSULTA_APP','Consulta','Consulta de Aplicaciones','AdministraciónAplicaciones','APLICACIONES',2);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','ADMINISTRACION','CONSULTA_APP','operación','Consulta');
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','ADMINISTRACION','MODIFICACION_APP','Modificación','Modificación de Aplicaciones','AdministraciónAplicaciones','APLICACIONES',3);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','ADMINISTRACION','MODIFICACION_APP','operación','Modificación');

                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,ORDEN) VALUES('ADMIN_APP','ADMINISTRACION','USUARIOS','Usuarios','Menú con opciones de Administración de usuarios',1);
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','ADMINISTRACION','ALTA_USER','Alta','Alta de Usuarios','AdministraciónUsuarios','USUARIOS',1);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','ADMINISTRACION','ALTA_USER','operación','Alta');
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','ADMINISTRACION','CONSULTA_USER','Consulta','Consulta de Usuarios','AdministraciónUsuarios','USUARIOS',2);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','ADMINISTRACION','CONSULTA_USER','operación','Consulta');
	                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','ADMINISTRACION','MODIFICACION_USER','Modificación','Modificación de Usuarios','AdministraciónUsuarios','USUARIOS',3);
	                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','ADMINISTRACION','MODIFICACION_USER','operación','Modificación');
	                        
        INSERT INTO ROL VALUES('ADMIN_APP','ADMINISTRADOR','Administrador');
	     	 INSERT INTO ROL_FUNCION VALUES('ADMIN_APP','ADMINISTRADOR','ADMINISTRACION','APLICACIONES','COMPLETO');
	     	 INSERT INTO ROL_FUNCION VALUES('ADMIN_APP','ADMINISTRADOR','ADMINISTRACION','USUARIOS','COMPLETO');

        INSERT INTO ROL VALUES('ADMIN_APP','ACL_ADMIN','Administrador de la Lista de Control de Acceso');

        INSERT INTO USUARIO_ROL VALUES('admin@gunix.mx','ADMIN_APP','ADMINISTRADOR');
        INSERT INTO USUARIO_ROL VALUES('admin@gunix.mx','ADMIN_APP','ACL_ADMIN');
        
        /* Funcionalidad Pública */
        INSERT INTO ROL VALUES('ADMIN_APP','PUBLIC','Público en General');
        INSERT INTO MODULO VALUES('ADMIN_APP','MODULO_PRUEBA','Demostraciones','1436568412_demo.png');
                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,ORDEN) VALUES('ADMIN_APP','MODULO_PRUEBA','DEMOS','Demos','Menú con funcionalidad de demostración',1);
                	INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','MODULO_PRUEBA','ALTA_FORM','Formulario Alta','Demostración de Formularios','DemoForm','DEMOS',1);
                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','MODULO_PRUEBA','ALTA_FORM','operación','Alta');

        INSERT INTO ROL_FUNCION VALUES('ADMIN_APP','PUBLIC','MODULO_PRUEBA','DEMOS','COMPLETO');

        INSERT INTO USUARIO_ROL VALUES('anonymous','ADMIN_APP','PUBLIC');

CREATE OR REPLACE FUNCTION menu_usuario(id_usr text)
 RETURNS TABLE(id_usuario text, password text, eliminado text, bloqueado text, activo text, id_aplicacion text, descripcion_aplicacion text, icono_aplicacion text, id_rol text, descripcion_rol text, habilitado_rol boolean, id_modulo text, descripcion_modulo text, icono_modulo text, id_funcion text, titulo text, descripcion_funcion text, process_key text, orden numeric, horario text, vengine text, id_param text, valor text, id_funcion_padre text, titulo_padre text, descripcion_padre text, process_key_padre text, orden_padre numeric, id_param_padre text, valor_padre text)
AS $function$
DECLARE 
    usuario record;
    appRol record;
    funcion record;
BEGIN
	FOR usuario IN(select
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
				end as ACTIVO
			from
				SEGURIDAD.USUARIO U
			where 
				U.ID_USUARIO= id_usr) LOOP
		FOR appRol IN(select
				APP.ID_APLICACION,
				APP.DESCRIPCION AS DESCRIPCION_APLICACION,
				APP.ICONO AS ICONO_APLICACION,
					UR.ID_ROL,
					RL.DESCRIPCION AS DESCRIPCION_ROL,
					RL.HABILITADO AS HABILITADO_ROL
				from
					SEGURIDAD.APLICACION APP inner JOIN
					SEGURIDAD.USUARIO_ROL UR ON (UR.ID_APLICACION=APP.ID_APLICACION) inner JOIN
					SEGURIDAD.ROL RL ON (RL.ID_APLICACION = UR.ID_APLICACION AND RL.ID_ROL = UR.ID_ROL)
				where 
					UR.ID_USUARIO= id_usr
				order by APP.id_aplicacion, UR.id_rol) LOOP
			FOR funcion IN(WITH RECURSIVE
							h as(
								select
							        F.ID_APLICACION,
									F.ID_MODULO,
									F.ID_FUNCION,
									F.ORDEN,
									case 
										when RF.NIV_ACC='COMPLETO' then 'true'
										when RF.NIV_ACC IS NULL then NULL
										else 'false' 
									end as ACCESO_COMPLETO,
									F.ID_FUNCION_PADRE
								from
									SEGURIDAD.FUNCION F  LEFT JOIN
									SEGURIDAD.ROL_FUNCION RF ON (F.ID_APLICACION=RF.ID_APLICACION AND F.ID_MODULO = RF.ID_MODULO AND F.ID_FUNCION=RF.ID_FUNCION AND RF.ID_APLICACION=appRol.id_aplicacion AND RF.ID_ROL=appRol.id_rol)
							), 
							q AS(
								SELECT
									H.*,
									1 AS level, 
									ARRAY[H.ID_APLICACION||'-'||H.ID_MODULO||'-'||H.ID_FUNCION||'-'||H.orden] AS breadcrumb 
								FROM
									H
								where
									H.ID_FUNCION_PADRE is null        
								UNION 
								SELECT
									case
										WHEN hi.ID_APLICACION IS NULL THEN q.ID_APLICACION
										else hi.ID_APLICACION 
									end as ID_APLICACION,
									hi.ID_MODULO,
									hi.ID_FUNCION,
									hi.ORDEN,
									case
										WHEN q.ACCESO_COMPLETO='true' THEN q.ACCESO_COMPLETO
										else hi.ACCESO_COMPLETO 
									end as ACCESO_COMPLETO,
									hi.ID_FUNCION_PADRE,
									q.level + 1 AS level,
									Q.breadcrumb || (q.ID_APLICACION||'-'||q.ID_MODULO||'-'||q.ID_FUNCION||'-'||q.orden)
								FROM
									q
								JOIN
									h hi ON (hi.ID_APLICACION = q.ID_APLICACION AND hi.ID_MODULO = q.id_MODULO AND hi.ID_FUNCION_PADRE = q.id_FUNCION )
							),
							R AS (
							SELECT
								q.ID_APLICACION,
								q.ID_MODULO,
								q.ID_FUNCION,
								q.ORDEN,
								q.ACCESO_COMPLETO,
								q.ID_FUNCION_PADRE
							FROM
								q
							),
							Q2 AS (
							SELECT
									R.*,
									1 AS level, 
									ARRAY[R.ID_APLICACION||'-'||R.ID_MODULO||'-'||R.ID_FUNCION||'-'||R.orden] AS breadcrumb
								FROM
									R
								where
									R.ACCESO_COMPLETO ='false'       
								UNION 
								SELECT
									case
										WHEN hi.ID_APLICACION IS NULL THEN q2.ID_APLICACION
										else hi.ID_APLICACION 
									end as ID_APLICACION,
									hi.ID_MODULO,
									hi.ID_FUNCION,
									hi.ORDEN,
									case
										WHEN hi.ACCESO_COMPLETO IS NULL THEN q2.ACCESO_COMPLETO
										else hi.ACCESO_COMPLETO 
									end as ACCESO_COMPLETO,
									hi.ID_FUNCION_PADRE,
									q2.level + 1 AS level,
									Q2.breadcrumb || (q2.ID_APLICACION||'-'||q2.ID_MODULO||'-'||q2.ID_FUNCION||'-'||q2.orden)
								FROM
									q2
								JOIN
									R hi ON (hi.ID_APLICACION = q2.ID_APLICACION AND hi.ID_MODULO = q2.id_MODULO AND hi.ID_FUNCION = q2.id_FUNCION_PADRE )
							), FUNCIONES_ROL AS (
							SELECT 
								q2.ID_APLICACION,
								q2.ID_MODULO,
								q2.ID_FUNCION,
								q2.ORDEN,
								q2.ACCESO_COMPLETO,
								q2.ID_FUNCION_PADRE,
								q.level,
								Q.breadcrumb::varchar
							FROM 
								Q2 join Q ON(q2.ID_APLICACION=Q.ID_APLICACION AND q2.ID_MODULO=Q.ID_MODULO AND q2.ID_FUNCION=Q.ID_FUNCION)
							UNION 
							SELECT
								q.ID_APLICACION,
								q.ID_MODULO,
								q.ID_FUNCION,
								q.ORDEN,
								q.ACCESO_COMPLETO,
								q.ID_FUNCION_PADRE,
								q.level,
								Q.breadcrumb::varchar	
							FROM
								Q
							WHERE Q.ACCESO_COMPLETO='true'
							)
							SELECT                     
								M.ID_MODULO,
		                        M.DESCRIPCION AS DESCRIPCION_MODULO,
		                        M.ICONO AS ICONO_MODULO,
		                                F.ID_FUNCION,
		                                F.TITULO,
		                                F.DESCRIPCION AS DESCRIPCION_FUNCION,
		                                F.PROCESS_KEY,
		                                F.ORDEN,
		                                F.HORARIO,
		                                F.VENGINE,
		                                PF.ID_PARAM,
		                                PF.VALOR,
		                                F.ID_FUNCION_PADRE,
		                                        FP.TITULO AS TITULO_PADRE,
		                                        FP.DESCRIPCION AS DESCRIPCION_PADRE,
		                                        FP.PROCESS_KEY AS PROCESS_KEY_PADRE,
		                                        FP.ORDEN AS ORDEN_PADRE,
		                                        PFP.ID_PARAM AS ID_PARAM_PADRE,
		                                        PFP.VALOR AS VALOR_PADRE
							FROM
								FUNCIONES_ROL FR INNER JOIN
								SEGURIDAD.FUNCION F ON(F.ID_APLICACION = FR.ID_APLICACION AND F.ID_MODULO = FR.ID_MODULO AND F.ID_FUNCION = FR.ID_FUNCION) INNER JOIN
								SEGURIDAD.MODULO M ON (M.ID_APLICACION=F.ID_APLICACION AND M.ID_MODULO=F.ID_MODULO) LEFT JOIN
								SEGURIDAD.PARAM_FUNCION PF ON (PF.ID_APLICACION=F.ID_APLICACION AND PF.ID_MODULO=F.ID_MODULO AND PF.ID_FUNCION=F.ID_FUNCION) LEFT JOIN
								SEGURIDAD.FUNCION FP ON(FP.ID_APLICACION = FR.ID_APLICACION AND FP.ID_MODULO = FR.ID_MODULO AND FP.ID_FUNCION = FR.ID_FUNCION_PADRE) LEFT JOIN 
								SEGURIDAD.PARAM_FUNCION PFP ON (PFP.ID_APLICACION = FP.ID_APLICACION AND PFP.ID_MODULO=FP.ID_MODULO AND PFP.ID_FUNCION=FP.ID_FUNCION)
							ORDER BY
							        FR.breadcrumb DESC,
							        FR.orden) LOOP
				id_usuario := usuario.ID_USUARIO;
				password := usuario.PASSWORD;
				eliminado := usuario.ELIMINADO;
				bloqueado := usuario.bloqueado;
				activo := usuario.activo;
				id_aplicacion := appRol.id_aplicacion;
				descripcion_aplicacion := appRol.descripcion_aplicacion;
				icono_aplicacion := appRol.icono_aplicacion;
				id_rol := appRol.id_rol;
				descripcion_rol := appRol.descripcion_rol;
				habilitado_rol := appRol.habilitado_rol;
				id_modulo := funcion.id_modulo;
				descripcion_modulo := funcion.descripcion_modulo;
				icono_modulo := funcion.icono_modulo;
				id_funcion := funcion.id_funcion;
				titulo := funcion.titulo;
				descripcion_funcion := funcion.descripcion_funcion;
				process_key := funcion.process_key;
				orden := funcion.orden;
				horario := funcion.horario;
				vengine := funcion.vengine;
				id_param := funcion.id_param;
				valor := funcion.valor;
				id_funcion_padre := funcion.id_funcion_padre;
				titulo_padre := funcion.titulo_padre;
				descripcion_padre := funcion.descripcion_padre;
				process_key_padre := funcion.process_key_padre;
				orden_padre := funcion.orden_padre;
				id_param_padre := funcion.id_param_padre;
				valor_padre := funcion.valor_padre;
				RETURN NEXT;
			END LOOP;
		END LOOP;
	END LOOP;
END;
$function$
LANGUAGE 'plpgsql' VOLATILE;

create or replace function FUNCIONES_ROL(id_aplicacion text, id_rol text)
  returns table (
	ID_APLICACION text,
                        ID_MODULO text,
                                ID_FUNCION text,
                                ID_FUNCION_PADRE text)
as
$body$
WITH RECURSIVE
h as(
	select
          F.ID_APLICACION,
		F.ID_MODULO,
		F.ID_FUNCION,
		F.ORDEN,
		case 
			when RF.NIV_ACC='COMPLETO' then 'true'
			when RF.NIV_ACC IS NULL then NULL
			else 'false' 
		end as ACCESO_COMPLETO,
		F.ID_FUNCION_PADRE
	from
		SEGURIDAD.FUNCION F  LEFT JOIN
		SEGURIDAD.ROL_FUNCION RF ON (F.ID_APLICACION=RF.ID_APLICACION AND F.ID_MODULO = RF.ID_MODULO AND F.ID_FUNCION=RF.ID_FUNCION AND RF.ID_APLICACION=$1 AND RF.ID_ROL=$2)
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
			WHEN hi.ID_APLICACION IS NULL THEN q.ID_APLICACION
			else hi.ID_APLICACION 
		end as ID_APLICACION,
		hi.ID_MODULO,
		hi.ID_FUNCION,
		hi.ORDEN,
		case
			WHEN q.ACCESO_COMPLETO='true' THEN q.ACCESO_COMPLETO
			else hi.ACCESO_COMPLETO 
		end as ACCESO_COMPLETO,
		hi.ID_FUNCION_PADRE,
		q.level + 1 AS level,
		Q.breadcrumb || (q.ID_APLICACION||'-'||q.ID_MODULO||'-'||q.ID_FUNCION||'-'||q.orden)
	FROM
		q
	JOIN
		h hi ON (hi.ID_APLICACION = q.ID_APLICACION AND hi.ID_MODULO = q.id_MODULO AND hi.ID_FUNCION_PADRE = q.id_FUNCION )
),
R AS (
SELECT
	q.ID_APLICACION,
	q.ID_MODULO,
	q.ID_FUNCION,
	q.ORDEN,
	q.ACCESO_COMPLETO,
	q.ID_FUNCION_PADRE
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
			WHEN hi.ID_APLICACION IS NULL THEN q2.ID_APLICACION
			else hi.ID_APLICACION 
		end as ID_APLICACION,
		hi.ID_MODULO,
		hi.ID_FUNCION,
		hi.ORDEN,
		case
			WHEN hi.ACCESO_COMPLETO IS NULL THEN q2.ACCESO_COMPLETO
			else hi.ACCESO_COMPLETO 
		end as ACCESO_COMPLETO,
		hi.ID_FUNCION_PADRE,
		q2.level + 1 AS level,
		Q2.breadcrumb || (q2.ID_APLICACION||'-'||q2.ID_MODULO||'-'||q2.ID_FUNCION||'-'||q2.orden)
	FROM
		q2
	JOIN
		R hi ON (hi.ID_APLICACION = q2.ID_APLICACION AND hi.ID_MODULO = q2.id_MODULO AND hi.ID_FUNCION = q2.id_FUNCION_PADRE )
), FUNCIONES_ROL AS (
SELECT 
	q2.ID_APLICACION,
	q2.ID_MODULO,
	q2.ID_FUNCION,
	q2.ORDEN,
	q2.ACCESO_COMPLETO,
	q2.ID_FUNCION_PADRE,
	q.level,
	Q.breadcrumb::varchar
FROM 
	Q2 join Q ON(q2.ID_APLICACION=Q.ID_APLICACION AND q2.ID_MODULO=Q.ID_MODULO AND q2.ID_FUNCION=Q.ID_FUNCION)
UNION ALL
SELECT
	q.ID_APLICACION,
	q.ID_MODULO,
	q.ID_FUNCION,
	q.ORDEN,
	q.ACCESO_COMPLETO,
	q.ID_FUNCION_PADRE,
	q.level,
	Q.breadcrumb::varchar	
FROM
	Q
WHERE Q.ACCESO_COMPLETO='true'
)
SELECT
	FUNCIONES_ROL.ID_APLICACION,
                        FUNCIONES_ROL.ID_MODULO,
                                FUNCIONES_ROL.ID_FUNCION,
                                FUNCIONES_ROL.ID_FUNCION_PADRE
FROM
	FUNCIONES_ROL 
ORDER BY
        FUNCIONES_ROL.breadcrumb,
        FUNCIONES_ROL.orden;
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
