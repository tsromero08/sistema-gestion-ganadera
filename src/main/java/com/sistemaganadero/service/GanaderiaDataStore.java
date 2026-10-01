package com.sistemaganadero.service;

import com.sistemaganadero.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.*;

public class GanaderiaDataStore {

    private static final ObservableList<Finca> FINCAS = FXCollections.observableArrayList();
    private static final Map<Integer, ObservableList<Lote>> LOTES_POR_FINCA = new HashMap<>();
    private static final Map<Integer, ObservableList<Animal>> ANIMALES_POR_FINCA = new HashMap<>();
    private static final Map<Integer, ObservableList<InventarioItem>> INVENTARIO_POR_FINCA = new HashMap<>();
    private static final Map<Integer, ObservableList<Pesaje>> PESAJES_POR_FINCA = new HashMap<>();
    private static final Map<Integer, ObservableList<Vacuna>> VACUNAS_POR_FINCA = new HashMap<>();
    private static final Map<Integer, ObservableList<Potrero>> POTREROS_POR_FINCA = new HashMap<>();
    private static final Map<Integer, ObservableList<RegistroSanitario>> SANITARIOS_POR_FINCA = new HashMap<>();
    private static final ObservableList<MarketplaceListing> MARKETPLACE = FXCollections.observableArrayList();
    private static final ObservableList<Puja> PUJAS = FXCollections.observableArrayList();
    private static final ObservableList<Ternero> TERNEROS_LEGACY = FXCollections.observableArrayList();
    private static boolean initialized = false;

    private static final Random RANDOM = new Random();
    private static final String[] FINCA_PREFIJOS = {"E", "C", "A", "S", "P"};

    private GanaderiaDataStore() {}

    public static void initializeData() {
        if (initialized) return;
        crearFincas();
        crearLotes();
        crearAnimales();
        crearInventario();
        crearPesajes();
        crearVacunas();
        crearPotreros();
        crearRegistrosSanitarios();
        crearMarketplaceDemo();
        crearTernerosLegacy();
        initialized = true;
    }

    // ---- ID Generation ----

    public static String generateUniqueArete(int fincaId) {
        initializeData();
        String prefix = (fincaId >= 1 && fincaId <= 5) ? FINCA_PREFIJOS[fincaId - 1] : "X";
        Set<String> existentes = new HashSet<>();
        ANIMALES_POR_FINCA.values().forEach(lista -> lista.forEach(a -> existentes.add(a.getArete())));
        String candidato;
        do {
            candidato = prefix + "-" + (RANDOM.nextInt(9000) + 1000);
        } while (existentes.contains(candidato));
        return candidato;
    }

    public static String generateInventoryCode(int fincaId) {
        initializeData();
        String prefix = (fincaId >= 1 && fincaId <= 5) ? FINCA_PREFIJOS[fincaId - 1] : "X";
        Set<String> existentes = new HashSet<>();
        INVENTARIO_POR_FINCA.values().forEach(lista -> lista.forEach(i -> existentes.add(i.getCodigo())));
        String candidato;
        do {
            candidato = prefix + "-INV-" + (RANDOM.nextInt(9000) + 1000);
        } while (existentes.contains(candidato));
        return candidato;
    }

    public static String generatePotreroCode(int fincaId) {
        initializeData();
        String prefix = (fincaId >= 1 && fincaId <= 5) ? FINCA_PREFIJOS[fincaId - 1] : "X";
        Set<String> existentes = new HashSet<>();
        POTREROS_POR_FINCA.values().forEach(lista -> lista.forEach(p -> existentes.add(p.getCodigo())));
        String candidato;
        do {
            candidato = prefix + "-POT-" + (RANDOM.nextInt(900) + 100);
        } while (existentes.contains(candidato));
        return candidato;
    }

    public static String generateMarketplaceCode() {
        initializeData();
        Set<String> existentes = new HashSet<>();
        MARKETPLACE.forEach(m -> existentes.add(m.getCodigo()));
        String candidato;
        do {
            candidato = "MKT-" + (RANDOM.nextInt(90000) + 10000);
        } while (existentes.contains(candidato));
        return candidato;
    }

