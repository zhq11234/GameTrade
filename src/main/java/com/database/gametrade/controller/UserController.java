package com.database.gametrade.controller;

import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.service.UserService;
import com.database.gametrade.util.LogUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private LogUtil logUtil;
    
    /**
     * 买家注册
     * POST /api/users/register/buyer
     */
    @PostMapping("/register/buyer")
    public ResponseEntity<?> registerBuyer(@Valid @RequestBody BuyerRegisterRequest registerRequest) {
        logUtil.logBuyerRegisterRequest(registerRequest.getAccount(), registerRequest.getNickname());
        
        boolean success = userService.registerBuyer(
            registerRequest.getAccount(),
            registerRequest.getPassword(),
            registerRequest.getContact(),
            registerRequest.getNickname()
        );
        
        if (success) {
            logUtil.logBuyerRegisterSuccess(registerRequest.getAccount(), registerRequest.getNickname());
            return ResponseEntity.ok().build();
        } else {
            logUtil.logBuyerRegisterFailure(registerRequest.getAccount(), "账号、联系方式或昵称已存在");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("账号、联系方式或昵称已存在");
        }
    }
    
    /**
     * 厂商注册
     * POST /api/users/register/vendor
     */
    @PostMapping("/register/vendor")
    public ResponseEntity<?> registerVendor(@Valid @RequestBody VendorRegisterRequest registerRequest) {
        logUtil.logVendorRegisterRequest(registerRequest.getAccount(), registerRequest.getCompanyName());
        
        boolean success = userService.registerVendor(
            registerRequest.getAccount(),
            registerRequest.getPassword(),
            registerRequest.getContact(),
            registerRequest.getCompanyName(),
            registerRequest.getRegisteredAddress(),
            registerRequest.getContactPerson()
        );
        
        if (success) {
            logUtil.logVendorRegisterSuccess(registerRequest.getAccount(), registerRequest.getCompanyName());
            return ResponseEntity.ok().build();
        } else {
            logUtil.logVendorRegisterFailure(registerRequest.getAccount(), "账号、联系方式或企业名已存在");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("账号、联系方式或企业名已存在");
        }
    }
    
    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserInfo user = userService.login(loginRequest.getAccount(), loginRequest.getPassword());
        if (user != null) {
            // 登录成功，返回用户信息（不包含密码）
            UserResponse userResponse = new UserResponse(
                user.getAccount(),
                user.getRole(),
                user.getContact(),
                user.getRegisterTime()
            );
            
            // 根据用户角色记录不同的登录成功日志
            if ("buyer".equals(user.getRole())) {
                logUtil.logBuyerLoginSuccess(user.getAccount(), "未知昵称"); // 暂时使用占位符
            } else if ("vendor".equals(user.getRole())) {
                logUtil.logVendorLoginSuccess(user.getAccount(), "未知企业名"); // 暂时使用占位符
            } else {
                logUtil.logWarning("未知用户角色登录成功 - 账号: " + user.getAccount() + ", 角色: " + user.getRole());
            }
            
            return ResponseEntity.ok(userResponse);
        } else {
            logUtil.logBuyerLoginFailure(loginRequest.getAccount(), "账号或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("账号或密码错误");
        }
    }
    
    /**
     * 买家登出
     * POST /api/users/logout/buyer
     */
    @PostMapping("/logout/buyer")
    public ResponseEntity<?> logoutBuyer(@RequestParam String account) {
        // 这里可以添加登出逻辑，比如清除session或token
        logUtil.logBuyerLogout(account, "未知昵称"); // 暂时使用占位符
        return ResponseEntity.ok().body("买家登出成功");
    }
    
    /**
     * 商家登出
     * POST /api/users/logout/vendor
     */
    @PostMapping("/logout/vendor")
    public ResponseEntity<?> logoutVendor(@RequestParam String account) {
        // 这里可以添加登出逻辑，比如清除session或token
        logUtil.logVendorLogout(account, "未知企业名"); // 暂时使用占位符
        return ResponseEntity.ok().body("商家登出成功");
    }
    
    /**
     * 检查账号是否存在
     * GET /api/users/check-account
     */
    @GetMapping("/check-account")
    public ResponseEntity<Boolean> checkAccountExists(@RequestParam String account) {
        boolean exists = userService.checkAccountExists(account);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 检查联系方式是否存在
     * GET /api/users/check-contact
     */
    @GetMapping("/check-contact")
    public ResponseEntity<Boolean> checkContactExists(@RequestParam String contact) {
        boolean exists = userService.checkContactExists(contact);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 检查昵称是否存在
     * GET /api/users/check-nickname
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNicknameExists(@RequestParam String nickname) {
        boolean exists = userService.checkNicknameExists(nickname);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 检查企业名是否存在
     * GET /api/users/check-company
     */
    @GetMapping("/check-company")
    public ResponseEntity<Boolean> checkCompanyNameExists(@RequestParam String companyName) {
        boolean exists = userService.checkCompanyNameExists(companyName);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * 买家注册请求DTO
     */
    @Setter
    @Getter
    public static class BuyerRegisterRequest {
        @NotBlank(message = "角色不能为空")
        @Size(max = 20, message = "角色长度不能超过20个字符")
        private String role = "buyer";
        
        @NotBlank(message = "账号不能为空")
        @Size(max = 50, message = "账号长度不能超过50个字符")
        private String account;
        
        @NotBlank(message = "密码不能为空")
        @Size(max = 100, message = "密码长度不能超过100个字符")
        private String password;
        
        @NotBlank(message = "联系方式不能为空")
        @Size(max = 100, message = "联系方式长度不能超过100个字符")
        private String contact;
        
        @NotBlank(message = "昵称不能为空")
        @Size(max = 50, message = "昵称长度不能超过50个字符")
        private String nickname;
    }
    
    /**
     * 厂商注册请求DTO
     */
    @Setter
    @Getter
    public static class VendorRegisterRequest {
        @NotBlank(message = "角色不能为空")
        @Size(max = 20, message = "角色长度不能超过20个字符")
        private String role = "vendor";
        
        @NotBlank(message = "账号不能为空")
        @Size(max = 50, message = "账号长度不能超过50个字符")
        private String account;
        
        @NotBlank(message = "密码不能为空")
        @Size(max = 100, message = "密码长度不能超过100个字符")
        private String password;
        
        @NotBlank(message = "联系方式不能为空")
        @Size(max = 100, message = "联系方式长度不能超过100个字符")
        private String contact;
        
        @NotBlank(message = "企业名不能为空")
        @Size(max = 100, message = "企业名长度不能超过100个字符")
        private String companyName;
        
        @NotBlank(message = "注册地址不能为空")
        @Size(max = 200, message = "注册地址长度不能超过200个字符")
        private String registeredAddress;
        
        @NotBlank(message = "联系人不能为空")
        @Size(max = 50, message = "联系人长度不能超过50个字符")
        private String contactPerson;
    }
    
    /**
     * 登录请求DTO
     */
    @Setter
    @Getter
    public static class LoginRequest {
        @NotBlank(message = "账号不能为空")
        @Size(max = 50, message = "账号长度不能超过50个字符")
        private String account;
        
        @NotBlank(message = "密码不能为空")
        @Size(max = 100, message = "密码长度不能超过100个字符")
        private String password;
    }
    
    /**
     * 用户响应DTO（不包含密码）
     */
    @Setter
    @Getter
    public static class UserResponse {
        private String account;
        private String role;
        private String contact;
        private String registerTime;
        
        public UserResponse() {}
        
        public UserResponse(String account, String role, String contact, java.time.LocalDateTime registerTime) {
            this.account = account;
            this.role = role;
            this.contact = contact;
            this.registerTime = registerTime != null ? registerTime.toString() : null;
        }
    }
}
