<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  CC-CPL-02 완성작품 상세 — 대표이미지+썸네일, 메타, 본문, 좋아요/댓글(작품 단위),
  공개작 경유 시 작업일지 타임라인 인라인(최신→1일차, 일차별 좋아요·댓글 동일 포맷).
  본인/관리자: 수정/삭제.
--%>
<c:set var="pageTitle" value="${vo.title} · 빚다"/>
<c:set var="activeMenu" value="complete"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<c:set var="isOwner" value="${not empty sessionScope.loginMember and sessionScope.loginMember.memberId == vo.memberId}"/>
<c:set var="isAdminUser" value="${not empty sessionScope.loginMember and sessionScope.loginMember.isAdmin eq 'Y'}"/>

<div style="max-width:820px;">
	<div id="heroArea">
		<div class="detail-hero ph"><c:out value="${fn:substring(vo.title, 0, 1)}"/></div>
	</div>
	<div class="detail-thumbs" id="thumbArea"></div>

	<div class="detail-title-row">
		<h2><c:out value="${vo.title}"/></h2>
		<c:if test="${isOwner or isAdminUser}">
		<span style="display:flex;gap:6px;flex-shrink:0;">
			<a class="btn ghost small" href="${ctx}/artwork/complete/modify?artworkId=${vo.artworkId}">수정</a>
			<button type="button" class="btn danger small" id="btnArtworkDelete">삭제</button>
		</span>
		</c:if>
	</div>
	<div class="detail-meta">
		<span>작성자 <strong><c:out value="${vo.nickname}"/></strong></span>
		<span>카테고리 <c:out value="${vo.categoryNm}"/></span>
		<c:if test="${not empty vo.compDt}"><span>완성일 ${fn:substring(vo.compDt, 0, 10)}</span></c:if>
		<span>조회수 ${vo.viewCount}</span>
	</div>

	<div class="detail-content"><c:out value="${vo.content}"/></div>

	<div class="engage-row">
		<button type="button" class="like-btn" data-target-type="ARTWORK" data-target-id="${vo.artworkId}" data-count="${vo.likeCount}">
			<span class="like-heart">&#9825;</span> <span class="like-count">${vo.likeCount}</span>
		</button>
	</div>

	<%-- 작품 단위 댓글 --%>
	<c:set var="cmtTargetType" value="ARTWORK"/>
	<c:set var="cmtTargetId" value="${vo.artworkId}"/>
	<%@ include file="/WEB-INF/views/comment/comment_box.jsp" %>

	<%-- 공개작 경유 시 작업일지 타임라인 인라인 (완성 상세는 조회 전용 — 수정/삭제 미노출) --%>
	<c:set var="tlEditable" value="false"/>
	<%@ include file="/WEB-INF/views/artwork/entry_timeline.jsp" %>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
	var ctx = document.body.dataset.ctx || '';
	var esc = (window.bitda && window.bitda.esc) || String;

	/* 작품 첨부 로딩: 대표=hero, 나머지 썸네일(클릭 시 hero 교체) */
	$.post(ctx + '/file/doRetrieve.do', { targetType: 'ARTWORK', targetId: ${vo.artworkId} }, function(res) {
		if (res.code !== '200' || !(res.data || []).length) { return; }
		var files = res.data;
		document.getElementById('heroArea').innerHTML = '<img class="detail-hero" id="heroImg" src="' + ctx + '/file/download.do?fileId=' + files[0].fileId + '" alt="">';
		var th = '';
		files.forEach(function(f, i) {
			th += '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" class="' + (f.isRep === 'Y' ? 'rep' : '') + '" data-file-id="' + f.fileId + '" alt="' + esc(f.orgFileNm) + '">';
		});
		document.getElementById('thumbArea').innerHTML = th;
	}, 'json');
	document.addEventListener('click', function(e) {
		var t = e.target.closest('#thumbArea img');
		if (!t) { return; }
		var hero = document.getElementById('heroImg');
		if (hero) { hero.setAttribute('src', ctx + '/file/download.do?fileId=' + t.dataset.fileId); }
	});

	/* 삭제(본인/관리자) */
	var btnDelete = document.getElementById('btnArtworkDelete');
	if (btnDelete) {
		btnDelete.addEventListener('click', function() {
			if (!confirm('작품을 삭제하시겠습니까? 첨부/댓글/좋아요가 함께 삭제됩니다.')) { return; }
			var form = document.createElement('form');
			form.method = 'post';
			form.action = ctx + '/artwork/doDelete';
			var input = document.createElement('input');
			input.type = 'hidden';
			input.name = 'artworkId';
			input.value = ${vo.artworkId};
			form.appendChild(input);
			document.body.appendChild(form);
			form.submit();
		});
	}
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
