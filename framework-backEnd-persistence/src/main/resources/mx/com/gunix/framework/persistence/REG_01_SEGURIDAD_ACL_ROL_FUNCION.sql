set search_path to SEGURIDAD;

drop function if exists MENU_USUARIO(text);
DROP TABLE if exists PERSISTENT_LOGINS cascade;
DROP TABLE if exists ROL_FUNCION  cascade;
DROP TYPE if exists NIVEL_ACCESO;
DROP TABLE if exists PARAM_FUNCION  cascade;
DROP TABLE if exists FUNCION cascade;
DROP TYPE if exists HORARIO_OPERACION;
DROP TABLE if exists MODULO cascade;
DROP TABLE if exists USUARIO_ROL cascade;
DROP TABLE if exists ROL cascade;
DROP TABLE if exists USUARIO_APLICACION cascade;
DROP TABLE if exists APLICACION cascade;

drop table if exists acl_entry cascade;
drop table if exists acl_object_identity cascade;
drop table if exists acl_class cascade;
drop table if exists acl_sid cascade;

DROP TABLE if exists USUARIO cascade;
DROP TYPE if exists ESTATUS_USUARIO cascade;

drop table if exists JOSSO_ASSERTION cascade;
drop table if exists JOSSO_SESSION cascade;

DROP EXTENSION if exists pgcrypto;

drop schema if exists SEGURIDAD;