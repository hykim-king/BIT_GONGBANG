package com.pcwk.ehr.admin.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pcwk.ehr.admin.service.AdminService;
import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.cmn.DTO;
import com.pcwk.ehr.cmn.PageUtil;
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

	/** 대시보드(통계) */
	@GetMapping("/dashboard.do")
	public String dashboard(Model model) {
		model.addAttribute("stats", adminService.stats());
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
