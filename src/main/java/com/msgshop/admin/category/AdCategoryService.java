package com.msgshop.admin.category;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.msgshop.admin.product.ThemeVO;
import com.msgshop.product.theme.ProductThemeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdCategoryService {

    private final AdCategoryMapper adCategoryMapper;
    private final ProductThemeMapper productThemeMapper; // Mapper 주입 추가

    // 1차 카테고리 전체 조회
    public List<CategoryVO> getFirstCategoryList() {
        return adCategoryMapper.getFirstCategoryList();
    }

    // 2차 카테고리 조회 (1차 기준)
    public List<CategoryVO> getSecondCategoryList(Integer firstCateCode) {
        return adCategoryMapper.getSecondCategoryList(firstCateCode);
    }

    // 3차 카테고리 조회 (2차 기준)
    public List<CategoryVO> getThirdCategoryList(Integer secondCateCode) {
        return adCategoryMapper.getThirdCategoryList(secondCateCode);
    }

    // cate_code 기준으로 cate_name 조회
    public String getCateNameByCode(Integer cateCode) {
        return adCategoryMapper.getCateNameByCode(cateCode);
    }

    // 2차 카테고리 코드로 1차 카테고리 정보 조회
    public CategoryVO getFirstCategoryBySecondCategory(Integer secondCateCode) {
        return adCategoryMapper.getFirstCategoryBySecondCategory(secondCateCode);
    }

    // 테마 전체 조회
    public List<ThemeVO> getThemeList() {
        return adCategoryMapper.getThemeList();
    }

    // -----------------------------
    // cate_code로 전체 지역 계층 구조 조회
    public List<CategoryVO> getFullRegionHierarchy(Integer cate_code) {
        List<CategoryVO> hierarchy = new ArrayList<>();
        
        // 현재 카테고리 정보 가져오기
        CategoryVO current = adCategoryMapper.getCategoryByCode(cate_code);
        if(current == null) return hierarchy;

        // 부모가 있으면 재귀적으로 추가
        if(current.getCate_prtcode() != null) {
            hierarchy.addAll(getFullRegionHierarchy(current.getCate_prtcode()));
        }
        
        // 1차 카테고리 "지역별"은 제외하고 추가
        if(!"지역별".equals(current.getCate_name())) {
            hierarchy.add(current);
        }

        return hierarchy;
    }
    
    // -----------------------------
    // 상품별 테마 조회
    public List<String> getThemeListByProduct(Integer proNum) {
        return productThemeMapper.selectThemesByProduct(proNum); // 인스턴스 Mapper 사용
    }

    // ================================
    // 🔹 3차 카테고리 코드로 2차 카테고리 코드 조회 🔹
    public Integer getParentCode(Integer thirdCateCode) {
        return adCategoryMapper.getParentCodeByThirdCode(thirdCateCode);
    }
}
