package com.pcwk.ehr.artworkentry.domain;

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
import com.pcwk.ehr.category.domain.CategoryVO;
import com.pcwk.ehr.mapper.ArtworkMapper;
import com.pcwk.ehr.mapper.CategoryMapper;

@ExtendWith(SpringExtension.class)
// @AfterAll 을 non-static 으로 쓰기 위해 인스턴스를 클래스당 1개로 (주입된 mapper 접근 목적)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/spring/root-context.xml",
        "file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class ArtworkEntryMapperJUnit {

    private static final Logger log = LogManager.getLogger(ArtworkEntryMapperJUnit.class);

    // 작업일지 Mapper (실제 DAO)직접 참조
    @Autowired
    private com.pcwk.ehr.mapper.ArtworkEntryMapper artworkEntryMapper;

    // artwork_entry.artwork_id 는 FK 라서, 매 테스트마다 상위 artwork 를 먼저 만들어 참조
    @Autowired
    private ArtworkMapper artworkMapper;

    // category FK 동적 확보용.
    // TEST_CATEGORY_ID 하드코딩은 위험 : CategoryMapperTest 가 category 를 deleteAll() 하면
    // 시퀀스(seq_category)는 되돌아가지 않아 해당 ID 가 영영 사라진다(ORA-02291 FK 위반).
    // 그래서 매 테스트마다 존재하는 category 를 동적으로 조회해 쓰고, 없으면 새로 만든다.
    @Autowired
    private CategoryMapper categoryMapper;

    private static final int    TEST_MEMBER_ID   = 1; // DB에 존재해야 하는 테스트용 FK

    private ArtworkEntryVO template; // 작업일지 템플릿 (각 테스트에서 재사용)
    private int hostArtworkId;       // 위 템플릿이 참조할 상위 artwork PK
    private int testCategoryId;      // setUp()에서 동적으로 확보한 category FK (하드코딩 제거)

    @BeforeEach
    public void setUp() {
        log.debug("---------------------------");
        log.debug("*BeforeEach()*");
        log.debug("---------------------------");

        // 0. category FK 동적 확보 (하드코딩 대신 실제 존재하는 ID 사용)
        testCategoryId = resolveCategoryId();

        // 1. 상위 작품(artwork) 선등록 : artwork_entry 는 artwork_id FK 를 반드시 필요로 함
        ArtworkVO hostArtwork = new ArtworkVO(0, TEST_MEMBER_ID, testCategoryId, "N",
                "작업일지 테스트용 작품", "작업일지 테스트용 본문", 0, null, null, null);
        artworkMapper.doSave(hostArtwork); // selectKey 로 hostArtwork.artworkId를 채움

        // 2. 발급된 artwork_id 를 보관 (이후 작업일지 등록 시 FK 값으로 사용)
        hostArtworkId = hostArtwork.getArtworkId();

        // 3. 작업일지 템플릿 준비 (artworkEntry=0 → selectKey 로 등록 시 채워짐)
        template = new ArtworkEntryVO(0, hostArtworkId, "JUnit 작업일지 본문", null, null);

        assertNotNull(artworkEntryMapper); // 4. Mapper DI 주입 확인
        assertNotNull(artworkMapper);      // 5. 상위 artwork Mapper DI 주입 확인
    }

    @AfterEach
    public void tearDown() {
        log.debug("---------------------------");
        log.debug("*AfterEach()*");
        log.debug("---------------------------");
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

    /**
     * 클래스의 모든 테스트가 끝난 뒤, 이 테스트가 남긴 데이터를 전부 정리.
     * 작업일지(artwork_entry)와 상위 작품(artwork)이 남으면 artwork 가 category_id 를 FK 로 참조하므로
     * 이후 CategoryMapperTest 의 DELETE FROM category(deleteAll)가 ORA-02292(child record found)로 실패한다.
     * artwork_entry 를 먼저 비우고 artwork 를 비우면 그 참조가 사라져 안전해진다.
     * (artwork 만 지워도 artwork_entry 는 FK CASCADE 로 함께 삭제되지만, 명시적으로 둘 다 정리)
     */
    @AfterAll
    public void afterAllCleanUp() {
        log.debug("---------------------------");
        log.debug("*AfterAll() - artwork_entry / artwork 데이터 정리*");
        log.debug("---------------------------");
        artworkEntryMapper.deleteAll(); // 남은 작업일지 전부 삭제
        artworkMapper.deleteAll();      // 남은 상위 작품 전부 삭제
    }

    /** 1. 등록 */
    //@Disabled
    @Test
    public void doSave() {
        log.debug("---------------------------");
        log.debug("*doSave()*");
        log.debug("---------------------------");

        // 1. 작업일지 등록 실행 (selectKey 로 PK 먼저 발급 후 INSERT)
        int flag = artworkEntryMapper.doSave(template);

        // 2. 등록 결과 건수 검증 (정상 1건)
        assertEquals(1, flag);

        // 3. selectKey 로 template.artworkEntry 에 새 PK 가 채워졌는지 확인
        assertTrue(template.getArtworkEntry() > 0);
    }

    /** 2. 목록 (작품별 작업일지, artwork_id 기준) */
    //@Disabled
    @Test
    public void doRetrieve() {
        log.debug("---------------------------");
        log.debug("*doRetrieve()*");
        log.debug("---------------------------");

        // 1. 조회 대상 작업일지 최소 1건 등록 보장
        artworkEntryMapper.doSave(template);

        // 2. 같은 상위 작품(artwork_id)의 목록 조회 파라미터 구성
        ArtworkEntryVO param = new ArtworkEntryVO();
        param.setArtworkId(hostArtworkId);

        // 3. 목록 조회 실행
        List<ArtworkEntryVO> list = artworkEntryMapper.doRetrieve(param);

        // 4. 결과 검증 (null 아님 + 방금 등록한 1건 이상 포함)
        assertNotNull(list);
        assertTrue(list.size() >= 1);

        // 5. 정렬 검증 (artwork_entry DESC → 최신이 맨 위)
        assertEquals(template.getArtworkEntry(), list.get(0).getArtworkEntry());
    }

    /** 3. 상세 (작업일지 PK 단건) */
    //@Disabled
    @Test
    public void doSelectOne() {
        log.debug("---------------------------");
        log.debug("*doSelectOne()*");
        log.debug("---------------------------");

        // 1. 조회 대상 작업일지 등록
        artworkEntryMapper.doSave(template);

        // 2. 등록 시 발급된 PK 로 상세조회 파라미터 구성
        ArtworkEntryVO param = new ArtworkEntryVO();
        param.setArtworkEntry(template.getArtworkEntry());

        // 3. 상세 조회 실행
        ArtworkEntryVO outVO = artworkEntryMapper.doSelectOne(param);

        // 4. 조회 결과 검증 (등록한 값과 일치해야 함)
        assertNotNull(outVO);
        assertEquals(template.getArtworkEntry(), outVO.getArtworkEntry());
        assertEquals(template.getContent(), outVO.getContent());
    }

    /** 4. 수정 (내용 + 수정일 갱신) */
    //@Disabled
    @Test
    public void doUpdate() {
        log.debug("---------------------------");
        log.debug("*doUpdate()*");
        log.debug("---------------------------");

        // 1. 수정 대상 작업일지 등록
        artworkEntryMapper.doSave(template);

        // 2. 수정할 내용으로 파라미터 구성 (PK + 새 content)
        ArtworkEntryVO param = new ArtworkEntryVO();
        param.setArtworkEntry(template.getArtworkEntry());
        param.setContent("수정된 작업일지 내용");

        // 3. 수정 실행
        int flag = artworkEntryMapper.doUpdate(param);

        // 4. 수정 결과 건수 검증 (정상 1건)
        assertEquals(1, flag);

        // 5. 재조회하여 내용이 실제로 반영됐는지 확인
        ArtworkEntryVO outVO = artworkEntryMapper.doSelectOne(param);
        assertEquals("수정된 작업일지 내용", outVO.getContent());

        // 6. 수정일(mod_dt) 이 채워졌는지 확인 (등록 시엔 NULL 이었음)
        assertNotNull(outVO.getModDt());
    }

    /** 5. 삭제 (단건) */
    //@Disabled
    @Test
    public void doDelete() {
        log.debug("---------------------------");
        log.debug("*doDelete()*");
        log.debug("---------------------------");

        // 1. 삭제 대상 작업일지 등록
        artworkEntryMapper.doSave(template);

        // 2. 삭제 파라미터 구성 (PK 기준)
        ArtworkEntryVO param = new ArtworkEntryVO();
        param.setArtworkEntry(template.getArtworkEntry());

        // 3. 삭제 실행
        int flag = artworkEntryMapper.doDelete(param);

        // 4. 삭제 결과 건수 검증 (정상 1건)
        assertEquals(1, flag);

        // 5. 삭제 후 재조회 시 조회되지 않아야 함 (null)
        ArtworkEntryVO outVO = artworkEntryMapper.doSelectOne(param);
        assertNull(outVO);
    }

    /** 6. 전체 삭제 (테스트 데이터 정리용, 릴리즈 시 제거 예정) */
    //@Disabled
    @Test
    public void deleteAll() {
        log.debug("---------------------------");
        log.debug("*deleteAll()*");
        log.debug("---------------------------");

        // 1. artwork_entry 테이블 전체 삭제 실행 (테스트 전용)
        artworkEntryMapper.deleteAll();
    }

}