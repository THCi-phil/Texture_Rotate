Example code for quick texture rotate

2022/12/27 working but with gaps to complete
-3 of the corner cases
-the no corners in image if tree


To make this work in Eclipse

(1) Clone it to your disc

(2) Make a new workspace in Eclipse.
  - to keep you sane, call it the same name as the GitHub repository
  - but it ***can't*** be the same location as the cloned GitHub repository on your local disk from step 1

(3) File > Import > Maven -> Existing Maven Project
  - and navigate to the folder with the cloned Github repository, then just click through Finish
    with all defaults
  - NOTE: Use the File menu route (this works), not the links on the Welcome page (which will lead you down rabbit holes 
    of dialogs which won't find project files, won't allow you to just click finish, if they do complete,
    attempt to compile will give error messages which are gobbledegook to a newby just attempting to learn
    how to write a simple ImagerJ plugin)  This is super-frustrating for a beginner.
  - when the import progress bar (bottom right) has finished, close the welcome tab, Package Explorer tab will then appear
  
Then Run.As Java Application
  - will launch an imageJ instance wit this plugin running
