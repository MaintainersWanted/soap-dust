#!/bin/bash -e

cd $(dirname $0)/..

branch=$(git branch  | grep '^*' | cut -d' ' -f2)

ant doc

git checkout gh-pages
git pull origin gh-pages
rm -rf "api/$branch"
cp -r target/api "api/$branch"

ls api | grep -v index.html \
| perl -e 'print "<html><body><ul>"; while(<>) { chop $_;  print "<li><a href=\"./$_\">$_</a></li>";} print "</ul></body></html>"' > api/index.html

git add "api"
git commit -m "Upgrade soapdust documentation for version $branch"

git checkout $branch

echo "Javadoc updated in branch gh-pages." >&2
echo "Please push gh-pages to publish." >&2
echo "For instance, run git push --all" >&2
