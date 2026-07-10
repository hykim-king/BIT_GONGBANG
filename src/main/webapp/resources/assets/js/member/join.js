/* ============================================================================
 * CC-USR-02 회원가입 (전체 페이지판) — 이메일/닉네임 중복확인 + 가입
 * 헤더의 회원가입 모달(cmn/common.js)과 화면만 다르고 흐름은 같다.
 * 백엔드 계약:
 *  - POST /member/checkEmail.do    {email}    → data=Boolean(사용가능)
 *  - POST /member/checkNickname.do {nickname} → data=Boolean(사용가능)
 *  - POST /member/doSave.do        {가입 폼}   → 200/400
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	var ctx = document.body.dataset.ctx || '';
	var emailChecked = false;
	var nickChecked = false;

	/* 중복확인 공통 처리: 결과 메시지 표시 + 확인 플래그 갱신 */
	function postCheck(url, data, msg, onOk) {
		$.post(url, data, function (res) {
			if (res.code === '200' && res.data === true) {
				msg.textContent = '사용 가능합니다.';
				msg.classList.remove('fail');
				msg.classList.add('ok');
				onOk(true);
			} else {
				msg.textContent = '이미 사용 중입니다.';
				msg.classList.remove('ok');
				msg.classList.add('fail');
				onOk(false);
			}
		}, 'json').fail(function () {
			msg.textContent = '확인 중 오류가 발생했습니다.';
			msg.classList.remove('ok');
			msg.classList.add('fail');
			onOk(false);
		});
	}

	var btnCheckEmail = document.getElementById('btnCheckEmail');
	if (btnCheckEmail) {
		btnCheckEmail.addEventListener('click', function () {
			var email = document.getElementById('email').value.trim();
			if (!email) { alert('이메일을 입력하세요.'); return; }
			postCheck(ctx + '/member/checkEmail.do', { email: email }, document.getElementById('emailMsg'), function (ok) {
				emailChecked = ok;
			});
		});
	}

	var btnCheckNick = document.getElementById('btnCheckNick');
	if (btnCheckNick) {
		btnCheckNick.addEventListener('click', function () {
			var nickname = document.getElementById('nickname').value.trim();
			if (!nickname) { alert('닉네임을 입력하세요.'); return; }
			postCheck(ctx + '/member/checkNickname.do', { nickname: nickname }, document.getElementById('nickMsg'), function (ok) {
				nickChecked = ok;
			});
		});
	}

	/* 값이 바뀌면 중복확인을 무효화한다 */
	document.querySelectorAll('#email, #nickname').forEach(function (el) {
		el.addEventListener('input', function () {
			if (this.id === 'email') emailChecked = false;
			if (this.id === 'nickname') nickChecked = false;
		});
	});

	var pageJoinForm = document.getElementById('pageJoinForm');
	if (pageJoinForm) {
		pageJoinForm.addEventListener('submit', function (e) {
			e.preventDefault();
			if (document.getElementById('password').value !== document.getElementById('confirmPassword').value) {
				alert('비밀번호가 일치하지 않습니다.');
				return;
			}
			if (!emailChecked) { alert('이메일 중복확인을 해주세요.'); return; }
			if (!nickChecked) { alert('닉네임 중복확인을 해주세요.'); return; }

			$.post(ctx + '/member/doSave.do', window.bitda.serializeForm(this), function (res) {
				if (res.code === '200') {
					alert('가입이 완료되었습니다.');
					location.href = ctx + '/member/login.do';
				} else {
					alert(res.message || '가입에 실패했습니다.');
				}
			}, 'json').fail(function () {
				alert('가입 처리 중 오류가 발생했습니다.');
			});
		});
	}
});
