CREATE TABLE competencias_bloqueadas (cenario TEXT NOT NULL REFERENCES scenarios(id), ug TEXT NOT NULL, competencia INTEGER NOT NULL CHECK (competencia BETWEEN 202601 AND 202612), PRIMARY KEY(cenario,ug,competencia));
PRAGMA user_version = 13;
