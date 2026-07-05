package com.pcwk.ehr.category.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.pcwk.ehr.category.domain.CategoryVO;
import com.pcwk.ehr.mapper.CategoryMapper;

/**
 * category 서비스(CategoryService) CRUD 테스트.
 * 실제 동작(등록/수정/삭제/조회)은 서비스로 호출하고, 데이터 초기화·건수 확인은 매퍼(deleteAll/totalCnt)로 한다.
 * 실행하려면 Oracle 을 켜고 각 테스트의 @Disabled 를 떼면 된다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class CategoryServiceJUnit {

	private static final Logger log = LogManager.getLogger(CategoryServiceJUnit.class);

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CategoryMapper categoryMapper;

	/** categoryNm 만 세팅한 신규 CategoryVO 생성(등록용 헬퍼). */
	private CategoryVO makeCategory(String nm) {
		CategoryVO c = new CategoryVO();
		c.setCategoryNm(nm);
		return c;
	}

	@BeforeEach
	void setUp() {
		log.debug("---------------------------");
		log.debug("*BeforeEach()*");
		log.debug("---------------------------");
		assertNotNull(categoryService);
		assertNotNull(categoryMapper);
		// 시작할 때 매퍼로 전체 삭제 후 0건 확인(초기화)
		categoryMapper.deleteAll();
		assertEquals(0, categoryMapper.totalCnt());
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
		assertNotNull(categoryService);
		assertNotNull(categoryMapper);
	}

	//@Disabled
	@Test
	void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");
		//1. 초기화 확인(매퍼)
		//2. 서비스로 단건 등록(categoryService.doSave) -> 매퍼 totalCnt==1
		//3. 매퍼 doSelectOne 으로 조회/비교

		//1
		log.debug("1. 초기화 확인");
		assertEquals(0, categoryMapper.totalCnt());

		//2
		log.debug("2. 서비스로 단건 등록");
		CategoryVO c = makeCategory("도자기");
		int flag = categoryService.doSave(c);
		assertEquals(1, flag);
		assertTrue(c.getCategoryId() > 0); // selectKey 채번
		assertEquals(1, categoryMapper.totalCnt());

		//3
		log.debug("3. 조회 및 비교");
		CategoryVO found = categoryMapper.doSelectOne(c);
		assertNotNull(found);
		assertEquals(c.getCategoryId(), found.getCategoryId());
		assertEquals("도자기", found.getCategoryNm());
		assertNotNull(found.getRegDt());
	}

	//@Disabled
	@Test
	void doUpdate() {
		log.debug("---------------------------");
		log.debug("*doUpdate()*");
		log.debug("---------------------------");
		//1. 초기화 확인
		//2. 서비스로 등록
		//3. 서비스로 수정(doUpdate) -> 매퍼 조회하여 변경/modDt 확인

		//1
		log.debug("1. 초기화 확인");
		assertEquals(0, categoryMapper.totalCnt());

		//2
		log.debug("2. 서비스로 등록");
		CategoryVO c = makeCategory("목공");
		assertEquals(1, categoryService.doSave(c));
		assertTrue(c.getCategoryId() > 0);
		assertEquals(1, categoryMapper.totalCnt());

		//3
		log.debug("3. 서비스로 수정");
		c.setCategoryNm("목공예");
		assertEquals(1, categoryService.doUpdate(c));

		CategoryVO updated = categoryMapper.doSelectOne(c);
		assertNotNull(updated);
		assertEquals("목공예", updated.getCategoryNm());
		assertNotNull(updated.getModDt());
	}

	//@Disabled
	@Test
	void doDelete() {
		log.debug("---------------------------");
		log.debug("*doDelete()*");
		log.debug("---------------------------");
		//1. 초기화 확인
		//2. 서비스로 등록 -> 매퍼 totalCnt==1
		//3. 서비스로 삭제(doDelete) -> 매퍼 totalCnt==0

		//1
		log.debug("1. 초기화 확인");
		assertEquals(0, categoryMapper.totalCnt());

		//2
		log.debug("2. 서비스로 등록");
		CategoryVO c = makeCategory("삭제될항목");
		assertEquals(1, categoryService.doSave(c));
		assertTrue(c.getCategoryId() > 0);
		assertEquals(1, categoryMapper.totalCnt());

		//3
		log.debug("3. 서비스로 삭제");
		assertEquals(1, categoryService.doDelete(c));
		assertEquals(0, categoryMapper.totalCnt());
	}

	//@Disabled
	@Test
	void doRetrieve() {
		log.debug("---------------------------");
		log.debug("*doRetrieve()*");
		log.debug("---------------------------");
		//1. 초기화 확인
		//2. 서비스로 3건 등록
		//3. 서비스로 전체 조회(doRetrieve) -> size 3, category_id ASC

		//1
		log.debug("1. 초기화 확인");
		assertEquals(0, categoryMapper.totalCnt());

		//2
		log.debug("2. 서비스로 3건 등록");
		categoryService.doSave(makeCategory("도자기"));
		categoryService.doSave(makeCategory("목공"));
		categoryService.doSave(makeCategory("가죽"));
		assertEquals(3, categoryMapper.totalCnt());

		//3
		log.debug("3. 서비스로 전체 조회");
		CategoryVO param = new CategoryVO();
		List<CategoryVO> list = categoryService.doRetrieve(param);
		assertNotNull(list);
		assertEquals(3, list.size());
		// ORDER BY category_id ASC -> get(0) < get(1)
		assertTrue(list.get(0).getCategoryId() < list.get(1).getCategoryId());
		assertEquals("도자기", list.get(0).getCategoryNm());
	}
}
