package com.pcwk.ehr.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.pcwk.ehr.cmn.WorkDiv;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * MemberMapper — 회원(member) MyBatis 매퍼.
 * XML: resources/mapper/member/memberMapper.xml
 */
@Mapper
public interface MemberMapper extends WorkDiv<MemberVO> {

	/** 로그인 — email 기준 1건 (password 포함) */
	MemberVO selectByEmail(MemberVO param);

	/** 이메일 중복 건수 */
	int countByEmail(MemberVO param);

	/** 닉네임 중복 건수 (memberId 제외 옵션) */
	int countByNickname(MemberVO param);

	/** 마이페이지 — 프로필 + artwork·like 집계 */
	MemberVO selectMyPage(MemberVO param);

	/** 테스트용 — 배포 전 삭제 */
	int deleteAll();

}
