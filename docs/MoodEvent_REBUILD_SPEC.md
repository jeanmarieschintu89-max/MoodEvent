# MoodEvent - Refonte propre des mini-jeux

Ce document fixe les objectifs de génération et de gameplay pour reconstruire MoodEvent proprement, sans empiler des correctifs qui se contredisent.

## Règle principale

Chaque mini-jeu doit avoir son builder dédié. Aucun système global ne doit repasser après génération pour corriger visuellement les lignes, les sas ou les plateformes.

À supprimer/neutraliser :

- correcteurs visuels globaux de type `GeneratedLineMarkerTask`
- anciens podiums de départ/arrivée
- anciens sas internes
- logique partagée qui modifie plusieurs jeux sans distinction

## Architecture voulue

- `GeneratedGameManager` doit seulement router vers le bon builder.
- Chaque jeu doit générer sa propre structure complète.
- Chaque jeu doit définir sa région exacte de backup/restauration.
- Chaque jeu doit définir son départ et son arrivée.
- Chaque jeu doit être autonome après lancement.

## Cycle commun de jeu

1. Admin génère un pack événement.
2. Le pack crée la salle d’attente et le mini-jeu.
3. Admin ouvre la file.
4. Joueurs rejoignent avec `/event`.
5. Admin ferme la file ou le lancement auto part.
6. Joueurs vont en salle d’attente.
7. Joueurs sont téléportés au départ.
8. Tampon de départ : `3`, `2`, `1`, `GO`.
9. Le jeu gère sa propre logique jusqu’à la fin.
10. Tous les joueurs sont renvoyés à leur position sauvegardée.
11. La structure est restaurée sans laisser de morceaux.

## Contraintes globales

- Aucun joueur ne doit mourir à la fin d’un event.
- Aucun joueur ne doit rester coincé dans la structure.
- La salle d’attente doit être protégée contre casse et pose.
- Un seul jeu actif à la fois.
- SquidCraft ne doit jamais se déclencher lorsqu’un autre jeu démarre.
- Effondrement/Tour Infernale ne doit jamais casser SquidCraft.
- Les messages doivent suivre les nouveaux noms.
- Les générations doivent éviter les pics de RAM.

## Jeux et objectifs

### Labyrinthe

Nom : `Labyrinthe`

Objectif : trouver la sortie rouge.

Génération attendue :

- vrai labyrinthe en pierre/gris
- murs hauts
- entrée aléatoire côté gauche
- sortie aléatoire côté droit
- un seul sas vert extérieur
- un seul sas rouge extérieur
- aucun podium vert ou rouge à l’intérieur
- couloir fermé entre sas et labyrinthe
- arrivée dans le sas rouge

À éviter absolument :

- deux sas verts
- deux sas rouges
- podiums internes
- gros blocs de lumière au milieu du passage
- morceaux qui restent après restauration

### Mur d’escalade

Ancien type technique : `JUMP`

Nom affiché : `Mur d’escalade`

Objectif : grimper jusqu’à la dernière plateforme rouge.

Génération attendue :

- cage vitrée autour
- départ propre au sol dans la cage
- premier saut accessible
- parcours en hauteur dans toute la cage, pas seulement au centre
- plateformes variées mais faisables
- obstacles : laine, bois, barrières, échelles, glace, slime, soul sand, magma
- dernière plateforme rouge = arrivée
- aucun podium final séparé
- aucun sas de fin
- aucun bloc hors cage

À éviter absolument :

- arrivée inaccessible
- grand plateau orange/blanc
- ancien podium au départ ou à l’arrivée
- saut impossible dès le départ
- correction visuelle externe

### Course

Nom : `Course`

Objectif : atteindre la ligne rouge.

Génération attendue :

- piste droite propre
- vraie ligne verte au sol au départ
- vraie ligne rouge au sol à l’arrivée
- obstacles variés mais faisables
- obstacles possibles : foin, dalles, barrières avec passage, zigzags, petits murs bas, tapis/repères
- aucun podium au départ ou à l’arrivée

À éviter absolument :

- vieux podium vert/rouge
- arrivée surélevée
- départ surélevé
- piste sans challenge

### Water Jump

Nom : `Water Jump`

Objectif : traverser les plateformes au-dessus de l’eau sans tomber.

Génération attendue :

- bassin d’eau propre
- laine constante, pas de styles qui remplacent la laine
- vraie ligne verte au départ
- vraie ligne rouge à l’arrivée
- plateformes avec un peu de hauteur/variation
- chute dans l’eau = retour au départ

À éviter absolument :

- deux lignes d’eau parasites
- blocs lumineux suspendus au départ
- podium d’arrivée
- parcours trop plat
- styles qui changent la laine

### Tour Infernale

Ancien type technique : `SURVIE_ETAGES`

Nom affiché : `Tour Infernale`

Objectif : survivre pendant que les blocs de sol disparaissent.

Génération attendue :

- cage/tour intacte
- blocs de sol intérieurs uniquement destructibles
- destruction progressive, pas trop rapide
- dernier survivant gagne
- la cage ne doit jamais être détruite

À éviter absolument :

- cage cassée
- sol qui disparaît trop vite
- interaction avec SquidCraft
- déclenchement pendant un autre jeu

### Mine en folie

Ancien type technique : `RUEE_OR`

Nom affiché : `Mine en folie`

Objectif : miner un maximum de minerais dans le temps donné.

Génération attendue :

- mine fermée
- pioche événement obligatoire
- vision nocturne pendant l’épreuve
- timer automatique
- minerais gardés par les joueurs
- fin auto + restauration

À éviter absolument :

- pioche normale autorisée
- night vision oubliée
- joueurs non renvoyés
- récompenses d’économie parasites si le mode ne doit pas en avoir

### SquidCraft

Nom : `SquidCraft`

Objectif : réussir une suite d’épreuves.

Génération attendue :

- pack séparé des autres mini-jeux
- dortoir fermé
- sas court
- Feu Rouge / Feu Vert avec poupée et feux visuels
- ligne départ verte au sol
- ligne arrivée rouge au sol
- retour dortoir après la poupée
- pause dortoir 1 minute
- pont de verre
- mauvaise vitre : vitre casse, joueur voit la chute, retour dortoir
- dernier gagnant seulement en vraie partie

À éviter absolument :

- SquidCraft déclenché par un autre jeu
- Effondrement qui casse SquidCraft
- dortoir ouvert
- pont qui TP instantanément
- victoire solo instantanée en test

## Noms affichés actuels

- Labyrinthe
- Mur d’escalade
- Course
- Water Jump
- Tour Infernale
- Mine en folie
- SquidCraft

## Plan de refonte

1. Geler les anciens correcteurs globaux.
2. Garder `GeneratedGameManager` comme routeur uniquement.
3. Refaire chaque builder indépendamment.
4. Supprimer les anciens helpers inutilisés.
5. Vérifier les menus et libellés.
6. Vérifier les régions de backup/restauration.
7. Tester chaque jeu en Petit puis Moyen avant Grand/Géant.
