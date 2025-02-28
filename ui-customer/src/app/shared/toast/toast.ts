export class Toast {
    title: string;

    body: string;

    status: string;

    id: number;

    autoClose: boolean;

    constructor(
        title: string,
        body: string,
        status: string,
        autoClose = true,
        id = 0
    ) {
        this.title = title;
        this.body = body;
        this.status = status;
        this.autoClose = autoClose;
        this.id = id;
    }
}
