import LatestObsForConceptList from './';
import 'angular-mocks';


const mockObsResults = [
    {
        "obsDatetime": new Date().setDate(new Date().getDate() - 20),
        "concept": {
            "uuid": "uuid-1",
            "display": "History of alcohol use",
            "datatype": {
                "uuid": "8d4a48b6-c2cc-11de-8d13-0010c6dffd0f"
            },
            "names": [
                {
                    "name": "Estati itilize Alkòl",
                    "locale": "ht",
                    "localePreferred": true,
                    "voided": false,
                    "conceptNameType": "FULLY_SPECIFIED"
                },
                {
                    "name": "Alcohol use",
                    "locale": "en",
                    "localePreferred": false,
                    "voided": false,
                    "conceptNameType": "SHORT"
                },
                {
                    "name": "History of alcohol use",
                    "locale": "en",
                    "localePreferred": true,
                    "voided": false,
                    "conceptNameType": null
                },
                {
                    "name": "Alcohol use status",
                    "locale": "en",
                    "localePreferred": false,
                    "voided": false,
                    "conceptNameType": "FULLY_SPECIFIED"
                }
            ]
        },
        "value": {
            "uuid": "3cdbdb2a-26fe-102b-80cb-0017a47871b2",
            "display": "Currently",
            "names": [
                {
                    "name": "Kounye a",
                    "locale": "ht",
                    "localePreferred": true,
                    "voided": false,
                    "conceptNameType": "FULLY_SPECIFIED"
                },
                {
                    "name": "Currently",
                    "locale": "en",
                    "localePreferred": true,
                    "voided": false,
                    "conceptNameType": "FULLY_SPECIFIED"
                },
                {
                    "name": "Now",
                    "locale": "en",
                    "localePreferred": false,
                    "voided": false,
                    "conceptNameType": "SHORT"
                }
            ]
        },
        "groupMembers": null
    },
    {
        "obsDatetime": new Date().setHours(new Date().getHours() - 1),
        "concept": {
            "uuid": "uuid-2",
            "display": "History of tobacco use",
            "datatype": {
                "uuid": "8d4a48b6-c2cc-11de-8d13-0010c6dffd0f"
            },
            "names": [
                {
                    "name": "History of tobacco use",
                    "locale": "en",
                    "localePreferred": true,
                    "voided": false,
                    "conceptNameType": "FULLY_SPECIFIED"
                }
            ]
        },
        "value": {
            "uuid": "3cdbd832-26fe-102b-80cb-0017a47871b2",
            "display": "In the past",
            "names": [
                {
                    "name": "In the past",
                    "locale": "en",
                    "localePreferred": true,
                    "voided": false,
                    "conceptNameType": "FULLY_SPECIFIED"
                }
            ]
        },
        "groupMembers": null
    }
]

