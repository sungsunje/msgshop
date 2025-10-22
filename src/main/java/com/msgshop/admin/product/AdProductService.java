package com.msgshop.admin.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.springframework.stereotype.Service;

import com.msgshop.common.utils.SearchCriteria;
import com.msgshop.admin.category.AdCategoryService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AdProductService {

    private final AdProductMapper adProductMapper;
    private final AdProductImgMapper adProductImgMapper; // 일반 이미지 Mapper
    private final AdCategoryService adCategoryService;   // 카테고리 서비스 주입

    // ---------------- 상품 관련 ----------------
    public void pro_insert(ProductVO vo) {
        adProductMapper.pro_insert(vo); 
        insertProductImages(vo);
    }

    public List<ProductVO> pro_list(SearchCriteria cri) {
        List<ProductVO> list = adProductMapper.pro_list(cri);
        for(ProductVO vo : list) {
            vo.setImgList(adProductImgMapper.getProductImages(vo.getPro_num()));
        }
        return list;
    }

    public int getTotalCount(SearchCriteria cri) {
        return adProductMapper.getTotalCount(cri);
    }

    // 단일 상품 삭제 (이미지 + 테마 포함)
    public void pro_delete(Integer pro_num) {
        if(pro_num == null) return;

        // 1️⃣ 이미지 삭제
        deleteProductImagesByProNum(pro_num);

        // 2️⃣ 테마 삭제
        deleteProductThemes(pro_num);

        // 3️⃣ 상품 삭제
        adProductMapper.pro_delete(pro_num);
    }

    // 선택 삭제 (배열)
    public void pro_sel_delete_2(int[] pro_num_arr) {
        if(pro_num_arr == null || pro_num_arr.length == 0) return;

        for(int pro_num : pro_num_arr) {
            pro_delete(pro_num); // 이미지+테마까지 포함 삭제
        }
    }

    // 선택 삭제 + 상품명 조건
    public void pro_sel_delete_3(int[] pro_num_arr, String pro_name) {
        if(pro_num_arr == null || pro_num_arr.length == 0 || pro_name == null) return;

        for(int pro_num : pro_num_arr) {
            ProductVO vo = adProductMapper.pro_edit_form(pro_num);
            if(vo != null && pro_name.equals(vo.getPro_name())) {
                pro_delete(pro_num); // 이미지+테마까지 포함 삭제
            }
        }
    }

    public ProductVO pro_edit_form(Integer pro_num) {
        ProductVO vo = adProductMapper.pro_edit_form(pro_num);

        // 기존 이미지 리스트 세팅
        vo.setImgList(adProductImgMapper.getProductImages(pro_num));

        // 기존 업로드 폴더, 대표 이미지가 null이면 DB에서 가져오기
        if (vo.getPro_up_folder() == null || vo.getPro_up_folder().isEmpty()) {
            vo.setPro_up_folder(adProductMapper.getProUpFolderByProNum(pro_num));
        }
        if (vo.getPro_img() == null || vo.getPro_img().isEmpty()) {
            vo.setPro_img(adProductMapper.getProImgByProNum(pro_num));
        }

        // ------------------ 2차/3차 카테고리 세팅 ------------------
        vo.setThird_cate_code(vo.getCate_code()); // 3차 카테고리 코드

        // 2차 코드 안전 처리
        Integer secondCode = null;
        if (vo.getCate_code() != null) {
            secondCode = adCategoryService.getParentCode(vo.getCate_code());
            if (secondCode == null) {
                System.out.println("Warning: Parent code not found for cate_code=" + vo.getCate_code());
            }
        }
        vo.setSecond_cate_code(secondCode);

        return vo;
    }

    public void pro_edit_ok(ProductVO vo) {
        // third_cate_code가 들어왔다면 cate_code에 세팅
        if (vo.getThird_cate_code() != null && vo.getThird_cate_code() != 0) {
            vo.setCate_code(vo.getThird_cate_code());
        }

        // 만약 아무것도 안 넘어왔으면 기존 값 유지
        if (vo.getCate_code() == null || vo.getCate_code() == 0) {
            ProductVO existing = adProductMapper.pro_edit_form(vo.getPro_num());
            vo.setCate_code(existing.getCate_code());
        }

        adProductMapper.pro_update(vo);

        // 이미지 처리 등 나머지 로직...
    }
    
    public void pro_update(ProductVO vo) {
        // third_cate_code가 있다면 cate_code로 세팅
        if (vo.getThird_cate_code() != null && vo.getThird_cate_code() != 0) {
            vo.setCate_code(vo.getThird_cate_code());
        }

        adProductMapper.pro_update(vo);
    }

    // ---------------- ProductImg 관련 ----------------
    public void insertProductImages(ProductVO vo) {
        if(vo.getImgList() == null || vo.getImgList().isEmpty()) return;
        for(ProductImgVO img : vo.getImgList()) {
            img.setPro_num(vo.getPro_num());
            adProductImgMapper.insertProductImg(img);
        }
    }

    public List<ProductImgVO> getProductImages(int pro_num) {
        return adProductImgMapper.getProductImages(pro_num);
    }

    public void deleteProductImagesByProNum(int pro_num) {
        adProductImgMapper.deleteProductImagesByProNum(pro_num);
    }

    // ---------------- 테마 관련 ----------------
    public List<ThemeVO> getThemeList() {
        return adProductMapper.getThemeList();
    }

    public List<Integer> getProductThemeCodes(int pro_num) {
        return adProductMapper.getProductThemeCodes(pro_num);
    }

    public List<String> getProductThemeNames(int pro_num) {
        return adProductMapper.getProductThemeNames(pro_num);
    }

    public void deleteProductThemes(int pro_num) {
        adProductMapper.deleteProductThemes(pro_num);
    }

    // ---------------- 상품 조회 ----------------
    public List<ProductVO> getProductListByCriteria(SearchCriteria cri,
                                                    List<Integer> themeList,
                                                    String period,
                                                    String start_date,
                                                    String end_date) {
        return adProductMapper.getProductListByCateAndTheme(cri, themeList, period, start_date, end_date);
    }

    public int getCountByCriteria(SearchCriteria cri,
                                  List<Integer> themeList,
                                  String period,
                                  String start_date,
                                  String end_date) {
        return adProductMapper.getCountProductListByCateAndTheme(cri, themeList, period, start_date, end_date);
    }
    
    // 상품 테마 삽입 (테마 코드 리스트)
    public void insertProductThemes(Integer pro_num, List<Integer> themeList) {
        if(themeList == null) themeList = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("pro_num", pro_num);
        map.put("themeList", themeList);
        adProductMapper.insertProductThemes(map);
    }

}
