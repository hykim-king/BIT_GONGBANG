package com.pcwk.ehr.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.pcwk.ehr.category.domain.CategoryVO;
import com.pcwk.ehr.mapper.CategoryMapper;

/**
 * CategoryMapper 단위 테스트.
 * root-context.xml 만 로드(dataSource + MapperScanner). 매 케이스 격리(deleteAll→totalCnt()==0).
 * ※ category 는 artwork(FK)의 부모. 참조하는 artwork 가 남아 있으면 deleteAll 이 FK 로 실패할 수 있으므로
 *    격리된 테스트 데이터(참조 artwork 없음)를 전제로 한다. ※ Oracle 기동 필요.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/root-context.xml" })
class CategoryMapperTest {

	private static final Logger log = LogManager.getLogger(CategoryMapperTest.class);

	@Autowired
	private CategoryMapper categoryMapper;

	private CategoryVO makeCategory(String nm) {
		CategoryVO c = new CategoryVO();
		c.setCategoryNm(nm);
		return c;
	}

	@BeforeEach
	void setUp() {
		categoryMapper.deleteAll();
		assertEquals(0, categoryMapper.totalCnt());
	}

	@Test
	void doSave_doSelectOne() {
		CategoryVO c = makeCategory("도자기");
		assertEquals(1, categoryMapper.doSave(c));
		assertTrue(c.getCategoryId() > 0); // selectKey 채번 확인
		assertEquals(1, categoryMapper.totalCnt());

		CategoryVO found = categoryMapper.doSelectOne(c);
		assertNotNull(found);
		assertEquals("도자기", found.getCategoryNm());
		assertNotNull(found.getRegDt()); // TO_CHAR → String
	}

	@Test
	void doUpdate() {
		CategoryVO c = makeCategory("목공");
		categoryMapper.doSave(c);

		c.setCategoryNm("목공예");
		assertEquals(1, categoryMapper.doUpdate(c));

		CategoryVO updated = categoryMapper.doSelectOne(c);
		assertEquals("목공예", updated.getCategoryNm());
		assertNotNull(updated.getModDt());
	}

	@Test
	void doRetrieve_orderByCategoryId() {
		categoryMapper.doSave(makeCategory("도자기"));
		categoryMapper.doSave(makeCategory("목공"));
		categoryMapper.doSave(makeCategory("가죽"));

		CategoryVO param = new CategoryVO();
		List<CategoryVO> list = categoryMapper.doRetrieve(param);
		assertEquals(3, list.size());
		// ORDER BY category_id ASC → 먼저 insert 된 것이 앞
		assertTrue(list.get(0).getCategoryId() < list.get(1).getCategoryId());
		assertEquals("도자기", list.get(0).getCategoryNm());
	}

	@Test
	void doDelete() {
		CategoryVO c = makeCategory("삭제될항목");
		categoryMapper.doSave(c);
		assertEquals(1, categoryMapper.doDelete(c));
		assertEquals(0, categoryMapper.totalCnt());
	}
}
