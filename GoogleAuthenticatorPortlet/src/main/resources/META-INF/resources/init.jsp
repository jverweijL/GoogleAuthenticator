<%@ page import="com.liferay.demo.googleauthenticator.configuration.GoogleAuthenticatorPortletConfiguration" %>
<%@ page import="com.liferay.portal.kernel.util.StringPool" %>
<%@ page import="com.liferay.portal.kernel.util.Validator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<liferay-theme:defineObjects />

<portlet:defineObjects />

<%
    GoogleAuthenticatorPortletConfiguration googleAuthenticatorPortletConfiguration =
            (GoogleAuthenticatorPortletConfiguration)
                    renderRequest.getAttribute(GoogleAuthenticatorPortletConfiguration.class.getName());

    String issuer = StringPool.BLANK;
    Integer maxtries = 1;
    String starturl = StringPool.BLANK;

    if (Validator.isNotNull(googleAuthenticatorPortletConfiguration)) {
        issuer = portletPreferences.getValue("issuer", googleAuthenticatorPortletConfiguration.issuer());
        maxtries = Integer.parseInt(portletPreferences.getValue("maxtries", googleAuthenticatorPortletConfiguration.maxtries()));
        starturl = portletPreferences.getValue("starturl", googleAuthenticatorPortletConfiguration.starturl());
    }

    Integer attempts = Integer.valueOf(themeDisplay.getUser().getExpandoBridge().getAttribute("GoogleAuthenticatorTokenTries",false).toString());
%>