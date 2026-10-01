# Configuración Google Cloud SQL — Sistema Ganadero

## Requisitos previos

- Cuenta activa en Google Cloud Platform
- Proyecto de GCP creado
- Cloud SQL API habilitada

## Paso 1 — Crear instancia Cloud SQL MySQL

1. Ir a **Google Cloud Console → SQL → Crear instancia**
2. Seleccionar **MySQL 8.0**
3. Configurar:
   - **ID de instancia**: `sistema-ganadero-db`
   - **Contraseña root**: (crear contraseña segura)
   - **Región**: `us-central1` o la más cercana a Colombia
   - **Tipo de máquina**: `db-n1-standard-1` (suficiente para empezar)
   - **Almacenamiento**: 10 GB SSD

## Paso 2 — Crear base de datos y usuario

En Cloud Shell o en la consola de SQL:

```sql
CREATE DATABASE sistema_ganadero CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'ganadero_user'@'%' IDENTIFIED BY 'TU_CONTRASEÑA_SEGURA';
GRANT ALL PRIVILEGES ON sistema_ganadero.* TO 'ganadero_user'@'%';
FLUSH PRIVILEGES;
```

## Paso 3 — Configurar red (IP pública)

1. En la instancia Cloud SQL → **Conexiones → Redes autorizadas**
2. Agregar tu IP pública: `TU_IP/32`
3. O para development: `0.0.0.0/0` (solo en desarrollo, no producción)

## Paso 4 — Ejecutar el esquema

```bash
mysql -h IP_DE_TU_INSTANCIA -u ganadero_user -p sistema_ganadero < database/cloud_schema.sql
```

## Paso 5 — Configurar el sistema

1. Copiar el archivo de configuración de ejemplo:
   ```
   cp config/database.properties.example config/database.properties
   ```

2. Editar `config/database.properties` con tus credenciales reales:
   ```properties
   db.url=jdbc:mysql://TU_IP_CLOUD_SQL:3306/sistema_ganadero
   db.user=ganadero_user
   db.password=TU_CONTRASEÑA_SEGURA
   ```

3. **IMPORTANTE**: Nunca subir `database.properties` a Git. Está en `.gitignore`.

## Paso 6 — Verificar conexión

Al iniciar el sistema, en los logs verás:
- ✓ `Base de datos configurada: jdbc:mysql://...` → Conectado
- ⚠ `Modo demo activo` → Sin configuración (usa datos en memoria)

## Usar Cloud SQL Auth Proxy (Recomendado para producción)

En lugar de exponer IP pública, usar el proxy de autenticación:

```bash
# Descargar Cloud SQL Auth Proxy
curl -o cloud-sql-proxy https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.6.0/cloud-sql-proxy.windows.amd64.exe

# Ejecutar proxy
cloud-sql-proxy TU_PROJECT:REGION:INSTANCIA --port=3306
```

Con el proxy, usar en database.properties:
```properties
db.url=jdbc:mysql://127.0.0.1:3306/sistema_ganadero
```

## Seguridad

- Usar contraseñas de al menos 16 caracteres
- Habilitar SSL en la conexión JDBC
- Hacer backups automáticos en Cloud SQL (diarios)
- No compartir `database.properties` bajo ninguna circunstancia
