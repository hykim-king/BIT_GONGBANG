package com.pcwk.ehr.member.domain;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.pcwk.ehr.mapper.MemberMapper;

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

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	private MemberVO template;

	@BeforeEach
	void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		template = new MemberVO();
		template.setEmail(TEST_EMAIL);
		template.setNickname(TEST_NICK);
		template.setPassword(encoder.encode("password123"));
		template.setIsAdmin("N");
		template.setUserIntro("JUnit 테스트 회원");
		assertNotNull(memberMapper);
	}

	@AfterEach
	void tearDown() {
		log.debug("*AfterEach()*");
	}

	@Disabled
	@Test
	void doSave() {
		cleanupTestMember();

		int flag = memberMapper.doSave(template);
		log.debug("doSave flag={}", flag);
		assertEquals(1, flag);
		assertTrue(template.getMemberId() > 0);
	}

	@Disabled
	@Test
	void selectByEmailAndLoginFlow() {
		cleanupTestMember();
		memberMapper.doSave(template);

		MemberVO param = new MemberVO();
		param.setEmail(TEST_EMAIL);
		MemberVO found = memberMapper.selectByEmail(param);

		assertNotNull(found);
		assertEquals(TEST_EMAIL, found.getEmail());
		assertTrue(encoder.matches("password123", found.getPassword()));
	}

	@Disabled
	@Test
	void countByEmailAndNickname() {
		cleanupTestMember();
		memberMapper.doSave(template);

		MemberVO emailParam = new MemberVO();
		emailParam.setEmail(TEST_EMAIL);
		assertEquals(1, memberMapper.countByEmail(emailParam));

		MemberVO nickParam = new MemberVO();
		nickParam.setNickname(TEST_NICK);
		assertEquals(1, memberMapper.countByNickname(nickParam));
	}

	@Disabled
	@Test
	void selectMyPage() {
		cleanupTestMember();
		memberMapper.doSave(template);

		MemberVO param = new MemberVO();
		param.setMemberId(template.getMemberId());
		MemberVO myPage = memberMapper.selectMyPage(param);

		assertNotNull(myPage);
		assertEquals(TEST_NICK, myPage.getNickname());
		assertTrue(myPage.getArtworkCnt() >= 0);
		assertTrue(myPage.getLikeCnt() >= 0);
	}

	@Disabled
	@Test
	void doUpdateAndDelete() {
		cleanupTestMember();
		memberMapper.doSave(template);
		int id = template.getMemberId();

		MemberVO updateParam = new MemberVO();
		updateParam.setMemberId(id);
		updateParam.setNickname("JUnitMod");
		updateParam.setUserIntro("수정됨");
		assertEquals(1, memberMapper.doUpdate(updateParam));

		MemberVO one = new MemberVO();
		one.setMemberId(id);
		MemberVO updated = memberMapper.doSelectOne(one);
		assertEquals("JUnitMod", updated.getNickname());

		MemberVO delParam = new MemberVO();
		delParam.setMemberId(id);
		assertEquals(1, memberMapper.doDelete(delParam));
		assertNull(memberMapper.doSelectOne(delParam));
	}

	@Disabled
	@Test
	void doRetrieve() {
		List<com.pcwk.ehr.member.domain.MemberVO> list = memberMapper.doRetrieve(new MemberVO());
		assertNotNull(list);
	}

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
