

# Getting started #

This page provides everything you'll need to know about getting started with AppUpdater4J

## Creating update file ##

The update file is a simple zip archive. The followed picture represents the content of the archive

![http://appupdater4j.googlecode.com/svn/trunk/AppUpdater4J/site/zip-structure.png](http://appupdater4j.googlecode.com/svn/trunk/AppUpdater4J/site/zip-structure.png)

|build.xml||
|:--------|:|
|update.properties||
|lib folder|contains ANT's libraries. As AppUpdater4J runs ANT's targets, these 3 libraries are mandatory. You can find them in you ANT\_HOME directory or at this [URL](http://code.google.com/p/appupdater4j/source/browse/#svn/trunk/AppUpdater4J/demo/ant/lib)|


## Checking for update ##

## Wrapping your application ##