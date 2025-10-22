package com.msgshop;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.msgshop.admin.category.AdCategoryService;
import com.msgshop.admin.category.CategoryVO;
import com.msgshop.admin.product.ProductVO;
import com.msgshop.admin.product.ThemeVO;
import com.msgshop.product.ProductService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final AdCategoryService adCategoryService;

    // 기본주소 - 메인페이지
    @GetMapping("/")
    public String home(Model model) {

        // 1차 카테고리 전체 목록
        List<CategoryVO> firstList = adCategoryService.getFirstCategoryList();
        model.addAttribute("cate_list", firstList);

        // '지역별' 1차 카테고리 코드 가져오기
        Integer regionCateCode = firstList.stream()
                .filter(c -> "지역별".equals(c.getCate_name()))
                .findFirst()
                .map(CategoryVO::getCate_code)
                .orElse(null);

        // 2차 카테고리(지역별) 데이터 모델에 추가
        if(regionCateCode != null) {
            List<CategoryVO> secondList = adCategoryService.getSecondCategoryList(regionCateCode);
            model.addAttribute("pro_list", secondList);   // 2차 카테고리
            model.addAttribute("detail_list", List.of()); // 3차 카테고리는 처음에 비워두고 Ajax로 로드
        }
        
        List<ThemeVO> themeList = adCategoryService.getThemeList(); // Service에서 테마 조회 메소드 필요
        model.addAttribute("theme_list", themeList);   // 테마 카테고리

        // ⭐ 메인페이지에서만 categorymenu 보여주기
        model.addAttribute("showCategoryMenu", true);

        return "index"; // index.html
    }
    
    private final ProductService productService; // 반드시 final로 선언

    @GetMapping("/search")
    public String search(@RequestParam String keyword, Model model) {
        List<ProductVO> products = productService.searchProducts(keyword);
        model.addAttribute("pro_list", products);  // 템플릿에서 th:each로 사용
        model.addAttribute("selectedRegions", List.of()); // 검색에서 선택된 지역 없으면 빈 리스트
        model.addAttribute("selectedThemes", List.of());  // 검색에서 선택된 테마 없으면 빈 리스트
        return "product/pro_list"; // templates/product/pro_list.html 사용
    }
    
}
