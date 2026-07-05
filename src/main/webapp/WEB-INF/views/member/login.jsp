<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>로그인 · 비트공방</title>
<style>
body { font-family: 'Malgun Gothic', sans-serif; max-width: 420px; margin: 60px auto; padding: 0 16px; }
h1 { font-size: 1.4rem; margin-bottom: 24px; }
label { display: block; margin-top: 12px; font-size: 0.9rem; }
input { width: 100%; padding: 10px; margin-top: 4px; box-sizing: border-box; }
button { width: 100%; margin-top: 20px; padding: 12px; background: #6DB33F; color: #fff; border: none; cursor: pointer; }
.error { color: #c00; margin-top: 12px; font-size: 0.9rem; }
.links { margin-top: 16px; text-align: center; font-size: 0.9rem; }
.links a { color: #333; }
</style>
</head>
<body>
<h1>로그인</h1>
<c:if test="${not empty errorMsg}">
<p class="error">${errorMsg}</p>
</c:if>
<form action="${pageContext.request.contextPath}/member/doLogin.do" method="post">
	<label for="email">이메일</label>
	<input type="email" id="email" name="email" required maxlength="255">

	<label for="password">비밀번호</label>
	<input type="password" id="password" name="password" required minlength="8">

	<button type="submit">로그인</button>
</form>
<p class="links"><a href="${pageContext.request.contextPath}/member/join.do">회원가입</a></p>
</body>
</html>
