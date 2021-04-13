DROP TABLE `peticiones`;
CREATE TABLE `peticiones` (
  `id` INTEGER PRIMARY KEY,
  `id_aplicativo` int(11) NOT NULL,
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
  `Tipo` int(11) DEFAULT NULL,
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
  `Horas_estimadas_actuales` double,
  `Horas_reales` double,
  `fecha_export` DATE NULL,
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
  `Horas_estimadas_iniciales` double DEFAULT NULL,
  `fecha_validada_CD` DATE NULL,
  `ult_modificacion` DATE NULL,
  `volatile_tipo` varchar(50) DEFAULT NULL
);  

DROP TABLE `tareasPeticion`;
CREATE TABLE `tareasPeticion` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `id_tareaGEDEON` varchar(20) DEFAULT NULL,
  `id_peticion` int(11) DEFAULT NULL,
  `id_tipotarea` int(11) DEFAULT NULL,
  `nombre` varchar(200) DEFAULT NULL,
  `horas_imputadas` double DEFAULT NULL,
  `horas_previstas` double DEFAULT NULL,
  `fecha_inicio_previsto` date DEFAULT NULL,
  `fecha_fin_previsto` date DEFAULT NULL,
  `fecha_inicio_real` date DEFAULT NULL,  
  `fecha_fin_real` date DEFAULT NULL,
  `Fecha_de_alta` dateDEFAULT NULL,
  `Fecha_de_tramitacion` date DEFAULT NULL,
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
CREATE TABLE `estudios` (
  `id` INTEGER PRIMARY KEY   AUTOINCREMENT,
  `titulo` varchar(250) NULL,
  `id_aplicativo` int(11) DEFAULT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  `num_meses` int(4) DEFAULT NULL,  
  `id_periodo` int(11) DEFAULT NULL,
  `volatile_tipopeticiones` int(11) DEFAULT NULL,
  `id_configuradorEstudios` int(11) DEFAULT NULL,
  `fec_lanzado_estudio` TIMESTAMP NULL
 );

DROP TABLE `resumenPeticiones`;
CREATE TABLE `resumenPeticiones` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `id_estudio` int(11) NOT NULL,
  `id_aplicativo` int(11) NOT NULL,
  `tipo` int(11) DEFAULT NULL,
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

DROP TABLE `resumenEntregas`;
CREATE TABLE `resumenEntregas` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `id_estudio` int(11) NOT NULL,  
  `id_aplicacion` int(11) NOT NULL,
  `id_gedeon_Entrega` int(11) NOT NULL,
  `num_peticiones` int(6) DEFAULT NULL,
  `volumen_uts` double DEFAULT NULL,
  `tipo` int(11) DEFAULT NULL,
  `num_rechazos` int(2) DEFAULT NULL,
  `ciclo_vida_entrega` double DEFAULT NULL,
  `fecha_solicitud_entrega` date DEFAULT NULL,
  `fecha_inicio_pruebasCD` date DEFAULT NULL,
  `fecha_fin_pruebasCD` date DEFAULT NULL,
  `fecha_inicio_instalacion_Prod` date DEFAULT NULL,
  `fecha_fin_instalacion_Prod` date DEFAULT NULL,  
  `tiempo_prepacion_en_DG` double DEFAULT NULL,
  `tiempo_validacion_en_CD` double DEFAULT NULL,
  `tiempo_desdeValidacion_hastaImplantacion` double DEFAULT NULL  
);  

 

CREATE INDEX index_search_tareasPet_1 on tareasPeticion (id_peticion);
CREATE INDEX index_search_resumenpetic_1 on resumenPeticiones (id_estudio);
CREATE INDEX index_Estudios_sr ON estudios (id_aplicativo);
CREATE INDEX index_Estudios_ti ON estudios (fecha_inicio);
CREATE INDEX index_search_gedeones_1 on peticiones (id);
CREATE INDEX index_search_gedeones_2 on peticiones (fecha_estado_modif);


CREATE TABLE `tiposPeticiones` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `nombre` varchar(30) NOT NULL
);


DROP TABLE `tiposTareas`;
CREATE TABLE `tiposTareas` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `nombre` varchar(100) NOT NULL
);

DROP TABLE `tiposPeriodos`;
CREATE TABLE `tiposPeriodos` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `numMeses` int(4) NOT NULL,
  `periodo` varchar(50) DEFAULT NULL
);

insert into tiposPeriodos (numMeses, periodo) values (1, 'mensual');
insert into tiposPeriodos (numMeses, periodo) values (2, 'bimensual');
insert into tiposPeriodos (numMeses, periodo) values (3, 'trimestre');
insert into tiposPeriodos (numMeses, periodo) values (4, 'cuatrimestre');
insert into tiposPeriodos (numMeses, periodo) values (6, 'semestre');
insert into tiposPeriodos (numMeses, periodo) values (12, 'anual');
insert into tiposPeriodos (numMeses, periodo) values (24, 'bienio');
insert into tiposPeriodos (numMeses, periodo) values (36, 'trienio');
insert into tiposPeriodos (numMeses, periodo) values (48, 'cuatrienio');
insert into tiposPeriodos (numMeses, periodo) values (99, 'indeterminado');

drop table tecnologia;
CREATE TABLE `tecnologia` (
  `id`  INTEGER PRIMARY KEY   AUTOINCREMENT,
  `nombre` varchar(100) DEFAULT NULL,
  `descripcion` varchar(250) DEFAULT NULL
);

