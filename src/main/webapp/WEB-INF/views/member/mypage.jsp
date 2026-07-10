<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  CC-USR-03 마이페이지 — 프로필(이니셜 아바타) + 3탭(공개/완성/관심) 고정비율 그리드.
  프로필 사진: TargetType.MEMBER 다형성은 attach_file CHECK 제약(ARTWORK/ARTWORK_ENTRY만)과
  충돌하여 DDL 변경(팀 합의) 전까지 닉네임 이니셜 아바타로 대체.
--%>
<c:set var="pageTitle" value="마이페이지 · 빚다"/>
<c:set var="pageScript" value="member/mypage"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<div class="mypage-head">
	<div class="mypage-avatar"><c:out value="${fn:substring(member.nickname, 0, 1)}"/></div>
	<div class="nick"><c:out value="${member.nickname}"/> <span style="font-weight:400;color:var(--ink-faint);font-size:12px;">(<c:out value="${member.email}"/>)</span></div>
	<p class="bio"><c:out value="${member.userIntro}" default="자기소개가 없습니다."/></p>
	<div class="mypage-stats">
		<span><strong>${member.artworkCnt}</strong>작품</span>
		<span><strong>${member.likeCnt}</strong>좋아요</span>
	</div>
	<div style="margin-top:14px;">
		<a class="btn ghost small" href="${ctx}/member/modify.do">수정하기</a>
	</div>
</div>

<div class="mypage-tabs">
	<button type="button" class="tab" data-pane="working">공개 (${fn:length(workingList)})</button>
	<button type="button" class="tab active" data-pane="complete">완성 (${fn:length(completeList)})</button>
	<button type="button" class="tab" data-pane="like">관심 (${fn:length(likedList)})</button>
</div>

<div class="tab-pane" id="pane-working">
	<c:choose>
		<c:when test="${empty workingList}"><div class="empty-state">공개작업 중인 작품이 없습니다.</div></c:when>
		<c:otherwise>
		<div class="fixed-grid">
			<c:forEach var="a" items="${workingList}">
			<a class="fixed-card" href="${ctx}/artwork/working/view?artworkId=${a.artworkId}">
				<c:choose>
					<c:when test="${a.repFileId > 0}"><img src="${ctx}/file/download.do?fileId=${a.repFileId}" alt="<c:out value='${a.title}'/>" loading="lazy"></c:when>
					<c:otherwise><div class="ph"><c:out value="${fn:substring(a.title, 0, 1)}"/></div></c:otherwise>
				</c:choose>
				<span class="cap"><c:out value="${a.title}"/></span>
			</a>
			</c:forEach>
		</div>
		</c:otherwise>
	</c:choose>
</div>

<div class="tab-pane active" id="pane-complete">
	<c:choose>
		<c:when test="${empty completeList}"><div class="empty-state">완성한 작품이 없습니다.</div></c:when>
		<c:otherwise>
		<div class="fixed-grid">
			<c:forEach var="a" items="${completeList}">
			<a class="fixed-card" href="${ctx}/artwork/complete/view?artworkId=${a.artworkId}">
				<c:choose>
					<c:when test="${a.repFileId > 0}"><img src="${ctx}/file/download.do?fileId=${a.repFileId}" alt="<c:out value='${a.title}'/>" loading="lazy"></c:when>
					<c:otherwise><div class="ph"><c:out value="${fn:substring(a.title, 0, 1)}"/></div></c:otherwise>
				</c:choose>
				<span class="cap"><c:out value="${a.title}"/></span>
			</a>
			</c:forEach>
		</div>
		</c:otherwise>
	</c:choose>
</div>

<div class="tab-pane" id="pane-like">
	<c:choose>
		<c:when test="${empty likedList}"><div class="empty-state">좋아요 누른 작품이 없습니다.</div></c:when>
		<c:otherwise>
		<div class="fixed-grid">
			<c:forEach var="a" items="${likedList}">
			<a class="fixed-card" href="${ctx}/artwork/${a.isStatus eq 'Y' ? 'complete' : 'working'}/view?artworkId=${a.artworkId}">
				<c:choose>
					<c:when test="${a.repFileId > 0}"><img src="${ctx}/file/download.do?fileId=${a.repFileId}" alt="<c:out value='${a.title}'/>" loading="lazy"></c:when>
					<c:otherwise><div class="ph"><c:out value="${fn:substring(a.title, 0, 1)}"/></div></c:otherwise>
				</c:choose>
				<span class="cap"><c:out value="${a.title}"/></span>
			</a>
			</c:forEach>
		</div>
		</c:otherwise>
	</c:choose>
</div>

<div style="text-align:center;margin-top:28px;">
	<button type="button" class="btn danger small" id="btnWithdraw">탈퇴하기</button>
</div>

<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
