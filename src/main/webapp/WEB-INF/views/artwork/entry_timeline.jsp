<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  작업일지 타임라인 프래그먼트 (CC-CPL-02/CC-WRK-02 공용, 최신 일차 → 1일차)
  전제(호스트에서 c:set):
    - vo.entryList (ArtworkEntryVO, artwork_entry DESC 정렬)
    - tlEditable: 'true' 면 본인 화면 — 일차 수정/삭제 버튼 노출 (기본 false)
  일차별: 사진(AJAX 로딩) + 좋아요(like-btn, ARTWORK_ENTRY) + 댓글(comment_box include)
--%>
<c:if test="${not empty vo.entryList}">
<div class="entry-timeline">
	<h4 style="font-size:13px;font-weight:800;color:var(--ink-soft);margin:0 0 10px;">작업과정 타임라인</h4>
	<c:set var="entryTotal" value="${fn:length(vo.entryList)}"/>
	<c:forEach var="en" items="${vo.entryList}" varStatus="st">
	<div class="entry-item" id="entry-${en.artworkEntry}" data-entry-id="${en.artworkEntry}" data-reg-dt="${fn:substring(en.regDt, 0, 10)}">
		<c:if test="${tlEditable eq 'true'}">
		<span class="entry-btns">
			<a class="entry-edit">수정</a>
			<a class="entry-del">삭제</a>
		</span>
		</c:if>
		<span class="day-badge">${entryTotal - st.index}일차</span>
		<span class="entry-date">${fn:substring(en.regDt, 0, 10)}<c:if test="${not empty en.modDt}"> (수정됨)</c:if></span>
		<div class="entry-photos" data-entry-id="${en.artworkEntry}"></div>
		<p class="entry-body"><c:out value="${en.content}"/></p>
		<div class="entry-edit-area"></div>
		<div class="engage-row" style="margin-bottom:0;">
			<button type="button" class="like-btn" data-target-type="ARTWORK_ENTRY" data-target-id="${en.artworkEntry}">
				<span class="like-heart">&#9825;</span> <span class="like-count">0</span>
			</button>
		</div>
		<c:set var="cmtTargetType" value="ARTWORK_ENTRY"/>
		<c:set var="cmtTargetId" value="${en.artworkEntry}"/>
		<%@ include file="/WEB-INF/views/comment/comment_box.jsp" %>
	</div>
	</c:forEach>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
	var ctx = document.body.dataset.ctx || '';
	var esc = (window.bitda && window.bitda.esc) || String;

	/* 일차별 사진 로딩 (targetType=ARTWORK_ENTRY) */
	document.querySelectorAll('.entry-photos').forEach(function(ph) {
		$.post(ctx + '/file/doRetrieve.do', { targetType: 'ARTWORK_ENTRY', targetId: ph.dataset.entryId }, function(res) {
			if (res.code !== '200' || !(res.data || []).length) { return; }
			var html = '';
			res.data.forEach(function(f) {
				html += '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" alt="' + esc(f.orgFileNm) + '" loading="lazy">';
			});
			ph.innerHTML = html;
		}, 'json');
	});

	/* 일차 수정(본인): 인라인 폼 전환 → POST /artworkEntry/doUpdate */
	document.addEventListener('click', function(e) {
		var t = e.target.closest('.entry-edit');
		if (!t) return;
		var item = t.closest('.entry-item');
		if (item.querySelectorAll('.entry-edit-form').length) { return; }
		var cur = item.querySelector('.entry-body').textContent;
		item.querySelector('.entry-body').style.display = 'none';
		item.querySelector('.entry-edit-area').innerHTML =
			'<form class="entry-edit-form" method="post" action="' + ctx + '/artworkEntry/doUpdate">' +
			'<input type="hidden" name="artworkEntry" value="' + item.dataset.entryId + '">' +
			'<textarea class="text-input" name="content" rows="4" required></textarea>' +
			'<div style="display:flex;gap:6px;margin-top:8px;">' +
			'<button type="submit" class="btn small">저장</button>' +
			'<button type="button" class="btn ghost small entry-edit-cancel">취소</button></div>' +
			'</form>';
		var ta = item.querySelector('.entry-edit-form textarea');
		ta.value = cur;
		ta.focus();
	});
	document.addEventListener('click', function(e) {
		var t = e.target.closest('.entry-edit-cancel');
		if (!t) return;
		var item = t.closest('.entry-item');
		item.querySelector('.entry-edit-area').replaceChildren();
		item.querySelector('.entry-body').style.display = '';
	});

	/* 일차 삭제(본인): confirm 후 POST form 제출 */
	document.addEventListener('click', function(e) {
		var t = e.target.closest('.entry-del');
		if (!t) return;
		if (!confirm('이 작업일지를 삭제하시겠습니까?')) { return; }
		var entryId = t.closest('.entry-item').dataset.entryId;
		var form = document.createElement('form');
		form.method = 'post';
		form.action = ctx + '/artworkEntry/doDelete';
		var input = document.createElement('input');
		input.type = 'hidden';
		input.name = 'artworkEntry';
		input.value = entryId;
		form.appendChild(input);
		document.body.appendChild(form);
		form.submit();
	});
});
</script>
</c:if>
