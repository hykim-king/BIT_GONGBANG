package com.pcwk.ehr.admin.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.artwork.service.ArtworkService;
import com.pcwk.ehr.mapper.AdminMapper;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * 관리자 서비스. 전용 테이블 없이 AdminMapper(여러 테이블 집계)에 위임한다.
 * 팀 컨벤션: 단일 @Service(인터페이스/Impl 분리 없음, 팀 CategoryService·CommentService 동형).
 * 조회는 @Transactional 불필요, 회원 수정/삭제(쓰기)만 트랜잭션 경계 적용.
 */
@Service
public class AdminService {

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private AdminMapper adminMapper;

	@Autowired
	private ArtworkService artworkService; // 회원 삭제 시 소유 작품 연쇄 정리(첨부 물리파일 포함) 재사용

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

	// ===== 대시보드 신규 통계 (CC-ADM-03) =====

	/** 완성:공개 구성비 — [{isStatus, cnt}] */
	public List<Map<String, Object>> statusRatio() {
		return adminMapper.statusRatio();
	}

	/** 카테고리별 게시글 수 — [{categoryNm, cnt}] */
	public List<Map<String, Object>> categoryArtworkStats() {
		return adminMapper.categoryArtworkStats();
	}

	/** 최근 가입 회원 상위 N건 */
	public List<MemberVO> recentMembers(int topN) {
		return adminMapper.recentMembers(topN);
	}

	// ===== 회원 관리 신설 (CC-ADM-01) =====

	/** 관리자 회원 수정(닉네임/관리자여부). 닉네임 1~10자 검증 */
	@Transactional
	public int updateMember(MemberVO param) {
		if (param.getMemberId() == 0) {
			return 0;
		}
		if (param.getNickname() != null && !param.getNickname().trim().isEmpty()) {
			String nickname = param.getNickname().trim();
			if (nickname.length() > 10) {
				return 0;
			}
			param.setNickname(nickname);
		}
		return adminMapper.updateMember(param);
	}

	/**
	 * 관리자 회원 삭제 — 연쇄 삭제(cascade, 팀 확정).
	 * ① 회원 소유 작품을 ArtworkService.doDelete 오케스트레이션으로 순차 정리
	 *    (작품·작업일지의 첨부 DB+물리파일, 타인이 남긴 댓글·좋아요까지 정리됨)
	 * ② member 행 삭제 — 이 회원이 타인 글에 남긴 댓글/좋아요/첨부(DB)는
	 *    member FK ON DELETE CASCADE 로 함께 삭제된다.
	 * 첨부 물리파일 삭제 실패(IOException) 시 전체 롤백.
	 */
	@Transactional(rollbackFor = Exception.class)
	public int deleteMember(MemberVO param) throws IOException {
		if (param.getMemberId() == 0) {
			return 0;
		}
		ArtworkVO cond = new ArtworkVO();
		cond.setMemberId(param.getMemberId());          // isStatus 미지정 → 전체(완성+공개)
		List<ArtworkVO> artworks = artworkService.selectByMember(cond);
		for (ArtworkVO artwork : artworks) {
			artworkService.doDelete(artwork);           // 첨부(DB+디스크)·댓글·좋아요·작업일지 연쇄 정리
		}
		return adminMapper.deleteMember(param);
	}
}
