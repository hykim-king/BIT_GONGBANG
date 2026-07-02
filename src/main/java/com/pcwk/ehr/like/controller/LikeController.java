package com.pcwk.ehr.like.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pcwk.ehr.cmn.MessageVO;
import com.pcwk.ehr.like.domain.LikeVO;
import com.pcwk.ehr.like.service.LikeService;

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
	public MessageVO toggle(LikeVO param) {
		log.debug("toggle param: " + param);
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
	public MessageVO myLikes(LikeVO param) {
		log.debug("myLikes param: " + param);
		return new MessageVO("200", "조회 성공", likeService.listByMember(param));
	}
}

