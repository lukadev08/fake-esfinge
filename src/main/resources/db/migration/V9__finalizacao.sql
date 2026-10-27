ALTER TABLE remessas ADD COLUMN finalizada_em TEXT;
DROP INDEX remessa_aberta_unica;
CREATE UNIQUE INDEX remessa_aberta_unica ON remessas(cenario,ug,competencia) WHERE estado='ABERTO' AND finalizada_em IS NULL;
PRAGMA user_version=9;
