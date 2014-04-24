describe("Diagnoses library", function() {

    var searchResults = [

        {
            "conceptName": {
                "id": 840,
                "conceptNameType": 'FULLY_SPECIFIED',
                "name": "Malaria"
            },
            "concept": {
                "id": 167,
                "conceptMappings": [
                    {
                        "conceptMapType": "SAME-AS",
                        "conceptReferenceTerm": {
                            "code": "123",
                            "name": null,
                            "conceptSource": {
                                "name": "PIH"
                            }
                        }
                    },
                    {
                        "conceptMapType": "SAME-AS",
                        "conceptReferenceTerm": {
                            "code": "MALARIA",
                            "name": null,
                            "conceptSource": {
                                "name": "PIH"
                            }
                        }
                    },
                    {
                        "conceptMapType": "NARROWER-THAN",
                        "conceptReferenceTerm": {
                            "code": "B54",
                            "name": null,
                            "conceptSource": {
                                "name": "ICD-10-WHO"
                            }
                        }
                    }
                ],
                "preferredName": "Malaria"
            },
            "nameIsPreferred": true
        },
        {
            "conceptName": {
                "id": 834,
                "conceptNameType": null,
                "name": "CLINICAL MALARIA"
            },
            "concept": {
                "id": 167,
                "conceptMappings": [
                    {
                        "conceptMapType": "SAME-AS",
                        "conceptReferenceTerm": {
                            "code": "123",
                            "name": null,
                            "conceptSource": {
                                "name": "PIH"
                            }
                        }
                    },
                    {
                        "conceptMapType": "SAME-AS",
                        "conceptReferenceTerm": {
                            "code": "MALARIA",
                            "name": null,
                            "conceptSource": {
                                "name": "PIH"
                            }
                        }
                    },
                    {
                        "conceptMapType": "NARROWER-THAN",
                        "conceptReferenceTerm": {
                            "code": "B54",
                            "name": null,
                            "conceptSource": {
                                "name": "ICD-10-WHO"
                            }
                        }
                    }
                ],
                "preferredName": "Malaria"
            },
            "nameIsPreferred": false
        },
        {
            "conceptName": {
                "id": 670,
                "conceptNameType": "FULLY_SPECIFIED",
                "name": "Confirmed malaria"
            },
            "concept": {
                "id": 136,
                "conceptMappings": [
                    {
                        "conceptMapType": "SAME-AS",
                        "conceptReferenceTerm": {
                            "code": "7646",
                            "name": null,
                            "conceptSource": {
                                "name": "PIH"
                            }
                        }
                    },
                    {
                        "conceptMapType": "NARROWER-THAN",
                        "conceptReferenceTerm": {
                            "code": "B53.8",
                            "name": null,
                            "conceptSource": {
                                "name": "ICD-10-WHO"
                            }
                        }
                    },
                    {
                        "conceptMapType": "SAME-AS",
                        "conceptReferenceTerm": {
                            "code": "Confirmed malaria",
                            "name": null,
                            "conceptSource": {
                                "name": "PIH"
                            }
                        }
                    }
                ],
                "preferredName": "Confirmed malaria"
            },
            "nameIsPreferred": true
        }
    ];

    var encounterDiagnoses;

    beforeEach(function() {
        encounterDiagnoses = diagnoses.EncounterDiagnoses();
    });

    it("should setup correctly", function() {
        expect(encounterDiagnoses.diagnoses.length).toBe(0);
    });

    it("should handle free-text diagnosis", function() {
        var d = diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Swine Flu"));
        encounterDiagnoses.addDiagnosis(d);

        expect(encounterDiagnoses.diagnoses.length).toBe(1);
        expect(encounterDiagnoses.diagnoses[0].valueToSubmit()).toBe(JSON.stringify({
            certainty: "PRESUMED",
            order: "PRIMARY",
            diagnosis: "Non-Coded:Swine Flu",
            existingObs: null
        }));
    });

    it("should handle multiple free-text diagnoses", function() {
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Swine Flu")));
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Bird Flu")));

        expect(encounterDiagnoses.diagnoses.length).toBe(2);
        expect(encounterDiagnoses.diagnoses[0].valueToSubmit()).toBe(JSON.stringify({
            certainty: "PRESUMED",
            order: "PRIMARY",
            diagnosis: "Non-Coded:Swine Flu",
            existingObs: null
        }));
        expect(encounterDiagnoses.diagnoses[1].valueToSubmit()).toBe(JSON.stringify({
            certainty: "PRESUMED",
            order: "SECONDARY",
            diagnosis: "Non-Coded:Bird Flu",
            existingObs: null
        }));
    });

    it("should handle coded diagnosis with specific concept name", function() {
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer(searchResults[0])));

        expect(encounterDiagnoses.diagnoses.length).toBe(1);
        expect(encounterDiagnoses.diagnoses[0].valueToSubmit()).toBe(JSON.stringify({
            certainty: "PRESUMED",
            order: "PRIMARY",
            diagnosis: "ConceptName:840",
            existingObs: null
        }));
    });

    it("should not allow two diagnoses of the same concept", function() {
        expect(encounterDiagnoses.diagnoses.length).toBe(0);
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer(searchResults[0])));
        expect(encounterDiagnoses.diagnoses.length).toBe(1);
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer(searchResults[0])));
        expect(encounterDiagnoses.diagnoses.length).toBe(1);
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer(searchResults[1])));
        expect(encounterDiagnoses.diagnoses.length).toBe(1);
    });

    it("should allow two diagnoses of different concepts", function() {
        expect(encounterDiagnoses.diagnoses.length).toBe(0);
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer(searchResults[0])));
        expect(encounterDiagnoses.diagnoses.length).toBe(1);
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer(searchResults[2])));
        expect(encounterDiagnoses.diagnoses.length).toBe(2);
    });

    it("should get primary diagnoses", function() {
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Swine Flu")));
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Bird Flu")));

        expect(encounterDiagnoses.primaryDiagnoses().length).toBe(1);
        expect(encounterDiagnoses.primaryDiagnoses()[0].diagnosis.matchedName).toBe("Swine Flu");
    });

    it("should get secondary diagnoses", function() {
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Swine Flu")));
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Bird Flu")));

        expect(encounterDiagnoses.secondaryDiagnoses().length).toBe(1);
        expect(encounterDiagnoses.secondaryDiagnoses()[0].diagnosis.matchedName).toBe("Bird Flu");
    });

    it("should remove primary diagnosis and make the top secondary diagnosis primary", function() {
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Swine Flu")));
        encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Bird Flu")));
        expect(encounterDiagnoses.primaryDiagnoses().length).toBe(1);
        expect(encounterDiagnoses.primaryDiagnoses()[0].diagnosis.matchedName).toBe("Swine Flu");
        expect(encounterDiagnoses.secondaryDiagnoses().length).toBe(1);
        expect(encounterDiagnoses.secondaryDiagnoses()[0].diagnosis.matchedName).toBe("Bird Flu");

        encounterDiagnoses.removeDiagnosis(encounterDiagnoses.diagnoses[0]);
        expect(encounterDiagnoses.primaryDiagnoses().length).toBe(1);
        expect(encounterDiagnoses.primaryDiagnoses()[0].diagnosis.matchedName).toBe("Bird Flu");
        expect(encounterDiagnoses.secondaryDiagnoses().length).toBe(0);
    });

    it("should add multiple diagonses", function() {
        var arrayOfDiagnoses = [];
        arrayOfDiagnoses.push(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Swine Flu")));
        arrayOfDiagnoses.push(diagnoses.Diagnosis(diagnoses.CodedOrFreeTextConceptAnswer("Bird Flu")));

        encounterDiagnoses.addDiagnoses(arrayOfDiagnoses);

        expect(encounterDiagnoses.diagnoses.length).toBe(2);
        expect(encounterDiagnoses.diagnoses[0].valueToSubmit()).toBe(JSON.stringify({
            certainty: "PRESUMED",
            order: "PRIMARY",
            diagnosis: "Non-Coded:Swine Flu",
            existingObs: null
        }));
        expect(encounterDiagnoses.diagnoses[1].valueToSubmit()).toBe(JSON.stringify({
            certainty: "PRESUMED",
            order: "SECONDARY",
            diagnosis: "Non-Coded:Bird Flu",
            existingObs: null
        }));
    });
});