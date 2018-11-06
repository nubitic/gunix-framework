alter session set current_schema = ADMON_SEG;


CREATE TYPE t_rol_usr_row AS OBJECT (
  ID_APLICACION varchar2(30),
  ID_MODULO varchar2(30),
  ID_FUNCION varchar2(30),
  ID_FUNCION_PADRE varchar2(30)
);

CREATE TYPE t_rol_usr_tab AS TABLE OF t_rol_usr_row;
	

CREATE OR REPLACE FUNCTION FUNCIONES_ROL(id_aplicacion_par varchar2, id_rol_par varchar2)
 RETURN t_rol_usr_tab PIPELINED  as
BEGIN
	FOR rol_usr in(WITH 
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
							SEGURIDAD.ROL_FUNCION RF ON (F.ID_APLICACION=RF.ID_APLICACION AND F.ID_MODULO = RF.ID_MODULO AND F.ID_FUNCION=RF.ID_FUNCION AND RF.ID_APLICACION=id_aplicacion_par AND RF.ID_ROL=id_rol_par)
					)
					, 
					q(ID_APLICACION,ID_MODULO,ID_FUNCION,ORDEN,ACCESO_COMPLETO,ID_FUNCION_PADRE, "LEVEL",breadcrumb) AS(
						SELECT
							H.*,
							1 AS "LEVEL", 
							ID_APLICACION||'-'||ID_MODULO||'-'||ID_FUNCION||'-'||orden AS breadcrumb 
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
							ID_APLICACION||'-'||ID_MODULO||'-'||ID_FUNCION||'-'||orden AS breadcrumb
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
							q2."LEVEL" + 1 AS "LEVEL",
							Q2.breadcrumb || (q2.ID_APLICACION||'-'||q2.ID_MODULO||'-'||q2.ID_FUNCION||'-'||q2.orden)
						FROM
							q2
						JOIN
							R hi ON (hi.ID_APLICACION = q2.ID_APLICACION AND hi.ID_MODULO = q2.id_MODULO AND hi.ID_FUNCION = q2.id_FUNCION_PADRE )
					)
					, FUNCIONES_ROL(ID_APLICACION,ID_MODULO,ID_FUNCION,ORDEN,ACCESO_COMPLETO,ID_FUNCION_PADRE,"LEVEL",breadcrumb) AS (
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
					UNION ALL
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
						FUNCIONES_ROL.ID_APLICACION,
					                        FUNCIONES_ROL.ID_MODULO,
					                                FUNCIONES_ROL.ID_FUNCION,
					                                FUNCIONES_ROL.ID_FUNCION_PADRE
					FROM
						FUNCIONES_ROL 
					ORDER BY
					        FUNCIONES_ROL.breadcrumb,
					        FUNCIONES_ROL.orden) LOOP
				PIPE ROW(t_rol_usr_row(rol_usr.ID_APLICACION,rol_usr.ID_MODULO,rol_usr.ID_FUNCION,rol_usr.ID_FUNCION_PADRE));
			END LOOP rol_usr;
RETURN;
END;
/