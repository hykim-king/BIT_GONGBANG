<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>회원정보 수정 · 비트공방</title>
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
.actions { margin-top: 24px; display: flex; gap: 8px; }
.actions button { flex: 1; padding: 12px; cursor: pointer; border: none; }
#btnSave { background: #6DB33F; color: #fff; }
#btnWithdraw { background: #eee; color: #c00; }
.hint { font-size: 0.8rem; color: #888; margin-top: 4px; }
.links { margin-top: 16px; font-size: 0.9rem; }
</style>
</head>
<body>
<h1>회원정보 수정</h1>
<form id="modifyForm">
	<label>이메일</label>
	<input type="text" value="${member.email}" disabled>

	<label for="nickname">닉네임</label>
	<div class="row">
		<input type="text" id="nickname" name="nickname" value="${member.nickname}" maxlength="10" required>
		<button type="button" id="btnCheckNick">중복확인</button>
	</div>
	<p id="nickMsg" class="msg"></p>

	<label for="userIntro">자기소개</label>
	<textarea id="userIntro" name="userIntro" rows="3" maxlength="100">${member.userIntro}</textarea>

	<label for="password">새 비밀번호 (변경 시만 입력)</label>
	<input type="password" id="password" name="password" minlength="8">
	<p class="hint">8자 이상. 변경하지 않으면 비워 두세요.</p>

	<label for="confirmPassword">새 비밀번호 확인</label>
	<input type="password" id="confirmPassword" name="confirmPassword" minlength="8">

	<div class="actions">
		<button type="submit" id="btnSave">저장</button>
		<button type="button" id="btnWithdraw">탈퇴</button>
	</div>
</form>
<p class="links"><a href="${pageContext.request.contextPath}/member/mypage.do">마이페이지로</a></p>

<script>
var ctx = '${pageContext.request.contextPath}';
var originalNick = '${member.nickname}';
var nickChecked = true;

$('#btnCheckNick').on('click', function() {
	var nickname = $('#nickname').val().trim();
	if (!nickname) { alert('닉네임을 입력하세요.'); return; }
	$.post(ctx + '/member/checkNickname.do', { nickname: nickname, memberId: ${member.memberId} }, function(res) {
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
	}, 'json');
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
	}, 'json');
});
</script>
</body>
</html>
