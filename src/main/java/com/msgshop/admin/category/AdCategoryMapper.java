package com.msgshop.admin.category;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.msgshop.admin.product.ThemeVO;

@Mapper
public interface AdCategoryMapper {

    // 1차 카테고리 목록
    List<CategoryVO> getFirstCategoryList();

    // 2차 카테고리 목록
    List<CategoryVO> getSecondCategoryList(@Param("cate_prt_code") Integer cate_prt_code);

    // 3차 카테고리 목록
    List<CategoryVO> getThirdCategoryList(@Param("cate_prt_code") Integer cate_prt_code);

    // 상품수정 폼에서 사용할 선택한 1차카테고리 정보
    CategoryVO getFirstCategoryBySecondCategory(@Param("secondCategory") int secondCategory);

    // 카테고리 코드로 cate_name 조회
    String getCateNameByCode(@Param("cateCode") Integer cateCode);

    // -----------------------------
    // 테마 목록 조회
    List<ThemeVO> getThemeList();
    
    CategoryVO getCategoryByCode(@Param("cate_code") Integer cate_code);
    
    // 3차 카테고리 코드로 2차 카테고리 코드 조회
    Integer getParentCodeByThirdCode(Integer thirdCateCode);

}
