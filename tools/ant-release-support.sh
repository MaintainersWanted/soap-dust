#!/bin/bash

if [ ! -z "$(svn st)" ]
then
  echo "You have local modifications you need to check in svn before proceeding" >&2
  exit 1
fi

DELIVERABLE=$(LANG=C svn info . | grep URL | sed -e 's:.*/\(.*\)/\(.*\):\1-\2:')

REVISION=$(LANG=C svn info . | grep Revision | cut -d' ' -f2)

echo $DELIVERABLE.$REVISION
