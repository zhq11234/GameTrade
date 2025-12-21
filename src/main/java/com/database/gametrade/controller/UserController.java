package com.database.gametrade.controller;

import com.database.gametrade.entity.User;
import com.database.gametrade.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user != null) {
            // 登录成功，返回用户信息（不包含密码）
            UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getNickname()
            );
            return ResponseEntity.ok(userResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户名或密码错误");
        }
    }
    
    /**
     * 用户注册
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // 调试日志：显示完整的请求数据
        logger.info("=== 注册请求调试信息 ===");
        logger.info("用户名: {}", registerRequest.getUsername());
        logger.info("密码: {}", registerRequest.getPassword() != null ? "[已设置]" : "[null]");
        logger.info("密码长度: {}", registerRequest.getPassword() != null ? registerRequest.getPassword().length() : 0);
        logger.info("邮箱: {}", registerRequest.getEmail());
        logger.info("手机: {}", registerRequest.getPhone());
        logger.info("昵称: {}", registerRequest.getNickname());
        logger.info("=== 调试信息结束 ===");

        // 检查密码是否为空
        if (registerRequest.getPassword() == null || registerRequest.getPassword().trim().isEmpty()) {
            logger.warn("密码验证失败: 密码为空");
            Map<String, String> error = new HashMap<>();
            error.put("password", "密码不能为空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // 检查密码长度
        if (registerRequest.getPassword().length() < 8) {
            logger.warn("密码验证失败: 密码长度不足8位");
            Map<String, String> error = new HashMap<>();
            error.put("password", "密码长度必须在8-100位之间");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // 后续处理逻辑...
        User user = new User(
            registerRequest.getUsername(),
            registerRequest.getPassword(),
            registerRequest.getEmail(),
            registerRequest.getPhone(),
            registerRequest.getNickname()
        );
        
        boolean success = userService.register(user);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("用户名或邮箱已存在");
        }
    }
    
    /**
     * 检查用户名是否存在
     * GET /api/users/check-username
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> checkUsernameExists(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 注册请求DTO
     */
    @Setter
    @Getter
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
        @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", message = "用户名只能包含字母、数字、下划线和中文字符")
        private String username;
        
        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 100, message = "密码长度必须在8-100位之间")
        private String password;
        
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱长度不能超过100个字符")
        private String email;
        
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
        
        @Size(max = 50, message = "昵称长度不能超过50个字符")
        private String nickname;
    }
    
    /**
     * 登录请求DTO
     */
    @Setter
    @Getter
    public static class LoginRequest {
        @jakarta.validation.constraints.NotBlank(message = "用户名不能为空")
        @jakarta.validation.constraints.Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
        private String username;
        
        @jakarta.validation.constraints.NotBlank(message = "密码不能为空")
        @jakarta.validation.constraints.Size(min = 8, message = "密码长度不能少于8位")
        private String password;
    }
    
    /**
     * 用户响应DTO（不包含密码）
     */
    @Setter
    @Getter
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String phone;
        private String nickname;
        
        public UserResponse() {}
        
        public UserResponse(Long id, String username, String email, String phone, String nickname) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.nickname = nickname;
        }
    }
}