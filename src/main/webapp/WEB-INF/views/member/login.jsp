<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="로그인 · 빚다"/>
<c:set var="pageScript" value="member/login"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>
<div class="page-narrow">
	<div class="section-head"><h2>로그인</h2><p>비로그인 접근 페이지에서 이동되었습니다. 로그인해 주세요.</p></div>
	<div class="panel" style="max-width:420px;">
		<p class="msg" id="pageLoginMsg" style="margin:0 0 12px;"></p>
		<form id="pageLoginForm" method="post">
			<div class="field">
				<label for="email">이메일</label>
				<input type="email" class="text-input" id="email" name="email" autocomplete="email" required maxlength="255">
			</div>
			<div class="field">
				<label for="password">비밀번호</label>
				<input type="password" class="text-input" id="password" name="password" autocomplete="current-password" required minlength="8">
			</div>
			<button type="submit" class="btn block">로그인</button>
		</form>
		<p class="switch-line">계정이 없으신가요? <a href="${ctx}/member/join.do">회원가입</a></p>
	</div>
</div>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
