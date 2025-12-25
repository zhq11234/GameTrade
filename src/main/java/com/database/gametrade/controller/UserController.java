package com.database.gametrade.controller;

import com.database.gametrade.entity.BuyerInfo;
import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.entity.VendorInfo;
import com.database.gametrade.repository.BuyerInfoRepository;
import com.database.gametrade.repository.VendorInfoRepository;
import com.database.gametrade.service.UserService;
import com.database.gametrade.util.LogUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private BuyerInfoRepository buyerInfoRepository;

    @Autowired
    private VendorInfoRepository vendorInfoRepository;

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
                // 查询买家信息获取昵称
                Optional<BuyerInfo> buyerInfo = buyerInfoRepository.findByAccount(user.getAccount());
                String nickname = buyerInfo.map(BuyerInfo::getNickname).orElse("未知昵称");
                logUtil.logBuyerLoginSuccess(user.getAccount(), nickname);
            } else if ("vendor".equals(user.getRole())) {
                // 查询厂商信息获取企业名
                Optional<VendorInfo> vendorInfo = vendorInfoRepository.findByAccount(user.getAccount());
                String companyName = vendorInfo.map(VendorInfo::getCompanyName).orElse("未知企业名");
                logUtil.logVendorLoginSuccess(user.getAccount(), companyName);
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
        Optional<BuyerInfo> buyerInfo = buyerInfoRepository.findByAccount(account);
        String nickname = buyerInfo.map(BuyerInfo::getNickname).orElse("未知昵称");
        logUtil.logBuyerLogout(account, nickname);
        return ResponseEntity.ok().body("买家登出成功");
    }

    /**
     * 商家登出
     * POST /api/users/logout/vendor
     */
    @PostMapping("/logout/vendor")
    public ResponseEntity<?> logoutVendor(@RequestParam String account) {
        // 这里可以添加登出逻辑，比如清除session或token
        Optional<VendorInfo> vendorInfo = vendorInfoRepository.findByAccount(account);
        String companyName = vendorInfo.map(VendorInfo::getCompanyName).orElse("未知企业名");
        logUtil.logVendorLogout(account, companyName);
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
     * 查询用户个人信息
     */
    @GetMapping("/personal-info")
    public ResponseEntity<?> getPersonalInfo(@RequestParam String account) {
        logUtil.logDebug("查询个人信息 - 账号: " + account);

        Object personalInfo = userService.getUserPersonalInfo(account);
        if (personalInfo == null) {
            logUtil.logWarning("查询个人信息失败 - 账号不存在: " + account);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在");
        }

        logUtil.logDebug("查询个人信息成功 - 账号: " + account);
        return ResponseEntity.ok(personalInfo);
    }

    /**
     * 修改用户个人信息
     */
    @PutMapping("/personal-info")
    public ResponseEntity<?> updatePersonalInfo(@RequestParam String account, @RequestBody Object personalInfo) {
        logUtil.logDebug("修改个人信息 - 账号: " + account);

        boolean success = userService.updatePersonalInfo(account, personalInfo);
        if (success) {
            logUtil.logDebug("修改个人信息成功 - 账号: " + account);
            return ResponseEntity.ok().body("个人信息修改成功");
        } else {
            logUtil.logWarning("修改个人信息失败 - 账号不存在: " + account);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("用户不存在或修改失败");
        }
    }

    /**
     * 游戏创建（厂商）
     * POST /api/users/create-game
     */
    @PostMapping("/create-game")
    public ResponseEntity<?> createGame(@Valid @RequestBody GameCreateRequest createRequest) {
        logUtil.logDebug("创建游戏 - 账号: " + createRequest.getAccount() + ", 游戏名: " + createRequest.getGameName());

        int result = userService.createGame(
                createRequest.getAccount(),
                createRequest.getGameName(),
                createRequest.getCategory(),
                createRequest.getPrice(),
                createRequest.getDescription(),
                createRequest.getDownloadLink(),
                createRequest.getLicenseNumber()
        );

        switch (result) {
            case 0:
                logUtil.logDebug("游戏创建成功 - 账号: " + createRequest.getAccount() + ", 游戏名: " + createRequest.getGameName());
                return ResponseEntity.ok().body("游戏创建成功");
            case -1:
                logUtil.logWarning("游戏创建失败 - 厂商账号不存在: " + createRequest.getAccount());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            case -2:
                logUtil.logWarning("游戏创建失败 - 游戏名已存在: " + createRequest.getGameName());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("游戏名已存在");
            case -3:
                logUtil.logWarning("游戏创建失败 - 版号已存在: " + createRequest.getLicenseNumber());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("版号已存在");
            case -4:
                logUtil.logWarning("游戏创建失败 - 价格不能为负数: " + createRequest.getPrice());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("价格不能为负数");
            default:
                logUtil.logWarning("游戏创建失败 - 未知错误: " + result);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏创建失败，请稍后重试");
        }
    }

    /**
     * 厂商拥有游戏查询
     * POST /api/users/query-vendor-games
     */
    @PostMapping("/query-vendor-games")
    public ResponseEntity<?> queryVendorGames(@Valid @RequestBody VendorGameQueryRequest queryRequest) {
        logUtil.logDebug("查询厂商游戏 - 账号: " + queryRequest.getAccount());

        Object result = userService.queryVendorGames(queryRequest.getAccount());
        
        if (result instanceof Integer) {
            int returnValue = (Integer) result;
            switch (returnValue) {
                case -1:
                    logUtil.logWarning("厂商游戏查询失败 - 厂商账号不存在: " + queryRequest.getAccount());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
                case -99:
                    logUtil.logError("厂商游戏查询失败 - 存储过程执行异常", null);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                default:
                    logUtil.logWarning("厂商游戏查询失败 - 未知错误: " + returnValue);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
            }
        } else {
            // 返回查询结果
            logUtil.logDebug("厂商游戏查询成功 - 账号: " + queryRequest.getAccount());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 游戏具体信息查询
     * POST /api/users/query-game-info
     */
    @PostMapping("/query-game-info")
    public ResponseEntity<?> queryGameInfo(@Valid @RequestBody GameInfoQueryRequest queryRequest) {
        logUtil.logDebug("查询游戏信息 - 游戏名: " + queryRequest.getGameName());

        Object result = userService.queryGameInfo(queryRequest.getGameName());
        
        if (result instanceof Integer) {
            int returnValue = (Integer) result;
            switch (returnValue) {
                case -1:
                    logUtil.logWarning("游戏信息查询失败 - 游戏不存在: " + queryRequest.getGameName());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在");
                case -99:
                    logUtil.logError("游戏信息查询失败 - 存储过程执行异常", null);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                default:
                    logUtil.logWarning("游戏信息查询失败 - 未知错误: " + returnValue);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
            }
        } else {
            // 返回查询结果
            logUtil.logDebug("游戏信息查询成功 - 游戏名: " + queryRequest.getGameName());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 游戏信息模糊查询
     * POST /api/users/query-game-info-fuzzy
     */
    @PostMapping("/query-game-info-fuzzy")
    public ResponseEntity<?> queryGameInfoFuzzy(@Valid @RequestBody GameInfoFuzzyQueryRequest queryRequest) {
        logUtil.logDebug("模糊查询游戏信息 - 关键词: " + queryRequest.getKeyword());

        Object result = userService.queryGameInfoFuzzy(queryRequest.getKeyword());
        
        if (result == null || (result instanceof java.util.List && ((java.util.List<?>) result).isEmpty())) {
            logUtil.logDebug("模糊查询游戏信息 - 未找到匹配的游戏: " + queryRequest.getKeyword());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到匹配的游戏");
        } else {
            // 返回查询结果
            logUtil.logDebug("模糊查询游戏信息成功 - 关键词: " + queryRequest.getKeyword());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 游戏信息修改（厂商）
     * PUT /api/users/update-game
     */
    @PutMapping("/update-game")
    public ResponseEntity<?> updateGame(@Valid @RequestBody GameUpdateRequest updateRequest) {
        logUtil.logDebug("修改游戏信息 - 账号: " + updateRequest.getAccount() + ", 游戏名: " + updateRequest.getGameName());

        int result = userService.updateGameInfo(
                updateRequest.getAccount(),
                updateRequest.getGameName(),
                updateRequest.getPrice(),
                updateRequest.getDescription(),
                updateRequest.getLicenseNumber(),
                updateRequest.getDownloadLink()
        );

        switch (result) {
            case 0:
                logUtil.logDebug("游戏信息修改成功 - 账号: " + updateRequest.getAccount() + ", 游戏名: " + updateRequest.getGameName());
                return ResponseEntity.ok().body("游戏信息修改成功");
            case -1:
                logUtil.logWarning("游戏信息修改失败 - 厂商账号不存在: " + updateRequest.getAccount());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            case -2:
                logUtil.logWarning("游戏信息修改失败 - 游戏不存在或不属于该厂商: " + updateRequest.getGameName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在或不属于该厂商");
            case -3:
                logUtil.logWarning("游戏信息修改失败 - 至少需要提供一个要修改的字段");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("至少需要提供一个要修改的字段");
            case -4:
                logUtil.logWarning("游戏信息修改失败 - 价格不能为负数: " + updateRequest.getPrice());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("价格不能为负数");
            case -5:
                logUtil.logWarning("游戏信息修改失败 - 版号已存在: " + updateRequest.getLicenseNumber());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("版号已存在");
            default:
                logUtil.logWarning("游戏信息修改失败 - 未知错误: " + result);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏信息修改失败，请稍后重试");
        }
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

    @Setter
    @Getter
    public static class GameCreateRequest {
        @NotBlank(message = "账号不能为空")
        @Size(max = 50, message = "账号长度不能超过50个字符")
        private String account;

        @NotBlank(message = "游戏名不能为空")
        @Size(max = 100, message = "游戏名长度不能超过100个字符")
        private String gameName;

        @NotBlank(message = "游戏类别不能为空")
        @Size(max = 50, message = "游戏类别长度不能超过50个字符")
        private String category;

        @DecimalMin(value = "0.0", message = "价格不能为负数")
        private BigDecimal price;

        @NotBlank(message = "游戏简介不能为空")
        @Size(max = 500, message = "游戏简介长度不能超过500个字符")
        private String description;

        @NotBlank(message = "下载链接不能为空")
        @Size(max = 255, message = "下载链接长度不能超过255个字符")
        private String downloadLink;

        @NotBlank(message = "版号不能为空")
        @Size(max = 50, message = "版号长度不能超过50个字符")
        private String licenseNumber;
    }

    @Setter
    @Getter
    public static class VendorGameQueryRequest {
        @NotBlank(message = "厂商账号不能为空")
        @Size(max = 50, message = "厂商账号长度不能超过50个字符")
        private String account;
    }

    @Setter
    @Getter
    public static class GameInfoQueryRequest {
        @NotBlank(message = "游戏名不能为空")
        @Size(max = 100, message = "游戏名长度不能超过100个字符")
        private String gameName;
    }

    @Setter
    @Getter
    public static class GameInfoFuzzyQueryRequest {
        @NotBlank(message = "游戏名关键词不能为空")
        @Size(max = 100, message = "游戏名关键词长度不能超过100个字符")
        private String keyword;
    }

    @Setter
    @Getter
    public static class GameUpdateRequest {
        @NotBlank(message = "厂商账号不能为空")
        @Size(max = 50, message = "厂商账号长度不能超过50个字符")
        private String account;

        @NotBlank(message = "游戏名不能为空")
        @Size(max = 100, message = "游戏名长度不能超过100个字符")
        private String gameName;

        @DecimalMin(value = "0.0", message = "价格不能为负数")
        private BigDecimal price;

        @Size(max = 500, message = "游戏简介长度不能超过500个字符")
        private String description;

        @Size(max = 50, message = "版号长度不能超过50个字符")
        private String licenseNumber;

        @Size(max = 255, message = "下载链接长度不能超过255个字符")
        private String downloadLink;
    }
}
