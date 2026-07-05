package com.pcwk.ehr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * 관리자 대시보드·관리용 Mapper.
 * 전용 테이블 없이 여러 테이블을 집계/조회한다. namespace = FQN(adminMapper.xml). flat 패키지 com.pcwk.ehr.mapper.
 * ※ 다른 담당자의 매퍼(Member/Artwork/Comment/Like)를 건드리지 않도록 admin 전용 쿼리를 여기 둔다.
 */
@Mapper
public interface AdminMapper {

	// ===== 대시보드 통계(각 테이블 총건수) =====
	int memberTotalCnt();

	int artworkTotalCnt();

	int commentTotalCnt();

	int likeTotalCnt();

	int categoryTotalCnt();

	// ===== 회원 관리(페이징·검색) =====
	List<MemberVO> memberList(MemberVO param);

	int memberCnt(MemberVO param);

	// ===== 게시물(작품) 관리(페이징·검색·상태필터) =====
	List<ArtworkVO> artworkList(ArtworkVO param);

	int artworkCnt(ArtworkVO param);
}
