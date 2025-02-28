export const BILL_CONSTANTS = {
  maxDecimalPlaces: 6,
  maxIntegerPlaces: 4,

  decimalNum_validation: "^-?\\d*[.,]?\\d{0,4}$",


  INFO_TXT: {
    billing_india_info_txt: `Supports upto max 4 digits before decimal and max 6 digits after decimal`,
    SMSRate: "Amount that will be deducted per sms. Supports upto max 4 digits before decimal and max 6 digits after decimal",
    DLTRate: "DLT charges to be deducted. Supports upto max 4 digits before decimal and max 6 digits after decimal",

  },

  ERROR_DISPLAY: {
    decimalPatternMsg: `accepts upto max 4 digits before decimal and max 6 digits after decimal`,
    indiaDecimalPatternMsg: "accepts upto max 4 digits before decimal and max 6 digits after decimal",
    minimumBillLength: "Rate cannot be lesser than your parent user's rate",
    maxBillLength: "Accepts only numbers upto 4 integer places",
    gtZero: "Enter a number greater than zero",
  }
}