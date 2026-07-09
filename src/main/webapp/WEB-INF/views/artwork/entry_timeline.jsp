<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
$(function() {
	var ctx = $('body').data('ctx') || '';
	var esc = (window.bitda && window.bitda.esc) || String;

	/* 일차별 사진 로딩 (targetType=ARTWORK_ENTRY) */
	$('.entry-photos').each(function() {
		var $ph = $(this);
		$.post(ctx + '/file/doRetrieve.do', { targetType: 'ARTWORK_ENTRY', targetId: $ph.data('entry-id') }, function(res) {
			if (res.code !== '200' || !(res.data || []).length) { return; }
			var html = '';
			res.data.forEach(function(f) {
				html += '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" alt="' + esc(f.orgFileNm) + '" loading="lazy">';
			});
			$ph.html(html);
		}, 'json');
	});

	/* 일차 수정(본인): 인라인 폼 전환 → POST /artworkEntry/doUpdate */
	$(document).on('click', '.entry-edit', function() {
		var $item = $(this).closest('.entry-item');
		if ($item.find('.entry-edit-form').length) { return; }
		var cur = $item.find('.entry-body').text();
		$item.find('.entry-body').hide();
		$item.find('.entry-edit-area').html(
			'<form class="entry-edit-form" method="post" action="' + ctx + '/artworkEntry/doUpdate">' +
			'<input type="hidden" name="artworkEntry" value="' + $item.data('entry-id') + '">' +
			'<textarea class="text-input" name="content" rows="4" required></textarea>' +
			'<div style="display:flex;gap:6px;margin-top:8px;">' +
			'<button type="submit" class="btn small">저장</button>' +
			'<button type="button" class="btn ghost small entry-edit-cancel">취소</button></div>' +
			'</form>');
		$item.find('.entry-edit-form textarea').val(cur).focus();
	});
	$(document).on('click', '.entry-edit-cancel', function() {
		var $item = $(this).closest('.entry-item');
		$item.find('.entry-edit-area').empty();
		$item.find('.entry-body').show();
	});

	/* 일차 삭제(본인): confirm 후 POST form 제출 */
	$(document).on('click', '.entry-del', function() {
		if (!confirm('이 작업일지를 삭제하시겠습니까?')) { return; }
		var entryId = $(this).closest('.entry-item').data('entry-id');
		$('<form>', { method: 'post', action: ctx + '/artworkEntry/doDelete' })
			.append($('<input>', { type: 'hidden', name: 'artworkEntry', value: entryId }))
			.appendTo('body').trigger('submit');
	});
});
</script>
</c:if>
