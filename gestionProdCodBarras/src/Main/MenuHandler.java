package Main;

import Entities.Producto;
import java.util.List;
import java.util.Scanner;
import Entities.CodigoBarras;
import Service.ProductoServiceImpl;

/**
 * Controlador de las operaciones del menú (Menu Handler).
 * Gestiona toda la lógica de interacción con el usuario para operaciones CRUD.
 *
 * Responsabilidades:
 * - Capturar entrada del usuario desde consola (Scanner)
 * - Validar entrada básica (conversión de tipos, valores vacíos)
 * - Invocar servicios de negocio (PersonaService, DomicilioService)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
 * - Mostrar resultados y mensajes de error al usuario
 * - Coordinar operaciones complejas (crear persona con domicilio, etc.)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
 *
 * Patrón: Controller (MVC) - capa de presentación en arquitectura de 4 capas
 * Arquitectura: Main → Service → DAO → Models
 *
 * IMPORTANTE: Este handler NO contiene lógica de negocio.
 * Todas las validaciones de negocio están en la capa Service.
 */
public class MenuHandler {
    /**
     * Scanner compartido para leer entrada del usuario.
     * Inyectado desde AppMenu para evitar múltiples Scanners de System.in.
     */
    private final Scanner scanner;

    /**
     * Servicio de personas para operaciones CRUD.
     * También proporciona acceso a DomicilioService mediante getDomicilioService().//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     */
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

    /**
     * Opción 1: Crear nueva persona (con domicilio opcional).//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *
     * Flujo:
     * 1. Solicita nombre, apellido y DNI//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 2. Pregunta si desea agregar domicilio//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 3. Si sí, captura calle y número//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 4. Crea objeto Persona y opcionalmente Domicilio//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     * 5. Invoca personaService.insertar() que://****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *    - Valida datos (nombre, apellido, DNI obligatorios)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *    - Valida DNI único (RN-001)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *    - Si hay domicilio, lo inserta primero (obtiene ID)//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *    - Inserta persona con FK domicilio_id correcta//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
     *
     * Input trimming: Aplica .trim() a todas las entradas (patrón consistente).
     *
     * Manejo de errores:
     * - IllegalArgumentException: Validaciones de negocio (muestra mensaje al usuario)
     * - SQLException: Errores de BD (muestra mensaje al usuario)
     * - Todos los errores se capturan y muestran, NO se propagan al menú principal
     */
    public void crearProducto() {
        try {
            System.out.print("Nombre: ");
            String nombre = scanner.nextLine().trim();
            System.out.print("Marca: ");
            String marca = scanner.nextLine().trim();
            System.out.print("Categoria: ");
            String categoria = scanner.nextLine().trim();
            System.out.print("Precio: ");
            String precio = scanner.nextLine().trim();
            System.out.print("Peso: ");
            String peso = scanner.nextLine().trim();

            CodigoBarras codigoBarras = null;
            System.out.print("¿Desea agregar un codigo de barras? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                codigoBarras = crearCodBarras();
            }

            Producto producto = new Producto(0, nombre, apellido, dni);//****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****MODIFICAR****
            producto.setCodBarras(codigoBarras);
            productoService.insertar(producto);
            System.out.println("Producto creado exitosamente con ID: " + producto.getId());
        } catch (Exception e) {
            System.err.println("Error al crear producto: " + e.getMessage());
        }
    }

