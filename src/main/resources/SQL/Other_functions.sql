--13按游戏名称查询游戏
CREATE PROCEDURE sp_SearchGameByName
    @GameName VARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;  --禁止返回受影响的行数信息，

    -- 查询游戏信息，只返回上架游戏的游戏名、分类、价格三列
SELECT
    game_name AS GameName,
    category AS Category,
    price AS Price
FROM game_info
WHERE game_name LIKE '%' + @GameName + '%'
  AND status = '上架'  -- 只显示上架游戏
ORDER BY game_name;

END
GO

--EXEC sp_SearchGameByName ‘王者荣耀’

--14按游戏分类查询游戏
CREATE PROCEDURE sp_SearchGameByCategory
    @Category NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    -- 查询游戏信息，只返回上架游戏的游戏名、分类、价格三列
SELECT
    game_name AS GameName,
    category AS Category,
    price AS Price
FROM game_info
WHERE category LIKE '%' + @Category + '%'
  AND status = '上架'  -- 只显示上架游戏
ORDER BY category, game_name;

END
GO

-- EXEC sp_SearchGameByCategory '角色扮演'

--15按游戏热度查询游戏
CREATE PROCEDURE sp_SearchGameByPopularity
    @MinPopularity DECIMAL(10,2) = 0  -- 最小热度阈值，可选参数
AS
BEGIN
    SET NOCOUNT ON;

    -- 查询游戏信息，按热度排序，只返回上架游戏的游戏名、分类、价格三列
SELECT
    game_name AS GameName,
    category AS Category,
    price AS Price
FROM game_info
WHERE
  -- 热度计算：销量 * COALESCE(评分, 2.5) 如果评分为空，使用默认值2.5
    (sales_volume * COALESCE(score, 5.0)) >= @MinPopularity
  AND status = '上架'  -- 只显示上架游戏
ORDER BY
    -- 按热度降序排列（销量 * 评分）
    (sales_volume * COALESCE(score, 5.0)) DESC,
    game_name;

END
GO

-- EXEC sp_SearchGameByPopularity  -- 查询所有游戏按热度排序
-- EXEC sp_SearchGameByPopularity 1000  -- 查询热度大于10的游戏

--16按买家偏好查询游戏
CREATE PROCEDURE sp_SearchGameByBuyerPreference
    @BuyerNickname VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    -- 创建临时表存储类别统计
CREATE TABLE #CategoryStats (
                                category VARCHAR(50),
                                source_type VARCHAR(20),  -- 'library' 或 'browse'
                                count_value INT
);

-- 统计买家游戏库中的类别分布
INSERT INTO #CategoryStats (category, source_type, count_value)
SELECT
    gi.category,
    'library' AS source_type,
    COUNT(*) AS game_count
FROM buyer_game_info bgi
         INNER JOIN game_info gi ON bgi.game_name = gi.game_name
WHERE bgi.nickname = @BuyerNickname
GROUP BY gi.category;

-- 统计买家浏览历史中的类别分布（考虑浏览次数）
INSERT INTO #CategoryStats (category, source_type, count_value)
SELECT
    gi.category,
    'browse' AS source_type,
    SUM(bh.browse_count) AS total_browse_count  -- 考虑浏览次数权重
FROM browse_history bh
         INNER JOIN game_info gi ON bh.game_name = gi.game_name
WHERE bh.nickname = @BuyerNickname
GROUP BY gi.category;

-- 找出游戏库中最偏好的类别
DECLARE @LibraryPreference NVARCHAR(50);
SELECT TOP 1 @LibraryPreference = category
FROM #CategoryStats
WHERE source_type = 'library'
ORDER BY count_value DESC;

-- 找出浏览历史中最偏好的类别
DECLARE @BrowsePreference NVARCHAR(50);
SELECT TOP 1 @BrowsePreference = category
FROM #CategoryStats
WHERE source_type = 'browse'
ORDER BY count_value DESC;

