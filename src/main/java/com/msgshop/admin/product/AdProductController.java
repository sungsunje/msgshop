package com.msgshop.admin.product;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.msgshop.admin.category.AdCategoryService;
import com.msgshop.admin.category.CategoryVO;
import com.msgshop.common.utils.FileUtils;
import com.msgshop.common.utils.PageMaker;
import com.msgshop.common.utils.SearchCriteria;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/product/*")
public class AdProductController {

    private final AdProductService adProductService;
    private final AdCategoryService adCategoryService;
    private final FileUtils fileUtils;

    @Value("${com.ezen.upload.path}")
    private String uploadPath;

    @Value("${com.ezen.upload.ckeditor.path}")
    private String uploadCKPath;

    // =================== 상품등록 폼 ===================
    @GetMapping("/pro_insert")
    public void pro_insert(Model model) {
        model.addAttribute("cate_list", adCategoryService.getFirstCategoryList());
        model.addAttribute("themeList", adProductService.getThemeList());
        model.addAttribute("productVO", new ProductVO());
    }

    // =================== 상품 등록 ===================
    @PostMapping("/pro_insert")
    public String pro_insert(ProductVO vo,
                             @RequestParam(value="pro_thumbnail_upload", required=false) MultipartFile pro_thumbnail_upload,
                             @RequestParam(value="pro_img_uploads", required=false) List<MultipartFile> pro_img_uploads,
                             @RequestParam(value="themeCodes", required=false) List<String> themeCodes) throws Exception {

        // 1️⃣ 썸네일 업로드
        if(pro_thumbnail_upload != null && !pro_thumbnail_upload.isEmpty()) {
            String dateFolder = fileUtils.getDateFolder();
            String saveFileName = fileUtils.uploadFile(uploadPath, dateFolder, pro_thumbnail_upload);
            vo.setPro_img(saveFileName);
            vo.setPro_up_folder(dateFolder);
        }

        // 2️⃣ 일반 이미지 업로드
        if(pro_img_uploads != null && !pro_img_uploads.isEmpty()) {
            List<ProductImgVO> imgList = new ArrayList<>();
            String dateFolder = fileUtils.getDateFolder();
            for(int i=0; i<pro_img_uploads.size(); i++) {
                MultipartFile file = pro_img_uploads.get(i);
                if(file.isEmpty()) continue;

                String saveFileName = fileUtils.uploadFile(uploadPath, dateFolder, file);

                ProductImgVO imgVO = new ProductImgVO();
                imgVO.setImg_name(saveFileName);
                imgVO.setImg_folder(dateFolder);
                imgVO.setIs_thumb("N");
                imgVO.setSort_order(i);
                imgList.add(imgVO);
            }
            vo.setImgList(imgList);
        }

        // 3️⃣ 카테고리명 세팅
        vo.setCate_name(adCategoryService.getCateNameByCode(vo.getCate_code()));

        // 4️⃣ DB 저장 (상품 + 이미지)
        adProductService.pro_insert(vo);

        // 5️⃣ 선택된 테마 저장
        if(themeCodes != null && !themeCodes.isEmpty()) {
            List<Integer> themeCodeList = themeCodes.stream().map(Integer::parseInt).collect(Collectors.toList());
            adProductService.insertProductThemes(vo.getPro_num(), themeCodeList);
        }

        return "redirect:/admin/product/pro_list";
    }

    // =================== 상품 목록 ===================
    @GetMapping("/pro_list")
    public String pro_list(@ModelAttribute("cri") SearchCriteria cri,
    					   @RequestParam(value="themeCode", required=false) String themeCodeStr,
                           @RequestParam(value="period", required=false) String period,
                           @RequestParam(value="start_date", required=false) String start_date,
                           @RequestParam(value="end_date", required=false) String end_date,
                           Model model) throws Exception {

        // themeCode 처리
        Integer themeCode = null;
        if(themeCodeStr != null && !themeCodeStr.isEmpty()) {
            try {
                themeCode = Integer.valueOf(themeCodeStr);
            } catch(NumberFormatException e) {
                log.warn("themeCode 변환 실패: {}", themeCodeStr);
            }
        }

        // 1️⃣ 테마 리스트를 모델에 담음
        List<ThemeVO> themeList = adProductService.getThemeList();
        model.addAttribute("themeList", themeList);
        model.addAttribute("selectedThemeCode", themeCode);

       
        // 2️⃣ 상품 목록 조회 (검색어 + 테마 + 기간 필터)
        List<ProductVO> pro_list = adProductService.getProductListByCriteria(
            cri,
            themeCode != null ? List.of(themeCode) : null,
            period,
            start_date,
            end_date
        );

        // 3️⃣ 전체 상품 수 조회 (페이징용)
        int totalCount = adProductService.getCountByCriteria(
            cri,
            themeCode != null ? List.of(themeCode) : null,
            period,
            start_date,
            end_date
        );

        model.addAttribute("pro_list", pro_list);

        // 4️⃣ 페이지메이커 세팅
        PageMaker pageMaker = new PageMaker();
        pageMaker.setCri(cri);
        pageMaker.setTotalCount(totalCount);
        model.addAttribute("pageMaker", pageMaker);

        // 5️⃣ 검색조건 유지 (뷰에서 필터값 다시 세팅할 수 있게)
        model.addAttribute("searchType", cri.getSearchType());
        model.addAttribute("keyword", cri.getKeyword());
        model.addAttribute("period", period);
        model.addAttribute("start_date", start_date);
        model.addAttribute("end_date", end_date);

        return "admin/product/pro_list";
    }

    // =================== 상품 수정 폼 ===================
    @GetMapping("/pro_edit")
    public void pro_edit(@ModelAttribute("cri") SearchCriteria cri, Integer pro_num, Model model) throws Exception {

        // 1️⃣ 1차 카테고리 목록
        model.addAttribute("cate_list", adCategoryService.getFirstCategoryList());

        // 2️⃣ ProductVO 가져오기
        ProductVO productVO = adProductService.pro_edit_form(pro_num);
        if(productVO.getPro_up_folder() != null) {
            productVO.setPro_up_folder(productVO.getPro_up_folder().replace("\\", File.separator));
        }

        // 3️⃣ 3차/2차 카테고리 세팅
        productVO.setThird_cate_code(productVO.getCate_code());
        Integer secondCode = null;
        if(productVO.getCate_code() != null) {
            secondCode = adCategoryService.getParentCode(productVO.getCate_code());
        }
        productVO.setSecond_cate_code(secondCode);

        // 4️⃣ 카테고리 이름 세팅
        String cateName = null;
        if(productVO.getCate_code() != null) {
            cateName = adCategoryService.getCateNameByCode(productVO.getCate_code());
        }
        productVO.setCate_name(cateName);

        // 5️⃣ 테마 정보 세팅
        productVO.setThemeCodes(adProductService.getProductThemeCodes(pro_num));
        productVO.setThemeNames(adProductService.getProductThemeNames(pro_num));

        // 6️⃣ 모델에 세팅
        model.addAttribute("product", productVO);
        model.addAttribute("themeList", adProductService.getThemeList());

        // 7️⃣ 카테고리 선택용
        CategoryVO categoryVO = null;
        Integer parentCode = null;
        if(productVO.getCate_code() != null) {
            categoryVO = adCategoryService.getFirstCategoryBySecondCategory(productVO.getCate_code());
            if(categoryVO != null) {
                parentCode = categoryVO.getCate_prtcode();
            }
        }
        model.addAttribute("categoryVO", categoryVO);
        model.addAttribute("secondCategoryVO", parentCode != null
                ? adCategoryService.getSecondCategoryList(parentCode)
                : new ArrayList<>()); // null이면 빈 리스트 반환
    }
    
    @PostMapping("/pro_edit_ok")
    public String pro_edit_ok(ProductVO vo,
                              @RequestParam(value="pro_thumbnail_upload", required=false) MultipartFile pro_thumbnail_upload,
                              @RequestParam(value="pro_img_uploads", required=false) List<MultipartFile> pro_img_uploads,
                              @RequestParam(value="themeCodes", required=false) List<String> themeCodes) throws Exception {

        // 썸네일/이미지 업로드 처리
    	// 1️⃣ 이미지 업로드 처리
    	if(pro_thumbnail_upload != null && !pro_thumbnail_upload.isEmpty()) {
    	    String dateFolder = fileUtils.getDateFolder();
    	    String saveFileName = fileUtils.uploadFile(uploadPath, dateFolder, pro_thumbnail_upload);
    	    vo.setPro_img(saveFileName);
    	    vo.setPro_up_folder(dateFolder);
    	}

    	if(pro_img_uploads != null && !pro_img_uploads.isEmpty()) {
    	    List<ProductImgVO> imgList = new ArrayList<>();
    	    String dateFolder = fileUtils.getDateFolder();
    	    for(int i=0; i<pro_img_uploads.size(); i++) {
    	        MultipartFile file = pro_img_uploads.get(i);
    	        if(file.isEmpty()) continue;

    	        String saveFileName = fileUtils.uploadFile(uploadPath, dateFolder, file);

    	        ProductImgVO imgVO = new ProductImgVO();
    	        imgVO.setImg_name(saveFileName);
    	        imgVO.setImg_folder(dateFolder);
    	        imgVO.setIs_thumb("N");
    	        imgVO.setSort_order(i);
    	        imgList.add(imgVO);
    	    }
    	    vo.setImgList(imgList);
    	}

    	// 2️⃣ DB 수정 호출
    	adProductService.pro_edit_ok(vo);

    	// themeCodes 업데이트
    	if(themeCodes != null && !themeCodes.isEmpty()) {
    	    List<Integer> themeCodeList = themeCodes.stream()
    	                                            .map(Integer::parseInt)
    	                                            .collect(Collectors.toList());
    	    // 기존 테마 삭제 후 재삽입
    	    adProductService.deleteProductThemes(vo.getPro_num());
    	    adProductService.insertProductThemes(vo.getPro_num(), themeCodeList);
    	}

        return "redirect:/admin/product/pro_list";
    }
    
    @GetMapping("/pro_delete")
    public String pro_delete(SearchCriteria cri, Integer pro_num, RedirectAttributes rttr) throws Exception {
        if(pro_num != null) {
            adProductService.pro_delete(pro_num);
        }

        // 검색/페이징 값 유지
        rttr.addAttribute("page", cri.getPage());
        rttr.addAttribute("perPageNum", cri.getPerPageNum());
        rttr.addAttribute("searchType", cri.getSearchType());
        rttr.addAttribute("keyword", cri.getKeyword());

        return "redirect:/admin/product/pro_list";
    }

    @PostMapping("/pro_sel_delete")
    public String pro_sel_delete(@RequestParam("pro_num_arr") int[] pro_num_arr,
                                 @RequestParam(value="pro_name", required=false) String pro_name,
                                 SearchCriteria cri,
                                 RedirectAttributes rttr) throws Exception {

        if(pro_name != null && !pro_name.isEmpty()) {
            adProductService.pro_sel_delete_3(pro_num_arr, pro_name);
        } else {
            adProductService.pro_sel_delete_2(pro_num_arr);
        }

        // 검색/페이징 값 유지
        rttr.addAttribute("page", cri.getPage());
        rttr.addAttribute("perPageNum", cri.getPerPageNum());
        rttr.addAttribute("searchType", cri.getSearchType());
        rttr.addAttribute("keyword", cri.getKeyword());

        return "redirect:/admin/product/pro_list";
    }
    
 // 상품목록 이미지출력하기.. 클라이언트에서 보낸 파라미터명 스프링의 컨트롤러에서 받는 파라미터명이 일치해야 한다.
 	@GetMapping("/image_display")
 	public ResponseEntity<byte[]> image_display(String dateFolderName, String fileName) throws Exception {
 		 
 		log.info("이미지");
 		
 		return fileUtils.getFile(uploadPath + File.separator + dateFolderName, fileName);
 	}

    // =================== 2차/3차 카테고리 JSON ===================
    @GetMapping("/category/secondcategory/{firstCode}")
    @ResponseBody
    public List<CategoryVO> getSecondCategory(@PathVariable int firstCode) {
        return adCategoryService.getSecondCategoryList(firstCode);
    }

    @GetMapping("/category/thirdcategory/{secondCode}")
    @ResponseBody
    public List<CategoryVO> getThirdCategory(@PathVariable int secondCode) {
        return adCategoryService.getThirdCategoryList(secondCode);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }

}
