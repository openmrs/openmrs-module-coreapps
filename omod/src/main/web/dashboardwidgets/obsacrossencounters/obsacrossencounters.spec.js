import ObsAcrossEncounters from './';
import 'angular-mocks';
import timezoneMock from 'timezone-mock';

// mockEncounters has obs with the following concept UUIDs,
// grouped into encounters as follows.
//
// Registration
//   - uuid-numeric
// Consult
//   - uuid-numeric
//   - uuid-coded
//   - uuid-construct
//     - uuid-drug
//     - uuid-text
//   - uuid-construct
//     - uuid-text
// Consult
//   - uuid-numeric
const mockEncounters = [
    {
        "uuid": "1a742d56-6528-4eac-b3a5-b222f2120ffa",
        "encounterDatetime": "2021-02-01T12:00:00.000-0000",
        "encounterType": {
            "name": "Registration",
            "description": "Patient registration -- normally a new patient"
        },
        "obs": [
            {
                "id": 40,
                "uuid": "aaaaaaaa-b9f5-4f87-8a3b-308a2402dfff",
                "concept": {
                    "id": 4746,
                    "uuid": "uuid-numeric",
                    "name": {
                        "display": "Height (cm)"
                    },
                    "datatype": {
                        "uuid": "8d4a4488-c2cc-11de-8d13-0010c6dffd0f"
                    }
                },
                "value": 165,
                "groupMembers": null
            }
        ]
    },
    {
        "uuid": "54654bb2-b1ef-42ab-b4dd-9edafa6ea775",
        "encounterDatetime": "2021-03-04T12:02:00.000-0000",
        "encounterType": {
            "name": "Consult",
            "description": "A consultation with a doctor"
        },
        "obs": [
            {
                "id": 45,
                "uuid": "22d2c210-b9f5-4f87-8a3b-308a2402d22a",
                "concept": {
                    "id": 4746,
                    "uuid": "uuid-numeric",
                    "name": {
                        "display": "Height (cm)"
                    },
                    "datatype": {
                        "uuid": "8d4a4488-c2cc-11de-8d13-0010c6dffd0f"
                    }
                },
                "value": 170,
                "groupMembers": null
            },
            {
                "id": 46,
                "uuid": "b31fe1c3-9fa1-4282-abab-a042f89e5c12",
                "concept": {
                    "id": 1628,
                    "uuid": "uuid-coded",
                    "name": {
                        "display": "Foot examination results"
                    },
                    "datatype": {
                        "uuid": "8d4a48b6-c2cc-11de-8d13-0010c6dffd0f"
                    }
                },
                "value": {
                    "uuid": "uuid-normal",
                    "display": "Normal",
                    "name": {
                        "display": "Normal",
                        "uuid": "123484BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
                        "name": "Normal",
                        "locale": "en",
                        "localePreferred": true,
                        "conceptNameType": "FULLY_SPECIFIED",
                        "resourceVersion": "1.9"
                    },
                    "datatype": {
                        "uuid": "8d4a4c94-c2cc-11de-8d13-0010c6dffd0f",
                        "display": "N/A",
                    },
                    "conceptClass": {
                        "uuid": "8d492774-c2cc-11de-8d13-0010c6dffd0f",
                        "display": "Misc",
                    },
                    "set": false,
                    "version": null,
                    "retired": false,
                    "names": [
                        {
                            "uuid": "f5d7fe72-d5db-102d-ad2a-000c29c2a5d7",
                            "display": "Normal",
                        },
                        {
                            "uuid": "3e157fbe-26fe-102b-80cb-0017a47871b2",
                            "display": "nrml",
                        }
                    ],
                    "descriptions": [
                        {
                            "uuid": "ece4b10e-07fe-102c-b5fa-0017a47871b2",
                            "display": "General descriptive answer.",
                        }
                    ],
                    "mappings": [
                        {
                            "uuid": "7b068ac4-466a-4de6-8224-f6eddcdce7b5",
                            "display": "CIEL: 1115"
                        },
                        {
                            "uuid": "137296ABBBBBBBBBBBBBBBBBBBBBBBBBBBBB",
                            "display": "SNOMED CT: 17621005"
                        }
                    ],
                    "answers": [],
                    "setMembers": [],
                    "attributes": [],
                    "resourceVersion": "2.0"
                },
                "groupMembers": null
            },
            {
                "id": 47,
                "uuid": "c38fde36-ece7-49be-900e-46e66928da85",
                "value": null,
                "concept": {
                    "id": 279,
                    "uuid": "uuid-construct",
                    "name": {
                        "display": "Dispensing construct"
                    },
                    "datatype": {
                        "uuid": "8d4a4c94-c2cc-11de-8d13-0010c6dffd0f"
                    }
                },
                "groupMembers": [
                    {
                        "id": 48,
                        "uuid": "11bf179d-9235-4bca-afb7-f137b79567d7",
                        "display": "Medication order: Paracetamol 500mg - 10 tabletas (Paracetamol)",
                        "concept": {
                            "id": 188,
                            "uuid": "uuid-drug",
                            "name": {
                                "display": "Medication orders"
                            },
                            "datatype": {
                                "uuid": "8d4a48b6-c2cc-11de-8d13-0010c6dffd0f"
                            }
                        },
                        "value": {
                            "display": "Paracetamol 500mg - 10 tabletas",
                            "uuid": "3d8dd9d2-aad7-40d3-9287-c0911175f42b",
                            "name": "Paracetamol 500mg - 10 tabletas",
                            "description": null,
                            "retired": false,
                            "dosageForm": null,
                            "maximumDailyDose": null,
                            "minimumDailyDose": null,
                            "concept": {
                                "uuid": "3cccd4d6-26fe-102b-80cb-0017a47871b2",
                                "display": "Paracetamol"
                            },
                            "combination": false,
                            "strength": null,
                            "drugReferenceMaps": [],
                            "ingredients": [],
                            "resourceVersion": "1.12"
                        }
                    },
                    {
                        "id": 50,
                        "uuid": "0be36014-df7e-457d-945d-9f919d10c4f2",
                        "display": "Prescription instructions, non-coded: Take with food",
                        "concept": {
                            "id": 231,
                            "uuid": "uuid-text",
                            "name": {
                                "display": "Prescription instructions, non-coded"
                            },
                            "datatype": {
                                "uuid": "8d4a4ab4-c2cc-11de-8d13-0010c6dffd0f"
                            }
                        },
                        "value": "Take with food"
                    }
                ]
            },
            {
                "id": 101,
                "uuid": "c38fde36-ece7-49be-900e-46e66928da85",
                "value": null,
                "concept": {
                    "id": 279,
                    "uuid": "uuid-construct",
                    "name": {
                        "display": "Dispensing construct"
                    },
                    "datatype": {
                        "uuid": "8d4a4c94-c2cc-11de-8d13-0010c6dffd0f"
                    }
                },
                "groupMembers": [
                    {
                        "id": 102,
                        "uuid": "0be36014-df7e-457d-945d-9f919d10c4f2",
                        "display": "Prescription instructions, non-coded: As needed",
                        "concept": {
                            "id": 231,
                            "uuid": "uuid-text",
                            "name": {
                                "display": "Prescription instructions, non-coded"
                            },
                            "datatype": {
                                "uuid": "8d4a4ab4-c2cc-11de-8d13-0010c6dffd0f"
                            }
                        },
                        "value": "As needed"
                    }
                ]
            }
        ]
    },
    {
        "uuid": "12354bb2-b1ef-42ab-b4dd-9edafa6eaaaa",
        "encounterDatetime": "2021-03-15T12:03:00.000-0000",
        "encounterType": {
            "name": "Consult",
            "description": "A consultation with a doctor"
        },
        "obs": [
            {
                "id": 51,
                "uuid": "12d99999-b9f5-4f87-8a3b-308a2402d23b",
                "concept": {
                    "id": 4746,
                    "uuid": "uuid-numeric",
                    "name": {
                        "display": "Height (cm)"
                    },
                    "datatype": {
                        "uuid": "8d4a4488-c2cc-11de-8d13-0010c6dffd0f"
                    }
                },
                "value": 175,
                "groupMembers": null
            }
        ]
    },
    {
        "uuid": "a8b338cc-3978-43eb-99b5-f1c601ac7e76",
        "encounterDatetime": "2021-06-01T09:00:00.000-0000",
        "encounterType": {
            "name": "Report",
            "description": "Patient operation report"
        },
        "obs": [
            {
                "id": 60,
                "uuid": "c098deb2-187c-4bfe-813a-cb2e1a4138db",
                "display": "Affected limb, non-coded: Left arm",
                "concept": {
                    "id": 231,
                    "uuid": "uuid-limb-text",
                    "name": {
                        "display": "Prescription instructions, non-coded"
                    },
                    "datatype": {
                        "uuid": "8d4a4ab4-c2cc-11de-8d13-0010c6dffd0f"
                    }
                },
                "value": "Left arm"
            },
            {
                "id": 61,
                "uuid": "c7ee73d9-9aaa-498f-a707-4b5ea8aaa076",
                "display": "Affected limb, non-coded: Left leg",
                "concept": {
                    "id": 231,
                    "uuid": "uuid-limb-text",
                    "name": {
                        "display": "Affected limb, non-coded"
                    },
                    "datatype": {
                        "uuid": "8d4a4ab4-c2cc-11de-8d13-0010c6dffd0f"
                    }
                },
                "value": "Left leg"
            }
        ]
    }
];

