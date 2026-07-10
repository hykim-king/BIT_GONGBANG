<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
  CC-FIL-01 이미지 업로드 부분 컴포넌트 (정적 include 프래그먼트)
  사용법(호스트 화면):
    <c:set var="upTargetType" value="ARTWORK"/>            (또는 ARTWORK_ENTRY)
    <c:set var="upTargetId" value="${vo.artworkId}"/>      (즉시 모드 — 대상 존재)
    <c:set var="upTargetId" value=""/>                     (지연 모드 — 글쓰기: 저장 후 업로드)
    <c:set var="upEditable" value="true"/>                 (본인/작성 화면에서만 true)
    <%@ include file="/WEB-INF/views/file/upload_widget.jsp" %>
  - 렌더링/이벤트는 resources/assets/js/file/upload.js 가 .upload-widget 단위로 자동 마운트.
  - 지연 모드: 파일을 위젯이 들고 있다가 bitda.uploader.uploadTo($el, targetType, targetId)
    호출 시 일괄 업로드(3단계 글쓰기 오케스트레이션용).
  - 규칙: 대상당 최대 9장(서버도 검증), 첫 업로드 자동 대표(1번 슬롯), 대표=setRep 슬롯 교환.
--%>
<div class="upload-widget"
	data-target-type="${upTargetType}"
	data-target-id="${upTargetId}"
	data-editable="${empty upEditable ? 'false' : upEditable}">
	<div class="up-head">
		<span class="up-title">이미지</span>
		<span class="up-count">(0/9)</span>
		<label class="btn ghost small up-pick-label" style="display:none;">
			파일 선택<input type="file" class="up-input" accept="image/jpeg,image/png,image/webp" multiple style="display:none;">
		</label>
	</div>
	<p class="hint up-hint" style="display:none;">jpg/jpeg/png/webp, 파일당 5MB, 최대 9장. 첫 장이 대표(썸네일)가 됩니다.</p>
	<div class="up-grid"></div>
</div>
