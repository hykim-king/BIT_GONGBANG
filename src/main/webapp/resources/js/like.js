/* ============================================================================
 * 좋아요 토글 컴포넌트 — .like-btn 자동 마운트 (작품/작업일지 공용, 다형)
 * 마크업 계약: <button class="like-btn" data-target-type="ARTWORK|ARTWORK_ENTRY"
 *                      data-target-id="N" data-count="7">...</button>
 * 백엔드: POST /like/toggle.do {targetType,targetId} → {code,data:{liked,count}} (401 미로그인)
 *        POST /like/myLikes.do → data:List<LikeVO> (로그인 시 초기 하트 상태 구성)
 * ==========================================================================*/
$(function () {
	'use strict';

	var ctx = $('body').data('ctx') || '';
	var loggedIn = $('body').data('login-member-id') !== undefined && String($('body').data('login-member-id')) !== '';

	function render($btn, liked, count) {
		$btn.toggleClass('liked', !!liked);
		$btn.find('.like-count').text(count);
		$btn.find('.like-heart').html(liked ? '&#10084;' : '&#9825;');
	}

	var $btns = $('.like-btn');
	if (!$btns.length) { return; }

	/* 초기 카운트: data-count 있으면 서버 렌더값, 없으면 count.do 조회.
	   로그인 시 내 좋아요 목록으로 하트 상태 반영 */
	$btns.each(function () {
		var $b = $(this);
		if ($b.data('count') !== undefined) {
			render($b, false, $b.data('count'));
		} else {
			render($b, false, 0);
			$.post(ctx + '/like/count.do', {
				targetType: $b.data('target-type'), targetId: $b.data('target-id')
			}, function (res) {
				if (res.code === '200') { render($b, $b.hasClass('liked'), res.data); }
			}, 'json');
		}
	});

	if (loggedIn) {
		$.post(ctx + '/like/myLikes.do', function (res) {
			if (res.code !== '200') { return; }
			var mine = {};
			(res.data || []).forEach(function (l) { mine[l.targetType + ':' + l.targetId] = true; });
			$btns.each(function () {
				var $b = $(this);
				if (mine[$b.data('target-type') + ':' + $b.data('target-id')]) {
					render($b, true, $b.find('.like-count').text());
				}
			});
		}, 'json');
	}

	$(document).on('click', '.like-btn', function () {
		var $b = $(this);
		$.post(ctx + '/like/toggle.do', {
			targetType: $b.data('target-type'), targetId: $b.data('target-id')
		}, function (res) {
			if (res.code === '200') {
				render($b, res.data.liked, res.data.count);
			} else if (res.code === '401') {
				alert('로그인이 필요합니다.');
			} else {
				alert(res.message || '처리에 실패했습니다.');
			}
		}, 'json').fail(function () {
			alert('로그인이 필요합니다.');
		});
	});
});
