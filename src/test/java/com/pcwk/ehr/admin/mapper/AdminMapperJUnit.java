package com.pcwk.ehr.admin.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.mapper.AdminMapper;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * admin 매퍼 테스트 — 관리자 대시보드용 집계/목록.
 * admin 은 전용 테이블 없이 여러 테이블을 "읽기만" 하는 매퍼라, 등록/삭제가 없어 삭제→등록→비교 방식을 못 쓴다.
 * 그래서 "오류 없이 실행되고, 건수가 음수가 아니고, 목록이 페이지 크기 이내인지"만 확인한다(스모크 테스트).
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class AdminMapperJUnit {

	private static final Logger log = LogManager.getLogger(AdminMapperJUnit.class);

	@Autowired
	private AdminMapper adminMapper;

	@BeforeEach
	void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		assertNotNull(adminMapper);
		// 읽기전용 집계 매퍼 → 초기화(삭제/등록) 없음. 데이터 미변경.
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
		assertNotNull(adminMapper);
	}

	//@Disabled
	@Test
	void totalCounts_notNegative() {
		log.debug("---------------------------");
		log.debug("*totalCounts_notNegative()*");
		log.debug("---------------------------");
		//1. 각 테이블 총건수 집계 호출
		//2. 계약 검증: 모든 집계값 >= 0 (음수 불가)
		log.debug("1. 각 테이블 총건수 집계 호출");
		int memberCnt = adminMapper.memberTotalCnt();
		int artworkCnt = adminMapper.artworkTotalCnt();
		int commentCnt = adminMapper.commentTotalCnt();
		int likeCnt = adminMapper.likeTotalCnt();
		int categoryCnt = adminMapper.categoryTotalCnt();

		log.debug("memberTotalCnt={}", memberCnt);
		log.debug("artworkTotalCnt={}", artworkCnt);
		log.debug("commentTotalCnt={}", commentCnt);
		log.debug("likeTotalCnt={}", likeCnt);
		log.debug("categoryTotalCnt={}", categoryCnt);

		log.debug("2. 계약 검증: 모든 집계값 >= 0 (음수 불가)");
		assertTrue(memberCnt >= 0);
		assertTrue(artworkCnt >= 0);
		assertTrue(commentCnt >= 0);
		assertTrue(likeCnt >= 0);
		assertTrue(categoryCnt >= 0);
	}

	//@Disabled
	@Test
	void memberList_paging_runs() {
		log.debug("---------------------------");
		log.debug("*memberList_paging_runs()*");
		log.debug("---------------------------");
		//1. 페이징 검색조건 구성(pageNo=1, pageSize=10)
		//2. 회원 목록 조회 → not null & size <= pageSize 계약
		//3. 회원 총건수 조회 → >= 0 계약
		log.debug("1. 페이징 검색조건 구성(pageNo=1, pageSize=10)");
		MemberVO cond = new MemberVO();
		cond.setPageNo(1);
		cond.setPageSize(10);

		log.debug("2. 회원 목록 조회 → not null & size <= pageSize 계약");
		List<MemberVO> list = adminMapper.memberList(cond);
		assertNotNull(list);
		assertTrue(list.size() <= 10); // 페이지 크기 계약

		log.debug("3. 회원 총건수 조회 → >= 0 계약");
		int cnt = adminMapper.memberCnt(cond);
		assertTrue(cnt >= 0);

		log.debug("memberList size={}, memberCnt={}", list.size(), cnt);
	}

	//@Disabled
	@Test
	void artworkList_paging_runs() {
		log.debug("---------------------------");
		log.debug("*artworkList_paging_runs()*");
		log.debug("---------------------------");
		//1. 페이징 검색조건 구성(pageNo=1, pageSize=10)
		//2. 작품 목록 조회 → not null & size <= pageSize 계약
		//3. 작품 총건수 조회 → >= 0 계약
		log.debug("1. 페이징 검색조건 구성(pageNo=1, pageSize=10)");
		ArtworkVO cond = new ArtworkVO();
		cond.setPageNo(1);
		cond.setPageSize(10);

		log.debug("2. 작품 목록 조회 → not null & size <= pageSize 계약");
		List<ArtworkVO> list = adminMapper.artworkList(cond);
		assertNotNull(list);
		assertTrue(list.size() <= 10); // 페이지 크기 계약

		log.debug("3. 작품 총건수 조회 → >= 0 계약");
		int cnt = adminMapper.artworkCnt(cond);
		assertTrue(cnt >= 0);

		log.debug("artworkList size={}, artworkCnt={}", list.size(), cnt);
	}
}
