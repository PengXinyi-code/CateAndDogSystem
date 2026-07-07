-- 数据标准化迁移脚本：将项目定位收敛为“基于图像识别的校园流浪猫狗管理平台”
-- 执行时机：导入 animal-succour.sql 之后执行一次。
-- 注意：如果你的 MySQL 版本不支持 utf8mb4_0900_ai_ci，请把本文件中的排序规则替换为 utf8mb4_general_ci。

-- 1. 分类表从自由分类升级为受控一级分类：猫 / 狗
ALTER TABLE `category`
    ADD COLUMN `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '分类编码' AFTER `category_id`,
    ADD COLUMN `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用' AFTER `sort`;

UPDATE `category`
SET `enabled` = 0
WHERE `category_id` NOT IN ('cat', 'dog');

INSERT INTO `category` (`category_id`, `code`, `name`, `sort`, `enabled`)
VALUES ('cat', 'cat', '猫', 1, 1)
ON DUPLICATE KEY UPDATE `code` = VALUES(`code`), `name` = VALUES(`name`), `sort` = VALUES(`sort`), `enabled` = VALUES(`enabled`);

INSERT INTO `category` (`category_id`, `code`, `name`, `sort`, `enabled`)
VALUES ('dog', 'dog', '狗', 2, 1)
ON DUPLICATE KEY UPDATE `code` = VALUES(`code`), `name` = VALUES(`name`), `sort` = VALUES(`sort`), `enabled` = VALUES(`enabled`);

-- 2. 新增二级品种字典：猫狗 Top9 + 其他
CREATE TABLE IF NOT EXISTS `breed` (
  `breed_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '品种ID',
  `category_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '所属分类ID',
  `code` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '品种编码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '品种名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为该分类兜底品种',
  PRIMARY KEY (`breed_id`) USING BTREE,
  UNIQUE KEY `uk_breed_code` (`code`) USING BTREE,
  KEY `idx_breed_category` (`category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '猫狗品种字典' ROW_FORMAT = DYNAMIC;

INSERT INTO `breed` (`breed_id`, `category_id`, `code`, `name`, `sort`, `enabled`, `is_default`) VALUES
('cat_chinese_domestic', 'cat', 'chinese_domestic_cat', '中华田园猫', 1, 1, 0),
('cat_orange', 'cat', 'orange_cat', '橘猫', 2, 1, 0),
('cat_calico', 'cat', 'calico_cat', '三花猫', 3, 1, 0),
('cat_tabby', 'cat', 'tabby_cat', '狸花猫', 4, 1, 0),
('cat_british_shorthair', 'cat', 'british_shorthair', '英短', 5, 1, 0),
('cat_american_shorthair', 'cat', 'american_shorthair', '美短', 6, 1, 0),
('cat_ragdoll', 'cat', 'ragdoll', '布偶猫', 7, 1, 0),
('cat_siamese', 'cat', 'siamese', '暹罗猫', 8, 1, 0),
('cat_maine_coon', 'cat', 'maine_coon', '缅因猫', 9, 1, 0),
('cat_other', 'cat', 'other_cat', '其他猫', 99, 1, 1),
('dog_chinese_rural', 'dog', 'chinese_rural_dog', '中华田园犬', 1, 1, 0),
('dog_golden_retriever', 'dog', 'golden_retriever', '金毛', 2, 1, 0),
('dog_labrador', 'dog', 'labrador_retriever', '拉布拉多', 3, 1, 0),
('dog_poodle', 'dog', 'poodle', '泰迪/贵宾', 4, 1, 0),
('dog_corgi', 'dog', 'corgi', '柯基', 5, 1, 0),
('dog_husky', 'dog', 'husky', '哈士奇', 6, 1, 0),
('dog_samoyed', 'dog', 'samoyed', '萨摩耶', 7, 1, 0),
('dog_border_collie', 'dog', 'border_collie', '边牧', 8, 1, 0),
('dog_german_shepherd', 'dog', 'german_shepherd', '德牧', 9, 1, 0),
('dog_other', 'dog', 'other_dog', '其他狗', 99, 1, 1)
ON DUPLICATE KEY UPDATE
  `category_id` = VALUES(`category_id`),
  `code` = VALUES(`code`),
  `name` = VALUES(`name`),
  `sort` = VALUES(`sort`),
  `enabled` = VALUES(`enabled`),
  `is_default` = VALUES(`is_default`);

-- 3. 动物档案增加标准分类和品种字段，species 暂时保留兼容旧代码和历史数据
ALTER TABLE `animals`
    ADD COLUMN `category_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '动物一级分类ID' AFTER `species`,
    ADD COLUMN `breed_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '动物品种ID' AFTER `category_id`;

CREATE INDEX `idx_animals_category` ON `animals` (`category_id`);
CREATE INDEX `idx_animals_breed` ON `animals` (`breed_id`);

-- 4. 迁移已有猫狗数据。旧的“其他”数据不自动归类，避免把非猫狗误迁入系统。
UPDATE `animals`
SET `category_id` = 'cat',
    `breed_id` = 'cat_other',
    `species` = '猫'
WHERE `species` = '猫';

UPDATE `animals`
SET `category_id` = 'dog',
    `breed_id` = 'dog_other',
    `species` = '狗'
WHERE `species` = '狗';

-- 5. 后台不再提供“动物分类管理”页面，分类固定为猫/狗，仅作为动物档案表单的数据字典使用。
DELETE FROM `sys_menu`
WHERE `menu_id` = 2006
   OR `component` = 'succour/category/indx'
   OR (`path` = 'category' AND `menu_name` = '动物分类管理');
