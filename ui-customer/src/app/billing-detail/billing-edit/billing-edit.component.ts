import { getCurrencySymbol } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { decimalValidator } from 'src/app/campaigns/validators/decimal-validator';
import { value } from 'src/app/shared/campaigns.constants';
import { CONSTANTS_URL } from 'src/app/shared/compaign.url';
import { BILL_CONSTANTS } from '../billing-constants'
import { BillingDetailService } from '../billing-detail.service';
@Component({
  selector: 'app-billing-edit',
  templateUrl: './billing-edit.component.html',
  styleUrls: ['./billing-edit.component.css']
})
export class BillingEditComponent implements OnInit {

  constructor(public fb: FormBuilder, 
    private billingService: BillingDetailService, 
    private http: HttpClient, 
    private route: ActivatedRoute,
    private localStorageService:LocalStorageService
    ) { }

  ngOnInit(): void {
    // this.getEuro();
    this.currencyType = this.userDetails.billing_currency;
    this.route.queryParams.subscribe((data: any) => {
      this.cliId = data.id;
      this.intl_en = data.intl_en;
      this.getIndianRates();
      this.currentUserDetail();
      if (data.intl_en == 1) {
        this.getCountryList();
        this.rowAPI();
      }
    })
    this.enabler();
    this.indiaBillRateForm.valueChanges.subscribe((val) => {
      this.indiaEnable();
    })
  }

  spinner = false;
  apiError = false;
  countryCopy = [];
  skeleton = false;
  skeletonRows = new Array(4);
  otherSaveLoading = false;
  other_Rates = [];
  billingAPIerror = false;
  cliId = "";
  intl_en = 0;
  countryList: any[] = [];
  india_rate_info_txt = BILL_CONSTANTS.INFO_TXT.billing_india_info_txt;
  decimalErrMsg = BILL_CONSTANTS.ERROR_DISPLAY.decimalPatternMsg;
  ZeroErrMsg = BILL_CONSTANTS.ERROR_DISPLAY.gtZero;
  wholeNumTotLength = BILL_CONSTANTS.maxIntegerPlaces;
  userDetails = JSON.parse(this.localStorageService.getLocal("user"))
  indiaSaveLoading = false;
  india_Rates = { smsrate: "", dltrate: "" };
  ROW_Rates: any = null;
  RowSaveLoading = false;
  currencyType: string;
  SMSRateInfoText;
  indian_SMS_Rate = "";
  indian_DLT_Rate = "";
  indian_min_SMS_Rate = 0.0;
  indian_min_DLT_Rate = 0.0;
  Row_currency = "";
  indian_bill_api_error = false;
  showPopUp = false;
  apiResponse: any = "";
  currentApiCall = "";
  minRowVal = 0;
  defaultCurrency = this.userDetails.billing_currency;
  myOptions = value;
  noData = false;
  conv_rate = 0;
  tooltipText = `Conversion Rate: 1${this.userDetails.billing_currency} = ${this.conv_rate}EUR`;
  get RowRate() {
    return this.ROWForm.controls.RowSMSRate
  }


  //indian 
  indiaBillRateForm = this.fb.group({
    SMSRate: [0.1, [Validators.required,
    decimalValidator("GTzero", this.wholeNumTotLength), Validators.min(this.indian_min_SMS_Rate)]],
    currency: [''],
    dltRate: [0.1, [Validators.required,
    decimalValidator("GTzero", this.wholeNumTotLength), Validators.min(this.indian_min_SMS_Rate)]]
  })

  //other countries
  editBillingForm = this.fb.group({
    otherCountriesBillRates: this.fb.array,
  })


  //Rest if the world
  ROWForm = this.fb.group({
    RowSMSRate: [0.1, [Validators.required,
    decimalValidator("GTzero", this.wholeNumTotLength)]],
    RowPricingCurrency: "",
    euroValue: 0
  })
  enabler() {

  }

  addIntlCountryRates(): FormGroup {
    return this.fb.group({
      country: [null, Validators.required],
      intlRate: [null, [Validators.required, decimalValidator("GTzero", this.wholeNumTotLength)]],
      billing_currency: [this.defaultCurrency],
      euroValue: 0,
      isFromApi: false,
      prepareToDelete: false
    });
  }


  getCountryList() {
    this.spinner = true;
    this.apiError = false;
    this.skeleton = true;
    this.billingAPIerror = false;
    this.billingService.getAllCountries().subscribe((data) => {

      // removing india from list
      let indiaIndex
      data.some((ele, i) => { if (ele.country == "India") { indiaIndex = i; return true } })
      data.splice(indiaIndex, 1)

      this.countryList = data;

      this.countryCopy = data;
      this.spinner = false;
      this.getbillingDetails();

    }, (err) => {
      this.spinner = false;
      this.apiError = true;
      this.skeleton = false;
      this.billingAPIerror = true;
    })

  }

