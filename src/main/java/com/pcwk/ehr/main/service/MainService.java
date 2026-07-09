package com.pcwk.ehr.main.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.mapper.ArtworkMapper;

@Service
public class MainService {

	private static final Logger log = LogManager.getLogger(MainService.class);
	private static final int MAIN_SECTION_SIZE = 8;
	private static final int POPULAR_DAYS = 7;
	private static final int RECOMMEND_LIKE_WEIGHT = 3;
	private static final int DEFAULT_PAGE_SIZE = 12;

	@Autowired
	private ArtworkMapper artworkMapper;

	public Map<String, Object> getMain() {
		Map<String, Object> mainData = new HashMap<>();
		mainData.put("recommendList", getRecommend());
		mainData.put("popularList", getPopular());
		mainData.put("latestList", getLatest());
		return mainData;
	}

	public List<ArtworkVO> getRecommend() {
		ArtworkVO vo = new ArtworkVO();
		vo.setPageSize(MAIN_SECTION_SIZE);
		vo.setLikeWeight(RECOMMEND_LIKE_WEIGHT);
		return artworkMapper.selectRecommend(vo);
	}

	public List<ArtworkVO> getPopular() {
		ArtworkVO vo = new ArtworkVO();
		vo.setDays(POPULAR_DAYS);
		vo.setPageSize(MAIN_SECTION_SIZE);
		return artworkMapper.selectPopular(vo);
	}

	public List<ArtworkVO> getLatest() {
		ArtworkVO vo = new ArtworkVO();
		vo.setIsStatus("Y");
		vo.setPageSize(MAIN_SECTION_SIZE);
		return artworkMapper.selectMain(vo);
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
