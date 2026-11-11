USE asistencia;

ALTER TABLE cuenta ADD COLUMN IF NOT EXISTS apellidos VARCHAR(120) AFTER nombre;
