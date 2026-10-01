package com.sistemaganadero.database;

import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseInitializer {

    private static final Logger LOG = Logger.getLogger(DatabaseInitializer.class.getName());

    private DatabaseInitializer() {}

    public static void initialize() {
        if (!DatabaseConnection.isConfigured()) return;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            createTables(st);
            seedData(st);
            LOG.info("Base de datos inicializada correctamente.");
        } catch (Exception e) {
            LOG.warning("Error inicializando base de datos: " + e.getMessage());
        }
    }

    private static void createTables(Statement st) throws Exception {
        st.execute("""
            CREATE TABLE IF NOT EXISTS usuarios (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nombre VARCHAR(100) NOT NULL,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100),
                password_hash VARCHAR(255) NOT NULL,
                rol VARCHAR(50) NOT NULL DEFAULT 'Operario',
                estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
                fecha_creacion DATE NOT NULL,
                ultimo_acceso DATETIME
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS fincas (
                id INT AUTO_INCREMENT PRIMARY KEY,
                nombre VARCHAR(100) NOT NULL,
                ubicacion VARCHAR(200),
                propietario VARCHAR(100),
                area_hectareas DECIMAL(10,2),
                descripcion TEXT,
                estado VARCHAR(20) NOT NULL DEFAULT 'Activa',
                fecha_creacion DATE NOT NULL
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS lotes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                finca_id INT NOT NULL,
                nombre VARCHAR(100) NOT NULL,
                capacidad INT,
                descripcion TEXT,
                estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
                fecha_creacion DATE NOT NULL,
                FOREIGN KEY (finca_id) REFERENCES fincas(id)
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS animales (
                id INT AUTO_INCREMENT PRIMARY KEY,
                finca_id INT NOT NULL,
                lote_id INT,
                arete VARCHAR(50) NOT NULL,
                nombre VARCHAR(100),
                sexo VARCHAR(10) NOT NULL,
                raza VARCHAR(100),
                fecha_nacimiento DATE,
                peso_inicial DECIMAL(8,2),
                peso_actual DECIMAL(8,2),
                gdp DECIMAL(6,3),
                estado_reproductivo VARCHAR(50),
                estado_sanitario VARCHAR(50),
                observaciones TEXT,
                activo BOOLEAN NOT NULL DEFAULT TRUE,
                fecha_registro DATE NOT NULL,
                FOREIGN KEY (finca_id) REFERENCES fincas(id),
                FOREIGN KEY (lote_id) REFERENCES lotes(id)
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS terneros (
                id INT AUTO_INCREMENT PRIMARY KEY,
                animal_madre_id INT,
                finca_id INT NOT NULL,
                lote_id INT,
                codigo VARCHAR(50) NOT NULL,
                sexo VARCHAR(10) NOT NULL,
                raza VARCHAR(100),
                fecha_nacimiento DATE,
                peso_nacimiento DECIMAL(8,2),
                estado VARCHAR(50),
                observaciones TEXT,
                FOREIGN KEY (finca_id) REFERENCES fincas(id)
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS pesajes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                animal_id INT NOT NULL,
                finca_id INT NOT NULL,
                fecha DATE NOT NULL,
                peso DECIMAL(8,2) NOT NULL,
                observaciones TEXT,
                usuario_id INT,
                FOREIGN KEY (animal_id) REFERENCES animales(id),
                FOREIGN KEY (finca_id) REFERENCES fincas(id)
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS vacunas (
                id INT AUTO_INCREMENT PRIMARY KEY,
                animal_id INT NOT NULL,
                finca_id INT NOT NULL,
                nombre_vacuna VARCHAR(150) NOT NULL,
                fecha_aplicacion DATE NOT NULL,
                proxima_dosis DATE,
                responsable VARCHAR(100),
                observaciones TEXT,
                FOREIGN KEY (animal_id) REFERENCES animales(id),
                FOREIGN KEY (finca_id) REFERENCES fincas(id)
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS movimientos (
                id INT AUTO_INCREMENT PRIMARY KEY,
                animal_id INT NOT NULL,
                finca_origen_id INT,
                finca_destino_id INT,
                lote_origen_id INT,
                lote_destino_id INT,
                fecha DATE NOT NULL,
                tipo_movimiento VARCHAR(100) NOT NULL,
                observaciones TEXT,
                usuario_id INT,
                FOREIGN KEY (animal_id) REFERENCES animales(id)
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS inventario (
                id INT AUTO_INCREMENT PRIMARY KEY,
                finca_id INT NOT NULL,
                codigo VARCHAR(50),
                producto VARCHAR(150) NOT NULL,
                categoria VARCHAR(100),
                cantidad DECIMAL(10,2) NOT NULL,
                unidad VARCHAR(50),
                stock_minimo DECIMAL(10,2),
                costo_unitario DECIMAL(12,2),
                fecha_ingreso DATE NOT NULL,
                estado VARCHAR(50) NOT NULL DEFAULT 'Disponible',
                FOREIGN KEY (finca_id) REFERENCES fincas(id)
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS imagenes_animales (
                id INT AUTO_INCREMENT PRIMARY KEY,
                animal_id INT NOT NULL,
                finca_id INT NOT NULL,
                nombre_archivo VARCHAR(255),
                ruta_url VARCHAR(500),
                tipo VARCHAR(50),
                fecha_subida DATETIME NOT NULL,
                activo BOOLEAN NOT NULL DEFAULT TRUE,
                FOREIGN KEY (animal_id) REFERENCES animales(id),
                FOREIGN KEY (finca_id) REFERENCES fincas(id)
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS auditoria (
                id INT AUTO_INCREMENT PRIMARY KEY,
                usuario_id INT,
                accion VARCHAR(100) NOT NULL,
                modulo VARCHAR(100),
                entidad VARCHAR(100),
                entidad_id INT,
                descripcion TEXT,
                fecha DATETIME NOT NULL
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS configuracion (
                id INT AUTO_INCREMENT PRIMARY KEY,
                clave VARCHAR(100) NOT NULL UNIQUE,
                valor TEXT,
                descripcion TEXT,
                fecha_actualizacion DATETIME NOT NULL
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS sesiones (
                id INT AUTO_INCREMENT PRIMARY KEY,
                usuario_id INT NOT NULL,
                token VARCHAR(255),
                finca_id INT,
                fecha_inicio DATETIME NOT NULL,
                fecha_fin DATETIME,
                activa BOOLEAN NOT NULL DEFAULT TRUE
            )""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS reportes_generados (
                id INT AUTO_INCREMENT PRIMARY KEY,
                usuario_id INT,
                finca_id INT,
                tipo_reporte VARCHAR(100),
                parametros TEXT,
                ruta_archivo VARCHAR(500),
                fecha_generacion DATETIME NOT NULL
            )""");
    }

    private static void seedData(Statement st) throws Exception {
        // Insertar fincas solo si no existen
        st.execute("""
            INSERT IGNORE INTO fincas (id, nombre, ubicacion, propietario, area_hectareas, descripcion, estado, fecha_creacion)
            VALUES
            (1,'La Esperanza','Cundinamarca, Colombia','Carlos Rodríguez',450.0,'Finca ganadera principal con praderas mejoradas','Activa','2020-01-15'),
            (2,'Costa Rica','Córdoba, Colombia','María González',380.0,'Excelentes condiciones para ganadería de levante','Activa','2019-06-10'),
            (3,'La Argentina','Antioquia, Colombia','Jorge Pérez',520.0,'Finca con sistemas de rotación avanzados','Activa','2018-03-22'),
            (4,'La Selva','Meta, Colombia','Ana Martínez',610.0,'Ganadería extensiva en zona tropical húmeda','Activa','2021-09-05'),
            (5,'San Pedro','Boyacá, Colombia','Luis Herrera',290.0,'Finca de altiplano con ganado de doble propósito','Activa','2017-11-30')
            """);

        // Lotes por finca
        st.execute("""
            INSERT IGNORE INTO lotes (id, finca_id, nombre, capacidad, descripcion, estado, fecha_creacion)
            VALUES
            (1,1,'Potrero Norte',15,'Pradera kikuyo con riego','Activo','2020-02-01'),
            (2,1,'Potrero Sur',20,'Pradera brome con sombra natural','Activo','2020-02-01'),
            (3,1,'Potrero Oriente',12,'Zona de recuperación','Activo','2020-02-01'),
            (4,2,'Pradera Alta',18,'Pasto estrella africana en buen estado','Activo','2019-07-01'),
            (5,2,'Pradera Baja',15,'Zona húmeda con pasto pangola','Activo','2019-07-01'),
            (6,2,'Pradera Central',10,'Potrero de manejo intensivo','Activo','2019-07-01'),
            (7,3,'Sector A',20,'Pasturas mejoradas con Brachiaria','Activo','2018-04-01'),
            (8,3,'Sector B',15,'Pradera con sistema silvopastoril','Activo','2018-04-01'),
            (9,3,'Sector C',12,'Zona de renovación de pasturas','Activo','2018-04-01'),
            (10,4,'Zona Norte',16,'Pasturas naturales de sabana','Activo','2021-10-01'),
            (11,4,'Zona Sur',14,'Brachiaria humidicola establecida','Activo','2021-10-01'),
            (12,4,'Zona Central',18,'Área de engorde intensivo','Activo','2021-10-01'),
            (13,5,'Potrero 1',20,'Ray grass con trébol rojo','Activo','2017-12-01'),
            (14,5,'Potrero 2',15,'Pradera de kikuyo fertilizado','Activo','2017-12-01'),
            (15,5,'Potrero 3',12,'Zona de maternidad','Activo','2017-12-01')
            """);

        // Animales por finca (5 por finca = 25 total)
        st.execute("""
            INSERT IGNORE INTO animales (id,finca_id,lote_id,arete,nombre,sexo,raza,fecha_nacimiento,peso_inicial,peso_actual,gdp,estado_reproductivo,estado_sanitario,observaciones,activo,fecha_registro)
            VALUES
            (1,1,1,'E-1001','Nube','Macho','Normando','2022-03-15',280,385,0.92,'Normal','Rentable','Animal en excelente condición',TRUE,'2022-03-20'),
            (2,1,2,'E-1022','Rayo','Macho','Normando','2021-08-10',340,416,0.75,'Normal','Mantener','Monitorear GDP mensualmente',TRUE,'2021-08-15'),
            (3,1,2,'E-1045','Luna','Hembra','Brauvión','2023-01-20',240,310,0.48,'Normal','Bajo','GDP por debajo del esperado',TRUE,'2023-01-25'),
            (4,1,3,'E-1019','Brisa','Hembra','Simmental','2022-11-05',260,312,0.35,'Normal','No Rentable','Requiere evaluación veterinaria urgente',TRUE,'2022-11-10'),
            (5,1,1,'E-1067','Trueno','Macho','Normando','2023-05-12',200,280,0.95,'Normal','Rentable','Excelente ganancia diaria',TRUE,'2023-05-18'),
            (6,2,4,'C-2001','Paloma','Hembra','Holstein','2022-02-20',300,415,0.88,'Preñada','Mantener','Próximo parto en 45 días',TRUE,'2022-02-25'),
            (7,2,4,'C-2034','Toro','Macho','Normando','2021-09-14',340,450,1.05,'Normal','Rentable','Mejor GDP del lote',TRUE,'2021-09-20'),
            (8,2,5,'C-2018','Viento','Macho','Brauvión','2023-03-01',220,295,0.48,'Normal','Bajo','Revisar alimentación',TRUE,'2023-03-08'),
            (9,2,6,'C-2056','Estrella','Hembra','Holstein','2022-07-22',280,380,0.82,'Normal','Mantener','Productora de leche establecida',TRUE,'2022-07-28'),
            (10,2,5,'C-2089','Sol','Macho','Simmental','2023-08-15',190,255,0.32,'Normal','No Rentable','GDP muy bajo, considerar descarte',TRUE,'2023-08-20'),
            (11,3,7,'A-3001','Rosa','Hembra','Normando','2022-04-10',260,365,0.88,'Preñada','Mantener','En buen estado reproductivo',TRUE,'2022-04-15'),
            (12,3,7,'A-3022','Torito','Macho','Brauvión','2021-11-30',350,460,1.10,'Normal','Rentable','Animal estrella del lote',TRUE,'2021-12-05'),
            (13,3,8,'A-3045','Flor','Hembra','Simmental','2023-02-14',230,305,0.46,'Normal','Bajo','Evaluar suplementación',TRUE,'2023-02-20'),
            (14,3,9,'A-3067','Vaquito','Macho','Holstein','2022-09-01',290,395,0.90,'Normal','Rentable','GDP estable',TRUE,'2022-09-07'),
            (15,3,8,'A-3089','Lluvia','Macho','Normando','2023-06-20',210,275,0.40,'Normal','No Rentable','GDP insuficiente',TRUE,'2023-06-25'),
            (16,4,10,'S-4001','León','Macho','Normando','2022-01-15',310,415,0.91,'Normal','Rentable','GDP consistente en todo el período',TRUE,'2022-01-20'),
            (17,4,11,'S-4022','Pampa','Hembra','Holstein','2021-07-22',285,390,0.84,'Preñada','Mantener','Segunda preñez en curso',TRUE,'2021-07-28'),
            (18,4,10,'S-4045','Río','Macho','Brauvión','2023-04-08',240,318,0.49,'Normal','Bajo','Bajo peso relativo a la raza',TRUE,'2023-04-14'),
            (19,4,12,'S-4067','Campo','Hembra','Simmental','2022-10-12',255,350,0.82,'Normal','Mantener','Buena adaptación a zona tropical',TRUE,'2022-10-18'),
            (20,4,11,'S-4089','Selva','Macho','Normando','2023-07-30',180,240,0.37,'Normal','No Rentable','GDP muy bajo para su edad',TRUE,'2023-08-05'),
            (21,5,13,'P-5001','Boyaco','Macho','Simmental','2022-06-10',295,405,0.96,'Normal','Rentable','Excelente adaptación a altiplano',TRUE,'2022-06-15'),
            (22,5,14,'P-5022','Nieva','Hembra','Normando','2021-12-20',270,375,0.86,'Normal','Mantener','GDP estable todo el año',TRUE,'2021-12-26'),
            (23,5,13,'P-5045','Cerro','Macho','Holstein','2023-01-05',250,335,0.75,'Normal','Mantener','GDP por encima del promedio',TRUE,'2023-01-10'),
            (24,5,15,'P-5067','Nevada','Hembra','Brauvión','2022-08-18',240,330,0.68,'Normal','Mantener','GDP aceptable para la raza',TRUE,'2022-08-24'),
            (25,5,14,'P-5089','Pico','Macho','Normando','2023-09-01',175,230,0.33,'Normal','No Rentable','Revisión nutricional urgente',TRUE,'2023-09-07')
            """);

        // Inventario por finca (4-5 items por finca)
        st.execute("""
            INSERT IGNORE INTO inventario (finca_id,codigo,producto,categoria,cantidad,unidad,stock_minimo,costo_unitario,fecha_ingreso,estado)
            VALUES
            (1,'E-INS-01','Sal mineralizada','Suplementos',18,'bultos',10,95000,'2026-01-10','Disponible'),
            (1,'E-INS-02','Vacuna clostridial','Sanidad',8,'frascos',5,78000,'2026-01-10','Disponible'),
            (1,'E-INS-03','Melaza','Alimentos',420,'kg',150,1800,'2026-01-10','Disponible'),
            (1,'E-INS-04','Concentrado levante','Alimentos',12,'bultos',15,118000,'2026-01-10','Bajo minimo'),
            (2,'C-INS-01','Sal mineral blanca','Suplementos',22,'bultos',8,88000,'2026-01-12','Disponible'),
            (2,'C-INS-02','Ivermectina 1%','Sanidad',14,'frascos',6,45000,'2026-01-12','Disponible'),
            (2,'C-INS-03','Pasto estrella semilla','Alimentos',80,'kg',30,12000,'2026-01-12','Disponible'),
            (3,'A-INS-01','Urea forrajera','Suplementos',35,'kg',20,5500,'2026-01-15','Disponible'),
            (3,'A-INS-02','Vacuna aftosa','Sanidad',6,'dosis',10,25000,'2026-01-15','Bajo minimo'),
            (3,'A-INS-03','Bloques nutricionales','Suplementos',9,'unidades',5,185000,'2026-01-15','Disponible'),
            (4,'S-INS-01','Sal mineralizada trópico','Suplementos',28,'bultos',12,98000,'2026-02-01','Disponible'),
            (4,'S-INS-02','Antibiótico oxitetraciclina','Sanidad',10,'frascos',8,67000,'2026-02-01','Disponible'),
            (4,'S-INS-03','Semilla Brachiaria','Alimentos',55,'kg',25,18000,'2026-02-01','Disponible'),
            (5,'P-INS-01','Sal mineralizada altiplano','Suplementos',16,'bultos',10,102000,'2026-01-20','Disponible'),
            (5,'P-INS-02','Vitaminas ADE','Sanidad',12,'frascos',5,55000,'2026-01-20','Disponible'),
            (5,'P-INS-03','Concentrado doble propósito','Alimentos',8,'bultos',10,125000,'2026-01-20','Bajo minimo')
            """);

        // Usuarios
        st.execute("""
            INSERT IGNORE INTO usuarios (id,nombre,username,email,password_hash,rol,estado,fecha_creacion)
            VALUES
            (1,'Administrador General','admin','admin@sistemaganadero.com','$2a$12$hashAdmin','Administrador','Activo','2024-01-01'),
            (2,'Operario Principal','operario','operario@sistemaganadero.com','$2a$12$hashOperario','Operario','Activo','2024-01-01'),
            (3,'Veterinario de Campo','veterinario','vet@sistemaganadero.com','$2a$12$hashVet','Veterinario','Activo','2024-01-01')
            """);

        // Vacunas próximas
        st.execute("""
            INSERT IGNORE INTO vacunas (animal_id,finca_id,nombre_vacuna,fecha_aplicacion,proxima_dosis,responsable,observaciones)
            VALUES
            (2,1,'Vacuna clostridial','2026-01-15','2026-04-25','Dr. García','Dosis de refuerzo próxima'),
            (3,1,'Vacuna aftosa','2025-10-01','2026-04-28','Dr. García','Biestral requerida'),
            (7,2,'Vacuna brucelosis','2026-02-10','2026-04-30','Dr. López','Vacuna obligatoria'),
            (11,3,'Vacuna carbón sintomático','2026-01-20','2026-04-26','Dr. Martínez','Refuerzo anual')
            """);

        // Configuracion inicial
        st.execute("""
            INSERT IGNORE INTO configuracion (clave,valor,descripcion,fecha_actualizacion)
            VALUES
            ('gdp_rentable_min','0.90','GDP mínimo para clasificar como Rentable (kg/día)','2026-01-01 00:00:00'),
            ('gdp_mantener_min','0.70','GDP mínimo para clasificar como Mantener (kg/día)','2026-01-01 00:00:00'),
            ('gdp_bajo_min','0.50','GDP mínimo para clasificar como Bajo (kg/día)','2026-01-01 00:00:00'),
            ('moneda','COP','Moneda del sistema','2026-01-01 00:00:00'),
            ('nombre_empresa','Sistema Ganadero','Nombre del sistema','2026-01-01 00:00:00')
            """);
    }
}
