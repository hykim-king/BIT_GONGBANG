package com.pcwk.ehr.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * comment 서비스(CommentService) 테스트 — 등록/수정/삭제 + 빈 내용 검증.
 * 동작은 서비스로 호출하고, 데이터 초기화·검증은 매퍼로 한다. 테스트용 대상(targetId=99999)의 댓글만 비운다.
 * 실행하려면 Oracle 을 켜고 각 테스트의 @Disabled 를 떼면 된다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class CommentServiceJUnit {

	private static final Logger log = LogManager.getLogger(CommentServiceJUnit.class);

	private static final int TEST_MEMBER_ID = 1;
	private static final TargetType TEST_TARGET_TYPE = TargetType.ARTWORK;
	private static final int TEST_TARGET_ID = 99999;

	@Autowired
	private CommentService commentService;

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
		assertNotNull(commentService);
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

	//@Disabled
	@Test
	public void beans() {
		log.debug("---------------------------");
		log.debug("*beans()*");
		log.debug("---------------------------");
		assertNotNull(commentService);
		assertNotNull(commentMapper);
	}

	/** 1. 등록 (유효 content → 1, commentId > 0) */
	//@Disabled
	@Test
	public void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");
		//1. 대상 스코프 초기화 확인
		log.debug("1. 대상 스코프 초기화 확인");
		assertEquals(0, commentMapper.countByTarget(targetKey()));
		//2. 서비스 등록(유효 content)
		log.debug("2. 서비스 등록(유효 content)");
		int flag = commentService.doSave(template);
		log.debug("doSave flag={}, commentId={}", flag, template.getCommentId());
		//3. 등록 검증(flag=1, commentId>0, count=1)
		log.debug("3. 등록 검증(flag=1, commentId>0, count=1)");
		assertEquals(1, flag);
		assertTrue(template.getCommentId() > 0);
		assertEquals(1, commentMapper.countByTarget(targetKey()));
	}

	/** 2. 등록 실패 (빈 content → 검증 실패 → 0, 미등록) */
	//@Disabled
	@Test
	public void doSave_빈content() {
		log.debug("---------------------------");
		log.debug("*doSave_빈content()*");
		log.debug("---------------------------");
		//1. 대상 스코프 초기화 확인
		log.debug("1. 대상 스코프 초기화 확인");
		assertEquals(0, commentMapper.countByTarget(targetKey()));
		//2. 빈 content 로 등록 시도
		log.debug("2. 빈 content 로 등록 시도");
		template.setContent("");
		int flag = commentService.doSave(template);
		log.debug("doSave flag={}", flag);
		//3. 검증 실패 검증(flag=0, 미등록)
		log.debug("3. 검증 실패 검증(flag=0, 미등록)");
		assertEquals(0, flag);
		assertEquals(0, commentMapper.countByTarget(targetKey()));
	}

	/** 3. 수정 (→ 1) */
	//@Disabled
	@Test
	public void doUpdate() {
		log.debug("---------------------------");
		log.debug("*doUpdate()*");
		log.debug("---------------------------");
		//1. 등록
		log.debug("1. 등록");
		int flag = commentService.doSave(template);
		assertEquals(1, flag);
		int id = template.getCommentId();
		//2. content 수정
		log.debug("2. content 수정");
		CommentVO param = new CommentVO();
		param.setCommentId(id);
		param.setMemberId(TEST_MEMBER_ID);
		param.setContent("수정된 댓글");
		int updated = commentService.doUpdate(param);
		//3. 수정 검증(flag=1, content 변경)
		log.debug("3. 수정 검증(flag=1, content 변경)");
		assertEquals(1, updated);
		CommentVO outVO = commentMapper.doSelectOne(param);
		log.debug("updated={}", outVO);
		assertEquals("수정된 댓글", outVO.getContent());
	}

	/** 4. 삭제 (→ 1) */
	//@Disabled
	@Test
	public void doDelete() {
		log.debug("---------------------------");
		log.debug("*doDelete()*");
		log.debug("---------------------------");
		//1. 등록
		log.debug("1. 등록");
		int flag = commentService.doSave(template);
		assertEquals(1, flag);
		int id = template.getCommentId();
		//2. 삭제
		log.debug("2. 삭제");
		CommentVO param = new CommentVO();
		param.setCommentId(id);
		param.setMemberId(TEST_MEMBER_ID);
		int deleted = commentService.doDelete(param);
		//3. 삭제 검증(flag=1, doSelectOne == null)
		log.debug("3. 삭제 검증(flag=1, doSelectOne == null)");
		assertEquals(1, deleted);
		assertNull(commentMapper.doSelectOne(param));
	}

	/** 5. 대상별 댓글 수 */
	//@Disabled
	@Test
	public void countByTarget() {
		log.debug("---------------------------");
		log.debug("*countByTarget()*");
		log.debug("---------------------------");
		//1. 초기화 확인
		log.debug("1. 초기화 확인");
		assertEquals(0, commentService.countByTarget(targetKey()));
		//2. 등록
		log.debug("2. 등록");
		int flag = commentService.doSave(template);
		assertEquals(1, flag);
		//3. 대상별 댓글 수 검증
		log.debug("3. 대상별 댓글 수 검증");
		assertTrue(commentService.countByTarget(targetKey()) >= 1);
	}

	/** 6. 대상별 일괄 삭제 */
	//@Disabled
	@Test
	public void deleteByTarget() {
		log.debug("---------------------------");
		log.debug("*deleteByTarget()*");
		log.debug("---------------------------");
		//1. 등록
		log.debug("1. 등록");
		int flag = commentService.doSave(template);
		assertEquals(1, flag);
		assertTrue(commentService.countByTarget(targetKey()) >= 1);
		//2. 대상별 일괄 삭제
		log.debug("2. 대상별 일괄 삭제");
		int deleted = commentService.deleteByTarget(targetKey());
		assertTrue(deleted >= 1);
		//3. 삭제 검증(countByTarget == 0)
		log.debug("3. 삭제 검증(countByTarget == 0)");
		assertEquals(0, commentService.countByTarget(targetKey()));
	}
}
