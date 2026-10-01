-- =====================================================================
-- SISTEMA GANADERO — Esquema completo Google Cloud SQL (MySQL 8.0)
-- Versión: 2.0 | Arquitectura: Base de datos única con finca_id FK
-- Contacto comercial: +57 301 389 6861
-- =====================================================================

CREATE DATABASE IF NOT EXISTS sistema_ganadero
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sistema_ganadero;

-- =====================================================================
-- 1. USUARIOS — Roles ampliados (Administrador, Vendedor Ganadero,
--    Comprador, Operario, Veterinario, Mesero, Cocina, Caja)
-- =====================================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(100)  NOT NULL,
    username       VARCHAR(50)   NOT NULL UNIQUE,
    email          VARCHAR(100)  UNIQUE,
    telefono       VARCHAR(30),
    documento      VARCHAR(40),
    password_hash  VARCHAR(255)  NOT NULL COMMENT 'SHA-256 con sal, nunca texto plano',
    rol            VARCHAR(50)   NOT NULL DEFAULT 'OPERARIO',
    departamento   VARCHAR(80),
    ciudad         VARCHAR(80),
    estado         VARCHAR(20)   NOT NULL DEFAULT 'Activo',
    fecha_creacion DATE          NOT NULL,
    ultimo_acceso  DATETIME,
    INDEX idx_rol (rol),
    INDEX idx_estado (estado)
) ENGINE=InnoDB;

