<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  CC-ART-02 작품 수정 — 완성/공개 공용(POST /artwork/doUpdate 가 is_status 기준 분기 redirect).
  제목/카테고리/본문 수정 + 기존 이미지 관리(즉시 모드 업로드 위젯: 추가/대표교환/삭제).
--%>
<c:set var="pageTitle" value="작품 수정 · 빚다"/>
<c:set var="activeMenu" value="${vo.isStatus eq 'Y' ? 'complete' : 'working'}"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<div class="page-narrow" style="max-width:680px;">
	<div class="section-head"><h2>작품 수정</h2></div>

	<div class="panel">
		<form id="artworkModifyForm" method="post" action="${ctx}/artwork/doUpdate">
			<input type="hidden" name="artworkId" value="${vo.artworkId}">
			<div class="field">
				<label for="categoryId">카테고리</label>
				<select class="text-input" id="categoryId" name="categoryId" data-selected="${vo.categoryId}" required>
					<option value="">카테고리 선택</option>
				</select>
			</div>
			<div class="field">
				<label for="title">제목</label>
				<input type="text" class="text-input" id="title" name="title" maxlength="30" value="<c:out value='${vo.title}'/>" required>
			</div>
			<div class="field">
				<label for="content">본문</label>
				<textarea class="text-input" id="content" name="content" rows="7" required><c:out value="${vo.content}"/></textarea>
			</div>

			<%-- 기존 이미지 관리: 대상이 존재하므로 즉시 모드(업로드/대표교환/삭제 즉시 반영) --%>
			<c:set var="upTargetType" value="ARTWORK"/>
			<c:set var="upTargetId" value="${vo.artworkId}"/>
			<c:set var="upEditable" value="true"/>
			<%@ include file="/WEB-INF/views/file/upload_widget.jsp" %>

			<div style="display:flex;gap:8px;margin-top:18px;">
				<button type="submit" class="btn" style="flex:1;">저장</button>
				<a class="btn ghost" style="flex:1;" href="${ctx}/artwork/${vo.isStatus eq 'Y' ? 'complete' : 'working'}/view?artworkId=${vo.artworkId}">취소</a>
			</div>
		</form>
	</div>
</div>

<script>
$(function() {
	var ctx = $('body').data('ctx') || '';
	var $sel = $('#categoryId');
	var selected = String($sel.data('selected') || '');
	$.get(ctx + '/category/doRetrieve.do', function(res) {
		if (res.code !== '200') { return; }
		(res.data || []).forEach(function(cItem) {
			var opt = $('<option>').val(cItem.categoryId).text(cItem.categoryNm);
			if (String(cItem.categoryId) === selected) { opt.prop('selected', true); }
			$sel.append(opt);
		});
	}, 'json');
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
