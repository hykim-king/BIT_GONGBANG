package com.pcwk.ehr.artworkentry.controller;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.artwork.service.ArtworkService;
import com.pcwk.ehr.artworkentry.domain.ArtworkEntryVO;
import com.pcwk.ehr.artworkentry.service.ArtworkEntryService;
import com.pcwk.ehr.cmn.SessionConst;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * ArtworkEntryController - 작업일지(artwork_entry)의 웹 진입점.
 * 목록(entry_list)은 공개작업 상세(working/view)에서 ArtworkService.viewWithEntry() 로
 * 이미 조합되어 내려가므로, 이 컨트롤러는 등록/수정/삭제 액션만 담당한다.
 *
 * [설계서 매핑 - CC-ENT-01]
 *   doSave / doUpdate / doDelete : 전부 상위 작품 작성자 본인만 (권한체크 보강 완료)
 *   - 수정/삭제는 클라이언트 artworkId 를 신뢰하지 않고 entry PK 로 재조회해 소속 확인.
 *
 * 처리 후 상위 작품의 공개작업 상세(working/view)로 redirect (PRG 패턴).
 * ※ 첨부파일(target_type=ARTWORK_ENTRY)은 attach_file 테이블 소관(다른 모듈)이라
 *   여기서는 다루지 않는다.
 */
@Controller
@RequestMapping("/artworkEntry")
public class ArtworkEntryController {

	Logger log = LogManager.getLogger(getClass()); // 클래스 전용 로거

	@Autowired
	private ArtworkEntryService artworkEntryService; // 작업일지 CRUD 로직 위임 대상

	@Autowired
	private ArtworkService artworkService;           // 상위 작품 소유자 검증용

	public ArtworkEntryController() {
		log.debug("ArtworkEntryController"); // 빈 생성 확인용 로그
	}

	/**
	 * 작업일지 등록 폼 (CC-ENT-01).
	 * 빈 등록 화면만 렌더링. 저장은 doSave 가 처리.
	 */
	@GetMapping("/reg")
	public String regForm() {
		return "artworkEntry/reg"; // /WEB-INF/views/artworkEntry/reg.jsp
	}

	/**
	 * 작업일지 등록 처리 (CC-ENT-01).
	 * 권한: 상위 작품 작성자 본인만. 등록 후 상세로 redirect.
	 */
	@PostMapping("/doSave")
	public String doSave(@ModelAttribute ArtworkEntryVO param, HttpSession session) {
		log.debug("doSave param: " + param);

		MemberVO login = getLoginMember(session);
		if (login == null) {
			return "redirect:/member/login.do";
		}
		if (!isArtworkOwner(login, param.getArtworkId())) {
			return "redirect:/artwork/working/view?artworkId=" + param.getArtworkId();
		}

		artworkEntryService.doSave(param); // 작업일지 등록 위임

		return "redirect:/artwork/working/view?artworkId=" + param.getArtworkId();
	}

	/**
	 * 작업일지 수정 처리 (CC-ENT-01).
	 * 권한: entry PK 로 재조회한 소속 작품의 작성자 본인만(클라이언트 artworkId 미신뢰).
	 */
	@PostMapping("/doUpdate")
	public String doUpdate(@ModelAttribute ArtworkEntryVO param, HttpSession session) {
		log.debug("doUpdate param: " + param);

		MemberVO login = getLoginMember(session);
		if (login == null) {
			return "redirect:/member/login.do";
		}
		ArtworkEntryVO saved = artworkEntryService.doSelectOne(param); // entry PK 기준 재조회
		if (saved == null || !isArtworkOwner(login, saved.getArtworkId())) {
			return "redirect:/artwork/working/view?artworkId=" + (saved != null ? saved.getArtworkId() : param.getArtworkId());
		}

		artworkEntryService.doUpdate(param); // 작업일지 수정 위임

		return "redirect:/artwork/working/view?artworkId=" + saved.getArtworkId();
	}

	/**
	 * 작업일지 삭제 처리 (CC-ENT-01).
	 * 권한: entry PK 로 재조회한 소속 작품의 작성자 본인만.
	 */
	@PostMapping("/doDelete")
	public String doDelete(@ModelAttribute ArtworkEntryVO param, HttpSession session) {
		log.debug("doDelete param: " + param);

		MemberVO login = getLoginMember(session);
		if (login == null) {
			return "redirect:/member/login.do";
		}
		ArtworkEntryVO saved = artworkEntryService.doSelectOne(param); // entry PK 기준 재조회
		if (saved == null || !isArtworkOwner(login, saved.getArtworkId())) {
			return "redirect:/artwork/working/view?artworkId=" + (saved != null ? saved.getArtworkId() : param.getArtworkId());
		}

		artworkEntryService.doDelete(param); // 작업일지 삭제 위임

		return "redirect:/artwork/working/view?artworkId=" + saved.getArtworkId();
	}

	// =================================================================
	//  권한 헬퍼
	// =================================================================

	/** 세션의 로그인 회원(MemberVO). 비로그인 시 null */
	private MemberVO getLoginMember(HttpSession session) {
		Object obj = session.getAttribute(SessionConst.LOGIN_MEMBER);
		return (obj instanceof MemberVO) ? (MemberVO) obj : null;
	}

	/** 상위 작품(artworkId)의 작성자 본인인지 */
	private boolean isArtworkOwner(MemberVO login, int artworkId) {
		ArtworkVO cond = new ArtworkVO();
		cond.setArtworkId(artworkId);
		ArtworkVO artwork = artworkService.findOne(cond); // 조회수 증가 없는 순수 조회
		return artwork != null && artwork.getMemberId() == login.getMemberId();
	}
}
