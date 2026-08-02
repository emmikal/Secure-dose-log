# Substance Database Generator

This directory contains the tooling used to generate the application's bundled `substances.json` database from the PsychonautWiki GraphQL API.

The generated database is committed to the repository and distributed with the Android application. The generation process is fully reproducible.

## Requirements

* Python 3.11 or newer
* Docker
* Git

Install the required Python package:

```bash
pip install -r requirements.txt
```

## Building Bifrost

Turboautism Dose Log currently relies on a patched fork of the PsychonautWiki Bifrost GraphQL server.

At the time this tooling was written, the upstream repository contained Dockerfile issues that prevented it from building successfully. The required fixes are included in my fork.

Clone and build the server:

```bash
git clone https://github.com/emmikal/bifrost.git
cd bifrost
docker build -t bifrost .
```

Original upstream project:

https://github.com/psychonautwiki/bifrost

If the upstream project incorporates these fixes in the future, this documentation can be updated to point back to the official repository.

## Updating the Database

From the project root, run:

```bash
./scripts/update_substances.sh
```

The script will automatically:

1. Start the local Bifrost container (if it is not already running)
2. Wait until the server is ready
3. Download the latest substance database from PsychonautWiki
4. Generate `app/src/main/assets/substances.json`
5. Generate `app/src/main/assets/substances_ATTRIBUTION.txt`
6. Stop the Bifrost container
7. Create a Git commit if the generated files have changed

If no data has changed since the previous update, no commit will be created.

## Generated Files

The update process generates:

* `app/src/main/assets/substances.json`
* `app/src/main/assets/substances_ATTRIBUTION.txt`

These files are committed to the repository and shipped with the application.

## Licensing

The generated substance database is derived from PsychonautWiki.

All generated data remains subject to the original PsychonautWiki license:

**CC BY-SA 4.0**

https://creativecommons.org/licenses/by-sa/4.0/

The generated attribution file (`substances_ATTRIBUTION.txt`) is included with every generated database to satisfy the license requirements.

## Why JSON?

The application intentionally stores the bundled substance database as JSON rather than generated Kotlin source code.

Advantages of this approach include:

* Smaller APK size
* Faster build times
* Separation of data from application code
* Easier automated updates
* Simpler future migration to downloadable database updates
* Improved maintainability

