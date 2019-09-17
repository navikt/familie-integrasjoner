#!/usr/bin/env bash

#0: Sjekk at du er på master
current_branch=$(git branch | grep \* | cut -d ' ' -f2)
master_branch="master"

if ! [ "$current_branch" == "$master_branch" ]; then
    printf "Må være på master-branch for å tagge. Avslutter script\n"
    exit 1
fi

#1: Hent nåværende versjon/tag
git fetch --prune --tags

version=$(git describe --abbrev=0 --tags)
current_major=${version:1:1}
current_minor=${version:3}
printf "Nåværende versjon: $version\n"

#2: Få major/minor som kommandolinjeargs og sjekk at det er høyere enn nåværende versjon/tag
new_major=$1
new_minor=$2

if [ -z $new_major ] && [ -z $new_minor ]; then
    printf "usage: Spesifiser major-versjon og minor-versjon som hhv 1 og 2 argument. Eksempelvis: ./tag.sh <major> <minor>\n"
    exit 1
fi

if ! [[ new_major -gt current_major || (new_major -eq current_major  &&  new_minor -gt current_minor) ]]; then
    printf "Ny tag er den samme eller lavere enn nåværende tag. Vennligst øk major og/eller minor\n"
    exit 1
fi

next_version="v$new_major.$new_minor"
printf "Neste versjon blir: $next_version\n"

#3: Tag git commit med ny versjon
git tag -a $next_version -m "Test av tag-script"

#4: Push tag
git push origin $next_version
