package com.msgshop.kakaologin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class KakaoLoginService {
	
	
	@Value("${kakao.token_url}")
	private String token_url;
	
	@Value("${kakao.user.logout}")
	private String kakaologout;
	
	@Autowired
	KakaoLoginDao dao;
	
	// 토큰요청
	public String getAccessTokenFromKakao(String client_id, String code, String redirect_uri, String client_secret) throws IOException {
        //------kakao POST 요청------
        String reqURL = token_url + "?grant_type=authorization_code&client_id="+client_id+"&code="+code+"&redirect_uri="+redirect_uri+"&client_secret=" + client_secret + "&prompt=login";
        URL url = new URL(reqURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line = "";
        String result = "";

        while ((line = br.readLine()) != null) {
            result += line;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {});

        //System.out.println("Response Body : " + result);

        String accessToken = (String) jsonMap.get("access_token");
        //String refreshToken = (String) jsonMap.get("refresh_token");
        //String scope = (String) jsonMap.get("scope");

        return accessToken;
    }
	
	// 사용자정보조회
	public KakaoLoginDto getUserInfo(String access_Token, KakaoLoginDto dto) throws IOException {
        //------kakao GET 요청------
        String reqURL = "https://kapi.kakao.com/v2/user/me"; // 카카오서버로부터 개인정보를 참조하는 주소.
        
        URL url = new URL(reqURL);
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + access_Token);

        int responseCode = conn.getResponseCode();
        System.out.println("responseCode : " + responseCode);

        // 입력스트림 작업.
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String line = "";
        String result = "";

        while ((line = br.readLine()) != null) {
            result += line;
        }

        // 카카오 서버로부터 개인정보를 받아옴.
        System.out.println("Response Body : " + result);

        // jackson objectmapper 객체 생성
        ObjectMapper objectMapper = new ObjectMapper();
        // JSON String -> Map
        Map<String, Object> jsonMap = objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {
        });

        //사용자 정보 추출
        //Map<String, Object> properties = (Map<String, Object>) jsonMap.get("properties");
        Map<String, Object> kakao_account = (Map<String, Object>) jsonMap.get("kakao_account");

//        Long id       = (Long) jsonMap.get("id");
//        String name   = kakao_account.get("name").toString();
        String email  = kakao_account.get("email").toString();
//        String gender = kakao_account.get("gender").toString();
//        String phone  = kakao_account.get("phone_number").toString();
        
        /*
        if(properties != null) {
        	String nickname     = properties.get("nickname").toString();
        	String profileImage = properties.get("profile_image").toString();  
        	
            dto.setNickname(nickname);
            dto.setProfile_image(profileImage);        	
        }
        */

        //userInfo에 넣기
//        dto.setId(id);
//        dto.setName(name);
        dto.setEmail(email);
//        dto.setPhone(phone);
        
        // 성별
//        if(gender.equals("male")) {
//        	dto.setGenderCd(null); // 남
//        } else {
//        	dto.setGenderCd(null); // 여
//        }

        return dto;
    }	
	
//	 로그인 id 확인
	public KakaoLoginDto kakaoOne(KakaoLoginDto dto) {
		return dao.kakaoOne(dto);
	};
	
	// 회원등록
	public int kakaoInsert(KakaoLoginDto dto) {
		return dao.kakaoInsert(dto);
	};
	
	// 카카오 로그아웃.  https://kauth.kakao.com/oauth/logout.  헤더는 있고, 파라미터는 없는 경우.
	// 헤더 Authorization: Bearer ${ACCESS_TOKEN}
	public void kakaologout(String accessToken) throws JsonProcessingException {
		
		// Http Header 생성.
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded");
		
		// Http 요청작업.
		HttpEntity<MultiValueMap<String, String>> kakaoLogoutRequest = new HttpEntity<>(headers);
		
		// Http 요청하기
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(kakaologout, HttpMethod.POST, kakaoLogoutRequest, String.class);
		
		//리턴된 정보 : JSON포맷의 문자열.
		String responseBody = response.getBody();
		log.info("responseBody:" + responseBody);
		
		// JSON문자열을 Java객체로 역직렬화 하거나 Java객체를 JSON으로 직렬화 할 때 사용하는 Jackson라이브러리의 클래스이다.
		// ObjectMapper 생성 비용이 비싸기때문에 bena/static 으로 처리하는 것이 성능에 좋다.
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(responseBody);
		
		Long id = jsonNode.get("id").asLong();
		
		log.info("id:" + id); // 로그아웃 이후 카카오에서 응답한 카카오회원번호
		
	}
	
}
