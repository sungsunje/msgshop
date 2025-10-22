package com.msgshop.product;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.msgshop.admin.category.AdCategoryService;
import com.msgshop.admin.category.CategoryVO;
import com.msgshop.admin.product.ProductImgVO;
import com.msgshop.admin.product.ProductVO;
import com.msgshop.admin.product.ThemeVO;
import com.msgshop.common.utils.FileUtils;
import com.msgshop.common.utils.PageMaker;
import com.msgshop.common.utils.SearchCriteria;
import com.msgshop.review.ReviewVO;
import com.msgshop.admin.partner.stats.PartnerStatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/product/*")
@Controller
public class ProductController {

    private final ProductService productService;
    private final AdCategoryService adCategoryService;
    private final FileUtils fileUtils;
    private final PartnerStatsService partnerStatsService;

    @Value("${com.ezen.upload.path}")
    private String uploadPath;

    @Value("${com.ezen.upload.ckeditor.path}")
    private String uploadCKPath;

    // ================================
    // 상품 목록
    // ================================
    @GetMapping("/pro_list")
    public String pro_list(
            SearchCriteria cri,
            @RequestParam(value="cate_code", required=false) Integer cate_code,
            @RequestParam(value="theme_code", required=false) List<Integer> themeList,
            Model model) throws Exception {

        // 1차 카테고리 목록
        model.addAttribute("cate_list", adCategoryService.getFirstCategoryList());

        // -------------------------
        // 필터 조건에 따라 상품 조회
        // -------------------------
        List<ProductVO> pro_list = productService.getProductListByCateAndTheme(cri, cate_code, themeList);
        int totalCount = productService.getCountProductListByCateAndTheme(cate_code, themeList);

        for (ProductVO vo : pro_list) {
            vo.setPro_up_folder(vo.getPro_up_folder().replace("\\", File.separator));
            vo.setCate_name(adCategoryService.getCateNameByCode(vo.getCate_code()));

            // -------------------------
            // 상품별 테마 조회
            List<String> themes = productService.getThemesByProduct(vo.getPro_num()); // 변경
            vo.setThemeList(themes != null ? themes : new ArrayList<>());
        }

        model.addAttribute("pro_list", pro_list);

        // -------------------------
        // 상단 선택 박스용 데이터 추가
        // -------------------------
        if (cate_code != null) {
            List<CategoryVO> regionHierarchy = adCategoryService.getFullRegionHierarchy(cate_code);
            model.addAttribute("selectedRegions", regionHierarchy);
        }

        if (themeList != null && !themeList.isEmpty()) {
            List<ThemeVO> allThemes = adCategoryService.getThemeList();

            List<String> selectedThemeNames = themeList.stream()
                    .map(themeCode -> allThemes.stream()
                            .filter(t -> t.getTheme_code() == themeCode)
                            .map(ThemeVO::getTheme_name)
                            .findFirst()
                            .orElse(""))
                    .collect(Collectors.toList());

            model.addAttribute("selectedThemes", selectedThemeNames);
        }

        // -------------------------
        // 페이징
        // -------------------------
        PageMaker pageMaker = new PageMaker();
        pageMaker.setCri(cri);
        pageMaker.setTotalCount(totalCount);
        model.addAttribute("pageMaker", pageMaker);

        return "product/pro_list";
    }

    // ================================
    // 상품 이미지 출력
    // ================================
    @GetMapping("/image_display")
    public ResponseEntity<byte[]> image_display(String dateFolderName, String fileName) throws Exception {
        return fileUtils.getFile(uploadPath + File.separator + dateFolderName, fileName);
    }

	// ================================
	// 상품 상세 정보
	// ================================
    @GetMapping("/pro_info")
    public String pro_info(@RequestParam("pro_num") Integer pro_num,
                           @RequestParam(value = "cate_name", required = false) String cate_name,
                           Model model) throws Exception {
    	// 통계기록 호출 추가
    	partnerStatsService.addViewCount(pro_num);
    	
        // 1. 상품 조회
        ProductVO productVO = productService.pro_info(pro_num);
        if (productVO == null) {
            return "redirect:/product/pro_list";
        }

        // 2. 폴더 구분자 통일
        productVO.setPro_up_folder(productVO.getPro_up_folder().replace("\\", File.separator));

        // 3. 카테고리명 세팅
        productVO.setCate_name(adCategoryService.getCateNameByCode(productVO.getCate_code()));

        // 4. 이미지 조회 (null-safe)
        List<ProductImgVO> images = productVO.getImgList();
        if (images == null) images = List.of();

        // 5. 메인 이미지 결정
        String mainImage = images.stream()
                                 .filter(img -> !"Y".equals(img.getIs_thumb())) // 일반 이미지
                                 .findFirst()
                                 .map(ProductImgVO::getImg_name)
                                 .orElse(
                                     images.stream()
                                           .filter(img -> "Y".equals(img.getIs_thumb())) // 썸네일
                                           .findFirst()
                                           .map(ProductImgVO::getImg_name)
                                           .orElse(productVO.getPro_img()) // 없으면 대표 이미지
                                 );

        // 6. 테마 조회
        List<String> themes = productVO.getThemeList();
        if (themes == null) themes = List.of();

        // 7. 주소 관련 필드 안전하게 null 처리
        String zipcode = productVO.getPro_zipcode() != null ? productVO.getPro_zipcode() : "";
        String addr = productVO.getPro_addr() != null ? productVO.getPro_addr() : "";
        String deaddr = productVO.getPro_deaddr() != null ? productVO.getPro_deaddr() : "";

        // 8. 후기 + 답변 리스트 가져오기 (기존 getReviewsByProduct -> getReviewsWithReplies)
        List<ReviewVO> reviewList = productService.getReviewsWithReplies(pro_num);
        productVO.setReviewList(reviewList != null ? reviewList : new ArrayList<>());

        // 9. 모델에 담기
        model.addAttribute("productVo", productVO);
        model.addAttribute("images", images);
        model.addAttribute("cate_name", cate_name);
        model.addAttribute("mainImage", mainImage);
        model.addAttribute("zipcode", zipcode);
        model.addAttribute("addr", addr);
        model.addAttribute("deaddr", deaddr);

        return "product/pro_info";
    }

    // ================================
    // 3차 카테고리 조회 (AJAX)
    // ================================
    @GetMapping(value = "/thirdcategory/{secondCateCode}", produces = "application/json")
    @ResponseBody
    public List<CategoryVO> getThirdCategory(@PathVariable("secondCateCode") Integer secondCateCode) {
        log.info("3차 카테고리 조회 요청: 2차 코드={}", secondCateCode);
        return adCategoryService.getThirdCategoryList(secondCateCode);
    }

    // ================================
    // Ajax 필터링 (2차, 3차, 테마별)
    // ================================
    @PostMapping(value = "/filter", produces = "application/json")
    @ResponseBody
    public List<ProductVO> filterProducts(
            @RequestParam(value = "secondCate", required = false) Integer secondCate,
            @RequestParam(value = "thirdCate", required = false) Integer thirdCate,
            @RequestParam(value = "themeList[]", required = false) List<Integer> themeList) {

        log.info("상품 필터링 요청 - 2차: {}, 3차: {}, 테마: {}", secondCate, thirdCate, themeList);

        List<ProductVO> filteredList = productService.getProductListByFilter(secondCate, thirdCate, themeList);

        for (ProductVO vo : filteredList) {
            vo.setPro_up_folder(vo.getPro_up_folder().replace("\\", File.separator));
            vo.setCate_name(adCategoryService.getCateNameByCode(vo.getCate_code()));

            // -------------------------
            // 상품별 테마 조회
            List<String> themes = productService.getThemesByProduct(vo.getPro_num());
            vo.setThemeList(themes != null ? themes : new ArrayList<>());
        }

        return filteredList;
    }
}
