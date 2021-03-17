
CREATE TABLE `peticiones` (
  `id` varchar(10) primary key,
  `Titulo` varchar(500),
  `Descripcion` varchar(100),
  `Observaciones` varchar(1000),
  `Usuario_creador` varchar(80),
  `Solicitante` varchar(200),
  `Estado` varchar(50),
  `Entidad_origen` varchar(100),
  `Unidad_origen` int(11),
  `Area_origen` varchar(100),
  `Centro_destino` varchar(100),
  `Area_destino` varchar(100),
  `Tipo` varchar(50),
  `Tipo_inicial` varchar(50),
  `Urgente` varchar(25),
  `Prioridad` varchar(25),
  `Fecha_de_alta` date,
  `Fecha_de_tramitacion` date,
  `Fecha_de_necesidad` date,
  `Fecha_fin_de_desarrollo` date,
  `Fecha_de_finalizacion` date,
  `Des_fecha_prevista_inicio` date,
  `Des_fecha_prevista_fin` date,
  `Des_fecha_real_inicio` date,
  `Des_fecha_real_fin` date,
  `Proyecto_ID` varchar(10) NOT NULL,
  `Proyecto_Name` varchar(150),
  `Horas_estimadas_actuales` double,
  `Horas_reales` double,
  `anyo_mes` VARCHAR2(7),
  `fecha_export` DATE,
  `version_analysis` varchar(20) DEFAULT NULL,
  `servicio_atiende_pet` varchar(20) DEFAULT 'SDG',
  `con_entrega` int(1) NOT NULL DEFAULT 0,
  `id_entrega_asociada` varchar(10) DEFAULT NULL,
  `pets_relacionadas` varchar(250) DEFAULT NULL,
  `fecha_estado_modif` TIMESTAMP DEFAULT NULL,
  `tipo_fecha` int(2),
  `fecha_informe` TIMESTAMP DEFAULT NULL, 
  `estado_informe` varchar(100) DEFAULT NULL, 
  `id_area` int(11) DEFAULT NULL, 
  `entorno` int(11) DEFAULT NULL,
  `Horas_estimadas_iniciales` double DEFAULT NULL
);  
CREATE INDEX index_search_gedeones_1 on peticiones (id);
CREATE INDEX index_search_gedeones_2 on peticiones (fecha_estado_modif);

DROP TABLE `agregadosPeticiones`;
CREATE TABLE `agregadosPeticiones` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `tituloEstudio` varchar(250) NULL,
  `entorno` varchar(50) DEFAULT NULL,
  `aplicaciones` varchar(500) DEFAULT NULL,
  `fecha_inicio_estudio` date NOT NULL,
  `fecha_fin_estudio` date NOT NULL,
  `num_peticiones` int(6) DEFAULT NULL,
  `num_meses` int(4) DEFAULT NULL,
  `total_uts` double DEFAULT NULL,
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
  `servicio` varchar(150) DEFAULT NULL  
);  
CREATE INDEX index_Estudios_sr ON agregadosPeticiones (servicio);
CREATE INDEX index_Estudios_ti ON agregadosPeticiones (tituloEstudio, fecha_inicio_estudio);

DROP TABLE `resumenPeticiones`;
CREATE TABLE `resumenPeticiones` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `id_estudio` int(11) NOT NULL,
  `aplicacion` varchar(150) DEFAULT NULL,
  `tipo` varchar(50) DEFAULT NULL,
  `gedeon_DG` varchar(10) DEFAULT NULL,
  `gedeon_AT` varchar(10) DEFAULT NULL,
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
  `titulo` varchar(500)
);
CREATE INDEX index_search_resumenpetic_1 on resumenPeticiones (id_estudio);









