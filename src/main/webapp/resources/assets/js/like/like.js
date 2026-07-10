/* ============================================================================
 * 좋아요 토글 컴포넌트 — .like-btn 자동 마운트 (작품/작업일지 공용, 다형)
 * 마크업 계약: <button class="like-btn" data-target-type="ARTWORK|ARTWORK_ENTRY"
 *                      data-target-id="N" data-count="7">...</button>
 * 백엔드: POST /like/toggle.do {targetType,targetId} → {code,data:{liked,count}} (401 미로그인)
 *        POST /like/myLikes.do → data:List<LikeVO> (로그인 시 초기 하트 상태 구성)
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const ctx = document.body.dataset.ctx || '';
	const loggedIn = document.body.dataset.loginMemberId !== undefined && String(document.body.dataset.loginMemberId) !== '';

	function render(btn, liked, count) {
		btn.classList.toggle('liked', !!liked);
		btn.querySelector('.like-count').textContent = count;
		btn.querySelector('.like-heart').innerHTML = liked ? '&#10084;' : '&#9825;';
	}

	const btns = document.querySelectorAll('.like-btn');
	if (!btns.length) { return; }

	/* 초기 카운트: data-count 있으면 서버 렌더값, 없으면 count.do 조회.
	   로그인 시 내 좋아요 목록으로 하트 상태 반영 */
	btns.forEach(function (b) {
		if (b.dataset.count !== undefined) {
			render(b, false, b.dataset.count);
		} else {
			render(b, false, 0);
			$.post(ctx + '/like/count.do', {
				targetType: b.dataset.targetType, targetId: b.dataset.targetId
			}, function (res) {
				if (res.code === '200') { render(b, b.classList.contains('liked'), res.data); }
			}, 'json');
		}
	});

	if (loggedIn) {
		$.post(ctx + '/like/myLikes.do', function (res) {
			if (res.code !== '200') { return; }
			const mine = {};
			(res.data || []).forEach(function (l) { mine[l.targetType + ':' + l.targetId] = true; });
			btns.forEach(function (b) {
				if (mine[b.dataset.targetType + ':' + b.dataset.targetId]) {
					render(b, true, b.querySelector('.like-count').textContent);
				}
			});
		}, 'json');
	}

	document.addEventListener('click', function (e) {
		const b = e.target.closest('.like-btn');
		if (!b) { return; }
		window.bitda.requestAjax({
			url: ctx + '/like/toggle.do',
			data: { targetType: b.dataset.targetType, targetId: b.dataset.targetId },
			/* 미로그인 시 LoginInterceptor 가 302(HTML)를 주므로 .fail 로 떨어진다 */
			failMessage: '로그인이 필요합니다.',
			resFunction: function (res) {
				if (res.code === '200') {
					render(b, res.data.liked, res.data.count);
				} else if (res.code === '401') {
					alert('로그인이 필요합니다.');
				} else {
					alert(res.message || '처리에 실패했습니다.');
				}
			}
		});
	});
});
