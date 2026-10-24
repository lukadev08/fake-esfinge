CREATE TABLE schema_migrations (version INTEGER PRIMARY KEY, checksum TEXT NOT NULL);
CREATE TABLE scenarios (id TEXT PRIMARY KEY, profile TEXT NOT NULL, seed INTEGER NOT NULL, initial_time TEXT NOT NULL, clock_mode TEXT NOT NULL, token_seconds INTEGER NOT NULL CHECK (token_seconds > 0), generation INTEGER NOT NULL DEFAULT 0);
CREATE TABLE users (scenario_id TEXT NOT NULL REFERENCES scenarios(id), user_id TEXT NOT NULL, access_code TEXT NOT NULL, password_hash TEXT NOT NULL, salt TEXT NOT NULL, PRIMARY KEY(scenario_id, user_id), UNIQUE(scenario_id, access_code));
CREATE TABLE user_ugs (scenario_id TEXT NOT NULL, user_id TEXT NOT NULL, ug TEXT NOT NULL, PRIMARY KEY(scenario_id, user_id, ug), FOREIGN KEY(scenario_id, user_id) REFERENCES users(scenario_id, user_id));
CREATE TABLE sessions (token_hash TEXT PRIMARY KEY, scenario_id TEXT NOT NULL, user_id TEXT NOT NULL, ug TEXT NOT NULL, issued_at TEXT NOT NULL, expires_at TEXT NOT NULL, FOREIGN KEY(scenario_id, user_id, ug) REFERENCES user_ugs(scenario_id, user_id, ug));
PRAGMA user_version = 1;
