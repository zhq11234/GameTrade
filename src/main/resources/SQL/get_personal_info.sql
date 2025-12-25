-- 创建查看个人信息存储过程
CREATE PROCEDURE sp_get_personal_info
    @account VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
        -- 首先获取用户基本信息和角色
        DECLARE @role VARCHAR(20);
        DECLARE @contact VARCHAR(100);
        DECLARE @register_time DATETIME2;

SELECT
    @role = role,
    @contact = contact,
    @register_time = register_time
FROM user_info
WHERE account = @account;

-- 检查用户是否存在
IF @role IS NULL
BEGIN
            PRINT '用户不存在';
RETURN -1;
END

        -- 根据角色查询对应的个人信息
        IF @role = 'buyer'
BEGIN
            -- 查询买家详细信息（只返回指定字段）
SELECT
    b.account,
    b.nickname,
    b.gender,
    b.birthdate
FROM buyer_info b
WHERE b.account = @account;

PRINT '买家个人信息查询成功';
RETURN 0;
END
ELSE IF @role = 'vendor'
BEGIN
            -- 查询供应商详细信息（只返回指定字段）
SELECT
    v.account,
    v.company_name,
    v.registered_address,
    v.contact_person
FROM vendor_info v
WHERE v.account = @account;

PRINT '供应商个人信息查询成功';
RETURN 0;
END
ELSE
BEGIN
            PRINT '未知用户角色: ' + @role;
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
