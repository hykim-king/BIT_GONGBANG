<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="회원정보 수정 · 빚다"/>
<c:set var="pageScript" value="member/member_modify"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>
<style>
.modify-actions { margin-top: 24px; display: flex; gap: 8px; }
.modify-actions .btn { flex: 1; padding: 12px; }
.links { margin-top: 16px; font-size: 0.9rem; }
.links a { color: var(--accent-strong); font-weight: 700; }
</style>
<div class="page-narrow">
	<div class="section-head"><h2>회원정보 수정</h2></div>
	<div class="panel">
	<form id="modifyForm" method="post" data-member-id="${member.memberId}">
		<div class="field">
			<label>이메일</label>
			<input type="text" class="text-input" value="<c:out value='${member.email}'/>" disabled>
		</div>
		<div class="field">
			<label for="nickname">닉네임</label>
			<div class="row">
				<input type="text" class="text-input" id="nickname" name="nickname" value="<c:out value='${member.nickname}'/>" maxlength="10" required>
				<button type="button" class="btn ghost small" id="btnCheckNick">중복확인</button>
			</div>
			<p id="nickMsg" class="msg"></p>
		</div>
		<div class="field">
			<label for="userIntro">자기소개</label>
			<textarea class="text-input" id="userIntro" name="userIntro" rows="3" maxlength="100"><c:out value="${member.userIntro}"/></textarea>
		</div>
		<div class="field">
			<label for="password">새 비밀번호 (변경 시만 입력)</label>
			<input type="password" class="text-input" id="password" name="password" minlength="8">
			<p class="hint">8자 이상. 변경하지 않으면 비워 두세요.</p>
		</div>
		<div class="field">
			<label for="confirmPassword">새 비밀번호 확인</label>
			<input type="password" class="text-input" id="confirmPassword" name="confirmPassword" minlength="8">
		</div>
		<div class="modify-actions">
			<button type="submit" class="btn" id="btnSave">저장</button>
			<button type="button" class="btn danger" id="btnWithdraw">탈퇴</button>
		</div>
	</form>
	</div>
	<p class="links"><a href="${ctx}/member/mypage.do">마이페이지로</a></p>
</div>

<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
