-- Khởi tạo vai trò người dùng
INSERT INTO Role (Role_name)
VALUES ('ADMIN'), ('USER')
    ON DUPLICATE KEY UPDATE Role_name = VALUES(Role_name);

-- Khởi tạo tài khoản ADMIN (mật khẩu: admin123 - đã hash bằng BCrypt)
INSERT INTO `User` (Username, Password_hash, First_Name, Last_Name, Email, Phone, Address, Role_ID, Created_at)
SELECT 'admin', '$2a$10$rRVH0Z3UkaDFY4Qbw5.V2.vQnkY4.F9SjOy0IA4UBEJe8iCJxGsuy', 'Quản', 'Trị Viên', 'admin@fashion-store.com', '0987654321', 'Hà Nội, Việt Nam', r.Role_ID, NOW()
FROM Role r WHERE r.Role_name = 'ADMIN' AND NOT EXISTS (SELECT 1 FROM `User` WHERE Username = 'admin');

-- Khởi tạo tài khoản người dùng thông thường (mật khẩu: user123)
INSERT INTO `User` (Username, Password_hash, First_Name, Last_Name, Email, Phone, Address, Role_ID, Created_at)
SELECT 'user', '$2a$10$bGw0vGGOqA5mGM0KArpQTesBzMMOUGvF.Qr2jYDbSg0pKRkqCwfye', 'Người', 'Dùng', 'user@fashion-store.com', '0123456789', 'Hồ Chí Minh, Việt Nam', r.Role_ID, NOW()
FROM Role r WHERE r.Role_name = 'USER' AND NOT EXISTS (SELECT 1 FROM `User` WHERE Username = 'user');

-- Khởi tạo thương hiệu
INSERT INTO Brand (Brand_name, Brand_desc, Logo_url) VALUES
                                                         ('Nike', 'Thương hiệu thời trang thể thao nổi tiếng thế giới', 'nike-logo.jpg'),
                                                         ('Adidas', 'Thương hiệu thời trang thể thao toàn cầu', 'adidas-logo.jpg'),
                                                         ('Zara', 'Thương hiệu thời trang nhanh đến từ Tây Ban Nha', 'zara-logo.jpg'),
                                                         ('H&M', 'Thương hiệu thời trang bình dân Thụy Điển', 'hm-logo.jpg'),
                                                         ('Uniqlo', 'Thương hiệu thời trang cơ bản đến từ Nhật Bản', 'uniqlo-logo.jpg'),
                                                         ('Gucci', 'Thương hiệu thời trang cao cấp của Ý', 'gucci-logo.jpg'),
                                                         ('Việt Tiến', 'Thương hiệu thời trang Việt Nam chất lượng cao', 'viettien-logo.jpg'),
                                                         ('Canifa', 'Thương hiệu thời trang Việt Nam dành cho gia đình', 'canifa-logo.jpg')
    ON DUPLICATE KEY UPDATE Brand_name = VALUES(Brand_name);

-- Khởi tạo danh mục chính
INSERT INTO Category (Category_name, Description, Image, Status, Created_at, Updated_at)
VALUES
    ('Thời trang nam', 'Các sản phẩm thời trang dành cho nam giới', 'men-category.jpg', 'active', NOW(), NOW()),
    ('Thời trang nữ', 'Các sản phẩm thời trang dành cho nữ giới', 'women-category.jpg', 'active', NOW(), NOW()),
    ('Thời trang trẻ em', 'Các sản phẩm thời trang dành cho trẻ em', 'kids-category.jpg', 'active', NOW(), NOW())
    ON DUPLICATE KEY UPDATE Category_name = VALUES(Category_name);

-- Lấy ID của các danh mục chính để sử dụng trong các câu lệnh tiếp theo
SET @men_cat_id = (SELECT Category_ID FROM Category WHERE Category_name = 'Thời trang nam' LIMIT 1);
SET @women_cat_id = (SELECT Category_ID FROM Category WHERE Category_name = 'Thời trang nữ' LIMIT 1);
SET @kids_cat_id = (SELECT Category_ID FROM Category WHERE Category_name = 'Thời trang trẻ em' LIMIT 1);

