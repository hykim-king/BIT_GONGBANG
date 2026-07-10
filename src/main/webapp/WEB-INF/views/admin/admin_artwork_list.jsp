<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  CC-ADM-02 관리자-게시물 관리 — 상태 탭(전체/완성/공개) + 제목/작성자 검색 + 페이징.
  수정: 기존 수정 폼 재사용(/artwork/complete/modify) / 삭제: ArtworkController.doDelete
  (관리자 허용 분기 — 4단계 반영) AJAX 호출 후 reload.
--%>
<c:set var="pageTitle" value="게시물 관리 · 빚다 관리자"/>
<c:set var="pageScript" value="admin/artwork_list"/>
<c:set var="adminActiveMenu" value="artwork"/>
<%@ include file="/WEB-INF/views/cmn/admin_header.jsp" %>

<div class="section-head"><h2>게시물 관리</h2><p>완성작과 공개작업 게시물을 함께 조회합니다.</p></div>

<form class="board-toolbar" method="get" action="${ctx}/admin/artwork_list.do">
	<span class="mypage-tabs" style="margin:0;">
		<a class="tab ${empty param.isStatus ? 'active' : ''}" href="${ctx}/admin/artwork_list.do">전체</a>
		<a class="tab ${param.isStatus eq 'Y' ? 'active' : ''}" href="${ctx}/admin/artwork_list.do?isStatus=Y">완성</a>
		<a class="tab ${param.isStatus eq 'N' ? 'active' : ''}" href="${ctx}/admin/artwork_list.do?isStatus=N">공개</a>
	</span>
	<input type="hidden" name="isStatus" value="${param.isStatus}">
	<input type="text" class="text-input search-word" name="searchWord" value="<c:out value='${param.searchWord}'/>" placeholder="제목 또는 작성자 검색">
	<button type="submit" class="btn ghost">검색</button>
</form>

<div class="table-scroll">
	<table>
		<thead><tr><th style="width:50px;">No</th><th>제목</th><th>작성자</th><th>카테고리</th><th style="width:70px;">상태</th><th style="width:70px;">좋아요</th><th style="width:60px;">조회</th><th>등록일</th><th style="width:130px;">관리</th></tr></thead>
		<tbody>
			<c:forEach var="a" items="${list}" varStatus="st">
			<tr data-artwork-id="${a.artworkId}" data-title="<c:out value='${a.title}'/>">
				<td>${(page.pageNo - 1) * page.pageSize + st.count}</td>
				<td><a href="${ctx}/artwork/${a.isStatus eq 'Y' ? 'complete' : 'working'}/view?artworkId=${a.artworkId}" style="font-weight:700;"><c:out value="${a.title}"/></a></td>
				<td><c:out value="${a.nickname}"/></td>
				<td><c:out value="${a.categoryNm}"/></td>
				<td><c:choose>
					<c:when test="${a.isStatus eq 'Y'}"><span class="pill complete">완성</span></c:when>
					<c:otherwise><span class="pill working">공개</span></c:otherwise>
				</c:choose></td>
				<td style="text-align:right;">${a.likeCount}</td>
				<td style="text-align:right;">${a.viewCount}</td>
				<td>${a.regDt}</td>
				<td><div style="display:flex;gap:6px;">
					<a class="btn ghost small" href="${ctx}/artwork/complete/modify?artworkId=${a.artworkId}">수정</a>
					<button type="button" class="btn danger small art-del">삭제</button>
				</div></td>
			</tr>
			</c:forEach>
			<c:if test="${empty list}">
			<tr><td colspan="9" class="empty-state">조건에 맞는 게시물이 없습니다.</td></tr>
			</c:if>
		</tbody>
	</table>
</div>

<c:if test="${page.totalPage > 1}">
<c:set var="tabQs" value="${pagingUrl}&isStatus=${param.isStatus}"/>
<div class="pager">
	<c:if test="${page.prev}"><a href="${tabQs}&pageNo=${page.startPage - 1}">&laquo;</a></c:if>
	<c:forEach var="p" begin="${page.startPage}" end="${page.endPage}">
		<c:choose>
			<c:when test="${p == page.pageNo}"><span class="cur">${p}</span></c:when>
			<c:otherwise><a href="${tabQs}&pageNo=${p}">${p}</a></c:otherwise>
		</c:choose>
	</c:forEach>
	<c:if test="${page.next}"><a href="${tabQs}&pageNo=${page.endPage + 1}">&raquo;</a></c:if>
</div>
</c:if>

<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
