DROP function if exists FUNCIONES_ROL(text, text);
DROP TABLE if exists CLIENTE;

drop function if exists MENU_USUARIO(text);
DROP TABLE if exists PERSISTENT_LOGINS cascade;
DROP TABLE if exists ROL_FUNCION  cascade;
DROP TYPE if exists NIVEL_ACCESO;
DROP TABLE if exists PARAM_FUNCION  cascade;
DROP TABLE if exists FUNCION cascade;
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

drop table if exists ACT_ID_INFO cascade;
drop table if exists ACT_ID_GROUP cascade;
drop table if exists ACT_ID_MEMBERSHIP cascade;
drop table if exists ACT_ID_USER cascade;

drop table if exists ACT_HI_PROCINST cascade;
drop table if exists ACT_HI_ACTINST cascade;
drop table if exists ACT_HI_VARINST cascade;
drop table if exists ACT_HI_TASKINST cascade;
drop table if exists ACT_HI_DETAIL cascade;
drop table if exists ACT_HI_COMMENT cascade;
drop table if exists ACT_HI_ATTACHMENT cascade;
drop table if exists ACT_HI_IDENTITYLINK cascade;

drop table if exists ACT_GE_PROPERTY cascade;
drop table if exists ACT_GE_BYTEARRAY cascade;
drop table if exists ACT_RE_DEPLOYMENT cascade;
drop table if exists ACT_RE_MODEL cascade;
drop table if exists ACT_RE_PROCDEF cascade;
drop table if exists ACT_RU_EXECUTION cascade;
drop table if exists ACT_RU_JOB cascade;
drop table if exists ACT_RU_TASK cascade;
drop table if exists ACT_RU_IDENTITYLINK cascade;
drop table if exists ACT_RU_VARIABLE cascade;
drop table if exists ACT_RU_EVENT_SUBSCR cascade;
drop table if exists ACT_EVT_LOG cascade;
