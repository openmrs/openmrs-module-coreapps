	import ProgramStatus from './';
	import 'angular-mocks';
	import { controllers } from 'chart.js';

	describe('ProgramStatus', () => {
		let $componentController;
		let $scope;
		let $httpBackend;
		let $window;
	  
		beforeEach(() => {
			angular.mock.module(ProgramStatus);
			inject((_$componentController_, $rootScope, _$httpBackend_, _$window_) => {
				$componentController = _$componentController_;
				$scope = $rootScope.$new();
				$httpBackend = _$httpBackend_;
				$window = _$window_;
				$window.sessionContext = {};
			});

			var sessionResponse = {
				"sessionId":"070AE5705521F5FF5107F3AB21300C27",
				"authenticated":true,
				"currentProvider":{
					"name":"Test User",
					"uuid":"c71aacce-e406-4ff5-8fce-2e4059e13c1f"
				},
				"sessionLocation":{
					"name":"Test Location",
					"uuid":"8fe901fb-4f09-466e-a7ea-dd95e0f3048d"
				},
				"user":{
					"name":null,
					"uuid":"1c3db49d-440a-11e6-a65c-00e04c680037",
					"privileges":[
						{	
							"uuid":"c4d9f61c-065c-46c4-9114-951ae139847c",
							"display":"App: icrc.clinicalDashboard",
							"name":"App: icrc.clinicalDashboard",
							"description":"Able to access the clinical dashboard",
							"retired":false,
							"links":[
								{"rel":"self","uri":"http://localhost:8080/openmrs/ws/rest/v1/privilege/c4d9f61c-065c-46c4-9114-951ae139847c"},
								{"rel":"full","uri":"http://localhost:8080/openmrs/ws/rest/v1/privilege/c4d9f61c-065c-46c4-9114-951ae139847c?v=full"}
							],
							"resourceVersion":"1.8"
						}
				]},
				"locale":"en_GB",
				"allowedLocales":["en","es","fr","it","pt"]
			};
			
			var locationResponse = {
									"results":[
											{"display":"Test Location","uuid":"8fe901fb-4f09-466e-a7ea-dd95e0f3048d"}
										],
									"links":[
											{"rel":"next","uri":"http://localhost:8080/openmrs/ws/rest/v1/location?tag=b8bbf83e-645f-451f-8efe-a0db56f09676&v=custom%3Adisplay%2Cuuid&startIndex=10000"}
									]
			};

			

			var programResponse = {
									"display":"Test Program",
									"uuid":"222e229b-f011-45c5-940e-e5599383c967",
									"outcomesConcept":null,
									"workflows":[
										{
											"uuid":"20696d25-8312-4e95-91cf-01da0a734b8b",
											"concept":{
												"display":"PRP Services Provision Workflow"
											},
											"states":[
												{
													"uuid":"21e3314d-cfba-4b15-addf-149a62647f64",
													"initial":false,
													"terminal":true,
													"concept":{
														"display":"EOS Closed Following Abandon"
													}
												},
												{
													"uuid":"24fa85e3-af4e-49a1-8a06-fbe70ad82bed",
													"initial":false,
													"terminal":false,
													"concept":{
														"display":"Services Approved"
													}
												},
												{
													"uuid":"1bfa573a-a801-4368-984d-1e46aa88de19",
													"initial":false,
													"terminal":true,
													"concept":{
														"display":"Not Eligbile For Services"
													}
												},
												{
													"uuid":"660eb32c-303b-4749-8029-2ee2f488a5e7",
													"initial":false,
													"terminal":false,
													"concept":{
														"display":"Eligible For Services"
													}
												},
												{
													"uuid":"ff9d33d1-c7f6-417e-8d10-a0addc0ee7ed",
													"initial":true,
													"terminal":false,
													"concept":{
														"display":"Awaiting MIA"
													}
												},
												{
													"uuid":"596804f9-de27-4b5d-b546-9b8fc96885fb",
													"initial":false,
													"terminal":false,
													"concept":{
														"display":"Pending Services Financing"
													}
												},
												{
													"uuid":"78e5178a-1c86-4f8c-a8ab-1665cae67911",
													"initial":false,
													"terminal":false,
													"concept":{
														"display":"Referred"
													}
												},
												{
													"uuid":"ef2d5378-2e4c-44f1-a7e0-f915617d1412",
													"initial":false,
													"terminal":false,
													"concept":{
														"display":"Discharged From Services"
													}
												},
												{
													"uuid":"5f04293f-b04f-4bd4-81c9-bf7251f39b47",
													"initial":false,
													"terminal":true,
													"concept":{
														"display":"EOS Closed"
													}
												},
												{
													"uuid":"8083d683-e6a1-4149-a85d-5f3ab507751f",
													"initial":false,
													"terminal":false,
													"concept":{
														"display":"Services Planned"
													}
												}
											]
										},
										//second workflow
										{
											"uuid":"445285c8-1997-443e-a6cd-a0ab02947db8",
											"concept":{
												"display":"PRP Services Follow Up Workflow"
											},
											"states":[
												{
													"uuid":"74855097-fe8a-460e-b69b-e8ce06da0a7e",
													"initial":true,
													"terminal":false,
													"concept":{
														"display":"Awaiting Follow Up"
													}
												},
												{
													"uuid":"4a7e3d49-8957-4d47-a433-6bf4a50fe805",
													"initial":false,
													"terminal":false,
													"concept":{
														"display":"Follow Up Done"
													}
												},
												{
													"uuid":"9d06e5a6-e744-42f7-be73-9fb016dde67d",
													"initial":false,
													"terminal":true,
													"concept":{
														"display":"EOS Closed"
													}
												},
												{
													"uuid":"cf56de14-e31a-46df-9089-d796b9f84b55",
													"initial":false,
													"terminal":true,
													"concept":{
														"display":"EOS Closed Following Abandon"
													}
												},
												{
													"uuid":"3b774570-2912-4f70-8877-3d1eaae92d45",
													"initial":false,
													"terminal":false,
													"concept":{
														"display":"Repair Done"
													}
												}
											]
										}
									]	
			};
			
			var programEnrollmentResponse = {
												"results":[
													{"program":{
														"uuid":"222e229b-f011-45c5-940e-e5599383c967",
														"display":"Test Program"
													},
													"dateEnrolled":"2020-02-18T18:24:31.000-0800",
													"dateCompleted":null}
												]
											};

			var programStatesResponse = {
											"results":[
												{
													"uuid":"5fc06904-e759-4403-9414-eb97541c866d",
													"program":{
														"uuid":"222e229b-f011-45c5-940e-e5599383c967"
													},
													"dateEnrolled":"2020-02-18T18:24:31.000-0800",
													"dateCompleted":null,
													"outcome":null,
													"location":{
														"display":"Test Location",
														"uuid":"8fe901fb-4f09-466e-a7ea-dd95e0f3048d"
													},
													"states":[
														{
															"uuid":"0c9257d8-9638-4286-adf3-c828dc995063",
															"startDate":"2020-02-18T00:00:00.000-0800",
															"endDate":"2020-02-18T00:00:00.000-0800",
															"dateCreated":"2020-02-18T18:24:37.000-0800",
															"voided":false,
															"state":{
																"uuid":"ff9d33d1-c7f6-417e-8d10-a0addc0ee7ed",
																"concept":{
																	"display":"Awaiting MIA"
																}
															}
														},
														{
															"uuid":"85adeddd-7a44-4e5c-a52a-45de762c899f",
															"startDate":"2020-02-20T00:00:00.000-0800",
															"endDate":null,
															"dateCreated":"2020-02-20T21:05:28.000-0800",
															"voided":false,
															"state":{
																"uuid":"9d06e5a6-e744-42f7-be73-9fb016dde67d",
																"concept":{
																	"display":"EOS Closed"
																}
															}
														},
														{
															"uuid":"bbac6c72-83db-4a0a-8481-c29ddcbc7013",
															"startDate":"2020-02-18T00:00:00.000-0800",
															"endDate":"2020-02-18T00:00:00.000-0800",
															"dateCreated":"2020-02-18T18:25:43.000-0800",
															"voided":false,
															"state":{
																"uuid":"8083d683-e6a1-4149-a85d-5f3ab507751f",
																"concept":{
																	"display":"Services Planned"
																}
															}
														},
														{
															"uuid":"4a0c1636-ae88-492d-9b3c-5658116e04e4",
															"startDate":"2020-02-18T00:00:00.000-0800",
															"endDate":"2020-02-18T00:00:00.000-0800",
															"dateCreated":"2020-02-18T18:26:53.000-0800",
															"voided":false,
															"state":{
																"uuid":"24fa85e3-af4e-49a1-8a06-fbe70ad82bed",
																"concept":{
																	"display":"Services Approved"
																}
															}
														},
														{
															"uuid":"de5045ea-afe1-4581-b689-b8d0996838e0",
															"startDate":"2020-02-19T00:00:00.000-0800",
															"endDate":"2020-02-20T00:00:00.000-0800",
															"dateCreated":"2020-02-19T16:45:03.000-0800",
															"voided":false,
															"state":{
																"uuid":"3b774570-2912-4f70-8877-3d1eaae92d45",
																"concept":{
																	"display":"Repair Done"
																}
															}
														},
														{
															"uuid":"300a9ffc-bb88-454f-a295-32fdf3c847cd",
															"startDate":"2020-02-19T00:00:00.000-0800",
															"endDate":null,
															"dateCreated":"2020-02-18T18:31:40.000-0800",
															"voided":true,
															"state":{
																"uuid":"9d06e5a6-e744-42f7-be73-9fb016dde67d",
																"concept":{
																	"display":"EOS Closed"
																}
															}
														},
														{
															"uuid":"560db18b-9f4b-4dae-99ee-5a1e2f89f002",
															"startDate":"2020-02-20T00:00:00.000-0800",
															"endDate":"2020-02-20T00:00:00.000-0800",
															"dateCreated":"2020-02-20T20:48:18.000-0800",
															"voided":false,
															"state":{
																"uuid":"3b774570-2912-4f70-8877-3d1eaae92d45",
																"concept":{
																	"display":"Repair Done"
																}
															}
														},
														{
															"uuid":"d29928fd-e217-422c-b5db-c2236a15b5d5",
															"startDate":"2020-02-18T00:00:00.000-0800",
															"endDate":"2020-02-18T00:00:00.000-0800",
															"dateCreated":"2020-02-18T18:24:53.000-0800",
															"voided":false,
															"state":{
																"uuid":"660eb32c-303b-4749-8029-2ee2f488a5e7",
																"concept":{
																	"display":"Eligible For Services"
																}
															}
														},
														{
															"uuid":"c31d6c0e-a646-42a3-97d5-d7d282783155",
															"startDate":"2020-02-18T00:00:00.000-0800",
															"endDate":"2020-02-19T00:00:00.000-0800",
															"dateCreated":"2020-02-18T18:27:43.000-0800",
															"voided":false,
															"state":{
																"uuid":"74855097-fe8a-460e-b69b-e8ce06da0a7e",
																"concept":{
																	"display":"Awaiting Follow Up"
																}
															}
														},
														{
															"uuid":"4f4f9e91-b3d1-4f32-a660-18e9a2eb9be6",
															"startDate":"2020-02-18T00:00:00.000-0800",
															"endDate":null,
															"dateCreated":"2020-02-18T18:27:43.000-0800",
															"voided":false,
															"state":{
																"uuid":"ef2d5378-2e4c-44f1-a7e0-f915617d1412",
																"concept":{
																	"display":"Discharged From Services"
																}
															}
														},
														{
															"uuid":"e872354b-9dd9-4625-b73a-7510d5be6911",
															"startDate":"2020-02-20T00:00:00.000-0800",
															"endDate":"2020-02-20T00:00:00.000-0800",
															"dateCreated":"2020-02-20T20:41:03.000-0800",
															"voided":false,
															"state":{
																"uuid":"4a7e3d49-8957-4d47-a433-6bf4a50fe805",
																"concept":{
																	"display":"Follow Up Done"
																}
															}
														},
														{
															"uuid":"6c8b0db3-9526-4452-b0ad-df1272d4921b",
															"startDate":"2020-02-20T00:00:00.000-0800",
															"endDate":"2020-02-20T00:00:00.000-0800",
															"dateCreated":"2020-02-20T20:50:12.000-0800",
															"voided":false,
															"state":{
																"uuid":"4a7e3d49-8957-4d47-a433-6bf4a50fe805",
																"concept":{
																	"display":"Follow Up Done"
																}
															}
														}
													]
												}
											]
										};

		$httpBackend.whenGET('/ws/rest/v1/session').respond(sessionResponse);
		$httpBackend.whenGET('/ws/rest/v1/session?v=custom:(privileges:(name))').respond(sessionResponse);
		$httpBackend.whenGET('/ws/rest/v1/appui/session?v=custom:name,uuid').respond(sessionResponse);

		$httpBackend.whenGET('/ws/rest/v1/location?tag=fb6f5fe0-904b-4a85-9806-d72ec139f9de&v=custom:display,uuid').respond(locationResponse);
		$httpBackend.whenGET('/ws/rest/v1/program/222e229b-f011-45c5-940e-e5599383c967?v=custom:display,uuid,outcomesConcept:(uuid),workflows:(uuid,concept:(display),states:(uuid,initial,terminal,concept:(display))').respond(programResponse);

		$httpBackend.whenGET('/ws/rest/v1/programenrollment?patient=919caaf7-122d-4001-8b53-ecdce347c8a5&v=custom:uuid,program:(uuid),dateEnrolled,dateCompleted,outcome:(display),location:(display,uuid),dateCompleted,outcome,states:(uuid,startDate,endDate,dateCreated,voided,state:(uuid,concept:(display)))').respond(programStatesResponse);
		
	});

    it('should sort first by start date, second by end date, and third on date created', () => {
		let bindings = {
						config: {
							icon:"icon-stethoscope",
							label:"statusWidget.label",
							widget:"programstatus",
							dateFormat:"dd MMM yyyy",
							program:"222e229b-f011-45c5-940e-e5599383c967",
							locationTag:"fb6f5fe0-904b-4a85-9806-d72ec139f9de",
							enableProgramDashboards:true,
							dashboardPage:"/coreapps/clinicianfacing/patient.page?patientId={{patientUuid}}&dashboard={{dashboard}}",
							patientUuid:"919caaf7-122d-4001-8b53-ecdce347c8a5",
							locale:"en_GB",
							language:"en"
						}
					};
		let ctrl = $componentController('programstatus', {$scope}, bindings);
		
		ctrl.$onInit().then(function(){

			expect(ctrl.patientProgram).not.toBeNull();
			expect(ctrl.patientProgram.states).not.toBeNull();

			var orderedStates = [
				{
					"uuid":"0c9257d8-9638-4286-adf3-c828dc995063",
					"startDate":"2020-02-18T00:00:00.000-0800",
					"endDate":"2020-02-18T00:00:00.000-0800",
					"dateCreated":"2020-02-18T18:24:37.000-0800",
					"voided":false,
					"state":{
						"uuid":"ff9d33d1-c7f6-417e-8d10-a0addc0ee7ed",
						"concept":{
							"display":"Awaiting MIA"
						}
					}
				},
				{
					"uuid":"d29928fd-e217-422c-b5db-c2236a15b5d5",
					"startDate":"2020-02-18T00:00:00.000-0800",
					"endDate":"2020-02-18T00:00:00.000-0800",
					"dateCreated":"2020-02-18T18:24:53.000-0800",
					"voided":false,
					"state":{
						"uuid":"660eb32c-303b-4749-8029-2ee2f488a5e7",
						"concept":{
							"display":"Eligible For Services"
						}
					}
				},
				{
					"uuid":"bbac6c72-83db-4a0a-8481-c29ddcbc7013",
					"startDate":"2020-02-18T00:00:00.000-0800",
					"endDate":"2020-02-18T00:00:00.000-0800",
					"dateCreated":"2020-02-18T18:25:43.000-0800",
					"voided":false,
					"state":{
						"uuid":"8083d683-e6a1-4149-a85d-5f3ab507751f",
						"concept":{
							"display":"Services Planned"
						}
					}
				},
				{
					"uuid":"4a0c1636-ae88-492d-9b3c-5658116e04e4",
					"startDate":"2020-02-18T00:00:00.000-0800",
					"endDate":"2020-02-18T00:00:00.000-0800",
					"dateCreated":"2020-02-18T18:26:53.000-0800",
					"voided":false,
					"state":{
						"uuid":"24fa85e3-af4e-49a1-8a06-fbe70ad82bed",
						"concept":{
							"display":"Services Approved"
						}
					}
				},
				{
					"uuid":"c31d6c0e-a646-42a3-97d5-d7d282783155",
					"startDate":"2020-02-18T00:00:00.000-0800",
					"endDate":"2020-02-19T00:00:00.000-0800",
					"dateCreated":"2020-02-18T18:27:43.000-0800",
					"voided":false,
					"state":{
						"uuid":"74855097-fe8a-460e-b69b-e8ce06da0a7e",
						"concept":{
							"display":"Awaiting Follow Up"
						}
					}
				},
				{
					"uuid":"de5045ea-afe1-4581-b689-b8d0996838e0",
					"startDate":"2020-02-19T00:00:00.000-0800",
					"endDate":"2020-02-20T00:00:00.000-0800",
					"dateCreated":"2020-02-19T16:45:03.000-0800",
					"voided":false,
					"state":{
						"uuid":"3b774570-2912-4f70-8877-3d1eaae92d45",
						"concept":{
							"display":"Repair Done"
						}
					}
				},
				{
					"uuid":"e872354b-9dd9-4625-b73a-7510d5be6911",
					"startDate":"2020-02-20T00:00:00.000-0800",
					"endDate":"2020-02-20T00:00:00.000-0800",
					"dateCreated":"2020-02-20T20:41:03.000-0800",
					"voided":false,
					"state":{
						"uuid":"4a7e3d49-8957-4d47-a433-6bf4a50fe805",
						"concept":{
							"display":"Follow Up Done"
						}
					}
				},
				{
					"uuid":"560db18b-9f4b-4dae-99ee-5a1e2f89f002",
					"startDate":"2020-02-20T00:00:00.000-0800",
					"endDate":"2020-02-20T00:00:00.000-0800",
					"dateCreated":"2020-02-20T20:48:18.000-0800",
					"voided":false,
					"state":{
						"uuid":"3b774570-2912-4f70-8877-3d1eaae92d45",
						"concept":{
							"display":"Repair Done"
						}
					}
				},
				{
					"uuid":"6c8b0db3-9526-4452-b0ad-df1272d4921b",
					"startDate":"2020-02-20T00:00:00.000-0800",
					"endDate":"2020-02-20T00:00:00.000-0800",
					"dateCreated":"2020-02-20T20:50:12.000-0800",
					"voided":false,
					"state":{
						"uuid":"4a7e3d49-8957-4d47-a433-6bf4a50fe805",
						"concept":{
							"display":"Follow Up Done"
						}
					}
				},
				{
					"uuid":"85adeddd-7a44-4e5c-a52a-45de762c899f",
					"startDate":"2020-02-20T00:00:00.000-0800",
					"endDate":null,
					"dateCreated":"2020-02-20T21:05:28.000-0800",
					"voided":false,
					"state":{
						"uuid":"9d06e5a6-e744-42f7-be73-9fb016dde67d",
						"concept":{
							"display":"EOS Closed"
						}
					}
				},
				{
					"uuid":"4f4f9e91-b3d1-4f32-a660-18e9a2eb9be6",
					"startDate":"2020-02-18T00:00:00.000-0800",
					"endDate":null,
					"dateCreated":"2020-02-18T18:27:43.000-0800",
					"voided":false,
					"state":{
						"uuid":"ef2d5378-2e4c-44f1-a7e0-f915617d1412",
						"concept":{
							"display":"Discharged From Services"
						}
					}
				}
			];

			expect(ctrl.patientProgram.states).toEqual(orderedStates);
		});

		$httpBackend.flush();
    });
	
});
