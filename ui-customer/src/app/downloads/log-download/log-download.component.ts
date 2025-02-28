import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { AfterContentChecked, AfterViewInit, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import * as moment from 'moment';
import { Observable } from 'rxjs';
import { CommonService } from 'src/app/shared/commonservice';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';
import { DownloadsService } from '../Helpers/downloads-service.service';
import { requestModel } from './reqest.model';
import { CONSTANTS, value} from "src/app/shared/campaigns.constants";

@Component({
  selector: 'app-log-download',
  templateUrl: './log-download.component.html',
  styleUrls: ['./log-download.component.css']
})
export class LogDownloadComponent implements OnInit, AfterViewInit, AfterContentChecked {

  UserTimeStamp;

  userZone: string = "";

  statsLoading: boolean = false;

  myOptions = value;

  public statsloading = this.downloadService.loadingS_List.subscribe((data: any) => { this.statsLoading = data });



  constructor(private downloadService: DownloadsService,
    private commonservice: CommonService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {

  }
  ngAfterViewInit(): void {
    this.onSearch();
  }

  ngAfterContentChecked() {
    this.cdr.detectChanges();
    //  console.log("contentchecked");

  }

  ngOnInit(): void {

    this.userZone = this.commonservice.getUserData();

    this.downloadService.downloadStats().subscribe((data) => {
      this.stats = data;
      // this.skeleton = false;

    })


  }
  public term: any = "";
  stats: any;
  skeleton: boolean = false;
  apiError: boolean = false;
  dateSelection: any;
  noData: boolean = false;

  downloadReq: requestModel[] = []

  receivedDateSelection(event) {

    this.dateSelection = event;


  }

  total: number = this.downloadReq.length;

  totalRecord: number = this.downloadReq.length;

  p: number = 1;

  pagesize: number;

  itemsPerPage: number = 10;

  perpageCount: number = 10;


  searchBarClear() {
    this.term = ""

  }


  noRecords: any;

  getcount() {
    if (this.term.trim().length == 0) {
      this.noRecords = 1;
      this.total = this.downloadReq.length;
      this.totalRecord = this.downloadReq.length;
      this.p = 1;
      this.itemsPerPage = this.perpageCount;
    } else {


      if (this.downloadService.totalRecords.length >= 0) {
        var count = this.downloadService.totalRecords.length
        if (count == 0) {
          this.noRecords = 0;
          this.total = 0;
          this.p = 1;
          this.perpageCount = 10;
        } else {
          this.noRecords = 1;
          this.total = count;
          this.p = 1;
          this.perpageCount = 10;
          // this.SearchArray = this.search.totalRecords ;


        }
      }
    }
  }

  onSearch() {


    this.skeleton = true;
    this.apiError = false;
    this.noData = false;
    this.searchBarClear();


    this.downloadService.dowloadTableData(this.dateSelection).subscribe(res => {


      // let data = res
      // data.forEach(element => {
      //   let temp = "";
      //   let arr = JSON.parse(element.filters)


      //   temp += "source :" + arr.source + ", ";
      //   temp += "campaignName :" + arr.campaign_name + ", ";
      //   temp += "senderId :" + arr.senderid + ", ";
      //   temp += "from :" + arr.from + ", ";
      //   temp += "to :" + arr.to + ", ";
      //   temp += "status :" + arr.status;
      //   element.filters = temp;
      // });


      this.downloadReq = res;
      if (this.downloadReq.length == 0) {
        this.noData = true;
      }
      this.getcount();
      this.skeleton = false;


    },
      (error: HttpErrorResponse) => {
        this.apiError = true;
        this.skeleton = false;
        this.noData = false;
        this.noRecords = false;
      })

  }

  filename = "sample.pdf";

  httpOptions = {
    headers: new HttpHeaders({
      Accept: "application/json",
      "Content-Type": "application/json"
    })
  };

  downloadIds: any[] = [];

  onDownload(rowData: any, from: any, to: any) {
    rowData.downloadStatus = true;
    this.downloadIds.push({ id: rowData.id });
    this.downloadService.setDownloadIds(this.downloadIds);

    this.UserTimeStamp = moment().format('YYYYMMDD_HHmmss');

    this.downloadService.getTextFile(rowData.id)
      .subscribe((data: any) => {
        const blob = new Blob([data], { type: 'application/zip' });
        const a = document.createElement('a');
        const objectUrl = URL.createObjectURL(blob);
        a.href = objectUrl;
        a.download = `log_${from}_${to}_${this.UserTimeStamp}.zip`;
        a.click();
        rowData.downloadStatus = false;
        this.downloadService.spliceDownloadIds(rowData.id);
        URL.revokeObjectURL(objectUrl);
      });
    // .subscribe((data) => {
    //         console.log(data);
    //     })
  }

  ReceivedpaginateValue(event) {
    this.p = event;
  }

  trimmedTerm = this.term.trim();

  nameOrder = "";
  searchprop: any = "";
  fromIcon = 0;
  toIcon = 0;
  statusIcon = 0;
  totalIcon = 0;
  createdTsIcon = 2;
  defaultProp = "created_ts_unix"
  createdDateCol = "Created Date"

  sort(event) {
    this.searchprop = event.prop;
    this.nameOrder = event.order;
  }

  iconChange(event) {
    if (event.prop == "from") {
      this.fromIcon = event.icon;
      this.statusIcon = 0;
      this.toIcon = 0;
      this.totalIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "to") {
      this.toIcon = event.icon;
      this.fromIcon = 0;
      this.statusIcon = 0;
      this.totalIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "status") {
      this.statusIcon = event.icon;
      this.toIcon = 0;
      this.totalIcon = 0;
      this.fromIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "total") {
      console.log(event);
      this.totalIcon = event.icon;
      this.statusIcon = 0;
      this.toIcon = 0;
      this.fromIcon = 0;
      this.totalIcon = 0;
      this.createdTsIcon = 0;
    }
    else if (event.prop == "created_ts_unix") {
      this.createdTsIcon = event.icon;
      this.statusIcon = 0;
      this.toIcon = 0;
      this.totalIcon = 0;
      this.fromIcon = 0;
    }

  }

}
