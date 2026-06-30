package com.pcwk.ehr.cmn;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;

/**
 * 공통 CRUD 계약 (v2). Service·Mapper가 동일 메서드명으로 구현.
 */
public interface WorkDiv<T> {

	List<T> doRetrieve(DTO param);

	T doSelectOne(T param) throws EmptyResultDataAccessException;

	int doSave(T param);

	int doUpdate(T param);

	int doDelete(T param);

}
