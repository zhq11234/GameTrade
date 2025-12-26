package com.database.gametrade.controller;

import com.database.gametrade.dto.*;
import com.database.gametrade.entity.VendorInfo;
import com.database.gametrade.repository.VendorInfoRepository;
import com.database.gametrade.service.VendorUserService;
import com.database.gametrade.util.LogUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/vendors")
public class VendorUserController {

    @Autowired
    private VendorUserService vendorUserService;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private VendorInfoRepository vendorInfoRepository;

    /**
     * 厂商注册
     * POST /api/vendors/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerVendor(@Valid @RequestBody VendorRegisterRequestDTO registerRequest) {
        logUtil.logVendorRegisterRequest(registerRequest.getAccount(), registerRequest.getCompanyName());

        boolean success = vendorUserService.registerVendor(
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
     * 厂商登出
     * POST /api/vendors/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutVendor(@RequestParam String account) {
        // 这里可以添加登出逻辑，比如清除session或token
        Optional<VendorInfo> vendorInfo = vendorInfoRepository.findByAccount(account);
        String companyName = vendorInfo.map(VendorInfo::getCompanyName).orElse("未知企业名");
        logUtil.logVendorLogout(account, companyName);
        return ResponseEntity.ok().body("商家登出成功");
    }

    /**
     * 检查企业名是否存在
     * GET /api/vendors/check-company
     */
    @GetMapping("/check-company")
    public ResponseEntity<Boolean> checkCompanyNameExists(@RequestParam String companyName) {
        boolean exists = vendorUserService.checkCompanyNameExists(companyName);
        return ResponseEntity.ok(exists);
    }

    /**
     * 查询厂商个人信息
     * GET /api/vendors/personal-info
     */
    @GetMapping("/personal-info")
    public ResponseEntity<?> getPersonalInfo(@RequestParam String account) {
        logUtil.logDebug("查询厂商个人信息 - 账号: " + account);

        Object personalInfo = vendorUserService.getVendorPersonalInfo(account);
        if (personalInfo == null) {
            logUtil.logWarning("查询厂商个人信息失败 - 账号不存在: " + account);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商不存在");
        }

        logUtil.logDebug("查询厂商个人信息成功 - 账号: " + account);
        return ResponseEntity.ok(personalInfo);
    }

    /**
     * 修改厂商个人信息
     * PUT /api/vendors/personal-info
     */
    @PutMapping("/personal-info")
    public ResponseEntity<?> updatePersonalInfo(@RequestParam String account, @Valid @RequestBody VendorInfoDTO personalInfo) {
        logUtil.logDebug("修改厂商个人信息 - 账号: " + account);

        int result = vendorUserService.updateVendorPersonalInfo(account, personalInfo);
        
        return switch (result) {
            case 0 -> {
                logUtil.logDebug("修改厂商个人信息成功 - 账号: " + account);
                yield ResponseEntity.ok().body("厂商个人信息修改成功");
            }
            case -1 -> {
                logUtil.logWarning("修改厂商个人信息失败 - 账号不存在: " + account);
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商不存在");
            }
            case -2 -> {
                logUtil.logWarning("修改厂商个人信息失败 - 联系方式已存在: " + account);
                yield ResponseEntity.status(HttpStatus.CONFLICT).body("联系方式已存在");
            }
            default -> {
                logUtil.logWarning("修改厂商个人信息失败 - 未知错误: " + result);
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("修改失败，请稍后重试");
            }
        };
    }

