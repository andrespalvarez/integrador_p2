package Main;

// Clase utilitaria para mostrar el menú principal.

 
public class MenuDisplay {
    
// Muestra el menú principal con las opciones de productos y códigos de barras.
    
    
    
    public static void mostrarMenuPrincipal() {
        System.out.println("\n========= MENU =========");
        System.out.println("1. Crear producto");
        System.out.println("2. Listar productos");
        System.out.println("3. Actualizar producto");
        System.out.println("4. Eliminar producto");
        System.out.println("5. Crear codigo de barras");
        System.out.println("6. Listar codigo de barras");
        System.out.println("7. Actualizar codigo de barras por ID");
        System.out.println("8. Eliminar codigo de barras por ID");
        System.out.println("9. Actualizar codigo de barras por ID de producto");
        System.out.println("10. Eliminar codigo de barras por ID de producto");
        System.out.println("0. Salir");
        System.out.print("Ingrese una opcion: ");
    }
}