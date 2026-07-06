package com.pcwk.ehr.admin.service;

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

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * admin 서비스(AdminService) 테스트 — 대시보드 통계 + 회원·작품 목록.
 * 읽기 전용이라 등록/삭제 없이, "오류 없이 실행되고, 값이 음수가 아니고, 목록이 페이지 크기 이내인지"만 확인한다(스모크 테스트).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class AdminServiceJUnit {

	private static final Logger log = LogManager.getLogger(AdminServiceJUnit.class);

	@Autowired
	private AdminService adminService;

	@BeforeEach
	void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		assertNotNull(adminService);
		// 읽기전용 집계 서비스 → 초기화(삭제/등록) 없음. 데이터 미변경.
	}

	@AfterEach
	void tearDown() {
		log.debug("---------------------------");
		log.debug("*AfterEach()*");
		log.debug("---------------------------");
		// 정리 대상 없음(데이터 미변경).
	}

	//@Disabled
	@Test
	void beans() {
		log.debug("---------------------------");
		log.debug("*beans()*");
		log.debug("---------------------------");
		assertNotNull(adminService);
	}

	//@Disabled
	@Test
	void stats() {
		log.debug("---------------------------");
		log.debug("*stats()*");
		log.debug("---------------------------");
		//1. 대시보드 통계 조회
		//2. not null & 필수 키(member/artwork/comment/like/category) 포함 계약
		//3. 각 집계값 >= 0 계약
		log.debug("1. 대시보드 통계 조회");
		Map<String, Integer> stats = adminService.stats();
		log.debug("2. not null & 필수 키(member/artwork/comment/like/category) 포함 계약");
		assertNotNull(stats);

		assertTrue(stats.keySet().contains("member"));
		assertTrue(stats.keySet().contains("artwork"));
		assertTrue(stats.keySet().contains("comment"));
		assertTrue(stats.keySet().contains("like"));
		assertTrue(stats.keySet().contains("category"));

		log.debug("3. 각 집계값 >= 0 계약");
		assertTrue(stats.get("member") >= 0);
		assertTrue(stats.get("artwork") >= 0);
		assertTrue(stats.get("comment") >= 0);
		assertTrue(stats.get("like") >= 0);
		assertTrue(stats.get("category") >= 0);

		log.debug("stats={}", stats);
	}

	//@Disabled
	@Test
	void listMember() {
		log.debug("---------------------------");
		log.debug("*listMember()*");
		log.debug("---------------------------");
		//1. 페이징 검색조건 구성(pageNo=1, pageSize=10)
		//2. 회원 목록 조회 → not null & size <= pageSize 계약
		log.debug("1. 페이징 검색조건 구성(pageNo=1, pageSize=10)");
		MemberVO cond = new MemberVO();
		cond.setPageNo(1);
		cond.setPageSize(10);

		log.debug("2. 회원 목록 조회 → not null & size <= pageSize 계약");
		List<MemberVO> list = adminService.listMember(cond);
		assertNotNull(list);
		assertTrue(list.size() <= 10); // 페이지 크기 계약

		log.debug("listMember size={}", list.size());
	}

	//@Disabled
	@Test
	void memberCnt() {
		log.debug("---------------------------");
		log.debug("*memberCnt()*");
		log.debug("---------------------------");
		//1. 검색조건 구성
		//2. 회원 총건수 조회 → >= 0 계약
		log.debug("1. 검색조건 구성");
		MemberVO cond = new MemberVO();
		cond.setPageNo(1);
		cond.setPageSize(10);

		log.debug("2. 회원 총건수 조회 → >= 0 계약");
		int cnt = adminService.memberCnt(cond);
		assertTrue(cnt >= 0);

		log.debug("memberCnt={}", cnt);
	}

	//@Disabled
	@Test
	void listArtwork() {
		log.debug("---------------------------");
		log.debug("*listArtwork()*");
		log.debug("---------------------------");
		//1. 페이징 검색조건 구성(pageNo=1, pageSize=10)
		//2. 작품 목록 조회 → not null & size <= pageSize 계약
		log.debug("1. 페이징 검색조건 구성(pageNo=1, pageSize=10)");
		ArtworkVO cond = new ArtworkVO();
		cond.setPageNo(1);
		cond.setPageSize(10);

		log.debug("2. 작품 목록 조회 → not null & size <= pageSize 계약");
		List<ArtworkVO> list = adminService.listArtwork(cond);
		assertNotNull(list);
		assertTrue(list.size() <= 10); // 페이지 크기 계약

		log.debug("listArtwork size={}", list.size());
	}

	//@Disabled
	@Test
	void artworkCnt() {
		log.debug("---------------------------");
		log.debug("*artworkCnt()*");
		log.debug("---------------------------");
		//1. 검색조건 구성
		//2. 작품 총건수 조회 → >= 0 계약
		log.debug("1. 검색조건 구성");
		ArtworkVO cond = new ArtworkVO();
		cond.setPageNo(1);
		cond.setPageSize(10);

		log.debug("2. 작품 총건수 조회 → >= 0 계약");
		int cnt = adminService.artworkCnt(cond);
		assertTrue(cnt >= 0);

		log.debug("artworkCnt={}", cnt);
	}
}
