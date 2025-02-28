import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { WalletUsers } from 'src/app/account/shared/model/wallet-users';
import { EnterExitRight, Container1 } from "src/app/shared/animation";
import { CONSTANTS } from '../../campaigns.constants';
import { WalletuserService } from './walletuser.service';

@Component({
  selector: 'app-myacct-wallet-user-slider',
  templateUrl: './myacct-wallet-user-slider.component.html',
  styleUrls: ['./myacct-wallet-user-slider.component.css'],
  animations: [EnterExitRight, Container1]
})
export class MyacctWalletUserSliderComponent implements OnInit {
  @Input() list: any;
  @Input() singleSelect: boolean;
  @Input() apiError: boolean;
  @Input() noRecords: boolean;
  @Input() spinner: boolean;
  @Output() closeSlider = new EventEmitter();
  @Output() addedItems = new EventEmitter<any>();
  listData: WalletUsers[];
  selectedItems: WalletUsers[] = [];
  selectAllCheckBox = false;
  searchElement = "";
  borderStyle = "";
  messageColor = "";
  overall = false;
  currencyType = CONSTANTS.currency;
  currencyFormat = CONSTANTS.curencyFormat;


  @Output() tryAgain = new EventEmitter();
  constructor(
    private sliderService: WalletuserService) { }

  ngOnInit(): void {
    this.listData = this.list;

    const selected = this.sliderService.getAllselectedItems();

    if (this.listData !== null && this.listData.length > 0 && selected != null && this.listData.length === selected.length) {
      this.overall = true;
    }
    if (this.noRecords) {
      this.listData = [];
    }
  }
  ngOnChanges() {

    this.listData = this.list;
  }
  onClickTry() {
    this.tryAgain.emit();
  }

  onCloseSlider() {
    this.searchElement = "";

    this.closeSlider.emit();
  }
  changes() {
    // this.overall = false;
  }
  searchBarClear() {
    this.searchElement = "";
    this.overall = false;
  }
  selectAll(event) {
    const val=event.target.checked
    this.sliderService.selectAllItems(val);
    this.passCompleteData();
  }

  passCompleteData() {
    this.selectedItems = this.sliderService.getAllselectedItems();
    //console.log("pass Data ",this.selectedItems);
    this.addedItems.emit(this.selectedItems);

  }


  addItemById(id: number) {
    const i = this.listData.map((item: any) => item.cli_id).indexOf(id);

    if (!this.singleSelect) {//with checkboxes in slider
      this.overall = this.sliderService.onClickItem(id);
      this.passCompleteData();
    } else {
      this.selectedItems = [];
      this.selectedItems.push(this.listData[i]);
      this.addedItems.emit(this.selectedItems);
      this.onCloseSlider();
    }
  }


}