  currentUserDetail() {


    this.billingService.getAcctInfo(this.userDetails.cli_id).subscribe((data: any) => {
      this.indian_min_SMS_Rate = data.smsrate;
      this.indian_min_DLT_Rate = data.dltrate;
      this.indiaBillRateForm.get('SMSRate').setValidators([Validators.min(this.indian_min_SMS_Rate), decimalValidator("GTzero", this.wholeNumTotLength), Validators.required])
      this.indiaBillRateForm.get('dltRate').setValidators([Validators.min(this.indian_min_DLT_Rate), decimalValidator("GTzero", this.wholeNumTotLength), Validators.required])

      this.indiaBillRateForm.updateValueAndValidity();
    }, (err) => {
      console.log(err);

    })
  }





  //ainfo api call for indian rates
  indianRateCurrency = "";
  indianRateApiLoading = false;
  userName = ""
  getIndianRates() {
    this.indianRateApiLoading = true;
    this.indian_bill_api_error = false;
    this.billingService.getAcctInfo(this.cliId).subscribe((data: any) => {
      this.india_Rates.smsrate = data.smsrate;
      this.india_Rates.dltrate = data.dltrate;
      this.userName = data.user;
      this.indian_SMS_Rate = data.smsrate;
      this.indian_DLT_Rate = data.dltrate;
      this.indianRateCurrency = data.billing_currency;
      this.indiaBillRateForm.controls.SMSRate.setValue(this.indian_SMS_Rate)
      this.indiaBillRateForm.controls.dltRate.setValue(this.indian_DLT_Rate)
      this.indiaBillRateForm.controls.currency.setValue(this.indianRateCurrency)
      this.indianRateApiLoading = false;
    },

      (err) => {
        this.indian_bill_api_error = true;
        this.indianRateApiLoading = false;
      })
  }

  //international and row detail api call
  getbillingDetails() {
    this.noData = false;
    this.billingService.getAccRates(this.cliId).subscribe((data: any) => {
      let values = this.editBillingForm.get('otherCountriesBillRates') as FormArray;
      this.conv_rate = data.conv_rate;
      this.tooltipText = `Conversion Rate: 1${this.userDetails.billing_currency} = ${this.conv_rate}EUR`;
      let otherPricing = data.rates_arr.filter((ele) => {
        return ele.country != "ROW"
      })

      // let ROWpricing = data.rates_arr.filter((ele) => {
      //   return ele.country == "ROW"
      // })
      if (otherPricing.length == 0) {
        this.noData = true;
      }
      // if (!this.ROWForm.dirty) {
      //   this.getRowRates(ROWpricing);
      // }
      this.editBillingForm.controls.otherCountriesBillRates = this.presetIntlRates(otherPricing);
      this.other_Rates = otherPricing;
      this.resetCountryList();
      this.getCountryRates();
      this.billingAPIerror = false;
      this.skeleton = false;

    }, (err) => {
      console.log(err);

      this.skeleton = false;
      this.billingAPIerror = true;
    })

  }
  rowLoading = false;
  rowApierr = false;
  rowAPI() {
    console.log("roeapi");

    this.rowLoading = true;
    this.rowApierr = false;
    this.billingService.getAccRates(this.cliId).subscribe((data: any) => {
      this.conv_rate = data.conv_rate;
      this.tooltipText = `Conversion Rate: 1${this.userDetails.billing_currency} = ${this.conv_rate}EUR`;

      let ROWpricing = data.rates_arr.filter((ele) => {
        return ele.country == "ROW"
      })
      this.getRowRates(ROWpricing);
      this.rowLoading = false;
    }, (err) => {
      this.rowApierr = true;
      this.rowLoading = false;
    })
  }
  getRowRates(data) {

    if (data.length > 0) {
      this.ROW_Rates = data[0].sms_rate;
      this.Row_currency = data[0].billing_currency
      this.ROWForm.controls.RowSMSRate.setValue(this.ROW_Rates);
      this.ROWForm.controls.RowPricingCurrency.setValue(this.Row_currency);
      this.euroConverter(this.ROW_Rates);
    }
    else {
      this.ROWForm.controls.RowSMSRate.setValue(null);
      this.ROWForm.controls.RowPricingCurrency.setValue(this.defaultCurrency);
      this.ROWForm.updateValueAndValidity();
    }
  }
  otherCountryMinRates = []
  getCountryRates() {
    this.billingService.internationalRates().subscribe((res: any) => {
      let row = res.filter((ele) => { return ele.country == "ROW" })
      this.setROWminValidation(row)
      this.otherCountryMinRates = res.filter((ele) => { return ele.country != "ROW" })
      this.otherCountryValidaiton(this.otherCountryMinRates)
    }, (err) => {
      console.log(err);

    })
  }

