const PALETA = ['#10b981','#3b82f6','#8b5cf6','#f59e0b','#ef4444','#06b6d4','#ec4899','#f97316'];

const COLOR_CAT = {
  'Alimentación':   'bg-emerald-50 text-emerald-700 ring-emerald-200',
  'Transporte':     'bg-blue-50 text-blue-700 ring-blue-200',
  'Servicios':      'bg-purple-50 text-purple-700 ring-purple-200',
  'Salud':          'bg-red-50 text-red-700 ring-red-200',
  'Ropa':           'bg-amber-50 text-amber-700 ring-amber-200',
  'Educación':      'bg-cyan-50 text-cyan-700 ring-cyan-200',
  'Entretenimiento':'bg-pink-50 text-pink-700 ring-pink-200',
  'Viajes':         'bg-orange-50 text-orange-700 ring-orange-200',
};

function hoy() {
  return new Date().toISOString().split('T')[0];
}

function app() {
  return {
    // ── Estado ──────────────────────────────────────────────────────────────────
    token:   localStorage.getItem('finanzas_token') || '',
    nombre:  localStorage.getItem('finanzas_nombre') || '',
    seccion: 'inicio',
    cargando: false,

    resumen:     null,
    estadisticas: null,
    ingresos:    [],
    gastos:      [],
    metas:       [],
    alertas:     [],
    recompensas: [],

    _chart: null,
    _charts: { g1: null, g2: null, g3: null, g5: null },

    mostrarFormIngreso: false,
    mostrarFormGasto:   false,
    mostrarFormMeta:    false,

    formIngreso: { monto: '', concepto: '', fecha: hoy() },
    formGasto:   { nombre: '', monto: '', categoria: 'Alimentación', fecha: hoy() },
    formMeta:    { nombre: '', montoObjetivo: '', fechaEsperada: '' },

    notif: { visible: false, texto: '', ok: true },

    // ── Inicialización ───────────────────────────────────────────────────────────
    init() {
      if (!this.token) { window.location.href = '/'; return; }
      this.navegarA('inicio');
    },

    // ── Fetch wrapper ────────────────────────────────────────────────────────────
    async api(path, method = 'GET', body = null) {
      const opts = {
        headers: {
          'Authorization': 'Bearer ' + this.token,
          'Content-Type': 'application/json'
        }
      };
      if (method !== 'GET' && body != null) {
        opts.method = method;
        opts.body = JSON.stringify(body);
      } else if (method !== 'GET') {
        opts.method = method;
      }
      const r = await fetch('/api/' + path, opts);
      if (r.status === 401) { window.location.href = '/'; return null; }
      return r.json();
    },

    // ── Navegación ───────────────────────────────────────────────────────────────
    async navegarA(sec) {
      this.seccion  = sec;
      this.cargando = true;
      this.mostrarFormIngreso = false;
      this.mostrarFormGasto   = false;
      this.mostrarFormMeta    = false;

      try {
        if (sec === 'inicio')       await this.cargarInicio();
        else if (sec === 'graficas')    await this.cargarGraficas();
        else if (sec === 'ingresos')    this.ingresos    = await this.api('ingresos')    ?? [];
        else if (sec === 'gastos')      this.gastos      = await this.api('gastos')      ?? [];
        else if (sec === 'metas')       this.metas       = await this.api('metas')       ?? [];
        else if (sec === 'alertas')     this.alertas     = await this.api('alertas')     ?? [];
        else if (sec === 'recompensas') this.recompensas = await this.api('recompensas') ?? [];
      } finally {
        this.cargando = false;
      }
    },

    // ── Dashboard ────────────────────────────────────────────────────────────────
    async cargarInicio() {
      this.resumen = await this.api('inicio');
      this.$nextTick(() => this.renderGrafico());
    },

    renderGrafico() {
      const canvas = document.getElementById('graficoGastos');
      if (!canvas || !this.resumen?.gastosPorCategoria) return;
      if (this._chart) { this._chart.destroy(); this._chart = null; }
      const cats = Object.entries(this.resumen.gastosPorCategoria);
      if (cats.length === 0) return;
      this._chart = new Chart(canvas, {
        type: 'doughnut',
        data: {
          labels: cats.map(([k]) => k),
          datasets: [{
            data: cats.map(([, v]) => v),
            backgroundColor: cats.map((_, i) => PALETA[i % PALETA.length]),
            borderWidth: 0,
            hoverOffset: 6
          }]
        },
        options: {
          responsive: true,
          cutout: '65%',
          plugins: {
            legend: { position: 'bottom', labels: { padding: 16, boxWidth: 12, font: { size: 12 } } },
            tooltip: {
              callbacks: {
                label: (ctx) => ' ' + formatCOP(ctx.parsed)
              }
            }
          }
        }
      });
    },

    // ── Gráficas (5 vistas) ──────────────────────────────────────────────────────
    async cargarGraficas() {
      const [resumen, gastos, metas] = await Promise.all([
        this.api('inicio'), this.api('gastos'), this.api('metas')
      ]);
      const gastosPorMes = this.agruparGastosPorMes(gastos ?? []);
      const gastosPorCategoria = resumen?.gastosPorCategoria ?? {};
      const totalG = resumen?.totalGastos ?? 0;
      // Detecta si alguna categoría concentra más del 40% del total (interpretación de la gráfica 5)
      const concentracionAlta = totalG > 0 &&
        Object.values(gastosPorCategoria).some(v => (v / totalG) * 100 > 40);

      this.estadisticas = {
        totalIngresos: resumen?.totalIngresos ?? 0,
        totalGastos: totalG,
        gastosPorCategoria,
        gastosPorMes,
        concentracionAlta
      };
      this.metas = metas ?? [];
      this.$nextTick(() => this.renderTodasLasGraficas());
    },

    // Agrupa la lista de gastos por mes (yyyy-MM) sumando montos
    agruparGastosPorMes(gastos) {
      const out = {};
      for (const g of gastos) {
        if (!g.fecha) continue;
        const mes = g.fecha.slice(0, 7); // "yyyy-MM-dd" → "yyyy-MM"
        out[mes] = (out[mes] ?? 0) + Number(g.monto);
      }
      // Ordena cronológicamente
      return Object.fromEntries(Object.entries(out).sort(([a],[b]) => a.localeCompare(b)));
    },

    renderTodasLasGraficas() {
      this.renderG1Categorias();
      this.renderG2IngresosVsGastos();
      this.renderG3GastosPorMes();
      this.renderG5Distribucion();
      // La gráfica 4 (progreso de metas) se renderiza con HTML/CSS, no necesita Chart.js
    },

    // 1. BARRAS — Gastos por categoría (orden descendente)
    renderG1Categorias() {
      const canvas = document.getElementById('g1_categoriasBarra');
      if (!canvas) return;
      if (this._charts.g1) { this._charts.g1.destroy(); this._charts.g1 = null; }
      const cats = Object.entries(this.estadisticas.gastosPorCategoria)
        .sort(([,a],[,b]) => b - a);
      if (cats.length === 0) return;
      this._charts.g1 = new Chart(canvas, {
        type: 'bar',
        data: {
          labels: cats.map(([k]) => k),
          datasets: [{
            data: cats.map(([,v]) => v),
            backgroundColor: cats.map((_, i) => PALETA[i % PALETA.length]),
            borderRadius: 6
          }]
        },
        options: {
          responsive: true, maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: { callbacks: { label: (c) => ' ' + formatCOP(c.parsed.y) } }
          },
          scales: {
            y: { beginAtZero: true, ticks: { callback: (v) => formatCorto(v) } },
            x: { ticks: { maxRotation: 30, minRotation: 0 } }
          }
        }
      });
    },

    // 2. BARRAS — Ingresos vs Gastos (dos barras grandes)
    renderG2IngresosVsGastos() {
      const canvas = document.getElementById('g2_ingresosVsGastos');
      if (!canvas) return;
      if (this._charts.g2) { this._charts.g2.destroy(); this._charts.g2 = null; }
      this._charts.g2 = new Chart(canvas, {
        type: 'bar',
        data: {
          labels: ['Ingresos', 'Gastos'],
          datasets: [{
            data: [this.estadisticas.totalIngresos, this.estadisticas.totalGastos],
            backgroundColor: ['#10b981', '#ef4444'],
            borderRadius: 8,
            barThickness: 80
          }]
        },
        options: {
          responsive: true, maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: { callbacks: { label: (c) => ' ' + formatCOP(c.parsed.y) } }
          },
          scales: {
            y: { beginAtZero: true, ticks: { callback: (v) => formatCorto(v) } }
          }
        }
      });
    },

    // 3. LÍNEA — Evolución mensual de gastos
    renderG3GastosPorMes() {
      const canvas = document.getElementById('g3_gastosPorMes');
      if (!canvas) return;
      if (this._charts.g3) { this._charts.g3.destroy(); this._charts.g3 = null; }
      const meses = Object.entries(this.estadisticas.gastosPorMes);
      if (meses.length === 0) return;
      this._charts.g3 = new Chart(canvas, {
        type: 'line',
        data: {
          labels: meses.map(([k]) => formatMes(k)),
          datasets: [{
            data: meses.map(([,v]) => v),
            borderColor: '#3b82f6',
            backgroundColor: 'rgba(59, 130, 246, 0.1)',
            tension: 0.3,
            fill: true,
            pointBackgroundColor: '#3b82f6',
            pointRadius: 4,
            pointHoverRadius: 6
          }]
        },
        options: {
          responsive: true, maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: { callbacks: { label: (c) => ' ' + formatCOP(c.parsed.y) } }
          },
          scales: {
            y: { beginAtZero: true, ticks: { callback: (v) => formatCorto(v) } }
          }
        }
      });
    },

    // 5. PIE — Distribución porcentual de gastos
    renderG5Distribucion() {
      const canvas = document.getElementById('g5_distribucionPie');
      if (!canvas) return;
      if (this._charts.g5) { this._charts.g5.destroy(); this._charts.g5 = null; }
      const cats = Object.entries(this.estadisticas.gastosPorCategoria)
        .sort(([,a],[,b]) => b - a);
      if (cats.length === 0) return;
      const total = cats.reduce((s, [,v]) => s + v, 0);
      this._charts.g5 = new Chart(canvas, {
        type: 'pie',
        data: {
          labels: cats.map(([k]) => k),
          datasets: [{
            data: cats.map(([,v]) => v),
            backgroundColor: cats.map((_, i) => PALETA[i % PALETA.length]),
            borderWidth: 2,
            borderColor: '#fff'
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: { position: 'bottom', labels: { padding: 14, boxWidth: 12, font: { size: 12 } } },
            tooltip: {
              callbacks: {
                label: (c) => ` ${c.label}: ${formatCOP(c.parsed)} (${(c.parsed / total * 100).toFixed(1)}%)`
              }
            }
          }
        }
      });
    },

    // ── Ingresos ─────────────────────────────────────────────────────────────────
    async crearIngreso() {
      if (!this.formIngreso.concepto || !this.formIngreso.monto) return;
      await this.api('ingresos', 'POST', this.formIngreso);
      this.formIngreso = { monto: '', concepto: '', fecha: hoy() };
      this.mostrarFormIngreso = false;
      this.ingresos = await this.api('ingresos') ?? [];
      this.mostrar('Ingreso registrado', true);
    },

    async eliminarIngreso(id) {
      if (!confirm('¿Eliminar este ingreso?')) return;
      await this.api('ingresos/' + id, 'DELETE');
      this.ingresos = (await this.api('ingresos')) ?? [];
    },

    // ── Gastos ───────────────────────────────────────────────────────────────────
    async crearGasto() {
      if (!this.formGasto.nombre || !this.formGasto.monto) return;
      await this.api('gastos', 'POST', this.formGasto);
      this.formGasto = { nombre: '', monto: '', categoria: 'Alimentación', fecha: hoy() };
      this.mostrarFormGasto = false;
      this.gastos = await this.api('gastos') ?? [];
      this.mostrar('Gasto registrado', true);
    },

    async eliminarGasto(id) {
      if (!confirm('¿Eliminar este gasto?')) return;
      await this.api('gastos/' + id, 'DELETE');
      this.gastos = (await this.api('gastos')) ?? [];
    },

    // ── Metas ────────────────────────────────────────────────────────────────────
    async crearMeta() {
      if (!this.formMeta.nombre || !this.formMeta.montoObjetivo) return;
      await this.api('metas', 'POST', this.formMeta);
      this.formMeta = { nombre: '', montoObjetivo: '', fechaEsperada: '' };
      this.mostrarFormMeta = false;
      this.metas = await this.api('metas') ?? [];
      this.mostrar('Meta creada', true);
    },

    async eliminarMeta(id) {
      if (!confirm('¿Eliminar esta meta?')) return;
      await this.api('metas/' + id, 'DELETE');
      this.metas = (await this.api('metas')) ?? [];
    },

    // ── Alertas ──────────────────────────────────────────────────────────────────
    async marcarLeida(id) {
      await this.api('alertas/' + id + '/leer', 'PATCH');
      const a = this.alertas.find(x => x.id === id);
      if (a) a.leida = true;
    },

    // ── Sesión ───────────────────────────────────────────────────────────────────
    cerrarSesion() {
      localStorage.removeItem('finanzas_token');
      localStorage.removeItem('finanzas_nombre');
      window.location.href = '/';
    },

    // ── Helpers visuales ─────────────────────────────────────────────────────────
    mostrar(texto, ok) {
      this.notif = { visible: true, texto, ok };
      setTimeout(() => { this.notif.visible = false; }, 3000);
    },

    estadoClase(estado) {
      const mapa = { SUPERAVIT: 'bg-emerald-100 text-emerald-700', DEFICIT: 'bg-red-100 text-red-700', EQUILIBRIO: 'bg-amber-100 text-amber-700' };
      return 'px-3 py-1 rounded-full text-xs font-semibold ' + (mapa[estado] ?? 'bg-slate-100 text-slate-600');
    },

    catClase(cat) {
      return 'inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ring-1 ring-inset ' + (COLOR_CAT[cat] ?? 'bg-slate-100 text-slate-600 ring-slate-200');
    },

    alertaClase(a) {
      return a.leida
        ? 'border border-slate-100 bg-white rounded-xl p-4 opacity-60'
        : 'border border-amber-200 bg-amber-50 rounded-xl p-4';
    }
  };
}

function formatCOP(v) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(v);
}

// Formato corto para los ejes Y de las gráficas: $1.5M, $500K, $0
function formatCorto(v) {
  if (v >= 1_000_000) return '$' + (v / 1_000_000).toFixed(1) + 'M';
  if (v >= 1_000)     return '$' + Math.round(v / 1_000) + 'K';
  return '$' + Math.round(v);
}

// Convierte "yyyy-MM" a etiqueta legible "Ene 2026"
function formatMes(yyyymm) {
  const [y, m] = yyyymm.split('-').map(Number);
  const meses = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
  return meses[m - 1] + ' ' + y;
}
