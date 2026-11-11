USE asistencia;

CREATE TABLE IF NOT EXISTS perfil_facial (
    estudiante_id BIGINT PRIMARY KEY,
    descriptor MEDIUMBLOB NOT NULL,
    version_modelo VARCHAR(40) NOT NULL,
    registrado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    consentimiento_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (estudiante_id) REFERENCES cuenta(id)
) ENGINE=InnoDB;

ALTER TABLE perfil_facial
    ADD COLUMN IF NOT EXISTS consentimiento_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
