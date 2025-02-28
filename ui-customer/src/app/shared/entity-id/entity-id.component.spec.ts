import { ComponentFixture, TestBed } from "@angular/core/testing";

import { EntityIdComponent } from "./entity-id.component";

describe("EntityIdComponent", () => {
    let component: EntityIdComponent;
    let fixture: ComponentFixture<EntityIdComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [EntityIdComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EntityIdComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
