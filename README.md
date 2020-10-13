# GDriveChecker

## What and why
In 2014 I've got a big update in my family situation, and I needed something to privately share family photos with those who're interested in. In my daily life I use Googld Drive and using these photos using google drive seemed to be proper solution. However, I was tired of manually notifying all, who are interested of new photos. I needed a quick solution for that, but I sort of don't like scripting-style solutions, thus I decided to implement something on my own. However, since that time I lost the source code, so I decided to re-do this util.

Note, that this tool designed to suit my use case: report only new files. If any files were changed or udpated, this is not taken into account.

Note, that alike functionality exists in Dropbox, but with Google drive I require less from users: just Google account, whereas using other services would require an application, user account, etc.

## Configuration details
### Google Drive API
User is required to obtain her/his own Google Drive project and create secret.json

### GDriveChecker configuration
Configuration example:


    <Configuration>
        <instance>GDriveChecker1</instance>
        <secretFile>GDriveChecker-secret.json</secretFile>
        <credentialsStore>.gdrivechecker</credentialsStore>
        <folders>
            <folder>/Aero/</folder>
            <folder>/Upload/panos</folder>
        </folders>
    
        <from>reznitsky@gmail.com</from>
        <recipients>
            <recipient>reznitsky@gmail.com</recipient>
        </recipients>
    
        <parallelGDriveQueries>2</parallelGDriveQueries>
    
        <dbLocation>./.gdrivechecker.db/gdrivechecker</dbLocation>
    </Configuration>


## Requirements
JDK 10 (streams)
Maven (build)

## History of changes
GD-000: Initial seed

GD-001: Check out/play with Google Drive Api v3

GD-002: Use existing config, etc to read actual data from my drive

GD-003: After experiencing some issues, try to convert to thread-safe

GD-004: v3 Api forces(?) to enumerate files by pages -- comply to this; (sideeffect: minor update in logging; limit to 2 threads accessing gdrive)

GD-005: Persist Google Drive files info locally

GD-006: Calculate difference (search new files)

GD-007: Send e-mails with list of files and links to folders

GD-008: Move to Java 10

GD-009: Move to JDK 12

GD-010: Fix build
