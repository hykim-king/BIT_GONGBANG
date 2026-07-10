/* ============================================================================
 * CC-USR-03 마이페이지 — 탭 전환(공개/완성/관심) + 회원 탈퇴
 * 백엔드 계약:
 *  - POST /member/doDelete.do  (세션 회원 탈퇴) → 200/400/401
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	var ctx = document.body.dataset.ctx || '';

	/* 탭 전환: 클릭한 탭과 같은 이름의 pane 만 활성화 */
	document.querySelectorAll('.mypage-tabs .tab').forEach(function (tab) {
		tab.addEventListener('click', function () {
			document.querySelectorAll('.mypage-tabs .tab').forEach(function (t) {
				t.classList.remove('active');
			});
			tab.classList.add('active');
			document.querySelectorAll('.tab-pane').forEach(function (pane) {
				pane.classList.remove('active');
			});
			var target = document.getElementById('pane-' + tab.dataset.pane);
			if (target) {
				target.classList.add('active');
			}
		});
	});

	/* 탈퇴 */
	var btnWithdraw = document.getElementById('btnWithdraw');
	if (btnWithdraw) {
		btnWithdraw.addEventListener('click', function () {
			if (!confirm('정말 탈퇴하시겠습니까? 작성한 작품과 활동 내역이 삭제될 수 있습니다.')) return;
			$.post(ctx + '/member/doDelete.do', function (res) {
				if (res.code === '200') {
					alert('탈퇴가 완료되었습니다.');
					location.href = ctx + '/main/index.do';
				} else {
					alert(res.message || '탈퇴에 실패했습니다.');
				}
			}, 'json').fail(function () {
				alert('요청 처리 중 오류가 발생했습니다.');
			});
		});
	}
});