const mockConcepts = {
    'uuid-numeric': {
        "uuid": "uuid-numeric",
        "display": "Height (cm)",
        "names": [
            {
                "voided": false,
                "locale": "en",
                "conceptNameType": "Short",
                "localePreferred": false,
                "name": "Height"
            },
            {
                "voided": false,
                "locale": "en",
                "conceptNameType": "FULLY_SPECIFIED",
                "localePreferred": true,
                "name": "Height (cm)"
            }
        ]
    },
    'uuid-coded': {
        "uuid": "uuid-coded",
        "display": "Foot examination results",
        "names": [
            {
                "voided": false,
                "locale": "es",
                "conceptNameType": "FULLY_SPECIFIED",
                "localePreferred": true,
                "name": "Examen de pie"
            },
            {
                "voided": false,
                "locale": "en",
                "conceptNameType": "FULLY_SPECIFIED",
                "localePreferred": true,
                "name": "Foot examination results"
            }
        ]
    },
    'uuid-construct': {
        "uuid": "uuid-construct",
        "display": "Dispensing construct",
        "names": [
            {
                "voided": false,
                "locale": "en",
                "conceptNameType": "FULLY_SPECIFIED",
                "localePreferred": true,
                "name": "Dispensing construct"
            }
        ]
    },
    'uuid-drug': {
        "uuid": "uuid-drug",
        "display": "Medication orders",
        "names": [
            {
                "voided": false,
                "locale": "en",
                "conceptNameType": "FULLY_SPECIFIED",
                "localePreferred": true,
                "name": "Medication orders"
            }
        ]
    },
    'uuid-text': {
        "uuid": "uuid-text",
        "display": "Prescription instructions, non-coded",
        "names": [
            {
                "voided": false,
                "locale": "en",
                "conceptNameType": "FULLY_SPECIFIED",
                "localePreferred": true,
                "name": "Prescription instructions, non-coded"
            }
        ]
    },
    'uuid-limb-text': {
        "uuid": "uuid-limb-text",
        "display": "Affected limb, non-coded",
        "names": [
            {
                "voided": false,
                "locale": "en",
                "conceptNameType": "FULLY_SPECIFIED",
                "localePreferred": true,
                "name": "Affected limb, non-coded"
            }
        ]
    },
    'uuid-normal': {
        "uuid": "uuid-normal",
        "display": "Normal",
        "names": [
            {
                "voided": false,
                "locale": "en",
                "conceptNameType": "FULLY_SPECIFIED",
                "localePreferred": true,
                "name": "Normal"
            },
            {
                "voided": false,
                "locale": "en",
                "conceptNameType": "SHORT",
                "localePreferred": false,
                "name": "nrml"
            }
        ]
    }
};

