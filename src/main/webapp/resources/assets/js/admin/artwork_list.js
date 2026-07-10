/* ============================================================================
 * CC-ADM-03 게시물 관리 — 게시물 삭제
 * 백엔드 계약:
 *  - POST /artwork/doDelete {artworkId}
 *    ArtworkController.doDelete 를 재사용한다(관리자 허용 분기 있음).
 *    이 핸들러는 JSON이 아니라 redirect 를 돌려주는 페이지 컨트롤러이므로,
 *    응답 본문은 버리고 .always() 에서 관리 목록을 새로고침한다.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const ctx = document.body.dataset.ctx || '';

	document.querySelectorAll('.art-del').forEach(function (btn) {
		btn.addEventListener('click', function () {
			const tr = this.closest('tr');
			if (!confirm('"' + tr.dataset.title + '" 게시물을 삭제하시겠습니까?\n첨부/댓글/좋아요/작업일지가 함께 삭제됩니다.')) { return; }
			$.ajax({
				url: ctx + '/artwork/doDelete', method: 'POST',
				data: { artworkId: tr.dataset.artworkId }
			}).always(function () { location.reload(); });
		});
	});
});
