-- 创建用户信息表（基础表）
CREATE TABLE user_info (
                           account VARCHAR(50) NOT NULL,
                           role VARCHAR(20) NOT NULL,
                           password VARCHAR(100) NOT NULL,
                           contact VARCHAR(100) NOT NULL,
                           register_time DATETIME2 NOT NULL,
                           CONSTRAINT pk_user_info PRIMARY KEY (account),
                           CONSTRAINT uk_user_info_account UNIQUE (account),
                           CONSTRAINT uk_user_info_contact UNIQUE (contact)
);

-- 创建买家信息表
CREATE TABLE buyer_info (
                            nickname VARCHAR(50) NOT NULL,
                            account VARCHAR(50) NOT NULL,
                            gender VARCHAR(10) NOT NULL DEFAULT '男',
                            birthdate DATE NOT NULL DEFAULT '2000-01-01',
                            CONSTRAINT pk_buyer_info PRIMARY KEY (nickname),
                            CONSTRAINT uk_buyer_info_nickname UNIQUE (nickname),
                            CONSTRAINT fk_buyer_info_user_info FOREIGN KEY (account)
                                REFERENCES user_info(account)
);

-- 创建供应商信息表
CREATE TABLE vendor_info (
                             company_name VARCHAR(100) NOT NULL,
                             account VARCHAR(50) NOT NULL,
                             registered_address VARCHAR(200) NOT NULL,
                             contact_person VARCHAR(50) NOT NULL,
                             CONSTRAINT pk_vendor_info PRIMARY KEY (company_name),
                             CONSTRAINT uk_vendor_info_company_name UNIQUE (company_name),
                             CONSTRAINT fk_vendor_info_user_info FOREIGN KEY (account)
                                 REFERENCES user_info(account)
);

-- 创建游戏信息表
CREATE TABLE game_info (
                           game_name VARCHAR(100) NOT NULL,
                           category VARCHAR(50) NOT NULL,
                           price DECIMAL(10,2) NOT NULL,
                           company_name VARCHAR(100) NOT NULL,
                           release_time DATE NULL,
                           description VARCHAR(500) NOT NULL,
                           status VARCHAR(20) NOT NULL DEFAULT '下架',
                           download_link VARCHAR(255) NOT NULL,
                           license_number VARCHAR(50) NOT NULL,
                           score DECIMAL(2,1) NULL,
                           sales_volume INT NOT NULL DEFAULT 0,
                           visitor_count INT NOT NULL DEFAULT 0,
                           CONSTRAINT pk_game_info PRIMARY KEY (game_name),
                           CONSTRAINT fk_game_info_vendor_info FOREIGN KEY (company_name)
                               REFERENCES vendor_info(company_name)
);

-- 创建游戏申请表
CREATE TABLE game_application (
                                  application_id INT IDENTITY(1,1) NOT NULL,
                                  game_name VARCHAR(100) NOT NULL,
                                  company_name VARCHAR(100) NOT NULL,
                                  approval_status VARCHAR(20) NOT NULL DEFAULT '待审批',
                                  approval_result VARCHAR(200) NULL,
                                  application_time DATETIME2 NOT NULL,
                                  CONSTRAINT pk_game_application PRIMARY KEY (application_id),
                                  CONSTRAINT fk_game_application_game_info FOREIGN KEY (game_name)
                                      REFERENCES game_info(game_name),
                                  CONSTRAINT fk_game_application_vendor_info FOREIGN KEY (company_name)
                                      REFERENCES vendor_info(company_name)
);

-- 创建浏览历史表（复合主键）
CREATE TABLE browse_history (
                                nickname VARCHAR(50) NOT NULL,
                                game_name VARCHAR(100) NOT NULL,
                                browse_count INT NOT NULL DEFAULT 0,
                                CONSTRAINT pk_browse_history PRIMARY KEY (nickname, game_name),
                                CONSTRAINT fk_browse_history_buyer_info FOREIGN KEY (nickname)
                                    REFERENCES buyer_info(nickname),
                                CONSTRAINT fk_browse_history_game_info FOREIGN KEY (game_name)
                                    REFERENCES game_info(game_name)
);

-- 创建买家游戏信息表（复合主键）
CREATE TABLE buyer_game_info (
                                 nickname VARCHAR(50) NOT NULL,
                                 game_name VARCHAR(100) NOT NULL,
                                 license_number VARCHAR(50) NOT NULL,
                                 score DECIMAL(2,1) NOT NULL,
                                 comment VARCHAR(200) NULL,
                                 review_time DATETIME2 NULL,
                                 CONSTRAINT pk_buyer_game_info PRIMARY KEY (nickname, game_name),
                                 CONSTRAINT uk_buyer_game_info_license_number UNIQUE (license_number),
                                 CONSTRAINT fk_buyer_game_info_buyer_info FOREIGN KEY (nickname)
                                     REFERENCES buyer_info(nickname),
                                 CONSTRAINT fk_buyer_game_info_game_info FOREIGN KEY (game_name)
                                     REFERENCES game_info(game_name)
);

-- 创建订单信息表
CREATE TABLE order_info (
                            order_id VARCHAR(50) NOT NULL,
                            nickname VARCHAR(50) NOT NULL,
                            game_name VARCHAR(100) NOT NULL,
                            category VARCHAR(50) NULL,
                            price DECIMAL(10,2) NULL,
                            order_time DATETIME2 NOT NULL,
                            payment_time DATETIME2 NULL,
                            order_status VARCHAR(20) NOT NULL DEFAULT '待支付',
                            CONSTRAINT pk_order_info PRIMARY KEY (order_id),
                            CONSTRAINT fk_order_info_buyer_info FOREIGN KEY (nickname)
                                REFERENCES buyer_info(nickname),
                            CONSTRAINT fk_order_info_game_info FOREIGN KEY (game_name)
                                REFERENCES game_info(game_name)
);

-- 创建索引以提高查询性能
CREATE INDEX idx_user_info_role ON user_info(role);
CREATE INDEX idx_buyer_info_account ON buyer_info(account);
CREATE INDEX idx_vendor_info_account ON vendor_info(account);
CREATE INDEX idx_game_info_company_name ON game_info(company_name);
CREATE INDEX idx_game_info_category ON game_info(category);
CREATE INDEX idx_game_info_status ON game_info(status);
CREATE INDEX idx_game_application_status ON game_application(approval_status);
CREATE INDEX idx_browse_history_nickname ON browse_history(nickname);
CREATE INDEX idx_browse_history_game_name ON browse_history(game_name);
CREATE INDEX idx_buyer_game_info_nickname ON buyer_game_info(nickname);
CREATE INDEX idx_buyer_game_info_game_name ON buyer_game_info(game_name);
CREATE INDEX idx_order_info_nickname ON order_info(nickname);
CREATE INDEX idx_order_info_game_name ON order_info(game_name);
CREATE INDEX idx_order_info_status ON order_info(order_status);

