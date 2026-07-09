/* ============================================================================
 * CC-FIL-01 이미지 업로드 부분 컴포넌트 — .upload-widget 자동 마운트
 * 백엔드 계약(FileController, MessageVO):
 *  - POST /file/upload.do (multipart, 파라미터명 정확히 "files") +targetType+targetId → 200/400/401/500
 *  - POST /file/doRetrieve.do {targetType,targetId} → data=List<FileVO> (sort_no 순)
 *  - POST /file/setRep.do {fileId} → 200/400 (1번 슬롯 교환)
 *  - POST /file/remove.do {fileId} → 200/400/500 (슬롯 재정렬)
 *  - GET  /file/download.do?fileId=N (이미지 src 겸용)
 * 모드:
 *  - 즉시(targetId 있음): 선택 즉시 업로드, 목록은 서버 기준
 *  - 지연(targetId 없음): File 객체 보관+로컬 미리보기, bitda.uploader.uploadTo(...)로 일괄 업로드
 * ==========================================================================*/
$(function () {
	'use strict';

	var ctx = $('body').data('ctx') || '';
	var esc = (window.bitda && window.bitda.esc) || function (s) { return String(s == null ? '' : s); };
	var MAX = 9;
	var ALLOWED = /\.(jpe?g|png|webp)$/i;

	window.bitda = window.bitda || {};
	window.bitda.uploader = window.bitda.uploader || {};

	function mount($w) {
		var targetType = $w.data('target-type');
		var targetId = $w.data('target-id');       /* ''(지연) 또는 숫자(즉시) */
		var editable = String($w.data('editable')) === 'true';
		var deferred = (targetId === '' || targetId === undefined || targetId === null);
		var pending = [];                          /* 지연 모드 File 보관 */

		if (editable) {
			$w.find('.up-pick-label').show();
			$w.find('.up-hint').show();
		}

		function setCount(n) { $w.find('.up-count').text('(' + n + '/' + MAX + ')'); }

		/* ---------- 즉시 모드: 서버 목록 렌더 ---------- */
		function loadServer() {
			$.post(ctx + '/file/doRetrieve.do', { targetType: targetType, targetId: targetId }, function (res) {
				if (res.code !== '200') { return; }
				var list = res.data || [];
				setCount(list.length);
				var html = '';
				for (var i = 0; i < list.length; i++) {
					var f = list[i];
					var rep = (f.isRep === 'Y');
					html += '<div class="up-item' + (rep ? ' rep' : '') + '" data-file-id="' + f.fileId + '">';
					html += '<img src="' + ctx + '/file/download.do?fileId=' + f.fileId + '" alt="' + esc(f.orgFileNm) + '">';
					if (rep) { html += '<span class="up-rep-badge">대표</span>'; }
					if (editable) {
						html += '<div class="up-item-btns">';
						if (!rep) { html += '<a class="up-setrep">대표선택</a>'; }
						html += '<a class="up-remove">삭제</a></div>';
					}
					html += '</div>';
				}
				$w.find('.up-grid').html(html || '<p class="hint">등록된 이미지가 없습니다.</p>');
			}, 'json');
		}

		/* ---------- 지연 모드: 로컬 미리보기 렌더 ---------- */
		function renderPending() {
			setCount(pending.length);
			var html = '';
			for (var i = 0; i < pending.length; i++) {
				html += '<div class="up-item' + (i === 0 ? ' rep' : '') + '" data-idx="' + i + '">';
				html += '<img src="' + pending[i].url + '" alt="' + esc(pending[i].file.name) + '">';
				if (i === 0) { html += '<span class="up-rep-badge">대표</span>'; }
				html += '<div class="up-item-btns"><a class="up-remove-local">삭제</a></div>';
				html += '</div>';
			}
			$w.find('.up-grid').html(html || '<p class="hint">선택된 이미지가 없습니다.</p>');
		}

		function currentCount() { return deferred ? pending.length : $w.find('.up-item').length; }

		function validateFiles(files, remain) {
			if (files.length > remain) {
				alert('이미지는 최대 ' + MAX + '장까지 등록할 수 있습니다. (남은 슬롯 ' + remain + '장)');
				return false;
			}
			for (var i = 0; i < files.length; i++) {
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
		$w.on('change', '.up-input', function () {
			var files = Array.prototype.slice.call(this.files || []);
			this.value = '';
			if (!files.length) { return; }
			if (!validateFiles(files, MAX - currentCount())) { return; }

			if (deferred) {
				for (var i = 0; i < files.length; i++) {
					pending.push({ file: files[i], url: URL.createObjectURL(files[i]) });
				}
				renderPending();
			} else {
				uploadNow(files, targetType, targetId, function () { loadServer(); });
			}
		});

		function uploadNow(files, tType, tId, done) {
			var fd = new FormData();
			for (var i = 0; i < files.length; i++) { fd.append('files', files[i]); }
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
		$w.on('click', '.up-setrep', function () {
			var fileId = $(this).closest('.up-item').data('file-id');
			$.post(ctx + '/file/setRep.do', { fileId: fileId }, function (res) {
				if (res.code === '200') { loadServer(); }
				else { alert(res.message || '대표 지정에 실패했습니다.'); }
			}, 'json').fail(function () { alert('요청 처리 중 오류가 발생했습니다.'); });
		});
		$w.on('click', '.up-remove', function () {
			if (!confirm('이미지를 삭제하시겠습니까?')) { return; }
			var fileId = $(this).closest('.up-item').data('file-id');
			$.post(ctx + '/file/remove.do', { fileId: fileId }, function (res) {
				if (res.code === '200') { loadServer(); }
				else { alert(res.message || '삭제에 실패했습니다.'); }
			}, 'json').fail(function () { alert('요청 처리 중 오류가 발생했습니다.'); });
		});

		/* ---------- 지연 모드: 로컬 삭제 ---------- */
		$w.on('click', '.up-remove-local', function () {
			var idx = $(this).closest('.up-item').data('idx');
			URL.revokeObjectURL(pending[idx].url);
			pending.splice(idx, 1);
			renderPending();
		});

		/* ---------- 외부 API (3단계 글쓰기 오케스트레이션) ---------- */
		$w.data('bitdaUploader', {
			count: currentCount,
			pendingCount: function () { return pending.length; },
			/* 지연 보관분을 지정 대상으로 일괄 업로드. jqXHR(Promise) 반환, 보관분 없으면 null */
			uploadTo: function (tType, tId) {
				if (!pending.length) { return null; }
				var files = pending.map(function (p) { return p.file; });
				return uploadNow(files, tType, tId, function () {
					pending.forEach(function (p) { URL.revokeObjectURL(p.url); });
					pending = [];
				});
			}
		});

		/* 초기 로드 */
		if (deferred) { renderPending(); } else { loadServer(); }
	}

	window.bitda.uploader.get = function (el) { return $(el).data('bitdaUploader'); };

	$('.upload-widget').each(function () { mount($(this)); });
});
