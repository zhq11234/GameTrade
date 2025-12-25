-- 创建游戏信息查询存储过程
CREATE PROCEDURE sp_query_game_info
    @game_name VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRY
        -- 查询游戏详细信息
SELECT
    game_name AS 游戏名,
    category AS 游戏类别,
    price AS 价格,
    company_name AS 企业名,
    release_time AS 上线时间,
    description AS 游戏简介,
    status AS 状态,
    download_link AS 下载链接,
    license_number AS 版号
FROM game_info
WHERE game_name = @game_name;

-- 检查游戏是否存在
IF @@ROWCOUNT = 0
BEGIN
            PRINT '游戏不存在';
RETURN -1;
END

        PRINT '游戏信息查询成功';
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