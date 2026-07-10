/* ============================================================================
 * CC-CPL-01 완성품 목록 — 카테고리 필터 옵션 채우기
 * 백엔드 계약: GET /category/doRetrieve.do → data=List<CategoryVO>
 * 목록·검색·페이징 자체는 폼 전송(GET)으로 처리하므로 여기서 다루지 않는다.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	var ctx = document.body.dataset.ctx || '';
	var sel = document.getElementById('categoryFilter');
	if (!sel) { return; }

	/* 현재 선택된 카테고리(서버가 data-selected 로 내려줌) */
	var selected = String(sel.dataset.selected || '0');

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
