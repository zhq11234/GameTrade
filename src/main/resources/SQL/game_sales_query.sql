-- 创建游戏销售数据查询存储过程
CREATE PROCEDURE sp_query_game_sales_data
    @account VARCHAR(50)
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

        -- 查询厂商所有游戏的销售数据
SELECT
    game_name                                                  AS 游戏名,
    category                                                   AS 游戏类别,
    price                                                      AS 价格,
    sales_volume                                               AS 销量,
    visitor_count                                              AS 访客数,
    -- 计算销售额：销量 × 价格
    CAST(sales_volume * price AS DECIMAL(15,2))                AS 销售额,
    -- 计算转化率：销量 ÷ 访客数 × 100%，处理除零情况
    IIF(visitor_count > 0, CAST((CAST(sales_volume AS DECIMAL(15, 4)) / CAST(visitor_count AS DECIMAL(15, 4)) *
                                 100) AS DECIMAL(5, 2)), 0.00) AS 转化率百分比,
    status                                                     AS 状态
FROM game_info
WHERE company_name = @company_name
ORDER BY sales_volume DESC, visitor_count DESC;

PRINT '游戏销售数据查询成功';
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