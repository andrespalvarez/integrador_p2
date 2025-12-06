package Main;

import java.util.Scanner;
import Dao.CodigoBarrasDAO;
import Dao.ProductoDAO;
import Service.CodigoBarrasServiceImpl;
import Service.ProductoServiceImpl;

/**
 * Orquestador principal del menú de la aplicación.
 * Gestiona el ciclo de vida del menú y coordina todas las dependencias.
 *
 * Responsabilidades:
 * - Crear y gestionar el Scanner único (evita múltiples instancias de System.in)
 * - Inicializar toda la cadena de dependencias (DAOs → Services → Handler)
 * - Ejecutar el loop principal del menú
 * - Manejar la selección de opciones y delegarlas a MenuHandler
 * - Cerrar recursos al salir (Scanner)
 *
 * Patrón: Application Controller + Dependency Injection manual
 * Arquitectura: Punto de entrada que ensambla las 4 capas (Main → Service → DAO → Models)
 *
 * IMPORTANTE: Esta clase NO tiene lógica de negocio ni de UI.
 * Solo coordina y delega.
 */
public class AppMenu {
    /**
     * Scanner único compartido por toda la aplicación.
     * IMPORTANTE: Solo debe haber UNA instancia de Scanner(System.in).
     * Múltiples instancias causan problemas de buffering de entrada.
     */
    private final Scanner scanner;

    /**
     * Handler que ejecuta las operaciones del menú.
     * Contiene toda la lógica de interacción con el usuario.
     */
    private final MenuHandler menuHandler;

    /**
     * Flag que controla el loop principal del menú.
     * Se setea a false cuando el usuario selecciona "0 - Salir".
     */
    private boolean running;

    /**
     * Constructor que inicializa la aplicación. 
     *
     * Flujo de inicialización:
     * 1. Crea Scanner único para toda la aplicación
     * 2. Crea cadena de dependencias (DAOs → Services) mediante createPersonaService()
     * 3. Crea MenuHandler con Scanner y PersonaService
     * 4. Setea running=true para iniciar el loop
     *
     * Patrón de inyección de dependencias (DI) manual:
     * - DomicilioDAO (sin dependencias)    //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****                                                  
     * - PersonaDAO (depende de DomicilioDAO)       //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR**** 
     * - DomicilioServiceImpl (depende de DomicilioDAO)     //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****  
     * - PersonaServiceImpl (depende de PersonaDAO y DomicilioServiceImpl)  //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR**** 
     * - MenuHandler (depende de Scanner y PersonaServiceImpl)        //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *
     * Esta inicialización garantiza que todas las dependencias estén correctamente conectadas.
     */
    public AppMenu() {
        this.scanner = new Scanner(System.in);
        ProductoServiceImpl productoService = createProductoService();    //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
        this.menuHandler = new MenuHandler(scanner, productoService);    //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
        this.running = true;
    }

    /**
     * Punto de entrada de la aplicación Java.
     * Crea instancia de AppMenu y ejecuta el menú principal.
     *
     * @param args Argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }

    /**
     * Loop principal del menú.
     *
     * Flujo:
     * 1. Mientras running==true:
     *    a. Muestra menú con MenuDisplay.mostrarMenuPrincipal()
     *    b. Lee opción del usuario (scanner.nextLine())
     *    c. Convierte a int (puede lanzar NumberFormatException)
     *    d. Procesa opción con processOption()
     * 2. Si el usuario ingresa texto no numérico: Muestra mensaje de error y continúa
     * 3. Cuando running==false (opción 0): Sale del loop y cierra Scanner
     *
     * Manejo de errores:
     * - NumberFormatException: Captura entrada no numérica (ej: "abc")
     * - Muestra mensaje amigable y NO termina la aplicación
     * - El usuario puede volver a intentar
     *
     * IMPORTANTE: El Scanner se cierra al salir del loop.
     * Cerrar Scanner(System.in) cierra System.in para toda la aplicación.
     */
    public void run() {
        while (running) {
            try {
                MenuDisplay.mostrarMenuPrincipal();
                int opcion = Integer.parseInt(scanner.nextLine());
                processOption(opcion);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Por favor, ingrese un numero.");
            }
        }
        scanner.close();
    }

