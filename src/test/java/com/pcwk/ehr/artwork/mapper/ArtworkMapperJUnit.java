package com.pcwk.ehr.artwork.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.artworkentry.domain.ArtworkEntryVO;
import com.pcwk.ehr.category.domain.CategoryVO;
import com.pcwk.ehr.mapper.ArtworkEntryMapper;
import com.pcwk.ehr.mapper.ArtworkMapper;
import com.pcwk.ehr.mapper.CategoryMapper;

/**
 * artwork(작품 게시판) 매퍼 테스트 — CRUD + 조회수/완성전환/메인·추천·인기·검색.
 * 매번 같은 결과가 나오도록 각 테스트는 deleteAll 로 비우고 시작한다.
 * (Oracle 을 켜고 @Disabled 를 떼면 실행된다.)
 */
@ExtendWith(SpringExtension.class)
// @AfterAll 을 non-static 으로 쓰기 위해 인스턴스를 클래스당 1개로 (주입된 mapper 접근 목적)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/spring/root-context.xml",
        "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class ArtworkMapperJUnit {

    private static final Logger log = LogManager.getLogger(ArtworkMapperJUnit.class);

    @Autowired
    private ArtworkMapper artworkMapper;

    // selectCount 하이브리드 필터 검증용 (artwork_entry 존재 여부 확인)
    @Autowired
    private ArtworkEntryMapper artworkEntryMapper;

    // category FK 동적 확보용.
    // TEST_CATEGORY_ID 하드코딩은 위험 : CategoryMapperTest 가 category 를 deleteAll() 하면
    // 시퀀스(seq_category)는 되돌아가지 않아 해당 ID 가 영영 사라진다(ORA-02291 FK 위반).
    // 그래서 매 테스트마다 존재하는 category 를 동적으로 조회해 쓰고, 없으면 새로 만든다.
    @Autowired
    private CategoryMapper categoryMapper;

    // 테스트용 FK (DB에 존재하는 값이어야 함)
    private static final int    TEST_MEMBER_ID = 1;
    private static final String TEST_STATUS    = "N"; // 공개작업으로 등록

    private ArtworkVO template;
    private int testCategoryId; // setUp()에서 동적으로 확보한 category FK (하드코딩 제거)

    @BeforeEach
    public void setUp() {
        log.debug("---------------------------");
        log.debug("*BeforeEach()*");
        log.debug("---------------------------");

        // category FK 동적 확보 (하드코딩 대신 실제 존재하는 ID 사용)
        testCategoryId = resolveCategoryId();

        // 생성자 순서: artworkId, memberId, categoryId, isStatus,
        //             title, content, viewCount, regDt, modDt, compDt
        template = new ArtworkVO(0, TEST_MEMBER_ID, testCategoryId, TEST_STATUS,
                "JUnit 테스트 작품", "JUnit 본문 내용", 0, null, null, null);
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

    /**
     * 클래스의 모든 테스트가 끝난 뒤, 이 테스트가 남긴 artwork 데이터를 전부 정리.
     * artwork 는 category_id 를 FK 로 참조하므로 남아 있으면
     * 이후 CategoryMapperTest 의 DELETE FROM category(deleteAll)가
     * ORA-02292(child record found)로 실패한다.
     * 여기서 artwork 를 비우면(artwork_entry 는 FK CASCADE 로 함께 삭제) 그 참조가 사라져 안전해진다.
     */
    @AfterAll
    public void afterAllCleanUp() {
        log.debug("---------------------------");
        log.debug("*AfterAll() - artwork 데이터 정리*");
        log.debug("---------------------------");
        artworkMapper.deleteAll(); // 남은 artwork 전부 삭제 (artwork_entry 는 CASCADE)
    }

    /**
     * category FK 로 쓸 category_id 를 동적으로 확보.
     * 1) 이미 등록된 category 가 있으면 그 중 첫 번째 ID 재사용 (불필요한 행 증가 방지)
     * 2) 하나도 없으면(예: CategoryMapperTest 가 방금 다 지운 경우) 새로 하나 만들어 발급받은 ID 사용
     * → 다른 테스트가 category 를 어떻게 만들고 지우든 항상 유효한 FK 를 확보한다.
     */
    private int resolveCategoryId() {
        List<CategoryVO> list = categoryMapper.doRetrieve(new CategoryVO());
        if (list != null && !list.isEmpty()) {
            return list.get(0).getCategoryId();   // 존재하는 category 재사용
        }
        CategoryVO category = new CategoryVO();
        category.setCategoryNm("JUnit");
        categoryMapper.doSave(category);          // 없으면 새로 등록(selectKey 로 categoryId 채워짐)
        return category.getCategoryId();
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

    /** 8. 목록 총건수 (하이브리드 조건부: is_status='N' 이거나 작업일지 존재) */
    //@Disabled
    @Test
    public void selectCount() {
        log.debug("---------------------------");
        log.debug("*selectCount()*");
        log.debug("---------------------------");
        artworkMapper.deleteAll();

        // 0. 다른 테스트가 남긴 데이터와 섞이지 않도록 이 테스트 전용 마커로 제목을 구분
        String marker = "SELCNT_" + System.currentTimeMillis();

        // 1. N상태 작품 등록 : 하이브리드 필터에서 무조건 포함 대상
        ArtworkVO working = new ArtworkVO(0, TEST_MEMBER_ID, testCategoryId, "N",
                marker + "_작업중", "본문", 0, null, null, null);
        artworkMapper.doSave(working);

        // 2. Y상태 + 작업일지 없음 : 완성필터(Y)에는 포함되지만 하이브리드 필터에는 제외되어야 함
        ArtworkVO compNoEntry = new ArtworkVO(0, TEST_MEMBER_ID, testCategoryId, "Y",
                marker + "_완성_일지없음", "본문", 0, null, null, null);
        artworkMapper.doSave(compNoEntry);

        // 3. Y상태 + 작업일지 있음 : 완성 전환되어도 일지가 있으므로 하이브리드 필터에도 포함되어야 함
        ArtworkVO compWithEntry = new ArtworkVO(0, TEST_MEMBER_ID, testCategoryId, "Y",
                marker + "_완성_일지있음", "본문", 0, null, null, null);
        artworkMapper.doSave(compWithEntry);
        ArtworkEntryVO entry = new ArtworkEntryVO(0, compWithEntry.getArtworkId(), "작업일지 내용", null, null);
        artworkEntryMapper.doSave(entry);

        // 4. 하이브리드 카운트 : isStatus 미설정 + marker 로 이 테스트가 만든 3건만 한정
        ArtworkVO hybridParam = new ArtworkVO();
        hybridParam.setSearchWord(marker);
        int hybridCnt = artworkMapper.selectCount(hybridParam);

        // 5. 완성 카운트 : isStatus='Y' + 동일 marker 한정
        ArtworkVO compParam = new ArtworkVO();
        compParam.setIsStatus("Y");
        compParam.setSearchWord(marker);
        int compCnt = artworkMapper.selectCount(compParam);

        log.debug("hybridCnt={}, compCnt={}", hybridCnt, compCnt);

        // 6. 하이브리드 = N(1) + Y·일지있음(1) = 2건 (Y·일지없음은 제외되어야 함)
        assertEquals(2, hybridCnt);

        // 7. 완성 카운트 = Y 2건(일지 유무 무관 전부 포함)
        assertEquals(2, compCnt);
    }

    /** 9. 메인: 최신 완성작 */
    //@Disabled
    @Test
    public void selectMain() {
        log.debug("---------------------------");
        log.debug("*selectMain()*");
        log.debug("---------------------------");
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 완성작 3건 등록");
        for (int i = 1; i <= 3; i++) {
            ArtworkVO vo = new ArtworkVO(0, TEST_MEMBER_ID, testCategoryId, "Y",
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
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 완성작 3건 등록");
        for (int i = 1; i <= 3; i++) {
            ArtworkVO vo = new ArtworkVO(0, TEST_MEMBER_ID, testCategoryId, "Y",
                    "추천테스트" + i, "본문" + i, 0, null, null, null);
            artworkMapper.doSave(vo);
        }

        ArtworkVO param = new ArtworkVO();
        param.setLikeWeight(2);   // 좋아요 가중치
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
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 완성작 3건 등록 (오늘 등록 → 기간 안에 포함)");
        for (int i = 1; i <= 3; i++) {
            ArtworkVO vo = new ArtworkVO(0, TEST_MEMBER_ID, testCategoryId, "Y",
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
        log.debug("1. 전체삭제");
        artworkMapper.deleteAll();
        log.debug("2. 검색 대상 완성작 1건 등록");
        ArtworkVO seed = new ArtworkVO(0, TEST_MEMBER_ID, testCategoryId, "Y",
                "검색전용제목ABC", "검색본문내용", 0, null, null, null);
        artworkMapper.doSave(seed);

        log.debug("3. 제목검색/전체검색 검증");
        // (1) 제목 검색 : searchDiv=1
        // search()는 ROW_NUMBER() 기반 페이징 쿼리라 pageNo/pageSize를 반드시 세팅해야 함
        // (미설정 시 기본값 0 -> rn BETWEEN 1 AND 0 이 되어 결과가 항상 0건 나옴)
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

    /** 13. 전체삭제 (테스트 데이터 정리용) */
    //@Disabled
    @Test
    public void deleteAll() {
        log.debug("---------------------------");
        log.debug("*deleteAll()*");
        log.debug("---------------------------");
        artworkMapper.deleteAll();
    }

}
