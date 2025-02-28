import React, { useState } from 'react'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'
import { IoIosArrowDown } from "react-icons/io";
// import { Dropdown } from 'flowbite-react';
import { FaEye } from "react-icons/fa";
import { GoPencil } from "react-icons/go";
import { FaAnglesLeft } from "react-icons/fa6";
import { FaAnglesRight } from "react-icons/fa6";








function Account() {
    const [togiledrop, settogiledrop] = useState(false)
    return (
        <div>
            <Navbar pageName="Account" />

            <div className='flex w-auto '>
                <Sidebar />


                <div className=' w-full  md:p-0 '>
                    <div className=''>
                        <div class="flex  md:pl-[33px]">
                            <div class="md:w-1/2 md:h-[64px] flex items-center flex-row ">
                                <div className='flex items-center flex-row '>
                                    <div>
                                        <svg width="24px" height="24px" viewBox="0 0 32 33" fill='[#B01A4D]' xmlns="http://www.w3.org/2000/svg">
                                            <path d="M26.6667 3.06836H5.33334C3.86667 3.06836 2.66667 4.26836 2.66667 5.73503V29.735L8.00001 24.4017H26.6667C28.1333 24.4017 29.3333 23.2017 29.3333 21.735V5.73503C29.3333 4.26836 28.1333 3.06836 26.6667 3.06836ZM26.6667 21.735H6.93334L5.33334 23.335V5.73503H26.6667V21.735ZM22.6667 15.0684H20V12.4017H22.6667V15.0684ZM17.3333 15.0684H14.6667V12.4017H17.3333V15.0684ZM12 15.0684H9.33334V12.4017H12" fill='[#B01A4D]' />
                                        </svg>
                                    </div>
                                    <div className='pl-[5px] text-[#B01A4D]'>
                                        Campaigns
                                    </div>


                                </div>

                                <div className=' md:pl-[40px] pl-[10px] flex items-center flex-row'>
                                    <div>
                                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M10 20C8.61667 20 7.31667 19.7375 6.1 19.2125C4.88333 18.6875 3.825 17.975 2.925 17.075C2.025 16.175 1.3125 15.1167 0.7875 13.9C0.2625 12.6833 0 11.3833 0 10C0 8.61667 0.2625 7.31667 0.7875 6.1C1.3125 4.88333 2.025 3.825 2.925 2.925C3.825 2.025 4.88333 1.3125 6.1 0.7875C7.31667 0.2625 8.61667 0 10 0C11.3833 0 12.6833 0.2625 13.9 0.7875C15.1167 1.3125 16.175 2.025 17.075 2.925C17.975 3.825 18.6875 4.88333 19.2125 6.1C19.7375 7.31667 20 8.61667 20 10C20 10.45 19.975 10.8917 19.925 11.325C19.875 11.7583 19.7917 12.1833 19.675 12.6C19.4417 12.3333 19.1708 12.1083 18.8625 11.925C18.5542 11.7417 18.2167 11.6167 17.85 11.55C17.9 11.3 17.9375 11.0458 17.9625 10.7875C17.9875 10.5292 18 10.2667 18 10C18 7.76667 17.225 5.875 15.675 4.325C14.125 2.775 12.2333 2 10 2C7.76667 2 5.875 2.775 4.325 4.325C2.775 5.875 2 7.76667 2 10C2 12.2333 2.775 14.125 4.325 15.675C5.875 17.225 7.76667 18 10 18C10.85 18 11.6625 17.875 12.4375 17.625C13.2125 17.375 13.925 17.025 14.575 16.575C14.775 16.8583 15.0208 17.1083 15.3125 17.325C15.6042 17.5417 15.9167 17.7083 16.25 17.825C15.4 18.5083 14.4458 19.0417 13.3875 19.425C12.3292 19.8083 11.2 20 10 20ZM17.25 16C16.9 16 16.6042 15.8792 16.3625 15.6375C16.1208 15.3958 16 15.1 16 14.75C16 14.4 16.1208 14.1042 16.3625 13.8625C16.6042 13.6208 16.9 13.5 17.25 13.5C17.6 13.5 17.8958 13.6208 18.1375 13.8625C18.3792 14.1042 18.5 14.4 18.5 14.75C18.5 15.1 18.3792 15.3958 18.1375 15.6375C17.8958 15.8792 17.6 16 17.25 16ZM13.3 14.7L9 10.4V5H11V9.6L14.7 13.3L13.3 14.7Z" fill="#1C1B1F" />
                                        </svg>

                                    </div>
                                    <div className='pl-[5px]'>
                                        Scheduled
                                    </div>



                                </div>

                            </div>

                            <div class="w-1/2  md:h-[64px] flex justify-end ">
                                <div className=' pl-2  flex justify-center items-center md:pr-[76px] pt-[13px] pb-[13px] ' onClick={() => settogiledrop(!togiledrop)}>

                                    <div className='flex flex-row justify-center items-center bg-blue-200 md:py-[10px] py-[5px] rounded-[4px]'>

                                        <div className='font-poppins text-xs md:pl-[20px] pl-[4px]'>
                                            New Campaign


                                        </div>
                                        <div className='flex justify-center items-center md:pr-[11px] pr-[5px] md:pl-[5px] pl-[2px]'>
                                            <IoIosArrowDown />


                                        </div>
                                    </div>



                                    {togiledrop &&
                                        <div className='flex flex-col absolute md:right-10 right-1 md:top-[150px] top-24 mt-2 bg-white shadow-md rounded-md '>
                                            <div className='px-4 py-2'>Option 1</div>
                                            <div className='px-4 py-2'>Option 2</div>
                                            <div className='px-4 py-2'>Option 3</div>
                                        </div>}

                                </div>

                            </div>
                        </div>


                    </div>



                    <div className='bg-gray-500'>
                        <div className='md:px-[33px] px-2 pb-11 bg-gray-200 md:h-full '>
                            <div className=' mb-5 pt-4'>
                                <p className='font-poppins text-[#979797] '>Campaigns</p>

                            </div>




                            <div className='bg-white  p-[30px] rounded-[20px] mb-4'>
                                <div className='mb-[30px]'>
                                    <p className='font-poppins font-semibold'>Today’s Count</p>
                                    <p className='font-poppins text-gray-300'>Campaigns Summery</p>

                                </div>



                                {/* here */}

                                <div className=' '>



                                    <div className=' md:flex md:flex-row  md:gap-[8px]  '>

                                        <div className='bg-[#F3E8FF] md:w-[25%] p-[20px] rounded-[12px] md:mb-0 mb-3 '>


                                            <div className=' flex-row  gap-[8px] flex items-center justify-between'>


                                                <div className='flex flex-row gap-[10px]   justify-center items-center'>
                                                    <div className='rounded-full bg-[#BF83FF] h-[40px] w-[40px] flex items-center justify-center'>

                                                        <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path fill-rule="evenodd" clip-rule="evenodd" d="M2 0C0.895432 0 0 0.895447 0 2V16C0 17.1046 0.895432 18 2 18H16C17.1046 18 18 17.1046 18 16V2C18 0.895447 17.1046 0 16 0H2ZM5 10C5 9.44769 4.55228 9 4 9C3.44772 9 3 9.44769 3 10V14C3 14.5523 3.44772 15 4 15C4.55228 15 5 14.5523 5 14V10ZM9 6C9.55228 6 10 6.44769 10 7V14C10 14.5523 9.55228 15 9 15C8.44772 15 8 14.5523 8 14V7C8 6.44769 8.44772 6 9 6ZM15 4C15 3.44769 14.5523 3 14 3C13.4477 3 13 3.44769 13 4V14C13 14.5523 13.4477 15 14 15C14.5523 15 15 14.5523 15 14V4Z" fill="white" />
                                                        </svg>


                                                    </div>

                                                    <div>
                                                        <p className='font-poppins font-semibold'>200</p>


                                                    </div>



                                                </div>


                                                <div>
                                                    <p className='font-poppins font-medium'>Total Campaigns</p>
                                                </div>


                                            </div>


                                        </div>





                                        <div className='bg-[#DCFCE7] p-[20px] md:w-[25%]  rounded-[12px] md:mb-0 mb-3 '>

                                            <div className=' flex-row  gap-[52px] flex items-center justify-between'>


                                                <div className='flex flex-row gap-[10px]   items-center'>
                                                    <div className='rounded-full bg-[#3CD856] h-[40px] w-[40px] flex items-center justify-center'>

                                                        <svg width="21" height="17" viewBox="0 0 21 17" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path d="M2.13086 16.0815C1.58086 16.0815 1.11003 15.8857 0.718359 15.494C0.326693 15.1024 0.130859 14.6315 0.130859 14.0815V2.08154C0.130859 1.53154 0.326693 1.06071 0.718359 0.669043C1.11003 0.277376 1.58086 0.081543 2.13086 0.081543H18.1309C18.6809 0.081543 19.1517 0.277376 19.5434 0.669043C19.935 1.06071 20.1309 1.53154 20.1309 2.08154V14.0815C20.1309 14.6315 19.935 15.1024 19.5434 15.494C19.1517 15.8857 18.6809 16.0815 18.1309 16.0815H2.13086ZM2.13086 4.08154H18.1309V2.08154H2.13086V4.08154ZM9.08086 12.6315L14.7309 6.98154L13.2809 5.53154L9.08086 9.73154L6.98086 7.63154L5.53086 9.08154L9.08086 12.6315Z" fill="white" />
                                                        </svg>


                                                    </div>

                                                    <div className='flex '>
                                                        <p className='font-poppins font-semibold'>150</p>


                                                    </div>



                                                </div>


                                                <div>
                                                    <p className='font-poppins font-medium'>Completed</p>
                                                </div>


                                            </div>


                                        </div>

                                        <div className='bg-[#FFF4DE] md:w-[25%]  p-[20px] rounded-[12px] md:mb-0 mb-3 '>

                                            <div className=' flex-row  gap-[52px] flex items-center justify-between'>


                                                <div className='flex flex-row gap-[20px]  justify-center items-center'>
                                                    <div className='rounded-full bg-[#FF947A] h-[40px] w-[40px] flex items-center justify-center'>

                                                        <svg width="19" height="21" viewBox="0 0 19 21" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path d="M14 21C12.6167 21 11.4375 20.5125 10.4625 19.5375C9.4875 18.5625 9 17.3833 9 16C9 14.6167 9.4875 13.4375 10.4625 12.4625C11.4375 11.4875 12.6167 11 14 11C15.3833 11 16.5625 11.4875 17.5375 12.4625C18.5125 13.4375 19 14.6167 19 16C19 17.3833 18.5125 18.5625 17.5375 19.5375C16.5625 20.5125 15.3833 21 14 21ZM15.675 18.375L16.375 17.675L14.5 15.8V13H13.5V16.2L15.675 18.375ZM2 20C1.45 20 0.979167 19.8042 0.5875 19.4125C0.195833 19.0208 0 18.55 0 18V4C0 3.45 0.195833 2.97917 0.5875 2.5875C0.979167 2.19583 1.45 2 2 2H6.175C6.35833 1.41667 6.71667 0.9375 7.25 0.5625C7.78333 0.1875 8.36667 0 9 0C9.66667 0 10.2625 0.1875 10.7875 0.5625C11.3125 0.9375 11.6667 1.41667 11.85 2H16C16.55 2 17.0208 2.19583 17.4125 2.5875C17.8042 2.97917 18 3.45 18 4V10.25C17.7 10.0333 17.3833 9.85 17.05 9.7C16.7167 9.55 16.3667 9.41667 16 9.3V4H14V7H4V4H2V18H7.3C7.41667 18.3667 7.55 18.7167 7.7 19.05C7.85 19.3833 8.03333 19.7 8.25 20H2ZM9 4C9.28333 4 9.52083 3.90417 9.7125 3.7125C9.90417 3.52083 10 3.28333 10 3C10 2.71667 9.90417 2.47917 9.7125 2.2875C9.52083 2.09583 9.28333 2 9 2C8.71667 2 8.47917 2.09583 8.2875 2.2875C8.09583 2.47917 8 2.71667 8 3C8 3.28333 8.09583 3.52083 8.2875 3.7125C8.47917 3.90417 8.71667 4 9 4Z" fill="white" />
                                                        </svg>


                                                    </div>

                                                    <div>
                                                        <p className='font-poppins font-semibold'>50</p>


                                                    </div>



                                                </div>


                                                <div>
                                                    <p className='font-poppins font-medium'>Running</p>
                                                </div>


                                            </div>


                                        </div>

                                        <div className='bg-[#FFE2E5] p-[20px] md:w-[25%] rounded-[12px] md:mb-0 mb-3 '>

                                            <div className=' flex-row  gap-[0px] flex items-center justify-between'>


                                                <div className='flex flex-row gap-[20px]  justify-center items-center'>
                                                    <div className='rounded-full bg-[#FA5A7D] h-[40px] w-[40px] flex items-center justify-center'>

                                                        <svg width="20" height="19" viewBox="0 0 20 19" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path d="M12.3559 15.9037L14.1059 14.1537L15.8559 15.9037L16.6059 15.1537L14.8809 13.4287L16.6309 11.6787L15.8809 10.9287L14.1309 12.6787L12.3809 10.9287L11.6309 11.6787L13.3809 13.4287L11.6309 15.1787L12.3559 15.9037ZM0.130859 16.4287V10.4287L8.13086 8.42871L0.130859 6.42871V0.428711L14.4309 6.42871H14.1309C12.1809 6.42871 10.5267 7.11621 9.16836 8.49121C7.81003 9.86621 7.13086 11.5287 7.13086 13.4787L0.130859 16.4287ZM14.1309 18.4287C12.7475 18.4287 11.5684 17.9412 10.5934 16.9662C9.61836 15.9912 9.13086 14.812 9.13086 13.4287C9.13086 12.0454 9.61836 10.8662 10.5934 9.89121C11.5684 8.91621 12.7475 8.42871 14.1309 8.42871C15.5142 8.42871 16.6934 8.91621 17.6684 9.89121C18.6434 10.8662 19.1309 12.0454 19.1309 13.4287C19.1309 14.812 18.6434 15.9912 17.6684 16.9662C16.6934 17.9412 15.5142 18.4287 14.1309 18.4287Z" fill="white" />
                                                        </svg>


                                                    </div>

                                                    <div>
                                                        <p className='font-poppins font-semibold'>50</p>


                                                    </div>



                                                </div>


                                                <div>
                                                    <p className='font-poppins font-medium'>Failed Campaigns</p>
                                                </div>


                                            </div>


                                        </div>


                                    </div>



                                </div>



                                {/* here */}


                            </div>


                            <div className='bg-white  p-[30px] rounded-[20px] mb-10 md:h-[779px]'>
                                <div class="flex mb-4">
                                    <div class="md:w-1/2 w-[40%]  h-12">
                                        <p className='font-poppins font-semibold md:text-[20px]'>Campaign List</p>
                                        <p className='font-poppins text-gray-400 md:text-[16px] text-[9px]'>Please note that selected this week includes today</p>
                                    </div>
                                    <div class="md:w-1/2 w-[60%]  h-12 flex flex-row md:gap-[20px] gap-2 justify-end items-center">
                                        <div className=' md:py-[10px] '>

                                            <div className='border border-gray-600  md:py-[5px] md:px-[30px] rounded-md text-gray-600'>
                                                <button>This week  v</button>

                                            </div>
                                        </div>
                                        <div className=' md:py-[10px] '>

                                            <div className='bg-[#3B79BC] md:py-[5px] md:px-[30px] rounded-md text-white'>
                                                <button>Submit</button>

                                            </div>
                                        </div>




                                    </div>
                                </div>



                                <div className=''>



                                    {/* <div className=' h-auto md:py-52 py-14'>

                    <div className=' flex justify-center items-center'>
                      <svg width="200" height="200" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M180 0H20C9 0 0 9 0 20V200L40 160H180C191 160 200 151 200 140V20C200 9 191 0 180 0ZM180 140H32L20 152V20H180V140ZM150 90H130V70H150V90ZM110 90H90V70H110V90ZM70 90H50V70H70" fill="#D7D7D7" />
                      </svg>


                    </div>
                    <div className='flex justify-center items-center'>
                      <h1 className='font-poppins font-semibold'>No Campaigns</h1>


                    </div>
                    <div className='flex justify-center items-center'>
                      <h1 className='font-poppins text-gray-400'> You don't have any campaigns for the selected period. Please try with different range</h1>


                    </div>



                  </div> */}



                                    <div class="flex flex-col">
                                        <div class="overflow-x-auto sm:-mx-6 lg:-mx-8 mb-6">
                                            <div class="inline-block min-w-full py-2 sm:px-6 lg:px-8">
                                                <div class="overflow-hidden">
                                                    <table class="min-w-full text-left text-sm font-light">
                                                        <thead
                                                            class="border-b bg-neutral-100 font-medium dark:border-neutral-500 dark:bg-neutral-700">
                                                            <tr>
                                                                <th scope="col" class="px-6 py-4">Serial No.</th>
                                                                <th scope="col" class="px-6 py-4">Campaign Name</th>
                                                                <th scope="col" class="px-6 py-4">Message</th>
                                                                <th scope="col" class="px-6 py-4">Campaign Preference</th>
                                                                <th scope="col" class="px-6 py-4">Action </th>

                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <tr
                                                                class="border-b bg-white dark:border-neutral-500 dark:bg-neutral-700">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">1</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Mark</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Otto</td>

                                                                <td class="whitespace-nowrap px-6 py-4">@mdo</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>
                                                            </tr>
                                                            <tr
                                                                class="border-b bg-neutral-100 dark:border-neutral-500 dark:bg-neutral-600">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">2</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Jacob</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Thornton</td>
                                                                <td class="whitespace-nowrap px-6 py-4">@fat</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>

                                                            </tr>
                                                            <tr
                                                                class="border-b bg-white dark:border-neutral-500 dark:bg-neutral-700">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">3</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Mark</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Otto</td>

                                                                <td class="whitespace-nowrap px-6 py-4">@mdo</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>
                                                            </tr>
                                                            <tr
                                                                class="border-b bg-neutral-100 dark:border-neutral-500 dark:bg-neutral-600">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">4</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Jacob</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Thornton</td>
                                                                <td class="whitespace-nowrap px-6 py-4">@fat</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>

                                                            </tr>
                                                            <tr
                                                                class="border-b bg-white dark:border-neutral-500 dark:bg-neutral-700">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">5</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Mark</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Otto</td>

                                                                <td class="whitespace-nowrap px-6 py-4">@mdo</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>
                                                            </tr>
                                                            <tr
                                                                class="border-b bg-neutral-100 dark:border-neutral-500 dark:bg-neutral-600">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">6</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Jacob</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Thornton</td>
                                                                <td class="whitespace-nowrap px-6 py-4">@fat</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>

                                                            </tr>
                                                            <tr
                                                                class="border-b bg-white dark:border-neutral-500 dark:bg-neutral-700">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">7</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Mark</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Otto</td>

                                                                <td class="whitespace-nowrap px-6 py-4">@mdo</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>
                                                            </tr>
                                                            <tr
                                                                class="border-b bg-neutral-100 dark:border-neutral-500 dark:bg-neutral-600">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">8</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Jacob</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Thornton</td>
                                                                <td class="whitespace-nowrap px-6 py-4">@fat</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>

                                                            </tr>

                                                            <tr
                                                                class="border-b bg-white dark:border-neutral-500 dark:bg-neutral-700">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">9</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Mark</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Otto</td>

                                                                <td class="whitespace-nowrap px-6 py-4">@mdo</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>
                                                            </tr>
                                                            <tr
                                                                class="border-b bg-neutral-100 dark:border-neutral-500 dark:bg-neutral-600">
                                                                <td class="whitespace-nowrap px-6 py-4 font-medium">10</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Jacob</td>
                                                                <td class="whitespace-nowrap px-6 py-4">Thornton</td>
                                                                <td class="whitespace-nowrap px-6 py-4">@fat</td>
                                                                <td className='px-6 py-4'>
                                                                    <button className="btn-icon-shedule md:pr-4 pr-2" style={{ color: '#A10039' }}><FaEye /></button>
                                                                    <button className="btn-icon-shedule" style={{ color: '#A10039' }}><GoPencil /></button>
                                                                </td>

                                                            </tr>

                                                        </tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        </div>
                                        <div className=' md:flex flex-row justify-between items-center'>
                                            <div className='flex items-center md:mb-0 mb-4 justify-center'>
                                                <p className="text-gray-500 text-sm mt-5">Showing 1 to 10 of 100 entries</p>


                                            </div>
                                            <div>
                                                <nav className="flex justify-between items-center" aria-label="Page navigation example pagination">
                                                    <ul className="flex justify-end items-center space-x-2">
                                                        <li className="page-item disabled"></li>
                                                        <li>
                                                            <FaAnglesLeft />
                                                        </li>
                                                        <li className="page-item">
                                                            <p className="page-link onecount border border-gray-300 rounded px-3 py-1 text-gray-700">1</p>
                                                        </li>
                                                        <li className="page-item">
                                                            <p className="page-link count bg-red-700 text-white rounded px-3 py-1">2</p>
                                                        </li>
                                                        <li className="page-item">
                                                            <p className="page-link count border border-gray-300 rounded px-3 py-1 text-gray-700">3</p>
                                                        </li>
                                                        <li className="page-item">
                                                            <p className="page-link count border border-gray-300 rounded px-3 py-1 text-gray-700">4<i></i></p>
                                                        </li>
                                                        <li className="page-item">
                                                            <p className="page-link count border border-gray-300 rounded px-3 py-1 text-gray-700">5<i></i></p>
                                                        </li>
                                                        <li>
                                                            <FaAnglesRight />
                                                        </li>
                                                    </ul>
                                                </nav>


                                            </div>


                                        </div>
                                    </div>

                                </div>







                            </div>


                        </div>


                    </div>






                </div>

            </div>

        </div>
    )
}

export default Account