    // ---- Data Initialization ----

    private static void crearFincas() {
        FINCAS.addAll(
            new Finca(1, "La Esperanza", "Cundinamarca, Colombia", "Carlos Rodríguez", 450.0, "Finca ganadera principal con praderas mejoradas"),
            new Finca(2, "Costa Rica", "Córdoba, Colombia", "María González", 380.0, "Excelentes condiciones para ganadería de levante"),
            new Finca(3, "La Argentina", "Antioquia, Colombia", "Jorge Pérez", 520.0, "Finca con sistemas de rotación avanzados"),
            new Finca(4, "La Selva", "Meta, Colombia", "Ana Martínez", 610.0, "Ganadería extensiva en zona tropical húmeda"),
            new Finca(5, "San Pedro", "Boyacá, Colombia", "Luis Herrera", 290.0, "Finca de altiplano con ganado de doble propósito")
        );
    }

    private static void crearLotes() {
        LOTES_POR_FINCA.put(1, FXCollections.observableArrayList(
            new Lote(1, 1, "Potrero Norte", 15, "Pradera kikuyo con riego"),
            new Lote(2, 1, "Potrero Sur", 20, "Pradera brome con sombra natural"),
            new Lote(3, 1, "Potrero Oriente", 12, "Zona de recuperación")
        ));
        LOTES_POR_FINCA.put(2, FXCollections.observableArrayList(
            new Lote(4, 2, "Pradera Alta", 18, "Pasto estrella africana en buen estado"),
            new Lote(5, 2, "Pradera Baja", 15, "Zona húmeda con pasto pangola"),
            new Lote(6, 2, "Pradera Central", 10, "Potrero de manejo intensivo")
        ));
        LOTES_POR_FINCA.put(3, FXCollections.observableArrayList(
            new Lote(7, 3, "Sector A", 20, "Pasturas mejoradas con Brachiaria"),
            new Lote(8, 3, "Sector B", 15, "Pradera con sistema silvopastoril"),
            new Lote(9, 3, "Sector C", 12, "Zona de renovación de pasturas")
        ));
        LOTES_POR_FINCA.put(4, FXCollections.observableArrayList(
            new Lote(10, 4, "Zona Norte", 16, "Pasturas naturales de sabana"),
            new Lote(11, 4, "Zona Sur", 14, "Brachiaria humidicola establecida"),
            new Lote(12, 4, "Zona Central", 18, "Área de engorde intensivo")
        ));
        LOTES_POR_FINCA.put(5, FXCollections.observableArrayList(
            new Lote(13, 5, "Potrero 1", 20, "Ray grass con trébol rojo"),
            new Lote(14, 5, "Potrero 2", 15, "Pradera de kikuyo fertilizado"),
            new Lote(15, 5, "Potrero 3", 12, "Zona de maternidad")
        ));
    }

