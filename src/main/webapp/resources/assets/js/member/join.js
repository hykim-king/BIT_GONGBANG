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

	const ctx = document.body.dataset.ctx || '';
	let emailChecked = false;
	let nickChecked = false;

	const btnCheckEmail = document.getElementById('btnCheckEmail');
	if (btnCheckEmail) {
		btnCheckEmail.addEventListener('click', function () {
			const emailInput = document.getElementById('email');
			if (window.bitda.isEmpty(emailInput, '이메일을 입력하세요.')) { return; }
			window.bitda.checkDuplicate(
				ctx + '/member/checkEmail.do',
				{ email: emailInput.value.trim() },
				document.getElementById('emailMsg'),
				function (ok) { emailChecked = ok; }
			);
		});
	}

	const btnCheckNick = document.getElementById('btnCheckNick');
	if (btnCheckNick) {
		btnCheckNick.addEventListener('click', function () {
			const nicknameInput = document.getElementById('nickname');
			if (window.bitda.isEmpty(nicknameInput, '닉네임을 입력하세요.')) { return; }
			window.bitda.checkDuplicate(
				ctx + '/member/checkNickname.do',
				{ nickname: nicknameInput.value.trim() },
				document.getElementById('nickMsg'),
				function (ok) { nickChecked = ok; }
			);
		});
	}

	/* 값이 바뀌면 중복확인을 무효화한다 */
	document.querySelectorAll('#email, #nickname').forEach(function (el) {
		el.addEventListener('input', function () {
			if (this.id === 'email') emailChecked = false;
			if (this.id === 'nickname') nickChecked = false;
		});
	});

	const pageJoinForm = document.getElementById('pageJoinForm');
	if (pageJoinForm) {
		pageJoinForm.addEventListener('submit', function (e) {
			e.preventDefault();
			// 1. 입력값 읽기
			const form = this;

			// 2. 유효성 검사
			if (document.getElementById('password').value !== document.getElementById('confirmPassword').value) {
				alert('비밀번호가 일치하지 않습니다.');
				return;
			}
			if (!emailChecked) { alert('이메일 중복확인을 해주세요.'); return; }
			if (!nickChecked) { alert('닉네임 중복확인을 해주세요.'); return; }

			// 3. ajax()
			window.bitda.requestAjax({
				url: ctx + '/member/doSave.do',
				data: window.bitda.serializeForm(form),
				failMessage: '가입 처리 중 오류가 발생했습니다.',
				// 4. 응답 처리
				resFunction: function (res) {
					if (res.code === '200') {
						alert('가입이 완료되었습니다.');
						location.href = ctx + '/member/login.do';
					} else {
						alert(res.message || '가입에 실패했습니다.');
					}
				}
			});
		});
	}
});
