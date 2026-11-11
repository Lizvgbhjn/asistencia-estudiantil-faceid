USE asistencia;

ALTER TABLE materia ADD COLUMN IF NOT EXISTS periodo_id BIGINT;

UPDATE materia m
JOIN (
    SELECT materia_id, MIN(desde) AS desde, MAX(hasta) AS hasta
    FROM parcial GROUP BY materia_id
) fechas ON fechas.materia_id=m.id
JOIN semestre_academico s ON s.desde<=fechas.desde AND s.hasta>=fechas.hasta
SET m.periodo_id=s.id
WHERE m.periodo_id IS NULL;

SET @fk_periodo_existe = (
    SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='materia'
      AND CONSTRAINT_NAME='fk_materia_periodo'
);
SET @ddl_periodo = IF(@fk_periodo_existe=0,
    'ALTER TABLE materia ADD CONSTRAINT fk_materia_periodo FOREIGN KEY (periodo_id) REFERENCES semestre_academico(id)',
    'SELECT 1');
PREPARE stmt_periodo FROM @ddl_periodo;
EXECUTE stmt_periodo;
DEALLOCATE PREPARE stmt_periodo;
