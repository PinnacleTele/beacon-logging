export class Group {
    groupname: string;

    noofcontacts: number;

    checked: boolean;
}


export class GroupModel{
    
        id: string;
        g_name: string;
        g_type: string;
        g_visibility: string;
        total: number;
        total_human: string;
        is_owner: boolean;
        created_ts: string;
        modified_ts: string;
        created_ts_unix: number;
       checked?:boolean;
       status?:string;
      }
