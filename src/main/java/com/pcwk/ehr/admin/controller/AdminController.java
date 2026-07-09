package com.pcwk.ehr.admin.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pcwk.ehr.admin.service.AdminService;
import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.cmn.DTO;
import com.pcwk.ehr.cmn.MessageVO;
import com.pcwk.ehr.cmn.PageUtil;
import com.pcwk.ehr.cmn.SessionConst;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * 관리자 컨트롤러. 전 URL(/admin/**) 은 servlet-context 의 AdminInterceptor 로 관리자 세션 보호.
 * 조회 화면(@GetMapping) 위주. 화면 JSP 는 후속 단계(백엔드 우선).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

	Logger log = LogManager.getLogger(getClass());

	private static final int DEFAULT_PAGE_SIZE = 10;

	@Autowired
	private AdminService adminService;

	public AdminController() {
		log.debug("AdminController");
	}

	/** 대시보드(통계): 총계 5종 + 완성:공개 구성비 + 카테고리별 게시글 수 + 최근 가입 회원 */
	@GetMapping("/dashboard.do")
	public String dashboard(Model model) {
		model.addAttribute("stats", adminService.stats());
		model.addAttribute("statusRatio", adminService.statusRatio());
		model.addAttribute("categoryStats", adminService.categoryArtworkStats());
		model.addAttribute("recentMembers", adminService.recentMembers(5));
		return "admin/dashboard";
	}

	/** 회원 관리(목록·페이징·검색) */
	@GetMapping("/member_list.do")
	public String memberList(@ModelAttribute MemberVO cond, HttpServletRequest req, Model model) {
		normalizePaging(cond);
		List<MemberVO> list = adminService.listMember(cond);
		int totalCnt = adminService.memberCnt(cond);
		model.addAttribute("list", list);
		model.addAttribute("page", new PageUtil(cond.getPageNo(), cond.getPageSize(), totalCnt));
		model.addAttribute("pagingUrl", pagingUrl(req, "/admin/member_list.do", cond.getSearchDiv(),
				cond.getSearchWord(), cond.getPageSize()));
		return "admin/admin_member_list";
	}

	/** 게시물 관리(작품 전체: 완성/공개작업) */
	@GetMapping("/artwork_list.do")
	public String artworkList(@ModelAttribute ArtworkVO cond, HttpServletRequest req, Model model) {
		normalizePaging(cond);
		List<ArtworkVO> list = adminService.listArtwork(cond);
		int totalCnt = adminService.artworkCnt(cond);
		model.addAttribute("list", list);
		model.addAttribute("page", new PageUtil(cond.getPageNo(), cond.getPageSize(), totalCnt));
		model.addAttribute("pagingUrl", pagingUrl(req, "/admin/artwork_list.do", cond.getSearchDiv(),
				cond.getSearchWord(), cond.getPageSize()));
		return "admin/admin_artwork_list";
	}

	/** 카테고리 관리(화면). CRUD 는 기존 /category AJAX 재사용 */
	@GetMapping("/category.do")
	public String category() {
		return "admin/admin_category";
	}

	/**
	 * 관리자 회원 수정 (CC-ADM-01 신설, AJAX MessageVO).
	 * 닉네임/관리자여부만 갱신. 자기 자신의 관리자 해제는 차단(관리자 잠금 방지).
	 */
	@PostMapping("/member_update.do")
	@ResponseBody
	public MessageVO memberUpdate(MemberVO param, HttpSession session) {
		log.debug("memberUpdate param: " + param);

		MemberVO login = (MemberVO) session.getAttribute(SessionConst.LOGIN_MEMBER);
		if (login != null && login.getMemberId() == param.getMemberId()
				&& "N".equals(param.getIsAdmin())) {
			return new MessageVO("400", "자기 자신의 관리자 권한은 해제할 수 없습니다.");
		}

		int flag = adminService.updateMember(param);
		return flag == 1
				? new MessageVO("200", "회원 정보가 수정되었습니다.")
				: new MessageVO("400", "회원 수정 실패(입력값 확인)");
	}

	/**
	 * 관리자 회원 삭제 (CC-ADM-01 신설, AJAX MessageVO).
	 * 연쇄 삭제(팀 확정): 소유 작품(첨부 물리파일 포함)·작업일지·댓글·좋아요까지 정리.
	 * 자기 자신은 삭제 불가.
	 */
	@PostMapping("/member_delete.do")
	@ResponseBody
	public MessageVO memberDelete(MemberVO param, HttpSession session) {
		log.debug("memberDelete param: " + param);

		MemberVO login = (MemberVO) session.getAttribute(SessionConst.LOGIN_MEMBER);
		if (login != null && login.getMemberId() == param.getMemberId()) {
			return new MessageVO("400", "자기 자신은 삭제할 수 없습니다. 마이페이지 탈퇴를 이용하세요.");
		}

		try {
			int flag = adminService.deleteMember(param);
			return flag == 1
					? new MessageVO("200", "회원과 연관 데이터가 삭제되었습니다.")
					: new MessageVO("400", "회원 삭제 실패(대상 없음)");
		} catch (IOException e) {
			log.error("memberDelete 실패(첨부 파일 정리): memberId=" + param.getMemberId(), e);
			return new MessageVO("500", "첨부 파일 정리 중 오류가 발생해 삭제가 취소되었습니다.");
		}
	}

	/** pageNo>=1, pageSize>=1(기본 10) 보정. MemberVO/ArtworkVO 공통 부모 DTO 로 처리 */
	private void normalizePaging(DTO cond) {
		if (cond.getPageNo() < 1) {
			cond.setPageNo(1);
		}
		if (cond.getPageSize() < 1) {
			cond.setPageSize(DEFAULT_PAGE_SIZE);
		}
	}

	/** 페이징 링크 베이스 URL(검색조건 유지). JSP 가 &pageNo= 를 이어붙인다 */
	private String pagingUrl(HttpServletRequest req, String path, String searchDiv, String searchWord, int pageSize) {
		return req.getContextPath() + path + "?searchDiv=" + nvl(searchDiv)
				+ "&searchWord=" + nvl(searchWord) + "&pageSize=" + pageSize;
	}

	private String nvl(String s) {
		return s == null ? "" : s;
	}
}
