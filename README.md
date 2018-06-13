# Oak

This project's goal is to offer an easy and seamless way to create ocarina tutorials. 
At present it includes only video tutorials and in the future will include PDFs. 
It is customisable while still allowing the process to be quick and automating most of the time-consuming tasks.

### Progress:
  - [x] Implement a command-line interface.
  - [x] Create video tutorial from midi and audio file input.
  - [x] Map midi notes to ocarina sprites.
  - [x] Create video frames from stitched together images.
  - [x] Allow custom backgrounds.
  - [x] Implement a preview panel showing a preview of the next video frame.
  - [x] Customise colors of ocarina sprites including on/off notes and preview note.
  - [x] Customise title and title color.
  - [x] Adjustable framerate and audio offset (should rarely need to change these).
  - [x] Progress bar for commandline as it can take a few minutes for a basic song.
  - [ ] ~~Allow custom frames to be added before and after the main tutorial (e.g. you can include an intro image linking to your website etc.)~~
  - [ ] Option to output tutorial in PDF form.
  - [ ] ~~Option to output sheet music with tutorial as PDF.~~
  - [x] Separate audio file now optional as audio wav file can be generated and used with an ocarina soundfont.
  - [x] Create a simple GUI as an alternative to commandline, this will be similar to a 'wizard' simply setting up a project.
  - [ ] Include a sample preview of what a frame will look like before the process begines.
  - [x] Handle polyphonic midi files. Ocarinas mostly play a single note at a time and this program is created only to process midi playing a single note at a time.
  - [ ] Include other ocarinas and give the user options of which tutotials to create for.
  - [x] Now that there is not need to include audio, options for transposing midi files to fit an ocarinas range is possible.
  - [ ] Create robust error handling and logging.
  - [ ] The sound only synchronises accurately at a high framerate (180fps). Figuring a way to lower this to 60 would dramatically increase performance.
  
#### Challenges I faced and my learning progress
 
As this is one of my first projects I wanted to include things I learnt along the way as when I began this project I knew very little
of what I needed to know to accomplish it.

- Figuring out how to layer the ocarina sprites on the background and adding color. For this I needed to work with algorithms such as flood fill which could make the areas around the sprites transparent for when I set them on top of the background. I also used the same algorithm to color the sprites. I had to avoid recursion as it was causing a stack overflow and used a queue instead.

- Learning about midi files took quite a bit of time as I had to learn how notes are determined, their duration etc. This journey into midi files also meant learning about banks and instruments to include the custom soundfont. Midi had a number of 'quirks' I had to learn e.g. midi message 'note on' is actually a 'note off' if the velocity is 0.

- My first attempt to create the video from frames was to save the files in a temporary folder then have FFMPEG read them in order. The performance was very poor with the method and through a lot of reading and playing around with it I was able to skip that part and pipe them to FFMPEG directly which dramatically improved performance and the output process taught me a lot about muxing, FFMPEG and video processing. 

- I learnt many lessons about image processing, including different image formats, alpha channels, resizing, combining images and editing images as the frames are all processed based on the custom options and midi notes. 
