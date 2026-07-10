/* ============================================================================
 * CC-CMT-01 댓글 부분 컴포넌트 — .comment-box 자동 마운트
 * 백엔드 계약(CommentController, 전부 POST + MessageVO):
 *  - /comment/doRetrieve.do   {targetType,targetId}            → data=List (comment_id DESC)
 *  - /comment/countByTarget.do{targetType,targetId}            → data=Integer
 *  - /comment/doSave.do       {targetType,targetId,content}    → 200/400/401
 *  - /comment/doUpdate.do     {commentId,content}              → 200/400/401
 *  - /comment/doDelete.do     {commentId}                      → 200/400/401
 *  본인 판정: 목록 항목의 memberId === data-login-member-id
 *  주의: 비로그인 세션 만료 시 LoginInterceptor 가 302(HTML)를 주므로 .fail 로 떨어짐.
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	var ctx = document.body.dataset.ctx || '';
	var esc = (window.bitda && window.bitda.esc) || function (s) { return String(s == null ? '' : s); };

	function fmtDt(s) {
		/* "2026-07-09 17:57:14.0" / "2026-07-09 17:57:14" → "2026-07-09 17:57" */
		return s ? String(s).substring(0, 16) : '';
	}

	function itemHtml(c, loginMemberId) {
		var mine = loginMemberId !== '' && String(c.memberId) === String(loginMemberId);
		var html = '<li class="cmt-item" data-comment-id="' + c.commentId + '">';
		html += '<div class="cmt-meta"><strong class="cmt-nick">' + esc(c.nickname) + '</strong>';
		html += '<span class="cmt-dt">' + fmtDt(c.regDt) + (c.modDt ? ' (수정됨)' : '') + '</span>';
		if (mine) {
			html += '<span class="cmt-btns"><a class="cmt-edit">수정</a> <a class="cmt-del">삭제</a></span>';
		}
		html += '</div>';
		html += '<p class="cmt-content">' + esc(c.content) + '</p>';
		html += '<div class="cmt-edit-area"></div>';
		html += '</li>';
		return html;
	}

	function mount(box) {
		var targetType = box.dataset.targetType;
		var targetId = box.dataset.targetId;
		var loginMemberId = String(box.dataset.loginMemberId === undefined ? '' : box.dataset.loginMemberId);

		function load() {
			$.post(ctx + '/comment/doRetrieve.do', { targetType: targetType, targetId: targetId }, function (res) {
				if (res.code !== '200') { return; }
				var list = res.data || [];
				box.querySelector('.cmt-count').textContent = '(' + list.length + ')';
				var html = '';
				for (var i = 0; i < list.length; i++) {
					html += itemHtml(list[i], loginMemberId);
				}
				box.querySelector('.cmt-list').innerHTML = html || '<li class="cmt-empty">첫 댓글을 남겨보세요.</li>';
			}, 'json');
		}

		/* 입력 영역: 로그인 시 폼, 비로그인 시 안내 */
		if (loginMemberId !== '') {
			box.querySelector('.cmt-form-area').innerHTML =
				'<form class="cmt-form" method="post">' +
				'<div class="row"><input type="text" class="text-input cmt-input" maxlength="1000" placeholder="댓글을 입력하세요 (1~1000자)">' +
				'<button type="submit" class="btn small">등록</button></div>' +
				'</form>';
		} else {
			box.querySelector('.cmt-form-area').innerHTML = '<p class="hint">로그인 후 댓글을 작성할 수 있습니다.</p>';
		}

		/* 등록 */
		box.addEventListener('submit', function (e) {
			var t = e.target.closest('.cmt-form');
			if (!t) { return; }
			e.preventDefault();
			var content = box.querySelector('.cmt-input').value.trim();
			if (!content) { alert('댓글 내용을 입력하세요.'); return; }
			window.bitda.requestAjax({
				url: ctx + '/comment/doSave.do',
				data: { targetType: targetType, targetId: targetId, content: content },
				failMessage: '요청 처리 중 오류가 발생했습니다. 로그인 상태를 확인하세요.',
				resFunction: function (res) {
					if (res.code === '200') {
						box.querySelector('.cmt-input').value = '';
						load();
					} else if (res.code === '401') {
						alert('로그인이 필요합니다.');
					} else {
						alert(res.message || '댓글 등록에 실패했습니다.');
					}
				}
			});
		});

		/* 수정 — 인라인 편집 폼 전환 */
		box.addEventListener('click', function (e) {
			var t = e.target.closest('.cmt-edit');
			if (!t) { return; }
			var item = t.closest('.cmt-item');
			if (item.querySelector('.cmt-edit-form')) { return; }
			var cur = item.querySelector('.cmt-content').textContent;
			item.querySelector('.cmt-content').style.display = 'none';
			item.querySelector('.cmt-edit-area').innerHTML =
				'<form class="cmt-edit-form" method="post">' +
				'<div class="row"><input type="text" class="text-input cmt-edit-input" maxlength="1000">' +
				'<button type="submit" class="btn small">저장</button>' +
				'<button type="button" class="btn ghost small cmt-edit-cancel">취소</button></div>' +
				'</form>';
			var input = item.querySelector('.cmt-edit-input');
			input.value = cur;
			input.focus();
		});
		box.addEventListener('click', function (e) {
			var t = e.target.closest('.cmt-edit-cancel');
			if (!t) { return; }
			var item = t.closest('.cmt-item');
			item.querySelector('.cmt-edit-area').replaceChildren();
			item.querySelector('.cmt-content').style.display = '';
		});
		box.addEventListener('submit', function (e) {
			var t = e.target.closest('.cmt-edit-form');
			if (!t) { return; }
			e.preventDefault();
			var item = t.closest('.cmt-item');
			var commentId = item.dataset.commentId;
			var content = item.querySelector('.cmt-edit-input').value.trim();
			if (!content) { alert('댓글 내용을 입력하세요.'); return; }
			window.bitda.requestAjax({
				url: ctx + '/comment/doUpdate.do',
				data: { commentId: commentId, content: content },
				resFunction: function (res) {
					if (res.code === '200') {
						load();
					} else {
						alert(res.message || '댓글 수정에 실패했습니다.');
					}
				}
			});
		});

		/* 삭제 */
		box.addEventListener('click', function (e) {
			var t = e.target.closest('.cmt-del');
			if (!t) { return; }
			if (!confirm('댓글을 삭제하시겠습니까?')) { return; }
			var commentId = t.closest('.cmt-item').dataset.commentId;
			window.bitda.requestAjax({
				url: ctx + '/comment/doDelete.do',
				data: { commentId: commentId },
				resFunction: function (res) {
					if (res.code === '200') {
						load();
					} else {
						alert(res.message || '댓글 삭제에 실패했습니다.');
					}
				}
			});
		});

		load();
	}

	document.querySelectorAll('.comment-box').forEach(function (box) {
		mount(box);
	});
});
