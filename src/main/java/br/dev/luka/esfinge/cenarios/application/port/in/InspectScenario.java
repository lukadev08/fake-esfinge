package br.dev.luka.esfinge.cenarios.application.port.in;

import br.dev.luka.esfinge.cenarios.domain.ScenarioSnapshot;

public interface InspectScenario { ScenarioSnapshot inspect(String adminKey); }
