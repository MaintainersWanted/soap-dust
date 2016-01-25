#!/bin/bash

cd $(dirname $0)/..

if [ $(git status --porcelain | wc -l) -ne 0 ]
then
  echo "You have local modifications you need to check in svn before proceeding" >&2
  exit 1
fi

DELIVERABLE=soap-dust

BRANCH=$(git branch  | grep '^*' | cut -d' ' -f2)

REVISION=$(($(git tag | grep "^$BRANCH" | sed -e 's:.*\.::' | sort -n | tail -n1)+1))

git tag $BRANCH.$REVISION

echo $DELIVERABLE.$BRANCH.$REVISION
