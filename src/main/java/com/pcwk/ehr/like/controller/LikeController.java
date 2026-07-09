package com.pcwk.ehr.like.controller;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pcwk.ehr.cmn.MessageVO;
import com.pcwk.ehr.cmn.SessionConst;
import com.pcwk.ehr.like.domain.LikeVO;
import com.pcwk.ehr.like.service.LikeService;
import com.pcwk.ehr.member.domain.MemberVO;

@Controller
@RequestMapping("/like")
public class LikeController {

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private LikeService likeService;

	public LikeController() {
		log.debug("LikeController");
	}

	@PostMapping("/toggle.do")
	@ResponseBody
	public MessageVO toggle(LikeVO param, HttpSession session) {
		log.debug("toggle param: " + param);
		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return new MessageVO("401", "로그인이 필요합니다.");
		}
		param.setMemberId(loginMember.getMemberId());

		return new MessageVO("200", "처리 성공", likeService.toggle(param));
	}

	@PostMapping("/count.do")
	@ResponseBody
	public MessageVO count(LikeVO param) {
		log.debug("count param: " + param);
		return new MessageVO("200", "조회 성공", likeService.countByTarget(param));
	}

	@PostMapping("/myLikes.do")
	@ResponseBody
	public MessageVO myLikes(HttpSession session) {
		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return new MessageVO("401", "로그인이 필요합니다.");
		}

		LikeVO param = new LikeVO();
		param.setMemberId(loginMember.getMemberId());
		log.debug("myLikes memberId: " + param.getMemberId());
		return new MessageVO("200", "조회 성공", likeService.selectByMember(param));
	}

	private MemberVO getLoginMember(HttpSession session) {
		if (session == null) {
			return null;
		}
		Object loginMember = session.getAttribute(SessionConst.LOGIN_MEMBER);
		return loginMember instanceof MemberVO ? (MemberVO) loginMember : null;
	}
}
