/*declare @owner_user;
SET @owner_user = 'exsipqluzgntfw';*/

CREATE SCHEMA IF NOT EXISTS ADMON_SEG /*AUTHORIZATION @owner_user*/;
set search_path to ADMON_SEG;

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
