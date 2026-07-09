package com.pcwk.ehr.admin.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.mapper.AdminMapper;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * 관리자 서비스. 전용 테이블 없이 AdminMapper(여러 테이블 집계)에 위임한다.
 * 팀 컨벤션: 단일 @Service(인터페이스/Impl 분리 없음, 팀 CategoryService·CommentService 동형).
 * 전부 조회(read)라 @Transactional 불필요.
 */
@Service
public class AdminService {

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private AdminMapper adminMapper;

	public AdminService() {
		log.debug("AdminService");
	}

	/** 대시보드 통계(회원/작품/댓글/좋아요/카테고리 수). LinkedHashMap 으로 표시 순서 유지 */
	public Map<String, Integer> stats() {
		Map<String, Integer> m = new LinkedHashMap<>();
		m.put("member", adminMapper.memberTotalCnt());
		m.put("artwork", adminMapper.artworkTotalCnt());
		m.put("comment", adminMapper.commentTotalCnt());
		m.put("like", adminMapper.likeTotalCnt());
		m.put("category", adminMapper.categoryTotalCnt());
		return m;
	}

	/** 회원 목록(페이징·검색) */
	public List<MemberVO> listMember(MemberVO cond) {
		return adminMapper.memberList(cond);
	}

	/** 회원 총건수(검색조건 반영, 페이징용) */
	public int memberCnt(MemberVO cond) {
		return adminMapper.memberCnt(cond);
	}

	/** 게시물(작품) 목록(페이징·검색·상태) */
	public List<ArtworkVO> listArtwork(ArtworkVO cond) {
		return adminMapper.artworkList(cond);
	}

	/** 게시물 총건수(검색조건 반영, 페이징용) */
	public int artworkCnt(ArtworkVO cond) {
		return adminMapper.artworkCnt(cond);
	}
}
