#### Compile                                                            PowerShell:                                                                                  
javac -d out (Get-ChildItem -Recurse -Filter *.java src).FullName   

#### Run Application                                                                                
java -cp out voting.Main

#### Run Tests                                                                                    
java -cp out voting.test.TestRunner     