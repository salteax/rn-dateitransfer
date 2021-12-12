# Test des Belegs

## Lokales Testscript
Mittels des bereitgestellten [Testskripts](test_beleg_lokal.sh) kann die formale Erfüllung der Abgabekriterien getestet werden.

## Testserver der HTW
Nachfolgende Server stehen zum Testen Ihres Programms zur Verfügung. Die Server erlauben jeweils nur einen Nutzer. Beschränken Sie deshalb die zeitliche Nutzung aus Rücksicht auf Ihre Kommilitonen.

1. Auf `idefix Port 3330` läuft zum Testen Ihres Clients ein Server mit den Parametern: Delay: 0 ms, Packet Loss: 0,0
   * Die allgemeinen Ausgaben des Servers erhalten Sie über: http://idefix.informatik.htw-dresden.de/rn-server0
   * Debugausgaben des Servers erhalten Sie über: http://idefix.informatik.htw-dresden.de/rn-server0-debug
2. Auf `idefix Port 3333` läuft zum Testen Ihres Clients ein Server mit den Parametern: Delay: 10 ms, Packet Loss: 0,1
   * Die allgemeinen Ausgaben des Servers erhalten Sie über: http://idefix.informatik.htw-dresden.de/rn-server1
   * Debugausgaben des Servers erhalten Sie über: http://idefix.informatik.htw-dresden.de/rn-server1-debug
3. Auf `idefix Port 3340` läuft zum Testen Ihres Clients+Servers ein Proxy mit den Parametern: Delay: 10 ms, Loss: 0,1
   * Ihr Client und Server müssen hierzu zwingend über dieselbe IP-Adresse erreichbar sein
   * Ihr Server muss auf Port 3400 empfangsbereit sein
