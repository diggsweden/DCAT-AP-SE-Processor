# DCAT-AP-SE Processor
[Sveriges dataportal](https://www.dataportal.se/) synliggör information om datamängder (d.v.s. metadata) där själva datamängderna och åtkomstpunkterna finns publicerade hos olika aktörer.
Detta sker genom att Sveriges dataportal automatiskt inhämtar, ”skördar”, informationen hos publicerande aktör.
En aktör kan vara både från offentlig och privat sektor samt från civilsamhället.

En förutsättning för att dataportalen ska kunna synliggöra bland annat information om datamängder och dess API:er,
är att aktörerna upprättar och publicerar informationen enligt en gemensam och standardiserad metadataspecifikation som är tillgänglig för skördning.
Den specifikationen heter [DCAT-AP-SE](https://docs.dataportal.se/dcat/sv/) och är speciellt framtagen för att passa Sveriges dataportal på grund av den är tillräckligt övergripande för
att passa inhämtning av metadata från olika typer av organisationer och datadomäner.
Det möjliggör en enhetlig beskrivning av datamängder för att förenkla insamling, sökning och presentation av data på Sveriges dataportal. Följande information innehåller mycket tekniska termer och begrepp. För att kunna tillgodogöra sig informationen bör därför någon med sådan typ av kompetens läsa instruktionen.

## Automatiserad process för framställning av metadata
I syfte att hjälpa producenter av metadata, som ska skördas till dataportalen, har detta verktyg tagits fram för att kunna införlivas i godtycklig CI/CD driven kedja eller köras separat.
Verktyget skapar en metadataspecifikation på RDF-format utifrån en API-definition alternativt separat metadatafil.
RDF är det språk som används för att uttrycka metadata om ting på webben. En central egenskap med RDF är att man använder webbadresser (URI:er) för att referera till ting i olika påståenden.

## Format på API-definitioner
Det finns stöd för formaten OpenAPI eller RAML.
* OpenAPI 3.x tillägg av metadata sker via extensions<br>
  Lägg till en extension x-dcat och underliggande metadata hanteras.
* RAML1.x tillägg av metadata sker via annotations<br>
  Definiera en annotationType och använd den sedan.
* OAS2.x och RAML0.8 tillägg av metadata sker via separat metadatafil på json format.

## Ingående delar i verktyget
Sekvensdiagram över flödet i verktyget.
![img_1.png](docs/dcat-sekvens.png)

### PreprocessorController (REST API)
REST API:n för verktyget, följande två är de som finns att använda:

"/dcat-generation/files/" - Skickar man in directory (dir) som sedan skickas vidare till Managern för hantering. <br>
"/dcat-generation/web/" - Är endpointen från Web-guit som skickar med antingen en sträng med hela apidefinitionen eller en lista med filer som sedan skickas vidare till Managern.

### Manager

Tar emot anrop från REST API't eller formuläret och styr parsning, konvertering och uppskapande av RDF-data.

### ApiDefinitionParser

Använder [snakeYaml](https://bitbucket.org/asomov/snakeyaml) för att parsa RAML1.0 och OAS3 yaml strängen, som innehåller apidefinitionen, till JSONObject.

När det gäller OAS3 json format läses den strängen in som JSONObject direkt.

RAML0.8 och/eller OAS2 på json/yaml format stöder inte annotations/extensions, så där får användaren tillgång att lägga metadata i en separat fil på json format, som parsas till JSONObject.

### Converter

Använder MultiValuedMap (Apache Commons) och JSON.simple.

För att konvertera mellan inläst metadata till element på DCAT-AP-SE format används konverteringsfiler (t.ex. TO_DCAT_OAS.json).<br>
I senare skeden kan man enkelt lägga till nya konverteringsfiler för andra format eller ändra till nyare versioner av befintliga format.

### RDFWorker

Använder RDF4J.

Tar emot en lista av Katalog objekt och skapar matchande RDF utifrån det.

## Hur du använder DCAT-AP-SE Processor
Bygg en container image från koden i det här repositoryt, t.ex:
```
docker build --no-cache -t "dcatprocessor" .
```

docker run -it --rm -p 8080:8080 dcatprocessor:latest 

När container startas finns ett formulär och ett REST API tillgängligt att använda efter behov.

Alternativt, finns det också en experimentell färdigbyggd image för att testa.

```
docker run ghcr.io/diggsweden/dcat-ap-processor:latest
```
**ANVÄND INTE OVAN IMAGE I PRODUKTIONSYFTEN** - Den är endast avsedd för lokal test,
och inga garantier lämnas på säkerhetsuppdateringar med mera.

Verktyget kan användas på följande sätt.
### 1. Via UI
Starta docker container, öppna browser till http://localhost:8080

Det finns val för att:
* skicka in en sträng med API-definitionen.
* bifoga en fil med API-definitionen.
* ange en katalog som håller flera API-definitioner.

Verktyget levererar resultatet som svar på sidan.

### 2. Via anrop till REST gränssnitt
Anropa endpoints med valfritt verktyg. I utveckling har vi använt curl från git bash.

Jenkins pipeline exempel
[Jenkinsfile](docs/jenkinsfile).

### 3. Via CLI
Konvertera en specifikationsfil och få DCAT-data till stdout:
```
java -jar dcatprocessor.jar -f FIL
```

Konvertera en katalog med specifikationsfiler och få DCAT-data till stdout:
```
java -jar dcatprocessor.jar -d KATALOG
```

## Lägga till stöd för nya metadata i verktyget
[Översikt över vad som finns och fungerar enligt DCAT-AP-SE spec](docs/DCATAPSE_completion.md)<br>
[Tillägg i Converter](docs/converter-tutorial.md)<br>
[Tillägg i RDFWorker](docs/rdfworker-tutorial.md)<br>
[specifikationsfil](src/main/resources/dcat_specification.properties)

## Arbetsprocess för att publicera api/er på dataportalen
- [Skapa konto](https://docs.dataportal.se/accounts/) till de bakomliggande systemen för dataportal.se eller kontrollera [status](https://admin.dataportal.se/status/public) om er organisation redan finns upplagd. Upprätta sedan en skördningskälla, [Komma igång](https://docs.dataportal.se/registry/start/).<br>
- Inför metadata i apidefinitionen, eller skapa en separat metadatafil.<br>
- Använd verktyget för att generera en RDF fil.
- Skörda RDF filen, [Hantera organisatoiner och skördningskällor](https://docs.dataportal.se/registry/organisationer/#lagga-till-en-organisation).
- Verifiera att skördningen fungerar.
[Dataportalen docs](https://docs.dataportal.se/) har ingående information om hur skördningen fungerar samt hur en organisation sätter upp sin katalogkälla.

### Införa metadata
För att verktyget ska ges information att generera data behöver api definitionen uppdateras med metadata information.<br>
Repositoryt innehåller [exempelfiler](src/main/resources/metadataExample) som visar hur metadata kan införas i  apidefinition eller i separat metadatafil.<br>
[Attribut som stöds](docs/supported_attributes.md) finns listade med beskrivning.
Utgå från exempelfilerna och ta hjälp av [rekommendationer](https://docs.dataportal.se/dcat/docs/recommendations/) på dataportalen<br>

Det finns exempelfiler som visar hur obligatoriska, rekommenderade och valfria värden kan läggas in.<br>
När apidefinition/erna är uppdaterade med metadata görs de tillgängliga på en publikt nåbar folder, så att den separata pipeline som kör verktyget kan nå dem.

#### Om ni har ett API
Se exempel [enkel fil](src/main/resources/metadataExample/single), där Katalog elementet ligger i samma fil som resterande metadata.

Exempel med obligatoriska och rekommenderade värden.<br>
[RAML](src/main/resources/metadataExample/single/full_example.raml)<br>
[OAS YAML](src/main/resources/metadataExample/single/full_example_oas.yaml)<br>
[OAS JSON](src/main/resources/metadataExample/single/full_example_oas.json)<br>

För API som inte har en definition (code-first) kan dataproducenten tillhandahålla separat metadata på json format<br>
[Separat JSON](src/main/resources/metadataExample/single/full_example.json)<br>

#### Om ni har flera API:er
Organisation med multipla API att producera RDF från använder en separat catalog.json fil för att hålla samman de ingående API:ernas metadata, se exempel under [multipla filer](src/main/resources/metadataExample/multiple).<br>
För att verktyget ska generera en sammanslagen RDF-fil krävs att organisationen skapar filer enligt följande:<br>
catalog.json - beskriver det övergripande katalog elementet och är samma för alla ingående apier.<br>
[catalog.json](src/main/resources/metadataExample/multiple/catalog.json) - Katalog elementet i separat fil på json format.<br>

Exempel på ingående API-definitioner innehållande metadata för DCAT-AP-SE<br>
[full_example.raml](src/main/resources/metadataExample/multiple/full_example.raml) - Api A på RAML format<br>
[full_example_oas.yaml](src/main/resources/metadataExample/multiple/full_example_oas.yaml) - Api B på OAS3 yaml format<br>
[full_example_oas.json](src/main/resources/metadataExample/multiple/full_example_oas.json) - Api C på OAS3 json format<br>
[full_example.json](src/main/resources/metadataExample/multiple/full_example.json), Api D, separat metadataspecifikation på json format<br>

## Licens
dcat-ap-se-processor är licensierad under [GPL v3](LICENSE)

## Beroenden
snakeYaml [Apache license](docs/Licenser/Apache.txt)<br>
RDF4J [EDL v1.0 license](docs/Licenser/edl-v10.txt)<br>
Spring boot, Spring framework [Apache license](docs/Licenser/Apache.txt)<br>
Project Lombok [MIT license](docs/Licenser/MIT.txt)<br>
commonmark-java [BSD-2 clause simplified license](docs/Licenser/BSD-2.txt)<br>
json-ld-java [BSD-3 clause license](docs/Licenser/BSD-3.txt)<br>
jackson-dataformat-yaml [Apache license](docs/Licenser/Apache.txt)<br>
hibernate-json-org-contributor [BSD-3 clause license](docs/Licenser/BSD-3.txt  )<br>
json-simple [Apache license](docs/Licenser/Apache.txt)<br>
commons-collections4 [Apache license](docs/Licenser/Apache.txt)<br>

## Status v0.9
Detta är en första version av verktyget. <br>
Arbetsförmedlingen och Bolagsverket kommer prova mjukvaran skarpt under hösten 2022. <br>
När mjukvaran fungerar för tillräckligt många offentliga organisationer kommer versionen uppdateras till 1.0. <br>
Mjukvaran utvecklas av DIGG och Arbetsförmedlingen.

## Bidra
