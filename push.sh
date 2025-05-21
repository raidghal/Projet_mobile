#!/bin/bash
set -e
PROJET_DIR=$(pwd)
cd "$PROJET_DIR"
git init
git remote remove origin 2> /dev/null || true
git remote add origin https://github.com/raidghal/Projet_mobile.git
git add .
git commit -m "Mise à jour du projet"
git branch -M main
git push -u origin main --force
