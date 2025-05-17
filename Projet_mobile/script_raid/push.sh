#!/bin/bash
cd "$(dirname "$0")"
git add .
echo "Message de commit : "
read msg
git commit -m "$msg"

