package com.pcwk.ehr.like.mapper;

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
import com.pcwk.ehr.like.domain.LikeVO;
import com.pcwk.ehr.mapper.LikeMapper;

/**
 * board_like 테이블 매퍼 테스트 — 좋아요 등록/조회/삭제.
 * 좋아요가 한 테이블에 섞여 있어, 테스트용 대상(targetType=ARTWORK, targetId=99999)의 좋아요만 비우고 검증한다.
 * 실행하려면 Oracle 을 켜고 각 테스트의 @Disabled 를 떼면 된다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class LikeMapperJUnit {

	private static final Logger log = LogManager.getLogger(LikeMapperJUnit.class);

	// member_id, target_id 는 DB 에 이미 존재하는 값이어야 한다(FK).
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
		assertNotNull(likeMapper);

		// 테스트 스코프(대상키) 격리 초기화: 이 대상의 좋아요를 모두 제거하고 시작
		template = new LikeVO();
		template.setMemberId(TEST_MEMBER_ID);
		template.setTargetType(TEST_TARGET_TYPE);
		template.setTargetId(TEST_TARGET_ID);
		likeMapper.deleteByTarget(template);
	}

	@AfterEach
	public void tearDown() {
		log.debug("---------------------------");
		log.debug("*AfterEach()*");
		log.debug("---------------------------");
		// 스코프 정리: 남은 좋아요 제거
		likeMapper.deleteByTarget(template);
	}

	/** 대상키(targetType+targetId) 로 count 조회용 파라미터 */
	private LikeVO targetParam() {
		LikeVO param = new LikeVO();
		param.setTargetType(TEST_TARGET_TYPE);
		param.setTargetId(TEST_TARGET_ID);
		return param;
	}

	//@Disabled
	@Test
	public void beans() {
		log.debug("---------------------------");
		log.debug("*beans()*");
		log.debug("---------------------------");
		assertNotNull(likeMapper);
	}

	/** 1. 등록 */
	//@Disabled
	@Test
	public void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");
		//1. 대상 삭제(초기화) - setUp 에서 deleteByTarget 수행
		//2. 초기 count==0 확인
		//3. 단건등록(flag==1, likeId>0)
		//4. 등록 후 count>=1 확인

		//2.
		log.debug("2. 초기 count==0 확인");
		assertEquals(0, likeMapper.countByTarget(targetParam()));

		//3.
		log.debug("3. 단건등록(flag==1, likeId>0)");
		int flag = likeMapper.doSave(template);
		log.debug("doSave flag={}, likeId={}", flag, template.getLikeId());
		assertEquals(1, flag);
		assertTrue(template.getLikeId() > 0);

		//4.
		log.debug("4. 등록 후 count>=1 확인");
		assertTrue(likeMapper.countByTarget(targetParam()) >= 1);
	}

	/** 2. 단건 조회 (member+target 로 조회 후 비교) */
	//@Disabled
	@Test
	public void doSelectOne() {
		log.debug("---------------------------");
		log.debug("*doSelectOne()*");
		log.debug("---------------------------");
		//1. 초기화(setUp) 후 count==0 확인
		//2. 단건등록
		//3. member+target 로 단건조회 후 필드 비교

		//1.
		log.debug("1. 초기화(setUp) 후 count==0 확인");
		assertEquals(0, likeMapper.countByTarget(targetParam()));

		//2.
		log.debug("2. 단건등록");
		int flag = likeMapper.doSave(template);
		assertEquals(1, flag);

		//3.
		log.debug("3. member+target 로 단건조회 후 필드 비교");
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
	//@Disabled
	@Test
	public void countByTarget() {
		log.debug("---------------------------");
		log.debug("*countByTarget()*");
		log.debug("---------------------------");
		//1. 초기화(setUp) 후 count==0 확인
		//2. 단건등록
		//3. 대상별 count>=1 확인

		//1.
		log.debug("1. 초기화(setUp) 후 count==0 확인");
		assertEquals(0, likeMapper.countByTarget(targetParam()));

		//2.
		log.debug("2. 단건등록");
		int flag = likeMapper.doSave(template);
		assertEquals(1, flag);

		//3.
		log.debug("3. 대상별 count>=1 확인");
		int cnt = likeMapper.countByTarget(targetParam());
		log.debug("count={}", cnt);
		assertTrue(cnt >= 1);
	}

	/** 4. 회원별 좋아요 목록 */
	//@Disabled
	@Test
	public void selectByMember() {
		log.debug("---------------------------");
		log.debug("*selectByMember()*");
		log.debug("---------------------------");
		//1. 초기화(setUp)
		//2. 단건등록
		//3. 회원별 목록 조회 size>=1 확인

		//2.
		log.debug("2. 단건등록");
		int flag = likeMapper.doSave(template);
		assertEquals(1, flag);

		//3.
		log.debug("3. 회원별 목록 조회 size>=1 확인");
		LikeVO param = new LikeVO();
		param.setMemberId(TEST_MEMBER_ID);
		List<LikeVO> list = likeMapper.selectByMember(param);

		log.debug("list size={}", list.size());
		assertNotNull(list);
		assertTrue(list.size() >= 1);
	}

	/** 5. 삭제 (토글 off) — 삭제 후 doSelectOne==null */
	//@Disabled
	@Test
	public void doDelete() {
		log.debug("---------------------------");
		log.debug("*doDelete()*");
		log.debug("---------------------------");
		//1. 초기화(setUp) 후 count==0 확인
		//2. 단건등록
		//3. 단건삭제(flag==1)
		//4. 삭제 후 doSelectOne==null 확인

		//1.
		log.debug("1. 초기화(setUp) 후 count==0 확인");
		assertEquals(0, likeMapper.countByTarget(targetParam()));

		//2.
		log.debug("2. 단건등록");
		int saveFlag = likeMapper.doSave(template);
		assertEquals(1, saveFlag);

		//3.
		log.debug("3. 단건삭제(flag==1)");
		LikeVO param = new LikeVO();
		param.setMemberId(TEST_MEMBER_ID);
		param.setTargetType(TEST_TARGET_TYPE);
		param.setTargetId(TEST_TARGET_ID);
		int flag = likeMapper.doDelete(param);
		assertEquals(1, flag);

		//4.
		log.debug("4. 삭제 후 doSelectOne==null 확인");
		LikeVO outVO = likeMapper.doSelectOne(param);
		assertNull(outVO);
	}

	/** 6. 대상별 일괄 삭제 — 후 countByTarget==0 */
	//@Disabled
	@Test
	public void deleteByTarget() {
		log.debug("---------------------------");
		log.debug("*deleteByTarget()*");
		log.debug("---------------------------");
		//1. 초기화(setUp)
		//2. 단건등록 후 count>=1 확인
		//3. 대상별 일괄삭제(flag>=1)
		//4. 삭제 후 count==0 확인

		//2.
		log.debug("2. 단건등록 후 count>=1 확인");
		int saveFlag = likeMapper.doSave(template);
		assertEquals(1, saveFlag);
		assertTrue(likeMapper.countByTarget(targetParam()) >= 1);

		//3.
		log.debug("3. 대상별 일괄삭제(flag>=1)");
		int flag = likeMapper.deleteByTarget(targetParam());
		assertTrue(flag >= 1);

		//4.
		log.debug("4. 삭제 후 count==0 확인");
		assertEquals(0, likeMapper.countByTarget(targetParam()));
	}
}
