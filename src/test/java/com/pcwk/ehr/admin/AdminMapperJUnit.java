package com.pcwk.ehr.admin;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.mapper.AdminMapper;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * AdminMapper 스모크 테스트(읽기 전용, 데이터 미변경).
 * root-context.xml 로드(dataSource + MapperScanner). ※ Oracle 기동 + 시드 데이터 전제.
 * 집계/페이징 쿼리가 오류 없이 실행되고 사이즈 계약(페이지 크기 이내)을 지키는지 검증한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/root-context.xml" })
class AdminMapperJUnit {

	private static final Logger log = LogManager.getLogger(AdminMapperJUnit.class);

	@Autowired
	private AdminMapper adminMapper;

	@Test
	void totalCounts_notNegative() {
		assertTrue(adminMapper.memberTotalCnt() >= 0);
		assertTrue(adminMapper.artworkTotalCnt() >= 0);
		assertTrue(adminMapper.commentTotalCnt() >= 0);
		assertTrue(adminMapper.likeTotalCnt() >= 0);
		assertTrue(adminMapper.categoryTotalCnt() >= 0);
	}

	@Test
	void memberList_paging_runs() {
		MemberVO cond = new MemberVO();
		cond.setPageNo(1);
		cond.setPageSize(10);

		List<MemberVO> list = adminMapper.memberList(cond);
		assertNotNull(list);
		assertTrue(list.size() <= 10); // 페이지 크기 계약
		assertTrue(adminMapper.memberCnt(cond) >= 0);
		log.debug("memberList size={}", list.size());
	}

	@Test
	void artworkList_paging_runs() {
		ArtworkVO cond = new ArtworkVO();
		cond.setPageNo(1);
		cond.setPageSize(10);

		List<ArtworkVO> list = adminMapper.artworkList(cond);
		assertNotNull(list);
		assertTrue(list.size() <= 10); // 페이지 크기 계약
		assertTrue(adminMapper.artworkCnt(cond) >= 0);
		log.debug("artworkList size={}", list.size());
	}
}
