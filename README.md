# The Impossible Penalty

2D-Elfmeterspiel in Java mit einem lernenden Torwart, der sich an deine Schussmuster anpasst.

Entstanden als Einzelprojekt im Modul Objektorientierte Softwareentwicklung an der Hochschule RheinMain (WiSe 2025/26).

<img width="959" height="502" alt="image" src="https://github.com/user-attachments/assets/79dbe10e-9abc-4a8a-a25c-d1e0d72eb0b7" />


## Features

- *Lernende Torwart-KI:* Der Torwart wertet die letzten 12 Schüsse gewichtet aus, erkennt Muster und sagt die Schussrichtung voraus.
- *Zwei Schwierigkeitsgrade:* Im schweren Modus täuscht der Torwart zusätzlich Bewegungen an.
- *Ballphysik:* Vier Schussarten, u. a. Kurvenschüsse und Lupfer.
- *Flugbahnvorschau* beim Zielen.


## Steuerung

- *Zielen:* Maus bewegen oder Pfeiltasten ← →
- *Schießen:* Leertaste zum Aufladen, erneut tippen zum Schuss
- *Schussart:* 1 = Normal, 2 = Drall links, 3 = Drall rechts, 4 = Lupfer
- *Schwierigkeit:* N = Normal, I = Impossible
- *Neustart:* R

*Ziel:* 5 Tore in Folge erzielen.

## Technik

- Java, objektorientierter Aufbau
- Spielidee, Design und Balancing eigenständig entwickelt

## Ausführen

Das Projekt basiert auf dem Game2D-Framework aus der Vorlesung von Prof. Panitz. Das Framework ist *nicht* in diesem Repository enthalten, da es nicht von mir stammt. Zum Ausführen muss es im Package name.panitz.game2d ergänzt werden.
