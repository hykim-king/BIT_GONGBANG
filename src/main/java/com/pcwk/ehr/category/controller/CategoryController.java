package com.pcwk.ehr.category.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pcwk.ehr.category.domain.CategoryVO;
import com.pcwk.ehr.category.service.CategoryService;
import com.pcwk.ehr.cmn.MessageVO;
import com.pcwk.ehr.cmn.SessionConst;
import com.pcwk.ehr.member.domain.MemberVO;

/**
 * 카테고리 컨트롤러. 전부 AJAX(@ResponseBody MessageVO, code "200"성공/"400"실패).
 * - doRetrieve/doSelectOne : 공개 조회(GET).
 * - doSave/doUpdate/doDelete : 관리자(세션 loginMember 의 is_admin=='Y') 전용(POST).
 * ※ /category 는 /admin/** 밖이라 인터셉터 대상이 아니며, 쓰기 3종은 loginMember.getIsAdmin() 가드로 보호한다.
 */
@Controller
@RequestMapping("/category")
public class CategoryController {

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private CategoryService categoryService;

	public CategoryController() {
		log.debug("CategoryController");
	}

	/** 전체 카테고리 목록(드롭다운/관리) — 공개. data=List&lt;CategoryVO&gt; */
	@GetMapping("/doRetrieve.do")
	@ResponseBody
	public MessageVO doRetrieve() {
		List<CategoryVO> list = categoryService.doRetrieve(new CategoryVO());
		return new MessageVO("200", "조회 성공", list);
	}

	/** 단건 조회 — data=CategoryVO */
	@GetMapping("/doSelectOne.do")
	@ResponseBody
	public MessageVO doSelectOne(@RequestParam int categoryId) {
		CategoryVO param = new CategoryVO();
		param.setCategoryId(categoryId);
		try {
			CategoryVO vo = categoryService.doSelectOne(param);
			if (vo == null) { // MyBatis 기본은 미존재 시 null
				return new MessageVO("400", "해당 카테고리가 없습니다.");
			}
			return new MessageVO("200", "조회 성공", vo);
		} catch (EmptyResultDataAccessException e) {
			return new MessageVO("400", "해당 카테고리가 없습니다.");
		}
	}

	/** 등록(관리자) — data=채번된 categoryId */
	@PostMapping("/doSave.do")
	@ResponseBody
	public MessageVO doSave(@ModelAttribute CategoryVO param, HttpSession session) {
		log.debug("doSave param: " + param);
		if (!isAdmin(session)) {
			return new MessageVO("400", "권한이 없습니다.");
		}
		if (param.getCategoryNm() == null || param.getCategoryNm().trim().isEmpty()) {
			return new MessageVO("400", "카테고리명을 입력해 주세요.");
		}
		int flag = categoryService.doSave(param);
		return flag == 1
				? new MessageVO("200", "등록되었습니다.", param.getCategoryId())
				: new MessageVO("400", "등록에 실패했습니다.");
	}

	/** 수정(관리자) */
	@PostMapping("/doUpdate.do")
	@ResponseBody
	public MessageVO doUpdate(@ModelAttribute CategoryVO param, HttpSession session) {
		log.debug("doUpdate param: " + param);
		if (!isAdmin(session)) {
			return new MessageVO("400", "권한이 없습니다.");
		}
		if (param.getCategoryNm() == null || param.getCategoryNm().trim().isEmpty()) {
			return new MessageVO("400", "카테고리명을 입력해 주세요.");
		}
		int flag = categoryService.doUpdate(param);
		return flag == 1 ? new MessageVO("200", "수정되었습니다.") : new MessageVO("400", "수정에 실패했습니다.");
	}

	/** 삭제(관리자) — 작품이 참조 중이면 FK 무결성 예외 방어 */
	@PostMapping("/doDelete.do")
	@ResponseBody
	public MessageVO doDelete(@RequestParam int categoryId, HttpSession session) {
		log.debug("doDelete categoryId: " + categoryId);
		if (!isAdmin(session)) {
			return new MessageVO("400", "권한이 없습니다.");
		}
		CategoryVO param = new CategoryVO();
		param.setCategoryId(categoryId);
		try {
			int flag = categoryService.doDelete(param);
			return flag == 1 ? new MessageVO("200", "삭제되었습니다.") : new MessageVO("400", "삭제에 실패했습니다.");
		} catch (DataIntegrityViolationException e) {
			log.debug("카테고리 삭제 실패(참조 중) categoryId={}", categoryId);
			return new MessageVO("400", "작품이 등록된 카테고리는 삭제할 수 없습니다.");
		}
	}

	/**
	 * 관리자 여부(세션). 팀 세션 모델 기준 — m1 로그인은 세션에 loginMember(MemberVO)만 저장하므로
	 * 별도 "isAdmin" 속성이 아니라 MemberVO.is_admin 필드로 판정한다.
	 */
	private boolean isAdmin(HttpSession session) {
		Object loginMember = session.getAttribute(SessionConst.LOGIN_MEMBER);
		return loginMember instanceof MemberVO && "Y".equals(((MemberVO) loginMember).getIsAdmin());
	}
}
