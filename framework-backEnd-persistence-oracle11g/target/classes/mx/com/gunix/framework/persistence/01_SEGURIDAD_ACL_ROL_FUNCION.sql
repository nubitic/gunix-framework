alter session set current_schema = seguridad;

CREATE TABLE USUARIO
(
	ID_USUARIO VARCHAR2(254) NOT NULL,
	PASSWORD VARCHAR2(60),
	ESTATUS VARCHAR2(15) DEFAULT 'SIN PASSWORD' NOT NULL check (ESTATUS in ('ACTIVO','ELIMINADO','BLOQUEADO','SIN PASSWORD'))
);

ALTER TABLE USUARIO ADD CONSTRAINT "USUARIO_PK" PRIMARY KEY (ID_USUARIO);

create table acl_sid(
    id number not null primary key,
    principal number(1) not NULL check(principal IN (0,1)),
    sid VARCHAR2(254) not null,
    constraint unique_uk_1 unique(sid)
);

CREATE SEQUENCE acl_sid_id START WITH 1;

CREATE OR REPLACE TRIGGER acl_sid_id_trg 
BEFORE INSERT ON acl_sid 
FOR EACH ROW

BEGIN
  SELECT acl_sid_id.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/

ALTER TABLE acl_sid ADD CONSTRAINT "acl_sid_USUARIO_FK1" FOREIGN KEY (sid) REFERENCES USUARIO (ID_USUARIO);

create table acl_class(
    id number not null primary key,
    class varchar2(100) not null,
    id_aplicacion varchar2(30) not null,
    descripcion varchar2(100) not null,
    get_all_uri varchar2(100) not null,
    constraint unique_uk_2 unique(class)
);

CREATE SEQUENCE acl_class_id START WITH 1;

CREATE OR REPLACE TRIGGER acl_class_id_trg 
BEFORE INSERT ON acl_class 
FOR EACH ROW
BEGIN
  SELECT acl_class_id.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/

create index acl_class_aplicacion_fk_idx on ACL_CLASS(id_aplicacion);

create table acl_object_identity(
    id number primary key,
    object_id_class number not null,
    object_id_identity number NOT NULL,
    parent_object number,
    owner_sid number,
    entries_inheriting NUMBER(1) not null check(entries_inheriting IN (0,1)),
    constraint unique_uk_3 unique(object_id_identity),
    constraint foreign_fk_1 foreign key(parent_object)references acl_object_identity(id),
    constraint foreign_fk_2 foreign key(object_id_class)references acl_class(id),
    constraint foreign_fk_3 foreign key(owner_sid)references acl_sid(id)
);

CREATE SEQUENCE acl_object_identity_id START WITH 1;

CREATE OR REPLACE TRIGGER acl_object_identity_id_trg 
BEFORE INSERT ON acl_object_identity 
FOR EACH ROW
BEGIN
  SELECT acl_object_identity_id.NEXTVAL
  INTO   :new.id
  FROM   dual;
  SELECT acl_object_identity_id.CURRVAL
  INTO   :new.object_id_identity
  FROM   dual;
END;
/

CREATE INDEX acl_object_identity_FK1IDX ON acl_object_identity(parent_object);
CREATE INDEX acl_object_identity_FK2IDX ON acl_object_identity(object_id_class);
CREATE INDEX acl_object_identity_FK3IDX ON acl_object_identity(owner_sid);

create table acl_entry(
    id number primary key,
    acl_object_identity number not null,
    ace_order number not null,
    sid number not null,
    mask number not null,
    granting NUMBER(1) not null check(granting IN (0,1)),
    audit_success NUMBER(1) not null check(audit_success IN (0,1)),
    audit_failure NUMBER(1) not null check(audit_failure IN (0,1)),
    constraint unique_uk_4 unique(acl_object_identity,ace_order),
    constraint foreign_fk_4 foreign key(acl_object_identity) references acl_object_identity(id),
    constraint foreign_fk_5 foreign key(sid) references acl_sid(id)
);

