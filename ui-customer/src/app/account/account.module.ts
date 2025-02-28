import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccountRoutingModule } from './account-routing.module';
import { AccountInfoComponent } from './create-new-account/account-info/account-info.component';
import { AuthSectionComponent } from './create-new-account/auth-section/auth-section.component';
import { SettingsComponent } from './create-new-account/settings/settings.component';
import { OtherServicesComponent } from './create-new-account/other-services/other-services.component';
import { AccountTypeComponent } from './create-new-account/account-type/account-type.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CreateNewAccountComponent } from './create-new-account/create-new-account.component';
import { AccountHomeComponent } from './account-home/account-home.component';
import { DLTSectionComponent } from './create-new-account/dlt-section/dlt-section.component';
import { ErrorDisplayComponent } from './shared/components/error-display/error-display.component';
import { SharedModule } from '../shared/shared.module';
import { CountryComponent } from './shared/components/country/country.component';
import { NgSelectModule } from '@ng-select/ng-select';
import { DateRangePickerModule } from "@syncfusion/ej2-angular-calendars";
import { TimezoneComponent } from './shared/components/timezone/timezone.component';
import { AccountListComponent } from './account-list/account-list.component';
import { NgxPaginationModule } from "ngx-pagination";
import { userListFilter } from "./shared/accountHelper/account.filter";
import { walletTxUserListFilter } from "./shared/accountHelper/wallet-tx-user.filter";
import { EditAccountComponent } from './edit-account/edit-account.component';
import { EditAccountInfoComponent } from './edit-account/edit-account-right-panel/edit-account-info/edit-account-info.component';
import { LastNameComponent } from './shared/components/last-name/last-name.component';
import { FirstNameComponent } from './shared/components/first-name/first-name.component';
import { AddressComponent } from './shared/components/address/address.component';
import { CompanyComponent } from './shared/components/company/company.component';
import { SMSRateComponent } from './shared/components/sms-rate/sms-rate.component';
import { DLTRateComponent } from './shared/components/dlt-rate/dlt-rate.component';
import { NewlineCharComponent } from './shared/components/newline-char/newline-char.component';
import { ToggleButtonComponent } from './shared/components/toggle-button/toggle-button.component';
import { EditAccountLeftPanelComponent } from './edit-account/edit-account-left-panel/edit-account-left-panel.component';
import { EditAccountRightPanelComponent } from './edit-account/edit-account-right-panel/edit-account-right-panel.component';
import { EditDltCardComponent } from './edit-account/edit-account-right-panel/edit-dlt-card/edit-dlt-card.component';
import { EditSigninSecuritiesComponent } from './edit-account/edit-account-right-panel/edit-signin-securities/edit-signin-securities.component';
import { EditMessagingSettingsComponent } from './edit-account/edit-account-right-panel/edit-messaging-settings/edit-messaging-settings.component';
import { EditDataSecuritiesSettingsComponent } from './edit-account/edit-account-right-panel/edit-data-securities-settings/edit-data-securities-settings.component';
import { EditAssignServicesComponent } from './edit-account/edit-account-right-panel/edit-assign-services/edit-assign-services.component';
import { EditWalletComponent } from './edit-account/edit-account-right-panel/edit-wallet/edit-wallet.component';
import { EditSharedGroupsComponent } from './edit-account/edit-account-right-panel/edit-shared-groups/edit-shared-groups.component';
import { SmppCharsetComponent } from './shared/components/smpp-charset/smpp-charset.component';
import { ApiErrorComponent } from './shared/components/api-error/api-error.component';
import { SaveButtonComponent } from './edit-account/buttons/save-button/save-button.component';
import { DiscardButtonComponent } from './edit-account/buttons/discard-button/discard-button.component';

import { MyAccountComponent } from './my-account/my-account.component';
import { MyAcctSettingsComponent } from './my-account/my-acct-settings/my-acct-settings.component';
import { MyAcctWalletComponent } from './my-account/my-acct-wallet/my-acct-wallet.component';

