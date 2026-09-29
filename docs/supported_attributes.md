# Attribut från DCAT-AP-SE som stöds i den här versionen

I metadata finns de värden som api producenten inför.<br>
Dessa värden finns beskrivna i DCAT-AP-Se specifikationen som länkas i tabellen nedan

| Primär klass | Metadata | Property DCAT-AP-SE 3.0.1 | Beskrivning |
|:---|:---|:---|:---|
| Katalog (Catalog) | about | - | Identifierare för katalogen |
| | title | dcterms:title | Katalogens namn. Denna egenskap kan upprepas för parallella språkversioner av namnet. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | description | dcterms:description | Katalogens beskrivning. Denna egenskap kan upprepas för parallella språkversioner av beskrivningen. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | publisher | dcterms:publisher | Egenskapen hänvisar till en enhet (organisation) som ansvarar för att göra katalogen tillgänglig. Anges som en Aktör, se egen sektion nedan. |
| | license | dcterms:license | I Sverige kräver vi licensen CC0-1.0 (Public domain) på katalogen, dvs man ska använda URI:n <http://creativecommons.org/publicdomain/zero/1.0/> |
| | issued | dcterms:issued | Egenskapen innehåller katalogens utgivningsdatum. |
| | modified | dcterms:modified | Datum för senaste ändring av katalogen. |
| | homepage | foaf:homepage | Webadress till katalogens huvudsida. |
| | location | dcterms:spatial | Det geografiska området som omfattas av katalogen. Anges som ett Geografiskt område, se egen sektion nedan. |
| | rights | dcterms:rights | Egenskapen refererar till en rättighetsförklaring som syftar till att gruppera en mängd egenskaper som upphovsrätt. Anges som en Rättighetsförklaring, se egen sektion nedan. |
| | hasPart | dcterms:hasPart | Hänvisar till en relaterad katalog som är del av den beskrivna katalogen. |
| | isPartOf | dcterms:isPartOf | Hänvisar till en närstående katalog där den beskrivna katalogen fysiskt eller logiskt ingår. |
|---|---|---|---|
| Datamängd (Dataset) | about | - | Identifierare för datamängden |
| | title | dcterms:title | Datamängdens namn. Denna egenskap kan upprepas för parallella språkversioner av namnet. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | description | dcterms:description | Datamängdens beskrivning. Denna egenskap kan upprepas för parallella språkversioner av beskrivningen. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | publisher | dcterms:publisher | Egenskapen hänvisar till en enhet (organisation) som ansvarar för att göra datamängden tillgänglig. Anges som en Aktör, se egen sektion nedan. |
| | creator | dcterms:creator | Egenskapen hänvisar till en enhet (organisation) som är huvudansvarig för att producera datamängden. Anges som en Aktör, se egen sektion nedan. |
| | other agent | prov:qualifiedAttribution | Konstruktionen med övrig aktör gör det möjligt att peka ut aktörer i andra roller än utgivare (dcterms:publisher) och producent (dcterms:creator). Se egen sektion Övrig aktör nedan. |
| | contactPoint | dcat:contactPoint | Egenskapen innehåller kontaktuppgifter som kan användas för att flagga fel i datamängden eller för att skicka kommentarer. Anges som en Kontaktuppgift, se egen sektion nedan. |
| | keyword | dcat:keyword | Egenskapen innehåller ett nyckelord som beskriver datamängden. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | theme | dcat:theme | En kategori för datamängden. En datamängd kan associeras med flera teman. För att se vilka värden man kan skriva in se: [Kategori - värden](supported_attributes_theme.md) |
| | identifier | dcterms:identifier | Datamängdens huvudsakliga identifikator |
| | adms | adms:identifier | Egenskapen avser en sekundär identifikator för datamängden |
| | issued | dcterms:issued | Egenskapen anger datamängdens utgivningsdatum. |
| | modified | dcterms:modified | Datum för senaste ändring av datamängden. |
| | landingPage | dcat:landingPage | Den här egenskapen hänvisar till en webbsida som ger tillgång till datamängden eller dess distributioner och/eller ytterligare information. Den bör peka på en landningssida hos den ursprungliga dataleverantören och inte till en sida på en webbplats av en tredje part |
| | conformsTo | dcterms:conformsTo | Egenskapen hänvisar till en tillämpningsföreskrift eller annan specifikation. Anges som en Standard, se egen sektion nedan. |
| | location | dcterms:spatial | Det geografiska området som omfattas av datamängden. Anges som ett Geografiskt område, se egen sektion nedan. |
| | spatialUrl | dcterms:spatial | Namngivet geografiskt område angivet som URI. Det rekommenderas att använda Geonames URI:er i följande form: <http://sws.geonames.org/6695072> |
| | temporal | dcterms:temporal | Datamängdens giltighetsperiod. Anges som en Tidsperiod, se egen sektion nedan. |
| | temporalResolution | dcat:temporalResolution | Den minsta tidsperiod som går att urskilja i datamängden. Till exempel P1Y som indikerar att data har en upplösning per år. |
| | spatialResolutionInMeters | dcat:spatialResolutionInMeters | Den minsta rumsliga upplösning som går att urskilja i datamängden |
| | accrualPeriodicity | dcterms:accrualPeriodicity | Avser datamängdens uppdateringsfrekvens. För att se vilka värden man kan skriva in se: [Uppdateringsfrekvens - värden](supported_attributes_accrualPeriodicity.md) |
| | version | dcat:version | Ett versionsnummer eller annan versionsbeteckning för datamängden. |
| | versionNotes | adms:versionNotes | Innehåller en beskrivning av skillnaderna mellan denna version och en tidigare version av datamängden. Denna egenskap kan upprepas för parallella språkversioner av versionsanteckningar. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | source | dcterms:source | En referens till en relaterad datamängd från vilken den beskrivna datamängden härleds. |
| | accessRights | dcterms:accessRights | Den här egenskapen hänvisar till information som indikerar huruvida datamängden/datatjänsten innehåller öppna data. För att se vilka värden man kan skriva in se: [Åtkomsträttigheter - värden](supported_attributes_accessRights.md) |
| | offers | schema:offers | Avgift. Anges som en Avgift, se egen sektion nedan. |
| | hasVersion | dcat:hasVersion | En relaterad datamängd som är en version, utgåva eller anpassning av den beskrivna datamängden. |
| | isReferencedBy | dcterms:isReferencedBy | Relaterad resurs, t.ex. en publikation, som refererar, citerar eller på något annat sätt pekar på datamängden. |
| | relation | dcterms:relation | Egenskapen pekar ut en relaterad resurs. |
| | qualifiedRelation | dcat:qualifiedRelation | En kvalificerad relation gör det möjligt att peka på en relaterad resurs tillsammans med en roll som förtydligar relationens karaktär. Se egen sektion Kvalificerad relation nedan. |
| | page | foaf:page | En sida eller ett dokument om denna datamängd. Anges som ett Dokument, se egen sektion nedan. |
| | provenance | dcterms:provenance | Egenskapen uttrycker datamängdens härkomst, inklusive ursprung och ägarhistorik. Värdet anges med språkändelse, t.ex. `provenance-sv`, och skrivs ut som en beskrivning i RDF-filen. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | distribution | dcat:distribution | Denna egenskap förbinder en datamängd med en av dess distributioner. Se egen sektion Distribution nedan. |
| | inSeries | dcat:inSeries | En datamängdsserie som datamängden ingår i. |
| | subject | dcterms:subject | Kategorisering motsvarande nationella grunddatadomäner. Se lista med domäner: dataportal.se/sv/terminology/grunddata |
| | applicableLegislation | dcatap:applicableLegislation | Lagstiftning som kräver att datamängden skapas och/eller hanteras. |
| | hvdCategory | dcatap:hvdCategory | Kategori för värdefulla datamängder enligt kommissionens genomförandeförordning (EU) 2023/138. Endast obligatoriskt om datamängden är klassad som "värdefull datamängd" — annars ska det inte anges alls. |
|---|---|---|---|
| Distribution (Distribution) | about | - | Identifierare för distributionen |
| | title | dcterms:title | Distributionens namn. Denna egenskap kan upprepas för parallella språkversioner av namnet. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | description | dcterms:description | Distributionens beskrivning. Denna egenskap kan upprepas för parallella språkversioner av beskrivningen. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | accessURL | dcat:accessURL | En webbadress till datamängdens distribution. |
| | downloadURL | dcat:downloadURL | En fil som innehåller datamängdens distribution i ett givet format. |
| | formatMedia | dcterms:format | Ett urval av vanliga mediatyper enligt listan föreslaget av rekommendationen DCAT-AP. För att se vilka värden man kan skriva in se: [Vanliga mediatyper - värden](supported_attributes_media.md) |
| | formatGeo | dcterms:format | Denna lista innehåller mediatyper som bör användas för datamängder inom INSPIRE, mer specifikt i INSPIREs nedladdningstjänster. Närhelst etablerade IANA mediatyper finns används dessa. När mediatyper saknas har INSPIRE - i allafall temporärt - definierat egna mediatyper. I enlighet med reglerna RFC 2046/RFC 4288 startas subtypen för dessa med x-. Utöver de dataorienterade mediatyperna listas också fyra protokollorienterade format (application/vnd.ogc.\[wms&#x23ae;wmts&#x23ae;wfs&#x23ae;wcs\]_xml) för att täcka in de fall när man skapar upp enbart en distribution som motsvarar protokollet istället för en distribution per format. För att se vilka värden man kan skriva in se: [Geografiska mediatyper - värden](supported_attributes_geographicalmedia.md) |
| | formatText | dcterms:format | Om du inte kan hitta din mediatyp i listan ovanför kan du tillhandahålla en för hand. Se lista över etablerade mediatyper på: <http://www.iana.org/assignments/media-types/media-types.xhtml>. |
| | accessService | dcat:accessService | Datatjänst för åtkomst till datamängdens distribution. Denna egenskap kan upprepas när åtkomst är möjligt via flera tjänster. |
| | temporalResolution | dcat:temporalResolution | Den minsta tidsperiod som går att urskilja i datamängdens distribution. Till exempel P1Y som indikerar att data har en upplösning per år. |
| | spatialResolutionInMeters | dcat:spatialResolutionInMeters | Den minsta rumsliga upplösning som går att urskilja i datamängdens distribution |
| | byteSize | dcat:byteSize | Distributionens storlek i bytes. |
| | issued | dcterms:issued | Egenskapen anger distributionens utgivningsdatum. |
| | modified | dcterms:modified | Datum för senaste ändring av distributionen. |
| | status | adms:status | Distributionens mognadsgrad. För att se vilka värden man kan skriva in se: [Status - värden](supported_attributes_status.md) |
| | availability | dcatap:availability | Denna egenskap berättar om den planerade tillgängligheten hos distributionen. För att se vilka värden man kan skriva in se: [Tillgänglighet - värden](supported_attributes_availability.md) |
| | licenseurl | dcterms:license | Ett juridiskt dokument som ger officiellt tillstånd att göra något med resursen. För att ange en Creative Commons Licens kan följande värden skrivas in: [Licens - Creative Commons - värden](supported_attributes_license.md) |
| | licensedoc | dcterms:license | Hänvisar till vilken licens som distributionen görs tillgänglig om du inte använder en CC licens. Anges som ett Licensdokument, se egen sektion nedan. |
| | rights | dcterms:rights | Egenskapen refererar till en rättighetsförklaring som syftar till att gruppera en mängd egenskaper som upphovsrätt. Anges som en Rättighetsförklaring, se egen sektion nedan. |
| | checksum | spdx:checksum | Denna egenskap kan användas för att kontrollera om innehållet i en distribution har förändrats. Se egen sektion Checksumma nedan. |
| | page | foaf:page | En sida eller ett dokument om denna distribution. Anges som ett Dokument, se egen sektion nedan. |
| | conformsTo | dcterms:conformsTo | Egenskapen hänvisar till ett etablerat schema som den beskrivna distributionen använder. Länk till api-specen läggs i about-fältet för att kunna testköra direkt på dataportalen. Anges som en Standard, se egen sektion nedan. |
| | applicableLegislation | dcatap:applicableLegislation | Lagstiftning som kräver att distributionen skapas och/eller hanteras. Observera, använd endast om den omgivande datamängden har motsvarande märkning. |
|---|---|---|---|
| Datatjänst (Dataservice) | about | - | Identifierare för datatjänsten |
| | title | dcterms:title | Datatjänstens namn. Denna egenskap kan upprepas för parallella språkversioner av namnet. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | description | dcterms:description | Datatjänstens beskrivning. Denna egenskap kan upprepas för parallella språkversioner av beskrivningen. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | endpointURL | dcat:endpointURL | Basadress eller primär startpunkt för datatjänsten (en URL). |
| | endpointDescription | dcat:endpointDescription | Beskriver datatjänstens åtkomstadresser inklusive de operationer och parametrar som stöds. Beskrivningen ger specifika detaljer om de faktiska åtkomstadresserna medans egenskapen dcterms:conformsTo används för att beskriva den generella standard eller specifikation som implementeras av tjänsten. För en geografisk tjänst (WMS eller WFS) pekar man med fördel på getCapabilities anropet, för traditionella web services pekar man på en WSDL fil medans för REST tjänster pekar man på en beskrivning uttryckt enligt Open API specifikationen eller liknande. |
| | publisher | dcterms:publisher | Egenskapen hänvisar till en enhet (organisation) som ansvarar för att göra datatjänsten tillgänglig. Anges som en Aktör, se egen sektion nedan. |
| | contactPoint | dcat:contactPoint | Egenskapen innehåller kontaktuppgifter som kan användas för att flagga fel i datatjänsten eller för att skicka kommentarer. Anges som en Kontaktuppgift, se egen sektion nedan. |
| | type | dcterms:type | Vilken arkitekturstil som tjänsten använder. Använd dcterms:conformsTo för att indikera mer detaljer som exakt vilket protokoll som används. För att se vilka värden man kan skriva in se: [Arkitekturstil - värden](supported_attributes_arcitectureStyle.md) |
| | keyword | dcat:keyword | Egenskapen innehåller ett nyckelord som beskriver datatjänsten. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | theme | dcat:theme | En kategori för datatjänsten. En datatjänst kan associeras med flera teman. För att se vilka värden man kan skriva in se: [Kategori - värden](supported_attributes_theme.md) |
| | conformsTo | dcterms:conformsTo | Egenskapen hänvisar till en tillämpningsföreskrift eller annan specifikation. Länk till api-specen läggs i about-fältet för att kunna testköra direkt på dataportalen. Anges som en Standard, se egen sektion nedan. |
| | servesDataset | dcat:servesDataset | Egenskapen refererar till en eller flera datamängder som denna datatjänst kan tillhandahålla. |
| | licenseurl | dcterms:license | Ett juridiskt dokument som ger officiellt tillstånd att göra något med resursen. För att ange en Creative Commons Licens kan följande värden skrivas in: [Licens - Creative Commons - värden](supported_attributes_license.md) |
| | licensedoc | dcterms:license | Ett juridiskt dokument som ger officiellt tillstånd att göra något med resursen. Anges som ett Licensdokument, se egen sektion nedan. |
| | accessRights | dcterms:accessRights | Den här egenskapen hänvisar till information som indikerar huruvida datamängden/datatjänsten innehåller öppna data, har åtkomstrestriktioner eller inte är offentlig. För att se vilka värden man kan skriva in se: [Åtkomsträttigheter - värden](supported_attributes_accessRights.md) |
| | landingPage | dcat:landingPage | Den här egenskapen hänvisar till en webbsida som ger tillgång till datatjänsten och/eller ytterligare information. Den bör peka på en landningssida hos den ursprungliga dataleverantören och inte till en sida på en webbplats av en tredje part. |
| | page | foaf:page | En sida eller ett dokument för datatjänsten. Anges som ett Dokument, se egen sektion nedan. |
| | format | dcterms:format | Datatjänstens format. |
| | subject | dcterms:subject | Kategorisering motsvarande grunddatadomäner. Se lista med domäner: dataportal.se/sv/terminology/grunddata |
| | applicableLegislation | dcatap:applicableLegislation | Lagstiftning som kräver att datatjänsten skapas och/eller hanteras. |
| | hvdCategory | dcatap:hvdCategory | Kategori för värdefulla datamängder enligt kommissionens genomförandeförordning (EU) 2023/138. Endast obligatoriskt om datatjänsten är klassad som "värdefull datamängd" — annars ska det inte anges alls. |
|---|---|---|---|
| Datamängdsserie (DatasetSeries) | about | - | Identifierare för datamängdsserien |
| | title | dcterms:title | Datamängdsseriens namn. Denna egenskap kan upprepas för parallella språkversioner av namnet. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | description | dcterms:description | Datamängdsseriens beskrivning. Denna egenskap kan upprepas för parallella språkversioner av beskrivningen. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | publisher | dcterms:publisher | Egenskapen hänvisar till en enhet (organisation) som ansvarar för att göra datamängdsserien tillgänglig. Anges som en Aktör, se egen sektion nedan. |
| | contactPoint | dcat:contactPoint | Egenskapen innehåller kontaktuppgifter som kan användas för att flagga fel i datamängdsserien eller för att skicka kommentarer. Anges som en Kontaktuppgift, se egen sektion nedan. |
| | keyword | dcat:keyword | Egenskapen innehåller ett nyckelord som beskriver data i datamängdsserien. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | theme | dcat:theme | En kategori för datat i datamängdsserien. En datamängdsserie kan associeras med flera teman. För att se vilka värden man kan skriva in se: [Kategori - värden](supported_attributes_theme.md) |
| | location | dcterms:spatial | Det geografiska området som omfattas av datat i datamängdsserien. Om det anges på serien ska det motsvara unionen av motsvarande uppgifter på datamängderna i serien. Anges som ett Geografiskt område, se egen sektion nedan. |
| | spatialUrl | dcterms:spatial | Namngivet geografiskt område angivet som URI. Det rekommenderas att använda Geonames URI:er i följande form: <http://sws.geonames.org/6695072> |
| | temporal | dcterms:temporal | Giltighetsperiod för data i datamängdsserien. Om det uttrycks på serien ska det motsvara unionen av motsvarande uttryck på de ingående datamängderna. Anges som en Tidsperiod, se egen sektion nedan. |
| | issued | dcterms:issued | Utgivningsdatum av datat i datamängdsserien. Ska sammanfalla med det tidigaste utgivningsdatumet för datamängderna i serien. |
| | modified | dcterms:modified | Datum för senaste ändring av datat i datamängdsserien. Ska vara samma datum som det senaste modifieringsdatumet hos datamängderna i serien. |
| | landingPage | dcat:landingPage | En webbsida som ger tillgång till datamängdsserien. |
| | conformsTo | dcterms:conformsTo | Egenskapen hänvisar till en tillämpningsföreskrift eller annan specifikation. Anges som en Standard, se egen sektion nedan. |
| | accrualPeriodicity | dcterms:accrualPeriodicity | Avser uppdateringsfrekvensen för när ny data läggs till i serien. |
| | relation | dcterms:relation | Egenskapen pekar ut en relaterad resurs. |
| | qualifiedRelation | dcat:qualifiedRelation | En kvalificerad relation gör det möjligt att peka på en relaterad resurs tillsammans med en roll som förtydligar relationens karaktär. Se egen sektion Kvalificerad relation nedan. |
| | page | foaf:page | En sida eller ett dokument om denna datamängdsserie. Anges som ett Dokument, se egen sektion nedan. |
| | subject | dcterms:subject | Kategorisering motsvarande grunddatadomäner. Se lista med domäner: dataportal.se/sv/terminology/grunddata |
| | applicableLegislation | dcatap:applicableLegislation | Lagstiftning som kräver att datamängdsserien skapas och/eller hanteras. |
| | hvdCategory | dcatap:hvdCategory | Kategori för värdefulla datamängder enligt kommissionens genomförandeförordning (EU) 2023/138. Endast obligatoriskt om serien är klassad som "värdefull datamängd" — annars ska det inte anges alls. |

## Återanvända strukturer

Nedanstående strukturer används av flera primärklasser. Kolumnen Metadata anger fältnamnen inuti respektive struktur.

| Struktur | Metadata | Property | Beskrivning |
|:---|:---|:---|:---|
| Aktör (publisher / creator / copyrightHolder) | about | - | Identifierare för aktören |
| | name | foaf:name | Ett namn på en person eller organisation. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | type | dcterms:type | Resursens natur eller genre. För att se vilka värden man kan skriva in se: [Aktör - Typ - värden](supported_attributes_type.md) |
| | homepage | foaf:homepage | En hemsida för en person eller organisation. |
| | mbox | foaf:mbox | En mailadress som används av en person eller en funktionsadress inom en organisation. |
| | description | dcterms:description | En textuell beskrivning av aktören. |
| | identifier | dcterms:identifier | Aktörens huvudsakliga identifierare, t.ex. för en svensk organisation bör organisationsnumret anges. |
| | sameAs | owl:sameAs | En känd alternativ URI för denna aktör. |
| | classification | org:classification | Klassificering av aktör, t.ex. om man är reglerad att ta ut avgifter eller inte. |
|---|---|---|---|
| Övrig aktör (other agent) | about | - | Identifierare för aktören |
| | name | foaf:name | Ett namn på en person eller organisation. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | type | dcterms:type | Resursens natur eller genre. För att se vilka värden man kan skriva in se: [Aktör - Typ - värden](supported_attributes_type.md) |
| | homepage | foaf:homepage | En hemsida för en person eller organisation. |
| | mbox | foaf:mbox | En mailadress som används av en person eller en funktionsadress inom en organisation. |
| | role | dcat:hadRole | Ansvarig parts funktion. För att se vilka roller man kan skriva in se: [Roll - värden](supported_attributes_userRole.md) |
| | description | dcterms:description | En textuell beskrivning av aktören. |
| | identifier | dcterms:identifier | Aktörens huvudsakliga identifierare, t.ex. för en svensk organisation bör organisationsnumret anges. |
| | sameAs | owl:sameAs | En känd alternativ URI för denna aktör. |
| | classification | org:classification | Klassificering av aktör, t.ex. om man är reglerad att ta ut avgifter eller inte. |
|---|---|---|---|
| Kontaktuppgift (contactPoint) | about | - | Identifierare för kontaktuppgiften |
| | type | rdf:type | Egenskapen anger om kontaktuppgiften motsvarar en individ eller organisation. För att se vilka värden man kan skriva in se: [Kontaktuppgift - Typ - värden](supported_attributes_contactType.md) |
| | name | vcard:fn | En text som motsvarar namnet på kontaktuppgiften. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | email | vcard:hasEmail | En mailadress som används av en person eller en funktionsadress inom en organisation. |
| | phone | vcard:hasTelephone | Ett telefonnummer ska ges inklusive landskod, t.ex. +4618100000. |
| | address | vcard:hasAddress | För att specificera en adress för kontaktuppgiften. |
| | address | vcard:hasAddress | Adress för kontaktuppgiften. Anges som fyra värden separerade med semikolon i ordningen gatuadress; postnummer; postort; land, t.ex. `Storgatan 5; 85230; Sundsvall; Sverige` |
| | url | vcard:hasURL | En webbadress som tillhandahåller mer information om en person eller en funktionsadress inom en organisation. |
|---|---|---|---|
| Geografiskt område (location) | about | - | Identifierare för det geografiska området |
| | centroid | dcat:centroid | Centrumkoordinat. |
| | bbox | dcat:bbox | Omskrivande rektangel. |
| | geometry | locn:geometry | Geografisk form. |
|---|---|---|---|
| Tidsperiod (temporal) | startDate | dcat:startDate | Startdatum för den period då resursen innehåller data. |
| | endDate | dcat:endDate | Slutdatum för den period då resursen innehåller data. |
|---|---|---|---|
| Rättighetsförklaring (rights) | attributionText | odrs:attributionText | Texten som ska användas i en erkännandetext. Detta kan vara namnet på utgivaren eller en hänvisning till ett gemenskap eller en grupp av bidragare. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | attributionURL | odrs:attributionURL | Länken (URL) som ska användas när en erkänner källa till data. URL:en kan bli refererad till datamängden eller publicistens hemsida, men kan också vara en hemsida dedicerad för erkännanden. Det här är användbart när en tillhandahåller vidare erkännande till källor uppströms. |
| | copyrightNotice | odrs:copyrightNotice | Meddelandet om upphovsrätt som associeras med en rättighetsförklaring. Ett sådant meddelande bör typiskt anges i samband med att man anger datas ursprung. Denna egenskap är uttryckt som en kortare oformaterad text och är därför lämplig för enkla notiser om upphovsrätt. När en datapublicist behöver referera ett större upphovsrättsligt yttrande och/eller relaterad vägledning så bör egenskapen copyrightStatement användas istället. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | copyrightStatement | odrs:copyrightStatement | En länk (URL) till ett dokument som inkluderar ett yttrande om upphovsrättsstatusen av innehållet för en datamängd. Webbsidan kan inkludera både ett meddelande om upphovsrätt och vägledning för de som tänkt vidareutnyttja en datamängd. Anges som ett Licensdokument, se nedan. |
| | copyrightYear | odrs:copyrightYear | Det år från när upphovsrätt över innehållet av datamängden gäller. |
| | copyrightHolder | odrs:copyrightHolder | En referens till den organisation som har upphovsrätt över innehållet av datamängden. Anges som en Aktör, se ovan. |
| | jurisdiction | odrs:jurisdiction | En referens (URI) till den jurisdiktionen där upphovsrätt och/eller databasrättigheter hävdas. Det är rekommenderat att referera till ett land eller region (via en URI). |
| | reuserGuidelines | odrs:reuserGuidelines | Länk (URL) till ett dokument som tillhandahåller riktlinjer för återanvändare av data som omfattas av en specifik rättighetsförklaring. Riktlinjerna kan inkludera fler detaljer om riktlinjer för erkännande, en mer komplett text om upphovsrätten, och generell vägledning om hur datan kan bli återanvänd |
|---|---|---|---|
| Licensdokument (licensedoc / copyrightStatement) | about | - | Identifierare för licensdokumentet |
| | title | dcterms:title | Ett namn för licensdokumentet. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | description | dcterms:description | En beskrivning av licensdokumentet. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
|---|---|---|---|
| Dokument (page) | about | - | Identifierare för dokumentet |
| | title | dcterms:title | Ett namn för dokumentet. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | description | dcterms:description | En beskrivning av dokumentet. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
|---|---|---|---|
| Standard (conformsTo) | about | - | Identifierare för standarden |
| | title | dcterms:title | Ett namn för standarden. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | description | dcterms:description | En beskrivning av standarden. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
|---|---|---|---|
| Checksumma (checksum) | value | spdx:checksumValue | Ett hexadecimalt digest-värde i gemener som genereras med hjälp av en särskild algoritm. |
|---|---|---|---|
| Avgift (offers) | description | schema:description | Här informerar du om avgift, beräkningsgrund och annan relevant information. För att ange språk se [språk som stöds](supported_attributes_languages.md) |
| | page | schema:mainEntityOfPage | En webbsida där information om avgift och beräkningsgrund beskrivs i mer detalj. |
|---|---|---|---|
| Kvalificerad relation (qualifiedRelation) | role | dcat:hadRole | Anger på vilket sätt (vilken roll) resursen är relaterad till en annan resurs. För att se vilka värden man kan skriva in se: [Resursroll - värden](supported_attributes_resourceRole.md) |
| | relation | dcterms:relation | Den relaterade resursen. |