    private static void crearAnimales() {
        ANIMALES_POR_FINCA.put(1, FXCollections.observableArrayList(
            new Animal(1, 1, 1, "E-1001", "Nube", "Normando", "Macho", LocalDate.of(2022, 3, 15), 280, 385, 0.92, "Rentable", "Animal en excelente condición", "Potrero Norte"),
            new Animal(2, 1, 2, "E-1022", "Rayo", "Normando", "Macho", LocalDate.of(2021, 8, 10), 340, 416, 0.75, "Mantener", "Monitorear GDP mensualmente", "Potrero Sur"),
            new Animal(3, 1, 2, "E-1045", "Luna", "Brauvión", "Hembra", LocalDate.of(2023, 1, 20), 240, 310, 0.48, "Bajo", "GDP por debajo del esperado", "Potrero Sur"),
            new Animal(4, 1, 3, "E-1019", "Brisa", "Simmental", "Hembra", LocalDate.of(2022, 11, 5), 260, 312, 0.35, "No Rentable", "Requiere evaluación veterinaria urgente", "Potrero Oriente"),
            new Animal(5, 1, 1, "E-1067", "Trueno", "Normando", "Macho", LocalDate.of(2023, 5, 12), 200, 280, 0.95, "Rentable", "Excelente ganancia diaria", "Potrero Norte")
        ));
        ANIMALES_POR_FINCA.put(2, FXCollections.observableArrayList(
            new Animal(6, 2, 4, "C-2001", "Paloma", "Holstein", "Hembra", LocalDate.of(2022, 2, 20), 300, 415, 0.88, "Mantener", "Próximo parto en 45 días", "Pradera Alta"),
            new Animal(7, 2, 4, "C-2034", "Toro", "Normando", "Macho", LocalDate.of(2021, 9, 14), 340, 450, 1.05, "Rentable", "Mejor GDP del lote", "Pradera Alta"),
            new Animal(8, 2, 5, "C-2018", "Viento", "Brauvión", "Macho", LocalDate.of(2023, 3, 1), 220, 295, 0.48, "Bajo", "Revisar alimentación", "Pradera Baja"),
            new Animal(9, 2, 6, "C-2056", "Estrella", "Holstein", "Hembra", LocalDate.of(2022, 7, 22), 280, 380, 0.82, "Mantener", "Productora de leche establecida", "Pradera Central"),
            new Animal(10, 2, 5, "C-2089", "Sol", "Simmental", "Macho", LocalDate.of(2023, 8, 15), 190, 255, 0.32, "No Rentable", "GDP muy bajo, considerar descarte", "Pradera Baja")
        ));
        ANIMALES_POR_FINCA.put(3, FXCollections.observableArrayList(
            new Animal(11, 3, 7, "A-3001", "Rosa", "Normando", "Hembra", LocalDate.of(2022, 4, 10), 260, 365, 0.88, "Mantener", "En buen estado reproductivo", "Sector A"),
            new Animal(12, 3, 7, "A-3022", "Torito", "Brauvión", "Macho", LocalDate.of(2021, 11, 30), 350, 460, 1.10, "Rentable", "Animal estrella del lote", "Sector A"),
            new Animal(13, 3, 8, "A-3045", "Flor", "Simmental", "Hembra", LocalDate.of(2023, 2, 14), 230, 305, 0.46, "Bajo", "Evaluar suplementación", "Sector B"),
            new Animal(14, 3, 9, "A-3067", "Vaquito", "Holstein", "Macho", LocalDate.of(2022, 9, 1), 290, 395, 0.90, "Rentable", "GDP estable", "Sector C"),
            new Animal(15, 3, 8, "A-3089", "Lluvia", "Normando", "Macho", LocalDate.of(2023, 6, 20), 210, 275, 0.40, "No Rentable", "GDP insuficiente", "Sector B")
        ));
        ANIMALES_POR_FINCA.put(4, FXCollections.observableArrayList(
            new Animal(16, 4, 10, "S-4001", "León", "Normando", "Macho", LocalDate.of(2022, 1, 15), 310, 415, 0.91, "Rentable", "GDP consistente en todo el período", "Zona Norte"),
            new Animal(17, 4, 11, "S-4022", "Pampa", "Holstein", "Hembra", LocalDate.of(2021, 7, 22), 285, 390, 0.84, "Mantener", "Segunda preñez en curso", "Zona Sur"),
            new Animal(18, 4, 10, "S-4045", "Río", "Brauvión", "Macho", LocalDate.of(2023, 4, 8), 240, 318, 0.49, "Bajo", "Bajo peso relativo a la raza", "Zona Norte"),
            new Animal(19, 4, 12, "S-4067", "Campo", "Simmental", "Hembra", LocalDate.of(2022, 10, 12), 255, 350, 0.82, "Mantener", "Buena adaptación a zona tropical", "Zona Central"),
            new Animal(20, 4, 11, "S-4089", "Selva", "Normando", "Macho", LocalDate.of(2023, 7, 30), 180, 240, 0.37, "No Rentable", "GDP muy bajo para su edad", "Zona Sur")
        ));
        ANIMALES_POR_FINCA.put(5, FXCollections.observableArrayList(
            new Animal(21, 5, 13, "P-5001", "Boyaco", "Simmental", "Macho", LocalDate.of(2022, 6, 10), 295, 405, 0.96, "Rentable", "Excelente adaptación a altiplano", "Potrero 1"),
            new Animal(22, 5, 14, "P-5022", "Nieva", "Normando", "Hembra", LocalDate.of(2021, 12, 20), 270, 375, 0.86, "Mantener", "GDP estable todo el año", "Potrero 2"),
            new Animal(23, 5, 13, "P-5045", "Cerro", "Holstein", "Macho", LocalDate.of(2023, 1, 5), 250, 335, 0.75, "Mantener", "GDP por encima del promedio", "Potrero 1"),
            new Animal(24, 5, 15, "P-5067", "Nevada", "Brauvión", "Hembra", LocalDate.of(2022, 8, 18), 240, 330, 0.68, "Mantener", "GDP aceptable para la raza", "Potrero 3"),
            new Animal(25, 5, 14, "P-5089", "Pico", "Normando", "Macho", LocalDate.of(2023, 9, 1), 175, 230, 0.33, "No Rentable", "Revisión nutricional urgente", "Potrero 2")
        ));

        // Set fechaIngreso a la fecha de nacimiento + 6 meses (aproximación realista)
        // y sembrar ficha técnica completa
        String[] coloresPorRaza = {"Blanco con marrón", "Café oscuro", "Pintado blanco/negro", "Marrón claro", "Negro sólido"};
        ANIMALES_POR_FINCA.values().forEach(lista ->
                lista.forEach(a -> {
                    if (a.getFechaIngreso() == null && a.getFechaNacimiento() != null) {
                        a.setFechaIngreso(a.getFechaNacimiento().plusMonths(6));
                    }
                    String fincaNombre = FINCAS.stream().filter(f -> f.getId() == a.getFincaId())
                            .findFirst().map(Finca::getNombre).orElse(null);
                    a.setFincaNombre(fincaNombre);

                    // Ficha técnica demo
                    a.setColor(coloresPorRaza[a.getId() % coloresPorRaza.length]);
                    a.setEstadoCorporal(a.getGdp() >= 0.9 ? 4 : a.getGdp() >= 0.7 ? 3 : a.getGdp() >= 0.5 ? 2 : 2);
                    if ("Hembra".equalsIgnoreCase(a.getSexo())) {
                        a.setEstadoGestacion(a.getId() % 3 == 0 ? "2do trimestre" : (a.getId() % 3 == 1 ? "Lactando" : "No gestante"));
                        a.setNumeroPartos(a.getEdadMeses() / 18);
                        if ("Holstein".equalsIgnoreCase(a.getRaza())) {
                            a.setProduccionLecheDiaria(18 + (a.getId() % 8));
                            a.setTipoProduccion("Leche");
                            a.setUsoRecomendado("Leche");
                        } else {
                            a.setTipoProduccion("Doble propósito");
                            a.setUsoRecomendado("Cría");
                        }
                        a.setUltimoParto(a.getNumeroPartos() > 0
                                ? java.time.LocalDate.now().minusMonths(12L + (a.getId() % 10)) : null);
                    } else {
                        a.setEstadoGestacion("No aplica");
                        a.setTipoProduccion("Carne");
                        a.setUsoRecomendado(a.getEdadMeses() > 24 ? "Reproducción" : "Ceba");
                    }
                    a.setCalidadGenetica(a.getGdp() >= 0.9 ? "Premium" : a.getGdp() >= 0.7 ? "A" : "B");
                    a.setLineaGenetica(a.getRaza() + " — línea " + (a.getId() % 5 == 0 ? "europea" : "tropical"));
                    a.setRegistroGenealogico("RG-" + a.getArete().replace("-", ""));
                    a.setRegistroIcaNumero("ICA-" + (10000 + a.getId() * 17));
                    a.setVeterinarioResponsable("Dr. Veterinario de Campo");
                    if (a.getEstadoGestacion() != null && a.getEstadoGestacion().contains("trimestre")) {
                        a.setProximoServicio(java.time.LocalDate.now().plusMonths(3));
                    }
                })
        );
    }

