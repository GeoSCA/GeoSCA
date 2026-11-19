# GeoSCA - Gestión de cooperativas

## Configuración inicial

La aplicación crea de forma idempotente roles, permisos, el usuario `admin` y la lista editable de parroquias urbanas y rurales de Latacunga durante el arranque.

Configure `APP_ADMIN_PASSWORD` antes del primer inicio en un entorno compartido o productivo. El valor de desarrollo definido en `application.properties` es `Admin12345!`; debe reemplazarse inmediatamente y no debe reutilizarse en producción. La conexión MySQL admite `DB_USER` y `DB_PASSWORD` (valores locales predeterminados: `root`).

## Convención de estados

- Cooperativa, parroquia y ruta: `ACTIVA` / `INACTIVA`.
- Autobús, conductor y usuario: `ACTIVO` / `INACTIVO` (usuario se almacena como booleano).

## Migración para rutas existentes

Hibernate puede agregar las nuevas claves foráneas de parroquia y las tablas intermedias, pero no puede deducir de forma segura la relación entre las cadenas antiguas de `ruta.origen`/`ruta.destino` y una fila de `parroquia`. Revise y ejecute manualmente `docs/MIGRACION_RUTA_PARROQUIA.sql` después de que Hibernate cree el esquema nuevo. No se elimina ninguna columna de texto heredada.

Los índices únicos declarados en entidades requieren que los datos existentes no contengan duplicados. Revise las consultas de prevalidación del archivo de migración antes del arranque si la BD ya contiene información.

## Pruebas

`.\mvnw.cmd test` activa el perfil `test`, usa H2 en memoria y no debe conectarse a MySQL. La configuración de prueba se encuentra en `src/test/resources/application.properties`.
