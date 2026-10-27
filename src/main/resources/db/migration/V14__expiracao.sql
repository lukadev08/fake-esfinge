ALTER TABLE remessas ADD COLUMN expira_segundos INTEGER NOT NULL DEFAULT 0;
ALTER TABLE remessas ADD COLUMN expira_nanos INTEGER NOT NULL DEFAULT 0 CHECK(expira_nanos BETWEEN 0 AND 999999999);
ALTER TABLE remessas ADD COLUMN expirada_em TEXT;
DROP INDEX remessa_aberta_unica;
CREATE UNIQUE INDEX remessa_aberta_unica ON remessas(cenario,ug,competencia) WHERE estado='ABERTO' AND finalizada_em IS NULL AND cancelada_em IS NULL AND expirada_em IS NULL;
CREATE INDEX remessa_expiracao ON remessas(cenario,expira_segundos,expira_nanos,id) WHERE finalizada_em IS NULL AND cancelada_em IS NULL AND expirada_em IS NULL;
-- O registro V14 inclui backfill Java de atividade + 72h exatos, na mesma transação.
PRAGMA user_version=14;