-- 确定最终偏好类别（优先使用游戏库偏好，如果没有则使用浏览历史偏好）
DECLARE @FinalPreference NVARCHAR(50);
    IF @LibraryPreference IS NOT NULL
        SET @FinalPreference = @LibraryPreference;
ELSE IF @BrowsePreference IS NOT NULL
        SET @FinalPreference = @BrowsePreference;
ELSE
BEGIN
            -- 如果没有任何偏好数据，返回空结果
SELECT '暂无偏好数据，无法推荐游戏' AS Message;
RETURN;
END

    -- 查询偏好类别下的上架游戏，按热度排序（销量×评分）
SELECT
    gi.game_name AS GameName,
    gi.category AS Category,
    gi.price AS Price,
    gi.score AS Score,
    gi.sales_volume AS SalesVolume,
    gi.company_name AS CompanyName,
    -- 计算热度（销量×评分，评分为空时使用默认值5.0）
    CASE
        WHEN gi.score IS NOT NULL THEN gi.sales_volume * CAST(gi.score AS DECIMAL(4,2))
        ELSE gi.sales_volume * 5.0
        END AS Popularity
FROM game_info gi
WHERE gi.category = @FinalPreference
  AND gi.status = '上架'  -- 只查询上架的游戏
ORDER BY
    CASE
        WHEN gi.score IS NOT NULL THEN gi.sales_volume * CAST(gi.score AS DECIMAL(4,2))
        ELSE gi.sales_volume * 5.0
        END DESC;

-- 返回偏好分析结果（可选）
SELECT
    @FinalPreference AS PreferredCategory,
    @LibraryPreference AS LibraryPreference,
    @BrowsePreference AS BrowsePreference;

-- 清理临时表
DROP TABLE #CategoryStats;

END
GO

-- 使用示例:
-- EXEC sp_SearchGameByBuyerPreference '买家昵称'

--17游戏详细信息查询
CREATE PROCEDURE sp_GetGameDetails
    @GameName NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

BEGIN TRANSACTION;

BEGIN TRY
        -- 检查游戏是否存在且已上架
IF NOT EXISTS (SELECT 1 FROM game_info WHERE game_name = @GameName AND status = '上架')
BEGIN
            RAISERROR('游戏不存在或已下架: %s', 16, 1, @GameName);
ROLLBACK TRANSACTION;
RETURN;
END

        -- 更新游戏的访问量（加一）
UPDATE game_info
SET visitor_count = visitor_count + 1
WHERE game_name = @GameName AND status = '上架';

-- 查询游戏的详细信息
SELECT
    game_name AS GameName,
    category AS Category,
    price AS Price,
    company_name AS CompanyName,
    release_time AS ReleaseTime,
    description AS Description,
    status AS Status,
    download_link AS DownloadLink,
    license_number AS LicenseNumber,
    score AS Score,
    sales_volume AS SalesVolume,
    visitor_count AS VisitorCount
FROM game_info
WHERE game_name = @GameName AND status = '上架';

COMMIT TRANSACTION;
END TRY
BEGIN CATCH
ROLLBACK TRANSACTION;

        -- 返回错误信息
SELECT
    ERROR_NUMBER() AS ErrorNumber,
    ERROR_MESSAGE() AS ErrorMessage;
END CATCH

END
GO

-- EXEC sp_GetGameDetails '游戏名'

--18游戏评价查询
CREATE PROCEDURE sp_GetGameReviews
    @GameName NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

    -- 查询游戏的所有评价信息
SELECT
    bgi.nickname AS BuyerNickname,
    bgi.score AS Score,
    bgi.comment AS Comment,
    bgi.review_time AS ReviewTime
FROM buyer_game_info bgi
WHERE bgi.game_name = @GameName
ORDER BY bgi.review_time DESC;  -- 按评价时间降序排列

END
GO
-- EXEC sp_GetGameReviews '游戏名'

