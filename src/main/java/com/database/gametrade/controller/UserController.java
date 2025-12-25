package com.database.gametrade.controller;

import com.database.gametrade.dto.*;
import com.database.gametrade.entity.BuyerInfo;
import com.database.gametrade.entity.UserInfo;
import com.database.gametrade.entity.VendorInfo;
import com.database.gametrade.repository.BuyerInfoRepository;
import com.database.gametrade.repository.VendorInfoRepository;
import com.database.gametrade.service.UserService;
import com.database.gametrade.util.LogUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> registerBuyer(@Valid @RequestBody BuyerRegisterRequestDTO registerRequest) {
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
    public ResponseEntity<?> registerVendor(@Valid @RequestBody VendorRegisterRequestDTO registerRequest) {
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        UserInfo user = userService.login(loginRequest.getAccount(), loginRequest.getPassword());
        if (user != null) {
            // 登录成功，返回用户信息（不包含密码）
            UserResponseDTO userResponse = new UserResponseDTO(
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
    public ResponseEntity<?> createGame(@Valid @RequestBody GameCreateRequestDTO createRequest) {
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
    public ResponseEntity<?> queryVendorGames(@Valid @RequestBody VendorGameQueryRequestDTO queryRequest) {
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
    public ResponseEntity<?> queryGameInfo(@Valid @RequestBody GameInfoQueryRequestDTO queryRequest) {
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
    public ResponseEntity<?> queryGameInfoFuzzy(@Valid @RequestBody GameInfoFuzzyQueryRequestDTO queryRequest) {
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
    public ResponseEntity<?> updateGame(@Valid @RequestBody GameUpdateRequestDTO updateRequest) {
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
     * 游戏上架申请（厂商）
     * POST /api/users/game-application
     */
    @PostMapping("/game-application")
    public ResponseEntity<?> createGameApplication(@Valid @RequestBody GameApplicationRequestDTO applicationRequest) {
        logUtil.logDebug("创建游戏上架申请 - 账号: " + applicationRequest.getAccount() + ", 游戏名: " + applicationRequest.getGameName());

        int result = userService.createGameApplication(
                applicationRequest.getAccount(),
                applicationRequest.getGameName()
        );

        switch (result) {
            case 0:
                logUtil.logDebug("游戏上架申请创建成功 - 账号: " + applicationRequest.getAccount() + ", 游戏名: " + applicationRequest.getGameName());
                return ResponseEntity.ok().body("游戏上架申请创建成功");
            case -1:
                logUtil.logWarning("游戏上架申请失败 - 厂商账号不存在: " + applicationRequest.getAccount());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            case -2:
                logUtil.logWarning("游戏上架申请失败 - 游戏不存在或不属于该厂商: " + applicationRequest.getGameName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在或不属于该厂商");
            case -3:
                logUtil.logWarning("游戏上架申请失败 - 游戏已上架，无需重复申请: " + applicationRequest.getGameName());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("游戏已上架，无需重复申请");
            case -4:
                logUtil.logWarning("游戏上架申请失败 - 该游戏已有待审批的申请: " + applicationRequest.getGameName());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("该游戏已有待审批的申请");
            default:
                logUtil.logWarning("游戏上架申请失败 - 未知错误: " + result);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏上架申请失败，请稍后重试");
        }
    }

    /**
     * 游戏下架（厂商）
     * PUT /api/users/game-off-shelf
     */
    @PutMapping("/game-off-shelf")
    public ResponseEntity<?> offShelfGame(@Valid @RequestBody GameOffShelfRequestDTO offShelfRequest) {
        logUtil.logDebug("游戏下架 - 账号: " + offShelfRequest.getAccount() + ", 游戏名: " + offShelfRequest.getGameName());

        int result = userService.offShelfGame(
                offShelfRequest.getAccount(),
                offShelfRequest.getGameName()
        );

        switch (result) {
            case 0:
                logUtil.logDebug("游戏下架成功 - 账号: " + offShelfRequest.getAccount() + ", 游戏名: " + offShelfRequest.getGameName());
                return ResponseEntity.ok().body("游戏下架成功");
            case -1:
                logUtil.logWarning("游戏下架失败 - 厂商账号不存在: " + offShelfRequest.getAccount());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            case -2:
                logUtil.logWarning("游戏下架失败 - 游戏不存在或不属于该厂商: " + offShelfRequest.getGameName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在或不属于该厂商");
            case -3:
                logUtil.logWarning("游戏下架失败 - 游戏已处于下架状态: " + offShelfRequest.getGameName());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("游戏已处于下架状态，无需重复操作");
            default:
                logUtil.logWarning("游戏下架失败 - 未知错误: " + result);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏下架失败，请稍后重试");
        }
    }

    /**
     * 厂商查询游戏上架申请
     * POST /api/users/query-game-applications
     */
    @PostMapping("/query-game-applications")
    public ResponseEntity<?> queryGameApplications(@Valid @RequestBody GameApplicationQueryRequestDTO queryRequest) {
        logUtil.logDebug("查询游戏上架申请 - 账号: " + queryRequest.getAccount() + ", 状态: " + queryRequest.getApprovalStatus());

        Object result = userService.queryGameApplications(
                queryRequest.getAccount(),
                queryRequest.getApprovalStatus()
        );
        
        if (result instanceof Integer) {
            int returnValue = (Integer) result;
            switch (returnValue) {
                case -1:
                    logUtil.logWarning("游戏上架申请查询失败 - 厂商账号不存在: " + queryRequest.getAccount());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
                case -99:
                    logUtil.logError("游戏上架申请查询失败 - 存储过程执行异常", null);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                default:
                    logUtil.logWarning("游戏上架申请查询失败 - 未知错误: " + returnValue);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
            }
        } else {
            // 返回查询结果
            logUtil.logDebug("游戏上架申请查询成功 - 账号: " + queryRequest.getAccount());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 厂商取消游戏上架申请
     * DELETE /api/users/cancel-game-application
     */
    @DeleteMapping("/cancel-game-application")
    public ResponseEntity<?> cancelGameApplication(@Valid @RequestBody GameApplicationCancelRequestDTO cancelRequest) {
        logUtil.logDebug("取消游戏上架申请 - 账号: " + cancelRequest.getAccount() + ", 申请编号: " + cancelRequest.getApplicationId());

        int result = userService.cancelGameApplication(
                cancelRequest.getAccount(),
                cancelRequest.getApplicationId()
        );

        switch (result) {
            case 0:
                logUtil.logDebug("游戏上架申请取消成功 - 账号: " + cancelRequest.getAccount() + ", 申请编号: " + cancelRequest.getApplicationId());
                return ResponseEntity.ok().body("游戏上架申请取消成功");
            case -1:
                logUtil.logWarning("游戏上架申请取消失败 - 厂商账号不存在: " + cancelRequest.getAccount());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            case -2:
                logUtil.logWarning("游戏上架申请取消失败 - 申请不存在或不属于该厂商: " + cancelRequest.getApplicationId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("申请不存在或不属于该厂商");
            case -3:
                logUtil.logWarning("游戏上架申请取消失败 - 只能取消待审批的申请: " + cancelRequest.getApplicationId());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("只能取消待审批的申请");
            default:
                logUtil.logWarning("游戏上架申请取消失败 - 未知错误: " + result);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏上架申请取消失败，请稍后重试");
        }
    }
}
