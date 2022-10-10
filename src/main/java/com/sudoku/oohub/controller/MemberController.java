package com.sudoku.oohub.controller;

import com.sudoku.oohub.dto.request.CreateMemberDto;
import com.sudoku.oohub.dto.request.LoginDto;
import com.sudoku.oohub.dto.response.TokenDto;
import com.sudoku.oohub.jwt.JwtProperties;
import com.sudoku.oohub.jwt.TokenProvider;
import com.sudoku.oohub.service.MemberService;
import com.sudoku.oohub.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    @PostMapping("/v1/join")
    public ResponseEntity<Long> join(@RequestBody @Validated CreateMemberDto createMemberDto){
        Long memberId = memberService.join(createMemberDto);
        return ResponseEntity.ok(memberId);
    }

    @PostMapping("/v1/login")
    public ResponseEntity<TokenDto> login(@RequestBody @Validated LoginDto loginDto){
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        // PrincipalDetailsService의 loadUserByUsername 실행됨 -> 정상이면 authentication 객체 리턴
        // 즉 알아서 인증을 해줌 (= DB에 있는 username, password와 일치한다.)
        // authentication에는 내 로그인 정보가 담긴다.
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication);
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add(JwtProperties.HEADER_STRING,  JwtProperties.TOKEN_PREFIX + jwt);

        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/v1/users")
    public ResponseEntity<String> test(){
        String currentUsername = SecurityUtil.getCurrentUsername().orElseGet(null);
        return ResponseEntity.ok(currentUsername);
    }

}
