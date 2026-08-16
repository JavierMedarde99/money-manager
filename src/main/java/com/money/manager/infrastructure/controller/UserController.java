package com.money.manager.infrastructure.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.money.manager.application.mappers.UserMapper;
import com.money.manager.domain.User;
import com.money.manager.application.ports.UserService;
import com.money.manager.application.dtos.LoginRequestDTO;
import com.money.manager.application.dtos.TokenResponseDTO;
import com.money.manager.application.dtos.UserRequestDTO;
import com.money.manager.application.dtos.UserResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/user")
@CrossOrigin(origins =  "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> loginController(@RequestBody @Valid LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        return ResponseEntity.ok(userService.login(loginRequestDTO));
    }
    
    @PostMapping("")
    public ResponseEntity<TokenResponseDTO> insertUserController(@RequestBody @Valid UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.createUser(userRequestDTO));
    }
    
    @GetMapping("")
    public ResponseEntity<UserResponseDto> getUserController(Authentication authentication) {
        return ResponseEntity.ok(UserMapper.toDto((User) authentication.getPrincipal()));
    }

    @PutMapping("")
    public ResponseEntity<UserResponseDto> updateUserConroller(@RequestBody @Valid UserRequestDTO userRequestDTO, Authentication authentication) {
        return ResponseEntity.ok(userService.updateUser(userRequestDTO, (User) authentication.getPrincipal()));
    }

    @DeleteMapping("")
    public ResponseEntity<String> deleteUserController(Authentication authentication){
        return ResponseEntity.ok(userService.deleteUser((User) authentication.getPrincipal()));
    }
    

}
