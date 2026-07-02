package com.pcwk.ehr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pcwk.ehr.like.domain.LikeVO;

@Mapper
public interface LikeMapper {



	int doSave(LikeVO param);


	int doDelete(LikeVO param);

	//이미 누른 좋아요 인지 확인하기 
	LikeVO doSelectOne(LikeVO param);


	// 대상 좋아요 수.
	int countByTarget(LikeVO param);


	//내가 좋아요한 목록.
	List<LikeVO> selectByMember(LikeVO param);

	//  삭제 연동
	int deleteByTarget(LikeVO param);

	 // 테스트용 배포전에 삭제 ㄱㄱ
	int deleteAll();
}

