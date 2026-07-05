package com.pcwk.ehr.category.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcwk.ehr.category.domain.CategoryVO;
import com.pcwk.ehr.cmn.DTO;
import com.pcwk.ehr.cmn.WorkDiv;
import com.pcwk.ehr.mapper.CategoryMapper;

/**
 * 카테고리 서비스. 단일 @Service 로 WorkDiv&lt;CategoryVO&gt; 직접 구현(팀 CommentService 동형, 인터페이스/Impl 분리 없음).
 * CRUD 는 CategoryMapper 에 위임, 쓰기(doSave/doUpdate/doDelete)는 @Transactional.
 */
@Service
public class CategoryService implements WorkDiv<CategoryVO> {

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private CategoryMapper categoryMapper;

	public CategoryService() {
		log.debug("CategoryService");
	}

	@Override
	public List<CategoryVO> doRetrieve(DTO param) {
		return categoryMapper.doRetrieve(param);
	}

	@Override
	public CategoryVO doSelectOne(CategoryVO param) throws EmptyResultDataAccessException {
		return categoryMapper.doSelectOne(param);
	}

	@Override
	@Transactional
	public int doSave(CategoryVO param) {
		int flag = categoryMapper.doSave(param); // selectKey 로 categoryId 채번
		log.debug("doSave flag={}, categoryId={}", flag, param.getCategoryId());
		return flag;
	}

	@Override
	@Transactional
	public int doUpdate(CategoryVO param) {
		return categoryMapper.doUpdate(param);
	}

	@Override
	@Transactional
	public int doDelete(CategoryVO param) {
		return categoryMapper.doDelete(param);
	}
}
