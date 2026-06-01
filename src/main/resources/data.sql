INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '노트북', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '노트북');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '모니터', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '모니터');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '키보드', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '키보드');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '마우스', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '마우스');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '헤드폰', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '헤드폰');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '태블릿', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '태블릿');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '스마트폰', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '스마트폰');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '기타전자제품', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '기타전자제품');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '도서', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '도서');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '의류', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '의류');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '생활용품', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '생활용품');

INSERT INTO categories (name, parent_id)
SELECT * FROM (SELECT '기타', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = '기타');