# FleetCheck – Gradle version

Same FleetCheck application (same `src/`), built with Gradle instead of Maven.

Expected application output:

```
FleetCheck 1.0
Vehicles loaded: 4
Vehicles requiring service: 2
Average mileage: 37000 km
```

---

## Relatório – evidências e respostas (Gradle)

Ambiente: JDK 21.0.9, Gradle 9.6.0. O `src/` é igual ao do projeto Maven (já com o defeito de
fronteira corrigido e com `FleetServiceTest`).

### 8.1 – build inicial sem Jackson
`gradle clean build` falha em `:compileJava`:

```
App.java:4: error: package com.fasterxml.jackson.databind does not exist
```

**Evidência 8.1:** falta a dependência `com.fasterxml.jackson.core:jackson-databind`
(usada pelos imports `ObjectMapper` e `TypeReference` em `App.java`).

### 8.2 – dependência e grafo
Acrescentado `implementation 'com.fasterxml.jackson.core:jackson-databind:2.22.2'`.

Nota: com o Gradle 9 a fase de testes falhava com
`Failed to load JUnit Platform ... including the JUnit Platform launcher`. O Gradle 9 deixou de
fornecer automaticamente o launcher do JUnit Platform, por isso foi acrescentado
`testRuntimeOnly 'org.junit.platform:junit-platform-launcher'`. Depois disso: `BUILD SUCCESSFUL`.

`gradle dependencies --configuration runtimeClasspath`:

```
\--- com.fasterxml.jackson.core:jackson-databind:2.22.2
     +--- com.fasterxml.jackson.core:jackson-annotations:2.22
     +--- com.fasterxml.jackson.core:jackson-core:2.22.2
     |    \--- com.fasterxml.jackson:jackson-bom:2.22.2
     |         ...
     \--- com.fasterxml.jackson:jackson-bom:2.22.2 (*)
```

Direta: `jackson-databind`. Transitivas: `jackson-core`, `jackson-annotations` (e o `jackson-bom`,
que só alinha versões — linhas `(c)` = constraint).

**Evidência 8.2:** não. As dependências de runtime da aplicação são exatamente as mesmas do
`mvn dependency:tree` (databind 2.22.2, core 2.22.2, annotations 2.22). Só muda a forma de as declarar
e de mostrar o grafo; o Gradle mostra também o `jackson-bom` (metadados Gradle Module Metadata que o
Maven não usa para resolução).

### 8.3 – JAR executável
JAR por omissão: `no main manifest attribute, in build/libs/fleetcheck-1.0.0.jar`.

Com o plugin `application` e a configuração `jar { manifest ...; from { runtimeClasspath ... } }`:

```
> java -jar build/libs/fleetcheck-1.0.0.jar
FleetCheck 1.0
Vehicles loaded: 4
Vehicles requiring service: 2
Average mileage: 37000 km
```

**Evidência 8.3:** o JAR passou de alguns KB para ~2,3 MB. Agora tem no `MANIFEST.MF` o atributo
`Main-Class: pt.upt.fleetcheck.App` e inclui, descompactadas (`zipTree`), as classes de todas as
dependências do `runtimeClasspath` (Jackson). Ficheiros repetidos (ex.: `META-INF/LICENSE`) são
ignorados por `DuplicatesStrategy.EXCLUDE`. É o equivalente ao fat JAR do Shade no Maven, mas aqui
substitui o próprio JAR em vez de criar um artefacto `-all` separado.

### 8.4 – Gradle Wrapper
`gradle wrapper` gerou `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` e
`gradle-wrapper.properties` (Gradle 9.6.0). `gradlew.bat clean build` → `BUILD SUCCESSFUL`.

**Pergunta:** removeu o pressuposto de que a máquina já tem o Gradle instalado e na versão certa
(versões diferentes do Gradle têm comportamento diferente — ver o caso do JUnit launcher no Gradle 9).
O wrapper descarrega e usa sempre a versão fixada no projeto, localmente e no CI.

### 8.5 – GitHub Actions
Workflow em `.github/workflows/build-gradle.yml`; `gradlew` marcado como executável no Git.

**Evidência 8.5:** execução bem-sucedida (verde, artefacto `fleetcheck-gradle-build`): https://github.com/RodriMonteiroM/fleetcheck-gradle/actions/runs/37233974465
Repositório: https://github.com/RodriMonteiroM/fleetcheck-gradle

### 8.6 – SBOM com Gradle
`gradlew.bat cyclonedxBom` gera `build/reports/cyclonedx/bom.json`, que contém
`jackson-databind`, `jackson-core`, `jackson-annotations` (e `jackson-bom`).

**Evidência 8.6:** pela mesma razão que no Maven: o plugin CycloneDX gera o SBOM a partir do grafo de
dependências *resolvido* (`runtimeClasspath`), não do texto do `build.gradle`. Só escrevemos o
`jackson-databind`, mas ele puxa transitivamente `jackson-core` e `jackson-annotations`, que vão dentro
do JAR e são executados, por isso fazem parte do inventário de componentes do software.

### 8.7 – Comparação Maven vs Gradle

| Tarefa | Maven | Gradle |
|---|---|---|
| Configuração do build | `pom.xml` (XML declarativo) | `build.gradle` (DSL Groovy) + `settings.gradle` |
| Build limpo | `mvnw.cmd clean verify` | `gradlew.bat clean build` |
| Adicionar dependência | `<dependency>...</dependency>` | `implementation 'group:artifact:version'` |
| Inspecionar dependências | `mvn dependency:tree` | `gradle dependencies --configuration runtimeClasspath` |
| Wrapper | `mvnw` / `mvnw.cmd` + `.mvn/wrapper/` | `gradlew` / `gradlew.bat` + `gradle/wrapper/` |
| Pasta de output | `target/` | `build/` |
| Localização do JAR | `target/` (`fleetcheck-1.0.0-all.jar`) | `build/libs/` (`fleetcheck-1.0.0.jar`) |
| Fat JAR | `maven-shade-plugin` | configuração da task `jar` (`from runtimeClasspath`) |
| SBOM | CycloneDX Maven plugin → `target/bom.json` (fase `verify`) | CycloneDX Gradle plugin → `build/reports/cyclonedx/bom.json` (task `cyclonedxBom`) |

**Pergunta final – o que mudou: o software ou o processo de build?** Mudou apenas o processo de build.
O código-fonte, os recursos, os testes, as dependências resolvidas e o output da aplicação são
idênticos (`Vehicles loaded: 4`, `Vehicles requiring service: 2`, `Average mileage: 37000 km`). O que
muda é a ferramenta e a forma de descrever as mesmas etapas de qualidade: resolução de dependências,
compilação, testes, empacotamento, wrapper, CI e SBOM.
