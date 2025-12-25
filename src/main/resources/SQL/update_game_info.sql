-- 创建游戏信息修改存储过程
CREATE PROCEDURE sp_update_game_info
    @account VARCHAR(50),
    @game_name VARCHAR(100),
    @price DECIMAL(10,2) = NULL,
    @description VARCHAR(500) = NULL,
    @license_number VARCHAR(50) = NULL,
    @download_link VARCHAR(255) = NULL
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

        -- 检查是否至少提供了一个要修改的字段
        IF @price IS NULL AND @description IS NULL AND @license_number IS NULL AND @download_link IS NULL
BEGIN
            RAISERROR('至少需要提供一个要修改的字段', 16, 1);
ROLLBACK TRANSACTION;
RETURN -3;
END

        -- 检查价格是否为负数
        IF @price IS NOT NULL AND @price < 0
BEGIN
            RAISERROR('价格不能为负数', 16, 1);
ROLLBACK TRANSACTION;
RETURN -4;
END

        -- 检查版号是否已存在（排除当前游戏）
        IF @license_number IS NOT NULL AND EXISTS (
            SELECT 1 FROM game_info
            WHERE license_number = @license_number AND game_name != @game_name
        )
BEGIN
            RAISERROR('版号已存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -5;
END

        -- 更新游戏信息（只更新提供的字段）
UPDATE game_info
SET price = ISNULL(@price, price),
    description = ISNULL(@description, description),
    license_number = ISNULL(@license_number, license_number),
    download_link = ISNULL(@download_link, download_link)
WHERE game_name = @game_name AND company_name = @company_name;

PRINT '游戏信息修改成功';
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