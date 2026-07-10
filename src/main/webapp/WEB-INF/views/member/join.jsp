<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="회원가입 · 빚다"/>
<c:set var="pageScript" value="member/join"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>
<div class="page-narrow">
	<div class="section-head"><h2>회원가입</h2></div>
	<div class="panel" style="max-width:480px;">
	<%-- 주의: 헤더 모달의 #joinForm 과 충돌하지 않도록 페이지 폼은 #pageJoinForm 사용 --%>
	<form id="pageJoinForm" method="post">
		<div class="field">
			<label for="email">이메일</label>
			<div class="row">
				<input type="email" class="text-input" id="email" name="email" required maxlength="255">
				<button type="button" class="btn ghost small" id="btnCheckEmail">중복확인</button>
			</div>
			<p id="emailMsg" class="msg"></p>
		</div>
		<div class="field">
			<label for="password">비밀번호 (8자 이상)</label>
			<input type="password" class="text-input" id="password" name="password" required minlength="8">
		</div>
		<div class="field">
			<label for="confirmPassword">비밀번호 확인</label>
			<input type="password" class="text-input" id="confirmPassword" name="confirmPassword" required minlength="8">
		</div>
		<div class="field">
			<label for="nickname">닉네임 (10자 이내)</label>
			<div class="row">
				<input type="text" class="text-input" id="nickname" name="nickname" required maxlength="10">
				<button type="button" class="btn ghost small" id="btnCheckNick">중복확인</button>
			</div>
			<p id="nickMsg" class="msg"></p>
		</div>
		<div class="field">
			<label for="userIntro">자기소개 (100자 이내, 선택)</label>
			<textarea class="text-input" id="userIntro" name="userIntro" rows="3" maxlength="100"></textarea>
		</div>
		<button type="submit" class="btn block">가입하기</button>
	</form>
	<p class="switch-line"><a href="${ctx}/member/login.do">로그인으로</a></p>
	</div>
</div>

<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
