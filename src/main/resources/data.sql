-- Truncate tables to avoid duplicate data
SET FOREIGN_KEY_CHECKS = 0;

-- Không xóa bảng User và Role vì đã được khởi tạo bởi InitAdminConfig
-- TRUNCATE TABLE User;
-- TRUNCATE TABLE Role;

TRUNCATE TABLE Brand;
TRUNCATE TABLE Category;
TRUNCATE TABLE Product;
TRUNCATE TABLE Product_Variant;
TRUNCATE TABLE Product_Image;
TRUNCATE TABLE Related_Product;
TRUNCATE TABLE Review;

SET FOREIGN_KEY_CHECKS = 1;

-- Thêm thương hiệu (Brands)
INSERT INTO Brand (Brand_ID, Brand_name, Brand_desc, Logo_url) VALUES
(1, 'Nike', 'Thương hiệu thể thao hàng đầu thế giới', 'nike-logo.png'),
(2, 'Adidas', 'Thương hiệu thể thao và thời trang hàng đầu', 'adidas-logo.png'),
(3, 'Puma', 'Thương hiệu thể thao và thời trang từ Đức', 'puma-logo.png'),
(4, 'Vans', 'Thương hiệu giày skate nổi tiếng', 'vans-logo.png'),
(5, 'Converse', 'Thương hiệu giày vải kinh điển', 'converse-logo.png'),
(6, 'Uniqlo', 'Thương hiệu thời trang Nhật Bản', 'uniqlo-logo.png'),
(7, 'H&M', 'Thương hiệu thời trang giá rẻ', 'h&m-logo.png'),
(8, 'Zara', 'Thương hiệu thời trang nhanh', 'zara-logo.png');

-- Thêm danh mục (Categories)
INSERT INTO Category (Category_ID, Category_name, Description, Image, Category_Parent_ID, Status, Created_at, Updated_at) VALUES
(1, 'Giày dép', 'Các loại giày dép thể thao và thời trang', 'shoes.jpg', NULL, 'active', NOW(), NOW()),
(2, 'Quần áo', 'Các loại quần áo thể thao và thời trang', 'clothing.jpg', NULL, 'active', NOW(), NOW()),
(3, 'Phụ kiện', 'Các loại phụ kiện thời trang', 'accessories.jpg', NULL, 'active', NOW(), NOW()),

(4, 'Giày thể thao', 'Giày dành cho các hoạt động thể thao', 'sport-shoes.jpg', 1, 'active', NOW(), NOW()),
(5, 'Giày thời trang', 'Giày dành cho mục đích thời trang', 'fashion-shoes.jpg', 1, 'active', NOW(), NOW()),
(6, 'Dép & Sandal', 'Dép và sandal các loại', 'sandals.jpg', 1, 'active', NOW(), NOW()),

(7, 'Áo', 'Các loại áo thời trang', 'shirts.jpg', 2, 'active', NOW(), NOW()),
(8, 'Quần', 'Các loại quần thời trang', 'pants.jpg', 2, 'active', NOW(), NOW()),
(9, 'Đồ thể thao', 'Quần áo dành cho hoạt động thể thao', 'sportswear.jpg', 2, 'active', NOW(), NOW()),

(10, 'Áo phông', 'Áo phông các loại', 't-shirts.jpg', 7, 'active', NOW(), NOW()),
(11, 'Áo sơ mi', 'Áo sơ mi các loại', 'shirts.jpg', 7, 'active', NOW(), NOW()),
(12, 'Áo len', 'Áo len và áo nỉ', 'sweaters.jpg', 7, 'active', NOW(), NOW());

-- Thêm sản phẩm (Products)
-- Sản phẩm 1: Giày Nike Air Force 1
INSERT INTO Product (Product_ID, Name, Description, Base_Price, Category_ID, Brand_ID, Default_Variant_ID, Status, Product_Type, Created_at, Updated_at) VALUES
(1, 'Giày Nike Air Force 1', 'Giày thể thao Nike Air Force 1 với thiết kế cổ điển và sự thoải mái tối đa', 2500000, 4, 1, NULL, 'active', 'footwear', NOW(), NOW());

-- Sản phẩm 2: Áo phông Adidas Original
INSERT INTO Product (Product_ID, Name, Description, Base_Price, Category_ID, Brand_ID, Default_Variant_ID, Status, Product_Type, Created_at, Updated_at) VALUES
(2, 'Áo phông Adidas Original', 'Áo phông Adidas với logo ba lá cổ điển, chất liệu cotton cao cấp', 800000, 10, 2, NULL, 'active', 'clothing', NOW(), NOW());

