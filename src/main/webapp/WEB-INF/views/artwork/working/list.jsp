<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  CC-WRK-01 공개작품 게시판(목록) — 하이브리드(is_status='N' OR 작업일지 존재).
  완성 게시판과 동일 레이아웃 + 진행상태 뱃지(N=진행중/Y=완성).
--%>
<c:set var="pageTitle" value="공개작품 · 빚다"/>
<c:set var="activeMenu" value="working"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<div class="section-head"><h2>공개작품</h2><p>작업 중인 과정을 기록하고 공유합니다. (완성 전환 후에도 작업일지가 있으면 계속 노출됩니다)</p></div>

<form class="board-toolbar" method="get" action="${ctx}/artwork/working/list">
	<select class="text-input" name="searchDiv">
		<option value="1" ${param.searchDiv eq '3' ? '' : 'selected'}>제목</option>
		<option value="3" ${param.searchDiv eq '3' ? 'selected' : ''}>작성자</option>
	</select>
	<input type="text" class="text-input search-word" name="searchWord" value="<c:out value='${param.searchWord}'/>" placeholder="공개작품 안에서 검색">
	<select class="text-input" name="categoryId" id="categoryFilter" data-selected="${param.categoryId}">
		<option value="0">카테고리 전체</option>
	</select>
	<button type="submit" class="btn ghost">검색</button>
	<c:if test="${not empty sessionScope.loginMember}">
		<a class="btn" href="${ctx}/artwork/working/reg">+ 새 글</a>
	</c:if>
</form>

<c:choose>
	<c:when test="${empty list}"><div class="empty-state">조건에 맞는 공개 작품이 없습니다.</div></c:when>
	<c:otherwise>
	<div class="board-list">
		<c:forEach var="a" items="${list}" varStatus="st">
		<a class="board-row" href="${ctx}/artwork/working/view?artworkId=${a.artworkId}">
			<span class="idx">${totalCnt - ((page.pageNo - 1) * page.pageSize) - st.index}</span>
			<span class="main-cell">
				<span class="t"><c:out value="${a.title}"/></span>
				<span class="sub">
					<span><c:out value="${a.nickname}"/></span>
					<span><c:out value="${a.categoryNm}"/></span>
					<c:choose>
						<c:when test="${a.isStatus eq 'Y'}"><span class="pill complete">완성</span></c:when>
						<c:otherwise><span class="pill working">진행중</span></c:otherwise>
					</c:choose>
					<span>&#10084; ${a.likeCount}</span>
					<span>조회 ${a.viewCount}</span>
				</span>
			</span>
			<c:choose>
				<c:when test="${a.repFileId > 0}"><img class="row-thumb" src="${ctx}/file/download.do?fileId=${a.repFileId}" alt=""></c:when>
				<c:otherwise><span class="row-thumb ph"><c:out value="${fn:substring(a.title, 0, 1)}"/></span></c:otherwise>
			</c:choose>
		</a>
		</c:forEach>
	</div>
	</c:otherwise>
</c:choose>

<c:if test="${page.totalPage > 1}">
<c:set var="baseQs" value="searchDiv=${param.searchDiv}&searchWord=${param.searchWord}&categoryId=${empty param.categoryId ? 0 : param.categoryId}&pageSize=${page.pageSize}"/>
<div class="pager">
	<c:if test="${page.prev}"><a href="?${baseQs}&pageNo=${page.startPage - 1}">&laquo;</a></c:if>
	<c:forEach var="p" begin="${page.startPage}" end="${page.endPage}">
		<c:choose>
			<c:when test="${p == page.pageNo}"><span class="cur">${p}</span></c:when>
			<c:otherwise><a href="?${baseQs}&pageNo=${p}">${p}</a></c:otherwise>
		</c:choose>
	</c:forEach>
	<c:if test="${page.next}"><a href="?${baseQs}&pageNo=${page.endPage + 1}">&raquo;</a></c:if>
</div>
</c:if>

<script>
$(function() {
	var ctx = $('body').data('ctx') || '';
	var $sel = $('#categoryFilter');
	var selected = String($sel.data('selected') || '0');
	$.get(ctx + '/category/doRetrieve.do', function(res) {
		if (res.code !== '200') { return; }
		(res.data || []).forEach(function(cItem) {
			var opt = $('<option>').val(cItem.categoryId).text(cItem.categoryNm);
			if (String(cItem.categoryId) === selected) { opt.prop('selected', true); }
			$sel.append(opt);
		});
	}, 'json');
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
