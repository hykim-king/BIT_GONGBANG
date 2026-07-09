<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  CC-ADM-03 관리자-통계 대시보드
  총계 카드 5종 + 완성:공개 구성비 + 카테고리별 게시글 수 + 최근 가입 회원.
--%>
<c:set var="pageTitle" value="대시보드 · 빚다 관리자"/>
<c:set var="adminActiveMenu" value="dashboard"/>
<%@ include file="/WEB-INF/views/cmn/admin_header.jsp" %>
<style>
.stat-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; margin-bottom: 24px; }
@media (max-width: 860px) { .stat-grid { grid-template-columns: repeat(2, 1fr); } }
.stat-card { background: var(--bg-elevated); border: 1px solid var(--border); border-radius: var(--radius); padding: 16px; box-shadow: 0 1px 2px var(--shadow); }
.stat-card .label { font-size: 11px; color: var(--ink-faint); text-transform: uppercase; letter-spacing: .06em; font-weight: 700; }
.stat-card .value { font-size: 26px; font-weight: 800; margin-top: 8px; }
.split-bar { display: flex; height: 12px; border-radius: 999px; overflow: hidden; background: var(--border); }
.split-bar .seg-y { background: var(--accent); }
.split-bar .seg-n { background: var(--gold); }
.split-legend { display: flex; gap: 18px; margin-top: 10px; font-size: 12px; color: var(--ink-soft); }
.split-legend .dot { width: 8px; height: 8px; border-radius: 999px; display: inline-block; margin-right: 6px; }
.cat-stat-row { display: grid; grid-template-columns: 90px 1fr 40px; align-items: center; gap: 12px; padding: 7px 0; }
.cat-stat-row .cs-name { font-size: 12.5px; font-weight: 700; }
.cat-stat-row .cs-bar { height: 9px; border-radius: 999px; background: var(--border); overflow: hidden; }
.cat-stat-row .cs-bar span { display: block; height: 100%; background: var(--accent); border-radius: 999px; }
.cat-stat-row .cs-cnt { font-size: 12px; color: var(--ink-faint); text-align: right; }
</style>

<div class="section-head"><h2>대시보드</h2><p>전체 현황을 한눈에 확인합니다.</p></div>

<div class="stat-grid">
	<div class="stat-card"><div class="label">회원</div><div class="value">${stats.member}</div></div>
	<div class="stat-card"><div class="label">작품</div><div class="value">${stats.artwork}</div></div>
	<div class="stat-card"><div class="label">댓글</div><div class="value">${stats.comment}</div></div>
	<div class="stat-card"><div class="label">좋아요</div><div class="value">${stats.like}</div></div>
	<div class="stat-card"><div class="label">카테고리</div><div class="value">${stats.category}</div></div>
</div>

<%-- 완성:공개 구성비 --%>
<c:set var="cntY" value="0"/><c:set var="cntN" value="0"/>
<c:forEach var="r" items="${statusRatio}">
	<c:if test="${r.isStatus eq 'Y'}"><c:set var="cntY" value="${r.cnt}"/></c:if>
	<c:if test="${r.isStatus eq 'N'}"><c:set var="cntN" value="${r.cnt}"/></c:if>
</c:forEach>
<c:set var="cntSum" value="${cntY + cntN}"/>
<div class="panel">
	<h3 style="font-size:13.5px;font-weight:700;margin:0 0 14px;">작품 구성비 — 완성 ${cntY} : 공개작업 ${cntN} (총 ${cntSum})</h3>
	<c:if test="${cntSum > 0}">
	<div class="split-bar">
		<div class="seg-y" style="width:${cntY * 100 / cntSum}%"></div>
		<div class="seg-n" style="width:${cntN * 100 / cntSum}%"></div>
	</div>
	<div class="split-legend">
		<span><span class="dot" style="background:var(--accent)"></span>완성</span>
		<span><span class="dot" style="background:var(--gold)"></span>공개작업</span>
	</div>
	</c:if>
	<c:if test="${cntSum == 0}"><p class="hint">등록된 작품이 없습니다.</p></c:if>
</div>

<%-- 카테고리별 게시글 수 --%>
<div class="panel">
	<h3 style="font-size:13.5px;font-weight:700;margin:0 0 14px;">카테고리별 게시글 수</h3>
	<c:set var="maxCnt" value="1"/>
	<c:forEach var="cs" items="${categoryStats}">
		<c:if test="${cs.cnt > maxCnt}"><c:set var="maxCnt" value="${cs.cnt}"/></c:if>
	</c:forEach>
	<c:forEach var="cs" items="${categoryStats}">
	<div class="cat-stat-row">
		<span class="cs-name"><c:out value="${cs.categoryNm}"/></span>
		<div class="cs-bar"><span style="width:${cs.cnt * 100 / maxCnt}%"></span></div>
		<span class="cs-cnt">${cs.cnt}</span>
	</div>
	</c:forEach>
	<c:if test="${empty categoryStats}"><p class="hint">카테고리가 없습니다.</p></c:if>
</div>

<%-- 최근 가입 회원 --%>
<div class="panel">
	<h3 style="font-size:13.5px;font-weight:700;margin:0 0 14px;">최근 가입 회원</h3>
	<div class="table-scroll">
		<table>
			<thead><tr><th>닉네임</th><th>이메일</th><th>가입일</th><th>권한</th></tr></thead>
			<tbody>
				<c:forEach var="mVo" items="${recentMembers}">
				<tr>
					<td><c:out value="${mVo.nickname}"/></td>
					<td><c:out value="${mVo.email}"/></td>
					<td>${mVo.regDt}</td>
					<td><c:choose>
						<c:when test="${mVo.isAdmin eq 'Y'}"><span class="pill admin">관리자</span></c:when>
						<c:otherwise><span class="pill working">일반</span></c:otherwise>
					</c:choose></td>
				</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
