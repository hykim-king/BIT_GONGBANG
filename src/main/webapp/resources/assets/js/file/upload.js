/* ============================================================================
 * CC-FIL-01 이미지 업로드 부분 컴포넌트 — .upload-widget 자동 마운트
 * 백엔드 계약(FileController, MessageVO):
 *  - POST /file/upload.do (multipart, 파라미터명 정확히 "files") +targetType+targetId → 200/400/401/500
 *  - POST /file/doRetrieve.do {targetType,targetId} → data=List<FileVO> (sort_no 순)
 *  - POST /file/setRep.do {fileId} → 200/400 (1번 슬롯 교환)
 *  - POST /file/reorder.do {targetType,targetId,fileIds:"12,7,9"} → 200/400/401 (순서 일괄 재지정)
 *  - POST /file/remove.do {fileId} → 200/400/500 (슬롯 재정렬)
 *  - GET  /file/download.do?fileId=N (이미지 src 겸용)
 * 모드:
 *  - 즉시(targetId 있음): 선택 즉시 업로드, 목록은 서버 기준
 *  - 지연(targetId 없음): File 객체 보관+로컬 미리보기, bitda.uploader.uploadTo(...)로 일괄 업로드
 * 순서:
 *  - 드래그앤드롭으로 1~9번 슬롯을 재배치한다. 1번 슬롯이 곧 대표(썸네일)다.
 *  - HTML5 기본 Drag&Drop API 만 쓴다(외부 라이브러리 없음). PC 마우스 기준.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const ctx = document.body.dataset.ctx || '';
	const esc = (window.bitda && window.bitda.esc) || function (s) { return String(s == null ? '' : s); };
	const MAX = 9;
	const ALLOWED = /\.(jpe?g|png|webp)$/i;

	window.bitda = window.bitda || {};
	window.bitda.uploader = window.bitda.uploader || {};

	function mount(w) {
		const targetType = w.dataset.targetType;
		const targetId = w.dataset.targetId;       /* ''(지연) 또는 숫자(즉시) */
		const editable = String(w.dataset.editable) === 'true';
		const deferred = (targetId === '' || targetId === undefined || targetId === null);
		let pending = [];                          /* 지연 모드 File 보관 */
		let dragEl = null;                         /* 지금 끌고 있는 .up-item */

		if (editable) {
			w.querySelector('.up-pick-label').style.display = '';
			w.querySelector('.up-hint').style.display = '';
		}

		function setCount(n) { w.querySelector('.up-count').textContent = '(' + n + '/' + MAX + ')'; }

		/* 슬롯 번호(우상단) — 지금 몇 번째인지 항상 보이게 */
		function orderBadge(i) { return '<span class="slot-order">' + (i + 1) + '</span>'; }

		/* 편집 가능할 때만 끌 수 있다 */
		function dragAttr() { return editable ? ' draggable="true"' : ''; }

		/* ---------- 즉시 모드: 서버 목록 렌더 ---------- */
		function loadServer() {
			$.post(ctx + '/file/doRetrieve.do', { targetType: targetType, targetId: targetId }, function (res) {
				if (res.code !== '200') { return; }
				const list = res.data || [];
				setCount(list.length);
				let html = '';
				for (let i = 0; i < list.length; i++) {
					const f = list[i];
					const rep = (f.isRep === 'Y');
					html += '<div class="up-item' + (rep ? ' rep' : '') + '" data-file-id="' + f.fileId + '"' + dragAttr() + '>';
					html += '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" alt="' + esc(f.orgFileNm) + '">';
					html += orderBadge(i);
					if (rep) { html += '<span class="up-rep-badge">대표</span>'; }
					if (editable) {
						html += '<div class="up-item-btns">';
						if (!rep) { html += '<a class="up-setrep">대표선택</a>'; }
						html += '<a class="up-remove">삭제</a></div>';
					}
					html += '</div>';
				}
				w.querySelector('.up-grid').innerHTML = html || '<p class="hint">등록된 이미지가 없습니다.</p>';
			}, 'json');
		}

		/* ---------- 지연 모드: 로컬 미리보기 렌더 ---------- */
		function renderPending() {
			setCount(pending.length);
			let html = '';
			for (let i = 0; i < pending.length; i++) {
				html += '<div class="up-item' + (i === 0 ? ' rep' : '') + '" data-idx="' + i + '"' + dragAttr() + '>';
				html += '<img src="' + pending[i].url + '" alt="' + esc(pending[i].file.name) + '">';
				html += orderBadge(i);
				if (i === 0) { html += '<span class="up-rep-badge">대표</span>'; }
				html += '<div class="up-item-btns"><a class="up-remove-local">삭제</a></div>';
				html += '</div>';
			}
			w.querySelector('.up-grid').innerHTML = html || '<p class="hint">선택된 이미지가 없습니다.</p>';
		}

		function currentCount() { return deferred ? pending.length : w.querySelectorAll('.up-item').length; }

		function validateFiles(files, remain) {
			if (files.length > remain) {
				alert('이미지는 최대 ' + MAX + '장까지 등록할 수 있습니다. (남은 슬롯 ' + remain + '장)');
				return false;
			}
			for (let i = 0; i < files.length; i++) {
				if (!ALLOWED.test(files[i].name)) {
					alert('jpg/jpeg/png/webp 형식만 업로드할 수 있습니다: ' + files[i].name);
					return false;
				}
				if (files[i].size > 5 * 1024 * 1024) {
					alert('파일당 최대 5MB까지 업로드할 수 있습니다: ' + files[i].name);
					return false;
				}
			}
			return true;
		}

		/* ---------- 파일 선택 ---------- */
		w.addEventListener('change', function (e) {
			const t = e.target.closest('.up-input');
			if (!t) { return; }
			const files = Array.prototype.slice.call(t.files || []);
			t.value = '';
			if (!files.length) { return; }
			if (!validateFiles(files, MAX - currentCount())) { return; }

			if (deferred) {
				for (let i = 0; i < files.length; i++) {
					pending.push({ file: files[i], url: URL.createObjectURL(files[i]) });
				}
				renderPending();
			} else {
				uploadNow(files, targetType, targetId, function () { loadServer(); });
			}
		});

		function uploadNow(files, tType, tId, done) {
			const fd = new FormData();
			for (let i = 0; i < files.length; i++) { fd.append('files', files[i]); }
			fd.append('targetType', tType);
			fd.append('targetId', tId);
			return $.ajax({
				url: ctx + '/file/upload.do', method: 'POST', data: fd,
				processData: false, contentType: false, dataType: 'json'
			}).done(function (res) {
				if (res.code === '200') {
					if (done) { done(res); }
				} else if (res.code === '401') {
					alert('로그인이 필요합니다.');
				} else {
					alert(res.message || '업로드에 실패했습니다.');
				}
			}).fail(function () {
				alert('업로드 처리 중 오류가 발생했습니다.');
			});
		}

		/* ---------- 즉시 모드: 대표지정/삭제 ---------- */
		w.addEventListener('click', function (e) {
			const t = e.target.closest('.up-setrep');
			if (!t) { return; }
			const fileId = t.closest('.up-item').dataset.fileId;
			window.bitda.requestAjax({
				url: ctx + '/file/setRep.do',
				data: { fileId: fileId },
				resFunction: function (res) {
					if (res.code === '200') { loadServer(); }
					else { alert(res.message || '대표 지정에 실패했습니다.'); }
				}
			});
		});
		w.addEventListener('click', function (e) {
			const t = e.target.closest('.up-remove');
			if (!t) { return; }
			if (!confirm('이미지를 삭제하시겠습니까?')) { return; }
			const fileId = t.closest('.up-item').dataset.fileId;
			window.bitda.requestAjax({
				url: ctx + '/file/remove.do',
				data: { fileId: fileId },
				resFunction: function (res) {
					if (res.code === '200') { loadServer(); }
					else { alert(res.message || '삭제에 실패했습니다.'); }
				}
			});
		});

		/* ---------- 지연 모드: 로컬 삭제 ---------- */
		w.addEventListener('click', function (e) {
			const t = e.target.closest('.up-remove-local');
			if (!t) { return; }
			const idx = Number(t.closest('.up-item').dataset.idx);
			URL.revokeObjectURL(pending[idx].url);
			pending.splice(idx, 1);
			renderPending();
		});

		/* ---------- 드래그앤드롭 순서 변경 ----------
		   그리드는 AJAX 로 다시 그려지므로 위젯에 이벤트를 걸어 위임한다. */
		function clearDropMarks() {
			w.querySelectorAll('.drop-before, .drop-after').forEach(function (el) {
				el.classList.remove('drop-before', 'drop-after');
			});
		}

		/* 포인터가 대상 카드의 왼쪽 절반이면 그 앞에, 오른쪽 절반이면 그 뒤에 끼운다 */
		function dropBefore(item, clientX) {
			const r = item.getBoundingClientRect();
			return (clientX - r.left) < (r.width / 2);
		}

		w.addEventListener('dragstart', function (e) {
			if (!editable) { return; }
			const item = e.target.closest('.up-item');
			if (!item) { return; }
			dragEl = item;
			item.classList.add('drag-src');
			e.dataTransfer.effectAllowed = 'move';
			/* 데이터가 비어 있으면 파이어폭스는 드래그를 시작조차 하지 않는다 */
			e.dataTransfer.setData('text/plain', '');
		});

		/* 지금 끌고 있는 타일이 아직 이 위젯 안에 살아 있을 때만 true.
		   그리드를 다시 그리면 원래 타일이 사라지는데, 그때 dragEl 이 죽은 노드를 가리킨 채 남으면
		   나중에 (예: 탐색기에서 파일을 끌어다 놓을 때) 그 죽은 타일이 되살아나 붙어버린다. */
		function dragAlive() {
			if (!dragEl) { return false; }
			if (!w.contains(dragEl)) { dragEl = null; return false; }
			return true;
		}

		w.addEventListener('dragover', function (e) {
			if (!dragAlive()) { return; }
			const item = e.target.closest('.up-item');
			if (!item || item === dragEl) { return; }
			e.preventDefault();                     /* 이걸 해야 drop 이 발생한다 */
			e.dataTransfer.dropEffect = 'move';
			clearDropMarks();
			item.classList.add(dropBefore(item, e.clientX) ? 'drop-before' : 'drop-after');
		});

		w.addEventListener('drop', function (e) {
			if (!dragAlive()) { return; }
			const item = e.target.closest('.up-item');
			if (!item || item === dragEl) { return; }
			e.preventDefault();
			const before = dropBefore(item, e.clientX);
			item.parentNode.insertBefore(dragEl, before ? item : item.nextSibling);
			clearDropMarks();
			commitOrder();
		});

		w.addEventListener('dragend', function () {
			if (dragEl) { dragEl.classList.remove('drag-src'); }
			dragEl = null;
			clearDropMarks();
		});

		/* 바뀐 DOM 순서를 확정한다. 맨 앞이 대표가 된다. */
		function commitOrder() {
			const items = w.querySelectorAll('.up-item');

			if (deferred) {
				/* 지연 모드: 서버에 아직 파일이 없다. 보관 배열만 DOM 순서대로 재배치. */
				const reordered = [];
				items.forEach(function (el) { reordered.push(pending[Number(el.dataset.idx)]); });
				pending = reordered;
				/* renderPending() 이 그리드를 통째로 다시 그리므로 지금 끌던 타일이 여기서 사라진다.
				   그러면 그 타일에 실릴 dragend 가 위젯까지 올라오지 못해 dragEl 이 그대로 남는다.
				   다시 그리기 전에 먼저 끊어준다. */
				dragEl = null;
				renderPending();
				return;
			}

			/* 즉시 모드: 서버에 새 순서를 저장하고 서버 기준으로 다시 그린다. */
			const ids = [];
			items.forEach(function (el) { ids.push(el.dataset.fileId); });
			window.bitda.requestAjax({
				url: ctx + '/file/reorder.do',
				data: { targetType: targetType, targetId: targetId, fileIds: ids.join(',') },
				resFunction: function (res) {
					if (res.code !== '200') { alert(res.message || '순서 변경에 실패했습니다.'); }
					loadServer();               /* 성공이든 실패든 서버 기준으로 다시 그린다 */
				},
				/* 통신 자체가 실패하면 requestAjax 는 resFunction 을 부르지 않는다.
				   그대로 두면 화면만 새 순서로 보이고 DB 는 옛 순서로 남아 조용히 어긋난다. */
				failFunction: function () {
					alert('순서 변경 처리 중 오류가 발생했습니다.');
					loadServer();
				}
			});
		}

		/* ---------- 외부 API (3단계 글쓰기 오케스트레이션) ---------- */
		w.bitdaUploaderApi = {
			count: currentCount,
			pendingCount: function () { return pending.length; },
			/* 지연 보관분을 지정 대상으로 일괄 업로드. jqXHR(Promise) 반환, 보관분 없으면 null */
			uploadTo: function (tType, tId) {
				if (!pending.length) { return null; }
				const files = pending.map(function (p) { return p.file; });
				return uploadNow(files, tType, tId, function () {
					pending.forEach(function (p) { URL.revokeObjectURL(p.url); });
					pending = [];
				});
			}
		};

		/* 초기 로드 */
		if (deferred) { renderPending(); } else { loadServer(); }
	}

	window.bitda.uploader.get = function (el) { return el.bitdaUploaderApi; };

	document.querySelectorAll('.upload-widget').forEach(function (el) { mount(el); });
});
