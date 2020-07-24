import LatestObsForConceptList from './';
import 'angular-mocks';

describe('LatestObsForConceptList', () => {
	let $componentController;
	let $scope;
	let $httpBackend;
  
	beforeEach(() => {
		angular.mock.module(LatestObsForConceptList);
		inject((_$componentController_, $rootScope, _$httpBackend_) => {
			$componentController = _$componentController_;
			$scope = $rootScope.$new();
			$httpBackend = _$httpBackend_;
		});
		
		$httpBackend.expectGET('/module/uicommons/messages/messages.json?localeKey=en').respond({});
	});

    it('should use maxAge from config', () => {
		let bindings = {config: { maxAge: '2w', concepts: 'uuid-1, uuid-2', patientUuid: 'patientUuid' }};
		let ctrl = $componentController('latestobsforconceptlist', {$scope}, bindings);
		
		ctrl.$onInit();
		
		expect(ctrl.maxAgeInDays).toBe(14);
    });
});