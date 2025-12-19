package Main;

import Entities.Producto;
import java.util.List;
import java.util.Scanner;
import Entities.CodigoBarras;
import Service.ProductoServiceImpl;
import java.time.LocalDate;

/**
 * Controlador de las operaciones del menú (Menu Handler).
 * Gestiona toda la lógica de interacción con el usuario para operaciones CRUD.
 *
 *
 * Patrón: Controller (MVC) - capa de presentación en arquitectura de 4 capas
 * Arquitectura: Main → Service → DAO → Models
 *
 * IMPORTANTE: Este handler NO contiene lógica de negocio.
 * Todas las validaciones de negocio están en la capa Service.
 */
public class MenuHandler {
  
    private final Scanner scanner;

  
    private final ProductoServiceImpl productoService;

    /**
     * Constructor con inyección de dependencias.
     * Valida que las dependencias no sean null (fail-fast).
     *
     * @param scanner Scanner compartido para entrada de usuario
     * @param productoService Servicio de productos
     * @throws IllegalArgumentException si alguna dependencia es null
     */
    public MenuHandler(Scanner scanner, ProductoServiceImpl productoService) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner no puede ser null");
        }
        if (productoService == null) {
            throw new IllegalArgumentException("ProductoService no puede ser null");
        }
        this.scanner = scanner;
        this.productoService = productoService;
    }

    //Crear un nuevo producto, ingresando todos los parametros string y double
     
    public void crearProducto() {
        try {
            System.out.print("Nombre: ");
            String nombre = scanner.nextLine().trim();
            System.out.print("Marca: ");
            String marca = scanner.nextLine().trim();
            System.out.print("Categoria: ");
            String categoria = scanner.nextLine().trim();
            System.out.print("Precio: ");
            double precio = Double.parseDouble(scanner.nextLine().trim()); 
            System.out.print("Peso: ");
            double peso = Double.parseDouble(scanner.nextLine().trim()); 

            CodigoBarras codigoBarras = null;
            System.out.print("¿Desea agregar un codigo de barras? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                codigoBarras = crearCodBarras();
            }

            Producto producto = new Producto(0, nombre, marca, categoria, precio, peso);
            producto.setCodBarras(codigoBarras);
            productoService.insertar(producto);
            System.out.println("Producto creado exitosamente con ID: " + producto.getId());
        } catch (Exception e) {
            System.err.println("Error al crear producto: " + e.getMessage());
        }
    }

    
    // Listar productos / buscar por nombre o marca
    
    public void listarProductos() {
        try {
            System.out.print("¿Desea (1) listar todos o (2) buscar por nombre/marca? Ingrese opcion: ");
            int subopcion = Integer.parseInt(scanner.nextLine());

            List<Producto> productos;
            if (subopcion == 1) {
                productos = productoService.getAll();
            } else if (subopcion == 2) {
                System.out.print("Ingrese texto a buscar: ");
                String filtro = scanner.nextLine().trim();
                productos = productoService.buscarPorNombreMarca(filtro);
            } else {
                System.out.println("Opcion invalida.");
                return;
            }

            if (productos.isEmpty()) {
                System.out.println("No se encontraron productos.");
                return;
            }

            for (Producto p : productos) {
                System.out.println("ID: " + p.getId() + ", Nombre: " + p.getNombre() +
                        ", Marca: " + p.getMarca() + ", Categoria: " + p.getCategoria()+
                        ", Precio: " + p.getPrecio() + ", Peso: " + p.getPeso());
                if (p.getCodBarras() != null) {
                    System.out.println("   Tipo: " + p.getCodBarras().getTipo() +
                            " Valor: " + p.getCodBarras().getValor() + " Fecha: " + p.getCodBarras().getFechaAsignacion() +
                            " Observaciones: " + p.getCodBarras().getObservaciones());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
    }

    
      //Actualizar producto existente.
     
     
     
    public void actualizarProducto() {
        try {
            System.out.print("ID de la producto a actualizar: ");
            int id = Integer.parseInt(scanner.nextLine());
            Producto p = productoService.getById(id);

            if (p == null) {
                System.out.println("producto no encontrado.");
                return;
            }

            System.out.print("Nuevo nombre (actual: " + p.getNombre() + ", Enter para mantener): ");
            String nombre = scanner.nextLine().trim();
            if (!nombre.isEmpty()) {
                p.setNombre(nombre);
            }

            System.out.print("Nueva marca (actual: " + p.getMarca() + ", Enter para mantener): ");
            String marca = scanner.nextLine().trim();
            if (!marca.isEmpty()) {
                p.setMarca(marca);
            }

            System.out.print("Nueva categoria (actual: " + p.getCategoria() + ", Enter para mantener): ");
            String categoria = scanner.nextLine().trim();
            if (!categoria.isEmpty()) {
                p.setCategoria(categoria);
            }
            
            System.out.print("Nuevo precio (actual: " + p.getPrecio() + ", Enter para mantener): ");
            double precio = Double.parseDouble(scanner.nextLine().trim());
            if (!Double.isNaN(precio)) {
                p.setPrecio(precio);
            }
            
            System.out.print("Nuevo peso (actual: " + p.getPeso() + ", Enter para mantener): ");
            double peso = Double.parseDouble(scanner.nextLine().trim());
            if (!Double.isNaN(peso)) {
                p.setPeso(peso);
            }

            actualizarCodBarrasDeProducto(p);
            productoService.actualizar(p);
            System.out.println("Producto actualizado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
        }
    }

    // Eliminar producto (soft delete).
 
     
    public void eliminarProducto() {
        try {
            System.out.print("ID del producto a eliminar: ");
            int id = Integer.parseInt(scanner.nextLine());
            productoService.eliminar(id);
            System.out.println("Producto eliminado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
        }
    }


     // Opción 5: Crear producto 
     
    public void crearCodBarrasIndependiente() {
        try {
            CodigoBarras codigoBarras = crearCodBarras();
            productoService.getCodigoBarrasService().insertar(codigoBarras);
            System.out.println("Codigo de barras creado exitosamente con ID: " + codigoBarras.getId());
        } catch (Exception e) {
            System.err.println("Error al crear codigo de barras: " + e.getMessage());
        }
    }

    
     // Listar todos los productos activos.
     
    public void listarCodBarras() {
        try {
            List<CodigoBarras> codigosBarras = productoService.getCodigoBarrasService().getAll();
            if (codigosBarras.isEmpty()) {
                System.out.println("No se encontraron codigos de barra.");
                return;
            }
            for (CodigoBarras d : codigosBarras) {
                System.out.println("ID: " + d.getId() + ", " + d.getTipo() + " " + d.getValor() + 
                        " Fecha: "+ d.getFechaAsignacion() + " " + d.getObservaciones());
            }
        } catch (Exception e) {
            System.err.println("Error al listar codigos de barra: " + e.getMessage());
        }
    }

    
    //: Actualizar producto por ID.
    
    public void actualizarCodBarrasPorId() {
        try {
            System.out.print("ID del codigo de barras a actualizar: ");
            int id = Integer.parseInt(scanner.nextLine());
            CodigoBarras c = productoService.getCodigoBarrasService().getById(id);

            if (c == null) {
                System.out.println("Codigo de barras no encontrado.");
                return;
            }

            System.out.print("Indicar: EAN8 , EAN13 o UPC (Tipo Actual: "+ c.getTipo() + "): ");
            String tipo = scanner.nextLine().trim();
            if (!tipo.isEmpty()) {
                c.setTipo(tipo);
            }

            System.out.print("Nuevo valor (Valor Actual: " + c.getValor() + "): ");
            String valor = scanner.nextLine().trim();
            if (!valor.isEmpty()) {
                c.setValor(valor);
            }

            productoService.getCodigoBarrasService().actualizar(c);
            System.out.println("Codigo de barras actualizado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al actualizar codigo de barras: " + e.getMessage());
        }
    }

 //Eliminar domicilio por ID ( soft delete directo).
     
    public void eliminarCodBarrasPorId() {
        try {
            System.out.print("ID del codigo de barras a eliminar: ");
            int id = Integer.parseInt(scanner.nextLine());
            productoService.getCodigoBarrasService().eliminar(id);
            System.out.println("Codigo de barras eliminado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al eliminar codigo de barras: " + e.getMessage());
        }
    }

  // Actualizar domicilio de una persona específica.
    public void actualizarCodBarrasPorProducto() {
        try {
            System.out.print("ID del producto cuyo codigo de barras desea actualizar: ");
            int productoId = Integer.parseInt(scanner.nextLine());
            Producto p = productoService.getById(productoId);

            if (p == null) {
                System.out.println("Producto no encontrado.");
                return;
            }

            if (p.getCodBarras() == null) {
                System.out.println("El producto no tiene codigo de barras asociado.");
                return;
            }

            CodigoBarras c = p.getCodBarras();
            System.out.print("Indicar: EAN8 , EAN13 o UPC (Tipo Actual: "+ c.getTipo() + "): ");
            String tipo = scanner.nextLine().trim();
            if (!tipo.isEmpty()) {
                c.setTipo(tipo);
            }

            System.out.print("Nuevo valor (Valor Actual: " + c.getValor() + "): ");
            String valor = scanner.nextLine().trim();
            if (!valor.isEmpty()) {
                c.setValor(valor);
            }

            productoService.getCodigoBarrasService().actualizar(c);
            System.out.println("Codigo de barras actualizado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al actualizar codigo de barras: " + e.getMessage());
        }
    }

    // Eliminar codigo de barra por producto
     
    public void eliminarCodBarrasPorProducto() {
        try {
            System.out.print("ID del producto cuyo codigo de barras desea eliminar: ");
            int productoId = Integer.parseInt(scanner.nextLine());
            Producto p = productoService.getById(productoId);

            if (p == null) {
                System.out.println("Producto no encontrada.");
                return;
            }

            if (p.getCodBarras() == null) {
                System.out.println("El producto no tiene codigo de barras asociado.");
                return;
            }

            int codigoBarrasId = p.getCodBarras().getId();
            productoService.eliminarCodigoBarrasDeProducto(productoId, codigoBarrasId);
            System.out.println("Codigo de barras eliminado exitosamente y referencia actualizada.");
        } catch (Exception e) {
            System.err.println("Error al eliminar codigo de barras: " + e.getMessage());
        }
    }

    //Método auxiliar privado: Crea un objeto codigo de barras.
     
//    @return CodigoBarras nuevo (no persistido, ID=0)
    
    private CodigoBarras crearCodBarras() {
        System.out.print("Valor: ");
        String valor = scanner.nextLine().trim();
        System.out.print("Tipo (EAN8, EAN13 o UPC): ");
        String tipo = scanner.nextLine().trim();
        System.out.print("Fecha Asignación (aaaa-mm-dd) : ");
        LocalDate fechaAsignacion = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Observaciones: ");
        String observaciones = scanner.nextLine().trim();
        
        return new CodigoBarras(0, valor, tipo, fechaAsignacion, observaciones);
    }

    /* Método auxiliar privado: Maneja actualización de codigo dentro de actualizar producto.
     *
     * @param p Producto a la que se le actualizará/agregará codigoBarras
     * @throws Exception Si hay error al insertar/actualizar codigoBarras
     */
    private void actualizarCodBarrasDeProducto(Producto p) throws Exception {
        if (p.getCodBarras() != null) {
            System.out.print("¿Desea actualizar el codigo de barras? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                System.out.print("Nuevo tipo (" + p.getCodBarras().getTipo() + "): ");
                String tipo = scanner.nextLine().trim();
                if (!tipo.isEmpty()) {
                    p.getCodBarras().setTipo(tipo);
                }

                System.out.print("Nuevo valor (" + p.getCodBarras().getValor() + "): ");
                String valor = scanner.nextLine().trim();
                if (!valor.isEmpty()) {
                    p.getCodBarras().setValor(valor);
                }

                productoService.getCodigoBarrasService().actualizar(p.getCodBarras());
            }
        } else {
            System.out.print("El producto no tiene codigo de barras asignado. ¿Desea agregar uno? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                CodigoBarras nuevoCB = crearCodBarras();
                productoService.getCodigoBarrasService().insertar(nuevoCB);
                p.setCodBarras(nuevoCB);
            }
        }
    }
}