    private static void crearInventario() {
        Map<Integer, List<InventarioItem>> items = new HashMap<>();
        items.put(1, Arrays.asList(
            new InventarioItem("E-INS-01", "Sal mineralizada", "Suplementos", 18, "bultos", 10, 95000),
            new InventarioItem("E-INS-02", "Vacuna clostridial", "Veterinaria", 8, "frascos", 5, 78000),
            new InventarioItem("E-INS-03", "Melaza", "Alimentos", 420, "kg", 150, 1800),
            new InventarioItem("E-INS-04", "Concentrado levante", "Alimentos", 12, "bultos", 15, 118000)
        ));
        items.put(2, Arrays.asList(
            new InventarioItem("C-INS-01", "Sal mineral blanca", "Suplementos", 22, "bultos", 8, 88000),
            new InventarioItem("C-INS-02", "Ivermectina 1%", "Veterinaria", 14, "frascos", 6, 45000),
            new InventarioItem("C-INS-03", "Pasto estrella semilla", "Insumos", 80, "kg", 30, 12000)
        ));
        items.put(3, Arrays.asList(
            new InventarioItem("A-INS-01", "Urea forrajera", "Suplementos", 35, "kg", 20, 5500),
            new InventarioItem("A-INS-02", "Vacuna aftosa", "Veterinaria", 6, "dosis", 10, 25000),
            new InventarioItem("A-INS-03", "Bloques nutricionales", "Suplementos", 9, "unidades", 5, 185000)
        ));
        items.put(4, Arrays.asList(
            new InventarioItem("S-INS-01", "Sal mineralizada trópico", "Suplementos", 28, "bultos", 12, 98000),
            new InventarioItem("S-INS-02", "Antibiótico oxitetraciclina", "Veterinaria", 10, "frascos", 8, 67000),
            new InventarioItem("S-INS-03", "Semilla Brachiaria", "Insumos", 55, "kg", 25, 18000)
        ));
        items.put(5, Arrays.asList(
            new InventarioItem("P-INS-01", "Sal mineralizada altiplano", "Suplementos", 16, "bultos", 10, 102000),
            new InventarioItem("P-INS-02", "Vitaminas ADE", "Veterinaria", 12, "frascos", 5, 55000),
            new InventarioItem("P-INS-03", "Concentrado doble propósito", "Alimentos", 8, "bultos", 10, 125000)
        ));
        items.forEach((fincaId, lista) ->
            INVENTARIO_POR_FINCA.put(fincaId, FXCollections.observableArrayList(lista))
        );
    }

