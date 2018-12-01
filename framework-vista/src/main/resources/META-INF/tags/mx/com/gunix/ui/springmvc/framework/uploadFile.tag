<%@ attribute name="to" required="true" type="java.lang.String" description="¿A qué método hay que enviarle el archivo?"%>
<%@ attribute name="accept" required="false" type="java.lang.String" description="¿Qué tipo de archivos pueden seleccionarse? HTML5"%>
<%@ attribute name="id" required="false" type="java.lang.String"%>
<%@ attribute name="value" required="false" type="java.lang.String"%>
<%@ attribute name="onUploadDone" required="false" type="java.lang.String" description="Función a ejecutar cuando termine la carga"%>
<%@ attribute name="beforeUpload" required="false" type="java.lang.String" description="Función a ejecutar previo a iniciar la carga"%>
<%@ attribute name="responseType" required="false" type="java.lang.String" description="Tipo de respuesta esperada"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="gunix" uri="/framework/tags"%>
<%@ tag dynamic-attributes="dynAttrs" %>
<c:set var="randUploadId">upload<%= java.lang.Math.round(java.lang.Math.random() * 100) %></c:set>
<input type="file" data-url="uploadFile" id="${not empty id?id:randUploadId}" ${not empty accept?'accept="'.concat(accept).concat('"'):''} ${not empty value?'value="'.concat(value).concat('"'):''} <gunix:mapToHtmlAttrString map="${dynAttrs}"/>  />
<div id="${not empty id?id:randUploadId}Progress" style="width: 35%;">
	<div class="bar" style="width: 0%; height: 10px; border-radius: 3px; border-right-width: 0; border-left-width: 0; border-top-width: 0; border-bottom-width: 0; background-color: #197de1; background-image: -webkit-linear-gradient(top, #1b87e3 2%, #166ed5 98%); background-image: linear-gradient(to bottom, #1b87e3 2%, #166ed5 98%);"></div>
</div>
<script type="text/javascript">
$(function () {
	$('#${not empty id?id:randUploadId}').fileupload({
		${not empty responseType?'dataType:"'.concat(responseType).concat('",'):''}
		done: function(e, data) {
			${not empty onUploadDone?onUploadDone.concat('(e,data);'):''}
			$('#${not empty id?id:randUploadId}Progress .bar').css('width','0%');
		},
		progressall: function (e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10);
			$('#${not empty id?id:randUploadId}Progress .bar').css('width',progress + '%');
		}
	}).on('fileuploadsubmit',function (e, data) {
			${not empty beforeUpload?'if ('.concat(beforeUpload).concat('(e,data) == false) return false;'):''}
			var fTo = '${to}';
			if (fTo.indexOf(cCurrentGunixViewPath) < 0) {
				if (fTo.charAt(0) == '/') {
					fTo = cCurrentGunixViewPath + fTo;
				} else {
					fTo = cCurrentGunixViewPath + fTo;
				}
			}
			data.formData = $.extend(data.formData, {forwardFileTo: fTo});
			data.paramName = 'gunixFile';
		});
});
</script>