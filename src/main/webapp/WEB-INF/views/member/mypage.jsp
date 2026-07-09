<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="마이페이지 · 빚다"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>
<style>
.profile { background: var(--bg-elevated); border: 1px solid var(--border); padding: 20px; border-radius: var(--radius); margin-top: 16px; }
.stats { display: flex; gap: 16px; margin-top: 16px; max-width: 560px; }
.stat { flex: 1; text-align: center; background: var(--bg-elevated); border: 1px solid var(--border); padding: 16px; border-radius: var(--radius); }
.stat strong { display: block; font-size: 1.5rem; color: var(--accent); }
.actions { margin-top: 24px; display: flex; gap: 8px; flex-wrap: wrap; }
.intro { margin-top: 12px; color: var(--ink-soft); white-space: pre-wrap; }
</style>
<div class="page-narrow">
	<div class="section-head"><h2>마이페이지</h2></div>
	<div class="profile">
		<p><strong><c:out value="${member.nickname}"/></strong> (<c:out value="${member.email}"/>)</p>
		<p class="intro"><c:out value="${member.userIntro}" default="자기소개가 없습니다."/></p>
		<p style="font-size:0.85rem;color:var(--ink-faint);">가입일: ${member.regDt}</p>
	</div>
	<div class="stats">
		<div class="stat"><strong>${member.artworkCnt}</strong>등록 작품</div>
		<div class="stat"><strong>${member.likeCnt}</strong>좋아요</div>
	</div>
	<div class="actions">
		<a class="btn" href="${ctx}/member/modify.do">정보 수정</a>
		<a class="btn ghost" href="${ctx}/member/logout.do">로그아웃</a>
	</div>
</div>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