drop table servicioUTE;
CREATE TABLE `servicioUTE` (
  `id`  INTEGER PRIMARY KEY   AUTOINCREMENT,
  `nombre` varchar(100) DEFAULT NULL,
  `id_tecnologia` int(11) NOT NULL,
  `descripcion` varchar(250) DEFAULT NULL
);
drop table aplicativo;
CREATE TABLE `aplicativo` (
  `id`  INTEGER PRIMARY KEY   AUTOINCREMENT,
  `nombre` varchar(100) DEFAULT NULL,
  `id_servicio` int(11) DEFAULT NULL,
  `descripcion` varchar(250) DEFAULT NULL,
  `rochade` varchar(20) DEFAULT NULL,
  `id_tecnologia` int(11) DEFAULT NULL,
  `en_mantenimiento` int(1) NOT NULL DEFAULT 1
);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (1,'AFLO - AYFLO',1,'AFLO - AYFLO','AFLO',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (2,'APRO - ANTEPROYECTO',1,'APRO - ANTEPROYECTO','APRO',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (3,'AYFL - AYUDAS_FLOTA',3,'AYFL - AYUDAS_FLOTA','AYFL',2,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (4,'BISM - ISM en tu Bolsillo','BISM - ISM en tu Bolsillo','BISM',5,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (5,'CMAR - CONTAMAR2',1,'CMAR - CONTAMAR2','CMAR',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (6,'CONT - CONTAMAR',1,'CONT - CONTAMAR','CONT',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (7,'FAM2 - FAM2_BOTIQU',2,'FAM2 - FAM2_BOTIQU','FAM2',2,0);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (8,'FAMA - FARMAR_PROSA',3,'FAMA - FARMAR_PROSA','FAMA',2,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (9,'FARM - FARMAR',1,'FARM - FARMAR','FARM',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (10,'FMAR - FORMAR',1,'FMAR - FORMAR','FMAR',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (11,'FOM2 - FOMA2',2,'FOM2 - FOMA2','FOM2',2,0);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (12,'FOMA - FORMAR_PROSA',3,'FOMA - FORMAR_PROSA','FOMA',2,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (13,'FRMA - FRMA',3,'FRMA - FRMA','FRMA',2,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (14,'GFOA - GEFORA',2,'GFOA - GEFORA','GFOA',2,0);
insert into aplicativo (id,nombre,descripcion,rochade,en_mantenimiento) values (15,'IMAG - IMAGENES','IMAG - IMAGENES','IMAG',1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (16,'INBU - SEGUMAR',1,'INBU - SEGUMAR','INBU',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (17,'INCM - INCA_ISM',1,'INCM - INCA_ISM','INCA',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (18,'INVE - INVENTARIO',1,'INVE - INVENTARIO','INVE',1,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (19,'ISMW - WS_EXT_ISM','ISMW - WS_EXT_ISM','ISMW',3,1);
insert into aplicativo (id,nombre,descripcion,rochade,en_mantenimiento) values (20,'MEJP - MEJOPENS','MEJP - MEJOPENS','MEJP',1);
insert into aplicativo (id,nombre,descripcion,rochade,en_mantenimiento) values (21,'MGEN - AP_GENERICA','MGEN - AP_GENERICA','MGEN',1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (22,'MIND - ESTAD_IND',1,'MIND - ESTAD_IND','MIND',1,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (23,'MOVI - MOVIL_ISM','MOVI - MOVIL_ISM','MOVI',3,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values
 (24,'OBIS - Orquestador de servicios, operaciones, consultas vía aplicación móvil o web.',2,'OBIS - Orquestador de servicios, operaciones, consultas vía aplicación móvil o web.',
 'OBIS',2,0);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (25,'PAGO - PAGODA',1,'PAGO - PAGODA','PAGO',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (26,'PRES - PRESMAR',1,'PRES - PRESMAR','PRES',1,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (27,'SANI - SANIMA_PROSA',3,'SANI - SANIMA_PROSA','SANI',2,1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (28,'SBOT - SUBVEN_BOTIQ',2,'SBOT - SUBVEN_BOTIQ','SBOT',2,0);
insert into aplicativo (id,nombre,descripcion,rochade,en_mantenimiento) values (29,'SIEB - SIEBEL','SIEB - SIEBEL','SIEB',1);
insert into aplicativo (id,nombre,id_servicio,descripcion,rochade,id_tecnologia,en_mantenimiento) values (30,'TASA - TSE111',1,'TASA - TSE111','TASA',1,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (31,'TISM - Tu ISM','TISM - Tu ISM','TISM',5,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (32,'WBOF - WS_INSPBOT2','WBOF - WS_INSPBOT2','WBOF',3,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (33,'WISM - WS_CDISM','WISM - WS_CDISM','WISM',3,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (34,'WSAO - Servicio Web intercambio avisos OBIS','WSAO - Servicio Web intercambio avisos OBIS','WSAO',3,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (35,'WSCR - WS_CADUC_FSE','WSCR - WS_CADUC_FSE','WSCR',3,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (36,'WSPX - WS_PERMEX','WSPX - WS_PERMEX','WSPX',3,1);
insert into aplicativo (id,nombre,descripcion,rochade,id_tecnologia,en_mantenimiento) values (37,'WSRT - Servicio Web proveedor de Formación Marítima y Sanitaria','WSRT - Servicio Web proveedor de Formación Marítima y Sanitaria','WSRT',4,1);


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
  `login_name` varchar(20) NOT NULL,
  `password` varchar(20) NOT NULL,
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

  