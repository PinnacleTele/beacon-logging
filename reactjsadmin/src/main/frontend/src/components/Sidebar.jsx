import React, { useState } from 'react';
import wallet from '../assets/wallet.svg'
import download from '../assets/download.svg'
import browse from '../assets/browse.svg'
import Vector from '../assets/Vector.svg'

function Sidebar() {

    const [toggle, setToggle] = useState(false);



    const [isClickedCampaigns, setClickedCampaigns] = useState(false);


    const [isClickedDashboard, setClickedDashboard] = useState(false);
    const [isClickedGroups, setClickedGroups] = useState(false);
    const [isClickedTemplates, setClickedTemplates] = useState(false);
    const [isClickedDLT, setClickedDLT] = useState(false);
    const [isClickedSettings, setClickedSettings] = useState(false);
    const [isClickedSignOut, setClickedSignOut] = useState(false);




    const [isClickedAccounts, setClickedAccounts] = useState(false);

    const setActiveState = (setter) => {
        setClickedCampaigns(false);
        setClickedDashboard(false);
        setClickedGroups(false);
        setClickedTemplates(false);
        setClickedDLT(false);
        setClickedSettings(false);
        setClickedSignOut(false);
        setClickedAccounts(false);

        setter(true);
    };


    const handleAccountsClick = () => {
        setActiveState(setClickedAccounts);
    };


    const handleSignOutClick = () => {
        setActiveState(setClickedSignOut);
    };

    const handleSettingsClick = () => {
        setActiveState(setClickedSettings);
    };

    const handleDLTClick = () => {
        setActiveState(setClickedDLT);
    };

    const handleTemplatesClick = () => {
        setActiveState(setClickedTemplates);
    };

    const Campaigns = () => {
        setActiveState(setClickedCampaigns);
    };

    const handleDashboardClick = () => {
        setActiveState(setClickedDashboard);
    };

    const handleGroupsClick = () => {
        setActiveState(setClickedGroups);
    };


    return (
        <>

            <div className='hidden md:flex w-[18%] h-[1211px]  justify-center  px-[46px] pt-[30px] container ' style={{
                background: 'linear-gradient(to right, #B41749 5%, #B41749 5%, #387BBF 90%, #387BBF 90%)',

            }}>

                <div className='  w-full  h-full flex flex-col gap-[30px]'>
                    <div
                        className={`flex items-center justify-start py-4 px-4 gap-[24px] rounded-[16px] ${isClickedDashboard ? 'bg-white' : ''}`}
                        onClick={handleDashboardClick}
                    >
                        <div>
                            <svg width="32" height="32" viewBox="0 0 32 33" fill={isClickedDashboard ? '#A10039' : 'white'} xmlns="http://www.w3.org/2000/svg">
                                <mask id="mask0_1_11091" style={{ maskType: 'luminance' }} maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                    <rect y="0.401367" width="32" height="32" fill="white" />
                                </mask>
                                <g mask="url(#mask0_1_11091)">
                                    <path d="M13.6704 8.24756L14.0416 13.7675L14.2259 16.5419C14.2279 16.8272 14.2725 17.1107 14.3588 17.3831C14.5813 17.9119 15.1168 18.2479 15.6993 18.2244L24.5756 17.6438C24.96 17.6375 25.3312 17.7812 25.6075 18.0435C25.8377 18.262 25.9864 18.5479 26.0333 18.8554L26.0491 19.042C25.6817 24.1283 21.9461 28.3707 16.8704 29.4658C11.7946 30.5608 6.58967 28.2475 4.08147 23.7816C3.35837 22.4842 2.90672 21.0582 2.75303 19.5871C2.68883 19.1516 2.66056 18.7118 2.66849 18.2718C2.66056 12.8187 6.54382 8.10438 11.9796 6.96786C12.6339 6.86598 13.2752 7.21233 13.5376 7.80914C13.6055 7.94736 13.6503 8.09538 13.6704 8.24756Z" fill={isClickedDashboard ? '#A10039' : 'white'} />
                                    <path opacity="0.4" d="M29.3335 13.4847L29.3242 13.5282L29.2973 13.5914L29.301 13.7649C29.2871 13.9946 29.1983 14.2157 29.0455 14.3944C28.8862 14.5804 28.6686 14.707 28.429 14.7562L28.2829 14.7762L18.0418 15.4398C17.7011 15.4734 17.3619 15.3636 17.1087 15.1377C16.8975 14.9493 16.7626 14.6952 16.7245 14.4213L16.0371 4.19511C16.0251 4.16053 16.0251 4.12305 16.0371 4.08847C16.0465 3.80659 16.1706 3.54015 16.3817 3.34867C16.5926 3.15719 16.8731 3.05663 17.1602 3.06945C23.2401 3.22413 28.3499 7.59608 29.3335 13.4847Z" fill={isClickedDashboard ? '#A10039' : 'white'} />
                                </g>
                            </svg>
                        </div>
                        <div className={`text-${isClickedDashboard ? '#A10039' : 'white'}`}>
                            <p className="font-poppins text-[18px] font-semibold">Dashboard</p>
                        </div>
                    </div>
                    <div
                        className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedCampaigns ? 'bg-white' : ''}`}
                        onClick={Campaigns}
                    >
                        <div>
                            <svg width="32" height="32" viewBox="0 0 32 33" fill={isClickedCampaigns ? '#A10039' : 'white'} xmlns="http://www.w3.org/2000/svg">
                                <path d="M26.6667 3.06836H5.33334C3.86667 3.06836 2.66667 4.26836 2.66667 5.73503V29.735L8.00001 24.4017H26.6667C28.1333 24.4017 29.3333 23.2017 29.3333 21.735V5.73503C29.3333 4.26836 28.1333 3.06836 26.6667 3.06836ZM26.6667 21.735H6.93334L5.33334 23.335V5.73503H26.6667V21.735ZM22.6667 15.0684H20V12.4017H22.6667V15.0684ZM17.3333 15.0684H14.6667V12.4017H17.3333V15.0684ZM12 15.0684H9.33334V12.4017H12" fill={isClickedCampaigns ? '#A10039' : 'white'} />
                            </svg>
                        </div>
                        <div className={`text-${isClickedCampaigns ? '[#A10039]' : 'white'}`}>
                            <p className="font-poppins text-[18px]  font-semibold">Campaigns</p>
                        </div>
                    </div>
                    <div
                        className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedGroups ? 'bg-white' : ''}`}
                        onClick={handleGroupsClick}
                    >
                        <div>
                            <svg width="32" height="32" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <mask id="mask0_1_11098" maskType="alpha" maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                    <rect y="0.401367" width="32" height="32" fill="#D9D9D9" />
                                </mask>
                                <g mask="url(#mask0_1_11098)">
                                    <path d="M1.33333 29.5672V25.7839C1.33333 25.0182 1.52777 24.3145 1.91666 23.6727C2.30555 23.0309 2.82222 22.541 3.46666 22.2032C4.84444 21.5051 6.24444 20.9816 7.66666 20.6325C9.08888 20.2834 10.5333 20.1089 12 20.1089C13.4667 20.1089 14.9111 20.2834 16.3333 20.6325C17.7556 20.9816 19.1556 21.5051 20.5333 22.2032C21.1778 22.541 21.6944 23.0309 22.0833 23.6727C22.4722 24.3145 22.6667 25.0182 22.6667 25.7839V29.5672H1.33333ZM25.3333 29.5672V25.5137C25.3333 24.5228 25.0611 23.5713 24.5167 22.6593C23.9722 21.7472 23.2 20.9647 22.2 20.3116C23.3333 20.4467 24.4 20.6775 25.4 21.0041C26.4 21.3306 27.3333 21.7303 28.2 22.2032C29 22.6536 29.6111 23.1547 30.0333 23.7064C30.4556 24.2582 30.6667 24.8606 30.6667 25.5137V29.5672H25.3333ZM12 18.7577C10.5333 18.7577 9.27777 18.2285 8.23333 17.1701C7.18888 16.1117 6.66666 14.8393 6.66666 13.353C6.66666 11.8667 7.18888 10.5943 8.23333 9.53589C9.27777 8.47746 10.5333 7.94824 12 7.94824C13.4667 7.94824 14.7222 8.47746 15.7667 9.53589C16.8111 10.5943 17.3333 11.8667 17.3333 13.353C17.3333 14.8393 16.8111 16.1117 15.7667 17.1701C14.7222 18.2285 13.4667 18.7577 12 18.7577ZM25.3333 13.353C25.3333 14.8393 24.8111 16.1117 23.7667 17.1701C22.7222 18.2285 21.4667 18.7577 20 18.7577C19.7556 18.7577 19.4444 18.7296 19.0667 18.6733C18.6889 18.617 18.3778 18.555 18.1333 18.4875C18.7333 17.7669 19.1944 16.9674 19.5167 16.0891C19.8389 15.2109 20 14.2988 20 13.353C20 12.4072 19.8389 11.4951 19.5167 10.6168C19.1944 9.73856 18.7333 8.93911 18.1333 8.21848C18.4444 8.10588 18.7556 8.03269 19.0667 7.99891C19.3778 7.96513 19.6889 7.94824 20 7.94824C21.4667 7.94824 22.7222 8.47746 23.7667 9.53589C24.8111 10.5943 25.3333 11.8667 25.3333 13.353ZM3.99999 26.8648H20V25.7839C20 25.5362 19.9389 25.311 19.8167 25.1083C19.6944 24.9056 19.5333 24.748 19.3333 24.6354C18.1333 24.0274 16.9222 23.5713 15.7 23.2673C14.4778 22.9633 13.2444 22.8113 12 22.8113C10.7556 22.8113 9.52222 22.9633 8.29999 23.2673C7.07777 23.5713 5.86666 24.0274 4.66666 24.6354C4.46666 24.748 4.30555 24.9056 4.18333 25.1083C4.06111 25.311 3.99999 25.5362 3.99999 25.7839V26.8648ZM12 16.0554C12.7333 16.0554 13.3611 15.7907 13.8833 15.2615C14.4056 14.7323 14.6667 14.0961 14.6667 13.353C14.6667 12.6098 14.4056 11.9736 13.8833 11.4444C13.3611 10.9152 12.7333 10.6506 12 10.6506C11.2667 10.6506 10.6389 10.9152 10.1167 11.4444C9.59444 11.9736 9.33333 12.6098 9.33333 13.353C9.33333 14.0961 9.59444 14.7323 10.1167 15.2615C10.6389 15.7907 11.2667 16.0554 12 16.0554Z" fill={isClickedGroups ? '#A10039' : 'white'} />
                                </g>
                            </svg>
                        </div>
                        <div className={`text-${isClickedGroups ? '[#A10039]' : 'white'}`}>
                            <p className="font-poppins text-[18px] font-semibold">Groups</p>
                        </div>
                    </div>
                    <div
                        className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedTemplates ? 'bg-white' : ''}`}
                        onClick={handleTemplatesClick}
                    >
                        <div>
                            <svg width="24" height="26" viewBox="0 0 24 26" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M0 13.8302H2.66667V25.9909H0V13.8302ZM21.3333 8.42547H24V25.9909H21.3333V8.42547ZM10.6667 0.318359H13.3333V25.9909H10.6667V0.318359Z" fill={isClickedTemplates ? '#A10039' : 'white'} />
                            </svg>



                        </div>
                        <div className={`text-${isClickedTemplates ? '[#A10039]' : 'white'}`}>
                            <p className="font-poppins text-[18px] font-semibold">Templates</p>
                        </div>
                    </div>


                    <div
                        className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedAccounts ? 'bg-white' : ''}`}
                        onClick={handleAccountsClick}
                    >
                        <div>
                            <svg width="32" height="32" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <mask id="mask0_1_11108" style={{ maskType: 'alpha' }} maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                    <rect y="0.401367" width="32" height="32" fill="#D9D9D9" />
                                </mask>
                                <g mask="url(#mask0_1_11108)">
                                    <path d="M16 16.401C14.5333 16.401 13.2778 15.8788 12.2333 14.8344C11.1889 13.7899 10.6667 12.5344 10.6667 11.0677C10.6667 9.60104 11.1889 8.34549 12.2333 7.30104C13.2778 6.2566 14.5333 5.73438 16 5.73438C17.4667 5.73438 18.7222 6.2566 19.7667 7.30104C20.8111 8.34549 21.3333 9.60104 21.3333 11.0677C21.3333 12.5344 20.8111 13.7899 19.7667 14.8344C18.7222 15.8788 17.4667 16.401 16 16.401ZM5.33333 27.0677V23.3344C5.33333 22.5788 5.52777 21.8844 5.91666 21.251C6.30555 20.6177 6.82222 20.1344 7.46666 19.801C8.84444 19.1122 10.2444 18.5955 11.6667 18.251C13.0889 17.9066 14.5333 17.7344 16 17.7344C17.4667 17.7344 18.9111 17.9066 20.3333 18.251C21.7556 18.5955 23.1556 19.1122 24.5333 19.801C25.1778 20.1344 25.6944 20.6177 26.0833 21.251C26.4722 21.8844 26.6667 22.5788 26.6667 23.3344V27.0677H5.33333ZM7.99999 24.401H24V23.3344C24 23.0899 23.9389 22.8677 23.8167 22.6677C23.6944 22.4677 23.5333 22.3122 23.3333 22.201C22.1333 21.601 20.9222 21.151 19.7 20.851C18.4778 20.551 17.2444 20.401 16 20.401C14.7556 20.401 13.5222 20.551 12.3 20.851C11.0778 21.151 9.86666 21.601 8.66666 22.201C8.46666 22.3122 8.30555 22.4677 8.18333 22.6677C8.06111 22.8677 7.99999 23.0899 7.99999 23.3344V24.401ZM16 13.7344C16.7333 13.7344 17.3611 13.4733 17.8833 12.951C18.4056 12.4288 18.6667 11.801 18.6667 11.0677C18.6667 10.3344 18.4056 9.7066 17.8833 9.18438C17.3611 8.66215 16.7333 8.40104 16 8.40104C15.2667 8.40104 14.6389 8.66215 14.1167 9.18438C13.5944 9.7066 13.3333 10.3344 13.3333 11.0677C13.3333 11.801 13.5944 12.4288 14.1167 12.951C14.6389 13.4733 15.2667 13.7344 16 13.7344Z" fill={isClickedAccounts ? '#A10039' : 'white'} />
                                </g>
                            </svg>



                            {/* <svg width="32" height="33" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <mask id="mask0_1_11098" maskType="alpha" maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                    <rect y="0.401367" width="32" height="32" fill="#D9D9D9" />
                                </mask>
                                <g mask="url(#mask0_1_11098)">
                                    <path d="M1.33333 29.5672V25.7839C1.33333 25.0182 1.52777 24.3145 1.91666 23.6727C2.30555 23.0309 2.82222 22.541 3.46666 22.2032C4.84444 21.5051 6.24444 20.9816 7.66666 20.6325C9.08888 20.2834 10.5333 20.1089 12 20.1089C13.4667 20.1089 14.9111 20.2834 16.3333 20.6325C17.7556 20.9816 19.1556 21.5051 20.5333 22.2032C21.1778 22.541 21.6944 23.0309 22.0833 23.6727C22.4722 24.3145 22.6667 25.0182 22.6667 25.7839V29.5672H1.33333ZM25.3333 29.5672V25.5137C25.3333 24.5228 25.0611 23.5713 24.5167 22.6593C23.9722 21.7472 23.2 20.9647 22.2 20.3116C23.3333 20.4467 24.4 20.6775 25.4 21.0041C26.4 21.3306 27.3333 21.7303 28.2 22.2032C29 22.6536 29.6111 23.1547 30.0333 23.7064C30.4556 24.2582 30.6667 24.8606 30.6667 25.5137V29.5672H25.3333ZM12 18.7577C10.5333 18.7577 9.27777 18.2285 8.23333 17.1701C7.18888 16.1117 6.66666 14.8393 6.66666 13.353C6.66666 11.8667 7.18888 10.5943 8.23333 9.53589C9.27777 8.47746 10.5333 7.94824 12 7.94824C13.4667 7.94824 14.7222 8.47746 15.7667 9.53589C16.8111 10.5943 17.3333 11.8667 17.3333 13.353C17.3333 14.8393 16.8111 16.1117 15.7667 17.1701C14.7222 18.2285 13.4667 18.7577 12 18.7577ZM25.3333 13.353C25.3333 14.8393 24.8111 16.1117 23.7667 17.1701C22.7222 18.2285 21.4667 18.7577 20 18.7577C19.7556 18.7577 19.4444 18.7296 19.0667 18.6733C18.6889 18.617 18.3778 18.555 18.1333 18.4875C18.7333 17.7669 19.1944 16.9674 19.5167 16.0891C19.8389 15.2109 20 14.2988 20 13.353C20 12.4072 19.8389 11.4951 19.5167 10.6168C19.1944 9.73856 18.7333 8.93911 18.1333 8.21848C18.4444 8.10588 18.7556 8.03269 19.0667 7.99891C19.3778 7.96513 19.6889 7.94824 20 7.94824C21.4667 7.94824 22.7222 8.47746 23.7667 9.53589C24.8111 10.5943 25.3333 11.8667 25.3333 13.353ZM3.99999 26.8648H20V25.7839C20 25.5362 19.9389 25.311 19.8167 25.1083C19.6944 24.9056 19.5333 24.748 19.3333 24.6354C18.1333 24.0274 16.9222 23.5713 15.7 23.2673C14.4778 22.9633 13.2444 22.8113 12 22.8113C10.7556 22.8113 9.52222 22.9633 8.29999 23.2673C7.07777 23.5713 5.86666 24.0274 4.66666 24.6354C4.46666 24.748 4.30555 24.9056 4.18333 25.1083C4.06111 25.311 3.99999 25.5362 3.99999 25.7839V26.8648ZM12 16.0554C12.7333 16.0554 13.3611 15.7907 13.8833 15.2615C14.4056 14.7323 14.6667 14.0961 14.6667 13.353C14.6667 12.6098 14.4056 11.9736 13.8833 11.4444C13.3611 10.9152 12.7333 10.6506 12 10.6506C11.2667 10.6506 10.6389 10.9152 10.1167 11.4444C9.59444 11.9736 9.33333 12.6098 9.33333 13.353C9.33333 14.0961 9.59444 14.7323 10.1167 15.2615C10.6389 15.7907 11.2667 16.0554 12 16.0554Z" fill={isClickedAccounts ? '#A10039' : 'white'} />
                                </g>
                            </svg> */}
                        </div>
                        <div className={`text-${isClickedAccounts ? '[#A10039]' : 'white'}`}>
                            <p className="font-poppins text-[18px] font-semibold">Accounts</p>
                        </div>
                    </div>





                    <div
                        className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedDLT ? 'bg-white' : ''}`}
                        onClick={handleDLTClick}
                    >
                        <div>
                            <svg width="24" height="22" viewBox="0 0 24 22" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M1.33333 2.93479V0.232422H22.6667V2.93479H1.33333ZM1.33333 21.8514V13.7443H0V11.0419L1.33333 4.28598H22.6667L24 11.0419V13.7443H22.6667V21.8514H20V13.7443H14.6667V21.8514H1.33333ZM4 19.149H12V13.7443H4V19.149ZM2.73333 11.0419H21.2667L20.4667 6.98835H3.53333L2.73333 11.0419Z" fill={isClickedDLT ? '#A10039' : 'white'} />
                            </svg>

                        </div>
                        <div className={`text-${isClickedDLT ? '[#A10039]' : 'white'}`}>
                            <p className="font-poppins text-[18px] font-semibold">DLT</p>
                        </div>
                    </div>
                    <div
                        className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedSettings ? 'bg-[#fff]' : ''}`}
                        onClick={handleSettingsClick}
                    >
                        <div>
                            <svg width="32" height="33" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M16 11.0684C17.4145 11.0684 18.771 11.6303 19.7712 12.6305C20.7714 13.6307 21.3333 14.9872 21.3333 16.4017C21.3333 17.8162 20.7714 19.1727 19.7712 20.1729C18.771 21.1731 17.4145 21.735 16 21.735C14.5855 21.735 13.229 21.1731 12.2288 20.1729C11.2286 19.1727 10.6667 17.8162 10.6667 16.4017C10.6667 14.9872 11.2286 13.6307 12.2288 12.6305C13.229 11.6303 14.5855 11.0684 16 11.0684ZM16 13.735C15.2928 13.735 14.6145 14.016 14.1144 14.5161C13.6143 15.0162 13.3333 15.6944 13.3333 16.4017C13.3333 17.1089 13.6143 17.7872 14.1144 18.2873C14.6145 18.7874 15.2928 19.0684 16 19.0684C16.7072 19.0684 17.3855 18.7874 17.8856 18.2873C18.3857 17.7872 18.6667 17.1089 18.6667 16.4017C18.6667 15.6944 18.3857 15.0162 17.8856 14.5161C17.3855 14.016 16.7072 13.735 16 13.735ZM13.3333 29.735C13 29.735 12.72 29.495 12.6667 29.175L12.1733 25.6417C11.3333 25.3084 10.6133 24.855 9.92 24.3217L6.6 25.6684C6.30667 25.775 5.94667 25.6684 5.78667 25.375L3.12 20.7617C3.0384 20.6243 3.00963 20.4619 3.03908 20.3049C3.06852 20.1478 3.15417 20.0068 3.28 19.9084L6.09334 17.695L6 16.4017L6.09334 15.0684L3.28 12.895C3.15417 12.7965 3.06852 12.6556 3.03908 12.4985C3.00963 12.3415 3.0384 12.1791 3.12 12.0417L5.78667 7.42836C5.94667 7.13503 6.30667 7.01503 6.6 7.13503L9.92 8.46836C10.6133 7.94836 11.3333 7.49503 12.1733 7.16169L12.6667 3.62836C12.72 3.30836 13 3.06836 13.3333 3.06836H18.6667C19 3.06836 19.28 3.30836 19.3333 3.62836L19.8267 7.16169C20.6667 7.49503 21.3867 7.94836 22.08 8.46836L25.4 7.13503C25.6933 7.01503 26.0533 7.13503 26.2133 7.42836L28.88 12.0417C29.0533 12.335 28.9733 12.695 28.72 12.895L25.9067 15.0684L26 16.4017L25.9067 17.735L28.72 19.9084C28.9733 20.1084 29.0533 20.4684 28.88 20.7617L26.2133 25.375C26.0533 25.6684 25.6933 25.7884 25.4 25.6684L22.08 24.335C21.3867 24.855 20.6667 25.3084 19.8267 25.6417L19.3333 29.175C19.28 29.495 19 29.735 18.6667 29.735H13.3333ZM15 5.73503L14.5067 9.21503C12.9067 9.54836 11.4933 10.4017 10.4667 11.5884L7.25334 10.2017L6.25334 11.935L9.06667 14.0017C8.53334 15.5572 8.53334 17.2461 9.06667 18.8017L6.24 20.8817L7.24 22.615L10.48 21.2284C11.5067 22.4017 12.9067 23.255 14.4933 23.575L14.9867 27.0684H17.0133L17.5067 23.5884C19.0933 23.255 20.4933 22.4017 21.52 21.2284L24.76 22.615L25.76 20.8817L22.9333 18.815C23.4667 17.255 23.4667 15.5617 22.9333 14.0017L25.7467 11.935L24.7467 10.2017L21.5333 11.5884C20.4856 10.3755 19.0644 9.54526 17.4933 9.22836L17 5.73503H15Z" fill={isClickedSettings ? '#A10039' : 'white'} />
                            </svg>

                        </div>
                        <div className={`text-${isClickedSettings ? '[#A10039]' : 'white'}`}>
                            <p className="font-poppins text-[18px] font-semibold">Settings</p>
                        </div>
                    </div>
                    <div
                        className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedSignOut ? 'bg-[#fff]' : ''}`}
                        onClick={handleSignOutClick}
                    >
                        <div>
                            <svg width="32" height="33" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path fill-rule="evenodd" clip-rule="evenodd" d="M21.7747 22.6032L26.3371 17.2798C26.555 17.0313 26.6665 16.7175 26.6666 16.4021C26.6667 16.1863 26.6147 15.9697 26.5091 15.7727C26.4621 15.6849 26.4047 15.6015 26.3371 15.5244L21.7747 10.201C21.2955 9.64192 20.4538 9.57712 19.8947 10.0563C19.3356 10.5355 19.2708 11.3772 19.75 11.9364L22.4345 15.0687L12.1083 15.0687C11.3719 15.0687 10.775 15.6657 10.775 16.402C10.775 17.1384 11.3719 17.7354 12.1083 17.7354L22.4347 17.7354L19.75 20.8679C19.2708 21.427 19.3356 22.2687 19.8947 22.7479C20.4538 23.2271 21.2955 23.1623 21.7747 22.6032ZM13.3333 8.40202C14.0697 8.40202 14.6666 8.99897 14.6666 9.73535L14.6666 11.7354C14.6666 12.4717 15.2636 13.0687 16 13.0687C16.7363 13.0687 17.3333 12.4717 17.3333 11.7354L17.3333 9.73535C17.3333 7.52621 15.5424 5.73535 13.3333 5.73535L9.33329 5.73535C7.12415 5.73535 5.33329 7.52621 5.33329 9.73535L5.33329 23.0687C5.33329 25.2778 7.12415 27.0687 9.33329 27.0687L13.3333 27.0687C15.5424 27.0687 17.3333 25.2778 17.3333 23.0687L17.3333 21.0687C17.3333 20.3323 16.7363 19.7354 16 19.7354C15.2636 19.7354 14.6666 20.3323 14.6666 21.0687L14.6666 23.0687C14.6666 23.8051 14.0697 24.402 13.3333 24.402L9.33329 24.402C8.59691 24.402 7.99996 23.8051 7.99996 23.0687L7.99996 9.73535C7.99996 8.99897 8.59691 8.40202 9.33329 8.40202L13.3333 8.40202Z" fill={isClickedSignOut ? '#A10039' : 'white'} />
                            </svg>

                        </div>
                        <div className={`text-${isClickedSignOut ? '[#A10039]' : 'white'}`}>
                            <p className="font-poppins text-[18px] font-semibold">Sign Out</p>
                        </div>
                    </div>



                </div>

            </div>



            <div className="md:hidden absolute top-0 right-0 z-10 ">
                <button
                    className="w-14 h-14 relative focus:outline-none  rounded"
                    onClick={() => setToggle(!toggle)}
                >
                    <div className="block w-5 absolute left-6 top-1/2 transform -translate-x-1/2 -translate-y-1/2">
                        <span
                            className={`block absolute h-0.5 w-7  bg-current transform transition duration-500 ease-in-out ${toggle ? "rotate-45 text-white " : " -translate-y-1.5 bg-white bg-opacity-95"
                                }`}
                        ></span>
                        <span
                            className={`block absolute h-0.5 w-7 bg-white  bg-opacity-95e bg-current transform transition duration-500 ease-in-out ${toggle ? "opacity-0" : ""
                                }`}
                        ></span>
                        <span
                            className={`block absolute h-0.5 w-7 bg-current transform transition duration-500 ease-in-out ${toggle ? "-rotate-45 text-white " : "translate-y-1.5 bg-white  bg-opacity-95"
                                }`}
                        ></span>
                    </div>
                </button>
            </div>

            <div
                className={
                    toggle
                        ? "fixed md:hidden top-0 left-0 w-[300px] h-screen bg-gradient-to-r from-[#B41749] to-[#387BBF] bg-opacity-95 z-10 duration-300"
                        : "fixed md:hidden top-0 left-[-100%] w-[300px] h-screen bg-white z-10 duration-300"
                }
            >
                <button
                    className="w-14 h-14 relative focus:outline-none  rounded"
                    onClick={() => setToggle(!toggle)}
                >
                    <div className="block w-5 absolute left-6 top-1/2 transform -translate-x-1/2 -translate-y-1/2">
                        <span
                            className={`block absolute h-0.5 w-7 text-white bg-current transform transition duration-500 ease-in-out ${toggle ? "rotate-45" : " -translate-y-1.5"
                                }`}
                        ></span>
                        <span
                            className={`block absolute h-0.5 w-7 text-white bg-current transform transition duration-500 ease-in-out ${toggle ? "opacity-0" : ""
                                }`}
                        ></span>
                        <span
                            className={`block absolute h-0.5 w-7 text-white bg-current transform transition duration-500 ease-in-out ${toggle ? "-rotate-45" : "translate-y-1.5"
                                }`}
                        ></span>
                    </div>
                </button>
                <nav>
                    <div className='md:hidden    h-[70px] ' style={{ background: 'transparent linear-gradient(127deg, #A00039 0%, #1169B4 100%) 0% 0% no-repeat padding-box' }}
                    >
                        <div className=' mb-3 justify-center it w-full items-center'>
                            <div className='flex flex-row justify-center gap-4'>
                                <div className='text-white'>
                                    <p className='font-poppins'>Billing Rate </p>

                                </div>
                                <div className='text-white'>
                                    <p className='font-poppins'>Reports</p>
                                </div>

                            </div>


                        </div>

                        <div className='flex flex-row justify-between items-center h-[30px] px-6 mb-2'>
                            <div className='bg-white bg-opacity-50 flex justify-center items-center w-[32px] h-[32px] p-1 rounded-sm mb-3'>
                                <img src={wallet} alt="" />
                            </div>
                            <div className='bg-white bg-opacity-50 flex justify-center items-center  w-[32px] h-[32px] p-1 rounded-sm mb-3'>
                                <img src={download} alt="" />
                            </div>
                            <div className='bg-white bg-opacity-50 flex justify-center items-center  w-[32px] h-[32px] p-1 rounded-sm mb-3'>
                                <img src={browse} alt="" />
                            </div>
                            <div className='bg-white bg-opacity-50 flex justify-center items-center  w-[32px] h-[32px] p-1 rounded-sm mb-3'>
                                <img src={Vector} alt="" />
                            </div>
                        </div>

                    </div>
                    <div className=' flex w-full h-[1211px]   px-[46px] pt-[30px] container ' >


                        <div className='  w-full  h-full flex flex-col gap-[10px]'>

                            <div
                                className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedDashboard ? 'bg-white' : ''}`}
                                onClick={handleDashboardClick}
                            >
                                <div>
                                    <svg width="32" height="33" viewBox="0 0 32 33" fill={isClickedDashboard ? '#A10039' : 'white'} xmlns="http://www.w3.org/2000/svg">
                                        <mask id="mask0_1_11091" style={{ maskType: 'luminance' }} maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                            <rect y="0.401367" width="32" height="32" fill="white" />
                                        </mask>
                                        <g mask="url(#mask0_1_11091)">
                                            <path d="M13.6704 8.24756L14.0416 13.7675L14.2259 16.5419C14.2279 16.8272 14.2725 17.1107 14.3588 17.3831C14.5813 17.9119 15.1168 18.2479 15.6993 18.2244L24.5756 17.6438C24.96 17.6375 25.3312 17.7812 25.6075 18.0435C25.8377 18.262 25.9864 18.5479 26.0333 18.8554L26.0491 19.042C25.6817 24.1283 21.9461 28.3707 16.8704 29.4658C11.7946 30.5608 6.58967 28.2475 4.08147 23.7816C3.35837 22.4842 2.90672 21.0582 2.75303 19.5871C2.68883 19.1516 2.66056 18.7118 2.66849 18.2718C2.66056 12.8187 6.54382 8.10438 11.9796 6.96786C12.6339 6.86598 13.2752 7.21233 13.5376 7.80914C13.6055 7.94736 13.6503 8.09538 13.6704 8.24756Z" fill={isClickedDashboard ? '#A10039' : 'white'} />
                                            <path opacity="0.4" d="M29.3335 13.4847L29.3242 13.5282L29.2973 13.5914L29.301 13.7649C29.2871 13.9946 29.1983 14.2157 29.0455 14.3944C28.8862 14.5804 28.6686 14.707 28.429 14.7562L28.2829 14.7762L18.0418 15.4398C17.7011 15.4734 17.3619 15.3636 17.1087 15.1377C16.8975 14.9493 16.7626 14.6952 16.7245 14.4213L16.0371 4.19511C16.0251 4.16053 16.0251 4.12305 16.0371 4.08847C16.0465 3.80659 16.1706 3.54015 16.3817 3.34867C16.5926 3.15719 16.8731 3.05663 17.1602 3.06945C23.2401 3.22413 28.3499 7.59608 29.3335 13.4847Z" fill={isClickedDashboard ? '#A10039' : 'white'} />
                                        </g>
                                    </svg>
                                </div>
                                <div className={`text-${isClickedDashboard ? '#A10039' : 'white'}`}>
                                    <p className="font-poppins text-[18px] font-semibold">Dashboard</p>
                                </div>
                            </div>
                            <div
                                className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedCampaigns ? 'bg-white' : ''}`}
                                onClick={Campaigns}
                            >
                                <div>
                                    <svg width="32" height="33" viewBox="0 0 32 33" fill={isClickedCampaigns ? '#A10039' : 'white'} xmlns="http://www.w3.org/2000/svg">
                                        <path d="M26.6667 3.06836H5.33334C3.86667 3.06836 2.66667 4.26836 2.66667 5.73503V29.735L8.00001 24.4017H26.6667C28.1333 24.4017 29.3333 23.2017 29.3333 21.735V5.73503C29.3333 4.26836 28.1333 3.06836 26.6667 3.06836ZM26.6667 21.735H6.93334L5.33334 23.335V5.73503H26.6667V21.735ZM22.6667 15.0684H20V12.4017H22.6667V15.0684ZM17.3333 15.0684H14.6667V12.4017H17.3333V15.0684ZM12 15.0684H9.33334V12.4017H12" fill={isClickedCampaigns ? '#A10039' : 'white'} />
                                    </svg>
                                </div>
                                <div className={`text-${isClickedCampaigns ? '[#A10039]' : 'white'}`}>
                                    <p className="font-poppins text-[18px] font-semibold">Campaigns</p>
                                </div>
                            </div>
                            <div
                                className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedGroups ? 'bg-white' : ''}`}
                                onClick={handleGroupsClick}
                            >
                                <div>
                                    <svg width="32" height="33" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <mask id="mask0_1_11098" maskType="alpha" maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                            <rect y="0.401367" width="32" height="32" fill="#D9D9D9" />
                                        </mask>
                                        <g mask="url(#mask0_1_11098)">
                                            <path d="M1.33333 29.5672V25.7839C1.33333 25.0182 1.52777 24.3145 1.91666 23.6727C2.30555 23.0309 2.82222 22.541 3.46666 22.2032C4.84444 21.5051 6.24444 20.9816 7.66666 20.6325C9.08888 20.2834 10.5333 20.1089 12 20.1089C13.4667 20.1089 14.9111 20.2834 16.3333 20.6325C17.7556 20.9816 19.1556 21.5051 20.5333 22.2032C21.1778 22.541 21.6944 23.0309 22.0833 23.6727C22.4722 24.3145 22.6667 25.0182 22.6667 25.7839V29.5672H1.33333ZM25.3333 29.5672V25.5137C25.3333 24.5228 25.0611 23.5713 24.5167 22.6593C23.9722 21.7472 23.2 20.9647 22.2 20.3116C23.3333 20.4467 24.4 20.6775 25.4 21.0041C26.4 21.3306 27.3333 21.7303 28.2 22.2032C29 22.6536 29.6111 23.1547 30.0333 23.7064C30.4556 24.2582 30.6667 24.8606 30.6667 25.5137V29.5672H25.3333ZM12 18.7577C10.5333 18.7577 9.27777 18.2285 8.23333 17.1701C7.18888 16.1117 6.66666 14.8393 6.66666 13.353C6.66666 11.8667 7.18888 10.5943 8.23333 9.53589C9.27777 8.47746 10.5333 7.94824 12 7.94824C13.4667 7.94824 14.7222 8.47746 15.7667 9.53589C16.8111 10.5943 17.3333 11.8667 17.3333 13.353C17.3333 14.8393 16.8111 16.1117 15.7667 17.1701C14.7222 18.2285 13.4667 18.7577 12 18.7577ZM25.3333 13.353C25.3333 14.8393 24.8111 16.1117 23.7667 17.1701C22.7222 18.2285 21.4667 18.7577 20 18.7577C19.7556 18.7577 19.4444 18.7296 19.0667 18.6733C18.6889 18.617 18.3778 18.555 18.1333 18.4875C18.7333 17.7669 19.1944 16.9674 19.5167 16.0891C19.8389 15.2109 20 14.2988 20 13.353C20 12.4072 19.8389 11.4951 19.5167 10.6168C19.1944 9.73856 18.7333 8.93911 18.1333 8.21848C18.4444 8.10588 18.7556 8.03269 19.0667 7.99891C19.3778 7.96513 19.6889 7.94824 20 7.94824C21.4667 7.94824 22.7222 8.47746 23.7667 9.53589C24.8111 10.5943 25.3333 11.8667 25.3333 13.353ZM3.99999 26.8648H20V25.7839C20 25.5362 19.9389 25.311 19.8167 25.1083C19.6944 24.9056 19.5333 24.748 19.3333 24.6354C18.1333 24.0274 16.9222 23.5713 15.7 23.2673C14.4778 22.9633 13.2444 22.8113 12 22.8113C10.7556 22.8113 9.52222 22.9633 8.29999 23.2673C7.07777 23.5713 5.86666 24.0274 4.66666 24.6354C4.46666 24.748 4.30555 24.9056 4.18333 25.1083C4.06111 25.311 3.99999 25.5362 3.99999 25.7839V26.8648ZM12 16.0554C12.7333 16.0554 13.3611 15.7907 13.8833 15.2615C14.4056 14.7323 14.6667 14.0961 14.6667 13.353C14.6667 12.6098 14.4056 11.9736 13.8833 11.4444C13.3611 10.9152 12.7333 10.6506 12 10.6506C11.2667 10.6506 10.6389 10.9152 10.1167 11.4444C9.59444 11.9736 9.33333 12.6098 9.33333 13.353C9.33333 14.0961 9.59444 14.7323 10.1167 15.2615C10.6389 15.7907 11.2667 16.0554 12 16.0554Z" fill={isClickedGroups ? '#A10039' : 'white'} />
                                        </g>
                                    </svg>
                                </div>
                                <div className={`text-${isClickedGroups ? '[#A10039]' : 'white'}`}>
                                    <p className="font-poppins text-[18px] font-semibold">Groups</p>
                                </div>
                            </div>
                            <div
                                className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedTemplates ? 'bg-white' : ''}`}
                                onClick={handleTemplatesClick}
                            >
                                <div>
                                    <svg width="32" height="33" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <mask id="mask0_1_11098" maskType="alpha" maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                            <rect y="0.401367" width="32" height="32" fill="#D9D9D9" />
                                        </mask>
                                        <g mask="url(#mask0_1_11098)">
                                            <path d="M1.33333 29.5672V25.7839C1.33333 25.0182 1.52777 24.3145 1.91666 23.6727C2.30555 23.0309 2.82222 22.541 3.46666 22.2032C4.84444 21.5051 6.24444 20.9816 7.66666 20.6325C9.08888 20.2834 10.5333 20.1089 12 20.1089C13.4667 20.1089 14.9111 20.2834 16.3333 20.6325C17.7556 20.9816 19.1556 21.5051 20.5333 22.2032C21.1778 22.541 21.6944 23.0309 22.0833 23.6727C22.4722 24.3145 22.6667 25.0182 22.6667 25.7839V29.5672H1.33333ZM25.3333 29.5672V25.5137C25.3333 24.5228 25.0611 23.5713 24.5167 22.6593C23.9722 21.7472 23.2 20.9647 22.2 20.3116C23.3333 20.4467 24.4 20.6775 25.4 21.0041C26.4 21.3306 27.3333 21.7303 28.2 22.2032C29 22.6536 29.6111 23.1547 30.0333 23.7064C30.4556 24.2582 30.6667 24.8606 30.6667 25.5137V29.5672H25.3333ZM12 18.7577C10.5333 18.7577 9.27777 18.2285 8.23333 17.1701C7.18888 16.1117 6.66666 14.8393 6.66666 13.353C6.66666 11.8667 7.18888 10.5943 8.23333 9.53589C9.27777 8.47746 10.5333 7.94824 12 7.94824C13.4667 7.94824 14.7222 8.47746 15.7667 9.53589C16.8111 10.5943 17.3333 11.8667 17.3333 13.353C17.3333 14.8393 16.8111 16.1117 15.7667 17.1701C14.7222 18.2285 13.4667 18.7577 12 18.7577ZM25.3333 13.353C25.3333 14.8393 24.8111 16.1117 23.7667 17.1701C22.7222 18.2285 21.4667 18.7577 20 18.7577C19.7556 18.7577 19.4444 18.7296 19.0667 18.6733C18.6889 18.617 18.3778 18.555 18.1333 18.4875C18.7333 17.7669 19.1944 16.9674 19.5167 16.0891C19.8389 15.2109 20 14.2988 20 13.353C20 12.4072 19.8389 11.4951 19.5167 10.6168C19.1944 9.73856 18.7333 8.93911 18.1333 8.21848C18.4444 8.10588 18.7556 8.03269 19.0667 7.99891C19.3778 7.96513 19.6889 7.94824 20 7.94824C21.4667 7.94824 22.7222 8.47746 23.7667 9.53589C24.8111 10.5943 25.3333 11.8667 25.3333 13.353ZM3.99999 26.8648H20V25.7839C20 25.5362 19.9389 25.311 19.8167 25.1083C19.6944 24.9056 19.5333 24.748 19.3333 24.6354C18.1333 24.0274 16.9222 23.5713 15.7 23.2673C14.4778 22.9633 13.2444 22.8113 12 22.8113C10.7556 22.8113 9.52222 22.9633 8.29999 23.2673C7.07777 23.5713 5.86666 24.0274 4.66666 24.6354C4.46666 24.748 4.30555 24.9056 4.18333 25.1083C4.06111 25.311 3.99999 25.5362 3.99999 25.7839V26.8648ZM12 16.0554C12.7333 16.0554 13.3611 15.7907 13.8833 15.2615C14.4056 14.7323 14.6667 14.0961 14.6667 13.353C14.6667 12.6098 14.4056 11.9736 13.8833 11.4444C13.3611 10.9152 12.7333 10.6506 12 10.6506C11.2667 10.6506 10.6389 10.9152 10.1167 11.4444C9.59444 11.9736 9.33333 12.6098 9.33333 13.353C9.33333 14.0961 9.59444 14.7323 10.1167 15.2615C10.6389 15.7907 11.2667 16.0554 12 16.0554Z" fill={isClickedTemplates ? '#A10039' : 'white'} />
                                        </g>
                                    </svg>
                                </div>
                                <div className={`text-${isClickedTemplates ? '[#A10039]' : 'white'}`}>
                                    <p className="font-poppins text-[18px] font-semibold">Templates</p>
                                </div>
                            </div>
                            <div
                                className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedDLT ? 'bg-white' : ''}`}
                                onClick={handleDLTClick}
                            >
                                <div>
                                    <svg width="32" height="33" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <mask id="mask0_1_11098" maskType="alpha" maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                            <rect y="0.401367" width="32" height="32" fill="#D9D9D9" />
                                        </mask>
                                        <g mask="url(#mask0_1_11098)">
                                            <path d="M1.33333 29.5672V25.7839C1.33333 25.0182 1.52777 24.3145 1.91666 23.6727C2.30555 23.0309 2.82222 22.541 3.46666 22.2032C4.84444 21.5051 6.24444 20.9816 7.66666 20.6325C9.08888 20.2834 10.5333 20.1089 12 20.1089C13.4667 20.1089 14.9111 20.2834 16.3333 20.6325C17.7556 20.9816 19.1556 21.5051 20.5333 22.2032C21.1778 22.541 21.6944 23.0309 22.0833 23.6727C22.4722 24.3145 22.6667 25.0182 22.6667 25.7839V29.5672H1.33333ZM25.3333 29.5672V25.5137C25.3333 24.5228 25.0611 23.5713 24.5167 22.6593C23.9722 21.7472 23.2 20.9647 22.2 20.3116C23.3333 20.4467 24.4 20.6775 25.4 21.0041C26.4 21.3306 27.3333 21.7303 28.2 22.2032C29 22.6536 29.6111 23.1547 30.0333 23.7064C30.4556 24.2582 30.6667 24.8606 30.6667 25.5137V29.5672H25.3333ZM12 18.7577C10.5333 18.7577 9.27777 18.2285 8.23333 17.1701C7.18888 16.1117 6.66666 14.8393 6.66666 13.353C6.66666 11.8667 7.18888 10.5943 8.23333 9.53589C9.27777 8.47746 10.5333 7.94824 12 7.94824C13.4667 7.94824 14.7222 8.47746 15.7667 9.53589C16.8111 10.5943 17.3333 11.8667 17.3333 13.353C17.3333 14.8393 16.8111 16.1117 15.7667 17.1701C14.7222 18.2285 13.4667 18.7577 12 18.7577ZM25.3333 13.353C25.3333 14.8393 24.8111 16.1117 23.7667 17.1701C22.7222 18.2285 21.4667 18.7577 20 18.7577C19.7556 18.7577 19.4444 18.7296 19.0667 18.6733C18.6889 18.617 18.3778 18.555 18.1333 18.4875C18.7333 17.7669 19.1944 16.9674 19.5167 16.0891C19.8389 15.2109 20 14.2988 20 13.353C20 12.4072 19.8389 11.4951 19.5167 10.6168C19.1944 9.73856 18.7333 8.93911 18.1333 8.21848C18.4444 8.10588 18.7556 8.03269 19.0667 7.99891C19.3778 7.96513 19.6889 7.94824 20 7.94824C21.4667 7.94824 22.7222 8.47746 23.7667 9.53589C24.8111 10.5943 25.3333 11.8667 25.3333 13.353ZM3.99999 26.8648H20V25.7839C20 25.5362 19.9389 25.311 19.8167 25.1083C19.6944 24.9056 19.5333 24.748 19.3333 24.6354C18.1333 24.0274 16.9222 23.5713 15.7 23.2673C14.4778 22.9633 13.2444 22.8113 12 22.8113C10.7556 22.8113 9.52222 22.9633 8.29999 23.2673C7.07777 23.5713 5.86666 24.0274 4.66666 24.6354C4.46666 24.748 4.30555 24.9056 4.18333 25.1083C4.06111 25.311 3.99999 25.5362 3.99999 25.7839V26.8648ZM12 16.0554C12.7333 16.0554 13.3611 15.7907 13.8833 15.2615C14.4056 14.7323 14.6667 14.0961 14.6667 13.353C14.6667 12.6098 14.4056 11.9736 13.8833 11.4444C13.3611 10.9152 12.7333 10.6506 12 10.6506C11.2667 10.6506 10.6389 10.9152 10.1167 11.4444C9.59444 11.9736 9.33333 12.6098 9.33333 13.353C9.33333 14.0961 9.59444 14.7323 10.1167 15.2615C10.6389 15.7907 11.2667 16.0554 12 16.0554Z" fill={isClickedDLT ? '#A10039' : 'white'} />
                                        </g>
                                    </svg>
                                </div>
                                <div className={`text-${isClickedDLT ? '[#A10039]' : 'white'}`}>
                                    <p className="font-poppins text-[18px] font-semibold">DLT</p>
                                </div>
                            </div>
                            <div
                                className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedSettings ? 'bg-[#fff]' : ''}`}
                                onClick={handleSettingsClick}
                            >
                                <div>
                                    <svg width="32" height="33" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <mask id="mask0_1_11098" maskType="alpha" maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                            <rect y="0.401367" width="32" height="32" fill="#D9D9D9" />
                                        </mask>
                                        <g mask="url(#mask0_1_11098)">
                                            <path d="M1.33333 29.5672V25.7839C1.33333 25.0182 1.52777 24.3145 1.91666 23.6727C2.30555 23.0309 2.82222 22.541 3.46666 22.2032C4.84444 21.5051 6.24444 20.9816 7.66666 20.6325C9.08888 20.2834 10.5333 20.1089 12 20.1089C13.4667 20.1089 14.9111 20.2834 16.3333 20.6325C17.7556 20.9816 19.1556 21.5051 20.5333 22.2032C21.1778 22.541 21.6944 23.0309 22.0833 23.6727C22.4722 24.3145 22.6667 25.0182 22.6667 25.7839V29.5672H1.33333ZM25.3333 29.5672V25.5137C25.3333 24.5228 25.0611 23.5713 24.5167 22.6593C23.9722 21.7472 23.2 20.9647 22.2 20.3116C23.3333 20.4467 24.4 20.6775 25.4 21.0041C26.4 21.3306 27.3333 21.7303 28.2 22.2032C29 22.6536 29.6111 23.1547 30.0333 23.7064C30.4556 24.2582 30.6667 24.8606 30.6667 25.5137V29.5672H25.3333ZM12 18.7577C10.5333 18.7577 9.27777 18.2285 8.23333 17.1701C7.18888 16.1117 6.66666 14.8393 6.66666 13.353C6.66666 11.8667 7.18888 10.5943 8.23333 9.53589C9.27777 8.47746 10.5333 7.94824 12 7.94824C13.4667 7.94824 14.7222 8.47746 15.7667 9.53589C16.8111 10.5943 17.3333 11.8667 17.3333 13.353C17.3333 14.8393 16.8111 16.1117 15.7667 17.1701C14.7222 18.2285 13.4667 18.7577 12 18.7577ZM25.3333 13.353C25.3333 14.8393 24.8111 16.1117 23.7667 17.1701C22.7222 18.2285 21.4667 18.7577 20 18.7577C19.7556 18.7577 19.4444 18.7296 19.0667 18.6733C18.6889 18.617 18.3778 18.555 18.1333 18.4875C18.7333 17.7669 19.1944 16.9674 19.5167 16.0891C19.8389 15.2109 20 14.2988 20 13.353C20 12.4072 19.8389 11.4951 19.5167 10.6168C19.1944 9.73856 18.7333 8.93911 18.1333 8.21848C18.4444 8.10588 18.7556 8.03269 19.0667 7.99891C19.3778 7.96513 19.6889 7.94824 20 7.94824C21.4667 7.94824 22.7222 8.47746 23.7667 9.53589C24.8111 10.5943 25.3333 11.8667 25.3333 13.353ZM3.99999 26.8648H20V25.7839C20 25.5362 19.9389 25.311 19.8167 25.1083C19.6944 24.9056 19.5333 24.748 19.3333 24.6354C18.1333 24.0274 16.9222 23.5713 15.7 23.2673C14.4778 22.9633 13.2444 22.8113 12 22.8113C10.7556 22.8113 9.52222 22.9633 8.29999 23.2673C7.07777 23.5713 5.86666 24.0274 4.66666 24.6354C4.46666 24.748 4.30555 24.9056 4.18333 25.1083C4.06111 25.311 3.99999 25.5362 3.99999 25.7839V26.8648ZM12 16.0554C12.7333 16.0554 13.3611 15.7907 13.8833 15.2615C14.4056 14.7323 14.6667 14.0961 14.6667 13.353C14.6667 12.6098 14.4056 11.9736 13.8833 11.4444C13.3611 10.9152 12.7333 10.6506 12 10.6506C11.2667 10.6506 10.6389 10.9152 10.1167 11.4444C9.59444 11.9736 9.33333 12.6098 9.33333 13.353C9.33333 14.0961 9.59444 14.7323 10.1167 15.2615C10.6389 15.7907 11.2667 16.0554 12 16.0554Z" fill={isClickedSettings ? '#A10039' : 'white'} />
                                        </g>
                                    </svg>
                                </div>
                                <div className={`text-${isClickedSettings ? '[#A10039]' : 'white'}`}>
                                    <p className="font-poppins text-[18px] font-semibold">Settings</p>
                                </div>
                            </div>
                            <div
                                className={`flex items-center justify-start py-4 px-6 gap-[24px] rounded-[16px] ${isClickedSignOut ? 'bg-[#fff]' : ''}`}
                                onClick={handleSignOutClick}
                            >
                                <div>
                                    <svg width="32" height="33" viewBox="0 0 32 33" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <mask id="mask0_1_11098" maskType="alpha" maskUnits="userSpaceOnUse" x="0" y="0" width="32" height="33">
                                            <rect y="0.401367" width="32" height="32" fill="#D9D9D9" />
                                        </mask>
                                        <g mask="url(#mask0_1_11098)">
                                            <path d="M1.33333 29.5672V25.7839C1.33333 25.0182 1.52777 24.3145 1.91666 23.6727C2.30555 23.0309 2.82222 22.541 3.46666 22.2032C4.84444 21.5051 6.24444 20.9816 7.66666 20.6325C9.08888 20.2834 10.5333 20.1089 12 20.1089C13.4667 20.1089 14.9111 20.2834 16.3333 20.6325C17.7556 20.9816 19.1556 21.5051 20.5333 22.2032C21.1778 22.541 21.6944 23.0309 22.0833 23.6727C22.4722 24.3145 22.6667 25.0182 22.6667 25.7839V29.5672H1.33333ZM25.3333 29.5672V25.5137C25.3333 24.5228 25.0611 23.5713 24.5167 22.6593C23.9722 21.7472 23.2 20.9647 22.2 20.3116C23.3333 20.4467 24.4 20.6775 25.4 21.0041C26.4 21.3306 27.3333 21.7303 28.2 22.2032C29 22.6536 29.6111 23.1547 30.0333 23.7064C30.4556 24.2582 30.6667 24.8606 30.6667 25.5137V29.5672H25.3333ZM12 18.7577C10.5333 18.7577 9.27777 18.2285 8.23333 17.1701C7.18888 16.1117 6.66666 14.8393 6.66666 13.353C6.66666 11.8667 7.18888 10.5943 8.23333 9.53589C9.27777 8.47746 10.5333 7.94824 12 7.94824C13.4667 7.94824 14.7222 8.47746 15.7667 9.53589C16.8111 10.5943 17.3333 11.8667 17.3333 13.353C17.3333 14.8393 16.8111 16.1117 15.7667 17.1701C14.7222 18.2285 13.4667 18.7577 12 18.7577ZM25.3333 13.353C25.3333 14.8393 24.8111 16.1117 23.7667 17.1701C22.7222 18.2285 21.4667 18.7577 20 18.7577C19.7556 18.7577 19.4444 18.7296 19.0667 18.6733C18.6889 18.617 18.3778 18.555 18.1333 18.4875C18.7333 17.7669 19.1944 16.9674 19.5167 16.0891C19.8389 15.2109 20 14.2988 20 13.353C20 12.4072 19.8389 11.4951 19.5167 10.6168C19.1944 9.73856 18.7333 8.93911 18.1333 8.21848C18.4444 8.10588 18.7556 8.03269 19.0667 7.99891C19.3778 7.96513 19.6889 7.94824 20 7.94824C21.4667 7.94824 22.7222 8.47746 23.7667 9.53589C24.8111 10.5943 25.3333 11.8667 25.3333 13.353ZM3.99999 26.8648H20V25.7839C20 25.5362 19.9389 25.311 19.8167 25.1083C19.6944 24.9056 19.5333 24.748 19.3333 24.6354C18.1333 24.0274 16.9222 23.5713 15.7 23.2673C14.4778 22.9633 13.2444 22.8113 12 22.8113C10.7556 22.8113 9.52222 22.9633 8.29999 23.2673C7.07777 23.5713 5.86666 24.0274 4.66666 24.6354C4.46666 24.748 4.30555 24.9056 4.18333 25.1083C4.06111 25.311 3.99999 25.5362 3.99999 25.7839V26.8648ZM12 16.0554C12.7333 16.0554 13.3611 15.7907 13.8833 15.2615C14.4056 14.7323 14.6667 14.0961 14.6667 13.353C14.6667 12.6098 14.4056 11.9736 13.8833 11.4444C13.3611 10.9152 12.7333 10.6506 12 10.6506C11.2667 10.6506 10.6389 10.9152 10.1167 11.4444C9.59444 11.9736 9.33333 12.6098 9.33333 13.353C9.33333 14.0961 9.59444 14.7323 10.1167 15.2615C10.6389 15.7907 11.2667 16.0554 12 16.0554Z" fill={isClickedSignOut ? '#A10039' : 'white'} />
                                        </g>
                                    </svg>
                                </div>
                                <div className={`text-${isClickedSignOut ? '[#A10039]' : 'white'}`}>
                                    <p className="font-poppins text-[18px] font-semibold">Sign Out</p>
                                </div>
                            </div>



                        </div>

                    </div>


                </nav>
            </div>
        </>
    )
}

export default Sidebar