-- Khởi tạo danh mục con cho nam
INSERT INTO Category (Category_name, Description, Image, Category_Parent_ID, Status, Created_at, Updated_at)
VALUES
    ('Áo nam', 'Các loại áo dành cho nam giới', 'men-shirts.jpg', @men_cat_id, 'active', NOW(), NOW()),
    ('Quần nam', 'Các loại quần dành cho nam giới', 'men-pants.jpg', @men_cat_id, 'active', NOW(), NOW()),
    ('Áo khoác nam', 'Các loại áo khoác dành cho nam giới', 'men-jackets.jpg', @men_cat_id, 'active', NOW(), NOW()),
    ('Giày nam', 'Các loại giày dành cho nam giới', 'men-shoes.jpg', @men_cat_id, 'active', NOW(), NOW()),
    ('Phụ kiện nam', 'Phụ kiện thời trang nam như thắt lưng, tất, cà vạt', 'men-accessories.jpg', @men_cat_id, 'active', NOW(), NOW())
    ON DUPLICATE KEY UPDATE Category_name = VALUES(Category_name);

-- Khởi tạo danh mục con cho nữ
INSERT INTO Category (Category_name, Description, Image, Category_Parent_ID, Status, Created_at, Updated_at)
VALUES
    ('Áo nữ', 'Các loại áo dành cho nữ giới', 'women-shirts.jpg', @women_cat_id, 'active', NOW(), NOW()),
    ('Quần nữ', 'Các loại quần dành cho nữ giới', 'women-pants.jpg', @women_cat_id, 'active', NOW(), NOW()),
    ('Váy đầm', 'Các loại váy và đầm thời trang', 'women-dresses.jpg', @women_cat_id, 'active', NOW(), NOW()),
    ('Áo khoác nữ', 'Các loại áo khoác dành cho nữ giới', 'women-jackets.jpg', @women_cat_id, 'active', NOW(), NOW()),
    ('Giày nữ', 'Các loại giày dành cho nữ giới', 'women-shoes.jpg', @women_cat_id, 'active', NOW(), NOW()),
    ('Túi xách', 'Các loại túi xách thời trang', 'women-bags.jpg', @women_cat_id, 'active', NOW(), NOW()),
    ('Phụ kiện nữ', 'Phụ kiện thời trang nữ như vòng, lắc, khăn quàng', 'women-accessories.jpg', @women_cat_id, 'active', NOW(), NOW())
    ON DUPLICATE KEY UPDATE Category_name = VALUES(Category_name);

-- Khởi tạo danh mục con cho trẻ em
INSERT INTO Category (Category_name, Description, Image, Category_Parent_ID, Status, Created_at, Updated_at)
VALUES
    ('Áo trẻ em', 'Các loại áo dành cho trẻ em', 'kids-shirts.jpg', @kids_cat_id, 'active', NOW(), NOW()),
    ('Quần trẻ em', 'Các loại quần dành cho trẻ em', 'kids-pants.jpg', @kids_cat_id, 'active', NOW(), NOW()),
    ('Đồ bộ trẻ em', 'Các bộ quần áo dành cho trẻ em', 'kids-sets.jpg', @kids_cat_id, 'active', NOW(), NOW()),
    ('Giày trẻ em', 'Các loại giày dành cho trẻ em', 'kids-shoes.jpg', @kids_cat_id, 'active', NOW(), NOW())
    ON DUPLICATE KEY UPDATE Category_name = VALUES(Category_name);

-- Lấy ID danh mục con để sử dụng cho sản phẩm
SET @men_shirts_id = (SELECT Category_ID FROM Category WHERE Category_name = 'Áo nam' LIMIT 1);
SET @women_dresses_id = (SELECT Category_ID FROM Category WHERE Category_name = 'Váy đầm' LIMIT 1);
SET @men_shoes_id = (SELECT Category_ID FROM Category WHERE Category_name = 'Giày nam' LIMIT 1);
SET @women_bags_id = (SELECT Category_ID FROM Category WHERE Category_name = 'Túi xách' LIMIT 1);

