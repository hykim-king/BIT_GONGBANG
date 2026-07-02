package com.pcwk.ehr.main.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.mapper.ArtworkMapper;

@Service
public class MainService {

    private static final int MAIN_SECTION_SIZE = 8;
    private static final int POPULAR_DAYS = 7;

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
        return artworkMapper.selectRecommend(MAIN_SECTION_SIZE);
    }

    public List<ArtworkVO> getPopular() {
        return artworkMapper.selectPopular(POPULAR_DAYS, MAIN_SECTION_SIZE);
    }

    public List<ArtworkVO> getLatest() {
        ArtworkVO vo = new ArtworkVO();
        vo.setPageNo(1);
        vo.setPageSize(MAIN_SECTION_SIZE);
        return artworkMapper.selectMain(vo);
    }

    public ArtworkVO doRetrieve(ArtworkVO vo) {
        vo.setSearchWord(trimSearchWord(vo.getSearchWord()));
        if (vo.getSearchWord().isEmpty()) {
            vo.setTotalCnt(0);
        } else {
            vo.setTotalCnt(artworkMapper.searchCount(vo));
        }
        return vo;
    }

    public List<ArtworkVO> searchList(ArtworkVO vo) {
        if (vo.getSearchWord() == null || vo.getSearchWord().isEmpty()) {
            return Collections.emptyList();
        }
        return artworkMapper.search(vo);
    }

    private String trimSearchWord(String word) {
        return word == null ? "" : word.trim();
    }
}