package com.pcwk.ehr.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.pcwk.ehr.cmn.WorkDiv;
import com.pcwk.ehr.comment.domain.CommentVO;

@Mapper
public interface CommentMapper extends WorkDiv<CommentVO> {

	// 테스트 끝나면 삭제 ㄱㄱ
	int deleteAll();

	//게시글 삭제 시, 대상 댓글 일괄 삭제. 게시글의 타입이 들어가야함
	int deleteByTarget(CommentVO param);
	
 	//게시글별 댓글 수
	int countByTarget(CommentVO param);
	
}
