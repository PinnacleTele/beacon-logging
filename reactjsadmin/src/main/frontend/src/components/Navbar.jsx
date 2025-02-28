import React, { useState } from 'react'
import logo from '../assets/logo.svg'
import technowizard from '../assets/technowizard.svg'
import wallet from '../assets/wallet.svg'
import download from '../assets/download.svg'
import browse from '../assets/browse.svg'
import Vector from '../assets/Vector.svg'
import proimage from '../assets/proimage.png'
import { IoIosArrowDown } from "react-icons/io";








function Navbar({ pageName }) {

    const [togiletwo, setTogiletwo] = useState(false);


    return (
        <>
            <div class="flex  flex-row-reverse  flex-wrap md:flex-wrap md:h-[100px] h-[50px]  " style={{ background: 'transparent linear-gradient(127deg, #A00039 0%, #1169B4 100%) 0% 0% no-repeat padding-box' }}
            >
                <div class=" md:w-[82%] w-[70%]  md:p-4  text-center text-gray-200 md:h-[100px] h-[50px] border-l-2 border-white-500" >
                    <div className='hidden  md:flex md:flex-row flex-wrap pl-[36px]  justify-between'>
                        <div className='flex justify-center items-center '>
                            <h1 className='text-2xl font-bold text-white'>{pageName}</h1>

                        </div>
                        <div className=' flex md:flex-row flex-wrap ustify-center items-center pr-[40px]'>

                            <div className=' w-[200px] flex md:flex-row flex-wrap justify-between'>
                                <h3>Billing Rate</h3>
                                <h3>Reports</h3>


                            </div>
                            <div className='w-[280px] h-[70px]  flex md:flex-row items-center pl-[50px] flex-wrap justify-between'>
                                <div className='w-[48px] h-[48px] bg-white bg-opacity-50 flex justify-center items-center rounded-md '>
                                    <img src={wallet} alt="" />
                                </div>
                                <div className='w-[48px] h-[48px] bg-white bg-opacity-50 flex justify-center items-center rounded-md '>
                                    <img src={download} alt="" />
                                </div>    <div className='w-[48px] h-[48px] bg-white bg-opacity-50  flex justify-center items-center rounded-md '>
                                    <img src={browse} alt="" />
                                </div>    <div className='w-[48px] h-[48px] bg-white bg-opacity-50  flex justify-center items-center rounded-md '>
                                    <img src={Vector} alt="" />
                                </div>



                            </div>
                            <div className=' flex md:flex-row flex-wrap ustify-center items-center w-[226px] pl-[10px]'>
                                <div className='w-[60px] h-[60px] flex justify-center items-center rounded-xl'>
                                    <img src={proimage} alt="" />
                                </div>
                                <div className='  w-[146px] h-[48px] flex md:flex-row flex-wrap '>
                                    <div className=' w-[100px]'>
                                        <h6>Admin002</h6>
                                        <p>Admin</p>

                                    </div>
                                    <div className='flex justify-center  w-[46px] cursor-pointer ' onClick={() => setTogiletwo(!togiletwo)}
                                    >
                                        <IoIosArrowDown />
                                        {togiletwo &&
                                            <div className='flex flex-col absolute right-10 top-10 mt-2 bg-white shadow-md rounded-md '>
                                                <div className='px-4 py-2'>Option 1</div>
                                                <div className='px-4 py-2'>Option 2</div>
                                                <div className='px-4 py-2'>Option 3</div>
                                            </div>}

                                    </div>


                                </div>
                            </div>
                        </div>

                    </div>


                    {/* for mobile */}
                    <div className='md:hidden flex flex-row gap-5 '>
                        <div className='flex justify-start items-center '>
                            <p className='pl-2 font-poppins'>{pageName}</p>
                        </div>


                            <div className='flex flex-row  justify-start '  >
                                <div className='  font-poppins '>
                                    <h1>Admin002 </h1>
                                    <p>Admin</p>

                                </div>
                                <div className=' pl-2 pt-2 ' onClick={() => setTogiletwo(!togiletwo)}>
                                   
                                <IoIosArrowDown />
                                {togiletwo &&
                                            <div className='flex flex-col absolute right-10 top-10 mt-2 bg-white shadow-md rounded-md '>
                                                <div className='px-4 py-2'>Option 1</div>
                                                <div className='px-4 py-2'>Option 2</div>
                                                <div className='px-4 py-2'>Option 3</div>
                                            </div>}

                                </div>
                             


                            </div>

                            {/* <div className=" ">
                                <button
                                    className="w-14 h-14 relative focus:outline-none  rounded"
                                    onClick={() => setToggle(!toggle)}
                                >
                                    <div className="block w-5 absolute left-6 top-1/2 transform -translate-x-1/2 -translate-y-1/2">
                                        <span
                                            className={`block absolute h-0.5 w-7  bg-current transform transition duration-500 ease-in-out ${toggle ? "rotate-45 text-black" : " -translate-y-1.5 bg-black bg-opacity-95"
                                                }`}
                                        ></span>
                                        <span
                                            className={`block absolute h-0.5 w-7 bg-black bg-opacity-95e bg-current transform transition duration-500 ease-in-out ${toggle ? "opacity-0" : ""
                                                }`}
                                        ></span>
                                        <span
                                            className={`block absolute h-0.5 w-7 bg-current transform transition duration-500 ease-in-out ${toggle ? "-rotate-45 text-black" : "translate-y-1.5 bg-black bg-opacity-95"
                                                }`}
                                        ></span>
                                    </div>
                                </button>
                            </div> */}



                    </div>
                    {/* for mobile */}




                </div>
                <div class="md:w-[18%] w-[30%]  md:p-4 p-2 text-center md:h-[100px] h-[50px] text-gray-700 ">
                    <div className='hidden md:flex md:flex-row flex-wrap'>
                        <div className='md:pl-[1px]'>
                            <img className='md:h-[60px] md:w-[60px] h-[20px] w-[30px]' src={logo} alt="" />
                        </div>
                        <div className='pl-[0px]'>
                            <img src={technowizard} alt="" />
                        </div>

                    </div>



                    {/* for mobile */}

                    <div className='md:hidden flex flex-row justify-center items-center w-full  '>

                        <div className='pl-[10px]'>
                            <img className='h-[30px] w-[30px]' src={logo} alt="" />
                        </div>
                        <div className='pl-[11px]'>
                            <img className='h-[30px] w-[100px]' src={technowizard} alt="" />
                        </div>
                    </div>
                    {/* for mobile */}

                </div>
            </div>


            {/* for mobile togile */}
            {/* {toggle && (
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
            )} */}
            {/* for mobile togile */}






        </>


    )
}

export default Navbar