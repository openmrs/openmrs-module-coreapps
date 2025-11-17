angular.module('att.service.moduleUtils')

.factory('ModuleUtils', function($q) {

    this.family = {};
    this.family.IMAGE = "IMAGE";
    this.family.PDF = "PDF";
    this.family.OTHER = "OTHER";

    this.eventNewFile = "att_event_newComplexObs";
    this.webcamCaptureForUpload = "att_event_webcamCaptureForFileUpload";

    this.getProvider = function() {
        return "coreapps";
    }

    this.getPath = function(openmrsContextPath) {
        return openmrsContextPath + '/' + this.getProvider();
    }

    this.getPartialsPath = function(openmrsContextPath) {
        return openmrsContextPath + '/ms/uiframework/resource/' + this.getProvider() + '/partials/attachments';
    }

    /**
     * Turns a byte array into a Base64 encoded String.
     * See http://stackoverflow.com/a/9458996/321797
     */
    this.arrayBufferToBase64 = function(buffer) {
        var binary = '';
        var bytes = new Uint8Array(buffer);
        var len = bytes.byteLength;
        for (var i = 0; i < len; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return window.btoa(binary);
    }

    this.dataUritoBlob = function(dataUri) {
        // convert base64/URLEncoded data component to raw binary data held in a string
        var byteString;
        if (dataUri.split(',')[0].indexOf('base64') >= 0)
            byteString = atob(dataUri.split(',')[1]);
        else
            byteString = unescape(dataUri.split(',')[1]);

        // separate out the MIME type String
        var mimeType = dataUri.split(',')[0].split(':')[1].split(';')[0];

        // write the bytes of the string to a typed array
        var ia = new Uint8Array(byteString.length);
        for (var i = 0; i < byteString.length; i++) {
            ia[i] = byteString.charCodeAt(i);
        }

        return new Blob([ ia ], {
            type : mimeType
        });
    }

    return this

});