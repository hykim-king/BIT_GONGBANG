<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="회원정보 수정 · 빚다"/>
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

<script>
$(function() {
	var ctx = $('body').data('ctx') || '';
	var originalNick = $('#nickname').val();
	var memberId = $('#modifyForm').data('member-id');
	var nickChecked = true;

	$('#btnCheckNick').on('click', function() {
		var nickname = $('#nickname').val().trim();
		if (!nickname) { alert('닉네임을 입력하세요.'); return; }
		$.post(ctx + '/member/checkNickname.do', { nickname: nickname, memberId: memberId }, function(res) {
			if (res.code === '200' && res.data === true) {
				$('#nickMsg').text('사용 가능합니다.').removeClass('fail').addClass('ok');
				nickChecked = true;
			} else {
				$('#nickMsg').text('이미 사용 중입니다.').removeClass('ok').addClass('fail');
				nickChecked = false;
			}
		}, 'json');
	});

	$('#nickname').on('input', function() {
		nickChecked = ($(this).val().trim() === originalNick);
		$('#nickMsg').text('');
	});

	$('#modifyForm').on('submit', function(e) {
		e.preventDefault();
		var pw = $('#password').val();
		var cpw = $('#confirmPassword').val();
		if (pw && pw !== cpw) { alert('비밀번호가 일치하지 않습니다.'); return; }
		if (!nickChecked) { alert('닉네임 중복확인을 해주세요.'); return; }

		$.post(ctx + '/member/doUpdate.do', $(this).serialize(), function(res) {
			if (res.code === '200') {
				alert('수정되었습니다.');
				location.href = ctx + '/member/mypage.do';
			} else {
				alert(res.message || '수정에 실패했습니다.');
			}
		}, 'json').fail(function() {
			alert('요청 처리 중 오류가 발생했습니다. 로그인 상태를 확인하세요.');
		});
	});

	$('#btnWithdraw').on('click', function() {
		if (!confirm('정말 탈퇴하시겠습니까?')) return;
		$.post(ctx + '/member/doDelete.do', function(res) {
			if (res.code === '200') {
				alert('탈퇴가 완료되었습니다.');
				location.href = ctx + '/member/login.do';
			} else {
				alert(res.message || '탈퇴에 실패했습니다.');
			}
		}, 'json').fail(function() {
			alert('요청 처리 중 오류가 발생했습니다.');
		});
	});
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