    /**
     * Procesa la opción seleccionada por el usuario y delega a MenuHandler.
     *
     * Switch expression (Java 14+) con operador arrow (->):
     * - Más conciso que switch tradicional
     * - No requiere break (cada caso es independiente)
     * - Permite bloques con {} para múltiples statements
     *
     * Mapeo de opciones (corresponde a MenuDisplay):
     * 1  → Crear persona (con domicilio opcional)  //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 2  → Listar personas (todas o filtradas) //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 3  → Actualizar persona      //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 4  → Eliminar persona (soft delete)  //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 5  → Crear domicilio independiente   //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 6  → Listar domicilios   //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 7  → Actualizar domicilio por ID (afecta a todas las personas que lo comparten)  //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 8  → Eliminar domicilio por ID (PELIGROSO - puede dejar FKs huérfanas)   //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 9  → Actualizar domicilio de una persona (afecta a todas las personas que lo comparten)  //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 10 → Eliminar domicilio de una persona (SEGURO - actualiza FK primero)   //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 0  → Salir (setea running=false para terminar el loop)   //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *
     * Opción inválida: Muestra mensaje y continúa el loop.
     *
     * IMPORTANTE: Todas las excepciones de MenuHandler se capturan dentro de los métodos.
     * processOption() NO propaga excepciones al caller (run()).
     *
     * @param opcion Número de opción ingresado por el usuario
     */
    private void processOption(int opcion) {
        switch (opcion) {
            case 1 -> menuHandler.crearProducto();
            case 2 -> menuHandler.listarProductos();
            case 3 -> menuHandler.actualizarProducto();
            case 4 -> menuHandler.eliminarProducto();
            case 5 -> menuHandler.crearCodBarrasIndependiente();
            case 6 -> menuHandler.listarCodBarras();
            case 7 -> menuHandler.actualizarCodBarrasPorId();
            case 8 -> menuHandler.eliminarCodBarrasPorId();
            case 9 -> menuHandler.actualizarCodBarrasPorProducto();
            case 10 -> menuHandler.eliminarCodBarrasPorProducto();
            case 0 -> {
                System.out.println("Saliendo...");
                running = false;
            }
            default -> System.out.println("Opcion no valida.");
        }
    }

    /**
     * Factory method que crea la cadena de dependencias de servicios.
     * Implementa inyección de dependencias manual.
     *
     * Orden de creación (bottom-up desde la capa más baja):
     * 1. DomicilioDAO: Sin dependencias, acceso directo a BD   //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 2. PersonaDAO: Depende de DomicilioDAO (inyectado en constructor)    //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 3. DomicilioServiceImpl: Depende de DomicilioDAO //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 4. PersonaServiceImpl: Depende de PersonaDAO y DomicilioServiceImpl  //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *
     * Arquitectura resultante (4 capas):
     * Main (AppMenu, MenuHandler)
     *   ↓
     * Service (PersonaServiceImpl, DomicilioServiceImpl)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *   ↓
     * DAO (PersonaDAO, DomicilioDAO)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *   ↓
     * Models (Persona, Domicilio, Base)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *
     * ¿Por qué PersonaDAO necesita DomicilioDAO?//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * - Actualmente NO lo usa (inyección preparada para futuras operaciones)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * - Podría usarse para operaciones transaccionales coordinadas//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *
     * ¿Por qué PersonaService necesita DomicilioService?//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * - Para insertar/actualizar domicilios al crear/actualizar personas//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * - Para eliminar domicilios de forma segura (eliminarDomicilioDePersona)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *
     * Patrón: Factory Method para construcción de dependencias
     *
     * @return ProductoServiceImpl completamente inicializado con todas sus dependencias
     */
    private ProductoServiceImpl createProductoService() {    
        CodigoBarrasDAO codigoBarrasDAO = new CodigoBarrasDAO();
        ProductoDAO productoDAO = new ProductoDAO(codigoBarrasDAO);
        CodigoBarrasServiceImpl codigoBarrasService = new CodigoBarrasServiceImpl(codigoBarrasDAO);
        return new ProductoServiceImpl(productoDAO, codigoBarrasService);
    }
}