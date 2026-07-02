package com.pcwk.ehr.artwork.domain;
 
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
 
import com.pcwk.ehr.mapper.ArtworkMapper;
 
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
                "JUnit 테스트 작품", "JUnit 본문 내용", 0, null, null, null);
        assertNotNull(artworkMapper); // DI 주입 확인
    }
 
    @AfterEach
    public void tearDown() {
        log.debug("---------------------------");
        log.debug("*AfterEach()*");
        log.debug("---------------------------");
    }
 
    /** 방금 등록한(가장 최신) artwork_id 조회 헬퍼 (doRetrieve = artwork_id DESC 정렬) */
    private int getLatestId() {
        ArtworkVO param = new ArtworkVO();
        param.setIsStatus(TEST_STATUS);
        List<ArtworkVO> list = artworkMapper.doRetrieve(param);
        assertNotNull(list);
        return list.isEmpty() ? 0 : list.get(0).getArtworkId();
    }
 
    /** 1. 등록 */
    //@Disabled
    @Test
    public void doSave() {
        log.debug("---------------------------");
        log.debug("*doSave()*");
        log.debug("---------------------------");
        
        artworkMapper.deleteAll(template);
        
        int flag = artworkMapper.doSave(template);
        
        log.debug("doSave flag={}", flag);
        assertEquals(1, flag);
    }
 
    /** 2. 목록 (is_status 분기) */
    @Disabled
    @Test
    public void doRetrieve() {
        log.debug("---------------------------");
        log.debug("*doRetrieve()*");
        log.debug("---------------------------");
        artworkMapper.doSave(template); // 최소 1건 보장
 
        ArtworkVO param = new ArtworkVO();
        param.setIsStatus(TEST_STATUS);
        List<ArtworkVO> list = artworkMapper.doRetrieve(param);
 
        log.debug("list size={}", list.size());
        assertNotNull(list);
        assertEquals(true, list.size() >= 1);
    }
 
    /** 3. 상세 */
    @Disabled
    @Test
    public void doSelectOne() {
        log.debug("---------------------------");
        log.debug("*doSelectOne()*");
        log.debug("---------------------------");
        artworkMapper.doSave(template);
        int id = getLatestId();
 
        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);
        ArtworkVO outVO = artworkMapper.doSelectOne(param);
 
        log.debug("outVO={}", outVO);
        assertNotNull(outVO);
        assertEquals(id, outVO.getArtworkId());
        assertEquals(template.getTitle(), outVO.getTitle());
    }
 
    /** 4. 수정 */
    @Disabled
    @Test
    public void doUpdate() {
        log.debug("---------------------------");
        log.debug("*doUpdate()*");
        log.debug("---------------------------");
        artworkMapper.doSave(template);
        int id = getLatestId();
 
        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);
        param.setTitle("수정된 제목");
        param.setContent("수정된 본문");
        int flag = artworkMapper.doUpdate(param);
        assertEquals(1, flag);
 
        ArtworkVO outVO = artworkMapper.doSelectOne(param);
        log.debug("updated={}", outVO);
        assertEquals("수정된 제목", outVO.getTitle());
        assertEquals("수정된 본문", outVO.getContent());
    }
 
    /** 5. 삭제 */
    @Disabled
    @Test
    public void doDelete() {
        log.debug("---------------------------");
        log.debug("*doDelete()*");
        log.debug("---------------------------");
        artworkMapper.doSave(template);
        int id = getLatestId();
 
        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);
        int flag = artworkMapper.doDelete(param);
        assertEquals(1, flag);
 
        ArtworkVO outVO = artworkMapper.doSelectOne(param);
        assertNull(outVO); // 삭제 후 조회 → null
    }
    
    /** 6. 조회수 증가 */
    @Disabled
    @Test
    public void updateViewCount() {
        log.debug("---------------------------");
        log.debug("*updateViewCount()*");
        log.debug("---------------------------");
        artworkMapper.doSave(template);      // 조회수 대상 게시글 생성 (view_count=0)
        int id = getLatestId();

        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);

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
    @Disabled
    @Test
    public void updateStatus() {
        log.debug("---------------------------");
        log.debug("*updateStatus()*");
        log.debug("---------------------------");
        artworkMapper.doSave(template);      // TEST_STATUS = 'N' 로 등록
        int id = getLatestId();

        ArtworkVO param = new ArtworkVO();
        param.setArtworkId(id);
        int flag = artworkMapper.updateStatus(param);
        assertEquals(1, flag);

        ArtworkVO outVO = artworkMapper.doSelectOne(param);
        log.debug("after={}", outVO);
        assertEquals("Y", outVO.getIsStatus());   // 완성 전환됨
        assertNotNull(outVO.getCompDt());          // 완성일 세팅됨
    }
    
    /** 8. 목록 총건수 (is_status 조건부) */
    @Disabled
    @Test
    public void selectCount() {
        log.debug("---------------------------");
        log.debug("*selectCount()*");
        log.debug("---------------------------");
        artworkMapper.doSave(template);      // 'N' 최소 1건 보장

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
}
