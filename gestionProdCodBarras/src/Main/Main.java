package Main;

// Punto de entrada alternativo que delega la ejecución a AppMenu.

public class Main {

// Ejecuta la app creando AppMenu y llamando a run().

    
    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }
}
