package com.pcwk.ehr.comment.controller;

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
import com.pcwk.ehr.comment.domain.CommentVO;
import com.pcwk.ehr.comment.service.CommentService;
import com.pcwk.ehr.member.domain.MemberVO;

@Controller
@RequestMapping("/comment")
public class CommentController {

	Logger log = LogManager.getLogger(getClass());

	@Autowired
	private CommentService commentService;

	public CommentController() {
		log.debug("CommentController");
	}

	@PostMapping("/doRetrieve.do")
	@ResponseBody
	public MessageVO doRetrieve(CommentVO param) {
		log.debug("doRetrieve param: " + param);
		return new MessageVO("200", "조회 성공", commentService.doRetrieve(param));
	}

	@PostMapping("/countByTarget.do")
	@ResponseBody
	public MessageVO countByTarget(CommentVO param) {
		log.debug("countByTarget param: " + param);
		return new MessageVO("200", "조회 성공", commentService.countByTarget(param));
	}

	@PostMapping("/doSelectOne.do")
	@ResponseBody
	public MessageVO doSelectOne(CommentVO param, HttpSession session) {
		log.debug("doSelectOne param: " + param);
		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return new MessageVO("401", "로그인이 필요합니다.");
		}
		param.setMemberId(loginMember.getMemberId());

		CommentVO outVO = commentService.doSelectOne(param);
		if (outVO == null) {
			return new MessageVO("404", "댓글을 찾을 수 없습니다.");
		}
		return new MessageVO("200", "조회 성공", outVO);
	}

	@PostMapping("/doSave.do")
	@ResponseBody
	public MessageVO doSave(CommentVO param, HttpSession session) {
		log.debug("doSave param: " + param);
		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return new MessageVO("401", "로그인이 필요합니다.");
		}
		param.setMemberId(loginMember.getMemberId());

		int flag = commentService.doSave(param);
		if (flag == 1) {
			return new MessageVO("200", "댓글 등록 성공", param);
		}
		return new MessageVO("400", "댓글 등록 실패(내용 없음 또는 1000자 초과)");
	}

	@PostMapping("/doUpdate.do")
	@ResponseBody
	public MessageVO doUpdate(CommentVO param, HttpSession session) {
		log.debug("doUpdate param: " + param);
		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return new MessageVO("401", "로그인이 필요합니다.");
		}
		param.setMemberId(loginMember.getMemberId());

		int flag = commentService.doUpdate(param);
		if (flag == 1) {
			return new MessageVO("200", "댓글 수정 성공", param);
		}
		return new MessageVO("400", "댓글 수정 실패(작성자 불일치 또는 데이터 없음)");
	}

	@PostMapping("/doDelete.do")
	@ResponseBody
	public MessageVO doDelete(CommentVO param, HttpSession session) {
		log.debug("doDelete param: " + param);
		MemberVO loginMember = getLoginMember(session);
		if (loginMember == null) {
			return new MessageVO("401", "로그인이 필요합니다.");
		}
		param.setMemberId(loginMember.getMemberId());

		int flag = commentService.doDelete(param);
		if (flag == 1) {
			return new MessageVO("200", "댓글 삭제 성공");
		}
		return new MessageVO("400", "댓글 삭제 실패(작성자 불일치 또는 데이터 없음)");
	}

	private MemberVO getLoginMember(HttpSession session) {
		if (session == null) {
			return null;
		}
		Object loginMember = session.getAttribute(SessionConst.LOGIN_MEMBER);
		return loginMember instanceof MemberVO ? (MemberVO) loginMember : null;
	}
}
