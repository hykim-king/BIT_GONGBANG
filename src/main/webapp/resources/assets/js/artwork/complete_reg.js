/* ============================================================================
 * CC-ART-01 글쓰기(완성작) — 유형 전환 + 카테고리 로드 + 등록
 * 백엔드 계약:
 *  - GET  /category/doRetrieve.do → data=List<CategoryVO>
 *  - POST /artwork/complete/doSave (PRG) → redirect:/artwork/complete/view?artworkId=N
 *
 * 등록 흐름(PRG + 지연 업로드):
 *  1) 폼을 $.ajax 로 POST 하면 서버는 JSON 이 아니라 302 redirect 를 준다.
 *  2) jqXHR 은 리다이렉트 후 최종 URL 을 알려주지 않으므로,
 *     $.ajaxSettings.xhr() 로 네이티브 XHR 을 만들어 주입하고 responseURL 을 읽는다.
 *  3) 그 URL 에서 새 artworkId 를 캐내 지연 보관된 이미지를 업로드한 뒤 상세로 이동한다.
 *     (정규식이 안 맞으면 로그인 안 된 상태로 redirect:/member/login.do 가 온 것)
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const ctx = document.body.dataset.ctx || '';

	/* 유형 전환: 공개작업 선택 시 working/reg 로 이동 */
	document.querySelectorAll('input[name=regType]').forEach(function (radio) {
		radio.addEventListener('change', function () {
			if (this.value === 'working') { location.href = ctx + '/artwork/working/reg'; }
		});
	});

	/* 카테고리 드롭다운 로드 */
	window.bitda.fillCategorySelect(document.getElementById('categoryId'));

	/* 등록 */
	const artworkRegForm = document.getElementById('artworkRegForm');
	if (artworkRegForm) {
		artworkRegForm.addEventListener('submit', function (e) {
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
