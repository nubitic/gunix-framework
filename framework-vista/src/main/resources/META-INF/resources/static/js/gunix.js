function getAjaxOptions(url,data){
	return {
			url: url,
			headers : {
				"Accept" : "text/html;type=ajax"
			},
			method: "POST",
			data:data
		};
	
}

function ajaxFail( jqxhr, settings, exception ) {
	console.log("Ajax call failed", exception);
}

function initUI(isMenuLoading){
	if(isMenuLoading){
		hideShow();	
	}
	
	$(".tablesorter").tablesorter();

	//When page loads...
	$(".tab_content").hide(); //Hide all content
	$("ul.tabs li:first").addClass("active").show(); //Activate first tab
	$(".tab_content:first").show(); //Show first tab content

	//On Click Event
	$("ul.tabs li").click(
			function() {
				$("ul.tabs li").removeClass("active"); //Remove any "active" class
				$(this).addClass("active"); //Add "active" class to selected tab
				$(".tab_content").hide(); //Hide all tab content
	
				var activeTab = $(this).find("a").attr("href"); //Find the href attribute value to identify the active tab + content
				$(activeTab).fadeIn(); //Fade in the active ID content
				return false;
				}
		);
	
	$('.column').equalHeight();
	} 

var isLoading=false;
var isLoadingProgramado=false;
var startTimer = null;

$(document).ready(initUI);

$(document).ajaxStart(function() {
		var startTime = Date.now();
		var duration = 0.8;
		startTimer = setInterval(function () {
		    var diff = (Date.now() - startTime) / 1000;
		    if (diff > duration) {
				if(!isLoading){
			    	$("body").showLoading();
	    			isLoading=true;
				}
		        clearInterval(startTimer);
		    }
		}, 100);
		isLoadingProgramado = true;
	});

$(document).ajaxComplete(function( event, xhr, settings ) {
		if(isLoadingProgramado){
			clearInterval(startTimer);
			isLoadingProgramado=false;
		}
		$("body").hideLoading();
		isLoading=false;
	});

function onAppChange(idAplicacion){
	jQuery.ajax(getAjaxOptions(showFragment+"roles&idAplicacion="+idAplicacion))
		.done(
			function(newContent) {
				$("#modulos").html("");
				$("#menu").html("");
				$("#modulos").html("");
				$("#content").html("");
				$("#breadcrumb").html("");
				$("#roles").html(newContent);
				initUI(false);
			})
		.fail(ajaxFail);
}

function onRolChange(idRol, idAplicacion){
	if(idRol==""){
		$("#menu").html("");
		$("#content").html("");
		$("#modulos").html("");
		$("#breadcrumb").html("");
		initUI(false);
	}else{
		jQuery.ajax(getAjaxOptions(showFragment+"modulos&idAplicacion="+idAplicacion+"&idRol="+idRol))
		.done(
			function(newContent) {
				$("#menu").html("");
				$("#content").html("");
				$("#breadcrumb").html("");
				$("#modulos").html(newContent);
				initUI(false);
			})
		.fail(ajaxFail);	
	}
}

function onModuloChange(idModulo, idRol, idAplicacion, descModulo){
	jQuery.ajax(getAjaxOptions(showFragment+"menu&idAplicacion="+idAplicacion+"&idRol="+idRol+"&idModulo="+idModulo))
	.done(
		function(newContent) {
			$("#modulos").html("");
			$("#content").html("");
			$("#breadcrumb").html("<article class='breadcrumbs'><a href='#' onclick='$(\"select[name=rol]\").trigger(\"change\");'><img src='static/images/home.gif'/></a> <div class='breadcrumb_divider'></div> <a>"+descModulo+"</a> </article>");
			$("#menu").html(newContent);
			initUI(true);
		})
	.fail(ajaxFail);
}

function onFuncionClick(idFuncion, idModulo, idRol, idAplicacion, funcion){
	jQuery.ajax(getAjaxOptions(showFragment+"content&idAplicacion="+idAplicacion+"&idRol="+idRol+"&idModulo="+idModulo+"&idFuncion="+idFuncion))
	.done(
		function(newContent) {
			$("#modulos").html("");
			updateBreadCrumb(funcion);
			$("#content").html(newContent);
			initUI(false);
		})
	.fail(ajaxFail);	
}

function onCompleteTask(idAplicacion){
	jQuery.ajax(getAjaxOptions(showFragment+"content&idAplicacion="+idAplicacion+"&isCompleteTask=true",
							   $("#gunixMainForm").serialize()))
	.done(
		function(newContent) {
			$("#content").html(newContent);
			initUI(false);
		})
	.fail(ajaxFail);	
}

function updateBreadCrumb(funcion){
	var funcionPadre = getFuncionPadre(funcion);
	if(funcionPadre != null){
		updateBreadCrumb(funcionPadre);
	}
	
	$("#breadcrumb").children().first().append("<div class='breadcrumb_divider'></div><a>"+funcion.contents().first().text()+"</a>");
}

function getFuncionPadre(funcion){
	var funcionPadre = null;
	if(funcion.is("a")){
		funcionPadre = $(funcion).parent().parent().prev();
	}else{
		if(funcion.is("h3")&&$(funcion).parent().is("ul")){
			funcionPadre = $(funcion).parent().prev();
		}
	}
	return funcionPadre;
}