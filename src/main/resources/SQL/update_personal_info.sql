-- 创建修改个人信息存储过程（通用版本）
CREATE PROCEDURE sp_update_personal_info
    @account VARCHAR(50),
    @nickname VARCHAR(50) = NULL,
    @gender VARCHAR(10) = NULL,
    @birthdate DATE = NULL,
    @company_name VARCHAR(100) = NULL,
    @registered_address VARCHAR(200) = NULL,
    @contact_person VARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
BEGIN TRANSACTION;

        -- 首先获取用户角色
        DECLARE @role VARCHAR(20);

SELECT @role = role
FROM user_info
WHERE account = @account;

-- 检查用户是否存在
IF @role IS NULL
BEGIN
            RAISERROR('用户不存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -1;
END

        -- 根据角色修改对应的个人信息
        IF @role = 'buyer'
BEGIN
            -- 检查是否提供了买家相关的参数
            IF @company_name IS NOT NULL OR @registered_address IS NOT NULL OR @contact_person IS NOT NULL
BEGIN
                RAISERROR('买家不能修改企业相关信息', 16, 1);
ROLLBACK TRANSACTION;
RETURN -2;
END

            -- 检查买家信息是否存在
            IF NOT EXISTS (SELECT 1 FROM buyer_info WHERE account = @account)
BEGIN
                RAISERROR('买家信息不存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -3;
END

            -- 更新买家信息（只更新提供的字段）
UPDATE buyer_info
SET nickname = ISNULL(@nickname, nickname),
    gender = ISNULL(@gender, gender),
    birthdate = ISNULL(@birthdate, birthdate)
WHERE account = @account;

PRINT '买家个人信息修改成功';
COMMIT TRANSACTION;
RETURN 0;
END
ELSE IF @role = 'vendor'
BEGIN
            -- 检查是否提供了供应商相关的参数
            IF @nickname IS NOT NULL OR @gender IS NOT NULL OR @birthdate IS NOT NULL
BEGIN
                RAISERROR('供应商不能修改买家相关信息', 16, 1);
ROLLBACK TRANSACTION;
RETURN -2;
END

            -- 检查供应商信息是否存在
            IF NOT EXISTS (SELECT 1 FROM vendor_info WHERE account = @account)
BEGIN
                RAISERROR('供应商信息不存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -3;
END

            -- 更新供应商信息（只更新提供的字段）
UPDATE vendor_info
SET company_name = ISNULL(@company_name, company_name),
    registered_address = ISNULL(@registered_address, registered_address),
    contact_person = ISNULL(@contact_person, contact_person)
WHERE account = @account;

PRINT '供应商个人信息修改成功';
COMMIT TRANSACTION;
RETURN 0;
END
ELSE
BEGIN
            RAISERROR('未知用户角色: ' + @role, 16, 1);
ROLLBACK TRANSACTION;
RETURN -4;
END

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