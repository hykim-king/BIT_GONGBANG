package com.pcwk.ehr.like.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcwk.ehr.like.domain.LikeVO;
import com.pcwk.ehr.mapper.LikeMapper;

@Service
public class LikeService {

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private LikeMapper likeMapper;

	public LikeService() {
		log.debug("LikeService");
	}

	/**
	 * 좋아요 토글.
	 *
	 * @return data: {liked:boolean, count:int}
	 */
	@Transactional
	public Map<String, Object> toggle(LikeVO param) {
		LikeVO exist = likeMapper.doSelectOne(param);
		if (exist != null) {
			likeMapper.doDelete(param);
			return buildToggleResult(false, likeMapper.countByTarget(param));
		}

		try {
			likeMapper.doSave(param);
		} catch (DuplicateKeyException e) {
			// 동시 클릭 UNIQUE 위반이면 좋아요 상태로 간주
			log.debug("toggle duplicate key param: " + param);
		}

		return buildToggleResult(true, likeMapper.countByTarget(param));
	}

	public int countByTarget(LikeVO param) {
		return likeMapper.countByTarget(param);
	}

	public List<LikeVO> selectByMember(LikeVO param) {
		return likeMapper.selectByMember(param);
	}

	@Transactional
	public int deleteByTarget(LikeVO param) {
		return likeMapper.deleteByTarget(param);
	}

	private Map<String, Object> buildToggleResult(boolean liked, int count) {
		Map<String, Object> out = new HashMap<>();
		out.put("liked", liked);
		out.put("count", count);
		return out;
	}
}

