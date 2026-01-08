INSERT INTO kullanici (email, sifre, ad, soyad, aktif, role)
VALUES (
  'admin@local',
  '$2a$10$e0MYzXyjpJS2fQ9x4gK5eu7N0zG9V0LZ0pniS3pSDOMkMt2rt7QeK',
  'Admin',
  'User',
  true,
  'ADMIN'
);
INSERT INTO mekan (ad, sehir, kapasite)
VALUES ('Test Mekan', 'Malatya', 500);
