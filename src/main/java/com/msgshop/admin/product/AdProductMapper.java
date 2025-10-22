package com.msgshop.admin.product;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.msgshop.common.utils.SearchCriteria;

public interface AdProductMapper {

    // =================== 상품 관련 ===================
    // ProductVO 안에 proZipcode, proAddr, proDeaddr, proExtraAddr 포함
    void pro_insert(ProductVO vo);
    List<ProductVO> pro_list(SearchCriteria cri);
    int getTotalCount(SearchCriteria cri);
    void pro_sel_delete_2(int[] pro_num_arr);
    void pro_sel_delete_3(HashMap<String, Object> map);
    ProductVO pro_edit_form(Integer pro_num);
    void pro_edit_ok(ProductVO vo);
    void pro_delete(Integer pro_num);

    // =================== 테마 관련 ===================
    List<ThemeVO> getThemeList();
    void insertProductThemes(java.util.HashMap<String, Object> map);
    void insertProductThemesByNames(java.util.HashMap<String, Object> map);
    List<Integer> getProductThemeCodes(int pro_num);
    List<String> getProductThemeNames(int pro_num);
    void deleteProductThemes(int pro_num);

    // =================== 일반 이미지 관련 ===================
    void insertProductImg(ProductImgVO img);
    List<ProductImgVO> getProductImages(int pro_num);
    void deleteProductImagesByProNum(int pro_num);

    // =================== 기존 이미지/업로드 폴더 조회 ===================
    String getProUpFolderByProNum(Integer pro_num);  // 기존 업로드 폴더 조회
    String getProImgByProNum(Integer pro_num);       // 기존 대표 이미지 조회

    // =================== 관리자용: 검색 + 테마 + 날짜 필터 ===================
    List<ProductVO> getProductListByCateAndTheme(
            @Param("cri") SearchCriteria cri,
            @Param("themeList") List<Integer> themeList,
            @Param("period") String period,
            @Param("start_date") String start_date,
            @Param("end_date") String end_date
    );

    int getCountProductListByCateAndTheme(
            @Param("cri") SearchCriteria cri,
            @Param("themeList") List<Integer> themeList,
            @Param("period") String period,
            @Param("start_date") String start_date,
            @Param("end_date") String end_date
    );
    
    void pro_update(ProductVO vo);

}
