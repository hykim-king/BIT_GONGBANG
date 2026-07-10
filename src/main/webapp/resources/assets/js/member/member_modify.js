/* ============================================================================
 * CC-USR-04 회원정보 수정 — 닉네임 중복확인 + 수정 + 탈퇴
 * 백엔드 계약:
 *  - POST /member/checkNickname.do {nickname, memberId} → data=Boolean(사용가능)
 *    (본인 닉네임은 memberId 로 제외 처리되므로 그대로 두어도 '사용 가능')
 *  - POST /member/doUpdate.do  {수정 폼} → 200/400/401
 *  - POST /member/doDelete.do             → 200/400/401
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	var ctx = document.body.dataset.ctx || '';
	var nicknameInput = document.getElementById('nickname');
	var modifyForm = document.getElementById('modifyForm');
	var nickMsg = document.getElementById('nickMsg');

	/* 이 화면이 아니면 아무것도 하지 않는다 */
	if (!modifyForm || !nicknameInput) { return; }

	var originalNick = nicknameInput.value;
	var memberId = modifyForm.dataset.memberId;
	/* 처음 값 그대로면 중복확인 없이 저장 가능 */
	var nickChecked = true;

	var btnCheckNick = document.getElementById('btnCheckNick');
	if (btnCheckNick) {
		btnCheckNick.addEventListener('click', function () {
			if (window.bitda.isEmpty(nicknameInput, '닉네임을 입력하세요.')) { return; }
			window.bitda.checkDuplicate(
				ctx + '/member/checkNickname.do',
				{ nickname: nicknameInput.value.trim(), memberId: memberId },
				nickMsg,
				function (ok) { nickChecked = ok; }
			);
		});
	}

	/* 닉네임을 바꾸면 다시 확인해야 한다. 원래 값으로 되돌리면 확인 불필요. */
	nicknameInput.addEventListener('input', function () {
		nickChecked = (this.value.trim() === originalNick);
		nickMsg.textContent = '';
	});

	modifyForm.addEventListener('submit', function (e) {
		e.preventDefault();
		// 1. 입력값 읽기
		var pw = document.getElementById('password').value;
		var cpw = document.getElementById('confirmPassword').value;

		// 2. 유효성 검사
		if (pw && pw !== cpw) { alert('비밀번호가 일치하지 않습니다.'); return; }
		if (!nickChecked) { alert('닉네임 중복확인을 해주세요.'); return; }

		// 3. ajax()
		window.bitda.requestAjax({
			url: ctx + '/member/doUpdate.do',
			data: window.bitda.serializeForm(this),
			failMessage: '요청 처리 중 오류가 발생했습니다. 로그인 상태를 확인하세요.',
			// 4. 응답 처리
			resFunction: function (res) {
				if (res.code === '200') {
					alert('수정되었습니다.');
					location.href = ctx + '/member/mypage.do';
				} else {
					alert(res.message || '수정에 실패했습니다.');
				}
			}
		});
	});

	var btnWithdraw = document.getElementById('btnWithdraw');
	if (btnWithdraw) {
		btnWithdraw.addEventListener('click', function () {
			if (!confirm('정말 탈퇴하시겠습니까?')) return;
			window.bitda.requestAjax({
				url: ctx + '/member/doDelete.do',
				resFunction: function (res) {
					if (res.code === '200') {
						alert('탈퇴가 완료되었습니다.');
						location.href = ctx + '/member/login.do';
					} else {
						alert(res.message || '탈퇴에 실패했습니다.');
					}
				}
			});
		});
	}
});
