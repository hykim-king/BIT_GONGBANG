<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  CC-ART-01 글쓰기(작품 등록) — 완성작 폼.
  유형선택 라디오: 완성작(현재)/공개작업(working/reg 이동).
  이미지: 지연 업로드 위젯 — 저장(PRG) 후 redirect URL 의 artworkId 로 일괄 업로드.
--%>
<c:set var="pageTitle" value="글쓰기 · 빚다"/>
<c:set var="pageScript" value="artwork/complete_reg"/>
<c:set var="activeMenu" value="complete"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>

<div class="page-narrow" style="max-width:680px;">
	<div class="section-head"><h2>글쓰기</h2><p>완성작은 등록 즉시 완성 게시판에 노출됩니다.</p></div>

	<div class="panel">
		<div class="field">
			<label>유형 선택</label>
			<label style="margin-right:16px;"><input type="radio" name="regType" value="complete" checked> 완성작</label>
			<label><input type="radio" name="regType" value="working"> 공개작업</label>
		</div>

		<form id="artworkRegForm" method="post" action="${ctx}/artwork/complete/doSave">
			<div class="field">
				<label for="categoryId">카테고리</label>
				<select class="text-input" id="categoryId" name="categoryId" required>
					<option value="">카테고리 선택</option>
				</select>
			</div>
			<div class="field">
				<label for="title">제목</label>
				<input type="text" class="text-input" id="title" name="title" maxlength="30" required>
			</div>
			<div class="field">
				<label for="content">본문</label>
				<textarea class="text-input" id="content" name="content" rows="7" required></textarea>
			</div>

			<c:set var="upTargetType" value="ARTWORK"/>
			<c:set var="upTargetId" value=""/>
			<c:set var="upEditable" value="true"/>
			<%@ include file="/WEB-INF/views/file/upload_widget.jsp" %>

			<div style="display:flex;gap:8px;margin-top:18px;">
				<button type="submit" class="btn" style="flex:1;">등록</button>
				<a class="btn ghost" style="flex:1;" href="${ctx}/artwork/complete/list">취소</a>
			</div>
		</form>
	</div>
</div>

<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
