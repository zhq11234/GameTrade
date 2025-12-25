-- 创建游戏下架存储过程
CREATE PROCEDURE sp_off_shelf_game
    @account VARCHAR(50),
    @game_name VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
BEGIN TRANSACTION;

        -- 根据账号获取企业名
        DECLARE @company_name VARCHAR(100);

SELECT @company_name = company_name
FROM vendor_info
WHERE account = @account;

-- 检查厂商是否存在
IF @company_name IS NULL
BEGIN
            RAISERROR('厂商账号不存在或不是供应商角色', 16, 1);
ROLLBACK TRANSACTION;
RETURN -1;
END

        -- 检查游戏是否存在且属于该厂商
        IF NOT EXISTS (SELECT 1 FROM game_info WHERE game_name = @game_name AND company_name = @company_name)
BEGIN
            RAISERROR('游戏不存在或不属于该厂商', 16, 1);
ROLLBACK TRANSACTION;
RETURN -2;
END

        -- 检查游戏当前状态
        DECLARE @current_status VARCHAR(20);
SELECT @current_status = status FROM game_info WHERE game_name = @game_name;

-- 如果游戏已经是下架状态，无需重复操作
IF @current_status = '下架'
BEGIN
            RAISERROR('游戏已处于下架状态，无需重复操作', 16, 1);
ROLLBACK TRANSACTION;
RETURN -3;
END

        -- 更新游戏状态为下架
UPDATE game_info
SET status = '下架'
WHERE game_name = @game_name AND company_name = @company_name;

-- 返回下架后的游戏信息
SELECT
    game_name AS 游戏名,
    category AS 游戏类别,
    price AS 价格,
    company_name AS 企业名,
    status AS 状态,
    '下架成功' AS 操作结果
FROM game_info
WHERE game_name = @game_name;

PRINT '游戏下架成功';
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