--19进行游戏评价
CREATE PROCEDURE sp_SubmitGameReview
    @BuyerNickname NVARCHAR(50),
    @GameName NVARCHAR(100),
    @Score DECIMAL(2,1),  -- 评分（0.0-10.0）
    @Comment NVARCHAR(200) = NULL  -- 评论（可选）
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;  -- 发生错误时自动回滚事务

BEGIN TRANSACTION;

BEGIN TRY
        -- 检查游戏是否存在
IF NOT EXISTS (SELECT 1 FROM game_info WHERE game_name = @GameName)
BEGIN
                RAISERROR('游戏不存在: %s', 16, 1, @GameName);
                RETURN;
END

        -- 检查买家是否存在
        IF NOT EXISTS (SELECT 1 FROM buyer_info WHERE nickname = @BuyerNickname)
BEGIN
                RAISERROR('买家不存在: %s', 16, 1, @BuyerNickname);
                RETURN;
END

        -- 检查买家是否购买过该游戏（存在已支付的订单）
        IF NOT EXISTS (
            SELECT 1 FROM order_info
            WHERE nickname = @BuyerNickname
            AND game_name = @GameName
            AND order_status = '已支付'
        )
BEGIN
                RAISERROR('您尚未购买该游戏，无法进行评价: %s', 16, 1, @GameName);
                RETURN;
END

        -- 检查评分范围（0.0-10.0）
        IF @Score < 0.0 OR @Score > 10.0
BEGIN
                RAISERROR('评分必须在0.0到10.0之间', 16, 1);
                RETURN;
END

        -- 检查是否已经购买并拥有该游戏（buyer_game_info表中应该有记录）
        IF NOT EXISTS (SELECT 1 FROM buyer_game_info WHERE nickname = @BuyerNickname AND game_name = @GameName)
BEGIN
                RAISERROR('您尚未购买该游戏，无法进行评价: %s', 16, 1, @GameName);
                RETURN;
END

        -- 更新现有评价
UPDATE buyer_game_info
SET
    score = @Score,
    comment = @Comment,
    review_time = GETDATE()
WHERE nickname = @BuyerNickname AND game_name = @GameName;

-- 更新游戏的评分平均值
UPDATE game_info
SET score = (
    SELECT AVG(CAST(score AS DECIMAL(4,2)))
    FROM buyer_game_info
    WHERE game_name = @GameName
)
WHERE game_name = @GameName;

-- 返回成功消息
SELECT '评价提交成功' AS Result;

COMMIT TRANSACTION;
END TRY
BEGIN CATCH
ROLLBACK TRANSACTION;

        -- 返回错误信息
SELECT
    ERROR_NUMBER() AS ErrorNumber,
    ERROR_MESSAGE() AS ErrorMessage;
END CATCH

END
GO
-- EXEC sp_SubmitGameReview '买家昵称', '游戏名', 8.5, '很好的游戏'

--20游戏库查询
CREATE PROCEDURE sp_GetBuyerGameLibrary
    @BuyerNickname NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    -- 直接从买家游戏表查询游戏库信息
SELECT
    bgi.game_name AS GameName,
    bgi.license_number AS LicenseNumber
FROM buyer_game_info bgi
WHERE bgi.nickname = @BuyerNickname
ORDER BY bgi.game_name;

END
GO
-- EXEC sp_GetBuyerGameLibrary '买家昵称'

--21游戏下载
CREATE PROCEDURE sp_GetGameDownloadLink
    @BuyerNickname NVARCHAR(50),
    @GameName NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;

    -- 检查游戏是否在买家游戏库中
    IF NOT EXISTS (
        SELECT 1
        FROM buyer_game_info bgi
        WHERE bgi.nickname = @BuyerNickname
          AND bgi.game_name = @GameName
    )
BEGIN
            RAISERROR('游戏不在您的游戏库中: %s', 16, 1, @GameName);
            RETURN;
