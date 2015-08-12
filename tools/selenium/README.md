# Selenium server helper

This directory help to manage the Selenium server needed by Cerberus. 

## Before to go

To get ready to execute the Selenium server, you need to install your own Selenium server instance. Hereafter several steps to how to do it:

### Standalone Selenium server installation

 1. Download the **standalone** Selenium server from the official website: http://www.seleniumhq.org/download/
 2. Put the downloaded jar into the **lib** directory
 3. Create a link to this downloaded jar:
    
    ```bash
    $ cd path/to/the/lib/directory
    $ ln -s `pwd`/selenium-server-standalone-X.YY.Z.jar current.jar
    ```

### Browser Selenium drivers

If you need to use a dedicated Selenium driver for your browser (*e.g.,* Chrome),  then you could download them into the dedicated `drivers` directory.

Then, be ware to correctly configure scripts to use it (look at the script documentation)

## Scripts

Scripts are contained into the `bin` directory and are. listed below:

### selenium

#### Definition

Start & stop the selenium server.

#### Usage

```bash
$ selenium [start|stop]
```

### kill-firefox-zombieprocess

#### Definition

Kill old processes of firefox that sometimes, selenium does not manage to kill.

#### Usage

```bash
$ kill-firefox-zombieprocess
```
