#!/bin/bash

build() {
    echo "*** running maven..."
    mvn clean install;
}

release_clean() {
    echo "*** running maven release clean..."
    mvn release:clean
}

release_prepare() {
    echo "*** running maven release prepare..."
    mvn release:prepare
}

release_perform() {
    echo "*** running maven release perform..."
    mvn release:perform
}

release() {
    release_clean
    release_prepare
    release_perform
}

help() {
    echo "*** available commands:"
    typeset -F | awk 'NF>1{print $NF}'
}

set -e

if [ "$#" = 0 ]
then
    build
    exit
fi

for todo in "$@"
do
    $todo
done
