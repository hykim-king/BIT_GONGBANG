/* ============================================================================
 * CC-ART-01 글쓰기(공개작업) — 유형 전환 + 카테고리 로드 + 등록
 * 백엔드 계약:
 *  - GET  /category/doRetrieve.do → data=List<CategoryVO>
 *  - POST /artwork/working/doSave (PRG) → redirect:/artwork/working/view?artworkId=N
 *
 * 등록 흐름은 complete_reg.js 와 동일한 PRG + 지연 업로드 패턴이다.
 * (네이티브 XHR 의 responseURL 에서 새 artworkId 를 캐내 이미지를 붙인 뒤 이동)
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const ctx = document.body.dataset.ctx || '';

	/* 유형 전환: 완성작 선택 시 complete/reg 로 이동 */
	document.querySelectorAll('input[name=regType]').forEach(function (radio) {
		radio.addEventListener('change', function () {
			if (this.value === 'complete') { location.href = ctx + '/artwork/complete/reg'; }
		});
	});

	/* 카테고리 드롭다운 로드 */
	window.bitda.fillCategorySelect(document.getElementById('categoryId'));

	/* 등록 */
	const regForm = document.getElementById('artworkRegForm');
	if (regForm) {
		regForm.addEventListener('submit', function (e) {
			e.preventDefault();
			if (!document.getElementById('categoryId').value) { alert('카테고리를 선택하세요.'); return; }
			const upCheck = window.bitda.uploader.get(document.querySelector('.upload-widget'));
			if (!upCheck || upCheck.count() === 0) { alert('대표 사진을 1장 이상 등록하세요.'); return; }
			const form = this;
			const nativeXhr = $.ajaxSettings.xhr();
			$.ajax({
				url: form.getAttribute('action'), method: 'POST', data: window.bitda.serializeForm(form),
				xhr: function () { return nativeXhr; }
			}).always(function () {
				const finalUrl = nativeXhr.responseURL || '';
				const m = finalUrl.match(/artworkId=(\d+)/);
				if (!m) {
					alert('등록 처리 중 오류가 발생했습니다. 로그인 상태를 확인하세요.');
					return;
				}
				const artworkId = m[1];
				const up = window.bitda.uploader.get(document.querySelector('.upload-widget'));
				const job = up ? up.uploadTo('ARTWORK', artworkId) : null;
				if (job) {
					job.always(function () { location.href = finalUrl; });
				} else {
					location.href = finalUrl;
				}
			});
		});
	}
});