-- Lấy ID thương hiệu để sử dụng cho sản phẩm
SET @nike_id = (SELECT Brand_ID FROM Brand WHERE Brand_name = 'Nike' LIMIT 1);
SET @adidas_id = (SELECT Brand_ID FROM Brand WHERE Brand_name = 'Adidas' LIMIT 1);
SET @zara_id = (SELECT Brand_ID FROM Brand WHERE Brand_name = 'Zara' LIMIT 1);
SET @hm_id = (SELECT Brand_ID FROM Brand WHERE Brand_name = 'H&M' LIMIT 1);
SET @gucci_id = (SELECT Brand_ID FROM Brand WHERE Brand_name = 'Gucci' LIMIT 1);

-- Tạo sản phẩm áo nam
INSERT INTO Product (Name, Description, Base_Price, Category_ID, Brand_ID, Stock_Quantity, Image, Status, Created_at, Updated_at)
VALUES
    ('Áo thun nam Nike Sportswear', 'Áo thun cotton siêu nhẹ, thoáng mát, thấm hút mồ hôi tốt, phù hợp cho các hoạt động thể thao và hàng ngày', 450000, @men_shirts_id, @nike_id, 100, 'nike-tshirt.jpg', 'active', NOW(), NOW()),
    ('Áo polo nam Adidas Performance', 'Áo polo thể thao nam với công nghệ thoáng khí, chống tia UV, phù hợp cho chơi golf và tennis', 550000, @men_shirts_id, @adidas_id, 80, 'adidas-polo.jpg', 'active', NOW(), NOW()),
    ('Áo sơ mi nam Zara Slim Fit', 'Áo sơ mi nam dáng ôm, phong cách hiện đại, phù hợp cho công sở và các buổi hẹn', 750000, @men_shirts_id, @zara_id, 60, 'zara-shirt.jpg', 'active', NOW(), NOW())
    ON DUPLICATE KEY UPDATE Name = VALUES(Name);

-- Lấy ID sản phẩm để tạo biến thể
SET @nike_tshirt_id = (SELECT Product_ID FROM Product WHERE Name = 'Áo thun nam Nike Sportswear' LIMIT 1);
SET @adidas_polo_id = (SELECT Product_ID FROM Product WHERE Name = 'Áo polo nam Adidas Performance' LIMIT 1);
SET @zara_shirt_id = (SELECT Product_ID FROM Product WHERE Name = 'Áo sơ mi nam Zara Slim Fit' LIMIT 1);

-- Tạo biến thể cho áo thun Nike
INSERT INTO Product_Variant (Product_ID, Color, Size, Stock_Quantity, Price_Adjustment, Image, Status)
VALUES
    (@nike_tshirt_id, 'Đen', 'S', 20, 0, 'nike-tshirt-black-s.jpg', 'active'),
    (@nike_tshirt_id, 'Đen', 'M', 30, 0, 'nike-tshirt-black-m.jpg', 'active'),
    (@nike_tshirt_id, 'Đen', 'L', 25, 0, 'nike-tshirt-black-l.jpg', 'active'),
    (@nike_tshirt_id, 'Đen', 'XL', 15, 20000, 'nike-tshirt-black-xl.jpg', 'active'),
    (@nike_tshirt_id, 'Trắng', 'S', 15, 0, 'nike-tshirt-white-s.jpg', 'active'),
    (@nike_tshirt_id, 'Trắng', 'M', 25, 0, 'nike-tshirt-white-m.jpg', 'active'),
    (@nike_tshirt_id, 'Trắng', 'L', 20, 0, 'nike-tshirt-white-l.jpg', 'active'),
    (@nike_tshirt_id, 'Trắng', 'XL', 10, 20000, 'nike-tshirt-white-xl.jpg', 'active'),
    (@nike_tshirt_id, 'Xanh Navy', 'S', 10, 0, 'nike-tshirt-navy-s.jpg', 'active'),
    (@nike_tshirt_id, 'Xanh Navy', 'M', 15, 0, 'nike-tshirt-navy-m.jpg', 'active'),
    (@nike_tshirt_id, 'Xanh Navy', 'L', 15, 0, 'nike-tshirt-navy-l.jpg', 'active'),
    (@nike_tshirt_id, 'Xanh Navy', 'XL', 5, 20000, 'nike-tshirt-navy-xl.jpg', 'active');