CREATE SEQUENCE acl_entry_id START WITH 1;

CREATE OR REPLACE TRIGGER acl_entry_id_trg 
BEFORE INSERT ON acl_entry 
FOR EACH ROW
BEGIN
  SELECT acl_entry_id.NEXTVAL
  INTO   :new.id
  FROM   dual;
END;
/

CREATE INDEX acl_entry_FK4IDX ON acl_entry(acl_object_identity);
CREATE INDEX acl_entry_FK5IDX ON acl_entry(sid);

create table acl_fullreadaccess_sid(
	sid number not null,
	object_id_class number not null,
	constraint unique_uk_5 unique(sid,object_id_class),
	constraint foreign_fk_6 foreign key(sid) references acl_sid(id),
	constraint foreign_fk_7 foreign key(object_id_class)references acl_class(id)
);

CREATE INDEX acl_fullreadaccess_sid_FK6IDX ON acl_fullreadaccess_sid(sid);
CREATE INDEX acl_fullreadaccess_sid_FK7IDX ON acl_fullreadaccess_sid(object_id_class);

CREATE OR REPLACE TRIGGER ACL_UPDATE_FULLREAD_SIDS_TRGR
AFTER INSERT ON acl_fullreadaccess_sid
FOR EACH ROW
 BEGIN
	 FOR currSid IN (select sid from seguridad.acl_fullreadaccess_sid where object_id_class = :NEW.object_id_class) LOOP
		 insert into seguridad.acl_entry(ace_order,acl_object_identity,audit_failure,audit_success,granting,mask,sid)
		 select
		  nvl(max(ace.ace_order)+1,0) as ace_order,
		  oi.id as acl_object_identity,
		  0 audit_failure,
		  0 audit_success,
		  1 granting,
		  1 as mask,
		  afras.sid
		 from
		 seguridad.acl_object_identity oi inner join
		 seguridad.acl_fullreadaccess_sid afras on(oi.object_id_class = afras.object_id_class) left join
		 seguridad.acl_entry ace on(oi.id=ace.acl_object_identity)
		 where
		 not exists (select 1 as existe from seguridad.acl_entry iace where iace.acl_object_identity = ace.acl_object_identity and iace.sid = afras.sid )
		 and afras.object_id_class = :NEW.object_id_class
		 and afras.sid=currSid.sid
		 group by
		 afras.sid,
		 oi.id;
	end LOOP currSid;
 END;
/

INSERT INTO USUARIO(ID_USUARIO,PASSWORD,ESTATUS) VALUES('admin@gunix.mx','$2y$16$rTB3.OnwSWmgAdrA0UwIIe8vBGzFRRQfjc7isEFZjYXcaZ6aLqKMe','ACTIVO'); --loloq123
INSERT INTO USUARIO(ID_USUARIO,PASSWORD,ESTATUS) VALUES('anonymous','$2y$16$.SdUkN4Cqv2xDmLLp18fk.eD0edzTgl8bLy5Q7atARCDZQoIv0.FO','ACTIVO'); --anonymous
INSERT INTO acl_sid(principal,sid) VALUES(1,'admin@gunix.mx');

/****************************************************/

CREATE TABLE APLICACION
(
	ID_APLICACION VARCHAR2(30) NOT NULL,
	ACL_ID number NOT NULL,
	DESCRIPCION VARCHAR2(100) DEFAULT 'APLICACION SIN DESCRIPCION' NOT NULL,
	ICONO VARCHAR(25) DEFAULT 'APLICACION_SIN_ICONO.png' NOT NULL 
);

ALTER TABLE APLICACION ADD CONSTRAINT "APLICACION_PK" PRIMARY KEY (ID_APLICACION);
ALTER TABLE APLICACION ADD CONSTRAINT "APLICACION_ACL_UQ" UNIQUE (ACL_ID);
ALTER TABLE APLICACION ADD CONSTRAINT "APLICACION_ACL_FK1" FOREIGN KEY (ACL_ID) REFERENCES acl_object_identity (object_id_identity) ;