    /**
     * 游戏创建（厂商）
     * POST /api/vendors/create-game
     */
    @PostMapping("/create-game")
    public ResponseEntity<?> createGame(@Valid @RequestBody GameCreateRequestDTO createRequest) {
        logUtil.logDebug("创建游戏 - 账号: " + createRequest.getAccount() + ", 游戏名: " + createRequest.getGameName());

        int result = vendorUserService.createGame(
                createRequest.getAccount(),
                createRequest.getGameName(),
                createRequest.getCategory(),
                createRequest.getPrice(),
                createRequest.getDescription(),
                createRequest.getDownloadLink(),
                createRequest.getLicenseNumber()
        );

        return switch (result) {
            case 0 -> {
                logUtil.logDebug("游戏创建成功 - 账号: " + createRequest.getAccount() + ", 游戏名: " + createRequest.getGameName());
                yield ResponseEntity.ok().body("游戏创建成功");
            }
            case -1 -> {
                logUtil.logWarning("游戏创建失败 - 厂商账号不存在: " + createRequest.getAccount());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            }
            case -2 -> {
                logUtil.logWarning("游戏创建失败 - 游戏名已存在: " + createRequest.getGameName());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body("游戏名已存在");
            }
            case -3 -> {
                logUtil.logWarning("游戏创建失败 - 版号已存在: " + createRequest.getLicenseNumber());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body("版号已存在");
            }
            case -4 -> {
                logUtil.logWarning("游戏创建失败 - 价格不能为负数: " + createRequest.getPrice());
                yield ResponseEntity.status(HttpStatus.BAD_REQUEST).body("价格不能为负数");
            }
            default -> {
                logUtil.logWarning("游戏创建失败 - 未知错误: " + result);
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏创建失败，请稍后重试");
            }
        };
    }

