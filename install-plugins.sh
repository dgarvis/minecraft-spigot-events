#!/bin/sh

loc=$(dirname "$0")

cd $loc

cp mces-spigot/target/mces-spigot-1.1-SNAPSHOT.jar test-env/world1-plugins/
cp mces-spigot/target/mces-spigot-1.1-SNAPSHOT.jar test-env/world2-plugins/

cp mces-bungee/target/mces-bungee-1.1-SNAPSHOT.jar test-env/bungee-plugins
