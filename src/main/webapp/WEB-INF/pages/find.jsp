<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Complex Web Service</title>
</head>
<body>
<h2>Find method</h2>
<br>This method is to find complex matches from a list of UniProt ACs. Please find below different examples about that:
<ul>
    <li>Find complexes with proteins Q15554, P54274, Q96AP0 & Q9BSI:<br><a href="${pageContext.request.contextPath}/find?proteinAcs=Q15554,P54274,Q96AP0,Q9BSI4">/find?proteinAcs=Q15554,P54274,Q96AP0,Q9BSI4</a></li>
    <li>Find complexes with proteins Q9VS01, A4V2Z1 & Q7JZC9:<br><a href="${pageContext.request.contextPath}/find?proteinAcs=Q9VS01,A4V2Z1,Q7JZC9">/find?proteinAcs=Q9VS01,A4V2Z1,Q7JZC9</a></li>
</ul>
</body>
</html>
