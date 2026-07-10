/* ============================================================================
 * CC-ADM-02 회원 관리 — 회원 수정 모달 + 회원 삭제
 * 백엔드 계약:
 *  - POST /admin/member_update.do {memberId, nickname, isAdmin} → 200/400
 *    (자기 자신의 관리자 권한 해제는 서버에서 차단)
 *  - POST /admin/member_delete.do {memberId} → 200/400/500
 *    (작품·작업일지·댓글·좋아요 연쇄 삭제. 자기 자신 삭제는 서버에서 차단)
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const ctx = document.body.dataset.ctx || '';

	/* 수정: 행 값을 모달 폼에 채우고 연다 */
	document.querySelectorAll('.mem-edit').forEach(function (btn) {
		btn.addEventListener('click', function () {
			const tr = btn.closest('tr');
			document.getElementById('editMemberId').value = tr.dataset.memberId;
			document.getElementById('editNickname').value = tr.dataset.nickname;
			document.getElementById('editIsAdmin').value = tr.dataset.isAdmin;
			document.getElementById('memberEditModal').classList.add('open');
		});
	});

	const editForm = document.getElementById('memberEditForm');
	if (editForm) {
		editForm.addEventListener('submit', function (e) {
			e.preventDefault();
			window.bitda.requestAjax({
				url: ctx + '/admin/member_update.do',
				data: window.bitda.serializeForm(editForm),
				resFunction: function (res) {
					if (res.code === '200') {
						alert(res.message);
						location.reload();
					} else {
						alert(res.message || '수정에 실패했습니다.');
					}
				}
			});
		});
	}

	/* 삭제 */
	document.querySelectorAll('.mem-del').forEach(function (btn) {
		btn.addEventListener('click', function () {
			const tr = btn.closest('tr');
			if (!confirm('"' + tr.dataset.nickname + '" 회원을 삭제하시겠습니까?\n작성한 작품·작업일지·댓글·좋아요가 모두 함께 삭제됩니다.')) { return; }
			window.bitda.requestAjax({
				url: ctx + '/admin/member_delete.do',
				data: { memberId: tr.dataset.memberId },
				resFunction: function (res) {
					if (res.code === '200') {
						alert(res.message);
						location.reload();
					} else {
						alert(res.message || '삭제에 실패했습니다.');
					}
				}
			});
		});
	});
});
