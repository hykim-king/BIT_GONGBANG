/* ============================================================================
 * CC-WRK-02 공개작품 상세 — 첨부(hero/썸네일), 작품 삭제, 작업일지 등록,
 *                           미니 캘린더, 완성 전환 모달(CC-WRK-03)
 * 전제: 컨테이너 .detail-grid 가 data-artwork-id 를 들고 있다.
 * 백엔드 계약:
 *  - POST /file/doRetrieve.do {targetType:'ARTWORK', targetId} → data=List<FileVO>
 *  - POST /file/setRep.do     {fileId} → 200/400
 *  - POST /artwork/doDelete   {artworkId}      (PRG, 폼 전송)
 *  - POST /artworkEntry/doSave (PRG) → redirect:...&newEntryId=E
 * 댓글·좋아요·작업일지 타임라인은 각자 자동 마운트 컴포넌트가 담당한다.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const root = document.querySelector('.detail-grid[data-artwork-id]');
	if (!root) { return; }

	const ctx = document.body.dataset.ctx || '';
	const esc = (window.bitda && window.bitda.esc) || String;
	const artworkId = root.dataset.artworkId;

	/* ---------- 작품 첨부: hero + 썸네일 ---------- */
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

	/* 썸네일 클릭 → hero 교체 (AJAX 이후 생기므로 위임) */
	document.addEventListener('click', function (e) {
		const img = e.target.closest('#thumbArea img');
		if (!img) { return; }
		const heroImg = document.getElementById('heroImg');
		if (heroImg) { heroImg.setAttribute('src', ctx + '/file/download.do?fileId=' + img.dataset.fileId); }
	});

	/* ---------- 삭제(본인/관리자에게만 버튼이 렌더된다) ---------- */
	const btnArtworkDelete = document.getElementById('btnArtworkDelete');
	if (btnArtworkDelete) {
		btnArtworkDelete.addEventListener('click', function () {
			if (!confirm('작품을 삭제하시겠습니까? 첨부/댓글/좋아요/작업일지가 함께 삭제됩니다.')) { return; }
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

	/* ---------- 작업일지 등록: PRG + 지연 업로드 ----------
	   서버가 redirect 를 주므로 네이티브 XHR 의 responseURL 에서 newEntryId 를 캐낸다. */
	const entryRegForm = document.getElementById('entryRegForm');
	if (entryRegForm) {
		entryRegForm.addEventListener('submit', function (e) {
			e.preventDefault();
			const entryContent = document.getElementById('entryContent');
			if (!entryContent.value.trim()) { alert('작업 내용을 입력하세요.'); return; }
			const form = this;
			const nativeXhr = $.ajaxSettings.xhr();
			$.ajax({
				url: form.getAttribute('action'), method: 'POST', data: window.bitda.serializeForm(form),
				xhr: function () { return nativeXhr; }
			}).always(function () {
				const finalUrl = nativeXhr.responseURL || '';
				const m = finalUrl.match(/newEntryId=(\d+)/);
				if (!m) {
					alert('작업일지 등록 처리 중 오류가 발생했습니다.');
					return;
				}
				const up = window.bitda.uploader.get(form.querySelector('.upload-widget'));
				const job = up ? up.uploadTo('ARTWORK_ENTRY', m[1]) : null;
				if (job) { job.always(function () { location.href = finalUrl; }); }
				else { location.href = finalUrl; }
			});
		});
	}

	/* ---------- 미니 캘린더 ----------
	   타임라인의 reg_dt 를 모아 월 그리드를 그리고, 클릭하면 해당 일차로 스크롤한다. */
	const entryDates = {};
	document.querySelectorAll('.entry-item').forEach(function (item) {
		const d = String(item.dataset.regDt || '');
		if (d) { entryDates[d] = item.id; }
	});

	/* 기준 달 = 가장 최근 작업일지가 있는 달 (없으면 이번 달) */
	let calBase = new Date();
	const dateKeys = Object.keys(entryDates).sort();
	if (dateKeys.length) { calBase = new Date(dateKeys[dateKeys.length - 1] + 'T00:00:00'); }
	let calY = calBase.getFullYear(), calM = calBase.getMonth();

	function pad(n) { return (n < 10 ? '0' : '') + n; }

	function renderCal() {
		document.getElementById('calTitle').textContent = calY + '년 ' + (calM + 1) + '월';
		const first = new Date(calY, calM, 1).getDay();
		const days = new Date(calY, calM + 1, 0).getDate();
		let html = '';
		['일', '월', '화', '수', '목', '금', '토'].forEach(function (w) { html += '<span class="cal-dow">' + w + '</span>'; });
		for (let i = 0; i < first; i++) { html += '<span class="cal-day empty">.</span>'; }
		for (let d = 1; d <= days; d++) {
			const key = calY + '-' + pad(calM + 1) + '-' + pad(d);
			html += '<span class="cal-day' + (entryDates[key] ? ' has-entry' : '') + '" data-key="' + key + '">' + d + '</span>';
		}
		document.getElementById('calGrid').innerHTML = html;
	}
	renderCal();

	const calPrev = document.getElementById('calPrev');
	if (calPrev) { calPrev.addEventListener('click', function () { calM--; if (calM < 0) { calM = 11; calY--; } renderCal(); }); }
	const calNext = document.getElementById('calNext');
	if (calNext) { calNext.addEventListener('click', function () { calM++; if (calM > 11) { calM = 0; calY++; } renderCal(); }); }

	/* 날짜 칸은 renderCal 이 다시 그리므로 위임 */
	document.addEventListener('click', function (e) {
		const cell = e.target.closest('.cal-day.has-entry');
		if (!cell) { return; }
		const id = entryDates[cell.dataset.key];
		if (id) { document.getElementById(id).scrollIntoView({ behavior: 'smooth' }); }
	});

	/* ---------- CC-WRK-03 완성 전환 모달 ----------
	   후보 사진(ARTWORK)을 보여준다.
	   - 드래그앤드롭으로 순서(1~9)를 바꾼다. 1번이 곧 대표(완성 게시판 썸네일)다.
	   - 사진을 그냥 클릭해도 곧바로 대표로 지정된다(빠른 경로).
	   HTML5 기본 Drag&Drop API 만 쓴다(외부 라이브러리 없음). PC 마우스 기준. */
	function loadCompletePhotos() {
		$.post(ctx + '/file/doRetrieve.do', { targetType: 'ARTWORK', targetId: artworkId }, function (res) {
			if (res.code !== '200') { return; }
			const files = res.data || [];
			const grid = document.getElementById('completePhotoGrid');
			if (!grid) { return; }
			if (!files.length) {
				grid.innerHTML = '<p class="hint">등록된 작품 사진이 없습니다. 사진 없이도 완성 등록은 가능합니다.</p>';
				return;
			}
			let html = '';
			files.forEach(function (f, i) {
				html += '<div class="photo-slot' + (f.isRep === 'Y' ? ' rep' : '') + '" data-file-id="' + f.fileId + '" draggable="true">'
					+ '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" alt="' + esc(f.orgFileNm) + '">'
					+ '<span class="slot-order">' + (i + 1) + '</span>'
					+ (f.isRep === 'Y' ? '<span class="rep-mark">대표</span>' : '')
					+ '</div>';
			});
			grid.innerHTML = html;
		}, 'json');
	}

	const btnOpenComplete = document.getElementById('btnOpenComplete');
	if (btnOpenComplete) {
		btnOpenComplete.addEventListener('click', function () {
			loadCompletePhotos();
			const completeModal = document.getElementById('completeModal');
			if (completeModal) { completeModal.classList.add('open'); }
		});
	}

	/* ---------- 완성 전환 모달: 드래그앤드롭 순서 변경 ----------
	   그리드는 AJAX 로 다시 그려지므로 document 에 위임한다. */
	let dragSlot = null;

	function clearSlotMarks() {
		document.querySelectorAll('#completePhotoGrid .drop-before, #completePhotoGrid .drop-after')
			.forEach(function (el) { el.classList.remove('drop-before', 'drop-after'); });
	}

	/* 포인터가 대상 카드의 왼쪽 절반이면 그 앞에, 오른쪽 절반이면 그 뒤에 끼운다 */
	function slotDropBefore(slot, clientX) {
		const r = slot.getBoundingClientRect();
		return (clientX - r.left) < (r.width / 2);
	}

	/* 끌던 사진이 아직 화면에 살아 있을 때만 true.
	   그리드를 다시 그리면 원래 노드가 사라지는데, 그때 dragSlot 이 죽은 노드를 가리킨 채 남으면
	   나중에 다른 드래그(예: 탐색기에서 파일 끌어오기)에 그 죽은 노드가 되살아나 붙어버린다. */
	function slotAlive() {
		if (!dragSlot) { return false; }
		if (!dragSlot.isConnected) { dragSlot = null; return false; }
		return true;
	}

	document.addEventListener('dragstart', function (e) {
		const slot = e.target.closest('#completePhotoGrid .photo-slot');
		if (!slot) { return; }
		dragSlot = slot;
		slot.classList.add('drag-src');
		e.dataTransfer.effectAllowed = 'move';
		/* 데이터가 비어 있으면 파이어폭스는 드래그를 시작조차 하지 않는다 */
		e.dataTransfer.setData('text/plain', '');
	});

	document.addEventListener('dragover', function (e) {
		if (!slotAlive()) { return; }
		const slot = e.target.closest('#completePhotoGrid .photo-slot');
		if (!slot || slot === dragSlot) { return; }
		e.preventDefault();                       /* 이걸 해야 drop 이 발생한다 */
		e.dataTransfer.dropEffect = 'move';
		clearSlotMarks();
		slot.classList.add(slotDropBefore(slot, e.clientX) ? 'drop-before' : 'drop-after');
	});

	document.addEventListener('drop', function (e) {
		if (!slotAlive()) { return; }
		const slot = e.target.closest('#completePhotoGrid .photo-slot');
		if (!slot || slot === dragSlot) { return; }
		e.preventDefault();
		const before = slotDropBefore(slot, e.clientX);
		slot.parentNode.insertBefore(dragSlot, before ? slot : slot.nextSibling);
		clearSlotMarks();
		commitPhotoOrder();
	});

	document.addEventListener('dragend', function () {
		if (dragSlot) { dragSlot.classList.remove('drag-src'); }
		dragSlot = null;
		clearSlotMarks();
	});

	/* 바뀐 DOM 순서를 서버에 저장한다. 맨 앞이 대표가 된다. */
	function commitPhotoOrder() {
		const ids = [];
		document.querySelectorAll('#completePhotoGrid .photo-slot').forEach(function (el) {
			ids.push(el.dataset.fileId);
		});
		if (!ids.length) { return; }
		$.post(ctx + '/file/reorder.do',
			{ targetType: 'ARTWORK', targetId: artworkId, fileIds: ids.join(',') },
			function (res) {
				if (res.code !== '200') { alert(res.message || '순서 변경에 실패했습니다.'); }
				loadCompletePhotos();             /* 성공이든 실패든 서버 기준으로 되돌려 맞춘다 */
			}, 'json')
			.fail(function () {
				/* 통신 자체가 실패하면 화면만 새 순서로 보이고 DB 는 옛 순서로 남는다.
				   조용히 어긋나게 두지 말고 반드시 알린다.
				   (되돌리기도 같은 네트워크를 타므로, 통신이 완전히 죽었으면 실패할 수 있다) */
				alert('순서 변경 처리 중 오류가 발생했습니다.');
				loadCompletePhotos();
			});
	}

	/* 대표가 아닌 사진을 클릭하면 대표로 지정 (빠른 경로. 드래그와 결과는 같다) */
	document.addEventListener('click', function (e) {
		const slot = e.target.closest('#completePhotoGrid .photo-slot:not(.rep)');
		if (!slot) { return; }
		$.post(ctx + '/file/setRep.do', { fileId: slot.dataset.fileId }, function (res) {
			if (res.code === '200') { loadCompletePhotos(); }
			else { alert(res.message || '대표 지정에 실패했습니다.'); }
		}, 'json');
	});
});
