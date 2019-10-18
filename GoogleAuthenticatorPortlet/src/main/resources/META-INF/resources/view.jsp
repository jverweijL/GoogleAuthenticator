<%@ include file="/init.jsp" %>

<c:if test="${ErrorMessage != null}">
<div class="alert alert-danger" role="alert">
	<span class="alert-indicator">
		<svg class="lexicon-icon lexicon-icon-exclamation-full" focusable="false" role="presentation">
			<use href="/images/icons/icons.svg#exclamation-full"></use>
		</svg>
	</span>
	<strong class="lead">Error:</strong>${ErrorMessage}
</div>
	<img src="${ErrorImage}"/>

</c:if>

<c:if test="${QRCode != null}">
	<div class="row">
		<div class="col-xs-4 col-md-3"><img src="${QRCode}" style="border: 5px solid black;"/></div>
		<div class="col-xs-8 col-md-9">
			If you don't have the Google Authenticator App you can download it in the Google Play Store or Apple App Store.<br/>

			Open your Google Authenticator app and scan the code.<br/>
			Once you're done click the 'Ready' button.<br/><br/>

			<portlet:actionURL name="storekey" var="storekeyURL"/>

			<form action="${storekeyURL}" method="post">
				<button class="btn btn-primary" type="submit">Ready</button>
			</form></div>
	</div>
</c:if>

<c:if test="${QRCode == null && ErrorMessage == null}">
	<% if (attempts == maxtries-1) { %>
		<div class="alert alert-warning" role="alert"><strong>Warning: </strong>This will be your last attempt!</div>
	<% } %>

	<% if (attempts >= maxtries) { %>
		<div class="alert alert-danger" role="alert"><strong>Error: </strong>Your account is locked! Please contact the administrator.</div>
	<% } else { %>
		<portlet:actionURL name="validatekey" var="validatekeyURL"/>
		<form action="${validatekeyURL}" method="post">
		Enter your verificationcode: <input type="text" name="<portlet:namespace/>verificationcode"/>
		<button class="btn btn-primary" type="submit">Validate</button>
		</form>
	<% } %>
</c:if>