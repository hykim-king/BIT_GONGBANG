/* ============================================================================
 * CC-USR-01 로그인 (전체 페이지판)
 * 헤더 모달(cmn/common.js)과 같은 엔드포인트를 쓴다 — 인증 경로는 하나다.
 * 백엔드 계약: POST /member/doLoginAjax.do {email, password} → 200/400
 * 성공 시 인터셉터가 보내온 원래 목적지 대신 마이페이지로 이동한다.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const ctx = document.body.dataset.ctx || '';
	const pageLoginForm = document.getElementById('pageLoginForm');
	if (!pageLoginForm) { return; }

	const loginMsg = document.getElementById('pageLoginMsg');
	const showMsg = function (text) {
		loginMsg.textContent = text;
		loginMsg.classList.remove('ok');
		loginMsg.classList.add('fail');
	};

	pageLoginForm.addEventListener('submit', function (e) {
		e.preventDefault();
		// 1. 입력값 읽기
		const emailInput = document.getElementById('email');
		const passwordInput = document.getElementById('password');

		// 2. 유효성 검사
		if (window.bitda.isEmpty(emailInput, '이메일을 입력하세요.')) { return; }
		if (window.bitda.isEmpty(passwordInput, '비밀번호를 입력하세요.')) { return; }

		// 3. ajax()
		window.bitda.requestAjax({
			url: ctx + '/member/doLoginAjax.do',
			data: { email: emailInput.value.trim(), password: passwordInput.value },
			failMessage: '요청 처리 중 오류가 발생했습니다.',
			// 4. 응답 처리
			resFunction: function (res) {
				if (res.code === '200') {
					location.href = ctx + '/member/mypage.do';
				} else {
					showMsg('이메일 또는 비밀번호가 올바르지 않습니다.');
				}
			}
		});
	});
});