-- Sản phẩm 3: Quần jeans Zara Slim Fit
INSERT INTO Product (Product_ID, Name, Description, Base_Price, Category_ID, Brand_ID, Default_Variant_ID, Status, Product_Type, Created_at, Updated_at) VALUES
(3, 'Quần jeans Zara Slim Fit', 'Quần jeans ống đứng vừa vặn, thiết kế hiện đại và thoải mái', 1200000, 8, 8, NULL, 'active', 'clothing', NOW(), NOW());

-- Sản phẩm 4: Giày Converse Chuck Taylor
INSERT INTO Product (Product_ID, Name, Description, Base_Price, Category_ID, Brand_ID, Default_Variant_ID, Status, Product_Type, Created_at, Updated_at) VALUES
(4, 'Giày Converse Chuck Taylor', 'Giày Converse Chuck Taylor cổ điển, phù hợp với mọi phong cách', 1300000, 5, 5, NULL, 'active', 'footwear', NOW(), NOW());

-- Sản phẩm 5: Áo sơ mi Uniqlo Oxford
INSERT INTO Product (Product_ID, Name, Description, Base_Price, Category_ID, Brand_ID, Default_Variant_ID, Status, Product_Type, Created_at, Updated_at) VALUES
(5, 'Áo sơ mi Uniqlo Oxford', 'Áo sơ mi Oxford chất lượng cao, phù hợp cho cả trang phục công sở và thường ngày', 650000, 11, 6, NULL, 'active', 'clothing', NOW(), NOW());

-- Thêm biến thể sản phẩm (Product Variants)
-- Biến thể cho Giày Nike Air Force 1
INSERT INTO Product_Variant (Variant_ID, Product_ID, Color, Size, Size_Type, Stock_Quantity, Price_Adjustment, SKU, Status) VALUES
(1, 1, 'Trắng', '38', 'shoe_size', 10, 0, 'AF1-WHITE-38', 'active'),
(2, 1, 'Trắng', '39', 'shoe_size', 15, 0, 'AF1-WHITE-39', 'active'),
(3, 1, 'Trắng', '40', 'shoe_size', 20, 0, 'AF1-WHITE-40', 'active'),
(4, 1, 'Đen', '38', 'shoe_size', 8, 0, 'AF1-BLACK-38', 'active'),
(5, 1, 'Đen', '39', 'shoe_size', 12, 0, 'AF1-BLACK-39', 'active'),
(6, 1, 'Đen', '40', 'shoe_size', 18, 0, 'AF1-BLACK-40', 'active');

-- Biến thể cho Áo phông Adidas Original
INSERT INTO Product_Variant (Variant_ID, Product_ID, Color, Size, Size_Type, Stock_Quantity, Price_Adjustment, SKU, Status) VALUES
(7, 2, 'Trắng', 'S', 'clothing_size', 20, 0, 'ADIDAS-ORG-WHITE-S', 'active'),
(8, 2, 'Trắng', 'M', 'clothing_size', 30, 0, 'ADIDAS-ORG-WHITE-M', 'active'),
(9, 2, 'Trắng', 'L', 'clothing_size', 25, 0, 'ADIDAS-ORG-WHITE-L', 'active'),
(10, 2, 'Đen', 'S', 'clothing_size', 15, 0, 'ADIDAS-ORG-BLACK-S', 'active'),
(11, 2, 'Đen', 'M', 'clothing_size', 25, 0, 'ADIDAS-ORG-BLACK-M', 'active'),
(12, 2, 'Đen', 'L', 'clothing_size', 20, 0, 'ADIDAS-ORG-BLACK-L', 'active'),
(13, 2, 'Xanh navy', 'S', 'clothing_size', 10, 0, 'ADIDAS-ORG-NAVY-S', 'active'),
(14, 2, 'Xanh navy', 'M', 'clothing_size', 20, 0, 'ADIDAS-ORG-NAVY-M', 'active'),
(15, 2, 'Xanh navy', 'L', 'clothing_size', 15, 0, 'ADIDAS-ORG-NAVY-L', 'active');

