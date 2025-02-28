export class FileUploadStatistics {
    constructor(
        public uploadedFileNames: string[],
        public storedFileNames: string[],
        public contacts: number,
        public size: number
    ) {}
}
