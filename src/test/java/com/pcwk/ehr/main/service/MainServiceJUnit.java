package com.pcwk.ehr.main.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.pcwk.ehr.artwork.domain.ArtworkVO;

/**
 * main 서비스(MainService) 테스트 — 메인화면 추천/인기/최신 + 검색.
 * main 은 자체 매퍼가 없어(ArtworkMapper 재사용) 서비스만 테스트한다.
 * 읽기 전용이라, 조회가 예외 없이 not-null 목록을 주고 섹션 상한(8) 이내인지만 확인한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/spring/root-context.xml",
        "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class MainServiceJUnit {

    private static final Logger log = LogManager.getLogger(MainServiceJUnit.class);

    // 메인 섹션 상한 (MainService.MAIN_SECTION_SIZE 와 동일)
    private static final int MAIN_SECTION_SIZE = 8;
    // DB 에 존재하는(시드) 검색어 전제 — 읽기전용이므로 데이터 변경 없음
    private static final String TEST_SEARCH_WORD = "도자기";

    @Autowired
    private MainService mainService;

    @BeforeEach
    public void setUp() {
        log.debug("---------------------------");
        log.debug("*BeforeEach()*");
        log.debug("---------------------------");
        assertNotNull(mainService); // DI 주입 확인
    }

    @AfterEach
    public void tearDown() {
        log.debug("---------------------------");
        log.debug("*AfterEach()*");
        log.debug("---------------------------");
    }

    /** 1. 빈 주입 확인 */
    //@Disabled
    @Test
    public void beans() {
        log.debug("---------------------------");
        log.debug("*beans()*");
        log.debug("---------------------------");
        assertNotNull(mainService);
    }

    /** 2. 메인 데이터 묶음 조회 (recommend/popular/latest 키 포함) */
    //@Disabled
    @Test
    public void getMain() {
        log.debug("---------------------------");
        log.debug("*getMain()*");
        log.debug("---------------------------");
        //1. 메인 데이터 조회
        log.debug("1. 메인 데이터 조회");
        Map<String, Object> mainData = mainService.getMain();
        log.debug("mainData={}", mainData);

        //2. Map not null 확인
        log.debug("2. Map not null 확인");
        assertNotNull(mainData);

        //3. keySet 에 3개 섹션 키가 모두 있는지 확인
        log.debug("3. keySet 에 3개 섹션 키가 모두 있는지 확인");
        assertTrue(mainData.containsKey("recommendList"));
        assertTrue(mainData.containsKey("popularList"));
        assertTrue(mainData.containsKey("latestList"));
    }

    /** 3. 추천 섹션 (not null, size<=8) */
    //@Disabled
    @Test
    public void getRecommend() {
        log.debug("---------------------------");
        log.debug("*getRecommend()*");
        log.debug("---------------------------");
        //1. 추천 목록 조회
        log.debug("1. 추천 목록 조회");
        List<ArtworkVO> list = mainService.getRecommend();
        log.debug("recommend size={}", (list == null ? null : list.size()));

        //2. not null
        log.debug("2. not null");
        assertNotNull(list);

        //3. 섹션 상한(8) 이하
        log.debug("3. 섹션 상한(8) 이하");
        assertTrue(list.size() <= MAIN_SECTION_SIZE);
    }

    /** 4. 인기 섹션 (not null, size<=8) */
    //@Disabled
    @Test
    public void getPopular() {
        log.debug("---------------------------");
        log.debug("*getPopular()*");
        log.debug("---------------------------");
        //1. 인기 목록 조회
        log.debug("1. 인기 목록 조회");
        List<ArtworkVO> list = mainService.getPopular();
        log.debug("popular size={}", (list == null ? null : list.size()));

        //2. not null
        log.debug("2. not null");
        assertNotNull(list);

        //3. 섹션 상한(8) 이하
        log.debug("3. 섹션 상한(8) 이하");
        assertTrue(list.size() <= MAIN_SECTION_SIZE);
    }

    /** 5. 최신 섹션 (not null, size<=8) */
    //@Disabled
    @Test
    public void getLatest() {
        log.debug("---------------------------");
        log.debug("*getLatest()*");
        log.debug("---------------------------");
        //1. 최신 목록 조회
        log.debug("1. 최신 목록 조회");
        List<ArtworkVO> list = mainService.getLatest();
        log.debug("latest size={}", (list == null ? null : list.size()));

        //2. not null
        log.debug("2. not null");
        assertNotNull(list);

        //3. 섹션 상한(8) 이하
        log.debug("3. 섹션 상한(8) 이하");
        assertTrue(list.size() <= MAIN_SECTION_SIZE);
    }

    /** 6. 검색 준비 (totalCnt 세팅) */
    //@Disabled
    @Test
    public void prepareSearch() {
        log.debug("---------------------------");
        log.debug("*prepareSearch()*");
        log.debug("---------------------------");
        //1. 검색어 세팅한 VO 준비
        log.debug("1. 검색어 세팅한 VO 준비");
        ArtworkVO vo = new ArtworkVO();
        vo.setSearchWord(TEST_SEARCH_WORD);

        //2. 검색 준비 호출 (totalCnt 계산)
        log.debug("2. 검색 준비 호출 (totalCnt 계산)");
        ArtworkVO result = mainService.prepareSearch(vo);
        log.debug("prepareSearch totalCnt={}", result.getTotalCnt());

        //3. 반환 not null + totalCnt >= 0
        log.debug("3. 반환 not null + totalCnt >= 0");
        assertNotNull(result);
        assertTrue(result.getTotalCnt() >= 0);
    }

    /** 7. 검색 목록 (not null) */
    //@Disabled
    @Test
    public void searchList() {
        log.debug("---------------------------");
        log.debug("*searchList()*");
        log.debug("---------------------------");
        //1. 검색어 세팅한 VO 준비
        log.debug("1. 검색어 세팅한 VO 준비");
        ArtworkVO vo = new ArtworkVO();
        vo.setSearchWord(TEST_SEARCH_WORD);

        //2. 검색 목록 조회
        log.debug("2. 검색 목록 조회");
        List<ArtworkVO> list = mainService.searchList(vo);
        log.debug("searchList size={}", (list == null ? null : list.size()));

        //3. not null
        log.debug("3. not null");
        assertNotNull(list);
    }

}
