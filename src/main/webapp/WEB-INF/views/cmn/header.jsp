<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  CC-CMN-01 공통 레이아웃 헤더 (정적 include 프래그먼트)
  사용법(각 화면 JSP 최상단):
    <c:set var="pageTitle" value="화면명 · 빚다"/>
    <c:set var="activeMenu" value="home|complete|working|hall"/>  (선택)
    <%@ include file="/WEB-INF/views/cmn/header.jsp" %>
      ...본문 콘텐츠...
    <%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
  로그인 상태 분기: sessionScope.loginMember(MemberVO), 관리자 판정 loginMember.isAdmin == 'Y'
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${empty pageTitle ? '빚다 · BITDA' : pageTitle}"/></title>
<link rel="stylesheet" href="${ctx}/resources/css/common.css">
<script src="${ctx}/resources/assets/js/jquery-4.0.0.js"></script>
<script src="${ctx}/resources/js/common.js"></script>
<script src="${ctx}/resources/js/comment.js"></script>
<script src="${ctx}/resources/js/upload.js"></script>
</head>
<body data-ctx="${ctx}">

<%-- ==================== 상단바 ==================== --%>
<header class="topbar">
	<a class="brand" href="${ctx}/main/index.do">
		<span class="brand-mark">빚</span>
		<span class="brand-text"><strong>빚다</strong><span>BITDA</span></span>
	</a>
	<div class="auth-actions">
		<c:choose>
			<c:when test="${empty sessionScope.loginMember}">
				<button type="button" class="btn ghost" id="btnOpenLogin">로그인</button>
				<button type="button" class="btn" id="btnOpenJoin">회원가입</button>
			</c:when>
			<c:otherwise>
				<span class="nick"><c:out value="${sessionScope.loginMember.nickname}"/></span>
				<c:if test="${sessionScope.loginMember.isAdmin eq 'Y'}">
					<a class="btn ghost" href="${ctx}/admin/dashboard.do">관리자페이지</a>
				</c:if>
				<a class="btn ghost" href="${ctx}/member/mypage.do">마이페이지</a>
				<a class="btn ghost" href="${ctx}/member/logout.do">로그아웃</a>
			</c:otherwise>
		</c:choose>
	</div>
</header>

<%-- ==================== 로그인 모달 (CC-USR-01) ==================== --%>
<c:if test="${empty sessionScope.loginMember}">
<div class="overlay" id="loginModal">
	<div class="modal">
		<div class="modal-head">
			<h3>로그인</h3>
			<button type="button" class="modal-close" aria-label="닫기">&times;</button>
		</div>
		<form id="loginForm" method="post">
			<div class="field">
				<label for="loginEmail">이메일</label>
				<input type="email" class="text-input" id="loginEmail" name="email" autocomplete="email" required>
			</div>
			<div class="field">
				<label for="loginPassword">비밀번호</label>
				<input type="password" class="text-input" id="loginPassword" name="password" autocomplete="current-password" required>
			</div>
			<p class="msg" id="loginMsg"></p>
			<button type="submit" class="btn block">로그인</button>
		</form>
		<p class="switch-line">계정이 없으신가요? <a id="linkToJoin">회원가입</a></p>
	</div>
</div>

<%-- ==================== 회원가입 모달 (CC-USR-02) ==================== --%>
<div class="overlay" id="joinModal">
	<div class="modal">
		<div class="modal-head">
			<h3>회원가입</h3>
			<button type="button" class="modal-close" aria-label="닫기">&times;</button>
		</div>
		<form id="joinForm" method="post">
			<div class="field">
				<label for="joinEmail">이메일</label>
				<div class="row">
					<input type="email" class="text-input" id="joinEmail" name="email" autocomplete="email" required>
					<button type="button" class="btn ghost small" id="btnJoinCheckEmail">중복확인</button>
				</div>
				<p class="msg" id="joinEmailMsg"></p>
			</div>
			<div class="field">
				<label for="joinPassword">비밀번호</label>
				<input type="password" class="text-input" id="joinPassword" name="password" minlength="8" autocomplete="new-password" required>
				<p class="hint">8자 이상</p>
			</div>
			<div class="field">
				<label for="joinConfirmPassword">비밀번호 확인</label>
				<input type="password" class="text-input" id="joinConfirmPassword" name="confirmPassword" minlength="8" autocomplete="new-password" required>
			</div>
			<div class="field">
				<label for="joinNickname">닉네임</label>
				<div class="row">
					<input type="text" class="text-input" id="joinNickname" name="nickname" maxlength="10" required>
					<button type="button" class="btn ghost small" id="btnJoinCheckNick">중복확인</button>
				</div>
				<p class="msg" id="joinNickMsg"></p>
				<p class="hint">10자 이내</p>
			</div>
			<div class="field">
				<label for="joinUserIntro">자기소개 <span style="font-weight:400;">(선택)</span></label>
				<textarea class="text-input" id="joinUserIntro" name="userIntro" rows="3" maxlength="100"></textarea>
				<p class="hint">100자 이내</p>
			</div>
			<button type="submit" class="btn block">가입하기</button>
		</form>
		<p class="switch-line">이미 회원이신가요? <a id="linkToLogin">로그인</a></p>
	</div>
</div>
</c:if>

<%-- ==================== 좌측 세로 네비 + 본문 열기 ==================== --%>
<div class="layout">
	<aside class="side">
		<nav class="side-nav">
			<a href="${ctx}/main/index.do" class="${activeMenu eq 'home' ? 'on' : ''}">홈</a>
			<a href="${ctx}/artwork/complete/list" class="${activeMenu eq 'complete' ? 'on' : ''}">완성품</a>
			<a href="${ctx}/artwork/working/list" class="${activeMenu eq 'working' ? 'on' : ''}">공개작품</a>
			<a href="${ctx}/main/hall.do" class="${activeMenu eq 'hall' ? 'on' : ''}">명예의전당</a>
		</nav>
	</aside>
	<main class="content">
		<div class="content-inner">
