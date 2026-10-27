package br.dev.luka.esfinge.remessas.adapter.in.scheduler;

import br.dev.luka.esfinge.remessas.application.port.in.Processamento;
import org.springframework.scheduling.annotation.Scheduled;

public final class ProcessingScheduler {
    private final Processamento processing;
    public ProcessingScheduler(Processamento processing) {this.processing=processing;}
    @Scheduled(fixedDelay=100)
    public void tick() {
        try {processing.tick();}catch(RuntimeException failure) {System.err.println("Processamento temporariamente indisponivel");}
    }
}
