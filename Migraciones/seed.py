"""
Script de datos de prueba (seed) para el proyecto de finanzas personales.

Usuario de prueba: juliana@correo.com / clave123
Periodo: mayo 2025 - abril 2026 (12 meses completos)

Resumen:
  - 17 ingresos  (salario mensual + primas + extras)
  - 87 gastos    (8 categorias, variacion realista mes a mes)
  - 5  metas
  - 5  alertas
  - 5  recompensas

Para ejecutar:
  python Migraciones/seed.py
"""

import sqlite3
import sys
from pathlib import Path
from collections import defaultdict

DB_PATH = Path(__file__).parent.parent / "finanzas.db"

sys.path.insert(0, str(Path(__file__).parent))
from crear_db import crear_base_datos


# ─────────────────────────────────────────────────────────────
#  INGRESOS  (17 registros)
#  (monto, fecha, concepto)
# ─────────────────────────────────────────────────────────────
INGRESOS = [
    (2_500_000, "2025-05-01", "Salario mayo"),
    (2_500_000, "2025-06-02", "Salario junio"),
    (  500_000, "2025-06-15", "Prima mitad de año"),
    (2_500_000, "2025-07-01", "Salario julio"),
    (2_500_000, "2025-08-01", "Salario agosto"),
    (2_500_000, "2025-09-01", "Salario septiembre"),
    (2_500_000, "2025-10-01", "Salario octubre"),
    (  350_000, "2025-10-20", "Comision de ventas"),
    (2_500_000, "2025-11-03", "Salario noviembre"),
    (2_500_000, "2025-12-01", "Salario diciembre"),
    (  600_000, "2025-12-15", "Prima navidena"),
    (2_500_000, "2026-01-05", "Salario enero"),
    (2_500_000, "2026-02-03", "Salario febrero"),
    (  300_000, "2026-02-20", "Freelance diseno grafico"),
    (2_500_000, "2026-03-03", "Salario marzo"),
    (2_500_000, "2026-04-01", "Salario abril"),
    (  400_000, "2026-04-15", "Comision de ventas"),
]
# Total ingresos: 32.150.000


