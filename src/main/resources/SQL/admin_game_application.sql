
-- 创建查询所有游戏上架申请存储过程
CREATE PROCEDURE sp_query_all_game_applications
    AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
        -- 查询所有游戏上架申请
SELECT
    application_id AS 申请编号,
    company_name AS 企业名,
    game_name AS 游戏名,
    application_time AS 申请时间,
    approval_status AS 审批状态
FROM game_application
ORDER BY application_time DESC;

PRINT '所有游戏上架申请查询成功';
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

-- 创建申请详细信息查询存储过程
CREATE PROCEDURE sp_query_application_details
    @application_id INT
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
        -- 检查申请是否存在
IF NOT EXISTS (SELECT 1 FROM game_application WHERE application_id = @application_id)
BEGIN
            PRINT '申请编号不存在';
RETURN -1;
END
        
        -- 查询申请详细信息
SELECT
    ga.application_id AS 申请编号,
    ga.company_name AS 企业名,
    ga.game_name AS 游戏名,
    ga.application_time AS 申请时间,
    ga.approval_status AS 审批状态,
    ga.approval_result AS 审批结果,
    -- 关联游戏信息
    gi.category AS 游戏类别,
    gi.price AS 价格,
    gi.description AS 游戏简介,
    gi.license_number AS 版号,
    gi.status AS 游戏状态,
    -- 关联企业信息
    vi.registered_address AS 注册地址,
    vi.contact_person AS 联系人
FROM game_application ga
         LEFT JOIN game_info gi ON ga.game_name = gi.game_name AND ga.company_name = gi.company_name
         LEFT JOIN vendor_info vi ON ga.company_name = vi.company_name
WHERE ga.application_id = @application_id;

PRINT '申请详细信息查询成功';
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

-- 创建游戏上架申请审批存储过程
CREATE PROCEDURE sp_approve_game_application
    @application_id INT,
    @approval_status VARCHAR(20),  -- 新的审批状态（通过、拒绝）
    @approval_result VARCHAR(500) = NULL  -- 审批结果说明
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
BEGIN TRANSACTION;
        
        -- 检查申请是否存在
        IF NOT EXISTS (SELECT 1 FROM game_application WHERE application_id = @application_id)
BEGIN
            RAISERROR('申请编号不存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -1;
END
        
        -- 检查申请当前状态是否为待审批
        DECLARE @current_status VARCHAR(20);
        DECLARE @game_name VARCHAR(100);
        DECLARE @company_name VARCHAR(100);

SELECT @current_status = approval_status,
       @game_name = game_name,
       @company_name = company_name
FROM game_application
WHERE application_id = @application_id;

IF @current_status != '待审批'
BEGIN
            RAISERROR('只能审批待审批状态的申请', 16, 1);
ROLLBACK TRANSACTION;
RETURN -2;
END
        
        -- 验证审批状态的有效性
        IF @approval_status NOT IN ('通过', '拒绝')
BEGIN
            RAISERROR('审批状态无效，只能为"通过"或"拒绝"', 16, 1);
ROLLBACK TRANSACTION;
RETURN -3;
END
        
        -- 更新申请信息
UPDATE game_application
SET approval_status = @approval_status,
    approval_result = ISNULL(@approval_result, approval_result)
WHERE application_id = @application_id;

-- 如果审批通过，更新游戏信息
IF @approval_status = '通过'
BEGIN
            -- 更新现有游戏状态和上线时间
UPDATE game_info
SET status = '上架',
    release_time = GETDATE()
WHERE game_name = @game_name AND company_name = @company_name;
END
        
        PRINT '游戏上架申请审批成功';
COMMIT TRANSACTION;
RETURN 0;

END TRY
BEGIN CATCH
IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        
        DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
        DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
        DECLARE @ErrorState INT = ERROR_STATE();
        
        RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
RETURN -99;
END CATCH
END;
GO