-- Tạo biến thể cho áo polo Adidas
INSERT INTO Product_Variant (Product_ID, Color, Size, Stock_Quantity, Price_Adjustment, Image, Status)
VALUES
    (@adidas_polo_id, 'Đen', 'S', 15, 0, 'adidas-polo-black-s.jpg', 'active'),
    (@adidas_polo_id, 'Đen', 'M', 20, 0, 'adidas-polo-black-m.jpg', 'active'),
    (@adidas_polo_id, 'Đen', 'L', 25, 0, 'adidas-polo-black-l.jpg', 'active'),
    (@adidas_polo_id, 'Đen', 'XL', 10, 30000, 'adidas-polo-black-xl.jpg', 'active'),
    (@adidas_polo_id, 'Trắng', 'S', 15, 0, 'adidas-polo-white-s.jpg', 'active'),
    (@adidas_polo_id, 'Trắng', 'M', 20, 0, 'adidas-polo-white-m.jpg', 'active'),
    (@adidas_polo_id, 'Trắng', 'L', 25, 0, 'adidas-polo-white-l.jpg', 'active'),
    (@adidas_polo_id, 'Trắng', 'XL', 10, 30000, 'adidas-polo-white-xl.jpg', 'active');

-- Tạo biến thể cho áo sơ mi Zara
INSERT INTO Product_Variant (Product_ID, Color, Size, Stock_Quantity, Price_Adjustment, Image, Status)
VALUES
    (@zara_shirt_id, 'Trắng', 'S', 10, 0, 'zara-shirt-white-s.jpg', 'active'),
    (@zara_shirt_id, 'Trắng', 'M', 15, 0, 'zara-shirt-white-m.jpg', 'active'),
    (@zara_shirt_id, 'Trắng', 'L', 20, 0, 'zara-shirt-white-l.jpg', 'active'),
    (@zara_shirt_id, 'Trắng', 'XL', 5, 50000, 'zara-shirt-white-xl.jpg', 'active'),
    (@zara_shirt_id, 'Xanh Nhạt', 'S', 10, 0, 'zara-shirt-lightblue-s.jpg', 'active'),
    (@zara_shirt_id, 'Xanh Nhạt', 'M', 15, 0, 'zara-shirt-lightblue-m.jpg', 'active'),
    (@zara_shirt_id, 'Xanh Nhạt', 'L', 20, 0, 'zara-shirt-lightblue-l.jpg', 'active'),
    (@zara_shirt_id, 'Xanh Nhạt', 'XL', 5, 50000, 'zara-shirt-lightblue-xl.jpg', 'active');

-- Tạo sản phẩm thời trang nữ
INSERT INTO Product (Name, Description, Base_Price, Category_ID, Brand_ID, Stock_Quantity, Image, Status, Created_at, Updated_at)
VALUES
    ('Váy đầm nữ Zara Floral', 'Váy đầm họa tiết hoa tay ngắn, phù hợp cho mùa hè', 850000, @women_dresses_id, @zara_id, 50, 'zara-dress.jpg', 'active', NOW(), NOW()),
    ('Váy đầm dự tiệc H&M', 'Váy đầm dáng dài, thiết kế sang trọng, phù hợp cho các buổi tiệc', 1250000, @women_dresses_id, @hm_id, 30, 'hm-dress.jpg', 'active', NOW(), NOW()),
    ('Túi xách Gucci Marmont', 'Túi xách cao cấp làm từ da thật, thiết kế sang trọng với logo Gucci nổi bật', 25000000, @women_bags_id, @gucci_id, 10, 'gucci-bag.jpg', 'active', NOW(), NOW())
    ON DUPLICATE KEY UPDATE Name = VALUES(Name);

