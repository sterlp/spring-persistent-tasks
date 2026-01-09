# Spring Persistent Tasks Documentation

This directory contains the MkDocs-based documentation for Spring Persistent Tasks.

## Setup

### Prerequisites
- Python 3.7 or higher
- pip (Python package manager)

### Installation

```bash
pip install -r requirements.txt
```

## Commands

### Development
-   `mkdocs serve` - Start the live-reloading docs server at http://127.0.0.1:8000
-   `mkdocs serve --dirtyreload` - Faster reload during development

**Windows alternative** (if mkdocs not in PATH):
```bash
python -m mkdocs serve
python -m mkdocs serve --dirtyreload
```

### Build
-   `mkdocs build` - Build the documentation site to `site/` directory
-   `mkdocs build --clean` - Build with clean output directory

**Windows alternative** (if mkdocs not in PATH):
```bash
python -m mkdocs build
python -m mkdocs build --clean
```

### Maintenance
-   `pip install --upgrade -r requirements.txt` - Update dependencies
-   `mkdocs -h` - Print help message and exit

**Windows alternative** (if mkdocs not in PATH):
```bash
python -m pip install --upgrade -r requirements.txt
python -m mkdocs serve
```

## Documentation Structure

```
docs/
├── index.md                    # Home page
├── register-spring-task.md     # Task registration guide
├── queue-a-spring-task.md      # Task queuing guide
├── delete-task-trigger.md      # Task cancellation
├── life-cycle-events.md        # Event handling
├── failed-spring-triggers.md   # Error handling
├── transaction-management.md   # Transaction configuration
├── setup/                      # Setup guides
│   ├── maven-setup.md
│   ├── liquibase-setup.md
│   ├── spring-configuration-options.md
│   └── ...
└── test/                       # Testing guides
    └── junit-test.md
```

## SEO Optimization

The documentation includes comprehensive SEO optimization:

- **Meta Tags**: Each page includes title, description, and keywords
- **Structured Tags**: Pages are tagged for better categorization
- **Site Configuration**: sitemap.xml, robots.txt support via MkDocs
- **Search Optimization**: Enhanced search with separators and language support
- **Minification**: HTML, CSS, and JS minification for faster loading

## Publishing

The documentation is published to: https://spring-persistent-task.sterl.org

To publish changes:
```bash
./publish.sh
```
