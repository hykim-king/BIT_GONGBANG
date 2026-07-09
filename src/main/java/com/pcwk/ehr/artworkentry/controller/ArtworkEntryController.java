package com.pcwk.ehr.artworkentry.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pcwk.ehr.artworkentry.domain.ArtworkEntryVO;
import com.pcwk.ehr.artworkentry.service.ArtworkEntryService;

/**
 * ArtworkEntryController - 작업일지(artwork_entry)의 웹 진입점.
 * 목록(entry_list)은 공개작업 상세(working/view)에서 ArtworkService.viewWithEntry() 로
 * 이미 조합되어 내려가므로, 이 컨트롤러는 등록/수정/삭제 액션만 담당한다.
 *
 * [설계서 매핑 - CC-ENT-01 / M3-2 작업일지]
 *   entry_reg    : 등록 폼 + 등록 처리
 *   entry_modify : 수정 처리
 *   entry_del    : 삭제 처리
 *
 * 처리 후 상위 작품의 공개작업 상세(working/view)로 redirect (PRG 패턴, 새로고침 중복방지).
 * ※ 첨부파일(target_type=ARTWORK_ENTRY)은 attach_file 테이블 소관(다른 모듈)이라
 *   여기서는 다루지 않는다.
 */
@Controller
@RequestMapping("/artworkEntry")
public class ArtworkEntryController {

	Logger log = LogManager.getLogger(getClass()); // 클래스 전용 로거

	@Autowired
	private ArtworkEntryService artworkEntryService; // 작업일지 CRUD 로직 위임 대상

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
	 * 등록 후 상위 작품의 공개작업 상세로 redirect.
	 */
	@PostMapping("/doSave")
	public String doSave(@ModelAttribute ArtworkEntryVO param) {
		log.debug("doSave param: " + param); // 등록 요청 파라미터 로그

		artworkEntryService.doSave(param); // 작업일지 등록 위임

		return "redirect:/artwork/working/view?artworkId=" + param.getArtworkId(); // 등록 후 상세로 이동
	}

	/**
	 * 작업일지 수정 처리.
	 * (권한 체크: 작성자/관리자만 - 인터셉터 또는 서비스 보강 필요, 현재 미구현)
	 */
	@PostMapping("/doUpdate")
	public String doUpdate(@ModelAttribute ArtworkEntryVO param) {
		log.debug("doUpdate param: " + param); // 수정 요청 파라미터 로그

		artworkEntryService.doUpdate(param); // 작업일지 수정 위임

		return "redirect:/artwork/working/view?artworkId=" + param.getArtworkId(); // 수정 후 상세로 이동
	}

	/**
	 * 작업일지 삭제 처리.
	 * 삭제 폼에는 artworkEntry(PK) 와 artworkId(상위 작품, redirect 용)가 함께 전달되어야 한다.
	 */
	@PostMapping("/doDelete")
	public String doDelete(@ModelAttribute ArtworkEntryVO param) {
		log.debug("doDelete param: " + param); // 삭제 요청 파라미터 로그

		artworkEntryService.doDelete(param); // 작업일지 삭제 위임

		return "redirect:/artwork/working/view?artworkId=" + param.getArtworkId(); // 삭제 후 상세로 이동
	}
}
