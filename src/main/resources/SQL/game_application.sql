-- 创建游戏上架申请存储过程
CREATE PROCEDURE sp_create_game_application
    @account VARCHAR(50),
    @game_name VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
        -- 根据账号获取企业名
        DECLARE @company_name VARCHAR(100);

SELECT @company_name = company_name
FROM vendor_info
WHERE account = @account;

-- 检查厂商是否存在
IF @company_name IS NULL
BEGIN
            RAISERROR('厂商账号不存在或不是供应商角色', 16, 1);
RETURN -1;
END

        -- 检查游戏是否存在且属于该厂商
        IF NOT EXISTS (SELECT 1 FROM game_info WHERE game_name = @game_name AND company_name = @company_name)
BEGIN
            RAISERROR('游戏不存在或不属于该厂商', 16, 1);
RETURN -2;
END

        -- 检查游戏是否已上架
        DECLARE @current_status VARCHAR(20);
SELECT @current_status = status FROM game_info WHERE game_name = @game_name;

IF @current_status = '上架'
BEGIN
            RAISERROR('游戏已上架，无需重复申请', 16, 1);
RETURN -3;
END

        -- 检查是否已存在待审批的申请
        IF EXISTS (
            SELECT 1 FROM game_application
            WHERE game_name = @game_name AND company_name = @company_name AND approval_status = '待审批'
        )
BEGIN
            RAISERROR('该游戏已有待审批的申请', 16, 1);
RETURN -4;
END

        -- 插入游戏上架申请（application_id由数据库自动生成）
        DECLARE @application_id INT;

INSERT INTO game_application (
    game_name,
    company_name,
    approval_status,
    approval_result,
    application_time
) VALUES (
             @game_name,
             @company_name,
             '待审批',  -- 初始状态
             NULL,      -- 初始审批结果为空
             GETDATE()  -- 申请时间
         );

-- 获取生成的申请编号
SELECT @application_id = SCOPE_IDENTITY();

PRINT '游戏上架申请提交成功';
RETURN 0;

END TRY
BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();

        RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
RETURN -99;
END CATCH
END;
GO

-- 创建按状态查询游戏上架申请存储过程（替代原有的两个查询存储过程）
CREATE PROCEDURE sp_query_applications_by_status
    @account VARCHAR(50),
    @approval_status VARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
        -- 根据账号获取企业名
        DECLARE @company_name VARCHAR(100);

SELECT @company_name = company_name
FROM vendor_info
WHERE account = @account;

-- 检查厂商是否存在
IF @company_name IS NULL
BEGIN
            PRINT '厂商账号不存在或不是供应商角色';
RETURN -1;
END

        -- 查询游戏上架申请（可按状态筛选）
        IF @approval_status IS NULL
BEGIN
            -- 查询所有申请
SELECT
    application_id AS 申请编号,
    game_name AS 游戏名,
    company_name AS 企业名,
    approval_status AS 审批状态,
    approval_result AS 审批结果,
    application_time AS 申请时间
FROM game_application
WHERE company_name = @company_name
ORDER BY application_time DESC;
END
ELSE
BEGIN
            -- 按指定状态查询申请
SELECT
    application_id AS 申请编号,
    game_name AS 游戏名,
    company_name AS 企业名,
    approval_status AS 审批状态,
    approval_result AS 审批结果,
    application_time AS 申请时间
FROM game_application
WHERE company_name = @company_name AND approval_status = @approval_status
ORDER BY application_time DESC;
END

        PRINT '游戏上架申请查询成功';
RETURN 0;

END TRY
BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();

        RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
RETURN -99;
END CATCH
END;
GO


-- 创建取消游戏上架申请存储过程
CREATE PROCEDURE sp_cancel_game_application
    @account VARCHAR(50),
    @application_id INT
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
        -- 根据账号获取企业名
        DECLARE @company_name VARCHAR(100);

SELECT @company_name = company_name
FROM vendor_info
WHERE account = @account;

-- 检查厂商是否存在
IF @company_name IS NULL
BEGIN
            RAISERROR('厂商账号不存在或不是供应商角色', 16, 1);
RETURN -1;
END

        -- 检查申请是否存在且属于该厂商
        IF NOT EXISTS (
            SELECT 1 FROM game_application
            WHERE application_id = @application_id AND company_name = @company_name
        )
BEGIN
            RAISERROR('申请不存在或不属于该厂商', 16, 1);
RETURN -2;
END

        -- 检查申请状态是否为待审批
        DECLARE @current_status VARCHAR(20);

SELECT @current_status = approval_status
FROM game_application
WHERE application_id = @application_id;

IF @current_status != '待审批'
BEGIN
            RAISERROR('只能取消待审批的申请', 16, 1);
RETURN -3;
END

        -- 删除申请记录
DELETE FROM game_application
WHERE application_id = @application_id AND company_name = @company_name;

PRINT '游戏上架申请取消成功';
RETURN 0;

END TRY
BEGIN CATCH
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();

        RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
RETURN -99;
END CATCH
END;
GO