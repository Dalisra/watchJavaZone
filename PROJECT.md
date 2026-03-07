# Watch JavaZone

Nettside, Watch JavaZone
Gjennom årene har javaBin akkumulert en rekke videoer av innhold levert på JavaZone. Dette innholdet ligger i dag i Vimeo, og oppleves i stor grad som vanskelig tilgjengelig, rett og slett fordi det ikke er på YouTube. Diskusjon om kopiering til YouTube går så smått, men vi kan fortsatt lage en god løsning.

Informasjon om innhold ligger i dag i MoreSleep (sleepingpill.javazone.no), og inneholder informajson om temaet som presenteres, hvem som presenterer og lenke til Videmo. Dette kan indekseres (det finnes allerede noe kode her) og gjøres søkbar med mulighet for å se videoene i en egen visning (f.eks. watch.javazone.no) hos oss. Vi kan da bruke indekseringen til å vise andre relevante videoer osv, rett og slett lage et kaninhull med masse godt innhold myntet på primært et norsk utviklermiljø.

## API som inneholder data (sleepingpill.javazone.no) er beskrevet her:

Koden ligger her og Readme filen inneholder alt som trengs å vite: https://github.com/javaBin/moresleep

Eksempel på å hente ut en liste over alle konferanser:
https://sleepingpill.javazone.no/public/allSessions

Eksempel på å hente ut en sesjons data fra enn konferanse:
https://sleepingpill.javazone.no/public/allSessions/javazone_2017


# Ting som må ivaretas:

# 1. Lage en youtube lignende nettside som viser videoer fra JavaZone.
# 2. Tracke hvem som søker på hva, og hva folk ser på mest, hvilke temaer er mest populære og andre metrikker.
# 3. Samle data om hva folk liker å se og basert på det foreslå neste video eller tilsvarende videoer.

# Teknologivalg:

# Frontend: React vs Vuejs?
# Backend: Micronaut, Java, Gradle
# Database: PostgreSQL hos Supabase?
# Hosting: Foreløpig alt lokalt.


