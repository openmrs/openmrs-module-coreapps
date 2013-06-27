describe("Visit javascript functions", function() {

    it("should create the creation form dialog object", function() {
        spyOn(emr, 'setupConfirmationDialog');
        quickVisitCreationDialogMock = jasmine.createSpyObj('dialog', ['close']);
        emr.setupConfirmationDialog.andReturn(quickVisitCreationDialogMock);

        visit.createQuickVisitCreationDialog();

        expect(emr.setupConfirmationDialog).toHaveBeenCalled();
        expect(quickVisitCreationDialogMock.close).toHaveBeenCalled();
    });

    it("should display the quick visit creation form dialog", function() {
        visit.quickVisitCreationDialog = jasmine.createSpyObj('dialog', ['show']);
        visit.showQuickVisitCreationDialog();
        expect(visit.quickVisitCreationDialog.show).toHaveBeenCalled();
    });
})