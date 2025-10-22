package com.msgshop.product.theme;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProductThemeMapper {

    // 특정 상품이 가진 테마 조회
    List<String> selectThemesByProduct(@Param("proNum") int proNum);

    // 모든 테마 조회
    List<String> selectAllThemes();

    // 상품-테마 연결 추가
    int insertProductTheme(@Param("proNum") int proNum, @Param("themeCode") int themeCode);

    // 상품-테마 연결 삭제
    int deleteProductTheme(@Param("proNum") int proNum, @Param("themeCode") int themeCode);
}
