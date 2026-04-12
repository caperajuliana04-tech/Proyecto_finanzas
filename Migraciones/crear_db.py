import sqlite3
from pathlib import Path


DB_NAME = "finanzas.db"


def crear_base_datos(nombre_db: str = DB_NAME) -> None:
    ruta_db = Path(nombre_db)

    with sqlite3.connect(ruta_db) as conn:
        conn.execute("PRAGMA foreign_keys = ON;")

        cursor = conn.cursor()

        cursor.executescript("""
        PRAGMA foreign_keys = ON;

        ------------------------------------------------------------
        -- TABLA BASE: PERSONA
        ------------------------------------------------------------
        CREATE TABLE IF NOT EXISTS persona (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            contrasena TEXT NOT NULL,
            correo TEXT NOT NULL UNIQUE,
            numeroTelefono TEXT,
            edad INTEGER NOT NULL CHECK (edad >= 0),
            cc TEXT NOT NULL UNIQUE
        );

        ------------------------------------------------------------
        -- HERENCIA: USUARIO
        -- Usa el mismo id de persona
        ------------------------------------------------------------
        CREATE TABLE IF NOT EXISTS usuario (
            id INTEGER PRIMARY KEY,
            saldoActual REAL NOT NULL DEFAULT 0 CHECK (saldoActual >= 0),
            FOREIGN KEY (id) REFERENCES persona(id) ON DELETE CASCADE ON UPDATE CASCADE
        );

        ------------------------------------------------------------
        -- HERENCIA: ADMIN
        -- Usa el mismo id de persona
        ------------------------------------------------------------
        CREATE TABLE IF NOT EXISTS admin (
            id INTEGER PRIMARY KEY,
            FOREIGN KEY (id) REFERENCES persona(id) ON DELETE CASCADE ON UPDATE CASCADE
        );

        ------------------------------------------------------------
        -- INGRESO
        ------------------------------------------------------------
        CREATE TABLE IF NOT EXISTS ingreso (
            idIngreso INTEGER PRIMARY KEY AUTOINCREMENT,
            monto REAL NOT NULL CHECK (monto >= 0),
            fecha TEXT NOT NULL,
            concepto TEXT NOT NULL,
            idUsuario INTEGER NOT NULL,
            FOREIGN KEY (idUsuario) REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE
        );

        ------------------------------------------------------------
        -- GASTO
        ------------------------------------------------------------
        CREATE TABLE IF NOT EXISTS gasto (
            idGasto INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            monto REAL NOT NULL CHECK (monto >= 0),
            fecha TEXT NOT NULL,
            categoria TEXT NOT NULL,
            idUsuario INTEGER NOT NULL,
            FOREIGN KEY (idUsuario) REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE
        );

        ------------------------------------------------------------
        -- META
        ------------------------------------------------------------
        CREATE TABLE IF NOT EXISTS meta (
            idMeta INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            montoObjetivo REAL NOT NULL CHECK (montoObjetivo > 0),
            montoActual REAL NOT NULL DEFAULT 0 CHECK (montoActual >= 0),
            fechaRegistro TEXT NOT NULL,
            fechaEsperada TEXT NOT NULL,
            idUsuario INTEGER NOT NULL,
            FOREIGN KEY (idUsuario) REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE
        );

        ------------------------------------------------------------
        -- ALERTA
        ------------------------------------------------------------
        CREATE TABLE IF NOT EXISTS alerta (
            idAlerta INTEGER PRIMARY KEY AUTOINCREMENT,
            tipo TEXT NOT NULL,
            mensaje TEXT NOT NULL,
            fecha TEXT NOT NULL,
            leida INTEGER NOT NULL DEFAULT 0 CHECK (leida IN (0, 1)),
            idUsuario INTEGER NOT NULL,
            FOREIGN KEY (idUsuario) REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE
        );

        ------------------------------------------------------------
        -- RECOMPENSA
        ------------------------------------------------------------
        CREATE TABLE IF NOT EXISTS recompensa (
            idRecompensa INTEGER PRIMARY KEY AUTOINCREMENT,
            tipo TEXT NOT NULL,
            mensaje TEXT NOT NULL,
            fechaDesbloqueo TEXT NOT NULL,
            desbloqueada INTEGER NOT NULL DEFAULT 0 CHECK (desbloqueada IN (0, 1)),
            idUsuario INTEGER NOT NULL,
            FOREIGN KEY (idUsuario) REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE
        );

        ------------------------------------------------------------
        -- ÍNDICES
        ------------------------------------------------------------
        CREATE INDEX IF NOT EXISTS idx_ingreso_usuario ON ingreso(idUsuario);
        CREATE INDEX IF NOT EXISTS idx_gasto_usuario ON gasto(idUsuario);
        CREATE INDEX IF NOT EXISTS idx_meta_usuario ON meta(idUsuario);
        CREATE INDEX IF NOT EXISTS idx_alerta_usuario ON alerta(idUsuario);
        CREATE INDEX IF NOT EXISTS idx_recompensa_usuario ON recompensa(idUsuario);
        CREATE INDEX IF NOT EXISTS idx_gasto_categoria ON gasto(categoria);

        ------------------------------------------------------------
        -- TRIGGERS PARA MANTENER saldoActual AUTOMÁTICAMENTE
        -- saldoActual = sum(ingresos) - sum(gastos)
        ------------------------------------------------------------

        -- INGRESO: INSERT
        CREATE TRIGGER IF NOT EXISTS trg_ingreso_insert
        AFTER INSERT ON ingreso
        BEGIN
            UPDATE usuario
            SET saldoActual = saldoActual + NEW.monto
            WHERE id = NEW.idUsuario;
        END;

        -- INGRESO: DELETE
        CREATE TRIGGER IF NOT EXISTS trg_ingreso_delete
        AFTER DELETE ON ingreso
        BEGIN
            UPDATE usuario
            SET saldoActual = saldoActual - OLD.monto
            WHERE id = OLD.idUsuario;
        END;

        -- INGRESO: UPDATE
        CREATE TRIGGER IF NOT EXISTS trg_ingreso_update
        AFTER UPDATE OF monto, idUsuario ON ingreso
        BEGIN
            UPDATE usuario
            SET saldoActual = saldoActual - OLD.monto
            WHERE id = OLD.idUsuario;

            UPDATE usuario
            SET saldoActual = saldoActual + NEW.monto
            WHERE id = NEW.idUsuario;
        END;

        -- GASTO: INSERT
        CREATE TRIGGER IF NOT EXISTS trg_gasto_insert
        AFTER INSERT ON gasto
        BEGIN
            UPDATE usuario
            SET saldoActual = saldoActual - NEW.monto
            WHERE id = NEW.idUsuario;
        END;

        -- GASTO: DELETE
        CREATE TRIGGER IF NOT EXISTS trg_gasto_delete
        AFTER DELETE ON gasto
        BEGIN
            UPDATE usuario
            SET saldoActual = saldoActual + OLD.monto
            WHERE id = OLD.idUsuario;
        END;

        -- GASTO: UPDATE
        CREATE TRIGGER IF NOT EXISTS trg_gasto_update
        AFTER UPDATE OF monto, idUsuario ON gasto
        BEGIN
            UPDATE usuario
            SET saldoActual = saldoActual + OLD.monto
            WHERE id = OLD.idUsuario;

            UPDATE usuario
            SET saldoActual = saldoActual - NEW.monto
            WHERE id = NEW.idUsuario;
        END;
        """)

        conn.commit()

        print(f"Base de datos creada correctamente: {ruta_db.resolve()}")

        # Mostrar tablas creadas
        cursor.execute("""
            SELECT name
            FROM sqlite_master
            WHERE type='table'
            AND name NOT LIKE 'sqlite_%'
            ORDER BY name;
        """)
        tablas = cursor.fetchall()

        print("\nTablas creadas:")
        for tabla in tablas:
            print(f"- {tabla[0]}")


if __name__ == "__main__":
    crear_base_datos()