-- Biến thể cho Quần jeans Zara Slim Fit
INSERT INTO Product_Variant (Variant_ID, Product_ID, Color, Size, Size_Type, Stock_Quantity, Price_Adjustment, SKU, Status) VALUES
(16, 3, 'Xanh đậm', '29', 'numeric_size', 15, 0, 'ZARA-JEAN-DARK-29', 'active'),
(17, 3, 'Xanh đậm', '30', 'numeric_size', 20, 0, 'ZARA-JEAN-DARK-30', 'active'),
(18, 3, 'Xanh đậm', '31', 'numeric_size', 18, 0, 'ZARA-JEAN-DARK-31', 'active'),
(19, 3, 'Xanh đậm', '32', 'numeric_size', 15, 0, 'ZARA-JEAN-DARK-32', 'active'),
(20, 3, 'Xanh nhạt', '29', 'numeric_size', 12, 0, 'ZARA-JEAN-LIGHT-29', 'active'),
(21, 3, 'Xanh nhạt', '30', 'numeric_size', 18, 0, 'ZARA-JEAN-LIGHT-30', 'active'),
(22, 3, 'Xanh nhạt', '31', 'numeric_size', 15, 0, 'ZARA-JEAN-LIGHT-31', 'active'),
(23, 3, 'Xanh nhạt', '32', 'numeric_size', 10, 0, 'ZARA-JEAN-LIGHT-32', 'active');

-- Biến thể cho Giày Converse Chuck Taylor
INSERT INTO Product_Variant (Variant_ID, Product_ID, Color, Size, Size_Type, Stock_Quantity, Price_Adjustment, SKU, Status) VALUES
(24, 4, 'Trắng', '38', 'shoe_size', 12, 0, 'CONV-LOW-WHITE-38', 'active'),
(25, 4, 'Trắng', '39', 'shoe_size', 18, 0, 'CONV-LOW-WHITE-39', 'active'),
(26, 4, 'Trắng', '40', 'shoe_size', 15, 0, 'CONV-LOW-WHITE-40', 'active'),
(27, 4, 'Đen', '38', 'shoe_size', 10, 0, 'CONV-LOW-BLACK-38', 'active'),
(28, 4, 'Đen', '39', 'shoe_size', 15, 0, 'CONV-LOW-BLACK-39', 'active'),
(29, 4, 'Đen', '40', 'shoe_size', 12, 0, 'CONV-LOW-BLACK-40', 'active'),
(30, 4, 'Đỏ', '38', 'shoe_size', 8, 0, 'CONV-LOW-RED-38', 'active'),
(31, 4, 'Đỏ', '39', 'shoe_size', 12, 0, 'CONV-LOW-RED-39', 'active'),
(32, 4, 'Đỏ', '40', 'shoe_size', 10, 0, 'CONV-LOW-RED-40', 'active');

-- Biến thể cho Áo sơ mi Uniqlo Oxford
INSERT INTO Product_Variant (Variant_ID, Product_ID, Color, Size, Size_Type, Stock_Quantity, Price_Adjustment, SKU, Status) VALUES
(33, 5, 'Trắng', 'S', 'clothing_size', 15, 0, 'UNIQLO-OX-WHITE-S', 'active'),
(34, 5, 'Trắng', 'M', 'clothing_size', 25, 0, 'UNIQLO-OX-WHITE-M', 'active'),
(35, 5, 'Trắng', 'L', 'clothing_size', 20, 0, 'UNIQLO-OX-WHITE-L', 'active'),
(36, 5, 'Xanh nhạt', 'S', 'clothing_size', 12, 0, 'UNIQLO-OX-LBLUE-S', 'active'),
(37, 5, 'Xanh nhạt', 'M', 'clothing_size', 22, 0, 'UNIQLO-OX-LBLUE-M', 'active'),
(38, 5, 'Xanh nhạt', 'L', 'clothing_size', 18, 0, 'UNIQLO-OX-LBLUE-L', 'active');

-- Cập nhật defaultVariantId cho các sản phẩm
UPDATE Product SET Default_Variant_ID = 1 WHERE Product_ID = 1;
UPDATE Product SET Default_Variant_ID = 8 WHERE Product_ID = 2;
UPDATE Product SET Default_Variant_ID = 17 WHERE Product_ID = 3;
UPDATE Product SET Default_Variant_ID = 25 WHERE Product_ID = 4;
UPDATE Product SET Default_Variant_ID = 34 WHERE Product_ID = 5;

-- Thêm hình ảnh sản phẩm (Product Images)
-- Hình ảnh cho Giày Nike Air Force 1
INSERT INTO Product_Image (Image_ID, Product_ID, Variant_ID, Image_URL, Is_Primary, Sort_Order, Alt_Text, Created_at) VALUES
(1, 1, 1, '/images/products/nike-af1-white-1.jpg', 1, 1, 'Nike Air Force 1 Trắng - Mặt trước', NOW()),
(2, 1, 1, '/images/products/nike-af1-white-2.jpg', 0, 2, 'Nike Air Force 1 Trắng - Mặt bên', NOW()),
(3, 1, 4, '/images/products/nike-af1-black-1.jpg', 1, 1, 'Nike Air Force 1 Đen - Mặt trước', NOW()),
(4, 1, 4, '/images/products/nike-af1-black-2.jpg', 0, 2, 'Nike Air Force 1 Đen - Mặt bên', NOW());

