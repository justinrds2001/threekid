# Logboek

## Tijd

| Stap | Datum | Tijd |
| --- | --- | --- |
| 1. Opzet | 2026-11-22 | 20 min |
| 2. Domein en matcher | 2026-11-22 | 15 min |
| 3. Opslag en upsert | 2026-11-22 | 15 min |
| 4. Bidirectionele integriteit | 2026-11-22 | 30 min |
| 5. REST-endpoint | 2026-11-22 | 20 min |
| 6. Performance | | |
| 7. Fase 2 | | |
| 8. Afronden | | |
| **Totaal** | | |

## Vragen en keuzes

Per open punt in de opdracht: de vraag, de opties die ik heb overwogen, mijn keuze en waarom.

| # | Vraag | Overwogen | Keuze | Waarom |
| --- | --- | --- | --- | --- |
| 1 | Telt een kind zonder geboortedatum als jonger dan 18? | Nee, of ja | Nee | Een match moet aantoonbaar kloppen. Zonder geboortedatum is niet te bewijzen dat het kind jonger is dan 18. |
| 2 | Kan een placeholder matchen? | Ja, of nee met een placeholder-vlag | Ja | Als andere records hem een partner en kinderen geven, voldoet hij aan het patroon. Het kost geen extra code. |
| 3 | Tellen kinderen die alleen vanuit het kind-record verwijzen mee? | Ja na reparatie, of alleen wat de ouder zelf noemt | Ja | Na de reparatie staat de link aan beide kanten. Welk record hem noemde maakt dan niet meer uit. |
| 4 | Is partner wederzijds? | Ja met reparatie, of alleen wat iemand zelf opgeeft | Ja, een eerdere partner verliest de link | Het binnenkomende record is de waarheid over zijn eigen relaties. De andere kant wordt daarop aangepast. |
| 5 | Een derde ouder claimt een kind dat al 2 ouders heeft | De laatste schrijfactie wint, of weigeren met `409` | Weigeren met `409` | Bij de laatste schrijfactie moet een van de twee ouders weg, en welke is willekeurig. Weigeren is expliciet en er gaat geen data stil verloren. |
| 6 | Een update laat een kind weg | De ouderlink bij het kind laten staan, of ook weghalen | Ook weghalen | Zelfde principe als bij 4. Dit geldt ook voor een weggelaten ouder of partner. |
| 7 | Iemand is zijn eigen kind, ouder of partner, dubbele kinderen, ontbrekend `id` | Alles in de web-laag, of splitsen | `400`, gesplitst | Zelfverwijzing en maximaal 2 ouders zijn regels van het domein. Dubbele kinderen en een ontbrekend `id` zijn invoerfouten en worden gevalideerd op de DTO. |
| 8 | Peildatum voor "jonger dan 18" | Vandaag, of een datum in het request | Vandaag via een `Clock`-bean | Het request heeft geen peildatum. Met een `Clock` kunnen tests de datum vastzetten. |
| 9 | Opslag | In-memory, of een database | In-memory, data verdwijnt bij een herstart | Past bij de omvang van de casus. De port maakt een database later een losse adapter. |
| 10 | Body bij `444` | Leeg, of een melding | Leeg | `444` zegt al dat er geen match is. Een body voegt niets toe. |
| 11 | Onbekende velden in de response | Als `null` tonen, of weglaten | Als `null` tonen | Elke persoon heeft zo dezelfde vorm, en "onbekend" is expliciet. |
| 12 | Body bij `400` en `409` | `ProblemDetail` (RFC 9457), of leeg | `ProblemDetail` | Standaard foutformaat dat in Spring zit. De client ziet waarom het request is geweigerd. |
| 13 | `parent1` en `parent2` met hetzelfde id | `400`, of toestaan als één ouder | `400` | Zelfde soort fout als dubbele kinderen. Anders verdwijnt er stil een ouder. |
| 14 | Hoe meten we een POST met 10.000 personen? | JUnit-test met een eigen Gradle-taak, JMH, of een loadtool zoals `wrk` | JUnit-test, `./gradlew benchmark` | Draait zonder extra installatie en is reproduceerbaar. Genoeg om voor en na te vergelijken. JMH is nauwkeuriger, maar veel opzet voor deze casus. |