const mockObsGroupResults = [
    {
        "obsDatetime": new Date().setDate(new Date().getDate() - 3),
        "concept": {
            "uuid": "uuid-10",
            "display": "Dispensing construct",
            "datatype": {
                "uuid": "8d4a4c94-c2cc-11de-8d13-0010c6dffd0f"
            },
            "names": [
                {
                    "name": "Dispensing construct",
                    "locale": "en",
                    "localePreferred": true,
                    "voided": false,
                    "conceptNameType": "FULLY_SPECIFIED"
                }
            ]
        },
        "value": null,
        "groupMembers": [
            {
                "value": {
                    "display": "Supplement for pregnancy",
                    "uuid": "c68f9805-5c86-44ac-94d9-7b7ef77e4e19",
                    "name": "Supplement for pregnancy",
                    "concept": {
                        "uuid": "ed69b6e2-63c6-416d-b471-f95c9c5e8847",
                        "display": "Vitamin A",
                    }
                },
                "concept": {
                    "display": "Medication(s) name",
                    "names": [
                        {
                            "name": "Rx",
                            "locale": "en",
                            "localePreferred": false,
                            "voided": false,
                            "conceptNameType": "SHORT"
                        },
                        {
                            "name": "Medication(s) name",
                            "locale": "en",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": null
                        },
                        {
                            "name": "Demande de médicaments",
                            "locale": "fr",
                            "localePreferred": false,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        },
                        {
                            "name": "Medication orders",
                            "locale": "en",
                            "localePreferred": false,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        }
                    ]
                }
            },
            {
                "value": 2,
                "concept": {
                    "display": "Quantity of medication dispensed",
                    "names": [
                        {
                            "name": "Quantité de médicament administrés",
                            "locale": "fr",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        },
                        {
                            "name": "Quantity of medication dispensed",
                            "locale": "en",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        }
                    ]
                }
            },
            {
                "concept": {
                    "display": "History of alcohol use",
                    "names": [
                        {
                            "name": "Estati itilize Alkòl",
                            "locale": "ht",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        },
                        {
                            "name": "Alcohol use",
                            "locale": "en",
                            "localePreferred": false,
                            "voided": false,
                            "conceptNameType": "SHORT"
                        },
                        {
                            "name": "History of alcohol use",
                            "locale": "en",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": null
                        },
                        {
                            "name": "Alcohol use status",
                            "locale": "en",
                            "localePreferred": false,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        }
                    ]
                },
                "value": {
                    "uuid": "3cdbdb2a-26fe-102b-80cb-0017a47871b2",
                    "display": "Currently",
                    "names": [
                        {
                            "name": "Kounye a",
                            "locale": "ht",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        },
                        {
                            "name": "Currently",
                            "locale": "en",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        },
                        {
                            "name": "Now",
                            "locale": "en",
                            "localePreferred": false,
                            "voided": false,
                            "conceptNameType": "SHORT"
                        }
                    ]
                }
            }
        ]
    },
    {
        "obsDatetime": new Date().setDate(new Date().getDate() - 25),
        "concept": {
            "uuid": "uuid-11",
            "display": "Dispensing construct",
            "datatype": {
                "uuid": "8d4a4c94-c2cc-11de-8d13-0010c6dffd0f"
            },
            "names": [
                {
                    "name": "Dispensing construct",
                    "locale": "en",
                    "localePreferred": true,
                    "voided": false,
                    "conceptNameType": "FULLY_SPECIFIED"
                }
            ]
        },
        "value": null,
        "groupMembers": [
            {
                "value": {
                    "display": "Acetaminophen",
                    "uuid": "c6111105-1186-44ac-14d9-7b7ef77e4e19",
                    "name": "Acetaminophen",
                    "concept": {
                        "uuid": "1111b6e2-63c6-416d-b471-f95c9c555847",
                        "display": "Paracetamol",
                    }
                },
                "concept": {
                    "display": "Medication(s) name",
                    "names": [
                        {
                            "name": "Rx",
                            "locale": "en",
                            "localePreferred": false,
                            "voided": false,
                            "conceptNameType": "SHORT"
                        },
                        {
                            "name": "Medication(s) name",
                            "locale": "en",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": null
                        },
                        {
                            "name": "Demande de médicaments",
                            "locale": "fr",
                            "localePreferred": false,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        },
                        {
                            "name": "Medication orders",
                            "locale": "en",
                            "localePreferred": false,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        }
                    ]
                }
            },
            {
                "value": 5,
                "concept": {
                    "display": "Quantity of medication dispensed",
                    "names": [
                        {
                            "name": "Quantité de médicament administrés",
                            "locale": "fr",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        },
                        {
                            "name": "Quantity of medication dispensed",
                            "locale": "en",
                            "localePreferred": true,
                            "voided": false,
                            "conceptNameType": "FULLY_SPECIFIED"
                        }
                    ]
                }
            }
        ]
    }
]

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

    it('should make the expected request', () => {
        let bindings = {config: {concepts: 'uuid-1, uuid-2', patientUuid: 'some-patient-uuid', nLatestObs: 9}};
        let ctrl = $componentController('latestobsforconceptlist', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "list").and.callFake(() => {
            return Promise.resolve({ results: [] });
        });

        return ctrl.$onInit().then(() => {
            expect(ctrl.openmrsRest.list.calls.first().args[0]).toBe('latestobs');
            expect(ctrl.openmrsRest.list.calls.first().args[1].patient).toBe('some-patient-uuid');
            expect(ctrl.openmrsRest.list.calls.first().args[1].concept).toBe('uuid-1,uuid-2');
            expect(ctrl.openmrsRest.list.calls.first().args[1].nLatestObs).toBe(9);
        });

    });

    it('should display normal obs correctly', () => {
        let bindings = {config: { concepts: 'uuid-1, uuid-2', patientUuid: 'patientUuid' }};
        let ctrl = $componentController('latestobsforconceptlist', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "list").and.callFake(() => {
            return Promise.resolve({ results: mockObsResults });
        });

        return ctrl.$onInit().then(() => {
            expect(ctrl.obs.length).toBe(2);
            expect(ctrl.obs[0].conceptName).toBe("History of alcohol use");
            expect(ctrl.obs[0].value).toBe("Currently");
            expect(ctrl.obs[0].obsDaysAgo).toBe("20 days ago");
            expect(ctrl.obs[1].conceptName).toBe("History of tobacco use");
            expect(ctrl.obs[1].value).toBe("In the past");
            expect(ctrl.obs[1].obsDaysAgo).toBe("today");
        });
    });

    it('should display obs groups correctly', () => {
        let bindings = {config: { concepts: 'uuid-10', patientUuid: 'patientUuid' }};
        let ctrl = $componentController('latestobsforconceptlist', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "list").and.callFake(() => {
            return Promise.resolve({ results: mockObsGroupResults });
        });

        return ctrl.$onInit().then(() => {
            expect(ctrl.obs.length).toBe(2);
            expect(ctrl.obs[0].groupMembers.length).toBe(3);
            expect(ctrl.obs[0].obsDaysAgo).toBe("3 days ago");
            expect(ctrl.obs[0].groupMembers[0].prefix).toBe("");
            expect(ctrl.obs[0].groupMembers[0].value).toBe("Supplement for pregnancy");
            expect(ctrl.obs[0].groupMembers[1].prefix).toBe("");
            expect(ctrl.obs[0].groupMembers[1].value).toBe(2);
            expect(ctrl.obs[0].groupMembers[2].prefix).toBe("");
            expect(ctrl.obs[0].groupMembers[2].value).toBe("Currently");
            expect(ctrl.obs[1].groupMembers.length).toBe(2);
            expect(ctrl.obs[1].obsDaysAgo).toBe("25 days ago");
            expect(ctrl.obs[1].groupMembers[0].prefix).toBe("");
            expect(ctrl.obs[1].groupMembers[0].value).toBe("Acetaminophen");
            expect(ctrl.obs[1].groupMembers[1].prefix).toBe("");
            expect(ctrl.obs[1].groupMembers[1].value).toBe(5);
        });
    });

    it('should display FSNs correctly', () => {
        let bindings = { config: {
                concepts: 'uuid-10', patientUuid: 'patientUuid', obsGroupLabels: 'FSN', conceptNameType: 'FSN'
            }};
        let ctrl = $componentController('latestobsforconceptlist', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, 'list').and.callFake(() => {
            return Promise.resolve({results: mockObsGroupResults});
        });

        return ctrl.$onInit().then(() => {
            expect(ctrl.obs[0].groupMembers[0].prefix).toBe("(Medication(s) name) ");
            expect(ctrl.obs[0].groupMembers[0].value).toBe("Supplement for pregnancy");
            expect(ctrl.obs[0].groupMembers[1].prefix).toBe("(Quantity of medication dispensed) ");
            expect(ctrl.obs[0].groupMembers[2].prefix).toBe("(History of alcohol use) ");
            expect(ctrl.obs[0].groupMembers[2].value).toBe("Currently");
            expect(ctrl.obs[1].groupMembers[0].prefix).toBe("(Medication(s) name) ");
            expect(ctrl.obs[1].groupMembers[0].value).toBe("Acetaminophen");
            expect(ctrl.obs[1].groupMembers[1].prefix).toBe("(Quantity of medication dispensed) ");
        });
    });

    it('should display shortNames correctly', () => {
        let bindings = { config: {
                concepts: 'uuid-10', patientUuid: 'patientUuid', obsGroupLabels: 'shortName', conceptNameType: 'shortName'
            }};
        let ctrl = $componentController('latestobsforconceptlist', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, 'list').and.callFake(() => {
            return Promise.resolve({results: mockObsGroupResults});
        });

        return ctrl.$onInit().then(() => {
            expect(ctrl.obs[0].groupMembers[0].prefix).toBe("(Rx) ");
            expect(ctrl.obs[0].groupMembers[0].value).toBe("Supplement for pregnancy");
            expect(ctrl.obs[0].groupMembers[1].prefix).toBe("(Quantity of medication dispensed) ");
            expect(ctrl.obs[0].groupMembers[2].prefix).toBe("(Alcohol use) ");
            expect(ctrl.obs[0].groupMembers[2].value).toBe("Currently");    // TODO: it should actually display 'NOW'
            expect(ctrl.obs[1].groupMembers[0].prefix).toBe("(Rx) ");
            expect(ctrl.obs[1].groupMembers[0].value).toBe("Acetaminophen");
            expect(ctrl.obs[1].groupMembers[1].prefix).toBe("(Quantity of medication dispensed) ");
        });
    });

    it('should respect maxAge parameter', () => {
        let bindings = {config: { maxAge: '2w', concepts: 'uuid-1, uuid-2', patientUuid: 'patientUuid' }};
        let ctrl = $componentController('latestobsforconceptlist', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "list").and.callFake(() => {
            return Promise.resolve({ results: mockObsResults });
        });

        return ctrl.$onInit().then(() => {
            expect(ctrl.obs.length).toBe(1);
            expect(ctrl.obs[0].conceptName).toBe("History of tobacco use")
        });
    });

});