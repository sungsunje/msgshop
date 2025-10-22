package com.msgshop.product;

import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.msgshop.admin.product.ProductVO;
import com.msgshop.admin.product.ProductImgVO;
import com.msgshop.common.utils.SearchCriteria;
import com.msgshop.product.theme.ProductThemeMapper;
import com.msgshop.review.ReviewVO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductThemeMapper productThemeMapper;

    // ---------------- 2차 카테고리 상품 목록 ----------------
    public List<ProductVO> getProductListBysecondCategory(SearchCriteria cri, Integer second_cate_code) {
        return productMapper.getProductListBysecondCategory(cri, second_cate_code);
    }

    public int getCountProductListBysecondCategory(Integer second_cate_code) {
        return productMapper.getCountProductListBysecondCategory(second_cate_code);
    }

	 // ---------------- 상품 상세 조회 ----------------
	    public ProductVO pro_info(Integer pro_num) {
	        ProductVO vo = productMapper.pro_info(pro_num);
	
	        if (vo != null) {
	            // 이미지 VO 세팅 (Mapper에 메서드 없으면 빈 리스트 처리)
	            try {
	                List<ProductImgVO> imgList = productMapper.getProductImages(pro_num);
	                vo.setImgList(imgList != null ? imgList : List.of());
	            } catch (NoSuchMethodError | AbstractMethodError e) {
	                vo.setImgList(List.of());
	            }
	
	            // 테마 세팅
	            List<String> themes = productThemeMapper.selectThemesByProduct(pro_num);
	            vo.setThemeList(themes != null ? themes : List.of());
	
	            // ================================
	            // 주소 null-safe 처리
	            // ================================
	            if (vo.getPro_zipcode() == null) vo.setPro_zipcode("");
	            if (vo.getPro_addr() == null) vo.setPro_addr("");
	            if (vo.getPro_deaddr() == null) vo.setPro_deaddr("");
	        }
	
	        return vo;
	    }

    // ---------------- 리뷰 관련 ----------------
    public int review_count_pro_info(Integer pro_num) {
        return productMapper.review_count_pro_info(pro_num);
    }

    // ---------------- 필터링 ----------------
    public List<ProductVO> getProductListByFilter(Integer secondCate, Integer thirdCate, List<Integer> themeList) {
        if (themeList != null && themeList.isEmpty()) {
            themeList = null;
        }
        return productMapper.selectProductsByFilter(secondCate, thirdCate, themeList);
    }

    public List<ProductVO> getProductListByCateAndTheme(SearchCriteria cri, Integer cate_code, List<Integer> themeList) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("cri", cri);
        map.put("cate_code", cate_code);
        map.put("themeList", themeList);
        map.put("themeListSize", themeList != null ? themeList.size() : 0);

        return productMapper.getProductListByCateAndTheme(map);
    }

    public int getCountProductListByCateAndTheme(Integer cate_code, List<Integer> themeList) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("cate_code", cate_code);
        map.put("themeList", themeList);
        map.put("themeListSize", themeList != null ? themeList.size() : 0);

        return productMapper.getCountProductListByCateAndTheme(map);
    }

    // ---------------- 상품 테마 조회 ----------------
    public List<String> getThemesByProduct(Integer pro_num) {
        List<String> themes = productThemeMapper.selectThemesByProduct(pro_num);
        return themes != null ? themes : List.of(); // null 방지
    }
    
    // 후기 + 답변 조회
    public List<ReviewVO> getReviewsWithReplies(int proNum) {
        return productMapper.getReviewsWithReplies(proNum);
    }

    // 후기 답변 등록
    public void addReviewReply(Long revCode, String managerId, String replyText) {
        productMapper.insertReviewReply(revCode, managerId, replyText);
    }

    // 검색 메서드 추가
    public List<ProductVO> searchProducts(String keyword) {
        List<ProductVO> products;

        if (keyword == null || keyword.trim().isEmpty()) {
            products = productMapper.getAllProducts(); // 전체 상품 반환
        } else {
            products = productMapper.searchByKeyword(keyword); // 키워드 검색
        }

        // 각 상품에 지역명과 테마 리스트 세팅
        for (ProductVO p : products) {
            String cateName = productMapper.getCategoryName(p.getCate_code());
            p.setCate_name(cateName);

            List<String> themeNames = productMapper.getThemeNamesByProduct(p.getPro_num());
            p.setThemeList(themeNames);
        }

        return products;
    }


}
