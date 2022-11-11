# Arbetssätt vid lokal utveckling

## Använda Docker
Kopiera fil från lokal dator till den container man vill köra från

[docker cp](https://docs.docker.com/engine/reference/commandline/cp/)
```
$ docker cp apidef.raml pensive_gates:\opt\apidef.raml
```

Använda docker på dator. Bygg docker image
```
$ mvn clean install
$ docker build --no-cache -t "dcatprocessor" .
```
Starta containern
Från Docker Desktop genom att gå till images och klicka Run samt sätta "Ports/Local port" till 8080
eller från kommandorad
```
$ docker run -p 8080:8080 -d --name dcatprocessor dcatprocessor
```
Kopiera testfiler till container:/apidef<br>
I en riktig miljö hade api producenterna kopierat sina apidefinitioner till en gemensam katalog
och satt sin RDF pipeline att läsa upp dem därifrån.<br>
(docker cp source containername:destination)
```
(docker cp source containername:destination)
$ docker cp apidef.raml hopeful_boyd:/apidef
```

## Köra direkt från jar-filen
För att  köra verktyget direkt från jarfilen öppnar man ett kommandfönster och går till där jar-filen ligger. Sedan kör du:
```
java -jar <jar-file-name>.jar
```
Detta kör igång verktyget lokalt på datorn.

# Göra anrop till Verktyget
Kör anrop till rest api via git bash, Postman eller annat verktyg<br>

```
Läser filer från /apidef på containern
$ curl http://localhost:8080/api/v1/rdf
```
eller testa med anrop via formulär metoden (Stå i samma katalog som en fil som heter obl_rek_raml.raml eller peka ut nån annan)
```
$ curl -F apispecification= -F create=create -F apitype=apitype.R10 -F apifile=@obl_rek_raml.raml http://localhost:8080/generate/dcat/0
```
Verktyget har även ett enklare GUI man kan använda för att skicka in apispecifikationer genom. GUI:t startas i webläsare genom att gå till (förutsatt att verktyget körs på localhost):

http://localhost:8080/

# Loggning
Exceptions skrivs till container loggen "/opt/logs/dcatprocessor.log".<br>