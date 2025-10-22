package com.msgshop.admin.product;

import lombok.Data;

// 테마 정보를 담는 VO
@Data
public class ThemeVO {

    private int theme_code;   // 테마 코드 (PK)
    private String theme_name; // 테마 이름
    private int theme_order;  // 출력 순서
}
