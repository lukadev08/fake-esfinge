package br.dev.luka.esfinge.cenarios.application.port.out;

import br.dev.luka.esfinge.cenarios.domain.ScenarioSnapshot;

public interface ScenarioRepository { ScenarioSnapshot load(String scenario); }
