package com.msgshop.review;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Data
public class ReviewReply {

	private Long reply_id;
    private Long rev_code;
    private String manager_id;
    private String reply_text;
    private LocalDateTime reply_date;
    
    // 후기와 연동할 경우 VO에 pro_num도 포함시키면 redirect 편함
    private Integer pro_num;

}
