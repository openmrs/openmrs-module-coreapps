// This file was created in order to declare all the angular modules. This way we can ensure that all the
// dependency problems between controllers, directives and services will be resolved.

angular.module('att.page.main', [ 'obsService', 'session', 'att.widget.fileUpload', 'att.widget.gallery',
        'att.widget.thumbnail' ]);
angular.module('att.fragment.dashboardWidget', [ 'obsService', 'att.widget.gallery', 'att.widget.thumbnail' ]);
angular.module('att.fragment.encounterTemplate', [ 'att.widget.complexObsEncounter' ]);

angular.module('att.widget.modalImage', [ 'att.service.moduleUtils' ]);
angular.module('att.widget.complexObsEncounter', [ 'obsService', 'att.widget.gallery', 'att.widget.thumbnail',
        'att.widget.modalImage' ]);
angular.module('att.widget.gallery', [ 'att.service.configService', 'att.service.moduleUtils' ]);
angular.module('att.widget.modalWebcam', [ 'att.service.moduleUtils' ]);
angular.module('att.widget.fileUpload', [ 'att.widget.modalWebcam', 'att.service.moduleUtils' ]);
angular.module('att.widget.thumbnail', [ 'att.service.attachmentService', 'att.service.complexObsCacheService',
        'ngDialog', 'att.widget.modalImage', 'att.service.moduleUtils', 'cp.ng.fix-image-orientation' ])

angular.module('att.service.configService', [ 'att.service.moduleUtils' ]);
angular.module('att.service.moduleUtils', []);
angular.module('att.service.attachmentService', [ 'ngResource', 'uicommons.common' ]);
angular.module('att.service.complexObsCacheService', [ 'att.service.moduleUtils' ]);
