<%--
  ~ JOSSO: Java Open Single Sign-On
  ~
  ~ Copyright 2004-2009, Atricore, Inc.
  ~
  ~ This is free software; you can redistribute it and/or modify it
  ~ under the terms of the GNU Lesser General Public License as
  ~ published by the Free Software Foundation; either version 2.1 of
  ~ the License, or (at your option) any later version.
  ~
  ~ This software is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ Lesser General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with this software; if not, write to the Free
  ~ Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  ~ 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  ~
  --%>

<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ taglib prefix="tiles" uri="http://jakarta.apache.org/struts/tags-tiles"%>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html"%>
<%@ taglib prefix="bean" uri="http://jakarta.apache.org/struts/tags-bean"%>

<!DOCTYPE html>
<html>

<head>

<title><bean:message key="sso.title" /> - <tiles:getAsString name="josso.page.title" ignore="true" /></title>
<meta name="Title" content="Gunix" />
<meta name="Keywords" content="GUNIX, Single Sign On" />
<meta name="Description" content="GUNIX Java Open Single Sing-On." />

<meta name="Robots" content="index,follow" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<meta http-equiv="X-UA-Compatible" content="IE=11;chrome=1" />
<style type="text/css">
html, body {
	height: 100%;
	margin: 0;
}
</style>
<link rel="shortcut icon" type="image/vnd.microsoft.icon" href="<%=request.getContextPath()%>/resources/img/favicon.ico" />
<link rel="icon" type="image/vnd.microsoft.icon" href="<%=request.getContextPath()%>/resources/img/favicon.ico" />
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/resources/css/styles.css" />
</head>

<body scroll="auto" class="v-generated-body v-sa v-ch v-webkit v-win v-touch" style="text-align: center;">
	<div id="ROOT-2521314" class="v-app gunix mainui">
		<div tabindex="1" class="v-ui v-scrollable" style="width: 100%; height: 100%;">
			<div class="v-verticallayout v-layout v-vertical v-widget MainViewLayout v-verticallayout-MainViewLayout v-has-width v-margin-top v-margin-right v-margin-bottom v-margin-left"
				style="width: 792px; margin: 0 auto !important;">
				<div class="v-slot">
					<div class="v-customcomponent v-widget v-has-width" id="mx.com.gunix.framework.ui.vaadin.component.Header:ADMIN_APP" style="width: 100%;">
						<div class="v-verticallayout v-layout v-vertical v-widget v-has-width" style="width: 100%;">
							<div class="v-slot v-align-center v-align-middle">
								<div class="v-panel v-widget v-has-width"
									style="overflow: hidden; width: 100%; padding-top: 0px; padding-bottom: 0px; background: url('<%=request.getContextPath()%>/resources/img/rect4238-op.png'); background-repeat: no-repeat; background-position: right center;">
									<div class="v-panel-captionwrap" style="margin-top: 0px;">
										<div class="v-panel-nocaption">
											<span></span>
										</div>
									</div>
									<div class="v-panel-content v-scrollable" tabindex="-1" style="position: relative;">
										<div class="v-verticallayout v-layout v-vertical v-widget v-has-width v-margin-top v-margin-right v-margin-bottom v-margin-left"
											id="mx.com.gunix.ui.vaadin.view.adminapp.aplicacion.AplicacionView:75101439" style="width: 100%;">
											<div class="v-slot">
												<div class="v-widget v-has-caption v-caption-on-top v-has-width" style="width: 100%;">
													<tiles:insert attribute="josso.body" flush="false" />
												</div>
											</div>
										</div>
									</div>
									<div class="v-panel-deco" style="margin-bottom: 0px;"></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