# ─────────────────────────────────────────────────────────────
#  GASTOS  (87 registros)
#  (nombre, monto, fecha, categoria)
# ─────────────────────────────────────────────────────────────
GASTOS = [

    # ── Mayo 2025 ── total: 1.200.000
    ("Mercado semanal",       350_000, "2025-05-05", "Alimentacion"),
    ("Bus mayo",              150_000, "2025-05-05", "Transporte"),
    ("Servicios del hogar",    90_000, "2025-05-15", "Servicios"),
    ("Internet",               60_000, "2025-05-15", "Servicios"),
    ("Cine fin de semana",     80_000, "2025-05-17", "Entretenimiento"),
    ("Farmacia",              120_000, "2025-05-22", "Salud"),
    ("Ropa de temporada",     350_000, "2025-05-28", "Ropa"),

    # ── Junio 2025 ── total: 1.800.000  (vacaciones + prima)
    ("Mercado junio",         380_000, "2025-06-04", "Alimentacion"),
    ("Bus junio",             150_000, "2025-06-04", "Transporte"),
    ("Servicios del hogar",    90_000, "2025-06-15", "Servicios"),
    ("Internet",               60_000, "2025-06-15", "Servicios"),
    ("Viaje de vacaciones",   800_000, "2025-06-21", "Viajes"),
    ("Restaurantes vacac.",   200_000, "2025-06-23", "Alimentacion"),
    ("Entretenimiento viaje", 120_000, "2025-06-25", "Entretenimiento"),

    # ── Julio 2025 ── total: 1.100.000
    ("Mercado julio",         360_000, "2025-07-03", "Alimentacion"),
    ("Bus julio",             150_000, "2025-07-03", "Transporte"),
    ("Servicios del hogar",    90_000, "2025-07-15", "Servicios"),
    ("Internet",               60_000, "2025-07-15", "Servicios"),
    ("Curso online",          200_000, "2025-07-10", "Educacion"),
    ("Salida con amigos",     150_000, "2025-07-19", "Entretenimiento"),
    ("Medicamentos",           90_000, "2025-07-25", "Salud"),

    # ── Agosto 2025 ── total: 1.350.000  (regreso a clases)
    ("Mercado agosto",        380_000, "2025-08-04", "Alimentacion"),
    ("Bus agosto",            150_000, "2025-08-04", "Transporte"),
    ("Servicios del hogar",    90_000, "2025-08-15", "Servicios"),
    ("Internet",               60_000, "2025-08-15", "Servicios"),
    ("Ropa regreso a clases", 300_000, "2025-08-08", "Ropa"),
    ("Utiles y papeleria",    180_000, "2025-08-09", "Educacion"),
    ("Salida con amigos",     190_000, "2025-08-23", "Entretenimiento"),

    # ── Septiembre 2025 ── total: 1.200.000
    ("Mercado septiembre",    400_000, "2025-09-04", "Alimentacion"),
    ("Bus septiembre",        180_000, "2025-09-04", "Transporte"),
    ("Servicios del hogar",    90_000, "2025-09-15", "Servicios"),
    ("Internet",               60_000, "2025-09-15", "Servicios"),
    ("Consulta medica",       200_000, "2025-09-11", "Salud"),
    ("Streaming",              80_000, "2025-09-20", "Entretenimiento"),
    ("Restaurante cumple",    190_000, "2025-09-27", "Alimentacion"),

    # ── Octubre 2025 ── total: 1.400.000
    ("Mercado octubre",       420_000, "2025-10-06", "Alimentacion"),
    ("Bus octubre",           180_000, "2025-10-06", "Transporte"),
    ("Servicios del hogar",    90_000, "2025-10-15", "Servicios"),
    ("Internet",               60_000, "2025-10-15", "Servicios"),
    ("Halloween fiesta",      180_000, "2025-10-30", "Entretenimiento"),
    ("Ropa octubre",          250_000, "2025-10-12", "Ropa"),
    ("Curso de ingles",       150_000, "2025-10-07", "Educacion"),
    ("Farmacia",               70_000, "2025-10-22", "Salud"),

    # ── Noviembre 2025 ── total: 1.100.000
    ("Mercado noviembre",     350_000, "2025-11-04", "Alimentacion"),
    ("Bus noviembre",         180_000, "2025-11-04", "Transporte"),
    ("Servicios del hogar",    90_000, "2025-11-15", "Servicios"),
    ("Internet",               60_000, "2025-11-15", "Servicios"),
    ("Cine con amigos",        80_000, "2025-11-21", "Entretenimiento"),
    ("Mercado segunda quinc.", 180_000, "2025-11-25", "Alimentacion"),
    ("Medicamentos",          160_000, "2025-11-28", "Salud"),

    # ── Diciembre 2025 ── total: 2.200.000  (navidad - mes de mayor gasto)
    ("Mercado navideno",      550_000, "2025-12-05", "Alimentacion"),
    ("Bus diciembre",         180_000, "2025-12-06", "Transporte"),
    ("Servicios del hogar",    90_000, "2025-12-15", "Servicios"),
    ("Internet",               60_000, "2025-12-15", "Servicios"),
    ("Ropa de temporada",     400_000, "2025-12-08", "Ropa"),
    ("Regalos de navidad",    600_000, "2025-12-18", "Entretenimiento"),
    ("Cena navidena",         180_000, "2025-12-24", "Alimentacion"),
    ("Decoraciones",          140_000, "2025-12-10", "Entretenimiento"),

    # ── Enero 2026 ── total: 1.300.000  (gastos de salud - nuevos propositos)
    ("Mercado enero",         420_000, "2026-01-06", "Alimentacion"),
    ("Bus enero",             180_000, "2026-01-07", "Transporte"),
    ("Consulta medica",       250_000, "2026-01-12", "Salud"),
    ("Medicamentos",           80_000, "2026-01-13", "Salud"),
    ("Servicios del hogar",    90_000, "2026-01-15", "Servicios"),
    ("Internet",               60_000, "2026-01-15", "Servicios"),
    ("Gimnasio enero",        220_000, "2026-01-02", "Salud"),

    # ── Febrero 2026 ── total: 1.400.000
    ("Mercado febrero",       420_000, "2026-02-04", "Alimentacion"),
    ("Bus febrero",           180_000, "2026-02-05", "Transporte"),
    ("Servicios del hogar",    90_000, "2026-02-15", "Servicios"),
    ("Cena San Valentin",     200_000, "2026-02-14", "Alimentacion"),
    ("Ropa nueva",            300_000, "2026-02-16", "Ropa"),
    ("Streaming",              80_000, "2026-02-20", "Entretenimiento"),
    ("Internet",               60_000, "2026-02-15", "Servicios"),
    ("Flores San Valentin",    70_000, "2026-02-14", "Entretenimiento"),

    # ── Marzo 2026 ── total: 1.350.000
    # Alimentacion en marzo: 300+200+350 = 850.000 de 1.350.000 = 63% -> alerta concentracion
    ("Mercado semanal",       300_000, "2026-03-04", "Alimentacion"),
    ("Bus marzo",             200_000, "2026-03-05", "Transporte"),
    ("Restaurante",           200_000, "2026-03-08", "Alimentacion"),
    ("Mercado semanal",       350_000, "2026-03-18", "Alimentacion"),
    ("Servicios del hogar",    90_000, "2026-03-15", "Servicios"),
    ("Internet",               60_000, "2026-03-15", "Servicios"),
    ("Salida con amigos",     150_000, "2026-03-22", "Entretenimiento"),

    # ── Abril 2026 ── total: 1.800.000  (Semana Santa)
    ("Mercado abril",         500_000, "2026-04-04", "Alimentacion"),
    ("Bus abril",             200_000, "2026-04-05", "Transporte"),
    ("Zapatos nuevos",        280_000, "2026-04-08", "Ropa"),
    ("Salida con amigos",     200_000, "2026-04-12", "Entretenimiento"),
    ("Servicios del hogar",    90_000, "2026-04-15", "Servicios"),
    ("Internet",               60_000, "2026-04-15", "Servicios"),
    ("Cumpleanos amigo",      180_000, "2026-04-19", "Entretenimiento"),
    ("Medico especialista",   150_000, "2026-04-21", "Salud"),
    ("Viaje Semana Santa",    140_000, "2026-04-17", "Viajes"),
]
# Total gastos: 17.200.000


