classDiagram
direction TB

class Persona {
    <<abstract>>
    - int id
    - String nombre
    - String contrasena
    - String correo
    - String numeroTelefono
    - int edad
    - String cc
    + actualizarInfo(nombre:String, correo:String, telefono:String, edad:int): void
}

class Usuario {
    - double saldoActual
    + registrarUsuario(): void
    + leerPerfil(): void
}

class Admin {
    + buscarUsuario(id:int): Usuario
    + eliminarUsuario(id:int): boolean
    + listarUsuarios(): void
}

Persona <|-- Usuario
Persona <|-- Admin

class Ingreso {
    - int idIngreso
    - double monto
    - LocalDate fecha
    - String concepto
    - int idUsuario
    + registrarIngreso(): void
    + actualizarIngreso(): void
    + eliminarIngreso(idIngreso:int): void
    + leerIngreso(idIngreso:int): Ingreso
    + listarIngresosPorUsuario(idUsuario:int): List~Ingreso~
}

class Gasto {
    - int idGasto
    - String nombre
    - double monto
    - LocalDate fecha
    - String categoria
    - int idUsuario
    + agregarGasto(): void
    + actualizarGasto(): void
    + eliminarGasto(idGasto:int): void
    + leerGasto(idGasto:int): Gasto
    + listarGastosPorUsuario(idUsuario:int): List~Gasto~
}

class Meta {
    - int idMeta
    - String nombre
    - double montoObjetivo
    - double montoActual
    - LocalDate fechaRegistro
    - LocalDate fechaEsperada
    - int idUsuario
    + agregarMeta(): void
    + actualizarMeta(): void
    + eliminarMeta(idMeta:int): void
    + leerMeta(idMeta:int): Meta
    + listarMetasPorUsuario(idUsuario:int): List~Meta~
    + calcularProgreso(): double
}

class Alerta {
    - int idAlerta
    - String tipo
    - String mensaje
    - LocalDate fecha
    - boolean leida
    - int idUsuario
    + crearAlerta(): void
    + eliminarAlerta(idAlerta:int): void
    + leerAlerta(idAlerta:int): Alerta
    + listarAlertasPorUsuario(idUsuario:int): List~Alerta~
    + marcarComoLeida(): void
}

class Recompensa {
    - int idRecompensa
    - String tipo
    - String mensaje
    - LocalDate fechaDesbloqueo
    - boolean desbloqueada
    - int idUsuario
    + crearRecompensa(): void
    + eliminarRecompensa(idRecompensa:int): void
    + leerRecompensa(idRecompensa:int): Recompensa
    + listarRecompensasPorUsuario(idUsuario:int): List~Recompensa~
}

class AnalizadorFinanciero {
    + calcularTotalIngresos(idUsuario:int): double
    + calcularTotalGastos(idUsuario:int): double
    + calcularAhorro(idUsuario:int): double
    + calcularSaludFinanciera(idUsuario:int): double
    + predecirGastoSiguienteMes(idUsuario:int): double
    + generarAlertaPresupuesto(idUsuario:int): String
    + detectarAumentoCategoria(idUsuario:int, categoria:String): String
    + evaluarRecompensas(idUsuario:int): String
}

class VisualizadorGraficas {
    + mostrarGraficaGastosPorCategoria(idUsuario:int): void
    + mostrarGraficaIngresosMensuales(idUsuario:int): void
    + mostrarGraficaProgresoMetas(idUsuario:int): void
    + mostrarGraficaSaludFinanciera(idUsuario:int): void
}

Usuario "1" --> "0..*" Ingreso : registra
Usuario "1" --> "0..*" Gasto : registra
Usuario "1" --> "0..*" Meta : define
Usuario "1" --> "0..*" Alerta : recibe
Usuario "1" --> "0..*" Recompensa : obtiene

AnalizadorFinanciero ..> Ingreso : analiza
AnalizadorFinanciero ..> Gasto : analiza
AnalizadorFinanciero ..> Meta : evalua
AnalizadorFinanciero ..> Alerta : genera
AnalizadorFinanciero ..> Recompensa : asigna
VisualizadorGraficas ..> AnalizadorFinanciero : usa resultados
Admin ..> Usuario : administra