    /**
     * Opción 2: Listar personas (todas o filtradas por nombre/apellido).
     *
     * Submenú:
     * 1. Listar todas las personas activas (getAll)
     * 2. Buscar por nombre o apellido con LIKE (buscarPorNombreApellido)
     *
     * Muestra:
     * - ID, Nombre, Apellido, DNI
     * - Domicilio (si tiene): Calle Número
     *
     * Manejo de casos especiales:
     * - Si no hay personas: Muestra "No se encontraron personas"
     * - Si la persona no tiene domicilio: Solo muestra datos de persona
     *
     * Búsqueda por nombre/apellido:
     * - Usa PersonaDAO.buscarPorNombreApellido() que hace LIKE '%filtro%'
     * - Insensible a mayúsculas en MySQL (depende de collation)
     * - Busca en nombre O apellido
     */
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
                            " Valor: " + p.getCodBarras().getValor() + " Fecha: " + p.getCodBarras().getFechaimplantacion() +
                            " Observaciones: " + p.getCodBarras().getObservaciones());
                }
            }
        } catch (Exception e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }
    }

    /**
     * Opción 3: Actualizar persona existente.
     *
     * Flujo:
     * 1. Solicita ID de la persona
     * 2. Obtiene persona actual de la BD
     * 3. Muestra valores actuales y permite actualizar:
     *    - Nombre (Enter para mantener actual)
     *    - Apellido (Enter para mantener actual)
     *    - DNI (Enter para mantener actual)
     * 4. Llama a actualizarDomicilioDePersona() para manejar cambios en domicilio
     * 5. Invoca personaService.actualizar() que valida:
     *    - Datos obligatorios (nombre, apellido, DNI)
     *    - DNI único (RN-001), excepto para la misma persona
     *
     * Patrón "Enter para mantener":
     * - Lee input con scanner.nextLine().trim()
     * - Si isEmpty() → NO actualiza el campo (mantiene valor actual)
     * - Si tiene valor → Actualiza el campo
     *
     * IMPORTANTE: Esta operación NO actualiza el domicilio directamente.
     * El domicilio se maneja en actualizarDomicilioDePersona() que puede:
     * - Actualizar domicilio existente (afecta a TODAS las personas que lo comparten)
     * - Agregar nuevo domicilio si la persona no tenía
     * - Dejar domicilio sin cambios
     */
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
            String precio = scanner.nextLine().trim();
            if (!precio.isEmpty()) {
                p.setPrecio(precio);
            }
            
            System.out.print("Nuevo peso (actual: " + p.getPeso() + ", Enter para mantener): ");
            String peso = scanner.nextLine().trim();
            if (!peso.isEmpty()) {
                p.setPeso(peso);
            }

            actualizarCodBarrasDeProducto(p);
            productoService.actualizar(p);
            System.out.println("Producto actualizado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
        }
    }

    /**
     * Opción 4: Eliminar persona (soft delete).
     *
     * Flujo:
     * 1. Solicita ID de la persona
     * 2. Invoca personaService.eliminar() que:
     *    - Marca persona.eliminado = TRUE
     *    - NO elimina el domicilio asociado (RN-037)
     *
     * IMPORTANTE: El domicilio NO se elimina porque:
     * - Múltiples personas pueden compartir un domicilio
     * - Si se eliminara, afectaría a otras personas
     *
     * Si se quiere eliminar también el domicilio:
     * - Usar opción 10: "Eliminar domicilio de una persona" (eliminarDomicilioPorPersona)
     * - Esa opción primero desasocia el domicilio, luego lo elimina (seguro)
     */
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

    /**
     * Opción 5: Crear domicilio independiente (sin asociar a persona).
     *
     * Flujo:
     * 1. Llama a crearDomicilio() para capturar calle y número
     * 2. Invoca domicilioService.insertar() que:
     *    - Valida calle y número obligatorios (RN-023)
     *    - Inserta en BD y asigna ID autogenerado
     * 3. Muestra ID generado
     *
     * Uso típico:
     * - Crear domicilio que luego se asignará a varias personas (opción 7)
     * - Pre-cargar domicilios en la BD
     */
    public void crearCodBarrasIndependiente() {
        try {
            CodigoBarras codigoBarras = crearCodBarras();
            productoService.getCodigoBarrasService().insertar(codigoBarras);
            System.out.println("Codigo de barras creado exitosamente con ID: " + codigoBarras.getId());
        } catch (Exception e) {
            System.err.println("Error al crear codigo de barras: " + e.getMessage());
        }
    }

    /**
     * Opción 6: Listar todos los domicilios activos.
     *
     * Muestra: ID, Calle Número
     *
     * Uso típico:
     * - Ver domicilios disponibles antes de asignar a persona (opción 7)
     * - Consultar ID de domicilio para actualizar (opción 9) o eliminar (opción 8)
     *
     * Nota: Solo muestra domicilios con eliminado=FALSE (soft delete).
     */
    public void listarCodBarras() {
        try {
            List<CodigoBarras> codigosBarras = productoService.getCodigoBarrasService().getAll();
            if (codigosBarras.isEmpty()) {
                System.out.println("No se encontraron codigos de barra.");
                return;
            }
            for (CodigoBarras d : codigosBarras) {
                System.out.println("ID: " + d.getId() + ", " + d.getTipo() + " " + d.getValor());
            }
        } catch (Exception e) {
            System.err.println("Error al listar codigos de barra: " + e.getMessage());
        }
    }

    /**
     * Opción 9: Actualizar domicilio por ID.
     *
     * Flujo:
     * 1. Solicita ID del domicilio
     * 2. Obtiene domicilio actual de la BD
     * 3. Muestra valores actuales y permite actualizar:
     *    - Calle (Enter para mantener actual)
     *    - Número (Enter para mantener actual)
     * 4. Invoca domicilioService.actualizar()
     *
     * ⚠️ IMPORTANTE (RN-040): Si varias personas comparten este domicilio,
     * la actualización los afectará a TODAS.
     *
     * Ejemplo:
     * - Domicilio ID=1 "Av. Siempreviva 742" está asociado a 3 personas
     * - Si se actualiza a "Calle Nueva 123", las 3 personas tendrán la nueva dirección
     *
     * Esto es CORRECTO para familias que viven juntas.
     * Si se quiere cambiar la dirección de UNA sola persona:
     * 1. Crear nuevo domicilio (opción 5)
     * 2. Asignar a la persona (opción 7)
     */
    public void actualizarCodBarrasPorId() {
        try {
            System.out.print("ID del codigo de barras a actualizar: ");
            int id = Integer.parseInt(scanner.nextLine());
            CodigoBarras c = productoService.getCodigoBarrasService().getById(id);

            if (c == null) {
                System.out.println("Codigo de barras no encontrado.");
                return;
            }

            System.out.print("Nuevo tipo (actual: " + c.getTipo() + ", Enter para mantener): ");
            String tipo = scanner.nextLine().trim();
            if (!tipo.isEmpty()) {
                c.setTipo(tipo);
            }

            System.out.print("Nuevo valor (actual: " + c.getValor() + ", Enter para mantener): ");
            String valor = scanner.nextLine().trim();
            if (!valor.isEmpty()) {
                c.setValor(valor);
            }

            productoService.getCodigoBarrasService().actualizar(c);
            System.out.println("Domicilio actualizado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al actualizar domicilio: " + e.getMessage());
        }
    }

    /**
     * Opción 8: Eliminar domicilio por ID (PELIGROSO - soft delete directo).
     *
     * ⚠️ PELIGRO (RN-029): Este método NO verifica si hay personas asociadas.
     * Si hay personas con FK a este domicilio, quedarán con referencia huérfana.
     *
     * Flujo:
     * 1. Solicita ID del domicilio
     * 2. Invoca domicilioService.eliminar() directamente
     * 3. Marca domicilio.eliminado = TRUE
     *
     * Problemas potenciales:
     * - Personas con domicilio_id apuntando a domicilio "eliminado"
     * - Datos inconsistentes en la BD
     *
     * ALTERNATIVA SEGURA: Opción 10 (eliminarDomicilioPorPersona)
     * - Primero desasocia domicilio de la persona (domicilio_id = NULL)
     * - Luego elimina el domicilio
     * - Garantiza consistencia
     *
     * Uso válido:
     * - Cuando se está seguro de que el domicilio NO tiene personas asociadas
     * - Limpiar domicilios creados por error
     */
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

    /**
     * Opción 7: Actualizar domicilio de una persona específica.
     *
     * Flujo:
     * 1. Solicita ID de la persona
     * 2. Verifica que la persona exista y tenga domicilio
     * 3. Muestra valores actuales del domicilio
     * 4. Permite actualizar calle y número
     * 5. Invoca domicilioService.actualizar()
     *
     * ⚠️ IMPORTANTE (RN-040): Esta operación actualiza el domicilio compartido.
     * Si otras personas tienen el mismo domicilio, también se les actualizará.
     *
     * Diferencia con opción 9 (actualizarDomicilioPorId):
     * - Esta opción: Busca persona primero, luego actualiza su domicilio
     * - Opción 9: Actualiza domicilio directamente por ID
     *
     * Ambas tienen el mismo efecto (RN-040): afectan a TODAS las personas
     * que comparten el domicilio.
     */
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
            System.out.print("Nuevo tipo (" + c.getTipo() + "): ");
            String tipo = scanner.nextLine().trim();
            if (!tipo.isEmpty()) {
                c.setTipo(tipo);
            }

            System.out.print("Nuevo numero (" + c.getValor() + "): ");
            String numero = scanner.nextLine().trim();
            if (!numero.isEmpty()) {
                c.setValor(numero);
            }

            productoService.getCodigoBarrasService().actualizar(c);
            System.out.println("Codigo de barras actualizado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al actualizar codigo de barras: " + e.getMessage());
        }
    }

    /**
     * Opción 10: Eliminar domicilio de una persona (MÉTODO SEGURO - RN-029 solucionado).
     *
     * Flujo transaccional SEGURO:
     * 1. Solicita ID de la persona
     * 2. Verifica que la persona exista y tenga domicilio
     * 3. Invoca personaService.eliminarDomicilioDePersona() que:
     *    a. Desasocia domicilio de persona (persona.domicilio = null)
     *    b. Actualiza persona en BD (domicilio_id = NULL)
     *    c. Elimina el domicilio (ahora no hay FKs apuntando a él)
     *
     * Ventaja sobre opción 8 (eliminarDomicilioPorId):
     * - Garantiza consistencia: Primero actualiza FK, luego elimina
     * - NO deja referencias huérfanas
     * - Implementa eliminación segura recomendada en RN-029
     *
     * Este es el método RECOMENDADO para eliminar domicilios en producción.
     */
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

    /**
     * Método auxiliar privado: Crea un objeto Domicilio capturando calle y número.
     *
     * Flujo:
     * 1. Solicita calle (con trim)
     * 2. Solicita número (con trim)
     * 3. Crea objeto Domicilio con ID=0 (será asignado por BD al insertar)
     *
     * Usado por:
     * - crearPersona(): Para agregar domicilio al crear persona
     * - crearDomicilioIndependiente(): Para crear domicilio sin asociar
     * - actualizarDomicilioDePersona(): Para agregar domicilio a persona sin domicilio
     *
     * Nota: NO persiste en BD, solo crea el objeto en memoria.
     * El caller es responsable de insertar el domicilio.
     *
     * @return CodigoBarras nuevo (no persistido, ID=0)
     */
    private CodigoBarras crearCodBarras() {
        System.out.print("Calle: ");
        String calle = scanner.nextLine().trim();
        System.out.print("Numero: ");
        String numero = scanner.nextLine().trim();
        return new CodigoBarras(0, calle, numero);
    }

    /**
     * Método auxiliar privado: Maneja actualización de domicilio dentro de actualizar persona.
     *
     * Casos:
     * 1. Persona TIENE domicilio:
     *    - Pregunta si desea actualizar
     *    - Si sí, permite cambiar calle y número (Enter para mantener)
     *    - Actualiza domicilio en BD (afecta a TODAS las personas que lo comparten)
     *
     * 2. Persona NO TIENE domicilio:
     *    - Pregunta si desea agregar uno
     *    - Si sí, captura calle y número con crearDomicilio()
     *    - Inserta domicilio en BD (obtiene ID)
     *    - Asocia domicilio a la persona
     *
     * Usado exclusivamente por actualizarPersona() (opción 3).
     *
     * IMPORTANTE: El parámetro Persona se modifica in-place (setDomicilio).
     * El caller debe invocar personaService.actualizar() después para persistir.
     *
     * @param p Producto a la que se le actualizará/agregará codigoBarras
     * @throws Exception Si hay error al insertar/actualizar codigoBarras
     */
    private void actualizarCodBarrasDeProducto(Producto p) throws Exception {
        if (p.getCodBarras() != null) {
            System.out.print("¿Desea actualizar el domicilio? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                System.out.print("Nueva calle (" + p.getCodBarras().getTipo() + "): ");
                String calle = scanner.nextLine().trim();
                if (!calle.isEmpty()) {
                    p.getCodBarras().setTipo(calle);
                }

                System.out.print("Nuevo numero (" + p.getCodBarras().getValor() + "): ");
                String numero = scanner.nextLine().trim();
                if (!numero.isEmpty()) {
                    p.getCodBarras().setValor(numero);
                }

                productoService.getCodigoBarrasService().actualizar(p.getCodBarras());
            }
        } else {
            System.out.print("La persona no tiene domicilio. ¿Desea agregar uno? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                CodigoBarras nuevoDom = crearCodBarras();
                productoService.getCodigoBarrasService().insertar(nuevoDom);
                p.setCodBarras(nuevoDom);
            }
        }
    }
}