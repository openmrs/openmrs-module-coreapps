// Polyfills for APIs missing in the ancient HtmlUnit/Rhino used by jasmine-maven-plugin.
// HtmlUnit 2.25.0 simulates an old IE environment lacking ES5 and DOM4 APIs.
(function() {
    // DOM4
    if (!document.querySelector) {
        document.querySelector = function() { return null; };
    }
    if (!document.querySelectorAll) {
        document.querySelectorAll = function() { return []; };
    }

    // ES5 String
    if (!String.prototype.trim) {
        String.prototype.trim = function() { return this.replace(/^\s+|\s+$/g, ''); };
    }
    if (!String.prototype.trimLeft) {
        String.prototype.trimLeft = function() { return this.replace(/^\s+/, ''); };
    }
    if (!String.prototype.trimRight) {
        String.prototype.trimRight = function() { return this.replace(/\s+$/, ''); };
    }

    // ES5 Array
    if (!Array.isArray) {
        Array.isArray = function(a) { return Object.prototype.toString.call(a) === '[object Array]'; };
    }
    if (!Array.prototype.indexOf) {
        Array.prototype.indexOf = function(v, s) {
            for (var i = s || 0; i < this.length; i++) { if (this[i] === v) return i; }
            return -1;
        };
    }
    if (!Array.prototype.forEach) {
        Array.prototype.forEach = function(fn, ctx) { for (var i = 0; i < this.length; i++) fn.call(ctx, this[i], i, this); };
    }
    if (!Array.prototype.map) {
        Array.prototype.map = function(fn, ctx) { var r = []; for (var i = 0; i < this.length; i++) r.push(fn.call(ctx, this[i], i, this)); return r; };
    }
    if (!Array.prototype.filter) {
        Array.prototype.filter = function(fn, ctx) { var r = []; for (var i = 0; i < this.length; i++) if (fn.call(ctx, this[i], i, this)) r.push(this[i]); return r; };
    }
    if (!Array.prototype.some) {
        Array.prototype.some = function(fn, ctx) { for (var i = 0; i < this.length; i++) if (fn.call(ctx, this[i], i, this)) return true; return false; };
    }
    if (!Array.prototype.every) {
        Array.prototype.every = function(fn, ctx) { for (var i = 0; i < this.length; i++) if (!fn.call(ctx, this[i], i, this)) return false; return true; };
    }
    if (!Array.prototype.reduce) {
        Array.prototype.reduce = function(fn, init) {
            var i = 0, acc = arguments.length > 1 ? init : this[i++];
            for (; i < this.length; i++) acc = fn(acc, this[i], i, this);
            return acc;
        };
    }

    // ES5 Object
    if (!Object.keys) {
        Object.keys = function(o) { var k = []; for (var p in o) if (Object.prototype.hasOwnProperty.call(o, p)) k.push(p); return k; };
    }
    if (!Object.create) {
        Object.create = function(proto, props) {
            function F() {}
            F.prototype = proto;
            var o = new F();
            if (props) for (var k in props) o[k] = props[k].value;
            return o;
        };
    }

    // ES5 Function
    if (!Function.prototype.bind) {
        Function.prototype.bind = function(ctx) {
            var fn = this, args = Array.prototype.slice.call(arguments, 1);
            return function() { return fn.apply(ctx, args.concat(Array.prototype.slice.call(arguments))); };
        };
    }

    // DOM2 HTML — jQuery 3.x calls document.implementation.createHTMLDocument at init
    if (document.implementation && !document.implementation.createHTMLDocument) {
        try {
            document.implementation.createHTMLDocument = function(title) {
                return {
                    body: { innerHTML: '', childNodes: { length: 0 } },
                    createElement: function(tag) { return document.createElement(tag); }
                };
            };
        } catch(e) {}
    }
}());
