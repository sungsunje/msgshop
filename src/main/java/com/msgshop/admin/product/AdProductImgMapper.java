package com.msgshop.admin.product;

import java.util.List;

public interface AdProductImgMapper {

    // 1) 상품 이미지 등록
    void insertProductImg(ProductImgVO img);

    // 2) 상품별 이미지 조회
    List<ProductImgVO> getProductImages(int pro_num);

    // 3) 상품별 이미지 삭제
    void deleteProductImagesByProNum(int pro_num);
}
