import React from 'react'
import Navbar from '../components/Navbar'
import Sidebar from '../components/Sidebar'

function Groups() {
  return (
    <div>
        <Navbar pageName="Groups"/>
        <div className='flex w-auto '>
          <Sidebar/>
            
          </div>

      
    </div>
  )
}

export default Groups
