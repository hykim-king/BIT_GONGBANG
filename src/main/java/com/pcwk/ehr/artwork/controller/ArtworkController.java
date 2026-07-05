package com.pcwk.ehr.artwork.controller;
 
import java.util.List;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
 
import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.artwork.service.ArtworkService;
 
/**
 * ArtworkController (ver2) - 게시판(완성 + 공개작업)의 웹 진입점.
 *
 * [역할 - 4단계]
 *   1) URL 매핑        : @GetMapping / @PostMapping 으로 요청 경로 배정
 *   2) 파라미터 수집   : @ModelAttribute ArtworkVO 로 폼/쿼리값을 VO 에 자동 바인딩
 *   3) Service 호출    : artworkService 에 로직 위임 (Controller 는 로직/SQL 없음)
 *   4) 결과 전달       : 조회 -> Model 담고 JSP 뷰 반환 / 액션 -> redirect(PRG)
 *
 * [게시판 분기]
 *   완성게시판  : /artwork/complete/*  (is_status='Y')            - 완성 사진만
 *   공개작업게시판: /artwork/working/*   (is_status=null → 전체)    - Y+N 노출, 작업일지 포함
 *
 * [ver2 변경점]
 *   - 공개작업 상세를 artworkService.viewWithEntry() 로 호출 (작품 + 작업일지 조합)
 *   - 뷰 반환 방식(@Controller). 댓글/좋아요 같은 AJAX(.do + MessageVO)와 달리
 *     게시판은 실제 페이지 이동이라 JSP 뷰를 반환.
 */
@Controller
@RequestMapping("/artwork")
public class ArtworkController {
 
	Logger log = LogManager.getLogger(getClass());
 
	@Autowired
	private ArtworkService artworkService;
 
	public ArtworkController() {
		log.debug("ArtworkController");
	}
 
	// =================================================================
	//  완성게시판 (is_status='Y')  -  완성된 작품만 노출
	// =================================================================
 
	/**
	 * 완성작 목록 (CC-CPL-01).
	 * isStatus='Y' 로 강제 → 완성작만 조회. 검색/페이징은 param(DTO 상속)으로 전달.
	 */
	@GetMapping("/complete/list")
	public String completeList(@ModelAttribute ArtworkVO param, Model model) {
		log.debug("completeList param: " + param);
 
		param.setIsStatus("Y");                              // 완성작만
		List<ArtworkVO> list = artworkService.doRetrieve(param);   // 목록
		int totalCnt = artworkService.selectCount(param);          // 총건수(페이징)
 
		model.addAttribute("list", list);
		model.addAttribute("totalCnt", totalCnt);
		return "artwork/complete/list";                     
	}
 
	/**
	 * 완성작 상세 (CC-CPL-02).
	 * 서비스 doSelectOne 내부에서 조회수 +1 후 작품을 반환.
	 * (완성게시판 상세는 작품만. 작업일지는 공개작업 상세 전용)
	 */
	@GetMapping("/complete/view")
	public String completeView(ArtworkVO param, Model model) {
		log.debug("completeView param: " + param);
 
		ArtworkVO outVO = artworkService.doSelectOne(param); // 조회수+1 + 작품 조회
		model.addAttribute("vo", outVO);
		return "artwork/complete/view";
	}
 
	/**
	 * 완성작 등록 폼 (CC-CPL-03).
	 * 빈 등록 화면만 렌더링. 저장은 completeDoSave 가 처리.
	 */
	@GetMapping("/complete/reg")
	public String completeRegForm() {
		return "artwork/complete/reg";
	}
 
	/**
	 * 완성작 등록 처리 (CC-CPL-03).
	 * isStatus='Y' 로 등록. 등록 후 selectKey 로 param.artworkId 가 채워지므로
	 * 그 id 로 상세페이지로 redirect (PRG: 새로고침 중복등록 방지).
	 * 완성일(comp_dt)은 Mapper(doSave)에서 is_status='Y'일 때 SYSDATE로 자동 세팅됨.
	 */
	@PostMapping("/complete/doSave")
	public String completeDoSave(@ModelAttribute ArtworkVO param) {
		log.debug("completeDoSave param: " + param);

		param.setIsStatus("Y");                              // 완성으로 등록
		artworkService.doSave(param);                        // selectKey → param.artworkId 채움, comp_dt는 SQL에서 자동 세팅

		return "redirect:/artwork/complete/view?artworkId=" + param.getArtworkId();
	}
 
	/**
	 * 완성작 수정 폼 (CC-CPL-04).
	 * 기존 작품을 조회해 폼에 채워 보여준다.
	 * findOne 은 조회수를 올리지 않는 순수 조회라, 수정 폼 진입만으로 조회수가 오르지 않는다.
	 */
	@GetMapping("/complete/modify")
	public String completeModifyForm(ArtworkVO param, Model model) {
		log.debug("completeModifyForm param: " + param);

		ArtworkVO outVO = artworkService.findOne(param);   // 조회수 증가 없는 순수 조회
		model.addAttribute("vo", outVO);
		return "artwork/complete/modify";
	}
 
	// =================================================================
	//  공개작업게시판 (is_status=null → 하이브리드 필터: N 이거나 작업일지 존재)  -  작업일지 포함
	// =================================================================
 
