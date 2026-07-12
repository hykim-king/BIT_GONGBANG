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

<%-- 순위 표식(메달/N위)은 상위 몇 위까지만 붙일지를 rankLimit 값 하나로 정한다.
     JSP(1페이지 서버렌더)와 feed.js(2페이지 이후)가 이 값을 각자 하드코딩하면
     나중에 하나만 고쳤을 때 어긋나므로, data-rank-limit 속성 하나로 공유한다. --%>
<c:set var="rankLimit" value="10"/>
<div class="section-head"><h2 class="hall-heading">명예의전당</h2></div>
<div class="masonry-feed" data-endpoint="/main/hallMore.do" data-rank="true" data-rank-limit="${rankLimit}">
	<div class="masonry">
		<c:forEach var="a" items="${list}" varStatus="st">
		<a class="art-card" href="${ctx}/artwork/complete/view?artworkId=${a.artworkId}">
			<%-- 순위 표식: 1~3위 메달 이미지, 4~rankLimit위 텍스트 뱃지, 그 뒤는 표식 없이 카드만. --%>
			<c:choose>
				<c:when test="${st.count le 3}">
					<img class="rank-medal" src="${ctx}/resources/assets/image/rank-${st.count}.png" alt="${st.count}위" width="34" height="34">
				</c:when>
				<c:when test="${st.count le rankLimit}">
					<span class="rank-badge">${st.count}위</span>
				</c:when>
			</c:choose>
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
		<div class="empty-state">등록된 완성 작품이 없습니다.</div>
	</c:if>
	<div class="feed-sentinel"></div>
</div>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