    private static void crearPesajes() {
        for (int fincaId = 1; fincaId <= 5; fincaId++) {
            ObservableList<Pesaje> lista = FXCollections.observableArrayList();
            ObservableList<Animal> animales = ANIMALES_POR_FINCA.get(fincaId);
            if (animales != null) {
                int idBase = fincaId * 100;
                for (int i = 0; i < animales.size(); i++) {
                    Animal a = animales.get(i);
                    lista.add(new Pesaje(idBase + i, a.getId(), fincaId, a.getArete(),
                            LocalDate.now().minusDays(90), a.getPesoInicial(), "Pesaje inicial"));
                    lista.add(new Pesaje(idBase + i + 50, a.getId(), fincaId, a.getArete(),
                            LocalDate.now().minusDays(45), (a.getPesoInicial() + a.getPesoActual()) / 2, "Pesaje intermedio"));
                    lista.add(new Pesaje(idBase + i + 100, a.getId(), fincaId, a.getArete(),
                            LocalDate.now().minusDays(7), a.getPesoActual(), "Pesaje reciente"));
                }
            }
            PESAJES_POR_FINCA.put(fincaId, lista);
        }
    }

    private static void crearVacunas() {
        String[] tiposVacuna = {"Aftosa", "Brucela", "Clostridial", "Rabia bovina", "IBR/DVB"};
        for (int fincaId = 1; fincaId <= 5; fincaId++) {
            ObservableList<Vacuna> lista = FXCollections.observableArrayList();
            ObservableList<Animal> animales = ANIMALES_POR_FINCA.get(fincaId);
            if (animales != null) {
                int idBase = fincaId * 200;
                for (int i = 0; i < Math.min(4, animales.size()); i++) {
                    Animal a = animales.get(i);
                    String vacuna = tiposVacuna[i % tiposVacuna.length];
                    LocalDate aplicacion = LocalDate.now().minusDays(60 + i * 5);
                    LocalDate proxima = fincaId % 2 == 0
                            ? LocalDate.now().plusDays(i * 2)
                            : LocalDate.now().plusDays(30 + i * 10);
                    lista.add(new Vacuna(idBase + i, a.getId(), fincaId, a.getArete(),
                            vacuna, aplicacion, proxima, "Dr. Veterinario", "Aplicación rutinaria"));
                }
            }
            VACUNAS_POR_FINCA.put(fincaId, lista);
        }
    }

