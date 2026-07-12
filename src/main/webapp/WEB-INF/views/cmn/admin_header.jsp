<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  관리자 공통 레이아웃 헤더 (정적 include 프래그먼트) — CC-ADM-01/02/03, CC-CAT-01 공용.
  사용법:
    <c:set var="pageTitle" value="화면명 · 빚다 관리자"/>
    <c:set var="adminActiveMenu" value="dashboard|member|artwork|category"/>
    <%@ include file="/WEB-INF/views/cmn/admin_header.jsp" %>
      ...본문...
    <%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
  접근은 AdminInterceptor(/admin/**)가 보장(loginMember.isAdmin=='Y').
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${empty pageTitle ? '빚다 관리자' : pageTitle}"/></title>

<%-- 파비콘 (관리자 화면은 링크 공유 대상이 아니라 og 태그는 두지 않는다) --%>
<link rel="icon" href="${ctx}/resources/assets/image/favicon.ico" sizes="any">
<link rel="icon" type="image/png" sizes="32x32" href="${ctx}/resources/assets/image/favicon-32.png">
<link rel="icon" type="image/png" sizes="16x16" href="${ctx}/resources/assets/image/favicon-16.png">
<link rel="apple-touch-icon" href="${ctx}/resources/assets/image/apple-touch-icon.png">

<link rel="stylesheet" href="${ctx}/resources/assets/css/common.css">
<%-- defer: 로드 순서대로 DOMContentLoaded 직전에 실행. common.js 가 먼저 와야 한다. --%>
<script src="${ctx}/resources/assets/js/jquery-4.0.0.js" defer></script>
<script src="${ctx}/resources/assets/js/cmn/common.js" defer></script>
<%-- 화면별 스크립트: 호스트 JSP가 header include 전에 <c:set var="pageScript" .../> 로 지정 --%>
<c:if test="${not empty pageScript}">
<script src="${ctx}/resources/assets/js/${pageScript}.js" defer></script>
</c:if>
</head>
<body data-ctx="${ctx}">

<header class="topbar">
	<a class="brand" href="${ctx}/admin/dashboard.do">
		<img class="brand-mark" src="${ctx}/resources/assets/image/logo-mark.png" alt="빚다 로고">
		<span class="brand-text"><strong>빚다</strong><span>Admin</span></span>
	</a>
	<div class="auth-actions">
		<span class="nick"><c:out value="${sessionScope.loginMember.nickname}"/> <span class="pill admin">관리자</span></span>
		<a class="btn ghost" href="${ctx}/main/index.do">공개 사이트로</a>
		<a class="icon-btn" href="${ctx}/member/logout.do" aria-label="로그아웃" data-tip="로그아웃">
			<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M14 4.5H8a1.5 1.5 0 0 0-1.5 1.5v12A1.5 1.5 0 0 0 8 19.5h6"/><path d="M11 12h9m0 0-3-3m3 3-3 3"/></svg>
		</a>
	</div>
</header>

<div class="layout">
	<aside class="side">
		<nav class="side-nav admin-side-nav">
			<a href="${ctx}/admin/dashboard.do" class="${adminActiveMenu eq 'dashboard' ? 'on' : ''}">
				<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect x="4.5" y="4.5" width="6.2" height="6.2" rx="1.4"/><rect x="13.3" y="4.5" width="6.2" height="6.2" rx="1.4"/><rect x="4.5" y="13.3" width="6.2" height="6.2" rx="1.4"/><rect x="13.3" y="13.3" width="6.2" height="6.2" rx="1.4"/></svg>
				<span class="lbl">대시보드</span>
			</a>
			<a href="${ctx}/admin/member_list.do" class="${adminActiveMenu eq 'member' ? 'on' : ''}">
				<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="8.5" r="2.7"/><circle cx="16" cy="9.3" r="2.1"/><path d="M4.3 19c0-2.9 2.1-4.9 4.7-4.9s4.7 2 4.7 4.9"/><path d="M14.5 15.2c1.9.3 3.2 1.9 3.2 3.8"/></svg>
				<span class="lbl">회원 관리</span>
			</a>
			<a href="${ctx}/admin/artwork_list.do" class="${adminActiveMenu eq 'artwork' ? 'on' : ''}">
				<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><rect x="5.5" y="4.5" width="13" height="15" rx="1.6"/><path d="M8.3 9h7.4M8.3 12.3h7.4M8.3 15.6h4.6"/></svg>
				<span class="lbl">게시물 관리</span>
			</a>
			<a href="${ctx}/admin/category.do" class="${adminActiveMenu eq 'category' ? 'on' : ''}">
				<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"><path d="M11.6 4.5H6.8a1.3 1.3 0 0 0-1.3 1.3v4.8c0 .4.2.8.4 1l7.5 7.5c.5.5 1.4.5 2 0l4-4c.5-.5.5-1.4 0-2l-7.5-7.5c-.3-.2-.6-.4-1-.4Z"/><circle cx="9.3" cy="8.7" r="1.1" fill="currentColor" stroke="none"/></svg>
				<span class="lbl">카테고리 관리</span>
			</a>
		</nav>
	</aside>
	<main class="content">
		<div class="content-inner">
