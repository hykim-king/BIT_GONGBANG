package com.pcwk.ehr.main.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.pcwk.ehr.artwork.domain.ArtworkVO;
import com.pcwk.ehr.main.service.MainService;

@Controller
@RequestMapping("/main")
public class MainController {

    @Autowired
    private MainService mainService;

    @GetMapping({ "/", "/index" })
    public String index(Model model) {
        Map<String, Object> mainData = mainService.getMain();
        model.addAttribute("recommendList", mainData.get("recommendList"));
        model.addAttribute("popularList", mainData.get("popularList"));
        model.addAttribute("latestList", mainData.get("latestList"));
        return "main/index";
    }

    @GetMapping("/recommend")
    @ResponseBody
    public List<ArtworkVO> recommend() {
        return mainService.getRecommend();
    }

    @GetMapping("/popular")
    @ResponseBody
    public List<ArtworkVO> popular() {
        return mainService.getPopular();
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String searchDiv,
            @RequestParam(required = false) String searchWord,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "12") int pageSize,
            Model model) {

        ArtworkVO vo = new ArtworkVO();
        vo.setSearchDiv(searchDiv);
        vo.setSearchWord(searchWord);
        vo.setPageNo(pageNo);
        vo.setPageSize(pageSize);

        mainService.doRetrieve(vo);
        model.addAttribute("searchVO", vo);
        model.addAttribute("searchList", mainService.searchList(vo));
        return "main/search_result";
    }
}