const fakeGetFunction = (mockEncounters) => {
    return (url, options) => {
        if (url.startsWith('concept')) {
            const conceptUuid = url.split('/')[1]
            if (mockConcepts[conceptUuid]) {
                return Promise.resolve(mockConcepts[conceptUuid]);
            }
            const error = "Unexpected REST concept UUID: " + conceptUuid;
            console.log(error);
            return Promise.reject(error);
        }
        if (url === 'encounter') {
            const results = options.encounterType ?
                mockEncounters.filter(e =>  e.encounterType.name === options.encounterType) :
                mockEncounters;
            return Promise.resolve({ results: results });
        }
        if (url === 'session?') {
            return Promise.resolve({ locale: 'en' });
        }
        const error = "Unexpected REST .get() call: " + url;
        console.log(error)
        return Promise.reject(error);
    }
}

describe('ObsAcrossEncounters', () => {
    let $componentController;
    let $scope;
    let $httpBackend;

    beforeEach(() => {
        angular.mock.module(ObsAcrossEncounters);
        inject((_$componentController_, $rootScope, _$httpBackend_) => {
            $componentController = _$componentController_;
            $scope = $rootScope.$new();
            $httpBackend = _$httpBackend_;
        });

        timezoneMock.register('US/Eastern');

        $httpBackend.expectGET('/module/uicommons/messages/messages.json?localeKey=en').respond({});
    });

    afterEach(() => {
        timezoneMock.unregister()
    });

    it('should make the expected requests with minimum config', () => {
        let bindings = { config: { concepts: 'uuid-numeric, uuid-text', patientUuid: 'some-patient-uuid' }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));

        return ctrl.$onInit().then(() => {
            expect(ctrl.openmrsRest.get.calls.count()).toBe(4);
            const calls = ctrl.openmrsRest.get.calls.allArgs();
            const encCalls = calls.filter(c => c[0] === 'encounter');
            const cNumericCalls = calls.filter(c => c[0] === 'concept/uuid-numeric');
            const cTextCalls = calls.filter(c => c[0] === 'concept/uuid-text');
            expect(encCalls.length).toBe(1);
            expect(cNumericCalls.length).toBe(1);
            expect(cTextCalls.length).toBe(1);
            expect(encCalls[0][1].patient).toBe('some-patient-uuid');
            expect(encCalls[0][1].limit).toBe(4);  // the default limit
            expect(encCalls[0][1].fromdate).toBeNull();
            expect(encCalls[0][1].encounterType).toEqual('');
        });
    });

    it('should make the expected requests with encounterType, maxAge, and maxRecords specified', () => {
        let bindings = { config: {
            concepts: 'uuid-coded', patientUuid: 'some-patient-uuid', encounterType: 'et-1', maxAge: '2y', maxRecords: 8
        }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));
        jasmine.clock().mockDate(new Date('2021-03-01'));

        return ctrl.$onInit().then(() => {
            expect(ctrl.openmrsRest.get.calls.count()).toBe(3);
            const calls = ctrl.openmrsRest.get.calls.allArgs();
            const encCalls = calls.filter(c => c[0] === 'encounter');
            const cCodedCalls = calls.filter(c => c[0] === 'concept/uuid-coded');
            expect(encCalls.length).toBe(1);
            expect(cCodedCalls.length).toBe(1);
            const encCallArgs = encCalls[0][1];
            expect(encCallArgs.patient).toBe('some-patient-uuid');
            expect(encCallArgs.limit).toBe(8);
            expect(encCallArgs.fromdate).toEqual(new Date(new Date().setFullYear(new Date().getFullYear() - 2)));
            expect(encCallArgs.encounterType).toBe('et-1');
        });
    });

    it('should make the expected requests with encounterTypes specified', () => {
        let bindings = { config: {
                concepts: 'uuid-coded', patientUuid: 'some-patient-uuid', encounterTypes: 'Registration,Consult'
            }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));

        return ctrl.$onInit().then(() => {
            expect(ctrl.openmrsRest.get.calls.count()).toBe(4);
            const calls = ctrl.openmrsRest.get.calls.allArgs();
            const encCalls = calls.filter(c => c[0] === 'encounter');
            expect(encCalls.length).toBe(2);
            expect(encCalls[0][1].encounterType).toBe('Registration');
            expect(encCalls[1][1].encounterType).toBe('Consult');
        });
    });

    it('should produce the expected output for two concepts with any encounter type', () => {
        let bindings = { config: { concepts: 'uuid-numeric, uuid-coded', patientUuid: 'some-patient-uuid' }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));

        return ctrl.$onInit().then(() => {
            expect(ctrl.output.headers).toEqual(['coreapps.date', 'Height (cm)', 'Foot examination results']);
            expect(ctrl.output.rows.length).toEqual(3);
            expect(ctrl.output.rows[0][0]).toEqual({ value: "2021-02-01T07:00:00-05:00"});
            expect(ctrl.output.rows[0][1][0].value).toEqual(165);
            expect(ctrl.output.rows[0][2][0].value).toEqual("");
            expect(ctrl.output.rows[1][0]).toEqual({ value: "2021-03-04T07:02:00-05:00"});
            expect(ctrl.output.rows[1][1][0].value).toEqual(170);
            expect(ctrl.output.rows[1][2][0].value).toEqual("Normal");
            expect(ctrl.output.rows[2][0]).toEqual({ value: "2021-03-15T08:03:00-04:00"});
            expect(ctrl.output.rows[2][1][0].value).toEqual(175);
            expect(ctrl.output.rows[2][2][0].value).toEqual("");
        });
    });

    it('should produce the expected output for two concepts and a single encounter type', () => {
        let bindings = { config: { concepts: 'uuid-coded, uuid-numeric', patientUuid: 'some-patient-uuid', encounterTypes: 'Consult' }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));

        return ctrl.$onInit().then(() => {
            expect(ctrl.output.headers).toEqual(['coreapps.date', 'Foot examination results', 'Height (cm)']);
            expect(ctrl.output.rows.length).toEqual(2);
            expect(ctrl.output.rows[0][0]).toEqual({ value: "2021-03-04T07:02:00-05:00"});
            expect(ctrl.output.rows[0][1][0].value).toEqual("Normal");
            expect(ctrl.output.rows[0][2][0].value).toEqual(170);
            expect(ctrl.output.rows[1][0]).toEqual({ value: "2021-03-15T08:03:00-04:00"});
            expect(ctrl.output.rows[1][1][0].value).toEqual("");
            expect(ctrl.output.rows[1][2][0].value).toEqual(175);
        });
    });

    it('should produce the expected output with showEncounterTypeName', () => {
        let bindings = { config: { concepts: 'uuid-numeric, uuid-coded', patientUuid: 'some-patient-uuid', showEncounterTypeName: true }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));

        return ctrl.$onInit().then(() => {
            expect(ctrl.output.headers).toEqual(['coreapps.patientDashBoard.encounter', 'coreapps.date', 'Height (cm)', 'Foot examination results']);
            expect(ctrl.output.rows.length).toEqual(3);
            expect(ctrl.output.rows[0][0]).toEqual({ value: 'Registration', translate: true });
            expect(ctrl.output.rows[0][1]).toEqual({ value: "2021-02-01T07:00:00-05:00"});
            expect(ctrl.output.rows[0][2][0].value).toEqual(165);
            expect(ctrl.output.rows[0][3][0].value).toEqual("");
            expect(ctrl.output.rows[1][0]).toEqual({ value: 'Consult', translate: true });
            expect(ctrl.output.rows[1][1]).toEqual({ value: "2021-03-04T07:02:00-05:00"});
            expect(ctrl.output.rows[1][2][0].value).toEqual(170);
            expect(ctrl.output.rows[1][3][0].value).toEqual("Normal");
            expect(ctrl.output.rows[2][0]).toEqual({ value: 'Consult', translate: true });
            expect(ctrl.output.rows[2][1]).toEqual({ value: "2021-03-15T08:03:00-04:00"});
            expect(ctrl.output.rows[2][2][0].value).toEqual(175);
            expect(ctrl.output.rows[2][3][0].value).toEqual("");
        });
    });

    it('should respect useConceptShortName', () => {
        let bindings = { config: { concepts: 'uuid-numeric, uuid-coded', patientUuid: 'some-patient-uuid', useConceptShortName: true }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));

        return ctrl.$onInit().then(() => {
            expect(ctrl.output.headers).toEqual(['coreapps.date', 'Height (cm)', 'Foot examination results']);
            expect(ctrl.output.rows.length).toEqual(3);
            expect(ctrl.output.rows[1][2][0].value).toEqual("nrml");
        });
    });

    it('should respect useConceptNameForDrugValues', () => {
        let bindings = { config: { concepts: 'uuid-drug', patientUuid: 'some-patient-uuid', useConceptNameForDrugValues: true }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));

        return ctrl.$onInit().then(() => {
            expect(ctrl.output.headers).toEqual(['coreapps.date', 'Medication orders']);
            expect(ctrl.output.rows.length).toEqual(1);
            expect(ctrl.output.rows[0][1][0].value).toEqual("Paracetamol");
        });
    });

    it('should show separate obs groups on separate lines', () => {
        let bindings = { config: { concepts: 'uuid-drug,uuid-text', patientUuid: 'some-patient-uuid' }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));

        return ctrl.$onInit().then(() => {
            expect(ctrl.output.headers).toEqual(['coreapps.date', 'Medication orders', 'Prescription instructions, non-coded']);
            expect(ctrl.output.rows.length).toEqual(2);
            expect(ctrl.output.rows[0][0]).toEqual({ value: "2021-03-04T07:02:00-05:00"});
            expect(ctrl.output.rows[0][1][0].value).toEqual("Paracetamol 500mg - 10 tabletas");
            expect(ctrl.output.rows[0][2][0].value).toEqual("Take with food");
            expect(ctrl.output.rows[1][0]).toEqual({ value: "2021-03-04T07:02:00-05:00"});
            expect(ctrl.output.rows[1][1][0].value).toEqual("");
            expect(ctrl.output.rows[1][2][0].value).toEqual("As needed");
        });
    });

    it('should show two obs values on same cell when they\'re answer to same concept within same form', () => {
        let bindings = { config: { concepts: 'uuid-limb-text', patientUuid: 'some-patient-uuid', encounterTypes: 'Report' }};
        let ctrl = $componentController('obsacrossencounters', {$scope}, bindings);

        spyOn(ctrl.openmrsRest, "get").and.callFake(fakeGetFunction(mockEncounters));

        return ctrl.$onInit().then(() => {
            expect(ctrl.output.headers).toEqual(['coreapps.date', 'Affected limb, non-coded']);
            expect(ctrl.output.rows.length).toEqual(1);
            expect(ctrl.output.rows[0][0]).toEqual({ value: "2021-06-01T05:00:00-04:00"});
            expect(ctrl.output.rows[0][1][0].value).toEqual("Take with food");
            expect(ctrl.output.rows[0][1][1].value).toEqual("Don't mix with alcohol");
        });
    });
});
