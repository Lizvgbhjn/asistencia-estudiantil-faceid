CREATE DATABASE IF NOT EXISTS asistencia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE asistencia;

CREATE TABLE IF NOT EXISTS cuenta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(64) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    apellidos VARCHAR(120),
    clave VARCHAR(180) NOT NULL,
    rol ENUM('ADMIN','DOCENTE','ESTUDIANTE') NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS perfil_facial (
    estudiante_id BIGINT PRIMARY KEY,
    descriptor MEDIUMBLOB NOT NULL,
    version_modelo VARCHAR(40) NOT NULL,
    registrado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consentimiento_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (estudiante_id) REFERENCES cuenta(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS semestre_academico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    desde DATE NOT NULL,
    hasta DATE NOT NULL,
    CHECK (hasta >= desde)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS materia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(24) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    docente_id BIGINT NOT NULL,
    cupos_a SMALLINT UNSIGNED NOT NULL DEFAULT 30,
    cupos_b SMALLINT UNSIGNED NOT NULL DEFAULT 30,
    nivel TINYINT UNSIGNED,
    periodo_id BIGINT,
    FOREIGN KEY (docente_id) REFERENCES cuenta(id),
    CONSTRAINT fk_materia_periodo FOREIGN KEY (periodo_id) REFERENCES semestre_academico(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS parcial (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    materia_id BIGINT NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    desde DATE NOT NULL,
    hasta DATE NOT NULL,
    UNIQUE (materia_id, nombre),
    FOREIGN KEY (materia_id) REFERENCES materia(id),
    CHECK (hasta >= desde)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS clase (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parcial_id BIGINT NOT NULL,
    tema VARCHAR(120) NOT NULL,
    inicio DATETIME NOT NULL,
    fin DATETIME NOT NULL,
    tolerancia SMALLINT UNSIGNED NOT NULL,
    cancelada BOOLEAN NOT NULL DEFAULT FALSE,
    paralelo ENUM('A','B') NOT NULL DEFAULT 'A',
    UNIQUE (parcial_id, inicio),
    FOREIGN KEY (parcial_id) REFERENCES parcial(id),
    CHECK (fin > inicio)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS matricula (
    materia_id BIGINT NOT NULL,
    estudiante_id BIGINT NOT NULL,
    estado ENUM('PENDIENTE','APROBADA','RECHAZADA','RETIRADA','HABILITADA','RESERVADA') NOT NULL,
    desde DATE,
    hasta DATE,
    foto_matricula MEDIUMBLOB,
    paralelo ENUM('A','B'),
    PRIMARY KEY (materia_id, estudiante_id),
    FOREIGN KEY (materia_id) REFERENCES materia(id),
    FOREIGN KEY (estudiante_id) REFERENCES cuenta(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS asistencia_registro (
    clase_id BIGINT NOT NULL,
    estudiante_id BIGINT NOT NULL,
    fecha DATETIME NOT NULL,
    estado ENUM('ASISTENCIA','ATRASO') NOT NULL,
    foto MEDIUMBLOB NOT NULL,
    PRIMARY KEY (clase_id, estudiante_id),
    FOREIGN KEY (clase_id) REFERENCES clase(id),
    FOREIGN KEY (estudiante_id) REFERENCES cuenta(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS justificacion (
    clase_id BIGINT NOT NULL,
    estudiante_id BIGINT NOT NULL,
    responsable_id BIGINT NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    fecha DATETIME NOT NULL,
    PRIMARY KEY (clase_id, estudiante_id),
    FOREIGN KEY (clase_id) REFERENCES clase(id),
    FOREIGN KEY (estudiante_id) REFERENCES cuenta(id),
    FOREIGN KEY (responsable_id) REFERENCES cuenta(id)
) ENGINE=InnoDB;
