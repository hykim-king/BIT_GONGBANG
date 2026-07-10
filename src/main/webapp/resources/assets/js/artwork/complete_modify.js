/* ============================================================================
 * CC-ART-02 작품 수정 — 카테고리 셀렉트 옵션 채우기(현재 값 미리 선택)
 * 백엔드 계약: GET /category/doRetrieve.do → data=List<CategoryVO>
 * 폼 제출은 <form action="/artwork/doUpdate" method="post"> 평범한 전송(PRG).
 * 이미지 위젯은 즉시 모드(upTargetId=artworkId)라 upload.js 가 알아서 처리한다.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	var ctx = document.body.dataset.ctx || '';
	var sel = document.getElementById('categoryId');
	if (!sel) { return; }

	/* 이 작품의 현재 카테고리(서버가 data-selected 로 내려줌) */
	var selected = String(sel.dataset.selected || '');

	$.get(ctx + '/category/doRetrieve.do', function (res) {
		if (res.code !== '200') { return; }
		(res.data || []).forEach(function (cItem) {
			var opt = document.createElement('option');
			opt.value = cItem.categoryId;
			opt.textContent = cItem.categoryNm;
			if (String(cItem.categoryId) === selected) { opt.selected = true; }
			sel.appendChild(opt);
		});
	}, 'json');
});
