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
<c:set var="pageScript" value="admin/category"/>
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

<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