  addButtonClicked() {
    this.editBillingForm.markAsDirty();
    (<FormArray>this.editBillingForm.get('otherCountriesBillRates')).push(
      this.addIntlCountryRates()
    );
    this.otherEnable();
  }

  symbolOfCurrency(eve): string {
    return getCurrencySymbol(eve, 'narrow')
  }

  resetCountryList() {
    let vals = this.editBillingForm.controls.otherCountriesBillRates['controls'];
    let selectedCountries = []
    vals.forEach(element => {
      selectedCountries.push(element.value.country)
    });
    if (selectedCountries.length > 0) {
      this.countryList = [];
      this.countryList = this.countryCopy.filter(
        (i) => !selectedCountries.find((f) => f === i.country)
      );
    }
  }

  indiaSave() {
    this.indiaBillRateForm.markAllAsTouched();

    if (this.indiaBillRateForm.valid) {
      //api call
      this.indiaSaveLoading = true;
      this.indiaBillRateForm.disable()
      // setTimeout(() => { this.indiaSaveLoading = false; this.indiaBillRateForm.enable();; }, 4000);
      // console.log(this.indiaBillRateForm);
      this.currentApiCall = "india"
      let payLoad = {

        "cli_id": this.cliId,
        "smsrate": Number(this.indiaBillRateForm.value.SMSRate),
        "dltrate": Number(this.indiaBillRateForm.value.dltRate),
        "smsrate_old": this.india_Rates.smsrate,
        "dltrate_old": this.india_Rates.dltrate

      }
      this.billingService.updateIndianRates(payLoad).subscribe((res: any) => {
        this.apiResponse = res;
        this.showPopUp = true;
        this.indiaSaveLoading = false
        this.indiaBillRateForm.enable();
      }, (err: any) => {

        this.apiResponse = this.billingService.badError;
        this.showPopUp = true;
        this.indiaSaveLoading = false
        this.indiaBillRateForm.enable();
        console.log(err);
      })


      this.indiaBillRateForm.markAsPristine();
    }
  }

  otherCountriesSave() {
    this.editBillingForm.updateValueAndValidity();
    this.validateOtherCountryForm();


    if (!this.editBillingForm.invalid) {
      this.currentApiCall = "others"
      this.otherSaveLoading = true;
      this.editBillingForm.disable();

      let billingDetails = this.editBillingForm.get('otherCountriesBillRates')['controls']
      let added, deleted, updated = [];
      [added, deleted, updated] = this.disbatchFormValues(billingDetails)
      let payLoad = { "cli_id": this.cliId, "add_arr": added, "update_arr": updated, "delete_arr": deleted }
      this.billingService.updateOthers(this.cliId, payLoad).subscribe((res: any) => {
        this.apiResponse = res;
        this.showPopUp = true;
        this.otherSaveLoading = false;
        this.editBillingForm.enable();
        this.editBillingForm.markAsPristine();



      }, (err) => {
        this.apiResponse = this.billingService.badError;
        this.otherSaveLoading = false;
        this.showPopUp = true;
        this.editBillingForm.enable();
        console.log(err);

      })
    }
  }
  ROWSave() {
    this.ROWForm.markAllAsTouched();
    if (this.ROWForm.valid) {
      //api call
      this.RowSaveLoading = true;
      this.ROWForm.disable();

      let data = { oldRate: this.ROW_Rates, newRate: Number(this.ROWForm.controls.RowSMSRate.value) }
      this.billingService.updateROW(this.cliId, data).subscribe((res: any) => {
        this.currentApiCall = "row"
        this.RowSaveLoading = false;
        this.ROWForm.markAsPristine();
        this.ROWForm.enable();
        this.apiResponse = res;
        this.showPopUp = true;
      }, (err) => {
        this.apiResponse = this.billingService.badError;
        this.showPopUp = true;
        this.RowSaveLoading = false;
        this.ROWForm.enable();
      })
    }
  }

  ngSelectChange(eve, index) {
    this.resetCountryList();
    if (eve) {


      if (this.editBillingForm.get('otherCountriesBillRates')['controls'].some((ele, i) => {
        if (ele.value.country == eve.country) {
          return true
        }
      })) {
        this.otherCountryValidaiton(this.otherCountryMinRates);
      }
      else {
        this.editBillingForm.get('otherCountriesBillRates')['controls'][index].get('intlRate').setValue(null)
        this.editBillingForm.updateValueAndValidity();
      }
    }
  }

