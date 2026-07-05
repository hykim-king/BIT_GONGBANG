<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>회원가입 · 비트공방</title>
<script src="${pageContext.request.contextPath}/resources/assets/js/jquery-4.0.0.js"></script>
<style>
body { font-family: 'Malgun Gothic', sans-serif; max-width: 480px; margin: 40px auto; padding: 0 16px; }
h1 { font-size: 1.4rem; }
label { display: block; margin-top: 12px; font-size: 0.9rem; }
input, textarea { width: 100%; padding: 10px; margin-top: 4px; box-sizing: border-box; }
.row { display: flex; gap: 8px; align-items: flex-end; }
.row input { flex: 1; }
.row button { padding: 10px 12px; white-space: nowrap; }
.msg { font-size: 0.85rem; margin-top: 4px; }
.msg.ok { color: #2a7; }
.msg.fail { color: #c00; }
button[type=submit] { width: 100%; margin-top: 24px; padding: 12px; background: #6DB33F; color: #fff; border: none; cursor: pointer; }
.links { margin-top: 16px; text-align: center; font-size: 0.9rem; }
</style>
</head>
<body>
<h1>회원가입</h1>
<form id="joinForm">
	<label for="email">이메일</label>
	<div class="row">
		<input type="email" id="email" name="email" required maxlength="255">
		<button type="button" id="btnCheckEmail">중복확인</button>
	</div>
	<p id="emailMsg" class="msg"></p>

	<label for="password">비밀번호 (8자 이상)</label>
	<input type="password" id="password" name="password" required minlength="8">

	<label for="confirmPassword">비밀번호 확인</label>
	<input type="password" id="confirmPassword" name="confirmPassword" required minlength="8">

	<label for="nickname">닉네임 (10자 이내)</label>
	<div class="row">
		<input type="text" id="nickname" name="nickname" required maxlength="10">
		<button type="button" id="btnCheckNick">중복확인</button>
	</div>
	<p id="nickMsg" class="msg"></p>

	<label for="userIntro">자기소개 (100자 이내, 선택)</label>
	<textarea id="userIntro" name="userIntro" rows="3" maxlength="100"></textarea>

	<button type="submit">가입하기</button>
</form>
<p class="links"><a href="${pageContext.request.contextPath}/member/login.do">로그인으로</a></p>

<script>
var ctx = '${pageContext.request.contextPath}';
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

$('#joinForm').on('submit', function(e) {
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
</script>
</body>
</html>
