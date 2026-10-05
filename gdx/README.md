# My Little Pony – Le Labyrinthe de Discord (libGDX)

Version du jeu pour **Android** et **PC**, construite avec [libGDX](https://libgdx.com).
La version Swing d'origine reste à la racine du dépôt.

## Installer sur Android

Sur le téléphone, télécharger
[ponymaze.apk](https://github.com/Calicles/my_little_pony_maze_2/releases/latest/download/ponymaze.apk),
l'ouvrir et accepter l'installation depuis cette source.

L'APK est compilé et publié par GitHub Actions (`.github/workflows/android.yml`)
à chaque modification de `gdx/` sur `master`.

## Jouer

- **Téléphone** : la croix en bas à gauche déplace le poney (on peut glisser le doigt d'une flèche
  à l'autre), les curseurs règlent la musique et les bruitages.
- **PC** : flèches du clavier (ou ZQSD / WASD).
- Quand un niveau est fini : toucher l'écran ou appuyer sur une flèche pour continuer.

## Développer

```
./gradlew lwjgl3:run              # lancer sur PC
./gradlew lwjgl3:run --args=portrait   # format téléphone, avec la croix tactile
./gradlew core:test               # tests : chaque labyrinthe est résolu et rejoué
./gradlew lwjgl3:dist             # archive autonome lwjgl3/build/libs/ponymaze-*.jar
./gradlew android:assembleDebug   # APK (nécessite le SDK Android)
```

Le module `android` n'est inclus que si un SDK Android est trouvé
(`ANDROID_HOME` ou `sdk.dir` dans `local.properties`).

## Organisation

- `core/` : la logique du jeu (niveaux, collisions, IA du boss avec A*) et l'affichage libGDX
  (`com.antoine.gdx`).
- `lwjgl3/` : lanceur PC. `android/` : lanceur Android.
- `assets/` : images, cartes et sons (musique en OGG).
