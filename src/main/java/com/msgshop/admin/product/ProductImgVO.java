package com.msgshop.admin.product;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProductImgVO {

    private Integer img_id;       // IMG_ID, auto_increment PK
    private Integer pro_num;      // 연결된 상품 번호 (FK)
    private String img_name;      // 실제 저장된 파일명 (UUID+원본)
    private String img_folder;    // 업로드 폴더 (예: 2025/09/10)
    private String is_thumb; // 썸네일 여부, 기본값 N
    private Integer sort_order = 0; // 이미지 순서, 기본값 0
}
