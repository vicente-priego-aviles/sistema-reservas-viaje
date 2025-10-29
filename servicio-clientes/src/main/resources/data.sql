-- ============================================================================
-- Script de Datos de Prueba - Sistema de Reservas de Viaje
-- ============================================================================
-- Descripción: Inserta 20 clientes con diferentes estados y tarjetas de crédito
-- Base de Datos: H2
-- Autor: javacadabra
-- Versión: 1.0.0
-- ============================================================================

-- ============================================================================
-- CLIENTES ACTIVOS (10 clientes con tarjetas válidas)
-- ============================================================================

-- Cliente 1: Juan Pérez García (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('123e4567-e89b-12d3-a456-426655440000', '12345678Z', 'Juan', 'Pérez García', 'juan.perez@example.com', '+34600123456', '1985-05-15', 'Calle Mayor 1', 'Madrid', '28001', 'Madrid', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000001', '123e4567-e89b-12d3-a456-426655440000', 'NDUzMjAxNTExMjgzMDM2Ng==', '0366', 2027, 12, 'VISA', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 2: María López Martínez (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('223e4567-e89b-12d3-a456-426655440001', '23456789A', 'María', 'López Martínez', 'maria.lopez@example.com', '+34600234567', '1990-08-22', 'Avenida de la Constitución 45', 'Barcelona', '08001', 'Barcelona', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000002', '223e4567-e89b-12d3-a456-426655440001', 'NTQyNTIzMzQzMDEwOTkwMw==', '9903', 2028, 6, 'MASTERCARD', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 3: Carlos Rodríguez Sánchez (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('323e4567-e89b-12d3-a456-426655440002', '34567890B', 'Carlos', 'Rodríguez Sánchez', 'carlos.rodriguez@example.com', '+34600345678', '1988-03-10', 'Calle Colón 23', 'Valencia', '46001', 'Valencia', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000003', '323e4567-e89b-12d3-a456-426655440002', 'Mzc4MjgyMjQ2MzEwMDA1', '0005', 2029, 3, 'AMEX', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 4: Ana Fernández Ruiz (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('423e4567-e89b-12d3-a456-426655440003', '45678901C', 'Ana', 'Fernández Ruiz', 'ana.fernandez@example.com', '+34600456789', '1992-11-05', 'Plaza España 8', 'Sevilla', '41001', 'Sevilla', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000004', '423e4567-e89b-12d3-a456-426655440003', 'NjAxMTExMTExMTExMTExNw==', '1117', 2027, 9, 'DISCOVER', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 5: Pedro Gómez Torres (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('523e4567-e89b-12d3-a456-426655440004', '56789012D', 'Pedro', 'Gómez Torres', 'pedro.gomez@example.com', '+34600567890', '1987-07-18', 'Calle Real 56', 'Zaragoza', '50001', 'Zaragoza', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000005', '523e4567-e89b-12d3-a456-426655440004', 'NDUzOTE0ODgwMzQzNjQ2Nw==', '6467', 2028, 11, 'VISA', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 6: Laura Jiménez Moreno (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('623e4567-e89b-12d3-a456-426655440005', '67890123E', 'Laura', 'Jiménez Moreno', 'laura.jimenez@example.com', '+34600678901', '1995-02-28', 'Avenida Andalucía 12', 'Málaga', '29001', 'Málaga', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000006', '623e4567-e89b-12d3-a456-426655440005', 'NTU1NTU1NTU1NTU1NDQ0NA==', '4444', 2029, 5, 'MASTERCARD', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 7: Miguel Álvarez Díaz (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('723e4567-e89b-12d3-a456-426655440006', '78901234F', 'Miguel', 'Álvarez Díaz', 'miguel.alvarez@example.com', '+34600789012', '1989-12-14', 'Calle Alcalá 78', 'Madrid', '28014', 'Madrid', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000007', '723e4567-e89b-12d3-a456-426655440006', 'NDkxNjMzODUwNjA4MjgzMg==', '2832', 2027, 8, 'VISA', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 8: Elena Ramírez Castro (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('823e4567-e89b-12d3-a456-426655440007', '89012345G', 'Elena', 'Ramírez Castro', 'elena.ramirez@example.com', '+34600890123', '1993-06-20', 'Paseo de Gracia 34', 'Barcelona', '08007', 'Barcelona', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000008', '823e4567-e89b-12d3-a456-426655440007', 'NTIwMDgyODI4MjgyODIxMA==', '8210', 2028, 4, 'MASTERCARD', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 9: Francisco Navarro Blanco (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('923e4567-e89b-12d3-a456-426655440008', '90123456H', 'Francisco', 'Navarro Blanco', 'francisco.navarro@example.com', '+34600901234', '1986-09-08', 'Calle San Vicente 90', 'Valencia', '46002', 'Valencia', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000009', '923e4567-e89b-12d3-a456-426655440008', 'MzcxNDQ5NjM1Mzk4NDMx', '8431', 2029, 7, 'AMEX', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 10: Isabel Romero Herrera (ACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('a23e4567-e89b-12d3-a456-426655440009', '01234567J', 'Isabel', 'Romero Herrera', 'isabel.romero@example.com', '+34601234567', '1991-04-12', 'Avenida Reina Mercedes 15', 'Sevilla', '41012', 'Sevilla', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000010', 'a23e4567-e89b-12d3-a456-426655440009', 'NjAxMTAwMDk5MDEzOTQyNA==', '9424', 2028, 10, 'DISCOVER', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- CLIENTES BLOQUEADOS (3 clientes - para probar rechazo)
-- ============================================================================

-- Cliente 11: Roberto Morales Gil (BLOQUEADO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('b23e4567-e89b-12d3-a456-426655440010', '11234567K', 'Roberto', 'Morales Gil', 'roberto.morales@example.com', '+34611234567', '1984-01-25', 'Calle Toledo 44', 'Madrid', '28005', 'Madrid', 'España', 'BLOQUEADO', 'Actividad sospechosa detectada', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000011', 'b23e4567-e89b-12d3-a456-426655440010', 'NDcxNjQ3MTA5Mzg2NTcyNw==', '5727', 2027, 12, 'VISA', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 12: Carmen Ortega Vega (BLOQUEADO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('c23e4567-e89b-12d3-a456-426655440011', '22345678L', 'Carmen', 'Ortega Vega', 'carmen.ortega@example.com', '+34622345678', '1990-10-30', 'Plaza Cataluña 5', 'Barcelona', '08002', 'Barcelona', 'España', 'BLOQUEADO', 'Impago de reserva anterior', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000012', 'c23e4567-e89b-12d3-a456-426655440011', 'NTQyNTIzMzQzMDEwOTkxMA==', '9910', 2028, 6, 'MASTERCARD', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 13: Javier Santos Delgado (BLOQUEADO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('d23e4567-e89b-12d3-a456-426655440012', '33456789M', 'Javier', 'Santos Delgado', 'javier.santos@example.com', '+34633456789', '1987-07-16', 'Calle Larios 20', 'Málaga', '29015', 'Málaga', 'España', 'BLOQUEADO', 'Solicitud del cliente', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000013', 'd23e4567-e89b-12d3-a456-426655440012', 'Mzc4MjgyMjQ2MzEwMDEy', '0012', 2029, 3, 'AMEX', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- CLIENTES PENDIENTE_VALIDACION (2 clientes - estado inicial)
-- ============================================================================

-- Cliente 14: Beatriz Cortés Fuentes (PENDIENTE_VALIDACION)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('e23e4567-e89b-12d3-a456-426655440013', '44567890N', 'Beatriz', 'Cortés Fuentes', 'beatriz.cortes@example.com', '+34644567890', '1994-03-22', 'Avenida Libertad 67', 'Bilbao', '48001', 'Vizcaya', 'España', 'PENDIENTE_VALIDACION', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000014', 'e23e4567-e89b-12d3-a456-426655440013', 'NDUzMjAxNTExMjgzMDM3Mw==', '0373', 2027, 12, 'VISA', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 15: Antonio Vázquez Prieto (PENDIENTE_VALIDACION)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('f23e4567-e89b-12d3-a456-426655440014', '55678901P', 'Antonio', 'Vázquez Prieto', 'antonio.vazquez@example.com', '+34655678901', '1988-11-09', 'Calle Santiago 33', 'Alicante', '03001', 'Alicante', 'España', 'PENDIENTE_VALIDACION', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000015', 'f23e4567-e89b-12d3-a456-426655440014', 'NTIwMDgyODI4MjgyODIyNw==', '8227', 2028, 4, 'MASTERCARD', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- CLIENTES CON TARJETAS EXPIRADAS (2 clientes - para probar validación)
-- ============================================================================

-- Cliente 16: Raquel Iglesias Márquez (ACTIVO pero tarjeta expirada)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('g23e4567-e89b-12d3-a456-426655440015', '66789012Q', 'Raquel', 'Iglesias Márquez', 'raquel.iglesias@example.com', '+34666789012', '1992-05-17', 'Calle Velázquez 88', 'Madrid', '28006', 'Madrid', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000016', 'g23e4567-e89b-12d3-a456-426655440015', 'NDUzOTE0ODgwMzQzNjQ3NA==', '6474', 2023, 8, 'VISA', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 17: Sergio Rubio Campos (ACTIVO pero tarjeta expirada)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('h23e4567-e89b-12d3-a456-426655440016', '77890123R', 'Sergio', 'Rubio Campos', 'sergio.rubio@example.com', '+34677890123', '1986-08-29', 'Ronda Universidad 12', 'Barcelona', '08007', 'Barcelona', 'España', 'ACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000017', 'h23e4567-e89b-12d3-a456-426655440016', 'NTU1NTU1NTU1NTU1NDQ1MQ==', '4451', 2024, 2, 'MASTERCARD', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- CLIENTES INACTIVOS (2 clientes - para probar estado)
-- ============================================================================

-- Cliente 18: Cristina Pascual León (INACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('i23e4567-e89b-12d3-a456-426655440017', '88901234S', 'Cristina', 'Pascual León', 'cristina.pascual@example.com', '+34688901234', '1989-12-03', 'Calle Mayor 150', 'Zaragoza', '50001', 'Zaragoza', 'España', 'INACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000018', 'i23e4567-e89b-12d3-a456-426655440017', 'MzcxNDQ5NjM1Mzk4NDQ4', '8448', 2029, 7, 'AMEX', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Cliente 19: Daniel Márquez Soler (INACTIVO)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('j23e4567-e89b-12d3-a456-426655440018', '99012345T', 'Daniel', 'Márquez Soler', 'daniel.marquez@example.com', '+34699012345', '1993-06-11', 'Plaza Nueva 7', 'Granada', '18001', 'Granada', 'España', 'INACTIVO', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000019', 'j23e4567-e89b-12d3-a456-426655440018', 'NjAxMTAwMDk5MDEzOTQzMQ==', '9431', 2028, 10, 'DISCOVER', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- CLIENTE EN_PROCESO_RESERVA (1 cliente - para probar concurrencia)
-- ============================================================================

-- Cliente 20: Mónica Gil Domínguez (EN_PROCESO_RESERVA)
INSERT INTO clientes (id, dni, nombre, apellidos, email, telefono, fecha_nacimiento, calle, ciudad, codigo_postal, provincia, pais, estado, motivo_bloqueo, fecha_creacion, fecha_modificacion)
VALUES ('k23e4567-e89b-12d3-a456-426655440019', '10123456V', 'Mónica', 'Gil Domínguez', 'monica.gil@example.com', '+34610123456', '1991-09-19', 'Avenida Diagonal 456', 'Barcelona', '08006', 'Barcelona', 'España', 'EN_PROCESO_RESERVA', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tarjetas_credito (id, cliente_id, numero_encriptado, ultimos_digitos, anio_expiracion, mes_expiracion, tipo_tarjeta, validada, motivo_rechazo, fecha_creacion, fecha_modificacion)
VALUES ('11111111-1111-1111-1111-000000000020', 'k23e4567-e89b-12d3-a456-426655440019', 'NDkxNjMzODUwNjA4Mjg0OQ==', '2849', 2027, 8, 'VISA', TRUE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================

-- Resumen de clientes insertados:
-- - 10 clientes ACTIVOS con tarjetas válidas ✅
-- - 3 clientes BLOQUEADOS ❌
-- - 2 clientes PENDIENTE_VALIDACION ⏳
-- - 2 clientes con tarjetas EXPIRADAS ⚠️
-- - 2 clientes INACTIVOS 💤
-- - 1 cliente EN_PROCESO_RESERVA 🔄
-- Total: 20 clientes