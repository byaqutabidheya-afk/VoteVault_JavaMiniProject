#### Compile                                                            PowerShell:                                                                                  
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName   

#### Run Application                                                                                
java -cp out voting.Main

#### Run Tests                                                                                    
java -cp out voting.test.TestRunner     

### Step 1: Clean Start & Launch                                                         
                                                                                           
  Open PowerShell in D:\VoteVault_JavaMiniProject and run:                                 
                                                                                           
    # 1. Clean previous data and build folder for a fresh demo:                            
    if (Test-Path data) { Remove-Item -Recurse -Force data }                               
    if (Test-Path out)  { Remove-Item -Recurse -Force out }                                
                                                                                           
    # 2. Compile all files using plain javac:                                              
    javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName                      
                                                                                           
    # 3. Launch the application:                                                           
    java -cp out voting.Main                                                               