CREATE TABLE `categoria_profesional` (
  `ID_CATEGORIA` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `TIPO_CATEGORIA` varchar(8) NOT NULL,
  `DESCRIPCION` varchar(50) NOT NULL,
  `IMPORTE_HORA` double NOT NULL
);
CREATE TABLE sqlite_sequence(name,seq);
CREATE TABLE `concurso` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `codigo` varchar(100) NOT NULL,
  `contrato` varchar(100) DEFAULT NULL,
  `fecha_inicio_vigencia` date NOT NULL,
  `fecha_fin_vigencia` date NOT NULL,
  `Observaciones` varchar(250) DEFAULT NULL,
  `importe_total_sin_IVA` double NOT NULL DEFAULT '0',
  `importe_total_con_IVA` double NOT NULL DEFAULT '0',
  `increm_decremento_por_cambio_IVA` double NOT NULL DEFAULT '0',
  `recursos_por_categoria_C` int(3) NOT NULL DEFAULT '0',
  `recursos_por_categoria_CJ` int(3) NOT NULL DEFAULT '0',
  `recursos_por_categoria_AF` int(3) NOT NULL DEFAULT '0',
  `recursos_por_categoria_AP` int(3) NOT NULL DEFAULT '0',
  `recursos_totales` int(3) NOT NULL DEFAULT '0',
  `importe_por_categoria_C_sin_IVA` double NOT NULL DEFAULT '0',
  `importe_por_categoria_CJ_sin_IVA` double NOT NULL DEFAULT '0',
  `importe_por_categoria_AF_sin_IVA` double NOT NULL DEFAULT '0',
  `importe_por_categoria_AP_sin_IVA` double NOT NULL DEFAULT '0',
  `horas_totales_por_categoria_C` double NOT NULL DEFAULT '0',
  `horas_totales_por_categoria_CJ` double NOT NULL DEFAULT '0',
  `horas_totales_por_categoria_AF` double NOT NULL DEFAULT '0',
  `horas_totales_por_categoria_AP` double NOT NULL DEFAULT '0',
  `horas_totales` double NOT NULL DEFAULT '0'
);
CREATE TABLE `edificio` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `localizacion` varchar(100) NOT NULL
);
CREATE TABLE `ejercicio` (
  `ejercicio` int(4) NOT NULL,
  PRIMARY KEY (`ejercicio`)
);
CREATE TABLE `empresaute` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `nombre` varchar(50) NOT NULL,
  `porcentaje` double DEFAULT NULL,
  `responsable` varchar(150) DEFAULT NULL
);
CREATE TABLE `iva` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `fecha_vigencia_desde` date NOT NULL,
  `fecha_vigencia_hasta` date NOT NULL,
  `porcentaje` double NOT NULL
);
CREATE TABLE `mes` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `numero` int(2) NOT NULL,
  `nombre` varchar(50) NOT NULL
);
CREATE TABLE INVERTIA_IMPORT
(
  IDENTIFICADOR         INTEGER           NOT NULL PRIMARY KEY   AUTOINCREMENT,
  GRUPO                 VARCHAR2(25)        NOT NULL,
  NUM_ENTRADAS          NUMBER(10)        NOT NULL,
  FEC_IMPORTACION       DATENOT NULL,
  FILENAME              VARCHAR2(50)        NOT NULL,
  EXCEL_FILE            BLOB NOT NULL
);
CREATE TABLE INVERTIA_DATA
   (
     IDENTIFICADOR         INTEGER              NOT NULL PRIMARY KEY   AUTOINCREMENT,
     GRUPO                 VARCHAR2(25)        NOT NULL,
     FECHA           DATE NOT NULL,
     LAST_PUNTAJE          NUMBER(8,2)        NOT NULL,
     INICIAL_PUNTAJE       NUMBER(8,2)        NOT NULL,
     PORCENTAJE_DIF        NUMBER(6,2)NOT NULL,
     MAX_PUNTAJE          NUMBER(8,2)        NOT NULL,
     MIN_PUNTAJE       NUMBER(8,2)        NOT NULL,
     VOLUMEN       NUMBER(14)        NOT NULL,
     FEC_IMPORTACION       DATE NOT NULL,
     ANYO_MES       VARCHAR2(7)
);
CREATE TABLE `departamento` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `nombre` varchar(50) NOT NULL,
  `servicio` int(11) NOT NULL
);
CREATE TABLE `rol` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `nombre` varchar(25) NOT NULL
  );
CREATE TABLE `administrador` (
  `id` integer primary key autoincrement,
  `login_name` varchar(10) NOT NULL,
  `password` varchar(10) NOT NULL,
  `profile` int(11) NOT NULL,
  `nombreCompleto` varchar(100));
