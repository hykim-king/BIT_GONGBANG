package com.pcwk.ehr.main.service;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.mapper.ArtworkMapper;

@Service
public class MainService {

	private static final Logger log = LogManager.getLogger(MainService.class);
	private static final int POPULAR_DAYS = 30;          // 아티팩트 확정: 최근 30일
	private static final int RECOMMEND_LIKE_WEIGHT = 3;  // 가중치 공식 계수
	private static final int DEFAULT_PAGE_SIZE = 12;

	@Autowired
	private ArtworkMapper artworkMapper;

	/** 메인 홈 단일 인기 피드(CC-MAIN-01): 가중치(like*3+view) + 최근 30일, 오프셋 페이징 */
	public List<ArtworkVO> getHomeFeed(int pageNo, int pageSize) {
		ArtworkVO vo = new ArtworkVO();
		vo.setPageNo(pageNo <= 0 ? 1 : pageNo);
		vo.setPageSize(pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize);
		vo.setDays(POPULAR_DAYS);
		vo.setLikeWeight(RECOMMEND_LIKE_WEIGHT);
		return artworkMapper.selectPopular(vo);
	}

	/** 명예의전당 피드(CC-MAIN-02): 가중치 누적(기간 제한 없음), 오프셋 페이징 */
	public List<ArtworkVO> getHallFeed(int pageNo, int pageSize) {
		ArtworkVO vo = new ArtworkVO();
		vo.setPageNo(pageNo <= 0 ? 1 : pageNo);
		vo.setPageSize(pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize);
		vo.setLikeWeight(RECOMMEND_LIKE_WEIGHT);
		return artworkMapper.selectRecommend(vo);
	}

	/** 검색 화면 — totalCnt 세팅 (searchDiv는 JSP용 원본 유지) */
	public ArtworkVO prepareSearch(ArtworkVO vo) {
		vo.setSearchWord(trimSearchWord(vo.getSearchWord()));
		normalizePaging(vo);
		if (vo.getSearchWord().isEmpty()) {
			vo.setTotalCnt(0);
		} else {
			Integer cnt = artworkMapper.searchCount(toQueryVo(vo));
			vo.setTotalCnt(cnt == null ? 0 : cnt);
		}
		return vo;
	}

	public List<ArtworkVO> searchList(ArtworkVO vo) {
		if (vo.getSearchWord() == null || vo.getSearchWord().isEmpty()) {
			return Collections.emptyList();
		}
		normalizePaging(vo);
		return artworkMapper.search(toQueryVo(vo));
	}

	private ArtworkVO toQueryVo(ArtworkVO vo) {
		ArtworkVO query = new ArtworkVO();
		query.setSearchWord(trimSearchWord(vo.getSearchWord()));
		query.setSearchDiv(toMapperSearchDiv(vo.getSearchDiv()));
		query.setPageNo(vo.getPageNo());
		query.setPageSize(vo.getPageSize());
		return query;
	}

	private void normalizePaging(ArtworkVO vo) {
		if (vo.getPageNo() <= 0) {
			vo.setPageNo(1);
		}
		if (vo.getPageSize() <= 0) {
			vo.setPageSize(DEFAULT_PAGE_SIZE);
		}
	}

	private String trimSearchWord(String word) {
		return word == null ? "" : word.trim();
	}

	/** JSP(title/content/…) → Mapper XML(1/2/3/4) */
	private String toMapperSearchDiv(String searchDiv) {
		if (searchDiv == null || searchDiv.isEmpty()) {
			return null;
		}
		switch (searchDiv) {
		case "title":
			return "1";
		case "content":
			return "2";
		case "nickname":
			return "3";
		case "category":
			return "4";
		default:
			return searchDiv;
		}
	}

}
