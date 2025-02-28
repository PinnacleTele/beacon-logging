import { Injectable } from '@angular/core';
import { WalletUsers } from 'src/app/account/shared/model/wallet-users';

@Injectable({
  providedIn: 'root'
})
export class WalletuserService {
  list : WalletUsers[] = [];
  filterdItems:WalletUsers[] = [];
  selectedItems : WalletUsers[] = [];
  constructor() { }
  populateList(gList : any){
    this.list = gList;
  }
  getAllItems(){
    return this.list;
  }
//  selectAllGroups(gList : any){
    //this.selectedItems = gList;
  selectAllItems(check : boolean){
    // if(check){
    //   this.selectedItems =[];
    //   this.list.forEach((element: WalletUsers) => {
    //     this.selectedItems.push(element);
    //     element.checked = true;
    //   });
    if(check){
      this.filterdItems.forEach((element: WalletUsers) => {
        if(!element.checked){
          this.selectedItems.push(element);
          element.checked = true;
        }
      });
    }else{
      // this.selectedItems =[];
      // this.list.forEach((element: WalletUsers) => {
        
      //   element.checked = false;
      // });
      this.filterdItems.forEach((element: WalletUsers) => {
        const i= this.selectedItems.findIndex( e=>{return element.cli_id ==e.cli_id })
        console.log(i);
        if(i!=-1){
          
          this.selectedItems.splice(i,1);
        }
         element.checked = false;
       });
    
    }
   
  }
  onClickItem(id : number){
    
   
    const indexselectedItems = this.selectedItems.map((item) => item.cli_id).indexOf(id);
    
    const i = this.list.map((item) => item.cli_id).indexOf(id);
    
    if ( indexselectedItems === -1) {
  //console.log("not in text area, checkbox checked",i)
        this.markGroupChecked(i,true);
        this.selectedItems.push(this.list[i]);

        this.populateselectedItems(this.selectedItems);
        if(this.selectedItems.length === this.list.length){
          return true;
        }else{ return false;}
    }else {// groupItem clicked is in the text area ,already selected
      
      this.markGroupChecked(i,false);
      //console.log(this.selectedItems,"before")
      this.selectedItems.splice(indexselectedItems, 1);
      //console.log(this.selectedItems,"deleted")
      this.populateselectedItems(this.selectedItems);
      return false;
    }
  }
  removeAllSelected(){
    this.selectedItems.splice(0, this.selectedItems.length);
    
  }
  markGroupChecked(index : number, chk : boolean){
    this.list[index].checked = chk;
  }
  getAllselectedItems(){
    return this.selectedItems 
  }
  populateselectedItems(gList : any){
    
    this.selectedItems = gList;
  }
  removeOneItem( y : number ,i : number){
    this.selectedItems.splice(y, 1);
    
    
  }
  loopListAndMarkChecked(selectedItms : WalletUsers[]){
    selectedItms.forEach((el : WalletUsers) => {
      const i = this.list.map((item) => item.cli_id).indexOf(el.cli_id);
      if(i !== -1){
        this.list[i].checked = true;
      }
    });
  }
  


}

