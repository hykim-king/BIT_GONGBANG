package com.pcwk.ehr.main.controller;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.cmn.MessageVO;
import com.pcwk.ehr.main.service.MainService;

@Controller
@RequestMapping("/main")
public class MainController {

	private static final Logger log = LogManager.getLogger(MainController.class);

	@Autowired
	private MainService mainService;

	public MainController() {
		log.debug("MainController");
	}

	@GetMapping("/index.do")
	public String index(Model model) {
		log.debug("index");
		Map<String, Object> mainData = mainService.getMain();
		model.addAttribute("recommendList", mainData.get("recommendList"));
		model.addAttribute("popularList", mainData.get("popularList"));
		model.addAttribute("latestList", mainData.get("latestList"));
		return "main/index";
	}

	@PostMapping("/recommend.do")
	@ResponseBody
	public MessageVO recommend() {
		log.debug("recommend");
		return new MessageVO("200", "조회 성공", mainService.getRecommend());
	}

	@PostMapping("/popular.do")
	@ResponseBody
	public MessageVO popular() {
		log.debug("popular");
		return new MessageVO("200", "조회 성공", mainService.getPopular());
	}

	@GetMapping("/search.do")
	public String search(
			@RequestParam(required = false) String searchDiv,
			@RequestParam(required = false) String searchWord,
			@RequestParam(defaultValue = "1") int pageNo,
			@RequestParam(defaultValue = "12") int pageSize,
			Model model) {

		log.debug("search searchDiv={} searchWord={} pageNo={}", searchDiv, searchWord, pageNo);

		ArtworkVO vo = new ArtworkVO();
		vo.setSearchDiv(searchDiv);
		vo.setSearchWord(searchWord);
		vo.setPageNo(pageNo);
		vo.setPageSize(pageSize);

		mainService.prepareSearch(vo);
		model.addAttribute("searchVO", vo);
		model.addAttribute("searchList", mainService.searchList(vo));
		return "main/search_result";
	}

}
