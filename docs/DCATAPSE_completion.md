| Statuskod | Kommentar |
|---|---|
|1|Fullt fungerande|
|2|Ev fel datatyp i RDF|
|3|Går igenom Converter men inte RDFWriter|
|4|Inte implementerat|
|5|Fungerar i verktyget, men går inte att validera hos Dataportalen|


| Katalog | | Status |
|-------|-------|-------|
|Obligatoriska|dcterms:title| 1 |
| | dcterms:description | 1 |
| | dcterms:publisher | 1 |
| | dcterms:license | 1 |
| | dcat:dataset | 1 |
| | | |
| Rekommenderade | dcterms:issued | 1 |
| | dcterms:language | 1 |
| | dcterms:modified | 1 |
| | foaf:homepage | 1 |
| | dcterms:spatial | 5 |
| | dcat:themeTaxonomy | 1 |
| | | |
| Valfri | dcterms:rights | 1 |
| | dcterms:hasPart | 1 |
| | dcterms:isPartOf | 1 |
| | dcat:catalog | 1 |
| | dcat:service | 1 |
| | | |
| DataSet | | |
| Obligatoriska | dcterms:title | 1 |
| | dcterms:description | 1 |
| | dcterms:publisher | 1 |
| | | |
| Rekommenderade | dcterms:issued | 1 |
| | dcat:contactPoint | 1 |
| | dcterms:temporal | 1 |
| | dcterms:spatial | 5 Geografisk värdemängd | |
| | dcat:theme | 1 |
| | dcat:keyword | 1 |
| | dcterms:accessRights | 1 |
| | dcterms:spatial | 1 Weblänk |
| | | |
| Valfri | dcterms:creator | 1 |
| | prov:qualifiedAttribution | 1 |
| | dcterms:identifier | 1 |
| | adms:identifier | 1 |
| | dcterms:modified | 1 |
| | dcterms:language | 1 |
| | dcat:landingPage | 1 |
| | dcterms:conformsTo | 1 |
| | dcat:temporalResolution | 1 |
| | dcat:spatialResolutionInMeters | 1 |
| | dcterms:accrualPeriodicity | 1|
| | owl:versionInfo | 1 |
| | adms:versionNotes | 1 |
| | dcterms:source | 1 |
| | schema:offers | 1 |
| | dcat:hasVersion | 1 |
| | dcat:isVersionOf | 1 |
| | dcterms:isReferencedBy | 1 |
| | dcterms:relation | 1 |
| | dcat:qualifiedRelation | 1 |
| | foaf:page | 1 |
| | dcterms:provenance | 1 |
| | dcat:distribution | 1 |
| | | |
| Distribution | |
| Obligatoriska:|dcat:accessURL | 1 |
| | | |
| Rekommenderade | dcterms:license | 1 |
| | dcatap:availability | 1 |
| | dcterms:format|Vanliga mediatyper | 1 |
| | dcterms:description | 1 |
| | dcterms:format|Övriga mediatyper | 1 |
| | dcterms:license|License Document | 1 |
| | | |
| Valfri | dcterms:title | 1 |
| | dcat:downloadURL | 1 |
| | dcterms:format| 1 Geografiska mediatyper||
| | dcat:accessService | 1 |
| | dcat:temporalResolution | 1 |
| | dcat:spatialResolutionInMeters | 1 |
| | dcat:byteSize | 1 |
| | dcterms:language | 1 |
| | dcterms:issued | 1 |
| | dcterms:modified | 1 |
| | adms:status | 1 |
| | dcterms:rights | 1 |
| | spdx:checksum | 1 |
| | foaf:page | 1 |
| | dcterms:conformsTo | 1 |
| | | |
| DataService | |
| Obligatoriska | dcterms:title | 1 |
| | dcat:endpointURL | 1 |
| | | |
| Rekommenderade | dcat:endpointDescription | 1 |
| | dcat:contactPoint | 1 |
| | dcat:keyword | 1 |
| | dcterms:license | 1 |
| | dcterms:accessRights | 1 |
| | | |
| Valfri | dcterms:description | 1 |
| | dcterms:publisher | 1 |
| | dcterms:type | 1 |
| | dcat:theme | 1 |
| | dcterms:conformsTo | 1 |
| | dcat:servesDataset | 1 |
| | dcat:landingPage | 1 |
| | foaf:page | 1 |
| | | |
| Aktör | ||
| Obligatoriska | foaf:name | 1 |
| | dcterms:type | 1 |
 | |||
| Valfri | foaf:homepage | 1 |
| | foaf:mbox | 1 |
| | | |
| Organization | ||
| Obligatoriska | rdf:type | 1 |
| | vcard:fn | 1 |
| | vcard:hasEmail | 1 |
| | | |
| Rekommenderade | vcard:hasTelephone | 1 |
| | vcard:hasAddress | 1 |