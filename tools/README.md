# Substance Database Generator

This directory contains the tooling used to generate the application's bundled `substances.json` database from the PsychonautWiki GraphQL API.

The generated database currently includes:

* Substance names
* Common names (aliases)
* Systematic names
* Routes of administration
* Duration information (onset, come-up, peak, offset, total duration, afterglow)
* Chemical classes
* Psychoactive classes
* Dangerous interactions
* Unsafe interactions
* Uncertain interactions

The generated database is committed to the repository and distributed with the Android application. The generation process is fully reproducible.

## Repository Contents

| File                   | Purpose                                                                                                                        |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `update_substances.sh` | Main entry point. Starts Bifrost, regenerates the database, stops the container, and creates a Git commit if anything changed. |
| `update_substances.py` | Downloads data from the PsychonautWiki GraphQL API and generates the bundled database files.                                   |
| `psychonautwiki.py`    | Lightweight GraphQL client used by the generator.                                                                              |
| `converter.py`         | Converts GraphQL responses into internal Python data models.                                                                   |
| `generator.py`         | Generates `substances.json` and the attribution file.                                                                          |
| `models.py`            | Internal Python data model used during generation.                                                                             |
| `interactions.py`      | Developer utility for validating interaction targets and identifying unresolved references in the PsychonautWiki dataset.      |

## Requirements

* Python 3.11 or newer
* Docker
* Git

Install the required Python dependencies:

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

The tooling communicates with a local Bifrost GraphQL server rather than querying PsychonautWiki directly. Running Bifrost locally makes the generation process reproducible, independent of external service availability, and avoids repeatedly querying public infrastructure during development.

## Updating the Database

From the project root, run:

```bash
./tools/update_substances.sh
```

The script will automatically:

1. Start the local Bifrost container (if it is not already running)
2. Wait until the GraphQL server is ready
3. Download the latest substance metadata from PsychonautWiki
4. Convert the GraphQL data into the application's internal format
5. Generate `app/src/main/assets/substances.json`
6. Generate `app/src/main/assets/substances_ATTRIBUTION.txt`
7. Stop the Bifrost container
8. Create a Git commit if the generated files have changed

If no data has changed since the previous update, no commit will be created.

## Generated Files

The update process generates:

* `app/src/main/assets/substances.json`
* `app/src/main/assets/substances_ATTRIBUTION.txt`

These files are committed to the repository and bundled with every application release.

## Included Metadata

The generated database currently contains:

* Substance names
* Common names (aliases)
* Systematic names
* Routes of administration
* Duration information
* Chemical classes
* Psychoactive classes
* Dangerous interactions
* Unsafe interactions
* Uncertain interactions

This metadata is consumed entirely offline by the Android application. The app never contacts PsychonautWiki or any other online service at runtime.

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

## Future Improvements

Possible future enhancements include:

* Automated monthly database updates
* Automatic GitHub Actions release builds
* Downloadable database updates without requiring a full application update
* Additional metadata as it becomes available through PsychonautWiki

The tooling has intentionally been designed so that extending the generated database requires only changes to the Python generator while keeping the Android application code largely unchanged.
# Substance Database Generator

This directory contains the tooling used to generate the application's bundled `substances.json` database from the PsychonautWiki GraphQL API.

The generated database includes:

substance names and aliases
systematic names
routes of administration
duration data
chemical classes
psychoactive classes
dangerous interactions
unsafe interactions
uncertain interactions

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
./tools/update_substances.sh
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

