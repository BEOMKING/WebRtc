package com.ssafy.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.api.request.UserRegisterDTO;
import com.ssafy.api.response.UserRes;
import com.ssafy.api.service.UserService;
import com.ssafy.common.auth.SsafyUserDetails;
import com.ssafy.common.model.response.BaseResponseBody;
import com.ssafy.db.entity.User;
import com.ssafy.db.repository.UserRepository;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import springfox.documentation.annotations.ApiIgnore;

// 유저 관련 API 요청 처리를 위한 컨트롤러 정의.
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
	@Autowired
	UserService userService;
	@Autowired
	UserRepository userRepository;
	
	@PostMapping("/signup") //회원가입
	@ApiOperation(value = "회원 가입", notes = "<strong>아이디, 패스워드, 이메일, 닉네임, 핸드폰 번호</strong>를 통해 회원가입 한다.") 
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 401, message = "인증 실패"),
        @ApiResponse(code = 404, message = "사용자 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
	public ResponseEntity<? extends BaseResponseBody> register(@RequestBody UserRegisterDTO userRegister) {
		
		User user = new User(userRegister.getId(), userRegister.getName(), userRegister.getPassword(),
				userRegister.getNickname(), userRegister.getEmailS(), userRegister.getEmailE(), userRegister.getPhone());
		
		String id = userService.join(user);

		return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
	}
	
	@GetMapping("/me") //회원정보조회
	public ResponseEntity<UserRes> getUserInfo(@ApiIgnore Authentication authentication) {
		/**
		 * 요청 헤더 액세스 토큰이 포함된 경우에만 실행되는 인증 처리이후, 리턴되는 인증 정보 객체(authentication) 통해서 요청한 유저 식별.
		 * 액세스 토큰이 없이 요청하는 경우, 403 에러({"error": "Forbidden", "message": "Access Denied"}) 발생.
		 */
		SsafyUserDetails userDetails = (SsafyUserDetails)authentication.getDetails();
		String userId = userDetails.getUsername();
		User user = userService.findOne(userId);
		
		return ResponseEntity.status(200).body(UserRes.of(user));
	}
	
	@GetMapping("/{userId}") // 회원가입 ID 중복 체크
	public ResponseEntity<? extends BaseResponseBody> duplicateId (@PathVariable("userId") String userId ) {

		User user = userService.findOne(userId);
		if(user != null) {
			return ResponseEntity.status(409).body(BaseResponseBody.of(409, "이미 존재하는 사용자 ID 입니다.")); 
		}
		return ResponseEntity.status(200).body(BaseResponseBody.of(200, "사용자 ID 없음")); 
		
	}
	
	@DeleteMapping //회원 탈퇴
	public ResponseEntity<? extends BaseResponseBody> deleteUser (Authentication authentication) {
		SsafyUserDetails userDetails = (SsafyUserDetails) authentication.getDetails();
		String nowId = userDetails.getUsername();
		
		userRepository.delete(nowId);
		return ResponseEntity.status(200).body(BaseResponseBody.of(200, "삭제되었습니다"));
	}
	
	@PatchMapping("/modify") //회원 수정
	public ResponseEntity<? extends BaseResponseBody> modifyUser (Authentication authentication, String userNickname) {
		System.out.println(authentication);
		SsafyUserDetails userDetails = (SsafyUserDetails) authentication.getDetails();
		String nowId = userDetails.getUser().getId();
		userService.modify(nowId, userNickname);//회원의 정보, 변경할 닉네임
		return ResponseEntity.status(200).body(BaseResponseBody.of(200, "수정되었습니다"));
	}
}
