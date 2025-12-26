-- 创建厂商游戏查询存储过程（基础版本）
CREATE PROCEDURE sp_query_vendor_games
    @account VARCHAR(50),
    @status VARCHAR(10) = '全部'
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

        -- 查询该厂商的所有游戏信息
IF @status = '全部'
BEGIN
    SELECT
        game_name AS 游戏名,
        category AS 游戏类别,
        price AS 价格,
        status As 状态,
        description AS 简介
    FROM game_info
    WHERE company_name = @company_name
    ORDER BY game_name;
END
ELSE
BEGIN
    SELECT
    game_name AS 游戏名,
    category AS 游戏类别,
    price AS 价格,
    status As 状态,
    description AS 简介
FROM game_info
WHERE company_name = @company_name and status = @status
ORDER BY game_name;
END
PRINT '厂商游戏查询成功';
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