-- Hình ảnh cho Áo phông Adidas Original
INSERT INTO Product_Image (Image_ID, Product_ID, Variant_ID, Image_URL, Is_Primary, Sort_Order, Alt_Text, Created_at) VALUES
(5, 2, 8, '/images/products/adidas-tee-white-1.jpg', 1, 1, 'Áo phông Adidas Trắng - Mặt trước', NOW()),
(6, 2, 8, '/images/products/adidas-tee-white-2.jpg', 0, 2, 'Áo phông Adidas Trắng - Mặt sau', NOW()),
(7, 2, 11, '/images/products/adidas-tee-black-1.jpg', 1, 1, 'Áo phông Adidas Đen - Mặt trước', NOW()),
(8, 2, 14, '/images/products/adidas-tee-navy-1.jpg', 1, 1, 'Áo phông Adidas Xanh navy - Mặt trước', NOW());

-- Hình ảnh cho Quần jeans Zara Slim Fit
INSERT INTO Product_Image (Image_ID, Product_ID, Variant_ID, Image_URL, Is_Primary, Sort_Order, Alt_Text, Created_at) VALUES
(9, 3, 17, '/images/products/zara-jeans-dark-1.jpg', 1, 1, 'Quần jeans Zara xanh đậm - Mặt trước', NOW()),
(10, 3, 17, '/images/products/zara-jeans-dark-2.jpg', 0, 2, 'Quần jeans Zara xanh đậm - Mặt sau', NOW()),
(11, 3, 21, '/images/products/zara-jeans-light-1.jpg', 1, 1, 'Quần jeans Zara xanh nhạt - Mặt trước', NOW()),
(12, 3, 21, '/images/products/zara-jeans-light-2.jpg', 0, 2, 'Quần jeans Zara xanh nhạt - Mặt sau', NOW());

-- Hình ảnh cho Giày Converse Chuck Taylor
INSERT INTO Product_Image (Image_ID, Product_ID, Variant_ID, Image_URL, Is_Primary, Sort_Order, Alt_Text, Created_at) VALUES
(13, 4, 25, '/images/products/converse-white-1.jpg', 1, 1, 'Giày Converse trắng - Mặt bên', NOW()),
(14, 4, 25, '/images/products/converse-white-2.jpg', 0, 2, 'Giày Converse trắng - Mặt trước', NOW()),
(15, 4, 28, '/images/products/converse-black-1.jpg', 1, 1, 'Giày Converse đen - Mặt bên', NOW()),
(16, 4, 31, '/images/products/converse-red-1.jpg', 1, 1, 'Giày Converse đỏ - Mặt bên', NOW());

-- Hình ảnh cho Áo sơ mi Uniqlo Oxford
INSERT INTO Product_Image (Image_ID, Product_ID, Variant_ID, Image_URL, Is_Primary, Sort_Order, Alt_Text, Created_at) VALUES
(17, 5, 34, '/images/products/uniqlo-oxford-white-1.jpg', 1, 1, 'Áo sơ mi Uniqlo Oxford trắng - Mặt trước', NOW()),
(18, 5, 34, '/images/products/uniqlo-oxford-white-2.jpg', 0, 2, 'Áo sơ mi Uniqlo Oxford trắng - Mặt sau', NOW()),
(19, 5, 37, '/images/products/uniqlo-oxford-lblue-1.jpg', 1, 1, 'Áo sơ mi Uniqlo Oxford xanh nhạt - Mặt trước', NOW()),
(20, 5, 37, '/images/products/uniqlo-oxford-lblue-2.jpg', 0, 2, 'Áo sơ mi Uniqlo Oxford xanh nhạt - Mặt sau', NOW());

-- Thêm sản phẩm liên quan (Related Products)
INSERT INTO Related_Product (Product_ID, Related_Product_ID, Relation_Type) VALUES
(1, 4, 'similar'),  -- Nike Air Force 1 liên quan đến Converse Chuck Taylor
(4, 1, 'similar'),  -- Converse Chuck Taylor liên quan đến Nike Air Force 1
(2, 5, 'cross_sell'),  -- Áo phông Adidas liên quan đến Áo sơ mi Uniqlo (thay COMPLEMENTARY bằng cross_sell)
(3, 2, 'cross_sell'),  -- Quần jeans Zara liên quan đến Áo phông Adidas (thay COMPLEMENTARY bằng cross_sell)
(3, 5, 'accessory'),  -- Quần jeans Zara và Áo sơ mi Uniqlo (thay OUTFIT bằng accessory)
(2, 3, 'accessory');  -- Áo phông Adidas và Quần jeans Zara (thay OUTFIT bằng accessory)