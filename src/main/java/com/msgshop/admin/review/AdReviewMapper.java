package com.msgshop.admin.review;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.msgshop.common.utils.SearchCriteria;
import com.msgshop.review.ReviewReply;
import com.msgshop.review.ReviewVO;

public interface AdReviewMapper {

	// 조인(join)의 결과를 받을 때 List<Map<String, Object>> 사용
	//List<Map<String, Object>> review_list(@Param("cri") SearchCriteria cri, @Param("rev_rate") String rev_rate, @Param("rev_content") String rev_content);
	
	List<ReviewVO> review_list(@Param("cri") SearchCriteria cri, @Param("rev_rate") String rev_rate, @Param("rev_content") String rev_content);
	
	int review_count(@Param("cri") SearchCriteria cri, @Param("rev_rate") String rev_rate, @Param("rev_content") String rev_content);
	
	ReviewReply reply_info(Long reply_id);
	
	void reply_insert(ReviewReply vo);
	
	void reply_modify(@Param("reply_id") Long reply_id, @Param("reply_text") String reply_text);
	
	void reply_delete(Long reply_id);
	
	List<ReviewVO> review_listWithProduct(@Param("cri") SearchCriteria cri, 
            @Param("rev_rate") String rev_rate, 
            @Param("rev_content") String rev_content);
}
