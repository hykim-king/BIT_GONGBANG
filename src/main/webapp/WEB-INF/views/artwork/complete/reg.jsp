<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  CC-ART-01 글쓰기(작품 등록) — 완성작 폼.
  유형선택 라디오: 완성작(현재)/공개작업(working/reg 이동).
  이미지: 지연 업로드 위젯 — 저장(PRG) 후 redirect URL 의 artworkId 로 일괄 업로드.
--%>
<c:set var="pageTitle" value="글쓰기 · 빚다"/>
<c:set var="activeMenu" value="complete"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<div class="page-narrow" style="max-width:680px;">
	<div class="section-head"><h2>글쓰기</h2><p>완성작은 등록 즉시 완성 게시판에 노출됩니다.</p></div>

	<div class="panel">
		<div class="field">
			<label>유형 선택</label>
			<label style="margin-right:16px;"><input type="radio" name="regType" value="complete" checked> 완성작</label>
			<label><input type="radio" name="regType" value="working"> 공개작업</label>
		</div>

		<form id="artworkRegForm" method="post" action="${ctx}/artwork/complete/doSave">
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
				<label for="content">본문</label>
				<textarea class="text-input" id="content" name="content" rows="7" required></textarea>
			</div>

			<c:set var="upTargetType" value="ARTWORK"/>
			<c:set var="upTargetId" value=""/>
			<c:set var="upEditable" value="true"/>
			<%@ include file="/WEB-INF/views/file/upload_widget.jsp" %>

			<div style="display:flex;gap:8px;margin-top:18px;">
				<button type="submit" class="btn" style="flex:1;">등록</button>
				<a class="btn ghost" style="flex:1;" href="${ctx}/artwork/complete/list">취소</a>
			</div>
		</form>
	</div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
	var ctx = document.body.dataset.ctx || '';

	/* 유형 전환: 공개작업 선택 시 working/reg 로 이동 */
	document.querySelectorAll('input[name=regType]').forEach(function(radio) {
		radio.addEventListener('change', function() {
			if (this.value === 'working') { location.href = ctx + '/artwork/working/reg'; }
		});
	});

	/* 카테고리 드롭다운 로드 */
	$.get(ctx + '/category/doRetrieve.do', function(res) {
		if (res.code !== '200') { return; }
		var esc = (window.bitda && window.bitda.esc) || String;
		(res.data || []).forEach(function(cItem) {
			document.getElementById('categoryId').insertAdjacentHTML('beforeend', '<option value="' + cItem.categoryId + '">' + esc(cItem.categoryNm) + '</option>');
		});
	}, 'json');

	/* 등록: 저장(PRG) → redirect 최종 URL 의 artworkId 로 지연 업로드 → 상세 이동.
	   native xhr.responseURL 로 최종 URL 확보(jqXHR 미지원 + follow된 상세가 404여도 URL 은 유효) */
	var artworkRegForm = document.getElementById('artworkRegForm');
	if (artworkRegForm) {
		artworkRegForm.addEventListener('submit', function(e) {
			e.preventDefault();
			if (!document.getElementById('categoryId').value) { alert('카테고리를 선택하세요.'); return; }
			var form = this;
			var nativeXhr = $.ajaxSettings.xhr();
			$.ajax({
				url: form.getAttribute('action'), method: 'POST', data: window.bitda.serializeForm(form),
				xhr: function() { return nativeXhr; }
			}).always(function() {
				var finalUrl = nativeXhr.responseURL || '';
				var m = finalUrl.match(/artworkId=(\d+)/);
				if (!m) {
					alert('등록 처리 중 오류가 발생했습니다. 로그인 상태를 확인하세요.');
					return;
				}
				var artworkId = m[1];
				var up = window.bitda.uploader.get(document.querySelector('.upload-widget'));
				var job = up ? up.uploadTo('ARTWORK', artworkId) : null;
				if (job) {
					job.always(function() { location.href = finalUrl; });
				} else {
					location.href = finalUrl;
				}
			});
		});
	}
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
