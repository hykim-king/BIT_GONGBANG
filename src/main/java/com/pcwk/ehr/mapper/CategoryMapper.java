package com.pcwk.ehr.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.pcwk.ehr.category.domain.CategoryVO;
import com.pcwk.ehr.cmn.WorkDiv;

/**
 * 카테고리 Mapper. WorkDiv 5종(doRetrieve/doSelectOne/doSave/doUpdate/doDelete) + 테스트 보조.
 * namespace = FQN(categoryMapper.xml). No Impl(MyBatis 프록시). flat 패키지 com.pcwk.ehr.mapper.
 */
@Mapper
public interface CategoryMapper extends WorkDiv<CategoryVO> {

	// 테스트 끝나면 삭제 ㄱㄱ
	int deleteAll();

	// 테스트/전체 카운트
	int totalCnt();
}
