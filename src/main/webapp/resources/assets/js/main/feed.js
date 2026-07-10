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
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const ctx = document.body.dataset.ctx || '';
	const esc = (window.bitda && window.bitda.esc) || function (s) { return String(s == null ? '' : s); };
	const PAGE_SIZE = 12;

	window.bitda = window.bitda || {};

	/* 카드 HTML — JSP(1페이지 서버 렌더)와 동일 구조 유지 */
	function cardHtml(a, rank) {
		const thumb = a.repFileId > 0
			? '<img class="thumb" src="' + ctx + '/file/download.do?fileId=' + a.repFileId + '" alt="' + esc(a.title) + '" loading="lazy">'
			: '<div class="thumb ph">' + esc(String(a.title || '').charAt(0)) + '</div>';
		let html = '<a class="art-card" href="' + ctx + '/artwork/complete/view?artworkId=' + a.artworkId + '">';
		if (rank) { html += '<span class="rank-badge">' + rank + '위</span>'; }
		html += thumb;
		html += '<div class="meta"><div class="t">' + esc(a.title) + '</div>';
		html += '<div class="row"><span>' + esc(a.nickname) + ' · ' + esc(a.categoryNm) + '</span>';
		html += '<span class="heart">&#10084; ' + a.likeCount + '</span></div></div></a>';
		return html;
	}
	window.bitda.cardHtml = cardHtml;

	document.querySelectorAll('.masonry-feed').forEach(function (feed) {
		const endpoint = feed.dataset.endpoint;
		const useRank = String(feed.dataset.rank) === 'true';
		const grid = feed.querySelector('.masonry');
		const sentinel = feed.querySelector('.feed-sentinel');
		let pageNo = 1;                                   /* 1페이지는 서버 렌더 */
		let loading = false;
		let done = grid.querySelectorAll('.art-card').length < PAGE_SIZE;   /* 첫 장부터 부족하면 끝 */

		function updateSentinel() {
			if (sentinel) { sentinel.textContent = done ? '작품을 모두 확인했습니다' : '스크롤하면 더 불러옵니다…'; }
		}
		updateSentinel();

		function loadNext() {
			if (loading || done) { return; }
			loading = true;
			$.post(ctx + endpoint, { pageNo: pageNo + 1, pageSize: PAGE_SIZE }, function (res) {
				if (res.code !== '200') { loading = false; return; }
				const list = res.data || [];
				pageNo += 1;
				const base = grid.querySelectorAll('.art-card').length;
				let html = '';
				for (let i = 0; i < list.length; i++) {
					html += cardHtml(list[i], useRank ? (base + i + 1) : 0);
				}
				grid.insertAdjacentHTML('beforeend', html);
				if (list.length < PAGE_SIZE) { done = true; }
				updateSentinel();
				loading = false;
			}, 'json').fail(function () { loading = false; });
		}

		if ('IntersectionObserver' in window && sentinel) {
			new IntersectionObserver(function (entries) {
				if (entries[0].isIntersecting) { loadNext(); }
			}, { rootMargin: '300px' }).observe(sentinel);
		}
	});
});