-- =====================================================================
-- 2. ROLES y PERMISOS
-- =====================================================================
CREATE TABLE IF NOT EXISTS roles (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    codigo    VARCHAR(40)  NOT NULL UNIQUE,
    etiqueta  VARCHAR(80)  NOT NULL,
    descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS permisos (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    codigo    VARCHAR(60)  NOT NULL UNIQUE,
    descripcion TEXT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rol_permisos (
    rol_id     INT NOT NULL,
    permiso_id INT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id)     REFERENCES roles(id)    ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 3. FINCAS
-- =====================================================================
CREATE TABLE IF NOT EXISTS fincas (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100)     NOT NULL,
    ubicacion       VARCHAR(200),
    departamento    VARCHAR(80),
    ciudad          VARCHAR(80),
    propietario     VARCHAR(100),
    area_hectareas  DECIMAL(10,2),
    descripcion     TEXT,
    estado          VARCHAR(20)      NOT NULL DEFAULT 'Activa',
    fecha_creacion  DATE             NOT NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 4. LOTES (separados por finca)
-- =====================================================================
CREATE TABLE IF NOT EXISTS lotes (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    finca_id       INT         NOT NULL,
    nombre         VARCHAR(100) NOT NULL,
    capacidad      INT,
    descripcion    TEXT,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'Activo',
    fecha_creacion DATE         NOT NULL,
    FOREIGN KEY (finca_id) REFERENCES fincas(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- 5. ANIMALES (ficha técnica completa)
-- =====================================================================
CREATE TABLE IF NOT EXISTS animales (
    id                       INT AUTO_INCREMENT PRIMARY KEY,
    finca_id                 INT          NOT NULL,
    lote_id                  INT,
    arete                    VARCHAR(50)  NOT NULL,
    nombre                   VARCHAR(100),
    sexo                     VARCHAR(10)  NOT NULL,
    raza                     VARCHAR(100),
    color                    VARCHAR(80),
    estado_corporal          TINYINT      DEFAULT 3 COMMENT 'Escala 1-5',
    fecha_nacimiento         DATE,
    fecha_ingreso            DATE,
    peso_inicial             DECIMAL(8,2),
    peso_actual              DECIMAL(8,2),
    gdp                      DECIMAL(6,3) COMMENT 'kg/día',
    estado_reproductivo      VARCHAR(50),
    estado_gestacion         VARCHAR(50)  DEFAULT 'No gestante',
    estado_sanitario         VARCHAR(50),
    tipo_produccion          VARCHAR(50)  COMMENT 'Carne, Leche, Doble propósito',
    uso_recomendado          VARCHAR(50)  COMMENT 'Ceba, Cría, Leche, Reproducción, Levante',
    produccion_leche_diaria  DECIMAL(6,2) DEFAULT 0 COMMENT 'L/día',
    numero_partos            INT          DEFAULT 0,
    ultimo_parto             DATE,
    proximo_servicio         DATE,
    calidad_genetica         VARCHAR(40),
    linea_genetica           VARCHAR(120),
    registro_genealogico     VARCHAR(80),
    registro_ica_numero      VARCHAR(60),
    veterinario_responsable  VARCHAR(120),
    foto_path                VARCHAR(500),
    video_path               VARCHAR(500),
    observaciones            TEXT,
    activo                   BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro           DATE         NOT NULL,
    FOREIGN KEY (finca_id) REFERENCES fincas(id) ON DELETE RESTRICT,
    FOREIGN KEY (lote_id)  REFERENCES lotes(id)  ON DELETE SET NULL,
    INDEX idx_finca_arete (finca_id, arete),
    INDEX idx_gdp (gdp)
) ENGINE=InnoDB;

-- =====================================================================
-- 6. GALERÍA DE FOTOS DE ANIMALES
-- =====================================================================
CREATE TABLE IF NOT EXISTS animal_fotos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    animal_id       INT          NOT NULL,
    ruta            VARCHAR(500) NOT NULL,
    descripcion     VARCHAR(200),
    es_principal    BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_subida    DATETIME     NOT NULL,
    FOREIGN KEY (animal_id) REFERENCES animales(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 7. DOCUMENTOS DE ANIMALES (ICA, sanitarias, genealógicos, facturas)
-- =====================================================================
CREATE TABLE IF NOT EXISTS animal_documentos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    animal_id       INT          NOT NULL,
    tipo            VARCHAR(50)  NOT NULL
        COMMENT 'REGISTRO_ICA, GUIA_SANITARIA, CERT_VACUNACION, REG_PROPIEDAD, REG_GENEALOGICO, FACTURA, OTRO',
    numero          VARCHAR(80),
    ruta            VARCHAR(500),
    descripcion     TEXT,
    fecha_emision   DATE,
    vigente         BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_subida    DATETIME     NOT NULL,
    FOREIGN KEY (animal_id) REFERENCES animales(id) ON DELETE CASCADE,
    INDEX idx_tipo (tipo)
) ENGINE=InnoDB;

-- =====================================================================
-- 8. TERNEROS
-- =====================================================================
CREATE TABLE IF NOT EXISTS terneros (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    animal_madre_id  INT,
    finca_id         INT         NOT NULL,
    lote_id          INT,
    codigo           VARCHAR(50)  NOT NULL,
    sexo             VARCHAR(10)  NOT NULL,
    raza             VARCHAR(100),
    fecha_nacimiento DATE,
    peso_nacimiento  DECIMAL(8,2),
    estado           VARCHAR(50),
    observaciones    TEXT,
    FOREIGN KEY (finca_id)        REFERENCES fincas(id)   ON DELETE RESTRICT,
    FOREIGN KEY (animal_madre_id) REFERENCES animales(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 9. PESAJES
-- =====================================================================
CREATE TABLE IF NOT EXISTS pesajes (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    animal_id     INT         NOT NULL,
    finca_id      INT         NOT NULL,
    fecha         DATE        NOT NULL,
    peso          DECIMAL(8,2) NOT NULL,
    observaciones TEXT,
    usuario_id    INT,
    FOREIGN KEY (animal_id) REFERENCES animales(id) ON DELETE CASCADE,
    FOREIGN KEY (finca_id)  REFERENCES fincas(id)   ON DELETE RESTRICT,
    INDEX idx_animal_fecha (animal_id, fecha)
) ENGINE=InnoDB;

-- =====================================================================
-- 10. VACUNAS (con dosis, lote y documento)
-- =====================================================================
CREATE TABLE IF NOT EXISTS vacunas (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    animal_id        INT          NOT NULL,
    finca_id         INT          NOT NULL,
    nombre_vacuna    VARCHAR(150) NOT NULL,
    fecha_aplicacion DATE         NOT NULL,
    proxima_dosis    DATE,
    responsable      VARCHAR(100),
    dosis            VARCHAR(40),
    lote_fabricante  VARCHAR(80),
    documento_ruta   VARCHAR(500),
    observaciones    TEXT,
    FOREIGN KEY (animal_id) REFERENCES animales(id) ON DELETE CASCADE,
    FOREIGN KEY (finca_id)  REFERENCES fincas(id)   ON DELETE RESTRICT,
    INDEX idx_proxima_dosis (proxima_dosis)
) ENGINE=InnoDB;

-- =====================================================================
-- 11. SANIDAD (enfermedades, tratamientos, purgas, vitaminas)
-- =====================================================================
CREATE TABLE IF NOT EXISTS sanidad (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    animal_id       INT          NOT NULL,
    finca_id        INT          NOT NULL,
    tipo            VARCHAR(50)  NOT NULL COMMENT 'PURGA, VITAMINAS, ENFERMEDAD, TRATAMIENTO',
    descripcion     TEXT,
    sintomas        TEXT,
    medicamento     VARCHAR(150),
    veterinario     VARCHAR(120),
    fecha           DATE         NOT NULL,
    proxima_fecha   DATE,
    costo           DECIMAL(12,2),
    estado          VARCHAR(40)  COMMENT 'EN_TRATAMIENTO, RESUELTO, CRITICO',
    observaciones   TEXT,
    FOREIGN KEY (animal_id) REFERENCES animales(id) ON DELETE CASCADE,
    FOREIGN KEY (finca_id)  REFERENCES fincas(id)   ON DELETE RESTRICT,
    INDEX idx_estado (estado),
    INDEX idx_animal_fecha (animal_id, fecha)
) ENGINE=InnoDB;

-- =====================================================================
-- 12. MOVIMIENTOS
-- =====================================================================
CREATE TABLE IF NOT EXISTS movimientos (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    animal_id         INT         NOT NULL,
    finca_origen_id   INT,
    finca_destino_id  INT,
    lote_origen_id    INT,
    lote_destino_id   INT,
    fecha             DATE        NOT NULL,
    tipo_movimiento   VARCHAR(100) NOT NULL COMMENT 'Traslado, Venta, Compra, Muerte, Nacimiento',
    observaciones     TEXT,
    usuario_id        INT,
    FOREIGN KEY (animal_id) REFERENCES animales(id) ON DELETE CASCADE,
    INDEX idx_animal_fecha (animal_id, fecha)
) ENGINE=InnoDB;

-- =====================================================================
-- 13. INVENTARIO
-- =====================================================================
CREATE TABLE IF NOT EXISTS inventario (
    id                 INT AUTO_INCREMENT PRIMARY KEY,
    finca_id           INT            NOT NULL,
    codigo             VARCHAR(50),
    producto           VARCHAR(150)   NOT NULL,
    categoria          VARCHAR(100),
    cantidad           DECIMAL(10,2)  NOT NULL,
    unidad             VARCHAR(50),
    stock_minimo       DECIMAL(10,2),
    costo_unitario     DECIMAL(12,2),
    proveedor          VARCHAR(150),
    fecha_ingreso      DATE           NOT NULL,
    fecha_vencimiento  DATE,
    estado             VARCHAR(50)    NOT NULL DEFAULT 'Disponible',
    FOREIGN KEY (finca_id) REFERENCES fincas(id) ON DELETE RESTRICT,
    INDEX idx_finca_categoria (finca_id, categoria),
    INDEX idx_vencimiento (fecha_vencimiento)
) ENGINE=InnoDB;

-- =====================================================================
-- 14. POTREROS Y ALIMENTACIÓN
-- =====================================================================
CREATE TABLE IF NOT EXISTS potreros (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    finca_id            INT          NOT NULL,
    codigo              VARCHAR(40)  NOT NULL,
    nombre              VARCHAR(120) NOT NULL,
    area_hectareas      DECIMAL(8,2),
    capacidad_animal    INT,
    tipo_pasto          VARCHAR(120),
    estado_suelo        VARCHAR(80),
    estado_cercado      VARCHAR(80),
    disponibilidad_agua VARCHAR(80),
    sistema_riego       VARCHAR(80),
    ph                  DECIMAL(4,2),
    altitud             DECIMAL(8,2),
    clima               VARCHAR(80),
    fecha_ultimo_uso    DATE,
    fecha_descanso      DATE,
    animales_actuales   INT          DEFAULT 0,
    observaciones       TEXT,
    FOREIGN KEY (finca_id) REFERENCES fincas(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS alimentacion_registros (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    finca_id        INT          NOT NULL,
    lote_id         INT,
    potrero_id      INT,
    tipo_alimento   VARCHAR(120) NOT NULL,
    cantidad        DECIMAL(10,2),
    unidad          VARCHAR(40),
    frecuencia      VARCHAR(60),
    responsable     VARCHAR(120),
    fecha           DATE         NOT NULL,
    costo           DECIMAL(12,2),
    observaciones   TEXT,
    FOREIGN KEY (finca_id)   REFERENCES fincas(id)   ON DELETE RESTRICT,
    FOREIGN KEY (lote_id)    REFERENCES lotes(id)    ON DELETE SET NULL,
    FOREIGN KEY (potrero_id) REFERENCES potreros(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 15. CULTIVOS Y PASTURAS
-- =====================================================================
CREATE TABLE IF NOT EXISTS pasturas (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    finca_id            INT          NOT NULL,
    potrero_id          INT,
    nombre              VARCHAR(120) NOT NULL,
    tipo_pasto          VARCHAR(120),
    area_sembrada       DECIMAL(8,2),
    fecha_siembra       DATE,
    estado              VARCHAR(60)  COMMENT 'Activa, En recuperación, Lista para consumo, Agotada',
    produccion_estimada DECIMAL(12,2),
    fertilizacion       TEXT,
    riego               TEXT,
    plagas              TEXT,
    historial           TEXT,
    observaciones       TEXT,
    FOREIGN KEY (finca_id)   REFERENCES fincas(id)   ON DELETE RESTRICT,
    FOREIGN KEY (potrero_id) REFERENCES potreros(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 16. MARKETPLACE — PUBLICACIONES
-- =====================================================================
CREATE TABLE IF NOT EXISTS marketplace_listings (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    codigo              VARCHAR(40)  NOT NULL UNIQUE,
    tipo                VARCHAR(20)  NOT NULL COMMENT 'INDIVIDUAL, LOTE',
    estado              VARCHAR(40)  NOT NULL DEFAULT 'PUBLICADO'
        COMMENT 'PUBLICADO, RECIBIENDO_OFERTAS, OFERTA_RECIBIDA, OFERTA_BAJA, OFERTA_ACEPTABLE, NEGOCIANDO, RESERVADO, VENDIDO, CANCELADO, RETIRADO',
    finca_id            INT,
    vendedor_id         INT,
    titulo              VARCHAR(200) NOT NULL,
    resumen             TEXT,
    raza                VARCHAR(120),
    sexo                VARCHAR(20),
    edad_meses          INT,
    peso_total          DECIMAL(10,2),
    tipo_produccion     VARCHAR(60),
    estado_sanitario    VARCHAR(60),
    vacunas_al_dia      BOOLEAN      DEFAULT TRUE,
    departamento        VARCHAR(80),
    ciudad              VARCHAR(80),
    distancia_km        DECIMAL(8,2),
    precio              DECIMAL(14,2) NOT NULL COMMENT 'Precio deseado, público',
    precio_minimo       DECIMAL(14,2) COMMENT 'Privado: sólo admin/vendedor',
    comision_porcentaje DECIMAL(5,2) DEFAULT 3.0,
    contacto            VARCHAR(160),
    total_vistas        INT          DEFAULT 0,
    total_ofertas       INT          DEFAULT 0,
    fecha_publicacion   DATE         NOT NULL,
    fecha_actualizacion DATETIME,
    observaciones       TEXT,
    FOREIGN KEY (finca_id)    REFERENCES fincas(id)   ON DELETE SET NULL,
    FOREIGN KEY (vendedor_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_estado (estado),
    INDEX idx_depto (departamento),
    INDEX idx_raza (raza)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS marketplace_listing_animales (
    listing_id INT NOT NULL,
    animal_id  INT NOT NULL,
    arete      VARCHAR(50),
    PRIMARY KEY (listing_id, animal_id),
    FOREIGN KEY (listing_id) REFERENCES marketplace_listings(id) ON DELETE CASCADE,
    FOREIGN KEY (animal_id)  REFERENCES animales(id)             ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 17. PUJAS / OFERTAS
-- =====================================================================
CREATE TABLE IF NOT EXISTS marketplace_pujas (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    listing_id        INT          NOT NULL,
    comprador_id      INT,
    comprador_nombre  VARCHAR(160) NOT NULL,
    comprador_contacto VARCHAR(160),
    valor_oferta      DECIMAL(14,2) NOT NULL,
    contra_oferta     DECIMAL(14,2),
    estado            VARCHAR(40)  NOT NULL DEFAULT 'PENDIENTE'
        COMMENT 'PENDIENTE, OFERTA_BAJA, ACEPTABLE, EN_NEGOCIACION, ACEPTADA, RECHAZADA, CONTRA_OFERTA, EXPIRADA',
    fecha             DATETIME     NOT NULL,
    observaciones     TEXT,
    FOREIGN KEY (listing_id)   REFERENCES marketplace_listings(id) ON DELETE CASCADE,
    FOREIGN KEY (comprador_id) REFERENCES usuarios(id)             ON DELETE SET NULL,
    INDEX idx_listing (listing_id),
    INDEX idx_estado (estado)
) ENGINE=InnoDB;

-- =====================================================================
-- 18. VENTAS Y COMISIONES
-- =====================================================================
CREATE TABLE IF NOT EXISTS marketplace_ventas (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    listing_id    INT          NOT NULL,
    puja_id       INT,
    vendedor_id   INT,
    comprador_id  INT,
    valor_final   DECIMAL(14,2) NOT NULL,
    comision      DECIMAL(14,2),
    neto_vendedor DECIMAL(14,2),
    fecha         DATETIME      NOT NULL,
    estado_pago   VARCHAR(40)   DEFAULT 'PENDIENTE',
    estado_entrega VARCHAR(40)  DEFAULT 'PENDIENTE',
    observaciones TEXT,
    FOREIGN KEY (listing_id)   REFERENCES marketplace_listings(id) ON DELETE RESTRICT,
    FOREIGN KEY (puja_id)      REFERENCES marketplace_pujas(id)    ON DELETE SET NULL,
    FOREIGN KEY (vendedor_id)  REFERENCES usuarios(id)             ON DELETE SET NULL,
    FOREIGN KEY (comprador_id) REFERENCES usuarios(id)             ON DELETE SET NULL,
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS marketplace_comisiones (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    venta_id        INT          NOT NULL,
    porcentaje      DECIMAL(5,2) NOT NULL,
    valor_comision  DECIMAL(14,2) NOT NULL,
    fecha           DATETIME      NOT NULL,
    estado_cobro    VARCHAR(40)   DEFAULT 'PENDIENTE',
    FOREIGN KEY (venta_id) REFERENCES marketplace_ventas(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS marketplace_favoritos (
    usuario_id INT NOT NULL,
    listing_id INT NOT NULL,
    fecha      DATETIME NOT NULL,
    PRIMARY KEY (usuario_id, listing_id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)             ON DELETE CASCADE,
    FOREIGN KEY (listing_id) REFERENCES marketplace_listings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 19. ALERTAS (segmentadas con 10 categorías y 5 estados)
-- =====================================================================
CREATE TABLE IF NOT EXISTS alertas (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    finca_id    INT,
    categoria   VARCHAR(40)  NOT NULL
        COMMENT 'VACUNAS, SANIDAD, STOCK, POTREROS, ALIMENTACION, MARKETPLACE, PEDIDOS, VENTAS, USUARIOS, SISTEMA',
    severidad   VARCHAR(20)  NOT NULL DEFAULT 'INFO' COMMENT 'CRITICA, ALTA, MEDIA, INFO',
    titulo      VARCHAR(200) NOT NULL,
    descripcion TEXT,
    referencia_tipo VARCHAR(40) COMMENT 'animal, listing, puja, inventario, potrero, usuario',
    referencia_id   INT,
    estado      VARCHAR(20)  NOT NULL DEFAULT 'NUEVA'
        COMMENT 'NUEVA, VISTA, EN_PROCESO, RESUELTA, IGNORADA',
    asignado_a  INT,
    fecha_creacion  DATETIME NOT NULL,
    fecha_actualizacion DATETIME,
    FOREIGN KEY (asignado_a) REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_categoria (categoria),
    INDEX idx_estado (estado),
    INDEX idx_severidad (severidad)
) ENGINE=InnoDB;

-- =====================================================================
-- 20. NOTIFICACIONES (campana, push, email, sms)
-- =====================================================================
CREATE TABLE IF NOT EXISTS notificaciones (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  INT          NOT NULL,
    tipo        VARCHAR(30)  NOT NULL COMMENT 'TOAST, CAMPANA, MODAL, PUSH, EMAIL, SMS, WHATSAPP',
    canal       VARCHAR(30)  COMMENT 'in-app, email, sms, whatsapp, push',
    asunto      VARCHAR(200),
    mensaje     TEXT         NOT NULL,
    alerta_id   INT,
    leida       BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha       DATETIME     NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (alerta_id)  REFERENCES alertas(id)  ON DELETE SET NULL,
    INDEX idx_usuario_leida (usuario_id, leida),
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB;

-- =====================================================================
-- 21. AUDITORÍA
-- =====================================================================
CREATE TABLE IF NOT EXISTS auditoria (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  INT,
    accion      VARCHAR(100) NOT NULL COMMENT 'CREAR, EDITAR, ELIMINAR, LOGIN, LOGOUT, APROBAR, RECHAZAR',
    modulo      VARCHAR(100),
    entidad     VARCHAR(100),
    entidad_id  INT,
    descripcion TEXT,
    ip_origen   VARCHAR(60),
    fecha       DATETIME     NOT NULL,
    INDEX idx_fecha (fecha),
    INDEX idx_usuario (usuario_id)
) ENGINE=InnoDB;

-- =====================================================================
-- 22. CONFIGURACIÓN DEL SISTEMA
-- =====================================================================
CREATE TABLE IF NOT EXISTS configuracion (
    id                   INT AUTO_INCREMENT PRIMARY KEY,
    clave                VARCHAR(100) NOT NULL UNIQUE,
    valor                TEXT,
    descripcion          TEXT,
    fecha_actualizacion  DATETIME     NOT NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 23. SESIONES
-- =====================================================================
CREATE TABLE IF NOT EXISTS sesiones (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   INT       NOT NULL,
    token        VARCHAR(255),
    finca_id     INT,
    ip_origen    VARCHAR(60),
    fecha_inicio DATETIME  NOT NULL,
    fecha_fin    DATETIME,
    activa       BOOLEAN   NOT NULL DEFAULT TRUE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 24. REPORTES GENERADOS
-- =====================================================================
CREATE TABLE IF NOT EXISTS reportes_generados (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id       INT,
    finca_id         INT,
    tipo_reporte     VARCHAR(100),
    parametros       TEXT,
    ruta_archivo     VARCHAR(500),
    fecha_generacion DATETIME NOT NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 25. HISTORIAL DE ACCIONES (detallado)
-- =====================================================================
CREATE TABLE IF NOT EXISTS historial_acciones (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id  INT,
    finca_id    INT,
    tipo        VARCHAR(100),
    descripcion TEXT,
    datos_json  JSON,
    fecha       DATETIME NOT NULL,
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB;

-- =====================================================================
-- DATOS INICIALES — ROLES Y PERMISOS
-- =====================================================================
INSERT INTO roles (codigo, etiqueta, descripcion) VALUES
('ADMINISTRADOR',     'Administrador',     'Acceso total al sistema'),
('VENDEDOR_GANADERO', 'Vendedor Ganadero', 'Publica animales y gestiona ofertas'),
('COMPRADOR',         'Comprador',         'Ve marketplace, hace pujas, guarda favoritos'),
('OPERARIO',          'Operario',          'Gestión diaria de la finca'),
('VETERINARIO',       'Veterinario',       'Gestiona sanidad y vacunas'),
('MESERO',            'Mesero',            'Pedidos desde celular (módulo restaurante)'),
('COCINA',            'Cocina',            'Estación de cocina'),
('CAJA',              'Caja',              'Cobros y cierre de caja');

INSERT INTO permisos (codigo, descripcion) VALUES
('marketplace.publicar',  'Publicar animales en el marketplace'),
('marketplace.gestionar', 'Aceptar/rechazar/contra ofertar pujas'),
('marketplace.ver_minimo','Ver precio mínimo privado'),
('marketplace.pujar',     'Hacer pujas sobre publicaciones'),
('usuarios.crud',         'Crear/editar/bloquear usuarios'),
('reportes.ver',          'Consultar reportes'),
('reportes.exportar',     'Exportar reportes'),
('finca.editar',          'Editar parámetros de finca'),
('sanidad.crud',          'Gestionar registros sanitarios'),
('vacunas.crud',          'Gestionar vacunas'),
('inventario.crud',       'Gestionar inventario');

-- DATOS INICIALES — 5 FINCAS
INSERT INTO fincas (nombre, ubicacion, departamento, ciudad, propietario, area_hectareas, descripcion, estado, fecha_creacion)
VALUES
('La Esperanza','Cundinamarca, Colombia','Cundinamarca','Bogotá','Carlos Rodríguez',450.0,'Finca ganadera principal con praderas mejoradas','Activa','2020-01-15'),
('Costa Rica','Córdoba, Colombia','Córdoba','Montería','María González',380.0,'Excelentes condiciones para ganadería de levante','Activa','2019-06-10'),
('La Argentina','Antioquia, Colombia','Antioquia','Medellín','Jorge Pérez',520.0,'Finca con sistemas de rotación avanzados','Activa','2018-03-22'),
('La Selva','Meta, Colombia','Meta','Villavicencio','Ana Martínez',610.0,'Ganadería extensiva en zona tropical húmeda','Activa','2021-09-05'),
('San Pedro','Boyacá, Colombia','Boyacá','Tunja','Luis Herrera',290.0,'Finca de altiplano con ganado de doble propósito','Activa','2017-11-30');

-- LOTES (3 por finca = 15 total)
INSERT INTO lotes (finca_id, nombre, capacidad, descripcion, estado, fecha_creacion)
VALUES
(1,'Potrero Norte',15,'Pradera kikuyo con riego','Activo','2020-02-01'),
(1,'Potrero Sur',20,'Pradera brome con sombra natural','Activo','2020-02-01'),
(1,'Potrero Oriente',12,'Zona de recuperación','Activo','2020-02-01'),
(2,'Pradera Alta',18,'Pasto estrella africana en buen estado','Activo','2019-07-01'),
(2,'Pradera Baja',15,'Zona húmeda con pasto pangola','Activo','2019-07-01'),
(2,'Pradera Central',10,'Potrero de manejo intensivo','Activo','2019-07-01'),
(3,'Sector A',20,'Pasturas mejoradas con Brachiaria','Activo','2018-04-01'),
(3,'Sector B',15,'Pradera con sistema silvopastoril','Activo','2018-04-01'),
(3,'Sector C',12,'Zona de renovación de pasturas','Activo','2018-04-01'),
(4,'Zona Norte',16,'Pasturas naturales de sabana','Activo','2021-10-01'),
(4,'Zona Sur',14,'Brachiaria humidicola establecida','Activo','2021-10-01'),
(4,'Zona Central',18,'Área de engorde intensivo','Activo','2021-10-01'),
(5,'Potrero 1',20,'Ray grass con trébol rojo','Activo','2017-12-01'),
(5,'Potrero 2',15,'Pradera de kikuyo fertilizado','Activo','2017-12-01'),
(5,'Potrero 3',12,'Zona de maternidad','Activo','2017-12-01');

-- USUARIOS demo (los hash deben ser reemplazados por el backend con SHA-256 + sal real)
INSERT INTO usuarios (nombre, username, email, telefono, password_hash, rol, estado, fecha_creacion)
VALUES
('Administrador General',         'admin',       'admin@sistemaganadero.co',     '+57 301 389 6861', 'SHA256_HASH', 'ADMINISTRADOR',    'Activo','2024-01-01'),
('Carlos Rodríguez (Vendedor)',   'vendedor',    'carlos@sistemaganadero.co',    '+57 310 555 1234', 'SHA256_HASH', 'VENDEDOR_GANADERO','Activo','2024-01-01'),
('Juan Pérez (Comprador)',        'comprador',   'juan@sistemaganadero.co',      '+57 315 444 5566', 'SHA256_HASH', 'COMPRADOR',        'Activo','2024-01-01'),
('Operario Principal',            'operario',    'operario@sistemaganadero.co',  '+57 311 222 3344', 'SHA256_HASH', 'OPERARIO',         'Activo','2024-01-01'),
('Dr. Veterinario de Campo',      'veterinario', 'vet@sistemaganadero.co',       '+57 312 555 1212', 'SHA256_HASH', 'VETERINARIO',      'Activo','2024-01-01'),
('Mesero de Sala',                'mesero',      'mesero@sistemaganadero.co',    '+57 316 777 8899', 'SHA256_HASH', 'MESERO',           'Activo','2024-01-01'),
('Estación de Cocina',            'cocina',      'cocina@sistemaganadero.co',    '+57 317 666 5544', 'SHA256_HASH', 'COCINA',           'Activo','2024-01-01'),
('Caja Principal',                'caja',        'caja@sistemaganadero.co',      '+57 318 999 1010', 'SHA256_HASH', 'CAJA',             'Activo','2024-01-01');

-- CONFIGURACION INICIAL
INSERT INTO configuracion (clave, valor, descripcion, fecha_actualizacion) VALUES
('gdp_rentable_min',    '0.90', 'GDP mínimo para Rentable (kg/día)',          NOW()),
('gdp_mantener_min',    '0.70', 'GDP mínimo para Mantener (kg/día)',          NOW()),
('gdp_bajo_min',        '0.50', 'GDP mínimo para Bajo (kg/día)',              NOW()),
('moneda',              'COP',  'Moneda del sistema',                          NOW()),
('nombre_empresa',      'Sistema Ganadero', 'Nombre del sistema',              NOW()),
('telefono_soporte',    '+57 301 389 6861', 'Teléfono de soporte comercial',  NOW()),
('comision_marketplace','3.0',  'Porcentaje de comisión por defecto',         NOW()),
('version',             '2.0.0','Versión del sistema',                         NOW());
