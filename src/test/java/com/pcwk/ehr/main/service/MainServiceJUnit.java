package com.pcwk.ehr.main.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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
 * main 서비스(MainService) 테스트 — 메인 홈 피드/명예의전당 피드 + 검색.
 * (2단계 개정: 3섹션 구조 → 단일 인기 피드(CC-MAIN-01) + 명예의전당(CC-MAIN-02))
 * main 은 자체 매퍼가 없어(ArtworkMapper 재사용) 서비스만 테스트한다.
 * 읽기 전용이라, 조회가 예외 없이 not-null 목록을 주고 페이지 상한 이내인지만 확인한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/spring/root-context.xml",
        "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class MainServiceJUnit {

    private static final Logger log = LogManager.getLogger(MainServiceJUnit.class);

    // 피드 페이지 크기 (index/hall 첫 페이지와 동일)
    private static final int FEED_PAGE_SIZE = 12;
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

    /** 2. 메인 홈 피드 (가중치+최근30일, not null, size<=pageSize) */
    //@Disabled
    @Test
    public void getHomeFeed() {
        log.debug("---------------------------");
        log.debug("*getHomeFeed()*");
        log.debug("---------------------------");
        //1. 홈 피드 1페이지 조회
        log.debug("1. 홈 피드 1페이지 조회");
        List<ArtworkVO> list = mainService.getHomeFeed(1, FEED_PAGE_SIZE);
        log.debug("homeFeed size={}", (list == null ? null : list.size()));

        //2. not null
        log.debug("2. not null");
        assertNotNull(list);

        //3. 페이지 상한 이하
        log.debug("3. 페이지 상한 이하");
        assertTrue(list.size() <= FEED_PAGE_SIZE);
    }

    /** 3. 명예의전당 피드 (가중치 누적, not null, size<=pageSize) */
    //@Disabled
    @Test
    public void getHallFeed() {
        log.debug("---------------------------");
        log.debug("*getHallFeed()*");
        log.debug("---------------------------");
        //1. 명예의전당 1페이지 조회
        log.debug("1. 명예의전당 1페이지 조회");
        List<ArtworkVO> list = mainService.getHallFeed(1, FEED_PAGE_SIZE);
        log.debug("hallFeed size={}", (list == null ? null : list.size()));

        //2. not null
        log.debug("2. not null");
        assertNotNull(list);

        //3. 페이지 상한 이하
        log.debug("3. 페이지 상한 이하");
        assertTrue(list.size() <= FEED_PAGE_SIZE);
    }

    /** 4. 페이징 방어값 (pageNo/pageSize 0 이하 → 보정 후 정상 조회) */
    //@Disabled
    @Test
    public void getHomeFeedPagingGuard() {
        log.debug("---------------------------");
        log.debug("*getHomeFeedPagingGuard()*");
        log.debug("---------------------------");
        //1. 잘못된 페이징 값으로 조회 (0, -1)
        log.debug("1. 잘못된 페이징 값으로 조회");
        List<ArtworkVO> list = mainService.getHomeFeed(0, -1);

        //2. 보정되어 예외 없이 not null
        log.debug("2. 보정되어 예외 없이 not null");
        assertNotNull(list);
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
