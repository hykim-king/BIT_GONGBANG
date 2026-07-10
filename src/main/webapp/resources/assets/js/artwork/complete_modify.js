/* ============================================================================
 * CC-ART-02 작품 수정 — 카테고리 셀렉트 옵션 채우기(현재 값 미리 선택)
 * 폼 제출은 <form action="/artwork/doUpdate" method="post"> 평범한 전송(PRG).
 * 이미지 위젯은 즉시 모드(upTargetId=artworkId)라 upload.js 가 알아서 처리한다.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	/* 이 작품의 현재 카테고리는 select[data-selected] 로 서버가 내려준다 */
	window.bitda.fillCategorySelect(document.getElementById('categoryId'));
});
