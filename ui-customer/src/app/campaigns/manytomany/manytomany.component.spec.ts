import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ManyToManyComponent } from "./manytomany.component";

describe("ManyToManyComponent", () => {
    let component: ManyToManyComponent;
    let fixture: ComponentFixture<ManyToManyComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ManyToManyComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ManyToManyComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
