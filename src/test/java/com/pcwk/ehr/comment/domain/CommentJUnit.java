package com.pcwk.ehr.comment.domain;

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
import com.pcwk.ehr.mapper.CommentMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class CommentJUnit {

	private static final Logger log = LogManager.getLogger(CommentJUnit.class);

	private static final int TEST_MEMBER_ID = 1;
	private static final TargetType TEST_TARGET_TYPE = TargetType.ARTWORK;
	private static final int TEST_TARGET_ID = 99999;

	@Autowired
	private CommentMapper commentMapper;

	private CommentVO template;

	@BeforeEach
	public void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		template = new CommentVO();
		template.setMemberId(TEST_MEMBER_ID);
		template.setTargetType(TEST_TARGET_TYPE);
		template.setTargetId(TEST_TARGET_ID);
		template.setContent("JUnit 테스트 댓글");
		assertNotNull(commentMapper);
	}

	@AfterEach
	public void tearDown() {
		log.debug("---------------------------");
		log.debug("*AfterEach()*");
		log.debug("---------------------------");
	}

	private int saveTemplate() {
		int flag = commentMapper.doSave(template);
		assertEquals(1, flag);
		return template.getCommentId();
	}

	/** 1. 등록 */
	@Disabled
	@Test
	public void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");

		int flag = commentMapper.doSave(template);

		log.debug("doSave flag={}, commentId={}", flag, template.getCommentId());
		assertEquals(1, flag);
		assertTrue(template.getCommentId() > 0);
	}

	/** 2. 대상별 목록 (member JOIN) */
	@Disabled
	@Test
	public void doRetrieve() {
		log.debug("---------------------------");
		log.debug("*doRetrieve()*");
		log.debug("---------------------------");
		saveTemplate();

		CommentVO param = new CommentVO();
		param.setTargetType(TEST_TARGET_TYPE);
		param.setTargetId(TEST_TARGET_ID);
		List<CommentVO> list = commentMapper.doRetrieve(param);

		log.debug("list size={}", list.size());
		assertNotNull(list);
		assertTrue(list.size() >= 1);
		assertNotNull(list.get(0).getNickname());
	}

	/** 3. 대상별 댓글 수 */
	@Disabled
	@Test
	public void countByTarget() {
		log.debug("---------------------------");
		log.debug("*countByTarget()*");
		log.debug("---------------------------");
		saveTemplate();

		CommentVO param = new CommentVO();
		param.setTargetType(TEST_TARGET_TYPE);
		param.setTargetId(TEST_TARGET_ID);
		int cnt = commentMapper.countByTarget(param);

		log.debug("count={}", cnt);
		assertTrue(cnt >= 1);
	}

	/** 4. 단건 조회 (본인) */
	@Disabled
	@Test
	public void doSelectOne() {
		log.debug("---------------------------");
		log.debug("*doSelectOne()*");
		log.debug("---------------------------");
		int id = saveTemplate();

		CommentVO param = new CommentVO();
		param.setCommentId(id);
		param.setMemberId(TEST_MEMBER_ID);
		CommentVO outVO = commentMapper.doSelectOne(param);

		log.debug("outVO={}", outVO);
		assertNotNull(outVO);
		assertEquals(id, outVO.getCommentId());
		assertEquals(template.getContent(), outVO.getContent());
	}

	/** 5. 수정 */
	@Disabled
	@Test
	public void doUpdate() {
		log.debug("---------------------------");
		log.debug("*doUpdate()*");
		log.debug("---------------------------");
		int id = saveTemplate();

		CommentVO param = new CommentVO();
		param.setCommentId(id);
		param.setMemberId(TEST_MEMBER_ID);
		param.setContent("수정된 댓글");
		int flag = commentMapper.doUpdate(param);
		assertEquals(1, flag);

		CommentVO outVO = commentMapper.doSelectOne(param);
		log.debug("updated={}", outVO);
		assertEquals("수정된 댓글", outVO.getContent());
		assertNotNull(outVO.getModDt());
	}

	/** 6. 삭제 (본인) */
	@Disabled
	@Test
	public void doDelete() {
		log.debug("---------------------------");
		log.debug("*doDelete()*");
		log.debug("---------------------------");
		int id = saveTemplate();

		CommentVO param = new CommentVO();
		param.setCommentId(id);
		param.setMemberId(TEST_MEMBER_ID);
		int flag = commentMapper.doDelete(param);
		assertEquals(1, flag);

		CommentVO outVO = commentMapper.doSelectOne(param);
		assertNull(outVO);
	}

	/** 7. 대상별 일괄 삭제 */
	@Disabled
	@Test
	public void deleteByTarget() {
		log.debug("---------------------------");
		log.debug("*deleteByTarget()*");
		log.debug("---------------------------");
		saveTemplate();

		CommentVO param = new CommentVO();
		param.setTargetType(TEST_TARGET_TYPE);
		param.setTargetId(TEST_TARGET_ID);
		int flag = commentMapper.deleteByTarget(param);
		assertTrue(flag >= 1);

		assertEquals(0, commentMapper.countByTarget(param));
	}
	@Disabled
	@Test
	public void deleteAll() {
		log.debug("---------------------------");
		log.debug("*deleteAll()*");
		log.debug("---------------------------");
		commentMapper.deleteAll();
	}
}
