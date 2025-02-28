import { ComponentFixture, TestBed } from "@angular/core/testing";

import { OneToManyComponent } from "./onetomany.component";

describe("OneToManyComponent", () => {
    let component: OneToManyComponent;
    let fixture: ComponentFixture<OneToManyComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [OneToManyComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(OneToManyComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
