package com.pcwk.ehr.comment.service;


import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcwk.ehr.cmn.DTO;
import com.pcwk.ehr.cmn.WorkDiv;
import com.pcwk.ehr.comment.domain.CommentVO;
import com.pcwk.ehr.mapper.CommentMapper;

@Service
public class CommentService implements WorkDiv<CommentVO> {

	Logger log = LogManager.getLogger(getClass());
	private static final int MAX_CONTENT_LENGTH = 1000;
	
	@Autowired
	private CommentMapper commentMapper; 
	
	public CommentService() {
		log.debug("CommentService");
	}

	@Override
	public List<CommentVO> doRetrieve(DTO param) {
		return commentMapper.doRetrieve(param);
	}

	@Override
	public CommentVO doSelectOne(CommentVO param) throws EmptyResultDataAccessException {
		return commentMapper.doSelectOne(param);
	}

	@Override
	@Transactional
	public int doSave(CommentVO param) {
		if (!isValidContent(param)) {
			return 0;
		}
		return commentMapper.doSave(param);
	}

	@Override
	@Transactional
	public int doUpdate(CommentVO param) {
		if (!isValidContent(param)) {
			return 0;
		}
		return commentMapper.doUpdate(param);
	}

	@Override
	@Transactional
	public int doDelete(CommentVO param) {
		return commentMapper.doDelete(param);
	}
	
	
	//게시글 삭제 연동해야함. 타입명과 아이디 기준으로 댓글 일괄 삭제
	@Transactional
	public int deleteByTarget(CommentVO param) {
		return commentMapper.deleteByTarget(param);
	}

	public int countByTarget(CommentVO param) {
		return commentMapper.countByTarget(param);
	}

	/** 테스트용 배포전에 삭제 ㄱㄱ */
	@Transactional
	public int deleteAll() {
		return commentMapper.deleteAll();
	}

	private boolean isValidContent(CommentVO param) {
		if (param == null || param.getContent() == null) {
			return false;
		}
		String content = param.getContent().trim();
		if (content.isEmpty()) {
			return false;
		}
		if (content.length() > MAX_CONTENT_LENGTH) {
			return false;
		}
		param.setContent(content);
		return true;
	}
	
	
}
