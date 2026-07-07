package com.pcwk.ehr.member.service;

import static org.junit.jupiter.api.Assertions.*;

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
 * member 서비스(MemberService) 테스트 — 가입/로그인/중복확인/마이페이지.
 * 가입은 입력값 검증(이메일 형식·비밀번호 8자 이상·비밀번호 확인 일치·닉네임 길이)을 통과해야 성공한다.
 * member 는 FK 부모라 deleteAll 금지 → 테스트용 이메일 회원만 지웠다 등록하는 식으로 초기화한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class MemberServiceJUnit {

	private static final Logger log = LogManager.getLogger(MemberServiceJUnit.class);
	private static final String TEST_EMAIL = "junit_member@test.local";
	private static final String TEST_NICK = "JUnitNick";

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberMapper memberMapper;

	private MemberVO member;

	@BeforeEach
	void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		// 가입폼 검증(email 정규식 / password>=8 / confirm==password / nickname<=10)을 통과하는 유효 VO
		member = new MemberVO();
		member.setEmail(TEST_EMAIL);
		member.setPassword("password123");
		member.setConfirmPassword("password123");
		member.setNickname(TEST_NICK);
		assertNotNull(memberService);
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
		assertNotNull(memberService);
		assertNotNull(memberMapper);
	}

	//@Disabled
	@Test
	void doSave_valid() {
		log.debug("---------------------------");
		log.debug("*doSave_valid()*");
		log.debug("---------------------------");
		// Test Isolation: 초기화 → 0건 확인 → 가입 → 저장값 검증
		//1. TEST_EMAIL cleanup 후 0건 확인
		log.debug("1. TEST_EMAIL cleanup 후 0건 확인");
		cleanupTestMember();
		MemberVO emailParam = new MemberVO();
		emailParam.setEmail(TEST_EMAIL);
		assertEquals(0, memberMapper.countByEmail(emailParam));

		//2. 유효 폼으로 서비스 가입 → 1
		log.debug("2. 유효 폼으로 서비스 가입 → 1");
		int flag = memberService.doSave(member);
		log.debug("doSave flag={}", flag);
		assertEquals(1, flag);

		//3. 저장 검증: isAdmin='N', password 가 원문 그대로 저장됨(암호화 제거)
		log.debug("3. 저장 검증: isAdmin='N', password 가 원문 그대로 저장됨(암호화 제거)");
		MemberVO found = memberMapper.selectByEmail(emailParam);
		assertNotNull(found);
		assertEquals("N", found.getIsAdmin());
		assertEquals("password123", found.getPassword());
	}

	//@Disabled
	@Test
	void doSave_invalid() {
		log.debug("---------------------------");
		log.debug("*doSave_invalid()*");
		log.debug("---------------------------");
		//1. 초기화
		log.debug("1. 초기화");
		cleanupTestMember();
		//2. 비밀번호를 8자 미만("123")으로 → 가입폼 검증 실패
		log.debug("2. 비밀번호를 8자 미만(\"123\")으로 → 가입폼 검증 실패");
		member.setPassword("123");
		member.setConfirmPassword("123");
		//3. 가입 실패(0)
		log.debug("3. 가입 실패(0)");
		int flag = memberService.doSave(member);
		log.debug("doSave(invalid) flag={}", flag);
		assertEquals(0, flag);
	}

	//@Disabled
	@Test
	void login() {
		log.debug("---------------------------");
		log.debug("*login()*");
		log.debug("---------------------------");
		//1. 초기화 후 가입
		log.debug("1. 초기화 후 가입");
		cleanupTestMember();
		assertEquals(1, memberService.doSave(member));

		//2. 정상 로그인 → not null, 세션용 반환은 password 제거(null)
		log.debug("2. 정상 로그인 → not null, 세션용 반환은 password 제거(null)");
		MemberVO loginParam = new MemberVO();
		loginParam.setEmail(TEST_EMAIL);
		loginParam.setPassword("password123");
		MemberVO loginResult = memberService.login(loginParam);
		assertNotNull(loginResult);
		assertNull(loginResult.getPassword());

		//3. 틀린 비밀번호 → null
		log.debug("3. 틀린 비밀번호 → null");
		MemberVO wrongParam = new MemberVO();
		wrongParam.setEmail(TEST_EMAIL);
		wrongParam.setPassword("wrongpassword");
		assertNull(memberService.login(wrongParam));
	}

	//@Disabled
	@Test
	void existsEmailAndNickname() {
		log.debug("---------------------------");
		log.debug("*existsEmailAndNickname()*");
		log.debug("---------------------------");
		//1. 초기화 후 가입
		log.debug("1. 초기화 후 가입");
		cleanupTestMember();
		assertEquals(1, memberService.doSave(member));

		//2. 이메일 중복 존재 → true
		log.debug("2. 이메일 중복 존재 → true");
		MemberVO emailParam = new MemberVO();
		emailParam.setEmail(TEST_EMAIL);
		assertTrue(memberService.existsEmail(emailParam));

		//3. 닉네임 중복 존재 → true
		log.debug("3. 닉네임 중복 존재 → true");
		MemberVO nickParam = new MemberVO();
		nickParam.setNickname(TEST_NICK);
		assertTrue(memberService.existsNickname(nickParam));
	}

	//@Disabled
	@Test
	void getMyPage() {
		log.debug("---------------------------");
		log.debug("*getMyPage()*");
		log.debug("---------------------------");
		//1. 초기화 후 가입
		log.debug("1. 초기화 후 가입");
		cleanupTestMember();
		assertEquals(1, memberService.doSave(member));

		//2. 가입한 회원의 memberId 확보
		log.debug("2. 가입한 회원의 memberId 확보");
		MemberVO emailParam = new MemberVO();
		emailParam.setEmail(TEST_EMAIL);
		MemberVO saved = memberMapper.selectByEmail(emailParam);
		assertNotNull(saved);

		//3. memberId 로 마이페이지 조회 → not null
		log.debug("3. memberId 로 마이페이지 조회 → not null");
		MemberVO myPage = memberService.getMyPage(saved.getMemberId());
		assertNotNull(myPage);
	}

	/**
	 * TEST_EMAIL 기준 격리 초기화(매퍼 활용).
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
