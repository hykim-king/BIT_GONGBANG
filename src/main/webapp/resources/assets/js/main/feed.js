/* ============================================================================
 * CC-MAIN-01/02 매스너리 무한스크롤 피드 — .masonry-feed 자동 마운트
 * 마크업 계약(호스트 JSP):
 *   <div class="masonry-feed" data-endpoint="/main/popular.do" data-rank="false">
 *     <div class="masonry">(서버 렌더 1페이지 카드)</div>
 *     <div class="feed-sentinel">...</div>
 *   </div>
 *   data-rank="true" 인 피드는 data-rank-limit="10" 처럼 몇 위까지 표식을 붙일지 같이 지정한다
 *   (JSP 1페이지 렌더 규칙과 여기 값이 어긋나지 않도록 이 속성 하나로 공유한다).
 * 응답: MessageVO { code:'200', data: List<ArtworkVO(cardColumns)> }
 * 카드 클릭 → /artwork/complete/view?artworkId=N (완성작 피드)
 * ==========================================================================*/
document.addEventListener('DOMContentLoaded', function () {
	'use strict';

	const ctx = document.body.dataset.ctx || '';
	const esc = (window.bitda && window.bitda.esc) || function (s) { return String(s == null ? '' : s); };
	const PAGE_SIZE = 12;

	window.bitda = window.bitda || {};

	/* 순위 표식(메달/N위)은 몇 위까지 붙일지를 호스트 JSP가 data-rank-limit 속성으로 정한다.
	   JSP(1페이지 서버 렌더)가 하드코딩한 숫자와 여기 하드코딩한 숫자가 따로 놀면
	   나중에 하나만 고쳤을 때 어긋나므로, 값을 이 속성 하나로만 공유한다. */

	/* 1~3위는 메달 이미지, 4~rankLimit위는 텍스트 뱃지, 그 뒤는 표식 없음. */
	function rankHtml(rank, rankLimit) {
		if (!rank || rank > rankLimit) { return ''; }
		if (rank <= 3) {
			return '<img class="rank-medal" src="' + ctx + '/resources/assets/image/rank-' + rank + '.png"'
				+ ' alt="' + rank + '위" width="34" height="34">';
		}
		return '<span class="rank-badge">' + rank + '위</span>';
	}

	/* 카드 HTML — JSP(1페이지 서버 렌더)와 동일 구조 유지.
	   rankLimit 은 순위를 매기지 않는 피드(data-rank="false")에서는 안 쓰이므로 생략 가능. */
	function cardHtml(a, rank, rankLimit) {
		const thumb = a.repFileId > 0
			? '<img class="thumb" src="' + ctx + '/file/download.do?fileId=' + a.repFileId + '" alt="' + esc(a.title) + '" loading="lazy">'
			: '<div class="thumb ph">' + esc(String(a.title || '').charAt(0)) + '</div>';
		let html = '<a class="art-card" href="' + ctx + '/artwork/complete/view?artworkId=' + a.artworkId + '">';
		html += rankHtml(rank, rankLimit);
		html += thumb;
		html += '<div class="meta"><div class="t">' + esc(a.title) + '</div>';
		html += '<div class="row"><span class="who">' + esc(a.nickname) + ' · ' + esc(a.categoryNm) + '</span>';
		html += '<span class="stats">';
		html += '<span class="view">조회 ' + a.viewCount + '</span>';
		html += '<span class="heart">&#10084; ' + a.likeCount + '</span>';
		html += '</span></div></div></a>';
		return html;
	}
	window.bitda.cardHtml = cardHtml;

	/* ---------- 매스너리 배치 ----------
	   CSS column-count 는 카드가 "세로로" 흐른다(1열을 다 채워야 2열로 넘어감).
	   그러면 인기 1~8위가 첫 "화면"이 아니라 첫 "열"만 채우고, 바로 옆 열엔 하위권 글이 올라온다.
	   그래서 컬럼을 직접 만들고 카드를 순서대로 좌→오른쪽으로 돌려 담는다.
	   1·2·3·4위가 첫 줄, 5~8위가 둘째 줄이 되어 "첫 화면 = 상위권"이 성립한다.
	   CSS 의 컬럼 수(4/3/2)와 아래 breakpoint 를 반드시 같이 맞춰야 한다. */
	function columnCount() {
		const w = window.innerWidth;
		if (w <= 760) { return 2; }
		if (w <= 1100) { return 3; }
		return 4;
	}

	/* 그리드를 컬럼으로 다시 짠다. 카드의 현재 DOM 순서(=순위 순서)는 그대로 보존된다. */
	function relayout(grid) {
		const cards = Array.prototype.slice.call(grid.querySelectorAll('.art-card'));
		const n = columnCount();
		grid.innerHTML = '';
		const cols = [];
		for (let i = 0; i < n; i++) {
			const col = document.createElement('div');
			col.className = 'masonry-col';
			grid.appendChild(col);
			cols.push(col);
		}
		cards.forEach(function (card, i) { cols[i % n].appendChild(card); });
	}

	document.querySelectorAll('.masonry-feed').forEach(function (feed) {
		const endpoint = feed.dataset.endpoint;
		const useRank = String(feed.dataset.rank) === 'true';
		/* data-rank-limit 이 없는 피드(순위 없는 메인 홈)는 useRank 자체가 false 라 안 쓰인다 */
		const rankLimit = Number(feed.dataset.rankLimit) || Infinity;
		const grid = feed.querySelector('.masonry');
		const sentinel = feed.querySelector('.feed-sentinel');
		let pageNo = 1;                                   /* 1페이지는 서버 렌더 */
		let loading = false;

		/* 카드 총 개수 — 컬럼으로 나눠 담은 뒤에도 grid 전체에서 세면 되므로 그대로 쓴다. */
		function cardCount() { return grid.querySelectorAll('.art-card').length; }

		let done = cardCount() < PAGE_SIZE;                /* 첫 장부터 부족하면 끝 */

		relayout(grid);                                    /* 서버가 렌더한 1페이지를 좌→우로 재배치 */

		/* 창 크기가 바뀌면 컬럼 수가 달라지므로 다시 담는다(연타 방지용 디바운스) */
		let resizeTimer = null;
		let lastCols = columnCount();
		window.addEventListener('resize', function () {
			clearTimeout(resizeTimer);
			resizeTimer = setTimeout(function () {
				if (columnCount() !== lastCols) {
					lastCols = columnCount();
					relayout(grid);
				}
			}, 150);
		});

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
				const base = cardCount();

				/* 새로 온 카드도 이어지는 순서 그대로 좌→우로 나눠 담는다.
				   (그리드에 통째로 붙이면 컬럼 구조가 깨진다) */
				const holder = document.createElement('div');
				let html = '';
				for (let i = 0; i < list.length; i++) {
					html += cardHtml(list[i], useRank ? (base + i + 1) : 0, rankLimit);
				}
				holder.innerHTML = html;

				const cols = grid.querySelectorAll('.masonry-col');
				const newCards = Array.prototype.slice.call(holder.querySelectorAll('.art-card'));
				newCards.forEach(function (card, i) { cols[(base + i) % cols.length].appendChild(card); });

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
