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
<link rel="stylesheet" href="${ctx}/resources/assets/css/common.css">
<script src="${ctx}/resources/assets/js/jquery-4.0.0.js"></script>
<script src="${ctx}/resources/assets/js/cmn/common.js"></script>
</head>
<body data-ctx="${ctx}">

<header class="topbar">
	<a class="brand" href="${ctx}/admin/dashboard.do">
		<span class="brand-mark">빚</span>
		<span class="brand-text"><strong>빚다</strong><span>Admin</span></span>
	</a>
	<div class="auth-actions">
		<span class="nick"><c:out value="${sessionScope.loginMember.nickname}"/> <span class="pill admin">관리자</span></span>
		<a class="btn ghost" href="${ctx}/main/index.do">공개 사이트로</a>
		<a class="btn ghost" href="${ctx}/member/logout.do">로그아웃</a>
	</div>
</header>

<div class="layout">
	<aside class="side">
		<nav class="side-nav admin-side-nav">
			<a href="${ctx}/admin/dashboard.do" class="${adminActiveMenu eq 'dashboard' ? 'on' : ''}">대시보드</a>
			<a href="${ctx}/admin/member_list.do" class="${adminActiveMenu eq 'member' ? 'on' : ''}">회원 관리</a>
			<a href="${ctx}/admin/artwork_list.do" class="${adminActiveMenu eq 'artwork' ? 'on' : ''}">게시물 관리</a>
			<a href="${ctx}/admin/category.do" class="${adminActiveMenu eq 'category' ? 'on' : ''}">카테고리 관리</a>
		</nav>
	</aside>
	<main class="content">
		<div class="content-inner">
