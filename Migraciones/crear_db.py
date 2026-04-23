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

        DROP TABLE IF EXISTS recompensa;
        DROP TABLE IF EXISTS alerta;
        DROP TABLE IF EXISTS meta;
        DROP TABLE IF EXISTS gasto;
        DROP TABLE IF EXISTS ingreso;
        DROP TABLE IF EXISTS admin;
        DROP TABLE IF EXISTS usuario;

        ------------------------------------------------------------
        -- USUARIO
        -- Incluye los campos heredados de Persona + saldo_actual
        ------------------------------------------------------------
        CREATE TABLE usuario (
            id INTEGER PRIMARY KEY,
            nombre TEXT NOT NULL,
            contrasena TEXT NOT NULL,
            correo TEXT NOT NULL UNIQUE,
            numero_telefono TEXT NOT NULL,
            edad INTEGER NOT NULL CHECK (edad >= 0),
            cc TEXT NOT NULL UNIQUE,
            saldo_actual REAL NOT NULL DEFAULT 0 CHECK (saldo_actual >= 0)
        );

        ------------------------------------------------------------
        -- ADMIN
        -- Incluye los campos heredados de Persona
        ------------------------------------------------------------
        CREATE TABLE admin (
            id INTEGER PRIMARY KEY,
            nombre TEXT NOT NULL,
            contrasena TEXT NOT NULL,
            correo TEXT NOT NULL UNIQUE,
            numero_telefono TEXT NOT NULL,
            edad INTEGER NOT NULL CHECK (edad >= 0),
            cc TEXT NOT NULL UNIQUE
        );

        ------------------------------------------------------------
        -- INGRESO
        ------------------------------------------------------------
        CREATE TABLE ingreso (
            id_ingreso INTEGER PRIMARY KEY AUTOINCREMENT,
            monto REAL NOT NULL CHECK (monto >= 0),
            fecha TEXT NOT NULL,
            concepto TEXT NOT NULL,
            id_usuario INTEGER NOT NULL,
            FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE
        );

        ------------------------------------------------------------
        -- GASTO
        ------------------------------------------------------------
        CREATE TABLE gasto (
            id_gasto INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            monto REAL NOT NULL CHECK (monto >= 0),
            fecha TEXT NOT NULL,
            categoria TEXT NOT NULL,
            id_usuario INTEGER NOT NULL,
            FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE
        );

        ------------------------------------------------------------
        -- META
        ------------------------------------------------------------
        CREATE TABLE meta (
            id_meta INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre TEXT NOT NULL,
            monto_objetivo REAL NOT NULL CHECK (monto_objetivo > 0),
            monto_actual REAL NOT NULL DEFAULT 0 CHECK (monto_actual >= 0),
            fecha_registro TEXT NOT NULL,
            fecha_esperada TEXT NOT NULL,
            id_usuario INTEGER NOT NULL,
            FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE
        );

        ------------------------------------------------------------
        -- ALERTA
        ------------------------------------------------------------
        CREATE TABLE alerta (
            id_alerta INTEGER PRIMARY KEY AUTOINCREMENT,
            tipo TEXT NOT NULL,
            mensaje TEXT NOT NULL,
            fecha TEXT NOT NULL,
            leida INTEGER NOT NULL DEFAULT 0 CHECK (leida IN (0, 1)),
            id_usuario INTEGER NOT NULL,
            FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE
        );

        ------------------------------------------------------------
        -- RECOMPENSA
        ------------------------------------------------------------
        CREATE TABLE recompensa (
            id_recompensa INTEGER PRIMARY KEY AUTOINCREMENT,
            tipo TEXT NOT NULL,
            mensaje TEXT NOT NULL,
            fecha_desbloqueo TEXT NOT NULL,
            desbloqueada INTEGER NOT NULL DEFAULT 0 CHECK (desbloqueada IN (0, 1)),
            id_usuario INTEGER NOT NULL,
            FOREIGN KEY (id_usuario) REFERENCES usuario(id) ON DELETE CASCADE
        );

        ------------------------------------------------------------
        -- INDICES
        ------------------------------------------------------------
        CREATE INDEX idx_ingreso_usuario ON ingreso(id_usuario);
        CREATE INDEX idx_gasto_usuario ON gasto(id_usuario);
        CREATE INDEX idx_meta_usuario ON meta(id_usuario);
        CREATE INDEX idx_alerta_usuario ON alerta(id_usuario);
        CREATE INDEX idx_recompensa_usuario ON recompensa(id_usuario);
        CREATE INDEX idx_gasto_categoria ON gasto(categoria);
        """)

        conn.commit()

        print(f"Base de datos creada correctamente: {ruta_db.resolve()}")

        cursor.execute("""
            SELECT name
            FROM sqlite_master
            WHERE type='table'
            AND name NOT LIKE 'sqlite_%'
            ORDER BY name;
        """)
        tablas = cursor.fetchall()

        print("\\nTablas creadas:")
        for tabla in tablas:
            print(f"- {tabla[0]}")


if __name__ == "__main__":
    crear_base_datos()