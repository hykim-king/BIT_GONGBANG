/* ============================================================================
 * CC-ADM-04 카테고리 관리 — 목록 조회 + 추가/수정/삭제
 * 백엔드 계약:
 *  - GET  /category/doRetrieve.do            → data=List<CategoryVO>
 *  - POST /category/doSave.do   {categoryNm} → 200/400
 *  - POST /category/doUpdate.do {categoryId, categoryNm} → 200/400
 *  - POST /category/doDelete.do {categoryId} → 200/400 (작품이 참조 중이면 400)
 *  관리자 판정은 CategoryController 메서드 내부에서 수행한다(AdminInterceptor 대상 아님).
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	var ctx = document.body.dataset.ctx || '';
	var esc = (window.bitda && window.bitda.esc) || function (s) { return String(s == null ? '' : s); };

	var catTbody = document.getElementById('catTbody');
	if (!catTbody) { return; }

	/* 목록 조회 후 표 전체를 다시 그린다 */
	function load() {
		$.get(ctx + '/category/doRetrieve.do', function (res) {
			if (res.code !== '200') { return; }
			var list = res.data || [];
			var html = '';
			for (var i = 0; i < list.length; i++) {
				var cItem = list[i];
				html += '<tr data-category-id="' + cItem.categoryId + '" data-category-nm="' + esc(cItem.categoryNm) + '">';
				html += '<td>' + (i + 1) + '</td>';
				html += '<td class="cat-nm">' + esc(cItem.categoryNm) + '</td>';
				html += '<td>' + esc(cItem.regDt || '-') + '</td>';
				html += '<td>' + esc(cItem.modDt || '-') + '</td>';
				html += '<td><div class="cat-row-actions">'
					+ '<button type="button" class="btn ghost small cat-edit">수정</button>'
					+ '<button type="button" class="btn danger small cat-del">삭제</button>'
					+ '</div></td>';
				html += '</tr>';
			}
			catTbody.innerHTML = html || '<tr><td colspan="5" class="empty-state">등록된 카테고리가 없습니다.</td></tr>';
		}, 'json');
	}

	/* 추가 */
	var btnCatAdd = document.getElementById('btnCatAdd');
	if (btnCatAdd) {
		btnCatAdd.addEventListener('click', function () {
			var categoryNm = document.getElementById('catNewInput').value.trim();
			if (!categoryNm) { alert('카테고리 이름을 입력하세요.'); return; }
			$.post(ctx + '/category/doSave.do', { categoryNm: categoryNm }, function (res) {
				if (res.code === '200') {
					document.getElementById('catNewInput').value = '';
					load();
				} else {
					alert(res.message || '카테고리 추가에 실패했습니다.');
				}
			}, 'json').fail(function () { alert('요청 처리 중 오류가 발생했습니다.'); });
		});
	}

	/* 수정 (행 위임) */
	catTbody.addEventListener('click', function (e) {
		var editBtn = e.target.closest('.cat-edit');
		if (!editBtn) { return; }
		var tr = editBtn.closest('tr');
		var cur = tr.dataset.categoryNm;
		var next = prompt('카테고리 이름을 수정하세요 (10자 이내)', cur);
		if (next == null) { return; }
		next = next.trim();
		if (!next) { alert('카테고리 이름을 입력하세요.'); return; }
		$.post(ctx + '/category/doUpdate.do', { categoryId: tr.dataset.categoryId, categoryNm: next }, function (res) {
			if (res.code === '200') { load(); }
			else { alert(res.message || '카테고리 수정에 실패했습니다.'); }
		}, 'json').fail(function () { alert('요청 처리 중 오류가 발생했습니다.'); });
	});

	/* 삭제 (행 위임) */
	catTbody.addEventListener('click', function (e) {
		var delBtn = e.target.closest('.cat-del');
		if (!delBtn) { return; }
		var tr = delBtn.closest('tr');
		if (!confirm('"' + tr.dataset.categoryNm + '" 카테고리를 삭제하시겠습니까?')) { return; }
		$.post(ctx + '/category/doDelete.do', { categoryId: tr.dataset.categoryId }, function (res) {
			if (res.code === '200') { load(); }
			else { alert(res.message || '카테고리 삭제에 실패했습니다.'); }
		}, 'json').fail(function () { alert('요청 처리 중 오류가 발생했습니다.'); });
	});

	load();
});
