-- 创建游戏评价查询存储过程
CREATE PROCEDURE sp_query_game_reviews
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
            PRINT '厂商账号不存在或不是供应商角色';
RETURN -1;
END

        -- 检查游戏是否存在且属于该厂商
        IF NOT EXISTS (SELECT 1 FROM game_info WHERE game_name = @game_name AND company_name = @company_name)
BEGIN
            PRINT '游戏不存在或不属于该厂商';
RETURN -2;
END

        -- 查询游戏的所有评价信息
SELECT
    bgi.nickname AS 买家昵称,
    bgi.score AS 评分,
    bgi.comment AS 评论内容,
    bgi.review_time AS 评价时间
FROM buyer_game_info bgi
         INNER JOIN buyer_info bi ON bgi.nickname = bi.nickname
WHERE bgi.game_name = @game_name
ORDER BY bgi.review_time DESC

PRINT '游戏评价查询成功';
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
