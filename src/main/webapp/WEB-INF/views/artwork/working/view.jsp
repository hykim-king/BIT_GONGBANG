<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  CC-WRK-02 공개작품 상세 — 작품 + 작업일지 타임라인(최신→1일차) + 일차별 좋아요·댓글
  + 미니캘린더(작업일지 날짜, 클릭 시 해당 일차로 스크롤).
  본인: 작업일지 추가/수정/삭제 + 완성작품 등록하기(CC-WRK-03 대표사진 선택 모달).
--%>
<c:set var="pageTitle" value="${vo.title} · 빚다"/>
<c:set var="activeMenu" value="working"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<c:set var="isOwner" value="${not empty sessionScope.loginMember and sessionScope.loginMember.memberId == vo.memberId}"/>
<c:set var="isAdminUser" value="${not empty sessionScope.loginMember and sessionScope.loginMember.isAdmin eq 'Y'}"/>

<div class="detail-grid">
	<div>
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
			<c:choose>
				<c:when test="${vo.isStatus eq 'Y'}"><span class="pill complete">완성</span></c:when>
				<c:otherwise><span class="pill working">진행중</span></c:otherwise>
			</c:choose>
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

		<%-- 작업일지 추가(본인) — 지연 업로드(ARTWORK_ENTRY): 저장 후 newEntryId 로 일괄 업로드 --%>
		<c:if test="${isOwner}">
		<div class="add-entry-card">
			<h4 style="font-size:13px;font-weight:800;color:var(--ink-soft);margin:0 0 10px;">작업일지 추가</h4>
			<form id="entryRegForm" method="post" action="${ctx}/artworkEntry/doSave">
				<input type="hidden" name="artworkId" value="${vo.artworkId}">
				<div class="field">
					<textarea class="text-input" name="content" id="entryContent" rows="4" placeholder="오늘의 작업 내용을 기록하세요" required></textarea>
				</div>
				<c:set var="upTargetType" value="ARTWORK_ENTRY"/>
				<c:set var="upTargetId" value=""/>
				<c:set var="upEditable" value="true"/>
				<%@ include file="/WEB-INF/views/file/upload_widget.jsp" %>
				<button type="submit" class="btn" style="margin-top:8px;">일지 등록</button>
			</form>
		</div>
		</c:if>

		<%-- 작업일지 타임라인 (본인이면 일차 수정/삭제 노출) --%>
		<c:set var="tlEditable" value="${isOwner ? 'true' : 'false'}"/>
		<%@ include file="/WEB-INF/views/artwork/entry_timeline.jsp" %>
	</div>

	<aside>
		<div class="side-panel mini-cal" id="miniCal">
			<h4>작업 캘린더</h4>
			<div class="cal-head">
				<span class="cal-nav" id="calPrev">&lt;</span>
				<span id="calTitle"></span>
				<span class="cal-nav" id="calNext">&gt;</span>
			</div>
			<div class="cal-grid" id="calGrid"></div>
		</div>

		<c:if test="${isOwner and vo.isStatus eq 'N'}">
		<div class="side-panel">
			<h4>완성 전환</h4>
			<p class="hint" style="margin:0 0 10px;">작품이 완성되었다면 대표사진을 골라 완성 게시판에 등록하세요.</p>
			<button type="button" class="btn block" id="btnOpenComplete">완성작품 등록하기</button>
		</div>
		</c:if>
	</aside>
</div>

<%-- CC-WRK-03 대표사진 선택 모달 (완성 전환 중간화면) --%>
<c:if test="${isOwner and vo.isStatus eq 'N'}">
<div class="overlay" id="completeModal">
	<div class="modal">
		<div class="modal-head">
			<h3>대표사진 선택</h3>
			<button type="button" class="modal-close" aria-label="닫기">&times;</button>
		</div>
		<p class="hint" style="margin:0 0 6px;">작품 사진 중 완성 게시판 썸네일로 쓸 대표사진을 선택하세요. (작업일지 사진은 대상이 아닙니다)</p>
		<div class="photo-grid" id="completePhotoGrid"></div>
		<form method="post" action="${ctx}/artwork/working/complete">
			<input type="hidden" name="artworkId" value="${vo.artworkId}">
			<button type="submit" class="btn block">완성작품 등록 확정</button>
		</form>
	</div>
</div>
</c:if>