  readyToDelete(i) {
    return this.editBillingForm.get('otherCountriesBillRates').value[i].prepareToDelete
  }
  removeFromDelete(i) {
    // this.editBillingForm.get('otherCountriesBillRates').value[i].prepareToDelete.set;
    this.editBillingForm.get('otherCountriesBillRates')['controls'][i].get('prepareToDelete').setValue(false);
    this.editBillingForm.updateValueAndValidity();
    this.otherEnable();
  }
  onRemove(i, e) {

    this.editBillingForm.get('otherCountriesBillRates').value[i].prepareToDelete = true;
    this.editBillingForm.get('otherCountriesBillRates')['controls'][i].get('prepareToDelete').setValue(true);
    this.editBillingForm.markAsDirty();
    this.otherEnable()
    this.editBillingForm.updateValueAndValidity();

  }

  delete(i) {
    (<FormArray>(
      this.editBillingForm.get('otherCountriesBillRates')
    )).removeAt(i);
    this.otherEnable();
    this.resetCountryList();
  }
  getStatus(i) {
    return this.editBillingForm.get('otherCountriesBillRates').value[i].prepareToDelete
  }

  presetIntlRates(element: any): FormArray {
    const formArray = new FormArray([]);
    element.forEach((ele, i) => {
      formArray.push(this.fb.group({
        country: [ele.country, Validators.required],
        intlRate: [ele.sms_rate, [Validators.required, decimalValidator("GTzero", this.wholeNumTotLength)]],
        billing_currency: [ele.billing_currency],
        euroValue: this.conv_rate * ele.sms_rate,
        isFromApi: true,
        prepareToDelete: false

      })

      )
    });

    return formArray

  }


  india_Discard() {
    this.indiaBillRateForm.controls.SMSRate.setValue(this.india_Rates.smsrate)
    this.indiaBillRateForm.controls.dltRate.setValue(this.india_Rates.dltrate)
    this.indiaBillRateForm.markAsPristine();
  }

  otherCountriesDiscard() {
    this.enabler();
    this.editBillingForm.controls.otherCountriesBillRates = this.presetIntlRates(this.other_Rates);
    this.resetCountryList();
    this.other_Rates.forEach((ele) => { ele.changed = false })
    this.otherEnable();
    this.editBillingForm.markAsPristine();
  }

  RowDiacard() {
    this.ROWForm.controls.RowSMSRate.setValue(this.ROW_Rates)
    this.ROWForm.controls.euroValue.setValue(this.RowOldEuro)
    this.ROWForm.markAsPristine();
    this.ROWForm.updateValueAndValidity();
  }

  validateOtherCountryForm() {
    let billingDetails = this.editBillingForm.get('otherCountriesBillRates')['controls']
    let deletable_rates = []
    billingDetails.forEach(element => {
      element['controls'].country.markAsTouched();
      element['controls'].intlRate.markAsTouched();
      deletable_rates.push(element['controls'])
    });
    this.editBillingForm.updateValueAndValidity();
  }

  disbatchFormValues(values: any) {
    let addedCountries = [];
    let deletedCountries = [];
    let updated = [];
    values.forEach((ele) => {
      if (ele.value.prepareToDelete == true) {
        deletedCountries.push(ele['controls'].country.value)
      }
      if (ele['controls'].isFromApi.value == false) {
        addedCountries.push({ country: ele['controls'].country.value, sms_rate: ele['controls'].intlRate.value })
      }
      else {
        if (ele['controls'].intlRate.dirty) {
          let oldvalue = this.other_Rates.filter((element) => {
            return element.country == ele['controls'].country.value;;
          })
          updated.push({ country: ele['controls'].country.value, sms_rate: Number(ele['controls'].intlRate.value), sms_rate_old: oldvalue[0].sms_rate })
        }
      }
    })

    return [addedCountries, deletedCountries, updated]

  }

  tryAgain(event) {
    this.showPopUp = event.bool
    if (event.type == "row") {
      this.ROWSave();
    }
    if (event.type == "others") {
      this.otherCountriesSave();
    }
    if (event.type == "india") {
      this.indiaSave();
    }
  }

  modalContinue(event) {
    this.showPopUp = event.bool
    if (event.type == "row") {
      this.rowAPI()
    }
    if (event.type == "others") {
      this.getCountryList()
    }
    if (event.type == "india") {
      this.getIndianRates();
    }
  }