END

    -- 查询游戏的下载链接
SELECT
    gi.download_link AS DownloadLink,
    gi.game_name AS GameName,
    gi.license_number AS LicenseNumber,
    gi.description AS Description
FROM game_info gi
WHERE gi.game_name = @GameName;

END
GO

-- EXEC sp_GetGameDownloadLink '买家昵称', '游戏名'
--22游戏更新
CREATE TABLE game_update_reminders (
                                       reminder_id INT IDENTITY(1,1) PRIMARY KEY,
                                       buyer_nickname NVARCHAR(50) NOT NULL,
                                       game_name NVARCHAR(100) NOT NULL,
                                       old_version NVARCHAR(50),
                                       new_version NVARCHAR(50) NOT NULL,
                                       reminder_time DATETIME2 DEFAULT GETDATE(),
                                       reminder_message NVARCHAR(500),
                                       is_read BIT DEFAULT 0,  -- 是否已读
                                       read_time DATETIME2      -- 阅读时间
);

-- 创建触发器
CREATE TRIGGER tr_GameUpdateReminder
    ON game_info
    AFTER UPDATE
              AS
BEGIN
    SET NOCOUNT ON;

    -- 只处理license_number字段的更新
    IF UPDATE(license_number)
BEGIN
            -- 遍历所有更新的游戏记录
INSERT INTO game_update_reminders (
    buyer_nickname,
    game_name,
    old_version,
    new_version,
    reminder_message
)
SELECT DISTINCT
    bgi.nickname,
    i.game_name,
    d.license_number AS old_version,
    i.license_number AS new_version,
    '亲爱的 ' + bgi.nickname + '，您拥有的游戏 "' + i.game_name +
    '" 有新版本发布！当前版本: ' + COALESCE(d.license_number, '未知') +
    ' → 新版本: ' + i.license_number +
    '。请及时更新以获得更好的游戏体验。' AS reminder_message
FROM inserted i
         INNER JOIN deleted d ON i.game_name = d.game_name
         INNER JOIN buyer_game_info bgi ON i.game_name = bgi.game_name
-- 只有当新版号比旧版号"更高"时才发送提醒
WHERE i.license_number > d.license_number
   OR d.license_number IS NULL;  -- 如果之前没有版号，现在有了也提醒

-- 可选：记录到系统日志
PRINT '检测到游戏版号更新，已为相关买家创建更新提醒';
END
END
GO

--23生成订单
CREATE PROCEDURE sp_CreateOrder
    @BuyerNickname NVARCHAR(50),
    @GameName NVARCHAR(100)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;  -- 发生错误时自动回滚事务

BEGIN TRANSACTION;

BEGIN TRY
        -- 检查买家是否存在
IF NOT EXISTS (SELECT 1 FROM buyer_info WHERE nickname = @BuyerNickname)
BEGIN
                RAISERROR('买家不存在: %s', 16, 1, @BuyerNickname);
                RETURN;
END

        -- 检查游戏是否存在
        IF NOT EXISTS (SELECT 1 FROM game_info WHERE game_name = @GameName)
BEGIN
                RAISERROR('游戏不存在: %s', 16, 1, @GameName);
                RETURN;
END

        -- 检查游戏是否已上架
        IF NOT EXISTS (SELECT 1 FROM game_info WHERE game_name = @GameName AND status = '上架')
BEGIN
                RAISERROR('游戏未上架或已下架: %s', 16, 1, @GameName);
                RETURN;
END

        -- 生成订单ID（格式：ORD + 时间戳 + 随机数）
        DECLARE @OrderId NVARCHAR(50);
        SET @OrderId = 'ORD' + REPLACE(CONVERT(NVARCHAR(20), GETDATE(), 112), '-', '') +
                       RIGHT('00000' + CAST(ABS(CHECKSUM(NEWID())) % 100000 AS NVARCHAR(5)), 5);

        -- 检查订单ID是否唯一
        WHILE EXISTS (SELECT 1 FROM order_info WHERE order_id = @OrderId)
