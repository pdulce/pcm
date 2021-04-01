
CREATE TABLE `administrador` (
  `id` integer primary key autoincrement,
  `login_name` varchar(20) NOT NULL,
  `password` varchar(20) NOT NULL,
  `profile` int(11) NOT NULL,
  `nombreCompleto` varchar(100)
);

CREATE TABLE `ejercicio` (
  `ejercicio` int(4) NOT NULL,
  PRIMARY KEY (`ejercicio`)
);

CREATE TABLE `mes` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `numero` int(2) NOT NULL,
  `nombre` varchar(50) NOT NULL
);

CREATE TABLE `rol` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `nombre` varchar(25) NOT NULL
);

DROP TABLE `unidadOrg`;  
CREATE TABLE `unidadOrg` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `nombre` varchar(100) NOT NULL
);

DROP TABLE `subdireccion`;
CREATE TABLE `subdireccion` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `unidadOrg` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL
);

DROP TABLE `servicio`;
CREATE TABLE `servicio` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `nombre` varchar(50) NOT NULL,
  `unidadOrg` int(11) NOT NULL,
  `subdireccion` int(11) NULL
);

DROP TABLE `tiposPeriodos`;
CREATE TABLE `tiposPeriodos` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `numMeses` int(4) NOT NULL,
  `periodo` varchar(50) DEFAULT NULL
);

DROP TABLE `configuradorEstudios`;
CREATE TABLE `configuradorEstudios` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `configname` varchar(100) NULL,
  `formula_jornadas_analisis` varchar(600) DEFAULT NULL,
  `formula_jornadas_desarrollo` varchar(600) DEFAULT NULL,
  `formula_jornadas_preparacionEntrega` varchar(600) DEFAULT NULL,
  `formula_jornadas_pruebasCD` varchar(600) DEFAULT NULL,
  `formula_jornadas_intervaloPlanifDG` varchar(600) DEFAULT NULL,
  `formula_jornadas_intervaloFinDG_SolicitudEntregaAT` varchar(600) DEFAULT NULL,
  `formula_jornadas_intervaloFinPruebasCD_InstalacProduc` varchar(600) DEFAULT NULL,
  `formula_fecha_ini_analisis` varchar(150) DEFAULT NULL,
  `formula_fecha_fin_analisis` varchar(150) DEFAULT NULL,
  `formula_fecha_ini_pruebas_CD` varchar(150) DEFAULT NULL,
  `formula_fecha_fin_pruebas_CD` varchar(150) DEFAULT NULL,
  `mlr_jornadas_analisis` varchar(600) DEFAULT NULL,
  `alias_peticion_a_DG` varchar(50) DEFAULT NULL,
  `alias_peticion_AT` varchar(50) DEFAULT NULL,
  `alias_peticion_entrega` varchar(50) DEFAULT NULL,
  `alias_peticion_pruebas` varchar(50) DEFAULT NULL,
  `alias_tarea_analisis` varchar(50) DEFAULT NULL,
  `alias_tarea_pruebas` varchar(50) DEFAULT NULL
);