CREATE TABLE USUARIO_APLICACION
(
	ID_USUARIO VARCHAR2(254) NOT NULL,
	ID_APLICACION VARCHAR2(30) NOT NULL
);
ALTER TABLE USUARIO_APLICACION ADD CONSTRAINT "USUARIO_APLICACION_PK" PRIMARY KEY (ID_USUARIO,ID_APLICACION);
ALTER TABLE USUARIO_APLICACION ADD CONSTRAINT "USUARIO_APLICACION_USUARIO_FK1" FOREIGN KEY (ID_USUARIO) REFERENCES USUARIO (ID_USUARIO);
ALTER TABLE USUARIO_APLICACION ADD CONSTRAINT "USUARIO_APLI_APLI_FK2" FOREIGN KEY (ID_APLICACION) REFERENCES APLICACION (ID_APLICACION);
CREATE INDEX USUARIO_APLICACION_FK1IDX ON USUARIO_APLICACION(ID_USUARIO);
CREATE INDEX USUARIO_APLICACION_FK2IDX ON USUARIO_APLICACION(ID_APLICACION);

CREATE TABLE ROL
(
    ID_APLICACION VARCHAR2(30) NOT NULL,
	ID_ROL VARCHAR2(30) NOT NULL,
	DESCRIPCION VARCHAR2(100) DEFAULT 'ROL SIN DESCRIPCION' NOT NULL,
	HABILITADO number(1) DEFAULT 1 NOT NULL check(HABILITADO IN (0,1))
);

ALTER TABLE ROL ADD CONSTRAINT "ROL_PK" PRIMARY KEY (ID_APLICACION,ID_ROL);
ALTER TABLE ROL ADD CONSTRAINT "ROL_APLICACION_FK1" FOREIGN KEY (ID_APLICACION) REFERENCES APLICACION (ID_APLICACION);

CREATE TABLE USUARIO_ROL
(
	ID_USUARIO VARCHAR2(254) NOT NULL,
	ID_APLICACION VARCHAR2(30) NOT NULL,
	ID_ROL VARCHAR2(30) NOT NULL
);
ALTER TABLE USUARIO_ROL ADD CONSTRAINT "USUARIO_ROL_PK" PRIMARY KEY (ID_USUARIO,ID_APLICACION,ID_ROL);
ALTER TABLE USUARIO_ROL ADD CONSTRAINT "USUARIO_ROL_USUARIO_FK1" FOREIGN KEY (ID_USUARIO) REFERENCES USUARIO (ID_USUARIO);
ALTER TABLE USUARIO_ROL ADD CONSTRAINT "USUARIO_ROL_ROL_FK2" FOREIGN KEY (ID_APLICACION,ID_ROL) REFERENCES ROL (ID_APLICACION,ID_ROL);
CREATE INDEX USUARIO_ROL_FK1IDX ON USUARIO_ROL(ID_USUARIO);
CREATE INDEX USUARIO_ROL_FK2IDX ON USUARIO_ROL(ID_APLICACION,ID_ROL);

create table DATOS_USUARIO (
ID_USUARIO varchar2(254) not null,
CURP varchar2(18) not null,
RFC  varchar2(13) not null,
AP_PATERNO varchar2(50) not null,
AP_MATERNO varchar2(50),
NOMBRE varchar2(100) not null,
CORREO_ELECTRONICO varchar2(50) not null,
TELEFONO varchar2(30),
CONSTRAINT curp UNIQUE(CURP),
CONSTRAINT rfc UNIQUE(RFC));

alter table DATOS_USUARIO     
add constraint DAT_FK_USUARIO foreign key (ID_USUARIO)
references USUARIO (ID_USUARIO); 

alter table DATOS_USUARIO     
add primary key(ID_USUARIO); 

