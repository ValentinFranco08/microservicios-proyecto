-- Test data for pedidos
INSERT INTO pedidos (id, usuario_id, numero_producto, nombre_producto, cantidad, precio_unitario, precio_total, estado, fecha_creacion, fecha_actualizacion, descripcion, direccion_envio) VALUES 
(1, 1, 'PROD-001', 'Laptop Dell', 1, 999.99, 999.99, 'PENDIENTE', NOW(), NULL, 'Laptop para desarrollo', 'Calle Principal 123'),
(2, 1, 'PROD-002', 'Mouse Logitech', 2, 29.99, 59.98, 'CONFIRMADO', NOW(), NOW(), 'Mouses inalámbricos', 'Calle Principal 123'),
(3, 2, 'PROD-003', 'Teclado Mecánico', 1, 149.99, 149.99, 'EN_PROCESO', NOW(), NOW(), 'Teclado RGB', 'Calle Secundaria 456'),
(4, 2, 'PROD-001', 'Laptop Dell', 1, 999.99, 999.99, 'ENTREGADO', NOW(), NOW(), 'Laptop para diseño', 'Calle Secundaria 456'),
(5, 4, 'PROD-004', 'Monitor LG 27"', 1, 349.99, 349.99, 'CANCELADO', NOW(), NOW(), 'Monitor Ultra HD', 'Calle Tercera 789');
