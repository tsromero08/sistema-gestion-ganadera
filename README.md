# Sistema de Gestión Ganadera

Aplicación de escritorio para la gestión integral de la producción ganadera en varias
fincas: registro de animales, peso, estado sanitario, vacunación, lotes, inventario y
alertas para anticipar incidencias.

**Tecnologías:** Java 21, JavaFX, SceneBuilder (FXML), Maven, MySQL (local o en la nube).

Proyecto JavaFX/Maven listo para abrir en IntelliJ.

## Qué incluye
- Login funcional
- Dashboard principal con topbar, sidebar, KPIs, gráficas y tabla
- Módulo de animales/terneros con agregar, editar, eliminar y TableView
- Módulo de inventario con formulario y tabla
- Estructura completa `src/main/java` y `src/main/resources`

## Paquetes
- `com.sistemaganadero.app`
- `com.sistemaganadero.controller`
- `com.sistemaganadero.model`
- `com.sistemaganadero.service`
- `com.sistemaganadero.util`

## Cómo abrir en IntelliJ
1. `File > Open` y abre la carpeta del proyecto.
2. Espera a que IntelliJ importe Maven.
3. Verifica `Project SDK = JDK 21`.
4. Ejecuta desde Maven: `Plugins > javafx > javafx:run`.

## Usuarios demo
- `admin / admin123`
- `operario / 1234`
- `veterinario / vet123`

## Archivos clave
- `src/main/resources/view/main_layout.fxml` -> estructura principal con topbar/sidebar
- `src/main/resources/view/dashboard_home.fxml` -> dashboard central basado en la interfaz original
- `src/main/resources/view/terneros.fxml` -> módulo animales
- `src/main/resources/view/inventario.fxml` -> módulo inventario
- `docs/Interfaz_original_cargada_por_usuario.fxml` -> respaldo del FXML original

---
Desarrollado por **Tomás Romero** · tomasromero2101@gmail.com
