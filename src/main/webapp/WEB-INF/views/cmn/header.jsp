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

<%-- 파비콘 --%>
<link rel="icon" href="${ctx}/resources/assets/image/favicon.ico" sizes="any">
<link rel="icon" type="image/png" sizes="32x32" href="${ctx}/resources/assets/image/favicon-32.png">
<link rel="icon" type="image/png" sizes="16x16" href="${ctx}/resources/assets/image/favicon-16.png">
<link rel="apple-touch-icon" href="${ctx}/resources/assets/image/apple-touch-icon.png">

<link rel="stylesheet" href="${ctx}/resources/assets/css/common.css">
<%-- defer: 문서 파싱을 막지 않고, 로드 순서대로 DOMContentLoaded 직전에 실행된다.
     common.js 가 window.bitda.esc / serializeForm 을 정의하므로 반드시 먼저 와야 한다. --%>
<script src="${ctx}/resources/assets/js/jquery-4.0.0.js" defer></script>
<script src="${ctx}/resources/assets/js/cmn/common.js" defer></script>
<script src="${ctx}/resources/assets/js/comment/comment.js" defer></script>
<script src="${ctx}/resources/assets/js/file/upload.js" defer></script>
<script src="${ctx}/resources/assets/js/main/feed.js" defer></script>
<script src="${ctx}/resources/assets/js/like/like.js" defer></script>
<script src="${ctx}/resources/assets/js/artwork/entry_timeline.js" defer></script>
<%-- 화면별 스크립트: 호스트 JSP가 header include 전에 <c:set var="pageScript" .../> 로 지정 --%>
<c:if test="${not empty pageScript}">
<script src="${ctx}/resources/assets/js/${pageScript}.js" defer></script>
</c:if>
</head>
<body data-ctx="${ctx}" data-login-member-id="${empty sessionScope.loginMember ? '' : sessionScope.loginMember.memberId}">

<%-- ==================== 상단바 ==================== --%>
<header class="topbar">
	<a class="brand" href="${ctx}/main/index.do">
		<img class="brand-mark" src="${ctx}/resources/assets/image/logo-mark.png" alt="빚다 로고">
		<span class="brand-text"><strong>빚다</strong><span>BITDA</span></span>
	</a>
	<div class="auth-actions">
		<c:choose>
			<%-- 명예의전당(CC-MAIN-02)은 로그인/회원가입 버튼 미노출: hideAuthButtons=true --%>
			<c:when test="${empty sessionScope.loginMember and hideAuthButtons eq 'true'}"></c:when>
			<c:when test="${empty sessionScope.loginMember}">
				<button type="button" class="btn ghost" id="btnOpenLogin">로그인</button>
				<button type="button" class="btn" id="btnOpenJoin">회원가입</button>
			</c:when>
			<c:otherwise>
				<span class="nick"><c:out value="${sessionScope.loginMember.nickname}"/></span>
				<c:if test="${sessionScope.loginMember.isAdmin eq 'Y'}">
					<a class="icon-btn" href="${ctx}/admin/dashboard.do" aria-label="관리자페이지" data-tip="관리자페이지">
						<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M12 4 5.5 6.4v5c0 4.3 2.8 7.3 6.5 8.6 3.7-1.3 6.5-4.3 6.5-8.6v-5Z"/><path d="M9.3 12l2 2 3.6-4.2"/></svg>
					</a>
				</c:if>
				<a class="icon-btn" href="${ctx}/member/mypage.do" aria-label="마이페이지" data-tip="마이페이지">
					<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8.3" r="3.3"/><path d="M5.5 19.5c0-3.6 2.9-6 6.5-6s6.5 2.4 6.5 6"/></svg>
				</a>
				<a class="icon-btn" href="${ctx}/member/logout.do" aria-label="로그아웃" data-tip="로그아웃">
					<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M14 4.5H8a1.5 1.5 0 0 0-1.5 1.5v12A1.5 1.5 0 0 0 8 19.5h6"/><path d="M11 12h9m0 0-3-3m3 3-3 3"/></svg>
				</a>
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
			<a href="${ctx}/main/index.do" class="${activeMenu eq 'home' ? 'on' : ''}">
				<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M4 11.5 12 4l8 7.5"/><path d="M6.5 10v9.5h11V10"/><path d="M10 19.5v-5h4v5"/></svg>
				<span class="lbl">홈</span>
			</a>
			<a href="${ctx}/artwork/complete/list" class="${activeMenu eq 'complete' ? 'on' : ''}">
				<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="9.5" r="5.2"/><path d="m8.7 14 -1.5 6.3 4.8-2.6 4.8 2.6-1.5-6.3"/></svg>
				<span class="lbl">완성품</span>
			</a>
			<a href="${ctx}/artwork/working/list" class="${activeMenu eq 'working' ? 'on' : ''}">
				<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M12 4.5c-3.6 1-6.5 1-6.5 1v7.2c0 4 2.9 6.3 6.5 7.3 3.6-1 6.5-3.3 6.5-7.3V5.5s-2.9 0-6.5-1Z"/><path d="M9.3 12.2l2 2 3.4-4"/></svg>
				<span class="lbl">공개작품</span>
			</a>
			<a href="${ctx}/main/hall.do" class="${activeMenu eq 'hall' ? 'on' : ''}">
				<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M7 4.5h10v3.8c0 3.3-2.1 5.7-5 6.1-2.9-.4-5-2.8-5-6.1V4.5Z"/><path d="M7 5.5H4.8c-.7 0-1.3.6-1.3 1.3 0 2 1.6 3.6 3.6 3.7"/><path d="M17 5.5h2.2c.7 0 1.3.6 1.3 1.3 0 2-1.6 3.6-3.6 3.7"/><path d="M12 14.4v2.6"/><path d="M9 19.5h6"/><path d="M10.3 17h3.4l.3 2.5h-4l.3-2.5Z"/></svg>
				<span class="lbl">명예의전당</span>
			</a>
		</nav>
	</aside>
	<main class="content">
		<div class="content-inner">
