<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="회원가입 · 빚다"/>
<%@ include file="/WEB-INF/views/cmn/header.jsp" %>
<div class="page-narrow">
	<div class="section-head"><h2>회원가입</h2></div>
	<div class="panel" style="max-width:480px;">
	<%-- 주의: 헤더 모달의 #joinForm 과 충돌하지 않도록 페이지 폼은 #pageJoinForm 사용 --%>
	<form id="pageJoinForm" method="post">
		<div class="field">
			<label for="email">이메일</label>
			<div class="row">
				<input type="email" class="text-input" id="email" name="email" required maxlength="255">
				<button type="button" class="btn ghost small" id="btnCheckEmail">중복확인</button>
			</div>
			<p id="emailMsg" class="msg"></p>
		</div>
		<div class="field">
			<label for="password">비밀번호 (8자 이상)</label>
			<input type="password" class="text-input" id="password" name="password" required minlength="8">
		</div>
		<div class="field">
			<label for="confirmPassword">비밀번호 확인</label>
			<input type="password" class="text-input" id="confirmPassword" name="confirmPassword" required minlength="8">
		</div>
		<div class="field">
			<label for="nickname">닉네임 (10자 이내)</label>
			<div class="row">
				<input type="text" class="text-input" id="nickname" name="nickname" required maxlength="10">
				<button type="button" class="btn ghost small" id="btnCheckNick">중복확인</button>
			</div>
			<p id="nickMsg" class="msg"></p>
		</div>
		<div class="field">
			<label for="userIntro">자기소개 (100자 이내, 선택)</label>
			<textarea class="text-input" id="userIntro" name="userIntro" rows="3" maxlength="100"></textarea>
		</div>
		<button type="submit" class="btn block">가입하기</button>
	</form>
	<p class="switch-line"><a href="${ctx}/member/login.do">로그인으로</a></p>
	</div>
</div>

<script>
$(function() {
	var ctx = $('body').data('ctx') || '';
	var emailChecked = false;
	var nickChecked = false;

	function postCheck(url, data, $msg, onOk) {
		$.post(url, data, function(res) {
			if (res.code === '200' && res.data === true) {
				$msg.text('사용 가능합니다.').removeClass('fail').addClass('ok');
				onOk(true);
			} else {
				$msg.text('이미 사용 중입니다.').removeClass('ok').addClass('fail');
				onOk(false);
			}
		}, 'json').fail(function() {
			$msg.text('확인 중 오류가 발생했습니다.').removeClass('ok').addClass('fail');
			onOk(false);
		});
	}

	$('#btnCheckEmail').on('click', function() {
		var email = $('#email').val().trim();
		if (!email) { alert('이메일을 입력하세요.'); return; }
		postCheck(ctx + '/member/checkEmail.do', { email: email }, $('#emailMsg'), function(ok) {
			emailChecked = ok;
		});
	});

	$('#btnCheckNick').on('click', function() {
		var nickname = $('#nickname').val().trim();
		if (!nickname) { alert('닉네임을 입력하세요.'); return; }
		postCheck(ctx + '/member/checkNickname.do', { nickname: nickname }, $('#nickMsg'), function(ok) {
			nickChecked = ok;
		});
	});

	$('#email, #nickname').on('input', function() {
		if (this.id === 'email') emailChecked = false;
		if (this.id === 'nickname') nickChecked = false;
	});

	$('#pageJoinForm').on('submit', function(e) {
		e.preventDefault();
		if ($('#password').val() !== $('#confirmPassword').val()) {
			alert('비밀번호가 일치하지 않습니다.');
			return;
		}
		if (!emailChecked) { alert('이메일 중복확인을 해주세요.'); return; }
		if (!nickChecked) { alert('닉네임 중복확인을 해주세요.'); return; }

		$.post(ctx + '/member/doSave.do', $(this).serialize(), function(res) {
			if (res.code === '200') {
				alert('가입이 완료되었습니다.');
				location.href = ctx + '/member/login.do';
			} else {
				alert(res.message || '가입에 실패했습니다.');
			}
		}, 'json').fail(function() {
			alert('가입 처리 중 오류가 발생했습니다.');
		});
	});
});
</script>
<%@ include file="/WEB-INF/views/cmn/footer.jsp" %>