    private static void crearPotreros() {
        String[] climas = {"Frío", "Templado", "Templado", "Caliente", "Frío"};
        double[] altitudes = {2600, 100, 1500, 250, 2900};
        String[] pastos = {"Kikuyo mejorado", "Estrella africana", "Brachiaria mulato", "Brachiaria humidicola", "Ray grass"};
        for (int fId = 1; fId <= 5; fId++) {
            final int fincaId = fId;
            ObservableList<Potrero> lista = FXCollections.observableArrayList();
            int idBase = fincaId * 300;
            String fincaNom = FINCAS.stream().filter(f -> f.getId() == fincaId)
                    .findFirst().map(Finca::getNombre).orElse("");
            for (int i = 0; i < 2; i++) {
                Potrero p = new Potrero(
                        idBase + i,
                        FINCA_PREFIJOS[fincaId - 1] + "-POT-" + (100 + i),
                        "Potrero " + (i + 1),
                        fincaId,
                        45.0 + i * 8,
                        pastos[fincaId - 1],
                        2500 + i * 200,
                        25 + i * 5,
                        altitudes[fincaId - 1],
                        climas[fincaId - 1],
                        5.5 + i * 0.3,
                        i == 0 ? "Bueno" : "Regular",
                        20 + i * 25,
                        i == 0 ? "Libre" : "Semi",
                        15 + i * 5,
                        null,
                        null,
                        "Pradera con buena cobertura"
                );
                p.setFincaNombre(fincaNom);
                lista.add(p);
            }
            POTREROS_POR_FINCA.put(fincaId, lista);
        }
    }

    private static void crearRegistrosSanitarios() {
        for (int fincaId = 1; fincaId <= 5; fincaId++) {
            ObservableList<RegistroSanitario> lista = FXCollections.observableArrayList();
            ObservableList<Animal> animales = ANIMALES_POR_FINCA.get(fincaId);
            if (animales != null && !animales.isEmpty()) {
                int idBase = fincaId * 400;
                Animal a = animales.get(0);
                lista.add(new RegistroSanitario(idBase, a.getId(), fincaId, a.getArete(), a.getNombre(),
                        RegistroSanitario.Tipo.PURGA, "Ivermectina 1% — purga rutinaria",
                        LocalDate.now().minusMonths(5), LocalDate.now().plusDays(7),
                        "Dr. Veterinario", 35000, "Aplicación cada 4 meses"));
            }
            SANITARIOS_POR_FINCA.put(fincaId, lista);
        }
    }

