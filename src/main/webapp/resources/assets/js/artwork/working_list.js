/* ============================================================================
 * CC-WRK-01 공개작품 목록 — 카테고리 필터 옵션 채우기
 * 목록·검색·페이징 자체는 폼 전송(GET)으로 처리하므로 여기서 다루지 않는다.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	/* 현재 선택된 카테고리는 select[data-selected] 로 서버가 내려준다 */
	window.bitda.fillCategorySelect(document.getElementById('categoryFilter'));
});
