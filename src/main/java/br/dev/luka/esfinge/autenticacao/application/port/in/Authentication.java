package br.dev.luka.esfinge.autenticacao.application.port.in;

public interface Authentication {
    br.dev.luka.esfinge.autenticacao.domain.Session authenticate(String token);
    String login(String accessCode, String password, String ug);
}
