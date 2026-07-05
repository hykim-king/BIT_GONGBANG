package com.pcwk.ehr.like.domain;

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
import com.pcwk.ehr.mapper.LikeMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class LikeJUnit {

	private static final Logger log = LogManager.getLogger(LikeJUnit.class);

	private static final int TEST_MEMBER_ID = 1;
	private static final TargetType TEST_TARGET_TYPE = TargetType.ARTWORK;
	private static final int TEST_TARGET_ID = 99999;

	@Autowired
	private LikeMapper likeMapper;

	private LikeVO template;

	@BeforeEach
	public void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		template = new LikeVO();
		template.setMemberId(TEST_MEMBER_ID);
		template.setTargetType(TEST_TARGET_TYPE);
		template.setTargetId(TEST_TARGET_ID);
		assertNotNull(likeMapper);
	}

	@AfterEach
	public void tearDown() {
		log.debug("---------------------------");
		log.debug("*AfterEach()*");
		log.debug("---------------------------");
	}

	private int saveTemplate() {
		int flag = likeMapper.doSave(template);
		assertEquals(1, flag);
		return template.getLikeId();
	}

	/** 1. 등록 */
	@Disabled
	@Test
	public void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");

		int flag = likeMapper.doSave(template);

		log.debug("doSave flag={}, likeId={}", flag, template.getLikeId());
		assertEquals(1, flag);
		assertTrue(template.getLikeId() > 0);
	}

	/** 2. 중복 확인용 단건 조회 */
	@Disabled
	@Test
	public void doSelectOne() {
		log.debug("---------------------------");
		log.debug("*doSelectOne()*");
		log.debug("---------------------------");
		saveTemplate();

		LikeVO param = new LikeVO();
		param.setMemberId(TEST_MEMBER_ID);
		param.setTargetType(TEST_TARGET_TYPE);
		param.setTargetId(TEST_TARGET_ID);
		LikeVO outVO = likeMapper.doSelectOne(param);

		log.debug("outVO={}", outVO);
		assertNotNull(outVO);
		assertEquals(TEST_MEMBER_ID, outVO.getMemberId());
		assertEquals(TEST_TARGET_TYPE, outVO.getTargetType());
		assertEquals(TEST_TARGET_ID, outVO.getTargetId());
	}

	/** 3. 대상별 좋아요 수 */
	@Disabled
	@Test
	public void countByTarget() {
		log.debug("---------------------------");
		log.debug("*countByTarget()*");
		log.debug("---------------------------");
		saveTemplate();

		LikeVO param = new LikeVO();
		param.setTargetType(TEST_TARGET_TYPE);
		param.setTargetId(TEST_TARGET_ID);
		int cnt = likeMapper.countByTarget(param);

		log.debug("count={}", cnt);
		assertTrue(cnt >= 1);
	}

	/** 4. 회원별 좋아요 목록 */
	@Disabled
	@Test
	public void selectByMember() {
		log.debug("---------------------------");
		log.debug("*selectByMember()*");
		log.debug("---------------------------");
		saveTemplate();

		LikeVO param = new LikeVO();
		param.setMemberId(TEST_MEMBER_ID);
		List<LikeVO> list = likeMapper.selectByMember(param);

		log.debug("list size={}", list.size());
		assertNotNull(list);
		assertTrue(list.size() >= 1);
	}

	/** 5. 삭제 (토글 off) */
	@Disabled
	@Test
	public void doDelete() {
		log.debug("---------------------------");
		log.debug("*doDelete()*");
		log.debug("---------------------------");
		saveTemplate();

		LikeVO param = new LikeVO();
		param.setMemberId(TEST_MEMBER_ID);
		param.setTargetType(TEST_TARGET_TYPE);
		param.setTargetId(TEST_TARGET_ID);
		int flag = likeMapper.doDelete(param);
		assertEquals(1, flag);

		LikeVO outVO = likeMapper.doSelectOne(param);
		assertNull(outVO);
	}

	/** 6. 대상별 일괄 삭제 */
	@Disabled
	@Test
	public void deleteByTarget() {
		log.debug("---------------------------");
		log.debug("*deleteByTarget()*");
		log.debug("---------------------------");
		saveTemplate();

		LikeVO param = new LikeVO();
		param.setTargetType(TEST_TARGET_TYPE);
		param.setTargetId(TEST_TARGET_ID);
		int flag = likeMapper.deleteByTarget(param);
		assertTrue(flag >= 1);

		assertEquals(0, likeMapper.countByTarget(param));
	}
	@Disabled
	@Test
	public void deleteAll() {
		log.debug("---------------------------");
		log.debug("*deleteAll()*");
		log.debug("---------------------------");
		likeMapper.deleteAll();
	}
}
