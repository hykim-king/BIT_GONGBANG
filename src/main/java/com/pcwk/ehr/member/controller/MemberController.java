package com.pcwk.ehr.member.controller;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.artwork.service.ArtworkService;
import com.pcwk.ehr.cmn.MessageVO;
import com.pcwk.ehr.cmn.SessionConst;
import com.pcwk.ehr.member.domain.MemberVO;
import com.pcwk.ehr.member.service.MemberService;

@Controller
@RequestMapping("/member")
public class MemberController {

	private static final Logger log = LogManager.getLogger(MemberController.class);

	@Autowired
	private MemberService memberService;

	@Autowired
	private ArtworkService artworkService;

	public MemberController() {
		log.debug("MemberController");
	}

	@GetMapping("/login.do")
	public String loginForm() {
		return "member/login";
	}

	@PostMapping("/doLogin.do")
	public String doLogin(MemberVO param, HttpSession session, Model model) {
		log.debug("doLogin param: {}", param);

		MemberVO loginMember = memberService.login(param);
		if (loginMember == null) {
			model.addAttribute("errorMsg", "이메일 또는 비밀번호가 올바르지 않습니다.");
			return "member/login";
		}

		session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
		return "redirect:/member/mypage.do";
	}

	@PostMapping("/doLoginAjax.do")
	@ResponseBody
	public MessageVO doLoginAjax(MemberVO param, HttpSession session) {
		log.debug("doLoginAjax param: {}", param);

		MemberVO loginMember = memberService.login(param);
		if (loginMember == null) {
			return new MessageVO("400", "로그인 실패");
		}
		session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);
		return new MessageVO("200", "로그인 성공", loginMember);
	}

	/** 로그아웃: 세션 무효화 후 홈으로 (CC-CMN-01 이벤트 8) */
	@GetMapping("/logout.do")
	public String logout(HttpSession session) {
		if (session != null) {
			session.invalidate();
		}
		return "redirect:/main/index.do";
	}

	@GetMapping("/join.do")
	public String joinForm() {
		return "member/join";
	}

	@PostMapping("/doSave.do")
	@ResponseBody
	public MessageVO doSave(MemberVO param) {
		log.debug("doSave param: {}", param);

		int flag = memberService.doSave(param);
		if (flag == 1) {
			param.setPassword(null);
			param.setConfirmPassword(null);
			return new MessageVO("200", "회원가입 성공", param);
		}
		return new MessageVO("400", "회원가입 실패(입력값·중복 확인)");
	}

	@PostMapping("/checkEmail.do")
	@ResponseBody
	public MessageVO checkEmail(MemberVO param) {
		log.debug("checkEmail param: {}", param);
		boolean exists = memberService.existsEmail(param);
		return new MessageVO("200", exists ? "중복" : "사용 가능", !exists);
	}

	@PostMapping("/checkNickname.do")
	@ResponseBody
	public MessageVO checkNickname(MemberVO param) {
		log.debug("checkNickname param: {}", param);
		boolean exists = memberService.existsNickname(param);
		return new MessageVO("200", exists ? "중복" : "사용 가능", !exists);
	}

	/** 마이페이지(CC-USR-03): 프로필+집계 + 3탭(공개/완성/관심) 작품 목록 */
	@GetMapping("/mypage.do")
	public String mypage(HttpSession session, Model model) {
		MemberVO loginMember = getLoginMember(session);
		MemberVO myPage = memberService.getMyPage(loginMember.getMemberId());
		model.addAttribute("member", myPage);

		ArtworkVO cond = new ArtworkVO();
		cond.setMemberId(loginMember.getMemberId());
		cond.setIsStatus("N");
		model.addAttribute("workingList", artworkService.selectByMember(cond));

		ArtworkVO condY = new ArtworkVO();
		condY.setMemberId(loginMember.getMemberId());
		condY.setIsStatus("Y");
		model.addAttribute("completeList", artworkService.selectByMember(condY));

		ArtworkVO condLike = new ArtworkVO();
		condLike.setMemberId(loginMember.getMemberId());
		model.addAttribute("likedList", artworkService.selectLikedByMember(condLike));

		return "member/mypage";
	}

	@GetMapping("/modify.do")
	public String modifyForm(HttpSession session, Model model) {
		MemberVO loginMember = getLoginMember(session);
		MemberVO member = memberService.getMyPage(loginMember.getMemberId());
		model.addAttribute("member", member);
		return "member/member_modify";
	}

	@PostMapping("/doUpdate.do")
	@ResponseBody
	public MessageVO doUpdate(MemberVO param, HttpSession session) {
		log.debug("doUpdate param: {}", param);

		MemberVO loginMember = getLoginMember(session);
		param.setMemberId(loginMember.getMemberId());

		int flag = memberService.doUpdate(param);
		if (flag == 1) {
			MemberVO refreshed = memberService.doSelectOne(param);
			session.setAttribute(SessionConst.LOGIN_MEMBER, refreshed);
			return new MessageVO("200", "정보 수정 성공", refreshed);
		}
		return new MessageVO("400", "정보 수정 실패");
	}

	@PostMapping("/doDelete.do")
	@ResponseBody
	public MessageVO doDelete(HttpSession session) {
		MemberVO loginMember = getLoginMember(session);

		int flag = memberService.doDelete(loginMember);
		if (flag == 1) {
			session.invalidate();
			return new MessageVO("200", "회원 탈퇴 완료");
		}
		return new MessageVO("400", "회원 탈퇴 실패");
	}

	private MemberVO getLoginMember(HttpSession session) {
		return (MemberVO) session.getAttribute(SessionConst.LOGIN_MEMBER);
	}

}
