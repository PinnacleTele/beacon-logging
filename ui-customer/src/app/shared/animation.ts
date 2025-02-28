import {
    trigger,
    transition,
    style,
    animate,
    animateChild,
    query,
    stagger
} from "@angular/animations";
import { CONSTANTS } from "./campaigns.constants";

// used for profiles menu and toast
export const EnterExitRight = [
    trigger("enterExitRight", [
        transition(":enter", [
            style({ opacity: 0, transform: "translateX(100%)" }),
            animate(
                CONSTANTS.SLIDERIN_TIME,
                style({ opacity: 1, transform: "translateX(0%)" })
            )
        ]),
        transition(":leave", [
            animate(
                CONSTANTS.SLIDEROUT_TIME,
                style({ opacity: 0, transform: "translateX(100%)" })
            )
        ])
    ])
];

export const openClose = [
    trigger('openClose', [
        transition(':enter', [
            style({ opacity: 0 }),
            animate(CONSTANTS.POPIN_TIME, style({ opacity: 1 })),
        ]),
        transition(':leave', [
            animate(CONSTANTS.POPOUT_TIME, style({ opacity: 0 }))
        ])
    ])
];
export const Container1 = [
    trigger("container1", [
        transition(":enter, :leave", [
            query("@*", animateChild(), { optional: true })
        ])
    ])
];

export const Container2 = [
    trigger("container2", [
        transition(":enter, :leave", [query("@*", animateChild())])
    ])
];
// used for freqently usedlinks menu in header
export const EnterExitTop = [
    trigger("enterExitTop", [
        transition(":enter", [
            style({ opacity: 0, height: 0, overflow: "hidden" }),

            animate(
                CONSTANTS.SLIDEDOWN_TIME,
                style({ opacity: 1, height: "*" })
            )
        ]),
        transition(":leave", [
            style({ height: "*", overflow: "hidden" }),
            animate(
                CONSTANTS.SLIDEUP_TIME,
                style({ height: 0 }))
        ])
    ])
];
// used for sidebar menu in small screen devices
export const EnterExitLeft = [
    trigger("enterExitLeft", [
        transition(":enter", [
            style({ opacity: 0, transform: "translateX(-100%)" }),
            animate(
                CONSTANTS.SLIDERIN_TIME,
                style({ opacity: 1, transform: "translateX(0%)" })
            )
        ]),
        transition(":leave", [
            animate(
                CONSTANTS.SLIDEROUT_TIME,
                style({ opacity: 0, transform: "translateX(-100%)" })
            )
        ])
    ])
];

export const Container = [
    trigger("container", [
        transition(":enter, :leave", [
            query("@*", animateChild(), { optional: true })
        ])
    ])
];

