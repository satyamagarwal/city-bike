# Oslo bysykkel

## Forhåndsvising

![Bilde](/media/demo.gif?raw=true)

## Forutsetninger :

* Last ned `node`. 
    * Kjøre feks `brew install node`, eller sjekk https://nodejs.org/en/download/
* Last ned `pnpm`.
    * Kjøre feks `brew install pnpm`, eller sjekk https://pnpm.io/installation
* Last ned jdk 18.
    * Kjør `brew install --cask temurin`, eller sjekk https://adoptium.net/en-GB/temurin/releases/?version=18
    * Husk at `JAVA_HOME` bør settes til å bruke `jdk18`.
* Clone prosjekt lokalt med `git clone git@github.com:satyamagarwal/city-bike.git`.

### Kjøre app med fat jar

* `cd <path-to-dir-holding-repository>/city-bike`
* `./gradlew clean installNodeModules copyDistFolder build shadowJar` 
* `java "-Dlog4j2.level=INFO" "-Denv=prod" -jar build/libs/city-bike-all.jar`
* Besøk http://localhost:8080

### Kjøre app i development mode

Dette oppsett antar at man bruker Intellij idea. 

* Åpne repo directory i intellij
* Kjøre `src/main/kotlin/city/bike/status/EntryMain.kt` file. Da kjøres backend server som serverer på `http://localhost:8080` 
* `cd city-bike-client`
* `pnpm install`
* `pnpm build`
* `pnpm dev`. Da serveres frontend client på `http://localhost:8081`
