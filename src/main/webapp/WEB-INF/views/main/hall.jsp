<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  CC-MAIN-02 명예의전당 — 가중치(like_count*3 + view_count) 누적(기간 제한 없음) 매스너리.
  순위 뱃지 표시. 로그인/회원가입 버튼 미노출(hideAuthButtons). 무한스크롤 POST /main/hallMore.do.
--%>
<c:set var="pageTitle" value="명예의전당 · 빚다"/>
<c:set var="activeMenu" value="hall"/>
<c:set var="hideAuthButtons" value="true"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<div class="section-head"><h2>명예의전당</h2><p>기간 제한 없이 누적 인기순으로 정렬됩니다.</p></div>
<div class="masonry-feed" data-endpoint="/main/hallMore.do" data-rank="true">
	<div class="masonry">
		<c:forEach var="a" items="${list}" varStatus="st">
		<a class="art-card" href="${ctx}/artwork/complete/view?artworkId=${a.artworkId}">
			<span class="rank-badge">${st.count}위</span>
			<c:choose>
				<c:when test="${a.repFileId > 0}">
					<img class="thumb" src="${ctx}/file/download.do?fileId=${a.repFileId}" alt="<c:out value='${a.title}'/>" loading="lazy">
				</c:when>
				<c:otherwise>
					<div class="thumb ph"><c:out value="${fn:substring(a.title, 0, 1)}"/></div>
				</c:otherwise>
			</c:choose>
			<div class="meta">
				<div class="t"><c:out value="${a.title}"/></div>
				<div class="row">
					<span><c:out value="${a.nickname}"/> · <c:out value="${a.categoryNm}"/></span>
					<span class="heart">&#10084; ${a.likeCount}</span>
				</div>
			</div>
		</a>
		</c:forEach>
	</div>
	<c:if test="${empty list}">
		<div class="empty-state">등록된 완성 작품이 없습니다.</div>
	</c:if>
	<div class="feed-sentinel"></div>
</div>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
