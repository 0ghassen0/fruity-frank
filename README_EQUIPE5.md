# Fruity Frank — PLE 2026, Équipe 5


## Aperçu du projet

Le projet se déroule en deux phases :

1. **Phase 1 — Le moteur générique.** Un moteur de jeu fondé sur l'architecture **MVCB** (Model-View-Controller-Bot), capable d'exécuter des automates finis (FSM) décrits en GAL pour piloter les entités.
2. **Phase 2 — Le jeu Fruity Frank.** Instanciation du moteur pour produire un jeu complet, jouable, sonorisé et configurable.

La particularité de ce rendu : **le comportement de chaque entité dynamique (monstres, pommes, fruits, spawner) est décrit dans un fichier `.gal`**, parsé par le parseur fourni par les enseignants, puis traduit en automate exécutable par un compilateur maison (`GalCompiler`). Aucun comportement n'est codé « en dur » : changer un `.gal` change le jeu sans recompiler.

---

## Règles du jeu

### Objectif

Ramasser **tous les fruits du niveau** sans se faire toucher par les monstres. Quand le compteur de fruits restants atteint zéro, le niveau est **gagné**.

### Le décor : une pelouse à deux couches

Le monde est entièrement recouvert de pelouse au départ. Chaque cellule contient deux couches superposées :

| Couche | Épaisseur | Position dans la cellule | Destruction |
| --- | --- | --- | --- |
| Pelouse épaisse (`GrassHigh`) | 9 cm | Partie haute | Détruite par tout déplacement la traversant |
| Pelouse fine (`GrassLow`) | 1 cm | Partie basse | Détruite uniquement en déplacement vertical |

Le monde est **torique horizontalement** (sortir à droite ramène à gauche) et **borné verticalement** (murs en haut et en bas).

### Creuser les galeries

- Frank creuse en se déplaçant : il transforme la pelouse en galerie.
- En déplacement **horizontal**, il ne détruit que la pelouse épaisse (la fine reste comme « sol » de la galerie).
- En déplacement **vertical**, il détruit les deux couches.
- Les monstres **creuseurs** creusent eux aussi ; les **non-creuseurs** sont confinés aux galeries déjà ouvertes.

### Les fruits et les cerises

Collectés par simple passage de Frank dessus. Chaque collecte augmente le score et **diminue le compteur de fruits restants**. La cerise, en plus, **recharge le projectile** de Frank.

### Les pommes

- **Poussée** : Frank pousse une pomme horizontalement, case par case, tant que la case derrière la pomme est libre (vide ou pelouse). Elle se bloque contre un mur, un fruit, un ennemi ou une autre pomme.
- **Chute** : une pomme tombe lorsqu'elle n'est plus soutenue (la cellule du dessous ne contient plus de pelouse épaisse), après un court délai. En tombant, elle **écrase la pelouse fine**, **tue Frank** (perte de vie) ou **détruit un monstre**, et s'arrête sur un obstacle solide.

La chute des pommes est pilotée par l'automate `AppleFall.gal`.

### Le projectile

Frank dispose d'**un seul projectile** à la fois. Il le récupère en collectant une cerise, et le lance avec `ESPACE` dans la direction où il regarde. Le projectile **détruit le premier monstre touché** puis disparaît, ou se détruit au contact d'un obstacle. Il faut récupérer une nouvelle cerise pour pouvoir tirer à nouveau.

### Vies, mort et fin de partie

Frank dispose de **3 vies** (réglable). Après une mort, il réapparaît temporairement **invincible** pour éviter une mort en chaîne. La partie se termine en **game over** (plus de vies) ou en **victoire** (tous les fruits ramassés).

---

## Commandes et menus

| Touche | Action |
| --- | --- |
| `↑` `↓` `←` `→` | Déplacer Frank (et creuser) |
| `ESPACE` | Tirer le projectile-cerise (si disponible) |
| `ÉCHAP` | Mettre en pause / reprendre |
| `F3` | Mode debug (hitboxes, états des automates) |

Le jeu comporte plusieurs écrans gérés par `FrankController` :

- **Écran de démarrage** (`fruity-frank_start.png`)
- **Menu pause** (`echap_menu.png`) avec *Resume*, *Restart*, *Exit*
- **Écran de game over** (`game-over_restart_exit.png`) avec *Restart* / *Exit*
- **Écran de victoire** (`victory_restart_exit.png`) avec *Restart* / *Exit*

Les boutons sont cliquables à la souris.

---

## Architecture MVCB