BEGIN
                SET @OrderId = 'ORD' + REPLACE(CONVERT(NVARCHAR(20), GETDATE(), 112), '-', '') +
                               RIGHT('00000' + CAST(ABS(CHECKSUM(NEWID())) % 100000 AS NVARCHAR(5)), 5);
END

        -- 获取游戏信息
        DECLARE @Category NVARCHAR(50);
        DECLARE @Price DECIMAL(10,2);
        DECLARE @LicenseNumber NVARCHAR(50);

SELECT
    @Category = category,
    @Price = price,
    @LicenseNumber = license_number
FROM game_info
WHERE game_name = @GameName;

-- 插入订单信息
INSERT INTO order_info (
    order_id,
    nickname,
    game_name,
    category,
    price,
    order_time,
    payment_time,
    order_status
)
VALUES (
           @OrderId,
           @BuyerNickname,
           @GameName,
           @Category,
           @Price,
           GETDATE(),  -- 订单时间为系统时间
           NULL,       -- 支付时间默认为空
           '待支付',   -- 订单状态初始为待支付
           @LicenseNumber
       );

-- 返回生成的订单信息
SELECT
    @OrderId AS OrderId,
    @BuyerNickname AS BuyerNickname,
    @GameName AS GameName,
    @Category AS Category,
    @Price AS Price,
    GETDATE() AS OrderTime,
    NULL AS PaymentTime,
    '待支付' AS OrderStatus;

COMMIT TRANSACTION;
END TRY
BEGIN CATCH
ROLLBACK TRANSACTION;

        -- 返回错误信息
SELECT
    ERROR_NUMBER() AS ErrorNumber,
    ERROR_MESSAGE() AS ErrorMessage;
END CATCH

END
GO

-- EXEC sp_CreateOrder '买家昵称', '游戏名'

--24订单查询
CREATE PROCEDURE sp_GetBuyerOrders
    @BuyerNickname NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    -- 查询买家的所有订单信息
SELECT
    order_id AS OrderId,
    nickname AS BuyerNickname,
    game_name AS GameName,
    category AS Category,
    price AS Price,
    order_time AS OrderTime,
    payment_time AS PaymentTime,
    order_status AS OrderStatus
FROM order_info
WHERE nickname = @BuyerNickname
ORDER BY order_time DESC;  -- 按订单时间降序排列

END
GO

-- EXEC sp_GetBuyerOrders '买家昵称'

--25订单支付
CREATE PROCEDURE sp_PayOrder
    @OrderId NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;  -- 发生错误时自动回滚事务

BEGIN TRANSACTION;

BEGIN TRY
        -- 检查订单是否存在且状态为待支付
IF NOT EXISTS (SELECT 1 FROM order_info WHERE order_id = @OrderId)
BEGIN
                RAISERROR('订单不存在: %s', 16, 1, @OrderId);
                RETURN;
END

        -- 检查订单是否已经是已支付状态
        IF EXISTS (SELECT 1 FROM order_info WHERE order_id = @OrderId AND order_status = '已支付')
BEGIN
                RAISERROR('订单已支付: %s', 16, 1, @OrderId);
                RETURN;
END

        -- 检查订单状态是否为待支付
        IF NOT EXISTS (SELECT 1 FROM order_info WHERE order_id = @OrderId AND order_status = '待支付')
BEGIN
                RAISERROR('订单状态不正确，无法支付: %s', 16, 1, @OrderId);
                RETURN;
END

        -- 更新订单状态和支付时间
UPDATE order_info
SET
    order_status = '已支付',
    payment_time = GETDATE()
WHERE order_id = @OrderId;

-- 返回支付成功信息
SELECT '订单支付成功' AS Result;

COMMIT TRANSACTION;
END TRY
BEGIN CATCH
ROLLBACK TRANSACTION;

        -- 返回错误信息
