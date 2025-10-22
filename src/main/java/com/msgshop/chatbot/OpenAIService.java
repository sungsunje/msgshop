package com.msgshop.chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.msgshop.order.OrderMapper;
import com.msgshop.order.OrderVO;

import java.text.SimpleDateFormat;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

	private final OrderMapper orderMapper;
	
    @Value("${openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askChatGPT(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";

        ChatRequest request = new ChatRequest(
                "gpt-3.5-turbo",
                Collections.singletonList(
                        new ChatRequest.Message("user", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ChatResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                ChatResponse.class
        );

        return response.getBody()
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();
    }
    
    public String handleUserMessage(String userMessage) {
    	
    	Integer maybeOrderId = extractFirstInteger(userMessage);
    	
    	log.info("주문번호: " + maybeOrderId);
    	
    	if (maybeOrderId != null) {
    		OrderVO order = orderMapper.findOrderById(maybeOrderId);
    		log.info("주문정보: " + order);
    		
    		if (order != null) {
    			
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy년M월d일");	
    			
    		return String.format("주문번호 %d의 상태는 [%s] 입니다. (주문일: %s)",
                    order.getOrd_code(),
                    order.getOrd_status() == null ? "정보없음" : order.getOrd_status(),
                    sdf.format(order.getOrd_regdate()));
	    	}else {
	    		// 주문 번호는 있어보이나 DB에 없다 -> OpenAI에게 '주문번호 없음'을 알리고 일반 응답도 요청 가능
	            String prompt = "사용자가 주문번호 " + maybeOrderId + " 에 대해 문의했습니다. DB에 해당 주문이 없습니다. " +
	                    "고객에게 친절하게 '주문번호를 찾을 수 없습니다'라고 안내하고, 반품/환불/배송조회 방법을 간단히 안내해주세요.";
	            return askChatGPT(prompt);
	    	}
    	}
    	// 숫자가 없거나 주문번호가 아니면 ChatGPT에 전달
        return askChatGPT(userMessage);
    }
    
    private Integer extractFirstInteger(String text) {
        try {
            String onlyNums = text.replaceAll("[^0-9]+", " ").trim();
            if (onlyNums.isEmpty()) return null;
            String first = onlyNums.split("\\s+")[0];
            return Integer.parseInt(first);
        } catch (Exception e) {
            return null;
        }
    }
}

