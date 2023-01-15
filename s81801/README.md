# Rechnernetze - Dateitransfer

#### Paul Koreng | s81801@informatik.htw-dresden.de
---

## Struktur
1. [Server ohne Fehlersimulation](#Server-ohne-Fehlersimulation)
2. [Server mit Fehlersimulation](#Server-mit-Fehlersimulation)
3. [Server über Hochschulproxy](#Server-über-Hochschulproxy)
4. [Hochschulserver ohne Fehlersimulation](#Hochschulserver-ohne-Fehlersimulation)
5. [Hochschulserver mit Fehlersimulation](#Hochschulserver-mit-Fehlersimulation)
5. [Funktionalität](#Funktionalität)
---

## Server ohne Fehlersimulation
Minimal speed: 8.70887kb/s\
Maximal speed: 123.59295kb/s\
Average speed: 53.05102kb/s

## Server mit Fehlersimulation
Getestet mit den gegeben Werten:\
lossRate: 0.1, delay: 150

Minimal speed: 7.48221kb/s\
Maximal speed: 133.71089kb/s\
Average speed: 50.14202kb/s

## Server über Hochschulproxy
## Hochschulserver ohne Fehlersimulation
## Hochschulserver mit Fehlersimulation
Konnte ich nicht richtig austesten, da ich mich nicht auf den Hochschulserver einloggen konnte und das mit dem Proxy auf meinem Rechner nicht funktioniert hat.

## Funktionalität
Beim einmaligen senden mit und ohne Fehlersimulation funktioniert alles aber beim offen lassen des Server und nochmaligem senden einer Datei funktioniert die CRC32 Checksumme nicht mehr da auf die letzten 8 Bit im letzten Datenpaket nicht richtig gelesen werden und somit die CRC32 Checksumme immer unterschiedlich ist (leider habe ich den Fehler auch nicht gefunden).