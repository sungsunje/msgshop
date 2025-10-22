package com.msgshop.admin.product;

import java.util.Date;
import java.util.List;

import com.msgshop.review.ReviewVO;

import java.util.ArrayList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductVO {

    private Integer pro_num;

    private Integer cate_code = 0;  // DB 기본 카테고리 코드 (3차 기준)
    private String cate_name;
    private String pro_name;
    private String pro_telephone;

    private Integer pro_price = 0;
    private Integer pro_discount = 0;

    private String pro_content;
    
    private String pro_zipcode;     // 우편번호
    private String pro_addr;        // 기본 주소
    private String pro_deaddr;      // 상세 주소
    private String pro_extra_addr;

    // -----------------------------
    // 기존 pro_img 필드 (DB 기본값 유지 가능)
    private String pro_img;                 // 대표 이미지 또는 단일 이미지 필드
    private String pro_up_folder;           // 일반 이미지 업로드 폴더 (썸네일과 동일 폴더 사용 가능)

    private String pro_buy = "Y";
    private Integer pro_review = 0;

    private Date pro_date;

    // -----------------------------
    // 일반 이미지 리스트 (다중 업로드용)
    private List<ProductImgVO> imgList;

    // -----------------------------
    // 상품별 테마 리스트
    private List<String> themeList = new ArrayList<>();
    
    private List<Integer> themeCodes = new ArrayList<>();
    private List<String> themeNames = new ArrayList<>();

    // -----------------------------
    // 새로 추가된 2차/3차 카테고리 코드 (Integer 타입)
    private Integer second_cate_code; // 2차 카테고리 코드
    private Integer third_cate_code;  // 3차 카테고리 코드

    // -----------------------------
    // 헬퍼 메서드
    public int getProNumSafe() {
        return pro_num != null ? pro_num : 0;
    }
    
	// -----------------------------
	// 상품별 후기 리스트
	private List<ReviewVO> reviewList = new ArrayList<>();
	
	public List<ReviewVO> getReviewList() {
	    return reviewList;
	}
	
	public void setReviewList(List<ReviewVO> reviewList) {
	    this.reviewList = reviewList;
	}
    
}
