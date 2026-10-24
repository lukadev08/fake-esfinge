package br.dev.luka.esfinge.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.dev.luka.esfinge")
public class SimulatorApplication {
    public static void main(String[] args) {
        try { SpringApplication.run(SimulatorApplication.class, args); System.out.println("Simulador iniciado"); }
        catch (Exception e) { System.err.println("SIM_INICIALIZACAO_INVALIDA: configuracao ou base incompativel; dados preservados"); System.exit(1); }
    }
}
