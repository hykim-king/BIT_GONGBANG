/* ============================================================================
 * CC-CPL-02 완성작 상세 — 첨부 이미지(hero/썸네일) + 작품 삭제
 * 전제: 컨테이너 #artworkDetail 이 data-artwork-id 를 들고 있다.
 * 백엔드 계약:
 *  - POST /file/doRetrieve.do {targetType:'ARTWORK', targetId} → data=List<FileVO>
 *  - GET  /file/download.do?fileId=N (이미지 src)
 *  - POST /artwork/doDelete {artworkId} (PRG, 폼 전송 — JSON 이 아니라 redirect 응답)
 * 댓글·좋아요·작업일지 타임라인은 각자 자동 마운트 컴포넌트가 담당한다.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const root = document.getElementById('artworkDetail');
	if (!root) { return; }

	const ctx = document.body.dataset.ctx || '';
	const esc = (window.bitda && window.bitda.esc) || String;
	const artworkId = root.dataset.artworkId;

	/* 작품 첨부 로딩: 대표=hero, 나머지 썸네일(클릭 시 hero 교체) */
	$.post(ctx + '/file/doRetrieve.do', { targetType: 'ARTWORK', targetId: artworkId }, function (res) {
		if (res.code !== '200' || !(res.data || []).length) { return; }
		const files = res.data;
		document.getElementById('heroArea').innerHTML = '<img class="detail-hero" id="heroImg" src="' + ctx + '/file/download.do?fileId=' + files[0].fileId + '" alt="">';
		let th = '';
		files.forEach(function (f) {
			th += '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" class="' + (f.isRep === 'Y' ? 'rep' : '') + '" data-file-id="' + f.fileId + '" alt="' + esc(f.orgFileNm) + '">';
		});
		document.getElementById('thumbArea').innerHTML = th;
	}, 'json');

	/* 썸네일 클릭 → hero 교체 (썸네일은 AJAX 이후 생기므로 위임) */
	document.addEventListener('click', function (e) {
		const t = e.target.closest('#thumbArea img');
		if (!t) { return; }
		const hero = document.getElementById('heroImg');
		if (hero) { hero.setAttribute('src', ctx + '/file/download.do?fileId=' + t.dataset.fileId); }
	});

	/* 삭제(본인/관리자에게만 버튼이 렌더된다) */
	const btnDelete = document.getElementById('btnArtworkDelete');
	if (btnDelete) {
		btnDelete.addEventListener('click', function () {
			if (!confirm('작품을 삭제하시겠습니까? 첨부/댓글/좋아요가 함께 삭제됩니다.')) { return; }
			const form = document.createElement('form');
			form.method = 'post';
			form.action = ctx + '/artwork/doDelete';
			const input = document.createElement('input');
			input.type = 'hidden';
			input.name = 'artworkId';
			input.value = artworkId;
			form.appendChild(input);
			document.body.appendChild(form);
			form.submit();
		});
	}
});
