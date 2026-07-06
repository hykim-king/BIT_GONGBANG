package com.pcwk.ehr.artwork.mapper;

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
import com.pcwk.ehr.mapper.ArtworkMapper;

/**
 * artwork(작품 게시판) 매퍼 테스트 — CRUD + 조회수/완성전환/메인·추천·인기·검색.
 * 매번 같은 결과가 나오도록 각 테스트는 deleteAll 로 비우고 시작한다.
 * (artwork 는 아직 Service 가 없어 매퍼 테스트만 있다. Oracle 을 켜고 @Disabled 를 떼면 실행된다.)
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/spring/root-context.xml",
        "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class ArtworkMapperJUnit {

    private static final Logger log = LogManager.getLogger(ArtworkMapperJUnit.class);

    @Autowired
    private ArtworkMapper artworkMapper;

    // 테스트용 FK (DB에 존재하는 값이어야 함)
    private static final int    TEST_MEMBER_ID   = 1;
    private static final int    TEST_CATEGORY_ID = 1;
    private static final String TEST_STATUS      = "N"; // 공개작업으로 등록

    private ArtworkVO template;

    @BeforeEach
    public void setUp() {
        log.debug("---------------------------");
        log.debug("*BeforeEach()*");
        log.debug("---------------------------");
        // 생성자 순서: artworkId, memberId, categoryId, isStatus,
        //             title, content, viewCount, regDt, modDt, compDt
        template = new ArtworkVO(0, TEST_MEMBER_ID, TEST_CATEGORY_ID, TEST_STATUS,
                "JUnit 테스트 작품", "JUnit 본문", 0, null, null, null);
        assertNotNull(artworkMapper); // DI 주입 확인
    }

    @AfterEach
    public void tearDown() {
        log.debug("---------------------------");
        log.debug("*AfterEach()*");
        log.debug("---------------------------");
        // 테스트가 남긴 데이터를 치워야 다음 테스트(특히 category)의 deleteAll 이 FK 로 막히지 않는다
        artworkMapper.deleteAll();
    }

    /** 방금 등록한(가장 최신) artwork_id 조회 헬퍼 (doRetrieve = artwork_id DESC 정렬) */
    private int getLatestId() {
        ArtworkVO param = new ArtworkVO();
        param.setIsStatus(TEST_STATUS);
        List<ArtworkVO> list = artworkMapper.doRetrieve(param);
        assertNotNull(list);
        return list.isEmpty() ? 0 : list.get(0).getArtworkId();
    }

    /** 0. DI 확인 */
    //@Disabled
    @Test
    public void beans() {
        log.debug("---------------------------");
        log.debug("*beans()*");
        log.debug("---------------------------");
        assertNotNull(artworkMapper);
    }

    /** 1. 등록 */
    //@Disabled
    @Test
    public void doSave() {
        log.debug("---------------------------");
        log.debug("*doSave()*");
        log.debug("---------------------------");
        // 테스트는 항상 동일한 결과가 나와야 하므로(Test Isolation) 데이터를 초기화하고 시작
        //1. 전체삭제
        //2. 단건등록
        //3. flag 검증
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();

        log.debug("2. 단건등록");
        int flag = artworkMapper.doSave(template);

        log.debug("3. flag 검증");
        log.debug("doSave flag={}", flag);
        assertEquals(1, flag);
    }

    /** 2. 목록 (is_status 분기) */
    //@Disabled
    @Test
    public void doRetrieve() {
        log.debug("---------------------------");
        log.debug("*doRetrieve()*");
        log.debug("---------------------------");
        //1. 전체삭제 후 0건 확인
        //2. 단건등록
        //3. 목록 조회 및 검증
        log.debug("1. 전체삭제 후 0건 확인");
        artworkMapper.deleteAll();

        ArtworkVO param = new ArtworkVO();
        param.setIsStatus(TEST_STATUS);
        List<ArtworkVO> before = artworkMapper.doRetrieve(param);
        assertNotNull(before);
        assertEquals(0, before.size());

        log.debug("2. 단건등록");
        artworkMapper.doSave(template); // 최소 1건 보장

        log.debug("3. 목록 조회 및 검증");
        List<ArtworkVO> after = artworkMapper.doRetrieve(param);
        log.debug("list size={}", after.size());
        assertNotNull(after);
        assertEquals(1, after.size());
    }

    /** 3. 상세 */
    //@Disabled
    @Test
    public void doSelectOne() {
        log.debug("---------------------------");
        log.debug("*doSelectOne()*");
        log.debug("---------------------------");
        //1. 전체삭제
        //2. 단건등록 후 최신 id 조회
        //3. 상세 조회 및 비교
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 단건등록 후 최신 id 조회");
        artworkMapper.doSave(template);
        int id = getLatestId();

        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);
        log.debug("3. 상세 조회 및 비교");
        ArtworkVO outVO = artworkMapper.doSelectOne(param);

        log.debug("outVO={}", outVO);
        assertNotNull(outVO);
        assertEquals(id, outVO.getArtworkId());
        assertEquals(template.getTitle(), outVO.getTitle());
    }

    /** 4. 수정 */
    //@Disabled
    @Test
    public void doUpdate() {
        log.debug("---------------------------");
        log.debug("*doUpdate()*");
        log.debug("---------------------------");
        //1. 전체삭제
        //2. 단건등록 후 최신 id 조회
        //3. 수정 및 결과 비교
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 단건등록 후 최신 id 조회");
        artworkMapper.doSave(template);
        int id = getLatestId();

        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);
        param.setTitle("수정된 제목");
        param.setContent("수정된 본문");
        log.debug("3. 수정 및 결과 비교");
        int flag = artworkMapper.doUpdate(param);
        assertEquals(1, flag);

        ArtworkVO outVO = artworkMapper.doSelectOne(param);
        log.debug("updated={}", outVO);
        assertEquals("수정된 제목", outVO.getTitle());
        assertEquals("수정된 본문", outVO.getContent());
    }

    /** 5. 삭제 */
    //@Disabled
    @Test
    public void doDelete() {
        log.debug("---------------------------");
        log.debug("*doDelete()*");
        log.debug("---------------------------");
        //1. 전체삭제
        //2. 단건등록 후 최신 id 조회
        //3. 삭제 및 null 확인
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 단건등록 후 최신 id 조회");
        artworkMapper.doSave(template);
        int id = getLatestId();

        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);
        log.debug("3. 삭제 및 null 확인");
        int flag = artworkMapper.doDelete(param);
        assertEquals(1, flag);

        ArtworkVO outVO = artworkMapper.doSelectOne(param);
        assertNull(outVO); // 삭제 후 조회 → null
    }

    /** 6. 조회수 증가 */
    //@Disabled
    @Test
    public void updateViewCount() {
        log.debug("---------------------------");
        log.debug("*updateViewCount()*");
        log.debug("---------------------------");
        //1. 전체삭제
        //2. 단건등록(view_count=0) 후 최신 id 조회
        //3. 조회수 증가 2회 검증
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 단건등록(view_count=0) 후 최신 id 조회");
        artworkMapper.doSave(template);      // 조회수 대상 게시글 생성 (view_count=0)
        int id = getLatestId();

        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);

        log.debug("3. 조회수 증가 2회 검증");
        // 1회차: 0 -> 1
        int flag1 = artworkMapper.updateViewCount(param);
        assertEquals(1, flag1);
        ArtworkVO outVO1 = artworkMapper.doSelectOne(param);
        log.debug("1회차 viewCount={}", outVO1.getViewCount());
        assertEquals(1, outVO1.getViewCount());

        // 2회차: 같은 글 다시 불러와 1 -> 2
        int flag2 = artworkMapper.updateViewCount(param);
        assertEquals(1, flag2);
        ArtworkVO outVO2 = artworkMapper.doSelectOne(param);
        log.debug("2회차 viewCount={}", outVO2.getViewCount());
        assertEquals(2, outVO2.getViewCount());
    }

    /** 7. 완성 전환 (N -> Y, comp_dt 세팅) */
    //@Disabled
    @Test
    public void updateStatus() {
        log.debug("---------------------------");
        log.debug("*updateStatus()*");
        log.debug("---------------------------");
        //1. 전체삭제
        //2. 단건등록(TEST_STATUS='N') 후 최신 id 조회
        //3. 완성 전환 및 검증
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 단건등록(TEST_STATUS='N') 후 최신 id 조회");
        artworkMapper.doSave(template);      // TEST_STATUS = 'N' 로 등록
        int id = getLatestId();

        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);
        log.debug("3. 완성 전환 및 검증");
        int flag = artworkMapper.updateStatus(param);
        assertEquals(1, flag);

        ArtworkVO outVO = artworkMapper.doSelectOne(param);
        log.debug("after={}", outVO);
        assertEquals("Y", outVO.getIsStatus());   // 완성 전환됨
        assertNotNull(outVO.getCompDt());          // 완성일 세팅됨
    }

    /** 8. 목록 총건수 (is_status 조건부) */
    //@Disabled
    @Test
    public void selectCount() {
        log.debug("---------------------------");
        log.debug("*selectCount()*");
        log.debug("---------------------------");
        //1. 전체삭제 후 0건 확인
        //2. 'N' 1건 등록
        //3. 전체/완성 건수 비교
        log.debug("1. 전체삭제 후 0건 확인");
        artworkMapper.deleteAll();

        ArtworkVO empty = new ArtworkVO();
        int zeroCnt = artworkMapper.selectCount(empty);
        assertEquals(0, zeroCnt);

        log.debug("2. 'N' 1건 등록");
        artworkMapper.doSave(template);      // 'N' 최소 1건 보장

        log.debug("3. 전체/완성 건수 비교");
        // 전체(공개작업 관점): isStatus 미설정 -> null
        ArtworkVO all = new ArtworkVO();
        int totalCnt = artworkMapper.selectCount(all);

        // 완성만: isStatus='Y'
        ArtworkVO comp = new ArtworkVO();
        comp.setIsStatus("Y");
        int compCnt = artworkMapper.selectCount(comp);

        log.debug("totalCnt={}, compCnt={}", totalCnt, compCnt);
        assertTrue(totalCnt >= 1);        // 방금 넣은 N 1건 이상
        assertTrue(totalCnt >= compCnt);  // 전체 >= 완성
    }

    /** 9. 메인: 최신 완성작 */
    //@Disabled
    @Test
    public void selectMain() {
        log.debug("---------------------------");
        log.debug("*selectMain()*");
        log.debug("---------------------------");
        //1. 전체삭제
        //2. 완성작 3건 등록
        //3. selectMain 조회 및 검증
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 완성작 3건 등록");
        for (int i = 1; i <= 3; i++) {
            ArtworkVO vo = new ArtworkVO(0, TEST_MEMBER_ID, TEST_CATEGORY_ID, "Y",
                    "메인테스트" + i, "본문" + i, 0, null, null, null);
            artworkMapper.doSave(vo);
        }

        ArtworkVO param = new ArtworkVO();
        param.setIsStatus("Y");
        param.setPageSize(5);
        log.debug("3. selectMain 조회 및 검증");
        List<ArtworkVO> list = artworkMapper.selectMain(param);

        log.debug("selectMain size={}", list.size());
        assertNotNull(list);
        assertTrue(list.size() >= 1);
        assertTrue(list.size() <= 5);
        for (ArtworkVO vo : list) {
            assertEquals("Y", vo.getIsStatus());   // 전부 완성작
            assertNotNull(vo.getNickname());       // JOIN 확인(작성자)
        }
    }

    /** 10. 메인: 추천 */
    //@Disabled
    @Test
    public void selectRecommend() {
        log.debug("---------------------------");
        log.debug("*selectRecommend()*");
        log.debug("---------------------------");
        //1. 전체삭제
        //2. 완성작 3건 등록
        //3. selectRecommend 조회 및 검증
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 완성작 3건 등록");
        for (int i = 1; i <= 3; i++) {
            ArtworkVO vo = new ArtworkVO(0, TEST_MEMBER_ID, TEST_CATEGORY_ID, "Y",
                    "추천테스트" + i, "본문" + i, 0, null, null, null);
            artworkMapper.doSave(vo);
        }

        ArtworkVO param = new ArtworkVO();
        param.setLikeCount(2);   // 좋아요 가중치
        param.setPageSize(5);
        log.debug("3. selectRecommend 조회 및 검증");
        List<ArtworkVO> list = artworkMapper.selectRecommend(param);

        log.debug("selectRecommend size={}", list.size());
        assertNotNull(list);
        assertTrue(list.size() <= 5);
        for (ArtworkVO vo : list) {
            assertEquals("Y", vo.getIsStatus());
        }
    }

    /** 11. 메인: 인기 (최근 days일 등록 완성작 좋아요순) */
    //@Disabled
    @Test
    public void selectPopular() {
        log.debug("---------------------------");
        log.debug("*selectPopular()*");
        log.debug("---------------------------");
        //1. 전체삭제
        //2. 완성작 3건 등록 (오늘 등록 → 기간 안에 포함)
        //3. 최근 30일/7일 인기 조회 및 검증
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 완성작 3건 등록 (오늘 등록 → 기간 안에 포함)");
        for (int i = 1; i <= 3; i++) {
            ArtworkVO vo = new ArtworkVO(0, TEST_MEMBER_ID, TEST_CATEGORY_ID, "Y",
                    "인기테스트" + i, "본문" + i, 0, null, null, null);
            artworkMapper.doSave(vo);
        }

        log.debug("3. 최근 30일/7일 인기 조회 및 검증");
        // (1) 최근 30일
        ArtworkVO p30 = new ArtworkVO();
        p30.setDays(30);
        p30.setPageSize(5);
        List<ArtworkVO> list30 = artworkMapper.selectPopular(p30);
        log.debug("30일 인기 size={}", list30.size());
        assertNotNull(list30);
        assertTrue(list30.size() >= 1);     // 방금 등록분 포함
        assertTrue(list30.size() <= 5);
        for (ArtworkVO vo : list30) {
            assertEquals("Y", vo.getIsStatus());
        }

        // (2) 최근 7일 (짧은 기간도 오늘 등록분은 포함돼야 함)
        ArtworkVO p7 = new ArtworkVO();
        p7.setDays(7);
        p7.setPageSize(5);
        List<ArtworkVO> list7 = artworkMapper.selectPopular(p7);
        log.debug("7일 인기 size={}", list7.size());
        assertNotNull(list7);
        assertTrue(list7.size() >= 1);
    }

    /** 12. 통합검색 */
    //@Disabled
    @Test
    public void search() {
        log.debug("---------------------------");
        log.debug("*search()*");
        log.debug("---------------------------");
        //1. 전체삭제
        //2. 검색 대상 완성작 1건 등록
        //3. 제목검색/전체검색 검증
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 검색 대상 완성작 1건 등록");
        ArtworkVO seed = new ArtworkVO(0, TEST_MEMBER_ID, TEST_CATEGORY_ID, "Y",
                "검색전용제목ABC", "검색본문내용", 0, null, null, null);
        artworkMapper.doSave(seed);

        log.debug("3. 제목검색/전체검색 검증");
        // (1) 제목 검색 : searchDiv=1
        // search 는 내부적으로 rn BETWEEN 페이징을 쓰므로 pageNo/pageSize 를 반드시 세팅해야 한다
        // (안 하면 0/0 -> BETWEEN 1 AND 0 이 되어 결과가 항상 0건)
        ArtworkVO p1 = new ArtworkVO();
        p1.setSearchDiv("1");
        p1.setSearchWord("검색전용제목");
        p1.setPageNo(1);
        p1.setPageSize(10);
        List<ArtworkVO> byTitle = artworkMapper.search(p1);
        log.debug("제목검색 size={}", byTitle.size());
        assertNotNull(byTitle);
        assertTrue(byTitle.size() >= 1);
        assertTrue(byTitle.get(0).getTitle().contains("검색전용제목"));

        // (2) 전체 검색 : searchDiv=0 -> otherwise (제목/내용/닉네임/카테고리 OR)
        ArtworkVO p2 = new ArtworkVO();
        p2.setSearchDiv("0");
        p2.setSearchWord("검색본문내용");
        p2.setPageNo(1);
        p2.setPageSize(10);
        List<ArtworkVO> byAll = artworkMapper.search(p2);
        log.debug("전체검색 size={}", byAll.size());
        assertTrue(byAll.size() >= 1);
    }

}
