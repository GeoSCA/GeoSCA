-- Ejecutar las consultas de prevalidación antes del primer arranque con ddl-auto=update.
-- La migración de claves foráneas de ruta se ejecuta después de ese arranque.
-- No elimina ni modifica las columnas heredadas ruta.origen y ruta.destino.

-- Prevalidación de campos con índice único. Resolver duplicados manualmente
-- antes de iniciar la aplicación; no se borran ni fusionan registros automáticamente.
SELECT LOWER(TRIM(username)) AS valor, COUNT(*) AS cantidad
FROM usuario
GROUP BY LOWER(TRIM(username))
HAVING COUNT(*) > 1;

SELECT LOWER(TRIM(correo)) AS valor, COUNT(*) AS cantidad
FROM usuario
GROUP BY LOWER(TRIM(correo))
HAVING COUNT(*) > 1;

SELECT TRIM(ruc) AS valor, COUNT(*) AS cantidad
FROM cooperativa
GROUP BY TRIM(ruc)
HAVING COUNT(*) > 1;

SELECT LOWER(TRIM(nombre)) AS valor, COUNT(*) AS cantidad
FROM parroquia
GROUP BY LOWER(TRIM(nombre))
HAVING COUNT(*) > 1;

SELECT UPPER(TRIM(placa)) AS valor, COUNT(*) AS cantidad
FROM autobus
GROUP BY UPPER(TRIM(placa))
HAVING COUNT(*) > 1;

SELECT TRIM(cedula) AS valor, COUNT(*) AS cantidad
FROM conductor
GROUP BY TRIM(cedula)
HAVING COUNT(*) > 1;

-- Restricción lógica del servicio: nombre de ruta único dentro de cada cooperativa.
SELECT id_cooperativa, LOWER(TRIM(nombre)) AS nombre, COUNT(*) AS cantidad
FROM ruta
GROUP BY id_cooperativa, LOWER(TRIM(nombre))
HAVING COUNT(*) > 1;

-- Prevalidación: revise las filas sin coincidencia antes de aceptar la migración.
-- Ejecutar las consultas siguientes después de que Hibernate haya creado las columnas nuevas.
SELECT r.id_ruta, r.origen
FROM ruta r
LEFT JOIN parroquia p ON UPPER(TRIM(p.nombre)) = UPPER(TRIM(r.origen))
WHERE r.id_parroquia_origen IS NULL AND p.id_parroquia IS NULL;

SELECT r.id_ruta, r.destino
FROM ruta r
LEFT JOIN parroquia p ON UPPER(TRIM(p.nombre)) = UPPER(TRIM(r.destino))
WHERE r.id_parroquia_destino IS NULL AND p.id_parroquia IS NULL;

-- Asignar las claves donde el valor textual coincide con un nombre de parroquia.
START TRANSACTION;

UPDATE ruta r
JOIN parroquia p ON UPPER(TRIM(p.nombre)) = UPPER(TRIM(r.origen))
SET r.id_parroquia_origen = p.id_parroquia
WHERE r.id_parroquia_origen IS NULL;

UPDATE ruta r
JOIN parroquia p ON UPPER(TRIM(p.nombre)) = UPPER(TRIM(r.destino))
SET r.id_parroquia_destino = p.id_parroquia
WHERE r.id_parroquia_destino IS NULL;

COMMIT;

-- Las paradasIntermedias heredadas permanecen como texto para evitar pérdida de datos.
-- Revise manualmente esos valores antes de convertirlos a filas de ruta_parada.