CREATE TABLE `responsabilidad` (
  `ID` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `TIPO` varchar(50) NOT NULL
);
CREATE TABLE `appColaborador` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `colaborador` int(11) NOT NULL,
  `proyecto` int(11) NOT NULL
);
CREATE TABLE `responsablecentro` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `nombre` varchar(32) NOT NULL,
  `apellidos` varchar(64) NOT NULL,
  `servicio` int(11) NOT NULL,
  `cargo` varchar(100) DEFAULT NULL,
  `id_ubicacion` int(11) DEFAULT NULL,
  `observaciones` varchar(150) DEFAULT NULL
);
CREATE TABLE `unidadOrg` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `nombre` varchar(100) NOT NULL
  );
CREATE TABLE `facturacionMesPorConcurso` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `anyo` INTEGER,
  `mes` int(11) NOT NULL,
  `id_concurso` int(11) NOT NULL,
  `presupuesto` double NOT NULL DEFAULT '0',
  `ejecutado` double NOT NULL DEFAULT '0',
  `porcentaje` double NOT NULL DEFAULT '0',
  `desviacion` double NOT NULL DEFAULT '0',
  `UTs` double NOT NULL DEFAULT '0'
);
CREATE TABLE `facturacionMesPorServicio` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `anyo` INTEGER,
  `mes` int(11) NOT NULL,
  `id_concurso` int(11) NOT NULL,
  `id_servicio` int(11) NOT NULL,
  `ejecutado` double NOT NULL DEFAULT '0',
  `porcentaje_concurso` double NOT NULL DEFAULT '0',
  `UTs` double NOT NULL DEFAULT '0',
  `idFactMesConcurso` int(11) NOT NULL
);
CREATE TABLE `facturacionMesPorDpto` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `anyo` INTEGER,
  `mes` int(11) NOT NULL,
  `id_concurso` int(11) NOT NULL,
  `id_servicio` int(11) NOT NULL,
  `id_dpto` int(11) NOT NULL,
  `ejecutado` double NOT NULL DEFAULT '0',
  `porcentaje_servicio` double NOT NULL DEFAULT '0',
  `UTs` double NOT NULL DEFAULT '0',
  `idFactMesServicio` int(11) NOT NULL
);
CREATE TABLE `facturacionMesPorApp` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `anyo` INTEGER,
  `mes` int(11) NOT NULL,
  `id_concurso` int(11) NOT NULL,
  `id_servicio` int(11) NOT NULL,
  `id_dpto` int(11) NOT NULL,
  `id_app` int(11) NOT NULL,
  `ejecutado` double NOT NULL DEFAULT '0',
  `porcentaje_dpto` double NOT NULL DEFAULT '0',
  `UTs` double NOT NULL DEFAULT '0',
  `idFactMesDpto` int(11) NOT NULL
);
CREATE TABLE `facturacionMesPorColaborador` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `anyo` INTEGER,
  `mes` int(11) NOT NULL,
  `id_colaborador` int(11) NOT NULL,
  `ejecutado` double NOT NULL DEFAULT '0',
  `porcentaje_concurso` double NOT NULL DEFAULT '0',
  `porcentaje_servicio` double NOT NULL DEFAULT '0',
  `porcentaje_dpto` double NOT NULL DEFAULT '0',
  `porcentaje_app` double NOT NULL DEFAULT '0',
  `UTs` double NOT NULL DEFAULT '0'
);
CREATE TABLE `resultado_prevision_mes` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `id_prevision_anualidad` int(11) NOT NULL,
  `mes` int(2) NOT NULL,
  `mes_de_verano` int(1) DEFAULT '0',
  `num_horas_mes_consultor` double DEFAULT '0',
  `num_horas_mes_cJunior` double DEFAULT '0',
  `num_horas_mes_anFuncional` double DEFAULT '0',
  `num_horas_mes_anProgramador` double DEFAULT '0',
  `num_horas_mes_total` double DEFAULT '0',
  `saldo_horas_mes_consultor` double DEFAULT '0',
  `saldo_horas_mes_cJunior` double DEFAULT '0',
  `saldo_horas_mes_anFuncional` double DEFAULT '0',
  `saldo_horas_mes_anProgramador` double DEFAULT '0',
  `saldo_total_horas_mes` double DEFAULT '0',
  `importe_mes_consultor` double DEFAULT '0',
  `importe_mes_cJunior` double DEFAULT '0',
  `importe_mes_anFuncional` double DEFAULT '0',
  `importe_mes_anProgramador` double DEFAULT '0',
  `importe_total_mes` double DEFAULT '0',
  `saldo_importe_mes_consultor` double DEFAULT '0',
  `saldo_importe_mes_cJunior` double DEFAULT '0',
  `saldo_importe_mes_anFuncional` double DEFAULT '0',
  `saldo_importe_mes_anProgramador` double DEFAULT '0',
  `saldo_total_importe_mes` double DEFAULT '0',
  `horas_ejecutadas_en_mes_consultor` double NOT NULL,
  `horas_ejecutadas_en_mes_cJunior` double NOT NULL,
  `horas_ejecutadas_en_mes_anFuncional` double NOT NULL,
  `horas_ejecutadas_en_mes_anProgramador` double NOT NULL,
  `horas_ejecutadas_total_en_mes` double NOT NULL,
  `importe_ejecutado_en_mes_consultor` double NOT NULL,
  `importe_ejecutado_en_mes_cJunior` double NOT NULL,
  `importe_ejecutado_en_mes_anFuncional` double NOT NULL,
  `importe_ejecutado_en_mes_anProgramador` double NOT NULL,
  `importe_ejecutado_total_en_mes` double NOT NULL,
  `nombre_mes` varchar(30) DEFAULT NULL
);
CREATE TABLE `colaborador` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `nombre` varchar(32) NOT NULL DEFAULT '',
  `apellidos` varchar(64) NOT NULL,
  `responsabilidad` int(11) NOT NULL,
  `relacion_extinguida` int(1) NOT NULL,
  `id_categoria` int(11) NOT NULL,
  `id_empresa_facturacion` int(11) NOT NULL,
  `fecha_alta` date DEFAULT NULL,
  `fecha_baja` date DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `observaciones` varchar(250) DEFAULT NULL,
  `id_concurso` int(11) NOT NULL
);
CREATE TABLE `facturacionMesPorColaboradoryApp` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `idFacturacionColab` int(11) NOT NULL,
  `id_colaborador` int(11) NOT NULL,
  `id_proyecto` int(11) NOT NULL,
  `UTs` double NOT NULL DEFAULT '0',
  `idFacturacionMesApp` int(11) NOT NULL,
  `ejecutado` double NOT NULL DEFAULT '0',
  `mesanyo` varchar(50) NOT NULL DEFAULT ''
);
CREATE TABLE `importacionesFacturacion` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `id_contrato` int(11) NOT NULL,
  `mes` int(11) NOT NULL,
  `anyo` int(4) NOT NULL,
  `filename` varchar(100) NOT NULL,
  `numEntradas` int(4),
  `fechaImportacion` date,
  `excelFile` BLOB NOT NULL
);
CREATE TABLE `resultado_prevision_anualidad` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `id_prevision_contrato` int(11) NOT NULL,
  `ejercicio` int(4) NOT NULL,
  `num_meses` int(2) NOT NULL,
  `horas_total_en_ejercicio_consultor` double DEFAULT '0',
  `horas_total_en_ejercicio_cJunior` double DEFAULT '0',
  `horas_total_en_ejercicio_anFuncional` double DEFAULT '0',
  `horas_total_en_ejercicio_anProgramador` double DEFAULT '0',
  `horas_total_en_ejercicio` double DEFAULT '0',
  `saldo_horas_en_ejercicio_consultor` double DEFAULT '0',
  `saldo_horas_en_ejercicio_cJunior` double DEFAULT '0',
  `saldo_horas_en_ejercicio_anFuncional` double DEFAULT '0',
  `saldo_horas_en_ejercicio_anProgramador` double DEFAULT '0',
  `saldo_horas_total_en_ejercicio` double DEFAULT '0',
  `importe_en_ejercicio_consultor` double DEFAULT '0',
  `importe_en_ejercicio_cJunior` double DEFAULT '0',
  `importe_en_ejercicio_anFuncional` double DEFAULT '0',
  `importe_en_ejercicio_anProgramador` double DEFAULT '0',
  `importe_total_en_ejercicio` double DEFAULT '0',
  `saldo_importe_en_ejercicio_consultor` double DEFAULT '0',
  `saldo_importe_en_ejercicio_cJunior` double DEFAULT '0',
  `saldo_importe_en_ejercicio_anFuncional` double DEFAULT '0',
  `saldo_importe_en_ejercicio_anProgramador` double DEFAULT '0',
  `saldo_importe_total_en_ejercicio_` double DEFAULT '0',
  `horas_ejecutadas_en_ejercicio_consultor` double DEFAULT '0',
  `horas_ejecutadas_en_ejercicio_cJunior` double DEFAULT '0',
  `horas_ejecutadas_en_ejercicio_anFuncional` double DEFAULT '0',
  `horas_ejecutadas_en_ejercicio_anProgramador` double DEFAULT '0',
  `horas_ejecutadas_total_en_ejercicio` double DEFAULT '0',
  `importe_ejecutado_en_ejercicio_consultor` double DEFAULT '0',
  `importe_ejecutado_en_ejercicio_cJunior` double DEFAULT '0',
  `importe_ejecutado_en_ejercicio_anFuncional` double DEFAULT '0',
  `importe_ejecutado_en_ejercicio_anProgramador` double DEFAULT '0',
  `importe_ejecutado_total_en_ejercicio` double DEFAULT '0',
  `importe_concurso_en_ejercicio` double DEFAULT '0',
  `saldo_pendiente_gastar_segun_prevision` double DEFAULT '0'
);
CREATE TABLE `importacionesGEDEON` (
  `rochade` varchar(10) primary key,
  `filename` varchar(100),
  `numEntradas` integer,
  `fechaImportacion` date,
  `excelFile` BLOB NOT NULL,
  `servicioDestino` varchar(20) NOT NULL
);
CREATE TABLE `proyecto` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `codigo` varchar(15) NOT NULL,
  `nombreProyecto` varchar(50) NOT NULL,
  `responsable` int(11) DEFAULT NULL,
  `id_concurso` int(11) NOT NULL,
  `responsableCentro` int(11) DEFAULT NULL,
  `departamento` int(11) NOT NULL,
  `RET_en_DG` int(11) DEFAULT NULL,
  `Plan_FILE` BLOB DEFAULT NULL,
  `FILE_1` BLOB DEFAULT NULL,
  `FILE_2` BLOB DEFAULT NULL,
  `FILE_3` BLOB DEFAULT NULL,
  `observaciones` varchar(10000) NOT NULL
