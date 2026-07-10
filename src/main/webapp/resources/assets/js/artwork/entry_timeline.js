/* ============================================================================
 * 작업일지 타임라인 컴포넌트 — .entry-timeline 자동 마운트
 * 호스트: artwork/complete/view.jsp, artwork/working/view.jsp
 *         (entry_timeline.jsp 프래그먼트를 정적 include)
 * 백엔드 계약:
 *  - POST /file/doRetrieve.do {targetType:'ARTWORK_ENTRY', targetId} → data=List<FileVO>
 *  - POST /artworkEntry/doUpdate {artworkEntry, content}  (PRG, 폼 전송)
 *  - POST /artworkEntry/doDelete {artworkEntry}           (PRG, 폼 전송)
 *  일차 수정/삭제 버튼은 tlEditable='true'(본인) 일 때만 렌더된다.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	/* 타임라인이 없는 화면에서는 아무것도 하지 않는다 */
	if (!document.querySelector('.entry-timeline')) { return; }

	var ctx = document.body.dataset.ctx || '';
	var esc = (window.bitda && window.bitda.esc) || String;

	/* 일차별 사진 로딩 (targetType=ARTWORK_ENTRY) */
	document.querySelectorAll('.entry-photos').forEach(function (ph) {
		$.post(ctx + '/file/doRetrieve.do', { targetType: 'ARTWORK_ENTRY', targetId: ph.dataset.entryId }, function (res) {
			if (res.code !== '200' || !(res.data || []).length) { return; }
			var html = '';
			res.data.forEach(function (f) {
				html += '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" alt="' + esc(f.orgFileNm) + '" loading="lazy">';
			});
			ph.innerHTML = html;
		}, 'json');
	});

	/* 일차 수정(본인): 인라인 폼 전환 → POST /artworkEntry/doUpdate */
	document.addEventListener('click', function (e) {
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
	document.addEventListener('click', function (e) {
		var t = e.target.closest('.entry-edit-cancel');
		if (!t) return;
		var item = t.closest('.entry-item');
		item.querySelector('.entry-edit-area').replaceChildren();
		item.querySelector('.entry-body').style.display = '';
	});

	/* 일차 삭제(본인): confirm 후 POST form 제출 */
	document.addEventListener('click', function (e) {
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