  change(i, val, country) {
    this.editBillingForm.updateValueAndValidity();
    if (val && val != 0) {
      this.editBillingForm.get('otherCountriesBillRates')['controls'][i].get('euroValue').setValue(val * this.conv_rate);
      this.editBillingForm.updateValueAndValidity();
      const bool = this.editBillingForm.get('otherCountriesBillRates')['controls'][i].get('intlRate').dirty;

      if (bool) {
        this.editBillingForm.markAsDirty();
      }
      this.editBillingForm.updateValueAndValidity();
      this.other_Rates.forEach((ele, index) => {
        if (ele.country == country) {
          let temp = Number(val)
          if (this.other_Rates[index].sms_rate != temp) {
            ele.changed = true;
          }
          else {
            ele.changed = false
          }
        }
      })
      this.otherEnable();
    }
    else {
      this.editBillingForm.get('otherCountriesBillRates')['controls'][i].get('euroValue').setValue(0);
      this.editBillingForm.markAsDirty();
      this.editBillingForm.updateValueAndValidity();
    }

  }
  click(eve) {
    console.log(eve);

  }
  RowOldEuro = 0;
  euroConverter(val) {
    this.rowEnable();

    if (val && val != 0) {
      this.RowOldEuro = this.ROWForm.controls.euroValue.value ? this.ROWForm.controls.euroValue.value : 0
      this.ROWForm.controls.euroValue.setValue(val * this.conv_rate)
      this.ROWForm.updateValueAndValidity();
    }
    else {

      this.ROWForm.controls.euroValue.setValue(0)
      this.editBillingForm.updateValueAndValidity();
    }
  }
  setROWminValidation(val) {
    if (val.length > 0) {
      this.ROWForm.controls['RowSMSRate'].setValidators([Validators.required, decimalValidator("GTzero", this.wholeNumTotLength), Validators.min(val[0].smsrate)])
      this.ROWForm.updateValueAndValidity();
    }
    else {
      this.ROWForm.controls['RowSMSRate'].setValidators([Validators.required, decimalValidator("GTzero", this.wholeNumTotLength), Validators.min(0)])
      this.ROWForm.updateValueAndValidity();
    }


  }

  otherCountryValidaiton(val: any[]) {
    let otherRates = val;
    otherRates.forEach((el) => {
      // let index = null
      this.editBillingForm.get('otherCountriesBillRates')['controls'].some((ele, i) => {
        if (ele.value.country == el.country) {
          if (this.editBillingForm.get('otherCountriesBillRates')['controls'][i].get('isFromApi').value == false) {
            this.editBillingForm.get('otherCountriesBillRates')['controls'][i].get('intlRate').setValue(el.smsrate)
            this.editBillingForm.get('otherCountriesBillRates')['controls'][i].get('euroValue').setValue(el.smsrate * this.conv_rate)
            //  this.getEuro(el.smsrate, i);
            this.editBillingForm.updateValueAndValidity();
          }
          this.editBillingForm.get('otherCountriesBillRates')['controls'][i].get('intlRate').setValidators([Validators.min(el.smsrate), decimalValidator("GTzero", this.wholeNumTotLength), Validators.required])
          this.editBillingForm.updateValueAndValidity();
          return true
        }
        else {


        }
      })

    })
    this.otherEnable();
  }
  getEuro() {
    this.http.get(CONSTANTS_URL.GLOBAL_URL + CONSTANTS_URL.CURRENCY_CONVERT + 1).subscribe((data: any) => {
      this.conv_rate = data.smsrate;
    })
  }
  rowEnable() {
    let newValue = this.ROWForm.controls['RowSMSRate'].value
    if (this.ROW_Rates != newValue) {
      return true
    }
    else {
      this.ROWForm.markAsPristine();
      return false
    }

  }
  indiaEnable() {
    let smsval = this.indiaBillRateForm.controls.SMSRate.value;
    let dltval = this.indiaBillRateForm.controls.dltRate.value;
    if (smsval != this.indian_SMS_Rate || dltval != this.indian_DLT_Rate) {
    }
    else {
      this.indiaBillRateForm.markAsPristine();

    }
  }
  isOthersEnable = false
  otherEnable() {
    let valueChanged = this.other_Rates.some((ele) => {

      return ele.changed == true
    });
    let added = this.other_Rates.length < this.editBillingForm.get('otherCountriesBillRates').value.length

    let deleted = this.editBillingForm.get('otherCountriesBillRates').value.some((ele) => {
      return ele.prepareToDelete == true
    })
    this.isOthersEnable = valueChanged || added || deleted;

  }
}

