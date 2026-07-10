<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  CC-CAT-01 관리자-카테고리 관리 — 목록 + 추가/수정/삭제.
  화면 진입: GET /admin/category.do (AdminInterceptor). CRUD 는 기존 /category AJAX 재사용:
   - GET  /category/doRetrieve.do          → {code, data:[CategoryVO]}
   - GET  /category/doSelectOne.do?categoryId=N
   - POST /category/doSave.do   {categoryNm}
   - POST /category/doUpdate.do {categoryId, categoryNm}
   - POST /category/doDelete.do {categoryId} — 작품 참조중이면 400("작품이 등록된 카테고리...")
--%>
<c:set var="pageTitle" value="카테고리 관리 · 빚다 관리자"/>
<c:set var="adminActiveMenu" value="category"/>
<%@ include file="/WEB-INF/views/cmn/admin_header.jsp" %>

<div class="section-head"><h2>카테고리 관리</h2><p>작품 등록 시 사용되는 카테고리를 추가·수정·삭제합니다.</p></div>

<div class="cat-add-row">
	<input type="text" class="text-input" id="catNewInput" maxlength="10" placeholder="새 카테고리 이름 (10자 이내)">
	<button type="button" class="btn" id="btnCatAdd">+ 추가</button>
</div>

<div class="table-scroll">
	<table>
		<thead><tr><th style="width:60px;">번호</th><th>카테고리명</th><th>등록일</th><th>수정일</th><th style="width:130px;">관리</th></tr></thead>
		<tbody id="catTbody"></tbody>
	</table>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
	var ctx = document.body.dataset.ctx || '';
	var esc = (window.bitda && window.bitda.esc) || function(s){ return String(s == null ? '' : s); };

	function load() {
		$.get(ctx + '/category/doRetrieve.do', function(res) {
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
			document.getElementById('catTbody').innerHTML = html || '<tr><td colspan="5" class="empty-state">등록된 카테고리가 없습니다.</td></tr>';
		}, 'json');
	}

	var btnCatAdd = document.getElementById('btnCatAdd');
	if (btnCatAdd) {
		btnCatAdd.addEventListener('click', function() {
			var categoryNm = document.getElementById('catNewInput').value.trim();
			if (!categoryNm) { alert('카테고리 이름을 입력하세요.'); return; }
			$.post(ctx + '/category/doSave.do', { categoryNm: categoryNm }, function(res) {
				if (res.code === '200') {
					document.getElementById('catNewInput').value = '';
					load();
				} else {
					alert(res.message || '카테고리 추가에 실패했습니다.');
				}
			}, 'json').fail(function() { alert('요청 처리 중 오류가 발생했습니다.'); });
		});
	}

	var catTbody = document.getElementById('catTbody');

	catTbody.addEventListener('click', function(e) {
		var editBtn = e.target.closest('.cat-edit');
		if (!editBtn) { return; }
		var tr = editBtn.closest('tr');
		var cur = tr.dataset.categoryNm;
		var next = prompt('카테고리 이름을 수정하세요 (10자 이내)', cur);
		if (next == null) { return; }
		next = next.trim();
		if (!next) { alert('카테고리 이름을 입력하세요.'); return; }
		$.post(ctx + '/category/doUpdate.do', { categoryId: tr.dataset.categoryId, categoryNm: next }, function(res) {
			if (res.code === '200') { load(); }
			else { alert(res.message || '카테고리 수정에 실패했습니다.'); }
		}, 'json').fail(function() { alert('요청 처리 중 오류가 발생했습니다.'); });
	});

	catTbody.addEventListener('click', function(e) {
		var delBtn = e.target.closest('.cat-del');
		if (!delBtn) { return; }
		var tr = delBtn.closest('tr');
		if (!confirm('"' + tr.dataset.categoryNm + '" 카테고리를 삭제하시겠습니까?')) { return; }
		$.post(ctx + '/category/doDelete.do', { categoryId: tr.dataset.categoryId }, function(res) {
			if (res.code === '200') { load(); }
			else { alert(res.message || '카테고리 삭제에 실패했습니다.'); }
		}, 'json').fail(function() { alert('요청 처리 중 오류가 발생했습니다.'); });
	});

	load();
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
