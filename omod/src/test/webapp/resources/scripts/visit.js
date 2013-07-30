describe("Visit javascript functions", function() {

    // temporarily ignored (this has been broken for a long time, but nobody noticed since we didn't have jasmine enabled fort this module)
    // the problem is that emr.js which is called here is from the uicommons module, and I don't know how to reference that from here.
//    it("should create the creation form dialog object", function() {
//        spyOn(emr, 'setupConfirmationDialog');
//        quickVisitCreationDialogMock = jasmine.createSpyObj('dialog', ['close']);
//        emr.setupConfirmationDialog.andReturn(quickVisitCreationDialogMock);
//
//        visit.createQuickVisitCreationDialog();
//
//        expect(emr.setupConfirmationDialog).toHaveBeenCalled();
//        expect(quickVisitCreationDialogMock.close).toHaveBeenCalled();
//    });

    it("should display the quick visit creation form dialog", function() {
        visit.quickVisitCreationDialog = jasmine.createSpyObj('dialog', ['show']);
        visit.showQuickVisitCreationDialog();
        expect(visit.quickVisitCreationDialog.show).toHaveBeenCalled();
    });
})