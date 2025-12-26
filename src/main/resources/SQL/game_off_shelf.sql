-- 创建游戏下架存储过程
CREATE PROCEDURE sp_off_shelf_game
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

        -- 检查游戏当前状态
        DECLARE @current_status VARCHAR(20);
SELECT @current_status = status FROM game_info WHERE game_name = @game_name;

-- 如果游戏已经是下架状态，无需重复操作
IF @current_status = '下架'
BEGIN
            RAISERROR('游戏已处于下架状态，无需重复操作', 16, 1);
RETURN -3;
END

        -- 更新游戏状态为下架
UPDATE game_info
SET status = '下架'
WHERE game_name = @game_name AND company_name = @company_name;
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

