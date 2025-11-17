angular
        .module('att.widget.fileUpload')

        .directive('dropzoneDirective', function() {
            return function(scope, element, attrs) {
                var config, dropzone;

                config = scope[attrs.dropzoneDirective];

                // create a Dropzone for the element with the given options
                dropzone = new Dropzone(element[0], config.options);

                scope.processDropzone = function() {
                    dropzone.processQueue();
                };

                scope.removeAllFiles = function() {
                    dropzone.removeAllFiles();
                };

                scope.addFile = function(file) {
                    dropzone.addFile(file);
                };

                scope.setMaxFilesize = function(maxFilesize) {
                    dropzone.options.maxFilesize = maxFilesize;
                };

                // bind the given event handlers
                angular.forEach(config.eventHandlers, function(handler, event) {
                    dropzone.on(event, handler);
                });
            };
        })

        .directive(
                'attFileUpload',
                [
                        'SessionInfo',
                        'ObsService',
                        'ModuleUtils',
                        '$timeout',
                        function(sessionInfo, obsService, module, $timeout) {
                            return {

                                restrict : 'E',
                                scope : {
                                    config : '='
                                },
                                templateUrl : '/' + module.getPartialsPath(OPENMRS_CONTEXT_PATH) + '/fileUpload.html',

                                controller : function($scope, $rootScope) {

                                    // Loading i18n messages
                                    var msgCodes = [ module.getProvider() + ".attachments.fileUpload.success",
                                            module.getProvider() + ".attachments.fileUpload.error",
                                            module.getProvider() + ".attachments.fileUpload.attentionPastVisit",
                                            module.getProvider() + ".attachments.attachmentspage.fileTitle",
                                            module.getProvider() + ".attachments.dropzone.innerlabel",
                                            module.getProvider() + ".attachments.attachmentspage.commentTitle",
                                            module.getProvider() + ".attachments.misc.label.enterCaption",
                                            module.getProvider() + ".attachments.attachmentspage.uploadButton",
                                            module.getProvider() + ".attachments.attachmentspage.clearFormsButton",
                                            module.getProvider() + ".attachments.noActiveVisit" ]
                                    emr.loadMessages(msgCodes.toString(), function(msgs) {
                                        $scope.msgs = msgs;
                                    });

                                    var providerUuid = "";
                                    $scope.visitUuid = ""; // In scope for toggling ng-show
                                    $scope.closedVisit = false;
                                    $scope.associateWithVisit = $scope.config.associateWithVisit;

                                    $rootScope.$on(module.webcamCaptureForUpload, function(event, webcamFile) {
                                        addFileToDropzone(webcamFile);
                                    });

                                    $scope.isWebcamDisabled = function() {
                                        // http://stackoverflow.com/a/24600597/321797
                                        return !($scope.config.allowWebcam === true)
                                                || (/Mobi/.test(navigator.userAgent));
                                    }

                                    $scope.init = function() {
                                        $scope.typedText = {};
                                        $scope.allowWebcam = $scope.config.allowWebcam;
                                        $scope.showWebcam = false;
                                        Dropzone.options.attachmentsDropzone = false;

                                        sessionInfo.get().$promise.then(function(info) {
                                            providerUuid = info.currentProvider.uuid;
                                        });
                                        if ($scope.config.visit && $scope.associateWithVisit) {
                                            $scope.visitUuid = $scope.config.visit.uuid;
                                            if ($scope.config.visit.stopDatetime) {
                                                $scope.closedVisit = true;
                                            }
                                        }
                                    }

                                    $scope.dropzoneConfig = {

                                        'options' : // passed into the Dropzone constructor
                                        {
                                            'url' : $scope.config.uploadUrl,
                                            'thumbnailHeight' : 100,
                                            'thumbnailWidth' : 100,
                                            'maxFiles' : 1,
                                            'autoProcessQueue' : false,
                                            'renameFilename' : function(name) {
                                                return name.replace(/\.[^/.]+$/, "") + "_"
                                                        + moment().format("YYYYMMDD_HHmmss") + "."
                                                        + name.split(".").pop(); // Timestamping the file name
                                            }
                                        },
                                        'eventHandlers' : {
                                            'addedfile' : function(file) {
                                                setMaxFileSizeOption(file.type); // Setting the max upload file size depending on whether the file can be compressed on the backend.
                                                if (this.files[1] != null) {
                                                    this.removeFile(this.files[0]);
                                                }
                                                $timeout(function() { // https://docs.angularjs.org/error/$rootScope/inprog?p0=$apply#triggering-events-programmatically
                                                    $scope.fileAdded = true;
                                                }, 0);
                                            },
                                            'sending' : function(file, xhr, formData) {
                                                formData.append('patient', $scope.config.patient.uuid);
                                                formData.append('visit',
                                                        $scope.visitUuid == null ? "" : $scope.visitUuid);
                                                formData.append('provider', providerUuid == null ? "" : providerUuid);
                                                formData
                                                        .append(
                                                                'fileCaption',
                                                                ($scope.typedText.fileCaption == null) ? "" : $scope.typedText.fileCaption);
                                            },
                                            'success' : function(file, response) {
                                                $rootScope.$emit(module.eventNewFile, response);
                                                $().toastmessage('showToast', {
                                                    type : 'success',
                                                    position : 'top-right',
                                                    text : emr.message(module.getProvider() + ".attachments.fileUpload.success")
                                                });
                                                $scope.clearForms();
                                            },
                                            'error' : function(file, response, xhr) {
                                                $().toastmessage(
                                                        'showToast',
                                                        {
                                                            type : 'error',
                                                            position : 'top-right',
                                                            text : emr.message(module.getProvider()
                                                                    + ".attachments.fileUpload.error")
                                                                    + " " + response
                                                        });
                                                console.log(response);
                                            }
                                        }
                                    };

                                    var setMaxFileSizeOption = function(mimeType) {
                                        if (!mimeType) {
                                            $scope.setMaxFilesize($scope.config.maxFileSize);
                                            return;
                                        }

                                        var contentFamily = $scope.config.contentFamilyMap[mimeType];
                                        switch (contentFamily) {
                                        case module.family.IMAGE:
                                            $scope.setMaxFilesize($scope.config.maxFileSize
                                                    * $scope.config.maxCompression);
                                            break;

                                        default:
                                            $scope.setMaxFilesize($scope.config.maxFileSize);
                                            break;
                                        }
                                    };

                                    var addFileToDropzone = function(file) {
                                        $scope.addFile(file);
                                    };

                                    $scope.uploadFile = function() {
                                        $scope.processDropzone();
                                    };

                                    $scope.clearForms = function() {
                                        $scope.removeAllFiles();
                                        $scope.typedText.fileCaption = "";
                                    }

                                    $scope.isUploadBtnDisabled = function() {
                                        return !($scope.typedText.fileCaption || $scope.config.allowNoCaption);
                                    }
                                }
                            };
                        } ]);
