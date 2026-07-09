<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>마이페이지 · 비트공방</title>
<style>
body { font-family: 'Malgun Gothic', sans-serif; max-width: 560px; margin: 40px auto; padding: 0 16px; }
h1 { font-size: 1.4rem; }
.profile { background: #f7f7f7; padding: 20px; border-radius: 8px; margin-top: 16px; }
.stats { display: flex; gap: 16px; margin-top: 16px; }
.stat { flex: 1; text-align: center; background: #fff; border: 1px solid #ddd; padding: 16px; border-radius: 8px; }
.stat strong { display: block; font-size: 1.5rem; color: #6DB33F; }
.actions { margin-top: 24px; display: flex; gap: 8px; flex-wrap: wrap; }
.actions a, .actions button { padding: 10px 16px; text-decoration: none; border: 1px solid #ccc; background: #fff; cursor: pointer; color: #333; }
.actions .primary { background: #6DB33F; color: #fff; border-color: #6DB33F; }
.intro { margin-top: 12px; color: #555; white-space: pre-wrap; }
</style>
</head>
<body>
<h1>마이페이지</h1>
<div class="profile">
	<p><strong>${member.nickname}</strong> (${member.email})</p>
	<p class="intro"><c:out value="${member.userIntro}" default="자기소개가 없습니다."/></p>
	<p style="font-size:0.85rem;color:#888;">가입일: ${member.regDt}</p>
</div>
<div class="stats">
	<div class="stat"><strong>${member.artworkCnt}</strong>등록 작품</div>
	<div class="stat"><strong>${member.likeCnt}</strong>좋아요</div>
</div>
<div class="actions">
	<a class="primary" href="${pageContext.request.contextPath}/member/modify.do">정보 수정</a>
	<a href="${pageContext.request.contextPath}/member/logout.do">로그아웃</a>
</div>
</body>
</html>
