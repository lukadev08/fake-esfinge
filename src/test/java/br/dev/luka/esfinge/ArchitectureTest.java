package br.dev.luka.esfinge;

import com.tngtech.archunit.core.importer.*;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class ArchitectureTest {
    final com.tngtech.archunit.core.domain.JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages("br.dev.luka.esfinge");
    @Test void nucleoDependeSomenteDeJavaENucleo() {
        noClasses().that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat().resideOutsideOfPackages("java..", "br.dev.luka.esfinge..domain..", "br.dev.luka.esfinge..application..")
                .check(classes);
    }
    @Test void dominioNaoDependeDaAplicacao() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..").check(classes);
    }
    @Test void capacidadesNaoPossuemCiclos() {
        slices().matching("br.dev.luka.esfinge.(*)..").should().beFreeOfCycles().check(classes);
    }
}