CREATE TABLE MODULO
(
    ID_APLICACION VARCHAR2(30) NOT NULL,
	ID_MODULO VARCHAR2(30) NOT NULL,
	DESCRIPCION VARCHAR2(200) DEFAULT 'MODULO SIN DESCRIPCION' NOT NULL,
	ICONO VARCHAR2(25) DEFAULT 'MODULO_SIN_ICONO.png' NOT NULL 
);

ALTER TABLE MODULO ADD CONSTRAINT "MODULO_PK" PRIMARY KEY (ID_APLICACION,ID_MODULO);
ALTER TABLE MODULO ADD CONSTRAINT "MODULO_APLICACION_FK1" FOREIGN KEY (ID_APLICACION) REFERENCES APLICACION (ID_APLICACION);
CREATE INDEX MODULO_APLICACION_FK1IDX ON MODULO(ID_APLICACION);

CREATE TYPE HORARIO_OPERACION AS ENUM ('LD24','LV24','LV9_18','PERSONALIZADO');
CREATE TYPE VIEW_ENGINE AS ENUM ('VAADIN','SPRINGMVC');

CREATE TABLE FUNCION
(
    ID_APLICACION VARCHAR2(30) NOT NULL,
	ID_MODULO VARCHAR2(30) NOT NULL,
	ID_FUNCION VARCHAR2(30) NOT NULL,
	TITULO VARCHAR2(50) NOT NULL,
	DESCRIPCION VARCHAR2(100) NOT NULL,
	PROCESS_KEY VARCHAR2(255) DEFAULT ' ' NOT NULL,
	ORDEN NUMERIC(4,2) DEFAULT 0.0 NOT NULL,
	HORARIO varchar2(15) DEFAULT 'LD24' NOT NULL check (HORARIO in ('LD24','LV24','LV9_18','PERSONALIZADO')),
	VENGINE varchar2(10) check (VENGINE in ('VAADIN','SPRINGMVC')),
	ID_FUNCION_PADRE VARCHAR2(30)
);

ALTER TABLE FUNCION ADD CONSTRAINT "FUNCION_PK" PRIMARY KEY (ID_APLICACION,ID_MODULO,ID_FUNCION);
ALTER TABLE FUNCION ADD CONSTRAINT "MODULO_FUNCION_FK" FOREIGN KEY (ID_APLICACION,ID_MODULO) REFERENCES MODULO (ID_APLICACION,ID_MODULO);
ALTER TABLE FUNCION ADD CONSTRAINT "FUNCION_FUNCION_FK" FOREIGN KEY (ID_APLICACION,ID_MODULO,ID_FUNCION_PADRE) REFERENCES FUNCION (ID_APLICACION,ID_MODULO,ID_FUNCION);
CREATE INDEX MODULO_FUNCION_FKIDX ON FUNCION(ID_APLICACION,ID_MODULO);
CREATE INDEX FUNCION_FUNCION_FKIDX ON FUNCION(ID_APLICACION,ID_MODULO,ID_FUNCION_PADRE);

CREATE TABLE PARAM_FUNCION
(
    ID_APLICACION VARCHAR2(30) NOT NULL,
	ID_MODULO VARCHAR2(30) NOT NULL,
	ID_FUNCION VARCHAR2(30) NOT NULL,
	ID_PARAM VARCHAR2(15) NOT NULL,
	VALOR VARCHAR2(500) NOT NULL
);

ALTER TABLE PARAM_FUNCION ADD CONSTRAINT "PARAM_FUNCION_PK" PRIMARY KEY (ID_APLICACION,ID_MODULO,ID_FUNCION,ID_PARAM);
ALTER TABLE PARAM_FUNCION ADD CONSTRAINT "PARAM_FUNCION_FK" FOREIGN KEY (ID_APLICACION,ID_MODULO,ID_FUNCION) REFERENCES FUNCION (ID_APLICACION,ID_MODULO,ID_FUNCION);
CREATE INDEX PARAM_FUNCION_FKIDX ON PARAM_FUNCION(ID_APLICACION,ID_MODULO,ID_FUNCION);

