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
	var esc = window.bitda.esc;

	var catTbody = document.getElementById('catTbody');
	if (!catTbody) { return; }

	/* 목록 조회 후 표 전체를 다시 그린다 */
	function load() {
		window.bitda.requestAjax({
			url: ctx + '/category/doRetrieve.do',
			type: 'GET',
			resFunction: function (res) {
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
			}
		});
	}

	/* 추가 */
	var btnCatAdd = document.getElementById('btnCatAdd');
	if (btnCatAdd) {
		btnCatAdd.addEventListener('click', function () {
			var input = document.getElementById('catNewInput');
			if (window.bitda.isEmpty(input, '카테고리 이름을 입력하세요.')) { return; }
			window.bitda.requestAjax({
				url: ctx + '/category/doSave.do',
				data: { categoryNm: input.value.trim() },
				resFunction: function (res) {
					if (res.code === '200') {
						input.value = '';
						load();
					} else {
						alert(res.message || '카테고리 추가에 실패했습니다.');
					}
				}
			});
		});
	}

	/* 수정 (행 위임) */
	catTbody.addEventListener('click', function (e) {
		var editBtn = e.target.closest('.cat-edit');
		if (!editBtn) { return; }
		var tr = editBtn.closest('tr');
		var next = prompt('카테고리 이름을 수정하세요 (10자 이내)', tr.dataset.categoryNm);
		if (next == null) { return; }
		next = next.trim();
		if (!next) { alert('카테고리 이름을 입력하세요.'); return; }
		window.bitda.requestAjax({
			url: ctx + '/category/doUpdate.do',
			data: { categoryId: tr.dataset.categoryId, categoryNm: next },
			resFunction: function (res) {
				if (res.code === '200') { load(); }
				else { alert(res.message || '카테고리 수정에 실패했습니다.'); }
			}
		});
	});

	/* 삭제 (행 위임) */
	catTbody.addEventListener('click', function (e) {
		var delBtn = e.target.closest('.cat-del');
		if (!delBtn) { return; }
		var tr = delBtn.closest('tr');
		if (!confirm('"' + tr.dataset.categoryNm + '" 카테고리를 삭제하시겠습니까?')) { return; }
		window.bitda.requestAjax({
			url: ctx + '/category/doDelete.do',
			data: { categoryId: tr.dataset.categoryId },
			resFunction: function (res) {
				if (res.code === '200') { load(); }
				else { alert(res.message || '카테고리 삭제에 실패했습니다.'); }
			}
		});
	});

	load();
});
