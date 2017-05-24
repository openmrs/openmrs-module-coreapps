 let services = angular.module('dashboardwidgets.services', []);
 
 import WidgetsCommons from './dashboardwidgetscommons.service';
 services.service('widgetsCommons', WidgetsCommons);
 
 export default services.name;