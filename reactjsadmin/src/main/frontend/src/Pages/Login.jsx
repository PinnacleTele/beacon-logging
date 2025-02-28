import React, { useState } from 'react';
import logo from '../assets/logo login.svg';
import { FaEye, FaEyeSlash } from 'react-icons/fa';
import women from '../assets/women.png';
import thunderbolt from '../assets/thunderbolt.png';
import axios from '../axios'
import { useNavigate } from 'react-router-dom';
// import { toast } from 'react-toastify';
// import { ToastContainer } from 'react-toastify';
// import 'react-toastify/dist/ReactToastify.css';
import { FaLongArrowAltRight } from "react-icons/fa";




function Login() {
  const navigate = useNavigate();

  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  console.log(email)
  console.log(password)



  const handleSubmit = async (e) => {
    e.preventDefault();

    const user = {
      username: email,
      password: password,

    };
    try {
      const response = await axios.post(
        '/auth/authenticate',
        user,
        {
          headers: {
            'Content-Type': 'application/json',
          },
          // withCredentials: true,
        }
      );
      if (response.status === 200) {
        localStorage.clear();
        localStorage.setItem('access_token', response.data.token
        );

        localStorage.setItem('refresh_token', response.data.refreshToken);
        localStorage.setItem('username', response.data.username);

        console.log(response.status, "test data")
        navigate('/account'); 

      }
      console.log(response)


  


    } catch (error) {
      if (error.response) {

        // If the server returns an error response
        if (error.response.status === 401) {
            console.log("User name or password do not match")
          // Unauthorized (user name or password not matched)
        //   toast.error('User name or password do not match.');
        } else {
            console.log("An error occurred during login. Please try again later")
          // Other server errors
        //   toast.error('An error occurred during login. Please try again later.');
        }
      } else {
        // Network error or other client-side error
        console.log('An error occurred. Please check your network connection.');
      }


      // Handle any errors here
      console.error(error);
    }
  };


  return (
    <>

      <div className='md:bg-[#080F19] flex justify-center items-center md:min-h-screen h-screen'>
        <div className='bg-bg-3 bg-cover bg-no-repeat w-full h-full flex justify-center items-center'>

          <div style={{ position: 'absolute', top: 0, left: '50%', transform: 'translateX(-50%)' }}>
            
          </div>
          <div class="md:flex mb-4  md:w-[1224px] w-full md:h-[576px] h-full bg-white justify-end rounded-[30px] overflow-hidden " >
            <div class="md:w-1/2 md:pt-0 pt-36 flex justify-center items-center ">
              <div className='md:w-[479px] md:h-[420px] bg-white '>
                <div className=' md:h-[100px]'>
                  <img className='md:h-[100px]' src={logo} alt="" />
                </div>
                <div className='mb-2 flex justify-start'>
                  <p className='font-poppins'>Enter your username and password</p>
                </div>
                <div className='mb-4 flex justify-start'>
                  <h1 className='text-4xl font-bold font-poppins'>Log in</h1>
                </div>
                <div className=''>
                  <div className="bg-white   flex flex-col">
                    <div className="mb-4 ">
                      <label className=" text-grey-darker px-1 text-sm  mb-2 flex justify-start" htmlFor="username">
                        Username
                      </label>
                      <input className={`appearance-none  bg-[#C0DBEA] rounded w-full py-2 px-3 text-grey-darker ${email ? 'bg-[#C0DBEA]' : ''
                        }`} id="username" type="text" placeholder="Enter Email" onChange={(e) => setEmail(e.target.value)}
                      />
                    </div>
                    <div className="py-2 mb-[40px]">
                      <div className='flex justify-between'>
                        <span className="px-1 text-sm mb-2">Password</span>
                        <span className="px-1 text-sm text-[#A10039]">Forgot Password ?</span>



                      </div>
                      <div className="relative text-grey-darker ">
                        <input
                          placeholder="Enter your password"
                          type={showPassword ? 'text' : 'password'}
                          className={`appearance-none  bg-[#C0DBEA] rounded w-full py-2 px-3 text-grey-darker ${password ? 'bg-[#C0DBEA]' : ''
                            }`}
                          onChange={(e) => setPassword(e.target.value)}


                        />
                        <div className="absolute inset-y-0 right-0 pr-3 flex items-center text-sm leading-5">
                          {showPassword ? (
                            <FaEyeSlash
                              className="h-6 text-[#A10039] cursor-pointer"
                              onClick={() => setShowPassword(!showPassword)}
                            />
                          ) : (
                            <FaEye
                              className="h-6 text-[#A10039] cursor-pointer"
                              onClick={() => setShowPassword(!showPassword)}
                            />
                          )}
                        </div>
                      </div>
                    </div>
                    <div className=' rounded-2xl h-[40px] flex justify-center text-white' onClick={handleSubmit} style={{ background: 'transparent linear-gradient(127deg, #1169B4 0%, #A00039 100%) 0% 0% no-repeat padding-box' }}>
                      <button className=' flex justify-center items-center'>
                        <div className='pr-[10px]'>
                          Login
                        </div>
                        <div>
                          <FaLongArrowAltRight />
                        </div>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="hidden md:w-1/2  overflow-hidden   rounded-l-[40px] md:flex justify-center items-center" style={{ background: 'transparent linear-gradient(127deg, #A00039 0%, #1169B4 100%) 0% 0% no-repeat padding-box' }} >
              <div className='bg-bg-2 bg-cover bg-no-repeat w-full h-full flex justify-center items-center py-[119px] px-[138px]'>
                <div className='md:relative  md:h-[437px] md:w-[547px] bg-white bg-opacity-30  rounded-[40px]'>
                  <div className=''>
                    <img className='md:absolute md:left-[55px]' src={women} alt="" />


                  </div>
                  <div className='md:absolute md:left-[-30px] pt-[54px] text-white'>
                    <h1 className='text-3xl font-bold'>Welcome </h1>
                    <h1 className='text-3xl font-bold'>to Campaign </h1>
                    <h1 className='text-3xl font-bold'>Manager!</h1>
                  </div>
                  <div className='md:absolute md:left-[-30px] pt-[254px] text-white'>
                    <div className='md:relative bg-white w-[79px] h-[79px] rounded-full flex justify-center items-center'>
                      <img className='w-[35px] h-[35px]' src={thunderbolt} alt="" />

                    </div>

                  </div>


                </div>

              </div>

            </div>
          </div>
        </div>
      </div>

    </>
  );
}

export default Login;
