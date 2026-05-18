-- Test data for usuarios
INSERT INTO usuarios (id, nombre, apellido, email, activo, fecha_creacion, fecha_actualizacion) VALUES 
(1, 'Juan', 'Pérez', 'juan@example.com', true, NOW(), NOW()),
(2, 'María', 'García', 'maria@example.com', true, NOW(), NOW()),
(3, 'Carlos', 'López', 'carlos@example.com', false, NOW(), NOW()),
(4, 'Ana', 'Martínez', 'ana@example.com', true, NOW(), NOW()),
(5, 'Roberto', 'Sánchez', 'roberto@example.com', true, NOW(), NOW());
