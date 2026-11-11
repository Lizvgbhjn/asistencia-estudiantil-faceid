USE asistencia;

CREATE TABLE IF NOT EXISTS semestre_academico (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    desde DATE NOT NULL,
    hasta DATE NOT NULL,
    CHECK (hasta >= desde)
) ENGINE=InnoDB;

INSERT INTO semestre_academico(nombre,desde,hasta)
SELECT 'Período existente', fechas.desde, fechas.hasta
FROM (SELECT MIN(desde) AS desde, MAX(hasta) AS hasta FROM parcial) fechas
WHERE fechas.desde IS NOT NULL AND NOT EXISTS (SELECT id FROM semestre_academico);
