#!/bin/bash

# Chemin absolu du script
SCRIPT_PATH="$(readlink -f "$0")"

# Dossier où se trouve le script
SCRIPT_DIR="$(dirname "$SCRIPT_PATH")"

# Dossier qu’on veut zipper (le dossier parent du dossier du script)
TARGET_DIR="$(dirname "$SCRIPT_DIR")"

# Nom du dossier à zipper (par exemple "Projet_mobile")
NOM_DOSSIER="$(basename "$TARGET_DIR")"

# Nom du zip à générer
NOM_ZIP="$NOM_DOSSIER.zip"

# Supprimer le zip s’il existe déjà dans le dossier parent
[ -f "$TARGET_DIR/$NOM_ZIP" ] && rm "$TARGET_DIR/$NOM_ZIP"

# Aller dans le dossier contenant le dossier à zipper
cd "$(dirname "$TARGET_DIR")"

# Zipper tout le dossier, en excluant tous les fichiers *.zip
zip -r "$NOM_DOSSIER/$NOM_ZIP" "$NOM_DOSSIER" -x '*.zip'
