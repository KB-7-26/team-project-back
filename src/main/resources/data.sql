INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '패션/잡화', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '패션/잡화');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '뷰티/미용', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '뷰티/미용');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '전자기기', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '전자기기');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '가구/인테리어', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '가구/인테리어');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '게임', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '게임');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '도서/문구', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '도서/문구');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '스포츠/레저', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '스포츠/레저');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '반려동물/취미', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '반려동물/취미');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '기타', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '기타');