```
CLAVIER / SOURIS
       │
       ▼
┌────────────────┐
│ FrankController│  capte les entrées, gère les menus, transmet au Bot du joueur
└────────────────┘
       │
       ▼
┌────────────┐
│    BOT     │  décide de l'action :
│            │    • FrankBot → clavier (Frank)
│            │    • GALBot   → automate GAL (monstres, pommes, fruits, spawner)
└────────────┘
       │ pilote
       ▼
┌──────────────────┐
│      MODEL        │  état du jeu : entités, physique, règles de collision
│ FruityFrankModel  │  ne connaît NI l'affichage NI les entrées
│   (+ Entités)     │
└──────────────────┘
       │ lu par
       ▼
┌────────────┐
│    VIEW    │  lit le Model et le dessine ; ne le modifie jamais
│ FrankView  │  (caméra suiveuse, avatars, HUD)
└────────────┘
       │
       ▼
     ÉCRAN
```

Le **Model** est le cœur logique et ignore tout de l'affichage et du clavier. La **View** observe le Model et le dessine sans jamais le modifier. Cette séparation permet de faire évoluer indépendamment la logique et le rendu.

### Répartition des classes

| Brique | Classes |
| --- | --- |
| **Model** | `FruityFrankModel`, `Rules`, `GameState`, `FruityFrankMap` |
| **Entités** | `Frank`, `Enemy`, `Fruit`, `Apple`, `Cherry`, `CherryProjectile`, `GrassHigh`, `GrassLow`, `EnemySpawnerEntity` |
| **View** | `FrankView`, `FrankAvatar`, `FrankPainter`, `FrankSprites`, `Palette` |
| **Controller** | `FrankController` (entrées, menus, pause, restart) |
| **Bot / IA** | `FrankBot` (joueur), `GALBot` + automates `.gal`, `FrankCategories` |
| **Pont GAL** | `GalCompiler` + actions/conditions dans `frank.gal.aut.*` |
| **Audio** | `FrankAudio` |
| **Configuration** | `FruityFrank`, `Category`, `Categorized`, `GameConfig` |
| **Assemblage** | `GameApp` (point d'entrée) |

---

## Le pont GAL : des fichiers `.gal` aux automates exécutables

C'est la pièce maîtresse du rendu. Le flux complet est :

```
fichier .gal
     │
     ▼  Parser.from_file()  (parseur fourni par les enseignants)
AST  (gal.ast.Automaton, Mode, Transition, Condition, FunCall…)
     │
     ▼  GalCompiler.compile()
Automaton exécutable  (engine.gal.aut.Automaton)
     │
     ▼  new GALBot(entité, automaton, vitesse, catégories)
Bot qui pilote l'entité à chaque tick
```

`GalCompiler` (dans `gal.ast`) traduit chaque nœud de l'AST du parseur en classe exécutable du moteur :

- les **conditions** GAL (`Closest`, `Cell`/`Step`, `Got`, `MyDir`, `Struck`→`CanFall`) deviennent des `iGALCondition` du package `frank.gal.aut.conditions` ;
- les **actions** GAL (`Move`, `Turn`, `Hit`, `Egg`, `Explode`, `Pick`, `Wait`) deviennent des `iGALAction` du package `frank.gal.aut.actions` ;
- les **états spéciaux** sont gérés : `()` → `State.DESTROY` (l'entité se détruit), `(_)` → `State.RANDOM` (état cible aléatoire).

Au démarrage, `GameApp.loadGalAutomata()` charge tous les fichiers du dossier `gal_fsm/` et les met dans une table `nom → Automaton`, dans laquelle les entités piochent leur comportement.

Un point d'architecture important : **la capacité à creuser n'est pas dans l'automate**. L'automate dit seulement « va vers Frank » ou « fuis » ; c'est le Model qui décide si ce déplacement traverse (et creuse) la pelouse, selon `Enemy.canDig()`. Ainsi, un même `.gal` produit des comportements visuellement différents selon que l'ennemi creuse ou non.

---

## Les comportements des entités

Tous les fichiers `.gal` se trouvent dans `gal_fsm/`.

| Fichier `.gal` | Automate | Rôle |
| --- | --- | --- |
| `Tracking.gal` | `Tracker` | Poursuit Frank en visant la direction la plus directe vers lui, contourne les obstacles |
| `Flee.gal` | `Fleeing` | Fuit Frank en se déplaçant à l'opposé de sa position |
| `Square.gal` | `Square` | Patrouille en carré, puis passe en mode attaque quand Frank est proche |
| `Digger.gal` | `WallFollower` | Suit les galeries (et les crée s'il sait creuser) |
| `EnemyRandom.gal` | `EnemyRandom` | Errance « intelligente » : poursuit si Frank est proche, sinon vagabonde |
| `AppleFall.gal` | `AppleFall` | Gère la chute des pommes (condition `Struck`/`CanFall`, action `Move(S)`) |
| `Pickup.gal` | `Pickup` | Permet à un fruit/cerise de se collecter quand Frank le recouvre, puis de se détruire |
| `EnemySpawner.gal` | `EnemySpawner` | Génère périodiquement de nouveaux monstres autour du point d'apparition |

### Le spawner de monstres

Les monstres sont créés par une entité dédiée, `EnemySpawnerEntity`, pilotée par `EnemySpawner.gal`. À intervalles réguliers (`Wait(100)`), l'automate exécute l'action `Egg` sur une case libre adjacente pour faire apparaître un nouveau monstre. Une **factory** (définie dans `GameApp`) attribue à chaque monstre créé un type et un automate : tant que **plus de la moitié des fruits restent**, les monstres reçoivent des comportements variés (patrouille, errance, suiveur de murs) ; en dessous de ce seuil, **tous passent en mode `Tracker`** pour intensifier la difficulté en fin de niveau.

---

## Structure du projet

```
ple2026/
├── Makefile                       # Build, run et tests (JavaCC + javac + jar)
├── game.cfg                       # Configuration du jeu (paramètres modifiables sans recompiler)
├── gal_fsm/                       # Automates GAL des entités
│   ├── Tracking.gal
│   ├── Flee.gal
│   ├── Square.gal
│   ├── Digger.gal
│   ├── EnemyRandom.gal
│   ├── AppleFall.gal
│   ├── Pickup.gal
│   └── EnemySpawner.gal
├── sprites/                       # Sprites, sons et écrans de menu
│   ├── Fruity_Frank-player.png    # Frank
│   ├── Fruity_Frank-enemies.png   # Monstres
│   ├── Fruity_Frank-Items.png     # Fruits, pomme, cerise
│   ├── jon.wav                    # Musique de fond
│   ├── eat.wav / death.wav / explosion.wav / gun-shot.wav / win.wav / game-over.wav
│   └── fruity-frank_start.png / echap_menu.png / *_restart_exit.png
├── src-Frank/frank/               # Code du jeu
│   ├── game/                      # Model, config, règles, contrôleur, audio, main
│   ├── entities/                  # Toutes les entités
│   ├── bots/                      # FrankBot (clavier)
│   ├── ai/                        # FrankCategories
│   ├── gal/aut/                   # Actions & conditions GAL maison (le pont)
│   │   ├── actions/               # Move, Turn, Hit, Egg, Explode, Pick
│   │   └── conditions/            # Closest, Step, CanFall, Got, MyDir
│   └── graphics/                  # View, Avatar, Painter, Sprites, Palette
├── src-engine/engine/             # Moteur générique MVCB (fourni)
├── src-parser/gal/                # Parseur GAL + AST + GalCompiler (le pont)
│   └── parser/parser.jj           # Grammaire JavaCC (générée à la compilation)
├── src-pacman/game/               # Jeu de référence PacMan (fourni)
├── lib/                           # Dépendances optionnelles (JUnit 6 pour src-pacman & tests)
├── bin/                           # Sortie de build (classes + fruity-frank.jar)
└── given.jar                      # Bibliothèque oop (runtime, graphics, sound)
```

---

## Compilation et lancement

### Avec le Makefile (recommandé)

Un `Makefile` automatise toute la chaîne : génération du parseur avec JavaCC, compilation, packaging du JAR et lancement. La cible par défaut (`make`) construit le JAR exécutable.

| Cible | Effet |
| --- | --- |
| `make` / `make all` | Construit le JAR exécutable (`bin/fruity-frank.jar`) |
| `make parser` | Génère les sources du parseur avec JavaCC |
| `make compile` | Compile toutes les sources dans `bin/classes/` |
| `make jar` | Crée le JAR exécutable (inclut `sprites/` et `game.cfg`) |
| `make run` | Compile, package puis lance le jeu via le JAR |
| `make run-dev` | Lance le jeu directement depuis le classpath (sans JAR) |
| `make test` | Exécute `EngineTest` avec JUnit 6 (ignoré si JUnit absent) |
| `make clean` | Supprime les artefacts de build |
| `make help` | Affiche l'aide listant toutes les cibles |

```bash
# Construire et lancer en une commande
make run

# Développement rapide, sans packaging du JAR
make run-dev

# Nettoyer le build
make clean
```

La chaîne de build enchaîne automatiquement `parser → compile → jar`, donc `make run` régénère le parseur et recompile au besoin avant de lancer. Le JAR embarque le manifeste (`Main-Class: frank.game.GameApp`, `Class-Path: given.jar`), le dossier `sprites/` et `game.cfg` ; la cible `run` copie aussi `given.jar` à côté du JAR pour la résolution du `Class-Path`.

#### Dépendances

- **Java 17+**
- **JavaCC** (pour la génération du parseur)
- *(optionnel)* **JUnit 6 API** (`junit-jupiter-api-6.1.0.jar`) dans `lib/` — nécessaire pour compiler `src-pacman` et les tests. S'il est absent, le Makefile exclut automatiquement `src-pacman` et le dossier de tests du moteur.
- *(optionnel)* **JUnit 6 Console Launcher** (`junit-platform-console-standalone-6.1.0.jar`) dans `lib/` — nécessaire pour `make test`.

> Le jeu charge ses ressources (`sprites/`, `game.cfg`, `gal_fsm/`) par chemin relatif depuis le répertoire de travail courant : lance-le donc depuis la racine du projet.

### Sous Eclipse

1. Importer le projet et ajouter `given.jar` aux bibliothèques.
2. Vérifier que `game.cfg`, le dossier `gal_fsm/` et le dossier `sprites/` sont **à la racine du projet** (le programme les charge par chemin relatif).
3. Lancer la classe `frank.game.GameApp`.

### En ligne de commande (sans Makefile)

```bash
# Configuration par défaut (game.cfg à la racine)
java frank.game.GameApp

# Configuration spécifique passée en argument
java frank.game.GameApp configs/grande_map.cfg
```

Si aucun argument n'est donné, le jeu charge `game.cfg`. Sinon il charge le fichier passé en premier argument.

---

## Fichier de configuration

`game.cfg` permet de modifier le jeu **sans recompiler**. Format `clé = valeur`, une par ligne ; les lignes vides et celles commençant par `#` sont ignorées.

| Clé | Effet |
| --- | --- |
| `cols`, `rows` | Taille de la carte |
| `frank_speed`, `enemy_speed` | Vitesses de déplacement |
| `fruit_ratio`, `apple_ratio`, `cherry_ratio` | **Plus grand = moins d'éléments** (1 pour N cases) |
| `spawnerx`, `spawnery` | Position du spawner ; toute sa ligne et sa colonne sont dégagées au départ |
| `view_cols`, `view_rows` | Dimensions de la fenêtre de jeu |
| `frank_lives` | Nombre de vies initial |

Pour tester une grande carte clairsemée, il suffit d'augmenter `cols`/`rows` et les `*_ratio` — aucune modification du code n'est nécessaire.

---

## Graphismes et son

### Sprites

Graphismes authentiques **rippés de la version Amstrad CPC** (source : *The Spriters Resource*). Les feuilles suivent une grille régulière de sprites **20 × 15 px**, origine `(4, 5)`, pas `24 × 20`.

| Feuille | Dimensions | Contenu |
| --- | --- | --- |
| `Fruity_Frank-player.png` | 100 × 76 | Frank (3 directions × 4 frames) |
| `Fruity_Frank-enemies.png` | 148 × 136 | 4 monstres (orange, violet, vert, rouge) |
| `Fruity_Frank-Items.png` | 124 × 64 | Fruits, pomme, cerise |

Le rendu respecte le ratio des sprites pour éviter toute déformation : Frank est posé au sol, les monstres flottent dans les galeries, animés selon leur direction réelle.

### Son (`FrankAudio`)

Chaque événement déclenche un effet sonore, par-dessus la musique de fond :

| Son | Événement |
| --- | --- |
| `jon.wav` | Musique de fond (boucle) |
| `eat.wav` | Collecte d'un fruit ou d'une cerise |
| `gun-shot.wav` | Tir du projectile |
| `explosion.wav` | Destruction d'un monstre |
| `death.wav` | Frank perd une vie |
| `win.wav` | Victoire |
| `game-over.wav` | Fin de partie |

---

## Points techniques de la démo

Conformément au contrat d'équipe, les points techniques mis en avant lors de la démonstration sont :

1. **La gestion de la chute des pommes** avec le système à deux couches (pelouse épaisse / pelouse fine) : une pomme reste stable sur la pelouse épaisse, tombe quand elle n'est plus soutenue, écrase la pelouse fine et tue ce qui se trouve dessous.
2. **La poussée des pommes** par Frank, case par case, avec blocage contre les obstacles.

Les autres difficultés traitées : le creusage des galeries, la collecte des fruits et la condition de victoire, le projectile unique rechargeable, et l'IA des monstres pilotée par automates GAL interchangeables.
