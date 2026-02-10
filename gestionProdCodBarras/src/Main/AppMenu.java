package Main;

import java.util.Scanner;
import Dao.CodigoBarrasDAO;
import Dao.ProductoDAO;
import Service.CodigoBarrasServiceImpl;
import Service.ProductoServiceImpl;

// Clase que inicia la app, arma dependencias y ejecuta el menú.



public class AppMenu {

// Scanner único para leer entradas del usuario.

    
    private final Scanner scanner;

// Maneja las acciones seleccionadas del menú.

    
    private final MenuHandler menuHandler;

// Controla si el menú sigue corriendo.

    
    private boolean running;

// Inicializa scanner, servicios y handler del menú.

    
    
    public AppMenu() {
        this.scanner = new Scanner(System.in);
        ProductoServiceImpl productoService = createProductoService();    //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
        this.menuHandler = new MenuHandler(scanner, productoService);    //****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
        this.running = true;
    }

// Crea AppMenu y ejecuta la aplicación.

    
    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }

    // Ejecuta el menú en un loop hasta que el usuario elija salir.

    
    
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

// Procesa la opción elegida y llama al método correspondiente.
   
        
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

   // Crea e inicializa los DAOs y servicios usados por productos.

    
    private ProductoServiceImpl createProductoService() {    
        CodigoBarrasDAO codigoBarrasDAO = new CodigoBarrasDAO();
        ProductoDAO productoDAO = new ProductoDAO(codigoBarrasDAO);
        CodigoBarrasServiceImpl codigoBarrasService = new CodigoBarrasServiceImpl(codigoBarrasDAO);
        return new ProductoServiceImpl(productoDAO, codigoBarrasService);
    }
}