    /**
     * 厂商拥有游戏查询
     * POST /api/vendors/query-vendor-games
     */
    @PostMapping("/query-vendor-games")
    public ResponseEntity<?> queryVendorGames(@Valid @RequestBody VendorGameQueryRequestDTO queryRequest) {
        logUtil.logDebug("查询厂商游戏 - 账号: " + queryRequest.getAccount());

        Object result = vendorUserService.queryVendorGames(queryRequest.getAccount(), queryRequest.getStatus());
        
        if (result instanceof Integer) {
            int returnValue = (Integer) result;
            return switch (returnValue) {
                case -1 -> {
                    logUtil.logWarning("厂商游戏查询失败 - 厂商账号不存在: " + queryRequest.getAccount());
                    yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
                }
                case -99 -> {
                    logUtil.logError("厂商游戏查询失败 - 存储过程执行异常", null);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
                default -> {
                    logUtil.logWarning("厂商游戏查询失败 - 未知错误: " + returnValue);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
            };
        } else {
            // 返回查询结果
            logUtil.logDebug("厂商游戏查询成功 - 账号: " + queryRequest.getAccount());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 游戏具体信息查询
     * POST /api/vendors/query-game-info
     */
    @PostMapping("/query-game-info")
    public ResponseEntity<?> queryGameInfo(@Valid @RequestBody GameInfoQueryRequestDTO queryRequest) {
        logUtil.logDebug("查询游戏信息 - 游戏名: " + queryRequest.getGameName());

        Object result = vendorUserService.queryGameInfo(queryRequest.getGameName());
        
        if (result instanceof Integer) {
            int returnValue = (Integer) result;
            return switch (returnValue) {
                case -1 -> {
                    logUtil.logWarning("游戏信息查询失败 - 游戏不存在: " + queryRequest.getGameName());
                    yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在");
                }
                case -99 -> {
                    logUtil.logError("游戏信息查询失败 - 存储过程执行异常", null);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
                default -> {
                    logUtil.logWarning("游戏信息查询失败 - 未知错误: " + returnValue);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
            };
        } else {
            // 返回查询结果
            logUtil.logDebug("游戏信息查询成功 - 游戏名: " + queryRequest.getGameName());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 模糊查询游戏信息
     * POST /api/vendors/query-game-info-fuzzy
     */
    @PostMapping("/query-game-info-fuzzy")
    public ResponseEntity<?> queryGameInfoFuzzy(@Valid @RequestBody GameInfoFuzzyQueryRequestDTO queryRequest) {
        logUtil.logDebug("模糊查询游戏信息 - 关键词: " + queryRequest.getKeyword());

        Object result = vendorUserService.queryGameInfoFuzzy(queryRequest.getKeyword());
        
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
     * 修改游戏信息
     * PUT /api/vendors/update-game
     */
    @PutMapping("/update-game")
    public ResponseEntity<?> updateGame(@Valid @RequestBody GameUpdateRequestDTO updateRequest) {
        logUtil.logDebug("修改游戏信息 - 账号: " + updateRequest.getAccount() + ", 游戏名: " + updateRequest.getGameName());

        int result = vendorUserService.updateGame(
                updateRequest.getAccount(),
                updateRequest.getGameName(),
                updateRequest.getCategory(),
                updateRequest.getPrice(),
                updateRequest.getDescription(),
                updateRequest.getDownloadLink(),
                updateRequest.getLicenseNumber()
        );

        return switch (result) {
            case 0 -> {
                logUtil.logDebug("游戏信息修改成功 - 账号: " + updateRequest.getAccount() + ", 游戏名: " + updateRequest.getGameName());
                yield ResponseEntity.ok().body("游戏信息修改成功");
            }
            case -1 -> {
                logUtil.logWarning("游戏信息修改失败 - 厂商账号不存在: " + updateRequest.getAccount());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            }
            case -2 -> {
                logUtil.logWarning("游戏信息修改失败 - 游戏不存在或不属于该厂商: " + updateRequest.getGameName());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在或不属于该厂商");
            }
            case -3 -> {
                logUtil.logWarning("游戏信息修改失败 - 版号已存在: " + updateRequest.getLicenseNumber());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body("版号已存在");
            }
            case -4 -> {
                logUtil.logWarning("游戏信息修改失败 - 价格不能为负数: " + updateRequest.getPrice());
                yield ResponseEntity.status(HttpStatus.BAD_REQUEST).body("价格不能为负数");
            }
            default -> {
                logUtil.logWarning("游戏信息修改失败 - 未知错误: " + result);
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏信息修改失败，请稍后重试");
            }
        };
    }

    /**
     * 创建游戏上架申请
     * POST /api/vendors/create-game-application
     */
    @PostMapping("/create-game-application")
    public ResponseEntity<?> createGameApplication(@Valid @RequestBody GameApplicationRequestDTO applicationRequest) {
        logUtil.logDebug("创建游戏上架申请 - 账号: " + applicationRequest.getAccount() + ", 游戏名: " + applicationRequest.getGameName());

        int result = vendorUserService.createGameApplication(
                applicationRequest.getAccount(),
                applicationRequest.getGameName()
        );

        return switch (result) {
            case 0 -> {
                logUtil.logDebug("游戏上架申请创建成功 - 账号: " + applicationRequest.getAccount() + ", 游戏名: " + applicationRequest.getGameName());
                yield ResponseEntity.ok().body("游戏上架申请创建成功");
            }
            case -1 -> {
                logUtil.logWarning("游戏上架申请失败 - 厂商账号不存在: " + applicationRequest.getAccount());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            }
            case -2 -> {
                logUtil.logWarning("游戏上架申请失败 - 游戏不存在或不属于该厂商: " + applicationRequest.getGameName());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在或不属于该厂商");
            }
            case -3 -> {
                logUtil.logWarning("游戏上架申请失败 - 游戏已上架，无需重复申请: " + applicationRequest.getGameName());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body("游戏已上架，无需重复申请");
            }
            case -4 -> {
                logUtil.logWarning("游戏上架申请失败 - 该游戏已有待审批的申请: " + applicationRequest.getGameName());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body("该游戏已有待审批的申请");
            }
            default -> {
                logUtil.logWarning("游戏上架申请失败 - 未知错误: " + result);
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏上架申请失败，请稍后重试");
            }
        };
    }

    /**
     * 游戏下架（厂商）
     * PUT /api/vendors/game-off-shelf
     */
    @PostMapping("/game-off-shelf")
    public ResponseEntity<?> offShelfGame(@Valid @RequestBody GameOffShelfRequestDTO offShelfRequest) {
        logUtil.logDebug("游戏下架 - 账号: " + offShelfRequest.getAccount() + ", 游戏名: " + offShelfRequest.getGameName());

        int result = vendorUserService.offShelfGame(
                offShelfRequest.getAccount(),
                offShelfRequest.getGameName()
        );

        return switch (result) {
            case 0 -> {
                logUtil.logDebug("游戏下架成功 - 账号: " + offShelfRequest.getAccount() + ", 游戏名: " + offShelfRequest.getGameName());
                yield ResponseEntity.ok().body("游戏下架成功");
            }
            case -1 -> {
                logUtil.logWarning("游戏下架失败 - 厂商账号不存在: " + offShelfRequest.getAccount());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            }
            case -2 -> {
                logUtil.logWarning("游戏下架失败 - 游戏不存在或不属于该厂商: " + offShelfRequest.getGameName());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在或不属于该厂商");
            }
            case -3 -> {
                logUtil.logWarning("游戏下架失败 - 游戏已处于下架状态: " + offShelfRequest.getGameName());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body("游戏已处于下架状态，无需重复操作");
            }
            default -> {
                logUtil.logWarning("游戏下架失败 - 未知错误: " + result);
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏下架失败，请稍后重试");
            }
        };
    }

    /**
     * 厂商查询游戏上架申请
     * POST /api/vendors/query-game-applications
     */
    @PostMapping("/query-game-applications")
    public ResponseEntity<?> queryGameApplications(@Valid @RequestBody GameApplicationQueryRequestDTO queryRequest) {
        logUtil.logDebug("查询游戏上架申请 - 账号: " + queryRequest.getAccount() + ", 状态: " + queryRequest.getApprovalStatus());

        Object result = vendorUserService.queryGameApplications(
                queryRequest.getAccount(),
                queryRequest.getApprovalStatus()
        );

        if (result instanceof Integer) {
            int returnValue = (Integer) result;
            return switch (returnValue) {
                case -1 -> {
                    logUtil.logWarning("游戏上架申请查询失败 - 厂商账号不存在: " + queryRequest.getAccount());
                    yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
                }
                case -99 -> {
                    logUtil.logError("游戏上架申请查询失败 - 存储过程执行异常", null);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
                default -> {
                    logUtil.logWarning("游戏上架申请查询失败 - 未知错误: " + returnValue);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
            };
        } else {
            // 返回查询结果
            logUtil.logDebug("游戏上架申请查询成功 - 账号: " + queryRequest.getAccount());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 根据企业名查询游戏上架申请（调用 sp_query_applications_by_company 存储过程）
     * POST /api/vendors/query-applications-by-company
     */
    @PostMapping("/query-applications-by-company")
    public ResponseEntity<?> queryApplicationsByCompany(@Valid @RequestBody GameApplicationQueryRequestDTO queryRequest) {
        logUtil.logDebug("根据企业名查询游戏上架申请 - 账号: " + queryRequest.getAccount());

        Object result = vendorUserService.queryApplicationsByCompany(queryRequest.getAccount());
        
        if (result instanceof Integer) {
            int returnValue = (Integer) result;
            return switch (returnValue) {
                case -1 -> {
                    logUtil.logWarning("根据企业名查询游戏上架申请失败 - 厂商账号不存在: " + queryRequest.getAccount());
                    yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
                }
                case -99 -> {
                    logUtil.logError("根据企业名查询游戏上架申请失败 - 存储过程执行异常", null);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
                default -> {
                    logUtil.logWarning("根据企业名查询游戏上架申请失败 - 未知错误: " + returnValue);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
            };
        } else {
            // 返回查询结果
            logUtil.logDebug("根据企业名查询游戏上架申请成功 - 账号: " + queryRequest.getAccount());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 厂商取消游戏上架申请
     * DELETE /api/vendors/cancel-game-application
     */
    @DeleteMapping("/cancel-game-application")
    public ResponseEntity<?> cancelGameApplication(@Valid @RequestBody GameApplicationCancelRequestDTO cancelRequest) {
        logUtil.logDebug("取消游戏上架申请 - 账号: " + cancelRequest.getAccount() + ", 申请编号: " + cancelRequest.getApplicationId());

        int result = vendorUserService.cancelGameApplication(
                cancelRequest.getAccount(),
                cancelRequest.getApplicationId()
        );

        return switch (result) {
            case 0 -> {
                logUtil.logDebug("游戏上架申请取消成功 - 账号: " + cancelRequest.getAccount() + ", 申请编号: " + cancelRequest.getApplicationId());
                yield ResponseEntity.ok().body("游戏上架申请取消成功");
            }
            case -1 -> {
                logUtil.logWarning("游戏上架申请取消失败 - 厂商账号不存在: " + cancelRequest.getAccount());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
            }
            case -2 -> {
                logUtil.logWarning("游戏上架申请取消失败 - 申请不存在或不属于该厂商: " + cancelRequest.getApplicationId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("申请不存在或不属于该厂商");
            }
            case -3 -> {
                logUtil.logWarning("游戏上架申请取消失败 - 只能取消待审批的申请: " + cancelRequest.getApplicationId());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body("只能取消待审批的申请");
            }
            default -> {
                logUtil.logWarning("游戏上架申请取消失败 - 未知错误: " + result);
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("游戏上架申请取消失败，请稍后重试");
            }
        };
    }

    /**
     * 游戏销售数据查询
     * POST /api/vendors/query-game-sales
     */
    @PostMapping("/query-game-sales")
    public ResponseEntity<?> queryGameSales(@Valid @RequestBody GameSalesQueryRequestDTO queryRequest) {
        logUtil.logDebug("查询游戏销售数据 - 账号: " + queryRequest.getAccount());

        Object result = vendorUserService.queryGameSales(queryRequest.getAccount());
        
        if (result instanceof Integer) {
            int returnValue = (Integer) result;
            return switch (returnValue) {
                case -1 -> {
                    logUtil.logWarning("游戏销售数据查询失败 - 厂商账号不存在: " + queryRequest.getAccount());
                    yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
                }
                case -99 -> {
                    logUtil.logError("游戏销售数据查询失败 - 存储过程执行异常", null);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
                default -> {
                    logUtil.logWarning("游戏销售数据查询失败 - 未知错误: " + returnValue);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
            };
        } else {
            // 返回查询结果
            logUtil.logDebug("游戏销售数据查询成功 - 账号: " + queryRequest.getAccount());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 厂商游戏评价查询
     * POST /api/vendors/query-game-reviews
     */
    @PostMapping("/query-game-reviews")
    public ResponseEntity<?> queryGameReviews(@Valid @RequestBody GameReviewQueryRequestDTO queryRequest) {
        logUtil.logDebug("查询厂商游戏评价 - 账号: " + queryRequest.getAccount() + ", 游戏名: " + queryRequest.getGameName());

        Object result = vendorUserService.queryGameReviews(queryRequest.getAccount(), queryRequest.getGameName());
        
        if (result instanceof Integer) {
            int returnValue = (Integer) result;
            return switch (returnValue) {
                case -1 -> {
                    logUtil.logWarning("厂商游戏评价查询失败 - 厂商账号不存在: " + queryRequest.getAccount());
                    yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("厂商账号不存在或不是供应商角色");
                }
                case -2 -> {
                    logUtil.logWarning("厂商游戏评价查询失败 - 游戏不存在或不属于该厂商: " + queryRequest.getGameName());
                    yield ResponseEntity.status(HttpStatus.NOT_FOUND).body("游戏不存在或不属于该厂商");
                }
                case -99 -> {
                    logUtil.logError("厂商游戏评价查询失败 - 存储过程执行异常", null);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
                default -> {
                    logUtil.logWarning("厂商游戏评价查询失败 - 未知错误: " + returnValue);
                    yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("查询失败，请稍后重试");
                }
            };
        } else {
            // 返回查询结果
            logUtil.logDebug("厂商游戏评价查询成功 - 账号: " + queryRequest.getAccount() + ", 游戏名: " + queryRequest.getGameName());
            return ResponseEntity.ok(result);
        }
    }
}