SELECT
    ERROR_NUMBER() AS ErrorNumber,
    ERROR_MESSAGE() AS ErrorMessage;
END CATCH

END
GO

-- EXEC sp_PayOrder 'ORD20241225000001'
-- 订单支付触发器
-- 触发器名: tr_OrderPayment
-- 功能: 当订单状态更新为已支付时，自动更新游戏销量并将游戏加入买家游戏库

CREATE TRIGGER tr_OrderPayment
    ON order_info
    AFTER UPDATE
              AS
BEGIN
    SET NOCOUNT ON;
    -- 只处理order_status字段的更新，且状态变为'已支付'
    IF UPDATE(order_status)
BEGIN
            -- 遍历所有更新的订单记录
INSERT INTO buyer_game_info (
    nickname,
    game_name,
    license_number,
    score,
    comment,
    review_time
)
SELECT
    i.nickname,
    i.game_name,

    NULL,  -- 评分初始为空
    NULL,  -- 评论初始为空
    NULL   -- 评价时间初始为空
FROM inserted i
         INNER JOIN deleted d ON i.order_id = d.order_id
-- 只有当订单状态从非'已支付'变为'已支付'时才处理
WHERE i.order_status = '已支付'
  AND d.order_status != '已支付'
              -- 检查是否已经存在于买家游戏库中（避免重复插入）
              AND NOT EXISTS (
                SELECT 1
                FROM buyer_game_info bgi
                WHERE bgi.nickname = i.nickname AND bgi.game_name = i.game_name
            );

-- 更新游戏销量（加一），只更新上架游戏的销量
UPDATE gi
SET sales_volume = sales_volume + 1
    FROM game_info gi
                     INNER JOIN inserted i ON gi.game_name = i.game_name
    INNER JOIN deleted d ON i.order_id = d.order_id
WHERE i.order_status = '已支付'
  AND d.order_status != '已支付'
  AND gi.status = '上架';

-- 可选：记录支付成功日志
PRINT '订单支付成功，已更新游戏销量并添加到买家游戏库';
END
END
GO

--26取消订单

CREATE PROCEDURE sp_CancelOrder
    @OrderId VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;  -- 发生错误时自动回滚事务

BEGIN TRANSACTION;

BEGIN TRY
        -- 检查订单是否存在
IF NOT EXISTS (SELECT 1 FROM order_info WHERE order_id = @OrderId)
BEGIN
                RAISERROR('订单不存在: %s', 16, 1, @OrderId);
                RETURN;
END

        -- 检查订单是否已经是已取消状态
        IF EXISTS (SELECT 1 FROM order_info WHERE order_id = @OrderId AND order_status = '已取消')
BEGIN
                RAISERROR('订单已取消: %s', 16, 1, @OrderId);
                RETURN;
END

        -- 检查订单是否已支付（已支付的订单可能不允许取消）
        IF EXISTS (SELECT 1 FROM order_info WHERE order_id = @OrderId AND order_status = '已支付')
BEGIN
                RAISERROR('订单已支付，无法取消: %s', 16, 1, @OrderId);
                RETURN;
END

        -- 检查订单是否已完成
        IF EXISTS (SELECT 1 FROM order_info WHERE order_id = @OrderId AND order_status = '已完成')
BEGIN
                RAISERROR('订单已完成，无法取消: %s', 16, 1, @OrderId);
                RETURN;
END

        -- 更新订单状态为已取消
UPDATE order_info
SET order_status = '已取消'
WHERE order_id = @OrderId;

-- 返回取消成功信息
SELECT '订单取消成功' AS Result;

COMMIT TRANSACTION;
END TRY
BEGIN CATCH
ROLLBACK TRANSACTION;

        -- 返回错误信息
SELECT
    ERROR_NUMBER() AS ErrorNumber,
    ERROR_MESSAGE() AS ErrorMessage;
END CATCH

END
GO