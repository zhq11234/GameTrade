-- 创建游戏创建存储过程
CREATE PROCEDURE sp_create_game
    @account VARCHAR(50),
    @game_name VARCHAR(100),
    @category VARCHAR(50),
    @price DECIMAL(10,2),
    @description VARCHAR(500),
    @download_link VARCHAR(255),
    @license_number VARCHAR(50)
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

        -- 检查游戏名是否已存在
        IF EXISTS (SELECT 1 FROM game_info WHERE game_name = @game_name)
BEGIN
            RAISERROR('游戏名已存在', 16, 1);
RETURN -2;
END

        -- 检查版号是否已存在
        IF EXISTS (SELECT 1 FROM game_info WHERE license_number = @license_number)
BEGIN
            RAISERROR('版号已存在', 16, 1);
RETURN -3;
END

        -- 检查价格是否为负数
        IF @price < 0
BEGIN
            RAISERROR('价格不能为负数', 16, 1);
RETURN -4;
END

        -- 插入游戏信息
INSERT INTO game_info (
    game_name,
    category,
    price,
    company_name,
    description,
    download_link,
    license_number,
    status,
    sales_volume,
    visitor_count
) VALUES (
             @game_name,
             @category,
             @price,
             @company_name,
             @description,
             @download_link,
             @license_number,
             '下架',  -- 初始状态
             0,       -- 初始销量
             0        -- 初始访问量
         );

PRINT '游戏创建成功';
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