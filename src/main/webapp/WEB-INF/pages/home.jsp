<%--
  Created by IntelliJ IDEA.
  User: maitesin
  Date: 13/03/2014
  Time: 14:31
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Complex Web Service</title>
</head>
<body>
<h2>Welcome to the complex web service</h2>
Methods available:
<ul>
    <li><a href="${pageContext.request.contextPath}/search/">/search/</a></li>
    <li><a href="${pageContext.request.contextPath}/details/">/details/</a></li>
    <li><a href="${pageContext.request.contextPath}/complex/">/complex/</a></li>
    <li><a href="${pageContext.request.contextPath}/export/">/export/</a></li>
    <li><a href="${pageContext.request.contextPath}/find/">/find//</a></li>
</ul>
</body>
</html>
