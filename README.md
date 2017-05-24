[![Build Status](https://travis-ci.org/openmrs/openmrs-module-coreapps.svg?branch=master)](https://travis-ci.org/openmrs/openmrs-module-coreapps)

OpenMRS Core Apps Module
=======================

Provides apps for performing the common tasks

# Dashboard widgets development

You need to have Node 6.x installed. We recommend using [nvm](https://github.com/creationix/nvm) or [nvm-windows](https://github.com/coreybutler/nvm-windows) to install Node.

Installing dependencies: `npm install` (once after code checkout or when dependencies change)

Development build: `npm run build:dev`

Continuous development build: `npm run watch` (whenever you make a change, a new UI build will be triggered)

Clean development build: `npm run clean && npm run build:dev`

Production build: `npm run build`

## Linking

Linking is a feature of npm, which allows you to modify a library and test modifications in your project.
1) Clone https://github.com/openmrs/openmrs-web-angularjs-api
2) Run `npm link` and `npm run build` from the lib directory
3) Run `npm link @openmrs/angularjs-openmrs-api` from the coreapps project directory.
4) Build the coreapps project.

In order to unlink do:
1) Run `npm unlink @openmrs/angularjs-openmrs-api` and `npm install` from the coreapps project directory.
2) Build the coreapps project. 