, tipoApp int(11) DEFAULT NULL, fecha_update TIMESTAMP default SYSDATE, id_subdireccion int(11) NULL, id_area int(11) DEFAULT NULL);
CREATE TABLE `tipoAplicacion` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `descripcion` varchar(60) NOT NULL
);
CREATE TABLE `datos_prevision_contrato` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `id_concurso` int(11) NOT NULL,
  `num_ejercicios` int(2) NOT NULL,
  `num_meses` int(2) NOT NULL,
  `num_recursos_por_defecto_consultor` int(3) NOT NULL,
  `num_recursos_por_defecto_cJunior` int(3) NOT NULL,
  `num_recursos_por_defecto_anFuncional` int(3) NOT NULL,
  `num_recursos_por_defecto_anProgramador` int(3) NOT NULL,
  `num_jornadas_por_defecto_mes_estandard` int(4) DEFAULT '0',
  `num_jornadas_por_defecto_mes_verano` int(4) DEFAULT '0',
  `jornada_verano` int(1) NOT NULL,
  `horas_por_defecto_dia_estandard` int(4) DEFAULT '0',
  `horas_por_defecto_dia_verano` int(4) DEFAULT '0',
  `num_recursos_con_C_jornada_reducida` int(3) DEFAULT '0',
  `num_recursos_con_CJ_jornada_reducida` int(3) DEFAULT '0',
  `num_recursos_con_AF_jornada_reducida` int(3) DEFAULT '0',
  `num_recursos_con_AP_jornada_reducida` int(3) DEFAULT '0',
  `horas_por_defecto_jornada_reducida` double DEFAULT '0',
  `jornadas_enero` int(3) DEFAULT '0',
  `jornadas_febrero` int(3) DEFAULT '0',
  `jornadas_marzo` int(3) DEFAULT '0',
  `jornadas_abril` int(3) DEFAULT '0',
  `jornadas_mayo` int(3) DEFAULT '0',
  `jornadas_junio` int(3) DEFAULT '0',
  `jornadas_julio` int(3) DEFAULT '0',
  `jornadas_agosto` int(3) DEFAULT '0',
  `jornadas_septiembre` int(3) DEFAULT '0',
  `jornadas_octubre` int(3) DEFAULT '0',
  `jornadas_noviembre` int(3) DEFAULT '0',
  `jornadas_diciembre` int(3) DEFAULT '0',
  `total_prevision` double DEFAULT '0',
  `total_facturado` double DEFAULT '0',
  `disponible` double DEFAULT '0',
  `fecha` date NOT NULL,
  `texto` varchar(250) DEFAULT NULL
);
CREATE TABLE `subdireccion` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `unidadOrg` int(11) NOT NULL,
  `nombre` varchar(150) NOT NULL
  );
