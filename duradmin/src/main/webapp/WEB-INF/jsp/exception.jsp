<%@include file="/WEB-INF/jsp/include.jsp" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="style/main.css" />
    <title>Error</title>
  </head>
  <body>
    <h3>An error prevented this page from loading correctly.</h3>

    <h5>Message</h5>
    <c:out value="${message}"/>

	<h5>Stack trace</h5>
	<c:out value="${stack}"/>

  </body>
</html>