<script>
$(function() {
	var ctx = $('body').data('ctx') || '';
	var esc = (window.bitda && window.bitda.esc) || String;
	var artworkId = ${vo.artworkId};

	/* 작품 첨부: hero + 썸네일 */
	$.post(ctx + '/file/doRetrieve.do', { targetType: 'ARTWORK', targetId: artworkId }, function(res) {
		if (res.code !== '200' || !(res.data || []).length) { return; }
		var files = res.data;
		$('#heroArea').html('<img class="detail-hero" id="heroImg" src="' + ctx + '/file/download.do?fileId=' + files[0].fileId + '" alt="">');
		var th = '';
		files.forEach(function(f) {
			th += '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" class="' + (f.isRep === 'Y' ? 'rep' : '') + '" data-file-id="' + f.fileId + '" alt="' + esc(f.orgFileNm) + '">';
		});
		$('#thumbArea').html(th);
	}, 'json');
	$(document).on('click', '#thumbArea img', function() {
		$('#heroImg').attr('src', ctx + '/file/download.do?fileId=' + $(this).data('file-id'));
	});

	/* 삭제(본인/관리자) */
	$('#btnArtworkDelete').on('click', function() {
		if (!confirm('작품을 삭제하시겠습니까? 첨부/댓글/좋아요/작업일지가 함께 삭제됩니다.')) { return; }
		$('<form>', { method: 'post', action: ctx + '/artwork/doDelete' })
			.append($('<input>', { type: 'hidden', name: 'artworkId', value: artworkId }))
			.appendTo('body').trigger('submit');
	});

	/* 작업일지 등록: 저장(PRG) → 최종 URL 의 newEntryId 로 지연 이미지 업로드 → 이동 */
	$('#entryRegForm').on('submit', function(e) {
		e.preventDefault();
		if (!$('#entryContent').val().trim()) { alert('작업 내용을 입력하세요.'); return; }
		var $form = $(this);
		var nativeXhr = $.ajaxSettings.xhr();
		$.ajax({
			url: $form.attr('action'), method: 'POST', data: $form.serialize(),
			xhr: function() { return nativeXhr; }
		}).always(function() {
			var finalUrl = nativeXhr.responseURL || '';
			var m = finalUrl.match(/newEntryId=(\d+)/);
			if (!m) {
				alert('작업일지 등록 처리 중 오류가 발생했습니다.');
				return;
			}
			var up = window.bitda.uploader.get($form.find('.upload-widget')[0]);
			var job = up ? up.uploadTo('ARTWORK_ENTRY', m[1]) : null;
			if (job) { job.always(function() { location.href = finalUrl; }); }
			else { location.href = finalUrl; }
		});
	});

	/* 미니 캘린더: 타임라인의 reg_dt 수집 → 월 그리드 렌더, 클릭 시 해당 일차로 스크롤 */
	var entryDates = {};
	$('.entry-item').each(function() {
		var d = String($(this).data('reg-dt') || '');
		if (d) { entryDates[d] = $(this).attr('id'); }
	});
	var calBase = new Date();
	var dateKeys = Object.keys(entryDates).sort();
	if (dateKeys.length) { calBase = new Date(dateKeys[dateKeys.length - 1] + 'T00:00:00'); }
	var calY = calBase.getFullYear(), calM = calBase.getMonth();

	function pad(n) { return (n < 10 ? '0' : '') + n; }
	function renderCal() {
		$('#calTitle').text(calY + '년 ' + (calM + 1) + '월');
		var first = new Date(calY, calM, 1).getDay();
		var days = new Date(calY, calM + 1, 0).getDate();
		var html = '';
		['일','월','화','수','목','금','토'].forEach(function(w) { html += '<span class="cal-dow">' + w + '</span>'; });
		for (var i = 0; i < first; i++) { html += '<span class="cal-day empty">.</span>'; }
		for (var d = 1; d <= days; d++) {
			var key = calY + '-' + pad(calM + 1) + '-' + pad(d);
			html += '<span class="cal-day' + (entryDates[key] ? ' has-entry' : '') + '" data-key="' + key + '">' + d + '</span>';
		}
		$('#calGrid').html(html);
	}
	renderCal();
	$('#calPrev').on('click', function() { calM--; if (calM < 0) { calM = 11; calY--; } renderCal(); });
	$('#calNext').on('click', function() { calM++; if (calM > 11) { calM = 0; calY++; } renderCal(); });
	$(document).on('click', '.cal-day.has-entry', function() {
		var id = entryDates[$(this).data('key')];
		if (id) { document.getElementById(id).scrollIntoView({ behavior: 'smooth' }); }
	});

	/* CC-WRK-03: 완성 전환 모달 — 후보 사진(ARTWORK) + 클릭=대표 지정(setRep) */
	function loadCompletePhotos() {
		$.post(ctx + '/file/doRetrieve.do', { targetType: 'ARTWORK', targetId: artworkId }, function(res) {
			if (res.code !== '200') { return; }
			var files = res.data || [];
			if (!files.length) {
				$('#completePhotoGrid').html('<p class="hint">등록된 작품 사진이 없습니다. 사진 없이도 완성 등록은 가능합니다.</p>');
				return;
			}
			var html = '';
			files.forEach(function(f) {
				html += '<div class="photo-slot' + (f.isRep === 'Y' ? ' rep' : '') + '" data-file-id="' + f.fileId + '">'
					+ (f.isRep === 'Y' ? '<span class="rep-mark">대표</span>' : '')
					+ '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" alt="' + esc(f.orgFileNm) + '"></div>';
			});
			$('#completePhotoGrid').html(html);
		}, 'json');
	}
	$('#btnOpenComplete').on('click', function() {
		loadCompletePhotos();
		$('#completeModal').addClass('open');
	});
	$(document).on('click', '#completePhotoGrid .photo-slot:not(.rep)', function() {
		$.post(ctx + '/file/setRep.do', { fileId: $(this).data('file-id') }, function(res) {
			if (res.code === '200') { loadCompletePhotos(); }
			else { alert(res.message || '대표 지정에 실패했습니다.'); }
		}, 'json');
	});
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
