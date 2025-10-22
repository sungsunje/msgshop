package com.msgshop.admin.partner.stats;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PartnerStatsVO {
    private int statId;               // PK
    private int proNum;               // 상품 번호
    private Integer viewCount;            // 조회수
    private Integer phoneInquiryCount;    // 전화문의 수
    private LocalDateTime statDate;   // 기록 날짜
    
    private String proName;
}