CREATE TABLE `servicio` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `nombre` varchar(50) NOT NULL,
  `unidadOrg` int(11) NOT NULL,
  `subdireccion` int(11) NULL
);
CREATE TABLE `sabana` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `Titulo` varchar(500),
  `Fecha_Necesidad` TIMESTAMP DEFAULT NULL,
  `Entrada_en_CDISM` TIMESTAMP DEFAULT NULL,
  `Estado_Peticion` varchar(25) DEFAULT NULL,
  `Prevision_Fin_Estado` TIMESTAMP DEFAULT NULL,
  `Fecha_Prev_Implantacion` TIMESTAMP DEFAULT NULL,
  `Fecha_Real_Implantacion` TIMESTAMP DEFAULT NULL,
  `Observaciones` varchar(500) DEFAULT NULL,
  `Aplicacion` int(11) NOT NULL,
  `Origen` varchar(50) DEFAULT NULL,
  `ID_Origen` varchar(50) DEFAULT NULL,
  `Fecha_Prev_Ini_Analisis` TIMESTAMP DEFAULT NULL,
  `Fecha_Real_Ini_Analisis` TIMESTAMP DEFAULT NULL,
  `Fecha_Prev_Fin_Analisis` TIMESTAMP DEFAULT NULL,
  `Fecha_Real_Fin_Analisis` TIMESTAMP DEFAULT NULL,
  `Prev_Ini_Pruebas_CD` TIMESTAMP DEFAULT NULL,
  `Real_Ini_Pruebas_CD` TIMESTAMP DEFAULT NULL,
  `Peticion_AES` varchar(15) DEFAULT NULL,
  `Prev_Fin_Pruebas_CD` TIMESTAMP DEFAULT NULL,
  `Real_Fin_Pruebas_CD` TIMESTAMP DEFAULT NULL,
  `Peticion_DG` varchar(15) DEFAULT NULL,
  `Fecha_Prev_Fin_DG` TIMESTAMP DEFAULT NULL,
  `Fecha_Real_Fin_DG` TIMESTAMP DEFAULT NULL,
  `UTS_Estimadas` double DEFAULT NULL,
  `Peticion_Entrega` varchar(15) DEFAULT NULL,
  `Fec_Entrega` TIMESTAMP DEFAULT NULL,
  `Estado_peticion_Entrega` varchar(25) DEFAULT NULL,
  `Subdireccion` int(11) NOT NULL
 );

  
CREATE INDEX index_cotizaciones ON invertia_data (GRUPO);
CREATE INDEX index_cotizaciones2 ON invertia_data (GRUPO, FECHA);
CREATE INDEX index_prev_anual ON resultado_prevision_anualidad(id_prevision_contrato);
CREATE INDEX index_prev_mes ON resultado_prevision_mes(id_prevision_anualidad);
CREATE INDEX index_FraCon ON facturacionMesPorConcurso (id_concurso);
CREATE INDEX index_FraServ ON facturacionMesPorServicio (id_servicio);
CREATE INDEX index_FraDpto ON facturacionMesPorDpto (id_dpto);
CREATE INDEX index_FraApp ON facturacionMesPorApp (id_app);
CREATE INDEX index_FraColab ON facturacionMesPorColaborador (id_colaborador);

  