	/**
	 * 공개작업 목록 (CC-WRK-01).
	 * isStatus 를 세팅하지 않으면 Mapper(doRetrieve)에서 하이브리드 필터 적용:
	 * is_status='N' 이거나 artwork_entry(작업일지)가 하나라도 있는 작품만 노출.
	 * (완성 전환돼도 작업일지가 있으면 계속 보이고, 작업일지 없이 바로 완성 등록된 건 제외)
	 */
	@GetMapping("/working/list")
	public String workingList(@ModelAttribute ArtworkVO param, Model model) {
		log.debug("workingList param: " + param);
 
		// isStatus 미설정 → Mapper 의 하이브리드 필터(N OR 작업일지 존재)로 조회
		List<ArtworkVO> list = artworkService.doRetrieve(param);
		int totalCnt = artworkService.selectCount(param);
 
		model.addAttribute("list", list);
		model.addAttribute("totalCnt", totalCnt);
		return "artwork/working/list";
	}
 
	/**
	 * 공개작업 상세 (CC-WRK-02).
	 * ver2: 서비스 viewWithEntry 로 '작품 + 작업일지 타임라인'을 한 번에 받는다.
	 * 조회수 증가 + 작품 조회 + 작업일지 조회가 서비스에서 한 트랜잭션으로 처리됨.
	 * 반환된 vo.entryList 에 작업일지가 담겨 있어 JSP 에서 타임라인 렌더 가능.
	 */
	@GetMapping("/working/view")
	public String workingView(ArtworkVO param, Model model) {
		log.debug("workingView param: " + param);
 
		ArtworkVO outVO = artworkService.viewWithEntry(param);   // 작품 + 작업일지 조합
		model.addAttribute("vo", outVO);
		return "artwork/working/view";
	}
 
	/**
	 * 공개작업 등록 폼 (CC-WRK-03).
	 */
	@GetMapping("/working/reg")
	public String workingRegForm() {
		return "artwork/working/reg";
	}
 
	/**
	 * 공개작업 등록 처리 (CC-WRK-03).
	 * isStatus='N' 로 등록 (comp_dt=null). 등록 후 상세로 redirect.
	 */
	@PostMapping("/working/doSave")
	public String workingDoSave(@ModelAttribute ArtworkVO param) {
		log.debug("workingDoSave param: " + param);
 
		param.setIsStatus("N");                              // 공개작업(작업중)으로 등록
		artworkService.doSave(param);
 
		return "redirect:/artwork/working/view?artworkId=" + param.getArtworkId();
	}
 
	// =================================================================
	//  공통 액션 (수정 / 삭제 / 완성전환)
	// =================================================================
 
	/**
	 * 수정 처리 (CC-CPL-04 / CC-WRK-04).
	 * 완성/공개작업 공용. 제목·내용 등을 수정 후, 저장된 is_status 기준으로
	 * 해당 게시판(완성/공개작업) 상세로 redirect. (하드코딩된 complete 고정 redirect 이슈 수정)
	 * (권한 체크: 작성자/관리자만 - 인터셉터 또는 서비스 보강 필요, 현재 미구현)
	 */
	@PostMapping("/doUpdate")
	public String doUpdate(@ModelAttribute ArtworkVO param) {
		log.debug("doUpdate param: " + param);

		artworkService.doUpdate(param);                              // 제목/내용/수정일 갱신
		ArtworkVO saved = artworkService.findOne(param);             // 조회수 증가 없이 is_status 재확인
		String isStatus = saved != null ? saved.getIsStatus() : param.getIsStatus(); // 재조회 실패 시 폴백
		return "redirect:" + viewUrl(isStatus, param.getArtworkId()); // is_status 에 맞는 게시판 상세로 이동
	}

	/**
	 * 삭제 처리.
	 * 서비스 doDelete 가 삭제 오케스트레이션 담당(첨부/댓글/좋아요 수동삭제는 팀원 Mapper 연동 후).
	 * artwork_entry 는 FK CASCADE 로 자동 삭제.
	 * 삭제 전에 is_status 를 확인해(삭제 후엔 조회 불가) 원래 있던 게시판 목록으로 redirect.
	 * (하드코딩된 complete 고정 redirect 이슈 수정)
	 */
	@PostMapping("/doDelete")
	public String doDelete(@ModelAttribute ArtworkVO param) {
		log.debug("doDelete param: " + param);

		ArtworkVO target = artworkService.findOne(param);            // 삭제 전 is_status 확보 (삭제 후엔 조회 불가)
		String isStatus = target != null ? target.getIsStatus() : param.getIsStatus(); // 재조회 실패 시 폴백
		artworkService.doDelete(param);                              // 실제 삭제
		return "redirect:" + listUrl(isStatus);                      // is_status 에 맞는 게시판 목록으로 이동
	}

	/** is_status='Y' → 완성 상세, 그 외('N') → 공개작업 상세 */
	private String viewUrl(String isStatus, int artworkId) {
		String board = "Y".equals(isStatus) ? "/artwork/complete/view" : "/artwork/working/view";
		return board + "?artworkId=" + artworkId;
	}

	/** is_status='Y' → 완성 목록, 그 외('N') → 공개작업 목록 */
	private String listUrl(String isStatus) {
		return "Y".equals(isStatus) ? "/artwork/complete/list" : "/artwork/working/list";
	}
 
	/**
	 * 완성 전환 (CC-WRK, 공개작업 → 완성).
	 * is_status='Y' + comp_dt=SYSDATE 로 전환 후, 완성 상세로 redirect.
	 * 전환 후에도 작업일지가 있으면 공개작업 목록(하이브리드 필터)에 계속 보인다.
	 */
	@PostMapping("/working/complete")
	public String complete(@ModelAttribute ArtworkVO param) {
		log.debug("complete param: " + param);
 
		artworkService.complete(param);
		return "redirect:/artwork/complete/view?artworkId=" + param.getArtworkId();
	}
}