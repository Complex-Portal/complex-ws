<%--
  Created by IntelliJ IDEA.
  User: maitesin
  Date: 13/03/2014
  Time: 14:43
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Complex Web Service</title>
</head>
<body>
<h2>Export method (Experimental state)</h2>
<ul>
    <li>Get Json format to visualize CPX-3247: <a href="${pageContext.request.contextPath}/export/CPX-3247">/export/CPX-376</a></li>
    <li>Get miXML25 format to visualize CPX-3247: <a href="${pageContext.request.contextPath}/export/CPX-3247?format=xml25">/export/CPX-376?format=xml25</a></li>
    <li>Get miXML30 format to visualize CPX-3247: <a href="${pageContext.request.contextPath}/export/CPX-3247?format=xml30">/export/CPX-376?format=xml30</a></li>
</ul>
</body>
</html>
