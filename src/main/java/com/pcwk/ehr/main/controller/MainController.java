package com.pcwk.ehr.main.controller;

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

	/** 메인 홈(CC-MAIN-01): 가중치+최근30일 단일 인기 피드 첫 페이지 */
	@GetMapping("/index.do")
	public String index(Model model) {
		log.debug("index");
		model.addAttribute("list", mainService.getHomeFeed(1, 12));
		return "main/index";
	}

	/** 메인 홈 무한스크롤 더보기(CC-MAIN-01 이벤트 2) */
	@PostMapping("/popular.do")
	@ResponseBody
	public MessageVO popular(
			@RequestParam(defaultValue = "1") int pageNo,
			@RequestParam(defaultValue = "12") int pageSize) {
		log.debug("popular pageNo={} pageSize={}", pageNo, pageSize);
		return new MessageVO("200", "조회 성공", mainService.getHomeFeed(pageNo, Math.min(pageSize, 48)));
	}

	/** 명예의전당 진입(CC-MAIN-02, 신설) */
	@GetMapping("/hall.do")
	public String hall(Model model) {
		log.debug("hall");
		model.addAttribute("list", mainService.getHallFeed(1, 12));
		return "main/hall";
	}

	/** 명예의전당 무한스크롤 더보기(CC-MAIN-02 이벤트 2, 신설) */
	@PostMapping("/hallMore.do")
	@ResponseBody
	public MessageVO hallMore(
			@RequestParam(defaultValue = "1") int pageNo,
			@RequestParam(defaultValue = "12") int pageSize) {
		log.debug("hallMore pageNo={} pageSize={}", pageNo, pageSize);
		return new MessageVO("200", "조회 성공", mainService.getHallFeed(pageNo, Math.min(pageSize, 48)));
	}

	/** (구) 추천 API — 화면 사용처 없음. 하위호환으로 명예의전당 첫 페이지를 반환 */
	@PostMapping("/recommend.do")
	@ResponseBody
	public MessageVO recommend() {
		log.debug("recommend");
		return new MessageVO("200", "조회 성공", mainService.getHallFeed(1, 8));
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
