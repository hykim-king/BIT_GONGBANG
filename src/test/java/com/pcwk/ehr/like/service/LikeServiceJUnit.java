package com.pcwk.ehr.like.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

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
 * like 서비스(LikeService) 테스트 — 좋아요 토글.
 * toggle 을 한 번 누르면 좋아요 ON(count=1), 다시 누르면 OFF(count=0) 가 되는지 확인한다.
 * 초기화·검증 보조는 매퍼로 하고, 테스트용 대상(targetId=99999)의 좋아요만 비운다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class LikeServiceJUnit {

	private static final Logger log = LogManager.getLogger(LikeServiceJUnit.class);

	// member_id, target_id 는 DB 에 이미 존재하는 값이어야 한다(FK).
	private static final int TEST_MEMBER_ID = 1;
	private static final TargetType TEST_TARGET_TYPE = TargetType.ARTWORK;
	private static final int TEST_TARGET_ID = 99999;

	@Autowired
	private LikeService likeService;

	@Autowired
	private LikeMapper likeMapper;

	private LikeVO template;

	@BeforeEach
	public void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		assertNotNull(likeService);
		assertNotNull(likeMapper);

		// 테스트 스코프(대상키) 격리 초기화: 매퍼로 이 대상의 좋아요를 모두 제거하고 시작
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
		assertNotNull(likeService);
		assertNotNull(likeMapper);
	}

	/** 1. 좋아요 토글 (on -> off) */
	//@Disabled
	@Test
	public void toggle() {
		log.debug("---------------------------");
		log.debug("*toggle()*");
		log.debug("---------------------------");
		//1. 초기화(setUp) 후 count==0 확인
		//2. 1차 toggle -> liked==true, count==1
		//3. 2차 toggle -> liked==false, count==0

		//1.
		log.debug("1. 초기화(setUp) 후 count==0 확인");
		assertEquals(0, likeMapper.countByTarget(targetParam()));

		//2.
		log.debug("2. 1차 toggle -> liked==true, count==1");
		Map<String, Object> res1 = likeService.toggle(template);
		log.debug("toggle#1={}", res1);
		assertNotNull(res1);
		assertTrue((Boolean) res1.get("liked"));
		assertEquals(Integer.valueOf(1), (Integer) res1.get("count"));

		//3.
		log.debug("3. 2차 toggle -> liked==false, count==0");
		Map<String, Object> res2 = likeService.toggle(template);
		log.debug("toggle#2={}", res2);
		assertNotNull(res2);
		assertFalse((Boolean) res2.get("liked"));
		assertEquals(Integer.valueOf(0), (Integer) res2.get("count"));
	}

	/** 2. 대상별 좋아요 수 */
	//@Disabled
	@Test
	public void countByTarget() {
		log.debug("---------------------------");
		log.debug("*countByTarget()*");
		log.debug("---------------------------");
		//1. 초기화(setUp) 후 count==0 확인
		//2. 1차 toggle(등록)
		//3. 서비스 countByTarget>=1 확인

		//1.
		log.debug("1. 초기화(setUp) 후 count==0 확인");
		assertEquals(0, likeService.countByTarget(targetParam()));

		//2.
		log.debug("2. 1차 toggle(등록)");
		likeService.toggle(template);

		//3.
		log.debug("3. 서비스 countByTarget>=1 확인");
		int cnt = likeService.countByTarget(targetParam());
		log.debug("count={}", cnt);
		assertTrue(cnt >= 1);
	}

	/** 3. 회원별 좋아요 목록 */
	//@Disabled
	@Test
	public void selectByMember() {
		log.debug("---------------------------");
		log.debug("*selectByMember()*");
		log.debug("---------------------------");
		//1. 초기화(setUp)
		//2. 1차 toggle(등록)
		//3. 서비스 회원별 목록 size>=1 확인

		//2.
		log.debug("2. 1차 toggle(등록)");
		likeService.toggle(template);

		//3.
		log.debug("3. 서비스 회원별 목록 size>=1 확인");
		LikeVO param = new LikeVO();
		param.setMemberId(TEST_MEMBER_ID);
		List<LikeVO> list = likeService.selectByMember(param);

		log.debug("list size={}", list.size());
		assertNotNull(list);
		assertTrue(list.size() >= 1);
	}

	/** 4. 대상별 일괄 삭제 */
	//@Disabled
	@Test
	public void deleteByTarget() {
		log.debug("---------------------------");
		log.debug("*deleteByTarget()*");
		log.debug("---------------------------");
		//1. 초기화(setUp)
		//2. 1차 toggle(등록) 후 count>=1 확인
		//3. 서비스 대상별 일괄삭제(flag>=1)
		//4. 삭제 후 count==0 확인

		//2.
		log.debug("2. 1차 toggle(등록) 후 count>=1 확인");
		likeService.toggle(template);
		assertTrue(likeService.countByTarget(targetParam()) >= 1);

		//3.
		log.debug("3. 서비스 대상별 일괄삭제(flag>=1)");
		int flag = likeService.deleteByTarget(targetParam());
		assertTrue(flag >= 1);

		//4.
		log.debug("4. 삭제 후 count==0 확인");
		assertEquals(0, likeService.countByTarget(targetParam()));
	}
}