# ─────────────────────────────────────────────────────────────
#  METAS  (5 registros)
#  (nombre, objetivo, actual, fecha_registro, fecha_esperada)
# ─────────────────────────────────────────────────────────────
METAS = [
    # 90% -> recompensa Meta avanzada + alerta Meta en progreso
    ("Viaje a la playa",     3_000_000,  2_700_000, "2025-07-01", "2026-06-30"),
    # 100% -> recompensa Meta cumplida
    ("Laptop nueva",         2_500_000,  2_500_000, "2025-09-01", "2026-04-30"),
    # 45%
    ("Fondo de emergencia", 10_000_000,  4_500_000, "2025-05-01", "2027-01-01"),
    # 30%
    ("Carro nuevo",         20_000_000,  6_000_000, "2025-08-01", "2028-06-01"),
    # 20% -> fase inicial
    ("Viaje a Europa",      15_000_000,  3_000_000, "2026-01-01", "2027-12-01"),
]


# ─────────────────────────────────────────────────────────────
#  ALERTAS  (5 registros)
#  (tipo, mensaje, fecha, leida)
# ─────────────────────────────────────────────────────────────
ALERTAS = [
    (
        "Concentracion",
        "La categoria 'Alimentacion' representa el 44% de tus gastos totales acumulados. "
        "Considera redistribuir ese presupuesto en otras categorias.",
        "2026-04-04",
        0,
    ),
    (
        "Deficit",
        "En diciembre tus gastos ($3,100,000) superaron tus ingresos ($3,100,000). "
        "Revisa tu presupuesto para evitar desequilibrios.",
        "2025-12-31",
        1,
    ),
    (
        "Precaucion",
        "Has gastado el 83% de tus ingresos de diciembre. "
        "Cuida el presupuesto en enero para recuperar el equilibrio.",
        "2025-12-28",
        1,
    ),
    (
        "Meta en progreso",
        "Tu meta 'Viaje a la playa' esta al 90%. "
        "Estas muy cerca de cumplirla, solo faltan $300,000.",
        "2026-03-10",
        1,
    ),
    (
        "Precaucion",
        "En junio el gasto en Viajes represento el 44% del ingreso mensual. "
        "Planifica con anticipacion los proximos viajes.",
        "2025-06-30",
        1,
    ),
]


# ─────────────────────────────────────────────────────────────
#  RECOMPENSAS  (5 registros)
#  (tipo, mensaje, fecha_desbloqueo, desbloqueada)
# ─────────────────────────────────────────────────────────────
RECOMPENSAS = [
    (
        "Superavit",
        "Excelente! Mantuviste tus gastos por debajo de tus ingresos durante 6 meses consecutivos. "
        "Sigue construyendo tu patrimonio.",
        "2025-11-01",
        1,
    ),
    (
        "Meta avanzada",
        "Vas muy bien con tu meta 'Viaje a la playa'. "
        "Llevas el 90% del objetivo. Ya casi lo logras!",
        "2026-03-10",
        1,
    ),
    (
        "Meta cumplida",
        "Felicitaciones! Completaste tu meta 'Laptop nueva' con $2,500,000 ahorrados. "
        "Excelente disciplina financiera!",
        "2026-02-28",
        1,
    ),
    (
        "Superavit",
        "Cerraste el primer semestre (mayo-oct 2025) con superavit de $12,050,000. "
        "Tus habitos financieros son solidos.",
        "2025-10-31",
        1,
    ),
    (
        "Meta avanzada",
        "Tu fondo de emergencia ya cubre 2 meses de gastos (45% del objetivo). "
        "Sigue aportando mensualmente para mayor seguridad.",
        "2026-04-01",
        0,
    ),
]


