<!-- markdownlint-configure-file {
  "MD013": {
    "code_blocks": false,
    "tables": false
  },
  "MD033": false,
  "MD041": false
} -->

<div align="center">

# CodeOwners CLI

Command line tool for [codeowners](https://github.com/nielsbasjes/codeowners) that helps identify the owners for files 
in a local repository or directory as well as identifying unowned code owners.

[![Release](https://github.com/vincentjames501/codeowners-cli/actions/workflows/release.yml/badge.svg)](https://github.com/vincentjames501/codeowners-cli/actions/workflows/release.yml)
[![Code quality checks](https://github.com/vincentjames501/codeowners-cli/actions/workflows/code-quality-checks.yml/badge.svg?branch=main)](https://github.com/vincentjames501/codeowners-cli/actions/workflows/code-quality-checks.yml) 
![GitHub release (latest by date)](https://img.shields.io/github/v/release/vincentjames501/codeowners-cli)
![GitHub last commit](https://img.shields.io/github/last-commit/vincentjames501/codeowners-cli)
![GitHub commit activity](https://img.shields.io/github/commit-activity/y/vincentjames501/codeowners-cli)
![GitHub pull requests](https://img.shields.io/github/issues-pr/vincentjames501/codeowners-cli)
![GitHub issues](https://img.shields.io/github/issues/vincentjames501/codeowners-cli)
![GitHub contributors](https://img.shields.io/github/contributors/vincentjames501/codeowners-cli)
![GitHub watchers](https://img.shields.io/github/watchers/vincentjames501/codeowners-cli)
![Known Vulnerabilities](https://snyk.io/test/github/vincentjames501/codeowners-cli/badge.svg)

<a href="https://github.com/vincentjames501/codeowners-cli/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=vincentjames501/codeowners-cli"  alt="Contributors"/>
</a>

Made with [contrib.rocks](https://contrib.rocks).

[Key Features](#key-features) •
[Getting started](#getting-started) •
[How to use](#how-to-use) •
[Configuration](#configuration) •
[Related projects](#related-projects) •
[License](#license) •
[Code Quality](#code-quality) •

</div>

## Getting started

Once you have built and released the application, you can use the following commands to deploy the application to your Mac, Linux or Windows machine.



```shell
brew tap vincentjames501/tap
brew install codeowners-cli
```

To upgrade version

```shell
brew update
brew upgrade codeowners-cli
```

## How To Use

Invoking the command displays the usage information as shown below.

```shell
$ codeowners-cli help

Usage: codeowners-cli [-hV] [COMMAND]
Process CODEOWNER files
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  help    Display help information about the specified command.
  list    Lists all files with the corresponding approvers
  verify  Verifies the format of the CODEOWNERS file
```

```shell
$ codeowners-cli list help

Usage: codeowners-cli list [-fu] [-idl] [-ngi] [-cf=<codeownersFile>]
                           [-gi=<gitignoreFile>] [-o=<owners>]... [<files>...]
                           [COMMAND]
Lists all files with the corresponding approvers
      [<files>...]           Specifies the files to scan
      -cf, --codeowners-file=<codeownersFile>
                             Specify the path to the CODEOWNERS file.
  -f, --fail-on-output       Whether to exit non-zero if there are any matches.
      -gi, --gitignore-file=<gitignoreFile>
                             Specify the path to the .gitignore file.
      -idl, --ignore-dot-files
                             Whether to ignore the dot files.
      -ngi, --no-gitignore   Whether to ignore the .gitignore file.
  -o, --owners=<owners>      Filters the results by owner
  -u, --unowned-files        Whether to only show unowned files (can be
                               combined with -o).
Commands:
  help  Display help information about the specified command.
```

```shell
$ codeowners-cli list

                                                                       File |               Approvers
                                                       ./CODE_OF_CONDUCT.md |           @default-team
                                                          ./CONTRIBUTING.md |           @default-team
                                                                  ./LICENSE |           @default-team
                                                                ./README.md |           @default-team
                                               ./dependency-reduced-pom.xml |           @default-team
                                         ./etc/eclipse-formatter-config.xml |           @default-team
                                                          ./etc/license.txt |           @default-team
                                                            ./jreleaser.yml |           @default-team
                                                                     ./mvnw |           @default-team
                                                                 ./mvnw.cmd |           @default-team
                                                                  ./pom.xml |           @default-team
                                           ./src/main/assembly/assembly.xml |    @devs, @default-team
          ./src/main/java/org/vincentjames501/codeowners/CodeOwnersCLI.java |    @devs, @default-team
./src/main/java/org/vincentjames501/codeowners/commands/ListCodeOwners.java |    @devs, @default-team
        ./src/main/java/org/vincentjames501/codeowners/commands/Verify.java |    @devs, @default-team
         ./src/main/resources/META-INF/native-image/native-image.properties |    @devs, @default-team
             ./src/main/resources/META-INF/native-image/reflect-config.json |    @devs, @default-team
      ./src/test/java/org/vincentjames501/codeowners/CodeOwnersCLITest.java | @testers, @default-team
                                            ./src/test/resources/CODEOWNERS | @testers, @default-team
```

```shell
codeowners-cli verify help

Usage: codeowners-cli verify [-cf=<codeownersFile>] [COMMAND]
Verifies the format of the CODEOWNERS file
      -cf, --codeowners-file=<codeownersFile>
         Specify the path to the CODEOWNERS file.
Commands:
  help  Display help information about the specified command.
```

```shell
$ codeowners-cli verify

CODEOWNERS file is valid.
```

## Pre-commit Usage

To use with Pre-commit, simply add the following to your `.pre-commit-config.yaml`:

### To prevent committing unowned files (also validate CODEOWNERS format)

```yaml
- repo: https://github.com/vincentjames501/codeowners-cli
  rev: v0.0.3
  hooks:
    - id: codeowners-cli
      args: [ "list", "--unowned-files", "--fail-on-output" ]
```

### To prevent committing just invalid CODEOWNERS

```yaml
- repo: https://github.com/vincentjames501/codeowners-cli
  rev: v0.0.3
  hooks:
    - id: codeowners-cli
      args: [ "verify" ]
```

## Building

This is powered by GraalVM native image and distribution using JReleaser and GitHub Actions and Workflow.

## Related projects

> rrajesh1979/ref-java-jwt – Build process heavily inspired by https://github.com/rrajesh1979/ref-java-jwt
> nielsbasjes/codeowners – Leverages parsers for the CLI tools https://github.com/nielsbasjes/codeowners

## License

![GitHub](https://img.shields.io/github/license/vincentjames501/codeowners-cli)

## Code Quality

[![codecov](https://codecov.io/gh/vincentjames501/codeowners-cli/branch/main/graph/badge.svg?token=nuivwdrnL1)](https://codecov.io/gh/vincentjames501/codeowners-cli)

[![Maintainability](https://api.codeclimate.com/v1/badges/6bfbafbfd54e673b5a0b/maintainability)](https://codeclimate.com/github/vincentjames501/codeowners-cli/maintainability)