    private static void crearMarketplaceDemo() {
        // Crear listings demo para mostrar funcionalidad
        ObservableList<Animal> animalesF1 = ANIMALES_POR_FINCA.get(1);
        if (animalesF1 != null && !animalesF1.isEmpty()) {
            Animal a = animalesF1.get(0);
            double precio = a.getPesoActual() * 6500;
            MarketplaceListing m = new MarketplaceListing();
            m.setId(1);
            m.setCodigo("MKT-10001");
            m.setTipo(MarketplaceListing.Tipo.INDIVIDUAL);
            m.setFincaId(1);
            m.setFincaNombre("La Esperanza");
            m.setUbicacion("Cundinamarca, Colombia");
            m.setDepartamento("Cundinamarca");
            m.setCiudad("Bogotá");
            m.setDistanciaKm(0);
            m.getAnimalIds().add(a.getId());
            m.getAretes().add(a.getArete());
            m.setTituloLote(a.getNombre() + " — " + a.getRaza() + " " + a.getSexo());
            m.setResumen("Animal rentable con GDP de " + String.format("%.2f kg/día", a.getGdp()));
            m.setPrecio(precio);
            m.setPrecioMinimo(precio * 0.85);
            m.setRaza(a.getRaza()); m.setSexo(a.getSexo());
            m.setEdadMeses(a.getEdadMeses()); m.setPesoTotal(a.getPesoActual());
            m.setTipoProduccion("Carne");
            m.setEstadoSanitario(a.getEstadoSanitario());
            m.setVacunasAlDia(true);
            m.setVendedor("Carlos Rodríguez");
            m.setContacto("+57 310 555 1234");
            MARKETPLACE.add(m);
        }
        ObservableList<Animal> animalesF3 = ANIMALES_POR_FINCA.get(3);
        if (animalesF3 != null && animalesF3.size() >= 2) {
            double precio = 2 * 400 * 6500;
            MarketplaceListing m = new MarketplaceListing();
            m.setId(2);
            m.setCodigo("MKT-10002");
            m.setTipo(MarketplaceListing.Tipo.LOTE);
            m.setFincaId(3);
            m.setFincaNombre("La Argentina");
            m.setUbicacion("Antioquia, Colombia");
            m.setDepartamento("Antioquia"); m.setCiudad("Medellín");
            m.setDistanciaKm(420);
            for (int i = 0; i < 2; i++) {
                m.getAnimalIds().add(animalesF3.get(i).getId());
                m.getAretes().add(animalesF3.get(i).getArete());
            }
            m.setTituloLote("Lote Sector A — 2 animales rentables");
            m.setResumen("Lote de levante con GDP promedio superior a 0.95 kg/día");
            m.setPrecio(precio);
            m.setPrecioMinimo(precio * 0.88);
            m.setRaza("Mixto Normando/Brauvión"); m.setSexo("Mixto");
            m.setEdadMeses(18); m.setPesoTotal(820);
            m.setTipoProduccion("Levante");
            m.setEstadoSanitario("Sano"); m.setVacunasAlDia(true);
            m.setVendedor("Jorge Pérez");
            m.setContacto("+57 320 555 9876");
            MARKETPLACE.add(m);
        }
    }

    private static void crearTernerosLegacy() {
        TERNEROS_LEGACY.addAll(
            new Ternero("1022", "Nube", "Normando", "Macho", 6, 180.5, "Activo"),
            new Ternero("1023", "Rayo", "Normando", "Macho", 5, 165.0, "Observación"),
            new Ternero("1024", "Luna", "Holstein", "Hembra", 7, 190.2, "Activo"),
            new Ternero("1025", "Brisa", "Normando", "Hembra", 4, 152.4, "Riesgo")
        );
    }

    // ---- Acceso a Fincas ----
    public static ObservableList<Finca> getFincas() { initializeData(); return FINCAS; }
    public static Finca getFincaById(int id) {
        initializeData();
        return FINCAS.stream().filter(f -> f.getId() == id).findFirst().orElse(null);
    }