-- Lấy ID sản phẩm thời trang nữ để tạo biến thể
SET @zara_dress_id = (SELECT Product_ID FROM Product WHERE Name = 'Váy đầm nữ Zara Floral' LIMIT 1);
SET @hm_dress_id = (SELECT Product_ID FROM Product WHERE Name = 'Váy đầm dự tiệc H&M' LIMIT 1);
SET @gucci_bag_id = (SELECT Product_ID FROM Product WHERE Name = 'Túi xách Gucci Marmont' LIMIT 1);

-- Tạo biến thể cho váy đầm Zara
INSERT INTO Product_Variant (Product_ID, Color, Size, Stock_Quantity, Price_Adjustment, Image, Status)
VALUES
    (@zara_dress_id, 'Hoa Xanh', 'S', 10, 0, 'zara-dress-blue-s.jpg', 'active'),
    (@zara_dress_id, 'Hoa Xanh', 'M', 15, 0, 'zara-dress-blue-m.jpg', 'active'),
    (@zara_dress_id, 'Hoa Xanh', 'L', 10, 0, 'zara-dress-blue-l.jpg', 'active'),
    (@zara_dress_id, 'Hoa Đỏ', 'S', 10, 50000, 'zara-dress-red-s.jpg', 'active'),
    (@zara_dress_id, 'Hoa Đỏ', 'M', 15, 50000, 'zara-dress-red-m.jpg', 'active'),
    (@zara_dress_id, 'Hoa Đỏ', 'L', 10, 50000, 'zara-dress-red-l.jpg', 'active');

-- Tạo biến thể cho váy đầm H&M
INSERT INTO Product_Variant (Product_ID, Color, Size, Stock_Quantity, Price_Adjustment, Image, Status)
VALUES
    (@hm_dress_id, 'Đen', 'S', 8, 0, 'hm-dress-black-s.jpg', 'active'),
    (@hm_dress_id, 'Đen', 'M', 10, 0, 'hm-dress-black-m.jpg', 'active'),
    (@hm_dress_id, 'Đen', 'L', 12, 0, 'hm-dress-black-l.jpg', 'active'),
    (@hm_dress_id, 'Đỏ', 'S', 5, 100000, 'hm-dress-red-s.jpg', 'active'),
    (@hm_dress_id, 'Đỏ', 'M', 8, 100000, 'hm-dress-red-m.jpg', 'active'),
    (@hm_dress_id, 'Đỏ', 'L', 7, 100000, 'hm-dress-red-l.jpg', 'active');

-- Tạo biến thể cho túi xách Gucci
INSERT INTO Product_Variant (Product_ID, Color, Size, Stock_Quantity, Price_Adjustment, Image, Status)
VALUES
    (@gucci_bag_id, 'Đen', 'Nhỏ', 3, 0, 'gucci-bag-black-small.jpg', 'active'),
    (@gucci_bag_id, 'Đen', 'Vừa', 5, 3000000, 'gucci-bag-black-medium.jpg', 'active'),
    (@gucci_bag_id, 'Đỏ', 'Nhỏ', 2, 500000, 'gucci-bag-red-small.jpg', 'active'),
    (@gucci_bag_id, 'Đỏ', 'Vừa', 3, 3500000, 'gucci-bag-red-medium.jpg', 'active');

