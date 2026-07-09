/* ============================================================================
 * CC-MAIN-01/02 매스너리 무한스크롤 피드 — .masonry-feed 자동 마운트
 * 마크업 계약(호스트 JSP):
 *   <div class="masonry-feed" data-endpoint="/main/popular.do" data-rank="false">
 *     <div class="masonry">(서버 렌더 1페이지 카드)</div>
 *     <div class="feed-sentinel">...</div>
 *   </div>
 * 응답: MessageVO { code:'200', data: List<ArtworkVO(cardColumns)> }
 * 카드 클릭 → /artwork/complete/view?artworkId=N (완성작 피드)
 * ==========================================================================*/
$(function () {
	'use strict';

	var ctx = $('body').data('ctx') || '';
	var esc = (window.bitda && window.bitda.esc) || function (s) { return String(s == null ? '' : s); };
	var PAGE_SIZE = 12;

	window.bitda = window.bitda || {};

	/* 카드 HTML — JSP(1페이지 서버 렌더)와 동일 구조 유지 */
	function cardHtml(a, rank) {
		var thumb = a.repFileId > 0
			? '<img class="thumb" src="' + ctx + '/file/download.do?fileId=' + a.repFileId + '" alt="' + esc(a.title) + '" loading="lazy">'
			: '<div class="thumb ph">' + esc(String(a.title || '').charAt(0)) + '</div>';
		var html = '<a class="art-card" href="' + ctx + '/artwork/complete/view?artworkId=' + a.artworkId + '">';
		if (rank) { html += '<span class="rank-badge">' + rank + '위</span>'; }
		html += thumb;
		html += '<div class="meta"><div class="t">' + esc(a.title) + '</div>';
		html += '<div class="row"><span>' + esc(a.nickname) + ' · ' + esc(a.categoryNm) + '</span>';
		html += '<span class="heart">&#10084; ' + a.likeCount + '</span></div></div></a>';
		return html;
	}
	window.bitda.cardHtml = cardHtml;

	$('.masonry-feed').each(function () {
		var $feed = $(this);
		var endpoint = $feed.data('endpoint');
		var useRank = String($feed.data('rank')) === 'true';
		var $grid = $feed.find('.masonry');
		var $sentinel = $feed.find('.feed-sentinel');
		var pageNo = 1;                                   /* 1페이지는 서버 렌더 */
		var loading = false;
		var done = $grid.find('.art-card').length < PAGE_SIZE;   /* 첫 장부터 부족하면 끝 */

		function updateSentinel() {
			$sentinel.text(done ? '작품을 모두 확인했습니다' : '스크롤하면 더 불러옵니다…');
		}
		updateSentinel();

		function loadNext() {
			if (loading || done) { return; }
			loading = true;
			$.post(ctx + endpoint, { pageNo: pageNo + 1, pageSize: PAGE_SIZE }, function (res) {
				if (res.code !== '200') { loading = false; return; }
				var list = res.data || [];
				pageNo += 1;
				var base = $grid.find('.art-card').length;
				var html = '';
				for (var i = 0; i < list.length; i++) {
					html += cardHtml(list[i], useRank ? (base + i + 1) : 0);
				}
				$grid.append(html);
				if (list.length < PAGE_SIZE) { done = true; }
				updateSentinel();
				loading = false;
			}, 'json').fail(function () { loading = false; });
		}

		if ('IntersectionObserver' in window && $sentinel.length) {
			new IntersectionObserver(function (entries) {
				if (entries[0].isIntersecting) { loadNext(); }
			}, { rootMargin: '300px' }).observe($sentinel[0]);
		}
	});
});
