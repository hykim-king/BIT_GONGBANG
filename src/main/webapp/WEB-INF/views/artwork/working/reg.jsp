<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  CC-ART-01 글쓰기(작품 등록) — 공개작업 폼.
  content = 1일차 작업내용을 겸함(artwork.content). 등록 후 공개 상세로 이동.
  이미지: 지연 업로드 위젯(targetType=ARTWORK, 작품 사진).
--%>
<c:set var="pageTitle" value="글쓰기 · 빚다"/>
<c:set var="activeMenu" value="working"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<div class="page-narrow" style="max-width:680px;">
	<div class="section-head"><h2>글쓰기</h2><p>공개작업은 1일차 작업 내용으로 시작하며, 상세 화면에서 작업일지를 이어서 기록합니다.</p></div>

	<div class="panel">
		<div class="field">
			<label>유형 선택</label>
			<label style="margin-right:16px;"><input type="radio" name="regType" value="complete"> 완성작</label>
			<label><input type="radio" name="regType" value="working" checked> 공개작업</label>
		</div>

		<form id="artworkRegForm" method="post" action="${ctx}/artwork/working/doSave">
			<div class="field">
				<label for="categoryId">카테고리</label>
				<select class="text-input" id="categoryId" name="categoryId" required>
					<option value="">카테고리 선택</option>
				</select>
			</div>
			<div class="field">
				<label for="title">제목</label>
				<input type="text" class="text-input" id="title" name="title" maxlength="30" required>
			</div>
			<div class="field">
				<label for="content">1일차 작업 내용</label>
				<textarea class="text-input" id="content" name="content" rows="7" required></textarea>
			</div>

			<c:set var="upTargetType" value="ARTWORK"/>
			<c:set var="upTargetId" value=""/>
			<c:set var="upEditable" value="true"/>
			<%@ include file="/WEB-INF/views/file/upload_widget.jsp" %>

			<div style="display:flex;gap:8px;margin-top:18px;">
				<button type="submit" class="btn" style="flex:1;">등록</button>
				<a class="btn ghost" style="flex:1;" href="${ctx}/artwork/working/list">취소</a>
			</div>
		</form>
	</div>
</div>

<script>
$(function() {
	var ctx = $('body').data('ctx') || '';

	$('input[name=regType]').on('change', function() {
		if (this.value === 'complete') { location.href = ctx + '/artwork/complete/reg'; }
	});

	$.get(ctx + '/category/doRetrieve.do', function(res) {
		if (res.code !== '200') { return; }
		var esc = (window.bitda && window.bitda.esc) || String;
		(res.data || []).forEach(function(cItem) {
			$('#categoryId').append('<option value="' + cItem.categoryId + '">' + esc(cItem.categoryNm) + '</option>');
		});
	}, 'json');

	/* native xhr.responseURL 로 최종 URL 확보(jqXHR 미지원 + follow된 상세가 404여도 URL 은 유효) */
	$('#artworkRegForm').on('submit', function(e) {
		e.preventDefault();
		if (!$('#categoryId').val()) { alert('카테고리를 선택하세요.'); return; }
		var $form = $(this);
		var nativeXhr = $.ajaxSettings.xhr();
		$.ajax({
			url: $form.attr('action'), method: 'POST', data: $form.serialize(),
			xhr: function() { return nativeXhr; }
		}).always(function() {
			var finalUrl = nativeXhr.responseURL || '';
			var m = finalUrl.match(/artworkId=(\d+)/);
			if (!m) {
				alert('등록 처리 중 오류가 발생했습니다. 로그인 상태를 확인하세요.');
				return;
			}
			var artworkId = m[1];
			var up = window.bitda.uploader.get($('.upload-widget')[0]);
			var job = up ? up.uploadTo('ARTWORK', artworkId) : null;
			if (job) {
				job.always(function() { location.href = finalUrl; });
			} else {
				location.href = finalUrl;
			}
		});
	});
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