CREATE TABLE ROL_FUNCION
(
    ID_APLICACION VARCHAR(30) NOT NULL,
	ID_ROL VARCHAR(30) NOT NULL,
	ID_MODULO VARCHAR(30) NOT NULL,
	ID_FUNCION VARCHAR(30) NOT NULL,
	NIV_ACC varchar2(10) DEFAULT 'PUNTUAL' NOT NULL check (NIV_ACC in ('COMPLETO','PUNTUAL'))
);

ALTER TABLE ROL_FUNCION ADD CONSTRAINT "ROL_FUNCION_PK" PRIMARY KEY (ID_APLICACION,ID_ROL,ID_MODULO,ID_FUNCION);
ALTER TABLE ROL_FUNCION ADD CONSTRAINT "ROL_FUNCION_FUNCION_FK1" FOREIGN KEY (ID_APLICACION,ID_MODULO,ID_FUNCION) REFERENCES FUNCION (ID_APLICACION,ID_MODULO,ID_FUNCION);
ALTER TABLE ROL_FUNCION ADD CONSTRAINT "ROL_FUNCION_ROL_FK2" FOREIGN KEY (ID_APLICACION,ID_ROL) REFERENCES ROL (ID_APLICACION,ID_ROL);
CREATE INDEX ROL_FUNCION_FK1IDX ON ROL_FUNCION(ID_APLICACION,ID_MODULO,ID_FUNCION);
CREATE INDEX ROL_FUNCION_FK2IDX ON ROL_FUNCION(ID_APLICACION,ID_ROL);

create table PERSISTENT_LOGINS 
(
        USERNAME VARCHAR2(254) not null,
        SERIES varchar2(64) primary key,
        TOKEN varchar2(64) not null,
        LAST_USED timestamp not null
);

INSERT INTO acl_class(class,descripcion,get_all_uri,id_aplicacion) values ('mx.com.gunix.framework.security.domain.Aplicacion','Aplicaciones alojadas en Gunix','http://localhost:8081/map-backEnd?servicio=aplicacionService','ADMIN_APP');
INSERT INTO acl_object_identity(object_id_class,owner_sid,entries_inheriting) values( acl_class_id.currval,(select id from acl_sid where sid = 'admin@gunix.mx'),1);

INSERT INTO APLICACION VALUES('ADMIN_APP',acl_object_identity_id.CURRVAL,'Gunix Admin App','Gunix.png');
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
	                        
        INSERT INTO ROL(ID_APLICACION,ID_ROL,DESCRIPCION) VALUES('ADMIN_APP','ADMINISTRADOR','Administrador');
	     	 INSERT INTO ROL_FUNCION VALUES('ADMIN_APP','ADMINISTRADOR','ADMINISTRACION','APLICACIONES','COMPLETO');
	     	 INSERT INTO ROL_FUNCION VALUES('ADMIN_APP','ADMINISTRADOR','ADMINISTRACION','USUARIOS','COMPLETO');

        INSERT INTO ROL(ID_APLICACION,ID_ROL,DESCRIPCION) VALUES('ADMIN_APP','ACL_ADMIN','Administrador de la Lista de Control de Acceso');

        INSERT INTO USUARIO_ROL VALUES('admin@gunix.mx','ADMIN_APP','ADMINISTRADOR');
        INSERT INTO USUARIO_ROL VALUES('admin@gunix.mx','ADMIN_APP','ACL_ADMIN');
        
        /* Funcionalidad Pública */
        INSERT INTO ROL(ID_APLICACION,ID_ROL,DESCRIPCION) VALUES('ADMIN_APP','PUBLIC','Público en General');
        INSERT INTO MODULO VALUES('ADMIN_APP','MODULO_PRUEBA','Demostraciones','1436568412_demo.png');
                INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,ORDEN) VALUES('ADMIN_APP','MODULO_PRUEBA','DEMOS','Demos','Menú con funcionalidad de demostración',1);
                	INSERT INTO FUNCION(ID_APLICACION, ID_MODULO,ID_FUNCION,TITULO,DESCRIPCION,PROCESS_KEY,ID_FUNCION_PADRE,ORDEN) VALUES('ADMIN_APP','MODULO_PRUEBA','ALTA_FORM','Formulario Alta','Demostración de Formularios','DemoForm','DEMOS',1);
                        INSERT INTO PARAM_FUNCION VALUES ('ADMIN_APP','MODULO_PRUEBA','ALTA_FORM','operación','Alta');

        INSERT INTO ROL_FUNCION VALUES('ADMIN_APP','PUBLIC','MODULO_PRUEBA','DEMOS','COMPLETO');

        INSERT INTO USUARIO_ROL VALUES('anonymous','ADMIN_APP','PUBLIC');

