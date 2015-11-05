#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR
mvn package
java -jar target/benchmarks.jar -f 3 -i 5 -wi 2 -bm all -foe true -v EXTRA
