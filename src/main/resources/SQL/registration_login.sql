-- 创建买家注册存储过程
CREATE PROCEDURE sp_register_buyer
    @account VARCHAR(50),
    @password VARCHAR(100),
    @contact VARCHAR(100),
    @nickname VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
BEGIN TRANSACTION;
        
        -- 检查账号是否已存在
        IF EXISTS (SELECT 1 FROM user_info WHERE account = @account)
BEGIN
            RAISERROR('账号已存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -1;
END
        
        -- 检查联系方式是否已存在
        IF EXISTS (SELECT 1 FROM user_info WHERE contact = @contact)
BEGIN
            RAISERROR('联系方式已存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -2;
END
        
        -- 检查昵称是否已存在
        IF EXISTS (SELECT 1 FROM buyer_info WHERE nickname = @nickname)
BEGIN
            RAISERROR('昵称已存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -3;
END
        
        -- 密码加密（使用SHA2_256哈希）
        DECLARE @hashed_password VARBINARY(64);
        SET @hashed_password = HASHBYTES('SHA2_256', @password);
        
        -- 插入用户信息表
INSERT INTO user_info (account, role, password, contact, register_time)
VALUES (@account, 'buyer', CONVERT(VARCHAR(100), @hashed_password, 2), @contact, GETDATE());

-- 插入买家信息表
INSERT INTO buyer_info (nickname, account, gender, birthdate)
VALUES (@nickname, @account, '男', '2000-01-01');

COMMIT TRANSACTION;
PRINT '买家注册成功';
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

-- 创建供应商注册存储过程
CREATE PROCEDURE sp_register_vendor
    @account VARCHAR(50),
    @password VARCHAR(100),
    @contact VARCHAR(100),
    @company_name VARCHAR(100),
    @registered_address VARCHAR(200),
    @contact_person VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
BEGIN TRANSACTION;
        
        -- 检查账号是否已存在
        IF EXISTS (SELECT 1 FROM user_info WHERE account = @account)
BEGIN
            RAISERROR('账号已存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -1;
END
        
        -- 检查联系方式是否已存在
        IF EXISTS (SELECT 1 FROM user_info WHERE contact = @contact)
BEGIN
            RAISERROR('联系方式已存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -2;
END
        
        -- 检查企业名是否已存在
        IF EXISTS (SELECT 1 FROM vendor_info WHERE company_name = @company_name)
BEGIN
            RAISERROR('企业名已存在', 16, 1);
ROLLBACK TRANSACTION;
RETURN -3;
END
        
        -- 密码加密（使用SHA2_256哈希）
        DECLARE @hashed_password VARBINARY(64);
        SET @hashed_password = HASHBYTES('SHA2_256', @password);
        
        -- 插入用户信息表
INSERT INTO user_info (account, role, password, contact, register_time)
VALUES (@account, 'vendor', CONVERT(VARCHAR(100), @hashed_password, 2), @contact, GETDATE());

-- 插入供应商信息表
INSERT INTO vendor_info (company_name, account, registered_address, contact_person)
VALUES (@company_name, @account, @registered_address, @contact_person);

COMMIT TRANSACTION;
PRINT '供应商注册成功';
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

-- 创建登录验证存储过程
CREATE PROCEDURE sp_login_user
    @account VARCHAR(50),
    @password VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
        -- 查找用户
        DECLARE @stored_password VARCHAR(100);
        DECLARE @role VARCHAR(20);
        DECLARE @contact VARCHAR(100);
        DECLARE @register_time DATETIME2;

SELECT
    @stored_password = password,
    @role = role,
    @contact = contact,
    @register_time = register_time
FROM user_info
WHERE account = @account;

-- 检查用户是否存在
IF @stored_password IS NULL
BEGIN
            PRINT '账号不存在';
RETURN -1;
END
        
        -- 验证密码（比较哈希值）
        DECLARE @input_password_hash VARBINARY(64);
        SET @input_password_hash = HASHBYTES('SHA2_256', @password);
        
        IF @stored_password = CONVERT(VARCHAR(100), @input_password_hash, 2)
BEGIN
            -- 登录成功，返回用户信息
SELECT
    account,
    role,
    contact,
    register_time
FROM user_info
WHERE account = @account;

PRINT '登录成功';
RETURN 0;
END
ELSE
BEGIN
            PRINT '密码错误';
RETURN -2;
END

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