CREATE TYPE t_menu_usr_row AS OBJECT (
  id_usuario varchar2(254), 
  password varchar2(60), 
  eliminado varchar2(5), 
  bloqueado varchar2(5), 
  activo varchar2(5), 
  id_aplicacion varchar2(30), 
  descripcion_aplicacion varchar2(100), 
  icono_aplicacion varchar2(25), 
  id_rol varchar2(30), 
  descripcion_rol varchar2(100), 
  habilitado_rol number(1), 
  id_modulo varchar2(30), 
  descripcion_modulo varchar2(200), 
  icono_modulo varchar2(25), 
  id_funcion varchar2(30), 
  titulo varchar2(50), 
  descripcion_funcion varchar2(100), 
  process_key varchar2(255), 
  orden number, 
  horario varchar2(15), 
  vengine varchar2(10), 
  id_param varchar2(15), 
  valor varchar2(500), 
  id_funcion_padre varchar2(30), 
  titulo_padre varchar2(50), 
  descripcion_padre varchar2(100), 
  process_key_padre varchar2(255), 
  orden_padre number, 
  id_param_padre varchar2(15), 
  valor_padre varchar2(500)
);

CREATE TYPE t_menu_usr_tab AS TABLE OF t_menu_usr_row;
       
CREATE OR REPLACE FUNCTION menu_usuario(id_usr varchar2)
 RETURN t_menu_usr_tab PIPELINED  as
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
			FOR funcion IN(WITH 
							h(ID_APLICACION,ID_MODULO,ID_FUNCION,ORDEN,ACCESO_COMPLETO,ID_FUNCION_PADRE) as(
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
									SEGURIDAD.ROL_FUNCION RF ON (F.ID_APLICACION=RF.ID_APLICACION AND F.ID_MODULO = RF.ID_MODULO AND F.ID_FUNCION=RF.ID_FUNCION AND RF.ID_APLICACION='ADMIN_APP' AND RF.ID_ROL='ADMINISTRADOR')
							), 
							q(ID_APLICACION,ID_MODULO,ID_FUNCION,ORDEN,ACCESO_COMPLETO,ID_FUNCION_PADRE,"LEVEL",breadcrumb) AS(
								SELECT
									H.*,
									1 AS "LEVEL", 
									H.ID_APLICACION||'-'||H.ID_MODULO||'-'||H.ID_FUNCION||'-'||H.orden AS breadcrumb 
								FROM
									H
								where
									H.ID_FUNCION_PADRE is null        
								UNION all
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
									q."LEVEL" + 1 AS "LEVEL",
									Q.breadcrumb || (q.ID_APLICACION||'-'||q.ID_MODULO||'-'||q.ID_FUNCION||'-'||q.orden)
								FROM
									q
								JOIN
									h hi ON (hi.ID_APLICACION = q.ID_APLICACION AND hi.ID_MODULO = q.id_MODULO AND hi.ID_FUNCION_PADRE = q.id_FUNCION )
							)
							,
							R(ID_APLICACION,ID_MODULO,ID_FUNCION,ORDEN,ACCESO_COMPLETO,ID_FUNCION_PADRE) AS (
							SELECT
								q.ID_APLICACION,
								q.ID_MODULO,
								q.ID_FUNCION,
								q.ORDEN,
								q.ACCESO_COMPLETO,
								q.ID_FUNCION_PADRE
							FROM
								q
							)
							,
							Q2(ID_APLICACION,ID_MODULO,ID_FUNCION,ORDEN,ACCESO_COMPLETO,ID_FUNCION_PADRE,"LEVEL",breadcrumb) AS (
							SELECT
									R.*,
									1 AS "LEVEL", 
									R.ID_APLICACION||'-'||R.ID_MODULO||'-'||R.ID_FUNCION||'-'||R.orden AS breadcrumb
								FROM
									R
								where
									R.ACCESO_COMPLETO ='false'       
								UNION all
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
									q2."LEVEL" + 1 AS "LEVEL",
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
								q."LEVEL",
								Q.breadcrumb
							FROM 
								Q2 join Q ON(q2.ID_APLICACION=Q.ID_APLICACION AND q2.ID_MODULO=Q.ID_MODULO AND q2.ID_FUNCION=Q.ID_FUNCION)
							UNION all
							SELECT
								q.ID_APLICACION,
								q.ID_MODULO,
								q.ID_FUNCION,
								q.ORDEN,
								q.ACCESO_COMPLETO,
								q.ID_FUNCION_PADRE,
								q."LEVEL",
								Q.breadcrumb	
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
				PIPE ROW(t_menu_usr_row(usuario.ID_USUARIO,usuario.PASSWORD,usuario.ELIMINADO,usuario.bloqueado,usuario.activo,appRol.id_aplicacion,
											appRol.descripcion_aplicacion,appRol.icono_aplicacion,appRol.id_rol,appRol.descripcion_rol,appRol.habilitado_rol,
											funcion.id_modulo,funcion.descripcion_modulo,funcion.icono_modulo,funcion.id_funcion,funcion.titulo,
											funcion.descripcion_funcion,funcion.process_key,funcion.orden,funcion.horario,funcion.vengine,funcion.id_param,
											funcion.valor,funcion.id_funcion_padre,funcion.titulo_padre,funcion.descripcion_padre,funcion.process_key_padre,
											funcion.orden_padre,funcion.id_param_padre,funcion.valor_padre));  
			END LOOP funcion;
		END LOOP appRol;
	END LOOP usuario;
	RETURN;
END;
/

CREATE TABLE JOSSO_SESSION
(
    SESSION_ID                       VARCHAR2 (64)                   NOT NULL
  , USERNAME                         VARCHAR2 (128)                  NOT NULL
  , CREATION_TIME                    NUMBER                         NOT NULL
  , LAST_ACCESS_TIME                 NUMBER                       NOT NULL
  , ACCESS_COUNT                     NUMBER                         NOT NULL
  , MAX_INACTIVE_INTERVAL            NUMBER                         NOT NULL
  , VALID                            NUMBER(1)                         NOT NULL check(VALID IN (0,1))
);

ALTER TABLE JOSSO_SESSION
       ADD  PRIMARY KEY (SESSION_ID);

CREATE TABLE JOSSO_ASSERTION
(
    ASSERTION_ID                     VARCHAR2 (64)                   NOT NULL
  , SECURITY_DOMAIN_NAME             VARCHAR2 (64)                   NOT NULL
  , SSO_SESSION_ID                   VARCHAR2 (64)                   NOT NULL
  , CREATION_TIME                    NUMBER                           NOT NULL
  , VALID                            NUMBER(1)                        NOT NULL check(VALID IN (0,1))
);

ALTER TABLE JOSSO_ASSERTION
       ADD  PRIMARY KEY (ASSERTION_ID);