    // ---- Acceso a Lotes ----
    public static ObservableList<Lote> getLotesPorFinca(int fincaId) {
        initializeData();
        return LOTES_POR_FINCA.getOrDefault(fincaId, FXCollections.observableArrayList());
    }

    // ---- Acceso a Animales ----
    public static ObservableList<Animal> getAnimalesPorFinca(int fincaId) {
        initializeData();
        return ANIMALES_POR_FINCA.getOrDefault(fincaId, FXCollections.observableArrayList());
    }

    public static ObservableList<Animal> getTodosLosAnimales() {
        initializeData();
        ObservableList<Animal> todos = FXCollections.observableArrayList();
        ANIMALES_POR_FINCA.values().forEach(todos::addAll);
        return todos;
    }

    public static Animal getAnimalById(int animalId) {
        initializeData();
        for (ObservableList<Animal> lista : ANIMALES_POR_FINCA.values()) {
            for (Animal a : lista) if (a.getId() == animalId) return a;
        }
        return null;
    }

    public static void agregarAnimal(int fincaId, Animal animal) {
        initializeData();
        ANIMALES_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList()).add(animal);
    }

    // ---- Acceso a Inventario ----
    public static ObservableList<InventarioItem> getInventarioPorFinca(int fincaId) {
        initializeData();
        return INVENTARIO_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList());
    }

    public static ObservableList<InventarioItem> getInventario() {
        initializeData();
        return INVENTARIO_POR_FINCA.getOrDefault(1, FXCollections.observableArrayList());
    }

    // ---- Acceso a Pesajes ----
    public static ObservableList<Pesaje> getPesajesPorFinca(int fincaId) {
        initializeData();
        return PESAJES_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList());
    }

    public static void agregarPesaje(int fincaId, Pesaje pesaje) {
        initializeData();
        PESAJES_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList()).add(pesaje);
    }

    // ---- Acceso a Vacunas ----
    public static ObservableList<Vacuna> getVacunasPorFinca(int fincaId) {
        initializeData();
        return VACUNAS_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList());
    }

    public static void agregarVacuna(int fincaId, Vacuna vacuna) {
        initializeData();
        VACUNAS_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList()).add(vacuna);
    }

    // ---- Acceso a Potreros ----
    public static ObservableList<Potrero> getPotrerosPorFinca(int fincaId) {
        initializeData();
        return POTREROS_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList());
    }

    public static void agregarPotrero(int fincaId, Potrero potrero) {
        initializeData();
        POTREROS_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList()).add(potrero);
    }

    // ---- Acceso a Registros Sanitarios ----
    public static ObservableList<RegistroSanitario> getRegistrosSanitariosPorFinca(int fincaId) {
        initializeData();
        return SANITARIOS_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList());
    }

    public static void agregarRegistroSanitario(int fincaId, RegistroSanitario registro) {
        initializeData();
        SANITARIOS_POR_FINCA.computeIfAbsent(fincaId, k -> FXCollections.observableArrayList()).add(registro);
    }

    // ---- Acceso a Marketplace ----
    public static ObservableList<MarketplaceListing> getMarketplace() {
        initializeData();
        return MARKETPLACE;
    }

    public static void agregarMarketplace(MarketplaceListing listing) {
        initializeData();
        MARKETPLACE.add(listing);
    }

    // ---- Acceso a Pujas ----
    public static ObservableList<Puja> getPujas() { initializeData(); return PUJAS; }

    public static ObservableList<Puja> getPujasPorListing(int listingId) {
        initializeData();
        ObservableList<Puja> resultado = FXCollections.observableArrayList();
        for (Puja p : PUJAS) if (p.getListingId() == listingId) resultado.add(p);
        return resultado;
    }

    public static void agregarPuja(Puja puja) {
        initializeData();
        PUJAS.add(puja);
    }

    public static int nextPujaId() {
        return PUJAS.size() + 1;
    }

    // ---- Terneros legacy ----
    public static ObservableList<Ternero> getTerneros() { initializeData(); return TERNEROS_LEGACY; }
}