# ─────────────────────────────────────────────────────────────
#  EJECUCION
# ─────────────────────────────────────────────────────────────
def main() -> None:
    total_ingresos = sum(m for m, _, _ in INGRESOS)
    total_gastos   = sum(m for _, m, _, _ in GASTOS)
    saldo_final    = total_ingresos - total_gastos

    print("Recreando base de datos...")
    crear_base_datos(str(DB_PATH))

    print("Insertando datos de prueba...")
    with sqlite3.connect(DB_PATH) as conn:
        conn.execute("PRAGMA foreign_keys = ON")
        cur = conn.cursor()

        cur.execute(
            "INSERT INTO usuario (id, nombre, contrasena, correo, numero_telefono, edad, cc, saldo_actual) "
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            (1, "Juliana Garcia", "clave123", "juliana@correo.com",
             "3101234567", 22, "1098765432", saldo_final),
        )

        for monto, fecha, concepto in INGRESOS:
            cur.execute(
                "INSERT INTO ingreso (monto, fecha, concepto, id_usuario) VALUES (?, ?, ?, ?)",
                (monto, fecha, concepto, 1),
            )

        for nombre, monto, fecha, categoria in GASTOS:
            cur.execute(
                "INSERT INTO gasto (nombre, monto, fecha, categoria, id_usuario) VALUES (?, ?, ?, ?, ?)",
                (nombre, monto, fecha, categoria, 1),
            )

        for nombre, objetivo, actual, f_reg, f_esp in METAS:
            cur.execute(
                "INSERT INTO meta (nombre, monto_objetivo, monto_actual, fecha_registro, fecha_esperada, id_usuario) "
                "VALUES (?, ?, ?, ?, ?, ?)",
                (nombre, objetivo, actual, f_reg, f_esp, 1),
            )

        for tipo, mensaje, fecha, leida in ALERTAS:
            cur.execute(
                "INSERT INTO alerta (tipo, mensaje, fecha, leida, id_usuario) VALUES (?, ?, ?, ?, ?)",
                (tipo, mensaje, fecha, leida, 1),
            )

        for tipo, mensaje, fecha, desbloqueada in RECOMPENSAS:
            cur.execute(
                "INSERT INTO recompensa (tipo, mensaje, fecha_desbloqueo, desbloqueada, id_usuario) "
                "VALUES (?, ?, ?, ?, ?)",
                (tipo, mensaje, fecha, desbloqueada, 1),
            )

        conn.commit()

    por_cat: dict[str, int] = defaultdict(int)
    for _, monto, _, cat in GASTOS:
        por_cat[cat] += monto

    por_mes: dict[str, int] = defaultdict(int)
    for _, monto, fecha, _ in GASTOS:
        por_mes[fecha[:7]] += monto

    print()
    print("=" * 58)
    print("  SEED CREADO EXITOSAMENTE - 1 ANIO DE DATOS")
    print("=" * 58)
    print(f"  Correo  : juliana@correo.com")
    print(f"  Clave   : clave123")
    print("-" * 58)
    print(f"  Periodo : mayo 2025 - abril 2026  (12 meses)")
    print(f"  Ingresos: {len(INGRESOS):>2} registros  ->  ${total_ingresos:>14,.0f}")
    print(f"  Gastos  : {len(GASTOS):>2} registros  ->  ${total_gastos:>14,.0f}")
    print(f"  Saldo   :                    ${saldo_final:>14,.0f}")
    print("-" * 58)
    print("  Gasto por mes:")
    for mes in sorted(por_mes):
        barra = "#" * (por_mes[mes] // 100_000)
        print(f"    {mes}   ${por_mes[mes]:>11,.0f}   {barra}")
    print("-" * 58)
    print("  Gasto por categoria:")
    for cat, total in sorted(por_cat.items(), key=lambda x: -x[1]):
        pct = total / total_gastos * 100
        barra = "#" * int(pct / 3)
        print(f"    {cat:<18} ${total:>9,.0f}  {pct:>5.1f}%  {barra}")
    print("-" * 58)
    print(f"  Metas       : {len(METAS)}  (100% - 90% - 45% - 30% - 20%)")
    print(f"  Alertas     : {len(ALERTAS)}  (1 pendiente - 4 leidas)")
    print(f"  Recompensas : {len(RECOMPENSAS)}  (4 desbloqueadas - 1 pendiente)")
    print("=" * 58)


if __name__ == "__main__":
    main()
