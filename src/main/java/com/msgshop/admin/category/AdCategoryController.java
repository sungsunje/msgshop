package com.msgshop.admin.category;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.msgshop.admin.product.ThemeVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/admin/category")
@RequiredArgsConstructor
@Slf4j
@Controller
public class AdCategoryController {

    private final AdCategoryService adCategoryService;

    // 1차 카테고리 전체 조회
    @GetMapping("/firstcategory")
    public ResponseEntity<List<CategoryVO>> getFirstCategoryList() {
        List<CategoryVO> list = adCategoryService.getFirstCategoryList();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // 2차 카테고리 조회
    @GetMapping("/secondcategory/{cate_prt_code}")
    public ResponseEntity<List<CategoryVO>> getSecondCategoryList(@PathVariable("cate_prt_code") Integer cate_prt_code) {
        log.info("1차카테고리코드: " + cate_prt_code);
        return new ResponseEntity<>(adCategoryService.getSecondCategoryList(cate_prt_code), HttpStatus.OK);
    }

    // 3차 카테고리 조회 (Ajax용)
    @GetMapping("/thirdcategory/{second_cate_code}")
    public ResponseEntity<List<CategoryVO>> getThirdCategoryList(@PathVariable("second_cate_code") Integer second_cate_code) {
        log.info("2차카테고리코드: " + second_cate_code);
        return new ResponseEntity<>(adCategoryService.getThirdCategoryList(second_cate_code), HttpStatus.OK);
    }

    // 메인페이지용 fragment
    @GetMapping("/main/category")
    public String getMainCategoryFragment(Model model) {
        // 1차 카테고리
        List<CategoryVO> firstList = adCategoryService.getFirstCategoryList();
        Integer regionCateCode = firstList.get(0).getCate_code(); // 예: '지역별'
        List<CategoryVO> secondList = adCategoryService.getSecondCategoryList(regionCateCode);

        // 테마 리스트 DB 조회
        List<ThemeVO> themeList = adCategoryService.getThemeList(); // Service에서 테마 조회 메소드 필요

        model.addAttribute("pro_list", secondList);    // 2차 카테고리
        model.addAttribute("theme_list", themeList);   // 테마 카테고리
        model.addAttribute("detail_list", List.of()); // 3차 카테고리는 처음에 비워두고 Ajax 로드

        return "fragments/category :: category"; // fragments/category.html의 th:fragment
    }
}
