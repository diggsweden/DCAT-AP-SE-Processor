# Lägga till ny ApiDefinition

OBS! Det går att använda separat metadata på json format istället för att stödja ny apidefinition.

För att lägga till en ny sorts apidefinition (ApidefN) krävs en kontroll av typ i ApiDefinitionParser (getApiJsonString funktionen), samt en ny funktion som gör om metadatat till ett jsonobjekt, där metadata ligger som "root element".

I exemplet nedan är ”new-dcat” en hållare för metadatat. Beroende på typ av apidefinition och möjligheter att lägga till metadata i en api definition kan den hållaren se olika ut. I Raml och OAS används t.ex. annotations och extensions.

```
{ 
  "apidefN": "1.0", 
  "info": { 
    "version": "1.0.0", 
    "title": "Ny apidefinition", 
    "description": "Exempel på en ny apidefinition", 
    "new-dcat": { 
      "dcat-dataset": { 
      ...
```
```
jsonobjekt = {"dcat-dataset": {...}
```

# Lägga till/ändra element i DCAT spec

För att införa/uppdatera element från DCAT specifikationen, görs ett tillägg/justering i matchande convertMapToDcat.json fil (en för Katalog objektet och en för alla andra).

Tillägget är fältets namn i DCAT specifikationen samt en textsträng som matchas mot metadatat i apidefinitionen.

```
"dcat:nyttelement": { 
  "text": " nytt metadata" },
```
Det är viktigt att elementet hamnar under rätt klass. Om den nya klassen har underelement ska taggens namn från specen stå som ovan och längre ner skapar man upp hela elementet.

T.ex.
```
"Dataset": { 
... 
  "dcat:nyttelement": { 
    "text": "nytt metadata" 
  },
... 
} 
... 
"dcat:nyttelement": { 
  "text": "nytt metadata", 
  ... 
  "dcterms:type": { 
    "text": "type" 
  },
},
```

Om fältet man lagt till är obligatoriskt (syns i DCAT specifikationen) behöver det också läggas till i rätt mandatory.json fil (även här en för Katalog och en för resten).

Här börjar taggen med namnet på huvudklassen och sedan vilket attribut det är i den klassen. <objektnamn>-<elementnamn>

T.ex.
```
"Dataset-dcat:nyttelement": { 
  "isMandatory": true 
 },
```
