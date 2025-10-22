package com.msgshop.product;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.msgshop.admin.product.ProductImgVO;
import com.msgshop.admin.product.ProductVO;
import com.msgshop.common.utils.SearchCriteria;
import com.msgshop.review.ReviewVO;

public interface ProductMapper {

    // ---------------- 2차 카테고리 상품 목록 ----------------
    List<ProductVO> getProductListBysecondCategory(
        @Param("cri") SearchCriteria cri, 
        @Param("cate_code") Integer second_cate_code
    );
    
    int getCountProductListBysecondCategory(@Param("cate_code") Integer second_cate_code);
    
    // ---------------- 상품 상세 정보 ----------------
    // ※ 쿼리에서 pro_zipcode, pro_addr, pro_deaddr 포함 필요
    ProductVO pro_info(Integer pro_num);
    
    // ---------------- 리뷰 관련 ----------------
    void review_count(Integer pro_num);
    
    int review_count_pro_info(Integer pro_num);
    
    // ---------------- 필터링 ----------------
    List<ProductVO> selectProductsByFilter(
        @Param("secondCate") Integer secondCate,
        @Param("thirdCate") Integer thirdCate,
        @Param("themeList") List<Integer> themeList
    );

    // ---------------- 지역 + 테마 필터 상품 조회 ----------------
    List<ProductVO> getProductListByCateAndTheme(HashMap<String, Object> map);

    int getCountProductListByCateAndTheme(HashMap<String, Object> map);
    
    // ---------------- 상품별 이미지 조회 ----------------
    List<ProductImgVO> getProductImages(Integer pro_num);
    
    // 후기 + 답변 조회
    List<ReviewVO> getReviewsWithReplies(@Param("proNum") int proNum);

    // 후기 답변 등록
    void insertReviewReply(@Param("rev_code") Long revCode,
                           @Param("manager_id") String managerId,
                           @Param("reply_text") String replyText);
    
    List<ProductVO> searchByKeyword(@Param("keyword") String keyword);
    List<ProductVO> getAllProducts(); // 키워드 없을 때
    
    // 카테고리 이름 조회
    String getCategoryName(Integer cate_code);

    // 특정 상품의 테마 이름 리스트 조회
    List<String> getThemeNamesByProduct(Integer pro_num);

}