import { ActiDeactivateModalComponent } from './edit-account/edit-account-left-panel/acti-deactivate-modal/acti-deactivate-modal.component';
import { RightPanelSkeletonComponent } from './shared/components/right-panel-skeleton/right-panel-skeleton.component';
import { MyaccountSkeletonComponent } from './shared/components/myaccount-skeleton/myaccount-skeleton.component';
import { MyaccSettingsPasswordComponent } from './my-account/my-acct-settings/myacc-settings-password/myacc-settings-password.component';
import { MyAcctQcsettingsComponent } from './my-account/my-acct-qcsettings/my-acct-qcsettings.component';
import { MyServicesComponent } from './my-account/my-acct-settings/my-services/my-services.component';
import { SpinnerOnButtonComponent } from './shared/components/spinner-on-button/spinner-on-button.component';
import { EditBillRatesComponent } from './edit-account/edit-account-right-panel/edit-bill-rates/edit-bill-rates.component';
import { MyAcctWalletTxComponent } from './my-account/my-acct-wallet-tx/my-acct-wallet-tx.component';
import { TxCalendarComponent } from './my-account/my-acct-wallet-tx/tx-calendar/tx-calendar.component';
import { walletTxListFilter } from './shared/accountHelper/wallet-tx.filter';
import { ServerErrComponent } from './my-account/server-err/server-err.component';
import { ZeroWalletTxComponent } from './my-account/zero-wallet-tx/zero-wallet-tx.component';
import { MyDltCardComponent } from './my-account/my-dlt-card/my-dlt-card.component';
import { TooltipModule } from 'ng2-tooltip-directive';
import { CurrencyComponent } from './shared/components/currency/currency.component';
import { RetryComponent } from './shared/components/retry/retry.component';


@NgModule({
  declarations: [
    CreateNewAccountComponent,
    AccountInfoComponent,
    AuthSectionComponent,
    SettingsComponent,
    OtherServicesComponent,
    AccountTypeComponent,
    AccountHomeComponent,
    DLTSectionComponent,
    ErrorDisplayComponent,
    CountryComponent,
    TimezoneComponent,
    AccountListComponent,
    userListFilter,
    walletTxListFilter,
    walletTxUserListFilter,
    EditAccountComponent,
    EditAccountInfoComponent,
    EditDltCardComponent,
    EditSigninSecuritiesComponent,
    EditMessagingSettingsComponent,
    EditDataSecuritiesSettingsComponent,
    EditAssignServicesComponent,
    EditWalletComponent,
    EditSharedGroupsComponent,
    LastNameComponent,
    FirstNameComponent,
    AddressComponent,
    CompanyComponent,
    SMSRateComponent,
    DLTRateComponent,
    NewlineCharComponent,
    ToggleButtonComponent,
    EditAccountLeftPanelComponent,
    EditAccountRightPanelComponent,
    SmppCharsetComponent,
    ApiErrorComponent,
    SaveButtonComponent,
    DiscardButtonComponent,
    MyAccountComponent,
    MyAcctSettingsComponent,
    MyAcctWalletComponent,
    SpinnerOnButtonComponent,
    ActiDeactivateModalComponent,
    RightPanelSkeletonComponent,
    MyaccountSkeletonComponent,
    MyaccSettingsPasswordComponent,
    MyAcctQcsettingsComponent,
    MyServicesComponent,
    EditBillRatesComponent,
    MyAcctWalletTxComponent,
    TxCalendarComponent,
    ServerErrComponent,
    ZeroWalletTxComponent,
    MyDltCardComponent,
    CurrencyComponent,
    RetryComponent,
    
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NgSelectModule,
    DateRangePickerModule,
    AccountRoutingModule,
    SharedModule,
    NgxPaginationModule,
    TooltipModule
  ],
  exports: [
    MyAccountComponent,


  ]
})
export class AccountModule { }
