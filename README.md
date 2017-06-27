[![Build Status](https://travis-ci.org/openmrs/openmrs-module-coreapps.svg?branch=master)](https://travis-ci.org/openmrs/openmrs-module-coreapps) 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8282d20655f84632876d16f71a8b3c2e)](https://www.codacy.com/app/openmrs/openmrs-module-coreapps?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=openmrs/openmrs-module-coreapps&amp;utm_campaign=Badge_Grade)
[![dependencies Status](https://david-dm.org/openmrs/openmrs-module-coreapps/status.svg)](https://david-dm.org/openmrs/openmrs-module-coreapps)
[![devDependencies Status](https://david-dm.org/openmrs/openmrs-module-coreapps/dev-status.svg)](https://david-dm.org/openmrs/openmrs-module-coreapps?type=dev)

OpenMRS Core Apps Module
=======================

Provides apps for performing the common tasks

# Dashboard widgets development

The widgets are built as part of the module (`mvn clean install`) without having to install Node or NPM manually. Nevertheless, if you want to develop widgets installing Node is recommended so that you can iterate faster by building widgets and executing tests continously.

You need to have Node 6.x installed. We recommend using [nvm](https://github.com/creationix/nvm) or [nvm-windows](https://github.com/coreybutler/nvm-windows) to install Node.

The code is at **omod/src/main/web**

The generated code is at **omod/target/classes/web/module/resources/scripts/web**

Installing dependencies: `npm install --no-optional` (once after code checkout or when dependencies change)

Development build: `npm run build:dev`

Continuous development build: `npm run watch` (whenever you make a change, a new UI build will be triggered)

Continuous test execution: `npm run test:dev` (in PhantomJS), `npm run test:dev-chrome` (in Chrome), `npm run test:dev-firefox` (in Firefox) (whenever you make a change, tests will be run again)

Clean development build: `npm run clean && npm run build:dev`

Production build: `npm run build`

For continous development we recommend setting up a server using OpenMRS SDK and adding the coreapps module to watched projects (**requires UI Framework 3.12.0+**). Next start the server and use the npm run watch command to have js code instantly transpiled and deployed to the server. Do remember to disable caching for your browser or use Ctrl + F5 to refresh page and its cache to see changes.

## Linking

Linking is a feature of npm, which allows you to modify a library and test modifications in your project.
1) Clone https://github.com/openmrs/openmrs-web-angularjs-api
2) Run `npm link` and `npm run build` from the lib directory
3) Run `npm link @openmrs/angularjs-openmrs-api` from the coreapps project directory.
4) Build the coreapps project.

In order to unlink do:
1) Run `npm unlink @openmrs/angularjs-openmrs-api` and `npm install` from the coreapps project directory.
2) Build the coreapps project. 
