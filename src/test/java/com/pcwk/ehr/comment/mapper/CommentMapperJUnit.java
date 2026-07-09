package com.pcwk.ehr.comment.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.pcwk.ehr.cmn.TargetType;
import com.pcwk.ehr.comment.domain.CommentVO;
import com.pcwk.ehr.mapper.CommentMapper;

/**
 * comment 테이블 CRUD 매퍼 테스트.
 * 여러 글의 댓글이 한 테이블에 섞여 있어, 전체 삭제 대신 테스트용 대상(targetType=ARTWORK, targetId=99999)의 댓글만 비우고 검증한다.
 * 실행하려면 Oracle 을 켜고 각 테스트의 @Disabled 를 떼면 된다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class CommentMapperJUnit {

	private static final Logger log = LogManager.getLogger(CommentMapperJUnit.class);

	private static final int TEST_MEMBER_ID = 1;
	private static final TargetType TEST_TARGET_TYPE = TargetType.ARTWORK;
	private static final int TEST_TARGET_ID = 99999;

	@Autowired
	private CommentMapper commentMapper;

	private CommentVO template;

	/** 대상 스코프 격리 키(targetType + targetId). deleteByTarget/countByTarget 용. */
	private CommentVO targetKey() {
		CommentVO key = new CommentVO();
		key.setTargetType(TEST_TARGET_TYPE);
		key.setTargetId(TEST_TARGET_ID);
		return key;
	}

	@BeforeEach
	public void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		assertNotNull(commentMapper);
		// 테스트 대상 스코프만 정리(대상 스코프 격리)
		commentMapper.deleteByTarget(targetKey());

		template = new CommentVO();
		template.setMemberId(TEST_MEMBER_ID);
		template.setTargetType(TEST_TARGET_TYPE);
		template.setTargetId(TEST_TARGET_ID);
		template.setContent("JUnit 테스트 댓글");
	}

	@AfterEach
	public void tearDown() {
		log.debug("---------------------------");
		log.debug("*AfterEach()*");
		log.debug("---------------------------");
		// 테스트 대상 스코프만 정리
		commentMapper.deleteByTarget(targetKey());
	}

	/** 대상 스코프 단건 등록 후 commentId 반환 */
	private int saveTemplate() {
		int flag = commentMapper.doSave(template);
		assertEquals(1, flag);
		return template.getCommentId();
	}

	//@Disabled
	@Test
	public void beans() {
		log.debug("---------------------------");
		log.debug("*beans()*");
		log.debug("---------------------------");
		assertNotNull(commentMapper);
	}

	/** 1. 등록 */
	//@Disabled
	@Test
	public void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");
		//1. 대상 스코프 초기화 확인
		log.debug("1. 대상 스코프 초기화 확인");
		assertEquals(0, commentMapper.countByTarget(targetKey()));
		//2. 단건 등록
		log.debug("2. 단건 등록");
		int flag = commentMapper.doSave(template);
		log.debug("doSave flag={}, commentId={}", flag, template.getCommentId());
		//3. 등록 검증(flag/commentId/count)
		log.debug("3. 등록 검증(flag/commentId/count)");
		assertEquals(1, flag);
		assertTrue(template.getCommentId() > 0);
		assertEquals(1, commentMapper.countByTarget(targetKey()));
	}

	/** 2. 대상별 목록 (member JOIN → nickname) */
	//@Disabled
	@Test
	public void doRetrieve() {
		log.debug("---------------------------");
		log.debug("*doRetrieve()*");
		log.debug("---------------------------");
		//1. 초기화 확인
		log.debug("1. 초기화 확인");
		assertEquals(0, commentMapper.countByTarget(targetKey()));
		//2. 등록
		log.debug("2. 등록");
		saveTemplate();
		//3. 대상별 목록 조회 + nickname(member JOIN) 검증
		log.debug("3. 대상별 목록 조회 + nickname(member JOIN) 검증");
		List<CommentVO> list = commentMapper.doRetrieve(targetKey());
		log.debug("list size={}", list.size());
		assertNotNull(list);
		assertTrue(list.size() >= 1);
		assertNotNull(list.get(0).getNickname());
	}

	/** 3. 대상별 댓글 수 */
	//@Disabled
	@Test
	public void countByTarget() {
		log.debug("---------------------------");
		log.debug("*countByTarget()*");
		log.debug("---------------------------");
		//1. 초기화 확인
		log.debug("1. 초기화 확인");
		assertEquals(0, commentMapper.countByTarget(targetKey()));
		//2. 등록
		log.debug("2. 등록");
		saveTemplate();
		//3. 대상별 댓글 수 검증
		log.debug("3. 대상별 댓글 수 검증");
		int cnt = commentMapper.countByTarget(targetKey());
		log.debug("count={}", cnt);
		assertTrue(cnt >= 1);
	}

	/** 4. 단건 조회 (commentId + memberId) */
	//@Disabled
	@Test
	public void doSelectOne() {
		log.debug("---------------------------");
		log.debug("*doSelectOne()*");
		log.debug("---------------------------");
		//1. 등록
		log.debug("1. 등록");
		int id = saveTemplate();
		//2. 본인 단건 조회
		log.debug("2. 본인 단건 조회");
		CommentVO param = new CommentVO();
		param.setCommentId(id);
		param.setMemberId(TEST_MEMBER_ID);
		CommentVO outVO = commentMapper.doSelectOne(param);
		//3. 검증
		log.debug("3. 검증");
		log.debug("outVO={}", outVO);
		assertNotNull(outVO);
		assertEquals(id, outVO.getCommentId());
		assertEquals(template.getContent(), outVO.getContent());
	}

	/** 5. 수정 (content 변경 → modDt 세팅) */
	//@Disabled
	@Test
	public void doUpdate() {
		log.debug("---------------------------");
		log.debug("*doUpdate()*");
		log.debug("---------------------------");
		//1. 등록
		log.debug("1. 등록");
		int id = saveTemplate();
		//2. content 수정
		log.debug("2. content 수정");
		CommentVO param = new CommentVO();
		param.setCommentId(id);
		param.setMemberId(TEST_MEMBER_ID);
		param.setContent("수정된 댓글");
		int flag = commentMapper.doUpdate(param);
		assertEquals(1, flag);
		//3. 수정 검증(content 변경 + modDt != null)
		log.debug("3. 수정 검증(content 변경 + modDt != null)");
		CommentVO outVO = commentMapper.doSelectOne(param);
		log.debug("updated={}", outVO);
		assertEquals("수정된 댓글", outVO.getContent());
		assertNotNull(outVO.getModDt());
	}

	/** 6. 삭제 (본인) */
	//@Disabled
	@Test
	public void doDelete() {
		log.debug("---------------------------");
		log.debug("*doDelete()*");
		log.debug("---------------------------");
		//1. 등록
		log.debug("1. 등록");
		int id = saveTemplate();
		//2. 본인 삭제
		log.debug("2. 본인 삭제");
		CommentVO param = new CommentVO();
		param.setCommentId(id);
		param.setMemberId(TEST_MEMBER_ID);
		int flag = commentMapper.doDelete(param);
		assertEquals(1, flag);
		//3. 삭제 검증(doSelectOne == null)
		log.debug("3. 삭제 검증(doSelectOne == null)");
		CommentVO outVO = commentMapper.doSelectOne(param);
		assertNull(outVO);
	}

	/** 7. 대상별 일괄 삭제 */
	//@Disabled
	@Test
	public void deleteByTarget() {
		log.debug("---------------------------");
		log.debug("*deleteByTarget()*");
		log.debug("---------------------------");
		//1. 등록
		log.debug("1. 등록");
		saveTemplate();
		assertTrue(commentMapper.countByTarget(targetKey()) >= 1);
		//2. 대상별 일괄 삭제
		log.debug("2. 대상별 일괄 삭제");
		int flag = commentMapper.deleteByTarget(targetKey());
		assertTrue(flag >= 1);
		//3. 삭제 검증(countByTarget == 0)
		log.debug("3. 삭제 검증(countByTarget == 0)");
		assertEquals(0, commentMapper.countByTarget(targetKey()));
	}
}