DROP TABLE `estudios`;
CREATE TABLE `estudiosPeticiones` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `tituloEstudio` varchar(250) NULL,
  `id_entorno` int(11) DEFAULT NULL,
  `aplicaciones` varchar(500) DEFAULT NULL,
  `fecha_inicio_estudio` date NOT NULL,
  `fecha_fin_estudio` date NOT NULL,
  `num_peticiones` int(6) DEFAULT NULL,
  `num_meses` int(4) DEFAULT NULL,
  `ciclo_vida` double DEFAULT NULL,
  `duracion_analysis` double DEFAULT NULL,
  `duracion_desarrollo` double DEFAULT NULL,
  `duracion_entregas` double DEFAULT NULL,
  `duracion_pruebas` double DEFAULT NULL,
  `gap_tram_iniRealDesa` double DEFAULT NULL,
  `gap_finDesa_solicEntrega` double DEFAULT NULL,
  `gap_finPrue_Producc` double DEFAULT NULL,
  `total_dedicaciones` double DEFAULT NULL,
  `total_gaps` double DEFAULT NULL,
  `ciclo_vida_permonth` double DEFAULT NULL,
  `duracion_analysis_permonth` double DEFAULT NULL,
  `duracion_desarrollo_permonth` double DEFAULT NULL,
  `duracion_entregas_permonth` double DEFAULT NULL,
  `duracion_pruebas_permonth` double DEFAULT NULL,
  `gap_tram_iniRealDesa_permonth` double DEFAULT NULL,
  `gap_finDesa_solicEntrega_permonth` double DEFAULT NULL,
  `gap_finPrue_Producc_permonth` double DEFAULT NULL,
  `total_dedicaciones_permonth` double DEFAULT NULL,
  `total_gaps_permonth` double DEFAULT NULL,
  `ciclo_vida_perpet` double DEFAULT NULL,
  `duracion_analysis_perpet` double DEFAULT NULL,
  `duracion_desarrollo_perpet` double DEFAULT NULL,
  `duracion_entregas_perpet` double DEFAULT NULL,
  `duracion_pruebas_perpet` double DEFAULT NULL,
  `gap_tram_iniRealDesa_perpet` double DEFAULT NULL,
  `gap_finDesa_solicEntrega_perpet` double DEFAULT NULL,
  `gap_finPrue_Producc_perpet` double DEFAULT NULL,
  `total_dedicaciones_perpet` double DEFAULT NULL,
  `total_gaps_perpet` double DEFAULT NULL,
  `porc_duracion_analysis` double DEFAULT NULL,
  `porc_duracion_desarrollo` double DEFAULT NULL,
  `porc_duracion_entrega` double DEFAULT NULL,
  `porc_pruebas` double DEFAULT NULL,
  `porc_gap_tram_iniRealDesa` double DEFAULT NULL,
  `porc_gap_finDesa_solicEntrega` double DEFAULT NULL,
  `porc_gap_finPrue_Producc` double DEFAULT NULL,
  `porc_total_dedicaciones` double DEFAULT NULL,
  `porc_total_gaps` double DEFAULT NULL,
  `id_servicio` int(11) DEFAULT NULL,
  `tipo_periodo` int(11) DEFAULT NULL,
  `total_hrs_analisis` double DEFAULT NULL,
  `hrs_analisis_permonth` double DEFAULT NULL,
  `hrs_analisis_perpet` double DEFAULT NULL,
  `total_uts` double DEFAULT NULL,
  `uts_permonth` double DEFAULT NULL,
  `uts_perpet` double DEFAULT NULL,
  `id_aplicativo` int(11) DEFAULT NULL,
  `ciclo_vida_perappmonth` double DEFAULT NULL,
  `duracion_analysis_perappmonth` double DEFAULT NULL,
  `duracion_desarrollo_perappmonth` double DEFAULT NULL,
  `duracion_entregas_perappmonth` double DEFAULT NULL,
  `duracion_pruebas_perappmonth` double DEFAULT NULL,
  `gap_tram_iniRealDesa_perappmonth` double DEFAULT NULL,
  `gap_finDesa_solicEntrega_perappmonth` double DEFAULT NULL,
  `gap_finPrue_Producc_perappmonth` double DEFAULT NULL,
  `total_dedicaciones_perappmonth` double DEFAULT NULL,
  `total_gaps_perappmonth` double DEFAULT NULL,
  `hrs_analisis_perappmonth` double DEFAULT NULL,
  `uts_perappmonth` double DEFAULT NULL,
  `ciclo_vida_perapp` double DEFAULT NULL,
  `duracion_analysis_perapp` double DEFAULT NULL,
  `duracion_desarrollo_perapp` double DEFAULT NULL,
  `duracion_entregas_perapp` double DEFAULT NULL,
  `duracion_pruebas_perapp` double DEFAULT NULL,
  `gap_tram_iniRealDesa_perapp` double DEFAULT NULL,
  `gap_finDesa_solicEntrega_perapp` double DEFAULT NULL,
  `gap_finPrue_Producc_perapp` double DEFAULT NULL,
  `total_dedicaciones_perapp` double DEFAULT NULL,
  `total_gaps_perapp` double DEFAULT NULL,
  `hrs_analisis_perapp` double DEFAULT NULL,
  `uts_perapp` double DEFAULT NULL,
  `esfuerzo_pruebas_estudio` double DEFAULT NULL,
  `esfuerzo_pruebas_permonth` double DEFAULT NULL,
  `esfuerzo_pruebas_perappmonth` double DEFAULT NULL,
  `esfuerzo_pruebas_perapp` double DEFAULT NULL,
  `esfuerzo_pruebas_perpet` double DEFAULT NULL,
  `tipo_peticiones` int(11) DEFAULT NULL,
  `desnormalizadasTipoPet` varchar(500) DEFAULT NULL,
  `id_configuradorEstudios` int(11) DEFAULT NULL,
  `fec_lanzado_estudio` TIMESTAMP NULL
);

DROP TABLE `resumenesEstudio`;
CREATE TABLE `resumenesEstudio` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `id_estudio` int(11) NOT NULL,
  `aplicacion` varchar(150) DEFAULT NULL,
  `tipo` varchar(50) DEFAULT NULL,
  `gedeon_DG` varchar(150) DEFAULT NULL,
  `gedeon_AT` varchar(150) DEFAULT NULL,
  `gedeon_Entrega` varchar(10) DEFAULT NULL,
  `ciclo_vida` double DEFAULT NULL,
  `duracion_analysis` double DEFAULT NULL,
  `duracion_desarrollo` double DEFAULT NULL,
  `duracion_entrega_DG` double DEFAULT NULL,
  `duracion_pruebas` double DEFAULT NULL,
  `gap_tram_iniRealDesa` double DEFAULT NULL,
  `gap_finDesa_solicitudEntrega` double DEFAULT NULL,
  `gap_finPrue_Producc` double DEFAULT NULL,
  `total_dedicaciones` double DEFAULT NULL,
  `total_intervalos_sin_dedicacion` double DEFAULT NULL,
  `fecha_inicio_analisis` date DEFAULT NULL,
  `fecha_fin_analisis` date DEFAULT NULL,
  `fecha_tramite_a_DG` date DEFAULT NULL,
  `fecha_inicio_desarrollo` date DEFAULT NULL,
  `fecha_fin_desarrollo` date DEFAULT NULL,
  `fecha_solicitud_entrega` date DEFAULT NULL,
  `fecha_inicio_pruebasCD` date DEFAULT NULL,
  `fecha_fin_pruebasCD` date DEFAULT NULL,
  `fecha_inicio_instalacion_Prod` date DEFAULT NULL,
  `fecha_fin_instalacion_Prod` date DEFAULT NULL,
  `uts` double DEFAULT NULL,
  `esfuerzo_analysis` double DEFAULT NULL,
  `esfuerzo_pruebas` double DEFAULT NULL,
  `titulo` varchar(500) DEFAULT NULL
);
