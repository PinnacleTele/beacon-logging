import os

def find_start_application(directory):
    print(f"Starting search in directory: {directory}")
    
    # Use os.walk to traverse all directories and subdirectories
    for root, dirs, files in os.walk(directory):
        # Check if 'StartApplication.java' exists in the current directory
        if 'StartApplication.java' in files:
            file_path = os.path.join(root, 'StartApplication.java')
            print(f"Found StartApplication.java: {file_path}")

# Determine the directory where the script is located
script_directory = os.path.dirname(os.path.abspath(__file__))

find_start_application(script_directory)
