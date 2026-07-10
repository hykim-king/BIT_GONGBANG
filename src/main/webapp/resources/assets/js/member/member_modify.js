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
			var nickname = nicknameInput.value.trim();
			if (!nickname) { alert('닉네임을 입력하세요.'); return; }
			$.post(ctx + '/member/checkNickname.do', { nickname: nickname, memberId: memberId }, function (res) {
				if (res.code === '200' && res.data === true) {
					nickMsg.textContent = '사용 가능합니다.';
					nickMsg.classList.remove('fail');
					nickMsg.classList.add('ok');
					nickChecked = true;
				} else {
					nickMsg.textContent = '이미 사용 중입니다.';
					nickMsg.classList.remove('ok');
					nickMsg.classList.add('fail');
					nickChecked = false;
				}
			}, 'json');
		});
	}

	/* 닉네임을 바꾸면 다시 확인해야 한다. 원래 값으로 되돌리면 확인 불필요. */
	nicknameInput.addEventListener('input', function () {
		nickChecked = (this.value.trim() === originalNick);
		nickMsg.textContent = '';
	});

	modifyForm.addEventListener('submit', function (e) {
		e.preventDefault();
		var pw = document.getElementById('password').value;
		var cpw = document.getElementById('confirmPassword').value;
		if (pw && pw !== cpw) { alert('비밀번호가 일치하지 않습니다.'); return; }
		if (!nickChecked) { alert('닉네임 중복확인을 해주세요.'); return; }

		$.post(ctx + '/member/doUpdate.do', window.bitda.serializeForm(this), function (res) {
			if (res.code === '200') {
				alert('수정되었습니다.');
				location.href = ctx + '/member/mypage.do';
			} else {
				alert(res.message || '수정에 실패했습니다.');
			}
		}, 'json').fail(function () {
			alert('요청 처리 중 오류가 발생했습니다. 로그인 상태를 확인하세요.');
		});
	});

	var btnWithdraw = document.getElementById('btnWithdraw');
	if (btnWithdraw) {
		btnWithdraw.addEventListener('click', function () {
			if (!confirm('정말 탈퇴하시겠습니까?')) return;
			$.post(ctx + '/member/doDelete.do', function (res) {
				if (res.code === '200') {
					alert('탈퇴가 완료되었습니다.');
					location.href = ctx + '/member/login.do';
				} else {
					alert(res.message || '탈퇴에 실패했습니다.');
				}
			}, 'json').fail(function () {
				alert('요청 처리 중 오류가 발생했습니다.');
			});
		});
	}
});
