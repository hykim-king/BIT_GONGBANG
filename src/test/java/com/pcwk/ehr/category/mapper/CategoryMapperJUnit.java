package com.pcwk.ehr.category.mapper;

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
 * category 테이블 CRUD 매퍼 테스트.
 * 테스트가 매번 같은 결과가 나오도록, 시작할 때 deleteAll 로 싹 비우고(0건 확인) → 등록 → 검증 순으로 짠다.
 * 실행하려면 Oracle 을 켜고 각 테스트의 @Disabled 를 떼면 된다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {
		"file:src/main/webapp/WEB-INF/spring/root-context.xml",
		"file:src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml" })
class CategoryMapperJUnit {

	private static final Logger log = LogManager.getLogger(CategoryMapperJUnit.class);

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
		assertNotNull(categoryMapper);
		// Test Isolation: 전체 삭제 후 0건 확인
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
		assertNotNull(categoryMapper);
	}

	//@Disabled
	@Test
	void doSave() {
		log.debug("---------------------------");
		log.debug("*doSave()*");
		log.debug("---------------------------");
		//1. 초기화(deleteAll)는 BeforeEach 에서 완료 -> totalCnt==0
		//2. 단건 등록(doSave) + selectKey 채번 확인
		//3. 조회(doSelectOne) 및 필드 비교

		//1
		log.debug("1. 초기화 확인");
		assertEquals(0, categoryMapper.totalCnt());

		//2
		log.debug("2. 단건 등록");
		CategoryVO c = makeCategory("도자기");
		int flag = categoryMapper.doSave(c);
		assertEquals(1, flag);
		assertTrue(c.getCategoryId() > 0); // selectKey 로 categoryId 채번
		assertEquals(1, categoryMapper.totalCnt());

		//3
		log.debug("3. 조회 및 비교");
		CategoryVO found = categoryMapper.doSelectOne(c);
		assertNotNull(found);
		assertEquals(c.getCategoryId(), found.getCategoryId());
		assertEquals("도자기", found.getCategoryNm());
		assertNotNull(found.getRegDt()); // DEFAULT SYSDATE, 조회 시 TO_CHAR -> String
	}

	//@Disabled
	@Test
	void doUpdate() {
		log.debug("---------------------------");
		log.debug("*doUpdate()*");
		log.debug("---------------------------");
		//1. 초기화 확인
		//2. 단건 등록
		//3. 카테고리명 수정(doUpdate) -> 조회하여 변경/modDt 확인

		//1
		log.debug("1. 초기화 확인");
		assertEquals(0, categoryMapper.totalCnt());

		//2
		log.debug("2. 단건 등록");
		CategoryVO c = makeCategory("목공");
		assertEquals(1, categoryMapper.doSave(c));
		assertTrue(c.getCategoryId() > 0);
		assertEquals(1, categoryMapper.totalCnt());

		//3
		log.debug("3. 카테고리명 수정");
		c.setCategoryNm("목공예");
		assertEquals(1, categoryMapper.doUpdate(c));

		CategoryVO updated = categoryMapper.doSelectOne(c);
		assertNotNull(updated);
		assertEquals("목공예", updated.getCategoryNm());
		assertNotNull(updated.getModDt());
	}

	//@Disabled
	@Test
	void doRetrieve() {
		log.debug("---------------------------");
		log.debug("*doRetrieve()*");
		log.debug("---------------------------");
		//1. 초기화 확인
		//2. 3건 등록
		//3. 전체 조회 -> size 3, category_id ASC(먼저 insert 된 것이 앞)

		//1
		log.debug("1. 초기화 확인");
		assertEquals(0, categoryMapper.totalCnt());

		//2
		log.debug("2. 3건 등록");
		categoryMapper.doSave(makeCategory("도자기"));
		categoryMapper.doSave(makeCategory("목공"));
		categoryMapper.doSave(makeCategory("가죽"));
		assertEquals(3, categoryMapper.totalCnt());

		//3
		log.debug("3. 전체 조회");
		CategoryVO param = new CategoryVO();
		List<CategoryVO> list = categoryMapper.doRetrieve(param);
		assertNotNull(list);
		assertEquals(3, list.size());
		// ORDER BY category_id ASC -> get(0) < get(1)
		assertTrue(list.get(0).getCategoryId() < list.get(1).getCategoryId());
		assertEquals("도자기", list.get(0).getCategoryNm());
	}

	//@Disabled
	@Test
	void doDelete() {
		log.debug("---------------------------");
		log.debug("*doDelete()*");
		log.debug("---------------------------");
		//1. 초기화 확인
		//2. 단건 등록 -> totalCnt==1
		//3. 삭제(doDelete) -> totalCnt==0

		//1
		log.debug("1. 초기화 확인");
		assertEquals(0, categoryMapper.totalCnt());

		//2
		log.debug("2. 단건 등록");
		CategoryVO c = makeCategory("삭제될항목");
		assertEquals(1, categoryMapper.doSave(c));
		assertTrue(c.getCategoryId() > 0);
		assertEquals(1, categoryMapper.totalCnt());

		//3
		log.debug("3. 삭제");
		assertEquals(1, categoryMapper.doDelete(c));
		assertEquals(0, categoryMapper.totalCnt());
	}
}
