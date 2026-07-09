package com.pcwk.ehr.member.mapper;

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

import com.pcwk.ehr.mapper.MemberMapper;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * member 테이블 CRUD 매퍼 테스트.
 * member 는 다른 테이블이 FK 로 참조하는 부모라 deleteAll 로 싹 지우면 안 된다.
 * 그래서 테스트용 이메일(TEST_EMAIL) 회원만 지웠다가 등록하는 방식으로 초기화한다.
 * 실행하려면 Oracle 을 켜고 각 테스트의 @Disabled 를 떼면 된다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class MemberMapperJUnit {

	private static final Logger log = LogManager.getLogger(MemberMapperJUnit.class);
	private static final String TEST_EMAIL = "junit_member@test.local";
	private static final String TEST_NICK = "JUnitNick";

	@Autowired
	private MemberMapper memberMapper;

	private MemberVO template;

	@BeforeEach
	void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		template = new MemberVO();
		template.setEmail(TEST_EMAIL);
		template.setNickname(TEST_NICK);
		template.setPassword("password123");
		template.setIsAdmin("N");
		template.setUserIntro("JUnit 테스트 회원");
		assertNotNull(memberMapper);
	}

	@AfterEach
	void tearDown() {
		log.debug("---------------------------");
		log.debug("*AfterEach()*");
		log.debug("---------------------------");
	}

	//@Disabled
	@Test
	void beans() {
		log.debug("---------------------------");
		log.debug("*beans()*");
		log.debug("---------------------------");
		assertNotNull(memberMapper);
	}

	//@Disabled
	@Test
	void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");
		//1. TEST_EMAIL 기준 cleanup 후 0건 확인(deleteAll 금지 — FK 부모 보호)
		log.debug("1. TEST_EMAIL 기준 cleanup 후 0건 확인(deleteAll 금지 — FK 부모 보호)");
		cleanupTestMember();
		MemberVO emailParam = new MemberVO();
		emailParam.setEmail(TEST_EMAIL);
		assertEquals(0, memberMapper.countByEmail(emailParam));

		//2. 단건 등록
		log.debug("2. 단건 등록");
		int flag = memberMapper.doSave(template);
		log.debug("doSave flag={}", flag);
		assertEquals(1, flag);
		assertTrue(template.getMemberId() > 0);

		//3. 등록 결과 검증(1건)
		log.debug("3. 등록 결과 검증(1건)");
		assertEquals(1, memberMapper.countByEmail(emailParam));
	}

	//@Disabled
	@Test
	void selectByEmailAndLoginFlow() {
		log.debug("---------------------------");
		log.debug("*selectByEmailAndLoginFlow()*");
		log.debug("---------------------------");
		//1. 초기화
		log.debug("1. 초기화");
		cleanupTestMember();
		//2. 등록
		log.debug("2. 등록");
		memberMapper.doSave(template);
		//3. email 로 조회 후 저장된 비밀번호가 원문과 일치하는지 검증(암호화 제거)
		log.debug("3. email 로 조회 후 저장된 비밀번호가 원문과 일치하는지 검증(암호화 제거)");
		MemberVO param = new MemberVO();
		param.setEmail(TEST_EMAIL);
		MemberVO found = memberMapper.selectByEmail(param);

		assertNotNull(found);
		assertEquals(TEST_EMAIL, found.getEmail());
		assertEquals("password123", found.getPassword());
	}

	//@Disabled
	@Test
	void countByEmailAndNickname() {
		log.debug("---------------------------");
		log.debug("*countByEmailAndNickname()*");
		log.debug("---------------------------");
		//1. 초기화
		log.debug("1. 초기화");
		cleanupTestMember();
		//2. 등록
		log.debug("2. 등록");
		memberMapper.doSave(template);
		//3. email·nickname 중복 건수 각각 1 확인
		log.debug("3. email·nickname 중복 건수 각각 1 확인");
		MemberVO emailParam = new MemberVO();
		emailParam.setEmail(TEST_EMAIL);
		assertEquals(1, memberMapper.countByEmail(emailParam));

		MemberVO nickParam = new MemberVO();
		nickParam.setNickname(TEST_NICK);
		assertEquals(1, memberMapper.countByNickname(nickParam));
	}

	//@Disabled
	@Test
	void selectMyPage() {
		log.debug("---------------------------");
		log.debug("*selectMyPage()*");
		log.debug("---------------------------");
		//1. 초기화
		log.debug("1. 초기화");
		cleanupTestMember();
		//2. 등록
		log.debug("2. 등록");
		memberMapper.doSave(template);
		//3. 마이페이지 조회 — nickname 일치, 집계 카운트 >= 0
		log.debug("3. 마이페이지 조회 — nickname 일치, 집계 카운트 >= 0");
		MemberVO param = new MemberVO();
		param.setMemberId(template.getMemberId());
		MemberVO myPage = memberMapper.selectMyPage(param);

		assertNotNull(myPage);
		assertEquals(TEST_NICK, myPage.getNickname());
		assertTrue(myPage.getArtworkCnt() >= 0);
		assertTrue(myPage.getLikeCnt() >= 0);
	}

	//@Disabled
	@Test
	void doUpdateAndDelete() {
		log.debug("---------------------------");
		log.debug("*doUpdateAndDelete()*");
		log.debug("---------------------------");
		//1. 초기화 후 등록
		log.debug("1. 초기화 후 등록");
		cleanupTestMember();
		memberMapper.doSave(template);
		int id = template.getMemberId();

		//2. nickname 수정 → doSelectOne 으로 반영 확인
		log.debug("2. nickname 수정 → doSelectOne 으로 반영 확인");
		MemberVO updateParam = new MemberVO();
		updateParam.setMemberId(id);
		updateParam.setNickname("JUnitMod");
		updateParam.setUserIntro("수정됨");
		assertEquals(1, memberMapper.doUpdate(updateParam));

		MemberVO one = new MemberVO();
		one.setMemberId(id);
		MemberVO updated = memberMapper.doSelectOne(one);
		assertNotNull(updated);
		assertEquals("JUnitMod", updated.getNickname());

		//3. 삭제 → doSelectOne == null 확인
		log.debug("3. 삭제 → doSelectOne == null 확인");
		MemberVO delParam = new MemberVO();
		delParam.setMemberId(id);
		assertEquals(1, memberMapper.doDelete(delParam));
		assertNull(memberMapper.doSelectOne(delParam));
	}

	//@Disabled
	@Test
	void doRetrieve() {
		log.debug("---------------------------");
		log.debug("*doRetrieve()*");
		log.debug("---------------------------");
		//1. 전체 목록 조회
		log.debug("1. 전체 목록 조회");
		List<MemberVO> list = memberMapper.doRetrieve(new MemberVO());
		//2. 리스트 not null 검증
		log.debug("2. 리스트 not null 검증");
		assertNotNull(list);
	}

	/**
	 * TEST_EMAIL 기준 격리 초기화.
	 * selectByEmail 로 기존 테스트 회원을 찾아 있으면 해당 memberId 로 doDelete.
	 * (deleteAll 금지 — 시드 회원/FK 부모 데이터 보호)
	 */
	private void cleanupTestMember() {
		MemberVO param = new MemberVO();
		param.setEmail(TEST_EMAIL);
		MemberVO existing = memberMapper.selectByEmail(param);
		if (existing != null) {
			MemberVO del = new MemberVO();
			del.setMemberId(existing.getMemberId());
			memberMapper.doDelete(del);
		}
	}

}