-- Tạo mẫu dữ liệu cho khuyến mãi
INSERT INTO Promotion (Name, Description, Discount_Type, Discount_Value, Code, Minimum_Order, Start_Date, End_Date, Usage_Limit, Usage_Count, Status, Created_at, Updated_at)
VALUES
    ('Khuyến mãi mùa hè', 'Giảm giá 10% cho tất cả sản phẩm mùa hè', 'percentage', 10, 'SUMMER2025', 300000, '2025-06-01 00:00:00', '2025-08-31 23:59:59', 1000, 0, 'active', NOW(), NOW()),
    ('Khuyến mãi giảm giá cố định', 'Giảm 100.000đ cho đơn hàng từ 500.000đ', 'fixed_amount', 100000, 'FIXED100K', 500000, '2025-04-01 00:00:00', '2025-12-31 23:59:59', 500, 0, 'active', NOW(), NOW()),
    ('Flash Sale', 'Giảm giá 15% trong thời gian giới hạn', 'percentage', 15, 'FLASH15', 0, '2025-05-01 00:00:00', '2025-05-07 23:59:59', 300, 0, 'active', NOW(), NOW())
    ON DUPLICATE KEY UPDATE Name = VALUES(Name);

-- Gán khuyến mãi cho danh mục
INSERT INTO Promotion_Category (Promotion_ID, Category_ID)
SELECT p.Promotion_ID, @women_dresses_id
FROM Promotion p WHERE p.Code = 'SUMMER2025'
    ON DUPLICATE KEY UPDATE Promotion_ID = VALUES(Promotion_ID);

-- Tạo dữ liệu mẫu cho đánh giá sản phẩm
INSERT INTO Review (Product_ID, User_ID, Rating, Comment, Created_at, Updated_at)
SELECT
    @nike_tshirt_id,
    u.User_ID,
    5,
    'Áo rất thoáng mát, chất lượng tốt, đúng kích cỡ.',
    '2025-03-10 15:30:00',
    '2025-03-10 15:30:00'
FROM `User` u
WHERE u.Username = 'user'
  AND NOT EXISTS (SELECT 1 FROM Review WHERE Product_ID = @nike_tshirt_id AND User_ID = u.User_ID);

INSERT INTO Review (Product_ID, User_ID, Rating, Comment, Created_at, Updated_at)
SELECT
    @zara_dress_id,
    u.User_ID,
    4,
    'Váy rất đẹp, nhưng hơi nhỏ so với kích cỡ thông thường.',
    '2025-03-12 10:15:00',
    '2025-03-12 10:15:00'
FROM `User` u
WHERE u.Username = 'user'
  AND NOT EXISTS (SELECT 1 FROM Review WHERE Product_ID = @zara_dress_id AND User_ID = u.User_ID);

-- Tạo sản phẩm liên quan
INSERT INTO Related_Product (Product_ID, Related_Product_ID, Relation_Type)
VALUES
    (@nike_tshirt_id, @adidas_polo_id, 'similar'),
    (@adidas_polo_id, @nike_tshirt_id, 'similar'),
    (@nike_tshirt_id, @zara_shirt_id, 'similar'),
    (@zara_dress_id, @hm_dress_id, 'similar'),
    (@hm_dress_id, @zara_dress_id, 'similar')
    ON DUPLICATE KEY UPDATE Relation_Type = VALUES(Relation_Type);

-- Tạo hình ảnh sản phẩm
INSERT INTO Product_Image (Product_ID, Image_URL, Sort_Order, Created_at)
VALUES
    (@nike_tshirt_id, 'nike-tshirt-1.jpg', 0, NOW()),
    (@nike_tshirt_id, 'nike-tshirt-2.jpg', 1, NOW()),
    (@nike_tshirt_id, 'nike-tshirt-3.jpg', 2, NOW()),
    (@zara_dress_id, 'zara-dress-1.jpg', 0, NOW()),
    (@zara_dress_id, 'zara-dress-2.jpg', 1, NOW()),
    (@zara_dress_id, 'zara-dress-3.jpg', 2, NOW()),
    (@gucci_bag_id, 'gucci-bag-1.jpg', 0, NOW()),
    (@gucci_bag_id, 'gucci-bag-2.jpg', 1, NOW()),
    (@gucci_bag_id, 'gucci-bag-3.jpg', 2, NOW())
    ON DUPLICATE KEY UPDATE Image_URL = VALUES(Image_URL);