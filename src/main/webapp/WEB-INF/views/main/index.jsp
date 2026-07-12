<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  CC-MAIN-01 메인 홈 — 핀터레스트형 매스너리 + 무한스크롤.
  정렬: 가중치(like_count*3 + view_count) + 최근 30일. 1페이지는 서버 렌더,
  2페이지부터 feed.js가 POST /main/popular.do 로 append.
--%>
<c:set var="pageTitle" value="빚다 · BITDA"/>
<c:set var="activeMenu" value="home"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<div class="masonry-feed" data-endpoint="/main/popular.do" data-rank="false">
	<div class="masonry">
		<c:forEach var="a" items="${list}">
		<a class="art-card" href="${ctx}/artwork/complete/view?artworkId=${a.artworkId}">
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
					<span class="who"><c:out value="${a.nickname}"/> · <c:out value="${a.categoryNm}"/></span>
					<span class="stats">
						<span class="view">조회 ${a.viewCount}</span>
						<span class="heart">&#10084; ${a.likeCount}</span>
					</span>
				</div>
			</div>
		</a>
		</c:forEach>
	</div>
	<c:if test="${empty list}">
		<div class="empty-state">최근 30일 동안 등록된 완성 작품이 없습니다.</div>
	</c:if>
	<div class="feed-sentinel"></div>
</div>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
