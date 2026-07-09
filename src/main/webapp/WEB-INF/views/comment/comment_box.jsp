<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%--
  CC-CMT-01 댓글 부분 컴포넌트 (정적 include 프래그먼트)
  사용법(호스트 화면):
    <c:set var="cmtTargetType" value="ARTWORK"/>          (또는 ARTWORK_ENTRY)
    <c:set var="cmtTargetId" value="${vo.artworkId}"/>
    <%@ include file="/WEB-INF/views/comment/comment_box.jsp" %>
  - 같은 화면에 여러 번 include 가능(공개 상세의 일차별 댓글). c:forEach 루프 안에서도 동작.
  - 렌더링/이벤트는 resources/js/comment.js 가 .comment-box 단위로 자동 마운트.
  - 비로그인: 목록/개수만 표시, 입력창 대신 로그인 안내.
--%>
<div class="comment-box"
	data-target-type="${cmtTargetType}"
	data-target-id="${cmtTargetId}"
	data-login-member-id="${empty sessionScope.loginMember ? '' : sessionScope.loginMember.memberId}">
	<h4 class="cmt-head">댓글 <span class="cmt-count">(0)</span></h4>
	<div class="cmt-form-area"></div>
	<ul class="cmt-list"></ul>
</div>
