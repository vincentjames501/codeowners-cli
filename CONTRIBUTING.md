# Contributing to codeowners-cli

The codeowners-cli community welcomes anyone that wants to help out in any way, whether that includes reporting problems, helping with documentation, or contributing code changes to fix bugs, add tests, or implement new features. 
This document outlines the basic steps required to work with and contribute to the codeowners-cli codebase.

## Table of contents

- [Install the tools](#install-the-tools)
- Repository
  - [GitHub account](#github-account)
  - [Fork the codeowners-cli repository](#fork-the-repository)
  - [Clone your fork](#clone-your-fork)
  - [Get the latest upstream code](#get-the-latest-upstream-code)
- Local development
  - [Building locally](#building-locally)
  - [Running and debugging tests](#running-and-debugging-tests)
  - [Making changes](#making-changes)
  - [Rebasing](#rebasing)
- Proposing the changes
  - [Creating a pull request](#creating-a-pull-request)
  - [Continuous Integration](#continuous-integration)
  - [Summary](#summary)

### Install the tools

The following software is required to work with the codeowners-cli codebase and build it locally:

* [Git 2.2.1](https://git-scm.com) or later
* JDK 21 or later, e.g. [OpenJDK](http://openjdk.java.net/projects/jdk/)
* [Apache Maven](https://maven.apache.org/index.html) 3.8.4 or later

See the links above for installation instructions on your platform. You can verify the versions are installed and running:

    $ git --version
    $ javac -version
    $ mvn -version

### GitHub account

codeowners-cli uses [GitHub](GitHub.com) for its primary code repository and for pull-requests, so if you don't already have a GitHub account you'll need to [join](https://github.com/join).

### Fork the codeowners-cli repository

Go to the [codeowners-cli repository](https://github.com/vincentjames501/codeowners-cli) and press the "Fork" button near the upper right corner of the page. When finished, you will have your own "fork" at `https://github.com/<your-username>/codeowners-cli`, and this is the repository to which you will upload your proposed changes and create pull requests. For details, see the [GitHub documentation](https://help.github.com/articles/fork-a-repo/).

### Clone your fork

At a terminal, go to the directory in which you want to place a local clone of the codeowners-cli repository, and run the following commands to use HTTPS authentication:

    $ git clone https://github.com/<your-username>/codeowners-cli.git

If you prefer to use SSH and have [uploaded your public key to your GitHub account](https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/), you can instead use SSH:

    $ git clone git@github.com:<your-username>/codeowners-cli.git

This will create a `codeowners-cli` directory, so change into that directory:

    $ cd codeowners-cli

This repository knows about your fork, but it doesn't yet know about the official or ["upstream" codeowners-cli repository](https://github.com/vincentjames501/codeowners-cli). Run the following commands:

    $ git remote add upstream https://github.com/vincentjames501/codeowners-cli.git
    $ git fetch upstream
    $ git branch --set-upstream-to=upstream/main main

Now, when you check the status using Git, it will compare your local repository to the *upstream* repository.

### Get the latest upstream code

You will frequently need to get all the of the changes that are made to the upstream repository, and you can do this with these commands:

    $ git fetch upstream
    $ git pull upstream main

The first command fetches all changes on all branches, while the second actually updates your local `main` branch with the latest commits from the `upstream` repository.

### Building locally

To build the source code locally, checkout and update the `main` branch:

    $ git checkout main
    $ git pull upstream main

Then use Maven to compile everything, run all unit and integration tests, build all artifacts, and install all JAR, ZIP, and TAR files into your local Maven repository:

    $ mvn clean install -Passembly

If you want to skip the integration tests (e.g., if you don't have Docker installed) or the unit tests, you can add `-DskipITs` and/or `-DskipTests` to that command:

    $ mvn clean install -Passembly -DskipITs -DskipTests
